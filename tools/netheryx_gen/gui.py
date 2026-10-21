"""Textures d'interface (style Minecraft : fond gris, biseaux clairs en haut a gauche, sombres en bas a droite)."""
from PIL import Image

from .common import asset_path
from .pixels import hexc

BG = hexc("#c6c6c6")
LIGHT = hexc("#ffffff")
DARK = hexc("#555555")
EDGE = hexc("#000000")
SLOT = hexc("#8b8b8b")
SOUL_DARK = hexc("#123a44")
SOUL = hexc("#2a8ca0")
SOUL_LIGHT = hexc("#7fe8ff")
GHOST = hexc("#7a7a7a")


def panel(img, x0, y0, w, h):
    """Cadre arrondi standard des interfaces vanilla."""
    x1, y1 = x0 + w - 1, y0 + h - 1
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            img.putpixel((x, y), BG)
    for x in range(x0 + 2, x1 - 1):
        img.putpixel((x, y0), EDGE)
        img.putpixel((x, y1), EDGE)
        img.putpixel((x, y0 + 1), LIGHT)
        img.putpixel((x, y0 + 2), LIGHT)
        img.putpixel((x, y1 - 1), DARK)
        img.putpixel((x, y1 - 2), DARK)
    for y in range(y0 + 2, y1 - 1):
        img.putpixel((x0, y), EDGE)
        img.putpixel((x1, y), EDGE)
        img.putpixel((x0 + 1, y), LIGHT)
        img.putpixel((x0 + 2, y), LIGHT)
        img.putpixel((x1 - 1, y), DARK)
        img.putpixel((x1 - 2, y), DARK)
    for (x, y) in ((x0 + 1, y0 + 1), (x1 - 1, y1 - 1)):
        img.putpixel((x, y), EDGE)
    img.putpixel((x0 + 1, y1 - 1), EDGE)
    img.putpixel((x1 - 1, y0 + 1), EDGE)
    img.putpixel((x0 + 3, y0 + 3), LIGHT)
    img.putpixel((x1 - 3, y1 - 3), DARK)


def inset(img, x0, y0, w, h, fill=SLOT, top=DARK, bottom=LIGHT):
    """Zone en creux (emplacement d'objet, cadre de texte)."""
    x1, y1 = x0 + w - 1, y0 + h - 1
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            img.putpixel((x, y), fill)
    for x in range(x0, x1 + 1):
        img.putpixel((x, y0), top)
        img.putpixel((x, y1), bottom)
    for y in range(y0, y1 + 1):
        img.putpixel((x0, y), top)
        img.putpixel((x1, y), bottom)
    img.putpixel((x1, y0), BG)
    img.putpixel((x0, y1), BG)


def slot(img, x, y):
    inset(img, x, y, 18, 18)


def soul_frame(img, x, y, w, h):
    """Liseré des ames autour d'un emplacement de l'autel."""
    for k in range(w):
        for (yy, c) in ((y, SOUL), (y + h - 1, SOUL_DARK)):
            img.putpixel((x + k, yy), c)
    for k in range(h):
        img.putpixel((x, y + k), SOUL)
        img.putpixel((x + w - 1, y + k), SOUL_DARK)
    for (cx, cy) in ((x, y), (x + w - 1, y), (x, y + h - 1), (x + w - 1, y + h - 1)):
        img.putpixel((cx, cy), SOUL_LIGHT)


def ghost(img, x, y, rows):
    """Silhouette pale dans un emplacement vide (indique ce qu'on peut y poser)."""
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch == "#":
                img.putpixel((x + dx, y + dy), GHOST)


BOOK = ["................",
        "...##########...",
        "..#.........#...",
        "..#.######..#...",
        "..#.........#...",
        "..#.#####...#...",
        "..#.........#...",
        "..#.######..#...",
        "..#.........#...",
        "..#.........#...",
        "..#.........#...",
        "..###########...",
        "...#.........#..",
        "....##########..",
        "................",
        "................"]
DUST = ["................",
        "................",
        "................",
        ".......#........",
        "......#.#.......",
        ".....#...#......",
        "......#.#...#...",
        ".......#...#.#..",
        "............#...",
        "....#...........",
        "...#.#....##....",
        "....#....#..#...",
        "....######.###..",
        "...#..........#.",
        "..############..",
        "................"]


def flame_icon(lit):
    rows = ["....#....",
            "...##....",
            "...###...",
            "..####...",
            "..#####..",
            ".######..",
            ".###.###.",
            ".##...##.",
            ".##...##.",
            "..##.##..",
            "...###...",
            "........."]
    img = Image.new("RGBA", (9, 12), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "#":
                inner = 2 <= x <= 6 and y >= 6
                if lit:
                    img.putpixel((x, y), SOUL_LIGHT if inner else (SOUL if y > 3 else hexc("#c8fbff")))
                else:
                    img.putpixel((x, y), hexc("#6a6a6a") if inner else hexc("#4a4a4a"))
    return img


def soul_altar():
    img = Image.new("RGBA", (512, 256), (0, 0, 0, 0))
    # Panneau principal (176 x 186)
    panel(img, 0, 0, 176, 186)
    for (x, y) in ((16, 19), (16, 61)):
        soul_frame(img, x - 2, y - 2, 22, 22)
        slot(img, x, y)
    ghost(img, 17, 20, BOOK)
    ghost(img, 17, 62, DUST)
    # Canal du rituel entre les deux emplacements (rempli par l'ecran selon l'avancement)
    inset(img, 20, 40, 10, 20, fill=hexc("#1c1c1c"))
    # Cadre d'information et emplacement du bouton
    inset(img, 42, 17, 127, 46, fill=hexc("#2b2b2b"), top=hexc("#373737"), bottom=LIGHT)
    for row in range(3):
        for col in range(9):
            slot(img, 7 + col * 18, 103 + row * 18)
    for col in range(9):
        slot(img, 7 + col * 18, 161)
    # Panneau du guide des offrandes (104 x 124)
    panel(img, 176, 0, 104, 124)
    for k in range(3):
        inset(img, 182, 17 + k * 34, 92, 32, fill=hexc("#b4b4b4"), top=DARK, bottom=LIGHT)
        slot(img, 185, 24 + k * 34 - 3)
    # Icones : flamme allumee, eteinte, canal plein
    img.alpha_composite(flame_icon(True), (284, 0))
    img.alpha_composite(flame_icon(False), (294, 0))
    for y in range(18):
        for x in range(8):
            t = y / 17
            c = (int(127 * (1 - t) + 18 * t), int(232 * (1 - t) + 140 * t), int(255 * (1 - t) + 160 * t), 255)
            img.putpixel((304 + x, y), c if (x + y) % 5 else SOUL_LIGHT)
    return img


def generate():
    path = asset_path("textures", "gui", "soul_altar.png")
    path.parent.mkdir(parents=True, exist_ok=True)
    soul_altar().save(path)
