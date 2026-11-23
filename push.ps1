param (
    [string]$ChangeSummary = "Automatisches Update",
    [string]$BumpType = "none" # none, patch, minor, major
)

$ErrorActionPreference = "Stop"

# 1. Versionierung in app/build.gradle.kts auslesen
$gradleFile = "app/build.gradle.kts"
$content = Get-Content $gradleFile -Raw

if ($content -match 'versionCode\s*=\s*(\d+)') {
    $currentCode = [int]$matches[1]
    $newCode = $currentCode
} else {
    $currentCode = 1
    $newCode = 1
}

if ($content -match 'versionName\s*=\s*"([^"]+)"') {
    $currentName = $matches[1]
    $newName = $currentName
} else {
    $currentName = "1.0.0"
    $newName = "1.0.0"
}

if ($BumpType -ne "none") {
    $newCode = $currentCode + 1
    $parts = $currentName.Split('.')
    $major = [int]$parts[0]
    $minor = [int]$parts[1]
    $patch = [int]$parts[2]

    if ($BumpType -eq "major") {
        $major++; $minor = 0; $patch = 0
    } elseif ($BumpType -eq "minor") {
        $minor++; $patch = 0
    } else {
        $patch++
    }
    $newName = "$major.$minor.$patch"

    Write-Host "==> Erhöhe Version: $currentName (Code $currentCode) -> $newName (Code $newCode)" -ForegroundColor Cyan
    $content = $content -replace 'versionCode\s*=\s*\d+', "versionCode = $newCode"
    $content = $content -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$newName`""
    Set-Content -Path $gradleFile -Value $content -NoNewline

    # CHANGELOG.md aktualisieren
    $changelogFile = "CHANGELOG.md"
    $today = (Get-Date).ToString("yyyy-MM-dd")
    $newEntry = @"
## [$newName] - $today

### Deutsch
- $ChangeSummary

### English
- $ChangeSummary

"@

    $existingChangelog = Get-Content $changelogFile -Raw
    if ($existingChangelog -match '(?s)(# Changelog.*?---\s*\r?\n\r?\n)(.*)') {
        $updatedChangelog = $matches[1] + $newEntry + "`n" + $matches[2]
        Set-Content -Path $changelogFile -Value $updatedChangelog -NoNewline
        Write-Host "==> Changelog für v$newName aktualisiert" -ForegroundColor Cyan
    }
}

# 2. Release APK kompilieren
Write-Host "==> Kompiliere Release-APK v$newName..." -ForegroundColor Cyan
$gradleBat = Join-Path $env:LOCALAPPDATA "AndroidBuildTools\gradle\gradle-8.9\bin\gradle.bat"
if (Test-Path $gradleBat) {
    & $gradleBat assembleRelease
} else {
    .\gradlew.bat assembleRelease
}

$apkFile = Join-Path $PSScriptRoot "app\build\outputs\apk\release\MinimalLauncher-release.apk"
if (-not (Test-Path $apkFile)) {
    $fallbackApk = Join-Path $PSScriptRoot "app\build\outputs\apk\release\app-release.apk"
    if (Test-Path $fallbackApk) {
        $apkFile = $fallbackApk
    } else {
        Write-Error "Release APK konnte nicht unter $apkFile erstellt werden!"
    }
}

# 3. Git Stage, Commit & Push
Write-Host "==> Stage & Commit..." -ForegroundColor Cyan
git add -A
git commit -m "Release v${newName}: $ChangeSummary"

Write-Host "==> Pushe nach GitHub (origin main)..." -ForegroundColor Cyan
git push origin main

Write-Host "==> Veröffentliche Release-APK auf GitHub Releases (v$newName)..." -ForegroundColor Cyan
$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
gh release view "v$newName" 2>$null | Out-Null
$releaseExists = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = $prevEap
if ($releaseExists) {
    gh release upload "v$newName" "$apkFile#MinimalLauncher-release.apk" --clobber
} else {
    gh release create "v$newName" "$apkFile#MinimalLauncher-release.apk" --title "MinimalLauncher v$newName" --notes "$ChangeSummary"
}
if ($LASTEXITCODE -ne 0) {
    throw "GitHub Release Upload für v$newName fehlgeschlagen!"
}

Write-Host "==> Erfolgreich veröffentlicht & gepusht: v$newName (Code $newCode)" -ForegroundColor Green
