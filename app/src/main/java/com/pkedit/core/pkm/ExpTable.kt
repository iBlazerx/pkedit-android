package com.pkedit.core.pkm

/**
 * Curvas de experiencia. 6 growth rates en juego:
 * 0 = Medium-Fast (n^3)
 * 1 = Erratic (fórmulas por tramos)
 * 2 = Fluctuating (fórmulas por tramos)
 * 3 = Medium-Slow (1.2*n^3 - 15*n^2 + 100*n - 140)
 * 4 = Fast (0.8*n^3)
 * 5 = Slow (1.25*n^3)
 */
object ExpTable {

    private val tables: Array<LongArray> by lazy {
        Array(6) { rate ->
            LongArray(101) { lv -> computeExp(rate, lv) }
        }
    }

    fun experienceForLevel(growthRate: Int, level: Int): Long {
        val gr = growthRate.coerceIn(0, 5)
        val lv = level.coerceIn(1, 100)
        return tables[gr][lv]
    }

    fun levelAtExperience(growthRate: Int, experience: Long): Int {
        val gr = growthRate.coerceIn(0, 5)
        val tbl = tables[gr]
        for (lv in 100 downTo 1) {
            if (experience >= tbl[lv]) return lv
        }
        return 1
    }

    private fun computeExp(rate: Int, n: Int): Long {
        if (n <= 1) return 0
        return when (rate) {
            0 -> (n.toLong() * n * n)                                 // Medium-Fast
            1 -> erratic(n)
            2 -> fluctuating(n)
            3 -> (1.2 * n * n * n - 15.0 * n * n + 100.0 * n - 140.0).toLong().coerceAtLeast(0)
            4 -> (0.8 * n * n * n).toLong()
            5 -> ((5.0 / 4.0) * n * n * n).toLong()
            else -> (n.toLong() * n * n)
        }
    }

    private fun erratic(n: Int): Long {
        val nL = n.toLong()
        return when {
            n <= 50 -> (nL * nL * nL * (100 - n)) / 50
            n <= 68 -> (nL * nL * nL * (150 - n)) / 100
            n <= 98 -> (nL * nL * nL * ((1911 - 10 * n) / 3)) / 500
            else    -> (nL * nL * nL * (160 - n)) / 100
        }
    }

    private fun fluctuating(n: Int): Long {
        val nL = n.toLong()
        return when {
            n <= 15 -> (nL * nL * nL * ((((n + 1) / 3) + 24).toLong())) / 50
            n <= 36 -> (nL * nL * nL * (14L + n)) / 50
            else    -> (nL * nL * nL * (((n / 2) + 32).toLong())) / 50
        }
    }
}
