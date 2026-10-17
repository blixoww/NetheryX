"""Primitives de pixel-art : couleurs, bruit tuilable, formes avec contour automatique."""
import math
import random

from PIL import Image


def hexc(value: str, alpha: int = 255):
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha)


def shade(color, factor: float):
    r, g, b, a = color
    return (max(0, min(255, int(r * factor))), max(0, min(255, int(g * factor))),
            max(0, min(255, int(b * factor))), a)


def mix(c1, c2, t: float):
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(4))


def new(size=16):
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def rng(seed: str) -> random.Random:
    return random.Random(seed)


def value_noise(size: int, seed: str, blur: int = 1):
    """Bruit lisse et tuilable dans [0, 1]."""
    r = rng(seed)
    grid = [[r.random() for _ in range(size)] for _ in range(size)]
    for _ in range(blur):
        new_grid = [[0.0] * size for _ in range(size)]
        for y in range(size):
            for x in range(size):
                total = 0.0
                for dy in (-1, 0, 1):
                    for dx in (-1, 0, 1):
                        weight = 2.0 if dx == 0 and dy == 0 else 1.0
                        total += grid[(y + dy) % size][(x + dx) % size] * weight
                new_grid[y][x] = total / 10.0
        grid = new_grid
    flat = [v for row in grid for v in row]
    low, high = min(flat), max(flat)
    span = (high - low) or 1.0
    return [[(grid[y][x] - low) / span for x in range(size)] for y in range(size)]


def noise_fill(img, palette, seed, blur=1, jitter=0.12):
    size = img.size[0]
    field = value_noise(size, seed, blur)
    r = rng(seed + "j")
    for y in range(size):
        for x in range(size):
            v = min(0.999, max(0.0, field[y][x] + (r.random() - 0.5) * jitter))
            img.putpixel((x, y), hexc(palette[int(v * len(palette))]))
    return img


def bevel(img, light=1.18, dark=0.72, width=1):
    size = img.size[0]
    for i in range(width):
        for k in range(size):
            for (x, y, f) in ((k, i, light), (i, k, light), (k, size - 1 - i, dark), (size - 1 - i, k, dark)):
                img.putpixel((x, y), shade(img.getpixel((x, y)), f))
    return img


class Shape:
    """Masque de pixels pour dessiner des objets, avec ombrage et contour automatique."""

    def __init__(self, size=16):
        self.size = size
        self.pixels = {}

    def put(self, x, y, color):
        if 0 <= x < self.size and 0 <= y < self.size:
            self.pixels[(int(x), int(y))] = color

    def line(self, x0, y0, x1, y1, color, width=1):
        steps = max(abs(x1 - x0), abs(y1 - y0), 1)
        for i in range(steps + 1):
            t = i / steps
            x = round(x0 + (x1 - x0) * t)
            y = round(y0 + (y1 - y0) * t)
            for w in range(width):
                self.put(x + w, y, color)

    def disc(self, cx, cy, radius, color, shaded=True):
        for y in range(self.size):
            for x in range(self.size):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                if dx * dx + dy * dy <= radius * radius:
                    if shaded:
                        f = 1.0 + 0.35 * (-(dx + dy) / max(radius, 1.0)) / 1.4
                        self.put(x, y, shade(color, f))
                    else:
                        self.put(x, y, color)

    def ellipse(self, cx, cy, rx, ry, color, shaded=True):
        for y in range(self.size):
            for x in range(self.size):
                dx, dy = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
                if dx * dx + dy * dy <= 1.0:
                    f = 1.0 + 0.3 * (-(dx + dy)) / 1.4 if shaded else 1.0
                    self.put(x, y, shade(color, f))

    def mask(self, rows, colors, origin=(0, 0)):
        ox, oy = origin
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in colors:
                    self.put(ox + x, oy + y, colors[ch])

    def render(self, outline=None, img=None):
        img = img or new(self.size)
        for (x, y), color in self.pixels.items():
            img.putpixel((x, y), color)
        if outline is not None:
            filled = set(self.pixels)
            for y in range(self.size):
                for x in range(self.size):
                    if (x, y) in filled:
                        continue
                    if any((x + dx, y + dy) in filled for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                        img.putpixel((x, y), outline)
        return img


def shaded_mask(rows, base, outline, size=16):
    """Remplit un masque ASCII ('#' = matiere, '+' = reflet, '-' = ombre) avec un ombrage diagonal."""
    shape = Shape(size)
    filled = [(x, y) for y, row in enumerate(rows) for x, ch in enumerate(row) if ch in "#+-"]
    if not filled:
        return shape.render()
    min_x = min(p[0] for p in filled)
    max_x = max(p[0] for p in filled)
    min_y = min(p[1] for p in filled)
    max_y = max(p[1] for p in filled)
    span = max(1, (max_x - min_x) + (max_y - min_y))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch not in "#+-":
                continue
            t = ((x - min_x) + (y - min_y)) / span
            color = shade(base, 1.25 - 0.5 * t)
            if ch == "+":
                color = shade(base, 1.65)
            elif ch == "-":
                color = shade(base, 0.6)
            shape.put(x, y, color)
    return shape.render(outline=outline)
