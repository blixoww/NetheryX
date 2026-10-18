"""Documentation complete de NetheryX (docs/DOCUMENTATION.md), generee a partir des donnees du mod :
pages du Codex, recettes, butins, apparitions et options de configuration."""
import json
import re
from collections import defaultdict

from .common import ASSETS, DATA, MOD_ID
from . import book
from .content import BIOMES, BLOCKS, BLOCKS_WITHOUT_ITEM, ENTITIES, GENERATED_ITEMS, HANDHELD_ITEMS, SPAWN_EGGS

ROOT = ASSETS.parents[4]
OUT = ROOT / "docs" / "DOCUMENTATION.md"
CONFIG = ROOT / "src" / "main" / "java" / "com" / "netheryx" / "config" / "NetheryXConfig.java"

# Biomes conserves dans le registre pour les anciens mondes mais qui ne sont plus generes
LEGACY_BIOMES = {"onyx_depths", "soulfire_marsh", "magma_rift"}

VANILLA_FR = {
    "netherite_ingot": "Lingot de netherite", "nether_star": "Étoile du Nether", "golden_apple": "Pomme dorée",
    "crying_obsidian": "Obsidienne pleureuse", "obsidian": "Obsidienne", "soul_lantern": "Lanterne des âmes", "stick": "Bâton",
    "blaze_rod": "Bâton de blaze", "blaze_powder": "Poudre de blaze", "magma_cream": "Crème de magma", "glass": "Verre",
    "glass_bottle": "Fiole", "iron_ingot": "Lingot de fer", "gold_ingot": "Lingot d'or", "gold_nugget": "Pépite d'or",
    "quartz": "Quartz du Nether", "netherrack": "Netherrack", "blackstone": "Pierre noire", "basalt": "Basalte",
    "leather": "Cuir", "string": "Ficelle", "book": "Livre", "bowl": "Bol", "bucket": "Seau", "chest": "Coffre",
    "diamond": "Diamant", "bone": "Os", "bone_meal": "Poudre d'os", "soul_sand": "Sable des âmes", "soul_soil": "Terre des âmes",
    "glowstone_dust": "Poudre lumineuse", "redstone": "Redstone", "phantom_membrane": "Membrane de phantom", "lava_bucket": "Seau de lave",
    "smithing_table": "Table de forgeron", "furnace": "Fourneau", "anvil": "Enclume", "nether_wart": "Verrue du Nether",
    "crimson_planks": "Planches carmin", "warped_planks": "Planches biscornues", "ghast_tear": "Larme de ghast",
    "fire_charge": "Boule de feu", "netherite_scrap": "Fragment de netherite", "ancient_debris": "Débris antiques",
    "shroomlight": "Champilampe", "lantern": "Lanterne", "chain": "Chaîne", "iron_nugget": "Pépite de fer", "sugar": "Sucre",
    "egg": "Œuf", "wheat": "Blé", "porkchop": "Côtelette de porc crue", "beef": "Bœuf cru", "coal": "Charbon",
    "flint_and_steel": "Briquet", "saddle": "Selle", "compass": "Boussole", "ender_pearl": "Perle de l'Ender",
    "polished_blackstone": "Pierre noire polie", "polished_blackstone_bricks": "Briques de pierre noire polie",
    "totem_of_undying": "Totem d'immortalité", "enchanted_book": "Livre enchanté", "wither_skeleton_skull": "Crâne de wither squelette",
    "skeleton_skull": "Crâne de squelette", "rotten_flesh": "Chair putréfiée", "emerald": "Émeraude", "gunpowder": "Poudre à canon",
    "basalt_deltas": "Deltas de basalte", "soul_sand_valley": "Vallée des âmes", "crimson_forest": "Forêt carmin",
    "warped_forest": "Forêt biscornue", "nether_wastes": "Désolation du Nether",
    "awkward_potion": "Potion étrange", "potion": "Potion", "honey_bottle": "Fiole de miel", "milk_bucket": "Seau de lait",
}


def load_lang():
    return json.loads((ASSETS / "lang" / "fr_fr.json").read_text(encoding="utf-8"))


LANG = {}


def name_of(ident):
    ident = ident.lstrip("#")
    ns, _, path = ident.partition(":") if ":" in ident else (MOD_ID, "", ident)
    if ns == MOD_ID:
        for prefix in ("item", "block", "entity", "biome"):
            key = f"{prefix}.{MOD_ID}.{path}"
            if key in LANG:
                return LANG[key]
        return path.replace("_", " ")
    return VANILLA_FR.get(path, path.replace("_", " ").capitalize())


def clean(text):
    """Convertit le balisage Patchouli en Markdown."""
    text = text.replace("$(br2)", "\n\n").replace("$(br)", "\n").replace("$(li)", "\n- ")
    text = re.sub(r"\$\(l\)(.*?)\$\(\)", r"**\1**", text)
    text = re.sub(r"\$\(item\)(.*?)\$\(\)", r"*\1*", text)
    text = re.sub(r"\$\([^)]*\)", "", text)
    return text.strip()


def ingredient(value):
    if isinstance(value, list):
        return " ou ".join(ingredient(v) for v in value)
    if "tag" in value:
        return f"n'importe quel(le) {value['tag'].split(':')[1].replace('_', ' ')}"
    return name_of(value["item"])


def recipe_text(rid):
    path = DATA / MOD_ID / "recipes" / f"{rid}.json"
    if not path.exists():
        return None
    r = json.loads(path.read_text(encoding="utf-8"))
    kind = r["type"].split(":")[1]
    result = r.get("result", {})
    out = name_of(result.get("item", "")) if isinstance(result, dict) else name_of(result)
    count = result.get("count", 1) if isinstance(result, dict) else r.get("count", 1)
    suffix = f" ×{count}" if count and count > 1 else ""
    if kind == "crafting_shaped":
        legend = ", ".join(f"`{k}` = {ingredient(v)}" for k, v in r["key"].items())
        grid = " / ".join(f"`{row.replace(' ', '·')}`" for row in r["pattern"])
        return f"Établi : {grid} ({legend}) → **{out}**{suffix}"
    if kind == "shapeless_book_recipe":
        return f"Établi (sans forme) : {' + '.join(ingredient(i) for i in r['ingredients'])} → **Codex NetheryX**"
    if kind == "crafting_shapeless":
        return f"Établi (sans forme) : {' + '.join(ingredient(i) for i in r['ingredients'])} → **{out}**{suffix}"
    if kind in ("smelting", "blasting", "smoking", "campfire_cooking"):
        label = {"smelting": "Four", "blasting": "Haut fourneau", "smoking": "Fumoir", "campfire_cooking": "Feu de camp"}[kind]
        return f"{label} : {ingredient(r['ingredient'])} → **{out}**"
    if kind == "stonecutting":
        return f"Tailleur de pierre : {ingredient(r['ingredient'])} → **{out}**{suffix}"
    if kind == "smithing_transform":
        return (f"Table de forgeron : {ingredient(r['template'])} + {ingredient(r['base'])} + {ingredient(r['addition'])} "
                f"→ **{out}**")
    return f"Recette spéciale ({kind}) → **{out}**"


def collect_recipes():
    by_output = defaultdict(list)
    for path in sorted((DATA / MOD_ID / "recipes").glob("*.json")):
        r = json.loads(path.read_text(encoding="utf-8"))
        result = r.get("result")
        item = result.get("item") if isinstance(result, dict) else result
        if item and item.startswith(f"{MOD_ID}:"):
            text = recipe_text(path.stem)
            if text:
                by_output[item.split(":")[1]].append(text)
    return by_output


def collect_loot():
    sources = defaultdict(set)
    base = DATA / MOD_ID / "loot_tables"
    for path in sorted(base.rglob("*.json")):
        kind = path.parent.name
        if kind == "blocks":
            continue
        label = {"entities": "butin de", "chests": "coffres :", "gameplay": "pêche à la nasse"}.get(kind, kind)
        what = name_of(path.stem) if kind == "entities" else path.stem.replace("_", " ")
        text = path.read_text(encoding="utf-8")
        for item in set(re.findall(rf'"{MOD_ID}:([a-z0-9_]+)"', text)):
            sources[item].add(f"{label} {what}" if kind != "gameplay" else label)
    return sources


def collect_spawns():
    spawns = defaultdict(set)
    for path in sorted((DATA / MOD_ID / "worldgen" / "biome").glob("*.json")):
        if path.stem in LEGACY_BIOMES:
            continue
        biome = json.loads(path.read_text(encoding="utf-8"))
        for entries in biome["spawners"].values():
            for e in entries:
                spawns[e["type"]].add(name_of(f"{MOD_ID}:{path.stem}"))
    for path in sorted((DATA / MOD_ID / "forge" / "biome_modifier").glob("*.json")):
        mod = json.loads(path.read_text(encoding="utf-8"))
        if mod.get("type") != "forge:add_spawns":
            continue
        biomes = mod["biomes"] if isinstance(mod["biomes"], list) else [mod["biomes"]]
        labels = []
        for b in biomes:
            if b.startswith("#"):
                labels.append("tout le Nether")
            elif b.split(":")[1] not in LEGACY_BIOMES:
                labels.append(name_of(b))
        for e in mod["spawners"]:
            spawns[e["type"]].update(labels)
    return spawns


def config_options():
    if not CONFIG.exists():
        return []
    src = CONFIG.read_text(encoding="utf-8")
    pattern = r'\.comment\("([^"]*)"\)\s*\.(define|defineInRange)\("([A-Za-z0-9]+)",\s*([^,)]+)'
    return [(key, default.strip(), comment) for comment, _, key, default in re.findall(pattern, src)]


def render_page(page, lines):
    kind = page["type"].split(":")[1]
    text = page.get("text")
    fr = text["fr_fr"] if isinstance(text, dict) else text
    if kind == "text":
        if page.get("title"):
            lines.append(f"**{page['title']['fr_fr'] if isinstance(page['title'], dict) else page['title']}**\n")
        lines.append(clean(fr) + "\n")
    elif kind == "spotlight":
        lines.append(f"- **{name_of(page['item'])}** : {clean(fr)}")
    elif kind == "entity":
        lines.append(f"- **{name_of(page['entity'])}** : {clean(fr)}")
    elif kind in ("crafting", "smelting", "stonecutting"):
        for key in ("recipe", "recipe2"):
            if key in page:
                rt = recipe_text(page[key].split(":")[1])
                if rt:
                    lines.append(f"- {rt}")
        if fr:
            lines.append(f"  - {clean(fr)}")
    lines.append("")


def generate():
    LANG.clear()
    LANG.update(load_lang())
    recipes = collect_recipes()
    loot = collect_loot()
    spawns = collect_spawns()
    lines = ["# NetheryX — Documentation complète", "",
             "> Ce fichier est généré automatiquement par `tools/generate_resources.py` à partir des données du mod "
             "(Codex, recettes, butins, apparitions). Ne le modifiez pas à la main.", "",
             "NetheryX transforme le Nether de Minecraft 1.20.1 (Forge) en une dimension à explorer : nouveaux biomes organiques, "
             "créatures, donjons à plusieurs niveaux avec boss, boss final, équipement en onyx, cultures et objets utiles.", "",
             "## Sommaire", ""]
    sections = [(cid, name["fr_fr"]) for cid, _icon, name, _desc in book.CATEGORIES]
    for cid, title in sections:
        lines.append(f"- [{title}](#{cid})")
    lines += ["- [Catalogue des blocs et objets](#catalogue)", "- [Créatures : apparitions et butin](#creatures-detail)",
              "- [Configuration](#configuration)", ""]
    for cid, title in sections:
        lines += [f'<a id="{cid}"></a>', f"## {title}", ""]
        for entry in book.ENTRIES:
            if entry["category"] != cid:
                continue
            lines += [f"### {entry['name']['fr_fr']}", ""]
            for page in entry["pages"]:
                render_page(page, lines)

    # Catalogue : chaque bloc et objet, avec sa recette et ses sources de butin
    lines += ['<a id="catalogue"></a>', "## Catalogue des blocs et objets", "",
              "Chaque bloc et objet du mod, avec la façon de l'obtenir.", ""]
    block_items = [b[0] for b in BLOCKS if b[0] not in BLOCKS_WITHOUT_ITEM]
    groups = [("Blocs", block_items), ("Objets", sorted(set(GENERATED_ITEMS + HANDHELD_ITEMS + ["onyx_bow"]))),
              ("Œufs d'apparition (créatif)", SPAWN_EGGS)]
    for title, names in groups:
        lines += [f"### {title}", ""]
        for item in names:
            lines.append(f"- **{name_of(f'{MOD_ID}:{item}')}** (`{MOD_ID}:{item}`)")
            for rt in recipes.get(item, []):
                lines.append(f"  - {rt}")
            if item in loot:
                lines.append(f"  - Trouvé dans : {', '.join(sorted(loot[item]))}")
            if title == "Blocs" and not recipes.get(item) and item not in loot:
                lines.append("  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.")
        lines.append("")

    lines += ['<a id="creatures-detail"></a>', "## Créatures : apparitions et butin", ""]
    for ent in ENTITIES:
        if ent in ("sulfur_bomb", "obsidian_boat"):
            continue
        ident = f"{MOD_ID}:{ent}"
        lines.append(f"### {name_of(ident)}")
        where = sorted(spawns.get(ident, []))
        lines.append(f"- Apparition : {', '.join(where) if where else 'structures, invocation ou objet spécial uniquement'}")
        table = DATA / MOD_ID / "loot_tables" / "entities" / f"{ent}.json"
        if table.exists():
            drops = sorted({name_of(i) for i in re.findall(r'"name": "([a-z0-9_:]+)"', table.read_text(encoding="utf-8"))})
            if drops:
                lines.append(f"- Butin : {', '.join(drops)}")
        lines.append("")

    lines += ['<a id="configuration"></a>', "## Configuration", "",
              "Fichier `config/netheryx-common.toml` (créé au premier lancement).", "",
              "| Option | Défaut | Description |", "|---|---|---|"]
    for key, default, comment in config_options():
        lines.append(f"| `{key}` | `{default}` | {comment} |")
    lines.append("")
    lines += ["## Biomes retirés de la génération", "",
              "Les biomes *Profondeurs d'onyx*, *Marais des âmes* et *Faille de magma*, trop proches des biomes vanilla, ne sont "
              "plus générés. Ils restent enregistrés pour que les mondes existants se chargent sans erreur ; leurs rôles sont repris "
              "par la Forêt pétrifiée d'onyx, la Vallée des âmes vanilla et le Lac de verre en fusion.", ""]
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(lines), encoding="utf-8")
