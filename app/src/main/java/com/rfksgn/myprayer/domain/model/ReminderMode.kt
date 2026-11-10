package com.rfksgn.myprayer.domain.model

enum class ReminderMode(val label: String) {
    ADZAN("Suara Adzan"),
    VIBRATE("Getar Saja"),
    SILENT("Nonaktif");

    fun next(): ReminderMode = when (this) {
        ADZAN -> VIBRATE
        VIBRATE -> SILENT
        SILENT -> ADZAN
    }
}
