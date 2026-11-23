# MinimalLauncher

[Deutsch](#-deutsch) | [English](#-english)

[![Release](https://img.shields.io/github/v/release/fisch981/MinimalLauncher?style=flat-square)](https://github.com/fisch981/MinimalLauncher/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0+-black.svg?style=flat-square)](https://android.com)

---

## 🇩🇪 Deutsch

Minimalistischer, ergonomischer Android-Launcher in kompromissloser **Schwarz-Weiß- / AMOLED-Optik** mit alphabetischem Wave-Scroller, nativer Unterstützung für das **Lines Icon-Pack** und automatischer Monochrom-Konvertierung für alle Apps.

### ⚡ Funktionen

- **Alphabetischer Wave Scroller:** Vertikale Alphabet-Leiste (`★`, `A`–`Z`, `#`) mit Gaußscher Auswölbung, Buchstaben-Indikator und haptischem Feedback – konfigurierbar **links**, **rechts** oder **beidseitig** sowie mit **einstellbarer Ruhehelligkeit** (leuchtet beim Berühren sofort 100 % hell auf und fadet nach 1 Sekunde Verzögerung sanft aus).
- **Minimalistischer Homescreen:** Linksbündige Uhrzeit (Wecker-Shortcut), Datum & Akkustand (Kalender-Shortcut), optionales Medienplayer-Widget und alphabetisch sortierte **Favoriten**.
- **Zuletzt benutzte Apps (optional):** Einklappbare vertikale Liste der bis zu 5 zuletzt geöffneten Nicht-Favoriten-Apps unterhalb der Favoriten (schließt sich nach App-Start und Rückkehr automatisch wieder).
- **Lines Icon-Pack & Monochrom-Engine:** Lädt weiße Kontur-Icons aus *Lines* (`com.natewren.lines`, `linesfree`, `linesdark`) inkl. nativem Icon-Picker und wandelt alle übrigen App-Icons automatisch in kontraststarke Schwarz-Weiß-Icons um.
- **Pop-up-Menü, Ordner & Umbenennen:** Rechtswisch oder Gedrückthalten auf einer App öffnet Unter-Apps (Pop-up-Ordner), App-Shortcuts, eigene Namen und individuelle Icon-Auswahl.
- **Benachrichtigungsvorschau:** Zeigt aktive Benachrichtigungstexte direkt unter dem jeweiligen App-Namen über den `NotificationListenerService`.
- **Nutzungsstatistik in den Einstellungen:** Zeigt, welche Wellen-Seite (Links/Rechts) häufiger genutzt wird, sowie die meistgestarteten Apps mit direkter `+ Favorit`-Schaltfläche.
- **Nova-Backup-Import (in den Einstellungen):** Übernimmt benutzerdefinierte Icons, App-Namen und Favoriten direkt aus `.novabackup`-Archiven.
- **Zweisprachig (Deutsch & Englisch):** Übernimmt standardmäßig die Systemsprache (mit englischem Fallback) oder lässt sich in den Einstellungen manuell umschalten.
- **Pure AMOLED Black Design:** Tiefschwarzer Hintergrund (`#000000`) mit monochromer Typografie und sofortigem Favoriten-Startup-Cache.

### 📥 Download & Installation

Die signierte Release-APK steht direkt über GitHub Releases bereit:

👉 **[Neueste Version herunterladen (GitHub Releases)](https://github.com/fisch981/MinimalLauncher/releases/latest)**

### 🛠️ Entwicklung & Build

#### Voraussetzungen
- Android SDK (API 35, Min SDK 26)
- JDK 17
- Gradle 8.5+

#### Build-Befehl
```bash
./gradlew assembleRelease
```
Die signierte APK wird unter `app/build/outputs/apk/release/MinimalLauncher-release.apk` erzeugt.

---

## 🇬🇧 English

Minimalist, ergonomic Android launcher with a strict **black-and-white / AMOLED aesthetic**, an alphabetical wave scroller, native **Lines icon pack** support, and automatic monochrome conversion for all apps.

### ⚡ Features

- **Alphabetical Wave Scroller:** Vertical alphabet bar (`★`, `A`–`Z`, `#`) with Gaussian wave displacement, floating letter indicator, and haptic feedback — configurable on the **left**, **right**, or **both sides**, with **adjustable idle brightness** (snaps to 100% brightness immediately on touch and smoothly fades out after a 1-second hold).
- **Minimalist Home Screen:** Left-aligned clock (alarm shortcut), date & battery indicator (calendar shortcut), optional inline media controls, and alphabetically sorted **Favorites**.
- **Recently Used Apps (Optional):** Collapsible vertical list of up to 5 recently launched non-favorite apps below Favorites (automatically collapses on app launch or when returning to the launcher).
- **Lines Icon Pack & Monochrome Engine:** Loads white outline icons from *Lines* (`com.natewren.lines`, `linesfree`, `linesdark`) with native icon picker integration and converts all remaining app icons into high-contrast monochrome bitmaps.
- **Pop-up Menu, Folders & Renaming:** Swipe right or long-press an app to access sub-apps (pop-up folders), app shortcuts, custom display names, and per-app icon overrides.
- **Notification Previews:** Displays active notification text directly beneath app names via `NotificationListenerService`.
- **Usage Statistics in Settings:** Tracks left vs. right wave usage and most-launched apps with a quick `+ Favorite` toggle.
- **Nova Backup Importer (in Settings):** Imports custom icons, app names, and favorites directly from `.novabackup` archives.
- **Bilingual UI (English & German):** Defaults to the system language (with English fallback) and includes a manual language switcher in Settings.
- **Pure AMOLED Black Design:** Pitch-black background (`#000000`), monochrome typography, and an instant-start favorites cache.

### 📥 Download & Installation

The signed release APK is available directly from GitHub Releases:

👉 **[Download Latest Release (GitHub Releases)](https://github.com/fisch981/MinimalLauncher/releases/latest)**

### 🛠️ Development & Build

#### Requirements
- Android SDK (API 35, Min SDK 26)
- JDK 17
- Gradle 8.5+

#### Build Command
```bash
./gradlew assembleRelease
```
The signed APK is generated at `app/build/outputs/apk/release/MinimalLauncher-release.apk`.

---

## 📄 Lizenz / License

Veröffentlicht unter der [MIT-Lizenz](LICENSE) · Copyright (c) 2026 [fisch981](https://github.com/fisch981)  
Licensed under the [MIT License](LICENSE) · Copyright (c) 2026 [fisch981](https://github.com/fisch981)
