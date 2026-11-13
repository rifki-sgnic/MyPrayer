# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Muslims across Indonesia who pray five times a day and rely on their phone for prayer times. Their jobs: know when the next prayer is, be called to it on time by the adzan, and find the Qibla. They live in all three Indonesian time zones (WIB, WITA, WIT), not only Java.

## Product Purpose

MyPrayer is a public Play Store app that shows the daily prayer schedule for the user's location, sounds the adzan at each prayer time, and points to the Qibla. Success means the adzan fires at the right minute, every prayer, every day, on the phones Indonesians actually own.

## Positioning

A reliable adzan. Established apps (Muslim Pro, Umma, local schedule apps) are widely reported to sound the adzan late or not at all when Doze, battery savers or aggressive OEM ROMs kill them. That report is from desk research and has not been verified. MyPrayer treats on-time delivery as the core product: exact alarms, a foreground playback service, and a guided checklist for the system permissions that keep alarms alive.

## Operating Context

- Android phones in Indonesia, including Xiaomi, Oppo, Vivo and other ROMs with aggressive background restrictions. minSdk 26, targetSdk 37.
- The adzan has to play while the phone is locked or the app is in the background. The user stops it from the notification.
- Location comes from GPS, with a stored last-known location as the fallback.

## Capabilities and Constraints

Current implementation:
- Times are calculated on the device with adhan2, using Kemenag-style parameters (Fajr 20°, Isha 18°, Shafi'i Asr, +2 minute safety margin). There is no network dependency for the schedule.
- Three tabs: Jadwal (schedule, next-prayer countdown, Hijri date), Kiblat (compass), Setelan (settings).
- Per-prayer reminder mode: Suara Adzan, Getar Saja, or Nonaktif. Separate custom audio for Subuh and for the other prayers. Alarm volume control. Test buttons.
- Permission status for exact alarms, notifications and battery-optimization exemption.

Constraints:
- Indonesian only. All copy is in Bahasa Indonesia, using the local prayer names (Subuh, Dzuhur, Ashar, Maghrib, Isya). No localization is planned.
- Every Indonesian time zone must be correct. The code currently hard-codes "WIB", which is a known gap.

Undecided:
- Quran is a candidate next feature; not committed.
- Whether users can pick a calculation method or madhab, or Kemenag stays fixed.
- Business model (ads, donation, paid extras, or none). Make no "ad-free" or "free forever" claim until this is decided.

## Brand Commitments

The app name is MyPrayer (`app_name`). No logo, voice guide or other brand assets beyond the default launcher icon exist yet.

## Evidence on Hand

- Bundled adzan audio: `app/src/main/res/raw/adzan.mp3`, `app/src/main/res/raw/adzan_subuh.mp3`.
- Desk research on competitors and reference apps is in the Obsidian vault (`H:\Documents\Obsidian\Vault\prayer-vault`).
- There is no store listing, user base, reviews, testimonials or reliability metrics yet. Do not fabricate any of them.

## Product Principles

1. **The adzan comes first.** No feature, animation or screen may delay, block or weaken an on-time adzan.
2. **Explain permissions, don't hide them.** Battery and alarm permissions decide whether the product works, so guide users through them plainly and without alarm.
3. **Indonesian first.** Local language, local prayer names, Kemenag-style times, and all three time zones.
4. **Grow without crowding the core.** Future features such as Quran must sit alongside the schedule, not bury it.
