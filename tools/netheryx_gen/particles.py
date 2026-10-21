"""Particules : textures animees (8x8, une image par etape de vie) et definitions JSON."""
from .common import asset_path, write_json
from .pixels import hexc, new


def frame(rows, colors):
    img = new(8)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                img.putpixel((x, y), hexc(colors[ch][0], colors[ch][1]) if isinstance(colors[ch], tuple) else hexc(colors[ch]))
    return img


EMBER = {"w": "#fff6c8", "y": "#ffd34a", "o": "#ff8a1a", "r": ("#c8320a", 200)}
ASH = {"w": "#f2f0ee", "l": "#d0ccc8", "g": ("#a8a29e", 200)}
SOUL = {"w": "#effeff", "c": "#7fe8ff", "b": ("#2bb8d8", 210), "d": ("#16708a", 140)}
GLINT = {"w": "#ffffff", "p": "#d6b8ff", "v": ("#9a70e8", 220), "d": ("#5a3c96", 160)}
PUFF = {"y": ("#f4e27a", 230), "s": ("#d8c254", 190), "o": ("#a8952e", 140)}
POLLEN = {"w": "#fff0d8", "p": "#ffb08a", "o": ("#ff7a4a", 210)}
SPORE = {"l": "#9c94a8", "g": "#6a6274", "d": ("#3a3442", 210)}
FROST = {"w": "#ffffff", "c": "#c8f0ff", "b": ("#7fd0f8", 200)}
QUARTZ = {"w": "#ffffff", "c": "#f4ece0", "b": ("#c8beb4", 200)}

PARTICLES = {
    "ember_spark": [
        (["........", "...r....", "..ryr...", ".rywyr..", "..ryr...", "...r....", "........", "........"], EMBER),
        (["........", "........", "...o....", "..oyo...", "...o....", "........", "........", "........"], EMBER),
        (["........", "........", "...o....", "..oyo...", "...r....", "........", "........", "........"], EMBER),
        (["........", "........", "........", "...or...", "...r....", "........", "........", "........"], EMBER),
        (["........", "........", "........", "...r....", "........", "........", "........", "........"], EMBER),
    ],
    "ash_flake": [
        (["........", "........", "..gl....", "..lwlg..", "...lw...", "....g...", "........", "........"], ASH),
        (["........", "........", "...g....", "..lwl...", "..gwlg..", "...l....", "........", "........"], ASH),
        (["........", "........", "........", "..glwg..", "...wl...", "........", "........", "........"], ASH),
    ],
    "soul_wisp": [
        (["........", "...dd...", "..bccb..", ".dcwwcd.", ".dcwwcd.", "..bccb..", "...bd...", "....d..."], SOUL),
        (["........", "........", "...bb...", "..bcwb..", "..cwwc..", "..bccb..", "...bd...", "...d...."], SOUL),
        (["........", "........", "...bb...", "..bwcb..", "..bccb..", "...bd...", "...d....", "........"], SOUL),
        (["........", "........", "........", "...cb...", "..bccd..", "...bd...", "........", "........"], SOUL),
    ],
    "onyx_glint": [
        (["........", "...d....", "...v....", ".dvwvd..", "...v....", "...d....", "........", "........"], GLINT),
        (["...d....", "...v....", "...p....", "dvpwpvd.", "...p....", "...v....", "...d....", "........"], GLINT),
        (["........", "...v....", "..dpd...", ".vpwpv..", "..dpd...", "...v....", "........", "........"], GLINT),
        (["........", "........", "...v....", "..vwv...", "...v....", "........", "........", "........"], GLINT),
    ],
    "sulfur_puff": [
        (["........", "........", "...ys...", "..yyys..", "..syso..", "...so...", "........", "........"], PUFF),
        (["........", "..ys....", ".yyyss..", ".yysyso.", ".sysso..", "..soo...", "........", "........"], PUFF),
        (["........", ".ys.s...", "yyss.so.", ".ys.sso.", "s.so.o..", ".o..o...", "........", "........"], PUFF),
        (["........", "s...o...", ".s.o..o.", "o..s....", "..o..o..", "o.......", "........", "........"], PUFF),
    ],
    "ember_pollen": [
        (["........", "........", "...o....", "..opo...", "...pw...", "....o...", "........", "........"], POLLEN),
        (["........", "........", "........", "...po...", "...wp...", "........", "........", "........"], POLLEN),
    ],
    "frost_mote": [
        (["........", "........", "...b....", "..bwb...", "...b....", "........", "........", "........"], FROST),
        (["........", "........", "..b.b...", "...w....", "..b.b...", "........", "........", "........"], FROST),
        (["........", "........", "........", "...c....", "........", "........", "........", "........"], FROST),
    ],
    "quartz_glint": [
        (["........", "...b....", "...c....", ".bcwcb..", "...c....", "...b....", "........", "........"], QUARTZ),
        (["...b....", "...c....", "...c....", "bccwccb.", "...c....", "...c....", "...b....", "........"], QUARTZ),
        (["........", "........", "...c....", "..cwc...", "...c....", "........", "........", "........"], QUARTZ),
    ],
    "wither_spore": [
        (["........", "........", "...g....", "..glgd..", "...gd...", "........", "........", "........"], SPORE),
        (["........", "........", "...d....", "..gld...", "..dgg...", "........", "........", "........"], SPORE),
        (["........", "........", "........", "...gd...", "...d....", "........", "........", "........"], SPORE),
    ],
}


def generate():
    for name, frames in PARTICLES.items():
        textures = []
        for i, (rows, colors) in enumerate(frames):
            path = asset_path("textures", "particle", f"{name}_{i}.png")
            path.parent.mkdir(parents=True, exist_ok=True)
            frame(rows, colors).save(path)
            textures.append(f"netheryx:{name}_{i}")
        write_json(asset_path("particles", f"{name}.json"), {"textures": textures})
    generate_flames()


# ------------------------------------------------------------------------------------------------
# Jet de flammes de la wyverne : grandes bouffees de feu (32 x 32) qui gonflent et se dechirent au fil de leur vie.
# Les couleurs sont dans la texture ; la particule les affiche en melange additif (elles brillent).
# ------------------------------------------------------------------------------------------------
import math
import random


def _fbm(seed, size, octaves=4):
    """Bruit fractal lisse (non tuilable), valeurs dans [0, 1]."""
    r = random.Random(seed)
    layers = []
    for o in range(octaves):
        cells = 3 * (2 ** o)
        grid = [[r.random() for _ in range(cells + 2)] for _ in range(cells + 2)]
        layers.append((cells, grid, 0.5 ** o))
    norm = sum(w for _, _, w in layers)

    def sample(x, y):
        total = 0.0
        for cells, grid, w in layers:
            fx, fy = x / size * cells, y / size * cells
            ix, iy = int(fx), int(fy)
            tx, ty = fx - ix, fy - iy
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a = grid[iy][ix] + (grid[iy][ix + 1] - grid[iy][ix]) * tx
            b = grid[iy + 1][ix] + (grid[iy + 1][ix + 1] - grid[iy + 1][ix]) * tx
            total += (a + (b - a) * ty) * w
        return total / norm
    return sample


def _ramp(stops, t):
    t = max(0.0, min(1.0, t))
    for (t0, c0), (t1, c1) in zip(stops, stops[1:]):
        if t <= t1:
            k = (t - t0) / (t1 - t0) if t1 > t0 else 0.0
            return tuple(int(round(c0[i] + (c1[i] - c0[i]) * k)) for i in range(4))
    return stops[-1][1]


def _billow(name, i, n, stops, hole, size=32):
    """Une bouffee : disque deforme par le bruit, plus dechiquete et plus creux a mesure que t avance."""
    t = i / max(1, n - 1)
    noise = _fbm(f"{name}_{i}", size)
    swirl = _fbm(f"{name}_swirl_{i}", size, 3)
    img = new(size)
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            dx, dy = (x - c) / c, (y - c) / c
            d = math.hypot(dx, dy)
            n1 = noise(x, y)
            radius = 0.8 + 0.34 * (n1 - 0.5) * (1.0 + 1.6 * t) + 0.1 * (swirl(x, y) - 0.5)
            edge = 1.0 - d / max(0.05, radius)
            if edge <= 0:
                continue
            heat = edge ** (0.5 + 0.7 * t) * (0.8 + 0.5 * n1) * (1.0 - 0.38 * t)
            heat -= hole * t * max(0.0, 0.55 - n1) * 2.2
            if heat <= 0.02:
                continue
            col = _ramp(stops, heat)
            alpha = int(col[3] * min(1.0, heat * 2.6) * (1.0 - 0.35 * t))
            img.putpixel((x, y), (col[0], col[1], col[2], max(0, min(255, alpha))))
    return img


FLAME_STOPS = [(0.0, (120, 18, 4, 0)), (0.18, (200, 40, 8, 170)), (0.4, (255, 110, 20, 230)), (0.62, (255, 180, 50, 255)),
               (0.82, (255, 232, 140, 255)), (1.0, (255, 252, 230, 255))]
CORE_STOPS = [(0.0, (255, 150, 40, 0)), (0.3, (255, 210, 90, 220)), (0.7, (255, 245, 190, 255)), (1.0, (255, 255, 255, 255))]
SMOKE_STOPS = [(0.0, (28, 24, 22, 0)), (0.2, (46, 40, 36, 190)), (0.6, (66, 58, 52, 235)), (1.0, (88, 76, 68, 245))]
FLAMES = {
    "dragon_flame": (8, FLAME_STOPS, 0.9),
    "dragon_flame_core": (4, CORE_STOPS, 0.2),
    "dragon_smoke": (6, SMOKE_STOPS, 0.7),
}


def generate_flames():
    for name, (n, stops, hole) in FLAMES.items():
        textures = []
        for i in range(n):
            path = asset_path("textures", "particle", f"{name}_{i}.png")
            path.parent.mkdir(parents=True, exist_ok=True)
            _billow(name, i, n, stops, hole).save(path)
            textures.append(f"netheryx:{name}_{i}")
        write_json(asset_path("particles", f"{name}.json"), {"textures": textures})
