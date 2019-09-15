package com.ncorti.myonnaise

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import android.util.Half
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * This class help you to read the byte line from Myo.
 * Please pay attention that there are no checks for BufferOverFlow or similar problems,
 * you just receive the amount of data you request (1, 2 or 4 bytes).
 *
 * [ByteReader] is useful for handling raw data taken from bluetooth connection with Myo.
 * Use the [ByteReader.getBytes] to get an array of float from a [BluetoothGattCharacteristic].
 */
class ByteReader {

    internal var byteBuffer: ByteBuffer? = null

    var byteData: ByteArray? = null
        set(data) {
            field = data
            this.byteBuffer = ByteBuffer.wrap(field)
            byteBuffer?.order(ByteOrder.nativeOrder())
        }

    val short: Short
        get() = this.byteBuffer!!.short

    val byte: Byte
        get() = this.byteBuffer!!.get()

    val int: Int
        get() = this.byteBuffer!!.int

    fun rewind() = this.byteBuffer?.rewind()

    /**
     * Method for reading n consecutive floats, returned in a new array.
     *
     * @param size Number of bytes to be read (usually 8 or 16)
     * @return A new array with read bytes
     */
    fun getBytes(size: Int): FloatArray {
        val result = FloatArray(size)
        for (i in 0 until size)
            result[i] = byteBuffer!!.get().toFloat()
        return result
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

    fun getFloatFromHalf(size: Int): FloatArray{
        val result = FloatArray(size)
        for (i in 0 until size)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    var tmp : Half = Half(this.short)
                    if(tmp.toFloat() == Float.POSITIVE_INFINITY ) result[i]=  100000f
                    if(tmp.toFloat() == Float.NEGATIVE_INFINITY ) result[i]= -100000f
                    if(tmp.toFloat().isNaN()) result[i]= 0f
                    else result[i]=tmp.toFloat()
                }
                else{
                  //  result[i] = twoBytestoFloat(this.short.toInt())
                }
        return result
    }
}
