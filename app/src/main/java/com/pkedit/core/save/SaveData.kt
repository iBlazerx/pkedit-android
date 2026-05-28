package com.pkedit.core.save

import com.pkedit.core.pkm.Pk7
import com.pkedit.core.pkm.Pk7Crypto
import com.pkedit.core.util.Crc16Ccitt
import com.pkedit.core.util.readU16LE
import com.pkedit.core.util.slice
import com.pkedit.core.util.writeU16LE

/**
 * SaveData de USUM.
 *
 * Estructura del archivo `main` (0x6CC00 bytes):
 *   0x00000..0x6CA00 : 39 bloques de datos
 *   0x6CA00..0x6CA18 : header de metadata
 *   0x6CA18..      : tabla de bloques (8 bytes/entrada: u16 id, u16 crc, u32 length)
 *
 * Verificado: descifrado party correcto, CRC16Invert correcto, exportar
 * sin cambios produce save byte-a-byte idéntico al original.
 */
class SaveData(
    val raw: ByteArray,
    val version: GameVersion,
) {
    private val blocks: List<BlockInfo> = when (version) {
        GameVersion.SM -> Sm.BLOCKS
        GameVersion.USUM -> Usum.BLOCKS
    }
    private val partyOffset: Int = when (version) {
        GameVersion.SM -> Sm.PARTY_OFFSET
        GameVersion.USUM -> Usum.PARTY_OFFSET
    }
    private val bagOffset: Int = when (version) {
        GameVersion.SM -> Sm.BAG_OFFSET
        GameVersion.USUM -> Usum.BAG_OFFSET
    }
    private val checksumTableOffset: Int = when (version) {
        GameVersion.SM -> Sm.SAVE_SIZE - 0x200
        GameVersion.USUM -> Usum.CHECKSUM_TABLE_OFFSET
    }

    private val checksumEntriesOffset: Int get() = checksumTableOffset + 0x18

    val bag: Bag = Bag(
        data = raw,
        bagBaseOffset = bagOffset,
        pouches = if (version == GameVersion.USUM) Usum.POUCHES_USUM else emptyList(),
    )

    /**
     * Lee los 6 slots del party. Cada slot ocupa 260 bytes (232 de PK7 + 28 de
     * party stats que el juego recalcula). Aquí leemos solo los 232 principales.
     */
    fun readParty(): List<Pk7?> {
        val out = mutableListOf<Pk7?>()
        for (slot in 0 until 6) {
            val offset = partyOffset + slot * Pk7Crypto.PK7_PARTY_SIZE
            if (offset + Pk7Crypto.PK7_SIZE > raw.size) {
                out.add(null); continue
            }
            val bytes = raw.slice(offset, Pk7Crypto.PK7_SIZE)  // SOLO 232 bytes
            if (Pk7Crypto.isEmpty(bytes)) {
                out.add(null); continue
            }
            val ok = Pk7Crypto.decrypt(bytes)
            out.add(if (ok) Pk7(bytes) else null)
        }
        return out
    }

    /**
     * Escribe un slot del party (solo 232 bytes; los 28 de party stats se
     * dejan intactos para que el juego los recalcule al cargar).
     */
    fun writePartySlot(slot: Int, pk: Pk7?) {
        require(slot in 0..5)
        val offset = partyOffset + slot * Pk7Crypto.PK7_PARTY_SIZE
        if (pk == null) {
            // Limpiar el slot entero (232 + 28 party stats)
            for (i in 0 until Pk7Crypto.PK7_PARTY_SIZE) raw[offset + i] = 0
            return
        }
        // Trabajamos solo con los 232 bytes principales
        val pkBytes = if (pk.raw.size == Pk7Crypto.PK7_SIZE) {
            pk.raw.copyOf()
        } else {
            // Si vino como PK7_PARTY_SIZE, tomamos los primeros 232
            pk.raw.copyOf(Pk7Crypto.PK7_SIZE)
        }
        Pk7Crypto.encrypt(pkBytes)
        // SOLO sobrescribir los primeros 232 bytes del slot.
        // Los 28 bytes de stats al final NO se tocan.
        System.arraycopy(pkBytes, 0, raw, offset, Pk7Crypto.PK7_SIZE)
    }

    /**
     * Recalcula los CRC16Invert de cada bloque y los escribe en la metadata.
     * Algoritmo CRC16 con tabla precalculada portado de PKHeX.
     */
    fun export(): ByteArray {
        if (version != GameVersion.USUM) {
            error("Export solo soportado para USUM en esta versión")
        }
        val out = raw.copyOf()
        for ((index, block) in blocks.withIndex()) {
            val crc = Crc16Ccitt.compute(out, block.offset, block.length)
            val entryOffset = checksumEntriesOffset + index * 8
            // Layout entry: u16 id (+0), u16 crc (+2), u32 length (+4)
            out.writeU16LE(entryOffset + 2, crc)
        }
        return out
    }

    /** Devuelve los IDs de bloques con checksum NO válido. */
    fun verifyChecksums(): List<Int> {
        if (version != GameVersion.USUM) return emptyList()
        val bad = mutableListOf<Int>()
        for ((index, block) in blocks.withIndex()) {
            val stored = raw.readU16LE(checksumEntriesOffset + index * 8 + 2)
            val actual = Crc16Ccitt.compute(raw, block.offset, block.length)
            if (stored != actual) bad.add(block.id)
        }
        return bad
    }

    companion object {
        fun load(bytes: ByteArray): SaveData? {
            val v = GameVersion.detect(bytes) ?: return null
            return SaveData(bytes.copyOf(), v)
        }
    }
}
