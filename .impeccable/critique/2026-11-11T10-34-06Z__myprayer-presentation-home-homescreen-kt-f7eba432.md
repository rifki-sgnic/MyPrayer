---
target: home
total_score: 21
max_score: 40
na_heuristics: 
p0_count: 1
p1_count: 3
target_identity: "file:H:\\Programming\\android\\MyPrayer\\app\\src\\main\\java\\com\\rfksgn\\myprayer\\presentation\\home\\HomeScreen.kt"
target_fingerprint: "sha256:f35db25cd2970a2f050a9b643e9cb7829fbfa943f7d2be6c4824e964e2c1089a"
target_path: "H:\\Programming\\android\\MyPrayer\\app\\src\\main\\java\\com\\rfksgn\\myprayer\\presentation\\home\\HomeScreen.kt"
timestamp: 2026-11-11T10-34-06Z
slug: myprayer-presentation-home-homescreen-kt-f7eba432
closed: true
---
Method: design review only (detector not applicable, native platform)

Tested on a Pixel 6 Pro emulator (Android API 37), installed fresh: light, dark, 130% text size, first run, reminder-icon taps, location refused.

## Design Health Score
| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 2 | Default location, GPS failure and adzan readiness never shown on Home |
| 2 | Match System / Real World | 2 | "WIB" hard-coded; pill reads "Jakarta Pusat (Def" |
| 3 | User Control and Freedom | 3 | Mode change can be undone by tapping again, but no undo prompt |
| 4 | Consistency and Standards | 2 | Palette follows wallpaper (Dynamic Color); bell vs speaker icon for adzan |
| 5 | Error Prevention | 1 | Silent Jakarta default; two taps silence a prayer |
| 6 | Recognition Rather Than Recall | 3 | Icon states must be remembered |
| 7 | Flexibility and Efficiency | 2 | No widget |
| 8 | Aesthetic and Minimalist Design | 3 | Six outlined cards; Qibla bearing redundant on Home |
| 9 | Error Recovery | 1 | Location refused or GPS failure gives no message |
| 10 | Help and Documentation | 2 | One hint line only |
| Total | | 21/40 | Acceptable |

## Design Specificity
The intended Brass Astrolabe design is specific, but Android 12+ Dynamic Color replaces it with generic Material You periwinkle (hero flips from dark to pastel, gold becomes slate). Astrolabe rings at 5-7% are nearly invisible on a real screen.

## Priority Issues
- [P0] New users silently get Jakarta's prayer times: no first-run location request, pill truncated to "Jakarta Pusat (Def", refusal or GPS failure is silent. WITA/WIT users get adzan ~50 min off. Fix: first-run location step, hero banner while default is in use, ellipsis, snackbar on failure. Commands: onboard, harden.
- [P1] Brand palette lost on Android 12+ (dynamicColor = true). Fix: turn Dynamic Color off, define every colour slot, rings at 10-12%. Command: polish.
- [P1] Home never shows whether the adzan will fire; permissions only on Setelan; notification prompt shown cold at launch. Fix: "Adzan aktif untuk 5 waktu" line / gold "Adzan bisa terlambat" attention row; ask with context. Commands: onboard, clarify.
- [P1] Two taps silence a prayer with no confirmation; 40dp targets; screen-reader label lacks the current mode. Fix: snackbar with undo (Urungkan), label with state, 48dp targets. Command: harden.
- [P2] At 130% text the header collides and lines wrap; ~72dp rows overflow a 6.7" screen (Isya cut off). Fix: wrap header, ~56dp rows, drop Qibla bearing from hero. Commands: layout, adapt.

## Persona Red Flags
- Sari (Makassar, WITA): sees WIB and Jakarta times with no warning; Maghrib adzan ~50 min late.
- Casey: 40dp edge targets where the thumb scrolls; accidental mute; pill out of thumb reach; no widget.
- Sam: passed prayers ~2.9:1 contrast; button label lacks mode; header collision at 130%.
- Jordan: default Android robot splash; cold notification prompt; "(Def" cue unreadable; refresh icon doesn't read as "detect location".

## Minor Observations
Default launcher/splash icon; hard-coded "Barat-Barat Laut"; "00:" hours under an hour; timeline dots are decoration; Hijri from HijrahDate (Umm al-Qura, changes at midnight not Maghrib); no refresh at midnight (header shows yesterday until Subuh); blank screen while loading; heading and hint compete.

## Questions
Should the hero answer "will my adzan fire?"; does Home need the Qibla bearing; one calm list instead of six cards; fade passed prayers or keep next near the top?
