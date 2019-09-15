package com.ncorti.myonnaise.myoCompoments

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import com.ncorti.myonnaise.ByteReader
import com.ncorti.myonnaise.CommandList
import com.ncorti.myonnaise.Myo
import com.ncorti.myonnaise.MyoStatus


class ControlService(var myo :Myo){

    private var serviceControl: BluetoothGattService? = null
    private var characteristicInfo: BluetoothGattCharacteristic? = null

    fun putDataControlService(characteristic: BluetoothGattCharacteristic) {
        // Myo Device Information
        val data = characteristic.value
        if (data != null && data.isNotEmpty()) {
            val byteReader = ByteReader()
            byteReader.byteData = data
            // TODO We might expose these to the public
            val callbackMsg =
                    String.format(
                            "Serial Number     : %02x:%02x:%02x:%02x:%02x:%02x",
                            byteReader.byte, byteReader.byte, byteReader.byte,
                            byteReader.byte, byteReader.byte, byteReader.byte
                    ) +
                            '\n'.toString() + String.format("Unlock            : %d", byteReader.short) +
                            '\n'.toString() + String.format(
                            "Classifier builtin:%d active:%d (have:%d)",
                            byteReader.byte, byteReader.byte, byteReader.byte
                    ) +
                            '\n'.toString() + String.format("Stream Type       : %d", byteReader.byte)
            Log.d(TAG, "MYO info string: $callbackMsg")
        }
    }

    fun FindGattServiceServiceControl(gatt: BluetoothGatt) {
        serviceControl = gatt.getService(SERVICE_CONTROL_ID)
        serviceControl?.apply {
            characteristicInfo = this.getCharacteristic(CHAR_INFO_ID)
            characteristicInfo?.apply {
                // if there is only 1 item in the queue, then read it.  If more than 1, we handle asynchronously in the
                // callback. GIVE PRECEDENCE to descriptor writes. They must all finish first.
                myo.readQueue.add(this)
                if (myo.readQueue.size == 1 && myo.writeQueue.size == 0) {
                    gatt.readCharacteristic(this)
                }
            }
            myo.characteristicCommand = this.getCharacteristic(CHAR_COMMAND_ID)
            myo.characteristicCommand?.apply {
                myo.lastKeepAlive = System.currentTimeMillis()
                myo.sendCommand(CommandList.sleepMode(SleppMode.NEVER))
                // We send the ready event as soon as the characteristicCommand is ready.
                myo.connectionStatusSubject.onNext(MyoStatus.READY)
            }
        }
    }
}

