---
name: MyPrayer
description: A brass astrolabe for the five daily prayers. Deep cypress, warm limestone and brass gold, read in tabular numerals.
colors:
  cypress-primary: "#0F4C3A"
  cypress-container: "#14382E"
  on-cypress-container: "#F4F1EA"
  cypress-tint: "#DDE6E3"
  brass-gold: "#C59B27"
  limestone-bg: "#F6F4EF"
  paper-surface: "#FFFFFF"
  sand-variant: "#EDE9E0"
  sand-border: "#E2DDD2"
  ink-charcoal: "#181816"
  stone-muted: "#737067"
  error-container: "#F9DEDC"
  on-error-container: "#410E0B"
  jade-primary: "#34D399"
  on-jade: "#052E16"
  jade-container: "#132A22"
  jade-mist: "#1C3329"
  amber-gold: "#EAB308"
  on-amber: "#1C1917"
  obsidian-bg: "#0A0F0D"
  pine-surface: "#121A16"
  pine-variant: "#1A2620"
  pine-border: "#23332B"
  crisp-white: "#F3F4F2"
  sage-muted: "#8A9991"
  error-container-dark: "#8C1D18"
  on-error-container-dark: "#F9DEDC"
  kaaba-black: "#141413"
  quran-night-bg: "#10110D"
  quran-night-surface: "#181914"
  quran-night-variant: "#24211A"
  quran-night-border: "#2C2A23"
  quran-night-ink: "#E9E2D0"
  quran-night-muted: "#9A927E"
  quran-night-latin: "#B5AC96"
  quran-night-jade: "#7FC8A6"
  quran-night-amber: "#D9B45A"
  quran-night-mist: "#3A3729"
  quran-night-container: "#1E2A23"
  quran-night-error: "#E8A08F"
typography:
  display:
    fontFamily: "Roboto, sans-serif"
    fontSize: "80px"
    fontWeight: 700
    lineHeight: "0.92"
    letterSpacing: "-0.03em"
  headline:
    fontFamily: "Roboto, sans-serif"
    fontSize: "24px"
    fontWeight: 700
    lineHeight: "32px"
  readout:
    fontFamily: "Roboto, sans-serif"
    fontSize: "26px"
    fontWeight: 700
    fontFeature: "tnum"
  title:
    fontFamily: "Roboto, sans-serif"
    fontSize: "16px"
    fontWeight: 600
    lineHeight: "24px"
  time:
    fontFamily: "Roboto, sans-serif"
    fontSize: "17px"
    fontWeight: 600
    fontFeature: "tnum"
  body:
    fontFamily: "Roboto, sans-serif"
    fontSize: "16px"
    fontWeight: 500
    lineHeight: "24px"
    letterSpacing: "0.5px"
  body-small:
    fontFamily: "Roboto, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: "20px"
  label:
    fontFamily: "Roboto, sans-serif"
    fontSize: "12px"
    fontWeight: 500
    lineHeight: "16px"
  overline:
    fontFamily: "Roboto, sans-serif"
    fontSize: "11px"
    fontWeight: 700
    letterSpacing: "1.2px"
rounded:
  badge: "6px"
  control: "10px"
  card: "16px"
  section: "18px"
  full: "9999px"
spacing:
  xxs: "4px"
  xs: "8px"
  sm: "12px"
  md: "16px"
  lg: "20px"
  xl: "24px"
components:
  next-prayer:
    backgroundColor: "{colors.limestone-bg}"
    textColor: "{colors.ink-charcoal}"
    typography: "{typography.display}"
    padding: "44px 0 32px"
  prayer-row:
    backgroundColor: "{colors.paper-surface}"
    textColor: "{colors.ink-charcoal}"
    typography: "{typography.body}"
    rounded: "{rounded.card}"
    padding: "14px 18px"
  prayer-row-next:
    backgroundColor: "{colors.paper-surface}"
    textColor: "{colors.cypress-primary}"
    typography: "{typography.time}"
    rounded: "{rounded.card}"
    padding: "18px"
  badge-next:
    backgroundColor: "{colors.cypress-tint}"
    textColor: "{colors.cypress-primary}"
    rounded: "{rounded.badge}"
    padding: "3px 8px"
  section-card:
    backgroundColor: "{colors.paper-surface}"
    textColor: "{colors.ink-charcoal}"
    rounded: "{rounded.section}"
    padding: "16px"
  location-link:
    textColor: "{colors.ink-charcoal}"
    typography: "{typography.label}"
    padding: "0 2px"
  button-outlined:
    backgroundColor: "{colors.paper-surface}"
    textColor: "{colors.cypress-primary}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "10px 16px"
  button-stop:
    backgroundColor: "{colors.error-container}"
    textColor: "{colors.on-error-container}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "10px 16px"
  nav-bar:
    backgroundColor: "{colors.paper-surface}"
    textColor: "{colors.stone-muted}"
    typography: "{typography.label}"
  nav-item-selected:
    # The astrolabe pointer and selected icon. Jadwal, Kiblat and Setelan; Al-Quran takes its own palette (see Navigation)
    textColor: "{colors.cypress-primary}"
---

# Design System: MyPrayer

## Overview

**Creative North Star: "The Brass Astrolabe"**

MyPrayer is an instrument for reading sacred time. The astrolabe is the governing image: brass markings on deep cypress, fine graduated ticks, and numerals set in tabular figures so a time or a bearing never shifts as it counts. Precision lives in the numerals and the brass accents. The Kiblat tab turns the astrolabe into a sundial: Tongkat Istiwa, after Rashdul Kiblat, the days the sun stands over the Ka'bah and every upright stick's shadow lies on the Qibla line. Setelan turns it into a clock: the five prayers stand on the astrolabe's hour scale at their real times.

Around that precision, the surfaces stay calm and devotional. Backgrounds are warm limestone, never stark white. Cards are flat with hairline outlines and softly rounded corners. Gold appears in small amounts and means something each time it does. The density is that of a single-column phone schedule: one large prayer name, six rows, and plenty of air between them. The intended feel is quiet and respectful, an app you open five times a day without it ever raising its voice.

The system has two equal schemes. Light is "Warm Limestone & Deep Cypress". Dark is "Obsidian Night & Mint Jade". Both are hand-tuned, not inverted from each other.

**Key Characteristics:**
- Deep cypress green with brass gold, on warm limestone (light) or green-black obsidian (dark).
- Tabular numerals for every time, countdown, degree and percentage.
- Flat surfaces, 1dp hairline outlines, depth from tone rather than shadow.
- Soft corners that grow with importance: 10dp controls, 16dp rows, 18dp sections.
- Astrolabe geometry as the decorative motif: a faint astrolabe plate behind the top of the schedule screen, and the turning plate under the stick on Kiblat.
- The sky on Jadwal and Kiblat follows the time of day of the next prayer.

## Colors

A two-hue palette: deep evergreen for structure and state, brass gold for moments and alignment, over warm neutrals.

### Primary
- **Deep Cypress** (`cypress-primary`): the brand colour in light mode. Marks the next prayer (its 1.5dp outline, timeline node, time), the selected nav item, selected reminder modes, granted-permission checks, and the main actions.
- **Cypress Night** (`cypress-container`): the snackbar. The schedule screen has no large saturated block.
- **Cypress Mist** (`cypress-tint`): Deep Cypress at about 12–14% over white. Used for the "BERIKUTNYA" badge, selected chips, the nav indicator, and icon tiles.
- **Mint Jade** (`jade-primary`) on **Jade Depth** (`jade-container`): the dark-mode counterparts, with text in `on-jade`. **Jade Mist** (`jade-mist`) is Cypress Mist's dark counterpart.

### Secondary
- **Brass Gold** (`brass-gold`) / **Lit Amber** (`amber-gold`, dark): the moment colour. Used for the Hijri date line, reminder icons on upcoming rows. It also marks a missing permission (warning icon). That is the only place gold means "attention" rather than "moment".
- **Brass Ink** (`#75580C`, `BrassInk`): brass for words in light mode. Brass Gold reads at about 2:1 on limestone and the skies, so every gold text (the Hijri date, "Perbaiki", "Sunnah Hari Jumat", Setelan's next-prayer time) reads `MaterialTheme.colorScheme.brassText`: Brass Ink by day (at least 4.6:1 on all five skies), Lit Amber at night. Icons, dots, rings and the plate keep `secondary`.

### Neutral
- **Warm Limestone** (`limestone-bg`) / **Obsidian Night** (`obsidian-bg`): screen background. Warm, never pure white or pure black.
- **Paper** (`paper-surface`) / **Pine Surface** (`pine-surface`): cards, rows, nav bar, pill.
- **Soft Sand** (`sand-variant`) / **Pine Variant** (`pine-variant`): secondary surfaces.
- **Sand Hairline** (`sand-border`) / **Pine Hairline** (`pine-border`): every 1dp outline and divider. Dividers inside cards use it at 50–60% opacity.
- **Ink Charcoal** (`ink-charcoal`) / **Crisp White** (`crisp-white`): primary text.
- **Stone** (`stone-muted`) / **Sage** (`sage-muted`): secondary text, section overlines, inactive icons.

### Time-of-Day Sky
The schedule screen's top is tinted by the next prayer, so the page itself says what time of day is coming. Defined in `ui/theme/PrayerSky.kt`, never in screens.

| Next | Sky (light) | Sky (dark) | Body (light / dark) |
|---|---|---|---|
| Subuh, Terbit | `#E6DAE6` dawn rose | `#2A2231` | brass `#C59B27` / amber `#EAB308` |
| Dzuhur | `#DCE7EA` pale sky | `#13262D` | brass / amber |
| Ashar | `#EFE1C3` afternoon amber | `#2A2413` | brass / amber |
| Maghrib | `#EFD3C3` dusk | `#321E16` | `#C2702F` / `#E08A4A` |
| Isya | `#D8DDE6` night | `#111A2A` | moon `#8A93A6` / `#D9DEE6` |

- **Sky Stone** (`#5F5C55`): secondary text on the schedule screen in light mode. Stone deepened so it keeps at least 4.5:1 on every sky.

### Functional
- **Stop Rose** (`error-container`, `on-error-container`; dark `error-container-dark`): only the "Hentikan" (stop adzan) button. These are Material 3 baseline values and are not yet set explicitly in the theme.
- **Kaaba Black** (`kaaba-black`): the Kaaba emblem body only.

Kiblat and Setelan use the main palette and the time-of-day sky. On Kiblat, Brass Gold marks the Ka'bah and the aligned state; on Setelan, the next prayer and missing permissions.

### Quran Malam Hangat
The Quran tab has a night scheme of its own: a warm, low-glare page, like reading a mushaf by lamplight. It replaces Obsidian Night only on the Quran tab and its navigation bar. Tokens live in `Color.kt` (`QuranNight*`); `QuranTheme` in `Theme.kt` applies them.
- **Surfaces:** Warm Black (`quran-night-bg`) page, `quran-night-surface` cards and nav bar, `quran-night-border` hairlines and ayah dividers. Warm, never green-black.
- **Text:** Ivory (`quran-night-ink`) for Arabic and titles, ivory at 88% for the translation, Warm Latin (`quran-night-latin`) for the italic transliteration, `quran-night-muted` for meta. Ivory, not white: pure white blooms on OLED in a dark room.
- **Amber** (`quran-night-amber`): the ayah-end medallion, the surah's Arabic name and the resume ribbon. Nothing else.
- **Jade** (`quran-night-jade`): interactive state only (Simpan ayat, the progress line, selected controls). Never text that is read.
- **Error** (`quran-night-error`, warm rose, with `#3A1510` on it): load errors. The same value is the app's dark `error` role.
- **Reading order:** Arabic (14.6:1), then translation (11.5:1), then Latin (8.4:1). Latin must never be the most colourful text on the page.

**When it shows.** Automatically: night from Isya to Subuh and whenever the phone is in dark mode. There is no manual override. By day the tab uses the light scheme, with cypress for the medallion. The status and navigation bar icons follow the page, not the system theme.

**Reader colours without a Material role** (Latin line, open-ayah fill at ink 6%, jump-pill edge at ink 18%) come from `LocalQuranReadingColors`. Screens read them; they never branch on the scheme.

### Named Rules
**The Brand Palette Rule.** MyPrayer always renders its own palette. Dynamic Color (Material You) is not supported on any Android version; `MyPrayerTheme` takes no `dynamicColor` parameter.

**The Brass Means Moment Rule.** Gold marks time and alignment: the next prayer, the Hijri date, and the Ka'bah and the aligned shadow on Kiblat. It never fills a large surface. At most it is an outline at 25% opacity, a dot, a line of text or an icon.

**The Every Role Is Set Rule.** Every Material colour role a component reads must be defined in both schemes, because unset roles fall back to Material's baseline purple. `Theme.kt` sets them all:
- `secondaryContainer` is Cypress Mist (`jade-mist` in dark). It carries tonal buttons, the slider track and the BERIKUTNYA badge.
- The `surfaceContainer*` family comes from the sand and pine neutrals.
- `inverseSurface` is Cypress Night, with `inversePrimary` as the snackbar action colour.

Adding a component that reads a new role means adding that role to both schemes.

## Typography

**Display Font:** Roboto (system default)
**Body Font:** Roboto (system default)

**Character:** A single system family carried by weight, size and tabular figures rather than by a display face. Numbers do the expressive work: big, bold and steady.

### Hierarchy
- **Display** (700, 80sp, 0.92 line height, −0.03em tracking): the next prayer's name only. It shrinks step by step to 40sp rather than clip a long name at large font scales.
- **Headline** (700, 24sp/32sp): screen titles such as "Pengaturan Adzan & Izin".
- **Title** (600, 16sp/24sp): the Gregorian date and the location name.
- **Time** (600, 17sp, tabular; 700 when it's the next prayer): prayer times in the schedule.
- **Body** (500, 16sp/24sp, 0.5sp tracking; 700 for the next prayer): prayer names.
- **Body small** (400, 14sp/20sp): permission titles and audio file names (set to 600), plus supporting copy.
- **Label** (500, 12sp/16sp): buttons, chips, pill text, nav labels (one weight in both states).
- **Overline** (700, 11sp, 1.4sp tracking, uppercase; `typography.overline`): the Hijri date, Jadwal's only overline. The "BERIKUTNYA" badge is `typography.badge` (700, 11sp, 0.8sp tracking), Material's smallest label size.

### Named Rules
**The Steady Numerals Rule.** Every number that changes or is compared (times, countdowns, degrees, deltas, volume percentage) uses tabular figures (`fontFeatureSettings = "tnum"`). A countdown that jitters breaks the instrument.

**The Overline Budget Rule.** Uppercase overlines are labels, not decoration: at most one per section. Copy in them should be devotional rather than technical. Kiblat and Setelan use none.

Sizes are in sp. Shared roles live in `ui/theme/Type.kt`, not in screens:
- **Display** is Material's `displayLarge` (tabular, so the Kiblat degrees hold still), used by Jadwal's prayer name and Kiblat's hero.
- **Sub-line** is `MaterialTheme.typography.subLine` (500, 15sp/20, tabular, balanced line breaks), used under both heroes.
- **Readout** is `MaterialTheme.typography.readout` (600, 12sp, tabular), used for "Kiblat {n}°", "Arah HP {n}°" and the reminder status.
- Kiblat's notes use Material's `bodyMedium` (14sp/20), the body-small role.

- **Overline** and **Badge** are `MaterialTheme.typography.overline` and `.badge`. The prayer-row time (17sp tabular) is still screen-local and should move into `Type.kt` the same way.

## Background

### Astrolabe Plate (schedule screen)
- **Sky:** the next prayer's sky colour at the very top, fading to the screen background by 300dp. It runs under the status bar.
- **Plate:** altitude rings every 28dp and 24 hour lines (every 15°), 1dp, in the body colour at 25%. Centred on the theme hub (44dp in from the right edge, at the header row's middle below the status bar), visible from 46dp to 220dp from the centre and gone by 360dp, so it sits behind the header and the prayer name and never reaches the schedule rows.
- **Theme hub:** the plate's hollow centre holds the app's theme control: a 40dp Paper / Pine Surface disc with a 1dp primary ring (cypress by day, jade at night) in a 48dp target, with a 6dp halo of the body colour at 14% drawn by the plate. It is the plate's centre pin, kept quiet so the prayer name stays the strongest mark. The glyph is in primary: half-filled circle for Otomatis (follows the phone), sun for Terang, moon for Gelap. Each tap moves to the next mode; the choice persists and applies to every tab. On the Quran tab, Otomatis keeps its own night window (Isya to Subuh, or a dark phone). The header gives the hub row 1 to itself: Hijri and Gregorian dates on the left with the location link under them.
- **Sun or moon:** a 12dp body-colour disc with halos of 11dp (18%) and 17dp (8%). Its x follows the sun's position for the prayer (low left at Subuh, high centre at Dzuhur, low right at Maghrib, the moon at Isya). It travels in the free band between the header and the name (160dp at the horizon, 128dp at the zenith), so it never shows through text.
- **Motion:** colours and the sun move over 800ms when the next prayer changes.
- Drawn after the scroll modifier, so it scrolls with the content.

## Layout

A single column on phones. Jadwal fits one screen and does not scroll: it is two blocks, the hero (header, next prayer, status row; 16dp apart) at the top and the schedule (at least 16dp below) pinned to the bottom, with spare height on tall screens opening between them. Scrolling switches on only when the page cannot fit (large font scale, a short screen, the first-run location card). Jadwal uses a 20dp side gutter, 8dp top and 12dp bottom padding. Prayer rows stack with 6dp gaps. Inside cards, groups are separated by 14–16dp and divided by hairlines. Section headers sit inset 4dp from the card edge.

The Kiblat screen is not scrolled. Like Jadwal, its sky runs under the status bar. The header row sits at the 20dp gutter below the status bar, the stick stands 286dp below the status bar (raised on short screens so its shadow clears the panel), and the guidance panel sits on a fade at the bottom.

Navigation is a four-item bar (Jadwal, Kiblat, Al-Quran, Setelan) with a 1dp hairline above it, no tonal elevation, and an astrolabe pointer marking the selected tab. There is no tablet or landscape adaptation yet. A navigation rail on expanded widths is the expected path when that is needed.

## Elevation & Depth

Flat by default. Depth comes from tone and line: a warm background, paper-white cards, 1dp sand hairlines, and the next prayer's name in large bold type as the one heavy element. The navigation bar has its tonal elevation set to zero.

### Shadow Vocabulary
- **Next-prayer lift** (2dp `shadowElevation`): only on the next prayer's row, together with its 1.5dp cypress outline.

### Named Rules
**The Flat Sanctuary Rule.** Surfaces don't float. Emphasis comes from outline weight (1dp to 1.5dp), colour and tone. The single 2dp lift on the next prayer is the one exception, and it stays the only one.

## Shapes

Softly rounded rectangles whose corner radius grows with importance: 6dp for the badge, 10dp for controls (buttons, mode chips, icon tiles), 16dp for rows and instrument cards, and 18dp for settings sections. Pills and status dots are fully round. The astrolabe introduces the only non-rectangular geometry: concentric circles, a horizon arc, graduated ticks and triangular pointers.

## Components

Calm and devotional: soft corners, generous padding, quiet outlines. The instrument precision belongs to numerals and the compass, not to borders and chrome.

### Next Prayer (signature)
No card. The block sits straight on the screen background, so the prayer name is the one dominant mark on the screen.
- **Name:** the next prayer ("Isya") in Display, Ink Charcoal / Crisp White. 24dp of air above it (the 16dp block gap plus 8dp).
- **Sub-line:** 10dp below the name, one line in 15sp medium Stone: "pukul **19:12** WIB · **00:24:30** lagi". The time is bold Deep Cypress; the countdown is semibold ink; both tabular. The zone label comes from the device offset (WIB, WITA or WIT; GMT±h elsewhere). Name and sub-line are one merged group for screen readers.
- **Status row:** 16dp below the sub-line, under a hairline at 60%, a 48dp row. Left, the reminder status; right, "Kiblat {n}°", a 48dp button that opens Kiblat, whose screen-reader label names the 16-point compass direction.
  - The status answers "will my next adzan sound?", most urgent first, and never shows a check while the next reminder is off:
  - Notifications off: a gold warning icon, "Notifikasi adzan belum aktif" in ink, and a bold Brass Ink "Perbaiki". The whole row is a 48dp button that opens Setelan.
  - Exact alarms off (only possible on Android 12/12L; `USE_EXACT_ALARM` keeps them on from 13): the same form, "Adzan bisa terlambat".
  - Next prayer muted: a Stone bell-off icon and "Pengingat {waktu} nonaktif" in Stone.
  - Otherwise: a cypress check and "Pengingat aktif · 5 waktu" (or "n dari 5 waktu"), or a bell-off and "Semua pengingat nonaktif".
  - On first run, while the location is still the default, the row shows the neutral count, so the setup card is the only call to action.

### Prayer Rows
The 48dp trailing control sets the row height, giving a compact row of 52dp (60dp for the next prayer).
- **Default:** Paper fill, 1dp Sand Hairline, 16dp radius. Padding is 18dp at the start, 4dp at the end and 2dp vertically. The name is in Body and the time in Time. There is no timeline dot; the next prayer is marked by its outline and badge.
- **Next:** 1.5dp Deep Cypress outline, a 2dp lift, 6dp vertical padding, bold name, the "BERIKUTNYA" badge (Cypress Mist via `secondaryContainer`), and the time in cypress.
- **Passed:** background fill, hairline at 50%, text at 62% opacity, which keeps it at or above 4.5:1 on Warm Limestone.
- **Trailing control:** a 48dp icon button that cycles the reminder mode (adzan, vibrate, off). Its icon is gold on upcoming rows, cypress on the next row, and muted at 60% when off. Its screen-reader label states the current mode: "Pengingat Isya: Getar Saja. Ketuk untuk mengganti." Every change shows a snackbar with "Urungkan" (undo). Terbit shows a static sun icon in a 48dp box instead, since it isn't a prayer and has no reminder.

### Friday Card
From Thursday's Maghrib to Friday's, a 16dp section card sits between the next prayer and the schedule: a 38dp Cypress Mist icon tile with a book, "Sunnah Hari Jumat" (11sp bold, Brass Ink) over "Baca Surah Al-Kahfi (QS. 18)" (14sp semibold), and a cypress arrow. The whole card opens Surah Al-Kahfi. It stays for the whole window. On shorter phones it pushes the last row below the fold, so the schedule scrolls on those days.

### Reminder Hint
The schedule has no header. While every reminder is still at its default, "Ketuk lonceng untuk berganti: adzan, getar, mati." in 12sp Stone sits above the rows. After the first change the undo snackbar has taught it, so it goes.

### Buttons
- **Shape:** 10dp radius, 10dp vertical padding, Label type with a 16dp leading icon.
- **Filled** (Gunakan Lokasi Saya): full-width cypress fill with a leading 18dp icon. Only for a setup action the schedule depends on, at most one per screen. While working, it is disabled with a 16dp spinner and progress copy ("Mencari lokasi…").
- **Stop** (Hentikan): Stop Rose fill. The one destructive-coloured control. On Setelan it is a 48dp square icon button.

Setelan's own controls are described under Setelan Rete below.

### Cards / Sections
- **Corner Style:** 16dp (section cards on Jadwal).
- **Background:** Paper / Pine Surface, 1dp hairline, no shadow.
- **Internal Padding:** 16–18dp, with hairline dividers at 50–60% between items.

### Setelan Rete (signature)
Setelan is the astrolabe read as a clock. The five prayers sit on a 24-hour dial at their real times, and the dial is also the reminder control. It uses the app scheme and Jadwal's sky, like Kiblat; there is no palette of its own.
- **Sky:** the next prayer's sky (`prayerSkyColors`) fading to the background by 300dp, under the status bar, scrolling with the page. Secondary text is Sky Stone in light mode.
- **Header:** "Setelan" 24sp/32 bold (a heading) over "{n} dari 5 pengingat aktif" in 14sp medium Stone. Vibrate counts as active.
- **Dial:** full width inside a 16dp gutter, square. It is drawn on a 328-unit square and scales from there (`DialUnits` in `SettingsScreen.kt`). 12:00 is at the top, midnight at the bottom, clockwise (`hourAngle`).
  - Plate: rings every 28 units from 56 to 140 and hour lines every 15° from 35 to 146, 1dp, body colour at 25%. Outer circle, 1dp, body at 45%.
  - Rim: a tick every quarter hour (157–163), longer every hour (152–163), body at 60%; brass 2dp marks at 00, 06, 12 and 18.
  - Alidade: a 2dp brass rule from the hub (36) toward the next prayer (114), fading toward the hub. Hidden while Terbit is next.
  - Hub: a 60-unit disc with a 7-unit halo of the body colour at 14%. Primary with a check when both permissions are on; Brass with the missing count (24sp bold, tabular) otherwise. Colour eases over 300ms. Silent to screen readers; the verdict below carries it.
  - Markers: on radius 134, a 48dp touch target holding a 34dp mode disc with an 18dp icon. Adzan: primary fill, bell. Getar: `secondaryContainer`, primary vibrate icon. Mati: background fill, 1dp outline, Stone bell-off. Colours cross over 150ms. The next prayer wears a 1.5dp brass ring 2dp clear of its disc. A tap cycles adzan, getar, mati (`ReminderMode.next`); the label reads "Pengingat {waktu} {jam}: {mode}".
  - Labels: on radius 88, centred, the name in 12sp semibold (Stone when off) over the time in 11sp tabular (bold Brass for the next prayer).
  - Caption under the dial: "Ketuk waktu untuk berganti: adzan, getar, mati." 12sp Stone, centred.
- **Verdict:** 24dp below, "Adzan siap tepat waktu" or "{n} izin belum aktif" in 16sp/24 semibold (a heading), one line of reason in 13sp Stone. Then the two permissions as 52dp rows under outline-70% hairlines: Stone check and "Aktif" when granted; a Brass error icon, a 12sp hint and a primary pill "Aktifkan" when missing.
- **Suara:** a 16sp/24 semibold heading 32dp above. "Volume alarm adzan" in Label Stone with the percentage as a 26sp bold tabular readout. The volume is a graduated rule (a tick every 5%, a long one every 25%), a 2dp primary level and a 3dp × 28dp brass index; an invisible Material slider on top takes the input and the accessibility. Two-way sync with the system alarm volume is unchanged.
- **Tests:** four 72dp keys spaced across the row: a 48dp round button (Paper with a 1dp outline and a primary icon; Stop Rose for Hentikan) over a 12sp Stone label.
- **Audio rows:** 56dp under outline-70% hairlines: the prayers it covers in Label Stone over the file name in 14sp semibold on one line, an optional 48dp restore button and a primary "Ganti".

### Location Link
Borderless: a 15dp cypress pin, the location name in Label on one line with an ellipsis, and a 15dp Stone refresh icon that becomes a 12dp spinner while updating. The whole row is a 48dp touch target. It shares a `FlowRow` with the date (Hijri overline in gold, then the date in 14sp medium Stone), so it wraps onto its own line when the two don't fit, for example with a long place name or large text.

### Location Setup Card
Shown only while the location is still the default. It is a 16dp section card containing:
- a gold warning icon;
- a title, "Jadwal ini masih untuk {kota}";
- a one-line reason, which names notifications too on Android 13+;
- a full-width filled "Gunakan Lokasi Saya" button.

That button requests location, and notifications on Android 13+, in the same explained step. The card disappears once a location is saved.

### Snackbar
Material snackbar on Cypress Night (`inverseSurface`), with cream text and the action in Mint Jade (`inversePrimary`). It is used for every one-off outcome on Home:
- a reminder-mode change, with "Urungkan";
- a location update;
- a failed location fix, with "Coba Lagi";
- refused location access, with "Buka Setelan".

A newer message replaces the one on screen. Messages without an action get a dismiss button.

### Navigation
A custom bar (`AppNavigationBar` in `MainActivity.kt`), 68dp plus the system navigation inset, with a 1dp hairline above and no tonal elevation. Jadwal (clock), Kiblat (compass), Al-Quran (book), Setelan (gear). Labels in Label type under the icons, always shown.
- **Astrolabe scale (signature).** The top of the bar is a graduated scale, like the rim of an astrolabe: a 1dp × 4dp tick every 9dp in the unselected colour at 30%, and a 1dp × 7dp tick at the centre of each destination at 55%. A pointer in the accent, a 36dp × 2dp bar on the hairline with a 10dp × 6dp arrowhead below it, marks the selected tab and travels the scale over 300ms with emphasized easing, in step with the page slide. Drawn in the draw phase only. It reads the astrolabe plate on Jadwal down into the bar.
- **Palette follows the page.** Jadwal, Kiblat and Setelan use the app scheme: Paper surface, primary pointer, icon and label, Stone or Sage when unselected and for the scale. Al-Quran uses `quranColorScheme`. Colours move with the incoming page (90ms delay, 210ms).
- **Icons:** outlined when unselected, filled when selected (150ms crossfade). Labels keep one weight in both states, so they never change width.
- **Tap feedback:** every tap, reselects included, settles the icon from 90% scale with a slight overshoot (spring, damping 0.8). Kiblat's needle swings 45° onto its line and Setelan's gear turns a quarter as they settle. There is no ripple; the settle is the answer.
- **Hidden in the reader.** While a surah is open the bar slides away (300ms in, 200ms out, both emphasized) and the reader is full screen; its back button returns to the list. The reader pads only for the system navigation bar, so the bar slides over it and the content never jumps.
- **Missing-permission dot:** grows from 90% and fades over 150ms.
- **Reselect:** Setelan scrolls back to the dial.
- **Screen reader:** each item is a tab in a selectable group, labelled with its name.
- **Back:** from Kiblat, Al-Quran or Setelan returns to Jadwal (the Quran reader closes its surah first). Back on Jadwal leaves the app.
- **Missing permission:** a 6dp badge in `secondary` (Brass / Amber) on Setelan while notifications or exact alarms are off; the item reads "Setelan, {n} izin belum aktif". Hidden on first run, while the location card is the call to action. Re-checked on every resume.
- **Tab change:** Material shared axis along the bar. Moving right, the new page enters from the right and the old one leaves to the left; moving left (Back included), the reverse. Both slide a fifth of the width over 300ms with emphasized easing; the old page fades out in 90ms, the new one fades in over the next 210ms. The system's "Remove animations" setting turns it, and the icon motion, into a cut.

### Launch
The app opens straight onto Jadwal's own ground, never a white window.
- **Splash** (`Theme.MyPrayer.Starting`, androidx core-splashscreen): the page background (Warm Limestone / Obsidian Night) with the theme hub as the mark: a 56dp primary disc, an 8dp halo of the body colour at 14% and a 1dp ring at 30% (`drawable/splash_mark.xml`, colours in `values/` and `values-night/splash.xml`). On Android 12+ the app sets `UiModeManager.setApplicationNightMode` from the theme hub, so the splash follows Terang/Gelap rather than the phone; the manifest handles `uiMode`, so this never recreates the activity. The window behind Compose has the same background.
- **Hold:** the splash stays until DataStore has the theme and the first schedule exists (capped at 1.5s), so neither a blank frame nor the spinner shows.
- **Exit:** the splash fades in 160ms with a strong ease-out (0.23, 1, 0.32, 1), so the schedule shows from the first frames; the mark shrinks to 80% as the splash's fade carries it.
- **Cascade:** underneath, Jadwal settles in from the top: header, location card (first run), next prayer, the Friday card (Thursday night and Friday), the reminder hint, then the six rows. Each block fades in over 220ms (emphasized decelerate) at its own start (0, 30, 50, 70, 80, 88, 94, 98, 100, 100ms; `ui/theme/LaunchEntrance.kt`). The gaps shrink down the page so it reads as one wave. The next prayer, the lead, rises 12dp; everything else rises 6dp. The sky, plate and navigation bar arrive with the splash fade. About 320ms in all.
- Plays only on a fresh launch (no saved state); a rotation or restored process opens settled. "Remove animations" makes it a cut.

### Quran Reader (signature)
A quiet reading column on the page background, no cards. A compact bar (back, then the surah name and "Ayat {n} dari {total}" once the opening scrolls away, then a "Tampilan bacaan" button) with a 2dp progress line in primary along its hairline.
- **Opening:** the surah's Arabic name in `tertiary` (cypress by day, amber at night), the Latin name as a heading, meta in muted, then the basmalah under a hairline.
- **Ayah:** Arabic in the Quran font at the reader's size (26/30/34/38sp, default 30; line height 2.15em) ending in the ۝ medallion with Arabic-Indic digits in `tertiary`; the italic Latin line; the translation led by its number in muted semibold tabular figures. Ayahs are separated by hairlines. Tapping an ayah opens it (fill from `LocalQuranReadingColors`) and reveals "Simpan ayat".
- **Resume:** a brass ribbon hangs from the resume ayah's hairline with "Lanjut dari sini"; on arrival it unrolls and the ayah glows once. While that ayah is below the reader, a "Lanjut ke ayat {n}" pill floats above the bottom edge. It steps aside while an ayah is open, so it never covers the save button.
- **Tampilan sheet:** a Material bottom sheet titled "Tampilan bacaan": a "Teks Latin" switch and "Ukuran huruf Arab" as four segments that preview the size with a growing ع. Both persist in `QuranPreferences`.
- **Navigation bar:** takes the Quran palette on this tab (see Navigation).

### Kiblat Tongkat Istiwa (signature)
The phone is an upright stick (tongkat istiwa) on Home's astrolabe plate. On Rashdul Kiblat the sun stands over the Ka'bah, so a stick's shadow lies on the Qibla line; here the plate turns with the Qibla bearing and the phone faces the Qibla when the stick's shadow falls straight down its own line.
- **Sky:** the same time-of-day sky as Jadwal (`prayerSkyColors` in `PrayerSky.kt`), fading to the background by 300dp. Secondary text uses Sky Stone in light mode.
- **Plate (turns):** rings every 28dp and hour lines every 15°, 1dp, in the sky's body colour at 25%, centred on the stick, visible from 46dp to 230dp and gone by 340dp. On it: a hairline Qibla axis in brass fading toward the stick, the Ka'bah 196dp ahead (an 18dp Kaaba Black square with a brass band and outline, wrapped in Home's sun halos: brass 18% at +7dp, 8% at +15dp), and the shadow, a 190dp tapering wedge from the stick in ink at 46% (34% dark) that holds its tone for half its length, then fades. The copy names the shadow as the target, so after the Ka'bah it is the strongest mark on the plate.
- **Fixed:** the phone's line, a 1.5dp dashed (6/6) cypress line at 55%, from 76dp to 476dp below the status bar, with a 12x9dp front marker above it; the stick, a 16dp paper disc with a 2dp ink ring, a 6dp mark-colour centre and a 14dp halo at 18%.
- **Header:** Home's location link (pin, name, refresh; a 48dp button that updates the location) on the left and "Kiblat {bearing}°" on the right in 12sp semibold tabular, whose screen-reader label adds the 16-point direction.
- **Guidance:** the degrees still to turn in Display (80sp bold tabular, shrinking to 40sp), then "putar ke **kanan** sampai bayangan tongkat jatuh di garis" in 15sp medium with the direction in bold cypress; 32dp below, under a hairline at 60%, a 48dp footer slot that holds "Arah HP {n}°" or one of the notes below. The degree and line are one polite live region.
- **States:** within ±3° the line turns solid brass, the front marker, halo and centre turn brass, the number becomes "Lurus" and the line "Bayangan di garis, menghadap kiblat" with a brass check. The phone gives a long-press haptic on lock. The lock lets go only past 5°, so sensor noise at the edge never flickers or buzzes twice. The degrees, "Arah HP" and the lock all read the plate's smoothed heading, so the numbers never run ahead of the picture.
  - **Tilted** (the screen more than 45° from flat, cleared back within 35°): the footer slot reads "**Letakkan HP mendatar,** layar menghadap ke atas. Arah kompas goyah saat HP berdiri." with a cypress phone icon, in the same note form as calibration. The note slot's order is no compass, calibration, tilt, default location, then "Arah HP".
  - **Default location** (the location was never set): the footer row reads "Arah ini masih untuk {kota}." with a cypress pin and a bold cypress "Gunakan lokasi saya"; the whole row is a 48dp button that runs the same location request as Jadwal. Calibration takes the row first.
  - **Waiting** (no heading yet): the plate rests with the Ka'bah ahead, the phone's line and marker are hidden, the hero is "—" in muted and the sub-line "Membaca kompas…". The plate turns into place on the first reading.
  - **Calibrating** (magnetometer accuracy below medium): guidance still runs, but the lock, brass and haptic are withheld. The footer row becomes a cypress compass icon and "**Kompas belum akurat.** Gerakkan HP membentuk angka 8 sampai pesan ini hilang." in 14sp, as its own polite live region.
  - **No compass** (no magnetometer, or no heading within 4s): the plate stands still as a Rashdul Kiblat diagram (no phone line). The hero is the bearing in whole degrees, the sub-line "dari utara, ke arah {arah}", and the footer names the next Rashdul Kiblat in local time (27/28 May 09:18 UTC, 15/16 July 09:27 UTC) and how to read the shadow. Where the sun stands below 5° at that moment (eastern Indonesia), the footer points to the nearest mosque instead. Copy on Kiblat addresses the user as Anda, like the rest of the app.
- **Sensors:** fused rotation vector, then geomagnetic rotation vector, then accelerometer plus magnetometer. The magnetometer's accuracy is the calibration signal on every path.
- **Screen reader:** the guidance is one polite live region whose text changes only on meaningful steps (turn direction, about 10° steps, "hampir lurus" under 10°, the lock), never on every sensor tick.
- **Motion:** the heading is unwrapped (never spins the long way past north) and follows a no-bounce, low-stiffness spring; the sky and body colours ease over 800ms; the plate is drawn in the draw phase only, so rotation never recomposes.

## Do's and Don'ts

### Do:
- **Do** render the brand palette on every Android version (The Brand Palette Rule).
- **Do** use tabular figures for every number that changes or is compared (The Steady Numerals Rule).
- **Do** express emphasis with outline weight (1dp to 1.5dp), cypress colour and type weight before reaching for shadow.
- **Do** keep astrolabe geometry faint and behind the content: the schedule screen's plate (25% of the body colour, masked so it never reaches the schedule rows).
- **Do** read colours from Material roles (`MaterialTheme.colorScheme.*`), and define every role a component uses in both schemes.
- **Do** design and check light and dark mode together. Both are first-class.
- **Do** keep touch targets at least 48dp, even when the visible mark is smaller (the location link, the next-prayer status row).
- **Do** make any change that can silence an adzan announce itself and offer undo.

### Don't:
- **Don't** enable Dynamic Color.
- **Don't** add drop shadows beyond the next-prayer row's 2dp lift (The Flat Sanctuary Rule).
- **Don't** fill large areas with gold, or use gold for anything other than moments, alignment, and the missing-permission warning.
- **Don't** hard-code hex values in screens. Colours live in `Color.kt`.
- **Don't** reintroduce unused palettes in `Color.kt`. It holds only the tokens the two schemes use.
- **Don't** hard-code a time-zone label or compass direction. Derive WIB/WITA/WIT from the device offset, and the direction from the bearing.
- **Don't** stack uppercase overlines or use control-panel wording where a gentle label will do (The Overline Budget Rule).
