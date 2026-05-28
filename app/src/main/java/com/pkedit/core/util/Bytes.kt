package com.pkedit.core.util

/**
 * Lectura/escritura little-endian sobre ByteArray.
 * Todos los formatos de save y de Pokémon en Gen 6/7 son LE.
 */

fun ByteArray.readU8(offset: Int): Int = this[offset].toInt() and 0xFF

fun ByteArray.readU16LE(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or
    ((this[offset + 1].toInt() and 0xFF) shl 8)

fun ByteArray.readU32LE(offset: Int): Long =
    (this[offset].toLong() and 0xFF) or
    ((this[offset + 1].toLong() and 0xFF) shl 8) or
    ((this[offset + 2].toLong() and 0xFF) shl 16) or
    ((this[offset + 3].toLong() and 0xFF) shl 24)

fun ByteArray.readU64LE(offset: Int): Long {
    var result = 0L
    for (i in 0..7) {
        result = result or ((this[offset + i].toLong() and 0xFF) shl (i * 8))
    }
    return result
}

fun ByteArray.writeU8(offset: Int, value: Int) {
    this[offset] = (value and 0xFF).toByte()
}

fun ByteArray.writeU16LE(offset: Int, value: Int) {
    this[offset] = (value and 0xFF).toByte()
    this[offset + 1] = ((value ushr 8) and 0xFF).toByte()
}

fun ByteArray.writeU32LE(offset: Int, value: Long) {
    this[offset] = (value and 0xFF).toByte()
    this[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    this[offset + 2] = ((value ushr 16) and 0xFF).toByte()
    this[offset + 3] = ((value ushr 24) and 0xFF).toByte()
}

fun ByteArray.writeU64LE(offset: Int, value: Long) {
    for (i in 0..7) {
        this[offset + i] = ((value ushr (i * 8)) and 0xFF).toByte()
    }
}

/** Copia un subarray sin perder bytes signados */
fun ByteArray.slice(offset: Int, length: Int): ByteArray {
    val out = ByteArray(length)
    System.arraycopy(this, offset, out, 0, length)
    return out
}

/** Sustituye un bloque dentro de this por src */
fun ByteArray.replaceFrom(offset: Int, src: ByteArray, srcOffset: Int = 0, length: Int = src.size) {
    System.arraycopy(src, srcOffset, this, offset, length)
}
