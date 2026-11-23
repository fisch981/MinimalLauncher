# Changelog

Alle nennenswerten Änderungen werden hier auf **Deutsch** und **Englisch** dokumentiert.  
All notable changes are documented here in **German** and **English**.  
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [1.0.25] - 2026-11-23

### Deutsch
- **Layout-Feinschliff für „Zuletzt benutzt“:** Der Pfeil zum Ein- und Ausklappen der zuletzt benutzten Apps sitzt auf dem Homescreen nun direkt hinter dem Text „Zuletzt benutzt“ (`6.dp` Abstand) statt am rechten Bildschirmrand.

### English
- **Recent Apps Layout Polish:** Placed the expand/collapse arrow right after the “Recently used” label (`6.dp` spacing) on the home screen instead of aligning it to the far right edge.

---

## [1.0.24] - 2026-11-23

### Deutsch
- **Datum & Uhrzeit optional ausblendbar:**
  - Neue Schalter *„Uhrzeit im Header anzeigen“* und *„Datum im Header anzeigen“* in den Einstellungen unter „Widgets & Benachrichtigungen“.
  - Ist das Datum ausgeblendet, erscheint der Akkustand sauber ohne führenden Trennpunkt (`XX%`); sind alle Header-Elemente ausgeblendet, entsteht oben kein unnötiger Leerraum.
- **Einstellbare Uhrzeit-Größe:**
  - Neuer Schieberegler *„Größe der Uhrzeit“* (`28`–`84 sp`, Standard: `52 sp`) in den Einstellungen – inklusive automatischer Sicherung im `.json`-Backup (`LauncherBackupData`).

### English
- **Optional Clock & Date Visibility:**
  - Added *“Show clock in header”* and *“Show date in header”* switches under Settings → “Widgets & Notifications”.
  - When the date is hidden, battery level renders cleanly without a leading separator bullet; when all header widgets are hidden/inactive, no empty header padding is reserved.
- **Adjustable Clock Size:**
  - Added a *“Clock size”* slider (`28`–`84 sp`, default: `52 sp`) in Settings, also persisted in the `.json` backup format (`LauncherBackupData`).

---

## [1.0.23] - 2026-11-23

### Deutsch
- **Eigenes MinimalLauncher Backup & Restore (.json):**
  - Neuer Bereich „Sicherung & Wiederherstellung“ in den Einstellungen zum vollständigen Exportieren und Wiederherstellen aller Launcher-Daten über Androids System-Dateiauswahl (`CreateDocument` / `OpenDocument`).
  - Sichert Favoriten, Pop-up-Ordner (Unter-Apps), umbenannte App-Namen, ausgeblendete Apps, manuell gewählte/importierte Icons (inkl. Base64-PNGs), Wave-Scroller-Position & Ruhehelligkeit, Sprache, Header-Schalter, Zuletzt-benutzt-Historie sowie Nutzungsstatistiken in einer einzigen `.json`-Datei.
  - Neuer reiner Kotlin-Codec (`LauncherBackupCodec`) inklusive Round-Trip-Unit-Tests (`LauncherBackupCodecTest`).

### English
- **MinimalLauncher JSON Backup & Restore:**
  - Added a “Backup & Restore” section in Settings to export and restore the complete launcher configuration using Android’s Storage Access Framework (`CreateDocument` / `OpenDocument`).
  - Persists favorites, pop-up folders (sub-apps), custom app names, hidden apps, custom/imported icons (including Base64 PNG payloads), wave scroller side & idle brightness, app language, header toggles, recent app history, and usage statistics in a single `.json` file.
  - Added pure-Kotlin `LauncherBackupCodec` with unit test coverage (`LauncherBackupCodecTest`).

---

## [1.0.22] - 2026-11-23

### Deutsch
- **Zweisprachige Benutzeroberfläche (Deutsch & Englisch):**
  - Alle Texte der App (Homescreen, Suche, Pop-up-Menüs, Dialoge, Datumsformat und Nova-Backup-Fehlermeldungen) sind nun vollständig auf **Deutsch** und **Englisch** verfügbar.
  - **Systemsprache als Standard:** Im Modus *System* wird automatisch die Gerätesprache übernommen (`de` -> Deutsch; alle anderen Sprachen fallen auf Englisch zurück).
  - **Manuelle Sprachwahl:** In den Einstellungen unter „Erscheinungsbild & Wave Scroller“ lässt sich die Sprache jederzeit zwischen `System`, `Deutsch` und `English` umschalten.
- **Einstellungen:** Der Zusatzhinweis neben der Versionsnummer am Seitenende wurde entfernt (`MinimalLauncher v1.0.22`).

### English
- **Bilingual User Interface (English & German):**
  - All in-app strings (Home screen, Search, Pop-up sheets, Dialogs, Date formatting, and Nova backup error messages) are now fully localized in **English** and **German**.
  - **System Language Default:** In *System* mode, MinimalLauncher automatically uses the device locale (`de` -> German; all other languages fall back to English).
  - **Manual Language Selector:** Added a `System` / `Deutsch` / `English` language toggle in Settings under “Appearance & Wave Scroller”.
- **Settings:** Removed the extra tag next to the version display in the settings footer (`MinimalLauncher v1.0.22`).

---

## [1.0.21] - 2026-11-23

### Deutsch
- **Veröffentlichungs-Vorbereitung & Sicherheit:**
  - `app.keystore` und Signatur-Passwörter aus Git bzw. `app/build.gradle.kts` entfernt; lokale Builds lesen stattdessen `keystore.properties` bzw. Umgebungsvariablen (mit sauberem Debug-Fallback für externe Builds).
  - `MIT License` (`LICENSE`) hinzugefügt und in `README.md` verlinkt.
  - Regionalen Testnamen aus `NovaBackupParserTest` sowie benutzerspezifischen Pfad in `push.ps1` durch neutrale bzw. portablere Werte ersetzt.

### English
- **Public Repository Readiness & Security:**
  - Removed `app.keystore` and signing passwords from Git and `app/build.gradle.kts`; local release signing now reads from gitignored `keystore.properties` or environment variables (with clean debug fallback for external contributors).
  - Added `MIT License` (`LICENSE`) and linked it in `README.md`.
  - Replaced regional sample package names in `NovaBackupParserTest` and machine-specific path in `push.ps1` with neutral/portable values.

---

## [1.0.20] - 2026-11-23

### Deutsch
- **Dokumentation & Bereinigung:** `README.md` und `CHANGELOG.md` vollständig zweisprachig (Deutsch & Englisch) aufgebaut, `push.ps1`-Vorlage auf zweisprachige Changelog-Einträge umgestellt sowie alle Verweise auf externe Launcher-Marken oder gesonderte Funktionsbezeichnungen aus Code und Dokumentation entfernt.

### English
- **Documentation & Cleanup:** Rewrote `README.md` and `CHANGELOG.md` completely bilingually (German & English), updated the `push.ps1` template to generate bilingual changelog entries, and removed all references to external launcher brands or separate feature tiers from code and documentation.

---

## [1.0.19] - 2026-11-23

### Deutsch
- **Launcher-Logo verfeinert:** Das rechte Bein des weißen Kontur-**„M“** ist nun selbst als stilisierte Welle nach innen gewölbt – alles aus einer durchgehenden Linie auf tiefschwarzem Hintergrund (`ic_launcher.xml`).

### English
- **Launcher logo refined:** The right leg of the white outline **“M”** now curves inward into the stylized wave itself as one continuous line on a pitch-black background (`ic_launcher.xml`).

---

## [1.0.18] - 2026-11-23

### Deutsch
- **Launcher-Logo angepasst:** Die stilisierte Welle steht senkrecht rechts neben dem weißen Kontur-**„M“** (analog zur vertikalen Alphabet-Welle im Launcher) auf tiefschwarzem Hintergrund (`ic_launcher.xml`).

### English
- **Launcher logo updated:** Placed the stylized wave vertically to the right of the white outline **“M”** (matching the vertical alphabet wave inside the launcher) on a pitch-black background (`ic_launcher.xml`).

---

## [1.0.17] - 2026-11-23

### Deutsch
- **Neues Launcher-Logo:** Minimalistisches weißes Kontur-**„M“** mit stilisierter weißer Welle darunter auf tiefschwarzem Hintergrund (`ic_launcher.xml`).

### English
- **New launcher logo:** Minimalist white outline **“M”** with a stylized white wave underneath on a pitch-black background (`ic_launcher.xml`).

---

## [1.0.16] - 2026-11-23

### Deutsch
- **Echtes, sanftes 1,8-Sekunden-Ausfaden der Alphabet-Welle:** Die Ausfade-Animation läuft nach Ablauf von 1 Sekunde Wartezeit nun über eine Echtzeit-Frame-Schleife (`withFrameNanos` + `SystemClock.uptimeMillis()`, Smoothstep-Kurve über 1,8 Sekunden) unabhängig von deaktivierter System-Animationsskalierung (`ANIMATOR_DURATION_SCALE`), sodass das langsame Ausfaden zuverlässig und deutlich wahrnehmbar funktioniert.

### English
- **True smooth 1.8-second alphabet wave fade-out:** After the 1-second hold delay, the fade-out animation now runs via a real-time frame loop (`withFrameNanos` + `SystemClock.uptimeMillis()`, 1.8s smoothstep curve) independent of disabled system animation scaling (`ANIMATOR_DURATION_SCALE`), ensuring a consistent and clearly visible dim transition.

---

## [1.0.15] - 2026-11-23

### Deutsch
- **Favoriten immer alphabetisch sortiert:** Sowohl im Live-Zustand (`LauncherViewModel`) als auch beim Laden aus `StartupCache` werden Favoriten automatisch alphabetisch nach ihrem Anzeigenamen sortiert.
- **Abdunkeln & Ausfaden der Alphabet-Welle:** Neuer Regler „Helligkeit der Buchstaben im Ruhezustand“ (0–100 %, Standard: 40 %) in den Einstellungen unter „Erscheinungsbild & Wave Scroller“. Bei Aktivierung der Welle leuchten die Buchstaben sofort mit voller Helligkeit auf und dimmen nach der Nutzung verzögert aus.

### English
- **Alphabetically sorted favorites:** Favorites are always sorted alphabetically by display label both in live state (`LauncherViewModel`) and when loading from `StartupCache`.
- **Adjustable wave letter dimming & fade-out:** Added an “Idle wave letter brightness” slider (0–100%, default: 40%) in Settings under Appearance & Wave Scroller. Letters brighten to 100% immediately when touching the wave and fade down after release.

---

## [1.0.14] - 2026-11-22

### Deutsch
- **Kein Einstellungs-Aufruf durch Gedrückthalten im leeren Bereich:** Das Long-Press-Öffnen der Launcher-Einstellungen auf dem freien Homescreen-Hintergrund sowie auf Uhrzeit/Datum (`HeaderWidget`) wurde entfernt; die Einstellungen bleiben über das Zahnrad-Symbol, die Fußzeile in der App-Liste oder die Suche erreichbar.
- **Keine automatischen Zufalls-Favoriten mehr:** Wenn alle Favoriten entfernt werden (oder noch keine gewählt wurden), bleibt die Favoritenliste leer statt automatisch Standard-Apps hinzuzufügen. `StartupCache` leert dabei auch den gespeicherten Favoriten-Snapshot sauber.

### English
- **Disabled opening Settings via empty-space long-press:** Removed long-press to open Launcher Settings from empty home screen areas and the clock/date header (`HeaderWidget`); Settings remain accessible via the gear icon, app drawer footer, and search overlay.
- **No automatic fallback favorites:** Clearing all favorites leaves the favorites list empty instead of auto-populating default apps, and `StartupCache` clears the stored favorites snapshot accordingly.

---

## [1.0.13] - 2026-11-22

### Deutsch
- **Kein unnötiges Scrollen bei ausgeklappten „Zuletzt benutzt“-Apps:** Vertikale Abstände von Header (`HeaderWidget`), App-Zeilen (`AppListItem`), „Zuletzt benutzt“-Eintrag und der unteren Aktionsleiste gestrafft sowie `userScrollEnabled` im Favoriten-Modus nur aktiviert, wenn der Inhalt tatsächlich größer als der Viewport ist (inkl. automatischem Zurücksetzen von `homeListState` beim Einklappen/Zurückkehren).

### English
- **Eliminated unnecessary scrolling when Recent Apps is expanded:** Tightened vertical paddings on `HeaderWidget`, `AppListItem`, `RecentAppsEntry`, and the bottom action bar, enabled `userScrollEnabled` on the home `LazyColumn` only when content exceeds the viewport, and reset `homeListState` to item 0 on collapse/return.

---

## [1.0.12] - 2026-11-22

### Deutsch
- **Keine Überlagerung von „Suche“ & „Einstellungen“:** Die untere Schnellaktions-Leiste („Suche“ + Einstellungen-Icon) liegt im Favoriten-Modus nun unterhalb der `LazyColumn` innerhalb einer gemeinsamen `Column`, sodass ausgeklappte „Zuletzt benutzt“-Apps oder viele Favoriten den unteren Bereich nicht mehr überdecken können.

### English
- **Fixed overlap with bottom Search & Settings bar:** Placed the Favorites `LazyColumn` and the bottom Search/Settings action row inside a shared `Column` so expanded Recent Apps or long favorite lists can never overlap the bottom controls.

---

## [1.0.11] - 2026-11-22

### Deutsch
- **Sofortiges Laden der Favoriten ohne Verzögerung nach Standby / Cold-Start:**
  - `launcherState` in `LauncherViewModel` nutzt `SharingStarted.Eagerly` statt `WhileSubscribed(5000)`.
  - `MainActivity` verhindert dank `android:configChanges` unnötiges Activity-Recreate beim Aufwecken/Entsperren.
  - Neuer synchroner `StartupCache` speichert Favoriten samt 96×96-Monochrom-Icons und stellt sie nach einem Prozess-Neustart sofort im ersten Frame bereit.
  - `IconPackManager` puffert aufgelöste Icons im Arbeitsspeicher (`ConcurrentHashMap`) und lädt System-Icons über einen Lazy-Provider erst bei Bedarf nach; die Initialisierung läuft asynchron auf `Dispatchers.IO`.

### English
- **Instant favorites loading after deep standby / cold starts:**
  - Switched `LauncherViewModel.launcherState` from `WhileSubscribed(5000)` to `SharingStarted.Eagerly`.
  - Added `android:configChanges` to `MainActivity` to prevent unnecessary Activity recreation on unlock.
  - Added synchronous `StartupCache` persisting favorite entries and 96×96 monochrome PNG icons for immediate first-frame display.
  - Added an in-memory `ConcurrentHashMap` icon cache, lazy system fallback icon loading, and asynchronous `Dispatchers.IO` initialization in `IconPackManager`.

---

## [1.0.10] - 2026-11-22

### Deutsch
- **Automatisches Zuklappen von „Zuletzt benutzt“:** Die vertikale „Zuletzt benutzt“-Liste startet standardmäßig eingeklappt und klappt sich automatisch wieder zu, sobald von irgendwo eine App gestartet wird oder man aus einer App zum Launcher zurückkehrt.

### English
- **Auto-collapse Recent Apps:** The vertical “Zuletzt benutzt” list starts collapsed by default and automatically collapses whenever an app is launched or the user returns to the launcher.

---

## [1.0.9] - 2026-11-22

### Deutsch
- **Klassische vertikale Liste für zuletzt benutzte Apps:** Der Bereich „Zuletzt benutzt“ auf der Favoriten-Seite stellt die bis zu 5 zuletzt geöffneten Apps standardmäßig untereinander als reguläre `AppListItem`-Zeilen (Icon + App-Name) dar.

### English
- **Classic vertical list for Recent Apps:** Updated “Zuletzt benutzt” on the Favorites screen to display up to 5 recent apps stacked vertically as standard `AppListItem` rows instead of horizontal icon tiles.

---

## [1.0.8] - 2026-11-22

### Deutsch
- **Zuletzt benutzte Apps ohne Favoriten:** Apps, die bereits als Favorit auf dem Homescreen stehen, werden nicht mehr in „Zuletzt benutzt“ aufgenommen (und bei nachträglicher Markierung automatisch ausgeblendet), um Dopplungen zu vermeiden.

### English
- **Exclude favorites from Recent Apps:** Favorite apps are excluded from the “Zuletzt benutzt” list (and immediately hidden if favorited later) to avoid duplicate entries on the Home screen.

---

## [1.0.7] - 2026-11-22

### Deutsch
- **Zuletzt benutzte Apps auf der Favoriten-Seite:** Optionaler, einklappbarer Eintrag „Zuletzt benutzt“ unterhalb der Favoriten samt Einstellungs-Schalter und persistenter Start-Historie.
- **Nutzungsstatistik in den Einstellungen:** Zeigt Wellen-Nutzung (Links vs. Rechts mit Anzahl, Prozentanteil & Balken) sowie die meistgestarteten Apps mit direkter Favoriten-Aktion (`+ Favorit` / `★ Favorit`) und Reset-Button.

### English
- **Recent Apps on Home screen & Usage Statistics in Settings:** Added an optional collapsible “Zuletzt benutzt” section with a Settings toggle, plus usage analytics for left vs. right wave interactions and most-launched apps with direct favorite toggling and stats reset.

---

## [1.0.6] - 2026-11-22

### Deutsch
- **Alphabet-Welle links, rechts oder beidseitig:** Umschaltbar zwischen **Links**, **Rechts** und **Beidseitig** (`ScrollerSide`) inkl. automatischer Abstands- und Wischverschiebungs-Anpassung.
- **Nova-Backup-Import nur in den Einstellungen:** Entfernt aus der App-Listen-Fußzeile und ausschließlich in den Einstellungen platziert.

### English
- **Left, Right, or Dual-Sided Wave Scroller & Settings-only Nova Import:** Added a 3-way selector (`ScrollerSide`: Left / Right / Both) with automatic clearance spacing and moved the `.novabackup` import trigger exclusively into Settings.

---

## [1.0.5] - 2026-11-22

### Deutsch
- **Abstand bei linker Alphabet-Welle:** Linksseitiger Wave Scroller verschiebt Listen- und Header-Einträge sauber auf `72.dp` (sowie `76.dp` während des Wischens via `animateDpAsState`), um Textüberlagerungen zu verhindern.

### English
- **Left Wave Scroller clearance:** Added `72.dp` resting start padding and dynamic `76.dp` slide clearance (`animateDpAsState`) when the wave is positioned on the left side.

---

## [1.0.4] - 2026-11-21

### Deutsch
- **Apps umbenennen („Name ändern“):** Benutzerdefinierte App-Namen über `RenameAppDialog`, persistent über Favoriten, App-Liste, Ordner, Suche und Nova-Import.
- **Immer zurück zu den Favoriten:** Automatische Rückkehr zur Favoriten-Ansicht nach App-Starts sowie bei `onNewIntent` / `onResume`.
- **Monochrome Einstellungen:** Farbige Emojis durch monochrome Texteinträge ersetzt.

### English
- **App Renaming, Return-to-Favorites & Monochrome Settings:** Added custom per-app naming (`RenameAppDialog`), automatic return to Favorites on app launch / home press / resume, and replaced colored emojis in Settings with monochrome styling.

---

## [1.0.3] - 2026-11-21

### Deutsch
- **Nativer Icon-Pack-Picker („Icon ändern“):** Öffnet den System-/Icon-Pack-Auswahldialog installierter Lines-Packs direkt (mit „Standard-Icon wiederherstellen“).
- **Button „Standard-Launcher“ repariert:** Verwendet `ActivityResultContracts.StartActivityForResult` für `RoleManager.ROLE_HOME` mit Fallback auf die System-Home-Einstellungen.

### English
- **Native Icon-Pack Picker & Default Launcher Button Fix:** Integrated external `ACTION_PICK_ICON` / theme picker intents with reset support and fixed the `ROLE_HOME` default launcher prompt via `ActivityResultContracts.StartActivityForResult`.

---

## [1.0.2] - 2026-11-21

### Deutsch
- **Monochromes UI & Kontrastfix (`Type.kt` & `HomeScreen.kt`):** Hardcodierte weiße Textfarbe aus der globalen Typografie entfernt und minimalistisches AMOLED-Startlayout bereinigt.
- **Alphabet-Wellen-Scrollrad (`WaveScroller.kt`):** Einheitliche `awaitEachGesture`-Steuerung ohne Clipping mit dunklem Monochrom-Indikator.
- **Nova Backup Importer (`NovaBackupParser.kt` & `ImportedIconStore.kt`):** Extrahiert PNG-Icons, Icon-Pack-Zuordnungen und Favoriten aus `nova.db` und `nova.xml`.
- **Icon-Editor & Pop-up-Ordner:** Manuelle Icon-Auswahl, Rechtswisch-Geste auf Apps und konfigurierbare Unter-Apps.

### English
- **Monochrome Typography Fix, Unified Wave Gesture & Nova Backup Importer:** Fixed button text contrast in `Type.kt`, unified `WaveScroller` gesture handling via `awaitEachGesture`, implemented SQLite `nova.db` icon/favorite extraction, and added per-app icon editing and pop-up folders.

---

## [1.0.1] - 2026-11-21

### Deutsch
- **Favoriten-Startansicht:** Zeigt auf dem Homescreen gezielt Favoriten; alle Apps öffnen sich beim Wischen auf dem Alphabet oder über „Alle Apps“.
- **Alphabet-Welle & Lupen-Indikator:** Verbesserte Randabstände und schwebende Buchstaben-Blase beim Wischen.
- **Nova-Backup-Importer & Favoriten-Auswahl:** `.novabackup`-Import via Storage Access Framework, Zurück-Tasten-Schutz in `MainActivity` und Favoriten-Dialog.

### English
- **Favorites Home View, Letter Bubble & SAF Nova Import:** Switched home view to display Favorites by default, added floating letter bubble on the wave scroller, SAF `.novabackup` import, back-button home protection, and favorites selection dialog.

---

## [1.0.0] - 2026-11-21

### Deutsch
- **Initiales Release von MinimalLauncher (`de.fisch981.minimallauncher`)**
- **Alphabet-Wellen-Scrollrad (Wave Scroller):** Ergonomische Einhandbedienung mit Gaußscher Auswölbung und haptischem Feedback entlang `A`–`Z`.
- **Lines Icon-Pack & Monochrom-Engine:** Unterstützung für `com.natewren.lines`, `linesfree` und `linesdark` sowie automatischer Graustufen-Kontrastfilter für alle anderen Apps.
- **Pure AMOLED Black Design:** Tiefschwarzer Hintergrund (`#000000`) mit weißer und grauer Typografie.
- **Funktionsumfang:** Pop-up-Ordner & Swipe-Gesten, Benachrichtigungsvorschau via `NotificationListenerService`, Medienplayer-Widget im Header, Uhr/Datum/Akku-Shortcuts, Apps verbergen, Echtzeit-Suche und konfigurierbare Händigkeit.

### English
- **Initial release of MinimalLauncher (`de.fisch981.minimallauncher`)**
- **Alphabet Wave Scroller:** Ergonomic one-handed navigation with Gaussian curve displacement and haptic feedback across `A`–`Z`.
- **Lines Icon Pack & Monochrome Engine:** Support for `com.natewren.lines`, `linesfree`, and `linesdark`, plus automatic monochrome contrast filtering for all remaining apps.
- **Pure AMOLED Black Design:** Pitch-black (`#000000`) UI with crisp white and gray typography.
- **Core Features:** Pop-up folders & swipe gestures, notification previews via `NotificationListenerService`, inline media player widget, clock/date/battery header shortcuts, app hiding, real-time search, and configurable handedness.
