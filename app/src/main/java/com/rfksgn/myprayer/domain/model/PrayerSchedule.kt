package com.rfksgn.myprayer.domain.model

data class PrayerSchedule (
    val dateFormatted: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val qiblaDirection: Double,
    val nextPrayerName: String,
    val nextPrayerTimeMillis: Long
)