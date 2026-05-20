# Brass: Lancashire — Development Changelog

## 490 versions of iterative development

### Birmingham game-page polish — kill the Lancashire bleed-through (v1.0.133)

Tested the Birmingham game page in-app and a bunch of Lancashire elements were leaking through. This release stops the bleeding while the proper Birmingham board renderer waits for a later phase:

- **Nav brand + page title** now reflect the game's `gameType` — Birmingham games show "Brass: Birmingham" everywhere instead of the hard-coded "Brass: Lancashire".
- **Lancashire SVG board hidden** for Birmingham games. The board renderer's `render()` now short-circuits at the top when `gameState.gameType === 'birmingham'` and draws a clean three-line placeholder ("Brass: Birmingham — early access", "Board renderer not implemented yet — use the action panel on the right", "Locations + links + Birmingham markets coming in a later phase."). The `.board-controls` strip above the SVG is hidden too. This covers the previously-visible Lancashire map background, port + shipyard panels with the destroyed icons, the demand market panel, and the wrong-shape coal/iron market columns.
- **Cards in hand** — `parseCardId` now recognises Birmingham IDs (`loc-<location>-N`, `ind-<group>-N`, `wild-loc-N`, `wild-ind-N`) and returns useful info. `renderCardHTML` shows Mill / Manuf for the combined card, "Any Location" / "Any Industry" with a ⭐ badge for wilds, and falls back to the raw industry/location for everything else. No more "8 LOC cards" misrender.
- **Mat panel** — Birmingham industries `manufacturer`, `pottery`, `brewery` added to the client `INDUSTRIES` map (sparse: name + icon + colour + flipCondition only, no cost data — that stays server-authoritative in `lib/games/birmingham/industry-data.js`) so the mat-panel label resolver returns "Manufacturer" / "Pottery" / "Brewery" instead of the raw key.

Birmingham action panels (Phase 4a) remain the playable surface — the placeholder explicitly directs you there. Phase 5 will replace the placeholder with a Birmingham-shaped SVG board (no background map per your earlier spec — plain canvas with locations + links + merchant slots).

### Phase 4: Brass: Birmingham action panels + achievement/trophy tagging (v1.0.132)

Birmingham games are now playable end-to-end (no board renderer yet — every spatial choice is via dropdowns).

**Birmingham action panels** (`public/js/game-ui-birmingham.js`):
- Augments the existing `GameUI` singleton with `_birminghamActionPanel` that fires when `gameState.gameType === 'birmingham'`. The Lancashire panel is untouched.
- 7 action dialogs: Build Industry, Network (Link), Sell, Take Loan, Develop, Scout, Pass. Each dialog uses plain HTML `<select>` dropdowns for location / industry / link / merchant / card pickers — no board clicking needed.
- Sell dialog supports the "use merchant beer" toggle (consumes the slot's beer cube + triggers its bonus); develop dialog supports 1-or-2 industry types; network dialog has a Double-rail toggle (rail era only) that reveals a second-link dropdown; scout dialog enforces 3 distinct cards.
- Loaded after `game-ui.js` in `views/game.ejs`.

**Achievements — gameType tagging + storage namespacing** (`lib/achievements.js`, `lib/db.js`, `routes/game-routes.js`):
- Every definition can carry a `gameTypes` field (default `['lancashire']` for back-compat). Cross-game ones (streak, time-of-day, meta, generic VP, comeback, tiebreak) explicitly tagged `['lancashire', 'birmingham']`. Existing Lancashire-specific ones (cotton-empire, port-authority, distant-empire, etc.) implicitly stay Lancashire-only.
- `evaluateGameEnd(ctx, gt)` / `evaluateAction(ctx, gt)` / `evaluateStreak(ctx, gt)` now filter by gameType and check the namespaced storage key via `game-types.achievementKey(gt, id)`. Lancashire keeps the bare id (`first_game`), Birmingham prefixes with `bb:` (`bb:first_game`) — so a single user record can hold both without colliding.
- `db.grantAchievements(userId, ids, { gameId, gameName, gameType })` stamps both the storage key and the toast/news entry with the gameType.
- Routes pass `gameTypeOf(state)` through to both evaluators and grant.

**Birmingham-specific achievements** (8 new in `lib/achievements.js`):
- **Scout Master** — 10+ scouts lifetime (action-kind, uses a new `user.bb_scoutCount` counter the bot/route layer can populate).
- **Junction Builder** — built the Worcester / Kidderminster / UnnamedBrewery2 Y-junction.
- **Brewmaster** — built 5+ of your brewery tiles in a single game.
- **Unnamed Champion** — built at BOTH unnamed breweries in the same game.
- **Cargo King** — sold 5+ tiles in a single Sell action.
- **VP Hunter** — claimed BOTH the Shrewsbury (+4 VP) and Nottingham (+3 VP) merchant bonuses in the same game.
- **Globe Trotter (Birmingham)** — own a link to every active external market for this game's player count (5 in 4P, 4 in 3P, 3 in 2P).
- **Pottery Pyramid** — built a Pottery tile at every level L1–L5 across your Birmingham games.

**Trophy tagging + Birmingham trophies** (`lib/hall-of-fame.js`):
- METRIC_DEFS entries now carry `gameTypes` (default `['lancashire', 'birmingham']` for cross-game). Lancashire-only metrics explicitly tagged: `flipShip`, `flipPort`, `sellsDist`, `dist4`, `dist0`, `meanShip`, `meanPort`, `meanSellsDist`, `meanDist4`, `meanDist0`.
- `compute(db, gameType)` filters the final trophies array by gameType so Lancashire metrics don't leak into the Birmingham HoF and vice versa.
- 4 new Birmingham-only trophies: **Scouts** (lifetime scout actions), **Y-Junction Tiles Built**, **Most Tiles in One Sell**, **Merchant Beer Used**. Aggregator scans action data per-game for each.

Totals: **91 achievements**, **77 trophies**. Lancashire HoF view shows 73 trophies, Birmingham HoF view shows 67 (everything cross-game + the 4 Birmingham-only).

What's still ahead (Phase 5+): board renderer for Birmingham (locations + links on plain SVG, no background map per user spec), bot AI for Birmingham, in-game wiki section for Birmingham, polish on the Birmingham action panels (better validation hints, summaries, etc.).

### Phase 3 complete: Brass: Birmingham — sell + develop + scout + era transition + scoring (v1.0.131)

All seven Birmingham actions are now wired end-to-end. A 2P pass-only game now runs through canal era → era transition → rail era → final scoring → tiebreaker resolution without error.

**Phase 3d — `actionSell`** (`lib/games/birmingham/sell.js`):
- Multi-good per action: any number of own cottonMill / manufacturer / pottery tiles in one Sell action.
- Each sub-sale validates: tile owned + unflipped + sellable type; tile location connected via network to the chosen merchant slot; merchant tile accepts the industry (or is wild).
- `barrelsToSell` beer cubes paid per tile. Beer sources priority: own brewery (no network) → opponent brewery (via network from tile location) → merchant slot beer (one cube, if `useMerchantBeer: true` in the sub-sale's action data).
- **Merchant beer triggers the slot's location bonus**: Gloucester = +1 free develop credit (consumed by a later develop action), Oxford = +2 income levels, Shrewsbury = +4 VP, Nottingham = +3 VP, Warrington = +£5. Beer cube marked unavailable until era transition.
- Tile flips on sale; income gain applied. VP is accumulated at era-end scoring, not on the sale itself (matches Birmingham rules).

**Phase 3e — `actionDevelop` + `actionScout`** (`lib/games/birmingham/develop_scout.js`):
- Develop: 1 or 2 tiles per action. Each removes the lowest-level tile from the player's mat for the chosen industry; tile's `developable` flag must be true. Iron cost waived per tile when a `freeDevelopCredits` from the Gloucester bonus is available; otherwise 1 iron from any iron works (no network) or the iron market.
- Scout: discard the played card + 2 more from hand (all 3 to personal discard); gain one wild location + one wild industry. Forbidden if any wild already in hand. Wild stockpile must have at least one of each remaining.

**Phase 3f — scoring + era transition** (`lib/games/birmingham/scoring.js`):
- `scoreCanalEra` / `scoreRailEra`: VP per flipped industry tile + VP per built link (sum of each endpoint's contribution — flipped industries' `valueForLinks` + 2 VP per external-market endpoint, matching Lancashire). Y-junction naturally contributes 3 endpoints' worth.
- `transitionToRailEra`: scores canal era; removes all canal links; refreshes merchant beer cubes; reshuffles every player's personal discardPile + remaining deck into a fresh rail-era draw pile; deals fresh 8-card hands; resets spent boxes; sets `era: 'rail'`, `round: 1`, `actionsRemaining: 2`. **Level-1 industries are NOT swept at canal end** (Birmingham keeps them, vs Lancashire's L1-sweep).
- `finalizeRailEra`: scores rail era; marks `phase: 'finished'`; **money does NOT convert to VP** (Birmingham rule); tiebreaker is income → money → draw (no spent / no turn-order kicker per the user's rule).
- The end-of-round income phase is implicitly skipped on the final round of rail era — `endRound` routes straight to `endEra` before running income whenever the deck and all hands are empty.

**`lib/games/birmingham/index.js`** now exposes the full engine surface — every method is real, no more `notImplemented` stubs.

Smoke-tested: 2P all-pass game completes in 78 actions (38 canal + 40 rail), declared a draw with both players on 0 VP (correctly, no builds = no scoring). 3P/4P math also checks out.

What's still ahead (Phase 4+): the in-game UI (board renderer, action panels, sell dialog, scout dialog, merchant-beer toggle), bot AI for Birmingham, in-game wiki for Birmingham, polish.

### Phase 3c: Brass: Birmingham — buildLink (Network action) (v1.0.130)

`actionBuildLink` is live with the full Birmingham rule set:

- **Era costs**: canal £3 / single rail £5 + 1 coal / **double rail £15 + 2 coal + 1 beer** (the Birmingham-only beer requirement).
- **Y-junction** (Worcester ⇄ Kidderminster ⇄ UnnamedBrewery2) is treated as a single link tile with 3 endpoints. Building it places one tile and registers all 3 locations in the player's network. Smoke-tested: building the junction from a network that already contains Kidderminster correctly extends to Worcester + brewery2.
- **Network-connectivity check**: the first link's endpoints must include at least one location in the player's network. The second rail (if any) is checked against the network **extended with the first link's endpoints**, mirroring Lancashire's chain-build rule. First-build rule waives connectivity entirely when the player has no industries and no links on the board.
- **14-tile-per-era cap**: the engine pre-checks `countOwnedLinks(...) + linksNeeded ≤ 14` (Y-junction counts as 1 tile).
- **Coal sourcing**: cheapest connected coal mine (BFS distance across all anchors — endpoints of every link being built). Market fallback requires one anchor to reach an external market (Gloucester / Oxford / Shrewsbury / Nottingham / Warrington). Auto-flip when a coal mine empties; owner gets the income gain.
- **Beer for double-rail**: own brewery first (no connectivity needed — "own beer flies"), then opponent breweries reachable from the first link's endpoint. Merchant beer is NOT consumed here (rule clarification — merchant beer only applies to sells).
- **Card destination**: wild cards back to the wild stockpile, normal cards to the player's personal discard.

Dispatcher accepts both `buildLink` and `network` action types as aliases.

Phase 3d–3f (sell, develop, scout, era transition, scoring) still ahead.

### Phase 3b: Brass: Birmingham — buildIndustry (v1.0.129)

The build action is now fully wired for Birmingham:

**`lib/games/birmingham/markets.js`** — coal/iron market helpers (`buyFromMarket`, `sellToMarket`, `nextBuyPrice`, `reseedFromState`). Buys take from the cheapest occupied slot; sells fill the cheapest empty slot; bank fallback prices are £8/coal and £6/iron.

**`lib/games/birmingham/build.js` `actionBuildIndustry`** implements:
- **Card validation**: location / industry / millOrManuf / wildLocation / wildIndustry. Wild cards return to the wild stockpile (`state.wildLocationPile` / `state.wildIndustryPile`) on use, NOT to the player's discard — per the user's confirmed rule.
- **Wild-location ban** on the two unnamed breweries (`brewery1` / `brewery2`).
- **Slot-restriction rule**: if the location has a single-industry slot matching the chosen industry, that slot MUST be used; only fall back to a multi-type slot if no single-type is available. Confirmed working: building cottonMill at Kidderminster fills slot 1 (`[cottonMill]`) before slot 0 (`[cottonMill, coalMine]`).
- **First-build rule**: with an industry/wild-industry card, the location must be in the player's network — UNLESS the network is empty (very first build of the game).
- **Tile pulled** as the lowest-level available for that industry on the player's mat, subject to `canBuildCanal` / `canBuildRail` for the current era.
- **Resource cost**: tile money cost + iron cost (any iron works no-network, then market) + coal cost (board coal mines connected via network, then market). Coal market fallback REQUIRES a connection from the build location to any external market (Gloucester / Oxford / Shrewsbury / Nottingham / Warrington) — Birmingham rule. Iron market is always reachable.
- **Engine-recorded `totalCost`** = tile + market-bought coal/iron, attached to the action so the Largest Single Spend trophy stays accurate for Birmingham too.
- **Cube placement**: production tiles get their `coalCubesAdded` / `ironCubesAdded` / `barrelsAdded` (era-aware for breweries) cubes on the tile.
- **Auto-market push**: built iron works push their iron cubes to the iron market immediately (no connectivity gate); built coal mines push to the coal market IF the build location reaches any external market. Player gets paid the slot prices; tile cubes deplete; if the tile empties, it auto-flips and the player gains the tile's income.
- **Resource consumption auto-flips** opponent coal mines / iron works when their last cube is taken; the OWNER of the flipped tile gets the income gain (matches Lancashire).

Smoke-tested end to end: coal mine build at Dudley costs £5, decreases the mat from 7 → 6, leaves 2 cubes on the tile (no market reachable yet), card goes to personal discard. Wild-location targeting `brewery1` correctly rejected. Slot-restriction rule correctly picks slot 1 over slot 0 at Kidderminster.

Phase 3c–3f (network/sell/develop/scout, era transition, scoring) still ahead.

### Phase 3a: Brass: Birmingham — pass / loan / advanceTurn + network helpers (v1.0.128)

The Birmingham engine takes its first real steps:

**`lib/games/birmingham/network.js`** — BFS-based connectivity helpers that handle the 3-endpoint **Y-junction link** (Worcester ⇄ Kidderminster ⇄ UnnamedBrewery2). Walks neighbours from `link.endpoints[]` rather than the pairwise `from/to` Lancashire uses. Exposes `getPlayerNetwork` / `isConnected` / `getConnectedLocations` / `getConnectedWithDistances` / `countOwnedLinks` plus three resource-source finders (`findIronSources`, `findCoalSources`, `findBeerSources`) — the beer finder applies Birmingham's "own brewery = no network needed, opponent brewery = via network" asymmetry.

**`lib/games/birmingham/engine.js`** — Phase 3a action surface:
- `applyAction(state, userId, action)` dispatcher; everything not in this phase returns a `Phase 3 sub-phase pending` error.
- `actionPass` — discards the played card to the player's personal discard.
- `actionTakeLoan` — Birmingham's fixed £30 / 3-income-levels loan. Blocked when income value is below −7 (the rule the user clarified to be strict `<`, so income value −7 is still loanable). Also blocked in rail era after the draw deck exhausts (same restriction as Lancashire to keep end-game stable).
- `advanceTurn` — Birmingham's end-of-round phase order: reorder by spent-this-round (ascending, ties keep prior order), reset spent boxes, refill hands to 8 from the deck, **then collect income** (Lancashire collects at start-of-round; Birmingham at end). First canal round = 1 action per player; everything else 2.
- `collectIncomeOrPenalty` — implements the negative-income rule the user confirmed matches Lancashire: pay from money first, then remove cheapest tiles for half-cost-rounded-down each, then −1 VP per pound still owed.
- Era-end detection — when the deck and every hand are empty, the era ends. Phase 3f wires the actual canal→rail transition and scoring; until then the game just halts cleanly at `phase: 'finished'`.

**Smoke-tested** end-to-end with a pass-only game at 2P/3P/4P:
- 2P canal completes in 38 actions (= 1×2 + 2×2×9), final round 10, deck and hands empty.
- 3P canal completes in 51 actions (= 1×3 + 2×3×8), final round 9.
- 4P canal completes in 60 actions (= 1×4 + 2×4×7), final round 8.

Matches the deck math from the rule digest exactly. Phase 3b–3f (build, network, sell, develop, scout, era transition, scoring) still ahead.

### Lobby — game-type awareness, tiers reversed, players table with headers (v1.0.127)

**Games list**:
- Each game row now shows a small **BL / BB** game-type badge next to its name, colour-coded (Lancashire blue, Birmingham orange).
- A new tab strip above the games list filters by game type: **All Games | Brass: Lancashire | Brass: Birmingham**. Works on top of the existing status filter; persists via `localStorage.brass_gameTypeFilter`.

**System Data section** — new tabs **Total | Lancashire | Birmingham**. The route now computes per-game-type active/waiting/finished/total counts (`systemCounts.{lancashire,birmingham,total}`). Total is the default; Players count is only shown on the Total tab.

**ELO + Rankings sections** — new **Lancashire | Birmingham** tabs. The Birmingham panes show a "no data yet" placeholder until Phase 3 finishes Birmingham's engine and the first game produces ELO / VP records.

**Tier table** — order reversed: tier 9 (Birkenhead's Virtual Link Æther) at the top, tier 1 at the bottom, with the "No tier yet" row still at the very bottom.

**Players list → real table with headers**:
- Was a flex-row list; converted to `<table id="players-table">` with four columns: **Player**, **Tier**, **Metric**, **Last seen**.
- The Metric column header label updates dynamically (`sortPlayers()` writes "2P ELO" / "🏆 Ach" / etc. into `#players-metric-header`).
- The whole Metric column is hidden via CSS (`:has()` selector) when the active filter is Recent / A-Z; works on mobile too — the table layout handles the narrower viewport cleanly.
- Replaces the previous desktop-only one-badge-at-a-time hack with a column that's always semantically meaningful.

### Desktop players-list — one-metric-at-a-time filter (v1.0.126)
Each player row was showing all six metric badges in one line (2P/3P/4P ELO + 🏆 ach + 🔥 streak + 🎲 today) plus the activity timestamp. On desktop with a narrow players-list panel the row overflowed off the right edge.

The existing sort buttons (Recent / A-Z / 2P ELO / 3P ELO / 4P ELO / 🏆 Ach / 🔥 Streak / 🎲 Today) now **also** drive a single-active-metric filter. Each badge got a `prow-metric prow-metric-<key>` class pair; CSS hides every `.prow-metric` by default on desktop and shows only the one matching the players-list container's `data-filter` attribute. "Recent" and "A-Z" don't add any column. Name + tier badge + activity time remain always visible.

`sortPlayers(mode)` writes the active mode to `localStorage.brass_playerFilter` and an inline restore-on-load reads it back so the user's choice survives page reloads. Mobile is unchanged — the row layout there already tolerates all badges showing at once.

### Phase 2: Brass: Birmingham board data + state assembly + lobby picker (v1.0.125)

Birmingham games can now be **created and persisted** (their action surface is still stubbed — clicking anything will return a Phase 3 error from the engine).

**New data modules** under `lib/games/birmingham/`:
- `board-data.js` — 22 locations (incl. 2 unnamed breweries), 39 link tiles (incl. the **Worcester ⇄ Kidderminster ⇄ UnnamedBrewery2 Y-junction** modeled as one tile with 3 endpoints), 5 external markets with their bonuses (Develop, +2 income, +4 VP, +3 VP 4P-only, +5 money 3P/4P-only), 9-tile merchant pool (2 mill, 2 manuf, 1 pottery, 1 wild, 3 empty), coal/iron market setups (8/6 slot ladders with their starting cube counts of 13/8).
- `industry-data.js` — full per-level table for all 6 industries (cottonMill, manufacturer, pottery, ironWorks, brewery, coalMine) with cost / coal+iron cost / VP / **valueForLinks** / **barrelsToSell** / barrelsAdded (era-dependent for brewery L2-4) / developable flag / era-build flags.
- `card-data.js` — exact deck composition by player count (41/35/27 location cards + 23/19/13 industry cards = 64/54/40 deck, plus a separate pool of 4 wild-location and 4 wild-industry cards). The Mill/Manufacturer combined card is represented as `industryGroup: 'millOrManuf'` so the engine knows the player chooses at build time.
- `game-setup.js` — `createInitialState` that assembles a complete Birmingham state: 8-card hands + 1 face-down R1-compensation card per player, £17 start, income square 10, full board filtered by player count, shuffled deck, merchant tiles randomly assigned to merchant slots (each seeded with a beer cube), industry mats per player.

**Engine adapter** `lib/games/birmingham/index.js` now exposes the real `createInitialState`. Other methods (`applyAction`, `advanceTurn`, scoring, network helpers) remain Phase-3 stubs.

**Route layer** (`routes/lobby-routes.js`) — the create-game endpoints (`/games/create`, `/games/quick`, `/games/:id/start`, and the `startGameIfFull` auto-start helper) now dispatch `createInitialState` through the engine registry based on the game's `gameType`. `db.createGame(name, np, by, gameType)` accepts the new argument; both `/games/create` and `/games/quick` read `req.body.gameType` and default to Lancashire.

**Lobby UI** — the Custom Game form gets a "Game" radio group with two options: **Brass: Lancashire** (default) and **Brass: Birmingham** (labelled "early access — no bots, no playable actions yet"). Picking Birmingham creates a Birmingham game record + state.

**Phase 3 ahead** — wire `applyAction` for the 7 Birmingham actions (build, network, sell, loan, develop, scout, pass) including beer routing, merchant beer/bonus, the Y-junction in the network BFS, scoring (no money→VP, income → money → draw tiebreak), and the end-of-round income/turn-order phase (skipped in the final rail-era round).

### Phase 1: Brass: Birmingham engine-registry indirection (v1.0.124)

The route layer no longer imports `applyAction` / `getValidActions` / `advanceTurn` directly from `lib/game-engine.js`. Everything goes through a small registry:

- **`lib/games/registry.js`** — single dispatch point. `engineForState(state)` returns the right game's engine module based on `state.gameType` (defaulting to Lancashire for missing/legacy data). Two slots: `lancashire`, `birmingham`.
- **`lib/games/lancashire/index.js`** — adapter that re-exports the existing top-level modules (`game-engine.js`, `game-setup.js`, `scoring.js`) behind the shared engine interface. Zero behaviour change.
- **`lib/games/birmingham/index.js`** — Phase-1 stub. Every method returns an error like `Brass: Birmingham — applyAction not implemented yet (Phase 3 of the Birmingham integration)`. Lets the route layer compile against the registry without any conditional logic for the missing engine.

**Routes updated**:
- `routes/game-routes.js` — action submit (`/api/games/:id/action`), confirm-turn (`/api/games/:id/confirm-turn`), and valid-actions (`/api/games/:id/actions`) all now do `engineForState(state).{applyAction,advanceTurn,getValidActions}(...)`.
- `lib/bot-engine.js` — same. Bot driver picks the right engine for the game it's playing.

**Out of scope for Phase 1** (Phase 2/3 still ahead):
- `routes/lobby-routes.js` game-creation still hard-codes Lancashire (no game-type picker yet).
- `liveProjection` in `lib/scoring.js` is called directly from `lobby-routes.js` for active-game VP projection — Lancashire-only since Phase 1 only allows Lancashire games to be created. Will be routed through the engine once Birmingham scoring exists.
- Birmingham board data, industries, cards, sell flow, beer/merchant routing — all Phase 3.

### Phase 0: Brass: Birmingham game-type plumbing (v1.0.123)

No user-visible gameplay change — this is the foundation for the second game.

**New shared module**: `lib/game-types.js` defines two game types (`lancashire`, `birmingham`). Lancashire is the default with empty prefixes (so existing data shapes — `user.ratings['2p'|'3p'|'4p']`, `user.achievements.first_game`, etc. — are unchanged). Birmingham uses `bb_` ELO prefix and `bb:` achievement-ID prefix when it ships.

**State + game records carry `gameType`**:
- `lib/game-setup.js` `createInitialState` writes `gameType: 'lancashire'`.
- `lib/db.js` `createGame(name, np, by, gameType)` writes the field on every new game record.
- A one-time migration (`data.meta.gameTypeBackfillDone`) walks every existing game record AND every saved state and stamps them with `gameType: 'lancashire'` so reads can rely on the field being present.

**Hall of Fame is now per-game-type**:
- `lib/hall-of-fame.js` `compute(db, gameType)` filters `finishedGames` by `g.gameType`. Default 'lancashire' so legacy callers behave identically.
- `lib/db.js` `getHallOfFame(maxAgeMs, gameType)` keeps a per-type cache under `meta.hallOfFameByType[gt]`. The legacy `meta.hallOfFame` slot is mirrored as the `lancashire` cache on first access.
- `routes/lobby-routes.js` `/api/hall-of-fame` accepts `?gameType=…` and routes to the matching cache.

**Lobby UI**:
- New tabs at the top of the Hall of Fame section: **Brass: Lancashire | Brass: Birmingham**. Click to swap which game's HoF is shown. Birmingham renders empty until any Birmingham game finishes.
- Per-tab cache invalidation is automatic — clicking a tab fetches `/api/hall-of-fame?gameType=…` and re-renders.

**Wiki**:
- New page at `/wiki/birmingham` with the full rule digest gathered in this session (setup numbers, action list, beer mechanics, scout, scoring, tiebreaker, etc.).
- Nav link added to every existing wiki sub-page + index.

**Out of scope for Phase 0** (handled by later phases):
- No game creation picker for Birmingham yet — only Lancashire games can be created.
- No Birmingham engine — `lib/games/birmingham/` doesn't exist yet.
- Existing achievements/trophies are not yet tagged by `gameType` (will happen alongside the engine fork in Phase 1).
- ELO storage keys for Lancashire remain `ratings.2p|3p|4p`. Birmingham will write to `ratings.bb_2p|bb_3p|bb_4p`.

### Batch 5: 9 new achievements + 6 new trophies + a Badges wiki page (v1.0.122)

**9 new achievements** (`lib/achievements.js`):
- **Pioneer** — built the very first link in a game.
- **Tower** — finished owning 3+ tiles at the same location.
- **Cotton Stockpile** — finished with 3+ of your own cotton mills still unflipped.
- **Eve of Destruction** — overbuilt 3+ opponents' tiles in one game (attacker side of Demolisher).
- **Sleeper Win** — won despite a 48+ hour gap between two of your own actions.
- **Centurion +1** — finished with exactly 101 VP.
- **Apprentice** — used the Develop action for the first time.
- **Daily Double** — finished 2+ all-human games on the same UTC date.
- **Burst Mode** — submitted 6+ actions within a single hour (action-kind, uses a new rolling 1-hour buffer on the user record via `db.recordRecentActionTs`).

**6 new trophies** (`lib/hall-of-fame.js`):
- **Overbuilds Caused** (Battle, 💥) — lifetime count of opponents' tiles you overbuilt.
- **Tile-Level Sum** (Industry, 🏭) — sum of levels across every tile you've ever built. Rewards quality over quantity.
- **Biggest Hoard at Game End** (Money, 💰) — largest money any player has ended a game with.
- **Smallest Winning Hoard** (Money, 🪙) — smallest money a solo winner has ended with.
- **Days Active** (Activity, 📆) — distinct UTC dates with at least one action.
- **Tenure** (Activity, 🕰️) — days since the player's very first recorded action.

**Wiki — new "Badges" page** at `/wiki/badges`. Auto-generated from `lib/achievements.js` and `lib/hall-of-fame.js`, so it always reflects the live definitions. Listed by group (game-end / action / streak for achievements; Ratings / Activity / Games / Records / Industry / Money / Selling / Building / Battle / Duration / Means for trophies). Each trophy has a one-line explanation of what it actually tracks. Link added to every wiki page's nav strip and the wiki index. All wiki pages render with the same chrome.

Total: **83 achievements** / **73 trophies**.

### Remove the Distinct Industries trophy (v1.0.121)
The `distinctIndustries` trophy (number of unique industry types built lifetime, capped at 5) was too easy to max out — any active player would saturate it within their first ~5 games and it stopped distinguishing anyone. Removed the trophy definition, its accumulator, its aggregation, and its `add()` call.

### HoF canal/rail classifier — timestamp-based era fallback (v1.0.120)
Even with the v1.0.118 set-based dedup, the canal/rail split was still skewed (e.g. **21 canals vs 300+ rails** lifetime) because pre-v1.0.88 buildLink actions (no `totalCost` in their `action_data`) fell through to the "final `link.type` on the board" fallback. Any canal that was later rebuilt as a rail on the same link → final `link.type === 'rail'` → mis-classified as rail.

Better fallback: every log entry is prefixed by `[C${round}]` or `[R${round}]` via `logMsg`. `findEraTransitionTs(state)` scans `state.log` for the first `/^\[R\d/` entry and returns its `ts` — that's the wall-clock instant when the canal→rail transition happened. The classifier now compares the action's `created_at` against that boundary to assign era retroactively. Last-resort `link.type` fallback only fires when both `totalCost` and the boundary timestamp are missing (very old games or corrupted state).

Result: lifetime canal / rail counts and their Avg per Game means re-stabilise on historic games. New games are unaffected (already accurately tagged via `totalCost`).

### Batch 4: 11 new achievements + 7 new trophies (v1.0.119)

**Engine flag** — `actionBuildIndustry` now sets `action.usedWeirdestRule = true` when a build succeeds via the rail-era Birkenhead/Liverpool cross-Mersey exception. Persisted into `action_data` so the new achievement can detect it retroactively for future games.

**11 new achievements** (all evaluated at game end except Insomniac which is action-time):
- **Mersey Bridge** — build a Liverpool or Birkenhead industry using the Weirdest Rule (across-the-Mersey without an actual link).
- **Both Sides of the Mersey** — build at least one industry in BOTH Liverpool and Birkenhead in one game.
- **Income Sky** — reach the top of the income track (square 97+, +30/turn) in a single game.
- **Skinflint** — win a game with under £10 remaining.
- **Untouched** — win without any of your tiles being overbuilt.
- **First Light** — submit the very first action of a game.
- **Closer** — submit the last action of a game (the one that triggers end-of-rail-era scoring).
- **Pure Strategy** — win without taking a loan AND without ever passing.
- **Pass Master** — pass 5+ times in one game (joke achievement).
- **Snake Eyes** — finish with 0 or 1 VP.
- **Insomniac** — submit a turn between 03:00–05:00 local time (narrower window than Night Owl, action-kind).

**7 new trophies** in the Hall of Fame:
- **Highest Income Square** (Records, 📈) — peak income-track square reached at any game end. Display includes the +N/turn value via `incomeTrack[v]`.
- **Wild Builds** (Building, 🌟) — lifetime count of `buildIndustry` with `wildBuild: true`.
- **Tiles Developed** (Building, 🛠️) — lifetime sum of `develops.length` across all `develop` actions.
- **Distinct Industries** (Industry, 🧭) — number of unique `industryType` values in your lifetime build log (max 5).
- **Earliest First Sell** (Selling, ⚡) — smallest delta from `game.started_at` to the first `sellCotton` action across all games (uses `pickHoldersMin`).
- **Latest Last Sell** (Selling, 🕰️) — largest delta from `game.started_at` to the last `sellCotton` action across all games.
- **Closest Tiebreak Margin** (Battle, ⚖️) — for games that ended with 2+ tied at top VP, the smallest diff at the first tiebreak chain level (income → money → spent → turn-order) where they differ. Attributed to the tiebreak winner.

All wired into `emptyAcc`, `aggregateGame`, `METRIC_DEFS`, and the `add()` calls in `compute()`. English `ach.<id>` translations added so non-English UIs fall back to English instead of showing raw IDs.

### HoF link counts: set-based dedup + safer classifier (v1.0.118)
The "Most Links in a Game" trophy was capable of showing values above the physical 28-tile cap (14 canal + 14 rail per player). Two ways that could happen:
1. **Classifier rule 1** (`secondLinkId → rail`) was triggering on canal-era actions that had a stray `secondLinkId` field (UI-bug or migration artifact), then counting them as 2 rails instead of 1 canal.
2. **Duplicate buildLink entries** in the action log (rare retry/replay paths) were counted as additional links.

Two structural fixes in `lib/hall-of-fame.js`:
- The classifier no longer trusts `secondLinkId` on its own to imply "rail". It now goes: `resourcePlan present → rail` (canals never consume resources); then `totalCost ∈ {3, 6} → canal, else rail` (v1.0.88+); then a fallback to the final `link.type` on the board. Stray `secondLinkId` on a canal action no longer poisons the classification.
- The per-game link counter switched from `count += 1 (or +2 for double-rail)` per action to a **Set of distinct linkIds per era per player**. Each link is counted at most once per era regardless of how many times the action appears in the log, so the per-game total naturally caps at 28 (14 canal + 14 rail). The lifetime `canals`, `rails`, and `links` accumulators are bumped from those set sizes at the end of each game.

Side effect: the "Avg Canals / Game" and "Avg Rails / Game" means trophies will also re-stabilise — they were inflated by the same misclassification.

### Locations more opaque when the map slider is dimmed (v1.0.117)
Previously the location rectangles on the board had a fixed `~67%` fill alpha (`#d6c8a8aa`). With the map at full brightness it looked balanced — the colourful map provided the background contrast — but with the map slider dragged left toward 0%, the dark page background showed through the translucent rectangles, making the location names and slots hard to read.
The fill-opacity now tracks the map slider inversely:
- map at 100% → location fill 60% (slightly more transparent than before, since the map carries the contrast)
- map at 70% (default) → location fill 72% (very close to the old default look)
- map at 0% → location fill 100% (solid tan, fully readable against the dark page)
Updates happen live in-place via `querySelectorAll('rect.board-location')` while dragging the slider, no re-render needed.

### Mobile lobby — top of the achievements bar was clipped by the fixed navbar (v1.0.116)
The mobile navbar wraps to two lines once the nav-links + tier badge + username + logout button don't fit in one row at 12 px. The lobby's `padding-top: 50px` wasn't enough to clear that, so the top of the achievements / streak bar (and even the very top of the trophy icon) was hidden behind the navbar's second line. Bumped `body.is-mobile .container` top padding from 50 to 90 px.

### Top-of-lobby tier progress bar showed NaN / NaN (v1.0.115)
When building the per-user `userTiers` map in `routes/lobby-routes.js`, the route copied only `{ id, name, icon, turns }` from the matched tier object — `threshold` was dropped. The top-of-lobby progress bar reads `myTier.threshold` to compute the lower bound of the current tier's span, so it was `undefined`, every arithmetic op became `NaN`, and the bar rendered "NaN / NaN (NaN%)". Fix: include `threshold` in the stored object.

### Tier visibility — who's in which tier (v1.0.114)
Two surface-level additions so tiers are easy to see at a glance:
- **Tier section** now has a fourth "Players" column listing every user whose current tier matches that row. Untiered users (not enough turns yet) get their own row at the bottom. Donor highlights are preserved on the names; clicking a name jumps to that user's profile.
- **Players list** in the sidebar — the small tier icon next to each name is replaced with a proper gold-tinted **tier badge** that shows both the icon AND the tier name (e.g. `🪵 Wooden Coins`). Clicking it jumps to the Tiers section. Hover tooltip still shows tier number and lifetime turn count.

### Tier progress bar moved to the top of the lobby (v1.0.113)
The "progress to next tier" bar now sits right under the my-turn / up-to-date banner at the very top of the lobby so it's visible at a glance — no need to scroll to the Tiers section to see how close you are to the next rank. The bar is a clickable anchor that jumps down to the full Tiers section (which still has the descriptive text and the table of all tiers, just without the duplicate bar inside it).

### Distant-market bottom-out on last action of last round now holds for confirm (v1.0.112)
Fixed a hole in the end-of-turn confirmation path. When a player sold cotton to the distant market and the market **bottomed out** (demand dropped to 0, flipping the last demand tile), the engine force-finalised the sell action — but because the client had submitted it as a `partial: true` sub-action, no `holdForConfirm` flag was attached. The dispatcher therefore advanced the turn immediately. If that happened on the player's last action of the last round, the game ended with no confirmation dialog and no chance to reset.

Fix in `lib/game-engine.js`: the dispatcher now treats `result.bottomedOut === true` on the player's last action of the turn (`actionsRemaining === 1`) as an implicit hold — sets `pendingConfirm` and waits for the player to explicitly confirm (or reset, where reset is still meaningful).

Side fixes in `routes/game-routes.js`: the "your turn" notification and the bot trigger after action submission now check `result.newState.pendingConfirm` in addition to `action.holdForConfirm`. Previously a server-side hold (added in this release) would have wrongly notified the same player it's their turn and pinged the bot driver, because both blocks only watched the client-set flag.

### Largest Single Spend trophy now counts coal/iron cubes, not just base cost (v1.0.88)
The trophy was using the **base tile/link cost** only (proxy), so a shipyard L2 build (£25 tile) showed as £25 even when the player also paid £5+£5 for iron and coal from a depleted market. That's the issue you spotted (£15 record was the £15 base for a double-rail — true cost can be £25 once coal market is dry).

Fix is wired front-to-back:
- `lib/game-engine.js` — `actionBuildIndustry`, `actionBuildLink` (canal + single-rail + double-rail), and `actionDevelop` now all return `{ success: true, totalCost }` where `totalCost = base + market resources actually paid`. The dispatcher carries it up to the caller.
- `routes/game-routes.js` + `lib/bot-engine.js` — embed `action.totalCost` into the stored action_data before stringifying, so the figure is persistent.
- `lib/hall-of-fame.js` — the trophy aggregator prefers `parsed.totalCost` when present and falls back to the old base-cost proxy for actions recorded before v1.0.88. So pre-existing games keep their (low) proxy value and new spends start setting accurate records.

Spend now includes:
- **Build industry**: tile cost + each coal/iron cube actually purchased from market (board cubes are still free, as in the rules).
- **Build link (rail, single)**: £5 + coal cost (£0 if board, up to £5 if market-bought).
- **Build link (rail, double)**: £15 + 2× coal cost.
- **Build link (canal)**: £3.
- **Develop**: iron market cost (board iron is free, so this is only > £0 when the player explicitly buys iron from the market).

### Turn navigator moved up + buttons fit in the panel (desktop) (v1.0.87)
The ⏮ ◀ ▶ ⏭ rewind / fast-forward controls in the desktop left panel were buried at the bottom of the panel, below the long Controls description block. They're now placed **right under the player cards** where they're easy to reach.
The buttons were also wider than the panel, so the outermost ⏮ and ⏭ were clipping outside the section. The `.nav-btn` rule was shrunk for desktop (font-size 10px, padding 3×4, min-width 22) and the inline row gap dropped from 6px to 3px and the label min-width from 60px to 44px, so the 4 buttons + label now sit inside the 195px-wide panel section without overflowing. Mobile keeps its own larger `.nav-btn` sizing (the `body.is-mobile .nav-btn` override is untouched).

### Floating VPs panel — bump gap between coloured + gold hex from 1px to 2px (v1.0.86)
Tiny visual tweak: shifted the gold delta hex one more SVG-unit right so the gap between the two hexes is ~2px instead of ~1px.

### Floating VPs panel — 1px breathing room between coloured + gold hex (v1.0.85)
Tiny visual polish: nudged the gold delta hex 0.3 SVG-units further right so there's a ~1px gap between the coloured hex's right vertex and the gold hex's left vertex, instead of them touching.

### Floating VPs panel — delta wrapped in a gold hex, snug to the coloured one (v1.0.84)
Two refinements to the v1.0.83 floating-VPs-panel delta:
- The `+N` is now drawn inside its own gold outline hex (r=6, no fill, gold stroke) so it matches the pink coloured hex as a sibling — same shape language as the player-bar `.vp-potential` badge in HTML.
- The gold hex now touches the coloured hex on the right, instead of floating a few px away.
- The player name shifted slightly further right (pos.x+32 worth of clearance) so the gold hex doesn't clip into the first letter of the name even for long usernames or donor emoji prefixes.

### Potential-VP delta on the floating VPs panel + revert player-bar to delta (v1.0.83)
Two adjustments:
- The **player bar on the left** goes back to showing the gap as `+N` (the v1.0.82 absolute-value rendering was a misread of the request).
- The **floating VPs panel** (the on-board panel showing player name + VP hex) now also gets a small gold `+N` between the hex and the player name when there's a gap to potential. The name was nudged 10 SVG-px to the right to make room. Hidden when the delta is 0 or when the game is finished.

### Potential VP shown as an absolute value in the player bar (v1.0.82)
The gold-outlined hex next to each player's live VP score used to display the **delta** to their potential ("+12"). Now it shows the **absolute potential VP** ("97"), so the player bar always shows both numbers side by side: pink hex = projected score right now (live, if scoring happened in this state), gold hex = best-case score if every tile on the board flipped. The tooltip on the gold hex still surfaces the delta. Mobile and desktop both use the same pattern — both hexes are the same size (16×15 desktop, 18×17 mobile), so the layout is unchanged.

### i18n catch-up + upset-trophy default-elo fallback (v1.0.81)
Three small bug fixes around the v1.0.80 release:
- Added the missing `hof.group.duration` translation key in all 10 languages — the Duration trophy group was rendering its raw key (`hof.group.duration`) in the lobby instead of a label like "Duration" / "Duración" / "Durée".
- Added English `ach.<id>` entries for 16 achievements that were missing them (`globe_trotter`, `marathon_builder`, `wide_builder`, `loan_king`, `coal_tycoon`, `demolisher`, `speedrun`, `marathon`, `dynasty`, `reset_master`, `polyglot`, `customizer`, plus the older `track_layer`, `iron_horse`, `tiebreak_champion`, `bridesmaid` that had slipped through). Non-English languages fall back to English instead of showing the raw `ach.<id>` key. Translations into the other 9 languages can be filled in later.
- Hardened the "Higher-ELO Beaten" (`upsets`) trophy: missing per-player entries in `state.eloAtStart` now default to 1000 (same convention as the `giant_killer` achievement) instead of silently skipping the comparison. The trophy can still be unclaimed if no one has actually outscored a higher-rated opponent yet, but it no longer drops valid candidates when one player's snapshot row is incomplete.

### Eleven New Trophies + Twelve New Achievements (v1.0.80)
A big batch of new Hall of Fame trophies and per-player achievements, shipped in three logical groups inside one release.

**New trophies (11)** — added across the existing Records / Battle / Means / Activity groups:
- 🏗 **Most Tiles in a Game** — peak `buildIndustry` count across all your games (single game).
- 🛤 **Most Links in a Game** — peak built links (rails count 2 in a double-rail action).
- 🌍 **Widest Build (locations)** — most distinct locations you built in inside one game.
- 💸 **Largest Single Spend** — the biggest single-action money cost (proxy from tile/link base cost).
- 🎯 **Closest Win** — smallest VP gap between a solo winner and 2nd place. Holder = the winner of that game.
- 💥 **Largest Win Margin** — biggest VP gap, same attribution.
- 🏛 **Opponents Faced** — number of distinct humans you've sat down with in at least one all-human game.
- 🏅 **Longest Winning Streak** — most consecutive game wins in chronological order.
- 🐌 **Slowest Avg Turn** — per-player mean turn duration across all timed turns (≥5 turns to qualify).
- 🚀 **Fastest Avg Turn** — same metric, smallest mean.
- 🔁 **Most Concurrent Games** — peak count of in-progress all-human games you were in at any single instant. Computed by walking each game's `started_at → finished_at` interval per player.

Each trophy whose record points at a single game gets a deep-link to that game (same pattern as v1.0.70/v1.0.71). Win-streak trophy links to the game that closed the streak.

**New achievements (12)**:
- **Globe Trotter** — own a link to all three external ports (Scotland, Yorkshire, The Midlands) in one game.
- **Marathon Builder** — build 15+ industry tiles in a single game.
- **Wide Builder** — build in 8+ distinct locations in one game.
- **Loan King** — take all three loan bands (£10, £20, £30) in a single game.
- **Coal Tycoon** — build 5+ coal mines in a single game (Coal Baron is the all-7 max).
- **Demolisher** — be overbuilt 3+ times in a single game (your tiles got replaced).
- **Speedrun** — be in a game that finished within 24 hours of starting.
- **Marathon** — be in a game that took 30+ days to finish.
- **Dynasty** — win 5 all-human games in a row.
- **Reset Master** — use Reset Turn 50 times across your games. Tracked via a new `user.resetCount` counter, bumped on every successful `/reset-turn`.
- **Polyglot** — use the interface in at least 3 different languages. Tracked via a new `user.langsUsed` array, appended on every language change.
- **Customizer** — try at least 10 different name highlights. Tracked via a new `user.donorStylesTried` array, appended every time you pick a non-empty style on the account page.

**Notes**:
- Trophy/achievement counts will populate live for new games and as users do new things; the Reset/Polyglot/Customizer trackers don't backfill (no historical events to count for those), so a returning player needs to do the action once to start their counter.
- Marathon Builder / Pacifist note: `Self-Made` already covers "win without taking any loans", so Pacifist was substituted with Marathon Builder. Underdog Crown was likewise substituted with Dynasty since `underdog` and `comeback_kid` already cover the lowest-rated-wins / last-place-comeback angles.
- Hall of Fame total goes from 50 → 61 trophies. Achievements total goes from 51 → 63.

### Stale-Trophy-News Cache-Hit-Path Scrub (v1.0.79)
- The v1.0.78 stale-trophy-news scrubber only ran on the recompute path, so cache hits left the bogus pre-v1.0.77-fix trophy news visible until the next recompute. Hooked the scrub into the cache-hit branch too, plus a one-shot cache invalidation on first boot so the next read forces a fresh recompute and clean news.

### Stale Trophy News Scrub on HoF Recompute (v1.0.78)
- The pre-v1.0.77-fix Hall of Fame had been emitting trophy-change news entries with values inflated by the link-classifier bug ("Avg Rails / Game" > 14, etc.). Scrubbed those stale entries on every Hall of Fame recompute so the news feed stops showing the bogus numbers. Also added a CLAUDE.md note about the git-proxy 403 workaround via the GitHub MCP.

### Hall of Fame classifyLinkBuild Fix (v1.0.77)
- `classifyLinkBuild` previously checked `parsed.coalSources || parsed.resourcePlan || ...` as a "rail" heuristic, but the client sometimes leaves those initialised to `[]`. Empty arrays are TRUTHY in JS, so canal builds were being mis-classified as rails — inflating `acc.rails` / `acc.links` and producing impossible "Avg Rails / Game > 14" trophies. Replaced with a single read of `state.board.links[linkId].type` (which is `'rail'`, `'canal'`, or `null` for a removed canal — anything not `'rail'` is treated as canal).

### Shortest Turn Trophy (v1.0.76)
- New 💨 **Shortest Turn** trophy in the Duration group: the smallest single-turn elapsed time recorded across all all-human finished games. Same calc as Longest Turn (skips the game's very first action so lobby-discovery lag doesn't poison the result), tracked alongside it.
- Holder is the user who has the smallest minimum turn anywhere; value links to the exact replay version of that turn (`?v=…`), same deep-link pattern as Longest Turn.
- New `pickHoldersMin` helper for "smallest positive value across users" trophies — reusable for other future "lowest" records.

### Mobile News Init Targets the Right Wrapper Node (v1.0.75)
- On mobile, the lobby's news section was permanently hidden after the first toggle: the mobile-init code set `display:none` on `#news-list` (the inner entries div) but the header toggle has been operating on `#news-section-body` (the outer wrapper added in v1.0.56) since the filter pills were introduced. So clicking the header toggled the wrapper but the inline `display:none` on the inner list never got cleared, leaving the entries permanently hidden even when the wrapper showed as expanded. Fix: mobile init now targets `#news-section-body`, the same node the header toggle works on, so a single header click cleanly expands the whole panel.

### Updated Controls Help Panel in the Left Sidebar (v1.0.74)
- The in-game Controls help panel (left sidebar) was missing about half of the toggles that have been added in the last few months. Rewritten with section headings (Sliders / Display / View filters / Hand & turn) and entries for every current control:
  - View filters: Only mine, Hide flipped, No blink, Highlight active, Dim flipped (4 steps), £ number money toggle.
  - Hand & turn: Reset Turn (appears mid-turn), Confirm Turn (with the checkbox to enable/disable), Time toggle in the log header for timestamps.
  - Display: split Undo and Like Xai into separate entries with clear descriptions.
- Sliders annotated with their actual ranges (Bot delay 1–5s, Dim flipped Off→Strong).

### Game-Duration Trophies Use started_at + Auto-Start Self-Heal (v1.0.73)
- **Game-duration trophies** (Shortest Game, Longest Game, Slowest/Fastest Avg Game) now measure from `started_at` (when the lobby filled and play began) → `finished_at`, not from `created_at`. Same reasoning as the v1.0.72 longest-turn fix: the lobby-wait period isn't really "playing the game", and including it inflates the duration. Old games that don't have `started_at` recorded fall back to `created_at` so they're not excluded entirely.
- **Lobby auto-start self-heal**: every lobby render now checks each waiting game for full seats and runs `startGameIfFull` on it. Games that filled via a path that bypassed the auto-start hooks (or were created before v0.0.335 added them) now flip to Active on the next lobby load instead of getting stuck in Waiting until someone manually starts. Existing per-action auto-start hooks (join, accept-invite, add-bot, create) still fire immediately as before.

### Longest-Turn No Longer Charges the First Player for Lobby Lag (v1.0.72)
- **Bug**: the Longest Turn trophy reported things like "8d 7h" and deep-linked to `?v=1`, but reviewing the game's logs nothing of the sort happened. Cause: the calc anchored the very first action against `started_at` (or `created_at` as a fallback), so the first player got charged for the entire period between the game becoming active and them noticing it had started — often days, since async play.
- **Fix**: the very first action of each game is now skipped from longest-turn timing. There's no real "previous turn" to anchor against. Timing starts at the SECOND player's first action, where the boundary is real (their wait clock starts when the first player committed their last action). All subsequent turn boundaries unchanged.
- Net effect: the trophy now reflects actual turn-to-turn elapsed time rather than initial-onboarding lag. Recompute on next Hall of Fame refresh.

### Longest-Turn Trophy Deep-Links to the Exact Replay Moment (v1.0.71)
- The **Longest Turn** value link now goes to `/games/<id>?v=<version>` — the state version of the action that ENDED the longest turn — so clicking it doesn't just open the game, it jumps the replay navigator to that exact moment.
- Implementation: the aggregator already tracked `gameId`; now it also captures the `state_version` of the turn-ending action. `pickHolders()` accepts an optional `versionMap` and threads it onto the picked result; `add()` builds the link with `?v=`.
- Game page reads the `v` query param in `init()` and calls the existing `fetchAndShowVersion()` after the live state has loaded — so the "back to Live" button still works and polling stays aware of the underlying live version.
- The other linkable trophies (Shortest Game, Longest Game, Highest VP per count) still link to the game's live page (no `?v=`) since their record is the GAME, not a specific moment.

### Hall of Fame: Trophy Values Link to Where It Happened (v1.0.70)
- The trophy values that point to a single specific game now render as links to that game. Clicking the value opens the game.
- Trophies with links:
  - **Shortest Game / Longest Game** → link to one of the matched games (most-recent, if there's a tie).
  - **Longest Turn** → link to the game where the holder's longest turn occurred.
  - **Highest VP — 2P / 3P / 4P** → link to the game where the holder hit that VP.
- Trophies that aggregate across many games or aren't tied to a single event (counts, means, ELO ratings, achievements, streaks, etc.) keep showing a plain value with no link, since there's no single source to point to.
- Hover styling matches the existing player-name links: gold highlight + underline.

### ELO Log Shows Live Rating + Game-Over Shows Your Placement (v1.0.69)
- **ELO log fix**: the `=== ELO update ===` lines on the game state log were showing `snapshot_before → snapshot+delta` (the rating you had when the game STARTED, plus the delta). The actually stored rating is `live_before + delta`, where `live_before` already reflects any concurrent games that finished in the meantime — so the log number didn't match the rating shown elsewhere. Now the log captures each player's live rating right before applying the delta and prints `live_before → live_after`, so the numbers line up with what you see on the lobby/profile/account pages. The delta itself is unchanged: still computed against the snapshotted ratings (so finishing order between concurrent games doesn't matter).
- **Game-over banner now shows your placement** if you weren't the winner. Examples:
  - `Winner: alice 124 VP — You finished 2nd out of 4 (108 VP)`
  - `Co-winners: alice & bob 142 VP — You finished tied for 3rd (2 players at 95 VP)`
  - If YOU were a co-winner, the banner reads `tied for 1st with 1 other (142 VP)`.
- Banner formatting is in `updateGameInfo()`, so it applies on the live game page and any time the panel re-renders.

### Account Page: Vertical Breathing Room Between Sections (v1.0.68)
- The Language, Name highlight, and Favorite color section descriptions all carried `margin-top: 0` inline, so each paragraph sat flush against its section header. Replaced with `margin: 6px 0 14px` (above + below) and gave each h3 a `margin-top: 32px` for clearer section separation.
- Same change applied to all three sections; the rest of the account page (ELO grid, Achievements, Change Password, Game History) was already spaced fine.

### Radioactive Section: Inline-Flex Was Beating display:none (v1.0.67)
- The collapse toggle was firing correctly and adding the `collapsed` class — but the buttons stayed visible because their inline `style="display:flex; flex-wrap:wrap; …"` has higher CSS specificity than a normal class selector. The `display:none` rule on `.radio-section.collapsed .radio-buttons` was silently overridden. Fixed with `!important` on that one rule.

### Smooth Radioactive Pulse + Working Collapse Toggle (v1.0.66)
- **Pulse no longer leaps in brightness**: the v1.0.65 keyframes used `color-mix(in srgb, currentColor X%, transparent)` to dial the alpha across the cycle, but at least one engine wasn't smoothly interpolating it across keyframes — the result jumped between two states instead of easing. Switched to pure blur-radius modulation on `currentColor` (a tight 2px+5px halo at the cycle's bottom, an 8px+16px bloom at the peak), which interpolates reliably everywhere. The gradient-text variant got the same treatment with matched filter-layer counts at both ends.
- **Radioactive section collapse now actually toggles**: the inline `onclick` handler from v1.0.64 wasn't taking effect (likely a quoting/scoping quirk inside the form). Replaced with a defined `toggleRadioSection(this)` function plus `event.preventDefault()` so it can't accidentally submit the form.

### Radioactive Pulse Matches Coal-Ember Rhythm (v1.0.65)
- The v1.0.64 keyframes pulsed all the way to fully transparent at the bottom of the cycle, which over-corrected the previous "always-glowing" complaint. Returned to the same rhythm as `donor-coal-ember`: a soft glow at the low end (smaller blur + reduced alpha), full glow at the peak.
- For solid-colour variants, alpha is applied to `currentColor` via `color-mix(in srgb, currentColor X%, transparent)` so each variant pulses in its own colour with the right intensity envelope. Modern-browser-only (Chrome 111+, Firefox 113+, Safari 16.2+).
- Gradient-text variants (rainbow / disco / ocean / galaxy / diamond) use the same rhythm via white-ish `drop-shadow` filters since `currentColor` is transparent in their case.

### Radioactive Pulse Hits Zero + Collapsible Section (v1.0.64)
- **Pulse now goes to (almost) no glow at the bottom of the cycle**, mirroring the way the coal ember reads — full glow at 50%, transparent at 0%/100%. Previously both ends had visible shadows so the variation looked compressed.
- **Radioactive section on the account page is collapsed by default** to avoid the wall-of-pulses effect when 28 samples are rendered side by side. Clicking the `☢️ Radioactive` header toggles it open. If the user has currently selected a radioactive style, the section starts open so they can see the active pick.
- **Collapsed body is removed from the layout** (`display:none`), so the animations don't keep running off-screen — saves the GPU and keeps the page calm.

### Highlight Picker Shows Style Names + Live Nick Beside the Heading (v1.0.63)
- **Picker samples now show the style's own name** ("gold", "dragon", "radioactive dragon", ...) styled in that highlight, instead of repeating the user's nick on every button. Easier to scan and to spot which one is which.
- **Section heading is now `Name highlight: <your nick>`** with the nick rendered in the currently-applied highlight, so you always see what you actually look like across the lobby. Plain nick if no highlight is selected.
- **Radioactive labels** read as "radioactive dragon", "radioactive coal", etc. (space, not hyphen) for the visible label; the underlying style ID stays `radioactive-dragon` etc.
- **Radioactive pulse slowed from 1.6s → 2s** (only the radioactive variants; the base coal-ember / fire / ghost animations are unchanged).

### Radioactive Variants — Drop the ☢️ Prefix, Pulse in Base Colour (v1.0.62)
- The radioactive variants from v1.0.61 were over-stylised: a forced green text + a ☢️ prefix emoji on top of the base style's emoji. Reworked to match the original brief: each radioactive variant keeps the **same colour** and the **same emoji** as its base style, and the only added thing is a coal-style pulsing glow that radiates in the text's own colour.
- Solid-colour variants (gold/silver/dragon/coal/etc.) share a single keyframes that uses `currentColor` for the text-shadow, so the pulse takes each variant's colour automatically. Per-variant CSS just copies the base's colour/border/emoji.
- Gradient-text variants (rainbow, diamond, galaxy, ocean, disco) use a separate `filter: drop-shadow` pulse since `currentColor` is transparent there — the glow shows through the gradient. Disco keeps its background-position shift; the radioactive pulse runs alongside it.

### Radioactive Variants for Every Name Highlight (v1.0.61)
- **28 new "radioactive" name highlights**, one per existing style. Each is `radioactive-<style>` (e.g. `radioactive-dragon`, `radioactive-coal`, `radioactive-rainbow`).
- All radioactive variants share the same green pulsing glow — modeled on the radiating ember effect of the existing **coal** style — and each one keeps the original style's emoji, prefixed with ☢️ (so `radioactive-dragon` reads as ☢️🐉 with the green pulse).
- The account-page picker shows them as a separate row below the regular ones, under a small `☢️ Radioactive` sub-heading. Click-to-apply works the same as the base styles.
- Implementation note: the animation + green color + transparent-text reset live in a single shared `[class*="donor-radioactive-"]` rule, so each variant only needs its own `::before` content line. Adding more later is a one-line change.

### £-Number Toggle Re-Renders Spent Box (v1.0.60)
- **Bug**: ticking the "£ number" checkbox flipped the player-bar money display from coin discs to a "£N" string instantly, but the same toggle on the on-board Spent box only took effect after a full page reload.
- **Cause**: the toggle handler refreshed only the player bar. The Spent box is drawn by the board renderer, which has a cache key based on `gameState`. Since the underlying state didn't change, the cache short-circuited the re-render.
- **Fix**: after flipping the flag, the handler now also clears the board cache key and calls `BoardRenderer.render()` directly, so the Spent box (and anything else on the board that reads `moneyAsNumber` at draw time) updates immediately.

### Duration Trophies + Hall of Fame Filters + Loan Order (v1.0.59)
- **Five new Hall of Fame trophies** under a new **Duration** group:
  - **⚡ Shortest Game** — players in the all-human finished game with the smallest creation→end span. Shared by everyone in that game (and across multiple games if there's a tie).
  - **🐢 Longest Game** — same idea, max span.
  - **⏱️ Longest Turn** — single longest turn anyone has taken in any game (elapsed clock from when it became their turn until they submitted their last action of that turn — includes thinking/AFK time, which is most of it for async play).
  - **🐌 Slowest Avg Game** and **🚀 Fastest Avg Game** — per-player mean game duration. Both require ≥2 finished games for the player to qualify, so a single quick game can't trivially crown someone.
- **Hall of Fame filter pills**, news-style: 11 group toggles (Ratings, Activity, Games, Records, Industry, Money, Selling, Building, Battle, Duration, Means) plus a × clear button. Empty filter set shows everything (default); selecting one or more shows only those groups. Filters don't persist across page loads — same as the news panel.
- **Loan amount buttons re-ordered**: £30 (3 bands) is on top, £20 in the middle, £10 at the bottom. £30 is the typical pick; putting it first cuts the click for the common case.

### Name Highlight Open to All — Click-to-Apply (v1.0.58)
- **Every player can pick a name highlight** on their account page now, no admin grant required. The picker shows your username rendered in each of the 28 available styles (gold, silver, crown, rainbow, pint, dragon, …) plus a "None" option that strips the highlight.
- **Click the sample to apply it** — no separate confirm or radio. Each preview is itself a submit button, so clicking the gold version of your username sets the gold highlight; clicking "None" removes it. The currently-applied option keeps a gold border + inset glow so you can see the selection at a glance.
- **Admin lobby panel kept** for setting other users' highlights (handy for moderation or assigning unusual styles on someone's behalf).
- The legacy `donorEnabled` flag is retained on user records as a no-op historical marker; it no longer gates anything.

### News Feed: 4th Filter Pill — Streaks (v1.0.57)
- Added a `🔥 Streaks` toggle next to the existing `👑 Wins / 🏆 Achievements / 🏅 Trophies` pills, so all-time streak-record entries can be filtered like any other type. Behaviour mirrors the others: 0 active filters → show everything (default); 1+ active → show only entries whose type is in the active set. Dropped the previous "streak records always pass through" special case now that they have their own filter; unknown types still pass through so a future news type can never accidentally vanish.

### News Feed: Drop Persistence + Drop Inner Scroll + Fix Toggle Sibling (v1.0.56)
- **Three real bugs** behind "the news section is empty" reports on v1.0.53–v1.0.55:
  - **Filter persistence in `localStorage`** could leave a stale value (`["win"]`, `["achievement"]`, etc.) from an earlier click hiding every entry on the next page load — even after the v1.0.54 sanitiser, a *valid* persisted filter would still hide non-matching items. Fix: filters no longer persist across page loads. Every lobby visit starts with an empty filter set, so the panel always shows everything by default. The page also actively `localStorage.removeItem('newsFilters')` on load to clear leftover keys from the old version.
  - **Nested scroll containers**. The outer `.lobby-news` aside already has `max-height: calc(100vh - 24px); overflow-y: auto`. v1.0.53 added `max-height: 520px; overflow-y: auto` on the inner `.news-list` too — two stacked scroll containers, which some browsers clipped instead of nesting. Removed the inner constraint; the parent panel handles scrolling on its own.
  - **`toggleLobbySection` collapse target**. The function toggles `header.nextElementSibling`. v1.0.53 inserted a new `.news-filters` div between the `<h3>` header and the `#news-list`, so clicking the news header was hiding the filters instead of the list (or, after a second click, leaving things in a half-collapsed state). Wrapped the filters and the list in a single `#news-section-body` div so the header's nextElementSibling is once again the unit that should collapse.

### Fifteen New Donor Highlights: Ghost, Diamond, Galaxy, Leaf, Ocean, Crystal, Gear, Anchor, Train, Coal, Medal, Disco, Rose, Butterfly, Moon (v1.0.55)
- Total donor styles: 13 → **28**. Wired everywhere the existing styles already appear (lobby admin picker, account self-pick, lobby preview chips, news, Hall of Fame, game cards, rankings, ELO leaderboard, feedback, top-bar nav-user, in-game floating VP panel SVG, in-game player bar, log lines).
  - **👻 ghost** — pale blue-white text with a 3.4s flicker animation
  - **💎 diamond** — icy cyan/white gradient text (same trick as rainbow, different palette)
  - **🌌 galaxy** — purple → indigo → pink gradient text
  - **🍃 leaf** — lime-green glow (lighter than turtle)
  - **🌊 ocean** — deep-blue → cyan gradient text
  - **🔮 crystal** — magenta neon glow
  - **⚙️ gear** — bronze steampunk glow (Brass theme!)
  - **⚓ anchor** — navy-blue nautical glow (canal era)
  - **🚂 train** — steel-grey glow (rail era)
  - **🪨 coal** — ember red with a 2.0s pulse animation
  - **🏅 medal** — bronze-amber glow
  - **🪩 disco** — rainbow gradient with a 4.0s `background-position` shift so the colours travel across the name
  - **🌹 rose** — rose-pink glow
  - **🦋 butterfly** — indigo glow
  - **🌙 moon** — silvery glow with a faint blue secondary shadow
- Add to `DONOR_STYLES` (validation array in `lib/db.js`), `style.css` (CSS classes — gradient ones mirror `.donor-rainbow`'s background-clip trick; emoji-prefixed ones go through `::before` so the donor-name container's padding/border-radius applies), and `board-renderer.js` (SVG fallback used in the in-game VP panel — gradient styles fall back to a single representative colour since each gradient would otherwise need its own `<linearGradient>` def).

### News Feed: Recover-From-Stale-Filter Fixes (v1.0.54)
- v1.0.53 introduced a persisted news filter set, but a stale or unrecognised value in `localStorage.newsFilters` could end up hiding everything (the panel looked empty even though the server was sending entries). Three guardrails:
  - **Sanitise on load** — `newsFilters` is intersected with the known `{win, achievement, trophy}` set on every page load, so stale or invalid keys can't filter anything out anymore.
  - **Permissive type matching** — items whose class doesn't match a known type now also pass through (instead of silently hiding) so a future news type can never accidentally vanish behind an active filter.
  - **Empty-state notice + Clear button** — when active filters hide every visible item, a "No entries match the selected filters" message is appended, and a small `×` Clear button next to the filter pills resets the filter set in one click.

### News Feed: Bigger Scrollable History + Type Filters + Donor Highlights in News & HoF (v1.0.53)
- **Larger history**: news panel now shows up to 150 entries (was 30) inside a fixed-height scrollable list (max 520px, custom thin scrollbar). Server-side ring buffer raised from 200 → 500 entries (`NEWS_MAX` in `lib/db.js`) so historical depth actually accumulates instead of getting trimmed. `/api/news?limit=…` cap raised from 100 → 300.
- **Type filters**: three pill toggles above the news list — 👑 **Wins**, 🏆 **Achievements**, 🏅 **Trophies** — multi-select. 0 active = show everything (default). 1+ active = show only those types. Streak-record entries always pass through (rare and special). Filter state persists in `localStorage` so it survives refreshes; the 30-second auto-poll re-applies the filter after every fetch.
- **Donor highlights now render in news subject names + Hall of Fame holder names** — both server-side EJS *and* the client-side JS pollers (`renderNewsItem`, `renderHallOfFame`) now apply `donor-name donor-<style>` classes, so a refresh no longer wipes the highlights. Lobby exposes a `DONOR_STYLES` JS global (mirrors the `game.ejs` pattern) and a `donorClassFor(username)` helper. HoF partial include now explicitly forwards the donor map instead of relying on `res.locals` inheritance.

### Donor Highlights Render in ELO / Clasificaciones / Game Cards / Feedback (v1.0.52)
- **Why donor highlights weren't showing in the lobby ELO leaderboard rows or the Clasificaciones (group rankings) rows**, despite working in the nav-user, preview chips, and player list: four sites in `views/lobby.ejs` build the entire `class="..."` attribute from inside a `<%= … %>` interpolation. EJS's `<%=` HTML-escapes its output, so the literal `"` characters in ` class="donor-name donor-rainbow"` became `&quot;`, and the browser saw `<span class=&quot;donor-name donor-rainbow&quot;>` — i.e. no usable `class` attribute, so `.donor-name` / `.donor-<style>` never matched. The CSS was correct, the data was correct, the markup was just being mangled at render-time.
- **Fix**: switched `<%=` → `<%-` (raw, unescaped) on the four offending sites: ELO leaderboard rows (line 579, user's complaint), group rankings rows (line 621, user's complaint), game cards in My Games (line 387), and recent feedback inside the admin panel (line 165). Safe because the interpolated value is `userDonorStyles[username]`, which is constrained at write-time to the closed `DONOR_STYLES` enum in `lib/db.js`. The other three working sites (nav-user line 18, preview chips line 140, player list line 209) interpolate inside an already-quoted attribute and never had this problem.

### SVG Silver/Gold Match HTML Donor Style (v1.0.51)
- The in-game VP panel SVG drew silver/gold donor names as **dark text on a solid silver/gold pill**, while every HTML surface (lobby preview chip, game cards, rankings, ELO leaderboard, nav-user, etc.) showed them as **coloured text on a faint matching tint with a thin border**. Two visually different versions of the same style. SVG now mirrors the HTML: silver = `#e8e8ee` text on `rgba(220,220,230,0.18)` tint, gold = `#ffd166` text on `rgba(212,168,67,0.18)` tint, both with the existing rounded-rect background that was already drawn — just lighter, transparent, and bordered to match.

### Six New Donor Styles: Turtle, Dragon, Rocket, Lightning, Fire, Wizard (v1.0.50)
- Six new themed glow highlights, each with its own neon-coloured text shadow and an emoji prefix on one side. Wired everywhere the existing seven styles already appear (lobby admin picker, account self-pick, lobby preview chips, game cards, rankings, ELO leaderboard, feedback, top-bar nav-user, in-game floating VP panel SVG, in-game player bar, log lines):
  - **🐢 turtle** — green glow (user request)
  - **🐉 dragon** — red/orange glow
  - **🚀 rocket** — cyan glow
  - **⚡ lightning** — bright yellow glow
  - **🔥 fire** — orange glow with a 1.6s pulse animation
  - **🧙 wizard** — purple glow
- Total donor styles: 7 → 13. Add to `DONOR_STYLES` (validation array), `style.css` (CSS classes), and `board-renderer.js` (SVG fallback used in the VP panel).

### Cache-Bust Static Assets (v1.0.49)
- **Why donor highlights weren't showing in game cards / rankings / ELO / etc. for many users**: every view links `/css/style.css` (and `game.ejs` links the JS files) without any version query string. With no `Cache-Control` set on `express.static`, browsers heuristically cache aggressively — many users were running with a `style.css` from a v1.0.x weeks ago, predating the `.donor-name` / `.donor-rainbow` rules, the £-number pill, and the v1.0.47 outline-only `.vp-potential`. The DOM had `class="donor-name donor-gold"` but the CSS for it didn't exist in the cached file, so it rendered as plain text.
- **Fix**: every `style.css` and the four `game.ejs` script tags now include `?v=<%= appVersion %>`. Each version bump invalidates the cache automatically. Going forward, after a deploy users see the latest CSS/JS on next page load instead of holding onto the stale copy until they manually hard-reload.

### Tiebreak Achievements + "King of Empatitos" Trophy (v1.0.48)
- **Two new achievements** for games where two or more players finish tied at the top VP. The single "tiebreak winner" is picked by the same chain ELO already uses (income → money → spent → turn order):
  - **Tiebreak Champion** — you matched a top-VP tie and came out on top of the tiebreaker.
  - **Bridesmaid** — you matched the winner on VP but lost the tiebreaker chain.
  Total achievements goes from 49 → 51. Hall of Fame's `'/45'` denominator was already stale; switched it to read `definitions.length` lazily so it stays in sync from now on.
- **Hall of Fame: "King of Empatitos" 🤝** (Battle group). Counts how many finished all-human games each player was in a tie-at-top situation — capped at +1 per game per player (naturally, since each player has a single VP). Holder is the player with the most such games; ties share the trophy.

### Spent Box £-Number + Potential VP Hex Fix (v1.0.47)
- The "£ number" toggle now also applies to the **SPENT** SVG panel: when on, each row renders `£N` in gold text instead of the silver/bronze disc stack, matching the player bar's money row.
- **`.vp-potential` hex** (the gold "+N" badge next to live VP) was visibly larger than the pink VP hex it sat beside (22×20 vs 16×15). Resized to match `.tile-vp-inline` (16×15, 8px font) and switched from a solid gold `clip-path` polygon to an outline-only inline-SVG background — the bonus now reads as a same-size gold-bordered hex sitting next to the pink VP hex, not a fat overpowering blob. Mobile rule scaled down accordingly.

### Workflow Note: Environment vs Repo Secrets (v1.0.46)
- Added a commented `environment:` line in `deploy-with-maintenance.yml` plus a clearer error message in the pre-flight step explaining the two GitHub Actions secret scopes (Repository vs Environment) and how to handle each. If your `RENDER_API_KEY` / `RENDER_SERVICE_ID` are stored under Settings → Environments → *(env)* → Environment secrets, uncomment `# environment: production` and set the name; otherwise move them to the repo-wide "Repository secrets" tab.

### Drop Fake Render Maintenance Endpoint (v1.0.45)
- The previous `deploy-with-maintenance.yml` workflow tried to POST to `https://api.render.com/v1/services/{id}/maintenance-mode/enable` — that endpoint **doesn't exist** in Render's public API (404). My mistake; Render's Maintenance Mode is a dashboard-only feature, not exposed via REST.
- **Fixed**: workflow renamed in spirit (file kept) and stripped to just two real Render API calls — `POST /v1/services/{id}/deploys` and polling `GET /v1/services/{id}/deploys/{id}` until `status: live`. That part works and is useful when Auto-Deploy is off. A pre-flight step now hard-fails with a clear error if `RENDER_API_KEY` or `RENDER_SERVICE_ID` aren't set, so silent failures stop happening.
- **For the maintenance page during deploys**: the only options remain (a) Render dashboard → Settings → Maintenance Mode toggled manually, (b) Cloudflare custom 5xx page, or (c) accept the ~20s gap.

### Rainbow CSS Fix + £-Number Money Toggle (v1.0.44)
- **Rainbow donor name was rendering plain white** wherever the parent had an inline `style="color:#fff"` (e.g. lobby game cards). Cause: `background: linear-gradient(...) text;` is invalid CSS — the `text` keyword isn't a valid value for the `background` shorthand, so the entire declaration was dropped, leaving only the `color: transparent !important` from later rules and no gradient image to clip. Fixed by switching to `background-image: linear-gradient(...)` plus the existing `background-clip: text` / `-webkit-text-fill-color: transparent`.
- **New "£ number" checkbox** in the board's View row. When ticked, the player bar's money row shows `£123` in a gold pill instead of the stack of silver/bronze coin discs. State persists per browser via `localStorage['brass_moneyAsNumber']` and the checkbox restores from that on page load (alongside the other view filters).

### Render API Automation + Donor Highlights Everywhere (v1.0.43)
- **`.github/workflows/deploy-with-maintenance.yml`**: new workflow that, on every push to `main`, calls Render's API to **enable maintenance mode** before triggering a deploy, polls until the deploy is `live`, then **disables maintenance mode** (always — even if the deploy fails). Setup: add `RENDER_API_KEY` and `RENDER_SERVICE_ID` repo secrets, then turn OFF "Auto-Deploy" in Render's dashboard so this workflow drives deploys. Maintenance is served from Render's edge during the disk-swap gap that persistent-disk services hit, so users see the spinner+changelog instead of a 502.
- **Mirror workflow extended** to also copy `public/maintenance.html` → `xaiworld/mainpage/brass-maintenance.html`. The maintenance page now stays in sync with whatever changes the brass repo makes (e.g. the next fix below).
- **Maintenance page picked the OLDEST changelog entry**: `pickLatestSection()` in `public/maintenance.html` looped through all lines keeping the LAST `### Title (vN.N.N)` match, but the changelog is now in newest-first order, so "last" was actually "oldest" (it showed v0.0.1–v0.0.7). Fixed to break on the FIRST match. Same fix applies to the mainpage copy via the mirror workflow.
- **Donor highlights now propagate to**:
  - Top-bar **`nav-user`** link (your username with your own donor style) — applied to all 12 navbars by exposing `myDonorStyle` via `res.locals` in `server.js`.
  - **Hall of Fame** holder names — `views/partials/hall-of-fame.ejs` looks up `donorStyles` and adds the donor class to the `.player-link`.
  - **Feedback** entries' username (`<strong>` wrapped with donor class).
  - **ELO Ratings** leaderboard rows — username cell wrapped.
  - **Rankings** group rows — username cell wrapped.
- **SVG VP panel: gold + silver styles get a real `<rect>` background** drawn behind the name (rounded, sized to the text), so the "card" look matches the HTML highlight. Other styles still use fill+emoji as before.
- The SVG VP panel rendered every donor style with a flat gold fill (`#ffd166`), so a `rainbow` highlighted name showed up yellow even though the player's seat color was different (e.g. violet) — the user couldn't tell why their name went yellow.
- **Fix**: a real `<linearGradient id="donor-rainbow-grad">` is now added once to the SVG `<defs>` (red → yellow → green → blue → purple, matching the HTML rainbow style), and rainbow names use `fill="url(#donor-rainbow-grad)"` so the gradient renders. Idempotent helper `_ensureRainbowGradient()` registers it.
- Other donor styles (`gold`, `silver`, `crown`, `pint`, `sparkle`, `glow`) keep their existing fill+emoji approach since SVG text can't easily host backgrounds, borders, or text-shadow.

### Mirror Workflow: Explicit PAT in Push URL (v1.0.41)
- The `mirror-changelog.yml` workflow's final `git push` now uses an explicit URL with the PAT (`https://x-access-token:${PAT}@github.com/xaiworld/mainpage.git`) instead of relying on `actions/checkout`'s `extraheader` config carrying through. If the push still 403s after this, the PAT itself lacks `Contents: Write` on `xaiworld/mainpage` — see deploy-notes below for the exact PAT settings.

### Donor Highlight Everywhere (v1.0.40)
- The donor highlight now propagates to **every surface** that renders a player's name, not just the lobby Players list:
  - **Lobby game-row player names** (under each Active/Waiting/Finished card) — wraps the username with `.donor-name .donor-<style>`.
  - **In-game player bar** (left side panel) — `_donorWrap()` helper in `public/js/game-ui.js` is now called from `updatePlayerBar` so the username sits inside the donor span.
  - **VP panel on the SVG board** — SVG `<text>` can't take HTML classes, so the renderer falls back to a "lite" highlight: gold fill + bold + emoji prefix for `crown`/`pint`/`sparkle`, silver fill for `silver`. The seat color shows on the bar to the side, so the gold name still reads as ownership.
  - **Game log entries** — only the nick gets the donor class. The rest of the line keeps the player's seat color (the donor class's `!important` color override applies inside the wrapped span only). Regex-escapes the username so special characters in nicks are safe.
- **Server-side `donorStyles` map** is now exposed via `res.locals.donorStyles` (a `{username → style}` map of every user with an active highlight), so any view can use it without re-querying. The game page injects it as a `DONOR_STYLES` global JS variable for the client renderers.

### Donor Self-Service + Live Preview (v1.0.39)
- **Granted users can now change their own highlight in the account page.** Once an admin assigns a donor style to a user, `user.donorEnabled = true` is set; the user thereafter sees a "Donor highlight 🍺" section on their `/account` page with one radio per style (name shown styled with their actual username) plus a "None" option that removes the highlight without revoking donor status. Backed by `db.setOwnDonorStyle(userId, style)` (refuses if `donorEnabled !== true`) and `POST /account/donor-style`.
- **Admin clearing the style preserves donor status** — `donorEnabled` stays `true` so the user can re-enable any style on their own.
- **Admin preview chips now show the picked username** in each style. The lobby's "Admin: Donor Highlights" preview row used to show the style names ("gold", "silver", …) — when the admin picks a username from the dropdown, all chips swap to the username so the admin sees exactly how that name will look in each style. Includes an **8th "normal" chip** (no highlight) for the side-by-side comparison.

### Donate Button + Donor Highlights (v1.0.38)
- **Donate button** changed from "☕ Buy me a coffee" → "**🍺 Buy me a pint**". The "Loving the game? Tip via Revolut." hint line is removed.
- **Admin-controlled donor highlights**: a new "Admin: Donor Highlights" panel in the lobby sidebar (xai-only) lets the admin pick a username + a style and POST `/admin/set-donor-style` to apply it. Backed by `db.setUserDonorStyle(userId, style)` and a new `user.donorStyle` field. Empty/unknown style clears the highlight.
- **Seven highlight styles** (`donor-gold`, `donor-silver`, `donor-crown`, `donor-rainbow`, `donor-glow`, `donor-pint`, `donor-sparkle`), each with a different look — gold/silver pill, gold-glow text, rainbow text-gradient, leading 👑 / 🍺 / ✨ emoji, etc. The full list lives in `public/css/style.css` keyed by class names; the admin form builds the picker from `db.DONOR_STYLES` so adding a new entry there auto-extends the dropdown.
- **Live preview** under the picker shows each style applied to its own name, so the admin can eyeball the look before picking.
- **Players list** in the lobby renders the donor's name with `.donor-name .donor-<style>` so the highlight shows up next to their ELO/streak/turns badges. Other surfaces (game rows, news feed, profile) still use the plain name for now — easy to extend by adding the same class wherever `<a class="player-link">` appears.

### Mandatory Turn Confirmation + Wild-Build Reset Fix (v1.0.37)
- **Bug**: a player who finished a turn with a Wild Build couldn't reset — by the time they clicked Reset the server had already advanced to the next player. Wild Build atomically consumes 2 actions, so the client's `holdForConfirm` check (`gameState.actionsRemaining === 1`) didn't fire (Wild Build needs 2 actions, so `actionsRemaining` was 2 going in), the server didn't set `pendingConfirm`, and the turn auto-advanced.
- **Fix**: new `_actionEndsTurn(action)` helper that returns true for any submission that will exhaust the player's last action — the normal `actionsRemaining === 1` case AND `buildIndustry` + `wildBuild` flag at `actionsRemaining >= 2`. All three submit paths (sellCotton, submitActionDirect/wildBuild, submitAction) now use it.
- **Confirmation is now mandatory** — `confirmTurnOnEnd` is hard-coded to `true` (no per-user opt-out, no toggle in the action panel). The turn never advances to the next player until `/api/games/:id/confirm-turn` is hit, so Reset always works on the just-completed turn.
- **Round-end confirmation included** — when a player's last action also ends the round, `pendingConfirm` is still set; the engine's `endRound` runs only inside `confirm-turn`, so the round (and any era transition) doesn't apply until the player clicks Confirm.
- **Loan-warning overlay timing** is now naturally correct: while the previous player has `pendingConfirm`, the live `currentPlayerIndex` hasn't advanced, so `updateActionPanel` early-returns for the next viewer and `checkLoanWarning` is never reached. The overlay only fires once the previous player has explicitly committed.

### Auto-Pick Specialized Slot for Port (v1.0.36)
- Lancaster and Preston each have **two slots that can host a port**: a single-purpose `['port']` slot and a multi-purpose `['cottonMill','port']` slot. Previously, when a player chose "Build Port" at one of these locations and BOTH slots were empty, the UI asked which slot to use — which is fine, but most players want to burn the dedicated port slot first and keep the multi-purpose slot for a future cotton mill.
- **Fix**: when all candidate slots at the chosen location are empty, the slot picker now auto-selects the slot with the **smallest `allowed.length`** (most restrictive) by index. Ties or any overbuild candidates still show the explicit picker. Same logic applied to the location-click path so the selection happens in one click rather than via a re-render bounce.
- General-purpose: this also applies to any future location where a slot is uniquely most-restrictive for the chosen industry.

### Potential VP Pill Becomes a Hexagon (v1.0.35)
- The `+N` "potential VP" badge in the left-side player panel now uses the **same pointy-top hexagon shape** (`clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%)`) as the existing `.tile-vp-hex`, so a player glancing at the player bar instantly reads it as a VP-related value rather than a generic pill.
- Filled in **gold** (`#d4a843`) with dark text to stay visually distinct from the pink already-scored VP hex right next to it.
- Sized at 22×20 px on desktop / 26×23 px on mobile, fitting `+0` through `+99` comfortably.

### Link Tiles Derived From Board (v1.0.34)
- **`linksRemaining` is no longer a stored field.** It's derived on demand from the live board state via `countOwnedLinks(state, seat)` in `lib/game-engine.js`, both at the action-time eligibility check (`actionBuildLink`) and in the player-bar render. Canals lose their `owner` at the canal→rail transition, so the same count function naturally reads "0 used this era" for rail era — no per-era reset needed and no risk of a stored counter drifting from reality.
- **Player-bar badge format** is now `🔗 X/14` (was just `🔗 X`), with tooltip "*Link tiles remaining this era (used N / 14 — returned at canal→rail transition)*". Makes the per-era budget explicit.
- **Engine** still sets `player.allLinksUsedThisGame = true` when post-build `countOwnedLinks(...) >= 14`, so the **Track Layer** achievement still fires for any player who exhausts the 14-tile set in either era.
- **Migration removed** — no backfill needed since the value is derived. Any stale `linksRemaining` field on legacy state is just ignored.
- **Why**: the prior stored-field approach showed `🔗 11` to a user who had visually counted 14 links on the board. A stored counter can drift; deriving from the board removes that whole class of bug. (The user's "14 on board" likely included canal-era builds that now render gray after the era transition wiped their owner — those don't count toward the rail-era 14 budget.)

### Link Tiles Reset Per Era (v1.0.33)
- **Per-era link budget** corrected: each player starts the **rail era** with a fresh **14 link tiles** (canal-era tokens are returned at the era transition, matching the physical-game rule). `transitionToRailEra` in `lib/game-setup.js` now resets `player.linksRemaining = 14` for every player alongside the `spentThisRound = 0` clear.
- **Track Layer achievement** now uses a sticky `player.allLinksUsedThisGame` flag set the moment `linksRemaining` hits 0 in either era. The flag survives the canal→rail reset, so a player who exhausts their canal budget still earns the trophy at game end.
- **Migration v2**: simplified to "links currently on the board → linksRemaining = 14 − that count", which naturally produces the right value for rail-era games (where only rail links exist on the board after the era transition). Replaces the earlier flag (`linksRemainingBackfillDone`) which double-counted canal builds for rail-era games.
- **Player-bar tooltip** updated to: "Link tiles remaining this era (14 per era — returned at canal→rail transition)".

### Link Tiles Tracked (14 / Player) + Achievements (v1.0.32)
- **Players now start with `linksRemaining: 14`** (Brass: Lancashire physical-game link-tile budget). Set in `lib/game-setup.js`. Decremented inside `actionBuildLink`:
  - canal build → −1
  - single rail → −1
  - 2-rail action (`secondLinkId`) → −2
- **Engine refuses `buildLink` when the player doesn't have enough tiles** with `Out of link tiles (X left, need Y)`. Existing money/connection checks still apply first.
- **Player-bar badge** in the left side panel now shows `🔗 N` next to income / cards (tooltip: "Link tiles remaining (start with 14)").
- **DB migration** in `lib/db.js` `migrate()` backfills `linksRemaining` for in-progress games: counts links currently owned on the board (correct for canal-era games) and adds a log-derived count of canal builds for rail-era games (whose canals were wiped at the era transition). Idempotent behind `data.meta.linksRemainingBackfillDone`.
- **Two new achievements**:
  - **Track Layer** — use all 14 link tiles in one game (`linksRemaining === 0`).
  - **Iron Horse** — execute at least one 2-rail (£15 + 2 coal) action in a game; uses the action log via `ctx.actions`.

### Loan Capped by Income Floor (v1.0.31)
- **Engine** (`actionTakeLoan` in `lib/game-engine.js`): rejects loan amounts that would push the player's income level below **-10** (the bottom of the track). Each £10 borrowed drops 1 income level, so the maximum allowable amount at current level `L` is `(L − (-10)) × £10`. Concretely:
  - Level **−9** → max **£10** (1 band)
  - Level **−8** → max **£20** (1 or 2 bands)
  - Level **−7 or higher** → max **£30** (any of 1/2/3 bands)
  Error: `"Loan would drop income below the floor (-10). At -9/turn the maximum loan is £10."`
- **Client UI** (`renderActionFlow` `case 'takeLoan'` in `public/js/game-ui.js`): the £20 and £30 buttons are now `disabled` when they'd violate the floor, with a hover-tooltip "Would drop income below −10". A small grey hint above the buttons explains the cap when relevant: *"At -9/turn, max loan is £10 (income floor is −10)."*

### Early-Listen Boot + Wiki Cards Page (v1.0.30)
- **Real fix for the persistent 502s on Render**. Previously `server.js` ran `preBootCompact()` (synchronous `spawnSync`) and `db.load()` *before* calling `app.listen()`, so the new Render container had **no listener at all** for 10–60 s during boot. With nothing on the port, Render's healthCheckPath probe couldn't even connect — `/health` was effectively absent — so the load balancer routed traffic to a half-shut-down old container or returned 502.
- **Refactor**: bind to `PORT` immediately with a stub app that returns **503 on `/health`** while `isReady === false` and serves `public/maintenance.html` (with status 503) for any other request. Render now sees a live listener returning 503 and keeps the OLD container serving real traffic. `init()` then runs `preBootCompactAsync()` (async `spawn`, not `spawnSync`) and `db.load()` in the background, registers all middleware/routes, and flips `isReady = true` — at which point `/health` returns 200, Render swaps over, and live traffic transitions to the new container with **zero 502s**.
- **`init()` failures** flip a `bootError` flag so `/health` reports `500 boot error: …` instead of looping in 503 forever.
- **New `/wiki/cards` page** with full deck breakdowns:
  - 4P / 3P deck (66 cards) — 41 location cards (Liverpool ×4, Manchester ×4, Lancaster ×3, Preston ×3, … Bury ×1, Ellesmere Port ×1, Fleetwood ×1) + 25 industry cards (Cotton ×8, Port ×6, Coal ×5, Iron ×3, Shipyard ×3).
  - 2P deck (40 cards, custom): which locations/industries are removed and how many copies remain.
  - "Canal removed" / "Rail removed" per player count + how the draw pile size drives era length.
- **"Cards" added to all wiki nav strips** (rules, actions, industries, strategy, index, cards), and routed via `wiki-routes.js`.

### Done (1 rail) Strips 2-Rail Leftovers (v1.0.29)
- **Bug**: clicking "Done (1 rail)" after "Add Second Rail" had been pressed could still produce `Not enough money (2 tracks costs £15)` from the server. The Done panel renders only when `!secondLinkId`, but the highlight callback on the board (armed by `startSecondLink`) remains active even after the panel changes — a stray click on a highlighted link sets `secondLinkId` and on the next submit the action carried `secondLinkId` along with `singleConfirmed`, so the engine ran the 2-rail check.
- **Fix**: `confirmSingleRail()` now explicitly deletes `secondLinkId`, `coalEnd2`, `resourcePending`, `resourceIdx`, and `resourcePlan` from `actionParams` and calls `BoardRenderer.clearHighlights()` before submitting, so the resulting action is unambiguously a 1-rail commit regardless of any prior 2-rail attempts in this turn.

### Mobile Top-Stack Heights Are Now Dynamic (v1.0.28)
- The hardcoded mobile offsets (`game-name top: 34`, `turn-indicator top: 60`, `container padding-top: 92`) assumed a 34 px navbar, but on devices with larger system font, button line-heights, or zoom, the navbar runs taller — the game-name bar then started inside the navbar and the turn-indicator's era/round/money content disappeared behind the gold game-name strip.
- **Fix**: `updateMobileTopStack()` measures `navbar.offsetHeight` and `nav-game-name.offsetHeight` at runtime and writes the correct `top:` to the game-name bar, the turn-indicator, and `padding-top` on `.game-container`. Re-runs on `resize`, `orientationchange`, and at 100 ms / 500 ms after init to catch late font/icon-load reflows.
- **CSS**: removed the hardcoded `top: 34px` / `top: 60px` from `body.is-mobile .nav-game-name` / `.turn-indicator-bar` — the JS sets them now. All other styles (height, line-height, background, border, ellipsis truncation) are kept.

### Mobile Game-Name / Turn-Indicator Spacing (v1.0.27)
- The mobile game-name bar at `top: 34px` was overlapping the turn-indicator bar (era / round / money color bar) at `top: 58px` because the bar's natural height varied with the user's font-size / line-height settings.
- Fixed by giving `.nav-game-name` an explicit `height: 24px; line-height: 18px; box-sizing: border-box` so it always fits in the 34–58 px slot, and bumping the turn-indicator to `top: 60px` for a 2 px breathing gap. `.game-container` `padding-top` adjusted from 90 → 92 px to match.

### Highlight Active Surrounds Player Color (v1.0.26)
- Previously the **Highlight active** view-filter overrode the tile's stroke to gold (`stroke: var(--gold) !important; stroke-width: 2.5 !important`), which painted *over* the player-color border and lost the ownership cue.
- Now the player-color stroke is preserved and a **2 px hard gold outline + soft halo** is added *around* it instead. Implemented as four orthogonal `drop-shadow(±2px 0/±2px 0 0 gold)` filters (since CSS `drop-shadow` has no spread parameter) plus one soft `drop-shadow(0 0 4px gold)` for the glow. The player border still reads as the owner's color.

### Mobile Game-Name Bar (v1.0.25)
- **`.nav-game-name` is now a fixed bar directly under the navbar on mobile** (`top: 34px; left: 0; right: 0`) instead of an absolutely-centered overlay inside the navbar. On phone widths the centered overlay collided with the nav-links, masking Lobby / Wiki / Changelog / Stats / username taps. The new bar gets its own row, ellipsis-truncated for very long names.
- **Turn indicator bumped to `top: 58px`** (was `34px`) and `.game-container` padding-top raised to `90px` (was `66px`) to make room for the new bar without covering board content.
- **Desktop unchanged** — the absolutely-centered overlay still sits between the brand and nav-links since there's plenty of horizontal room there.

### Non-Buildable Link VP in Live Display (v1.0.24)
- **Bug**: a link touching a non-buildable location (Northwich, Blackpool, Southport, Scotland, Yorkshire, The Midlands) — e.g. a Northwich↔Midlands rail — scored **0 VP** in the in-game live display, even though server scoring correctly counts those endpoints as **2 VP icons each**. So a Northwich↔Midlands link should show **4 VP**, not 0.
- **Cause**: client-side `calculateVPBreakdown` and `calculatePotentialVP` in `public/js/game-ui.js` looked up endpoints in `s.board.locations[locId]` only — non-buildable locations live in `BOARD.nonBuildable`, so the lookup returned `undefined`, the loop fell through with `vp = 0`, and the link contributed nothing to the player-bar VP hex or the VP breakdown popup.
- **Fix**: both functions now check `BOARD.nonBuildable[locId]` first; if it matches, add 2 VP icons for that endpoint (matching the server's `countLocationVP` in `lib/scoring.js`). Same rule applied to the potential-VP best-case calc.
- **Server-side scoring was always correct** — the actual canal/rail era scoring at era-end and game-end used the right rule. This fix only affects the live VP shown during play.

### Board View Filters (v1.0.23)
- **New "View:" controls row** in the board control bar (above the SVG, below the existing Minimal/No names/No icons row), visible on every game board both desktop and mobile. Doesn't shift the board or interfere with click targets.
- **Five filters**, all opt-in, all persisted per browser via `localStorage`:
  - **Only mine** — hides every tile the viewer doesn't own. Tiles are tagged with `tile-mine` / `tile-other` at render time based on `slot.owner === viewer's seat`, and `body.board-only-mine .tile-other { display: none }` does the rest.
  - **Hide flipped** — `body.board-hide-flipped .tile-flipped { display: none }`.
  - **No blink** — disables the `tile-glow` pulse on unflipped tiles via `animation: none !important; opacity: 1 !important;` (needed to beat the inline `style.animation`/`style.opacity` set in `board-renderer.js`).
  - **Highlight active** — gold stroke + drop-shadow halo on every still-active tile, so unfinished work pops.
  - **Dim flipped** — 4-stop slider (0=Off, 1=Light 0.85, 2=Medium 0.7 default, 3=Strong 0.35). Replaces the fixed 0.7 inline opacity.
- **`BoardRenderer.toggleVisFlag(name, on)` / `setFlippedDim(level)` / `restoreVisFlags()`** in `public/js/board-renderer.js`. `restoreVisFlags()` runs in `init()` before the first render, so saved preferences apply on page load with no flicker.
- **Mobile**: the new row gets hoisted into `#mobile-board-controls` alongside the existing row 2, so the filters are reachable from the Info tab on phones too.
- "☕ Buy me a coffee" button now points to `https://revolut.me/wejejeei`.

### "Buy me a coffee" Donate Button (v1.0.21)
- **New gold gradient pill** under the Brass cover image in the lobby sidebar reading **"☕ Buy me a coffee"** with a small "Loving the game? Tip via Revolut." hint underneath. Links to `https://revolut.me/REVOLUT_HANDLE` — replace `REVOLUT_HANDLE` in `views/lobby.ejs` with your actual Revolut Tag (Settings → Profile → Revolut Tag in the app).
- Sits naturally between the cover image and the existing sidebar panels; on mobile it stacks with everything else when the sidebar collapses.

### Reset-Turn Resilience to Stale turnStart (v1.0.20)
- **Bug**: a player would submit their final action with `holdForConfirm`, see the Confirm/Reset overlay, click **Reset**, and get back `Cannot reset — target state is not your turn` even though it clearly *was* their turn (the overlay was showing because of their own action).
- **Cause**: the reset endpoint used `state.turnStart` unconditionally as the rollback target. If that snapshot was somehow stale (its `currentPlayerIndex` pointing at a different player — for example, never refreshed on the player's first action because `actionsRemaining` wasn't at the era's max), the seat-match check failed and the entire reset was rejected. The historical version (`hist`) was never tried as a fallback.
- **Fix in `routes/game-routes.js`**:
  - Authoritative ownership check now runs against the LIVE state first: if the live `turnOrder[currentPlayerIndex]` doesn't match the user's seat, we return a clearer "It's no longer your turn — the game advanced" message instead of the confusing turnStart-mismatch one.
  - Rollback candidates are now collected in priority order (`turnStart` → history at `targetVersion`) and **each candidate is validated** that its current-player seat matches the user. A stale `turnStart` silently falls through to the history candidate instead of nuking the whole reset.
  - Added per-candidate skip logging so future stale-snapshot incidents leave a breadcrumb in the server logs.

### Coal/Iron Build Achievements + HoF Flip Regex (v1.0.19)
- **Build-all achievements (`Iron Magnate`, `Coal Baron`, `Cotton Empire`, `Port Authority`, `Shipwright`, `Diversified`)** were counting tiles by iterating the FINAL game state. That undercounted in two scenarios: L1 tiles removed at the canal→rail era transition, and tiles overbuilt with a higher level later in the game. A player who built all four iron-works levels (L1+L2+L3+L4) only saw 3 tiles in the final state because L1 was gone — so Iron Magnate would never trigger. Same for the other "build all of X" achievements.
- **Fix**: new `userBuildsCount(ctx, type)` helper in `lib/achievements.js` counts every `buildIndustry` action of the given industry type, scoped to the user's `user_id` in `ctx.actions`. `recordGameResult` in `routes/game-routes.js` now passes `actions` (filtered to this game) into `ctx`. Falls back to the old state-based counter only when `ctx.actions` isn't available.
- **Hall of Fame "Coal Mines / Iron Works Flipped" regex fix**: the auto-flip log lines are wrapped by `logMsg` and indented with `  ↳ ` (e.g. `[C3]   ↳ Coal Mine L2 at Bolton flipped! alice inc ...`), so my v1.0.17 anchored regex `^(?:\[\w\d+\] )?Coal Mine L\d+...` missed every single one. Switched to a sub-string match (`Coal Mine L\d+ at .+? flipped! (\S+) /`) so the prefix doesn't matter — coal/iron flip counts are now actually counted.
- **HoF cache bumped** (`hofFlipsRecomputeV` 1 → 2) so the cached holders recompute once on next boot rather than waiting out the 60 s TTL.

### Finished-Game Notification Banner (v1.0.18)
- **New purple lobby banner** at the very top: "🏆 N game(s) finished:" listing every all-finished game the viewer participated in but hasn't acknowledged yet. Each row shows the game name (linked), a per-row summary ("🎉 You won (89 VP)" if you won, or "Won by alice 89 — you finished with 72 VP" otherwise), and the time-ago. Modeled on the existing "▸ Your turn in N games" banner.
- **Per-game ✕ dismiss** and a "Dismiss all" button. Visiting the game via its link also marks it seen automatically (in the `/games/:id` route handler).
- **Storage**: `user.seenFinishedGameIds` (sorted integer array on the user record) — added via a new `db.markFinishedGamesSeen(userId, ids)` helper. Idempotent.
- **API**: `POST /api/finished/dismiss` accepts `{ gameIds: [...] }` to dismiss specific games, or `{ all: true }` to dismiss every finished game the viewer is in. Used by the per-row ✕ and the "Dismiss all" button.
- **Banner appears alongside, not replaced by, the existing banners** — the My Turn / Up-To-Date / Join-Create banner still shows below it as appropriate, since "you have unseen finished games" and "it's your turn somewhere else" are independent states.

### Lobby Live VP + Hall of Fame Lifetime Flips (v1.0.17)
- **Lobby game-row VPs for active games** now use a live projection (canal-era already-scored VP + flipped tiles in current era + current-era links + money / 10) instead of the raw `p.vp` field. Previously a player with many flipped tiles in canal era still showed **0 VP** in the lobby (since `p.vp` is 0 until canal-era scoring runs at era end), and during rail era they only showed their canal-only score. Now the lobby reflects what each player would score if the game ended right now — same logic the in-game VP hex uses.
- **`lib/scoring.js`** gains a pure `liveProjection(state)` helper, used by `routes/lobby-routes.js` for active games. Finished games still read from the gameResults table (final VP). Mirrors `scoreCanalEra`/`scoreRailEra` exactly, including the rule that non-buildable locations (waypoints + external ports) count as 2 VP icons for link scoring.
- **Hall of Fame flip counts** are now computed from the action log + state log, not just `iterFlippedSlots(finalState)`. Previously, L1 industries that were flipped during canal era then **removed at the canal→rail era transition** were missed from "Cotton Mills Flipped", "Coal Mines Flipped", "Iron Works Flipped", "Ports Flipped", and "Shipyards Flipped" trophies. Same for tiles flipped then overbuilt. The new counter:
  - **Cotton mills**: each `sellCotton` sale = 1 mill flip, attributed to the seller (mill owner).
  - **Coal mines / iron works**: parsed from the engine's explicit "Coal Mine L<N> at <loc> flipped! <user>" / "Iron Works L<N> at <loc> flipped! <user>" log lines.
  - **Ports**: each via-port `sellCotton` sale = 1 port flip, attributed to the port owner (resolved by walking buildIndustry actions chronologically, with a fallback to the final-state slot owner if the port survived).
  - **Shipyards**: each `buildIndustry` action with `industryType === 'shipyard'` = 1 flip (shipyards flip immediately on build).
- **Hall of Fame cache invalidation**: a one-shot `data.meta.hofFlipsRecomputeV` flag in `migrate()` sets `hallOfFameStale = true` once after this deploy so the cached holders/values from the old algorithm get recomputed immediately rather than waiting out the 60 s TTL.

### Loan-Lock Warning Overlay (v1.0.16)
- **At the start of each player's turn** in rail-era rounds **5 (2P/3P) / 3 (4P)** — the second-to-last round in which loans can still be taken — a gold-bordered overlay appears: "*Heads up — second-to-last loan round*". Player clicks "I understand" to dismiss; doesn't appear again that round.
- **Rail-era round 6 (2P/3P) / 4 (4P)** shows a more emphatic red-bordered, pulsing overlay titled "**⚠️ LAST CHANCE TO TAKE A LOAN**", explaining that this is the final round in which a loan can still be taken — the engine blocks loans once the draw pile runs out shortly after.
- **Per-game-round-per-user dismissal** stored in `localStorage` (`brass-loan-warn:<gameId>:<userId>:<round>`), so the overlay never reappears once acknowledged. The check runs inside `updateActionPanel` only when it's the user's turn, so opponents' turns can't trigger it.
- **No engine change** — this is purely a UX reminder. The actual rule (no loans once `state.drawPile` is empty in rail era) was already enforced in `actionTakeLoan` and is unchanged.

### Rail-Era Round 1 Income Fix (v1.0.15)
- **Bug**: `transitionToRailEra()` in `lib/game-setup.js` set `era='rail'`, `round=1`, then went straight to actions without running Phase 1 (income collection). `endRound()` collects income at the top of every new round, but the canal→rail transition bypassed it entirely — so every player skipped a full round of income at the start of rail era. Existed in every version since the engine was written.
- **Fix**: added the same Phase 1 income block to `transitionToRailEra` (uses `incomeTrack[player.income]` and logs `[R1] Phase 1: <name> collects £N income …`).
- **Backfill migration in `lib/db.js`**: scans every game state where `era === 'rail'` && `round === 1` && there's no `[R1] Phase 1` line in the log, credits each player with the income they should've received, and appends a backfill log entry. Idempotent behind `data.meta.railR1IncomeBackfillDone`.

### Submenu Stays Anchored to the Card (v1.0.14)
- **Bug**: Click a card → action options appear next to it. Click "Develop" → the first dialog cascades from the card. But the next dialog (e.g., the Done/Cancel screen) jumped to the upper-right of the viewport instead of staying near the floating cards.
- **Cause**: The submenu re-anchored on every render. The card popup is what positioned it the first time, but mid-flow the popup gets hidden and the fallback anchor — the right-sidebar action panel — took over, yanking the submenu across the screen.
- **Fix**: Once the submenu is positioned for an action flow, subsequent renders keep its existing left/top instead of re-anchoring. Only viewport edge clamps apply. When the action flow ends (cancel, submit, or close), the inline position resets so the next flow re-anchors fresh against whichever card was clicked.

### Reset-Turn Reliability Fix (v1.0.13)
- **Bug**: building a coal mine in rail era (with the auto-sell to coal market) and then resetting the turn could leave the live state unchanged — the overlay reported success, but the action was still on the board and actions-remaining stayed decremented.
- **Root cause**: the rollback path was preferring the historical version snapshot (which can drift when state-history pruning runs) over the deterministic embedded `state.turnStart` snapshot. The two paths usually agree, but in some scenarios the historical entry resolved to a state that, once written back, looked indistinguishable from the live state — so nothing actually rolled back.
- **Fix**: the reset endpoint now uses `state.turnStart` as the primary source (it is always the start of the CURRENT player's turn by construction), with the historical version as a fallback for backwards-compat with games created before that snapshot field existed. Plus a defensive engine-side check: at the start of each action submission, if the player's turn just started but `state.turnStart` is missing or stale (was taken at a different player's turn), the snapshot is refreshed before the action runs.
- **Server logging** added to the reset endpoint so future reset issues report which source was used (turnStart vs history) and what version the rollback came from.

### Wider i18n Coverage — Account, Profile, Achievements (v1.0.12)
- **Account and Profile pages** now translate the section labels in all 9 languages: Game History, ELO Ratings, Achievements, Last Login, Login Count, "X games" (per ELO category), "X/Y earned" badge, the "no completed games" placeholder, the streak pill ("🔥 N day(s)"), and the day/days unit.
- **All 45 achievement names** are translated in all 9 languages (English + Español, Galego, Català, Valencià, Asturianu, Euskara, Français, Deutsch, Italiano). The on-card name now reflects the viewer's chosen language. Achievement descriptions remain English in this revision and will be translated in a follow-up.
- **Lobby**: the "You are up-to-date with the community, Mr. Wallace would be proud." banner, the "no active games" fallback, the System Data labels (Total games, Active games, Waiting, Finished, Players), the My Games heading, the Streak / Today sort buttons, the Rankings heading + descriptive paragraph, the ELO descriptive paragraph, and the no-games-yet placeholder all run through translations.
- **Game credits** in the navbar ("Game by Martin Wallace · Art by Peter Dennis · Published by Eagle-Gryphon Games") translates across all pages that show it (lobby, profile, account, login, game-incompatible).
- **Deferred to a follow-up**: achievement descriptions (45 strings × 9 languages, longer text), Hall of Fame trophy labels (43 × 9), wiki rules content (multi-page), and changelog content itself.

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

*Built with love iteratively through 490 versions of user-driven development — from a blank repository to **v1.0.133**: a full multiplayer Brass: Lancashire with neural-network AI, mobile UI, push notifications, ELO, achievements, streak records, daily turns counter, live news feed with type filters and deep scrollable history, a wired-up maintenance page, per-viewer favorite-color recoloring, a 49-trophy Hall of Fame with shared ties, group filters, name highlights (56 of them, including a collapsible radioactive section that pulses smoothly in each base's own colour) for everyone, and duration records, a 10-language interface with proper i18n coverage for every Hall of Fame group and every achievement name, a newest-first changelog, a more reliable reset-turn, and an action submenu that stays put.*
