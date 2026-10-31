"""Textures 1.3.4 : onyx plus lisible (tons moyens remontes, eclats violets) et textures animees (lave encroutee,
briques de magma, cristaux et verre en fusion). Une texture animee est une bande verticale d'images 16 x 16
accompagnee de son fichier .mcmeta."""
import math

from PIL import Image

from .common import asset_path, write_json
from .pixels import hexc, mix, rng
from .textures import crusted_lava, polished_onyx, save
from .textures_v121 import onyx_block
from .textures_v124 import magma_bricks_v2, molten_glass
from .textures import crystals

GLINT = "#e0ccff"
GLINT_HALO = "#a684e8"


def lift(img, gain=1.28, floor=10):
    """Remonte les tons sombres et moyens (courbe douce) sans bruler les reflets, contour compris."""
    out = img.copy()
    for y in range(16):
        for x in range(16):
            r, g, b, a = img.getpixel((x, y))
            lifted = []
            for c in (r, g, b):
                v = c / 255.0
                v = 1.0 - (1.0 - v) ** gain  # courbe : les sombres montent plus que les clairs
                lifted.append(min(255, round(v * 255) + floor))
            out.putpixel((x, y), tuple(lifted) + (a,))
    return out


def glints(img, points):
    """Eclats violets de 1 a 2 px avec un halo d'un pixel."""
    for (x, y, size) in points:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            hx, hy = x + dx, y + dy
            if 0 <= hx < 16 and 0 <= hy < 16:
                img.putpixel((hx, hy), mix(img.getpixel((hx, hy)), hexc(GLINT_HALO), 0.55))
        img.putpixel((x, y), hexc(GLINT))
        if size > 1 and x + 1 < 16:
            img.putpixel((x + 1, y), hexc(GLINT))
    return img


def onyx_block_v3():
    return glints(lift(onyx_block()), ((4, 4, 2), (11, 10, 1)))


def polished_onyx_v2():
    return glints(lift(polished_onyx(), gain=1.7, floor=20), ((3, 3, 1), (12, 11, 2)))


def animated(frames, name, frametime, interpolate=True):
    """Assemble les images en bande verticale et ecrit le .mcmeta."""
    strip = Image.new("RGBA", (16, 16 * len(frames)), (0, 0, 0, 0))
    for i, frame in enumerate(frames):
        strip.paste(frame, (0, 16 * i))
    save(strip, "block", f"{name}.png")
    write_json(asset_path("textures", "block", f"{name}.png.mcmeta"),
               {"animation": {"frametime": frametime, "interpolate": interpolate}})


def pulse(base, is_hot, count, low, high):
    """Fait battre les pixels incandescents (is_hot) entre low et high fois leur luminosite."""
    frames = []
    for i in range(count):
        k = low + (high - low) * (0.5 - 0.5 * math.cos(2 * math.pi * i / count))
        frame = base.copy()
        for y in range(16):
            for x in range(16):
                r, g, b, a = base.getpixel((x, y))
                if a and is_hot(r, g, b):
                    frame.putpixel((x, y), (min(255, round(r * k)), min(255, round(g * k)), min(255, round(b * k)), a))
        frames.append(frame)
    return frames


def hot(r, g, b):
    return r > 190 and g > 70


def molten_crystal_frames(count=8):
    """Le cristal s'illumine par vagues, de la base vers la pointe."""
    base = crystals("#c8460e", "#ff8a1a", "#fff0a0", "molten_crystal")
    frames = []
    for i in range(count):
        frame = base.copy()
        crest = 15 - (i * 16 / count)  # la vague remonte
        for y in range(16):
            glow = max(0.0, 1.0 - abs(y - crest) / 4.0)
            for x in range(16):
                r, g, b, a = base.getpixel((x, y))
                if a:
                    frame.putpixel((x, y), mix((r, g, b, a), hexc("#fff4c0", a), 0.45 * glow))
        frames.append(frame)
    return frames


def molten_glass_frames(count=16):
    """Bulles qui remontent lentement dans le verre en fusion (elles bouclent tous les 16 pixels)."""
    base = molten_glass()
    r = rng("molten_glass_rise")
    bubbles = [(r.randrange(16), r.randrange(16)) for _ in range(5)]
    frames = []
    for i in range(count):
        frame = base.copy()
        for (bx, by) in bubbles:
            y = (by - i) % 16
            frame.putpixel((bx, y), hexc("#fff0a0", 240))
            below = (y + 1) % 16
            frame.putpixel((bx, below), mix(frame.getpixel((bx, below)), hexc("#ffc060", 225), 0.5))
        frames.append(frame)
    return frames


def generate():
    save(onyx_block_v3(), "block", "onyx_block.png")
    save(polished_onyx_v2(), "block", "polished_onyx.png")
    animated(pulse(crusted_lava(), hot, 8, 0.55, 1.15), "crusted_lava", 5)
    animated(pulse(magma_bricks_v2(), hot, 8, 0.6, 1.2), "magma_bricks", 6)
    animated(molten_crystal_frames(), "molten_crystal", 4)
    animated(molten_glass_frames(), "molten_glass", 6, interpolate=False)
