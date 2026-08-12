package de.project.lukas.utils

import java.util.Locale

object HexUtils {
    /** Formats a byte array as a space-separated hex string. Only used for Logcat output. */
    fun byteToHexString(bytes: ByteArray): String =
        bytes.joinToString(" ") { String.format(Locale.ROOT, "%02X", it) }
}
