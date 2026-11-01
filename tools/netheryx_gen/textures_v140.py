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


def generate():
    generate_particles()
