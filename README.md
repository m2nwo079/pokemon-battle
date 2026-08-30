# Pokémon Card Battle

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![Three.js](https://img.shields.io/badge/Three.js-r185-000000?logo=threedotjs&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-4169E1?logo=postgresql&logoColor=white)
![Render](https://img.shields.io/badge/Deploy-Render-46E3B7?logo=render&logoColor=white)
![CI](https://github.com/m2nwo079/pokemon-battle/actions/workflows/ci.yml/badge.svg)

A real-time, 1v1 turn-based Pokémon card battle game. Each player is dealt 6 random Pokémon and fights across 6 rounds — where type matchups can flip raw stats on their head.

**▶ Play: https://pokemon-battle-web.onrender.com**

> Hosted on a free tier. The backend is kept awake during the day via a scheduled ping, but the first request during off-hours may take ~1 minute to spin up.

## What the game is

Two players join the same room and each receive 6 Pokémon cards. Every round, both play one card, and the two Pokémon fight it out turn by turn. Whoever wins the most of the 6 rounds takes the match.

Cards are graded by their base-stat totals, and each grade has its own draw probability. The roster spans generations 1–3 (~180 Pokémon), and each Pokémon's moves are drawn only from what it can actually learn — pulled from PokéAPI and stored in a relational table.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Spring Boot 4, Java 21, WebSocket, JPA / Hibernate |
| Frontend | React, Vite, Three.js |
| Database | PostgreSQL (Neon) |
| Deployment | Render (backend + static hosting) |
| CI | GitHub Actions |
| Data source | PokéAPI |

## Design decisions worth noting

**Hidden information is filtered on the server, not the client.** Data that must stay secret is never sent to the client at all — an opponent's card ships with no move list whatsoever, and your own moves omit nothing except what you're allowed to see. Hiding it in the UI would be trivial to bypass with dev tools, so the serialization step itself produces different payloads for "mine" versus "theirs."

**External APIs are called only at seed time, never at runtime.** A single PokéAPI call has ~0.8s of round-trip latency, which is unusable mid-game. All Pokémon and move data is pre-loaded into the database once, and the game reads from the DB in milliseconds.

**Battle logic is a framework-agnostic pure function.** The damage calculation (`BattleEngine`) and turn resolution (`Battle`) know nothing about Spring, the database, or WebSocket. Even randomness — the damage roll, the critical-hit check, the speed-tie coin flip — is injected through a `RandomSource` interface, so the whole thing is deterministic and unit-testable without a running server.

**The server is the single source of truth.** Who strikes first, how much damage lands, how much HP remains — the server decides all of it and pushes the result. The client only renders what it receives; it never recomputes HP or PP on its own, even for drain healing or Struggle recoil.

**The free tier is a first-class design constraint.** The server sleeps after 15 minutes idle, so the frontend wakes it ahead of time, and JVM options and connection-pool sizes are tuned for a 512 MB / single-CPU instance. A scheduled ping keeps it awake during active hours.

## Game rules

- **Grades** — Pokémon are split into S/A/B/C by base-stat total, and cards are dealt by grade-weighted probability.
- **Damage** — an approximation of the original formula, with a same-type attack bonus (1.5×), type effectiveness, and a 1/16 critical-hit chance (1.5×).
- **Turn order** — the faster Pokémon moves first; a speed tie is broken by a fair coin flip; a knockout denies the opponent their turn.
- **Status conditions** — poison, burn, and paralysis, taken from PokéAPI's real ailment data (poison chips HP each turn, burn also halves physical damage, paralysis may skip a turn and quarters speed).
- **PP & Struggle** — when every move is out of PP (or a card has no usable moves at all), the UI surfaces a Struggle action — a fixed attack that ignores type and effectiveness and recoils on the user.
- **Drain moves** — absorbing moves heal the user for a fraction of the damage dealt.
- **Draw prevention** — once the battle reaches 30 turns, the winner is decided by remaining HP ratio.

## Running locally

**Backend**

```bash
./gradlew bootRun
```

Requires Neon connection details as environment variables (`DATABASE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

**Seeding** — run once to pull the gen 1–3 roster from PokéAPI into the database (takes ~25–30 minutes; it clears existing data first, then refills).

```bash
./gradlew bootRun --args='--seed'
```

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

Open a second incognito window in the same browser to play both sides yourself.

**Tests**

```bash
./gradlew test
```

`BattleEngineTest` covers damage, type effectiveness, criticals, accuracy, speed ties, and Struggle recoil — all deterministic thanks to the injected `RandomSource`, so no database is needed. The full-context `@SpringBootTest` is gated on `DATABASE_URL` and is skipped when it's absent, so the test task stays green without a database. GitHub Actions runs the battle tests on every push and pull request.

## Project structure

```
src/main/java/com/example/pokemonbattle/
├── PokemonBattleApplication.java      entry point, --seed / --deal branch
├── config/                            CORS and WebSocket configuration
├── web/                               WebSocket routing, health check, protocol DTOs
│   ├── GameSocketHandler.java         message routing, room lifecycle
│   ├── Player.java
│   └── RoundStart / RoundEnd / GameEnd / HpState   record DTOs
├── game/                              rooms and battle logic
│   ├── GameRoom.java / RoomRegistry.java
│   ├── Battle.java / BattleEngine.java             turn resolution and damage
│   ├── Fighter.java / TypeChart.java               in-battle Pokémon and type chart
│   └── RandomSource / ThreadLocalRandomSource      injectable randomness
├── card/                              Card / Move / CardFactory / Grade
└── persistence/                       JPA entities, repositories, PokémonSeeder

frontend/src/
├── useGameSocket.js                   WebSocket + state (reducer), connection-loss handling
├── api.js                             server URLs, sprites
├── screens/                           lobby, battle
├── components/                        card, move bar (with Struggle fallback), type chip
├── three/                             3D stage, per-type effects
└── styles.css                         design tokens, dark theme, reduced-motion support
```

## Roadmap

### Shipped

- [x] **Status conditions** — poison, burn, and paralysis from PokéAPI ailment data
- [x] **Drain moves** — HP absorption via `meta.drain`
- [x] **Critical hits** — 1/16 chance for 1.5× damage
- [x] **Real-time PP** — reflected on screen the instant a move is used
- [x] **Disconnect handling** — the remaining player is notified and the game ends on opponent drop; the client also surfaces a connection-loss screen if its own socket drops
- [x] **Deterministic battle tests + CI** — injectable `RandomSource`, `BattleEngineTest`, and a GitHub Actions workflow
- [x] **Roster expansion** — grade-balanced expansion to generations 1–3 (~180 Pokémon)
- [x] **Cold-start mitigation** — bottleneck traced to framework init (not the DB); worked around with a 10-minute keep-alive ping
- [x] **Package restructure** — split a flat package into `config` / `web` / `game` / `card` / `persistence`

### Planned

- [ ] **Switching** — swap Pokémon mid-round to deepen the matchup game (the next major feature; needs a design pass first)

### Deferred

- [ ] **Reconnection** — rejoin an in-progress room after a refresh via a player token (conflicts with current drop handling; low payoff in a single short match)
- [ ] **JPA → JdbcClient** — would cut ~17s of startup, but the keep-alive ping made cold start a non-issue

### Out of scope

- ~~Stat stages~~ — low payoff in a single short match
- ~~Quick match / match history / spectator mode~~ — set aside to keep the project focused on the core 1v1 loop
