package com.pkedit.core.save

/**
 * Descriptor de un bloque dentro del SaveData de Gen 7.
 *
 * Datos extraídos del código fuente real de PKHeX (SaveBlockAccessor7USUM.cs).
 * Verificados contra un save real de Ultra Sun / Ultra Moon.
 */
data class BlockInfo(
    val offset: Int,
    val length: Int,
    val id: Int,
    val name: String = "",
)

/**
 * Tabla de bloques del save de Ultra Sun / Ultra Moon.
 * Copiada de PKHeX.Core/Saves/Access/SaveBlockAccessor7USUM.cs
 *
 * La tabla de metadata está al final del save (offset SIZE - 0x200 = 0x6CA00).
 * Estructura de cada entrada en la metadata: u16 id, u16 crc, u32 length (8 bytes/entrada).
 */
object Usum {
    const val SAVE_SIZE = 0x6CC00
    const val CHECKSUM_TABLE_OFFSET = SAVE_SIZE - 0x200  // 0x6CA00

    /** Lista exacta de bloques en USUM (39 bloques). Verificada contra PKHeX. */
    val BLOCKS = listOf(
        BlockInfo(0x00000, 0x00E28,  0, "MyItem"),
        BlockInfo(0x01000, 0x0007C,  1, "Situation"),
        BlockInfo(0x01200, 0x00014,  2, "RandomGroup"),
        BlockInfo(0x01400, 0x000C0,  3, "MyStatus"),
        BlockInfo(0x01600, 0x0061C,  4, "PokePartySave"),       // <-- PARTY
        BlockInfo(0x01E00, 0x00E00,  5, "EventWork"),
        BlockInfo(0x02C00, 0x00F78,  6, "ZukanData"),
        BlockInfo(0x03C00, 0x00228,  7, "GtsData"),
        BlockInfo(0x04000, 0x0030C,  8, "UnionPokemon"),
        BlockInfo(0x04400, 0x001FC,  9, "Misc"),
        BlockInfo(0x04600, 0x0004C, 10, "FieldMenu"),
        BlockInfo(0x04800, 0x00004, 11, "ConfigSave"),
        BlockInfo(0x04A00, 0x00058, 12, "GameTime"),
        BlockInfo(0x04C00, 0x005E6, 13, "BOX"),
        BlockInfo(0x05200, 0x36600, 14, "BoxPokemon"),          // <-- PC Boxes
        BlockInfo(0x3B800, 0x0572C, 15, "ResortSave"),
        BlockInfo(0x41000, 0x00008, 16, "PlayTime"),
        BlockInfo(0x41200, 0x01218, 17, "FieldMoveModelSave"),
        BlockInfo(0x42600, 0x01A08, 18, "Fashion"),
        BlockInfo(0x44200, 0x06408, 19, "JoinFestaPersonalSave1"),
        BlockInfo(0x4A800, 0x06408, 20, "JoinFestaPersonalSave2"),
        BlockInfo(0x50E00, 0x03998, 21, "JoinFestaDataSave"),
        BlockInfo(0x54800, 0x00100, 22, "BerrySpot"),
        BlockInfo(0x54A00, 0x00100, 23, "FishingSpot"),
        BlockInfo(0x54C00, 0x10528, 24, "LiveMatchData"),
        BlockInfo(0x65200, 0x00204, 25, "BattleSpotData"),
        BlockInfo(0x65600, 0x00B60, 26, "PokeFinderSave"),
        BlockInfo(0x66200, 0x03F50, 27, "MysteryGiftSave"),
        BlockInfo(0x6A200, 0x00358, 28, "Record"),
        BlockInfo(0x6A600, 0x00728, 29, "ValidationSave"),
        BlockInfo(0x6AE00, 0x00200, 30, "GameSyncSave"),
        BlockInfo(0x6B000, 0x00718, 31, "PokeDiarySave"),
        BlockInfo(0x6B800, 0x001FC, 32, "BattleInstSave"),
        BlockInfo(0x6BA00, 0x00200, 33, "Sodateya"),
        BlockInfo(0x6BC00, 0x00120, 34, "WeatherSave"),
        BlockInfo(0x6BE00, 0x001C8, 35, "QRReaderSaveData"),
        BlockInfo(0x6C000, 0x001D0, 36, "TurtleSalmonSave"),
        BlockInfo(0x6C200, 0x0039C, 37, "BattleFesSave"),
        BlockInfo(0x6C600, 0x00400, 38, "FinderStudioSave"),
    )

    /** Party está en el bloque índice 4. */
    const val PARTY_OFFSET = 0x01600

    /** MyItem está en el bloque índice 0. */
    const val BAG_OFFSET = 0x00000

    /** Estructura interna de la mochila en USUM, dentro del bloque MyItem.
     * Copiado de PlayerBag7USUM.cs en PKHeX.
     * Cada pouch: (offset relativo dentro del bloque, num slots, max count). */
    val POUCHES_USUM = listOf(
        PouchInfo(0x000, 427, 999, "Items"),
        PouchInfo(0x6AC, 198,   1, "KeyItems"),
        PouchInfo(0x9C4, 108,   1, "TMs/HMs"),
        PouchInfo(0xB74,  60, 999, "Medicine"),
        PouchInfo(0xC64,  67, 999, "Berries"),
        PouchInfo(0xD70,  35,   1, "ZCrystals"),
        PouchInfo(0xDFC,  11, 999, "BattleItems"),
    )
}

data class PouchInfo(
    val offset: Int,
    val maxSlots: Int,
    val maxCount: Int,
    val name: String,
)

/** SM stub: por ahora solo se soporta USUM correctamente. SM aún no validado. */
object Sm {
    const val SAVE_SIZE = 0x6BE00
    val BLOCKS = emptyList<BlockInfo>()
    const val PARTY_OFFSET = 0x00000  // placeholder
    const val BAG_OFFSET = 0x00000    // placeholder
}
