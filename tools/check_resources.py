#!/usr/bin/env python3
"""Verifie la coherence des ressources generees avec le code Java (references, modeles, traductions, livre)."""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
R = ROOT / "src/main/resources"
A = R / "assets/netheryx"
D = R / "data"
java = "".join(p.read_text() for p in (ROOT / "src/main/java").rglob("*.java"))
reg = set(re.findall(r'register\("([a-z_]+)"', java))
non_items = {"blaze_pepper_crop", "soul_tuber_crop", "fire_berry_bush", "crusted_lava", "netheryx", "add_item", "nether_floor",
             "ember_stalker", "ash_wraith", "onyx_sentinel", "magma_crab", "sulfur_bomb_entity", "soul_moth",
             "withered_knight", "basalt_beetle", "infernal_warlock", "withered_champion", "ember_cow", "cinder_hen",
             "fire_ghast", "onyx_cube", "cinder_wheat_crop", "potted_"}
reg.discard("potted_")
reg |= {f"potted_{p}" for p in re.findall(r'pot\("([a-z_]+)"', java)}
non_items |= {f"potted_{p}" for p in re.findall(r'pot\("([a-z_]+)"', java)}
items = (reg - non_items) | {"sulfur_bomb"}
errors = []

for f in A.rglob("models/**/*.json"):
    j = json.loads(f.read_text())
    for t in j.get("textures", {}).values():
        if t.startswith("netheryx:") and not (A / "textures" / (t.split(":")[1] + ".png")).exists():
            errors.append(f"texture manquante {t} ({f.name})")
for f in A.rglob("blockstates/*.json"):
    for m in re.findall(r'"netheryx:(block/[a-z_0-9]+)"', f.read_text()):
        if not (A / "models" / (m + ".json")).exists():
            errors.append(f"modele manquant {m}")
for n in items:
    if not (A / "models/item" / (n + ".json")).exists():
        errors.append(f"modele d'objet manquant {n}")
for lang in ("en_us", "fr_fr"):
    keys = json.loads((A / "lang" / f"{lang}.json").read_text())
    for n in reg - {"add_item", "nether_floor", "netheryx"}:
        if not any(k.endswith("." + n) for k in keys):
            errors.append(f"traduction {lang} manquante {n}")
recipes = {p.stem for p in (D / "netheryx/recipes").glob("*.json")}
known = reg | recipes | {p.stem for p in D.rglob("*.json")} | {"ember_stems", "withered_stems"}
for f in D.rglob("*.json"):
    for m in re.findall(r'"netheryx:([a-z_0-9]+)"', f.read_text()):
        if m not in known:
            errors.append(f"reference inconnue netheryx:{m} dans {f.relative_to(R)}")
for f in (A / "patchouli_books").rglob("entries/**/*.json"):
    j = json.loads(f.read_text())
    icon = j["icon"]
    if icon.startswith("netheryx:") and icon.split(":", 1)[1] not in items:
        errors.append(f"icone inconnue {icon} ({f.name})")
    for page in j["pages"]:
        for key in ("recipe", "recipe2"):
            if key in page and page[key].split(":", 1)[1] not in recipes:
                errors.append(f"recette inconnue {page[key]} ({f.name})")
        for key in ("item", "entity"):
            if key in page and page[key].split(":", 1)[1] not in reg | items:
                errors.append(f"{key} inconnu {page[key]} ({f.name})")
en = sorted(p.relative_to(A / "patchouli_books/codex/en_us") for p in (A / "patchouli_books/codex/en_us").rglob("*.json"))
fr = sorted(p.relative_to(A / "patchouli_books/codex/fr_fr") for p in (A / "patchouli_books/codex/fr_fr").rglob("*.json"))
if en != fr:
    errors.append("le livre anglais et le livre francais n'ont pas les memes pages")

print("\n".join(sorted(set(errors))) or f"OK : {len(reg)} enregistrements, {len(recipes)} recettes, {len(en)} fichiers de livre par langue")
sys.exit(1 if errors else 0)
