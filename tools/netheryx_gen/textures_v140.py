"""Textures 1.4.0 : annonces au sol et glyphes des combats de boss."""
import math

from PIL import Image

from .common import asset_path, write_json
from .textures import save


def telegraph_ring(size=64):
    """Contour epais et net : blanc (teinte a l'affichage), lisible de loin sur n'importe quel sol."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c) / c
            if 0.86 <= d <= 1.0:
                edge = min(d - 0.86, 1.0 - d) / 0.03
                img.putpixel((x, y), (255, 255, 255, int(255 * min(1.0, edge))))
            elif 0.78 <= d < 0.86:
                img.putpixel((x, y), (255, 255, 255, int(70 * (d - 0.78) / 0.08)))
    return img


def telegraph_fill(size=64):
    """Disque plein, un peu plus clair sur son bord : c'est lui qui grandit jusqu'a l'impact."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    c = (size - 1) / 2.0
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c) / c
            if d <= 1.0:
                alpha = 110 + 145 * d ** 4
                edge = min(1.0, (1.0 - d) / 0.03)
                img.putpixel((x, y), (255, 255, 255, int(alpha * edge)))
    return img


GLYPHS = (
    ["..####..", ".#....#.", "...##...", "..#..#..", "...##...", "...##...", "..####..", "........"],
    ["#......#", ".#....#.", "..#..#..", "...##...", "..####..", "...##...", "...##...", "..####.."],
    ["..#..#..", "..#..#..", "########", "..#..#..", "..#..#..", "########", "..#..#..", "..#..#.."],
    ["...##...", "..#..#..", ".#.##.#.", "#.#..#.#", "#.#..#.#", ".#.##.#.", "..#..#..", "...##..."],
    ["####....", "#..#....", "#..####.", "#.....#.", "####..#.", "...#..#.", "...####.", "........"],
)


def glyph(rows):
    img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "#":
                img.putpixel((x, y), (214, 184, 255, 255))
    return img


def generate_particles():
    save(telegraph_ring(), "particle", "telegraph_ring.png")
    save(telegraph_fill(), "particle", "telegraph_fill.png")
    write_json(asset_path("particles", "telegraph_ring.json"), {"textures": ["netheryx:telegraph_ring"]})
    write_json(asset_path("particles", "telegraph_fill.json"), {"textures": ["netheryx:telegraph_fill"]})
    names = []
    for i, rows in enumerate(GLYPHS):
        save(glyph(rows), "particle", f"rune_glyph_{i}.png")
        names.append(f"netheryx:rune_glyph_{i}")
    write_json(asset_path("particles", "rune_glyph.json"), {"textures": names})


def rune_lock(lit):
    base = ((42, 30, 62), (58, 42, 86), (30, 22, 46))
    rune = (235, 200, 255, 255) if lit else (92, 70, 130, 255)
    glow = (172, 112, 255, 255) if lit else (66, 50, 98, 255)
    img = Image.new("RGBA", (16, 16), base[0] + (255,))
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            img.putpixel((x, y), base[2] + (255,))
        for (x, y) in ((i, 1), (1, i)):
            if 0 < i < 15:
                img.putpixel((x, y), base[1] + (255,))
    pattern = ["..........", "....##....", "...#..#...", "..#.##.#..", ".#.#..#.#.", ".#.#..#.#.", "..#.##.#..", "...#..#...", "....##....", ".........."]
    for y, row in enumerate(pattern):
        for x, ch in enumerate(row):
            if ch == "#":
                img.putpixel((3 + x, 3 + y), rune)
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
        img.putpixel((x, y), glow)
    return img


def rune_key():
    rows = ["................", "...........ooo..", "..........oAAAo.", ".........oAooAo.", ".........oAooAo.", ".........oAAAAo.",
            "........oAAoo...", ".......oAAo.....", "......oAAo......", ".....oAAo.......", "....oAAo........", "..ooAAo.........",
            ".oAAoAo.........", ".oAoooo.........", "..oo............", "................"]
    colors = {"o": (34, 22, 54, 255), "A": (190, 140, 255, 255)}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                img.putpixel((x, y), colors[ch])
    img.putpixel((11, 2), (255, 240, 255, 255))
    img.putpixel((7, 8), (255, 240, 255, 255))
    return img


def generate():
    generate_particles()
    save(rune_lock(False), "block", "rune_lock.png")
    save(rune_lock(True), "block", "rune_lock_lit.png")
    save(rune_key(), "item", "rune_key.png")
