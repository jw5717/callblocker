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
- [ ] User has "a couple of ideas to make it better" — capture them here when shared.
- [ ] Add a 512 Play Store icon variant / tablet screenshots if ever desired.

## Done
- Core app (contacts-only blocking, day-grouped log, whitelist, toggle, optional
  block notifications, custom icon), signed release build, GitHub repo, landing
  page + privacy policy, Play listing + assets, Internal testing upload.
- **v2** (versionCode 2 / 1.1): split the log screen into **Blocked** + **Whitelist**
  tabs; whitelist is now managed on its own tab and is never affected by "Clear".
- **v3** (versionCode 3 / 1.2): renamed the launcher label to **"Simple Call Blocker"**
  (matches the Play listing); updated landing page title/hero/footer.
