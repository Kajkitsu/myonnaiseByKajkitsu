@file:Suppress("MagicNumber")

package com.ncorti.myonnaise

import com.ncorti.myonnaise.MyoCompoments.*
import java.util.Arrays

typealias Command = ByteArray

/**
 * List of commands you can send to a [Myo] via the [Myo.sendCommand] method.
 * A [Command] is basically a [ByteArray] with all the bytes properly set.
 *
 * This is defined according to the Myo's Bluetooth specs defined here:
 * https://github.com/thalmiclabs/myo-bluetooth
 */
object CommandList {

    /** Send a vibration
     * by defult args it's "NONE" vibration comand
     * */
    fun vibration(vibrationType: VibrationType = VibrationType.NONE): Command {
        val command_vibrate = 0x03.toByte()
        val payload_vibrate = 1.toByte()
        val vibrate_type = vibrationType.value.toByte()
        return byteArrayOf(command_vibrate, payload_vibrate, vibrate_type)
    }

    /** Setup the Streaming from the device
     * by defult args it stoping all streaming
     * */
    fun setStreaming(emgMode: EmgModeType = EmgModeType.NONE, imuMode: ImuModeType = ImuModeType.NONE, classMode: ClassifierMode = ClassifierMode.DISABLE ): Command {
        val command_data = 0x01.toByte()
        val payload_data = 3.toByte()
        val emg_mode = emgMode.value.toByte()
        val imu_mode = imuMode.value.toByte()
        val class_mode = classMode.value.toByte()
        return byteArrayOf(command_data, payload_data, emg_mode, imu_mode, class_mode)
    }

    /** Set up a sleep mode
     * by defult args it's normal sleep mode
     * */
    fun sleepMode(sleepMode: SleppMode = SleppMode.NORMAL) : Command {
        val command_sleep_mode = 0x09.toByte()
        val payload_unlock = 1.toByte()
        val sleep_mode = sleepMode.value.toByte()
        return byteArrayOf(command_sleep_mode, payload_unlock, sleep_mode)
    }
}

/** Extension function to check the [Command] is a generic "start streaming" command. */
fun Command.isStartStreamingCommand() =
    this.size >= 4 &&
        this[0] == 0x01.toByte() &&
        (this[2] != 0x00.toByte() || this[3] != 0x00.toByte() || this[4] != 0x00.toByte())

/** Extension function to check the [Command] is a stop streaming command */
fun Command.isStopStreamingCommand() = Arrays.equals(this, CommandList.setStreaming(emgMode = EmgModeType.NONE,imuMode = ImuModeType.NONE,classMode = ClassifierMode.DISABLE))
