"""Armures NetheryX recolorees depuis les armures vanilla en diamant (icones et textures portees).

Chaque pixel du diamant est recolore d'apres sa luminosite, sur la rampe de tons de l'ensemble
(netheryx_gen.textures_v131.SETS) : les pieces gardent exactement la silhouette et l'ombrage vanilla.
Le resultat est ecrit dans art/, que tools/generate_resources.py copie par-dessus les textures generees.

Le jar de Minecraft n'est pas dans le depot : il faut l'executer en local apres un premier ./gradlew build
(ForgeGradle le place dans ~/.gradle/caches), puis relancer le generateur :

    python3 tools/diamond_armor.py [chemin/vers/client-extra.jar]
    python3 tools/generate_resources.py
"""
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
PIECES = ("helmet", "chestplate", "leggings", "boots")

# Icones de chaque ensemble, dans l'ordre casque, plastron, jambieres, bottes (None = piece absente)
ITEMS = {
    "onyx": ("onyx_helmet", "onyx_chestplate", "onyx_leggings", "onyx_boots"),
    "ember": ("ember_helmet", "ember_chestplate", "ember_leggings", "ember_boots"),
    "wraith": ("wraith_hood", "wraith_robe", "wraith_leggings", "wraith_boots"),
    "carapace": ("carapace_helmet", "carapace_chestplate", "carapace_leggings", "carapace_boots"),
    "abyssal": ("abyssal_helmet", "abyssal_chestplate", "abyssal_leggings", "abyssal_boots"),
    "magma": (None, None, None, "magma_treads"),
}

# Luminosite des tons du diamant vanilla : contour, sombre, moyen, clair, reflet, blanc
ANCHORS = (28, 47, 127, 186, 222, 255)


def hex_rgb(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def ramp(palette):
    """Tons cibles alignes sur ANCHORS : les cinq tons de l'ensemble, puis la couleur d'accent a la place des eclats
    blancs du diamant (c'est elle qui distingue la carapace du spectre, ou l'abysse de la braise)."""
    outline, dark, mid, light, highlight, accent = (hex_rgb(c) for c in palette)
    return (outline, dark, mid, light, highlight, accent)


def recolor(image, tones):
    out = Image.new("RGBA", image.size, (0, 0, 0, 0))
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = image.getpixel((x, y))
            if a == 0:
                continue
            lum = 0.299 * r + 0.587 * g + 0.114 * b
            if lum <= ANCHORS[0]:
                color = tones[0]
            else:
                i = next((k for k in range(1, len(ANCHORS)) if lum <= ANCHORS[k]), len(ANCHORS) - 1)
                t = min(1.0, (lum - ANCHORS[i - 1]) / (ANCHORS[i] - ANCHORS[i - 1]))
                color = mix(tones[i - 1], tones[i], t)
            out.putpixel((x, y), color + (a,))
    return out


def main():
    jar = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_JAR
    if not jar.is_file():
        sys.exit(f"Jar de Minecraft introuvable : {jar}\nLancez d'abord ./gradlew build, ou passez son chemin.")
    with zipfile.ZipFile(jar) as zf:
        def vanilla(path):
            return Image.open(BytesIO(zf.read(VANILLA + path))).convert("RGBA")

        icons = {piece: vanilla(f"item/diamond_{piece}.png") for piece in PIECES}
        layers = {n: vanilla(f"models/armor/diamond_layer_{n}.png") for n in (1, 2)}

    count = 0
    for name, items in ITEMS.items():
        tones = ramp(SETS[name])
        for piece, item in zip(PIECES, items):
            if item:
                (OUT / "item").mkdir(parents=True, exist_ok=True)
                recolor(icons[piece], tones).save(OUT / "item" / f"{item}.png")
                count += 1
        for n, layer in layers.items():
            (OUT / "models" / "armor").mkdir(parents=True, exist_ok=True)
            recolor(layer, tones).save(OUT / "models" / "armor" / f"{name}_layer_{n}.png")
            count += 1
    print(f"NetheryX : {count} textures d'armure recolorees depuis le diamant dans {OUT.relative_to(ROOT)}.")


if __name__ == "__main__":
    main()
