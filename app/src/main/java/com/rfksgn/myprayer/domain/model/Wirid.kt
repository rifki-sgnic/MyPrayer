package com.rfksgn.myprayer.domain.model

/**
 * A reading the user keeps, ticked once a day. [surah] is null for the khatam, whose daily portion is one juz.
 * Friday readings count from Thursday evening (malam Jumat) through Friday.
 */
enum class Wirid(val surah: Int?, val label: String, val schedule: String, val friday: Boolean) {
    KHATAM(null, "Khatam, 1 juz sehari", "Setiap hari", false),
    MULK(67, "Al-Mulk", "Setiap malam", false),
    KAHF(18, "Al-Kahf", "Malam dan hari Jumat", true),
    YASIN(36, "Yasin", "Malam Jumat", true),
    WAQIAH(56, "Al-Waqi'ah", "Setiap malam", false)
}
