"""Textures 1.2.0 (suite) : icones d'armure v3, boss final, objets uniques, Jungle infernale, maconnerie scellee."""
from PIL import Image

from .pixels import Shape, hexc, mix, new, rng, shade
from .textures import save
from .textures_v121 import PALETTES, armor_layers
from .textures_v120 import vine

# ------------------------------------------------------------------------------------------------
# Icones d'armure v3 : chaque set a sa propre silhouette (cimier, capuche, cornes...), un ombrage
# automatique (lumiere en haut a gauche) et une pierre / lueur d'accent.
#   '#' matiere principale, 't' garniture, 'a'/'A' accent (gemme, lueur), 'k' creux sombre, 'w' reflet.
#   Une ligne de 8 caracteres est completee par symetrie.
# ------------------------------------------------------------------------------------------------

SETS = {
    "onyx": {"o": "#0b0710", "body": ("#1d1428", "#33244a", "#4e3878", "#7c5cc0"), "w": "#efe4ff",
             "trim": ("#5a5478", "#8a84a8", "#bab4d8", "#f0ecff"), "a": "#b06cff", "A": "#f4d8ff", "k": "#08050c"},
    "ember": {"o": "#2a0a04", "body": ("#6a1e08", "#a8380e", "#e0601a", "#ffa040"), "w": "#fff0c0",
              "trim": ("#8a5a10", "#c8901c", "#f0c040", "#fff0a0"), "a": "#ffb020", "A": "#fffbe0", "k": "#1a0602"},
    "wraith": {"o": "#111116", "body": ("#2c2c36", "#44444f", "#5e5e6c", "#80808e"), "w": "#c4c8d8",
               "trim": ("#4a3a2a", "#6a5440", "#8a7058", "#a88a6c"), "a": "#4ae8ff", "A": "#d8ffff", "k": "#050508"},
    "carapace": {"o": "#08080a", "body": ("#1c1c22", "#2c2c34", "#40404a", "#5e5e6a"), "w": "#9a9aa8",
                 "trim": ("#3a3026", "#54463a", "#6e5e4e", "#8a7a68"), "a": "#ff6a1a", "A": "#ffd06a", "k": "#050505"},
    "magma": {"o": "#1a0402", "body": ("#3a1006", "#5a1a08", "#7a2a0e", "#a03a14"), "w": "#ffd36b",
              "trim": ("#c8460e", "#ff7a1a", "#ffa040", "#ffd36b"), "a": "#ff8a1a", "A": "#fff0a0", "k": "#140402"},
    "abyssal": {"o": "#050102", "body": ("#140608", "#22090c", "#361014", "#521a1e"), "w": "#ff9a5a",
                "trim": ("#4a0a0a", "#7a1410", "#a82018", "#d8382a"), "a": "#ff5a1a", "A": "#ffd08a", "k": "#000000"},
}

# Palette des couches portees du set abyssal (meme format que les autres sets)
PALETTES.setdefault("abyssal", {"o": "#050102", "d": "#140608", "m": "#22090c", "l": "#3a1216", "h": "#5a1e22", "w": "#ff7a2a"})

ICONS = {
    "onyx": {
        "helmet": ["", "", ".......A", "......#a", "....####", "...#####", "..######", "..tttttt", "..##kkkk",
                   "..##...#", "..##....", "..t#....", "...t...."],
        "chestplate": ["", ".tt.....", "t##t....", "t###....", "t####...", ".t#####.", "..######", "..#####A",
                       "..#####a", "..######", "..tttttt", "..######", "...#####", "...ttttt"],
        "leggings": ["", "", "..tttttt", "..#####a", "..######", "..######", "..#####.", "..#####.", "..#ttt#.",
                     "..#####.", "..#####.", "..#####.", "..ttttt."],
        "boots": ["", "", "", "", "", "", "...ttt..", "...###..", "...###..", "...###..", "..####..", ".t####..",
                  "######..", "tttttt.."],
    },
    "ember": {
        "helmet": ["", "......A.", "......aA", ".....aa#", "....####", "...#####", "..######", "..tttttt",
                   "..##kkkk", "..##...#", "..##....", "..t#....", "...a...."],
        "chestplate": ["A.......", "aA......", "tta.....", "t##t....", "t###....", "t####...", ".t#####.",
                       "..#####a", "..####aA", "..#####a", "..tttttt", "..######", "...#####", "...ttttt"],
        "leggings": ["", "", "..tttttt", "..#####A", "..######", "..######", "..#####.", "..#####.", "..#tat#.",
                     "..#####.", "..#####.", "..#####.", "..ttttt."],
        "boots": ["", "", "", "", "", "...A....", "...ttt..", "...#a#..", "...###..", "...###..", "..####..",
                  ".t####..", "######..", "tttttt.."],
    },
    "wraith": {
        "helmet": ["", ".......#", "......##", ".....###", "....####", "...#####", "...##kkk", "..##kkkk",
                   "..##kAka", "..##kkkk", "..#tkkkk", "..#t....", "...t...."],
        "chestplate": ["", "..t.....", ".###t...", "####kk..", "####k#..", "######..", ".#####t#", "..####t#",
                       "..#tttt#", "..######", "..######", ".#######", ".#######", ".##.##.#"],
        "leggings": ["", "", "..tttttt", "..######", "..t#####", "..#t###.", "..##t##.", "..###t#.", "..t###t.",
                     "..#t###.", "..##t##.", "..###t#.", "..#####."],
        "boots": ["", "", "", "", "", "", "...t#t..", "...#t#..", "...t#t..", "...#t#..", "..##t#..", ".#####..",
                  "######..", "tttttt.."],
    },
    "carapace": {
        "helmet": ["", "....A...", "....#...", "....##..", "....####", "...#####", "..######", "..#kkkkk",
                   "..######", "..##...#", "..##....", "..#t....", "...t...."],
        "chestplate": ["", ".##.....", "####....", "####t...", "t####...", ".t######", "..######", "..kkkkka",
                       "..######", "..######", "..kkkkkk", "..######", "...#####", "...ttttt"],
        "leggings": ["", "", "..tttttt", "..######", "..kkkkka", "..######", "..#####.", "..kkkk#.", "..#####.",
                     "..#####.", "..kkka#.", "..#####.", "..ttttt."],
        "boots": ["", "", "", "", "", "", "...ttt..", "...###..", "...kak..", "...###..", "..####..", ".kkkk#..",
                  "######..", "tttttt.."],
    },
    "abyssal": {
        "helmet": ["t.......", "t.......", ".t......", ".t#....A", "..t##..a", "...#####", "..######", "..tttttt",
                   "..##kkkk", "..##k#.#", "..##....", "..t#....", "...t...."],
        "chestplate": ["", "tt......", "t##t....", "t###....", "t####...", ".t#####.", "..####aa", "..###aAA",
                       "..####aa", "..######", "..tttttt", "..##a###", "...#####", "...ttttt"],
        "leggings": ["", "", "..tttttt", "..####aa", "..######", "..######", "..#####.", "..#a###.", "..#ttt#.",
                     "..#####.", "..###a#.", "..#####.", "..ttttt."],
        "boots": ["", "", "", "", "", "", "...ttt..", "...###..", "...#a#..", "...###..", "..####..", ".t####..",
                  "##a###..", "tttttt.."],
    },
    "magma": {
        "boots": ["", "", "", "", "", "...a....", "...ttt..", "...#a#..", "...###..", "...a##..", "..##a#..",
                  ".t####..", "##a#a#..", "tttttt.."],
    },
}


def expand(rows):
    out = []
    for row in rows:
        if len(row) == 8:
            row = row + row[::-1]
        out.append(row.ljust(16, "."))
    while len(out) < 16:
        out.append("." * 16)
    return out[:16]


def sprite(rows, pal, outline=True):
    """Dessine un masque ASCII avec ombrage automatique (bords hauts/gauches clairs, bas/droits sombres)."""
    rows = expand(rows)
    solid = {(x, y): ch for y, row in enumerate(rows) for x, ch in enumerate(row) if ch != "."}
    img = new()
    if not solid:
        return img
    min_s = min(x + y for x, y in solid)
    max_s = max(x + y for x, y in solid)
    span = max(1, max_s - min_s)
    for (x, y), ch in solid.items():
        if ch in "#t":
            d, m, l, h = pal["body"] if ch == "#" else pal["trim"]
            up, left = solid.get((x, y - 1)), solid.get((x - 1, y))
            down, right = solid.get((x, y + 1)), solid.get((x + 1, y))
            t = (x + y - min_s) / span
            if up is None:
                color = h
            elif left is None:
                color = l
            elif down is None or right is None:
                color = d
            elif up == "k":
                color = d
            else:
                color = l if t < 0.38 else m
            img.putpixel((x, y), hexc(color))
        elif ch in "aAkw":
            img.putpixel((x, y), hexc(pal[ch]))
    # Reflet speculaire : le premier pixel clair en haut a gauche de chaque masse
    for (x, y), ch in sorted(solid.items(), key=lambda p: (p[0][0] + p[0][1], p[0][1])):
        if ch == "#" and solid.get((x - 1, y)) and solid.get((x, y - 1)) and solid.get((x + 1, y)) == "#":
            img.putpixel((x, y), hexc(pal["w"]))
            break
    if outline:
        for y in range(16):
            for x in range(16):
                if (x, y) in solid:
                    continue
                if any((x + dx, y + dy) in solid for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    img.putpixel((x, y), hexc(pal["o"]))
    return img


def armor_icon_v3(set_name, piece):
    return sprite(ICONS[set_name][piece], SETS[set_name])


# ------------------------------------------------------------------------------------------------
# Objets uniques et boss final
# ------------------------------------------------------------------------------------------------

def gem(rows, colors):
    img = new()
    rows = expand(rows)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                img.putpixel((x, y), hexc(colors[ch]))
    return img


def onyx_apple():
    shape = Shape()
    shape.ellipse(8, 9.5, 5.2, 5.0, hexc("#6a48b0"))
    for (x, y) in ((5, 7), (6, 6), (5, 8)):
        shape.put(x, y, hexc("#e8d8ff"))
    for (x, y) in ((8, 3), (8, 4), (7, 4)):
        shape.put(x, y, hexc("#3a2414"))
    for (x, y) in ((9, 3), (10, 3), (10, 2), (11, 2)):
        shape.put(x, y, hexc("#b06cff"))
    shape.put(8, 5, hexc("#2c1c48"))
    for (x, y) in ((10, 12), (11, 11), (9, 13)):
        shape.put(x, y, hexc("#2c1c48"))
    return shape.render(outline=hexc("#0b0710"))


def sentinel_eye():
    shape = Shape()
    shape.ellipse(8, 8, 6, 4.2, hexc("#3a2c58"))
    shape.ellipse(8, 8, 3.2, 3.2, hexc("#b06cff"))
    shape.disc(8, 8, 1.6, hexc("#1a0a2a"), shaded=False)
    shape.put(7, 6, hexc("#ffffff"))
    for x in (2, 13):
        shape.put(x, 8, hexc("#5a4a7a"))
    return shape.render(outline=hexc("#0b0710"))


def warlock_sigil():
    shape = Shape()
    shape.disc(8, 8, 6.2, hexc("#7a1a0a"))
    shape.disc(8, 8, 4.8, hexc("#2a0804"), shaded=False)
    for (x0, y0, x1, y1) in ((8, 4, 5, 11), (5, 11, 12, 7), (12, 7, 4, 7), (4, 7, 11, 11), (11, 11, 8, 4)):
        shape.line(x0, y0, x1, y1, hexc("#ff8a1a"))
    shape.put(8, 8, hexc("#ffe27a"))
    return shape.render(outline=hexc("#1a0402"))


def champion_crown():
    pal = {"o": "#0b0b0d", "body": ("#3a3a40", "#56565e", "#74747e", "#a0a0aa"), "w": "#e0e0e8",
           "trim": ("#5a1a1a", "#8a2a22", "#b03a2a", "#d85a3a"), "a": "#3ad8b0", "A": "#c8fff0", "k": "#000000"}
    return sprite(["", "", "", "..#..#..", "..#..#.#", "..##.###", "..######", "..#a##A#", "..######", "..tttttt",
                   "..######", "", ""], pal)


def sealed_heart():
    shape = Shape()
    for (cx, cy) in ((5.5, 6.5), (10.5, 6.5)):
        shape.disc(cx, cy, 3.6, hexc("#a8180e"))
    for y in range(7, 14):
        half = max(0, 7 - (y - 7) * 1.15)
        for x in range(16):
            if abs(x + 0.5 - 8) <= half:
                shape.put(x, y, shade(hexc("#a8180e"), 1.1 - (y - 7) * 0.05))
    for (x, y) in ((4, 5), (5, 4), (4, 6)):
        shape.put(x, y, hexc("#ffb08a"))
    # Chaines d'onyx qui scellent le coeur
    for k in range(16):
        y = 4 + k * 9 // 15
        if (k % 3) != 2 and 2 <= k <= 13:
            shape.put(k, y, hexc("#5a3f86") if k % 2 else hexc("#9a70e8"))
    shape.put(8, 9, hexc("#ffe27a"))
    shape.put(7, 9, hexc("#ff8a1a"))
    return shape.render(outline=hexc("#1a0404"))


def abyssal_shard():
    rows = ["", "", "..........oo....", ".........oAao...", "........oAaado..", ".......oAaaado..", "......oAaaado...",
            ".....oaaaaddo...", "....oaAaaddo....", "....oaaaddo.....", "...oaaddo.......", "...odddo........",
            "....ooo.........", ""]
    return gem(rows, {"o": "#050102", "a": "#a82018", "A": "#ff8a3a", "d": "#4a0a0a"})


def abyssal_template():
    pal = {"o": "#050102", "body": ("#1a0a0c", "#2a1014", "#3a161a", "#521e22"), "w": "#ff9a5a",
           "trim": ("#4a0a0a", "#7a1410", "#a82018", "#d8382a"), "a": "#ff5a1a", "A": "#ffd08a", "k": "#000000"}
    return sprite(["", "...#####", "..######", "..#ttttt", "..#t####", "..#t#kkk", "..#t#kaA", "..#t#kaa", "..#t#kkk",
                   "..#t####", "..#ttttt", "..######", "...#####", ""], pal)


# ------------------------------------------------------------------------------------------------
# Jungle infernale et maconnerie scellee
# ------------------------------------------------------------------------------------------------

def fern_half(top):
    shape = Shape()
    r = rng("hellfern" + str(top))
    greens = ["#7a1e0a", "#a8300e", "#d04a14", "#f07a2a"]
    for stem_x, lean in ((5, -1), (8, 0), (11, 1)):
        x = float(stem_x)
        height = r.randint(12, 16) if top else 16
        for k in range(height):
            y = 15 - k
            col = hexc(greens[min(3, k * 4 // max(1, height))] if top else greens[min(2, k // 6)])
            shape.put(round(x), y, col)
            if k % 2 == 1 and k > 1:
                side = 1 if (k // 2 + stem_x) % 2 else -1
                length = r.randint(2, 4)
                for f in range(1, length + 1):
                    shape.put(round(x) + side * f, y + (f // 2) - (0 if top else 1), shade(col, 1.0 + 0.08 * f))
            if top:
                x += lean * 0.12
        if top:
            shape.put(round(x), 15 - height, hexc("#ffd36b"))
    return shape.render()


def blazing_grass():
    shape = Shape()
    r = rng("blazing_grass")
    tones = ["#8a200a", "#c8400e", "#f07a1a", "#ffc040"]
    for x0 in range(1, 15, 2):
        h = r.randint(6, 13)
        x = x0
        for k in range(h):
            shape.put(x, 15 - k, hexc(tones[min(3, k * 4 // h)]))
            if r.random() < 0.25:
                x = max(0, min(15, x + r.choice((-1, 1))))
        if r.random() < 0.5:
            shape.put(x, 15 - h, hexc("#fff0a0"))
    return shape.render()


def warded_runes():
    """Calque discret pose sur les briques du coffre-fort : une petite rune centrale a peine lumineuse et un liseré sombre."""
    img = new()
    glyph = ["..#..", ".#.#.", "#.#.#", ".#.#.", "..#.."]
    for dy, row in enumerate(glyph):
        for dx, ch in enumerate(row):
            if ch == "#":
                img.putpixel((5 + dx, 5 + dy), hexc("#ffb050", 150 if (dx + dy) % 2 else 200))
    for k in range(16):
        for (x, y) in ((k, 0), (k, 15), (0, k), (15, k)):
            img.putpixel((x, y), hexc("#1a0a04", 70))
    return img


def generate():
    sets = {
        "onyx": ("onyx_helmet", "onyx_chestplate", "onyx_leggings", "onyx_boots"),
        "ember": ("ember_helmet", "ember_chestplate", "ember_leggings", "ember_boots"),
        "wraith": ("wraith_hood", "wraith_robe", "wraith_leggings", "wraith_boots"),
        "carapace": ("carapace_helmet", "carapace_chestplate", "carapace_leggings", "carapace_boots"),
        "abyssal": ("abyssal_helmet", "abyssal_chestplate", "abyssal_leggings", "abyssal_boots"),
    }
    for set_name, names in sets.items():
        for piece, name in zip(("helmet", "chestplate", "leggings", "boots"), names):
            save(armor_icon_v3(set_name, piece), "item", f"{name}.png")
    save(armor_icon_v3("magma", "boots"), "item", "magma_treads.png")
    l1, l2 = armor_layers("abyssal", "#ff5a1a")
    save(l1, "models", "armor", "abyssal_layer_1.png")
    save(l2, "models", "armor", "abyssal_layer_2.png")

    save(onyx_apple(), "item", "onyx_apple.png")
    save(sentinel_eye(), "item", "sentinel_eye.png")
    save(warlock_sigil(), "item", "warlock_sigil.png")
    save(champion_crown(), "item", "champion_crown.png")
    save(sealed_heart(), "item", "sealed_heart.png")
    save(abyssal_shard(), "item", "abyssal_shard.png")
    save(abyssal_template(), "item", "abyssal_upgrade_template.png")

    save(fern_half(False), "block", "hellfern_bottom.png")
    save(fern_half(True), "block", "hellfern_top.png")
    save(blazing_grass(), "block", "blazing_grass.png")
    save(vine(["#c8400e", "#e0601a", "#a8300e"], "ember_vines", tip="#ffe27a", head=True), "block", "ember_vines.png")
    save(vine(["#c8400e", "#e0601a", "#a8300e"], "ember_vines_plant"), "block", "ember_vines_plant.png")
    save(warded_runes(), "block", "warded_runes.png")
