#!/usr/bin/env python3
"""Convertit les PNG de cartes (zips téléchargés depuis l'API) en WebP embarqués.

Lit les PNG directement depuis un ou plusieurs zips (pas d'extraction disque),
plafonne la largeur (sans jamais agrandir), encode en WebP et écrit dans
`app/src/main/assets/cards/images/<slug>.webp`.

Le `<slug>` = nom de fichier PNG sans extension (ex. `001-apprentice_wizard-b-s`),
qui doit correspondre EXACTEMENT au slug du catalogue `cards.json`.

Idempotent / résumable : saute les WebP déjà présents.

Les impressions Foil (code de finition `-f`) sont ignorées par défaut : leur artwork est
identique à la version Standard et l'app n'en affiche jamais qu'une par carte. Utiliser
`--foils` pour les régénérer. Rainbow (`-rf`) et Standard (`-s`) restent toujours convertis.

Usage :
    tools/compress_images.py [zip ...] [--width 500] [--quality 80] [--method 6] [--foils]

Nécessite Pillow avec support WebP.
"""
from __future__ import annotations

import argparse
import io
import os
import sys
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT_DIR = os.path.join(ROOT, "app", "src", "main", "assets", "cards", "images")

DEFAULT_ZIPS = [
    "/home/sam/Documents/code/sorcery/Card Images (API)-20260825T202830Z-1-001.zip",
    "/home/sam/Documents/code/sorcery/Card Images (API)-20260825T202830Z-1-002.zip",
]


FOIL_FINISH_CODE = "f"


def is_foil(slug: str) -> bool:
    """Vrai si le slug est une impression Foil (code `f`), sans confondre avec Rainbow (`rf`)."""
    return slug.rsplit("-", 1)[-1] == FOIL_FINISH_CODE


def convert(zips: list[str], width: int, quality: int, method: int, include_foils: bool) -> int:
    os.makedirs(OUT_DIR, exist_ok=True)
    done = skipped = foils = failed = 0
    total_bytes = 0
    for zp in zips:
        with zipfile.ZipFile(zp) as z:
            pngs = [n for n in z.namelist() if n.lower().endswith(".png")]
            for n in pngs:
                slug = os.path.splitext(os.path.basename(n))[0]
                if not include_foils and is_foil(slug):
                    foils += 1
                    continue
                out = os.path.join(OUT_DIR, slug + ".webp")
                if os.path.exists(out):
                    skipped += 1
                    continue
                try:
                    im = Image.open(io.BytesIO(z.read(n))).convert("RGB")
                    w, h = im.size
                    if w > width:
                        im = im.resize((width, round(h * width / w)), Image.LANCZOS)
                    im.save(out, "WEBP", quality=quality, method=method)
                    total_bytes += os.path.getsize(out)
                    done += 1
                except Exception as e:  # noqa: BLE001
                    failed += 1
                    print(f"ÉCHEC {slug}: {e}", file=sys.stderr)
                if (done + skipped) % 200 == 0:
                    print(f"... {done} convertis, {skipped} déjà présents", flush=True)
    print(
        f"Terminé : {done} convertis, {skipped} déjà présents, {foils} Foil ignorés, "
        f"{failed} échecs -> {OUT_DIR}\n"
        f"Poids des nouveaux WebP : {total_bytes / 1_048_576:.1f} Mo"
    )
    return 0 if failed == 0 else 1


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("zips", nargs="*", default=DEFAULT_ZIPS)
    p.add_argument("--width", type=int, default=500)
    p.add_argument("--quality", type=int, default=80)
    p.add_argument("--method", type=int, default=6)
    p.add_argument("--foils", action="store_true", help="Régénérer aussi les impressions Foil (ignorées par défaut).")
    a = p.parse_args()
    zips = a.zips or DEFAULT_ZIPS
    foil_note = "avec Foil" if a.foils else "sans Foil"
    print(f"Sortie : {OUT_DIR}  (largeur max {a.width}px, qualité {a.quality}, method {a.method}, {foil_note})")
    return convert(zips, a.width, a.quality, a.method, a.foils)


if __name__ == "__main__":
    raise SystemExit(main())