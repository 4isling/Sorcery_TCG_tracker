#!/usr/bin/env python3
"""Génère le catalogue de cartes offline pour SorceryApplication.

Récupère les cartes depuis l'API officielle Sorcery, les aplatit dans le schéma
attendu par `CardCatalogSeeder` (Room), et écrit :
  - app/src/main/assets/cards/cards.json  (données)
  - tools/expected_images.txt             (liste des slugs -> images WebP à fournir)

Les images ne sont PAS téléchargées ici : télécharge/compresse-les toi-même en WebP
et place-les dans app/src/main/assets/cards/images/<slug>.webp
(le nom de fichier doit correspondre EXACTEMENT au slug).

Usage :
    python3 tools/fetch_cards.py

Stdlib uniquement, aucune dépendance externe.
"""
from __future__ import annotations

import json
import os
import sys
import urllib.request

API_URL = "https://api.sorcerytcg.com/api/cards"

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS_DIR = os.path.join(ROOT, "app", "src", "main", "assets", "cards")
CARDS_JSON = os.path.join(ASSETS_DIR, "cards.json")
IMAGES_LIST = os.path.join(os.path.dirname(os.path.abspath(__file__)), "expected_images.txt")


def fetch(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": "SorceryApp/1.0 (offline catalog build)"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def flatten(raw_cards: list) -> list:
    cards = []
    for c in raw_cards:
        guardian = c.get("guardian") or {}
        thresholds = guardian.get("thresholds") or {}
        printings = []
        for s in c.get("sets", []) or []:
            set_name = s.get("name", "")
            released_at = s.get("releasedAt", "") or ""
            for v in s.get("variants", []) or []:
                slug = v.get("slug")
                if not slug:
                    continue
                printings.append({
                    "slug": slug,
                    "setName": set_name,
                    "releasedAt": released_at,
                    "finish": v.get("finish", "") or "",
                    "product": v.get("product", "") or "",
                    "artist": v.get("artist", "") or "",
                    "flavorText": v.get("flavorText", "") or "",
                    "typeText": v.get("typeText", "") or "",
                })
        cards.append({
            "name": c.get("name", ""),
            "type": guardian.get("type", "") or "",
            "rarity": guardian.get("rarity", "") or "",
            "rulesText": guardian.get("rulesText", "") or "",
            "cost": guardian.get("cost"),
            "attack": guardian.get("attack"),
            "defence": guardian.get("defence"),
            "life": guardian.get("life"),
            "thresholds": {
                "air": thresholds.get("air", 0) or 0,
                "earth": thresholds.get("earth", 0) or 0,
                "fire": thresholds.get("fire", 0) or 0,
                "water": thresholds.get("water", 0) or 0,
            },
            "elements": c.get("elements", "") or "",
            "subTypes": c.get("subTypes", "") or "",
            "printings": printings,
        })
    return cards


def main() -> int:
    print(f"Récupération : {API_URL}")
    raw = fetch(API_URL)
    if not isinstance(raw, list):
        print("Réponse inattendue (liste de cartes attendue).", file=sys.stderr)
        return 1

    cards = flatten(raw)
    os.makedirs(ASSETS_DIR, exist_ok=True)
    with open(CARDS_JSON, "w", encoding="utf-8") as f:
        json.dump({"cards": cards}, f, ensure_ascii=False, indent=2)

    slugs = sorted({p["slug"] for c in cards for p in c["printings"]})
    with open(IMAGES_LIST, "w", encoding="utf-8") as f:
        f.write("\n".join(slugs) + "\n")

    print(f"{len(cards)} cartes, {len(slugs)} impressions -> {CARDS_JSON}")
    print(f"Slugs d'images attendus -> {IMAGES_LIST}")
    print("Place les WebP dans app/src/main/assets/cards/images/<slug>.webp")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
