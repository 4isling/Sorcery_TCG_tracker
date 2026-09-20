# Sorcery TCG Tracker

A companion Android app for **Sorcery: Contested Realm**. It brings the full card catalog, collection tracking, deck building, a live game counter, and peer-to-peer trading together in one place — so you can play and manage your cards without ever leaving the table.

> Personal project — Kotlin + Jetpack Compose, multi-feature Clean Architecture.

---

## Features

### Cards
Browser for the complete catalog (~1,100 cards, 3,000+ images bundled into the app and available offline).
- Search by name plus **filters**: element (Air, Earth, Fire, Water), type, rarity, set, and ownership (all / owned / missing / surplus).
- Cards grouped by set › element › type › rarity for fast scanning.
- Card detail view: rules text, cost, attack/defence/life, element thresholds, subtypes, and printings.

### Collection
Track what you own, with import from Curiosa.
- **Curiosa CSV import** with a *Replace* or *Merge* mode, plus a detailed import report (matches, unrecognised / ambiguous / invalid rows).
- Edit quantities per printing and per finish (foils tracked individually).
- Three tracking views: **Completion** (progress per set), **Surplus** (copies beyond the playset allowed by rarity), and **Missing**.
- Quick and advanced filters, shared with the card browser.

### Decks
Multi-deck builder with data-driven format validation.
- Build and edit multiple decks; owned vs. missing cards surfaced from your collection.
- **Format validation** (Constructed / Poorcery) driven by an extensible, data-defined ruleset — adding a new format is a data change, not new UI code.

### Game Tracker
A counter for your live matches.
- Life counters (starting at 20, an avatar's base life in Sorcery), resources and per-player affinities, avatar status, and multi-player support.
- **Optional match timer**: independent global (default 50 min) and per-turn (default 5 min) clocks, a configurable end-of-time action (sudden death with N extra turns, verdict, draw, or keep going), and tournament-rule verdict resolution.
- Configurable new game; game state and timer are persisted separately, so undo never rewinds the clock.
- Game history.

### Social / Trading
Find and complete trades with nearby players — no server required.
- Automatic **trade suggestions** from your surplus against another player's wanted list (and vice-versa); mark cards as "wanted" straight from the card views.
- Peer-to-peer exchange over **Nearby Connections**, **NFC** (Host Card Emulation), and **QR codes**, with saved trades and trade rooms.

---

## Tech stack

| Area | Choice |
|---|---|
| Language / UI | Kotlin, Jetpack Compose (Material 3) |
| Architecture | Clean Architecture per feature (`data` / `domain` / `ui`) |
| Dependency injection | Koin |
| Navigation | Navigation Compose (drawer on phone, rail on tablet) |
| Persistence | Room, DataStore + kotlinx.serialization state stores |
| Serialization | kotlinx.serialization |
| Images | Coil 3 |
| P2P | Nearby Connections, NFC/HCE, QR |

- **Package**: `com.hayse.sorcery`
- **minSdk** 29 · **targetSdk** 36 · **compileSdk** 37

---

## Project structure

```
app/src/main/java/com/hayse/sorcery/
├── core/                 # navigation, theme, shared composables and models
│   ├── navigation/       # AppNavHost, AppScaffold, Destinations
│   ├── ui/theme/         # colors, typography, shapes, dimensions
│   └── shared/model/     # Element, Rarity, Ownership
├── di/                   # Koin modules
└── feature/
    ├── cards/            # catalog and card detail
    ├── collection/       # collection + Curiosa import
    ├── deck/             # deck builder
    ├── deckformat/       # data-driven format validation
    ├── game_tracker/     # live game counter + timer
    ├── social/           # P2P trading (Nearby / NFC / QR)
    ├── home/             # dashboard
    └── settings/         # app settings
```

Each feature follows the same `data` / `domain` / `ui` split (+ `di`).

---

## Data

The catalog and images are bundled in `app/src/main/assets/cards/`:
- `cards.json` — card data,
- `images/` — printing artwork.

---

## Screenshots

<!-- TODO: Add your own screenshots here.
Examples (replace with real paths):
- ![Card browser](screenshots/01_cards.png) 
- ![Deck builder](screenshots/02_deck.png) 
- ![Game counter](screenshots/03_tracker.png)
-->

---

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 37 (compileSdk)
- minSdk 29

### Build & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/4isling/Sorcery_TCG_tracker.git
   cd Sorcery_TCG_tracker
   ```

2. **Build the app:**
   ```bash
   ./gradlew build
   ```

3. **Run on a device or emulator:**
   ```bash
   ./gradlew installDebug
   ```

### Releases

APKs for manual installation are available under [Releases](https://github.com/4isling/Sorcery_TCG_tracker/releases).

---

## About the author

Hi, I'm **Hayse** — an Android developer based in France. I build polished, offline-first apps for the hobbies I care about. Sorcery TCG Tracker is a solo project where I own the whole stack, from data modelling to UI.

**Things this project demonstrates:**
- **Modern Android stack** — 100% Kotlin and Jetpack Compose (Material 3), adaptive layouts for phone and tablet.
- **Clean, testable architecture** — feature-based Clean Architecture with Koin DI, pure domain logic decoupled from UI (e.g. the game timer is event-sourced and reconciled against the wall clock so undo never rewinds it).
- **Going past the happy path** — CSV import with real reconciliation reporting, data-driven format rules, and serverless peer-to-peer trading over Nearby Connections, NFC (HCE) and QR codes.

**Currently exploring:** Kotlin Multiplatform Mobile, testing strategies for complex state machines, and offline-first data synchronization patterns.

**Get in touch:**
- GitHub: [@4isling](https://github.com/4isling)
- "LATER"

---

## License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for details.

---

## Contributing

This is a personal hobby project, but I'm open to feedback and contributions. If you find a bug or have an idea, feel free to:
- Open an [issue](https://github.com/4isling/Sorcery_TCG_tracker/issues)
- Submit a [pull request](https://github.com/4isling/Sorcery_TCG_tracker/pulls)

Please keep discussions respectful and focused on the app's goals.