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
JAVA = ROOT / "src/main/java/com/netheryx"


def names_in(*files):
    found = set()
    for f in files:
        found |= set(re.findall(r'register\("([a-z_]+)"', (JAVA / f).read_text()))
    return found


particles = names_in("registry/ModParticles.java")
reg -= particles
# Enregistrements sans objet (entites, enchantements, effets, potions, features, recettes, butins...)
entities = names_in("registry/ModEntities.java")
technical = names_in("registry/ModFeatures.java", "registry/ModRecipes.java", "registry/ModLootModifiers.java") | {"netheryx", "nether_floor"}
effects = names_in("registry/ModEffects.java", "registry/ModEnchantments.java")
block_only = set(re.findall(r'BLOCKS\.register\("([a-z_]+)"', java))
item_registered = set(re.findall(r'ITEMS\.register\(', java))
reg.discard("potted_")
pots = {f"potted_{p}" for p in re.findall(r'pot\("([a-z_]+)"', java)}
reg |= pots
non_items = entities | technical | effects | block_only | pots
items = (reg - non_items) | {"sulfur_bomb", "obsidian_boat"} | (set(re.findall(r'ModItems\.[A-Z_]+ = register\("([a-z_]+)"', java)))
items |= set(re.findall(r'RegistryObject<Item> [A-Z_]+ = (?:register|armor)\("([a-z_]+)"', java))
untranslated = technical | {f"long_{p}" for p in effects}
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
    for n in reg - untranslated:
        if not any(k.endswith("." + n) for k in keys):
            errors.append(f"traduction {lang} manquante {n}")
for n in particles:
    pj = A / "particles" / (n + ".json")
    if not pj.exists():
        errors.append(f"particule sans definition {n}")
    else:
        for t in json.loads(pj.read_text())["textures"]:
            if not (A / "textures/particle" / (t.split(":")[1] + ".png")).exists():
                errors.append(f"texture de particule manquante {t}")
recipes = {p.stem for p in (D / "netheryx/recipes").glob("*.json")}
known = reg | particles | recipes | {p.stem for p in D.rglob("*.json")} | {"ember_stems", "withered_stems"}
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
