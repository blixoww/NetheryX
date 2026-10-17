"""Blockstates, modeles de blocs et d'objets, fichiers de langue."""
import re

from .common import MOD_ID, asset_path, mid, write_json
from .content import (BIOMES, BLOCKS, BLOCKS_WITHOUT_ITEM, EXTRA_LANG, GENERATED_ITEMS, HANDHELD_ITEMS, NAMES,
                      SPAWN_EGGS)

CUTOUT = "minecraft:cutout"


def block_tex(name):
    return f"{MOD_ID}:block/{name}"


def model(name, parent, textures=None, render_type=None):
    data = {"parent": parent}
    if textures:
        data["textures"] = textures
    if render_type:
        data["render_type"] = render_type
    write_json(asset_path("models", "block", f"{name}.json"), data)
    return f"{MOD_ID}:block/{name}"


def blockstate(name, variants=None, multipart=None):
    data = {"variants": variants} if variants is not None else {"multipart": multipart}
    write_json(asset_path("blockstates", f"{name}.json"), data)


def item_model(name, parent=None, layer0=None):
    data = {"parent": parent or "minecraft:item/generated"}
    if layer0:
        data["textures"] = {"layer0": layer0}
    write_json(asset_path("models", "item", f"{name}.json"), data)


def v(model_id, x=0, y=0, uvlock=False):
    entry = {"model": model_id}
    if x:
        entry["x"] = x
    if y:
        entry["y"] = y
    if uvlock:
        entry["uvlock"] = True
    return entry


FACING_Y = {"north": 0, "east": 90, "south": 180, "west": 270}


def gen_cube(name, texture=None):
    m = model(name, "minecraft:block/cube_all", {"all": block_tex(texture or name)})
    blockstate(name, {"": v(m)})
    return m


def gen_stairs(name, tex):
    t = {"bottom": block_tex(tex), "top": block_tex(tex), "side": block_tex(tex)}
    straight = model(name, "minecraft:block/stairs", t)
    inner = model(f"{name}_inner", "minecraft:block/inner_stairs", t)
    outer = model(f"{name}_outer", "minecraft:block/outer_stairs", t)
    variants = {}
    base_y = {"east": 0, "south": 90, "west": 180, "north": 270}
    for facing, fy in base_y.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                m = straight if shape == "straight" else (inner if shape.startswith("inner") else outer)
                y = fy
                if shape in ("inner_left", "outer_left"):
                    y = (fy + 270) % 360
                x = 0
                if half == "top":
                    x = 180
                    if shape in ("inner_left", "outer_left"):
                        y = fy
                    elif shape in ("inner_right", "outer_right"):
                        y = (fy + 90) % 360
                variants[f"facing={facing},half={half},shape={shape}"] = v(m, x=x, y=y, uvlock=(x or y) != 0)
    blockstate(name, variants)
    return straight


def gen_slab(name, tex):
    t = {"bottom": block_tex(tex), "top": block_tex(tex), "side": block_tex(tex)}
    bottom = model(name, "minecraft:block/slab", t)
    top = model(f"{name}_top", "minecraft:block/slab_top", t)
    blockstate(name, {"type=bottom": v(bottom), "type=top": v(top), "type=double": v(f"{MOD_ID}:block/{tex}")})
    return bottom


def gen_wall(name, tex):
    t = {"wall": block_tex(tex)}
    post = model(f"{name}_post", "minecraft:block/template_wall_post", t)
    side = model(f"{name}_side", "minecraft:block/template_wall_side", t)
    tall = model(f"{name}_side_tall", "minecraft:block/template_wall_side_tall", t)
    model(f"{name}_inventory", "minecraft:block/wall_inventory", t)
    parts = [{"when": {"up": "true"}, "apply": v(post)}]
    for facing, y in FACING_Y.items():
        parts.append({"when": {facing: "low"}, "apply": v(side, y=y, uvlock=True)})
        parts.append({"when": {facing: "tall"}, "apply": v(tall, y=y, uvlock=True)})
    blockstate(name, multipart=parts)
    return f"{MOD_ID}:block/{name}_inventory"


def gen_fence(name, tex):
    t = {"texture": block_tex(tex)}
    post = model(f"{name}_post", "minecraft:block/fence_post", t)
    side = model(f"{name}_side", "minecraft:block/fence_side", t)
    model(f"{name}_inventory", "minecraft:block/fence_inventory", t)
    parts = [{"apply": v(post)}]
    for facing, y in FACING_Y.items():
        parts.append({"when": {facing: "true"}, "apply": v(side, y=y, uvlock=True)})
    blockstate(name, multipart=parts)
    return f"{MOD_ID}:block/{name}_inventory"


def gen_fence_gate(name, tex):
    t = {"texture": block_tex(tex)}
    closed = model(name, "minecraft:block/template_fence_gate", t)
    opened = model(f"{name}_open", "minecraft:block/template_fence_gate_open", t)
    wall = model(f"{name}_wall", "minecraft:block/template_fence_gate_wall", t)
    wall_open = model(f"{name}_wall_open", "minecraft:block/template_fence_gate_wall_open", t)
    gate_y = {"south": 0, "west": 90, "north": 180, "east": 270}
    variants = {}
    for facing, y in gate_y.items():
        for in_wall in ("false", "true"):
            for open_ in ("false", "true"):
                if in_wall == "true":
                    m = wall_open if open_ == "true" else wall
                else:
                    m = opened if open_ == "true" else closed
                variants[f"facing={facing},in_wall={in_wall},open={open_}"] = v(m, y=y, uvlock=True)
    blockstate(name, variants)
    return closed


def gen_door(name):
    t = {"bottom": block_tex(f"{name}_bottom"), "top": block_tex(f"{name}_top")}
    models = {}
    for half in ("bottom", "top"):
        for hinge in ("left", "right"):
            for open_ in (False, True):
                suffix = f"{half}_{hinge}" + ("_open" if open_ else "")
                models[suffix] = model(f"{name}_{suffix}", f"minecraft:block/door_{suffix}", t, CUTOUT)
    base_y = {"east": 0, "south": 90, "west": 180, "north": 270}
    variants = {}
    for facing, fy in base_y.items():
        for half, mhalf in (("lower", "bottom"), ("upper", "top")):
            for hinge in ("left", "right"):
                for open_ in (False, True):
                    y = fy
                    if open_:
                        y = (fy + (90 if hinge == "left" else 270)) % 360
                    m = models[f"{mhalf}_{hinge}" + ("_open" if open_ else "")]
                    key = f"facing={facing},half={half},hinge={hinge},open={str(open_).lower()}"
                    variants[key] = v(m, y=y)
    blockstate(name, variants)
    item_model(name, layer0=f"{MOD_ID}:item/{name}")
    return None


def gen_trapdoor(name):
    t = {"texture": block_tex(name)}
    bottom = model(f"{name}_bottom", "minecraft:block/template_orientable_trapdoor_bottom", t, CUTOUT)
    top = model(f"{name}_top", "minecraft:block/template_orientable_trapdoor_top", t, CUTOUT)
    opened = model(f"{name}_open", "minecraft:block/template_orientable_trapdoor_open", t, CUTOUT)
    base = {"north": 0, "east": 90, "south": 180, "west": 270}
    top_open_y = {"north": 180, "east": 270, "south": 0, "west": 90}
    variants = {}
    for facing, y in base.items():
        variants[f"facing={facing},half=bottom,open=false"] = v(bottom, y=y)
        variants[f"facing={facing},half=bottom,open=true"] = v(opened, y=y)
        variants[f"facing={facing},half=top,open=false"] = v(top, y=y)
        variants[f"facing={facing},half=top,open=true"] = v(opened, x=180, y=top_open_y[facing])
    blockstate(name, variants)
    return bottom


def gen_button(name, tex):
    t = {"texture": block_tex(tex)}
    up = model(name, "minecraft:block/button", t)
    pressed = model(f"{name}_pressed", "minecraft:block/button_pressed", t)
    model(f"{name}_inventory", "minecraft:block/button_inventory", t)
    floor_y = {"north": 0, "east": 90, "south": 180, "west": 270}
    ceiling_y = {"north": 180, "east": 270, "south": 0, "west": 90}
    variants = {}
    for powered in ("false", "true"):
        m = pressed if powered == "true" else up
        for facing in floor_y:
            variants[f"face=floor,facing={facing},powered={powered}"] = v(m, y=floor_y[facing])
            variants[f"face=ceiling,facing={facing},powered={powered}"] = v(m, x=180, y=ceiling_y[facing])
            variants[f"face=wall,facing={facing},powered={powered}"] = v(m, x=90, y=floor_y[facing], uvlock=True)
    blockstate(name, variants)
    return f"{MOD_ID}:block/{name}_inventory"


def gen_pressure_plate(name, tex):
    t = {"texture": block_tex(tex)}
    up = model(name, "minecraft:block/pressure_plate_up", t)
    down = model(f"{name}_down", "minecraft:block/pressure_plate_down", t)
    blockstate(name, {"powered=false": v(up), "powered=true": v(down)})
    return up


def gen_log(name, tex, wood=False):
    end = block_tex(tex) if wood else block_tex(f"{tex}_top")
    t = {"end": end, "side": block_tex(tex)}
    vertical = model(name, "minecraft:block/cube_column", t)
    horizontal = model(f"{name}_horizontal", "minecraft:block/cube_column_horizontal", t)
    blockstate(name, {"axis=y": v(vertical), "axis=z": v(horizontal, x=90), "axis=x": v(horizontal, x=90, y=90)})
    return vertical


def gen_nylium(name):
    m = model(name, "minecraft:block/cube_bottom_top", {
        "bottom": "minecraft:block/netherrack", "top": block_tex(name), "side": block_tex(f"{name}_side")})
    blockstate(name, {"": v(m)})
    return m


def gen_cross(name):
    m = model(name, "minecraft:block/cross", {"cross": block_tex(name)}, CUTOUT)
    blockstate(name, {"": v(m)})
    item_model(name, layer0=block_tex(name))
    return None


def gen_cluster(name):
    m = model(name, "minecraft:block/cross", {"cross": block_tex(name)}, CUTOUT)
    rotations = {"up": (0, 0), "down": (180, 0), "north": (90, 0), "south": (90, 180), "east": (90, 90), "west": (90, 270)}
    blockstate(name, {f"facing={f}": v(m, x=x, y=y) for f, (x, y) in rotations.items()})
    item_model(name, layer0=block_tex(name))
    return None


def gen_lantern(name):
    t = {"lantern": block_tex(name)}
    standing = model(name, "minecraft:block/template_lantern", t, CUTOUT)
    hanging = model(f"{name}_hanging", "minecraft:block/template_hanging_lantern", t, CUTOUT)
    blockstate(name, {"hanging=false": v(standing), "hanging=true": v(hanging)})
    item_model(name, layer0=f"{MOD_ID}:item/{name}")


def gen_grass_block(name):
    m = model(name, "minecraft:block/cube_bottom_top", {
        "bottom": "minecraft:block/netherrack", "top": block_tex(name), "side": block_tex(f"{name}_side")})
    blockstate(name, {"": v(m)})
    return m


def gen_pot(name, plant):
    m = model(name, "minecraft:block/flower_pot_cross", {"plant": block_tex(plant)}, CUTOUT)
    blockstate(name, {"": v(m)})


def gen_bow(name):
    """Arc avec les 3 etapes de tension (predicats 'pulling' et 'pull' enregistres cote client)."""
    overrides = []
    for i, pull in enumerate((None, 0.65, 0.9)):
        predicate = {"pulling": 1}
        if pull:
            predicate["pull"] = pull
        overrides.append({"predicate": predicate, "model": f"{MOD_ID}:item/{name}_pulling_{i}"})
        write_json(asset_path("models", "item", f"{name}_pulling_{i}.json"),
                   {"parent": f"{MOD_ID}:item/{name}", "textures": {"layer0": f"{MOD_ID}:item/{name}_pulling_{i}"}})
    write_json(asset_path("models", "item", f"{name}.json"), {
        "parent": "minecraft:item/bow", "textures": {"layer0": f"{MOD_ID}:item/{name}"}, "overrides": overrides})


def gen_anvil(name):
    m = model(name, "minecraft:block/template_anvil",
              {"particle": block_tex(name), "body": block_tex(name), "top": block_tex(f"{name}_top")})
    blockstate(name, {"facing=south": v(m), "facing=west": v(m, y=90), "facing=north": v(m, y=180), "facing=east": v(m, y=270)})
    return m


def gen_furnace(name):
    t = {"top": block_tex(f"{name}_top"), "side": block_tex(f"{name}_side")}
    off = model(name, "minecraft:block/orientable", dict(t, front=block_tex(f"{name}_front")))
    on = model(f"{name}_on", "minecraft:block/orientable", dict(t, front=block_tex(f"{name}_front_on")))
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        variants[f"facing={facing},lit=false"] = v(off, y=y)
        variants[f"facing={facing},lit=true"] = v(on, y=y)
    blockstate(name, variants)
    return off


def gen_aged(name, parent, texture_prefix, key="crop"):
    variants = {}
    for age in range(4):
        tex = f"{texture_prefix}{age}"
        m = model(f"{name}_stage{age}", parent, {key: block_tex(tex)}, CUTOUT)
        variants[f"age={age}"] = v(m)
    blockstate(name, variants)


def generate_blocks():
    for name, kind, extra in BLOCKS:
        item_parent = None
        if kind == "cube":
            item_parent = gen_cube(name)
        elif kind == "aged_cube":
            m = model(name, "minecraft:block/cube_all", {"all": block_tex(name)})
            blockstate(name, {f"age={a}": v(m) for a in range(4)})
        elif kind == "stairs":
            item_parent = gen_stairs(name, extra)
        elif kind == "slab":
            item_parent = gen_slab(name, extra)
        elif kind == "wall":
            item_parent = gen_wall(name, extra)
        elif kind == "fence":
            item_parent = gen_fence(name, extra)
        elif kind == "fence_gate":
            item_parent = gen_fence_gate(name, extra)
        elif kind == "door":
            gen_door(name)
        elif kind == "trapdoor":
            item_parent = gen_trapdoor(name)
        elif kind == "button":
            item_parent = gen_button(name, extra)
        elif kind == "pressure_plate":
            item_parent = gen_pressure_plate(name, extra)
        elif kind == "log":
            item_parent = gen_log(name, extra)
        elif kind == "wood":
            item_parent = gen_log(name, extra, wood=True)
        elif kind == "nylium":
            item_parent = gen_nylium(name)
        elif kind == "cross":
            gen_cross(name)
        elif kind == "cluster":
            gen_cluster(name)
        elif kind == "anvil":
            item_parent = gen_anvil(name)
        elif kind == "furnace":
            item_parent = gen_furnace(name)
        elif kind == "grass_block":
            item_parent = gen_grass_block(name)
        elif kind == "pot":
            gen_pot(name, extra)
        elif kind == "lantern":
            gen_lantern(name)
        elif kind == "crop":
            gen_aged(name, "minecraft:block/crop", extra, "crop")
        elif kind == "bush":
            gen_aged(name, "minecraft:block/cross", extra, "cross")
        else:
            raise ValueError(kind)
        if item_parent and name not in BLOCKS_WITHOUT_ITEM:
            item_model(name, parent=item_parent)


def generate_items():
    for name in HANDHELD_ITEMS:
        item_model(name, parent="minecraft:item/handheld", layer0=f"{MOD_ID}:item/{name}")
    for name in GENERATED_ITEMS:
        item_model(name, layer0=f"{MOD_ID}:item/{name}")
    gen_bow("onyx_bow")
    for name in SPAWN_EGGS:
        item_model(name, parent="minecraft:item/template_spawn_egg")


FR_ACCENTS = [
    (r"\bEclat\b", "Éclat"), (r"\bEpee\b", "Épée"), (r"\becorcees\b", "écorcées"), (r"\becorcee\b", "écorcée"),
    (r"\bBarriere\b", "Barrière"), (r"\bJambieres\b", "Jambières"), (r"\bRagout\b", "Ragoût"),
    (r"\bOeuf\b", "Œuf"), (r"\bcendrees\b", "cendrées"), (r"\bResistance\b", "Résistance"),
    (r"\bresistance\b", "résistance"), (r"\bCoeur\b", "Cœur"), (r"\bcroutee\b", "croûtée"),
    (r"\bcalcine\b", "calciné"), (r"\bame\b", "âme"), (r"\bames\b", "âmes"), (r"\bVetu\b", "Vêtu"),
    (r"\bGuide par\b", "Guidé par"), (r"\bresonne\b", "résonne"), (r"\bconfere\b", "confère"),
    (r"\bqu'il est porte\b", "qu'il est porté"), (r"\bCa pique\b", "Ça pique"), (r"\ba (\%s)", r"à \1"),
    (r"\bmeme\b", "même"), (r"\bBle\b", "Blé"), (r"\bble\b", "blé"), (r"\bRagout\b", "Ragoût"),
    (r"\bFleches\b", "Flèches"), (r"\benflammees\b", "enflammées"), (r"\bsecrete\b", "secrète"), (r"\bCite\b", "Cité"),
    (r"\bOeuf\b", "Œuf"), (r"\bBoeuf\b", "Bœuf"), (r"\best tombe\b", "est tombé"), (r"\btresor\b", "trésor"), (r"\bfletri\b", "flétri"), (r"\bfletrie\b", "flétrie"), (r"\bfletries\b", "flétries"),
    (r"\bFletrissement\b", "Flétrissement"), (r"\bFletri\b", "Flétri"), (r"\bFletrie\b", "Flétrie"), (r"\bForet\b", "Forêt"),
    (r"\bPoussiere\b", "Poussière"), (r"\bPhalene\b", "Phalène"), (r"\bphalene\b", "phalène"), (r"\bScarabee\b", "Scarabée"),
    (r"\bscarabee\b", "scarabée"), (r"\bBaton\b", "Bâton"), (r"\bElixir\b", "Élixir"), (r"\breapparition\b", "réapparition"),
    (r"\ba votre\b", "à votre"), (r"\bdecrit\b", "décrit"), (r"\bmateriaux\b", "matériaux"), (r"\bcreatures\b", "créatures"),
    (r"\bChoisissez une categorie\b", "Choisissez une catégorie"), (r"\bcategorie\b", "catégorie"), (r"\bcreature\b", "créature"),
    (r"\bBriseur de citadelle\b", "Briseur de citadelle"), (r"\bcitadelle\b", "citadelle"), (r"\bfletris\b", "flétris"),
]


def french(text):
    for pattern, repl in FR_ACCENTS:
        text = re.sub(pattern, repl, text)
    return text


def generate_lang():
    en, fr = {}, {}
    block_names = {b[0] for b in BLOCKS}
    for key, (en_name, fr_name) in NAMES.items():
        if key.startswith("entity."):
            lang_key = f"entity.{MOD_ID}.{key[7:]}"
        elif key.startswith("biome."):
            lang_key = f"biome.{MOD_ID}.{key[6:]}"
        elif key in block_names:
            lang_key = f"block.{MOD_ID}.{key}"
        else:
            lang_key = f"item.{MOD_ID}.{key}"
        en[lang_key] = en_name
        fr[lang_key] = french(fr_name)
    for key, (en_text, fr_text) in EXTRA_LANG.items():
        en[key] = en_text
        fr[key] = french(fr_text)
    write_json(asset_path("lang", "en_us.json"), dict(sorted(en.items())))
    write_json(asset_path("lang", "fr_fr.json"), dict(sorted(fr.items())))


def generate():
    generate_blocks()
    generate_items()
    generate_lang()
