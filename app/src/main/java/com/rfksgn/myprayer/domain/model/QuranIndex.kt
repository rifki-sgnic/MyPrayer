package com.rfksgn.myprayer.domain.model

/**
 * The mushaf's fixed structure: ayah counts per surah and where each juz starts. It never changes, so it lives
 * in code rather than in the database, which may hold only some surahs until the full download.
 *
 * A "global" ayah index runs 1..[TOTAL] through the whole mushaf; 0 means nothing read yet.
 */
object QuranIndex {
    const val TOTAL = 6236

    private val verseCounts = intArrayOf(
        7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99, 128, 111, 110, 98, 135, 112, 78, 118, 64,
        77, 227, 93, 88, 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38, 29, 18, 45, 60,
        49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18, 12, 12, 30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46,
        42, 29, 19, 36, 25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
        5, 4, 5, 6
    )

    /** offsets[n] = ayahs before surah n + 1, so surah n spans offsets[n - 1] + 1 .. offsets[n]. */
    private val offsets = IntArray(115).also { o -> for (i in 0 until 114) o[i + 1] = o[i] + verseCounts[i] }

    /** Juz j (1..30) starts at (surah, ayah). */
    private val juzStarts = listOf(
        1 to 1, 2 to 142, 2 to 253, 3 to 93, 4 to 24, 4 to 148, 5 to 83, 6 to 111, 7 to 88, 8 to 41,
        9 to 93, 11 to 6, 12 to 53, 15 to 1, 17 to 1, 18 to 75, 21 to 1, 23 to 1, 25 to 21, 27 to 56,
        29 to 46, 33 to 31, 36 to 28, 39 to 32, 41 to 47, 46 to 1, 51 to 31, 58 to 1, 67 to 1, 78 to 1
    )
    private val juzStartGlobal = IntArray(30) { globalIndex(juzStarts[it].first, juzStarts[it].second) }

    fun verseCount(surah: Int): Int = verseCounts[surah - 1]

    fun globalIndex(surah: Int, ayah: Int): Int = offsets[surah - 1] + ayah

    /** The surah's last ayah, as a global index. */
    fun surahEnd(surah: Int): Int = offsets[surah]

    fun fromGlobal(g: Int): Pair<Int, Int> {
        val index = g.coerceIn(1, TOTAL)
        var surah = 1
        while (offsets[surah] < index) surah++
        return surah to index - offsets[surah - 1]
    }

    fun juzStart(juz: Int): Pair<Int, Int> = juzStarts[juz - 1]

    fun juzStartGlobal(juz: Int): Int = juzStartGlobal[juz - 1]

    fun juzEndGlobal(juz: Int): Int = if (juz < 30) juzStartGlobal[juz] - 1 else TOTAL

    fun juzOf(g: Int): Int {
        val index = g.coerceIn(1, TOTAL)
        var juz = 1
        while (juz < 30 && juzStartGlobal[juz] <= index) juz++
        return juz
    }

    fun juzOf(surah: Int, ayah: Int): Int = juzOf(globalIndex(surah, ayah))
}
