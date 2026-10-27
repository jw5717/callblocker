# TODO / backlog

Planned changes. Several of these are intentionally held back to roll out as
updates **during the 14-day closed test**, to demonstrate active development
(Google wants to see a few updates during testing).

## Queued for mid-test updates

- [ ] **Rename app to "Simple Call Blocker"** (the Play listing name is already
      "Simple Call Blocker"; this syncs the rest):
  - Launcher label → `app/src/main/res/values/strings.xml` (`app_name`, currently "CallBlocker")
  - Landing page title + hero → `docs/index.html`
  - References in `docs/` and `CLAUDE.md`
  - Bump `versionCode` → 2, rebuild `bundleRelease`, re-upload.

## Optional / future (not required to ship)

- [ ] Enable R8 minification + upload the mapping file and native debug symbols
      (smaller download, cleaner crash reports). Currently optimization is off.
- [ ] Add a 512 Play Store icon variant / tablet screenshots if ever desired.

## Done
- Core app (contacts-only blocking, day-grouped log, whitelist, toggle, optional
  block notifications, custom icon), signed release build, GitHub repo, landing
  page + privacy policy, Play listing + assets, Internal testing upload.
