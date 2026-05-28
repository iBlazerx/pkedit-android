package com.pkedit.core.save

import com.pkedit.core.util.readU32LE
import com.pkedit.core.util.writeU32LE

/**
 * Mochila de USUM.
 *
 * Estructura según PKHeX (InventoryPouch7.cs):
 *   Cada slot = u32 LE:
 *     - bits  0..15: Item Index (ID del objeto)
 *     - bits 16..25: Count (10 bits, max 1023)
 *     - bits 26..29: FreeSpaceIndex (4 bits)
 *     - bit  30   : reserved
 *     - bit  31   : New flag
 *
 * En USUM la mochila NO está cifrada; los slots son legibles directamente.
 * ⚠️ NOTA: ciertos saves muestran valores aparentemente cifrados. Esto puede
 * ser porque el bloque MyItem está parcialmente codificado en algunas
 * revisiones del juego. Si los items se muestran como basura,
 * activar [SaveData.bagSeemsScrambled] (próxima versión).
 */
data class BagItem(
    var id: Int = 0,
    var count: Int = 0,
    var isNew: Boolean = false,
    var freeSpaceIndex: Int = 0,
) {
    val isEmpty: Boolean get() = id == 0 && count == 0
}

class Bag(
    private val data: ByteArray,
    private val bagBaseOffset: Int,
    val pouches: List<PouchInfo>,
) {

    /** Lee todos los slots NO vacíos de un pouch. */
    fun read(pouch: PouchInfo): MutableList<BagItem> {
        val out = mutableListOf<BagItem>()
        val base = bagBaseOffset + pouch.offset
        for (i in 0 until pouch.maxSlots) {
            val pos = base + i * 4
            val raw = data.readU32LE(pos)
            if (raw == 0L) continue
            val item = decodeItem(raw)
            if (item.isEmpty) continue
            out.add(item)
        }
        return out
    }

    fun write(pouch: PouchInfo, items: List<BagItem>) {
        val base = bagBaseOffset + pouch.offset
        val capped = items.take(pouch.maxSlots)
        for (i in 0 until pouch.maxSlots) {
            val pos = base + i * 4
            if (i < capped.size) {
                val it = capped[i]
                val packed = encodeItem(it, pouch.maxCount)
                data.writeU32LE(pos, packed)
            } else {
                data.writeU32LE(pos, 0L)
            }
        }
    }

    companion object {
        fun decodeItem(raw: Long): BagItem {
            val id = (raw and 0xFFFFL).toInt()
            val count = ((raw shr 16) and 0x3FFL).toInt()
            val freeSpaceIndex = ((raw shr 26) and 0xFL).toInt()
            val isNew = ((raw shr 31) and 1L) != 0L
            return BagItem(id, count, isNew, freeSpaceIndex)
        }

        fun encodeItem(item: BagItem, maxCount: Int): Long {
            val id = item.id.coerceIn(0, 0xFFFF).toLong()
            val cnt = item.count.coerceIn(0, minOf(maxCount, 0x3FF)).toLong()
            val fs = item.freeSpaceIndex.coerceIn(0, 0xF).toLong()
            val new = if (item.isNew) 1L else 0L
            return id or (cnt shl 16) or (fs shl 26) or (new shl 31)
        }
    }
}

/** Mantenemos el enum por compatibilidad pero ahora cada pouch viene de PouchInfo. */
enum class Pouch(val displayName: String) {
    ITEMS("Objetos"),
    KEY_ITEMS("Objetos clave"),
    TMS_HMS("MTs/MOs"),
    MEDICINE("Medicinas"),
    BERRIES("Bayas"),
    Z_CRYSTALS("Cristales Z"),
    ROTO_LOTO("Battle Items");

    companion object {
        fun fromUsumIndex(i: Int): Pouch = values()[i.coerceIn(0, values().size - 1)]
    }
}
