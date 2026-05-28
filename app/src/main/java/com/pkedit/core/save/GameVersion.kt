package com.pkedit.core.save

/**
 * Saves soportados. En Gen 6/7 el archivo "main" tiene un tamaño fijo según el juego.
 */
enum class GameVersion(val saveSize: Int, val displayName: String) {
    SM(0x6BE00, "Sun / Moon"),
    USUM(0x6CC00, "Ultra Sun / Ultra Moon"),
    ;

    companion object {
        fun detect(data: ByteArray): GameVersion? = entries.firstOrNull { it.saveSize == data.size }
    }
}
