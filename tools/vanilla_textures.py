"""Textures NetheryX derivees des textures vanilla : memes silhouettes et meme ombrage, couleurs du mod.

Epees et outils, nourritures, materiaux, bois et briques sont recolores depuis le jar de Minecraft. Comme pour les armures
(tools/diamond_armor.py), le resultat est ecrit dans art/, que tools/generate_resources.py copie sur les textures generees.

    python3 tools/vanilla_textures.py [chemin/vers/client-extra.jar]
    python3 tools/generate_resources.py
"""
import colorsys
import sys
import zipfile
from io import BytesIO
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from netheryx_gen.textures_v131 import SETS  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "art" / "assets" / "netheryx" / "textures"
DEFAULT_JAR = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"
VANILLA = "assets/minecraft/textures/"

# Rampes : du plus sombre au plus clair
ONYX = ("#140c20", "#2a1f40", "#46346a", "#6a5094", "#9a7ad2", "#e4d2ff")
ABYSS = ("#1a060c", "#4a0e16", "#7a1a24", "#a82a32", "#d84a48", "#ff9a5a")
WITHER = ("#14161a", "#2c3036", "#474d55", "#6a7078", "#98a0a6", "#d8e8e0")
BLAZE = ("#5a2204", "#a04a0a", "#e0820e", "#ffb82a", "#ffe066", "#fff6c0")
BONE = ("#3a3226", "#6a5c46", "#9a8a6c", "#c4b694", "#e4dabc", "#f8f2dc")
ICE = ("#16283a", "#2c4c66", "#4a7a98", "#78aac4", "#b0d8e8", "#eefaff")
SOUL = ("#0c2c34", "#14505c", "#1c8090", "#34b4c4", "#7fe8f0", "#e0ffff")
MOLTEN = ("#3a0e04", "#8a2a08", "#d0520e", "#ff8a1a", "#ffc048", "#fff0a0")
SULFUR = ("#3a3208", "#6e5e10", "#a89218", "#d8c030", "#f4e460", "#fffbb0")
MAGMA_SHELL = ("#1a0a06", "#3c1608", "#6a280c", "#a0401a", "#d8702a", "#ffb060")
BASALT = ("#121216", "#26262c", "#3c3c44", "#585862", "#80808a", "#b0b0ba")
WRAITH = ("#1a1e24", "#2e3640", "#46525e", "#64727e", "#8e9eaa", "#c4f0ff")

# Cible : (source, rampe de la lame/matiere, rampe du manche ou None)
TOOLS = {
    "onyx_sword": ("diamond_sword", ONYX, BLAZE),
    "onyx_pickaxe": ("diamond_pickaxe", ONYX, BLAZE),
    "onyx_axe": ("diamond_axe", ONYX, BLAZE),
    "onyx_shovel": ("diamond_shovel", ONYX, BLAZE),
    "onyx_hoe": ("diamond_hoe", ONYX, BLAZE),
    "abyssal_blade": ("netherite_sword", ABYSS, ONYX),
    "withered_blade": ("diamond_sword", WITHER, BONE),
    "soul_scythe": ("diamond_hoe", SOUL, BONE),
}
BOWS = {"onyx_bow": "bow", "onyx_bow_pulling_0": "bow_pulling_0", "onyx_bow_pulling_1": "bow_pulling_1", "onyx_bow_pulling_2": "bow_pulling_2"}

# Matieres : tout le sprite sur une rampe
MATERIALS = {
    "onyx": ("diamond", ONYX),
    "onyx_shard": ("amethyst_shard", ONYX),
    "rime_shard": ("amethyst_shard", ICE),
    "molten_shard": ("amethyst_shard", MOLTEN),
    "abyssal_shard": ("amethyst_shard", ABYSS),
    "sulfur": ("glowstone_dust", SULFUR),
    "soul_dust": ("glowstone_dust", SOUL),
    "withered_bone": ("bone", WITHER),
    "magma_shell": ("netherite_scrap", MAGMA_SHELL),
    "basalt_carapace": ("netherite_scrap", BASALT),
    "wraith_cloth": ("leather", WRAITH),
    "wraith_essence": ("ghast_tear", WRAITH),
}

# Nourritures : on garde les details (gras, croute, tige) et on decale la teinte : (source, teinte en degres, saturation, luminosite)
FOODS = {
    "infernal_beef": ("beef", 12, 1.15, 1.0),
    "cooked_infernal_beef": ("cooked_beef", 24, 1.2, 1.0),
    "infernal_chicken": ("chicken", 18, 1.3, 1.0),
    "cooked_infernal_chicken": ("cooked_chicken", 28, 1.25, 0.95),
    "blaze_pork": ("porkchop", 28, 1.2, 1.0),
    "cooked_blaze_pork": ("cooked_porkchop", 36, 1.3, 1.0),
    "stalker_meat": ("mutton", 345, 1.1, 0.95),
    "cooked_stalker_meat": ("cooked_mutton", 18, 1.1, 0.9),
    "crab_leg": ("rabbit", 10, 1.5, 1.05),
    "cooked_crab_leg": ("cooked_rabbit", 18, 1.4, 1.0),
    "magmafish": ("cod", 22, 1.6, 1.1),
    "grilled_magmafish": ("cooked_cod", 18, 1.3, 0.95),
    "soul_tuber": ("potato", 185, 0.8, 0.95),
    "baked_soul_tuber": ("baked_potato", 190, 0.9, 1.0),
    "cinder_bread": ("bread", 16, 0.9, 0.8),
    "cinder_wheat": ("wheat", 22, 0.7, 0.85),
    "cinder_wheat_seeds": ("wheat_seeds", 22, 0.5, 0.75),
    "blaze_pepper_seeds": ("melon_seeds", 30, 1.3, 1.1),
    "blaze_pepper": ("carrot", 8, 1.2, 1.0),
    "fire_berries": ("sweet_berries", 14, 1.2, 1.1),
    "ember_fig": ("apple", 24, 1.1, 1.0),
    "withered_plum": ("apple", 280, 0.7, 0.75),
    "wartfruit": ("beetroot", 355, 1.1, 1.0),
    "onyx_apple": ("golden_apple", 268, 0.85, 0.8),
    "ember_egg": ("egg", 22, 2.5, 0.95),
    "infernal_stew": ("rabbit_stew", 12, 1.2, 0.95),
    "glowcap_stew": ("mushroom_stew", 175, 0.9, 1.05),
    "wartfruit_tart": ("pumpkin_pie", 350, 1.05, 0.95),
}

# Bois : (teinte, saturation, luminosite) applique aux bois carmin du Nether
WOODS = {"ember": (22, 1.15, 1.1), "withered": (210, 0.18, 0.85), "marrow": (42, 0.45, 1.5), "wartstalk": (358, 1.1, 0.95)}
WOOD_PARTS = {"{w}_stem": "crimson_stem", "{w}_stem_top": "crimson_stem_top", "stripped_{w}_stem": "stripped_crimson_stem",
              "stripped_{w}_stem_top": "stripped_crimson_stem_top", "{w}_planks": "crimson_planks", "{w}_door_top": "crimson_door_top",
              "{w}_door_bottom": "crimson_door_bottom", "{w}_trapdoor": "crimson_trapdoor"}
# Blocs de pierre : tout le bloc sur une rampe
STONE = {"onyx_bricks": ("deepslate_bricks", ONYX), "rimestone_bricks": ("stone_bricks", ICE)}
BLOCK_TINTS = {"ember_wart_block": ("nether_wart_block", 24, 1.1, 1.05), "withered_wart_block": ("nether_wart_block", 210, 0.12, 0.8)}


def rgb(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def luminance(r, g, b):
    return 0.299 * r + 0.587 * g + 0.114 * b


def on_ramp(t, ramp):
    t = max(0.0, min(1.0, t)) * (len(ramp) - 1)
    i = min(int(t), len(ramp) - 2)
    a, b = rgb(ramp[i]), rgb(ramp[i + 1])
    return tuple(round(x + (y - x) * (t - i)) for x, y in zip(a, b))


def levels(image, select=lambda *p: True):
    values = [luminance(*p[:3]) for p in image.getdata() if p[3] > 0 and select(*p)]
    return (min(values), max(values)) if values else (0.0, 255.0)


def is_handle(r, g, b, a=255):
    """Manche d'un outil vanilla : brun (le rouge domine le bleu), la ou les lames sont froides ou neutres."""
    return r > b + 8


def to_ramp(image, ramp, handle_ramp=None):
    out = Image.new("RGBA", image.size, (0, 0, 0, 0))
    blade = lambda r, g, b, a: handle_ramp is None or not is_handle(r, g, b)
    low, high = levels(image, blade)
    h_low, h_high = levels(image, lambda r, g, b, a: is_handle(r, g, b)) if handle_ramp else (0, 255)
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = image.getpixel((x, y))
            if a == 0:
                continue
            if handle_ramp and is_handle(r, g, b):
                t = (luminance(r, g, b) - h_low) / max(1.0, h_high - h_low)
                color = on_ramp(t, handle_ramp)
            else:
                t = (luminance(r, g, b) - low) / max(1.0, high - low)
                color = on_ramp(t, ramp)
            out.putpixel((x, y), color + (a,))
    return out


def tint(image, hue, saturation, value):
    out = Image.new("RGBA", image.size, (0, 0, 0, 0))
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = image.getpixel((x, y))
            if a == 0:
                continue
            _, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
            nr, ng, nb = colorsys.hsv_to_rgb(hue / 360.0, min(1.0, s * saturation), min(1.0, v * value))
            out.putpixel((x, y), (round(nr * 255), round(ng * 255), round(nb * 255), a))
    return out


def bow(image):
    """Arc : le bois devient de l'onyx, la corde (claire et sans couleur) reste lumineuse, la fleche garde ses couleurs."""
    out = image.copy()
    wood = lambda r, g, b, a: r > b + 12
    low, high = levels(image, wood)
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = image.getpixel((x, y))
            if a and wood(r, g, b, a):
                out.putpixel((x, y), on_ramp((luminance(r, g, b) - low) / max(1.0, high - low), ONYX) + (a,))
            elif a and abs(r - b) < 12 and r > 150:
                out.putpixel((x, y), (214, 184, 255, a))
    return out


def main():
    jar = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_JAR
    if not jar.is_file():
        sys.exit(f"Jar de Minecraft introuvable : {jar}\nLancez d'abord ./gradlew build, ou passez son chemin.")
    count = 0
    with zipfile.ZipFile(jar) as zf:
        def vanilla(kind, name):
            img = Image.open(BytesIO(zf.read(f"{VANILLA}{kind}/{name}.png"))).convert("RGBA")
            return img.crop((0, 0, img.width, img.width))  # premiere image des textures animees

        def write(img, kind, name):
            nonlocal count
            (OUT / kind).mkdir(parents=True, exist_ok=True)
            img.save(OUT / kind / f"{name}.png")
            count += 1

        for name, (source, ramp, handle) in TOOLS.items():
            write(to_ramp(vanilla("item", source), ramp, handle), "item", name)
        for name, source in BOWS.items():
            write(bow(vanilla("item", source)), "item", name)
        for name, (source, ramp) in MATERIALS.items():
            write(to_ramp(vanilla("item", source), ramp), "item", name)
        for name, (source, hue, sat, val) in FOODS.items():
            write(tint(vanilla("item", source), hue, sat, val), "item", name)
        for wood, (hue, sat, val) in WOODS.items():
            for pattern, source in WOOD_PARTS.items():
                write(tint(vanilla("block", source), hue, sat, val), "block", pattern.format(w=wood))
            write(tint(vanilla("item", "crimson_door"), hue, sat, val), "item", f"{wood}_door")
        for name, (source, ramp) in STONE.items():
            write(to_ramp(vanilla("block", source), ramp), "block", name)
        for name, (source, hue, sat, val) in BLOCK_TINTS.items():
            write(tint(vanilla("block", source), hue, sat, val), "block", name)
    print(f"NetheryX : {count} textures derivees du vanilla dans {OUT.relative_to(ROOT)}.")


if __name__ == "__main__":
    main()
