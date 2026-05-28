package com.pkedit.core.data

/**
 * 25 naturalezas. Los índices coinciden con el byte 0x1C de PK7.
 */
data class Nature(val index: Int, val name: String, val plus: Stat?, val minus: Stat?)

enum class Stat(val displayName: String) {
    ATK("Atq"), DEF("Def"), SPE("Vel"), SPA("AtqEsp"), SPD("DefEsp")
}

object Natures {
    val ALL: List<Nature> = listOf(
        Nature(0,  "Fuerte",     null, null),
        Nature(1,  "Audaz",      Stat.ATK, Stat.DEF),
        Nature(2,  "Firme",      Stat.ATK, Stat.SPE),
        Nature(3,  "Pícara",     Stat.ATK, Stat.SPA),
        Nature(4,  "Huraña",     Stat.ATK, Stat.SPD),
        Nature(5,  "Osada",      Stat.DEF, Stat.ATK),
        Nature(6,  "Docil",      null, null),
        Nature(7,  "Activa",     Stat.DEF, Stat.SPE),
        Nature(8,  "Mansa",      Stat.DEF, Stat.SPA),
        Nature(9,  "Agitada",    Stat.DEF, Stat.SPD),
        Nature(10, "Miedosa",    Stat.SPE, Stat.ATK),
        Nature(11, "Activa",     Stat.SPE, Stat.DEF),
        Nature(12, "Seria",      null, null),
        Nature(13, "Alegre",     Stat.SPE, Stat.SPA),
        Nature(14, "Ingenua",    Stat.SPE, Stat.SPD),
        Nature(15, "Modesta",    Stat.SPA, Stat.ATK),
        Nature(16, "Afable",     Stat.SPA, Stat.DEF),
        Nature(17, "Activa",     Stat.SPA, Stat.SPE),
        Nature(18, "Plácida",    null, null),
        Nature(19, "Cauta",      Stat.SPA, Stat.SPD),
        Nature(20, "Tímida",     Stat.SPD, Stat.ATK),
        Nature(21, "Amable",     Stat.SPD, Stat.DEF),
        Nature(22, "Alegre",     Stat.SPD, Stat.SPE),
        Nature(23, "Grosera",    Stat.SPD, Stat.SPA),
        Nature(24, "Sosegada",   null, null),
    )

    fun byIndex(i: Int): Nature = ALL.getOrNull(i) ?: ALL[0]
}
