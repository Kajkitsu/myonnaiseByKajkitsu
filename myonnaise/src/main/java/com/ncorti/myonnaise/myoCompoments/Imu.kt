package com.ncorti.myonnaise.myoCompoments

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.util.Log
import com.ncorti.myonnaise.*
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit


class Imu(val myo: Myo){
    private val dataProcessor: PublishProcessor<FloatArray> = PublishProcessor.create()
    private var byteReaderImu = ByteReader()
    private var serviceImu: BluetoothGattService? = null
    private var characteristicImu0: BluetoothGattCharacteristic? = null

    fun dataFlowable(): Flowable<FloatArray> {
        return if (myo.frequency == 0) {
            dataProcessor
        } else {
            dataProcessor.sample((1000 / myo.frequency).toLong(), TimeUnit.MILLISECONDS)
        }
    }

    internal fun findGattService(gatt: BluetoothGatt) {
        serviceImu = gatt.getService(SERVICE_IMU_DATA_ID)
        serviceImu?.apply {
            characteristicImu0 = serviceImu?.getCharacteristic(CHAR_IMU_DATA_ID)

            val imuCharacteristics = listOf(
                    characteristicImu0
            )

            imuCharacteristics.forEach { imuCharacteristic ->
                imuCharacteristic?.apply {
                    if (gatt.setCharacteristicNotification(imuCharacteristic, true)) {
                        val descriptor = imuCharacteristic.getDescriptor(CHAR_CLIENT_CONFIG)
                        descriptor?.apply {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            myo.writeDescriptor(gatt, descriptor)
                        }
                    }
                }
                Log.d(TAG, "imuCharacteristic?.apply")
            }
        }
    }

    fun twoBytestoFloat(hbits:Int):Float {
        var mant = hbits and 0x03ff
        var exp = hbits and 0x7c00
        if (exp == 0x7c00)
            exp = 0x3fc00
        else if (exp != 0)
        {
            exp += 0x1c000
            if (mant == 0 && exp > 0x1c400)
                return java.lang.Float.intBitsToFloat(hbits and 0x8000 shl 16 or (exp shl 13) or 0x3ff)
        }
        else if (mant != 0)
        {
            exp = 0x1c400
            do
            {
                mant = mant shl 1
                exp -= 0x400
            }
            while (mant and 0x400 == 0)
            mant = mant and 0x3ff
        }
        return java.lang.Float.intBitsToFloat(hbits and 0x8000 shl 16 or (exp or mant shl 13))
    }

    var MYOHW_ORIENTATION_SCALE = 16384.0f ///< See myohw_imu_data_t::orientation
    var MYOHW_ACCELEROMETER_SCALE = 2048.0f  ///< See myohw_imu_data_t::accelerometer
    var MYOHW_GYROSCOPE_SCALE = 16.0f    ///< See myohw_imu_data_t::gyroscope
    fun putDataToDataProcessor(characteristic: BluetoothGattCharacteristic) {
        val imuData = characteristic.value
        byteReaderImu.byteData = imuData
        Log.d(TAG, "imuData.size) "+imuData.size)
        // ignores the higher 16 bits
        var floatArrayData : FloatArray = FloatArray(10)

        for(i in 0..9){
            if(i%2==0)
                floatArrayData[i] = twoBytestoFloat (
                    (byteReaderImu.byte.toInt() and 0xFF shl 8)
                            or (byteReaderImu.byte.toInt() and 0xFF shl 0))
            else
                floatArrayData[i] = twoBytestoFloat (
                        (byteReaderImu.byte.toInt() and 0xFF shl 0)
                                or (byteReaderImu.byte.toInt() and 0xFF shl 8))

            when (i) {
                in 0..3 -> floatArrayData[i] *= MYOHW_ORIENTATION_SCALE
                in 4..6 -> floatArrayData[i] *= MYOHW_ACCELEROMETER_SCALE
                in 7..9 -> floatArrayData[i] *= MYOHW_GYROSCOPE_SCALE
            }
        }
        dataProcessor.onNext(floatArrayData)

    }

}