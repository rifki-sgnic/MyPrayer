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
  carpet-red: "#7A1522"
  carpet-ivory: "#F2E3C6"
  carpet-gold: "#E0B04A"
  kaaba-black: "#141413"
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
    backgroundColor: "{colors.cypress-tint}"
    textColor: "{colors.cypress-primary}"
    rounded: "{rounded.full}"
---

# Design System: MyPrayer

## Overview

**Creative North Star: "The Brass Astrolabe"**

MyPrayer is an instrument for reading sacred time. The astrolabe is the governing image: brass markings on deep cypress, fine graduated ticks, and numerals set in tabular figures so a time or a bearing never shifts as it counts. Precision lives in the numerals and the brass accents. The Kiblat tab is the one place that leaves the astrolabe: it is a mosque carpet whose prayer rows (shaf) you straighten, with its own carpet palette.

Around that precision, the surfaces stay calm and devotional. Backgrounds are warm limestone, never stark white. Cards are flat with hairline outlines and softly rounded corners. Gold appears in small amounts and means something each time it does. The density is that of a single-column phone schedule: one large prayer name, six rows, and plenty of air between them. The intended feel is quiet and respectful, an app you open five times a day without it ever raising its voice.

The system has two equal schemes. Light is "Warm Limestone & Deep Cypress". Dark is "Obsidian Night & Mint Jade". Both are hand-tuned, not inverted from each other.

**Key Characteristics:**
- Deep cypress green with brass gold, on warm limestone (light) or green-black obsidian (dark).
- Tabular numerals for every time, countdown, degree and percentage.
- Flat surfaces, 1dp hairline outlines, depth from tone rather than shadow.
- Soft corners that grow with importance: 10dp controls, 16dp rows, 18dp sections.
- Astrolabe geometry as the decorative motif of the schedule: a faint astrolabe plate behind the top of the schedule screen. Kiblat uses the shaf carpet instead.
- The schedule screen's sky follows the time of day of the next prayer.

## Colors

A two-hue palette: deep evergreen for structure and state, brass gold for moments and alignment, over warm neutrals.

### Primary
- **Deep Cypress** (`cypress-primary`): the brand colour in light mode. Marks the next prayer (its 1.5dp outline, timeline node, time), the selected nav item, selected reminder modes, granted-permission checks, and the main actions.
- **Cypress Night** (`cypress-container`): the snackbar. The schedule screen has no large saturated block.
- **Cypress Mist** (`cypress-tint`): Deep Cypress at about 12–14% over white. Used for the "BERIKUTNYA" badge, selected chips, the nav indicator, and icon tiles.
- **Mint Jade** (`jade-primary`) on **Jade Depth** (`jade-container`): the dark-mode counterparts, with text in `on-jade`. **Jade Mist** (`jade-mist`) is Cypress Mist's dark counterpart.

### Secondary
- **Brass Gold** (`brass-gold`) / **Lit Amber** (`amber-gold`, dark): the moment colour. Used for the Hijri date line, reminder icons on upcoming rows. It also marks a missing permission (warning icon). That is the only place gold means "attention" rather than "moment".

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

### Kiblat Carpet
The Kiblat tab has its own three colours, defined in `Color.kt` and used nowhere else. They are the same in both schemes; dark mode darkens the carpet 45% toward black.
- **Carpet Red** (`carpet-red`): the full-bleed carpet. The location pill and the guidance panel use it darkened 25%.
- **Carpet Ivory** (`carpet-ivory`): shaf lines (50%, 38% dark), arch motif (20%, 14% dark), the phone line (70%) and all text on the carpet (100% for values, 72–80% for labels).
- **Carpet Gold** (`carpet-gold`): the Ka'bah marker, and the aligned state: shaf lines, phone line and "Lurus".

### Named Rules
**The Brand Palette Rule.** MyPrayer always renders its own palette. Dynamic Color (Material You) is not supported on any Android version; `MyPrayerTheme` takes no `dynamicColor` parameter.

**The Brass Means Moment Rule.** Gold marks time and alignment: the next prayer, the Hijri date, and (as Carpet Gold) the straightened shaf on Kiblat. It never fills a large surface. At most it is an outline at 25% opacity, a dot, a line of text or an icon.

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
- **Body** (500, 16sp/24sp, 0.5sp tracking; 700 for the next prayer): prayer names and the Kiblat guidance line (600).
- **Body small** (400, 14sp/20sp): permission titles and audio file names (set to 600), plus supporting copy.
- **Label** (500, 12sp/16sp): buttons, chips, pill text, nav labels (700 when selected).
- **Overline** (700, 11sp, 1.2–1.5sp tracking, uppercase): section headers and the Hijri date. The "BERIKUTNYA" badge uses a 9sp, 0.8sp-tracked micro version.

### Named Rules
**The Steady Numerals Rule.** Every number that changes or is compared (times, countdowns, degrees, deltas, volume percentage) uses tabular figures (`fontFeatureSettings = "tnum"`). A countdown that jitters breaks the instrument.

**The Overline Budget Rule.** Uppercase overlines are labels, not decoration: at most one per section. Copy in them should be devotional rather than technical. Kiblat and Setelan use none.

Sizes are in sp. Ad-hoc `TextStyle(fontSize = …)` values in screens should move into the theme's `Typography` so they follow the system font scale and stay consistent.

## Background

### Astrolabe Plate (schedule screen)
- **Sky:** the next prayer's sky colour at the very top, fading to the screen background by 300dp. It runs under the status bar.
- **Plate:** altitude rings every 28dp and 24 hour lines (every 15°), 1dp, in the body colour at 25%. Centred off-screen (60dp in from the right edge, 80dp down), visible from 46dp to 220dp from the centre and gone by 360dp, so it sits behind the header and the prayer name and never reaches the schedule rows.
- **Sun or moon:** a 12dp body-colour disc with halos of 11dp (18%) and 17dp (8%). Its x follows the sun's position for the prayer (low left at Subuh, high centre at Dzuhur, low right at Maghrib, the moon at Isya). It travels in the free band between the header and the name (160dp at the horizon, 128dp at the zenith), so it never shows through text.
- **Motion:** colours and the sun move over 800ms when the next prayer changes.
- Drawn after the scroll modifier, so it scrolls with the content.

## Layout

A single scrolling column on phones. Screens use a 20dp side gutter and 16dp top and bottom padding, with 20dp between major blocks (header, next prayer, section header, list). Prayer rows stack with 8dp gaps. Inside cards, groups are separated by 14–16dp and divided by hairlines. Section headers sit inset 4dp from the card edge.

The Kiblat screen is not scrolled. The carpet fills the whole content area; the location pill floats top-left at the 20dp gutter, the phone line crosses at 49% of the height, and the guidance panel sits on a gradient at the bottom.

Navigation is a three-item Material navigation bar (Jadwal, Kiblat, Setelan) with a 1dp hairline above it and no tonal elevation. There is no tablet or landscape adaptation yet. A navigation rail on expanded widths is the expected path when that is needed.

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
- **Name:** the next prayer ("Isya") in Display, Ink Charcoal / Crisp White. 44dp of air above it.
- **Sub-line:** 10dp below the name, one line in 15sp medium Stone: "pukul **19:12** WIB · **00:24:30** lagi". The time is bold Deep Cypress; the countdown is semibold ink; both tabular. The zone label comes from the device offset (WIB, WITA or WIT; GMT±h elsewhere). Name and sub-line are one merged group for screen readers.
- **Status row:** 32dp below the sub-line, under a hairline at 60%, a 48dp row. Left, the reminder status; right, "Kiblat {n}°", whose screen-reader label names the 16-point compass direction.
  - When the adzan is set up: a cypress check icon and "Pengingat aktif · 5 waktu" (or "n dari 5", or "Semua pengingat nonaktif") in Stone.
  - When a permission is missing: a gold warning icon, "Adzan bisa terlambat" (or "Notifikasi adzan belum aktif") in ink, and a gold bold "Perbaiki". The whole row is a 48dp button that opens Setelan.
  - On first run, while the location is still the default, the row shows the neutral count, so the setup card is the only call to action.

### Prayer Rows
The 48dp trailing control sets the row height, giving a compact row of about 56dp.
- **Default:** Paper fill, 1dp Sand Hairline, 16dp radius. Padding is 18dp at the start, 4dp at the end and 4dp vertically. The name is in Body and the time in Time. There is no timeline dot; the next prayer is marked by its outline and badge.
- **Next:** 1.5dp Deep Cypress outline, a 2dp lift, 8dp vertical padding, bold name, the "BERIKUTNYA" badge (Cypress Mist via `secondaryContainer`), and the time in cypress.
- **Passed:** background fill, hairline at 50%, text at 62% opacity, which keeps it at or above 4.5:1 on Warm Limestone.
- **Trailing control:** a 48dp icon button that cycles the reminder mode (adzan, vibrate, off). Its icon is gold on upcoming rows, cypress on the next row, and muted at 60% when off. Its screen-reader label states the current mode: "Pengingat Isya: Getar Saja. Ketuk untuk mengganti." Every change shows a snackbar with "Urungkan" (undo). Terbit shows a static sun icon in a 48dp box instead, since it isn't a prayer and has no reminder.

### Buttons
- **Shape:** 10dp radius, 10dp vertical padding, Label type with a 16dp leading icon.
- **Filled** (Gunakan Lokasi Saya): full-width cypress fill with a leading 18dp icon. Only for a setup action the schedule depends on, at most one per screen. While working, it is disabled with a 16dp spinner and progress copy ("Mencari lokasi…").
- **Stop** (Hentikan): Stop Rose fill. The one destructive-coloured control. On Setelan it is a 48dp square icon button.

Setelan's own buttons are described under Setelan Siaga below.

### Cards / Sections
- **Corner Style:** 16dp (section cards on Jadwal).
- **Background:** Paper / Pine Surface, 1dp hairline, no shadow.
- **Internal Padding:** 16–18dp, with hairline dividers at 50–60% between items.

### Setelan Siaga (signature)
Setelan answers one question first: will the adzan be on time? It continues the Kiblat carpet world: the carpet carries the verdict, ivory paper carries the controls.
- **Palette:** the page is Carpet Paper (`#F6F1E7`; dark `#140C0D`) with Carpet Ink and Carpet Mute text (`Color.kt`). The accent is Carpet Red in light mode and Carpet Gold in dark mode (selected icons on gold use Carpet On Gold). Dividers are ink at 14%.
- **Verdict hero:** full-width carpet (`carpetColor`, darkened in dark mode) with the prayer-arch motif in ivory at 14%, clipped to the hero. Headline 30sp/36 ExtraBold: "Adzan siap tepat waktu", or "{n} izin belum aktif" with n in gold. One line of reason in ivory 78%. Below, the three permissions (Notifikasi, Alarm tepat waktu, Baterai tanpa batas) as 52dp rows under ivory 18% hairlines: a 20dp check in ivory 70% and "Aktif" when granted; a gold error icon, a one-line hint and an ivory pill "Aktifkan" (carpet text) when missing.
- **Section titles:** 20sp/28 bold ink, 24dp above, marked as headings. No uppercase overlines.
- **Audio rows:** 64dp; a 40dp tile (accent at 12%, 10dp corners) with the icon (sunrise for Subuh, note for Dzuhur–Isya), the prayers it covers in 12sp Mute, the file name in 15sp semibold on one line, an optional 48dp restore button, and a "Ganti" text button in the accent.
- **Volume:** the same tile, a Material slider in the accent, and the percentage in 15sp bold tabular. Two-way sync with the system alarm volume is unchanged.
- **Tests:** three outlined 48dp buttons (Reguler, Subuh, Getar) on Carpet Card with accent icons, and the 48dp Stop button.
- **Reminder modes:** one 64dp row per prayer: name 17sp bold (Mute when off) over the mode label, and a segmented group on ink 6% (12dp corners, 3dp padding) of three 48dp icon toggles (adzan, getar, nonaktif). The selected one fills with the accent. Each toggle is a radio button labelled "Pengingat {waktu}: {mode}".

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
Material 3 navigation bar on a Paper surface with a hairline above. Selected items use a cypress icon and label (label bold) and a Cypress Mist indicator. Unselected items are Stone.

### Kiblat Shaf (signature)
The phone is a worshipper standing on a mosque carpet. The carpet turns with the Qibla bearing; the phone faces the Qibla when the prayer rows run straight across its own line, as the imam asks before salat ("luruskan shaf").
- **Carpet:** Carpet Red, full bleed. Shaf lines every 56dp (2dp), one arch prayer-space per 48dp column with its base on the line (1.4dp stroke). A shaf line passes through the pivot, so when aligned the phone stands in a row.
- **Ka'bah marker:** 250dp ahead of the pivot on the carpet's front axis: a 30dp Kaaba Black square with a gold band and outline, and a gold pointer above it. It rotates with the carpet, so it always sits in the Qibla direction.
- **Phone line:** fixed horizontal 2dp line at 49% height with a 7dp dot and 12dp halo at the centre. It never rotates.
- **Location pill:** "{kota} · Kiblat {bearing}°" on the darkened carpet, 36dp tall, fully round. Its screen-reader label adds the 16-point direction.
- **Guidance:** bottom panel on a gradient. The degrees still to turn in 72sp ExtraBold tabular Roboto, then "Putar ke **kanan** sampai shaf lurus"; under a hairline, a 48dp row with "Arah HP {n}°" and "Δ {n,n}°". The degree and line are one polite live region.
- **States:** within ±3° the shaf lines, phone line and big word turn Carpet Gold, the number becomes "Lurus" and the line "Shaf sudah lurus, menghadap kiblat". The phone gives a long-press haptic on lock.
- **Motion:** the heading is unwrapped (never spins the long way past north) and follows a no-bounce, low-stiffness spring; the carpet is drawn in the draw phase only, so rotation never recomposes.

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
- **Don't** hard-code hex values in screens. Colours live in `Color.kt`, including the Kiblat carpet tokens.
- **Don't** reintroduce unused palettes in `Color.kt`. It holds only the tokens the two schemes use.
- **Don't** hard-code a time-zone label or compass direction. Derive WIB/WITA/WIT from the device offset, and the direction from the bearing.
- **Don't** stack uppercase overlines or use control-panel wording where a gentle label will do (The Overline Budget Rule).
