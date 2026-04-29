# Brass: Lancashire — Development Changelog

## 368 versions of iterative development

### Hall of Fame Counts Historic Canals Correctly (v1.0.11)
- **Canals built were going uncounted in older games** because canal links get removed from the board at the canal→rail era transition — by the time a finished game is read, the engine no longer sees any link with `type: 'canal'` and the classifier returned null. Result: "Canals Built" and "Avg Canals / Game" trophies showed unclaimed even when players had built many canals across past games.
- **Fix**: the classifier now uses three signals in priority order — (1) rail-only payload fields (resourcePlan / coalSources / coalEnd / coalEnd2 / secondLinkId) → rail; (2) state has the link with a type set → use that; (3) link missing from state and no rail-specific fields → default to canal (the era-removed canal case). All historic finished games' canal builds now contribute to the canal trophies.
- **Caveat**: very old rail builds where the player let the engine auto-pick the coal source (no resource fields in payload) could in principle be misclassified as canals; this only matters for actions taken before the resource picker existed (v0.0.170). The trade-off favors counting canals correctly going forward over getting a few historic auto-pick rails right.

### Trophy News + Wider i18n + Newest-First Changelog (v1.0.10)
- **News panel announces trophy ownership changes**: when a Hall of Fame trophy moves from one player to another (or is claimed for the first time), the lobby News feed shows a fresh entry: `alice took the Cotton Mills Flipped trophy (12) from bob`. Multiple new co-holders are joined with `+`. Detected by diffing each Hall of Fame recompute against the previous cache.
- **Changelog now ordered newest-first**: the most recent release sits at the top of `CHANGELOG.md`, no more scrolling to the bottom. The whole file was reordered in one pass — older entries preserved in place, just flipped.
- **Wider lobby translations**: roughly twenty additional strings on the lobby get translated in all 9 languages — Create New Game, Quick Start (vs bots), the three Quick 2P/3P/4P buttons, Invite Players, Achievements badge text, Active games / Waiting / Finished system counters, Players label, Recent feedback, the Recent + A-Z sort buttons, the up-to-date / join-or-create banner links, and the new "took the X trophy" / "from" pieces used by the trophy news entries.
- **The 502 you may have seen** during the v1.0.9 deploy was transient — the live site is now serving v1.0.9 fine. With the health-check path properly configured, the platform should keep the previous container serving until the new instance is healthy; if 502s persist, double-check the dashboard's "Health Check Path" actually shows `/health` (the platform sometimes ignores updates to its own setting until a manual sync).

### Hall of Fame Expansion + i18n + Trophy Owner Highlight (v1.0.9)
- **Money spent now reflects the true total** including market resource costs (coal/iron purchases). The engine records lifetime spend per player on every money deduction in build, canal, single-rail, and double-rail actions; the Hall of Fame reads it from the final game state. Older games without the field fall back to the v1.0.8 proxy (industry tile cost + link base cost only).
- **Ties shared between players**: when two or more players reach the same value for a metric, all of them hold the trophy together — the panel shows "alice + bob" instead of arbitrarily breaking ties on user ID. Holders sorted alphabetically.
- **15 new "average per game" trophies** with 2-decimal precision. Same per-action stats as the cumulative ones (industry flips, loans, passes, mills sold, distant sells, distant -4 / 0 tiles, money spent, canals, rails, links) but divided by the number of all-human finished games each player played in. Listed under a new "Per-Game Averages" group at the bottom of the panel.
- **Trophy owner is the highlight**: the holder's name is now the visually prominent line in each trophy row — gold, larger, bold — with the metric label demoted to a smaller uppercase caption above it. Claimed trophies also get a subtle gold gradient and a thicker gold border so they stand out from unclaimed ones at a glance.
- **i18n with 9 languages**: a new picker on the account page sets the interface language. Choices: English (default), Español, Galego, Català, Valencià, Asturianu, Euskara, Français, Deutsch, Italiano. Translations cover the navbar, lobby panel headings, Hall of Fame group labels, color names, account page, and common buttons. Game-page text, the in-game wiki, individual trophy labels, and achievement names remain English in this revision and will be translated in follow-ups.

### Hall of Fame (v1.0.8)
- **28 trophies** displayed in a panel above the News feed (right column on desktop, hoisted to the top above News on mobile, both collapsible). Each trophy is held by exactly one player at any given time.
- **Only all-human games count** — bot-mixed games are excluded from every metric so trophies reflect head-to-head play.
- **Categories**:
  - **Ratings**: highest ELO in 2P, 3P, and 4P (3 trophies).
  - **Activity**: most achievements earned, longest streak, highest single-day turn count (3).
  - **Games Played**: most 2P, 3P, and 4P games played (3).
  - **Records**: highest VP achieved in a single 2P, 3P, and 4P game (3).
  - **Industry**: most flipped tiles owned per industry — Cotton Mills, Coal Mines, Iron Works, Ports, Shipyards (5). Attribution is by tile owner, regardless of who triggered the flip — your coal mine flipping because an opponent drained it still counts for you.
  - **Money**: most loans taken, most passes, most money committed to permanent infrastructure (industry tiles + link costs) in a single game (3).
  - **Selling**: most mills sold (any method), most sold to the distant market, most -4 distant tiles taken, most 0 distant tiles taken (4).
  - **Building**: most canals built, most rails built, most total links built (3).
  - **Battle**: most opponents with strictly higher pre-game ELO defeated (1).
- **Lazy recompute**: cached server-side, refreshed automatically when a game finishes or an achievement is granted, with a 60-second TTL fallback. The lobby polls the panel every 2 minutes.
- **Peak-day turn tracking**: a new per-user counter records the highest number of completed turns in a single UTC day. Backfilled from the action log on first boot.

### Daily Counter Counts Turns, Not Actions (v1.0.7)
- **🎲 badge now reflects completed turns**: Wild Build counts as 1 (one turn, even though it spends both actions), two loans count as 1 (both actions are part of the same turn), a single action that uses up the round-1 canal-era turn counts as 1.
- **Trigger point**: the counter increments only at the moment a player's turn definitively ends — when their `currentPlayerIndex` slot moves to someone else, the game finishes, or after they hit Confirm if the confirm-turn flow was on. Partial sell-cotton submissions and mid-turn actions don't tick.
- **Backfill recount**: existing daily counts are re-derived from today's action log using turn semantics (consecutive same-user actions in the same game = one turn). The previous per-action cache is invalidated and re-runs on the next boot.

### Per-Viewer Favorite Color (v1.0.6)
- **Pick your favorite color on the account page** — your own seat is recolored everywhere on your device (board, player bar, mat, logs, chat, troll overlay, panels, admin tools) so you always see yourself in the color you like.
- **Smart swap**: if your favorite is one of the four canonical seat colors and another seat already has it, that seat takes your old color in your view, so the four players still appear in four distinct colors. If your favorite is one of the three new ones (Black, Blue, White), only your seat changes — no other seat shifts.
- **Per-viewer only**: the actual game state still records canonical seat colors. Two players in the same game can both pick the same favorite and each see themselves in it; what they see for the *other* player is unchanged from each viewer's perspective.
- **Seven choices**: the original four (Red, Purple, Green, Yellow) plus Black, Blue, and White. The new three are favorite-only — never auto-assigned to a seat at game creation.
- **Default option** keeps the canonical seat-color behavior, no remap.

### Maintenance Page Wire-up (v1.0.5)
- **Hosted copy of the maintenance page** on a separate static site so it can be set as the deployment platform's "Custom Maintenance Page URL". The platform requires the maintenance URL to live on a different service than the one in maintenance, so the in-app preview alone wasn't enough.
- **Public mirror of the changelog** alongside the maintenance page so the page can fetch release notes without needing repo access. The original direct fetch was returning 404 because the source repo isn't public.
- **Auto-mirror on changelog change**: Every push that touches the development changelog automatically pushes a copy to the public mirror, so the maintenance page's "What's changing" panel always reflects the most recent release.
- **Changelog rewritten in user-facing language**: Internal file paths, function names, identifiers, and URLs scrubbed across the whole document so the public mirror reads as release notes rather than as engineering notes.
- **Deployment platform wiring**: One-time dashboard steps — paste the maintenance URL into the Custom Maintenance Page field, paste `/health` into the Health Check Path field, leave the Maintenance Mode toggle off for normal operation. The toggle is flipped on only for genuine planned downtime.
### Action Submenu Navbar-Overlap Fix (v1.0.4)
- **Cascading Done/Cancel panel always sits above the navbar** regardless of stacking-context surprises, with explicit pointer events so its buttons stay clickable.
- **Top clearance** keeps the submenu from ever landing on top of (or behind) the desktop navbar when the anchor is near the top of the right sidebar. The clamp is enforced before and after the off-screen adjustment, so horizontal repositioning can't slide it back into the navbar zone.
- **Off-right guard**: When the submenu would overflow the right edge, the leftward-shift fallback now keeps it fully on screen on narrow viewports where neither side has enough room.

### Maintenance Page (v1.0.3)
- **A self-contained HTML maintenance page** that auto-fetches the latest section of the development changelog and renders it with the game's color palette (gold on dark blue), a spinner, a version pill, and a 20-second auto-refresh, so the user lands back on the live site as soon as the deploy is done.
- **Local preview route** so the page is reachable at a clean URL during development.

### Turns-Today Backfill (v1.0.2)
- **Daily recount on first boot of each new UTC day**: Walks the action log for entries dated today and writes the count back to each non-bot user. Means newly deployed counters aren't blank for users who'd already played turns earlier in the same day.
- **Cached** so subsequent restarts on the same day don't redo the work.

### Turns-Today Counter (v1.0.1)
- **Per-user counter** of how many actions you've taken today, incremented every time you act. Resets at UTC midnight.
- **Navbar 🎲 badge** on every logged-in page shows the current user's count for today.
- **Lobby Players list** has a new 🎲 N blue-tinted badge per row (dimmed when zero) plus a matching "🎲 Today" sort button. Sort descends by today's count, ties alphabetical.
- **Day boundary**: Count is keyed to today's date, so yesterday's number doesn't bleed into today's badge before a user has acted today.

### v1.0.0 — Out of Early Testing
- **Version bumped from 0.0.356 to 1.0.0**. The game has been stable enough across 356 iterative releases (full ruleset, 2/3/4-player, neural-net bots, mobile, achievements, ELO, news feed) to leave alpha-style versioning behind.
- **Removed the early-testing banner** and its styling. Lobby content now starts immediately under the navbar.
- **Version badge in the navbar** on every page — gold rounded pill on the right side of the brand block.
- **Mobile**: Navbar version is hidden, and a footer version line at the bottom of the lobby is shown instead — "in the top bar on desktop, in the bottom on phone".
- **Mobile container top-padding** added (was missing — the old beta banner had been providing the offset for the fixed navbar; without it the first content was riding under the navbar).

### News Panel Mobile Spacing (v0.0.356)
- **Margin added** to the lobby news panel on mobile and a small top margin on the cover image, so the News panel and the Brass cover image no longer visually touch.

### News Panel: Collapsible + Mobile Hoist (v0.0.355)
- **News heading is now collapsible** — same pattern as Feedback / Players / System Data; click the header to fold or unfold. The refresh button no longer triggers the toggle.
- **Mobile auto-hoist**: The panel is moved out of the lobby layout and inserted above it (right under the top banner) instead of stacking below the games column where it was easy to miss. Starts collapsed by default on mobile so it advertises its existence without pushing games down.

### Wider Lobby Center Column (v0.0.354)
- **Lobby main area widened** from 900 px to 1400 px on desktop. With a 400 px left sidebar and a 300 px right news panel, the games column had been getting squeezed to under 200 px; it now opens up to around 600 px.
- **Other pages unchanged** — login, profile, game, and account still cap at the original width since they're single-column.

### Lobby News Feed (v0.0.353)
- **News panel on the lobby** — sticky on desktop (right side), color-coded per event type. Stacks below other content on mobile.
- **Three event sources** push into a rolling 200-entry feed:
  - **🏆 Achievement earned** — one news entry per newly-granted achievement.
  - **👑 Game won** — one entry per non-bot winner (ties produce multiple entries).
  - **🔥 Streak record** — fired when a user's current streak passes the all-time record. Tracks the previous holder so the entry reads "set a new all-time streak record: 12 days (was 7 by bob)".
- **Polling**: Lobby fetches the news every 30 s and flashes any newly-arrived items with a brief gold fade.
- **Initial render is server-side** (the first 30 entries come baked into the panel) so it appears immediately; the JS poller takes over for subsequent updates.
- **Hotfix in the same version**: A broken if/else chain in the template was throwing at render time and producing a 500 across the lobby. Wrapped the icon and title computation correctly.

### Zero-Downtime Deploys (v0.0.352)
- **Deploy config now declares a health-check path** so the platform keeps the old container serving traffic until the new instance is healthy, eliminating the brief 502 window that opened during deploys.
- **Health endpoints registered before any other middleware** so the health probe answers the moment the server is listening, with no wait on session store or database init.
- **Start command simplified** — the boot-time compactor already runs at server start, so the chained pre-start scripts were duplicating work and lengthening the deploy gap.

### Potential VP Display (v0.0.351)
- **Potential VP pill on each player's VP hex** (gold "+N"), representing what they'd score if every currently-on-board tile flipped (theirs and opponents' in their link locations, since link VP counts neighbors regardless of owner).
- **Calculation**: Sum of tile VP for every owned slot regardless of flipped status, plus 1 link-VP per owned-or-opponent tile in connected locations of every owned link, plus money/10 — i.e. the upper bound if the era ended right now and every tile flipped.
- **VP breakdown popup** also shows a Potential row in dashed gold beneath the existing Projected row.

### Lobby Player Order by VP (v0.0.350)
- **Player names in each lobby game row are now sorted by VP, highest first** — winner is always leftmost in finished games, current leader is leftmost in active games. Waiting games keep their original seat order.

### Client Board-Data Sync (v0.0.349)
- **Client copy of board data resynced** with the server copy for all link segment counts and canal/rail flags. The client was stuck on older values for several waypoint and external-port links, so the UI was displaying inflated link costs (e.g. £10 instead of £5) — server pricing was already correct; this is a UI-only correction.

### Distant-Sell Stuck Turn Fix (v0.0.348)
- **Distant-market sale that was the last action** could leave the UI stuck on the target picker and never advance the turn. Cause was a re-entry guard collision in the auto-finish path; fixed by deferring the auto-finish so the outer flag clears first.

### Streak Sort + Per-Player Streak Badge (v0.0.347)
- **🔥 N badge** on every row in the lobby Players list — current streak in days, red-tinted when positive, dimmed at zero.
- **🔥 Streak sort button** added next to the others; ties alphabetical.

### Player Links + Wider Sidebar (v0.0.346)
- **Player names in the lobby Players list are now links** to that user's stats page. Replaces the previous tiny "stats" link.
- **Achievement badge** is also a link to the same profile page.
- **Lobby sidebar doubled in width on desktop** — the ELO, trophy, and activity badges no longer wrap aggressively. Mobile layout unchanged.

### Per-Player VP + Trophy in Lobby (v0.0.345)
- **Each player name in lobby game rows** now shows their current or final VP (e.g. "alice 42 VP"). Active games pull from live state; finished games from the stored result.
- **Trophy 🏆 prefix** on the winner's name in finished-game rows for at-a-glance spotting.

### Backfill Achievements (v0.0.344)
- **Admin backfill endpoint** replays every finished all-human game in chronological order, evaluates achievements for each player against the archived final state, and grants any they would have earned. Idempotent.
- **Late Night** is reconstructed from log timestamps.
- **Streak achievements and current streak** are reconstructed from action history, deduped to UTC days, walked forward to find the longest run and award all crossed thresholds. Final current streak reflects the actual run if the last action was today or yesterday, else zero.

### Lobby Achievements Strip + Sort (v0.0.343)
- **Top-of-lobby strip** shows "🏆 N/45 Achievements" with your three most recently earned as small chips, alongside the streak pill.
- **Per-player trophy badge** in the lobby player list — gold "🏆 N" next to each player's ELO badges. Zero is dimmed.
- **Sort by achievements** — new sort button; ties alphabetical.

### Achievements Visibility Fixes (v0.0.342)
- **Achievements section now shows for everyone** on the profile page, including users with no finished games (was hidden inside an else-branch).
- **Account page mirrors the section** so you don't have to leave to see your own.
- **Section header shows progress** (e.g. "12/45 earned") next to the streak pill.

### Achievements + Streak (v0.0.341)
- **45 achievements** across game count, win count, single-game VP, ELO milestones, industry mastery, play behavior, distant market, money and loans, links, underdog and rivalry, marathon and sprint, daily streaks, and time-of-day quirks.
- **Game-end evaluation** runs after the ELO update; per-action evaluation runs as actions come in (covers late-night and streak updates).
- **Streak tracking**: Current and longest streaks recorded per user; advances by one per UTC calendar day; resets on a gap. Streak achievements at 5 / 10 / 20 / 50 / 100 / 365 days.
- **Pending toasts**: Newly earned achievements are queued and arrive on the next in-game poll, sliding in as a gold or red overlay — works mid-game in another tab.
- **Lobby streak pill** (🔥 N days) when current streak is positive; tooltip shows longest. Click goes to your profile.
- **Profile/Stats page** has a new Achievements section: 45-cell grid with earned ones colored, locked ones desaturated. Hover shows the criterion and (if earned) the date and game.

### Lobby Up-To-Date / Join-Create Banners (v0.0.340)
- **Three-state lobby banner** at the very top, all in the same shape as the existing turn banner so the layout doesn't shift:
  1. **Your turn** (gold) — at least one game is waiting on you.
  2. **Up-to-date** (green) — you're in active games but not on the clock anywhere — *"You are up-to-date with the community, Mr. Wallace would be proud."*
  3. **Join / Create** (blue) — you have no active games — invites you to jump into an open waiting game or create a new one.
- **Smart links**: The "Join a waiting game" link scrolls to and expands the Waiting section; "Create a new one" does the same for Create New Game.

### Tile Corner Markers + Lobby Invite Visibility (v0.0.339)
- **Mat tile corner markers**: developed tiles get a small orange square in the upper-right corner; canal-removed tiles get a small blue square. Hover tooltips distinguish "Developed — out of game" from "Removed at end of canal era".
- **Lobby invite visibility**: Pending invites (sent but not accepted) are now shown under each waiting game's row. Public for open games (anyone can join, so invites are public anyway); restricted to creator, current members, and invited users for closed games.

### Overbuilt Tile Visual (v0.0.338)
- **Overbuilt tiles** render with a dark red border, faded fill, and a red ✕ overlay — visually distinct from developed or canal-removed tiles. Hover tooltip reads "Overbuilt — replaced on the board".

### Mat "Develop First" Hint (v0.0.337)
- **Mat panel hint is era-aware**: If the top-of-mat tile can't be built in the current era, the panel shows an orange warning instead of the misleading "Next: x — £…" line. When the top tile is buildable, the panel reads as before.
- **New tile status — overbuilt**: Tiles that were replaced via overbuild used to default-render as available, misleading the player. They are now classified as overbuilt instead.

### Other-Turn Shortcut in Navbar (v0.0.336)
- **In-game navbar link** appears as "▸ Your turn in <game>" (with a "+N more" if there are extras) once your turn here is over and at least one other game is waiting on you. Hidden while it's still your turn here.

### Auto-Start When Full (v0.0.335)
- **Games auto-start the moment seats are full** — no more "creator must click Start". Hooked into joining open games, accepting invites, adding bots, and creating a game with the seats already filled.
- **Manual start still works for partial-fill starts** (e.g. a 4P slot starting with only two human players).
- **Whoever triggered the start** is redirected straight to the game; everyone else sees "active" and their turn highlight on next refresh, plus a push notification if subscribed.

### Lobby ELO Sort + Finished-Section Collapse (v0.0.334)
- **Players sidebar** has three new sort buttons next to Recent / A-Z: 2P ELO, 3P ELO, 4P ELO. Sorts by rating descending in the chosen category; players with zero games in a category drop to the bottom.
- **Finished games section** is force-collapsed on every page load.

### Lobby Highlights "Your Turn" Games (v0.0.333)
- **Top banner** above the lobby layout lists every game where it's your turn, each as a clickable shortcut. Hidden when there are none.
- **Per-row highlight** on those games: gold gradient, left border, glow, and a "▸ Your turn" badge in the game-info row.

### Migration Also Restores Canal Flag (v0.0.332)
- **Earlier window left both flags false** for Liverpool–Ellesmere on games created during the bad period — resulting in a dashed-grey line in canal era too. The migration now normalizes any unbuilt instance to canal-only. Already-built links are untouched. Idempotent.

### Hide Canal-Only Link in Rail Era (v0.0.331)
- **Rail era now hides unbuilt canal-only links**. Liverpool–Ellesmere Port is the only canal-only link, so the dashed-grey "topology only" line no longer shows up there during rail era.

### Retrofit Liverpool–Ellesmere Rail Flag (v0.0.330)
- **One-shot migration** clears the bad rail flag on Liverpool–Ellesmere Port in every existing game's links — but only where the link is unbuilt. Already-built rails are preserved. Idempotent: reruns do nothing once cleared.

### Liverpool–Ellesmere Port Connection Fix + Single-Rail Spend Log (v0.0.328 - v0.0.329)
- **Liverpool–Ellesmere Port** was briefly stored as rail-only; corrected to canal-only per the physical board.
- **Single-rail "spent £X" log fix**: was using cumulative round spend and reading as a running total; now uses the action's own cost like canal-build and 2-rail-build logs do.

### Cube Owner in Logs (v0.0.327)
- **Resource log now shows the cube's owner**: e.g. "1 coal from Manchester [byfed] (free, 2 left)" instead of just the location. Makes it instantly visible whose mine or iron works was tapped.

### Pre-Invite at Game Creation (v0.0.326)
- **Custom Game form has three player invite dropdowns**, each pre-populated with the alphabetical user list (excluding the creator). Selected players are sent invites at creation time the same way the per-game invite button works. Self-invites and empties are skipped.

### Invite Dropdown (v0.0.325)
- **Lobby invite is now a dropdown** populated from the alphabetical user list (excluding bots, the inviter themselves, current game members, and already-invited users). Submit disables when there's nobody left to invite.

### Sell Cotton Duplicate-Click Bug (v0.0.324)
- **Diagnosed from the new server logs**: The same mill-port pair was being queued multiple times, then the server flipped the mill on the first entry and rejected the rest with "mill already flipped". Cause: the port-pick handler persisted after a sale was queued and re-fired on a second click.
- **Three layers of fix**: highlights cleared right after queueing a sale (so the click handler is gone); a dedup guard rejects a duplicate of the same mill-port pair; the final submit dedupes by mill as defense in depth. The mill-pick callback also clears highlights and the dedup key when a new mill is chosen.
- **External port click is single-shot** — first click clears its own handler before triggering the distant sale.

### Lobby Date Format (v0.0.323)
- **"24 April 2026" format** in lobby game rows — locale-independent.

### Lobby Game Age + Sell Diagnostics (v0.0.322)
- **Lobby shows game age**: Each game row shows "Started <date> · N days old" under the player list.
- **Server-side sell-cotton logging**: Every "mill already flipped" / "not your cotton mill" / "not a cotton mill" / "invalid mill location" rejection now writes detailed diagnostics for debugging.

### Reset-Turn Fallback Snapshot (v0.0.321)
- **Per-turn fallback snapshot** kept on the live state, so reset-turn still works even after the emergency compactor has wiped state history.
- **Snapshot taken at**: game creation, every turn advance, every new round, the canal-to-rail transition, and after every irreversible distant-market commit.
- **Reset-turn endpoint** tries the requested history version first, then the fallback snapshot, then errors with a clearer message if neither exists.
- The snapshot strips its own previous copy before serializing to avoid exponential nesting.

### Self-Compaction at Boot (v0.0.320)
- **Server now runs the compactors itself** before opening the database, so it self-protects regardless of how the deploy harness was configured. Each compactor runs as a child process with a lower memory cap so its working set doesn't stay live in the server. Errors are logged but do not block boot.

### Streaming Compactor Fallback (v0.0.319)
- **Byte-level streaming compactor** for the case where the database has grown too large for the in-memory compactor. Walks the file as a stream, replaces the bulk state-history sections with empty arrays, and never loads the whole thing into memory.
- **Size-gated**: only runs when the database exceeds a threshold; under that, the in-memory compactor preserves per-game history.
- **Emergency semantics**: when this runs, all in-progress turn-reset history is wiped (final states of finished games are untouched).

### Boot-Time Database Compactor (v0.0.318)
- **Boot-time compactor** runs before the server starts on every deploy. Prunes per-game state history and rewrites the database compactly. Runs in a separate process so its memory doesn't compete with the running server.

### Memory Reductions (v0.0.317)
- **Compact saves**: Database writes use compact JSON instead of pretty-printed for around 20–40% smaller files in memory and on disk.
- **Cap state history per game**: Older snapshots are pruned on every save — enough for turn reset and recent replay, far less than the unbounded growth that was happening before.
- **Prune finished games' history** to a small handful of snapshots when a game becomes finished. A significant one-off reclaim.
- These together address the out-of-memory pressure that was building up under the deployed memory ceiling.

### Phase 4 Log Honesty at Game End (v0.0.316)
- **Rail-end log no longer claims spent boxes were emptied**: The end-game tiebreaker chain (VP, then income, then money, then last-round spending, then turn order) needs them, so the rail-end line now reads "Spent boxes kept for end-game tiebreakers." Canal end still says they're emptied for the next era.

### Era-End Phase Logs (v0.0.315)
- **Phase logs at era end**: The last round of each era now emits the same reorder and draw-cards log lines as a normal round, before scoring runs, so the log reads coherently.

### Rail Era Turn Order Bug (v0.0.314)
- **Canal-to-Rail transition was sorting on already-zeroed spends**: The transition zeroed last-round spending before computing the new rail-era turn order, so all players looked tied at zero and the stable sort kept them in canal order. Last-place players were stuck last even if they had taken loans. Fixed by computing the new turn order first, then zeroing.

### Admin Pause + Turn Order Fix (v0.0.313)
- **Admin pause button**: Pause and Resume at the top of the admin-fix panel. While paused, action, confirm, and reset endpoints reject with a "paused by admin" message; bot scheduling and execution short-circuit. Players see a red "⏸ Paused by admin — actions disabled" banner.
- **Turn order editor**: A Turn Order section in admin-fix lists each position with up/down buttons to swap adjacent seats. The fix endpoint validates that the new order is a permutation of all seats.

### ELO Badge Visibility (v0.0.312)
- **Always show 2P/3P/4P badges in the lobby**, even for unplayed categories (at 1000 with 0 games), dimmed. Previously hidden until the player had played in that category.

### ELO Visibility + Only-New-Games Rule (v0.0.311)
- **Only snapshotted games score ELO**: Games already active at the snapshot deploy are skipped, since they have no snapshot. Waiting games that start after the deploy count normally.
- **Lobby player list shows ELOs**: Each player row lists 2P/3P/4P ratings as small badges.
- **Profile page**: New "ELO Ratings" section with three cards showing rating and games played.
- **Account page**: Same section for your own account.

### ELO Snapshot at Game Start (v0.0.310)
- **Snapshot ELO when a game becomes active**: Each human player's current rating in the game's category is snapshotted at game start. Game-end uses that snapshot for the rating delta instead of whatever the rating has drifted to in concurrent games. Standard chess practice.

### ELO Badge in Game (v0.0.309)
- **ELO shown next to each player's name** in the game player bar — the current rating for this game's player-count category. Tooltip shows category and games played. Hidden for bots.

### ELO Ratings (v0.0.308)
- **Per-category ELO** for 2P, 3P, and 4P. Everyone starts at 1000. Only all-human games count. Pairwise ELO with K=32, no normalization — zero-sum per game.
- **Tiebreakers for placement**: VP, then income square, then money, then last-round spending (lower wins), then turn-order index.
- **Rating update logged into the game** so players see their delta on the game page when it ends.
- **Lobby leaderboard**: New "ELO Ratings" section above group rankings with one table per category. Players appear once they've played a game in that category.

### 2P Demand Pile (v0.0.307)
- **2P pile derived from the 12-tile base**: Removes 1×(-1), 1×(-3), and 2×0 from the 12-tile pile, giving 8 tiles instead of the previous 7.

### Missing Demand Tile (v0.0.306)
- **3P/4P demand pile fix**: The physical game has 12 demand tiles per era; the code had 11 (one less of the -1 value). Added the missing tile. Applies to new games and to rail-era transitions of existing canal games. In-progress canal games keep their original 11-tile pile; admin can patch via the demand editor.

### Demand Tiles Inspector (v0.0.305)
- **Info icon on the Demand panel**: Opens a popup listing every demand tile sorted ascending — flipped ones dimmed and struck through, remaining ones in gold. The pile's actual draw order is hidden so it doesn't leak the next tile.

### Unflip Demand Tile (v0.0.304)
- **Admin unflip**: A per-tile button in the admin Demand Pile section returns a flipped tile to the unrevealed pile, reshuffles, and restores the demand level. Income bonuses are not rolled back; adjust separately if needed.

### Board Re-render Key Fix (v0.0.303)
- **Demand and markets now re-render**: The board re-render trigger now includes coal market, iron market, distant-market demand, and the demand-pile counts — fixes stale demand display after admin flips and any other change that only touches markets.

### Admin Flip Actions (v0.0.302)
- **Flip the next demand tile for a player**: Button per player in admin-fix shifts the next distant-market tile into the flipped pile, reduces demand, and applies the standard income bonus.
- **Flip any industry tile**: Admin-fix lists every unflipped owned tile with a Flip button; flipping marks it scored and applies the tile's income gain and VP to the owner immediately.

### Private Notes (v0.0.301)
- **Per-game per-player notes**: A private text panel in the right panel (and on the mobile Hand tab). Only the player who wrote them can read them. Save button plus a keyboard shortcut. 10 K-character cap. Notes vanish if the game is deleted.

### Build Link UI Filter (v0.0.300)
- **Highlight only valid links**: The Build Link picker filters by your network so you can't pick a link the server would reject. The second-link picker uses your extended network including the first rail's endpoints.

### Build Link Validation + Error Messages (v0.0.299)
- **Second-rail network check**: Two-rail builds now validate the second rail against your network plus the first rail's endpoints, closing a hole that let players build a second rail anywhere on the board.
- **Better error messages**: Identify which link failed and clarify that opponent rails do not extend your network.

### Admin Mat Fix (v0.0.298)
- **Edit a player's mat**: The admin-fix endpoint can now patch the industry-mat tile levels per player, fixing state corruption from older bugs that silently removed tiles.
- **Admin UI**: A mat row per player with comma-separated tile levels — edit and tab away to apply.

### Phantom Card Guard (v0.0.297)
- **Phantom card error fix**: A submitting guard prevents poll-triggered double-submissions of the same action.

### Changelog + Group Rankings (v0.0.295 - v0.0.296)
- **Backfilled changelog entries**: v0.0.265–v0.0.294 added in grouped sections.
- **Group rankings**: The lobby shows rankings for player groups with two or more finished games. Points equal players you beat per game (1st in 4P = 3 pts). Separate tables for 2P/3P/4P, ties broken by total VP.

### Sell Cleanup + Wild Build + Slot Picker (v0.0.291 - v0.0.294)
- **Mid-sell flag now clears properly** so the next player isn't blocked.
- **Distant-market bottom-out**: Sell ends immediately when the market is empty.
- **Wild build overbuild**: Allowed on opponent coal/iron tiles when the market is empty.
- **Slot picker**: Asks the player to choose when both an empty slot and an overbuild option exist at the same location.

### Action Blocking + Sell Fixes (v0.0.286 - v0.0.290)
- **Block stale actions**: Actions are rejected when no actions remain or a confirm is pending.
- **Mid-sell tracking**: Once a sell is in progress, other actions are blocked until you finish or cancel.
- **Premature sell-end fix**: A port queued for one mill no longer blocks distant-market access for other mills.
- **Distant-market income bonus fix**: Uses the track values (3, 3, 2, 2, 1, 1, 0, 0) instead of the raw demand number.

### Submenu + Coal Sourcing (v0.0.281 - v0.0.285)
- **Quick Create buttons**: Inside the collapsed Create Game section with the right toggle arrows.
- **Submenu position fix**: Correct placement when the action started before a card was selected.
- **Draggable action submenu**: Desktop drag plus touch drag on mobile.
- **Two-rail coal sourcing fix**: Treats the first rail as already built when finding coal for the second rail.

### Reset Persistence + Training + Lobby Redesign (v0.0.276 - v0.0.280)
- **Persisted reset version**: Stored with the game so it survives a page reload.
- **Quieter Reset button**: A grey-themed button in the confirm overlay.
- **Per-player-count training**: Separate weights for 2P, 3P, and 4P with the right VP normalizers per count.
- **Logged coal-mine flips**: Single-rail coal consumption now appears in the log.
- **Lobby redesign**: Mobile bottom panels, quick-game rows, and bot limits.

### Coal Sourcing Fixes + Troll Polish (v0.0.270 - v0.0.275)
- **Rail coal sourcing**: Both endpoints of every link are checked for coal directly.
- **Two-rail coal fix**: Each rail's coal comes from its own endpoints only.
- **Troll action summary**: The phrase is prefixed with the action that just happened; HTML rendering bugs fixed.
- **Persisted preferences**: Confirm and Troll checkboxes are saved locally on your browser.

### Mobile Chat + Troll Mode (v0.0.265 - v0.0.269)
- **Mobile chat fills available space** vertically.
- **Troll mode**: A library of Spanish trash-talk phrases shown on the turn-confirm overlay.
- **210 phrases** all targeting a named rival and spoken by a random opponent in their color.
- **Overlay only**: Not sent to chat, only shown in the confirm dialog.

### Chat + Log Improvements (v0.0.261 - v0.0.264)
- **Separate chat from log**: The game log shows only events; chat lives in a collapsible section with an unread badge.
- **Chat as a fifth mobile tab**: Envelope icon with a "!" indicator for unread, cleared on open.
- **Turn-order log neutral**: System messages (turn order, round markers) are no longer colored as if a player said them.
- **Two rails on the last action**: Building two rails is a single action — always shows the double-rail prompt.
- **Chat input on top**: Newest messages first.

### Open Games + Weights Deploy (v0.0.259 - v0.0.260)
- **Open games**: A checkbox on Create Game lets anyone join without an invite, marked with a green badge.
- **Overbuild fix**: Must be the same industry type — can't replace a coal mine with a cotton mill.
- **Updated bot weights**: Average best-VP 94.8 with peak 128.

### Reset/Confirm Polish + Turn Hold (v0.0.246 - v0.0.258)
- **Floating Reset Turn button**: Fixed top-right, visible on any mobile tab after the first action.
- **Centered confirm dialog**: After the last action, with extra spacing to prevent misclicks.
- **Full turn hold**: Server pauses turn advance until the confirm; no next player, no round end, no income, no card draw until you confirm. Confirm triggers the advance and bots.
- **Action counter** correctly shows zero remaining while pending confirm.

### Reset Turn + Confirm Turn (v0.0.244 - v0.0.245)
- **Reset Turn**: A button appears once you've taken at least one action; it restores game state to the start of your turn. If a distant-market tile was already revealed, that becomes the earliest reset point.
- **Confirm Turn**: After both actions, "Turn complete!" is shown with Confirm and Reset. Polling pauses until you confirm. Configurable via a "Confirm turn" checkbox (default on).

### Mobile Panel Alignment (v0.0.241 - v0.0.243)
- **Combined mobile left panel**: VP, coal/iron, and income panels share the same horizontal position with dynamic vertical spacing based on player count — no more overlap.

### Bot Tier + Wild Build + Deploy (v0.0.238 - v0.0.240)
- **Per-bot tier selection**: Each bot has its own Pro/Avg/Noob dropdown in Quick Game and Create Game.
- **Wild Build UX**: One-click Quick Wild Build from the card popup, auto-selects the second card when only two are left, shows tile cost/VP/income while picking, and a clearer button label.
- **Deployed updated bot weights**: Average best-VP 77.9 with peak 124, trained on a coach-mixed mixture of self-play with sells and loans.

### Sell Cotton + Develop Fixes (v0.0.234 - v0.0.237)
- **"Sell Another" goes straight to the mill picker** instead of looping back through earlier steps.
- **Develop two tiles crash fix**: When both tiles target the same iron source with only one cube available, the second consume now falls back to the market or bank instead of crashing.
- **Iron source fallback**: Depleted specified sources fall through to auto-pick instead of returning empty.
- **All iron sources shown**: Iron picker shows every iron works on the board, since iron has no connectivity requirement.

### Beta Banner + Feedback + Polish (v0.0.228 - v0.0.233)
- **Hidden hands in active games**: Only finished games reveal opponent hands.
- **All mats in review mode**: Every player's mat is shown inline during review.
- **Beta banner**: Gold banner on the lobby — "This game is in early testing!"
- **Feedback form**: Players can report bugs and suggestions from the lobby. Admin sees recent feedback.
- **Mobile beta banner fix**: Top margin to clear the fixed navbar.
- **Full game log**: Shows every entry, not just the most recent.

### Training v6 + Bug Fixes (v0.0.222 - v0.0.227)
- **Sell cotton auto-finish**: Action ends automatically once no mills remain after a distant commit.
- **Board re-renders on stat changes**: Panels now refresh after loans, scoring, and income changes.
- **Income track alternating colors**: Negatives in dark reds, positives in dark greens, zero in grey.
- **Brighter unflipped tiles**: Player color shows through more clearly through reduced image opacity.
- **Darker yellow player**: Adjusted to a deeper amber.
- **Game review — all mats and hands**: History navigation shows every player's mat and hand. Finished games reveal full state with no hidden hands.
- **Training generations**: Added an MCTS + n-step TD experiment (slow, replaced by the next pipeline). The next generation peaked at an average best-VP of 74.3 using direct scoring with one-step lookahead and 14 parallel workers.

### Admin Fix Card Removal + New User Dialog (v0.0.216 - v0.0.221)
- **Missing changelog entries**: Backfilled.
- **Develop fix**: Guard against double-invocation; auto-submits when iron source is unambiguous.
- **Tile pulse**: Tuned the pulse opacity range.
- **Mobile market layout**: Coal and iron panels moved under VP, income below them.
- **New User dialog**: The login page has a "New User?" section where admin-created users set their first password.
- **Admin Fix — remove card**: The fix panel shows player hands with a remove button per card.

### Card Deselect + Rounds Fix (v0.0.213 - v0.0.215)
- **Click a card again to deselect** and close the action menu — works in any state.
- **Rounds per era**: Locked at 10 for 2P/3P and 8 for 4P.
- **Hand tab icon**: Replaced the lightning bolt with a neutral diamond on mobile.

### Finished Games + Lobby Progress (v0.0.210 - v0.0.212)
- **Finished games always accessible**: Players can enter finished games for review even on an old version.
- **Lobby game progress**: Active games show the era (Canal blue / Rail grey), round as X/Y, and a progress bar.

### Changelog Fix + Mobile Fixes (v0.0.206 - v0.0.209)
- **Changelog ordering fixed**: Strict ascending version order across the whole file.
- **Mobile tile pulse**: Inline animation injected so the pulse works on mobile.
- **Mobile chat**: Chat input moved into the mobile log overlay.
- **Chat overlay**: Incoming chat from other players appears as a temporary overlay.

### Lobby Polish (v0.0.201 - v0.0.205)
- **Create Game form**: Vertical column layout fixes button alignment.
- **Collapsible sections**: Players, Create Game, and System Data all toggle.
- **Non-admin users see finished games**: Members can review their own finished games.
- **System Data panel**: Shows active, waiting, and finished game counts plus total players.
- **Player last activity**: Shows time-since each player's last action.
- **Player sorting**: Toggle between Recent activity and A-Z.
- **My Games spacing**: Heading and quick-start buttons no longer crowd each other.

### Active Tile Pulse + UI Polish (v0.0.191 - v0.0.200)
- **Unflipped tiles pulse subtly** in the player's color on a roughly three-second cycle. Flipped tiles sit at lower opacity to read as "already scored".
- **Board re-render optimization**: The board only redraws when state actually changes, so animations don't restart on every poll.
- **Location card build fix**: Auto-picks the first valid slot when a card pre-sets a location.
- **Hover tooltips on every slot**: Filled tiles show industry, level, owner, VP, income, cubes, and flipped status; empty slots show allowed types.
- **Spacing**: More gap between player cards; taller VP rows.
- **Game name centered in the navbar**, in gold on the game page.

### Slot-Based Build Selection (v0.0.189 - v0.0.190)
- **Click specific slots to build**: Build Industry and Wild Build now highlight individual slots (the same way Sell Cotton does), with hover labels showing industry, level, location, and any overbuild info.
- **Click slot or location name**: Both work — slot picks that exact slot; name picks the first valid slot.

### In-Game Chat (v0.0.188)
- **Chat in the game log**: Text input below the log; messages appear inline with game events, sorted by timestamp, italic and in the player's color.
- **Stored with the game**: Messages persist (last 100 retained) and arrive on the regular state poll.

### Cover Image & Lobby Redesign (v0.0.185 - v0.0.187)
- **Game cover art**: Cover image on the login page (next to the form) and at the top of the lobby left sidebar.
- **Lobby layout**: System, Players, Admin, and Bot Training panels moved from the right sidebar into the left column under the cover.

### All Links Single Segment + 2 VP From Non-Buildable Locations (v0.0.184)
- **All links cost a single segment**: Removed the last few legacy 2-segment routes — every link is now £3 canal or £5 rail.
- **Non-buildable locations contribute 2 VP to links**: Waypoints (Blackpool, Southport, Northwich) and external ports (Scotland, Yorkshire, The Midlands) now grant 2 VP for link scoring; previously they gave none.

### Single Rail On Last Action (v0.0.183)
- **Skip the double-rail prompt when only one action remains**: Goes straight to coal sourcing and submission.

### View Other Players' Mats (v0.0.182)
- **Mat navigation arrows**: Left and right arrows in the mat panel header cycle through every player's industry mat, with their name shown in their color and a subtle background tint.
- **Own mat as default**: Returns to "Your Mat" when you cycle back to your seat.

### Loan Income Drop Fix (v0.0.181)
- **Land on the highest square of the level**: A loan was dropping you to the lowest square of the target income level instead of the highest. Players were losing one or two extra squares per loan level.

### Rail Era Turn Order Fix (v0.0.180)
- **Sort by spending, not VP**: Rail era was being seeded by VP (lowest first) when it should sort by spending in the last canal round (lowest first, ties keep order).

### Tile Era Validation (v0.0.179)
- **Cannot build canal-only tiles in the rail era**: The server now validates a tile's era flags. L1 cotton mills, coal mines, iron works, and ports are canal only; the L2 shipyard is rail only.
- **Client filters unavailable industries**: Build and Wild Build flows hide industry types whose top tile is not buildable in the current era.

### Income Track Cap Fix (v0.0.178)
- **Income cap raised to its real maximum**: The income track was being clamped at the wrong value, silently throwing away gains for high-income players.

### Link Segments From Static Data (v0.0.177)
- **Engine reads link segments from canonical data** instead of cached game state — fixes existing games that still had the old multi-segment cost for external-port links.

### External Port Link Cost Fix (v0.0.176)
- **External port links cost the same as normal links**: Scotland, Yorkshire, and The Midlands had been priced as 2-segment routes; corrected to single-segment everywhere, including the training engine.

### Distant Market Non-Revertible (v0.0.175)
- **Distant-market sales commit immediately**: Selling to the distant market is partial-submitted right away (the tile is revealed and the mill is flipped), since the distant tile cannot be unseen. Port sales remain queued locally until you press Done.
- **Cancel after distant**: If a distant sale was committed, the cancel button is hidden — Done is the only way out.

### Action Overlay & Sell Cotton UX (v0.0.174)
- **Action overlay for every action**: Every action a player takes shows an overlay box (the same way bot actions are announced) with the log summary, fading after a few seconds.
- **Iterative sell-cotton feedback**: Each sale shows an instant overlay confirming it, then asks "Sell another?" with a queued summary of all pending sales before final submission.
- **Multi-line overlay**: Supports actions that emit multiple log entries.

### Rail Coal Per Link Fix (v0.0.173)
- **One coal per rail link**: Multi-segment routes were charging coal per segment; rails now correctly charge a single coal per link, matched in the training engine too.

### Rail Link Visuals (v0.0.172)
- **Rail link colors**: Available rail links now appear dark grey instead of brownish; highlighted links glow bright grey with a drop shadow.

### Rail Link Coal Fix (v0.0.171)
- **Both-end coal sourcing for links**: When building a rail link, coal sources and market access are checked from both endpoints — fixes the spurious "cannot source coal" error when extending to an unconnected port.

### Resource Sourcing Rules (v0.0.170)
- **Distance-based cube sourcing**: Resources come from the closest source by link distance (same location is 0; each link adds 1). Coal needs network connectivity; iron is available from any iron works on the board.
- **Player choice**: When multiple sources tie at the same distance, the player picks which to drain — the picker shows location, level, cube count, and owner for each option.
- **Cube-by-cube evaluation**: Each cube is sourced individually; consuming a cube may flip a tile and change what's available for the next cube.
- **Resource order choice**: When a build needs both iron and coal, the player chooses which to source first.
- **Resource picker UI**: New step inside Build Industry, Wild Build, Develop, and rail Build Link flows; auto-selects when only one option exists.
- **Backward compatible**: Bots and older actions auto-pick the closest source when no resource plan is provided.

### Mobile Submenu Fix (v0.0.169)
- **Mobile cascading menu**: The action submenu now appears as a vertical bottom sheet on mobile instead of popping horizontally off-screen, sized full-width and positioned above the tab bar.

### Training Engine v4 (v0.0.168)
- **Python game-engine fixes**: Barrow slots corrected, income capped at the right value, coal-mine flip bug fixed, overbuild rules aligned, auto-sell of resources after building, distant-market income bonus, the Weirdest Rule for Birkenhead and Liverpool, and a coal-source fix for build link.
- **New training infrastructure**: Encoder for state and action features matching the in-game inference exactly; new larger network architecture; AlphaZero-style self-play with replay buffer and learning-rate schedules.
- **Pass action removed**: Real players never pass — bots no longer generate it unless no other action is possible.

### Cotton Sell Fix (v0.0.167)
- **Flipped port distant market**: Mills connected to a flipped port can now sell to the distant market — any built port enables distant access regardless of flipped state.

### Log Panel & Fixes (v0.0.165 - v0.0.166)
- **Log panel resize**: Increased default height, added a gold drag handle, and removed an animation that fought manual resize.
- **Changelog header fix**: Version count corrected.

### Changelog Page (v0.0.164)
- **Changelog nav link**: Added to the navbar on every page.
- **Changelog route**: Renders this file as a styled HTML page.
- **Changelog view**: Same wiki-container styling for a consistent look.

### The Weirdest Rule (v0.0.163)
- **Birkenhead special build**: In the rail era, if you have a link to Liverpool or an industry there, you can build in Birkenhead's empty shipyard slot with a Shipyard card. Coal still needs to be reachable through your network as normal.
- **Liverpool special build**: In the rail era, if you own the Birkenhead–Ellesmere Port link or have Birkenhead's Shipyard built, you can build in Liverpool's empty port and shipyard slots with a matching card, even without owning links to Liverpool.
- **Server and client both updated**: Validation and slot highlighting handle these special cases.

### Action Submenu & Selling Fixes (v0.0.156 - v0.0.162)
- **Action submenu**: A cascading popup appears next to the action menu when a card is clicked, listing all six action types.
- **Barrow-in-Furness fix**: Slots corrected to Shipyard plus Iron Works.
- **Barrow migration**: A one-shot script repairs Barrow's slots in existing games.
- **Resizable game log**: Desktop log panel can be dragged up to most of the screen height.
- **Cotton selling fix**: Per-mill port filtering and connectivity checks; distant market is reachable through any connected port.
- **Distant market access**: Client logic now matches server logic.
- **Slot-level highlighting**: Click an exact slot when selling cotton; hover labels show industry info.

### Build UX & Mobile Overhaul (v0.0.148 - v0.0.155)
- **Overbuild fix**: Higher-level tiles allowed on your own tiles; opponent coal and iron only when those markets are empty.
- **Mobile fixed bars**: Navbar and turn indicator stay visible across all tabs.
- **Turn indicator money**: Shows current money alongside action count, e.g. "2 actions, £17".
- **Per-develop iron cost**: Each develop in a double-develop logs its individual iron cost.
- **Game credits**: "Game by Martin Wallace — Art by Peter Dennis — Published by Eagle-Gryphon Games" in the navbar.
- **Controls help**: Tooltips explaining each in-game control.
- **Legend toggle**: Industry color legend appears only when icons are turned off.
- **Version display fix**: Version shown in the header now updates correctly.
- **VP panel rows**: Vertical layout with hexagon plus player name per row.
- **Hand & Tiles tab**: Actions merged into the Hand tab and renamed.

### Notifications & Admin (v0.0.143 - v0.0.147)
- **Web push notifications**: Your turn, game start, game finish, and game invite.
- **Service worker**: Push display with click-to-open-game.
- **Lobby improvements**: Collapsible Active / Waiting / Finished sections, filter tabs, and a current-turn indicator.
- **Turn indicator bar**: Colored bar at the top of the game page showing whose turn it is plus the era and round.
- **Market sell fix**: Cubes now fill expensive slots first (£4, 4, 3, 3) instead of the cheap ones.
- **Admin fix mode**: Admin can adjust player money, income, VP, markets, and turn state in-game.

### Neural Network Bot (v0.0.141 - v0.0.145)
- **AlphaZero-inspired**: Trained in Python; runs in pure JavaScript inference in the game.
- **Larger network**: 2.4 million parameters with residual blocks and a 512-dim hidden layer.
- **State encoding**: 1145 features covering board, links, players, hand, and strategy.
- **Self-play training**: Successive generations reaching peak VPs of 60.6, 67.8, then 73.3.
- **Reward shaping**: Combination of normalized VP, win, absolute VP, flips, links, and income.
- **Pro/Average/Noob**: One network with three different temperatures (0.05, 0.4, 1.0).

### Mobile UI (v0.0.139 - v0.0.142)
- **Mobile detection**: User-agent plus touch capability plus screen width.
- **Bottom tab bar**: Info, Board, Hand, Actions, Log.
- **Live panels in overlays**: Real panels are moved into mobile overlays so they update instantly.
- **Floating hand**: Always visible on the Board tab with horizontal scroll and touch-friendly sizing.
- **Turn navigator**: Fixed bar between hand and tabs, always visible.
- **Mobile market panels**: VP top-left, Turn Order top-right, Income bottom-left, Demand bottom-right.
- **Touch-friendly throughout**: Larger buttons, fonts, and tap targets.

### Industry Icons & 2-Player Mode (v0.0.137 - v0.0.138)
- **Industry icon images**: Real tile artwork from the physical game on board slots.
- **Built slots**: Player color background with the industry image overlaid plus a level badge.
- **Empty slots**: Dimmed icons on a warm beige background.
- **2-player mode**: Removed 6 locations, reduced deck and markets and demand pile, dedicated 2P map, Lancaster–Scotland canal added.
- **Improved bot features**: 44 features including sell opportunities, port connectivity, and era survival.

### Smarter Bots & Training (v0.0.133 - v0.0.136)
- **Valid-action generator**: Bots now enumerate only legal moves, eliminating action errors entirely.
- **33 features per action**: Type, money, income, VP, network reach, era, and tile statistics.
- **Six learning personalities**: Explorer Eve (heavy exploration) through Master Max (almost no exploration).
- **Full training pipeline**: Phase 1 ranks personalities across 1000 games; phase 2 trains 1000 games per tier with the top performers.
- **Tier-based bot selection**: Pro, Average, and Noob dropdowns when adding bots.

### Live VP & History (v0.0.100 - v0.0.112)
- **Live VP**: Real-time score from flipped tiles, links, and money divided by ten.
- **VP hover breakdown**: Popup shows scored, tile VP, link VP, and money VP with each contributor listed.
- **Server-side state history**: Every action stores a snapshot, enabling full game replay.
- **Turn navigator**: Skip-to-start, previous, next, and skip-to-end buttons step through every action of a game.
- **External ports**: Scotland, Yorkshire, and The Midlands are clickable "P" icons for selling.
- **Colored cubes**: Iron orange and coal grey appear in mat brackets and log messages.
- **Income on the mat**: Numbers are shown without a + sign; cube cost is shown per level.
- **Reset training**: Admin button to clear all training data.

### Game Flow (v0.0.92 - v0.0.99)
- **Build Industry filters**: Industry cards show only that type; location cards show only allowed types.
- **Wild Build**: Use 2 cards plus 2 actions to build anywhere on the board.
- **Develop step-by-step**: Pick the first tile, optionally add a second; same type twice is allowed.
- **Turn order fix**: Spending is zeroed only after reorder; tied players keep their previous position.
- **Overbuilding**: Permitted on your own tiles at a higher level, and on opponent coal or iron when the market is empty.

### Bot Training (v0.0.90 - v0.0.91)
- **Six bot personalities**: Cautious Carl, Aggressive Ada, Builder Bob, Wildcard Wil, Balanced Bea, Devver Dan.
- **Parameterized strategies**: Exploration rate plus per-action priority weights.
- **Training runner**: Plays a complete game in around 30 ms; runs tournaments with rankings.
- **Tier system**: Pro, Average, and Noob assigned from tournament results.
- **Background training**: Admin toggle, batches of 3 games every 10 seconds.
- **Login persistence**: Sessions extended to one year; cleanup logs silenced.

### Lobby & Multiplayer (v0.0.80 - v0.0.89)
- **Bot fix**: Game-data writes no longer trigger a server restart loop.
- **Lobby**: Shows only your games, with a system-stats sidebar and game invites.
- **Delete games**: Trash-bin button with confirmation, creator only.
- **Add bot button**: Fill player slots one at a time.
- **Player count enforcement**: A 3-player game needs three; a 4-player game needs four.
- **Card deck**: Exactly 66 cards matching the physical game.
- **White headings**: Neutral blue-grey buttons replace the previous scarlet.

### Slot Visuals (v0.0.73 - v0.0.79)
- **Cotton Mill**: Dim off-white slot color.
- **Coal icons**: Lighter on dark backgrounds; individually colored in dual slots.
- **Preston fix**: Port, Cotton/Port, Iron Works (corrected from the printed board).
- **Industry stripe**: A thin colored bar on top of built tiles showing the industry type.
- **Flipped hexagon**: Pink outline ringing flipped tiles to indicate VP scored.
- **Dimmed flipped tiles**: Lower opacity so flipped tiles read as "already used".

### Income Track & Logs (v0.0.61 - v0.0.72)
- **Proper 100-square income track**: -10 to +30 per turn mapped correctly across the squares.
- **Serpentine arrows**: Curved U-turn chevrons showing track flow direction.
- **Detailed game logs**: Money before and after, income changes with squares moved, per-turn amounts.
- **Round phase logging**: Income, reorder, and draw-cards phases each emit a log entry.
- **Log timestamps**: Toggle to show full date and time.
- **Log filters**: Per-player filter buttons and newest-first ordering.
- **Colored log entries**: Each player's actions appear in their color.

### Account & Auth (v0.0.57 - v0.0.60)
- **User account page**: Change password, view game history, see member-since date and last login.
- **Consistent navbars**: Lobby, Wiki, Stats, username (linking to account), and Logout on every page.
- **Admin-only user creation**: No self-registration; admin creates users and they set their password on first login.
- **Card sorting**: Default, by type, alphabetical, or type-then-alpha in the floating hand.

### Bot System (v0.0.51 - v0.0.56)
- **Bot announcements**: 🤖 prefixed log entries plus on-screen overlay notifications.
- **Configurable delay**: A 1–5 second slider to pace bot actions.
- **Bot reliability**: Duplicate-scheduling prevention and polling triggers.
- **Quick game buttons**: "Quick Game (2 bots)" and "Quick Game (3 bots)" in the lobby.
- **VP hexagons**: Player-colored with a pink outline; names properly spaced.

### Visual Polish (v0.0.31 - v0.0.50)
- **Demand panel**: Income circles (+3, +2, +1, +0) per demand level with the current row highlighted.
- **Turn Order**: Ordinals (1st, 2nd, 3rd, 4th) with cleaner spacing.
- **VP panel**: "VICTORY POINTS" label, hexagons in each player's color, names centered.
- **Income panel**: 100-square serpentine track from 0 to 99 with the actual income value per square, curved U-turn arrows, and player-colored markers.
- **Slot colors**: Cotton off-white, Coal dark grey, Iron orange, Port blue, Shipyard brown.
- **Dual slots**: Diagonal split showing both industry-type colors.
- **Legend**: Color swatches and letter meanings in the left panel.
- **First round fix**: All players take exactly one action in the first canal-era round.

### Node Customization (v0.0.23 - v0.0.30)
- **Undo positions**: A 50-step history for layout edits.
- **"Like Xai" button**: Copy xai's saved layout in one click.
- **Resize mode**: Mouse wheel, corner handles, and bottom bars resize any board element.
- **Scale persistence**: Saved per user alongside positions.
- **Golden-ratio cards**: 1:1.618 ratio enforced as the window resizes.

### Interactive Cards (v0.0.16 - v0.0.22)
- **Actionable popups**: Hovering a card opens clickable buttons for each action (Build, Link, Sell, Loan, Develop, Pass).
- **Floating hand**: Detachable, draggable, resizable, with golden-ratio cards.
- **Toggle links**: Show or hide link connections on the board.
- **Collapsible panels**: The left, right, and log panels can all be folded away.
- **Minimal mode**: Transparent backgrounds on VP hexagons, income circles, and money discs for a cleaner board.
- **Vertical market panels**: Coal and iron stack top-to-bottom from £1 to £4 with the price drawn inside each square.

### Markets & Panels (v0.0.11 - v0.0.15)
- **Distant market tiles**: 11 shuffled tiles per era (0, 0, -1, -2, -2, -2, -2, -3, -3, -3, -4).
- **Coal/Iron markets**: Eight slots each, prices £1, 1, 2, 2, 3, 3, 4, 4.
- **Draggable panels**: Turn Order, Money Spent, VP, Income Track, and Demand can all be moved.
- **Money discs**: Silver £5 and bronze £1 discs in the player bar.
- **Player colors**: Red, Purple, Green, Yellow.
- **Income/VP per row**: The industry mat shows an income circle and a VP hexagon once per level.

### Data & Stats (v0.0.8 - v0.0.10)
- **User stats**: Games played, wins, VP totals, per-opponent breakdown, and a personal game history page.
- **Draggable nodes**: Every board element can be moved in edit mode; positions are saved per user.
- **Tile corrections**: Exact costs, VP, and income for every tile (Cotton Mill, Coal Mine, Iron Works, Port at four levels, Shipyard) matching the physical game.
- **Card hover**: Location cards highlight that location; industry cards highlight valid build spots.
- **Link fixes**: Lancaster–Scotland is rail only; Preston–Fleetwood added; Wigan–Warrington has both canal and rail; Rochdale–Yorkshire and Ellesmere Port–Northwich added.

### Foundation (v0.0.1 - v0.0.7)
- **Initial release**: Full game with login, complete engine for all six action types, a draggable board, AI bots, an in-game wiki, and persistent saves.
- **Board corrections**: 19 buildable locations with correct dual-type slots (Cotton/Coal, Cotton/Port), 3 non-buildable waypoints (Northwich, Blackpool, Southport), 3 external ports (Scotland, Yorkshire, The Midlands).
- **Remember me**: Persistent login with a checkbox.
- **Login fix**: Race condition between session save and redirect resolved.
- **Save hardening**: Atomic writes plus automatic recovery from a backup if a save is corrupted.
- **Version compatibility**: Games are stamped with the version they were created on; incompatible games show a friendly message.
- **UI overhaul**: Rectangular locations, side panels, collapsible log, and a map background image with an opacity slider.

---

*Built with love iteratively through 368 versions of user-driven development — from a blank repository to **v1.0.11**: a full multiplayer Brass: Lancashire with neural-network AI, mobile UI, push notifications, ELO, achievements, streak records, daily turns counter, live news feed (now also tracking trophy ownership changes), a wired-up maintenance page, per-viewer favorite-color recoloring, a 43-trophy Hall of Fame with shared ties (now correctly attributing historic canal builds), a 9-language interface, and a newest-first changelog.*
