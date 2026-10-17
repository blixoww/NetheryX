"""Textures du contenu 1.2.0 (biomes, bois, ateliers, objets, armures)."""
from PIL import Image

from .pixels import Shape, bevel, hexc, mix, new, noise_fill, rng, shade, shaded_mask
from .textures import (OUTLINE, armor_icon, armor_layer, bark, bricks, char_art, crystals, door_half, door_item, dust,
                       grain_wood, grass_tufts, lantern_item, lantern_texture, netherrack, ore, planks_from, rings_top, save,
                       shard, trapdoor)

BONE = ["#bdb4a0", "#c9c0aa", "#d4ccb6", "#ded6c2"]
BONE_PLANKS = ["#d8ccb0", "#e2d6bc", "#cfc2a4", "#e8dec6"]
WART_BARK = ["#4a1418", "#5a1a1e", "#6a2024", "#7a262a"]
WART_PLANKS = ["#7a2e26", "#86342a", "#923a2e", "#9e4034"]
RIME = ["#5a6a80", "#66788f", "#73879f", "#8296ad"]


def soil_top(palette, seed, specks):
    img = noise_fill(new(), palette, seed, blur=1, jitter=0.35)
    r = rng(seed + "s")
    for _ in range(6):
        img.putpixel((r.randrange(16), r.randrange(16)), hexc(r.choice(specks)))
    return img


def soil_side(top, seed):
    side = netherrack(seed)
    r = rng(seed + "fringe")
    for x in range(16):
        for y in range(r.choice((2, 3, 3, 4))):
            side.putpixel((x, y), top.getpixel((x, y)))
    return side


def vine(palette, seed, tip=None, fruit=None, head=False):
    """Liane pendante : quelques brins qui ondulent, extremites lumineuses et fruits eventuels."""
    shape = Shape()
    r = rng(seed)
    for x0 in (3, 6, 9, 12):
        x = x0 + r.randint(-1, 1)
        length = r.randint(10, 16) if head else 16
        for y in range(length):
            shape.put(x, y, hexc(palette[(y + x0) % len(palette)]))
            if r.random() < 0.18:
                x = max(1, min(14, x + r.choice((-1, 1))))
        if tip and head:
            shape.put(x, length - 1, hexc(tip))
        if fruit:
            fy = r.randint(4, 12)
            for (dx, dy) in ((0, 0), (1, 0), (0, 1), (1, 1)):
                shape.put(x + dx - 1, fy + dy, hexc(fruit[0]) if (dx, dy) != (0, 0) else hexc(fruit[1]))
    return shape.render()


def porous(palette, pore_hex, seed):
    img = noise_fill(new(), palette, seed, blur=1, jitter=0.25)
    r = rng(seed + "pores")
    for _ in range(9):
        x, y = r.randrange(15), r.randrange(15)
        img.putpixel((x, y), hexc(pore_hex))
        img.putpixel((x + 1, y), shade(hexc(pore_hex), 1.2))
    return img


def wood_set(prefix, bark_palette, crack, streak, stripped, planks_palette, seam, frame):
    save(bark(bark_palette, crack, streak, prefix + "_bark"), "block", f"{prefix}_stem.png")
    save(rings_top(bark_palette[0], stripped, stripped[-1]), "block", f"{prefix}_stem_top.png")
    line = "#" + "".join(f"{c:02x}" for c in shade(hexc(stripped[0]), 0.75)[:3])
    save(grain_wood(stripped, line, f"stripped_{prefix}"), "block", f"stripped_{prefix}_stem.png")
    save(rings_top(stripped[0], stripped[1:], stripped[-1]), "block", f"stripped_{prefix}_stem_top.png")
    save(planks_from(planks_palette, seam, prefix + "_planks"), "block", f"{prefix}_planks.png")
    save(door_half(True, planks_palette, frame, prefix), "block", f"{prefix}_door_top.png")
    save(door_half(False, planks_palette, frame, prefix), "block", f"{prefix}_door_bottom.png")
    save(trapdoor(planks_palette, frame, prefix), "block", f"{prefix}_trapdoor.png")
    save(door_item(planks_palette, frame, prefix), "item", f"{prefix}_door.png")


def translucent(palette, seed, alpha, crack_hex):
    img = noise_fill(new(), palette, seed, blur=1, jitter=0.2)
    r = rng(seed + "c")
    for y in range(16):
        for x in range(16):
            c = img.getpixel((x, y))
            img.putpixel((x, y), (c[0], c[1], c[2], alpha))
    x = r.randrange(16)
    for y in range(16):
        img.putpixel((x % 16, y), hexc(crack_hex, 230))
        x += r.choice((-1, 0, 1))
    return img


def glass_frame(frame_hex, fill_hex, alpha):
    img = new()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                img.putpixel((x, y), hexc(frame_hex))
            elif (x + y) % 7 == 0 and 3 < x < 12:
                img.putpixel((x, y), hexc("#ffffff", 150))
            else:
                img.putpixel((x, y), hexc(fill_hex, alpha))
    return img


def cage():
    img = new()
    for y in range(16):
        for x in range(16):
            frame = x in (0, 15) or y in (0, 15)
            bar = x in (5, 10) or y in (5, 10)
            if frame:
                img.putpixel((x, y), hexc("#2a2226" if (x + y) % 2 else "#3a3034"))
            elif bar:
                img.putpixel((x, y), hexc("#4a3a36" if (x * y) % 3 else "#b8601a"))
    return img


def runed(palette, seed, rune_hex, runes):
    img = bevel(noise_fill(new(), palette, seed, blur=1, jitter=0.15), 1.3, 0.6)
    for (x, y) in runes:
        img.putpixel((x, y), hexc(rune_hex))
    return img


RUNES = ((4, 4), (5, 5), (4, 6), (11, 4), (10, 5), (11, 6), (7, 10), (8, 10), (7, 11), (8, 11), (6, 12), (9, 12))


def food_round(base, highlight, seed, radius=4.5):
    shape = Shape()
    shape.disc(8, 9, radius, hexc(base))
    shape.put(6, 6, hexc(highlight))
    shape.put(7, 6, hexc(highlight))
    shape.line(8, 3, 9, 2, hexc("#4a2a12"))
    return shape.render(outline=OUTLINE)


def fish(body, fin, eye="#ffffff"):
    rows = ["", "", "", "", "..............#.",
            "....######...##.", "..##########.##.", ".##+#########-#..", ".###########---..",
            "..##########.##.", "....######...##.", "..............#."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc(body), OUTLINE)
    for (x, y) in ((6, 6), (8, 6), (10, 7), (7, 9), (9, 9)):
        img.putpixel((x, y), hexc(fin))
    img.putpixel((3, 7), hexc(eye))
    return img


def tart():
    rows = ["", "", "", "", "", "", "...##########...", "..#++#########..", ".##############.",
            ".##############.", "..############..", "...----------..."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#c8904a"), OUTLINE)
    for x in range(3, 13):
        img.putpixel((x, 7), hexc("#e83a2a") if x % 2 else hexc("#ff7a3a"))
        img.putpixel((x, 8), hexc("#b82a1a") if x % 3 else hexc("#ffb060"))
    return img


def template_plate():
    rows = ["", "...##########...", "...#++++++++#...", "...#+######-#...", "...#+#....#-#...", "...#+#.##.#-#...",
            "...#+#.##.#-#...", "...#+#....#-#...", "...#+######-#...", "...#--------#...", "...##########..."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#2c2040"), OUTLINE)
    for (x, y) in ((7, 5), (8, 5), (7, 6), (8, 6)):
        img.putpixel((x, y), hexc("#b06cff"))
    return img


def fang():
    shape = Shape()
    for k, (x, y) in enumerate(((5, 2), (6, 3), (6, 4), (7, 5), (7, 6), (8, 7), (8, 8), (9, 9), (9, 10), (10, 11), (10, 12), (11, 13))):
        width = 3 if k < 4 else 2 if k < 8 else 1
        for w in range(width):
            shape.put(x + w, y, shade(hexc("#e8dcc0"), 1.2 - k * 0.04))
    shape.put(5, 2, hexc("#8a7a5a"))
    return shape.render(outline=OUTLINE)


def cloth():
    rows = ["", "", "", "...##########...", "..#++++++++##-..", "..#+########--..", "..##########--..",
            "..##-####-###-..", "..#-#-##-#-##-..", "...-..-...-.-..."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#8a8892"), OUTLINE)
    return img


def scale():
    shape = Shape()
    shape.ellipse(8, 8, 4.5, 5.5, hexc("#ff7a1a"))
    for y in range(5, 12, 2):
        shape.line(5, y, 11, y, hexc("#c84a0a"))
    shape.put(6, 5, hexc("#ffe27a"))
    return shape.render(outline=OUTLINE)


def cauldron_item():
    rows = ["", "", "", "..############..", "..#----------#..", "..#++########..", "..#+#########-..",
            "..#+#########-..", "..#+#########-..", "...#########-...", "...##.....##....", "...-#.....#-...."]
    img = shaded_mask([r.ljust(16, ".") for r in rows], hexc("#2a2226"), OUTLINE)
    for x in range(4, 12):
        img.putpixel((x, 4), hexc("#c050e0"))
    for (x, y) in ((6, 7), (9, 8), (7, 9)):
        img.putpixel((x, y), hexc("#ff7a1a"))
    return img


def generate():
    # --- Fourre de moelle ---
    top = soil_top(["#cfc6b0", "#ddd4be", "#e8e0cc", "#f2ecdc"], "ossified", ["#8a8270", "#fff8e8"])
    save(top, "block", "ossified_nylium.png")
    save(soil_side(top, "ossified_side"), "block", "ossified_nylium_side.png")
    save(porous(["#e6dcc4", "#eee6d0", "#f4eedc", "#fbf6ea"], "#b8ad94", "marrow_cap"), "block", "marrow_cap.png")
    save(vine(["#e8dcc0", "#f4ecd8", "#d8ccb0"], "marrow_vines", tip="#fff2a8", head=True), "block", "marrow_vines.png")
    save(vine(["#e8dcc0", "#f4ecd8", "#d8ccb0"], "marrow_vines_plant"), "block", "marrow_vines_plant.png")
    save(grass_tufts(["#b8ad94", "#d8ccb0", "#eee6d0", "#fff8e8"], "marrow_sprouts"), "block", "marrow_sprouts.png")
    wood_set("marrow", BONE, "#8a8270", "#f6f0e0", ["#e6d8b8", "#eee2c4", "#f4ead0", "#faf2de"], BONE_PLANKS, "#9a8e74", "#8a7e64")

    # --- Gouffre de givre ---
    save(noise_fill(new(), RIME, "rimestone", blur=1, jitter=0.25), "block", "rimestone.png")
    save(bricks(RIME, "#3a4658", "rimestone_bricks"), "block", "rimestone_bricks.png")
    save(translucent(["#a8d8f0", "#b8e2f6", "#c8eafa", "#d8f2ff"], "rime_ice", 210, "#ffffff"), "block", "rime_ice.png")
    save(crystals("#3a8ac0", "#7fd0f8", "#d8f4ff", "rime_crystal"), "block", "rime_crystal.png")

    # --- Enchevetrement de verrues ---
    top = soil_top(["#5a0e10", "#6e1416", "#821a1c", "#962224"], "wart_moss", ["#ff5a3a", "#ffb060"])
    save(top, "block", "wart_moss.png")
    save(soil_side(top, "wart_moss_side"), "block", "wart_moss_side.png")
    save(bevel(noise_fill(new(), ["#ff6a3a", "#ff8a4a", "#ffa860", "#ffc880"], "wart_bulb", blur=1, jitter=0.3), 1.15, 0.65),
         "block", "wart_bulb.png")
    red = ["#7a1418", "#962024", "#b02a2c"]
    save(vine(red, "wartvine", tip="#ff7a3a", head=True), "block", "wartvine.png")
    save(vine(red, "wartvine", tip="#ff7a3a", fruit=("#ff8a3a", "#ffe08a"), head=True), "block", "wartvine_lit.png")
    save(vine(red, "wartvine_plant"), "block", "wartvine_plant.png")
    save(vine(red, "wartvine_plant", fruit=("#ff8a3a", "#ffe08a")), "block", "wartvine_plant_lit.png")
    wood_set("wartstalk", WART_BARK, "#2a0a0c", "#ff5a3a", ["#8a3a2e", "#9a4434", "#a84e3a", "#b65a44"], WART_PLANKS, "#4a1814", "#3a1010")

    # --- Recifs de quartz ---
    save(ore("rich_quartz", "#c8beb4", "#e8e0d8", "#ffffff", "#ffffff",
             base=noise_fill(new(), ["#1e1a20", "#262126", "#2e282e", "#363036"], "rich_quartz_base", blur=1, jitter=0.25), clusters=4),
         "block", "rich_quartz_ore.png")
    save(glass_frame("#e8e2da", "#f4f0ea", 70), "block", "quartz_glass.png")
    edge = new()
    for y in range(16):
        for x in (7, 8):
            edge.putpixel((x, y), hexc("#e8e2da"))
    save(edge, "block", "quartz_glass_pane_top.png")
    lamp = noise_fill(new(), ["#fff6e0", "#fffaf0", "#ffffff"], "quartz_lamp", blur=1, jitter=0.2)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15) or x in (7, 8) or y in (7, 8):
                lamp.putpixel((x, y), hexc("#d8d0c4"))
    save(lamp, "block", "quartz_lamp.png")

    # --- Ateliers et rituels ---
    save(cage(), "block", "lava_trap.png")
    dark = ["#1e181c", "#262024", "#2e262a"]
    save(runed(dark, "cauldron_side", "#ff7a1a", RUNES), "block", "infernal_cauldron_side.png")
    save(runed(dark, "cauldron_top", "#ff7a1a", ()), "block", "infernal_cauldron_top.png")
    save(runed(dark, "cauldron_bottom", "#7a1e06", ()), "block", "infernal_cauldron_bottom.png")
    save(noise_fill(new(), ["#141012", "#1a1416", "#201a1c"], "cauldron_inner", blur=1, jitter=0.1), "block", "infernal_cauldron_inner.png")
    save(cauldron_item(), "item", "infernal_cauldron.png")
    stone = ["#16141c", "#1c1a24", "#22202c"]
    save(runed(stone, "altar_side", "#7fe8ff", RUNES), "block", "soul_altar_side.png")
    altar_top = runed(stone, "altar_top", "#7fe8ff", ())
    for k in range(3, 13):
        altar_top.putpixel((k, 3), hexc("#7fe8ff"))
        altar_top.putpixel((k, 12), hexc("#7fe8ff"))
        altar_top.putpixel((3, k), hexc("#7fe8ff"))
        altar_top.putpixel((12, k), hexc("#7fe8ff"))
    save(altar_top, "block", "soul_altar_top.png")
    save(runed(stone, "altar_bottom", "#3fb3c0", ()), "block", "soul_altar_bottom.png")
    save(lantern_texture("#24343c", "#c4f8ff"), "block", "ward_lantern.png")
    save(lantern_item("#24343c", "#c4f8ff"), "item", "ward_lantern.png")

    # --- Objets ---
    save(dust(["#f4eedc", "#e6dcc4", "#fff8e8", "#d8ccb0"], "marrow_meal"), "item", "marrow_meal.png")
    save(shard("#7fd0f8", "#ffffff"), "item", "rime_shard.png")
    save(food_round("#e8402a", "#ffd08a", "wartfruit"), "item", "wartfruit.png")
    save(tart(), "item", "wartfruit_tart.png")
    save(fish("#ff6a1a", "#2a0a06", "#ffe27a"), "item", "magmafish.png")
    save(fish("#8a3a12", "#3a1408", "#2a1a10"), "item", "grilled_magmafish.png")
    save(scale(), "item", "magmafish_scale.png")
    save(cloth(), "item", "wraith_cloth.png")
    save(template_plate(), "item", "onyx_infusion_template.png")
    save(fang(), "item", "burrower_fang.png")
    for piece, (ember, wraith, carapace) in {"helmet": ("ember_helmet", "wraith_hood", "carapace_helmet"),
                                              "chestplate": ("ember_chestplate", "wraith_robe", None),
                                              "leggings": ("ember_leggings", "wraith_leggings", "carapace_leggings"),
                                              "boots": ("ember_boots", "wraith_boots", "carapace_boots")}.items():
        icon = armor_icon(piece, "#c8501a")
        for (x, y) in ((6, 5), (9, 6), (7, 8)):
            if icon.getpixel((x, y))[3] > 0:
                icon.putpixel((x, y), hexc("#ffd36b"))
        save(icon, "item", f"{ember}.png")
        save(armor_icon(piece, "#7a7884"), "item", f"{wraith}.png")
        if carapace:
            icon = armor_icon(piece, "#3a3a40")
            for (x, y) in ((6, 5), (9, 6)):
                if icon.getpixel((x, y))[3] > 0:
                    icon.putpixel((x, y), hexc("#ff6a1a"))
            save(icon, "item", f"{carapace}.png")
    save(armor_layer("#a83c12", "#ffd36b", 1, "ember1"), "models", "armor", "ember_layer_1.png")
    save(armor_layer("#a83c12", "#ffd36b", 2, "ember2"), "models", "armor", "ember_layer_2.png")
    save(armor_layer("#5a5862", "#c4f8ff", 1, "wraith1"), "models", "armor", "wraith_layer_1.png")
    save(armor_layer("#5a5862", "#c4f8ff", 2, "wraith2"), "models", "armor", "wraith_layer_2.png")
