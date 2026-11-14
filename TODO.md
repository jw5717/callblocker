# TODO / backlog

Planned changes. Several of these are intentionally held back to roll out as
updates **during the 14-day closed test**, to demonstrate active development
(Google wants to see a few updates during testing).

## Queued for mid-test updates

(none right now)

## Optional / future (not required to ship)

- [ ] Enable R8 minification + upload the mapping file and native debug symbols
      (smaller download, cleaner crash reports). Currently optimization is off.
- [ ] Add a 512 Play Store icon variant / tablet screenshots if ever desired.

## Done
- Core app (contacts-only blocking, day-grouped log, whitelist, toggle, optional
  block notifications, custom icon), signed release build, GitHub repo, landing
  page + privacy policy, Play listing + assets, Internal testing upload.
- **v2** (versionCode 2 / 1.1): split the log screen into **Blocked** + **Whitelist**
  tabs; whitelist is now managed on its own tab and is never affected by "Clear".
- **v3** (versionCode 3 / 1.2): renamed the launcher label to **"Simple Call Blocker"**
  (matches the Play listing); updated landing page title/hero/footer.
