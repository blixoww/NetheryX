"""Textures 1.2.0 (suite) : bocaux de lucioles, verre et cristal en fusion, bois petrifie, feuillage cristallise,
cotelettes de blaze, eclat de verre en fusion, Lanterne du Nether et oeuf de wyverne."""
from .pixels import Shape, hexc, new, rng, shade
from .textures import bark, crystals, lantern_item, lantern_texture, rings_top, save
from .textures_v120 import translucent
from .textures_v121 import tool_image
from .textures_v123 import gem, sprite


def firefly_jar(cage_hex, glow_hex, spark_hex, item=False):
    img = lantern_item(cage_hex, glow_hex) if item else lantern_texture(cage_hex, glow_hex)
    r = rng(cage_hex + spark_hex)
    filled = [(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[:3] == hexc(glow_hex)[:3]]
    for (x, y) in r.sample(filled, min(5, len(filled))):
        img.putpixel((x, y), hexc(spark_hex))
    return img


def molten_glass():
    img = translucent(["#c8460e", "#e0601a", "#ff7a1a", "#ffa040"], "molten_glass", 215, "#ffe27a")
    r = rng("molten_glass_bubbles")
    for _ in range(7):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("#fff0a0", 235))
    return img


def crystal_leaves():
    img = translucent(["#4a2c80", "#5a3a98", "#6e48b0", "#8a62d0"], "onyx_crystal_leaves", 200, "#e0ccff")
    r = rng("onyx_leaves_holes")
    for _ in range(14):
        img.putpixel((r.randrange(16), r.randrange(16)), (0, 0, 0, 0))
    return img


def pork(raw):
    shape = Shape()
    base = hexc("#e87a6a" if raw else "#a8501e")
    shape.ellipse(8, 8.5, 5.5, 4.0, base)
    shape.ellipse(5.5, 7.5, 1.6, 1.4, hexc("#f4d0b0" if raw else "#e0b080"), shaded=False)
    for x in range(4, 13):
        if x % 3 == 0:
            shape.put(x, 10, hexc("#ffb020" if raw else "#ffd36b"))
    if not raw:
        for (x, y) in ((7, 7), (10, 8), (9, 11)):
            shape.put(x, y, hexc("#5a2a0e"))
    return shape.render(outline=hexc("#2a0a04"))


def molten_shard():
    rows = ["", "", "........oo......", ".......oAao.....", "......oAaado....", ".....oAaaado....", "....oaAaado.....",
            "....oaaado......", "...oaaddo.......", "...oaddo........", "..odddo.........", "..oddo..........", "...oo...........", ""]
    return gem(rows, {"o": "#2a0804", "a": "#ff7a1a", "A": "#fff0a0", "d": "#c8460e"})


def nether_lantern():
    pal = {"o": "#0b0710", "body": ("#1d1428", "#33244a", "#4e3878", "#7c5cc0"), "w": "#efe4ff",
           "trim": ("#8a5a10", "#c8901c", "#f0c040", "#fff0a0"), "a": "#ff8a1a", "A": "#fff0a0", "k": "#000000"}
    return sprite(["", ".......t", "......t.", ".....ttt", "....####", "....#aAa", "....#aaA", "....#aAa", "....#aaa",
                   "....#aAa", "....####", ".....ttt", "", ""], pal)


def wyvern_egg():
    shape = Shape()
    shape.ellipse(8, 9, 4.8, 6.0, hexc("#4a4646"))
    r = rng("wyvern_egg")
    for _ in range(10):
        x, y = r.randint(4, 11), r.randint(4, 14)
        if (x, y) in shape.pixels:
            shape.put(x, y, hexc("#ff7a2a"))
    for (x, y) in ((6, 5), (5, 7), (6, 6)):
        shape.put(x, y, hexc("#c8c0b8"))
    for (x, y) in ((9, 8), (10, 9), (9, 10), (10, 11)):
        shape.put(x, y, hexc("#ffd36b"))
    return shape.render(outline=hexc("#140c0a"))


def wyvern_whistle():
    """Sifflet taille dans une corne, bague doree et cordon de cuir."""
    shape = Shape()
    horn = ["#e6dcc4", "#d0c4aa", "#b8ac94", "#9a8e78"]
    for k in range(9):
        x, y = 3 + k, 11 - k // 2
        w = 2 if k < 6 else 3
        for dy in range(w):
            shape.put(x, y + dy - (w // 2), hexc(horn[min(3, dy + (1 if k > 5 else 0))]))
    shape.put(12, 5, hexc("#3a2414"))
    for (x, y) in ((6, 9), (6, 10), (7, 9), (7, 10)):
        shape.put(x, y, hexc("#f0c040"))
    for (x, y) in ((3, 12), (2, 13), (2, 14), (3, 15)):
        shape.put(x, y, hexc("#6a4022"))
    return shape.render(outline=hexc("#2a1a10"))


def greatsword(u, v):
    """Lame abyssale : lame large, arete incandescente au centre, garde en croissant."""
    if -3 <= u <= 11 and 12 <= v <= 17:
        if u >= 10 and v in (12, 17):
            return None
        if u == 11 and v in (13, 16):
            return None
        if v in (14, 15):
            return "w" if (u + v) % 3 else "h"
        return {12: "h", 13: "l", 16: "m", 17: "d"}[v]
    if u == 12 and v in (14, 15):
        return "h"
    if u in (-5, -4) and 9 <= v <= 20:
        return "h" if v <= 11 else "l" if v < 15 else "m" if v < 18 else "d"
    if u == -6 and v in (9, 20):
        return "h"
    return None


def magma_bricks_v2():
    """Briques de magma facon briques du Nether : petites briques biseautees (8 x 4), teinte propre a chaque brique,
    joints sombres et quelques fissures de magma incandescentes."""
    img = new()
    r = rng("magma_bricks_v2")
    palette = ["#5e1a0e", "#6c2010", "#7a2614", "#882c16", "#963418"]
    mortar = hexc("#1c0604")
    for row in range(4):
        offset = 4 if row % 2 else 0
        for col in range(-1, 3):
            x0 = col * 8 + offset
            base = hexc(r.choice(palette))
            crack = r.random() < 0.3
            cx, cy = r.randint(2, 5), r.randint(1, 2)
            for dy in range(4):
                for dx in range(8):
                    x, y = x0 + dx, row * 4 + dy
                    if not 0 <= x < 16:
                        continue
                    if dy == 3 or dx == 7:
                        img.putpixel((x, y), mortar)
                        continue
                    f = 1.0 + (r.random() - 0.5) * 0.12
                    if dy == 0 or dx == 0:
                        f += 0.2
                    elif dy == 2 or dx == 6:
                        f -= 0.18
                    img.putpixel((x, y), shade(base, f))
            if crack:
                for (dx, dy, col_hex) in ((cx, cy, "#ff8a1a"), (cx + 1, cy, "#ffb040"), (cx + 2, cy + (1 if cy == 1 else -1), "#e0601a")):
                    x = x0 + dx
                    if 0 <= x < 16:
                        img.putpixel((x, row * 4 + dy), hexc(col_hex))
    return img


def rich_quartz_overlay():
    """Calque du minerai de quartz riche, dans l'esprit du minerai de quartz vanilla (petits eclats blancs ombres
    de rouge sombre), mais plus dense : on le pose sur la netherrack vanilla."""
    img = new()
    tones = {"w": "#ffffff", "h": "#ece6de", "m": "#d4ccc2", "d": "#b0a69c"}
    shapes = [["hw", "md"], [".h.", "hwm", ".md"], ["hm", "m."], ["wh", ".d"], [".hm", "hwd"]]
    spots = [(1, 1), (6, 0), (11, 2), (3, 5), (9, 6), (13, 7), (0, 9), (6, 10), (11, 11), (2, 13), (8, 14), (13, 13)]
    r = rng("rich_quartz_v2")
    cells = {}
    for (sx, sy) in spots:
        shape = r.choice(shapes)
        for dy, row in enumerate(shape):
            for dx, ch in enumerate(row):
                if ch in tones:
                    cells[((sx + dx) % 16, (sy + dy) % 16)] = hexc(tones[ch])
    for (x, y) in list(cells):
        for (sx, sy) in ((x + 1, y + 1), (x, y + 1)):
            if (sx % 16, sy % 16) not in cells:
                img.putpixel((sx % 16, sy % 16), hexc("#3a0c0c", 170))
    for (x, y), col in cells.items():
        img.putpixel((x, y), col)
    return img


def basalt_heartstone():
    """Trophee du Golem de basalte : un bloc de basalte taille, fendu sur un coeur de magma incandescent."""
    shape = Shape()
    shape.ellipse(8, 8.5, 6, 5.5, hexc("#3a3a42"))
    shape.ellipse(8, 8.5, 4.2, 3.8, hexc("#4c4c56"))
    shape.disc(8, 8.5, 2.6, hexc("#ff7a1a"))
    shape.disc(8, 8.5, 1.3, hexc("#ffe27a"), shaded=False)
    for (x0, y0, x1, y1) in ((8, 3, 7, 6), (3, 8, 5, 9), (13, 10, 10, 9), (9, 14, 8, 11)):
        shape.line(x0, y0, x1, y1, hexc("#e0601a"))
    for (x, y) in ((4, 5), (12, 5), (5, 13)):
        shape.put(x, y, hexc("#6a6a74"))
    return shape.render(outline=hexc("#101014"))


def generate():
    save(firefly_jar("#c8d0d4", "#ffb020", "#fff0a0"), "block", "ember_firefly_jar.png")
    save(firefly_jar("#c8d0d4", "#ffb020", "#fff0a0", item=True), "item", "ember_firefly_jar.png")
    save(firefly_jar("#a8c0c8", "#4ad8f0", "#d8ffff"), "block", "soul_firefly_jar.png")
    save(firefly_jar("#a8c0c8", "#4ad8f0", "#d8ffff", item=True), "item", "soul_firefly_jar.png")
    save(molten_glass(), "block", "molten_glass.png")
    save(crystals("#c8460e", "#ff8a1a", "#fff0a0", "molten_crystal"), "block", "molten_crystal.png")
    save(bark(["#1a1a1e", "#24242a", "#2e2e36", "#38383f"], "#6a3fc0", "#46464e", "petrified_log", glints=((4, 5), (11, 9), (7, 13))),
         "block", "petrified_log.png")
    save(rings_top("#24242a", ["#2e2e36", "#3a3a44", "#4a3a68"], "#b06cff"), "block", "petrified_log_top.png")
    save(crystal_leaves(), "block", "onyx_crystal_leaves.png")
    save(pork(True), "item", "blaze_pork.png")
    save(pork(False), "item", "cooked_blaze_pork.png")
    save(molten_shard(), "item", "molten_shard.png")
    save(nether_lantern(), "item", "nether_lantern.png")
    save(wyvern_egg(), "item", "ash_wyvern_egg.png")
    save(tool_image(greatsword, -7, "abyssal"), "item", "abyssal_blade.png")
    save(wyvern_whistle(), "item", "wyvern_whistle.png")
    save(magma_bricks_v2(), "block", "magma_bricks.png")
    save(basalt_heartstone(), "item", "basalt_heartstone.png")
    save(rich_quartz_overlay(), "block", "rich_quartz_ore_overlay.png")
    save(crystals("#c8bcb0", "#ece6de", "#ffffff", "quartz_cluster"), "block", "quartz_cluster.png")
