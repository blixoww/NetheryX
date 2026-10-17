"""Retouches 1.2.0 : minerais et sols poses sur la netherrack vanilla, icones refaites (armures, outils, objets)."""
import math

from PIL import Image

from .common import asset_path
from .pixels import Shape, hexc, mix, new, rng, shade
from .textures import save
from .textures_v121 import PALETTES, art, tool_image, sword

# ------------------------------------------------------------------------------------------------
# Calques de minerais (sur fond transparent : la netherrack vanilla est dessinee dessous)
# ------------------------------------------------------------------------------------------------


def ore_overlay(seed, tones, chunks, pattern=(".hw.", "hlmd", ".md.")):
    img = new()
    for (cx, cy) in chunks:
        cells = {}
        for dy, row in enumerate(pattern):
            for dx, ch in enumerate(row):
                if ch in tones:
                    cells[((cx + dx) % 16, (cy + dy) % 16)] = hexc(tones[ch])
        # Ombre portee d'un pixel en bas a droite, comme les minerais vanilla
        for (x, y) in list(cells):
            for (sx, sy) in ((x + 1, y), (x, y + 1)):
                if (sx, sy) not in cells and 0 <= sx < 16 and 0 <= sy < 16:
                    img.putpixel((sx, sy), hexc(tones["s"], 170))
        for (x, y), col in cells.items():
            img.putpixel((x, y), col)
    return img


ONYX_TONES = {"h": "#9a70e8", "w": "#f0e4ff", "l": "#4a3870", "m": "#2c2040", "d": "#1b1226", "s": "#1a0606"}
SULFUR_TONES = {"h": "#fff27a", "w": "#ffffff", "l": "#e8d43a", "m": "#c8b020", "d": "#8a7410", "s": "#1a0606"}
QUARTZ_TONES = {"h": "#ffffff", "w": "#ffffff", "l": "#ece6de", "m": "#d4ccc2", "d": "#a89e94", "s": "#1a0606"}


def soil_side_overlay(top_name, seed):
    top = Image.open(asset_path("textures", "block", f"{top_name}.png")).convert("RGBA")
    img = new()
    r = rng(seed)
    for x in range(16):
        depth = r.choice((2, 3, 3, 4, 4, 5))
        if r.random() < 0.15:
            depth += 2  # racine qui pend
        for y in range(depth):
            img.putpixel((x, y), top.getpixel((x, y)))
        img.putpixel((x, depth), shade(top.getpixel((x, depth)), 0.6))
    return img


# ------------------------------------------------------------------------------------------------
# Icones d'armure v2 : plus de volume (reflets, ombre interieure, eclats)
# ------------------------------------------------------------------------------------------------

ARMOR_V2 = {
    "helmet": ["", "", "",
               ".....oooooo.....",
               "...oohhwhhhloo..",
               "..ohhhllllllmdo.",
               "..ohlllllllmmdo.",
               "..ohlmmmmmmmmdo.",
               "..ohloookkooldo.",
               "..ohlo.....omdo.",
               "..ohmo.....omdo.",
               "..oddo.....oddo.",
               "...oo.......oo.."],
    "chestplate": ["",
                   "..ooo......ooo..",
                   ".ohhlo....ohlmo.",
                   ".ohwlmookkolmdo.",
                   ".ohllmmooommmdo.",
                   ".ohhlllllllllmdo",
                   "..oohllwlllmmoo.",
                   "...ohlllllllmo..",
                   "...ohllllllmdo..",
                   "...ohllmmmmmdo..",
                   "...olmmmmmmmdo..",
                   "...odddddddddo..",
                   "....ooooooooo..."],
    "leggings": ["", "",
                 "...ooooooooooo..",
                 "...ohhwhhhhhlo..",
                 "...odddddddddo..",
                 "...ohllllllmdo..",
                 "...ohlmookolmdo.",
                 "...ohlmo..ohmdo.",
                 "...ohlmo..ohmdo.",
                 "...ohlmo..ohmdo.",
                 "...ohwmo..ohmdo.",
                 "...ohlmo..ohmdo.",
                 "...oddo...oddo..",
                 "....oo.....oo..."],
    "boots": ["", "", "", "", "",
              "..oooo....oooo..",
              "..ohlo....ohlo..",
              "..ohmo....ohmo..",
              "..ohmo....ohmo..",
              ".oohmo...oohmo..",
              "ohwlmo..ohwlmo..",
              "ohllmmo.ohllmmo.",
              "oddddo..oddddo..",
              "oooooo..oooooo.."],
}


def armor_v2(piece, pal):
    colors = dict(PALETTES[pal])
    colors["k"] = "#050307"
    img = art([row[:16] for row in ARMOR_V2[piece]], colors)
    if pal == "carapace":
        for (x, y) in {"helmet": ((6, 6), (10, 7)), "chestplate": ((6, 8), (9, 10), (7, 12)), "leggings": ((5, 7), (11, 10)),
                       "boots": ((3, 12), (10, 12))}[piece]:
            if img.getpixel((x, y))[3]:
                img.putpixel((x, y), hexc("#ff7a2a"))
    if pal == "ember":
        for (x, y) in {"helmet": ((7, 5),), "chestplate": ((7, 8), (8, 8)), "leggings": ((7, 4), (8, 4)), "boots": ((4, 11),)}[piece]:
            if img.getpixel((x, y))[3]:
                img.putpixel((x, y), hexc("#ffe27a"))
    return img


# ------------------------------------------------------------------------------------------------
# Objets
# ------------------------------------------------------------------------------------------------

def cauldron_item():
    rows = ["", "",
            ".oooooooooooooo.",
            ".ohhhhhhhhhhhlo.",
            ".olpppppppppplo.",
            ".ohlmmmmmmmmmdo.",
            ".ohlmmmrmmmmmdo.",
            ".ohlmmrrrmmmmdo.",
            ".ohlmmmrmmmrmdo.",
            ".ohlmmmmmmrrmdo.",
            ".ohlmmmmmmmmmdo.",
            "..oddddddddddo..",
            "..olo......olo..",
            "..ooo......ooo.."]
    colors = {"o": "#0e0a10", "h": "#5a4e68", "l": "#3e3448", "m": "#2a2232", "d": "#18121e", "p": "#c050e0", "r": "#ff7a1a"}
    img = art(rows, colors)
    img.putpixel((4, 4), hexc("#f0b0ff"))
    img.putpixel((9, 4), hexc("#f0b0ff"))
    return img


def infernal_heart():
    rows = ["", "",
            "...ooo...ooo....",
            "..ohhro.orrdo...",
            ".ohwhrroorrrdo..",
            ".ohhrrrrrrrrdo..",
            ".orrrrryrrrrdo..",
            ".orrryyyyrrrdo..",
            "..orrryrrrrdo...",
            "...orrrrrrdo....",
            "....orrrrdo.....",
            ".....ordo.......",
            "......oo........"]
    colors = {"o": "#2a0606", "h": "#ff9a6a", "w": "#ffe6c8", "r": "#c8200a", "d": "#6a0c06", "y": "#ffb02e"}
    img = art(rows, colors)
    for (x, y) in ((4, 6), (9, 5), (10, 8)):
        img.putpixel((x, y), hexc("#2c2040"))  # eclats d'onyx incrustes
    return img


def basalt_carapace():
    rows = ["", "",
            ".....oooooo.....",
            "...oohhlllmoo...",
            "..ohhlcllclmdo..",
            "..ohlllcclllmdo.",
            ".ohllcllllcllmdo",
            ".ohlllcccclllmdo",
            ".olllcllllclmmdo",
            "..olmllcclllmdo.",
            "..ommmmcmmmmmdo.",
            "...oodddddddoo..",
            ".....oooooo....."]
    colors = {"o": "#0b0b0d", "h": "#7a7a86", "l": "#4c4c56", "m": "#36363e", "d": "#24242a", "c": "#ff6a1a"}
    return art([r[:16] for r in rows], colors)


def soul_compass():
    rows = ["", "",
            ".....oooooo.....",
            "...ooffffffoo...",
            "..offccccccffo..",
            "..ofcccccwcccfo.",
            ".ofcccccwwcccfo.",
            ".ofccccwnwccccfo",
            ".ofcccnnwccccfo.",
            ".ofccnnccccccfo.",
            "..ofcccccccccfo.",
            "..offcccccccffo.",
            "...ooffffffoo...",
            ".....oooooo....."]
    colors = {"o": "#0a1418", "f": "#3a3048", "c": "#1e3a44", "w": "#c4f8ff", "n": "#7fe8ff"}
    img = art([r[:16] for r in rows], colors)
    for (x, y) in ((6, 4), (5, 5)):
        img.putpixel((x, y), hexc("#5a7a88"))
    img.putpixel((8, 8), hexc("#ffffff"))
    return img


def recall_stone():
    shape = Shape()
    shape.ellipse(8, 8.5, 5.8, 6.4, hexc("#3e3450"))
    r = rng("recall_runes")
    for y in range(16):
        for x in range(16):
            if (x, y) in shape.pixels:
                dx, dy = x - 8, y - 8.5
                if -2 * dx + -1.5 * dy > 6:
                    shape.put(x, y, hexc("#5e5274"))
                elif 2 * dx + 1.5 * dy > 6:
                    shape.put(x, y, hexc("#241e30"))
                if r.random() < 0.06:
                    shape.put(x, y, hexc("#2e2840"))
    spiral = [(8, 9), (9, 9), (9, 8), (8, 7), (7, 7), (6, 8), (6, 9), (6, 10), (7, 11), (8, 11), (9, 11), (10, 10), (10, 9),
              (10, 8), (10, 7), (9, 6), (8, 5)]
    for i, (x, y) in enumerate(spiral):
        shape.put(x, y, hexc("#e8b8ff" if i < 3 else "#b06cff"))
    shape.put(5, 5, hexc("#8a7aa4"))
    return shape.render(outline=hexc("#120a18"))


WITHERED = {"o": "#0a0a0c", "d": "#1c1c22", "m": "#2c2c34", "l": "#44444e", "h": "#7fe8ff", "w": "#e0fbff"}


def withered_blade():
    # Lame flétrie : lame noire dentelée au fil cyan, manche en os, pommeau-crane
    saved = PALETTES.get("withered")
    PALETTES["withered"] = WITHERED

    def blade(u, v):
        ch = sword(u, v)
        if ch and -3 <= u <= 10 and v == 16 and u % 3 == 0:
            return None  # dents
        if ch in ("w", "h") and u < 9:
            return "h" if u % 2 else "l"
        return ch

    img = tool_image(blade, -6, "withered")
    if saved is None:
        del PALETTES["withered"]
    for (x, y) in ((1, 13), (2, 14)):
        img.putpixel((x, y), hexc("#d8d0c0"))
    return img


def soul_scythe():
    pal = {"o": "#08141a", "d": "#123842", "m": "#1e5a68", "l": "#3fb3c0", "h": "#7fe8ff", "w": "#e0fbff"}
    handle = {"a": "#1a1018", "b": "#2e2230", "c": "#443448"}
    shape = Shape()
    for y in range(16):
        for x in range(16):
            u, v = x - y, x + y
            ch = None
            # Manche long sur toute la diagonale
            if -12 <= u <= 10 and v in (15, 16):
                ch = "c" if v == 15 else "b"
                if u % 5 == 0:
                    ch = "a"
            # Lame : grande courbe qui part du haut du manche vers la gauche
            for k in range(0, 12):
                cu = 10 - k * 0.55 - (k * k) * 0.05
                cv = 14 - k
                if abs(u - cu) <= 0.9 + (0.6 if k < 6 else 0.0) and abs(v - cv) <= 0.6:
                    ch = "h" if k < 3 else "l" if u > cu else "m"
                    if k > 9:
                        ch = "w"
            if ch:
                shape.put(x, y, hexc({**pal, **handle}[ch]))
    return shape.render(outline=hexc(pal["o"]))


def bow_frame(pull):
    """Arc en onyx facon vanilla : bois courbe vers le haut-droite, corde sur la diagonale, fleche si tendu."""
    pal = PALETTES["onyx"]
    shape = Shape()
    for y in range(16):
        for x in range(16):
            u, v = x - y, x + y
            arc = 6.5 * (1.0 - ((v - 15) / 13.0) ** 2)
            if 2 <= v <= 28 and arc - 1.3 <= u <= arc + 0.2:
                if 13 <= v <= 17:
                    col = "#5a2a14" if u > arc - 0.6 else "#3a1a0a"  # poignee en cuir
                else:
                    col = pal["h"] if u > arc - 0.4 else pal["l"] if abs(v - 15) < 9 else pal["m"]
                shape.put(x, y, hexc(col))
            string_u = -pull * (1.0 - abs(v - 15) / 13.0)
            if 2 <= v <= 28 and abs(u - string_u) <= 0.5 and (x, y) not in shape.pixels:
                shape.put(x, y, hexc("#d8d0e8"))
            if pull and v in (15, 16) and string_u - 0.5 <= u <= 9 - pull:
                tip = u >= 9 - pull - 2
                shape.put(x, y, hexc("#e8e0f8" if tip else "#8a6a4a"))
    return shape.render(outline=hexc(pal["o"]))


def boat_paddles(img):
    """Pagaies du bateau en obsidienne (zone des pagaies de la texture 128x64)."""
    r = rng("paddles")
    obsidian = ["#0d0716", "#140c22", "#1c1230", "#26193e"]
    for y in range(0, 40):
        for x in range(62, 128):
            col = hexc(obsidian[int(r.random() * 3.99)])
            if r.random() < 0.06:
                col = hexc("#7a4fc0")
            img.putpixel((x, y), col)
    return img


def generate():
    save(ore_overlay("onyx", ONYX_TONES, [(2, 2), (10, 1), (6, 7), (12, 9), (1, 11), (8, 13)]), "block", "onyx_ore_overlay.png")
    save(ore_overlay("sulfur", SULFUR_TONES, [(3, 3), (11, 2), (7, 8), (1, 10), (11, 12)]), "block", "sulfur_ore_overlay.png")
    save(ore_overlay("quartz", QUARTZ_TONES, [(2, 1), (9, 2), (5, 6), (12, 7), (2, 11), (8, 12), (13, 13)]),
         "block", "rich_quartz_ore_overlay.png")
    for soil in ("ember_nylium", "nether_grass_block", "ossified_nylium", "wart_moss"):
        save(soil_side_overlay(soil, soil), "block", f"{soil}_side_overlay.png")

    sets = {
        "onyx": ("onyx_helmet", "onyx_chestplate", "onyx_leggings", "onyx_boots"),
        "ember": ("ember_helmet", "ember_chestplate", "ember_leggings", "ember_boots"),
        "wraith": ("wraith_hood", "wraith_robe", "wraith_leggings", "wraith_boots"),
        "carapace": ("carapace_helmet", "carapace_chestplate", "carapace_leggings", "carapace_boots"),
    }
    for pal, names in sets.items():
        for piece, name in zip(("helmet", "chestplate", "leggings", "boots"), names):
            save(armor_v2(piece, pal), "item", f"{name}.png")
    save(armor_v2("boots", "magma"), "item", "magma_treads.png")

    save(cauldron_item(), "item", "infernal_cauldron.png")
    save(infernal_heart(), "item", "infernal_heart.png")
    save(basalt_carapace(), "item", "basalt_carapace.png")
    save(soul_compass(), "item", "soul_compass.png")
    save(recall_stone(), "item", "recall_stone.png")
    save(withered_blade(), "item", "withered_blade.png")
    save(soul_scythe(), "item", "soul_scythe.png")
    save(bow_frame(0), "item", "onyx_bow.png")
    for i in range(3):
        save(bow_frame(i + 1), "item", f"onyx_bow_pulling_{i}.png")
    boat_path = asset_path("textures", "entity", "obsidian_boat.png")
    save(boat_paddles(Image.open(boat_path).convert("RGBA")), "entity", "obsidian_boat.png")
