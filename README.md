# ⚔️ Pokémon Card Battle

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![Three.js](https://img.shields.io/badge/Three.js-r185-000000?logo=threedotjs&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-4169E1?logo=postgresql&logoColor=white)
![Render](https://img.shields.io/badge/Deploy-Render-46E3B7?logo=render&logoColor=white)

A real-time, 1v1 turn-based Pokémon card battle game. Each player is dealt 6 random Pokémon and fights across 6 rounds — where type matchups can flip raw stats on their head.

**▶ Play: https://pokemon-battle-web.onrender.com**

> Hosted on a free tier. The backend is kept awake during the day via a scheduled ping, but the first request during off-hours may take ~1 minute to spin up.

## What the game is

Two players join the same room and each receive 6 Pokémon cards. Every round, both play one card, and the two Pokémon fight it out turn by turn. Whoever wins the most of the 6 rounds takes the match.

Cards are graded by their base-stat totals, and each grade has its own draw probability. Moves are drawn only from what each Pokémon can actually learn — pulled from PokéAPI and stored in a relational table.

## Tech stack

| Layer | Technology |
| --- | --- |
| Backend | Spring Boot 4, Java 21, WebSocket, JDBC |
| Frontend | React, Vite, Three.js |
| Database | PostgreSQL (Neon) |
| Deployment | Render (backend + static hosting) |
| Data source | PokéAPI |

## Design decisions worth noting

**Hidden information is filtered on the server, not the client.** Data that must stay secret — like an opponent's remaining PP — is never sent to the client at all. Hiding it in the UI would be trivial to bypass with dev tools, so the serialization step itself produces different payloads for "mine" versus "theirs."

**External APIs are called only at seed time, never at runtime.** A single PokéAPI call has ~0.8s of round-trip latency, which is unusable mid-game. All Pokémon and move data is pre-loaded into the database once, and the game reads from the DB in milliseconds.

**Battle logic is a framework-agnostic pure function.** The damage calculation (`BattleEngine`) knows nothing about Spring, the database, or WebSocket. Even randomness — the damage roll and critical-hit check — is injected from the outside, so the whole thing is unit-testable without a running server.

**The server is the single source of truth.** Who strikes first, how much damage lands, how much HP remains — the server decides all of it and pushes the result. The client only renders what it receives; it never recomputes HP or PP on its own.

**The free tier is a first-class design constraint.** The server sleeps after 15 minutes idle, so the frontend wakes it ahead of time, and JVM options and connection-pool sizes are tuned for a 512 MB / single-CPU instance. A scheduled ping keeps it awake during active hours.

## Game rules

- **Grades** — Pokémon are split into S/A/B/C by base-stat total, and cards are dealt by grade-weighted probability.
- **Damage** — an approximation of the original formula, with a same-type attack bonus (1.5×), type effectiveness, and a 1/16 critical-hit chance (1.5×).
- **Turn order** — the faster Pokémon moves first; a knockout denies the opponent their turn.
- **PP** — running out forces Struggle (a fixed attack that ignores type and effectiveness).
- **Draw prevention** — past 30 turns, the winner is decided by remaining HP ratio.
- **Drain moves** — absorbing moves heal the user for a fraction of the damage dealt.

## Running locally

**Backend**

```bash
./gradlew bootRun
```

Requires Neon connection details as environment variables (`DATABASE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

**Seeding** — run once to pull data from PokéAPI into the database.

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

## Project structure

```
src/main/java/com/example/pokemonbattle/
├── PokemonBattleApplication.java   entry point, --seed branch
├── GameSocketHandler.java          WebSocket message routing
├── GameRoom.java / Player.java     room and game state
├── RoomRegistry.java               room creation / lookup
├── CardFactory.java                deal 6 cards from the DB
├── Battle.java / BattleEngine.java  turn resolution and damage
├── Fighter.java / TypeChart.java   in-battle Pokémon and type chart
├── PokemonSeeder.java              PokéAPI seeding
└── *Entity.java / *Repository.java persistence layer

frontend/src/
├── useGameSocket.js                WebSocket + state (reducer)
├── api.js                          server URLs, sprites
├── screens/                        lobby, battle
├── components/                     card, move bar, type chip
└── three/                          3D stage, per-type effects
```

## Roadmap

Grouped into three axes: polish, gameplay, and engineering.

### Polish

- [ ] **Real-time PP** — reflect PP on screen the instant a move is used ✅ *done*
- [ ] **Disconnect handling** — notify the remaining player and end the game on opponent drop ✅ *done*
- [ ] **Reconnection** — rejoin an in-progress room after a refresh via a player token

### Gameplay

- [x] **Drain moves** — HP absorption implemented via `meta.drain`, previously-cut moves restored ✅ *done*
- [x] **Critical hits** — 1/16 chance for 1.5× damage ✅ *done*
- [ ] **Status conditions** — paralysis, burn, and poison for deeper strategy
- [ ] **Switching** — swap Pokémon mid-round to deepen the matchup game
- [ ] ~~Stat stages~~ — intentionally skipped: low payoff in a single short match

### Engineering

- [x] **Cold-start mitigation** — measured the bottleneck (framework init, not DB), confirmed config tuning had little effect, and worked around it with a 10-minute keep-alive ping ✅ *done*
- [ ] **JPA removal** — replace Hibernate with JdbcClient to cut ~17s of startup (deferred)
- [ ] **Quick match** — auto-matchmaking without room codes
- [ ] **Match history** — persist results and add a records page
- [ ] **Spectator mode** — read-only connection to watch a live match
