# Sorcery TCG Tracker

Application Android compagnon pour **Sorcery: Contested Realm**. Elle réunit en un seul endroit le catalogue de cartes, le suivi de votre collection et un compteur de partie, pour jouer et gérer vos cartes sans quitter la table.

> Projet personnel — Kotlin + Jetpack Compose, architecture Clean multi-features.

---

## Fonctionnalités

### Cartes
Navigateur du catalogue complet (~1 100 cartes, plus de 3 000 images embarquées dans l'app, disponibles hors-ligne).
- Recherche par nom et **filtres** : élément (Air, Terre, Feu, Eau), type, rareté, extension, et appartenance (toutes / possédées / manquantes / surplus).
- Vue détail d'une carte : texte de règles, coût, attaque/défense/vie, seuils d'éléments, sous-types et impressions (printings).

### Collection
Suivi de ce que vous possé-dez, avec import depuis Curiosa.
- **Import CSV Curiosa** avec choix du mode : *Remplacer* ou *Fusionner* votre collection existante. Rapport d'import détaillé (correspondances, lignes non reconnues, ambiguës, invalides).
- Édition des quantités par impression et par finition (finish).
- Trois vues de suivi : **Complétion** (avancement par extension), **Surplus** (copies au-delà du playset autorisé par la rareté) et **Manquants**.
- Filtres rapides et avancés, partagés avec le navigateur de cartes.

### Suivi de partie
Compteur pour vos parties en direct.
- Compteurs de vie (départ à 20, la vie de base de l'avatar en Sorcery).
- Suivi des ressources et des affinités par joueur, gestion de plusieurs joueurs et du statut des avatars.
- Nouvelle partie configurable ; l'état de partie est persisté.

### Decks
Constructeur de decks (en cours d'intégration).

---

## Stack technique

| Domaine | Choix |
|---|---|
| Langage / UI | Kotlin, Jetpack Compose (Material 3) |
| Architecture | Clean Architecture par feature (`data` / `domain` / `ui`) |
| Injection | Koin |
| Navigation | Navigation Compose (drawer) |
| Persistance | Room, DataStore/sérialisation d'état de partie |
| Sérialisation | kotlinx.serialization |
| Images | Coil 3 |

- **Package** : `com.hayse.sorcery`
- **minSdk** 29 · **targetSdk** 36 · **compileSdk** 37

---

## Structure du projet

```
app/src/main/java/com/hayse/sorcery/
├── core/                 # navigation, thème, composables et modèles partagés
│   ├── navigation/       # AppNavHost, AppScaffold, Destinations
│   ├── ui/theme/         # couleurs, typographie, formes, dimensions
│   └── shared/model/     # Element, Rarity, Ownership
├── di/                   # modules Koin
└── feature/
    ├── cards/            # catalogue et détail des cartes
    ├── collection/       # collection + import Curiosa
    └── game_tracker/     # suivi de partie
```

Chaque feature suit le même découpage `data` / `domain` / `ui` (+ `di`).

---

## Données

Le catalogue et les images sont embarqués dans `app/src/main/assets/cards/` :
- `cards.json` — données des cartes,
- `images/` — visuels des impressions.