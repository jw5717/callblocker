# Play Store submission guide — Call Blocker

Everything the Play Console will ask you for, with the exact answers that match
how this app actually behaves. Nothing here is aspirational — it reflects the
shipped code (no `INTERNET` permission, `allowBackup="false"`, local-only
storage).

---

## 1. Privacy policy

The Console requires a publicly reachable **URL** (not a file). Steps:

1. Host `docs/PRIVACY_POLICY.md` somewhere public and free, e.g.:
   - A GitHub repo → raw file or GitHub Pages, or
   - A GitHub Gist (set to public), or
   - Google Sites / any static host.
2. Paste the resulting URL into **Play Console → App content → Privacy policy**.

Update the contact email in the policy if `jw5717@hotmail.com` isn't the address
you want public.

---

## 2. Data safety form

**Play Console → App content → Data safety.**

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | N/A — no data is transmitted |
| Do you provide a way for users to request that their data is deleted? | **Yes** — uninstalling the app, or the "Clear" button, deletes all local data |

Rationale you can rely on: under Google's definitions, "collection" means
transmitting data off the device. This app reads contacts and records blocked
numbers **only on the device** and never sends anything over a network (it holds
no `INTERNET` permission). On-device-only processing is explicitly **not**
considered collection or sharing, so "No" is the correct and defensible answer.

If the form asks you to confirm on-device processing, state: *"Contacts are read
on-device to match incoming callers; blocked-call records are stored on-device
for the user to review. No data is transmitted, collected, or shared."*

---

## 3. Permissions & sensitive APIs

This app uses two things Google scrutinizes. Neither is in the restricted
SMS/Call-Log permission group, but be ready to explain them in the app review:

**`READ_CONTACTS`** — core functionality. The app's entire purpose is to allow
contacts through and block everyone else, which is impossible without reading
contacts. Make sure your store listing clearly describes this so the use is
obvious to reviewers.

**Call-screening role (`ROLE_CALL_SCREENING`)** — requested at runtime from the
user, not a manifest permission. Apps whose core purpose is call blocking / spam
screening are a permitted use of this role. If prompted for a declaration,
describe the app as a call-blocking app that rejects calls from numbers not in
the user's contacts.

> Note: the app does **not** request `READ_CALL_LOG`, `READ_PHONE_STATE`, or any
> SMS permission, so the "Permissions declaration" form for restricted
> permissions generally does not apply. If the Console surfaces it anyway because
> of the screening role, the description above is your answer.

---

> NOTE: This is a PAID app (one-time purchase, ~$2.99). The app was set up as
> Paid in the Play Console; do not describe it as "free."

## 4. Store listing — short description (max 80 characters)

Use exactly this (61 characters, within the 80-char limit):

> Block unknown callers. No subscriptions, no ads, no tracking.

## 5. Store listing — full description (max 4000 characters)

Call Blocker does one thing, and does it well: it silently blocks phone calls from anyone who isn't in your contacts or on your personal whitelist. No spam, no scammers, no robocalls — just quiet.

But what really sets Call Blocker apart is everything it refuses to do.

What Call Blocker Does NOT Do
- No subscriptions. You buy it once. Wild concept, we know — no monthly fee just to keep a button working.
- No in-app purchases. The price on the listing is the whole price. There's no surprise checkout hiding inside.
- No premium tiers or upsells. No "Pro," no "Plus," no "Ultimate Platinum Edition" holding the good features hostage. You get all of it.
- No "battle passes." It's a call blocker. There's no season pass, no loot box, nothing to grind. We can't believe this needs saying either.
- No ads. Nobody paid to wedge themselves between you and your phone. Refreshing, isn't it?
- No tracking. It doesn't care where you go, what you tap, or how long you stared at the screen.
- No data harvesting. It won't sell your life to "trusted partners" (translation: total strangers). It can't sell what it never collects.
- No piggybacking. No other app can hitch a ride to skim your data — because this one literally cannot reach the internet. I removed its ability to make a network connection. It couldn't phone home if it wanted to.

Somewhere along the way, even flashlight apps started demanding your location, your contacts, and $4.99 a month. Call Blocker is the stubborn opposite: a plain tool that does its job and then gets out of your way.

What It Does
- Blocks every call that isn't from one of your saved contacts or a number you've whitelisted inside the app.
- Rejects unwanted calls silently — no ring, no interruption.
- Keeps a blocked-call log so you can see exactly who it stopped, grouped by day, with the time each call came in.
- Lets you whitelist any number straight from the log, so a specific caller can get through without adding them to your contacts.
- Puts you in control with a one-tap on/off switch.
- Optional alerts — if you want, it can quietly notify you when a call is blocked. Off by default.

How It Works
Call Blocker uses Android's built-in call-screening system. When a call comes in, it checks the number against your contacts and your whitelist. If there's a match, it rings normally. If not, it's turned away quietly and added to your log. That's it.

Permissions
Call Blocker asks only for what it genuinely needs:
- Contacts — read-only, used solely to recognize your contacts when a call arrives. They are never stored or sent anywhere.
- Notifications — only if you turn on blocked-call alerts.

It does not request internet access, location, microphone, SMS, or your call log. It can't, and it won't.

Privacy By Design
Everything happens on your device. No servers, no accounts, no sign-ups, no cloud. Because the app has no internet permission, your information simply cannot leave your phone — not by accident, not by design, not ever.

Call Blocker. It just blocks calls. Nothing else.

---

## 6. Graphic assets (already generated)

All in the `play-assets/` folder, sized correctly for the Console:

- `icon-512.png` — 512×512 app icon
- `feature-graphic-1024x500.png` — 1024×500 feature graphic
- `screenshot-1-home.png`, `screenshot-2-log.png` — 1440×2880 phone screenshots (2:1, Play-compliant)

## 7. Pre-launch checklist

Done (in this project):
- [x] `targetSdk = 36` (Android 16 requirement).
- [x] Signed release App Bundle builds: `app/build/outputs/bundle/release/app-release.aab`.
- [x] Upload keystore created (`release-key.jks` + `keystore.properties`).
- [x] Verified on a physical device: contacts allowed, non-contacts blocked and logged.
- [x] Graphic assets generated (see section 6).
- [x] Short + full descriptions written (sections 4–5).
- [x] Data safety answers prepared (section 2) — no data collected.

Still to do (your side, in Play Console / online):
- [ ] **Back up `release-key.jks` and its password** somewhere safe (critical — can't be recovered).
- [ ] Host `docs/PRIVACY_POLICY.md` at a public URL and paste it into the Console.
- [ ] Create the Play developer account ($25, identity verification).
- [ ] Create the app, complete the "Set up your app" checklist, build the listing.
- [ ] Run **Internal testing** first, then the **Closed test** (≥12 testers, 14 consecutive days).
- [ ] Apply for production; create the production release.
- [ ] Bump `versionCode` in `app/build.gradle.kts` before every new upload after the first.
