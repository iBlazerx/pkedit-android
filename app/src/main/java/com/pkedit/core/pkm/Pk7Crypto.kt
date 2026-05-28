package com.pkedit.core.pkm

import com.pkedit.core.util.readU16LE
import com.pkedit.core.util.readU32LE
import com.pkedit.core.util.writeU16LE

/**
 * Cifrado de Pokémon en Gen 6/7.
 *
 * Un PK7 ocupa 232 bytes (PK7_SIZE). En party hay 28 bytes adicionales con stats
 * derivadas (level/HP/etc.) que el juego recalcula al cargar — no se tocan al editar.
 *
 * Estructura PK7:
 *   0x00..0x07: cabecera sin cifrar (encryptionConstant, sanity, checksum)
 *   0x08..0xE7: 4 bloques (A,B,C,D) de 56 bytes cifrados y barajados según ec
 *
 * Cifrado:
 *   - PRNG: x_{n+1} = x_n * 0x41C64E6D + 0x6073 (mod 2^32), seed = ec
 *   - Cada u16 se XORea con (seed >> 16) tras avanzar el PRNG
 *
 * Shuffle: orden = ((ec >> 13) & 31) % 24, índice en BLOCK_POSITIONS.
 */
object Pk7Crypto {

    const val PK7_SIZE = 232
    const val PK7_PARTY_SIZE = 260
    const val BLOCK_SIZE = 56
    const val ENCRYPTED_START = 8
    const val ENCRYPTED_LEN = 224

    // Permutación A,B,C,D según shift. Igual en Gen 4-7.
    private val BLOCK_POSITIONS = intArrayOf(
        0,1,2,3, 0,1,3,2, 0,2,1,3, 0,3,1,2, 0,2,3,1, 0,3,2,1,
        1,0,2,3, 1,0,3,2, 2,0,1,3, 3,0,1,2, 2,0,3,1, 3,0,2,1,
        1,2,0,3, 1,3,0,2, 2,1,0,3, 3,1,0,2, 2,3,0,1, 3,2,0,1,
        1,2,3,0, 1,3,2,0, 2,1,3,0, 3,1,2,0, 2,3,1,0, 3,2,1,0,
    )

    private val BLOCK_POSITIONS_INVERSE: IntArray = IntArray(96).also { inv ->
        for (shift in 0 until 24) {
            for (pos in 0 until 4) {
                val block = BLOCK_POSITIONS[shift * 4 + pos]
                inv[shift * 4 + block] = pos
            }
        }
    }

    private fun advance(seed: Long): Long =
        (seed * 0x41C64E6DL + 0x6073L) and 0xFFFFFFFFL

    private fun cryptArray(data: ByteArray, start: Int, length: Int, initialSeed: Long) {
        var seed = initialSeed
        var i = 0
        while (i < length) {
            seed = advance(seed)
            val key = ((seed shr 16) and 0xFFFF).toInt()
            val pos = start + i
            val cur = data.readU16LE(pos)
            data.writeU16LE(pos, cur xor key)
            i += 2
        }
    }

    /** Reordena los 4 bloques de 56 bytes según el shift derivado de ec. */
    private fun shuffleBlocks(data: ByteArray, ec: Long, toUnshuffled: Boolean) {
        val shift = ((ec shr 13) and 0x1F).toInt() % 24
        val perm = if (toUnshuffled) {
            intArrayOf(
                BLOCK_POSITIONS[shift * 4],
                BLOCK_POSITIONS[shift * 4 + 1],
                BLOCK_POSITIONS[shift * 4 + 2],
                BLOCK_POSITIONS[shift * 4 + 3],
            )
        } else {
            intArrayOf(
                BLOCK_POSITIONS_INVERSE[shift * 4],
                BLOCK_POSITIONS_INVERSE[shift * 4 + 1],
                BLOCK_POSITIONS_INVERSE[shift * 4 + 2],
                BLOCK_POSITIONS_INVERSE[shift * 4 + 3],
            )
        }

        val tmp = ByteArray(ENCRYPTED_LEN)
        for (i in 0..3) {
            val src = ENCRYPTED_START + perm[i] * BLOCK_SIZE
            System.arraycopy(data, src, tmp, i * BLOCK_SIZE, BLOCK_SIZE)
        }
        System.arraycopy(tmp, 0, data, ENCRYPTED_START, ENCRYPTED_LEN)
    }

    /**
     * Descifra in-place SOLO los 232 bytes principales del PK7.
     * Los 28 bytes adicionales de party stats (si están presentes) se ignoran:
     * el juego los recalcula al cargar el save.
     *
     * Devuelve true si el checksum cuadra.
     */
    fun decrypt(data: ByteArray): Boolean {
        require(data.size == PK7_SIZE || data.size == PK7_PARTY_SIZE) {
            "Tamaño PK7 inválido: ${data.size}"
        }
        val ec = data.readU32LE(0)
        cryptArray(data, ENCRYPTED_START, ENCRYPTED_LEN, ec)
        shuffleBlocks(data, ec, toUnshuffled = true)
        return verifyChecksum(data)
    }

    /**
     * Cifra in-place los 232 bytes principales del PK7.
     * Si el array tiene 260 bytes (party), los últimos 28 NO se tocan
     * (deben dejarse en el estado del juego original).
     */
    fun encrypt(data: ByteArray) {
        require(data.size == PK7_SIZE || data.size == PK7_PARTY_SIZE)
        val ec = data.readU32LE(0)
        val checksum = computeChecksum(data)
        data.writeU16LE(0x06, checksum)
        shuffleBlocks(data, ec, toUnshuffled = false)
        cryptArray(data, ENCRYPTED_START, ENCRYPTED_LEN, ec)
    }

    /** Checksum: suma de U16 LE del payload desencriptado (orden A,B,C,D). */
    fun computeChecksum(decryptedData: ByteArray): Int {
        var chk = 0
        for (i in ENCRYPTED_START until ENCRYPTED_START + ENCRYPTED_LEN step 2) {
            chk = (chk + decryptedData.readU16LE(i)) and 0xFFFF
        }
        return chk
    }

    fun verifyChecksum(decryptedData: ByteArray): Boolean {
        val stored = decryptedData.readU16LE(0x06)
        return stored == computeChecksum(decryptedData)
    }

    fun isEmpty(data: ByteArray): Boolean = data.all { it == 0.toByte() }
}
