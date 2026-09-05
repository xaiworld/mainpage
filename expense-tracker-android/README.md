# Xai & Nat Expenses

A small Android app for two people (hardcoded as **Xai** and **Nat**) to log shared
expenses, split them by any ratio, keep both phones in sync just by being near each
other, and export the running list to WhatsApp.

## Features

- **Log an expense**: amount, date, who paid, an optional note.
- **Flexible split**: pick a preset (50/50, 60/40, 40/60, "Xai pays all", "Nat pays
  all") or drag a slider for any custom ratio, per expense.
- **Running balance**: the top of the list always shows "Xai owes Nat $X" / "Nat owes
  Xai $X" / "All settled up!", computed from every logged expense.
- **Sync by closeness**: tap "Sync" on both phones while they're near each other and
  they exchange their expense lists directly — no Wi-Fi network, internet connection,
  server, or account required.
- **Export to WhatsApp**: one tap turns the whole list (plus the balance) into a
  formatted text message and opens WhatsApp with it ready to send.

## Why Nearby Connections for sync

The task is literally "two phones close to each other, occasionally, with no shared
backend" — that's exactly the scenario Google's **Nearby Connections API**
(`com.google.android.gms:play-services-nearby`) is built for:

- It automatically picks whichever of Bluetooth, Bluetooth LE, or a local Wi-Fi
  hotspot actually works between the two devices — you don't have to think about
  which radio to use.
- No internet connection, server, or account is needed, which fits a private
  household expense list much better than building a backend.
- Both phones can advertise *and* discover at the same time (`P2P_CLUSTER`
  strategy), so it doesn't matter who taps "Sync" first.

On top of that transport, the sync protocol is intentionally simple and robust:

1. When two phones connect, **each one sends its entire expense list** (including
   soft-deleted "tombstone" rows) to the other, as one small JSON payload.
2. The receiver merges it in **last-write-wins per expense id**: every expense
   carries a `lastModified` timestamp, and whichever copy of a given expense is
   newer wins.
3. Deleting an expense doesn't remove the row locally — it flips an `isDeleted`
   flag and bumps `lastModified`. That tombstone then propagates to the other phone
   on the next sync, so a delete on one phone reliably removes it from the other
   too.

Because both phones always send their *full* state and merge with the same rule,
they converge to the same list regardless of which phone made which change first,
with no central source of truth needed. See `NearbySyncManager.kt` and
`ExpenseRepository.mergeIncoming()` for the implementation.

## Project layout

```
app/src/main/java/com/xaiworld/expensetracker/
├── data/            Room entity/DAO/database, repository, balance math, prefs
├── sync/            Nearby Connections wrapper, JSON wire format, permissions
├── export/          WhatsApp text export
├── ui/              Jetpack Compose screens (list, add-expense, sync, user picker)
└── MainActivity.kt  Hosts the Compose UI, handles runtime permission requests
```

## Building

This is a standard Gradle/Android Studio project:

1. Open the `expense-tracker-android/` folder in Android Studio (Iguana or newer).
2. Let it sync Gradle (it needs network access to `google()` and `mavenCentral()`
   the first time, to download the Android Gradle Plugin, Compose, Room, and Nearby
   Connections libraries).
3. Run on two physical Android phones (minSdk 26 / Android 8.0+) — Nearby
   Connections needs real radios, so an emulator can't sync with anything.

> The sandbox this project was written in has no Android SDK and its network policy
> blocks Google's Maven repository, so the code could not be compiled here. It was
> written and reviewed carefully against the real Compose/Room/Nearby Connections
> APIs, but give it a first build-and-run pass in Android Studio before relying on
> it, in case anything needs a small tweak for the exact toolchain version you have.

## First run on each phone

The first time the app launches (or any time from the person icon in the top bar),
it asks "Which phone is this?" — pick **Xai** on one phone and **Nat** on the other.
That choice:

- sets the default "paid by" value when logging a new expense on that phone, and
- is the name the phone advertises itself with during sync, so the other phone shows
  "Found Nat" instead of a random device id.

## Permissions

Nearby Connections needs Bluetooth/Wi-Fi-adjacent permissions, requested at runtime
the first time you tap "Sync" (see `NearbyPermissions.kt` for exactly which ones,
which vary by Android version). Everything else needs no special permissions.

## Notes / possible follow-ups

- Currency is unlabeled (just `12.50`) — add a currency symbol/locale formatting in
  `formatAmount()` (in `ui/App.kt` and `export/WhatsAppExporter.kt`) if you want one.
- There's no edit-existing-expense screen yet, only add + delete; wiring "tap a row"
  to reopen `AddExpenseDialog` pre-filled would be a small addition.
- All data lives only in each phone's local Room database; sync is the only way
  data moves between phones, and it only happens when you tap "Sync" — there's no
  automatic background sync.
