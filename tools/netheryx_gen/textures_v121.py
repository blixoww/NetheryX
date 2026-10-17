"""Refonte facon diamant : outils et armes en onyx, icones d'armure, armures portees (3D) et bloc d'onyx facon emeraude."""
from PIL import Image

from .pixels import Shape, hexc, mix, new, rng, shade
from .textures import save

# Palettes : o contour, d sombre, m moyen, l clair, h reflet, w eclat
PALETTES = {
    "onyx": {"o": "#0b0710", "d": "#1d1428", "m": "#33244a", "l": "#5a3f86", "h": "#9a70e8", "w": "#efe4ff"},
    "ember": {"o": "#2a0a04", "d": "#6a1e08", "m": "#a8380e", "l": "#e0601a", "h": "#ffa040", "w": "#ffe6a0"},
    "wraith": {"o": "#16161c", "d": "#3a3a44", "m": "#5a5a66", "l": "#80808e", "h": "#b8bccc", "w": "#c4f8ff"},
    "carapace": {"o": "#0b0b0d", "d": "#24242a", "m": "#36363e", "l": "#4c4c56", "h": "#74747e", "w": "#ff7a2a"},
    "magma": {"o": "#2a0804", "d": "#5a1606", "m": "#8a2a0a", "l": "#c8460e", "h": "#ff8a1a", "w": "#ffd36b"},
}
HANDLE = {"a": "#2a100a", "b": "#4a1e10", "c": "#6e3018", "g": "#b06cff", "G": "#efe4ff"}


def art(rows, colors):
    img = new()
    for y, row in enumerate(rows):
        assert len(row) <= 16, row
        for x, ch in enumerate(row):
            if ch in colors:
                img.putpixel((x, y), hexc(colors[ch]))
    return img


# ------------------------------------------------------------------------------------------------
# Outils et armes : construits sur la diagonale (u = x - y le long du manche, v = x + y en travers)
# ------------------------------------------------------------------------------------------------

def tool_image(head_fn, handle_end, pal):
    """head_fn(u, v) -> lettre de palette ou None ; le manche suit v = 15..16 jusqu'a u = handle_end."""
    colors = dict(PALETTES[pal])
    colors.update(HANDLE)
    shape = Shape()
    for y in range(16):
        for x in range(16):
            u, v = x - y, x + y
            ch = head_fn(u, v)
            if ch is None and -11 <= u <= handle_end and v in (15, 16):
                ch = "c" if v == 15 else "b"
                if u % 4 == 0:
                    ch = "a"  # bandes de cuir
            if ch is None and u in (-13, -12) and v in (14, 15, 16):
                ch = "g" if (u, v) != (-12, 15) else "G"  # pommeau : gemme d'onyx violette
            if ch:
                shape.put(x, y, hexc(colors[ch]))
    return shape.render(outline=hexc(colors["o"]))


def facet(v, near, far):
    """Ombrage en travers d'une lame : reflet cote haut-gauche, ombre cote bas-droit."""
    t = (v - near) / max(1, far - near)
    return "w" if t <= 0.0 else "h" if t < 0.34 else "l" if t < 0.67 else "m" if t < 1.0 else "d"


def sword(u, v):
    if -3 <= u <= 10 and 13 <= v <= 16:
        if u >= 9 and v in (13, 16):
            return None
        if v == 13:
            return "w" if u % 4 == 1 else "h"
        return {14: "l", 15: "m", 16: "d"}[v]
    if u == 11 and v in (14, 15):
        return "h"
    if u in (-5, -4) and 10 <= v <= 20:
        return "h" if v <= 12 else "l" if v < 16 else "m" if v < 19 else "d"
    return None


def pickaxe(u, v):
    if 6 <= v <= 24:
        center = 8.6 - 4.2 * ((v - 15) / 9.0) ** 2
        if center - 1.7 <= u <= center + 1.0:
            if abs(v - 15) >= 8 and u < center - 0.7:
                return None
            return "h" if u >= center + 0.4 else "l" if u >= center - 0.6 else "m" if u >= center - 1.2 else "d"
    return None


def axe(u, v):
    if 1 <= u <= 9 and 16 <= v <= 23:
        if v - 16 > u * 1.8 or (u == 9 and v > 21):
            return None
        return "w" if v == 23 and u in (6, 7) else "h" if v >= 22 else "l" if v >= 19 else "m"
    if 5 <= u <= 9 and 13 <= v <= 14:
        return "d" if v == 13 else "m"
    return None


def shovel(u, v):
    du, dv = (u - 7.5) / 4.6, (v - 15.5) / 4.1
    if du * du + dv * dv <= 1.0 and u >= 3:
        return "w" if (u, v) in ((9, 13), (10, 14)) else "h" if v <= 13 else "l" if v <= 15 else "m" if v <= 17 else "d"
    return None


def hoe(u, v):
    if 6 <= u <= 8 and 14 <= v <= 23:
        return "h" if u == 8 else "l" if u == 7 else "m"
    if 3 <= u <= 5 and 21 <= v <= 23:
        return "l" if v == 21 else "m" if v == 22 else "d"
    return None


def hammer(u, v):
    if 3 <= u <= 9 and 8 <= v <= 23:
        if v in (8, 23) and u in (3, 9):
            return None
        if v in (8, 23):
            return "d"
        return "w" if (u, v) in ((8, 10), (8, 11)) else "h" if u >= 8 else "l" if u >= 6 else "m" if u >= 4 else "d"
    return None


TOOLS = {"onyx_sword": (sword, -6), "onyx_pickaxe": (pickaxe, 6), "onyx_axe": (axe, 6), "onyx_shovel": (shovel, 3),
         "onyx_hoe": (hoe, 5), "onyx_hammer": (hammer, 4)}


# ------------------------------------------------------------------------------------------------
# Icones d'armure facon diamant
# ------------------------------------------------------------------------------------------------

ARMOR_ART = {
    "helmet": ["", "", "",
               "...oooooooooo...",
               "..ohwhhhhhhlmo..",
               "..ohhllllllmdo..",
               "..ohllllllmmdo..",
               "..olmooooooldo..",
               "..olmo....omdo..",
               "..olmo....omdo..",
               "..oddo....oddo..",
               "...oo......oo..."],
    "chestplate": ["",
                   "..ooo......ooo..",
                   ".ohhlo....ohldo.",
                   ".ohllmooooolmdo.",
                   ".ohwhhhhhhhlmdo.",
                   ".olhllllllllmdo.",
                   "..oohlllllllmoo.",
                   "...ohlllllllmo..",
                   "...olhlllllmdo..",
                   "...olmllllmmdo..",
                   "...olmmmmmmmdo..",
                   "...oddddddddoo..",
                   "....oooooooo...."],
    "leggings": ["", "",
                 "...oooooooooo...",
                 "...ohwhhhhhlo...",
                 "...ohllllllmo...",
                 "...olmmmmmmdo...",
                 "...olmdoodmdo...",
                 "...olmo..olmo...",
                 "...olmo..olmo...",
                 "...olmo..olmo...",
                 "...ohmo..ohmo...",
                 "...olmo..olmo...",
                 "...oddo..oddo...",
                 "....oo....oo...."],
    "boots": ["", "", "", "", "",
              "..oooo....oooo..",
              "..ohlo....ohlo..",
              "..olmo....olmo..",
              "..olmo....olmo..",
              "..olmo....olmo..",
              ".ohlmo...ohlmo..",
              "ohwlmo..ohwlmo..",
              "ohllmo..ohllmo..",
              "oddddo..oddddo..",
              "oooooo..oooooo.."],
}


def armor_item(piece, pal):
    img = art(ARMOR_ART[piece], PALETTES[pal])
    if pal == "carapace":
        # Craquelures de lave caracteristiques de la carapace de basalte
        for (x, y) in {"helmet": ((6, 5), (9, 6)), "chestplate": ((5, 7), (9, 9), (7, 11)), "leggings": ((6, 5), (10, 9)),
                       "boots": ((3, 11), (10, 11))}[piece]:
            if img.getpixel((x, y))[3]:
                img.putpixel((x, y), hexc("#ff7a2a"))
    return img


# ------------------------------------------------------------------------------------------------
# Armure portee : textures 64x32 au format des armures vanilla, plaques facettees
# ------------------------------------------------------------------------------------------------

# (x, y, largeur, hauteur, luminosite) de chaque face des boites vanilla
def box_faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d, 1.15), "bottom": (u + d + w, v, w, d, 0.7),
        "right": (u, v + d, d, h, 0.85), "front": (u + d, v + d, w, h, 1.0),
        "left": (u + d + w, v + d, d, h, 0.85), "back": (u + 2 * d + w, v + d, w, h, 0.8),
    }


HEAD = box_faces(0, 0, 8, 8, 8)
BODY = box_faces(16, 16, 8, 12, 4)
ARM = box_faces(40, 16, 4, 12, 4)
LEG = box_faces(0, 16, 4, 12, 4)


def paint_face(img, rect, pal, rows=None, seed="", cutout=None):
    """Plaque d'armure : degrade, liseré clair en haut a gauche, sombre en bas a droite, eclats en diagonale."""
    x0, y0, w, h, light = rect
    colors = {k: hexc(c) for k, c in PALETTES[pal].items()}
    r = rng(seed)
    first, last = (0, h) if rows is None else rows
    for yy in range(first, last):
        for xx in range(w):
            if cutout and cutout(xx, yy):
                continue
            t = yy / max(1, h - 1)
            base = mix(colors["l"], colors["m"], t)
            if xx == 0 or yy == first:
                base = colors["h"]
            elif xx == w - 1 or yy == last - 1:
                base = colors["d"]
            elif xx + (yy - first) in (2, 3) and w >= 4:
                base = mix(base, colors["h"], 0.7)
            img.putpixel((x0 + xx, y0 + yy), shade(base, light))
    # Eclat facon gemme
    if w >= 4 and last - first >= 3:
        gx, gy = x0 + 1 + r.randrange(max(1, w - 2)), y0 + first + 1
        img.putpixel((gx, gy), colors["w"])


def armor_layers(pal, accent=None):
    """Retourne (couche 1 : casque, plastron, bottes ; couche 2 : jambieres)."""
    l1 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    l2 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Casque : toute la tete sauf le dessous, visiere ouverte devant les yeux
    for name, rect in HEAD.items():
        if name == "bottom":
            continue
        cut = (lambda xx, yy: 1 <= xx <= 6 and 4 <= yy <= 6) if name == "front" else None
        rows = (0, rect[3]) if name in ("top", "front", "back") else (0, 7)
        paint_face(l1, rect, pal, rows, f"{pal}head{name}", cut)
    # Plastron : torse complet et haut des bras
    for name, rect in BODY.items():
        if name != "bottom":
            paint_face(l1, rect, pal, None, f"{pal}body{name}")
    for name, rect in ARM.items():
        if name == "bottom":
            continue
        paint_face(l1, rect, pal, None if name == "top" else (0, 6), f"{pal}arm{name}")
    # Bottes : bas des jambes et semelle
    for name, rect in LEG.items():
        if name == "top":
            continue
        paint_face(l1, rect, pal, None if name == "bottom" else (7, 12), f"{pal}boot{name}")
    # Jambieres : ceinture et cuisses
    for name, rect in BODY.items():
        if name in ("front", "back", "left", "right"):
            paint_face(l2, rect, pal, (8, 12), f"{pal}belt{name}")
    for name, rect in LEG.items():
        if name == "bottom":
            continue
        paint_face(l2, rect, pal, None if name == "top" else (0, 9), f"{pal}leg{name}")
    if accent:
        for img in (l1, l2):
            r = rng(pal + "accent")
            for _ in range(40):
                x, y = r.randrange(64), r.randrange(32)
                if img.getpixel((x, y))[3]:
                    img.putpixel((x, y), hexc(accent))
    return l1, l2


# ------------------------------------------------------------------------------------------------
# Bloc d'onyx facon bloc d'emeraude : cadre biseaute et facettes en losange
# ------------------------------------------------------------------------------------------------

def onyx_block():
    c = {k: hexc(v) for k, v in PALETTES["onyx"].items()}
    img = new()
    for y in range(16):
        for x in range(16):
            if x == 0 or y == 0:
                col = c["h"]
            elif x == 15 or y == 15:
                col = c["o"]
            elif x == 1 or y == 1:
                col = c["l"]
            elif x == 14 or y == 14:
                col = c["d"]
            else:
                dist = abs(x - 7.5) + abs(y - 7.5)
                upper = x + y < 15
                if dist <= 1.0:
                    col = c["w"]
                elif dist <= 3.0:
                    col = c["h"] if upper else c["l"]
                elif dist <= 3.6:
                    col = c["d"]
                elif dist <= 5.6:
                    col = c["l"] if upper else c["m"]
                elif dist <= 6.2:
                    col = c["d"]
                else:
                    col = c["m"] if upper else c["d"]
            img.putpixel((x, y), col)
    return img


# ------------------------------------------------------------------------------------------------
# Enclume et four en onyx : memes compositions que les textures vanilla, en metal d'onyx
# ------------------------------------------------------------------------------------------------

METAL = ["#2e2838", "#363042", "#3e374c", "#463e56"]


def metal_noise(seed, palette=METAL):
    r = rng(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), hexc(palette[min(len(palette) - 1, int(r.random() * len(palette) * 0.999))]))
    return img


def anvil_body():
    img = metal_noise("anvil_body")
    for y in range(16):
        for x in range(16):
            c = img.getpixel((x, y))
            if y % 4 == 0:
                c = shade(c, 1.35)          # arete claire de chaque bande, comme l'enclume vanilla
            elif y % 4 == 3:
                c = shade(c, 0.7)
            if x in (0, 15):
                c = shade(c, 0.75)
            img.putpixel((x, y), c)
    for (x, y) in ((3, 5), (11, 9), (6, 13)):
        img.putpixel((x, y), hexc("#6a50a0"))
    return img


def anvil_top():
    img = metal_noise("anvil_top", ["#3e3650", "#463d5a", "#4e4464"])
    for y in range(16):
        for x in range(16):
            c = img.getpixel((x, y))
            if x in (0, 15) or y in (0, 15):
                c = hexc("#120e18")
            elif x in (1, 14) or y in (1, 14):
                c = shade(c, 1.3)
            elif 3 <= x <= 12 and 5 <= y <= 10:
                # Table de frappe polie (zone usee au centre)
                c = hexc("#20182c") if y == 5 or x == 3 else hexc("#7a60b0") if y == 10 or x == 12 else shade(hexc("#5a4680"), 0.9 + 0.2 * ((x + y) % 2))
            img.putpixel((x, y), c)
    img.putpixel((5, 6), hexc("#9a70e8"))
    img.putpixel((6, 6), hexc("#9a70e8"))
    return img


def furnace_tile(seed, band=True):
    img = metal_noise(seed, ["#34303c", "#3c3746", "#443e50", "#4c455a"])
    for y in range(16):
        for x in range(16):
            c = img.getpixel((x, y))
            if x == 0 or y == 0:
                c = shade(c, 1.4)
            elif x == 15 or y == 15:
                c = shade(c, 0.6)
            if band and y in (7, 8):
                c = shade(c, 1.25 if y == 7 else 0.65)  # bandeau horizontal du four vanilla
            img.putpixel((x, y), c)
    return img


def furnace_top():
    img = furnace_tile("furnace_top", band=False)
    for k in range(3, 13):
        for (x, y) in ((k, 3), (k, 12), (3, k), (12, k)):
            img.putpixel((x, y), shade(img.getpixel((x, y)), 0.75))
    return img


def furnace_front(lit):
    img = furnace_tile("furnace_front")
    # Linteau au-dessus de la bouche
    for x in range(2, 14):
        img.putpixel((x, 8), hexc("#4a3870"))
    for y in range(9, 15):
        for x in range(3, 13):
            if not lit:
                c = hexc("#08050c")
            else:
                depth = (y - 9) / 5.0
                c = mix(hexc("#ff6a1a"), hexc("#ffd36b"), depth) if y >= 12 else mix(hexc("#b06cff"), hexc("#ff7a2a"), depth * 1.6)
                if (x + y) % 3 == 0 and y < 12:
                    c = hexc("#08050c")
            img.putpixel((x, y), c)
    # Grille de la bouche
    for x in (5, 8, 11):
        for y in range(9, 15):
            img.putpixel((x, y), hexc("#3a2c58"))
    for x in range(3, 13):
        img.putpixel((x, 9), hexc("#2a2036"))
    return img


def generate_workshops():
    save(anvil_body(), "block", "onyx_anvil.png")
    save(anvil_top(), "block", "onyx_anvil_top.png")
    save(furnace_tile("furnace_side"), "block", "onyx_furnace_side.png")
    save(furnace_top(), "block", "onyx_furnace_top.png")
    save(furnace_front(False), "block", "onyx_furnace_front.png")
    save(furnace_front(True), "block", "onyx_furnace_front_on.png")


def admin_compass():
    shape = Shape()
    shape.disc(8, 8, 6.2, hexc("#c89a20"))
    shape.disc(8, 8, 4.6, hexc("#1a1022"), shaded=False)
    for (x, y, col) in ((8, 4, "#ff3a3a"), (8, 5, "#ff3a3a"), (8, 6, "#ffd36b"), (8, 7, "#ffffff"), (8, 8, "#ffffff"),
                        (8, 9, "#9a70e8"), (8, 10, "#7fe8ff"), (8, 11, "#7fe8ff")):
        shape.put(x, y, hexc(col))
    for (x, y) in ((4, 8), (12, 8), (8, 3), (8, 13)):
        shape.put(x, y, hexc("#ffe27a"))
    return shape.render(outline=hexc("#2a1a06"))


def fire_veil():
    rows = ["", "", "...oooooooo.....", "..ohhhhhhhho....", "..ohwhhhhhlo....", "..ohhhllllloo...",
            "..ohhlllllmmo...", "...ohllllmmo....", "...ohlllmmmo....", "....ohllmmo.....", "....ohlmmmo.....",
            ".....ohmmo......", ".....odddo......", "......ooo......."]
    colors = {"o": "#1a1a22", "w": "#ffffff", "h": "#d8e8f0", "l": "#a8c8d8", "m": "#7aa0b8", "d": "#4a6878"}
    img = art([r[:16] for r in rows], colors)
    for (x, y) in ((6, 7), (7, 9), (8, 6)):
        img.putpixel((x, y), hexc("#ff8a2a"))
    return img


def generate():
    save(admin_compass(), "item", "admin_compass.png")
    save(fire_veil(), "item", "fire_veil.png")
    for name, (fn, end) in TOOLS.items():
        save(tool_image(fn, end, "onyx"), "item", f"{name}.png")
    sets = {
        "onyx": ("onyx_helmet", "onyx_chestplate", "onyx_leggings", "onyx_boots"),
        "ember": ("ember_helmet", "ember_chestplate", "ember_leggings", "ember_boots"),
        "wraith": ("wraith_hood", "wraith_robe", "wraith_leggings", "wraith_boots"),
        "carapace": ("carapace_helmet", "carapace_chestplate", "carapace_leggings", "carapace_boots"),
    }
    for pal, names in sets.items():
        for piece, name in zip(("helmet", "chestplate", "leggings", "boots"), names):
            save(armor_item(piece, pal), "item", f"{name}.png")
    save(armor_item("boots", "magma"), "item", "magma_treads.png")
    for pal, accent in (("onyx", None), ("ember", "#ffd36b"), ("wraith", "#c4f8ff"), ("carapace", "#ff7a2a"), ("magma", "#ffd36b")):
        l1, l2 = armor_layers(pal, accent)
        save(l1, "models", "armor", f"{pal}_layer_1.png")
        save(l2, "models", "armor", f"{pal}_layer_2.png")
    block = onyx_block()
    save(block, "block", "onyx_block.png")
    generate_workshops()
