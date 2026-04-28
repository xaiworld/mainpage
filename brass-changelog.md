# Brass: Lancashire — Development Changelog

## 360 versions of iterative development

### Foundation (v0.0.1 - v0.0.7)
- **Initial commit**: Full Express.js app with session auth, game engine (all 6 actions), SVG board renderer, AI bots, in-game wiki, JSON file-based persistence, Render deployment config
- **Board corrections**: 19 buildable locations with dual-type slots (Cotton/Coal, Cotton/Port), 3 non-buildable waypoints (Northwich, Blackpool, Southport), 3 external ports (Scotland, Yorkshire, The Midlands)
- **Remember me**: Persistent login sessions with checkbox
- **Login fix**: Session save before redirect to prevent race condition
- **DB hardening**: Atomic writes (temp file + rename), backup recovery on corruption
- **Version tracking**: Games stamped with version, incompatible games show friendly message
- **UI overhaul**: Rectangular locations, side panels, collapsible log, map background image with opacity slider

### Data & Stats (v0.0.8 - v0.0.10)
- **User stats**: Games played, wins, VP tracking, per-opponent breakdown, game history page
- **Draggable nodes**: All board elements movable in edit mode, positions saved per user
- **Tile corrections**: Exact costs/VP/income from the physical game — Cotton Mill, Coal Mine, Iron Works, Port (4 levels), Shipyard
- **Card hover**: Location cards highlight the location, industry cards highlight valid build spots
- **Link fixes**: Lancaster-Scotland rail only, Preston-Fleetwood added, Wigan-Warrington canal+rail, Rochdale-Yorkshire, Ellesmere Port-Northwich

### Markets & Panels (v0.0.11 - v0.0.15)
- **Distant market tiles**: 11 shuffled tiles (0,0,-1,-2,-2,-2,-2,-3,-3,-3,-4) per era
- **Coal/Iron markets**: 8 slots each, prices £1,1,2,2,3,3,4,4
- **SVG panels**: Turn Order, Money Spent, VP, Income Track, Demand — all draggable
- **Money discs**: Silver £5 and bronze £1 discs in player bar
- **Player colors**: Red, Purple, Green, Yellow
- **Income/VP per row**: Industry mat shows income circle + VP hexagon once per level

### Interactive Cards (v0.0.16 - v0.0.22)
- **Actionable popups**: Hover card → clickable action buttons (Build, Link, Sell, Loan, Develop, Pass)
- **Floating hand**: Detachable card hand with drag, resize, golden ratio cards
- **Toggle links**: Show/hide link connections on the board
- **Collapsible panels**: Left panel, right panel, log — all collapsible
- **Minimal mode**: Transparent backgrounds on VP hexagons, income circles, money discs
- **Market panels vertical**: Coal/iron top-to-bottom £1→£4 with price inside squares

### Node Customization (v0.0.23 - v0.0.30)
- **Undo positions**: Reset undoes to previous state (50-step history)
- **"Like Xai" button**: Copy xai's saved layout
- **Resize mode**: Mouse wheel + corner handles + bottom bars to resize all SVG elements
- **Scale persistence**: Saved per user alongside positions
- **Golden ratio cards**: 1:1.618 ratio enforced, JS-driven sizing with ResizeObserver

### Visual Polish (v0.0.31 - v0.0.50)
- **Demand panel**: Income circles (+3,+2,+1,+0) per demand level, highlighted current row
- **Turn Order**: Ordinals (1st, 2nd, 3rd, 4th), spacing improvements
- **VP panel**: "VICTORY POINTS" label, hexagons with player colors, names centered
- **Income panel**: 100-square serpentine track (0-99), actual income values per square, curved U-turn arrows, player-colored markers
- **Slot colors**: Cotton=dim white, Coal=dark grey, Iron=orange, Port=blue, Shipyard=brown
- **Dual slots**: Diagonal split showing both industry type colors
- **Legend**: Left panel with color swatches and letter meanings
- **First round fix**: All players get 1 action in Canal era round 1

### Bot System (v0.0.51 - v0.0.56)
- **Bot announcements**: 🤖 prefixed log entries, overlay notifications
- **Configurable delay**: 1-5 second slider for bot action timing
- **Bot reliability**: Duplicate scheduling prevention, polling triggers
- **Quick game buttons**: "Quick Game (2 bots)" and "Quick Game (3 bots)" in lobby
- **VP hexagons**: Player-colored with pink outline, VP names properly spaced

### Account & Auth (v0.0.57 - v0.0.60)
- **User account page**: Change password, game history, member since, login tracking
- **Consistent navbars**: Lobby, Wiki, Stats, username (→account), Logout on all pages
- **Admin-only user creation**: No self-registration, xai creates users, first-login password setup
- **Card sorting**: Default, by type, alphabetical, type+alpha in floating hand

### Income Track & Logs (v0.0.61 - v0.0.72)
- **Proper 100-square income track**: -10 to +30 income per turn mapped correctly
- **Serpentine arrows**: Curved U-turn chevrons showing track flow direction
- **Detailed game logs**: Money before/after, income changes with squares moved, per-turn amounts
- **Round phase logging**: Phase 1 (income), Phase 3 (reorder), Phase 4 (draw cards)
- **Log timestamps**: Toggle to show "20 Mar 2026 14:30:05"
- **Log filters**: Per-player filter buttons, newest-first order
- **Colored log entries**: Each player's actions in their color

### Slot Visuals (v0.0.73 - v0.0.79)
- **Cotton Mill**: Dim whitish slot color (#f5f0ea55)
- **Coal icons**: Lighter on dark backgrounds, individually colored in dual slots
- **Preston fix**: Port, Cotton/Port, Iron Works (corrected from game board)
- **Industry stripe**: Thin color bar on top of built tiles showing industry type
- **Flipped hexagon**: Pink outline circumscribing flipped tiles (VP scored indicator)
- **Dimmed flipped tiles**: Lower opacity to show "already used"

### Lobby & Multiplayer (v0.0.80 - v0.0.89)
- **Bot fix**: nodemon.json ignores data/ to prevent restart loops
- **Lobby**: Only shows your games, system stats sidebar, game invites
- **Delete games**: Trash bin with confirmation (creator only)
- **Add bot button**: Fill player slots one at a time
- **Player count enforcement**: 3 players for 3p, 4 for 4p before start
- **Card deck**: 66 cards exactly matching the physical game
- **White headings**: Neutral blue-grey buttons instead of scarlet

### Bot Training (v0.0.90 - v0.0.91)
- **6 bot personalities**: Cautious Carl, Aggressive Ada, Builder Bob, Wildcard Wil, Balanced Bea, Devver Dan
- **Parameterized strategies**: Exploration rate + priority weights for each action type
- **Training runner**: Complete games in ~30ms, tournaments with rankings
- **Tier system**: Pro/Average/Noob assigned from tournament results
- **Background training**: Admin toggle, batches of 3 games every 10 seconds
- **Session TTL**: Fixed to 1 year, suppressed cleanup logs

### Game Flow (v0.0.92 - v0.0.99)
- **Build Industry filters**: Industry cards show only that type, location cards show allowed types
- **Wild Build**: Use 2 cards + 2 actions to build anywhere on the board
- **Develop step-by-step**: Pick 1st tile, optionally add 2nd, can pick same type twice
- **Turn order fix**: Spending zeroed AFTER reorder, ties keep previous position
- **Overbuilding**: Own tiles with higher level, opponent coal/iron if market empty

### Live VP & History (v0.0.100 - v0.0.112)
- **Live VP**: Real-time calculation from flipped tiles + links + money/10
- **VP hover breakdown**: Popup showing scored, tiles VP, links VP, money VP with item list
- **Server-side state history**: Every action stores a snapshot for full game replay
- **Turn navigator**: ⏮◀▶⏭ buttons to browse entire game history action by action
- **External ports**: Scotland, Yorkshire, The Midlands as clickable "P" icons for selling
- **Colored cubes**: Iron orange, coal grey in mat brackets and log messages
- **Income in mat**: Numbers without + sign, cubes cost shown per level
- **Reset training**: Admin button to clear all training data

### DRL Bot Rewrite & Training (v0.0.133 - v0.0.136)
- **ValidActionGenerator**: Enumerates only valid moves (0 errors per game)
- **33 features per action**: Type, money, income, VP, network, era, tile stats
- **6 bot personalities**: Explorer Eve (ε=0.8) to Master Max (ε=0.01)
- **Full training pipeline**: Phase 1 (1000 games, rank 6 personalities) → Phase 2 (1000 games/tier with clones)
- **Tier-based bot selection**: Pro/Average/Noob dropdown when adding bots

### Industry Icons & 2-Player Mode (v0.0.137 - v0.0.138)
- **Industry icon images**: Compressed tile art from physical game on board slots
- **Built slots**: Player color background with industry image overlay + level badge
- **Empty slots**: Dimmed icons on warm beige background
- **2-player mode**: Removed 6 locations, reduced deck/markets/demand, Lancaster-Scotland canal, dedicated 2P map
- **Improved bot features**: 44 features including sell opportunities, port connectivity, era survival

### Mobile UI (v0.0.139 - v0.0.142)
- **Mobile detection**: User-agent + touch + width ≤ 768px
- **Bottom tab bar**: Info, Board, Hand, Actions, Log
- **Live DOM panels**: Real panels moved to overlays (not cloned), instant updates
- **Floating hand**: Always visible on Board tab, horizontal scroll, touch-friendly
- **Turn navigator**: Fixed bar between hand and tabs, always visible
- **Mobile market panels**: VP top-left, Turn Order top-right, Income bottom-left, Demand bottom-right
- **Touch-friendly**: Enlarged buttons, fonts, tap targets throughout

### Neural Network Bot (v0.0.141 - v0.0.145)
- **AlphaZero-inspired**: Python + PyTorch training, JS inference
- **BrassNetV2**: 2.4M params, 3 residual blocks, 512-dim hidden
- **State encoding**: 1145 features (board, links, players, hand, strategy)
- **Self-play training**: v1 (100 iters, MaxVP=60.6) → v2 (500 iters, 67.8) → v3 (500 more, 73.3)
- **Reward shaping**: Normalized VP + win + absolute VP + flips + links + income
- **Pro/Average/Noob**: Same network, different temperatures (0.05/0.4/1.0)

### Notifications & Admin (v0.0.143 - v0.0.147)
- **Web Push Notifications**: Turn, game start, game finish, game invite (VAPID-based)
- **Service worker**: Push display with click-to-open game
- **Lobby improvements**: Collapsible game sections (Active/Waiting/Finished), filter tabs, current turn indicator
- **Turn indicator bar**: Colored bar at top of game page showing whose turn + era/round
- **Market sell fix**: Cubes now fill expensive slots first (£4,4,3,3 not £1,1,2,2)
- **Admin fix mode**: xai can edit player money/income/VP, markets, turn state in-game

### Build UX & Mobile Overhaul (v0.0.148 - v0.0.155)
- **Overbuild fix**: Allow building higher level tiles on own tiles, opponent coal/iron only when market empty
- **Mobile fixed bars**: Navbar and turn indicator visible on all tabs, dynamic positioning
- **Turn indicator money**: Shows current money alongside action count (e.g. "2 actions, £17")
- **Per-develop iron cost**: Each develop in a double-develop shows individual iron cost in log
- **Game credits**: "Game by Martin Wallace - Art by Peter Dennis - Published by Eagle-Gryphon Games" in navbar
- **Controls help**: Tooltips explaining each game control in left sidebar
- **Legend toggle**: Industry color legend only visible when "No icons" checked
- **Version display fix**: APP_VERSION properly bumped and shown in header
- **VP panel rows**: Victory Points panel rewritten with vertical rows (hexagon + player name)
- **Hand & Tiles tab**: Merged Actions into Hand tab, renamed to "Hand & Tiles"

### Action Submenu & Selling Fixes (v0.0.156 - v0.0.162)
- **Action submenu**: Cascading popup appears next to action menu on card click, with all 6 action types
- **Barrow-in-Furness fix**: Corrected slots to Shipyard + Iron Works (was Port + Shipyard)
- **Barrow migration script**: `scripts/fix-barrow-slots.js` fixes existing games
- **Resizable game log**: Desktop log panel can be dragged up to 90vh height
- **Cotton selling fix**: Per-mill port filtering, BFS connectivity checks, distant market via any connected port
- **Distant market access**: Client matches server logic — any connected built port enables external market
- **Slot-level highlighting**: Click exact slot when selling cotton, hover labels show industry info

### The Weirdest Rule (v0.0.163)
- **Birkenhead special build**: In rail era, if you have a link to Liverpool or an industry in Liverpool, you can build in Birkenhead's empty shipyard slot with a Shipyard card. Coal must be reachable through any built links as normal
- **Liverpool special build**: In rail era, if you own the Birkenhead–Ellesmere Port link or have Birkenhead's Shipyard built, you can build in Liverpool's empty port/shipyard slots with a matching card, even without owning links to Liverpool
- **Server + client**: Both server validation and client-side highlighting updated for these special cases

### Changelog Page (v0.0.164)
- **Changelog nav link**: Added "Changelog" link to navbar on all pages (lobby, game, wiki, login, etc.)
- **Changelog route**: `/changelog` renders CHANGELOG.md as styled HTML page using basic markdown conversion
- **Changelog view**: Uses same wiki-container styling for consistent look

### Log Panel & Fixes (v0.0.165 - v0.0.166)
- **Log panel resize**: Increased default height to 150px, added gold drag handle indicator, removed height transition that fought manual resize
- **Changelog header fix**: Version count corrected from 112 to 164

### Cotton Sell Fix (v0.0.167)
- **Flipped port distant market**: Mills connected to a flipped port can now sell to distant market — client was only checking unflipped ports as targets, missing that any built port (flipped or not) enables distant market access

### AlphaZero Training v4 Infrastructure (v0.0.168)
- **Python game engine fixes**: Fixed Barrow slots (was port+shipyard, now shipyard+ironWorks), income cap 40, coal mine flip bug, overbuild rules (own tiles higher level OK, opponent coal/iron only if market empty), auto-sell resources to market after building, distant market income bonus, Weirdest Rule for Birkenhead/Liverpool, build link coal source fix
- **Training scripts**: Created `training/encode.py` (1145 state + 35 action features matching JS), `training/neural_net_v2.py` (BrassNetV2 2.5M params, matches JS inference architecture exactly), `training/train_v4.py` (AlphaZero self-play with replay buffer, cyclic LR, temperature decay)
- **Pass action removed**: Real players never pass — removed from action generation unless no other actions exist, forcing bots to always take loans, build, develop, or sell

### Mobile Submenu Fix (v0.0.169)
- **Mobile cascading menu**: Action submenu now appears as a vertical bottom sheet on mobile instead of popping horizontally off-screen — full-width, positioned above the tab bar

### Resource Sourcing Rules (v0.0.170)
- **Distance-based cube sourcing**: Resources now come from the closest source by BFS link distance (same location = 0, each link = +1). Coal requires network connectivity; iron available from any iron works on board
- **Player choice**: When multiple sources exist at the same minimum distance, the player chooses which to drain — UI shows all equidistant options with location name, level, cube count, and owner
- **Cube-by-cube evaluation**: Each resource cube is sourced individually; consuming a cube may flip a tile (changing available sources for the next cube)
- **Resource order choice**: When a build needs both iron and coal, player can choose which to source first via swap button
- **Resource picker UI**: New step in build industry, wild build, develop, and build link (rail) flows — auto-selects when only one option, shows picker when choices exist
- **Backward compatible**: Bots and old actions auto-pick closest source when no resource plan is provided

### Rail Link Coal Fix (v0.0.171)
- **Both-end coal sourcing for links**: When building a rail link, coal sources and market access are now checked from both ends of the link — fixes "cannot source coal" error when building a link from an isolated location to a port

### Rail Link Visuals (v0.0.172)
- **Rail link colors**: Available rail links now dark grey (`#555555aa`) instead of brownish; highlighted links glow bright grey (`#cccccc`) with drop-shadow instead of gold

### Rail Coal = 1 Per Link Fix (v0.0.173)
- **1 coal per rail**: Fixed bug where multi-segment links (through waypoints) consumed coal equal to segments instead of always 1 coal per rail link — also updated double-rail path to use resource plans and fixed Python training engine

### Action Overlay & Sell Cotton UX (v0.0.174)
- **Action overlay for all actions**: Every player action now shows a temporary overlay box on screen (like bot actions) with the log summary — 4 second display with fade
- **Sell cotton iterative feedback**: Each cotton sale (mill+port pick) shows an instant overlay confirming the sale, then asks "Sell another?" with a summary of all queued sales before batch submission
- **Multi-line overlay**: Overlay supports multi-line text for actions with multiple log entries

### Distant Market Non-Revertible (v0.0.175)
- **Distant market commits immediately**: Selling to distant market submits a partial action to the server (reveals tile, flips mill) — non-revertible since the tile was seen. Port sales remain queued locally until "Done"
- **Partial sell cotton**: Server supports `partial: true` flag for sell cotton — processes sales but doesn't consume card or advance turn, allowing continuation
- **Cancel after distant**: If distant sale was committed, cancel is hidden — player must press Done to finish the action

### External Port Link Cost Fix (v0.0.176)
- **External port links = 1 segment**: Links to Scotland, Yorkshire, and The Midlands were incorrectly set to 2 segments (costing £6/£10) — fixed to 1 segment (£3 canal, £5 rail) like normal links. Also fixed in Python training engine and applied card-removal-on-failure bug fix

### Link Segments From Board Data (v0.0.177)
- **Read segments from canonical data**: Engine now reads link segment count from static board-data.js instead of game state — fixes existing games that had stale segments:2 for external port links

### Income Track Cap Fix (v0.0.178)
- **Income cap 99 not 40**: Income track was capped at position 40 (£14/turn max) instead of 99 (£30/turn) — `adjustIncome` used `Math.min(40)` when it should be `Math.min(99)`. Players above £14/turn were silently losing income gains

### Tile Era Validation (v0.0.179)
- **Cannot build canal-only tiles in rail era**: Server now validates `canBuildCanal`/`canBuildRail` flags — L1 cotton mills, coal mines, iron works, ports are canal-only; shipyard L2 is rail-only
- **Client filters unavailable industries**: Build and wild build flows hide industry types whose top tile can't be built in the current era, using the `era` field from tile data

### Rail Era Turn Order Fix (v0.0.180)
- **Turn order by spending, not VP**: Rail era start was sorting players by VP (lowest first) instead of by spending from the last canal round (least spent first, ties keep order) — now matches normal round transition logic

### Loan Income Drop Fix (v0.0.181)
- **Land on highest square of level**: `dropIncomeLevel` was scanning forward (landing on the lowest square of the target level) instead of backward (highest square). Players were losing 1-2 extra income track squares per loan level — e.g. dropping from level 12 landed at square 31 instead of 33

### View Other Players Mats (v0.0.182)
- **Mat navigation arrows**: Left/right arrows in the mat panel header cycle through all players' industry mats — shows player name in their color with a dim background tint and color border
- **Own mat = default**: Returns to "Your Mat" when cycling back to your own seat

### Single Rail on Last Action (v0.0.183)
- **Skip double-rail prompt on last action**: When only 1 action remains, building a rail link no longer shows "Add a second rail?" — goes straight to coal sourcing and submission

### All Links 1 Segment + Non-Buildable 2VP Scoring (v0.0.184)
- **All links cost 1 segment**: Removed segments:2 from all remaining links (Southport, Blackpool, Northwich routes) — every link costs £3 canal / £5 rail
- **Non-buildable locations give 2 VP to links**: Waypoints (Blackpool, Southport, Northwich) and external ports (Scotland, Yorkshire, The Midlands) now contribute 2 VP icons for link scoring — previously gave 0

### Cover Image & Lobby Redesign (v0.0.185 - v0.0.187)
- **Game cover art**: Cover image on the left side of login page (next to form) and lobby left sidebar (above System/Players/Admin panels)
- **Lobby layout**: Moved System, Players, Admin, Bot Training panels from right sidebar to left column under the cover image

### In-Game Chat (v0.0.188)
- **Chat in game log**: Text input below the log panel to send chat messages — messages appear inline with game log entries, sorted by timestamp, styled in italic with player color
- **Stored in game state**: Chat messages persist in the game state (last 100), polled every 3 seconds with the regular state refresh

### Slot-Based Build Selection (v0.0.189 - v0.0.190)
- **Click specific slots to build**: Build industry and wild build now highlight individual slots (like sell cotton) with hover labels showing industry, level, location, and overbuild info
- **Click slot or location name**: Both methods work — clicking a slot picks that exact slot, clicking a location name auto-picks the first valid slot

### Active Tile Pulse + UI Polish (v0.0.191 - v0.0.200)
- **Unflipped tiles pulse**: Active tiles pulse opacity 0.8-1.0 in their player color with a 3s cycle. Flipped tiles at 0.7 opacity
- **SVG group wrapping**: Tiles wrapped in SVG group so pulse animates all layers together
- **Board re-render optimization**: Board only re-renders when state changes, so CSS animations persist
- **Location card build fix**: Auto-picks first valid slot when location is pre-set by card
- **Hover tooltips on all slots**: Filled tiles show industry, level, owner, VP, income, cubes, flipped status. Empty slots show allowed types
- **Player section spacing**: Gap between player cards increased to 8px
- **VP panel spacing**: Row height increased to 20px
- **Game name centered in navbar**: Shows in gold on the game page

### Lobby Polish (v0.0.201 - v0.0.205)
- **Create Game form alignment**: Vertical column layout fixes button alignment
- **Collapsible sections**: Players, Create Game, System Data all have toggle arrows
- **Non-admin users see finished games**: Members can see and review their own finished games
- **System Data panel**: Shows active/waiting/finished game counts + total players
- **Player last activity**: Shows time ago (m/h/d) of each player's last action
- **Player sorting**: Toggle between Recent activity or A-Z alphabetical order
- **My Games spacing**: More separation between heading and quick-start buttons

### Changelog Fix + Mobile Fixes (v0.0.206 - v0.0.209)
- **Changelog ordering fixed**: All entries now in strict ascending version order
- **Mobile tile pulse**: Inline animation + `@keyframes` injected inside SVG for mobile Chrome compatibility
- **Mobile chat**: Chat input moved to mobile log overlay
- **Chat overlay**: Incoming chat messages from other players shown as temporary overlay

### Finished Games + Lobby Progress (v0.0.210 - v0.0.212)
- **Finished games accessible**: Players can always enter finished games for review, even with old version
- **Lobby game progress**: Active games show era (Canal blue / Rail grey), round as X/Y, and progress bar

### Card Deselect + Rounds Fix (v0.0.213 - v0.0.215)
- **Click card to close**: Clicking the same card again deselects it and closes the action menu/popup — works in all states (your turn, not your turn, action in progress or not)
- **Rounds per era**: Fixed to 10 for 2P/3P, 8 for 4P
- **Hand tab icon**: Replaced yellow lightning bolt with neutral diamond icon on mobile

### Admin Fix Card Removal + New User Dialog (v0.0.216 - v0.0.221)
- **Missing changelog entries**: Added v206-v215
- **Develop fix**: Guard against double-invocation, auto-submit when iron source is obvious
- **Tile pulse 0.5-1.0**: Tuned pulse opacity range
- **Mobile market layout**: Coal/iron panels moved under VP, income below on mobile
- **New user dialog**: Login page has a "New User?" section where admin-created users can set their password
- **Admin Fix — remove card**: Fix panel shows player hands with x buttons to remove cards

### Train v6 + Coach + Bug Fixes (v0.0.222 - v0.0.227)
- **Sell cotton auto-finish**: Auto-finishes when no more mills after distant commit
- **Board re-render on stat changes**: Board panels now update after loans, scoring, income changes
- **Income track alternating colors**: Negative=dark reds, positive=dark greens, zero=grey
- **Brighter unflipped tiles**: Image opacity reduced to 0.55, player color shows through more
- **Darker yellow player**: Changed from #f39c12 to #d4850a
- **Game review — all mats and hands**: History navigation shows all players' mats and hands. Finished games show full state (no hidden hands)
- **v5 MCTS training**: Added MCTS + n-step TD training script (too slow, replaced by v6)
- **v6 training**: Direct scoring + 1-step lookahead + parallel workers (14 cores). Best AvgMaxVP: 74.3

### Beta Banner + Feedback + Polish (v0.0.228 - v0.0.233)
- **Admin hands hidden in active games**: Only finished games reveal all hands
- **All mats in review mode**: Shows all players' mats inline during review
- **Beta banner**: Gold banner on lobby — "This game is in early testing!"
- **Feedback form**: Players can report bugs/suggestions from the lobby. Admin sees recent feedback
- **Mobile beta banner fix**: Top margin for fixed navbar
- **Full game log**: Shows all entries, not just last 30

### Sell Cotton + Develop Fixes (v0.0.234 - v0.0.237)
- **Sell Another fix**: "Sell Another" now goes straight to mill picker instead of looping
- **Develop 2 tiles crash fix**: When both tiles point to same iron source with 1 cube, second consume now falls back to market/bank instead of crashing
- **Iron source fallback**: Depleted specified sources fall through to auto-pick instead of returning null
- **All iron sources shown**: Iron picker shows all iron works on board (no distance filtering) since iron has no connectivity requirement

### Bot Tier + Wild Build + Deploy (v0.0.238 - v0.0.240)
- **Individual bot tier selection**: Each bot gets its own Pro/Avg/Noob dropdown in Quick Game and Create Game
- **Wild Build UX**: Quick Wild Build from card popup (pre-selects first card), auto-select second card when only 2 in hand, tile info shown when picking industry (cost/VP/income), button renamed to "Wild Build (2 cards/any location)"
- **Deployed v6 weights**: AvgMaxVP 77.9, peak 124 VP, coach-mixed training with sells and loans

### Mobile Panel Alignment (v0.0.241 - v0.0.243)
- **Combined mobile left panel**: VP, coal/iron, income panels aligned to same X position with dynamic Y spacing based on player count — no more overlapping

### Reset Turn + Confirm Turn (v0.0.244 - v0.0.245)
- **Reset Turn**: Button appears after taking at least 1 action — restores game state to the start of your turn. If a distant market tile was revealed, that becomes the minimum reset point
- **Confirm Turn**: After both actions, shows "Turn complete!" with Confirm / Reset buttons. Polling paused until confirmed. Configurable via "Confirm turn" checkbox (default: on)

### Reset/Confirm Turn + Hold Game (v0.0.246 - v0.0.258)
- **Floating Reset Turn button**: Fixed position top-right, visible on any mobile tab after first action
- **Turn confirm overlay**: Centered dialog after last action with Confirm/Reset buttons, 20px gap to prevent misclicks
- **Full turn hold**: Server skips `advanceTurn` when `holdForConfirm` — no next player, no round end, no income, no card draw until confirmed. Confirm triggers `advanceTurn` then bots
- **Shows 0 actions remaining** while pending confirm

### Open Games + Weights Deploy (v0.0.259 - v0.0.260)
- **Open games**: "Open game" checkbox on Create Game — anyone can join without invite, green badge shown
- **Overbuild fix**: Must be same industry type — can't replace coal mine with cotton mill
- **Deployed v6 weights**: AvgMaxVP 94.8, peak 128 VP

### Chat + Log Improvements (v0.0.261 - v0.0.264)
- **Separate chat from log**: Game log shows only game events, chat in collapsible section with unread badge
- **Chat as 5th mobile tab**: Envelope icon, ! indicator for new messages, clears on open
- **Turn order log neutral**: System messages (turn order, round markers) no longer colored by player name
- **2 rails on last action**: Building 2 rails is 1 action — always shows double-rail option
- **Chat input on top**: Input above messages, newest messages first

### Mobile Chat + Troll Mode (v0.0.265 - v0.0.269)
- **Mobile chat fill**: Chat messages fill available vertical space on mobile
- **Troll mode**: Random Spanish trash talk phrases shown on turn confirm overlay
- **210 troll phrases**: All targeting `{rival}` by name, spoken by random opponent with colored name
- **Overlay only**: Troll phrases shown in confirm dialog, not sent to chat

### Coal Sourcing Fixes + Troll Polish (v0.0.270 - v0.0.275)
- **Coal sourcing for rails**: Directly check both endpoints of each link for coal sources
- **2-rail coal fix**: Check ALL link endpoints, each rail gets coal from its OWN endpoints only
- **Troll action summary**: Prefix troll phrase with last action taken, fix HTML tag breaking
- **Persist preferences**: Confirm and Troll checkboxes saved in localStorage

### Reset Persist + Training + Lobby Redesign (v0.0.276 - v0.0.280)
- **Persist reset version**: Stored in game state — survives page reload
- **Grey Reset button**: Less alarming color in confirm overlay
- **Per-player-count training**: Separate 2P/3P/4P weights with correct VP normalizers (250 for 2P/3P, 210 for 4P)
- **Log coal mine flips**: Single rail coal consumption now logged properly
- **Lobby redesign**: Mobile bottom panels, quick game rows, bot limits

### Submenu + Coal Sourcing (v0.0.281 - v0.0.285)
- **Quick Create buttons**: Inside collapsed Create Game section with proper arrows
- **Submenu position fix**: Correct placement when action started before card selected
- **Draggable action submenu**: Desktop drag support + touch drag on mobile
- **2-rail coal sourcing fix**: Simulate first rail as built when finding coal for second rail

### Action Blocking + Sell Fixes (v0.0.286 - v0.0.290)
- **Block stale actions**: Reject actions when `actionsRemaining=0` or `pendingConfirm` active
- **Partial sell tracking**: `partialSellCard` prevents other actions mid-sell, force finish before switching
- **Premature sell end fix**: Port queued for one mill no longer blocks distant market for other mills
- **Distant market income bonus fix**: Use track values (3,3,2,2,1,1,0,0) not raw demand number
- **Stale partialSellCard fix**: Clear on empty-sales finish path

### Sell Cleanup + Wild Build + Slot Picker (v0.0.291 - v0.0.294)
- **partialSellCard blocking fix**: Clear flag properly so next player isn't blocked
- **Distant market bottom out**: Ends sell action immediately when market is empty
- **Wild build overbuild**: Allow overbuilding opponent coal/iron tiles when market is empty
- **Slot picker**: Ask player to choose when both empty slot and overbuild exist at same location

### Changelog + Group Rankings (v0.0.295 - v0.0.296)
- **Changelog updated**: Added v265-v294 entries in grouped sections
- **Group rankings**: Lobby shows rankings for player groups with 2+ finished games. Points = players beaten per game (1st in 4P = 3 pts). Separate tables for 2P/3P/4P. Groups keyed by alphabetically sorted nicks (e.g., "alice-bob-charlie"). Ties broken by total VP.

### Phantom Card Guard (v0.0.297)
- **Phantom card error fix**: Added `_submitting` guard to prevent poll-triggered double submit of same action.

### Admin Mat Fix (v0.0.298)
- **Admin-fix `mat` parameter**: Extend admin-fix endpoint to patch `player.industryMat[industryType]` — repairs state corruption from old bugs that silently removed tiles without logging (e.g., game 3 maltzur port mat, 4 tiles missing despite logs only showing 2 develops + 2 builds).
- **Admin UI**: Mat row per player with comma-separated tile levels — edit and blur to apply.

### Build Link Validation + Error Messages (v0.0.299)
- **Second rail network check**: 2-rail builds now validate the second rail against the extended network (network + first rail's endpoints) — fixes a hole that let players build a second rail anywhere on the board regardless of network.
- **Better error messages**: "First rail X-Y must connect to your network" / "Second rail X-Y must connect to your network after the first rail is placed" — identifies which link failed and clarifies that opponent rails do not extend your network.

### Build Link UI Filter (v0.0.300)
- **Highlight only valid links**: Build Link picker now filters by the player's network (own industries + own links) so unconnected links aren't offered as choices. Second-link picker uses extended network including first rail's endpoints. Stops the UI from inviting picks the server will reject.

### Private Notes (v0.0.301)
- **Per-game per-player notes**: New textarea panel in the right panel (and mobile Hand tab) for private notes. Only the player who wrote them can read them. Persisted in `data/db.json` as `playerNotes`. Save button + Ctrl/Cmd+Enter shortcut. 10k-char cap. Notes are dropped when the game is deleted.

### Admin Flip Actions (v0.0.302)
- **Flip next demand tile for player**: Button per player in admin-fix shifts the next distant-market tile into flipped, reduces demand by |tile|, and applies the standard income bonus to the chosen player.
- **Flip any industry tile**: Admin-fix lists every unflipped owned tile with a Flip button; flipping sets `flipped=true`, applies the tile's income gain AND VP to the owner immediately. (Era scoring at era end will also count this tile, so if applied pre-scoring, adjust VP back afterward — noted in the panel.)

### Board Re-render Key Fix (v0.0.303)
- **Demand/markets re-render**: `updateAll()`'s board cache key now includes `coalMarket`, `ironMarket`, `distantMarketDemand`, and distant tile counts — fixes stale demand track after admin-fix flip, and the same class of bug for any change that only touches markets.

### Unflip Demand Tile (v0.0.304)
- **Admin-fix `unflipDemand`**: New per-tile button in the admin panel's Demand Pile section — click a flipped tile's value to return it to the unrevealed pile, Fisher-Yates shuffle the pile (including the returned tile), and restore `distantMarketDemand` by `|value|` (capped at DISTANT_MARKET_START=8). Does not roll back income bonuses; adjust player income separately if needed.

### Demand Tiles Inspector (v0.0.305)
- **"i" icon on Demand panel**: Small clickable info icon at the top of the Demand track. Opens a popup listing all demand tiles sorted ascending — flipped tiles dimmed/struck-through, remaining tiles in gold. The pile's actual draw order is hidden so opening it doesn't leak which tile comes next.

### Missing -1 Demand Tile (v0.0.306)
- **3/4P demand pile fix**: Physical Brass: Lancashire has 12 demand tiles per era (2×0, 2×(-1), 4×(-2), 3×(-3), 1×(-4)) but the code only had 11 (1×(-1)). Added the missing -1. Applies to new games and to rail-era transitions of existing canal games. In-progress canal games keep their original 11-tile pile — admin can use the Demand Pile editor to patch if desired. Python training engine updated to match. 2P variant unchanged (7 tiles, custom).

### 2P Demand Pile (v0.0.307)
- **2P derived from 12**: 2P pile now correctly removes 1×(-1), 1×(-3), 2×0 from the 12-tile pile, giving 8 tiles `[-1,-2,-2,-2,-2,-3,-3,-4]` instead of the previous 7. CLAUDE.md updated.

### ELO Ratings (v0.0.308)
- **Per-category ELO (2P/3P/4P)**: Every human user has three ELO ratings, one per player-count category. Everyone starts at 1000. Only all-human games count. Pairwise ELO with K=32, no normalization — zero-sum per game (max swing = K × (N−1) per player).
- **Tiebreakers for placement**: VP → income square → money → spentThisRound (lower wins) → turnOrder index (lower wins).
- **Rating update logged to the game state** with lines like `#1 xai: 1000 +27 → 1027` so players see the result on the game page when a game finishes.
- **Lobby leaderboard**: New "ELO Ratings" section above group rankings, one table per category (2P/3P/4P). Shows rank, username, rating, games played. Players only appear in a category once they've played a game in it.

### ELO Badge in Game (v0.0.309)
- **ELO shown next to each player's name** in the game player bar — the current rating for this game's player-count category. Tooltip shows category + number of games played. Hidden for bots.

### ELO Snapshot at Game Start (v0.0.310)
- **Game-start ELO snapshot**: When a game transitions to `active`, each human player's current ELO for the game's category is snapshotted into `state.eloAtStart`. `recordGameResult` uses that snapshot for the rating delta computation instead of whatever the rating has drifted to by game end. Standard chess practice — fair to performance at the time played, removes path dependence between concurrent games.

### ELO Visibility + Only-New-Games Rule (v0.0.311)
- **Only snapshotted games score ELO**: Games already `active` at the v0.0.310 deploy have no `eloAtStart`; `recordGameResult` now SKIPS ELO for those instead of falling back to current ratings. Waiting games that start after the deploy will be snapshotted and counted normally.
- **Lobby player list shows ELOs**: Each player row next to the "stats" link now lists their 2P/3P/4P ratings as small badges.
- **Profile page**: New "ELO Ratings" section with 3 cards (2P/3P/4P) showing rating + games played.
- **Account page**: Same "ELO Ratings" section for your own account.

### ELO Badge Visibility (v0.0.312)
- **Always show 2P/3P/4P badges in the lobby player list**, even for unplayed categories (at 1000 with 0 games), dimmed. Previously hidden until the player had played in that category, which meant nothing showed right after launch.

### Admin Pause + Turn Order Fix (v0.0.313)
- **Admin pause button**: Prominent Pause/Resume at the top of the admin-fix panel. When paused, `state.adminPaused=true`; the `/action`, `/confirm-turn`, and `/reset-turn` endpoints reject with HTTP 423 and a "game paused by admin" message. Bot scheduling and bot execution also short-circuit while paused. Players see a red `⏸ Paused by admin — actions disabled` banner in the game info panel.
- **Turn order editor**: Admin panel's new "Turn Order" section lists each position with the player, with ↑/↓ buttons to swap adjacent positions. Fix endpoint accepts `turnOrder: [seats]` and validates it's a permutation of all seats before applying.

### Rail Era Turn Order Bug (v0.0.314)
- **Canal → Rail transition was sorting on zeroed spends**: `transitionToRailEra` zeroed each player's `spentThisRound` BEFORE computing the new rail-era turn order, so all players looked like 0-spent and the stable sort preserved the canal-era order. Last-place players stayed last even if they spent nothing (e.g. took loans). Fixed by computing the new turn order FIRST, then zeroing `spentThisRound`. Regular round-end (`endRound`) was already correct.

### Era-End Phase Logs (v0.0.315)
- **Phase 3 / Phase 4 logged at era end**: The last round of each era now emits the same phase 3 ("New turn order: …") and phase 4 ("0 cards drawn, deck exhausted. Spent boxes emptied.") log entries as normal rounds do — before scoring runs, so the log reads coherently. Canal end shows the upcoming rail-era turn order; rail end shows the final spending order for tiebreaker visibility.

### Phase 4 Log Honesty at Game End (v0.0.316)
- **Rail-end log no longer claims spent boxes were emptied**: The phase 4 line at the last rail round now reads *"Spent boxes kept for end-game tiebreakers."* — the engine doesn't actually zero `spentThisRound` at game over because the end-game tiebreaker chain (VP → income → money → spent last round → turn order) needs it. Canal end still says *"emptied for the next era."* (the rail-era transition zeroes them after reordering, per v0.0.314).

### Memory Reductions (v0.0.317)
- **No pretty-print in `db.js` save**: `JSON.stringify(data)` without spacing — ~20-40% smaller in-memory string and on-disk output, with the same memory temporarily live next to the object during every write.
- **Cap `gameStateHistory` per game** at 30 versions. Older snapshots pruned on each push. Enough for turn reset + recent replay; far less than the unbounded growth that was happening before.
- **Prune finished games' history** to last 5 snapshots when a game transitions to `finished`. Significant one-off reclaim.

These address the OOM reported at ~190 MB steady state: whole-db stringify + growing history was spiking RAM well past the 512 MB Render limit during save.

### Boot-Time DB Compactor (v0.0.318)
- **New `scripts/compact-db.js`** runs before `server.js` on every boot. It prunes `gameStateHistory` per game (30 for active/waiting, 5 for finished) and rewrites `db.json` without pretty-print. Separated from the main process so memory for the prune doesn't compete with Express, NN weights, and session store at boot time.
- **`render.yaml` startCommand**: `node --max-old-space-size=450 scripts/compact-db.js && node --max-old-space-size=450 server.js` — the `&&` ensures server only boots if the compactor succeeds, and the explicit heap cap gives V8 ~450 MB of old-space on the 512 MB container (enough headroom so fragmentation + transient allocations don't hit the ceiling).

### Streaming DB Compactor Fallback (v0.0.319)
- **New `scripts/stream-compact-db.js`**: byte-level streaming rewrite of `db.json` that never calls `JSON.parse`. Finds `"gameStateHistory":[ … ]` in the byte stream, tracks nesting with JSON-string-aware bracket counting, and emits `[]` in its place — everything else passes through unchanged. Uses 64 KB read chunks, memory use is O(chunk), not O(file).
- **Size-gated**: only runs when `db.json` exceeds 20 MB; under that, the normal in-memory compactor handles it and preserves per-game history.
- **Emergency semantics**: when it runs, it clears ALL gameStateHistory (in-progress games lose turn-reset history). Finished-game final states in `gameStates` are untouched.
- **`render.yaml` startCommand** chains: `stream-compact-db.js && compact-db.js && server.js`. Fix for v0.0.318's failure to boot when `db.json` itself was too big for `JSON.parse` (SIGABRT / exit 134 from V8 "Reached heap limit" inside the in-memory compactor).

### Self-Compaction at Boot (v0.0.320)
- **`server.js` runs the compactors itself** (via `child_process.spawnSync`) BEFORE requiring `lib/db.js`. Independent of `render.yaml`'s `startCommand` — Render dashboards override the YAML, so even if the chain wasn't picked up by the platform, the server now self-protects. Each compactor runs in a child process with `--max-old-space-size=350` so its memory doesn't stay live in the server. Errors are logged but do not block boot.

### Reset-Turn Fallback Snapshot (v0.0.321)
- **`state.turnStart` embedded snapshot**: a serialized copy of the state at the start of each turn lives directly on the live state. Survives `gameStateHistory` pruning/wiping (e.g. emergency-compactor runs).
- Snapshot is taken at: game creation, every turn advance to next player (`advanceTurn`), every new round (`endRound`), canal→rail transition, and after every distant-market partial-sell commit (so a later reset doesn't undo the irreversible tile reveal).
- **Reset-turn endpoint** now tries the requested history version first, then falls back to `state.turnStart`, then errors with a clearer message if neither exists. Fixes broken reset for in-progress games whose history was wiped by the streaming compactor.
- The snapshot strips the previous `turnStart` before serializing to avoid exponential nesting; per-state cost is one extra ~20 KB string.

### Lobby Game Age + Sell Diagnostics (v0.0.322)
- **Lobby shows game age**: each game row now shows "Started <date> · N days old" under the player list. Records `started_at` when status flips from waiting to active (existing games fall back to `created_at` for display).
- **Server-side sell-cotton logging**: every "Mill already flipped" / "Not your cotton mill" / "Not a cotton mill" / "Invalid mill location" rejection now writes to the Render server log with full details (user, seat, sale payload, slot state at rejection time, queued sales, partialSellCard, cardPlayed). For diagnosing the persistent "mill already flipped" report from game 25 / didiita.

### Lobby Date Format (v0.0.323)
- **"24 April 2026" format**: `toLocaleDateString('en-GB', { day:'numeric', month:'long', year:'numeric' })` — day, full month name, year. Locale-independent.

### Sell Cotton Duplicate-Click Bug (v0.0.324)
- **Server log diagnosed it**: didiita's `sales` payload contained `bolton[0]` four times. Cause was the SVG port slot's `onclick` handler persisting after a sale was queued — clicking the same port again (intentionally or accidentally) re-fired the callback and pushed another duplicate entry. The server then iterated, flipped the mill on entry #1, and rejected entry #2 with "Mill already flipped".
- **Three layers of fix**: (1) the port-pick callback now calls `BoardRenderer.clearHighlights()` immediately after pushing the sale so the SVG handler is gone; (2) a `_lastSaleKey` dedup guard rejects a second push of the same mill→port pair; (3) `finishSellCotton` dedupes `sales` by mill before submitting as defense in depth. (4) The mill-pick callback also clears highlights and resets the dedup key when a new mill is chosen.
- **External port click is single-shot** — first click clears its own onclick handler before triggering `sellCottonToDistant`, so rapid double-clicks can't fire it twice.

### Invite Dropdown (v0.0.325)
- **Lobby invite is now a select**: text input replaced with a `<select>` populated from the alphabetical user list (excluding bots, the inviter themselves, current game members, and already-invited users). Submit button disables when there's nobody left to invite.

### Pre-Invite at Game Creation (v0.0.326)
- **Custom Game form has 3 player invite dropdowns**: each pre-populated with the alphabetical user list (excluding the creator). Selected players are sent invites at game-creation time, the same way the per-game invite button works. Server dedupes and skips empty / self.

### Cube Owner in Logs (v0.0.327)
- **Resource log now shows the cube's owner**: `1 coal from Manchester [byfed] (free, 2 left)` instead of the previous `1 coal from Manchester (free, 2 left)`. Makes it instantly visible whose mine/iron-works was tapped without cross-referencing the board.

### Liverpool–Ellesmere Port Connection Fix + Single-Rail Spend Log (v0.0.328 - v0.0.329)
- **v0.0.328** had Liverpool↔Ellesmere Port wrong as rail-only; **v0.0.329** corrects it to **canal-only** per the physical board (in `lib/board-data.js` and `training/game_engine.py`).
- **Single-rail "spent £X" log fix**: was using `player.spentThisRound` (cumulative for the round) which made each rail action's log read as the running total. Now uses the action's own cost like canal-build and 2-rail-build logs do — consistent across all link-build flavors.

### Retrofit Liverpool–Ellesmere Rail Flag (v0.0.330)
- **One-shot DB migration** in `migrate()` clears the `rail: true` flag on `liverpool-ellesmerePort` in every existing game's `state.board.links` — but only where the link is unbuilt (`type !== 'rail'`). Already-built rails are preserved. Idempotent: re-running on later boots is a no-op once the flag is cleared. Applies to all in-progress games on the next deploy.

### Hide Canal-Only Link in Rail Era (v0.0.331)
- **Rail era now hides unbuilt canal-only links** (`!linkState.rail && owner === null`). Liverpool↔Ellesmere Port is the only canal-only link, so this stops the dashed-gray "topology only" line from showing during rail era when it can never actually be built. Other links unaffected.

### Migration Also Restores `canal: true` (v0.0.332)
- v0.0.328 had Liverpool↔Ellesmere stored as `canal: false, rail: true` — games created during that brief window then had the v0.0.330 migration only flip `rail` to false, leaving `canal: false`. Result: both flags false → dashed-gray line in canal era too.
- New migration normalizes any unbuilt instance to `canal: true, rail: false`. Already-built links untouched. Idempotent.

### Lobby Highlights "Your Turn" Games (v0.0.333)
- **Top banner** above the lobby layout lists games where it's your turn, each as a clickable shortcut link to the game page. Hidden when there are none.
- **Per-row highlight** on those games: gold gradient + left border + glow + animated `▸ Your turn` badge in the game-info row. Existing badges, player names, and progress bars are kept unchanged.
- Server-side `isMyTurn` flag is computed only for active games where the user is a member and the current-player seat's `userId` matches.

### Lobby ELO Sort + Finished-Section Collapse (v0.0.334)
- **Players sidebar** has three new sort buttons next to Recent / A-Z: **2P ELO, 3P ELO, 4P ELO**. Sorts by rating descending in the chosen category; players with zero games in that category drop to the bottom (ties alphabetical). Each player row now carries `data-elo2p` / `data-elo3p` / `data-elo4p` plus their game counts so the sort is purely client-side.
- **Finished games section** is explicitly collapsed on every page load. Was already inline-styled `display:none`, now also force-collapsed via JS in case any future state-restore logic ever expands it.

### Auto-Start When Full (v0.0.335)
- **Games auto-start the moment the seat count is reached** — no more "creator must click Start". Hooked into `/games/:id/join` (open games), `/games/:id/accept-invite`, `/games/:id/add-bot`, and `/games/create` (covers creator-with-bots filling all seats at creation). Calls a new `startGameIfFull(gameId)` helper which idempotently no-ops if the game isn't waiting or isn't full.
- Manual `/games/:id/start` still works for partial-fill starts (creator can start a 4P slot with only 2 players, etc).
- After the auto-start, the player who triggered it is redirected straight to the game page; the others see "active" + their turn highlight when they next refresh the lobby (and get a push notification if subscribed).

### Other-Turn Shortcut in Navbar (v0.0.336)
- **`/api/user/other-turns?gameId=X`** returns active games (≠X) where it's the user's turn.
- **In-game navbar link** appears as `▸ Your turn in <gameName>` (with `(+N more)` if there are extra games) once the current player's turn here is over and at least one other game is waiting on them. While it's their turn in the current game, the link is hidden. Refetches immediately on the turn-status transition; otherwise throttled to once per 15s.

### Mat "Develop First" Hint (v0.0.337)
- **Mat panel "Next:" line is era-aware**. If the top-of-mat tile can't be built in the current era — `era: 'canal'` in rail era, `era: 'rail'` in canal era, or shipyard L0 placeholder — the panel now shows `⚠ Lx is canal-only — develop first to reach Lx+1` (orange) instead of the misleading `Next: x — £…` line that implies it's directly buildable. When the top tile IS buildable, the panel reads as before.
- **New `'overbuilt'` tile status**: tiles that aren't accounted for in mat/board/developed/canal-removed (only happens when overbuilt — own higher-level overbuild or empty-market opponent overbuild on coal/iron) used to default-render as `'available'`, misleading the player. Now classified as `overbuilt` instead.

### Overbuilt Tile Visual (v0.0.338)
- **`tile-overbuilt` CSS** renders overbuilt tiles with a dark red border, faded fill, and a red ✕ overlay — visually distinct from `tile-used` (developed / canal-removed). Hover tooltip reads "Overbuilt — replaced on the board". Diagnoses cases like didiita's L1 ironWorks in game 25 (xai overbuilt it with L3 in C6, market was empty so it was legal — the tile is gone, not pending develop).

### Tile Corner Markers + Lobby Invite Visibility (v0.0.339)
- **Mat tile corner markers**: developed tiles get a tiny **orange** square in the upper-right corner; canal-removed tiles get a tiny **blue** square. Hover tooltips read "Developed — out of game" and "Removed at end of canal era" respectively. Distinguishes the two flavors of "out of game" from the still-blends-in dim grey base.
- **Lobby invite list visibility**: pending invites (sent but not accepted yet) are now shown as `Invited (pending): a, b, c` under each waiting game's row. Visible to:
  - **Everyone** for open games (anyone can join, so the pending list is public).
  - **Creator, current members, invited users only** for closed games (privacy preserved from non-invited outsiders).

### Lobby Up-To-Date / Join-Create Banners (v0.0.340)
- **Three-state lobby banner** at the very top, all sharing the same shape as the existing turn banner so the layout doesn't shift:
  1. **Your turn**: when at least one game is waiting on the user (existing — gold).
  2. **Up-to-date** (green): when the user is in 1+ active games but it isn't their turn anywhere — *"You are up-to-date with the community, Mr. Wallace would be proud."*
  3. **Join / Create** (blue): when the user has no active games — invites them to either jump into an open waiting game or create a new one. If no open waiting games exist, only the create-link is shown.
- **Smart links**: the "Join a waiting game" link scrolls to the Waiting section and expands it; the "Create a new one" link scrolls to the Create New Game block and expands it.

### Achievements + Streak (v0.0.341)
- **`lib/achievements.js`** with 45 definitions across game count, win count, single-game VP, ELO milestones, industry mastery, play behavior, distant market, money/loans, links, underdog/rivalry, marathon/sprint, daily-streak, and time-of-day. Each has `id`, `name`, `desc`, `kind`, and `check(ctx)`.
- **Game-end evaluation** runs in `recordGameResult` after the ELO update; **action-time** evaluation runs in the `/action` handler (covers `late_night` and streak updates).
- **Streak tracking**: `user.streak = { current, longest, lastDate }` advances by 1 each new UTC calendar day a user submits an action; resets to 1 on a gap. Streak achievements at 5 / 10 / 20 / 50 / 100 / 365 days.
- **Pending toasts**: newly earned achievements are queued in `user.pendingAchievementToasts`. `/api/user/achievement-toasts` returns + clears them; the in-game client polls every 6s and shows a sliding gold/red overlay when one arrives — works mid-game in another tab.
- **Lobby top-bar streak pill** (`🔥 N days`) when current streak > 0; tooltip shows longest. Click goes to your profile.
- **Profile/Stats page** has a new **Achievements** section: 45-cell grid, earned ones colored/lit, locked ones desaturated. Hover shows criterion + (if earned) the date and game it was earned in.

### Achievements Visibility Fixes (v0.0.342)
- **Achievements section now shows for everyone** on the profile page, including users with zero finished games — was nested inside the `gamesPlayed === 0` else-branch and got hidden.
- **Account page mirrors the section** so you don't have to bounce to `/profile` to see your own.
- **Section header now shows progress count** (`12/45 earned`) next to the streak pill.

### Lobby Achievements Strip + Sort (v0.0.343)
- **Top-of-lobby strip** now shows your `🏆 N/45 Achievements` (with your 3 most recently earned as small chips) alongside the existing 🔥 streak pill. Click goes to your profile.
- **Per-player trophy badge** in the lobby player list — gold `🏆 N` next to each player's ELO badges. 0-count is dimmed.
- **Sort by achievements** — new "🏆 Ach" button in the player list sort row; sorts descending by achievement count, ties alphabetical.

### Backfill Achievements (v0.0.344)
- **`POST /api/admin/backfill-achievements`** (xai-only) replays every finished all-human game in chronological order, evaluates `evaluateGameEnd` for each player against the archived final state, and grants any achievements they would have earned. Idempotent — already-earned IDs are skipped.
- **Late Night** is reconstructed from log timestamps (`ts` on each log entry — checks for any user-named entry with UTC hour 02–05).
- **Streak achievements + `user.streak`** are reconstructed from the union of `gameActions` (main DB) and per-game archived `actions`, deduped to UTC days, walked forward to find the longest run and award all crossed thresholds (5/10/20/50/100/365). Final `user.streak.current` reflects the actual current run if the last action was today or yesterday, else 0; `longest` is preserved.

### Per-Player VP + Trophy in Lobby (v0.0.345)
- **Each player name in the lobby game row** now shows their current/final VP (`alice 42 VP`). Active games pull from the live state; finished games pull from the stored `gameResult`.
- **Trophy 🏆 prefix** on the winner's name in finished-game rows so you can spot the winner at a glance.

### Player Links + Wider Sidebar (v0.0.346)
- **Player name in the lobby Players list is now a link** to that user's `/profile/<username>` stats page (with hover highlight). Replaces the previous tiny "stats" link tail.
- **Achievement count badge** is also a link to the same profile page (so clicking the 🏆 N opens the achievements grid).
- **Lobby sidebar doubled** from 200 px to 400 px on desktop — the ELO + 🏆 + activity badges no longer wrap aggressively. Mobile layout unchanged (stacks full-width as before).

### Streak Sort + Per-Player Streak Badge (v0.0.347)
- **🔥 N badge** on every row in the lobby Players list — current streak in days, red-tinted when > 0, dimmed at 0. Tooltip says "N-day streak".
- **🔥 Streak sort button** added next to the other sort buttons; sorts players by current streak descending, ties alphabetical.

### Distant-Sell Stuck Turn Fix (v0.0.348)
- **Distant-market sale that's the last action and leaves no more mills to sell** used to leave the UI on the "Sell from <location>" target picker and the turn never advanced. Cause: `renderSellCottonFlow` auto-called `finishSellCotton()` when no more mills, but `finishSellCotton()` bails on its `_submitting` re-entry guard — and the outer `sellCottonToDistant()` was still holding `_submitting=true` at that point. Auto-call now fires via `setTimeout(0)` so the outer flag clears first.

### Client Board-Data Sync (v0.0.349)
- **`public/js/board-data-client.js` now matches `lib/board-data.js`** for all link `segments` and `canal/rail` flags. The client copy was stuck on the pre-v0.0.176 segments=2 for external-port and waypoint links (Lancaster–Scotland, Colne–Yorkshire, Rochdale–Yorkshire, Preston–Blackpool, Preston–Southport, Wigan–Southport, Southport–Liverpool, Ellesmere Port–Northwich, Northwich–The Midlands, Macclesfield–The Midlands), and on the pre-v0.0.329 rail-true Liverpool–Ellesmere Port. UI displayed costs as £10 + 1 coal where the server actually charges £5 + 1 coal — visible in the rail-build single/double prompt. Server pricing was already correct; this is a UI-only correction.

### Lobby Player Order by VP (v0.0.350)
- **Player names in each lobby game row are now sorted by VP, highest first** — so the winner is always leftmost in finished games, and the current leader is leftmost in active games. Waiting games (no VPs yet) keep their original seat order.

### Potential VP Display (v0.0.351)
- **Each player's VP hex in the in-game player bar now shows a `+N` gold pill** representing their *potential* VP — what they'd score if every currently-on-board tile were flipped (theirs *and* opponent tiles in their link locations, since link VP counts neighbors regardless of owner).
- **Calculation**: sums tile VP for every owned slot regardless of `flipped`, plus 1 link-VP per owned-or-opponent tile in connected locations of every owned link, plus money/10 — i.e. the upper-bound score assuming the era ended right now and every tile flipped.
- **VP breakdown popup** now also includes a `Potential: <total> +<delta>` row in dashed gold beneath the existing `Projected:` row, so the popup tells the same story as the badge with the running total spelled out.

### Zero-Downtime Deploys (v0.0.352)
- **`render.yaml` now declares `healthCheckPath: /health`** so Render keeps the old container serving traffic until the new instance returns 200 on `/health`, eliminating the 502 window that opened during deploys.
- **`/health` and `/healthz` registered before session/DB middleware** in `server.js` — the probe answers the moment the listener is up and doesn't wait on session-store init or any later middleware.
- **`startCommand` simplified** from `stream-compact-db && compact-db && server` to just `server`. `server.js` already runs `preBootCompact()` at module load, so the chained scripts were duplicating work (compaction was effectively running 4× per deploy, lengthening the boot gap).

### Lobby News Feed (v0.0.353)
- **New `📰 News` panel** on the right side of the lobby (desktop) — sticky, scrollable, color-coded per event type. Stacks below other content on mobile.
- **Three event sources** are pushed into a rolling 200-entry feed in `db.json`:
  - **🏆 Achievement earned** — fired from `db.grantAchievements()` for every newly-granted ID (one news entry per achievement).
  - **👑 Game won** — fired from `recordGameResult()` for each non-bot winner (handles ties → multiple entries).
  - **🔥 Streak record** — fired from a new `db.checkStreakRecord(user)` called from the action handler whenever `user.streak.current > d.meta.globalLongestStreak`. Tracks the previous holder so the news entry reads "set a new all-time streak record: 12 days (was 7 by bob)".
- **`GET /api/news?limit=30`** returns latest entries newest-first; the lobby polls it every 30 s and flashes any newly-arrived items with a 2.4 s gold fade-out (`@keyframes news-flash`).
- **Initial render is server-side** (first 30 entries injected directly into the panel) so the feed shows immediately without a flash; the JS poller takes over for subsequent updates and shares a `renderNewsItem()` shape that exactly matches the EJS template.
- **Hotfix in same version**: the EJS template had a broken `<% } %><% else if %>` if-chain split across separate JS islands, which threw at render time and produced a 500 across the whole lobby. Wrapped the icon/title computation inside a single `<% ... %>` block.

### Wider Lobby Center Column (v0.0.354)
- **`.container-wide` modifier** on the lobby's `<main>` raises `max-width` from **900 px → 1400 px**. With a 400 px left sidebar and 300 px right News panel, the games column was getting squeezed to ~150 px on desktop; it now opens up to ~600 px (~4× wider) so game cards, action lists, and player names actually fit on one row.
- **Other pages unchanged**: login, profile, game, account, etc. still cap at 900 px since they're single-column.

### News Panel: Collapsible + Mobile Hoist (v0.0.355)
- **News heading is now collapsible** via the same `toggleLobbySection(this)` pattern used by Feedback / Players / System Data — click the header to fold/unfold; the existing `↻` refresh button gets `event.stopPropagation()` so clicking it doesn't toggle.
- **Mobile auto-hoist**: on `body.is-mobile`, the panel is moved out of `lobby-layout` and inserted *above* it (right under the top banner) instead of stacking below the games column where it was easy to miss. It also starts **collapsed by default on mobile** so it advertises its existence without pushing games down — tap the header to expand.

### News Panel Mobile Spacing (v0.0.356)
- **`12 px / 16 px` margin** added to the lobby news panel on mobile (both the `body.is-mobile` rule and the `(max-width:768px) and (hover:none)` media query) and an `8 px` top margin on the cover image, so the News panel and the Brass cover image no longer visually touch.

### v1.0.0 — Out of Early Testing
- **Version bumped from `0.0.356` → `1.0.0`** in `lib/version.js`. The game has been stable enough across 356 iterative releases (full ruleset, 2/3/4-player, neural-net bots, mobile, achievements, ELO, news feed) to drop the alpha-style versioning.
- **Removed the early-testing banner** (`<div class="beta-banner">…</div>`) and its CSS (`.beta-banner` rule + `body.is-mobile .beta-banner` margin). Lobby content now starts immediately under the navbar.
- **Version badge in the navbar** on every page (`<span class="nav-version">v<%= appVersion %></span>`) — gold rounded pill on the right side of the brand block. `appVersion` is now exposed via `res.locals` middleware in `server.js`, so all 12 navbars (lobby, login, account, profile, changelog, game-incompatible, all 4 wiki pages) get it without per-route plumbing.
- **Mobile**: navbar version is hidden (`body.is-mobile .nav-version { display: none }`) and a `.footer-version` line at the bottom of the lobby is unhidden instead, matching the request "in the top bar in Desktop, in phone in the bottommost".
- **Mobile container top-padding** of `50 px` added (was missing — the old beta-banner had been providing the offset for the fixed navbar; without it the first content rode under the navbar).

### Turns-Today Counter (v1.0.1)
- **`user.streak.todayCount`** is now incremented on every action submission inside `updateStreak()` in `lib/achievements.js`. It resets to `1` whenever a new UTC day rolls over, and increments otherwise.
- **Navbar 🎲 badge** on every logged-in page (lobby, account, profile, changelog, game, game-incompatible, all wiki pages) shows the current user's count for today. Powered by `res.locals.myTurnsToday` set in `server.js` so all 11 navbars get it without per-route plumbing.
- **Lobby Players list** has a new `🎲 N` blue-tinted badge per row (dimmed when 0) plus a matching **`🎲 Today` sort button** alongside the existing Recent / A-Z / 2P-3P-4P-ELO / 🏆 Ach / 🔥 Streak buttons. Sort descends by today's count, ties alphabetical.
- **Day boundary**: count is keyed by `user.streak.lastDate === today` so yesterday's count from a user who hasn't yet acted today shows as 0 even if their `todayCount` field still holds yesterday's number — no stale displays.

### Turns-Today Backfill (v1.0.2)
- **Daily recount in `migrate()`** scans `data.gameActions` for entries with `created_at` matching today's UTC date, groups by `user_id`, and writes the tally to each non-bot user's `streak.todayCount`. Marks `streak.lastDate = today` for any user with ≥1 action so the navbar's stale-day guard doesn't suppress the value.
- **Cached behind `data.meta.turnsTodayDate`** — only runs on the first boot of each new UTC day, so it doesn't add work to subsequent restarts.
- **Why**: v1.0.1 only forward-counted, so existing users who'd already played turns today (before the deploy) saw 0 in the badge. The migration retroactively fills in the actual count from the actions log.

### Maintenance Page (v1.0.3)
- **`public/maintenance.html`** is a self-contained HTML page that auto-fetches the latest release section of `CHANGELOG.md` from the GitHub raw URL and renders it with the game's color palette (gold on dark blue) plus a spinner, version pill, and 20-second auto-refresh `<meta http-equiv="refresh">` so the user lands back on the live site as soon as the deploy is done.
- **`/maintenance` route alias** in `server.js` (`res.sendFile(...)`) so the page is reachable at a clean URL for previewing.
- **Hosted at `https://xai.world/brass-maintenance.html`** — committed to the `xaiworld/mainpage` repo (deployed as the `xai-world` static site on Render). Render requires the maintenance-page URL to live on a *different* service than the one in maintenance, so co-hosting in `public/` of `brass-lancashire` is not enough; the same HTML now exists in both places.
- **How to plug it in (one-time, manual on Render)**:
  1. Open Render dashboard → `brass-lancashire` service → **Settings → Maintenance Mode**.
  2. Set "Custom Maintenance Page URL" to `https://xai.world/brass-maintenance.html`. Save.
  3. Toggle the switch on only when you actually want incoming traffic blocked (manual DB migration, planned downtime). Render returns 503 with that page while the toggle is on.
- **Why not automatic per-deploy**: Render's maintenance toggle is dashboard-only (no documented public API endpoint as of v1.0.3), and `healthCheckPath: /health` from v0.0.352 already gives zero-downtime deploys — the old container keeps serving until `/health` returns 200 on the new one. The maintenance page is for *manual* outages, not automatic deploy gating. If you do still see 502s during a deploy, double-check Render dashboard → Settings → "Health Check Path" actually shows `/health` — the YAML can need a manual "Sync render.yaml" click to take effect on an existing service.

---

*Built with love iteratively through 360 versions of user-driven development — from a blank repository to **v1.0.3**: a full multiplayer Brass: Lancashire with neural-network AI, mobile UI, push notifications, ELO, achievements, streak records, daily turns counter, live news feed, and a deployment maintenance page.*
