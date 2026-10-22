"""Textures des blocs et objets, dessinees de maniere procedurale (reproductible)."""
import math

from PIL import Image, ImageDraw, ImageFont

from .common import asset_path, RESOURCES
from .pixels import (Shape, bevel, hexc, mix, new, noise_fill, rng, shade, shaded_mask, value_noise)

NETHERRACK = ["#3a0d0d", "#521414", "#6a1b1a", "#7f2422", "#953230"]
ONYX_DARK = ["#0b090e", "#121016", "#18151d", "#1f1a26", "#262030"]
EMBER_BARK = ["#24100d", "#331611", "#441d15", "#55251a"]
EMBER_WOOD = ["#6a2814", "#7f3218", "#933d1c", "#a84a22", "#bb5a2a"]
SULFUR_ROCK = ["#7c6c22", "#968529", "#b09d33", "#c7b444", "#dccb5e"]
ASH = ["#45413f", "#55514e", "#66615d", "#77716c", "#89827c"]
OUTLINE = hexc("#120a0a")


def save(img, *parts):
    path = asset_path("textures", *parts)
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)


# ------------------------------------------------------------------------------------------------
# Blocs
# ------------------------------------------------------------------------------------------------

def netherrack(seed):
    img = noise_fill(new(), NETHERRACK, seed, blur=1, jitter=0.3)
    r = rng(seed + "speck")
    for _ in range(10):
        x, y = r.randrange(16), r.randrange(16)
        img.putpixel((x, y), hexc("#2a0808"))
    return img


def ore(seed, gem_dark, gem_mid, gem_light, glint, base=None, clusters=4):
    img = base or netherrack(seed)
    r = rng(seed + "ore")
    spots = [(2, 2), (9, 3), (4, 9), (11, 10), (7, 6)][:clusters + 1]
    for (sx, sy) in spots:
        sx += r.randint(-1, 1)
        sy += r.randint(-1, 1)
        cells = [(0, 0), (1, 0), (0, 1), (1, 1)]
        if r.random() < 0.6:
            cells += [(2, 1), (1, 2)]
        for (dx, dy) in cells:
            img.putpixel(((sx + dx) % 16, (sy + dy) % 16), hexc(gem_mid))
        img.putpixel(((sx + 1) % 16, (sy + 1) % 16), hexc(gem_dark))
        img.putpixel((sx % 16, sy % 16), hexc(gem_light))
        if r.random() < 0.7:
            img.putpixel(((sx + 1) % 16, sy % 16), hexc(glint))
    return img


def bricks(palette, mortar, seed, brick_h=4, brick_w=8):
    img = noise_fill(new(), palette, seed, blur=1, jitter=0.2)
    for y in range(16):
        row = y // brick_h
        offset = (brick_w // 2) if row % 2 else 0
        for x in range(16):
            if y % brick_h == brick_h - 1 or (x + offset) % brick_w == brick_w - 1:
                img.putpixel((x, y), hexc(mortar))
            elif y % brick_h == 0:
                img.putpixel((x, y), shade(img.getpixel((x, y)), 1.2))
    return img


def onyx_ore():
    img = netherrack("onyx_ore")
    for (sx, sy) in ((2, 2), (9, 2), (4, 9), (11, 10), (8, 6)):
        cells = [(0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1), (1, 2), (1, -1)]
        for (dx, dy) in cells:
            img.putpixel(((sx + dx) % 16, (sy + dy) % 16), hexc("#120e18"))
        for (dx, dy) in ((-1, 0), (3, 1), (0, 2), (2, 2), (1, -2)):
            img.putpixel(((sx + dx) % 16, (sy + dy) % 16), hexc("#5a3d86"))
        img.putpixel(((sx + 1) % 16, (sy - 1) % 16), hexc("#e2c8ff"))
        img.putpixel((sx % 16, sy % 16), hexc("#8f62d6"))
    return img


def onyx_block():
    img = noise_fill(new(), ONYX_DARK, "onyx_block", blur=2, jitter=0.05)
    for i in range(16):
        for w in range(2):
            x = (i + w + 3) % 16
            img.putpixel((x, i), mix(img.getpixel((x, i)), hexc("#6b45a3"), 0.35))
    return bevel(img, 1.6, 0.6)


def polished_onyx():
    img = noise_fill(new(), ONYX_DARK[1:4], "polished_onyx", blur=3, jitter=0.03)
    r = rng("polished_vein")
    x, y = 2, 0
    while y < 16:
        img.putpixel((x % 16, y), hexc("#3b2c52"))
        x += r.choice((-1, 0, 1))
        y += 1
    bevel(img, 1.5, 0.55)
    for k in range(2, 14):
        img.putpixel((k, 2), shade(img.getpixel((k, 2)), 1.25))
    return img


def cross_plant(draw_fn, seed):
    shape = Shape()
    draw_fn(shape, rng(seed))
    return shape.render()


def ember_fungus(shape, r):
    stem = hexc("#e8c9a0")
    shape.line(7, 9, 7, 15, stem)
    shape.line(8, 10, 8, 15, shade(stem, 0.8))
    shape.ellipse(7.5, 7.5, 5.5, 3.2, hexc("#e2561a"))
    for (x, y) in ((5, 6), (9, 5), (7, 8), (11, 7), (4, 8)):
        shape.put(x, y, hexc("#ffd36b"))


def ember_roots(shape, r):
    for x0 in (2, 5, 8, 11, 13):
        height = r.randint(6, 11)
        x = x0
        for y in range(15, 15 - height, -1):
            shape.put(x, y, hexc("#9c2a10") if y > 15 - height + 2 else hexc("#ff8a2a"))
            if r.random() < 0.3:
                x += r.choice((-1, 1))
        shape.put(x, 15 - height, hexc("#ffd36b"))


def charred_shrub(shape, r):
    trunk = hexc("#1a1514")
    shape.line(7, 15, 7, 8, trunk)
    for (x1, y1) in ((3, 4), (12, 3), (2, 9), (13, 8), (8, 2), (5, 6), (10, 6)):
        shape.line(7, 10, x1, y1, shade(trunk, 1.0 + r.random() * 0.8))
    for _ in range(5):
        shape.put(r.randint(2, 13), r.randint(2, 9), hexc("#ff5a1a"))


def crystals(color_dark, color_mid, color_light, seed):
    shape = Shape()
    r = rng(seed)
    for (cx, height, width) in ((4, 9, 2), (8, 13, 3), (12, 8, 2), (6, 6, 1), (10, 5, 1)):
        for y in range(16 - height, 16):
            t = (y - (16 - height)) / max(1, height)
            half = max(0, round(width * min(1.0, t * 2.0)))
            for x in range(cx - half, cx + half + 1):
                color = color_light if x < cx else (color_mid if x == cx else color_dark)
                shape.put(x, y, hexc(color))
        shape.put(cx, 16 - height, hexc("#ffffff", 220))
    return shape.render()


def ember_nylium_top():
    img = noise_fill(new(), ["#5a1608", "#7c220b", "#a3340e", "#cc4d14", "#ee7424"], "ember_nylium", blur=1, jitter=0.35)
    r = rng("ember_glow")
    for _ in range(8):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("#ffc45a"))
    return img


def ember_nylium_side():
    img = netherrack("ember_side")
    top = ember_nylium_top()
    r = rng("ember_fringe")
    for x in range(16):
        depth = r.choice((2, 3, 3, 4, 5))
        for y in range(depth):
            img.putpixel((x, y), top.getpixel((x, y)))
    return img


def stem_side(palette, vein_colors, seed, veins=True):
    img = new()
    r = rng(seed)
    for x in range(16):
        base = r.randrange(len(palette))
        for y in range(16):
            idx = max(0, min(len(palette) - 1, base + r.choice((-1, 0, 0, 1))))
            img.putpixel((x, y), hexc(palette[idx]))
    if veins:
        for x in (2, 7, 12):
            y = r.randrange(16)
            length = r.randint(5, 10)
            for k in range(length):
                img.putpixel((x, (y + k) % 16), hexc(vein_colors[k % len(vein_colors)]))
    return img


def stem_top(bark, inner, seed, core=("#ffb347", "#ff7a1a")):
    img = new()
    for y in range(16):
        for x in range(16):
            ring = min(x, y, 15 - x, 15 - y)
            if ring == 0:
                color = hexc(bark[1])
            elif ring % 2 == 1:
                color = hexc(inner[1])
            else:
                color = hexc(inner[3])
            img.putpixel((x, y), color)
    for k in (7, 8):
        img.putpixel((k, 7), hexc(core[0]))
        img.putpixel((k, 8), hexc(core[1]))
    return img


def planks(palette, seed):
    img = noise_fill(new(), palette, seed, blur=0, jitter=0.0)
    r = rng(seed + "planks")
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), hexc(palette[(y // 4 + (x * 7 + y * 3) % 3 // 2 + r.randrange(2)) % len(palette)]))
    for y in range(16):
        for x in range(16):
            if y % 4 == 3:
                img.putpixel((x, y), shade(img.getpixel((x, y)), 0.55))
    for row, seam in enumerate((5, 12, 2, 9)):
        for y in range(row * 4, row * 4 + 3):
            img.putpixel((seam, y), shade(img.getpixel((seam, y)), 0.6))
    return img


def door_half(top: bool, palette=None, frame_hex="#3d1a12", seed="ember"):
    img = planks(palette or EMBER_WOOD, f"{seed}_door_top" if top else f"{seed}_door_bottom")
    frame = hexc(frame_hex)
    for k in range(16):
        img.putpixel((0, k), frame)
        img.putpixel((15, k), frame)
    if top:
        for x in range(16):
            img.putpixel((x, 0), frame)
        for (x0, y0) in ((3, 3), (9, 3), (3, 9), (9, 9)):
            for y in range(y0, y0 + 4):
                for x in range(x0, x0 + 4):
                    img.putpixel((x, y), (0, 0, 0, 0))
    else:
        for x in range(16):
            img.putpixel((x, 15), frame)
        img.putpixel((12, 1), hexc("#ffc45a"))
        img.putpixel((12, 2), hexc("#d88a12"))
    return img


def trapdoor(palette=None, frame_hex="#3d1a12", seed="ember"):
    img = planks(palette or EMBER_WOOD, f"{seed}_trapdoor")
    frame = hexc(frame_hex)
    for k in range(16):
        for (x, y) in ((k, 0), (k, 15), (0, k), (15, k)):
            img.putpixel((x, y), frame)
    for (x0, y0) in ((3, 3), (9, 3), (3, 9), (9, 9)):
        for y in range(y0, y0 + 4):
            for x in range(x0, x0 + 4):
                img.putpixel((x, y), (0, 0, 0, 0))
    return img


def crusted_lava():
    img = noise_fill(new(), ["#1f1410", "#2b1a14", "#3a2219", "#46291d"], "crusted_lava", blur=1, jitter=0.2)
    r = rng("crack")
    for _ in range(4):
        x, y = r.randrange(16), r.randrange(16)
        for _ in range(9):
            img.putpixel((x % 16, y % 16), hexc("#ff6a00"))
            if r.random() < 0.5:
                img.putpixel(((x + 1) % 16, y % 16), hexc("#ffb02e"))
            x += r.choice((-1, 0, 1))
            y += r.choice((-1, 0, 1))
    return img


def crop_stage(kind, stage):
    shape = Shape()
    r = rng(f"{kind}{stage}")
    stem = hexc("#4f6b1f") if kind == "blaze_pepper" else hexc("#2f6f73")
    leaf = hexc("#6f9a2a") if kind == "blaze_pepper" else hexc("#4fb3b0")
    height = [4, 7, 10, 13][stage]
    for x in (2, 6, 10, 13):
        h = height - r.randint(0, 2)
        shape.line(x, 15, x, 15 - h, stem)
        for y in range(15 - h + 1, 15, 3):
            shape.put(x - 1, y, leaf)
            shape.put(x + 1, y - 1, shade(leaf, 0.85))
    if stage == 3:
        if kind == "blaze_pepper":
            for (x, y) in ((3, 6), (7, 8), (11, 5), (14, 9)):
                shape.line(x, y, x, y + 3, hexc("#ff6a1a"))
                shape.put(x, y + 3, hexc("#d23a0c"))
                shape.put(x, y, hexc("#ffc45a"))
        else:
            for (x, y) in ((3, 13), (8, 14), (12, 13)):
                shape.disc(x + 0.5, y + 0.5, 1.6, hexc("#7fe0e6"))
    elif stage == 2 and kind == "blaze_pepper":
        for (x, y) in ((7, 9), (11, 7)):
            shape.line(x, y, x, y + 2, hexc("#7fae2a"))
    return shape.render()


def berry_bush(stage):
    shape = Shape()
    r = rng(f"bush{stage}")
    leaf = ["#4a120c", "#64190f", "#7c2414"]
    radius = [3.5, 5.5, 7.0, 7.5][stage]
    cx, cy = 8, 16 - radius
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            if dx * dx + dy * dy * 1.3 <= radius * radius and r.random() < 0.85:
                shape.put(x, y, hexc(r.choice(leaf)))
    berries = {0: 0, 1: 0, 2: 3, 3: 7}[stage]
    for _ in range(berries):
        x, y = r.randint(3, 12), r.randint(int(16 - radius * 1.5) + 1, 14)
        shape.put(x, y, hexc("#ff5a1a"))
        shape.put(x + 1, y, hexc("#ffb02e"))
        shape.put(x, y + 1, hexc("#c22a0a"))
    return shape.render()


def generate_block_textures():
    save(onyx_ore(), "block", "onyx_ore.png")
    save(onyx_block(), "block", "onyx_block.png")
    save(polished_onyx(), "block", "polished_onyx.png")
    save(bricks(ONYX_DARK[1:], "#050407", "onyx_bricks"), "block", "onyx_bricks.png")
    save(crystals("#1c1326", "#3c2a57", "#8f62d6", "onyx_cluster"), "block", "onyx_cluster.png")

    save(noise_fill(new(), ASH, "ash_block", blur=1, jitter=0.35), "block", "ash_block.png")
    save(cross_plant(charred_shrub, "charred_shrub"), "block", "charred_shrub.png")

    save(ember_nylium_top(), "block", "ember_nylium.png")
    save(ember_nylium_side(), "block", "ember_nylium_side.png")
    save(stem_side(EMBER_BARK, ["#ff7a1a", "#ffb347", "#ff9a2a"], "ember_stem"), "block", "ember_stem.png")
    save(stem_top(EMBER_BARK, EMBER_WOOD, "ember_stem_top"), "block", "ember_stem_top.png")
    save(stem_side(EMBER_WOOD, [], "stripped_ember_stem", veins=False), "block", "stripped_ember_stem.png")
    save(stem_top(EMBER_WOOD, EMBER_WOOD, "stripped_ember_stem_top"), "block", "stripped_ember_stem_top.png")
    save(planks(EMBER_WOOD[:4], "ember_planks"), "block", "ember_planks.png")
    save(door_half(True), "block", "ember_door_top.png")
    save(door_half(False), "block", "ember_door_bottom.png")
    save(trapdoor(), "block", "ember_trapdoor.png")
    wart = noise_fill(new(), ["#6e1a04", "#9a2a08", "#c8420c", "#e8661a", "#ffa040"], "ember_wart", blur=1, jitter=0.3)
    save(wart, "block", "ember_wart_block.png")
    save(cross_plant(ember_fungus, "ember_fungus"), "block", "ember_fungus.png")
    save(cross_plant(ember_roots, "ember_roots"), "block", "ember_roots.png")

    save(noise_fill(new(), SULFUR_ROCK, "sulfur_rock", blur=1, jitter=0.3), "block", "sulfur_rock.png")
    save(ore("sulfur_ore", "#a08a10", "#e8d43a", "#fff27a", "#ffffff"), "block", "sulfur_ore.png")
    save(bevel(noise_fill(new(), ["#c4a826", "#d8bf31", "#e8d24a", "#f4e26a"], "sulfur_block", blur=1, jitter=0.4), 1.15, 0.8),
         "block", "sulfur_block.png")
    save(crystals("#9a8410", "#e2cc34", "#fff59a", "sulfur_cluster"), "block", "sulfur_cluster.png")

    for stage in range(4):
        save(crop_stage("blaze_pepper", stage), "block", f"blaze_pepper_stage{stage}.png")
        save(crop_stage("soul_tuber", stage), "block", f"soul_tuber_stage{stage}.png")
        save(berry_bush(stage), "block", f"fire_berry_bush_stage{stage}.png")
    save(crusted_lava(), "block", "crusted_lava.png")


# ------------------------------------------------------------------------------------------------
# Objets
# ------------------------------------------------------------------------------------------------

MATERIALS = {
    "onyx": ("#2a2233", "#a37cf0"),
    "withered": ("#3a3a3d", "#d8d0c0"),
    "soul": ("#2a2f3a", "#6fe8f0"),
}
BLAZE_HANDLE = (hexc("#ffc531"), hexc("#c27a10"))
DARK_HANDLE = (hexc("#3d3346"), hexc("#221c28"))


def tool(kind, material="onyx", handle=BLAZE_HANDLE):
    base_hex, glint_hex = MATERIALS[material]
    base, glint = hexc(base_hex), hexc(glint_hex)
    light, dark = shade(base, 1.6), shade(base, 0.7)
    shape = Shape()

    def handle_line(x0, y0, x1, y1):
        steps = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(steps + 1):
            t = i / max(1, steps)
            x, y = round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t)
            shape.put(x, y, handle[0] if i % 3 else handle[1])

    if kind == "sword":
        handle_line(2, 13, 4, 11)
        shape.line(3, 8, 7, 12, dark)
        shape.line(4, 9, 6, 11, base)
        for i in range(8):
            shape.put(6 + i, 9 - i, light if i % 2 else base)
            shape.put(7 + i, 9 - i, base)
            shape.put(6 + i, 10 - i, dark)
        shape.put(13, 2, glint)
        shape.put(14, 1, light)
    elif kind == "pickaxe":
        handle_line(2, 13, 10, 5)
        head = [(4, 3), (5, 2), (6, 2), (7, 2), (8, 2), (9, 2), (10, 3), (11, 3), (12, 4), (12, 5), (13, 6), (13, 7), (13, 8), (12, 9)]
        for i, (x, y) in enumerate(head):
            shape.put(x, y, light if i % 4 == 1 else base)
            shape.put(x - (1 if x > 10 else 0), y + 1, dark)
        shape.put(9, 3, glint)
    elif kind == "axe":
        handle_line(3, 13, 10, 6)
        rows = {1: (9, 11), 2: (8, 12), 3: (7, 13), 4: (7, 13), 5: (8, 12), 6: (10, 11)}
        for y, (x0, x1) in rows.items():
            for x in range(x0, x1 + 1):
                color = light if (x + y) % 5 == 0 else (dark if x == x0 else base)
                shape.put(x, y, color)
        shape.put(11, 2, glint)
    elif kind == "shovel":
        handle_line(2, 13, 8, 7)
        for y in range(16):
            for x in range(16):
                if abs(x - 11) + abs(y - 4) <= 3:
                    shape.put(x, y, dark if (x - 11) + (y - 4) > 1 else base)
        shape.put(10, 3, light)
        shape.put(11, 2, glint)
    elif kind == "hoe":
        handle_line(3, 13, 10, 6)
        shape.line(7, 2, 12, 2, base)
        shape.line(7, 3, 11, 3, dark)
        shape.put(12, 3, base)
        shape.put(12, 4, dark)
        shape.put(9, 2, light)
        shape.put(11, 2, glint)
    elif kind == "hammer":
        handle_line(2, 14, 8, 8)
        cx, cy = 10.5, 5.5
        for y in range(16):
            for x in range(16):
                px, py = x + 0.5 - cx, y + 0.5 - cy
                along = (px + py) / math.sqrt(2)
                across = (px - py) / math.sqrt(2)
                if abs(along) <= 4.4 and abs(across) <= 2.3:
                    color = light if across < -1.2 else (dark if across > 1.0 else base)
                    shape.put(x, y, color)
        shape.put(8, 1, glint)
        shape.put(14, 7, glint)
    elif kind == "scythe":
        handle_line(2, 14, 11, 4)
        blade = [(11, 3), (10, 2), (9, 1), (8, 1), (7, 1), (6, 1), (5, 2), (4, 2), (3, 3), (2, 4), (2, 5), (1, 6)]
        for i, (x, y) in enumerate(blade):
            shape.put(x, y, light if i < 3 else base)
            if i > 1:
                shape.put(x, y + 1, dark if i % 2 else base)
        shape.put(7, 2, glint)
        shape.put(3, 4, glint)
    return shape.render(outline=OUTLINE)


ARMOR_MASKS = {
    "helmet": ["", "", "", "....########....", "...#++++++++#...", "...##########...", "...###....###...",
               "...##......##...", "...##......##...", "...--......--..."],
    "chestplate": ["", "", "..###......###..", "..#+##....##+#..", "..############..", "...##########...",
                   "....##+#####....", "....########....", "....########....", "....########....",
                   "....########....", "....--------...."],
    "leggings": ["", "", "", "....########....", "....#+######....", "....########....", "....###..###....",
                 "....###..###....", "....#+#..#+#....", "....###..###....", "....###..###....",
                 "....---..---...."],
    "boots": ["", "", "", "", "", "", "....##....##....", "....#+....#+....", "....##....##....",
              "...###...###....", "..####..####....", "..----..----...."],
}


def armor_icon(piece, base_hex):
    rows = [row.ljust(16, ".") for row in ARMOR_MASKS[piece]]
    return shaded_mask(rows, hexc(base_hex), OUTLINE)


def gem(base_hex, glint_hex):
    rows = ["", "", "", "....######....", "...#+######-...", "..#++######--..", "..##########--..",
            "...##########-..", "....########-...", ".....######-....", "......####......", ".......##......."]
    rows = [r.ljust(16, ".")[:16] for r in rows]
    img = shaded_mask(rows, hexc(base_hex), OUTLINE)
    img.putpixel((4, 5), hexc(glint_hex))
    img.putpixel((5, 4), hexc(glint_hex))
    return img


def shard(base_hex, glint_hex):
    shape = Shape()
    for (x0, y0, h) in ((4, 5, 7), (8, 3, 9), (11, 7, 5)):
        for k in range(h):
            shape.put(x0, y0 + k, shade(hexc(base_hex), 1.4 if k < 2 else 1.0))
            if k > 1 and k < h - 1:
                shape.put(x0 + 1, y0 + k, shade(hexc(base_hex), 0.7))
        shape.put(x0, y0, hexc(glint_hex))
    return shape.render(outline=OUTLINE)


def dust(colors, seed):
    shape = Shape()
    r = rng(seed)
    for _ in range(60):
        x = int(8 + r.gauss(0, 2.6))
        y = int(10 + r.gauss(0, 1.8))
        if 1 <= x <= 14 and 3 <= y <= 14:
            shape.put(x, y, hexc(r.choice(colors)))
    return shape.render(outline=None)


def wraith_essence():
    shape = Shape()
    shape.disc(8, 8, 5, hexc("#bdb7b2"))
    shape.disc(7, 7, 2.5, hexc("#f4f0ec"))
    r = rng("wisp")
    for k in range(5):
        shape.put(3 + k * 2, 14 - (k % 2), hexc("#8f8a86", 180))
        shape.put(4 + r.randint(0, 8), 2 + r.randint(0, 2), hexc("#d9d4cf", 160))
    return shape.render(outline=hexc("#3b3735"))


def magma_shell():
    rows = ["", "", "", "", "....########....", "...##########...", "..#+##-##-##-#..", "..##-##-##-###..",
            ".##############.", ".##############.", "..------------..", ""]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#8a2a10"), OUTLINE)
    for (x, y) in ((4, 8), (7, 7), (10, 8), (12, 7), (6, 9)):
        img.putpixel((x, y), hexc("#ffb02e"))
    return img


def sentinel_core():
    shape = Shape()
    shape.disc(8, 8, 6, hexc("#1a1520"))
    shape.disc(8, 8, 4, hexc("#7d3fe0"))
    shape.disc(7.5, 7.5, 2, hexc("#e2c8ff"))
    for (x, y) in ((8, 1), (8, 14), (1, 8), (14, 8)):
        shape.put(x, y, hexc("#3c2a57"))
    return shape.render(outline=OUTLINE)


def soul_compass():
    shape = Shape()
    shape.disc(8, 8, 6.5, hexc("#2a2430"))
    shape.disc(8, 8, 5, hexc("#3e5560"), shaded=False)
    shape.line(8, 4, 8, 8, hexc("#6fe8f0"))
    shape.line(8, 9, 8, 12, hexc("#c4f8ff"))
    shape.put(8, 8, hexc("#ffffff"))
    for (x, y) in ((8, 2), (2, 8), (14, 8), (8, 14)):
        shape.put(x, y, hexc("#6fe8f0"))
    return shape.render(outline=OUTLINE)


def infernal_heart():
    rows = ["", "", "...###...###....", "..#####.#####...", ".##+##########..", ".#++##########..",
            ".#############..", "..###########...", "...#########....", "....#######.....", ".....#####......",
            "......###.......", ".......#........"]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#c8200c"), OUTLINE)
    for (x, y) in ((7, 6), (8, 7), (6, 8), (9, 5)):
        img.putpixel((x, y), hexc("#ffb02e"))
    return img


def sulfur_bomb():
    shape = Shape()
    shape.disc(7.5, 9.5, 5, hexc("#2a2622"))
    shape.line(3, 9, 12, 9, hexc("#e8d43a"))
    shape.line(3, 10, 12, 10, hexc("#b09d33"))
    shape.line(9, 4, 11, 2, hexc("#c4a080"))
    shape.put(12, 1, hexc("#ffb02e"))
    shape.put(6, 7, hexc("#6a645e"))
    return shape.render(outline=OUTLINE)


def berries():
    shape = Shape()
    for (x, y, r) in ((6, 9, 2.7), (10, 10, 2.5), (8, 6, 2.4)):
        shape.disc(x, y, r, hexc("#e8401a"))
        shape.put(int(x - 1), int(y - 1), hexc("#ffc45a"))
    shape.line(8, 3, 9, 1, hexc("#5a3a1a"))
    return shape.render(outline=OUTLINE)


def seeds(color_hex):
    shape = Shape()
    for (x, y) in ((5, 6), (9, 5), (7, 9), (11, 9), (4, 11), (8, 12)):
        shape.put(x, y, hexc(color_hex))
        shape.put(x + 1, y, shade(hexc(color_hex), 0.75))
    return shape.render(outline=None)


def pepper():
    shape = Shape()
    pts = [(10, 3), (10, 4), (9, 5), (9, 6), (8, 7), (8, 8), (7, 9), (6, 10), (5, 11), (4, 12)]
    for i, (x, y) in enumerate(pts):
        color = hexc("#d8240c") if i < 6 else hexc("#ff5a1a")
        shape.put(x, y, color)
        shape.put(x + 1, y, shade(color, 0.8))
        if i < 7:
            shape.put(x - 1, y, shade(color, 1.2))
    shape.put(4, 13, hexc("#ffd36b"))
    shape.line(11, 1, 11, 2, hexc("#4f8a1f"))
    shape.put(12, 1, hexc("#6faa2a"))
    return shape.render(outline=OUTLINE)


def tuber(base_hex, spots_hex):
    shape = Shape()
    shape.ellipse(8, 9, 5.5, 4, hexc(base_hex))
    for (x, y) in ((6, 8), (9, 7), (10, 10), (5, 10)):
        shape.put(x, y, hexc(spots_hex))
    return shape.render(outline=OUTLINE)


def meat(base_hex, fat_hex):
    rows = ["", "", "", ".....######.....", "....#++######...", "...###########..", "..#############.",
            "..#############.", "..############..", "...##########...", "....#######.....", "......###......."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc(base_hex), OUTLINE)
    for (x, y) in ((5, 7), (6, 7), (7, 8), (10, 6), (11, 6), (9, 9)):
        img.putpixel((x, y), hexc(fat_hex))
    return img


def crab_leg(base_hex):
    shape = Shape()
    color = hexc(base_hex)
    shape.line(2, 13, 7, 8, color, width=2)
    shape.line(7, 8, 10, 5, shade(color, 0.85), width=2)
    shape.line(10, 5, 13, 2, color)
    shape.line(10, 5, 14, 5, shade(color, 1.2))
    for (x, y) in ((4, 11), (8, 7)):
        shape.put(x, y, hexc("#ffd36b"))
    return shape.render(outline=OUTLINE)


def stew():
    rows = ["", "", "", "", "", "", "..############..", ".#++##########-.", ".##############.",
            "..############..", "...##########...", "....########....", ".....------....."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#6b4423"), OUTLINE)
    for x in range(2, 14):
        img.putpixel((x, 6), hexc("#e8501a"))
        img.putpixel((x, 7) if 3 <= x <= 12 else (x, 6), hexc("#ff8a2a") if x % 3 else hexc("#ffd36b"))
    return img


def door_item(palette=None, frame_hex="#3d1a12", seed="ember"):
    shape = Shape()
    top, bottom = door_half(True, palette, frame_hex, seed), door_half(False, palette, frame_hex, seed)
    for y in range(16):
        for x in range(4, 12):
            src = top.getpixel(((x - 4) * 2, y * 2 % 16)) if y < 8 else bottom.getpixel(((x - 4) * 2, (y - 8) * 2))
            if src[3] > 0:
                shape.put(x, y, src)
    return shape.render()


def generate_item_textures():
    for kind in ("sword", "pickaxe", "axe", "shovel", "hoe", "hammer"):
        save(tool(kind), "item", f"onyx_{kind}.png")
    save(tool("scythe", "soul", DARK_HANDLE), "item", "soul_scythe.png")
    for piece in ("helmet", "chestplate", "leggings", "boots"):
        save(armor_icon(piece, "#2a2233"), "item", f"onyx_{piece}.png")
    save(armor_icon("boots", "#9a3412"), "item", "magma_treads.png")

    save(gem("#2a2233", "#c9a6ff"), "item", "onyx.png")
    save(shard("#3a2f48", "#c9a6ff"), "item", "onyx_shard.png")
    save(dust(["#e8d43a", "#fff27a", "#c4a826", "#f4e26a"], "sulfur"), "item", "sulfur.png")
    save(wraith_essence(), "item", "wraith_essence.png")
    save(magma_shell(), "item", "magma_shell.png")
    save(sentinel_core(), "item", "sentinel_core.png")
    save(soul_compass(), "item", "soul_compass.png")
    save(infernal_heart(), "item", "infernal_heart.png")
    save(sulfur_bomb(), "item", "sulfur_bomb.png")
    save(berries(), "item", "fire_berries.png")
    save(seeds("#ff8a2a"), "item", "blaze_pepper_seeds.png")
    save(pepper(), "item", "blaze_pepper.png")
    save(tuber("#3f5f72", "#7fe0e6"), "item", "soul_tuber.png")
    save(tuber("#b8762a", "#ffd36b"), "item", "baked_soul_tuber.png")
    save(meat("#b8302a", "#f0d0c0"), "item", "stalker_meat.png")
    save(meat("#7a3a1a", "#c89a6a"), "item", "cooked_stalker_meat.png")
    save(crab_leg("#e0661a"), "item", "crab_leg.png")
    save(crab_leg("#b8300c"), "item", "cooked_crab_leg.png")
    save(stew(), "item", "infernal_stew.png")
    save(door_item(), "item", "ember_door.png")


# ------------------------------------------------------------------------------------------------
# Armures portees et logo
# ------------------------------------------------------------------------------------------------

def armor_layer(base_hex, accent_hex, layer, seed):
    """Texture 64x32 au format des armures vanilla (zones remplies avec un bruit + bordures)."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    base = hexc(base_hex)
    accent = hexc(accent_hex)
    field = value_noise(64, seed, 1)
    if layer == 1:
        regions = [(0, 0, 32, 16), (32, 0, 64, 16), (16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32)]
    else:
        regions = [(0, 16, 16, 32), (16, 16, 40, 32)]
    for (x0, y0, x1, y1) in regions:
        for y in range(y0, y1):
            for x in range(x0, x1):
                f = 0.8 + 0.4 * field[y % 64][x % 64]
                img.putpixel((x, y), shade(base, f))
    for (x0, y0, x1, y1) in regions:
        for x in range(x0, x1):
            img.putpixel((x, y0), shade(base, 1.5))
            img.putpixel((x, y1 - 1), shade(base, 0.6))
    r = rng(seed + "accent")
    for (x0, y0, x1, y1) in regions:
        for _ in range((x1 - x0) * (y1 - y0) // 24):
            img.putpixel((r.randrange(x0, x1), r.randrange(y0, y1)), accent)
    # Le visage du casque reste ouvert (zone avant de la tete).
    if layer == 1:
        for y in range(10, 16):
            for x in range(10, 14):
                img.putpixel((x, y), (0, 0, 0, 0))
    return img


def logo():
    size = 128
    img = Image.new("RGBA", (size, size), hexc("#140a0c"))
    draw = ImageDraw.Draw(img)
    for y in range(size):
        t = y / size
        draw.line([(0, y), (size, y)], fill=mix(hexc("#2a0a0a"), hexc("#0a0610"), t))
    big = gem("#2a2233", "#c9a6ff").resize((80, 80), Image.NEAREST)
    img.alpha_composite(big, (24, 6))
    font = ImageFont.load_default()
    text = Image.new("RGBA", (52, 12), (0, 0, 0, 0))
    ImageDraw.Draw(text).text((1, 0), "NetheryX", font=font, fill=hexc("#ff8a2a"))
    text = text.resize((120, 30), Image.NEAREST)
    img.alpha_composite(text, (8, 92))
    return img


def generate_misc_textures():
    save(armor_layer("#221c2b", "#8f62d6", 1, "onyx1"), "models", "armor", "onyx_layer_1.png")
    save(armor_layer("#221c2b", "#8f62d6", 2, "onyx2"), "models", "armor", "onyx_layer_2.png")
    save(armor_layer("#7a2a10", "#ffb02e", 1, "magma1"), "models", "armor", "magma_layer_1.png")
    save(armor_layer("#7a2a10", "#ffb02e", 2, "magma2"), "models", "armor", "magma_layer_2.png")
    logo().save(RESOURCES / "netheryx_logo.png")




# ------------------------------------------------------------------------------------------------
# Contenu v1.1
# ------------------------------------------------------------------------------------------------

WITHERED_BARK = ["#1c1c1e", "#262628", "#303033", "#3a3a3d"]
WITHERED_WOOD = ["#5e5a55", "#6e6a64", "#7e7972", "#8e8880", "#9e978e"]
MAGMA = ["#3a0d06", "#5a1408", "#7a1e0a"]


def magma_bricks():
    img = bricks(["#4a1208", "#5c180a", "#6e200c", "#7e280e"], "#1a0604", "magma_bricks")
    r = rng("magma_glow")
    for _ in range(14):
        x, y = r.randrange(16), r.randrange(16)
        if y % 4 != 3:
            img.putpixel((x, y), hexc(r.choice(["#ff6a00", "#ff9a2a", "#ffb02e"])))
    return img


def soul_lily(shape, r):
    stem = hexc("#2f5f63")
    shape.line(7, 15, 7, 8, stem)
    shape.line(8, 15, 9, 10, shade(stem, 0.8))
    shape.put(5, 12, hexc("#3f7f83"))
    shape.put(10, 11, hexc("#3f7f83"))
    for (x, y) in ((5, 5), (6, 4), (7, 3), (8, 4), (9, 5), (6, 6), (7, 6), (8, 6), (5, 7), (9, 7), (7, 5)):
        shape.put(x, y, hexc("#7fe8ff"))
    for (x, y) in ((7, 4), (6, 5), (8, 5)):
        shape.put(x, y, hexc("#e0fbff"))
    shape.put(7, 7, hexc("#2aa8c0"))


def withered_fungus(shape, r):
    stem = hexc("#b8b0a0")
    shape.line(7, 9, 7, 15, stem)
    shape.line(8, 10, 8, 15, shade(stem, 0.75))
    shape.ellipse(7.5, 7.5, 5.5, 3.0, hexc("#4a4a4e"))
    for (x, y) in ((5, 6), (9, 6), (7, 8)):
        shape.put(x, y, hexc("#d8d0c0"))


def lantern_texture(cage_hex, glow_hex):
    """Texture au format du gabarit vanilla de lanterne (corps, chapeau, anse)."""
    img = new()
    cage, glow = hexc(cage_hex), hexc(glow_hex)
    for y in range(2, 9):
        for x in range(0, 6):
            edge = x in (0, 5) or y in (2, 8)
            img.putpixel((x, y), shade(cage, 1.1) if edge else mix(glow, hexc("#ffffff"), 0.15 if (x + y) % 3 else 0.4))
    for y in range(9, 15):
        for x in range(0, 6):
            img.putpixel((x, y), shade(cage, 1.25 if x in (0, 5) or y in (9, 14) else 0.9))
    for y in range(0, 2):
        for x in range(1, 5):
            img.putpixel((x, y), shade(cage, 1.3))
    for y in range(10, 14):
        for x in range(1, 5):
            img.putpixel((x, y), shade(cage, 1.0))
    for y in range(1, 3):
        for x in range(11, 14):
            img.putpixel((x, y), shade(cage, 1.1))
    for y in range(10, 16):
        for x in range(11, 14):
            img.putpixel((x, y), shade(cage, 0.95))
    return img


def lantern_item(cage_hex, glow_hex):
    shape = Shape()
    cage, glow = hexc(cage_hex), hexc(glow_hex)
    shape.line(7, 1, 8, 1, cage)
    shape.line(6, 2, 9, 2, cage)
    for y in range(3, 13):
        for x in range(5, 11):
            edge = x in (5, 10) or y in (3, 12)
            shape.put(x, y, shade(cage, 1.15) if edge else glow)
    shape.line(5, 13, 10, 13, shade(cage, 0.8))
    return shape.render(outline=OUTLINE)


def carapace_icon():
    rows = ["", "", "..###......###..", "..#+##....##+#..", "..############..", "...##-####-##...",
            "....###++###....", "....##-##-##....", "....########....", "....##-##-##....", "....########....",
            "....--------...."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#3a3a40"), OUTLINE)
    for (x, y) in ((7, 6), (8, 6), (7, 9), (8, 9)):
        img.putpixel((x, y), hexc("#ff6a1a"))
    return img


def bone_item(color_hex):
    shape = Shape()
    color = hexc(color_hex)
    shape.line(4, 11, 11, 4, color, width=2)
    for (x, y) in ((3, 11), (4, 12), (11, 3), (12, 4), (3, 12), (12, 3)):
        shape.put(x, y, shade(color, 1.2))
    return shape.render(outline=OUTLINE)


def recall_stone():
    shape = Shape()
    shape.ellipse(8, 8.5, 5.5, 6, hexc("#3a3048"))
    for (x, y) in ((8, 4), (7, 5), (9, 5), (8, 6), (6, 8), (10, 8), (8, 10), (8, 12), (7, 11), (9, 11)):
        shape.put(x, y, hexc("#d07cff"))
    shape.put(8, 8, hexc("#ffffff"))
    return shape.render(outline=OUTLINE)


def elixir():
    rows = ["", "", "......##........", "......##........", ".....#..#.......", "....#....#......",
            "...#......#.....", "...#......#.....", "...#......#.....", "....#....#......", ".....####......."]
    shape = Shape()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row.ljust(16, ".")):
            if ch == "#":
                shape.put(x, y, hexc("#c8e8f0", 220))
    for (y, x0, x1) in ((6, 4, 9), (7, 4, 9), (8, 4, 9), (5, 5, 8), (9, 5, 8)):
        for x in range(x0, x1 + 1):
            shape.put(x, y, hexc("#7fe8ff") if (x + y) % 3 else hexc("#e0fbff"))
    shape.put(6, 2, hexc("#8a6a4a"))
    shape.put(7, 2, hexc("#8a6a4a"))
    return shape.render(outline=OUTLINE)


def staff():
    shape = Shape()
    shape.line(2, 14, 11, 5, hexc("#5a2a14"))
    shape.disc(12, 4, 2.6, hexc("#ff6a1a"))
    shape.put(12, 3, hexc("#ffd36b"))
    shape.put(11, 4, hexc("#ffb02e"))
    for (x, y) in ((10, 2), (14, 6), (13, 1)):
        shape.put(x, y, hexc("#3a0d06"))
    return shape.render(outline=OUTLINE)


def generate_v11():
    save(noise_fill(new(), ["#4a4746", "#57534f", "#635e59", "#6f6a64", "#7b756e"], "withered_soil", blur=1, jitter=0.4),
         "block", "withered_soil.png")
    save(stem_side(WITHERED_BARK, ["#7fe8ff", "#4fb3c0"], "withered_stem"), "block", "withered_stem.png")
    save(stem_top(WITHERED_BARK, WITHERED_WOOD, "withered_stem_top", ("#c4f8ff", "#7fe8ff")), "block", "withered_stem_top.png")
    save(stem_side(WITHERED_WOOD, [], "stripped_withered_stem", veins=False), "block", "stripped_withered_stem.png")
    save(stem_top(WITHERED_WOOD, WITHERED_WOOD, "stripped_withered_stem_top", ("#b8b0a0", "#9e978e")), "block", "stripped_withered_stem_top.png")
    save(planks(WITHERED_WOOD[:4], "withered_planks"), "block", "withered_planks.png")
    save(door_half(True, WITHERED_WOOD[:4], "#222224", "withered"), "block", "withered_door_top.png")
    save(door_half(False, WITHERED_WOOD[:4], "#222224", "withered"), "block", "withered_door_bottom.png")
    save(trapdoor(WITHERED_WOOD[:4], "#222224", "withered"), "block", "withered_trapdoor.png")
    save(door_item(WITHERED_WOOD[:4], "#222224", "withered"), "item", "withered_door.png")
    save(noise_fill(new(), ["#2a2a2e", "#36363b", "#424248", "#55555c", "#7fe8ff"], "withered_wart", blur=1, jitter=0.3),
         "block", "withered_wart_block.png")
    save(cross_plant(withered_fungus, "withered_fungus"), "block", "withered_fungus.png")
    save(cross_plant(soul_lily, "soul_lily"), "block", "soul_lily.png")
    save(magma_bricks(), "block", "magma_bricks.png")
    save(lantern_texture("#1f1a26", "#b06cff"), "block", "onyx_lantern.png")
    save(lantern_item("#1f1a26", "#b06cff"), "item", "onyx_lantern.png")
    lamp = noise_fill(new(), ["#ff8a1a", "#ffa02e", "#ffb84a", "#ffd36b"], "ember_lamp", blur=1, jitter=0.3)
    save(bevel(lamp, 1.1, 0.6, width=1), "block", "ember_lamp.png")

    save(dust(["#7fe8ff", "#c4f8ff", "#3fb3c0", "#e0fbff"], "soul_dust"), "item", "soul_dust.png")
    save(bone_item("#4a4a4e"), "item", "withered_bone.png")
    save(gem("#3a3a40", "#ff6a1a"), "item", "basalt_carapace.png")
    save(carapace_icon(), "item", "carapace_chestplate.png")
    save(recall_stone(), "item", "recall_stone.png")
    save(elixir(), "item", "glimmer_elixir.png")
    save(tool("sword", "withered", DARK_HANDLE), "item", "withered_blade.png")
    save(staff(), "item", "warlock_staff.png")
    seal = noise_fill(new(), ["#120e18", "#1a1422", "#221a2c"], "guardian_seal", blur=1, jitter=0.1)
    for (x, y) in ((3, 3), (4, 4), (5, 5), (10, 5), (11, 4), (12, 3), (7, 7), (8, 8), (7, 8), (8, 7), (5, 10), (4, 11),
                   (3, 12), (10, 10), (11, 11), (12, 12), (7, 2), (8, 2), (7, 13), (8, 13), (2, 7), (2, 8), (13, 7), (13, 8)):
        seal.putpixel((x, y), hexc("#d07cff"))
    save(bevel(seal, 1.6, 0.5), "block", "guardian_seal.png")
    save(armor_layer("#3a3a40", "#ff6a1a", 1, "carapace1"), "models", "armor", "carapace_layer_1.png")
    save(armor_layer("#3a3a40", "#ff6a1a", 2, "carapace2"), "models", "armor", "carapace_layer_2.png")


def generate():
    generate_block_textures()
    generate_item_textures()
    generate_misc_textures()
    generate_v11()
    generate_v13()
    generate_v14()
    generate_v110()
    from . import textures_v120
    textures_v120.generate()
    from . import textures_v121
    textures_v121.generate()
    from . import textures_v122
    textures_v122.generate()
    from . import textures_v123
    textures_v123.generate()
    from . import textures_v124
    textures_v124.generate()
    from . import textures_v130
    textures_v130.generate()
    from . import textures_v131
    textures_v131.generate()


# ------------------------------------------------------------------------------------------------
# Contenu v1.3 (et refonte des arbres fletris)
# ------------------------------------------------------------------------------------------------

def leafy(base_hex, leaf_palette, seed, specks=(), speck_count=0):
    """Feuillage : feuilles en losange ombrees (reflet en haut a gauche, ombre en bas a droite) sur un fond sombre."""
    img = Image.new("RGBA", (16, 16), hexc(base_hex))
    r = rng(seed)
    centers = [(x + r.randint(-1, 1), y + r.randint(-1, 1)) for y in range(0, 16, 4) for x in range((y // 4) % 2 * 2, 16, 4)]
    r.shuffle(centers)
    leaf = [(0, -2), (-1, -1), (0, -1), (1, -1), (-2, 0), (-1, 0), (0, 0), (1, 0), (2, 0), (-1, 1), (0, 1), (1, 1), (0, 2)]
    for (cx, cy) in centers:
        color = hexc(leaf_palette[r.randrange(len(leaf_palette))])
        for (dx, dy) in leaf:
            img.putpixel(((cx + dx) % 16, (cy + dy) % 16), shade(color, 1.0 - (dx + dy) * 0.07))
        img.putpixel(((cx - 1) % 16, (cy - 1) % 16), shade(color, 1.3))
        img.putpixel(((cx + 1) % 16, (cy + 2) % 16), shade(color, 0.6))
    for _ in range(speck_count):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc(r.choice(specks)))
    return img


def bark(palette, crack_hex, streak_hex, seed, glints=()):
    img = new()
    r = rng(seed)
    for x in range(16):
        base = r.randrange(len(palette))
        for y in range(16):
            idx = max(0, min(len(palette) - 1, base + r.choice((-1, 0, 0, 0, 1))))
            img.putpixel((x, y), hexc(palette[idx]))
    for start in (1, 6, 11):
        x = start
        for y in range(16):
            img.putpixel((x % 16, y), hexc(crack_hex))
            if r.random() < 0.25:
                x += r.choice((-1, 1))
    for x in (3, 9, 14):
        y0 = r.randrange(16)
        for k in range(r.randint(3, 6)):
            img.putpixel((x, (y0 + k) % 16), hexc(streak_hex))
    for g in glints:
        img.putpixel(g, hexc("#6fd8e8"))
    return img


def grain_wood(palette, line_hex, seed):
    img = new()
    r = rng(seed)
    for x in range(16):
        col = hexc(palette[(x // 2 + r.randrange(2)) % len(palette)])
        for y in range(16):
            img.putpixel((x, y), shade(col, 0.95 + 0.1 * r.random()))
    for x in (2, 7, 12):
        for y in range(16):
            if r.random() < 0.8:
                img.putpixel((x, y), hexc(line_hex))
    return img


def rings_top(bark_hex, rings, center_hex):
    img = new()
    for y in range(16):
        for x in range(16):
            ring = min(x, y, 15 - x, 15 - y)
            img.putpixel((x, y), hexc(bark_hex) if ring == 0 else hexc(rings[ring % len(rings)]))
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
        img.putpixel((x, y), hexc(center_hex))
    return img


def planks_from(palette, seam_hex, seed):
    img = new()
    r = rng(seed)
    for y in range(16):
        row_col = palette[(y // 4) % len(palette)]
        for x in range(16):
            c = shade(hexc(row_col), 0.92 + 0.14 * r.random())
            img.putpixel((x, y), c)
    for y in range(16):
        for x in range(16):
            if y % 4 == 3:
                img.putpixel((x, y), hexc(seam_hex))
            elif y % 4 == 0:
                img.putpixel((x, y), shade(img.getpixel((x, y)), 1.12))
    for row, seam in enumerate((4, 11, 1, 8)):
        for y in range(row * 4, row * 4 + 3):
            img.putpixel((seam, y), hexc(seam_hex))
    return img


def flower(kind):
    shape = Shape()
    stem = hexc("#4f6b2a") if kind != "glowcap" else hexc("#2f5f63")
    if kind == "glowcap":
        for (x, y, h) in ((5, 10, 5), (9, 8, 7), (12, 11, 4)):
            shape.line(x, 15, x, 15 - h + 2, hexc("#c8d8d0"))
            shape.ellipse(x + 0.5, 15 - h + 1.5, 2.2, 1.4, hexc("#5fe8e0"))
            shape.put(x - 1, 15 - h + 1, hexc("#d0fff8"))
        return shape.render()
    shape.line(7, 15, 7, 8, stem)
    shape.put(6, 12, shade(stem, 1.2))
    shape.put(8, 11, shade(stem, 1.2))
    shape.put(5, 13, shade(stem, 1.1))
    if kind == "ember_bloom":
        for (dx, dy) in ((-2, 0), (2, 0), (0, -2), (0, 2), (-1, -1), (1, 1), (-1, 1), (1, -1)):
            shape.put(7 + dx, 6 + dy, hexc("#ff7a1a") if abs(dx) + abs(dy) == 2 else hexc("#ff9a3a"))
        shape.put(7, 6, hexc("#ffe066"))
    elif kind == "ash_daisy":
        for (dx, dy) in ((-2, 0), (2, 0), (0, -2), (0, 2), (-2, -1), (2, 1), (-1, 2), (1, -2)):
            shape.put(7 + dx, 6 + dy, hexc("#f0ece4"))
        for (dx, dy) in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            shape.put(7 + dx, 6 + dy, hexc("#d8d2c8"))
        shape.put(7, 6, hexc("#6a6460"))
    elif kind == "crimson_bellflower":
        shape.line(7, 8, 10, 5, stem)
        shape.line(7, 9, 4, 6, stem)
        for (x, y) in ((10, 6), (4, 7), (7, 4)):
            shape.put(x, y, hexc("#d8203a"))
            shape.put(x - 1, y + 1, hexc("#b0182e"))
            shape.put(x + 1, y + 1, hexc("#b0182e"))
            shape.put(x, y + 1, hexc("#ff4a5e"))
    elif kind == "sulfur_tulip":
        for y in range(3, 8):
            for x in range(5, 10):
                if (x in (5, 9) and y < 4) or (y == 7 and x in (5, 9)):
                    continue
                shape.put(x, y, hexc("#f4dc3a") if (x + y) % 3 else hexc("#ffee7a"))
        shape.put(7, 3, hexc("#c8a816"))
    return shape.render()


def grass_tufts(colors, seed):
    shape = Shape()
    r = rng(seed)
    for x0 in range(1, 15, 2):
        h = r.randint(5, 12)
        x = x0
        for k in range(h):
            shape.put(x, 15 - k, hexc(colors[min(len(colors) - 1, k * len(colors) // h)]))
            if r.random() < 0.2:
                x += r.choice((-1, 1))
    return shape.render()


def wheat_stage(stage):
    shape = Shape()
    r = rng(f"cinder{stage}")
    h = [4, 7, 10, 13][stage]
    for x in (2, 5, 8, 11, 14):
        hh = h - r.randint(0, 2)
        shape.line(x, 15, x, 15 - hh, hexc("#8a8270") if stage < 3 else hexc("#b8a070"))
        if stage >= 2:
            for k in range(3):
                shape.put(x - 1 if k % 2 else x + 1, 15 - hh + k + 1, hexc("#c8b080") if stage == 3 else hexc("#9a9078"))
        if stage == 3:
            shape.put(x, 15 - hh, hexc("#ffcf6a"))
    return shape.render()


def bow_frame(pull):
    """Arc en onyx : branches en onyx, poignee en blaze, corde tiree de 0 a 3 pixels."""
    shape = Shape()
    limb, light, grip = hexc("#2a2233"), hexc("#a37cf0"), hexc("#ffc531")
    ends = ((13, 2), (2, 13))
    for t in [i / 24 for i in range(25)]:
        x = (1 - t) ** 2 * 13 + 2 * (1 - t) * t * 1 + t * t * 2
        y = (1 - t) ** 2 * 2 + 2 * (1 - t) * t * 1 + t * t * 13
        shape.put(round(x), round(y), light if t < 0.15 or t > 0.85 else limb)
    for (x, y) in ((5, 5), (4, 5), (5, 4)):
        shape.put(x, y, grip)
    mx, my = 7 + pull, 7 + pull
    string = hexc("#d8d8d8")
    for (ex, ey) in ends:
        steps = max(abs(ex - mx), abs(ey - my))
        for i in range(steps + 1):
            shape.put(round(ex + (mx - ex) * i / steps), round(ey + (my - ey) * i / steps), string)
    if pull:
        for k in range(1, 7):
            shape.put(mx - k, my - k, hexc("#8a6a4a"))
        shape.put(mx - 7, my - 7, hexc("#d0d0d0"))
        shape.put(mx + 1, my + 1, hexc("#f0f0f0"))
    return shape.render(outline=None)


def mob_sheet(palette, seed, size=(64, 32)):
    img = Image.new("RGBA", size, (0, 0, 0, 255))
    field = value_noise(64, seed, 1)
    r = rng(seed)
    for y in range(size[1]):
        for x in range(size[0]):
            v = min(0.999, field[y % 64][x % 64] + (r.random() - 0.5) * 0.2)
            img.putpixel((x, y), hexc(palette[int(max(0.0, v) * len(palette))]))
    return img


def fire_ghast(shooting):
    img = mob_sheet(["#7a1e06", "#a8300a", "#d24a0f", "#f07a1f", "#ffb02e"], "fire_ghast")
    eye = hexc("#2a0606")
    face_x, face_y = 16, 16
    if shooting:
        for (x0, y0) in ((19, 21), (26, 21)):
            for dx in range(3):
                for dy in range(3):
                    img.putpixel((x0 + dx, y0 + dy), hexc("#ffee7a") if (dx, dy) == (1, 1) else eye)
        for x in range(20, 28):
            for y in range(26, 29):
                img.putpixel((x, y), eye)
    else:
        for (x0, y0) in ((19, 22), (26, 22)):
            for dx in range(3):
                img.putpixel((x0 + dx, y0), eye)
        for x in range(21, 27):
            img.putpixel((x, 27), eye)
    return img


def onyx_cube():
    img = mob_sheet(["#0e0c12", "#16131c", "#1f1a26", "#2a2233"], "onyx_cube")
    r = rng("onyx_cube_cracks")
    for _ in range(40):
        img.putpixel((r.randrange(64), r.randrange(32)), hexc(r.choice(["#7d3fe0", "#b06cff", "#d07cff"])))
    for y in range(16, 32):
        for x in range(0, 24):
            if r.random() < 0.5:
                img.putpixel((x, y), hexc(r.choice(["#7d3fe0", "#b06cff"])))
    return img


def egg(base_hex, speck_hex):
    shape = Shape()
    shape.ellipse(8, 9, 4.2, 5.3, hexc(base_hex))
    r = rng("egg" + base_hex)
    for _ in range(6):
        shape.put(r.randint(6, 10), r.randint(6, 12), hexc(speck_hex))
    return shape.render(outline=OUTLINE)


def fried_egg():
    shape = Shape()
    shape.ellipse(8, 9, 6, 4.5, hexc("#f4f0e8"), shaded=False)
    shape.disc(8, 8.5, 2.2, hexc("#ff8a1a"))
    shape.put(7, 7, hexc("#ffd36b"))
    return shape.render(outline=OUTLINE)


def bread():
    rows = ["", "", "", "", "....########....", "..############..", ".##+#+#+#+#####.", ".##############.",
            ".##############.", "..############..", "...----------...", ""]
    return shaded_mask([r.ljust(16, ".") for r in rows], hexc("#7a5a3a"), OUTLINE)


def wheat_bundle():
    shape = Shape()
    for x in (5, 7, 9, 11):
        shape.line(x - 2, 14, x + 1, 3, hexc("#b8a070"))
        shape.put(x + 1, 3, hexc("#ffcf6a"))
        shape.put(x, 5, hexc("#c8b080"))
    shape.line(5, 10, 10, 10, hexc("#7a3a1a"))
    return shape.render(outline=OUTLINE)


def generate_v13():
    # Refonte des arbres fletris : feuillage en grappes, ecorce crevassee, bois patine
    withered_leaves = leafy("#2a2634", ["#5a5470", "#6e6688", "#857ca0", "#9a92b4"], "w1",
                            specks=("#c4f8ff", "#7fe8ff"), speck_count=3)
    save(withered_leaves, "block", "withered_wart_block.png")
    save(bark(["#2b2a2e", "#323136", "#3a383e", "#424046"], "#18171b", "#6e6a78", "withered_bark_v2",
              glints=((4, 5), (12, 11))), "block", "withered_stem.png")
    save(rings_top("#2b2a2e", ["#8c8580", "#a39a92", "#b5ada4", "#9a928a"], "#d8d0c4"), "block", "withered_stem_top.png")
    save(grain_wood(["#8c8580", "#9a928a", "#a39a92", "#b5ada4"], "#6f6862", "stripped_withered_v2"), "block",
         "stripped_withered_stem.png")
    save(rings_top("#8c8580", ["#a39a92", "#b5ada4", "#9a928a"], "#d8d0c4"), "block", "stripped_withered_stem_top.png")
    withered_planks = ["#7a726b", "#8a8178", "#968c82", "#a3988d"]
    save(planks_from(withered_planks, "#4e4843", "withered_planks_v2"), "block", "withered_planks.png")
    save(door_half(True, withered_planks, "#3a3632", "withered"), "block", "withered_door_top.png")
    save(door_half(False, withered_planks, "#3a3632", "withered"), "block", "withered_door_bottom.png")
    save(trapdoor(withered_planks, "#3a3632", "withered"), "block", "withered_trapdoor.png")
    save(door_item(withered_planks, "#3a3632", "withered"), "item", "withered_door.png")
    soil = noise_fill(new(), ["#3c3a3e", "#46434a", "#504c54", "#5a565e"], "withered_soil_v2", blur=1, jitter=0.25)
    r = rng("bones")
    for _ in range(7):
        x, y = r.randrange(15), r.randrange(16)
        soil.putpixel((x, y), hexc("#cfc8b8"))
        soil.putpixel((x + 1, y), hexc("#b8b0a0"))
    save(soil, "block", "withered_soil.png")

    # Prairie de braise
    grass_top = noise_fill(new(), ["#5a5420", "#6b6424", "#7d752a", "#908732", "#a39a3c"], "nether_grass_top", blur=1, jitter=0.35)
    r = rng("meadow_specks")
    for _ in range(5):
        grass_top.putpixel((r.randrange(16), r.randrange(16)), hexc(r.choice(["#ff7a5a", "#ffb347", "#f0ece4"])))
    save(grass_top, "block", "nether_grass_block.png")
    side = netherrack("nether_grass_side")
    r = rng("grass_fringe")
    for x in range(16):
        for y in range(r.choice((2, 3, 3, 4, 5))):
            side.putpixel((x, y), grass_top.getpixel((x, y)))
    save(side, "block", "nether_grass_block_side.png")
    save(grass_tufts(["#5a5420", "#7d752a", "#a39a3c", "#c8bc56"], "nether_grass"), "block", "nether_grass.png")
    for kind in ("ember_bloom", "ash_daisy", "crimson_bellflower", "sulfur_tulip", "glowcap"):
        save(flower(kind), "block", f"{kind}.png")
    for stage in range(4):
        save(wheat_stage(stage), "block", f"cinder_wheat_stage{stage}.png")

    # Objets
    save(bow_frame(0), "item", "onyx_bow.png")
    for i in range(3):
        save(bow_frame(i + 1), "item", f"onyx_bow_pulling_{i}.png")
    save(seeds("#a89a80"), "item", "cinder_wheat_seeds.png")
    save(wheat_bundle(), "item", "cinder_wheat.png")
    save(bread(), "item", "cinder_bread.png")
    gstew = stew()
    for x in range(3, 13):
        gstew.putpixel((x, 6), hexc("#5fe8e0"))
        gstew.putpixel((x, 7), hexc("#9ff8f0") if x % 3 else hexc("#d0fff8"))
    save(gstew, "item", "glowcap_stew.png")
    save(meat("#c8402a", "#ffb347"), "item", "infernal_beef.png")
    save(meat("#6a2a12", "#d88a3a"), "item", "cooked_infernal_beef.png")
    save(meat("#e8b0a0", "#ff8a6a"), "item", "infernal_chicken.png")
    save(meat("#b8783a", "#e8b070"), "item", "cooked_infernal_chicken.png")
    save(egg("#ff8a3a", "#7a1e06"), "item", "ember_egg.png")
    save(fried_egg(), "item", "fried_ember_egg.png")

    # Creatures a modele vanilla
    save(fire_ghast(False), "entity", "fire_ghast.png")
    save(fire_ghast(True), "entity", "fire_ghast_shooting.png")
    save(onyx_cube(), "entity", "onyx_cube.png")


# ------------------------------------------------------------------------------------------------
# v1.4 : onyx facon diamant, cube d'onyx avec yeux, ghast de feu, enclume et four en onyx
# ------------------------------------------------------------------------------------------------

ONYX_FACETS = {"o": "#0a0610", "d": "#1b1226", "m": "#2c2040", "l": "#4a3870", "h": "#9a70e8", "w": "#f0e4ff"}


def char_art(rows, colors, size=16):
    img = new(size)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                img.putpixel((x, y), hexc(colors[ch]))
    return img


def onyx_gem():
    rows = ["", "", "",
            "....oooooooo....",
            "...ohwwhllmmo...",
            "..ohwhllllmmdo..",
            ".ohhlllmmmmmddo.",
            ".odmmmmmmmmmddo.",
            "..olmmmmmmmddo..",
            "...olmmmmmddo...",
            "....olmmmddo....",
            ".....olmddo.....",
            "......oldo......",
            ".......oo......."]
    return char_art(rows, ONYX_FACETS)


def onyx_shard_v2():
    rows = ["", "", "",
            "..........oo....",
            ".........ohwo...",
            "...oo...ohlmo...",
            "..ohwo..olmdo...",
            "..ohmdo..omdo...",
            "..olmdo...oo....",
            "...odo.....oo...",
            "....o.....ohmo..",
            "..........olmdo.",
            "...........odo..",
            "............o..."]
    return char_art(rows, ONYX_FACETS)


def onyx_ore_v2():
    img = netherrack("onyx_ore_v2")
    chunks = [(2, 2), (10, 1), (6, 7), (12, 9), (1, 11), (8, 13)]
    pattern = [".hw.", "hlmd", ".md."]
    tones = {"h": "#9a70e8", "w": "#f0e4ff", "l": "#4a3870", "m": "#2c2040", "d": "#1b1226"}
    shape = Shape()
    for (cx, cy) in chunks:
        for dy, row in enumerate(pattern):
            for dx, ch in enumerate(row):
                if ch in tones:
                    shape.put((cx + dx) % 16, (cy + dy) % 16, hexc(tones[ch]))
    return shape.render(outline=hexc("#0a0610"), img=img)


def onyx_block_v2():
    img = noise_fill(new(), ["#221932", "#2a1f3d", "#312447", "#382a52"], "onyx_block_v2", blur=2, jitter=0.05)
    for k in range(16):
        img.putpixel((k, 0), hexc("#6a50a0"))
        img.putpixel((0, k), hexc("#6a50a0"))
        img.putpixel((k, 15), hexc("#0a0610"))
        img.putpixel((15, k), hexc("#0a0610"))
    for k in range(2, 14):
        img.putpixel((k, 2), hexc("#140d1e"))
        img.putpixel((2, k), hexc("#140d1e"))
        img.putpixel((k, 13), hexc("#4a3870"))
        img.putpixel((13, k), hexc("#4a3870"))
    for i in range(4):
        img.putpixel((4 + i, 3 + i), hexc("#9a70e8"))
        img.putpixel((9 + i, 8 + i), hexc("#4a3870"))
    for (x, y) in ((4, 3), (5, 4), (11, 4), (10, 5)):
        img.putpixel((x, y), hexc("#f0e4ff"))
    img.putpixel((12, 3), hexc("#9a70e8"))
    return img


def onyx_cube_v2():
    """Texture au patron exact du modele vanilla du cube de magma (64x32), avec des yeux."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    field = value_noise(64, "onyx_cube_v2", 1)
    palette = [hexc(c) for c in ("#0e0a14", "#17111f", "#1f172a", "#2a2036", "#342842")]
    r = rng("onyx_cube_v2_cracks")
    segments = [(0, 0), (0, 1), (24, 10), (24, 19), (0, 4), (0, 5), (0, 6), (0, 7)]
    for (j, k) in segments:
        boxes = [(j + 8, k, 8, 8), (j + 16, k, 8, 8), (j, k + 8, 8, 1), (j + 8, k + 8, 8, 1), (j + 16, k + 8, 8, 1),
                 (j + 24, k + 8, 8, 1)]
        for (bx, by, bw, bh) in boxes:
            for y in range(by, by + bh):
                for x in range(bx, bx + bw):
                    color = palette[int(field[y % 64][x % 64] * 0.999 * len(palette))]
                    if r.random() < 0.07:
                        color = hexc(r.choice(["#7d3fe0", "#b06cff"]))
                    img.putpixel((x, y), color)
    # Yeux : rangees avant des segments 2 (y=18) et 3 (y=27), colonnes 32..39
    for x in (33, 34, 37, 38):
        img.putpixel((x, 18), hexc("#f0e4ff"))
    for (x, color) in ((33, "#d07cff"), (34, "#140a1e"), (37, "#140a1e"), (38, "#d07cff")):
        img.putpixel((x, 27), hexc(color))
    # Bouche (segment 5, rangee avant y=13)
    for x in range(10, 14):
        img.putpixel((x, 13), hexc("#07040a"))
    # Coeur lumineux (cube interieur 4x4x4, texOffs 0,16)
    for y in range(16, 24):
        for x in range(0, 16):
            img.putpixel((x, y), hexc(r.choice(["#7d3fe0", "#9a5cf0", "#b06cff", "#d07cff"])))
    return img


def fire_ghast_v2(shooting):
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    field = value_noise(64, "fire_ghast_v2", 1)
    flame = ["#5a1004", "#8a1e06", "#c0360a", "#e85a10", "#ff8a1a", "#ffb02e", "#ffe27a"]

    def paint(x0, y0, w, h, bright):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                t = (y - y0) / max(1, h - 1)
                level = bright * (1 - t) + 0.25 * field[y % 64][x % 64]
                img.putpixel((x, y), hexc(flame[max(0, min(len(flame) - 1, int(level * len(flame))))]))

    paint(16, 0, 16, 16, 1.0)   # dessus
    paint(32, 0, 16, 16, 0.25)  # dessous
    for x0 in (0, 16, 32, 48):  # cotes
        paint(x0, 16, 16, 16, 0.85)
    paint(0, 0, 8, 16, 0.6)     # tentacules
    eye = hexc("#2a0404")
    if shooting:
        for (x0, y0) in ((19, 21), (26, 21)):
            for dx in range(3):
                for dy in range(3):
                    img.putpixel((x0 + dx, y0 + dy), hexc("#fff4b0") if (dx, dy) == (1, 1) else eye)
        for x in range(20, 28):
            for y in range(26, 29):
                img.putpixel((x, y), eye if y != 26 or x % 2 else hexc("#ffe27a"))
    else:
        for (x0, y0) in ((19, 22), (26, 22)):
            for dx in range(3):
                img.putpixel((x0 + dx, y0), eye)
            img.putpixel((x0 + 1, y0 + 1), hexc("#ffe27a"))
        for x in range(21, 27):
            img.putpixel((x, 27), eye)
    return img


def metal(seed, base, light, dark):
    img = noise_fill(new(), base, seed, blur=2, jitter=0.08)
    for k in range(16):
        img.putpixel((k, 0), hexc(light))
        img.putpixel((k, 15), hexc(dark))
    return img


def furnace_front(lit):
    img = noise_fill(new(), ["#1a1424", "#221a2e", "#2a2038"], "onyx_furnace_front", blur=1, jitter=0.1)
    for k in range(16):
        for (x, y) in ((k, 0), (0, k)):
            img.putpixel((x, y), hexc("#4a3870"))
        for (x, y) in ((k, 15), (15, k)):
            img.putpixel((x, y), hexc("#0a0610"))
    for y in range(8, 14):
        for x in range(3, 13):
            if lit:
                t = (y - 8) / 5
                img.putpixel((x, y), hexc("#2aa8c0" if t > 0.7 else ("#7fe8ff" if (x + y) % 3 else "#e0fbff")))
            else:
                img.putpixel((x, y), hexc("#07040a"))
    for x in range(3, 13):
        img.putpixel((x, 7), hexc("#9a70e8"))
    for (x, y) in ((4, 3), (8, 3), (11, 3)):
        img.putpixel((x, y), hexc("#0a0610"))
        img.putpixel((x, y + 1), hexc("#0a0610"))
    return img


def generate_v14():
    save(onyx_gem(), "item", "onyx.png")
    save(onyx_shard_v2(), "item", "onyx_shard.png")
    save(onyx_ore_v2(), "block", "onyx_ore.png")
    save(onyx_block_v2(), "block", "onyx_block.png")
    save(onyx_cube_v2(), "entity", "onyx_cube.png")
    save(fire_ghast_v2(False), "entity", "fire_ghast.png")
    save(fire_ghast_v2(True), "entity", "fire_ghast_shooting.png")
    body = metal("onyx_anvil", ["#1e1628", "#261c33", "#2e223e"], "#6a50a0", "#0a0610")
    save(body, "block", "onyx_anvil.png")
    top = metal("onyx_anvil_top", ["#2a1f3d", "#312447", "#382a52"], "#9a70e8", "#140d1e")
    for k in range(3, 13):
        top.putpixel((k, 7), hexc("#4a3870"))
        top.putpixel((k, 8), hexc("#1b1226"))
    save(top, "block", "onyx_anvil_top.png")
    save(furnace_front(False), "block", "onyx_furnace_front.png")
    save(furnace_front(True), "block", "onyx_furnace_front_on.png")
    save(bevel(noise_fill(new(), ["#1a1424", "#221a2e", "#2a2038"], "onyx_furnace_side", blur=1, jitter=0.1), 1.6, 0.5),
         "block", "onyx_furnace_side.png")
    save(onyx_block_v2(), "block", "onyx_furnace_top.png")


# ------------------------------------------------------------------------------------------------
# Contenu 1.1.0 : bateau et seau d'obsidienne
# ------------------------------------------------------------------------------------------------

OBSIDIAN = {"o": "#050208", "h": "#7a4fc0", "l": "#3b2a5e", "m": "#1f1430", "d": "#120a1c", "p": "#a86cf0",
            "r": "#e0a030", "R": "#9a6418"}


def obsidian_boat_item():
    rows = ["", "", "",
            "......rR........",
            ".......rR.......",
            "........rR......",
            "oo.......rR...oo",
            "ohoooooooorooolo",
            "ohhmmmmpmmmmmlmo",
            ".ommmmmmmmmpmmo.",
            ".odmmpmmmmmmmdo.",
            "..oddddddddddo..",
            "...oooooooooo..."]
    return char_art(rows, OBSIDIAN)


def obsidian_bucket_item():
    rows = ["", "",
            "......oooo......",
            ".....o....o.....",
            "....o......o....",
            "...oooooooooo...",
            "..ohdddddddddo..",
            "..ohllllllllmo..",
            "..ohmmpmmmmmdo..",
            "...ohmmmmmmdo...",
            "...ohmmmmpmdo...",
            "...ohmmmmmmdo...",
            "....ohmmmmdo....",
            "....oddddddo....",
            ".....oooooo....."]
    return char_art(rows, OBSIDIAN)


def obsidian_boat_entity():
    """Texture 128x64 au format du modele de bateau vanilla : coque en obsidienne veinee, pagaies dorees."""
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    hull = ["#07040c", "#0d0716", "#140c22", "#1c1230", "#26193e"]
    field = value_noise(64, "obsidian_boat", 1)
    r = rng("obsidian_boat_veins")
    for y in range(64):
        for x in range(128):
            v = min(0.999, field[y % 64][x % 64] + (r.random() - 0.5) * 0.15)
            img.putpixel((x, y), hexc(hull[int(v * len(hull))]))
    for _ in range(70):
        x, y = r.randrange(0, 62), r.randrange(0, 51)
        for k in range(r.randint(2, 5)):
            if x + k < 62:
                img.putpixel((x + k, y), hexc("#4a3478" if k % 2 else "#6a4cb0"))
    for _ in range(18):
        img.putpixel((r.randrange(0, 62), r.randrange(0, 51)), hexc("#b07cff"))
    shaft = ["#6a4214", "#8a5a20", "#b07a2a", "#d09a3a"]
    field2 = value_noise(64, "obsidian_boat_paddle", 1)
    for y in range(0, 40):
        for x in range(62, 128):
            v = min(0.999, field2[y % 64][x % 64])
            img.putpixel((x, y), hexc(shaft[int(v * len(shaft))]))
    return img


def generate_v110():
    save(obsidian_boat_item(), "item", "obsidian_boat.png")
    save(obsidian_bucket_item(), "item", "obsidian_bucket.png")
    save(obsidian_boat_entity(), "entity", "obsidian_boat.png")
