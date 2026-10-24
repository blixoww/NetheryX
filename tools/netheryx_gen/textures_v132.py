"""Objets redessines : trophees des boss (Sceau du Sorcier, Oeil de la Sentinelle, Couronne du Champion, Pierre-coeur de
basalte), Coeur scelle, Coeur de sentinelle, Coeur infernal et eclat abyssal. Pixel art 16 x 16 a la maniere des objets
vanilla : contour sombre de la couleur du materiau, lumiere en haut a gauche, reflet blanc ponctuel."""
import math

from PIL import Image

from .pixels import hexc
from .textures import save


def canvas():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def put(img, x, y, color):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), hexc(color))


def outline(img, color):
    """Contour d'un pixel autour de toutes les zones opaques."""
    solid = {(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3] > 0}
    for y in range(16):
        for x in range(16):
            if (x, y) not in solid and any((x + dx, y + dy) in solid for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, color)


def sprite(rows, colors):
    img = canvas()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                put(img, x, y, colors[ch])
    return img


def warlock_sigil():
    """Medaillon d'or a anneau, email rouge sang et oeil de flamme au centre."""
    img = canvas()
    gold = ["#6a3e0c", "#a8701c", "#d8a43a", "#f6d670"]
    enamel = ["#2e0606", "#5a0c0a", "#861812"]
    cx, cy = 7.5, 9.0
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx - 0.5, y + 0.5 - cy - 0.5
            d = math.hypot(dx, dy)
            if d > 6.2:
                continue
            if d > 4.4:
                light = -(dx + dy) / max(0.1, d)  # +1 en haut a gauche
                put(img, x, y, gold[3] if light > 0.75 else gold[2] if light > 0.0 else gold[1] if light > -0.7 else gold[0])
            else:
                put(img, x, y, enamel[2] if d < 2.2 else enamel[1] if d < 3.6 else enamel[0])
    # Anneau de suspension
    for (x, y, c) in ((6, 1, gold[2]), (7, 0, gold[3]), (8, 0, gold[2]), (9, 1, gold[1]), (6, 2, gold[1]), (9, 2, gold[0])):
        put(img, x, y, c)
    # Oeil de flamme
    for x in range(6, 10):
        put(img, x, 7, "#e0601a")
        put(img, x, 10, "#e0601a")
    for x in range(5, 11):
        put(img, x, 8, "#ffb030")
        put(img, x, 9, "#ffb030")
    for (x, y) in ((7, 8), (8, 8), (7, 9), (8, 9)):
        put(img, x, y, "#1a0402")
    put(img, 7, 8, "#ffe9a0")
    for (x, y, c) in ((7, 5, "#ff8a2a"), (8, 4, "#ffd060"), (8, 5, "#ff8a2a"), (6, 6, "#e0601a"), (9, 6, "#e0601a")):
        put(img, x, y, c)
    put(img, 4, 6, "#fff8d8")  # reflet sur l'or
    outline(img, "#2a0e04")
    return img


def sentinel_eye():
    """Oeil d'onyx : orbite de pierre noire, iris violet lumineux, pupille fendue."""
    return sprite([
        "................",
        "................",
        "................",
        ".....oooooo.....",
        "...oo233332oo...",
        "..o2344444432o..",
        ".o234pPPPPp432o.",
        ".o34pPWvvPPp43o.",
        ".o34pPvkkvPp43o.",
        ".o34pPvkkvPp43o.",
        ".o234pPPPPp432o.",
        "..o2344444432o..",
        "...oo233332oo...",
        ".....oooooo.....",
        "................",
        "................",
    ], {"o": "#0c0812", "2": "#241a34", "3": "#3a2c52", "4": "#54426e", "p": "#6a3fc0", "P": "#9a6aea",
        "v": "#c8a8ff", "W": "#ffffff", "k": "#140820"})


def champion_crown():
    """Couronne de fer sombre a trois pointes, gemmes turquoise, bandeau rehausse."""
    return sprite([
        "................",
        "................",
        "................",
        "..o....o....o...",
        ".oho..oho..oho..",
        ".omo..omo..omo..",
        ".omdooomdooomdo.",
        ".ommmmmmmmmmmdo.",
        ".ohhhhhhhhhhhmo.",
        ".odaAdmaAdmaAdo.",
        ".odaadmaadmaado.",
        ".ommmmmmmmmmmdo.",
        ".odddddddddddoo.",
        "..ooooooooooo...",
        "................",
        "................",
    ], {"o": "#101014", "d": "#3a3a44", "m": "#5e5e6a", "h": "#9a9aa8", "a": "#1a9a80", "A": "#7fffe0"})


def sealed_heart():
    """Coeur rouge sombre barre d'une chaine, ferme par un cadenas d'or."""
    img = sprite([
        "................",
        "................",
        "..ooo....ooo....",
        ".oRhRo..oRRro...",
        "oRhRRRooRRRrro..",
        "oRRRRRRRRRRrro..",
        "oRRRRRRRRRRrro..",
        ".oRRRRRRRRrro...",
        "..oRRRRRRrro....",
        "...oRRRRrro.....",
        "....oRRrro......",
        ".....oRro.......",
        "......oo........",
        "................",
        "................",
        "................",
    ], {"o": "#2a0406", "R": "#8a1418", "r": "#5a0a0e", "h": "#d0484a"})
    # Chaine horizontale (maillons alternes) et cadenas d'or au centre
    for x in range(0, 14):
        if img.getpixel((x, 6))[3] > 0:
            put(img, x, 5, "#2a2a30")
            put(img, x, 6, "#b4b4c0" if x % 3 != 2 else "#5e5e6a")
            put(img, x, 7, "#6a6a76" if x % 3 != 2 else "#3a3a44")
    for (x, y, c) in ((5, 7, "#7a5010"), (6, 7, "#d8a43a"), (7, 7, "#7a5010"), (5, 8, "#d8a43a"), (6, 8, "#f6d670"),
                      (7, 8, "#a8701c"), (5, 9, "#a8701c"), (6, 9, "#3a2004"), (7, 9, "#7a5010")):
        put(img, x, y, c)
    return img


def sentinel_core():
    """Noyau d'onyx a facettes, fendu de lueurs violettes."""
    return sprite([
        "................",
        "................",
        "......oooo......",
        "....oo3443oo....",
        "...o33444442o...",
        "..o3344vv4422o..",
        "..o344vWvv422o..",
        ".o3344vvvv4222o.",
        ".o3444vvv42222o.",
        ".o344444422222o.",
        "..o34444222222o.",
        "..o33442222222o.",
        "...o222222222o..",
        "....oo22222oo...",
        "......oooo......",
        "................",
    ], {"o": "#0a0610", "2": "#22182e", "3": "#3a2c50", "4": "#54426e", "v": "#a67ef0", "W": "#f0e4ff"})


def infernal_heart():
    """Coeur de magma : roche sombre fendue de veines incandescentes."""
    img = sprite([
        "................",
        "................",
        "..ooo....ooo....",
        ".o334o..o3332o..",
        "o34442oo334422o.",
        "o34422223442222o",
        "o3422222222222do",
        ".o32222222222do.",
        "..o222222222do..",
        "...o2222222do...",
        "....o22222do....",
        ".....o222do.....",
        "......o2do......",
        ".......oo.......",
        "................",
        "................",
    ], {"o": "#1e0804", "2": "#4a1608", "3": "#7a2a10", "4": "#a84418", "d": "#2e0c06"})
    for (x, y) in ((4, 6), (5, 7), (6, 7), (7, 8), (8, 8), (9, 7), (10, 6), (11, 6), (7, 9), (7, 10), (8, 11)):
        put(img, x, y, "#ff7a1a")
    for (x, y) in ((6, 7), (8, 8), (10, 6)):
        put(img, x, y, "#ffd060")
    put(img, 3, 4, "#ffb070")
    return img


def abyssal_shard():
    """Eclat de cristal abyssal : pointe rouge sang au coeur incandescent."""
    return sprite([
        "................",
        "..........oo....",
        ".........oRho...",
        "........oRRho...",
        ".......oRReRo...",
        "......oRReeRo...",
        ".....oRReeRro...",
        "....oRReeRrro...",
        "...oRReEeRro....",
        "...oRReeRrro....",
        "..oRRReRrro.....",
        "..oRRRRrro......",
        "..orRRrro.......",
        "...orrro........",
        "....ooo.........",
        "................",
    ], {"o": "#160408", "R": "#7a1620", "r": "#46080e", "h": "#d0505a", "e": "#ff6a2a", "E": "#ffd060"})


def generate():
    save(warlock_sigil(), "item", "warlock_sigil.png")
    save(sentinel_eye(), "item", "sentinel_eye.png")
    save(champion_crown(), "item", "champion_crown.png")
    save(sealed_heart(), "item", "sealed_heart.png")
    save(sentinel_core(), "item", "sentinel_core.png")
    save(infernal_heart(), "item", "infernal_heart.png")
    save(abyssal_shard(), "item", "abyssal_shard.png")
