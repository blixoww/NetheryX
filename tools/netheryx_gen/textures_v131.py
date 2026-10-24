"""Textures d'armure construites sur le modele des armures vanilla en diamant : icones aux silhouettes du diamant (cinq
tons eclaires en haut a gauche, contour dans une teinte sombre du materiau, un petit detail de couleur), et textures
portees (disposition vanilla 64 x 32) faites de plaques pleines et lumineuses traversees de quelques eclats en facettes."""
from PIL import Image

from .pixels import hexc
from .textures import save

# Palette d'un ensemble : contour (O), sombre (D), moyen (M), clair (L), reflet (H), detail (A)
SETS = {
    "onyx": ("#1a1028", "#33264c", "#4a3868", "#64508a", "#9478c8", "#d8c0ff"),
    "ember": ("#5a1c06", "#a8420e", "#d2661a", "#ee8c2e", "#ffc060", "#fff0a0"),
    "wraith": ("#1c2026", "#39414a", "#525b66", "#6c7682", "#9aa4b0", "#7fe8ff"),
    "carapace": ("#141418", "#2c2c33", "#3f3f48", "#55555f", "#787884", "#ff7a1a"),
    "abyssal": ("#1a060c", "#4a0e16", "#6a1620", "#8c222c", "#c03c44", "#ff6a2a"),
    "magma": ("#3a0e04", "#7c220a", "#b23e0e", "#de5c14", "#ff9a30", "#ffe27a"),
}


def _paint(img, x0, y0, rows, pal, grain=False):
    """Pose un dessin (lettres = tons). Avec grain, quelques pixels des surfaces moyennes s'eclaircissent ou
    s'assombrissent, comme le metal martele des armures vanilla (tirage deterministe d'apres la position)."""
    keys = {"O": pal[0], "D": pal[1], "M": pal[2], "L": pal[3], "H": pal[4], "A": pal[5]}
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch not in keys:
                continue
            x, y = x0 + dx, y0 + dy
            if grain and ch == "M":
                roll = (x * 7 + y * 13 + (x * y) % 5) % 17
                ch = "L" if roll == 0 else ("D" if roll in (5, 11) else ch)
            img.putpixel((x, y), hexc(keys[ch]))


# ------------------------------------------------------------------------------------------------
# Icones (16 x 16)
# ------------------------------------------------------------------------------------------------

ICONS = {
    "helmet": [
        "................",
        "................",
        "................",
        "....OOOOOOOO....",
        "...OHHLLLLLMO...",
        "..OHLLLAALLMDO..",
        "..OLLMMMMMMMDO..",
        "..OLMDOOOOLMDO..",
        "..OLMO....OMDO..",
        "..OLMO....OMDO..",
        "..OMDO....ODDO..",
        "...OO......OO...",
        "................",
        "................",
        "................",
        "................",
    ],
    "chestplate": [
        "................",
        "..OOOO....OOOO..",
        ".OHLLMO..OLLMDO.",
        ".OLLMMDOOLMMDDO.",
        ".OLLLMMMMMMMDDO.",
        "..OOLLMMMMMDOO..",
        "...OLLMMMMMDO...",
        "...OLLMAAMMDO...",
        "...OLMMAAMMDO...",
        "...OLMMMMMMDO...",
        "...ODDDDDDDDO...",
        "...OLMMMMMMDO...",
        "...OMDDDDDDDO...",
        "....OOOOOOOO....",
        "................",
        "................",
    ],
    "leggings": [
        "................",
        "..OOOOOOOOOOOO..",
        "..OHLLLLLLLLMO..",
        "..ODDDDAADDDDO..",
        "..OLMMMMMMMMDO..",
        "..OLMMDOOLMMDO..",
        "..OLMDO..OLMDO..",
        "..OLMDO..OLMDO..",
        "..OHLDO..OHLDO..",
        "..OLMDO..OLMDO..",
        "..OLMDO..OLMDO..",
        "..OMDDO..OMDDO..",
        "..OOOOO..OOOOO..",
        "................",
        "................",
        "................",
    ],
    "boots": [
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "...OOOO..OOOO...",
        "...OHLO..OHLO...",
        "...OLMO..OLMO...",
        "...ODDO..ODDO...",
        ".OOLLMO.OOLLMO..",
        ".OHLMMO.OHLMMO..",
        ".OAMMDO.OAMMDO..",
        ".OOOOOO.OOOOOO..",
        "................",
        "................",
    ],
}


def icon(set_name, piece):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    _paint(img, 0, 0, ICONS[piece], SETS[set_name])
    return img


# ------------------------------------------------------------------------------------------------
# Textures portees, construites comme les couches du diamant vanilla : chaque face est une plaque pleine et lumineuse,
# claire sur ses bords haut et gauche, sombre sur ses bords bas et droit, traversee de rares eclats clairs en diagonale (les
# facettes, espaces). Le casque laisse le visage a decouvert.
# ------------------------------------------------------------------------------------------------

def plate(img, x0, y0, w, h, pal, holes=()):
    o, d, m, l, hi, _ = pal
    for dy in range(h):
        for dx in range(w):
            if any(hx0 <= dx < hx1 and hy0 <= dy < hy1 for (hx0, hy0, hx1, hy1) in holes):
                continue
            x, y = x0 + dx, y0 + dy
            if dy == h - 1 or dx == w - 1:
                c = o if (dy == h - 1 and dx == w - 1) else d
            elif dy == 0 or dx == 0:
                c = l
            elif (x - y) % 9 == 0 and (x + y) % 3 != 0:
                c = l
            elif (x + 2 * y) % 23 == 0:
                c = hi
            else:
                c = m
            img.putpixel((x, y), hexc(c))
    if w > 2 and h > 2:
        img.putpixel((x0, y0), hexc(hi))


def cube(img, u, v, w, h, d, pal, rows=None, top=True, bottom=False, front_holes=()):
    """Faces d'un cube a la disposition vanilla ; rows = (debut, fin) limite les faces laterales a ces lignes."""
    if top:
        plate(img, u + d, v, w, d, pal)
    if bottom:
        plate(img, u + d + w, v, w, d, pal)
    y0, y1 = (v + d + rows[0], v + d + rows[1]) if rows else (v + d, v + d + h)
    for x, fw, holes in ((u, d, ()), (u + d, w, front_holes), (u + d + w, d, ()), (u + 2 * d + w, w, ())):
        plate(img, x, y0, fw, y1 - y0, pal, holes)


def layer_1(set_name, boots_only=False):
    pal = SETS[set_name]
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    if not boots_only:
        # Casque : visage degage (bandeau sur 3 lignes, joues sur les colonnes laterales), cotes jusqu'a la machoire
        cube(img, 0, 0, 8, 8, 8, pal, rows=(0, 7), front_holes=((2, 3, 6, 7),))
        plate(img, 24, 8, 8, 8, pal)  # nuque complete
        # Plastron : torse entier, epaulieres sur le haut des bras
        cube(img, 16, 16, 8, 12, 4, pal)
        cube(img, 40, 16, 4, 12, 4, pal, rows=(0, 6))
    # Bottes : bas des jambes et semelle
    cube(img, 0, 16, 4, 12, 4, pal, rows=(6, 12), top=False, bottom=True)
    return img


def layer_2(set_name):
    pal = SETS[set_name]
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Ceinture (bas du torse) puis jambieres jusqu'au-dessus des chevilles
    cube(img, 16, 16, 8, 12, 4, pal, rows=(7, 12), top=False)
    cube(img, 0, 16, 4, 12, 4, pal, rows=(0, 10))
    return img


ITEM_NAMES = {
    "onyx": ("onyx_helmet", "onyx_chestplate", "onyx_leggings", "onyx_boots"),
    "ember": ("ember_helmet", "ember_chestplate", "ember_leggings", "ember_boots"),
    "wraith": ("wraith_hood", "wraith_robe", "wraith_leggings", "wraith_boots"),
    "carapace": ("carapace_helmet", "carapace_chestplate", "carapace_leggings", "carapace_boots"),
    "abyssal": ("abyssal_helmet", "abyssal_chestplate", "abyssal_leggings", "abyssal_boots"),
}


def generate():
    for set_name, names in ITEM_NAMES.items():
        for piece, name in zip(("helmet", "chestplate", "leggings", "boots"), names):
            save(icon(set_name, piece), "item", f"{name}.png")
        save(layer_1(set_name), "models", "armor", f"{set_name}_layer_1.png")
        save(layer_2(set_name), "models", "armor", f"{set_name}_layer_2.png")
    save(icon("magma", "boots"), "item", "magma_treads.png")
    save(layer_1("magma", boots_only=True), "models", "armor", "magma_layer_1.png")
    save(layer_2("magma"), "models", "armor", "magma_layer_2.png")
