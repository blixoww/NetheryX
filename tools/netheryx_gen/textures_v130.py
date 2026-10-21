"""Textures 1.3.0 : enclume d'onyx refaite (structure de l'enclume vanilla, en onyx poli veine de violet)."""
from .pixels import hexc, mix, new, rng
from .textures import save


def _brushed(seed, dark, mid, light):
    """Metal brosse : stries horizontales irregulieres, comme l'enclume vanilla."""
    r = rng(seed)
    img = new()
    rows = [r.uniform(-0.25, 0.25) for _ in range(16)]
    for y in range(16):
        for x in range(16):
            t = 0.5 + rows[y] + r.uniform(-0.12, 0.12)
            if r.random() < 0.06:
                t += 0.35
            c = mix(hexc(dark), hexc(mid), max(0.0, min(1.0, t * 1.4))) if t < 0.7 else mix(hexc(mid), hexc(light), min(1.0, (t - 0.7) * 2.5))
            img.putpixel((x, y), c)
    return img


def onyx_anvil_body():
    img = _brushed("onyx_anvil_v3", "#120c1a", "#221832", "#3c2c58")
    r = rng("onyx_anvil_veins")
    # Veines violettes qui courent dans la pierre, et quelques eclats brillants
    for _ in range(3):
        x, y = r.randrange(16), r.randrange(16)
        for _ in range(7):
            img.putpixel((x % 16, y % 16), hexc("#5a3c96"))
            x += 1
            y += r.choice((-1, 0, 0, 1))
    for _ in range(4):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc("#b48cff"))
    return img


def onyx_anvil_top():
    """Face de travail (colonnes 3 a 12 utilisees par le modele) : cadre poli, table usee, rune incrustee."""
    img = _brushed("onyx_anvil_top_v3", "#1a1226", "#2c2040", "#463462")
    r = rng("onyx_anvil_top_wear")
    for y in range(16):
        for x in range(3, 13):
            edge = x in (3, 12) or y in (0, 15)
            inner = x in (4, 11) or y in (1, 14)
            if edge:
                img.putpixel((x, y), hexc("#5a4480" if (x + y) % 3 else "#6e56a0"))
            elif inner:
                img.putpixel((x, y), hexc("#140d1e"))
            else:
                base = hexc("#2e2244") if (y + r.randrange(3)) % 5 else hexc("#3a2c56")
                img.putpixel((x, y), base)
    # Rayures de travail et rune centrale qui luit faiblement
    for _ in range(6):
        x, y = r.randrange(5, 11), r.randrange(2, 14)
        img.putpixel((x, y), hexc("#4e3a72"))
        if x + 1 < 11:
            img.putpixel((x + 1, y), hexc("#4e3a72"))
    # Gemme d'onyx incrustee au centre de la table (contour violet, coeur lumineux)
    for (x, y) in ((7, 5), (8, 5), (6, 6), (9, 6), (5, 7), (10, 7), (5, 8), (10, 8), (6, 9), (9, 9), (7, 10), (8, 10)):
        img.putpixel((x, y), hexc("#9a70e8"))
    for (x, y) in ((7, 6), (8, 6), (6, 7), (9, 7), (6, 8), (9, 8), (7, 9), (8, 9)):
        img.putpixel((x, y), hexc("#5a3c96"))
    for (x, y) in ((7, 7), (8, 8)):
        img.putpixel((x, y), hexc("#e0ccff"))
    for (x, y) in ((8, 7), (7, 8)):
        img.putpixel((x, y), hexc("#b48cff"))
    for y in (3, 12):
        for x in (7, 8):
            img.putpixel((x, y), hexc("#5a4480"))
    return img


def generate():
    save(onyx_anvil_body(), "block", "onyx_anvil.png")
    save(onyx_anvil_top(), "block", "onyx_anvil_top.png")
    generate_fruits()


# ------------------------------------------------------------------------------------------------
# Fruits des arbres du Nether : grappes suspendues (3 stades) et fruits
# ------------------------------------------------------------------------------------------------
from .pixels import Shape  # noqa: E402

FRUIT_STYLES = {
    # nom : (tige, feuilles, fruit vert, fruit mur [sombre, moyen, clair], reflet)
    "ember_fig": ("#4a1a0c", ["#7c2414", "#9a3418"], ["#6a5a1a", "#8a7a2a"], ["#a8300e", "#e0601a", "#ff9a2a"], "#ffe27a"),
    "withered_plum": ("#2a2830", ["#3a3442", "#4a4254"], ["#4a5a3a", "#5a6a46"], ["#2a1240", "#4a2470", "#7a46b0"], "#c8a8ff"),
}


def fruit_blob(shape, cx, cy, radius, colors, glint):
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = (dx * dx + dy * dy * 0.8) ** 0.5
            if d <= radius:
                k = 2 if d < radius * 0.45 else (1 if d < radius * 0.8 else 0)
                shape.put(x, y, hexc(colors[min(k, len(colors) - 1)]))
    shape.put(int(cx - radius * 0.4), int(cy - radius * 0.4), hexc(glint))


def fruit_cluster(name, stage):
    stem, leaves, unripe, ripe, glint = FRUIT_STYLES[name]
    r = rng(f"{name}_cluster{stage}")
    shape = Shape()
    # Tige qui descend du houppier et petites feuilles
    for y in range(0, 6):
        shape.put(8, y, hexc(stem))
    for (x, y) in ((6, 1), (7, 2), (9, 2), (10, 1), (5, 2), (11, 2)):
        shape.put(x, y, hexc(r.choice(leaves)))
    spots = [(8, 9), (5.5, 7.5), (10.5, 7.5), (7, 12), (9.5, 11.5)]
    count = [3, 4, 5][stage]
    radius = [1.2, 1.8, 2.3][stage]
    colors = unripe if stage == 0 else (unripe[1:] + ripe[:1] if stage == 1 else ripe)
    for (cx, cy) in spots[:count]:
        shape.line(8, 5, int(cx), int(cy - radius), hexc(stem))
    for (cx, cy) in spots[:count]:
        fruit_blob(shape, cx, cy, radius, colors, glint if stage == 2 else colors[-1])
    return shape.render(outline=hexc("#140806") if stage else None)


def fruit_item(name):
    stem, leaves, _, ripe, glint = FRUIT_STYLES[name]
    shape = Shape()
    if name == "ember_fig":
        # Figue : petit col, panse ronde striee, oeil sombre en dessous
        fruit_blob(shape, 8, 10.8, 4.4, ripe, glint)
        for y in range(4, 7):
            for x in (7, 8):
                shape.put(x, y, hexc(ripe[1]))
        for y in range(8, 14):
            for x in (6, 10):
                shape.put(x, y, hexc(ripe[0]))
        shape.put(8, 14, hexc("#5a1206"))
        shape.put(7, 8, hexc(glint))
    else:
        fruit_blob(shape, 8, 9.5, 5.2, ripe, glint)
        shape.put(6, 7, hexc(glint))
    for y in range(1, 4):
        shape.put(8, y, hexc(stem))
    shape.put(9, 2, hexc(leaves[1]))
    shape.put(10, 1, hexc(leaves[0]))
    return shape.render(outline=hexc("#140806"))


def generate_fruits():
    for name in FRUIT_STYLES:
        for stage in range(3):
            save(fruit_cluster(name, stage), "block", f"{name}_cluster_stage{stage}.png")
        save(fruit_item(name), "item", f"{name}.png")
