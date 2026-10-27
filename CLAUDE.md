# CallBlocker2 — project guide

Android call-blocking app for personal use by the owner (jw5717), headed for the
Google Play Store. **Read this before making any enhancement.**

## What it does

Only the user's **contacts** (and explicitly **whitelisted** numbers) ring
through. Everyone else is **rejected silently** and recorded in an on-device log.

Original requirements (all implemented):
- Allow only contacts to ring through.
- Blocked-call **log screen**, viewable anytime, with timestamps, grouped by day
  ("Today" / "Yesterday" / date), with a "Blocked number" / "Whitelist" column header.
- **Whitelist** a number from the log via a checkbox (bypasses contacts check).
- **Master on/off toggle**.
- Optional **"notify when a call is blocked"** toggle — status-bar notification,
  **defaulted OFF**.
- Play Store ready, privacy-first: nothing tracked, no data leaves the device,
  no other app can piggyback.

## Architecture (minimal by design — no Room/Hilt/Nav libraries)

- `app/src/main/java/com/jw/callblocker/`
  - `CallScreeningServiceImpl.kt` — the `CallScreeningService`. `onScreenCall`
    decides allow vs block. Incoming-only; crash-safe (allows the call on any
    error so a call is never left hanging). Blocks → logs + optional notification.
  - `data/CallStore.kt` — **single source of truth**. A singleton backed by
    `SharedPreferences`, exposing `StateFlow`s for `enabled`, `notifyOnBlock`,
    `whitelist`, and `blockedCalls`. Holds the `BlockedCall` data class. No
    database, no network. UI and service share this one instance (same process).
  - `MainActivity.kt` — Compose host. Requests `READ_CONTACTS` + the
    call-screening role, requests `POST_NOTIFICATIONS` when the notify toggle is
    switched on (Android 13+), and switches between HOME and LOG screens.
  - `ui/AppScreens.kt` — `HomeScreen` (status text, two toggle cards, "View
    blocked calls" button) and `LogScreen` (day-grouped list, column header,
    per-row whitelist checkbox).
  - `ui/theme/` — Compose Material3 theme.
- `app/src/main/AndroidManifest.xml` — permissions `READ_CONTACTS`,
  `POST_NOTIFICATIONS`; service guarded by `BIND_SCREENING_SERVICE`;
  `allowBackup="false"`; **no INTERNET permission**.
- `app/src/main/res/drawable/ic_launcher_foreground.xml` + `ic_launcher_background.xml`
  — custom icon (blue gradient, white phone, red "no-calls" ring+slash).
  `ic_notification.xml` — white monochrome small icon for notifications.
- `docs/` — `PRIVACY_POLICY.md`, `PLAY_STORE_SUBMISSION.md`.
- `play-assets/` — `icon-512.png`, `feature-graphic-1024x500.png`, two screenshots.

## Hard constraints — do NOT violate

- **Never add the INTERNET permission** or any networking/analytics/ads/3rd-party
  SDK. The "can't send data off device" guarantee is the core privacy promise and
  a Play Store selling point.
- Keep `allowBackup="false"`.
- Don't export new components. The only exported pieces are the launcher activity
  and the system-guarded screening service.
- **Don't log PII** (phone numbers, contact names) in committed code. Such logging
  was used temporarily for debugging and removed; keep it out of releases.

## Behavior notes / gotchas

- Contact matching uses `ContactsContract.PhoneLookup`, which matches across **all**
  contact accounts (device, Google, Google Meet/Duo "tachyon", Samsung). The user
  deliberately chose to keep all accounts (not filter to device-only). Consequence:
  a number can still match even after being "deleted" if a **linked copy** (often
  the Google Meet/Duo account) still holds it. This is correct behavior, not a bug.
- `adb shell content query` runs as a different uid than the app and may NOT see
  contacts the app sees (e.g. Meet/Duo rows). Trust the app's view, not the shell's.
- Whitelist comparison uses `PhoneNumberUtils.compare` (handles formatting).
- Blocked log: newest-first, capped at 500 entries, stored as JSON in prefs.

## Build / run / deploy (Windows, PowerShell)

- JAVA_HOME is not global — set it: `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"`
- Debug build: `.\gradlew.bat :app:assembleDebug`
- Signed release bundle: `.\gradlew.bat :app:bundleRelease`
  → `app/build/outputs/bundle/release/app-release.aab`
- Signing reads `keystore.properties` (git-ignored) → `release-key.jks`
  (git-ignored). **Both exist only on this machine; back them up — they are not
  recoverable.** Bump `versionCode` in `app/build.gradle.kts` before every Play
  upload after the first.
- Android SDK: `C:\Users\james\AppData\Local\Android\Sdk`; adb under `platform-tools`.
- Device is connected over **wireless adb**; the port changes — rediscover with
  `adb mdns services`, then `adb connect <ip:port>`.
- Git is at `C:\Program Files\Git\cmd` (not on PATH in fresh shells; prepend it).
  Remote: https://github.com/jw5717/callblocker (origin/main).

## Tech

Kotlin, Jetpack Compose (Material3), `minSdk 29`, `targetSdk 36`, `compileSdk 36`.
Only extra dependency beyond Compose/AndroidX: `kotlinx-coroutines-core` (for StateFlow).

## Backlog

See `TODO.md` for queued changes — notably some are intentionally held back to
roll out as updates **during the closed test** (e.g., renaming the launcher label
to "Simple Call Blocker"; the Play listing is already named that). Check it when
picking up enhancement work.

## Enhancement workflow

After changes: build (`assembleDebug`) to verify, optionally install to the device
to confirm behavior, then commit + push to GitHub. If a change affects the release,
rebuild `bundleRelease` and bump `versionCode`.
