# TODO / backlog

Planned changes. Several of these are intentionally held back to roll out as
updates **during the 14-day closed test**, to demonstrate active development
(Google wants to see a few updates during testing).

## Queued for mid-test updates

(none right now)

## Optional / future

- [ ] **Enable R8 (minify + obfuscate + shrink resources)** — Play Console flags
      "Obfuscation 0%, below 25% threshold" with a deadline of **~Feb 2027**. Not
      blocking, but do it before then. IMPORTANT: install the optimized release
      build on a device and verify it runs (screening service + UI) before
      shipping to testers — obfuscation can break things. Mapping file is bundled
      by AGP automatically, which also clears the "no deobfuscation file" warning.
- [ ] Add a 512 Play Store icon variant / tablet screenshots if ever desired.

## Play policy items to watch (from Oct 29, 2026 policy announcement)

- [ ] **Contacts permission declaration** — new "Manage Contacts Permission" policy
      requires a Play Console declaration to use READ_CONTACTS. App QUALIFIES (a
      call blocker must auto-check every incoming call against all contacts; a
      system contact picker is insufficient). Fill in the declaration in the
      Console (App content / Dashboard) if/when the task appears. ~30+ day window.
- [ ] **Trader status declaration** (required for paid apps from Nov 2026) — answer
      in Console; individual hobbyist seller is typically "non-trader."
- [ ] Japan payment authorization by Dec 1, 2026 — ONLY if selling into/out of
      Japan. Likely N/A (US-focused), but confirm.

## Explored and ruled out (do NOT re-chase)

- Auto-delete short/dead-air voicemails: NOT feasible. Traditional voicemail lives
  on the carrier (no API); visual voicemail requires becoming the default dialer +
  voicemail permissions — too invasive, wrecks the app's minimal-permission pitch.
  User's phone may have built-in spam-voicemail filtering instead.
- Keyword/content SMS spam blocking: NOT feasible. Requires becoming the default
  SMS app + the most-restricted permission group on Play. Samsung Messages already
  has "Block phrases" built in — use that instead.

## Done
- Core app (contacts-only blocking, day-grouped log, whitelist, toggle, optional
  block notifications, custom icon), signed release build, GitHub repo, landing
  page + privacy policy, Play listing + assets, Internal testing upload.
- **v2** (versionCode 2 / 1.1): split the log screen into **Blocked** + **Whitelist**
  tabs; whitelist is now managed on its own tab and is never affected by "Clear".
- **v3** (versionCode 3 / 1.2): renamed the launcher label to **"Simple Call Blocker"**
  (matches the Play listing); updated landing page title/hero/footer.
