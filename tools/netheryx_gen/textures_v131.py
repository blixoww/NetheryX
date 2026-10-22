"""Textures 1.3.1 : armures refaites dans l'esprit des armures vanilla (fer, diamant) : silhouettes simples, quatre
tons et un contour sombre, un seul petit detail de couleur par piece. Les textures portees suivent la disposition
vanilla (64 x 32) : faces pleines bordees, ouverture du visage sur le casque."""
from PIL import Image

from .pixels import hexc
from .textures import save

# Palette d'un ensemble : contour, sombre, moyen, clair, reflet, detail
SETS = {
    "onyx": ("#140d1e", "#2e2244", "#44335e", "#5c477e", "#8a6cb8", "#c8a8ff"),
    "ember": ("#3a1406", "#9a3a0c", "#c85a18", "#e8822a", "#ffbe5a", "#fff0a0"),
    "wraith": ("#16191e", "#353b44", "#4b525b", "#646c76", "#8c949e", "#7fe8ff"),
    "carapace": ("#101014", "#26262c", "#38383f", "#4c4c55", "#6c6c78", "#ff7a1a"),
    "abyssal": ("#0e0408", "#360a10", "#55121a", "#771c24", "#a8323a", "#ff6a2a"),
    "magma": ("#2e0a04", "#701c08", "#a8360c", "#d85612", "#ff9a2a", "#ffe27a"),
}

# Icones 16 x 16 : o contour, d sombre, m moyen, l clair, h reflet, a detail
ICONS = {
    "helmet": [
        "................",
        "................",
        "................",
        "....oooooooo....",
        "...ohhlllammo...",
        "..ohllllllmmdo..",
        "..olllmmmmmmdo..",
        "..olmmmmmmmmdo..",
        "..olmoooooomdo..",
        "..olmo....omdo..",
        "..olmo....omdo..",
        "..oddo....oddo..",
        "...oo......oo...",
        "................",
        "................",
        "................",
    ],
    "chestplate": [
        "................",
        "..ooo......ooo..",
        ".ohlmo....omldo.",
        ".ohllmooooomldo.",
        ".ohllllllllmmdo.",
        ".olllllaalllmdo.",
        ".oollllaallmmoo.",
        "..olllllllmmmo..",
        "..ollllllmmmmo..",
        "..ollllllmmmmo..",
        "..olllllmmmmdo..",
        "..olmmmmmmmmdo..",
        "..oddddddddddo..",
        "...oooooooooo...",
        "................",
        "................",
    ],
    "leggings": [
        "................",
        "..oooooooooooo..",
        "..ohhlllalllmo..",
        "..oddddddddddo..",
        "..olmmdoolmmdo..",
        "..olmmdoolmmdo..",
        "..olmdo..olmdo..",
        "..olmdo..olmdo..",
        "..olmdo..olmdo..",
        "..olmdo..olmdo..",
        "..olmdo..olmdo..",
        "..olmdo..olmdo..",
        "..oddoo..oddoo..",
        "..ooooo..ooooo..",
        "................",
        "................",
    ],
    "boots": [
        "................",
        "................",
        "................",
        "................",
        "................",
        "..oooo....oooo..",
        "..ohlo....ohlo..",
        "..olmo....olmo..",
        "..olmo....olmo..",
        "..olmdo...olmdo.",
        ".ollmmdo.ollmmdo",
        ".olmmmdo.olmmmdo",
        ".oddddao.oddddao",
        ".ooooooo.ooooooo",
        "................",
        "................",
    ],
}


def icon(set_name, piece):
    o, d, m, l, h, a = SETS[set_name]
    colors = {"o": o, "d": d, "m": m, "l": l, "h": h, "a": a}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(ICONS[piece]):
        for x, ch in enumerate(row[:16]):
            if ch in colors:
                img.putpixel((x, y), hexc(colors[ch]))
    return img


# ------------------------------------------------------------------------------------------------
# Textures portees (disposition vanilla 64 x 32)
# ------------------------------------------------------------------------------------------------

def face(img, x0, y0, w, h, pal, top_light=True):
    """Face pleine : bord sombre, interieur moyen eclaire en haut a gauche, quelques reflets reguliers."""
    o, d, m, l, hi, _ = pal
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            edge_bottom = y == y0 + h - 1
            edge_right = x == x0 + w - 1
            edge_top = y == y0
            edge_left = x == x0
            if edge_bottom or edge_right:
                c = d
            elif (edge_top or edge_left) and top_light:
                c = l
            else:
                c = m
            img.putpixel((x, y), hexc(c))
    if w > 3 and h > 3:
        img.putpixel((x0 + 1, y0 + 1), hexc(hi))


def box(img, u, v, w, h, d, pal, rows=None):
    """Toutes les faces d'un cube a la disposition vanilla ; rows limite les faces laterales a certaines lignes."""
    face(img, u + d, v, w, d, pal)              # dessus
    face(img, u + d + w, v, w, d, pal, False)   # dessous
    for (x, fw) in ((u, d), (u + d, w), (u + d + w, d), (u + 2 * d + w, w)):
        y0 = v + d
        if rows:
            face(img, x, y0 + rows[0], fw, rows[1] - rows[0], pal)
        else:
            face(img, x, y0, fw, h, pal)


def clear(img, x0, y0, x1, y1):
    for y in range(y0, y1):
        for x in range(x0, x1):
            img.putpixel((x, y), (0, 0, 0, 0))


def layer_1(set_name, boots_only=False):
    pal = SETS[set_name]
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    a = hexc(pal[5])
    if not boots_only:
        # Casque : cube de tete, ouverture du visage sur la face avant
        box(img, 0, 0, 8, 8, 8, pal)
        clear(img, 10, 12, 14, 16)
        clear(img, 9, 14, 15, 16)
        img.putpixel((11, 9), a)
        img.putpixel((12, 9), a)
        # Plastron : torse complet, epaulieres jusqu'au coude
        box(img, 16, 16, 8, 12, 4, pal)
        clear(img, 28, 16, 36, 20)  # dessous du torse invisible
        for (x, y) in ((23, 23), (24, 23), (23, 24), (24, 24)):
            img.putpixel((x, y), a)
        box(img, 40, 16, 4, 12, 4, pal, rows=(0, 7))
        clear(img, 48, 16, 52, 20)
    # Bottes : bas des jambes
    box(img, 0, 16, 4, 12, 4, pal, rows=(6, 12))
    clear(img, 4, 16, 8, 20)  # pas de dessus
    img.putpixel((6, 30), a)
    return img


def layer_2(set_name):
    pal = SETS[set_name]
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Ceinture : bas du torse
    box(img, 16, 16, 8, 12, 4, pal, rows=(7, 12))
    clear(img, 20, 16, 36, 20)
    img.putpixel((23, 28), hexc(pal[5]))
    img.putpixel((24, 28), hexc(pal[5]))
    # Jambieres : jambes presque entieres
    box(img, 0, 16, 4, 12, 4, pal, rows=(0, 10))
    clear(img, 8, 16, 12, 20)
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
