package com.ncorti.myonnaise.myoCompoments

import android.bluetooth.*
import android.util.Log
import com.ncorti.myonnaise.*
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit

class Emg(val myo: Myo){

    private val dataProcessor: PublishProcessor<FloatArray> = PublishProcessor.create()
    private var byteReader = ByteReader()
    private var service: BluetoothGattService? = null
    private var characteristic0: BluetoothGattCharacteristic? = null
    private var characteristic1: BluetoothGattCharacteristic? = null
    private var characteristic2: BluetoothGattCharacteristic? = null
    private var characteristic3: BluetoothGattCharacteristic? = null

    /**
     * Get a [Flowable] where you can receive data from the device.
     * Data is delivered as a FloatArray of size [MYO_CHANNELS].
     * If frequency is set (!= 0) then sub-sampling is performed to achieve the desired frequency.
     */
    fun dataFlowable(): Flowable<FloatArray> {
        return if (myo.frequency == 0) {
            dataProcessor
        } else {
            dataProcessor.sample((1000 / myo.frequency).toLong(), TimeUnit.MILLISECONDS)
        }
    }

    fun findGattService(gatt: BluetoothGatt) {
        service = gatt.getService(SERVICE_EMG_DATA_ID)
        service?.apply {
            characteristic0 = service?.getCharacteristic(CHAR_EMG_0_ID)
            characteristic1 = service?.getCharacteristic(CHAR_EMG_1_ID)
            characteristic2 = service?.getCharacteristic(CHAR_EMG_2_ID)
            characteristic3 = service?.getCharacteristic(CHAR_EMG_3_ID)

            val emgCharacteristics = listOf(
                    characteristic0,
                    characteristic1,
                    characteristic2,
                    characteristic3
            )

            emgCharacteristics.forEach { emgCharacteristic ->
                emgCharacteristic?.apply {
                    if (gatt.setCharacteristicNotification(emgCharacteristic, true)) {
                        val descriptor = emgCharacteristic.getDescriptor(CHAR_CLIENT_CONFIG)
                        descriptor?.apply {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            myo.writeDescriptor(gatt, descriptor)
                        }
                    }
                }
            }
        }
    }

    internal fun putDataToDataProcessor(characteristic: BluetoothGattCharacteristic) {
        val emgData = characteristic.value
        byteReader.byteData = emgData
        Log.d(TAG, "emgData.size) "+emgData.size)
        // We receive 16 bytes of data. Let's cut them in 2 and deliver both of them.
        dataProcessor.onNext(byteReader.getBytes(EMG_ARRAY_SIZE / 2))
        dataProcessor.onNext(byteReader.getBytes(EMG_ARRAY_SIZE / 2))
    }
}