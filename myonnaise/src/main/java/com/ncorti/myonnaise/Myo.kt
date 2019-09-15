@file:Suppress("MagicNumber", "TooManyFunctions")

package com.ncorti.myonnaise

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.ncorti.myonnaise.myoCompoments.*
import com.ncorti.myonnaise.myoCompoments.TAG
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*


enum class MyoStatus {
    CONNECTED, CONNECTING, READY, DISCONNECTED
}

enum class MyoControlStatus {
    STREAMING, NOT_STREAMING
}

/**
 * Class that represents a Myo Armband.
 * Use this class to connecting to it, send commands, start and stop streaming.
 *
 * @param device The [BluetoothDevice] that is backing this Myo.
 */
class Myo(private val device: BluetoothDevice) : BluetoothGattCallback() {

    /** The Device Name of this Myo */
    val name: String
        get() = device.name

    var emg = Emg(this)
    var imu = Imu(this)
    var controlService = ControlService(this)

    /** The Device Address of this Myo */
    val address: String
        get() = device.address

    /** The EMG Streaming frequency. 0 to reset to the [MYO_MAX_FREQUENCY]. Allowed values [0, MYO_MAX_FREQUENCY] */
    var frequency: Int = 0
        set(value) {
            field = if (value >= MYO_MAX_FREQUENCY) 0 else value
        }

    /**
     * Keep alive flag. If set to true, the library will send a [CommandList.unSleep] command
     * to the device every [KEEP_ALIVE_INTERVAL_MS] ms.
     */
    var keepAlive = true
    var lastKeepAlive = 0L

    // Subjects for publishing outside Connection Status, Control Status and the Data (Float Arrays).
    internal val connectionStatusSubject: BehaviorSubject<MyoStatus> =
            BehaviorSubject.createDefault(MyoStatus.DISCONNECTED)
    internal val controlStatusSubject: BehaviorSubject<MyoControlStatus> =
            BehaviorSubject.createDefault(MyoControlStatus.NOT_STREAMING)
    internal var gatt: BluetoothGatt? = null



    internal var characteristicCommand: BluetoothGattCharacteristic? = null


    // We are using two queues for writing and reading characteristics/descriptors.
    // Please note that we must always give precedence to the write.
    internal val writeQueue: LinkedList<BluetoothGattDescriptor> = LinkedList()
    val readQueue: LinkedList<BluetoothGattCharacteristic> = LinkedList()

    /**
     * Use this method to connect to the device. You need to connect before start streaming
     * @param context A valid application context.
     */
    fun connect(context: Context) {
        connectionStatusSubject.onNext(MyoStatus.CONNECTING)
        gatt = device.connectGatt(context, false, this)
    }

    /**
     * Use this method to disconnect from the device. This will release all the resources.
     * Don't forget to disconnect to the device when you're done (you will drain battery otherwise).
     */
    fun disconnect() {
        gatt?.close()
        controlStatusSubject.onNext(MyoControlStatus.NOT_STREAMING)
        connectionStatusSubject.onNext(MyoStatus.DISCONNECTED)
    }

    /**
     * @return true if this object is connected to a device
     */
    fun isConnected() =
            connectionStatusSubject.value == MyoStatus.CONNECTED ||
                    connectionStatusSubject.value == MyoStatus.READY

    /**
     * @return true if the device is currently streaming
     */
    fun isStreaming() = controlStatusSubject.value == MyoControlStatus.STREAMING

    /**
     * Get an observable where you can check the current device status.
     * Register to this Observable to be notified when the device is Connected/Disconnected.
     */
    fun statusObservable(): Observable<MyoStatus> = connectionStatusSubject

    /**
     * Get an observable where you can check the current streaming status.
     * Register to this Observable to be notified when the device is Streaming/Not Streaming.
     */
    fun controlObservable(): Observable<MyoControlStatus> = controlStatusSubject


    @Suppress("NestedBlockDepth", "ComplexMethod")
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        Log.d(TAG, "onServicesDiscovered received: $status")

        if (status != BluetoothGatt.GATT_SUCCESS) {
            return
        }
        // Find GATT Service EMG
        emg.findGattService(gatt)


        // Find GATT Service IMU
        imu.findGattService(gatt)


        // Find GATT Service Control
        controlService.FindGattServiceServiceControl(gatt)
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        readQueue.remove()
        Log.d(TAG, "onCharacteristicRead status: $status ${characteristic.uuid}")

        if (CHAR_INFO_ID == characteristic.uuid) {
            controlService.putDataControlService(characteristic)
        }

        if (readQueue.size > 0)
            gatt.readCharacteristic(readQueue.element())
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        super.onCharacteristicChanged(gatt, characteristic)

        if (characteristic.uuid.toString().endsWith(CHAR_EMG_POSTFIX)) {
            emg.putDataToDataProcessor(characteristic)
        }


        if (characteristic.uuid.toString() == CHAR_IMU_DATA_ID.toString()) {
            imu.putDataToDataProcessor(characteristic)
        }

        // Finally check if keep alive makes sense.
        val currentTimeMillis = System.currentTimeMillis()
        if (keepAlive && currentTimeMillis > lastKeepAlive + KEEP_ALIVE_INTERVAL_MS) {
            lastKeepAlive = currentTimeMillis
            sendCommand(CommandList.sleepMode(SleppMode.NEVER))
        }
    }

    fun writeDescriptor(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor) {
        writeQueue.add(descriptor)
        // When writing, if the queue is empty, write immediately.
        if (writeQueue.size == 1) {
            gatt.writeDescriptor(descriptor)
        }
    }
    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        Log.d(TAG, "onDescriptorWrite status: $status")
        writeQueue.remove()
        // if there is more to write, do it!
        if (writeQueue.size > 0)
            gatt.writeDescriptor(writeQueue.element())
        else if (readQueue.size > 0)
            gatt.readCharacteristic(readQueue.element())
    }

    fun sendCommand(command: Command): Boolean {
        characteristicCommand?.apply {
            this.value = command
            if (this.properties == BluetoothGattCharacteristic.PROPERTY_WRITE) {
                if (command.isStartStreamingCommand()) {
                    controlStatusSubject.onNext(MyoControlStatus.STREAMING)
                } else if (command.isStopStreamingCommand()) {
                    controlStatusSubject.onNext(MyoControlStatus.NOT_STREAMING)
                }
                gatt?.writeCharacteristic(this)
                return true
            }
        }
        return false
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.d(TAG, "onConnectionStateChange: $status -> $newState")
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, "Bluetooth Connected")
            connectionStatusSubject.onNext(MyoStatus.CONNECTED)
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // Calling disconnect() here will cause to release the GATT resources.
            disconnect()
            Log.d(TAG, "Bluetooth Disconnected")
        }
    }


}