package de.project.lukas.utils

/**
 * Named builders for the LEGO Powered Up messages that are shared across devices, so the raw
 * protocol bytes live in one place instead of being scattered as magic numbers.
 *
 * Message shape for direct port writes:
 *   0x81 = Port Output Command, <port>, 0x11 = Startup+Completion, 0x51 = WriteDirectModeData,
 *   <mode>, <payload...>
 */
object LegoProtocol {
    /** Sets the motor speed on [port] (already mapped via [LegoHelper.mapSpeed]). */
    fun setSpeed(port: Byte, speed: Byte): ByteArray =
        byteArrayOf(0x81.toByte(), port, 0x11, 0x51, 0x00, speed)

    /** Sets the LED light brightness on [port]. */
    fun setBrightness(port: Byte, brightness: Byte): ByteArray =
        byteArrayOf(0x81.toByte(), port, 0x11, 0x51, 0x00, brightness)

    /** Sets the RGB LED colour index on [port]. */
    fun setColor(port: Byte, color: Byte): ByteArray =
        byteArrayOf(0x81.toByte(), port, 0x11, 0x51, 0x00, color)

    /** Subscribes to a port's sensor mode reports. */
    fun activatePortMode(port: Byte): ByteArray =
        byteArrayOf(0x41, port, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01)

    /** Enables battery level reports. */
    val activateBatteryReports: ByteArray = byteArrayOf(0x01, 0x06, 0x02)

    /** Enables attached-port (device) reports. */
    val activatePortReports: ByteArray = byteArrayOf(0x03, 0x00, 0x04)

    /** Hub action: switch the hub off. */
    val switchOffHub: ByteArray = byteArrayOf(0x02, 0x01)

    /** Hub action: disconnect the hub. */
    val disconnectHub: ByteArray = byteArrayOf(0x02, 0x02)
}
