"""Modeles GeckoLib (geo), animations et textures des creatures, generes ensemble pour garder les UV coherents."""
import math

from PIL import Image

from .common import asset_path, write_json
from .pixels import hexc, mix, rng, shade, value_noise


class Cube:
    def __init__(self, origin, size, material, detail=None, inflate=0.0):
        self.origin = origin
        self.size = size
        self.material = material
        self.detail = detail
        self.inflate = inflate
        self.uv = (0, 0)

    @property
    def footprint(self):
        w, h, d = (math.ceil(v) for v in self.size)
        return 2 * (d + w), d + h


class Bone:
    def __init__(self, name, pivot, parent=None, rotation=None, cubes=()):
        self.name = name
        self.pivot = pivot
        self.parent = parent
        self.rotation = rotation
        self.cubes = list(cubes)


class Model:
    def __init__(self, name, tex_w, tex_h, bounds=(2, 2)):
        self.name = name
        self.tex_w = tex_w
        self.tex_h = tex_h
        self.bounds = bounds
        self.bones = []

    def bone(self, *args, **kwargs):
        b = Bone(*args, **kwargs)
        self.bones.append(b)
        return b

    def pack_uvs(self):
        cubes = sorted((c for b in self.bones for c in b.cubes), key=lambda c: -c.footprint[1])
        x = y = row_h = 0
        for cube in cubes:
            fw, fh = cube.footprint
            if x + fw > self.tex_w:
                x, y, row_h = 0, y + row_h, 0
            if y + fh > self.tex_h:
                raise ValueError(f"Texture trop petite pour {self.name}")
            cube.uv = (x, y)
            x += fw
            row_h = max(row_h, fh)

    def geo_json(self):
        bones = []
        for b in self.bones:
            data = {"name": b.name, "pivot": b.pivot}
            if b.parent:
                data["parent"] = b.parent
            if b.rotation:
                data["rotation"] = b.rotation
            if b.cubes:
                cubes = []
                for c in b.cubes:
                    cube = {"origin": c.origin, "size": c.size, "uv": list(c.uv)}
                    if c.inflate:
                        cube["inflate"] = c.inflate
                    cubes.append(cube)
                data["cubes"] = cubes
            bones.append(data)
        return {"format_version": "1.12.0", "minecraft:geometry": [{
            "description": {"identifier": f"geometry.{self.name}", "texture_width": self.tex_w,
                            "texture_height": self.tex_h, "visible_bounds_width": self.bounds[0],
                            "visible_bounds_height": self.bounds[1], "visible_bounds_offset": [0, self.bounds[1] / 2, 0]},
            "bones": bones}]}

    def paint(self, materials):
        """
        Peint chaque face de chaque cube. Retourne (texture, glowmask).

        Options d'un materiau : colors (palette sombre -> claire), pattern (stripes, plates, cracks, scales,
        patches, fur, flame, tatters, eyespots), speckle (couleur, probabilite), glow, trim (couleur de bordure).
        Chaque face recoit un degrade vertical, un eclairage selon son orientation et un contour d'un pixel.
        """
        tex = Image.new("RGBA", (self.tex_w, self.tex_h), (0, 0, 0, 0))
        glow = Image.new("RGBA", (self.tex_w, self.tex_h), (0, 0, 0, 0))
        field = value_noise(max(self.tex_w, self.tex_h), self.name, 1)
        r = rng(self.name + "paint")
        for b in self.bones:
            for c in b.cubes:
                mat = materials[c.material]
                palette = [hexc(col) for col in mat["colors"]]
                pattern = mat.get("pattern")
                w, h, d = (math.ceil(v) for v in c.size)
                u, v = c.uv
                faces = {
                    "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
                    "east": (u, v + d, d, h), "north": (u + d, v + d, w, h),
                    "west": (u + d + w, v + d, d, h), "south": (u + 2 * d + w, v + d, w, h),
                }
                cracks = set()
                if pattern == "cracks":
                    for _ in range(max(1, (w * h) // 18)):
                        fx0, fy0 = r.randrange(max(1, 2 * (d + w))), r.randrange(max(1, d + h))
                        for _ in range(r.randint(3, 7)):
                            cracks.add((u + fx0 % (2 * (d + w)), v + fy0 % (d + h)))
                            fx0 += r.choice((-1, 0, 1))
                            fy0 += r.choice((0, 1))
                for face, (fx, fy, fw, fh) in faces.items():
                    light = {"top": 1.15, "bottom": 0.72, "north": 1.0, "south": 0.9, "east": 0.86, "west": 0.86}[face]
                    for yy in range(fh):
                        for xx in range(fw):
                            px, py = fx + xx, fy + yy
                            if px >= self.tex_w or py >= self.tex_h:
                                continue
                            n = field[py % len(field)][px % len(field)]
                            tone = int(n * 0.999 * len(palette))
                            if pattern == "stripes" and face != "top" and (xx + yy // 2) % 4 == 0:
                                tone = 0
                            elif pattern == "plates" and (xx % 4 == 3 or yy % 4 == 3):
                                tone = 0
                            elif pattern == "scales" and (xx + (yy // 2) * 2) % 4 == 0 and yy % 2 == 0:
                                tone = max(0, tone - 2)
                            elif pattern == "patches" and field[(py * 3) % len(field)][(px * 3) % len(field)] > 0.62:
                                tone = 0
                            elif pattern == "fur" and (xx * 7 + px) % 3 == 0:
                                tone = max(0, tone - 1)
                            elif pattern == "flame":
                                tone = min(len(palette) - 1, int((1 - yy / max(1, fh)) * len(palette) * 0.9 + n * 1.2))
                            color = palette[max(0, min(len(palette) - 1, tone))]
                            gradient = 1.08 - 0.22 * (yy / max(1, fh - 1)) if face not in ("top", "bottom") else 1.0
                            color = shade(color, light * gradient)
                            if pattern == "tatters" and face not in ("top", "bottom") and yy >= fh - 3 and r.random() < 0.18 * (yy - fh + 4):
                                tex.putpixel((px, py), (0, 0, 0, 0))
                                continue
                            if pattern == "eyespots" and face in ("top", "bottom"):
                                cx, cy = fw / 2, fh / 2
                                dist = ((xx + 0.5 - cx) ** 2 + (yy + 0.5 - cy) ** 2) ** 0.5
                                if dist < 1.5:
                                    color = hexc("#ffffff")
                                elif dist < 2.6:
                                    color = hexc(mat.get("spot", "#1e3a44"))
                            if mat.get("speckle") and r.random() < mat["speckle"][1]:
                                color = hexc(mat["speckle"][0])
                                if mat.get("speckle_glow"):
                                    glow.putpixel((px, py), color)
                            if (px, py) in cracks:
                                color = hexc(mat.get("crack", "#b06cff"))
                                glow.putpixel((px, py), color)
                            edge = xx in (0, fw - 1) or yy in (0, fh - 1)
                            if edge and fw > 2 and fh > 2 and mat.get("edge", True):
                                color = shade(color, 0.72)
                            # Ourlet : liseré colore uniquement en bas des faces laterales (bas de robe, bord d'armure)
                            if mat.get("trim") and face not in ("top", "bottom") and yy == fh - 1 and fh > 3:
                                color = hexc(mat["trim"])
                            tex.putpixel((px, py), color)
                            if mat.get("glow"):
                                glow.putpixel((px, py), color)
                if c.detail:
                    c.detail(tex, glow, faces)
        return tex, glow


def eyes(color_hex, glow=True, y_frac=0.35, spacing=0.25, size=1, mouth=None, skull=False):
    """Visage : orbites sombres, iris lumineux, reflet, et en option bouche (dents) ou nez de crane."""
    def apply(tex, glow_img, faces):
        fx, fy, fw, fh = faces["north"]
        ey = fy + max(1, int(fh * y_frac))
        socket = hexc("#0c0608")
        eye_columns = () if size <= 0 else (fx + int(fw * (0.5 - spacing)), fx + int(fw * (0.5 + spacing)) - size + 1)
        for cx in eye_columns:
            for dx in range(-1, size + 1):
                for dy in range(-1 if skull else 0, size + (1 if skull else 0)):
                    x, y = cx + dx, ey + dy
                    if fx <= x < fx + fw and fy <= y < fy + fh:
                        tex.putpixel((x, y), socket)
            for dx in range(size):
                for dy in range(size):
                    tex.putpixel((cx + dx, ey + dy), hexc(color_hex))
                    if glow:
                        glow_img.putpixel((cx + dx, ey + dy), hexc(color_hex))
            if size > 1:
                tex.putpixel((cx, ey), hexc("#ffffff"))
                if glow:
                    glow_img.putpixel((cx, ey), hexc("#ffffff"))
        if skull:
            nx = fx + fw // 2
            tex.putpixel((nx, ey + size + 1), socket)
        if mouth:
            my = fy + fh - 2
            for x in range(fx + 2, fx + fw - 2):
                tex.putpixel((x, my), hexc("#1a0606"))
                if x % 2 == 0:
                    tex.putpixel((x, my - 1), hexc(mouth))
    return apply


def core_face(color_hex):
    def apply(tex, glow_img, faces):
        fx, fy, fw, fh = faces["north"]
        for yy in range(fh):
            for xx in range(fw):
                c = shade(hexc(color_hex), 1.3 if (xx + yy) % 3 == 0 else 1.0)
                tex.putpixel((fx + xx, fy + yy), c)
                glow_img.putpixel((fx + xx, fy + yy), c)
    return apply


# ------------------------------------------------------------------------------------------------
# Animations
# ------------------------------------------------------------------------------------------------

def wave(length, amplitude, phase=0.0, steps=8, axis=0, offset=0.0, base=None):
    """Keyframes sinusoidales bouclables sur un axe (rotation ou position)."""
    frames = {}
    for i in range(steps + 1):
        t = length * i / steps
        value = offset + amplitude * math.sin(2 * math.pi * (i / steps) + phase)
        vec = list(base) if base else [0.0, 0.0, 0.0]
        vec[axis] = round(vec[axis] + value, 3)
        frames[f"{round(t, 4)}"] = vec
    return frames


def keys(*pairs):
    return {f"{round(t, 4)}": [round(x, 3) for x in vec] for t, vec in pairs}


def anim(length, bones, loop=True):
    data = {"animation_length": length, "bones": bones}
    if loop:
        data["loop"] = True
    return data


def save_animations(name, animations):
    # Le Champion fletri herite du code du Chevalier fletri : il en garde les noms d'animation.
    prefix = "withered_knight" if name == "withered_champion" else name
    write_json(asset_path("animations", "entity", f"{name}.animation.json"), {
        "format_version": "1.8.0",
        "animations": {f"animation.{prefix}.{k}": v for k, v in animations.items()},
        "geckolib_format_version": 2})


def save_model(model, materials, glowmask=True):
    model.pack_uvs()
    write_json(asset_path("geo", "entity", f"{model.name}.geo.json"), model.geo_json())
    tex, glow = model.paint(materials)
    path = asset_path("textures", "entity", f"{model.name}.png")
    path.parent.mkdir(parents=True, exist_ok=True)
    tex.save(path)
    if glowmask:
        glow.save(asset_path("textures", "entity", f"{model.name}_glowmask.png"))


# ------------------------------------------------------------------------------------------------
# Creatures
# ------------------------------------------------------------------------------------------------

def ember_stalker():
    m = Model("ember_stalker", 64, 64, (2, 2))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 13, 0], "root", cubes=[
        Cube([-4, 10, -8], [8, 7, 16], "hide"),
        Cube([-1, 17, -6], [2, 2, 3], "flame"),
        Cube([-1, 17, -1], [2, 3, 3], "flame"),
        Cube([-1, 17, 4], [2, 2, 3], "flame"),
    ])
    m.bone("head", [0, 15, -8], "body", cubes=[
        Cube([-3.5, 12, -14], [7, 6, 6], "hide", detail=eyes("#ffd36b", y_frac=0.25, spacing=0.28)),
        Cube([-2, 12, -17], [4, 3, 3], "snout", detail=eyes("#ff7a1a", y_frac=0.0, spacing=0.0, size=0, mouth="#f0e8d0")),
        Cube([-3, 18, -12], [1, 2, 1], "flame"),
        Cube([2, 18, -12], [1, 2, 1], "flame"),
    ])
    m.bone("tail", [0, 16, 8], "body", rotation=[-20, 0, 0], cubes=[
        Cube([-1, 15, 8], [2, 2, 8], "hide"),
        Cube([-1.5, 14.5, 16], [3, 3, 3], "flame"),
    ])
    for name, x, z in (("leg_front_left", 2.5, -6), ("leg_front_right", -2.5, -6),
                       ("leg_back_left", 2.5, 6), ("leg_back_right", -2.5, 6)):
        m.bone(name, [x, 10, z], "root", cubes=[Cube([x - 1.5, 0, z - 1.5], [3, 10, 3], "leg")])
    save_model(m, {
        "hide": {"colors": ["#1e0c0a", "#3a1a12", "#4e2418", "#62301e", "#74381f"], "pattern": "stripes",
                 "speckle": ("#ff7a1a", 0.05), "speckle_glow": True},
        "snout": {"colors": ["#1a0c0a", "#2a140e", "#3a1a12"]},
        "flame": {"colors": ["#c8200a", "#ff5a0a", "#ff8a1a", "#ffb02e", "#ffe27a"], "pattern": "flame", "glow": True, "edge": False},
        "leg": {"colors": ["#140806", "#24100c", "#34160f"], "pattern": "fur"},
    })
    legs_walk = lambda amp, length: {
        "leg_front_left": {"rotation": wave(length, amp)},
        "leg_back_right": {"rotation": wave(length, amp)},
        "leg_front_right": {"rotation": wave(length, amp, math.pi)},
        "leg_back_left": {"rotation": wave(length, amp, math.pi)},
    }
    walk = legs_walk(30, 1.0)
    walk.update({"tail": {"rotation": wave(1.0, 12, axis=1)}, "body": {"position": wave(1.0, 0.3, axis=1, steps=4)}})
    run = legs_walk(50, 0.5)
    run.update({"tail": {"rotation": wave(0.5, 6, axis=0, offset=-15)}, "body": {"rotation": wave(0.5, 3, axis=0)},
                "head": {"rotation": wave(0.5, 4, axis=0, offset=-5)}})
    save_animations("ember_stalker", {
        "idle": anim(3.0, {"body": {"position": wave(3.0, 0.25, axis=1)}, "tail": {"rotation": wave(3.0, 10, axis=1)},
                           "head": {"rotation": wave(3.0, 3, axis=0, phase=1.0)}}),
        "walk": anim(1.0, walk),
        "run": anim(0.5, run),
        "attack": anim(0.5, {
            "head": {"rotation": keys((0, [0, 0, 0]), (0.15, [-25, 0, 0]), (0.3, [20, 0, 0]), (0.5, [0, 0, 0]))},
            "body": {"position": keys((0, [0, 0, 0]), (0.15, [0, 0, 1]), (0.3, [0, 0, -2]), (0.5, [0, 0, 0]))},
            "leg_front_left": {"rotation": keys((0, [0, 0, 0]), (0.2, [-40, 0, 0]), (0.5, [0, 0, 0]))},
            "leg_front_right": {"rotation": keys((0, [0, 0, 0]), (0.2, [-40, 0, 0]), (0.5, [0, 0, 0]))},
        }, loop=False),
    })


def ash_wraith():
    m = Model("ash_wraith", 64, 64, (2, 3))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 14, 0], "root", cubes=[Cube([-4, 12, -2.5], [8, 10, 5], "robe")])
    m.bone("tail", [0, 12, 0], "body", cubes=[Cube([-3.5, 6, -2], [7, 6, 4], "robe")])
    m.bone("tail_end", [0, 6, 0], "tail", cubes=[Cube([-2.5, 1, -1.5], [5, 5, 3], "wisp")])
    m.bone("head", [0, 22, 0], "body", cubes=[
        Cube([-4, 22, -4], [8, 8, 8], "skull", detail=eyes("#7fe8ff", y_frac=0.35, spacing=0.22, size=2, skull=True, mouth="#d8d2cc")),
        Cube([-4.5, 26, -4.5], [9, 5, 9], "robe", inflate=0.25),
    ])
    m.bone("arm_left", [5, 21, 0], "body", cubes=[Cube([4, 11, -1.5], [3, 11, 3], "wisp")])
    m.bone("arm_right", [-5, 21, 0], "body", cubes=[Cube([-7, 11, -1.5], [3, 11, 3], "wisp")])
    save_model(m, {
        "robe": {"colors": ["#262322", "#3a3634", "#4a4644", "#5a5552", "#6a6460"], "pattern": "tatters",
                 "speckle": ("#ffb347", 0.03), "speckle_glow": True},
        "wisp": {"colors": ["#5a5552", "#7a7470", "#8f8a86", "#a8a29c"], "pattern": "tatters", "edge": False},
        "skull": {"colors": ["#b8b2ac", "#c8c2bc", "#d8d2cc", "#e8e2dc"]},
    }, glowmask=False)
    save_animations("ash_wraith", {
        "float": anim(3.0, {
            "root": {"position": wave(3.0, 1.5, axis=1, offset=1.5)},
            "arm_left": {"rotation": wave(3.0, 8, axis=0, offset=-10)},
            "arm_right": {"rotation": wave(3.0, 8, math.pi, axis=0, offset=-10)},
            "tail": {"rotation": wave(3.0, 8, axis=0, offset=10)},
            "tail_end": {"rotation": wave(3.0, 12, 1.2, axis=0, offset=10)},
        }),
        "fly": anim(1.5, {
            "root": {"position": wave(1.5, 1.0, axis=1, offset=1.5)},
            "body": {"rotation": wave(1.5, 3, axis=2, base=[15, 0, 0])},
            "tail": {"rotation": wave(1.5, 10, axis=0, offset=30)},
            "tail_end": {"rotation": wave(1.5, 14, 1.0, axis=0, offset=25)},
            "arm_left": {"rotation": wave(1.5, 6, axis=0, offset=35)},
            "arm_right": {"rotation": wave(1.5, 6, math.pi, axis=0, offset=35)},
        }),
        "attack": anim(0.6, {
            "arm_left": {"rotation": keys((0, [0, 0, 0]), (0.2, [-120, 0, -20]), (0.4, [-10, 0, 20]), (0.6, [0, 0, 0]))},
            "arm_right": {"rotation": keys((0, [0, 0, 0]), (0.2, [-120, 0, 20]), (0.4, [-10, 0, -20]), (0.6, [0, 0, 0]))},
            "head": {"rotation": keys((0, [0, 0, 0]), (0.25, [15, 0, 0]), (0.6, [0, 0, 0]))},
        }, loop=False),
    })


def onyx_sentinel():
    m = Model("onyx_sentinel", 128, 128, (4, 4))
    m.bone("root", [0, 0, 0])
    m.bone("leg_left", [5, 20, 0], "root", cubes=[Cube([2, 0, -3.5], [6, 20, 7], "stone")])
    m.bone("leg_right", [-5, 20, 0], "root", cubes=[Cube([-8, 0, -3.5], [6, 20, 7], "stone")])
    m.bone("body", [0, 20, 0], "root", cubes=[
        Cube([-6, 20, -4], [12, 8, 8], "stone"),
        Cube([-10, 28, -6], [20, 14, 12], "plate"),
        Cube([-2.5, 32, -7], [5, 5, 1], "core", detail=core_face("#b06cff")),
    ])
    m.bone("head", [0, 42, 0], "body", cubes=[
        Cube([-5, 42, -6], [10, 9, 10], "plate", detail=eyes("#e2c8ff", y_frac=0.4, spacing=0.24, size=2)),
        Cube([-6, 48, -2], [2, 6, 2], "crystal"),
        Cube([4, 48, -2], [2, 6, 2], "crystal"),
    ])
    for side, sx in (("left", 1), ("right", -1)):
        shoulder_x = 10 if sx > 0 else -18
        arm_x = 11 if sx > 0 else -17
        m.bone(f"arm_{side}", [11 * sx, 40, 0], "body", cubes=[
            Cube([shoulder_x, 35, -4], [8, 7, 8], "plate"),
            Cube([arm_x, 14, -3], [6, 21, 6], "stone"),
            Cube([shoulder_x, 8, -4], [8, 6, 8], "plate"),
            Cube([shoulder_x + 3, 42, -1], [2, 4, 2], "crystal"),
        ])
    save_model(m, {
        "stone": {"colors": ["#0a080d", "#120f17", "#1a1621", "#221c2b", "#2a2334"], "pattern": "cracks", "crack": "#b06cff"},
        "plate": {"colors": ["#1a1622", "#241e2e", "#2e263b", "#3a3049", "#463a58"], "pattern": "plates", "trim": "#8f62d6"},
        "core": {"colors": ["#7d3fe0", "#b06cff"], "glow": True},
        "crystal": {"colors": ["#6a3fc0", "#8f62d6", "#c9a6ff"], "glow": True},
    })
    walk = {
        "leg_left": {"rotation": wave(1.6, 22)},
        "leg_right": {"rotation": wave(1.6, 22, math.pi)},
        "arm_left": {"rotation": wave(1.6, 16, math.pi)},
        "arm_right": {"rotation": wave(1.6, 16)},
        "body": {"rotation": wave(1.6, 3, axis=2)},
    }
    save_animations("onyx_sentinel", {
        "idle": anim(3.0, {
            "body": {"position": wave(3.0, 0.4, axis=1)},
            "arm_left": {"rotation": wave(3.0, 3, axis=2, offset=-3)},
            "arm_right": {"rotation": wave(3.0, 3, math.pi, axis=2, offset=3)},
            "head": {"rotation": wave(3.0, 4, 0.7, axis=1)},
        }),
        "walk": anim(1.6, walk),
        "punch": anim(0.6, {
            "arm_right": {"rotation": keys((0, [0, 0, 0]), (0.2, [-100, 15, 0]), (0.35, [-40, -10, 0]), (0.6, [0, 0, 0]))},
            "body": {"rotation": keys((0, [0, 0, 0]), (0.2, [0, 15, 0]), (0.35, [5, -15, 0]), (0.6, [0, 0, 0]))},
        }, loop=False),
        "slam": anim(1.5, {
            "arm_left": {"rotation": keys((0, [0, 0, 0]), (0.5, [-170, 0, -10]), (0.7, [-40, 0, 0]), (1.1, [-40, 0, 0]), (1.5, [0, 0, 0]))},
            "arm_right": {"rotation": keys((0, [0, 0, 0]), (0.5, [-170, 0, 10]), (0.7, [-40, 0, 0]), (1.1, [-40, 0, 0]), (1.5, [0, 0, 0]))},
            "body": {"rotation": keys((0, [0, 0, 0]), (0.5, [-10, 0, 0]), (0.7, [20, 0, 0]), (1.1, [20, 0, 0]), (1.5, [0, 0, 0]))},
            "head": {"rotation": keys((0, [0, 0, 0]), (0.5, [-15, 0, 0]), (0.7, [10, 0, 0]), (1.5, [0, 0, 0]))},
        }, loop=False),
    })


def magma_crab():
    m = Model("magma_crab", 64, 64, (2, 1))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 5, 0], "root", cubes=[
        Cube([-6, 3, -5], [12, 5, 10], "shell"),
        Cube([-4.5, 8, -3.5], [9, 2, 7], "magma"),
    ])
    m.bone("head", [0, 7, -5], "body", cubes=[
        Cube([-3, 7, -6], [1, 3, 1], "leg"),
        Cube([2, 7, -6], [1, 3, 1], "leg"),
        Cube([-3.5, 10, -6.5], [2, 2, 2], "eye"),
        Cube([1.5, 10, -6.5], [2, 2, 2], "eye"),
    ])
    for side, sx in (("left", 1), ("right", -1)):
        arm_x = 6 if sx > 0 else -8
        pincer_x = 6 if sx > 0 else -10
        m.bone(f"claw_{side}", [6 * sx, 5, -4], "body", rotation=[0, -20 * sx, 0], cubes=[
            Cube([arm_x, 4, -8], [2, 2, 5], "shell"),
            Cube([pincer_x, 3, -12], [4, 3, 4], "claw"),
        ])
    for i, z in enumerate((-3, 0, 3)):
        for side, sx in (("l", 1), ("r", -1)):
            x0 = 6 if sx > 0 else -10
            m.bone(f"leg_{side}{i}", [6 * sx, 4, z], "root", rotation=[0, 0, -25 * sx], cubes=[
                Cube([x0, 3.5, z - 0.5], [4, 1, 1], "leg"),
            ])
    save_model(m, {
        "shell": {"colors": ["#3a0e06", "#4a1408", "#5e1a0a", "#74220e", "#8a2c12"], "pattern": "cracks", "crack": "#ff8a1a"},
        "magma": {"colors": ["#ff5a0a", "#ff8a1a", "#ffb02e"], "glow": True},
        "claw": {"colors": ["#6e2210", "#8a2c12", "#a8381a", "#c24a22"], "pattern": "plates", "trim": "#ffb347"},
        "leg": {"colors": ["#3a1006", "#4a1408"]},
        "eye": {"colors": ["#ffd36b", "#fff0a0"], "glow": True},
    })
    legs = {}
    for i in range(3):
        legs[f"leg_l{i}"] = {"rotation": wave(0.6, 18, i * 1.3, axis=1, base=[0, 0, -25])}
        legs[f"leg_r{i}"] = {"rotation": wave(0.6, 18, i * 1.3 + math.pi, axis=1, base=[0, 0, 25])}
    walk = dict(legs)
    walk["body"] = {"rotation": wave(0.6, 4, axis=2)}
    save_animations("magma_crab", {
        "idle": anim(2.0, {
            "claw_left": {"rotation": wave(2.0, 8, axis=1, base=[0, -20, 0])},
            "claw_right": {"rotation": wave(2.0, 8, math.pi, axis=1, base=[0, 20, 0])},
            "head": {"position": wave(2.0, 0.3, axis=1)},
        }),
        "walk": anim(0.6, walk),
    })


def soul_moth():
    m = Model("soul_moth", 64, 32, (1, 1))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 4, 0], "root", cubes=[Cube([-1.5, 2, -3], [3, 3, 6], "fur")])
    m.bone("head", [0, 4, -3], "body", cubes=[
        Cube([-1.5, 2.5, -5], [3, 2, 2], "fur", detail=eyes("#7fe8ff", y_frac=0.0, spacing=0.3)),
        Cube([-1.5, 4.5, -6], [1, 2, 1], "antenna"),
        Cube([0.5, 4.5, -6], [1, 2, 1], "antenna"),
    ])
    m.bone("wing_left", [1.5, 4.5, 0], "body", cubes=[Cube([1.5, 4.5, -3], [7, 1, 7], "wing")])
    m.bone("wing_right", [-1.5, 4.5, 0], "body", cubes=[Cube([-8.5, 4.5, -3], [7, 1, 7], "wing")])
    save_model(m, {
        "fur": {"colors": ["#162c34", "#1e3a44", "#26505c", "#2e6270"], "pattern": "fur"},
        "antenna": {"colors": ["#7fe8ff"], "glow": True},
        "wing": {"colors": ["#1e7a8c", "#2aa8c0", "#4fd0e0", "#7fe8ff", "#c4f8ff"], "pattern": "eyespots", "spot": "#0e2a34", "glow": True},
    })
    save_animations("soul_moth", {
        "fly": anim(0.4, {
            "wing_left": {"rotation": wave(0.4, 45, axis=2)},
            "wing_right": {"rotation": wave(0.4, 45, math.pi, axis=2)},
            "body": {"position": wave(0.4, 0.4, axis=1)},
        }),
    })


def withered_knight(name="withered_knight", armor=("#4a4a4e", "#5a5a5e", "#6a6a6e"), trim="#8a8a80"):
    m = Model(name, 128, 64, (2, 3))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 24, 0], "root", cubes=[
        Cube([-4, 22, -2], [8, 12, 4], "armor"),
        Cube([-1, 14, -1], [2, 8, 2], "bone"),
    ])
    m.bone("head", [0, 34, 0], "body", cubes=[
        Cube([-4, 34, -4], [8, 8, 8], "skull", detail=eyes("#7fe8ff", y_frac=0.35, spacing=0.22, skull=True, mouth="#8a8a80")),
        Cube([-4.5, 39, -4.5], [9, 4, 9], "armor"),
    ])
    m.bone("arm_left", [5, 32, 0], "body", cubes=[Cube([4, 20, -1], [2, 12, 2], "bone"), Cube([3.5, 28, -2], [3, 5, 4], "armor")])
    m.bone("arm_right", [-5, 32, 0], "body", rotation=[-20, 0, 0], cubes=[
        Cube([-6, 20, -1], [2, 12, 2], "bone"), Cube([-6.5, 28, -2], [3, 5, 4], "armor")])
    m.bone("sword", [-5, 21, 0], "arm_right", cubes=[
        Cube([-5.5, 20, -8], [1, 2, 9], "blade"), Cube([-6, 19.5, 0], [2, 3, 1], "armor"), Cube([-5.5, 20.5, 1], [1, 1, 3], "bone")])
    m.bone("leg_left", [2, 14, 0], "root", cubes=[Cube([1, 0, -1], [2, 14, 2], "bone")])
    m.bone("leg_right", [-2, 14, 0], "root", cubes=[Cube([-3, 0, -1], [2, 14, 2], "bone")])
    save_model(m, {
        "bone": {"colors": ["#1e1e1e", "#2a2a2a", "#333333", "#3d3d3d"], "pattern": "fur"},
        "skull": {"colors": ["#1e1e1e", "#262626", "#303030", "#3a3a3a"]},
        "armor": {"colors": list(armor), "pattern": "plates", "trim": trim},
        "blade": {"colors": ["#1c1c20", "#2a2a30", "#7fe8ff"], "speckle": ("#7fe8ff", 0.1), "speckle_glow": True},
    })
    save_animations(name, {
        "idle": anim(3.0, {"body": {"position": wave(3.0, 0.3, axis=1)},
                           "arm_left": {"rotation": wave(3.0, 4, axis=0)},
                           "head": {"rotation": wave(3.0, 5, 0.6, axis=1)}}),
        "walk": anim(1.2, {"leg_left": {"rotation": wave(1.2, 30)}, "leg_right": {"rotation": wave(1.2, 30, math.pi)},
                           "arm_left": {"rotation": wave(1.2, 25, math.pi)},
                           "arm_right": {"rotation": wave(1.2, 10, base=[-20, 0, 0])}}),
        "attack": anim(0.6, {
            "arm_right": {"rotation": keys((0, [-20, 0, 0]), (0.2, [-150, 0, 10]), (0.35, [-30, 0, -10]), (0.6, [-20, 0, 0]))},
            "body": {"rotation": keys((0, [0, 0, 0]), (0.2, [0, 20, 0]), (0.35, [10, -20, 0]), (0.6, [0, 0, 0]))},
        }, loop=False),
    })


def basalt_beetle():
    m = Model("basalt_beetle", 128, 64, (3, 2))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 8, 0], "root", cubes=[
        Cube([-7, 4, -8], [14, 7, 17], "shell"),
        Cube([-6, 11, -6], [12, 3, 13], "shell"),
        Cube([-1, 11, -7], [2, 4, 15], "lava"),
    ])
    m.bone("head", [0, 7, -8], "body", cubes=[
        Cube([-4, 4, -13], [8, 6, 5], "plate", detail=eyes("#ff8a1a", y_frac=0.25, spacing=0.3)),
        Cube([-5, 5, -17], [2, 2, 5], "horn"),
        Cube([3, 5, -17], [2, 2, 5], "horn"),
    ])
    for i, z in enumerate((-5, 0, 5)):
        for side, sx in (("l", 1), ("r", -1)):
            x0 = 7 if sx > 0 else -12
            m.bone(f"leg_{side}{i}", [7 * sx, 5, z], "root", rotation=[0, 0, 30 * -sx], cubes=[
                Cube([x0, 4, z - 1], [5, 2, 2], "plate")])
    save_model(m, {
        "shell": {"colors": ["#1c1c20", "#26262a", "#303034", "#3a3a40", "#46464c"], "pattern": "cracks", "crack": "#ff6a1a"},
        "plate": {"colors": ["#141418", "#1c1c20", "#26262a"], "pattern": "plates"},
        "lava": {"colors": ["#ff5a0a", "#ff8a1a", "#ffb02e"], "glow": True},
        "horn": {"colors": ["#4a4a50", "#5a5a60"]},
    })
    legs = {}
    for i in range(3):
        legs[f"leg_l{i}"] = {"rotation": wave(0.8, 20, i * 2.1, axis=1, base=[0, 0, -30])}
        legs[f"leg_r{i}"] = {"rotation": wave(0.8, 20, i * 2.1 + math.pi, axis=1, base=[0, 0, 30])}
    walk = dict(legs)
    walk["body"] = {"position": wave(0.8, 0.3, axis=1, steps=4)}
    save_animations("basalt_beetle", {
        "idle": anim(3.0, {"head": {"rotation": wave(3.0, 5, axis=1)}, "body": {"position": wave(3.0, 0.2, axis=1)}}),
        "walk": anim(0.8, walk),
        "attack": anim(0.5, {"head": {"rotation": keys((0, [0, 0, 0]), (0.15, [-30, 0, 0]), (0.3, [25, 0, 0]), (0.5, [0, 0, 0]))},
                             "body": {"position": keys((0, [0, 0, 0]), (0.15, [0, 0, 1]), (0.3, [0, 0, -2]), (0.5, [0, 0, 0]))}},
                     loop=False),
    })


def infernal_warlock():
    m = Model("infernal_warlock", 128, 64, (2, 3))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 24, 0], "root", cubes=[
        Cube([-4, 12, -3], [8, 12, 6], "robe"),
        Cube([-5, 0, -3.5], [10, 12, 7], "robe"),
    ])
    m.bone("head", [0, 24, 0], "body", cubes=[
        Cube([-4, 24, -4], [8, 8, 8], "skin", detail=eyes("#ffb02e", y_frac=0.4, spacing=0.22, size=2)),
        Cube([-4.5, 29, -4.5], [9, 4, 9], "hood"),
        Cube([-1, 22, -5], [2, 4, 1], "beard"),
    ])
    m.bone("arm_left", [5, 22, 0], "body", cubes=[Cube([4, 12, -2], [4, 11, 4], "robe")])
    m.bone("arm_right", [-5, 22, 0], "body", cubes=[Cube([-8, 12, -2], [4, 11, 4], "robe")])
    m.bone("staff", [-6, 13, 0], "arm_right", cubes=[
        Cube([-6.5, 2, -0.5], [1, 22, 1], "wood"), Cube([-7.5, 24, -1.5], [3, 3, 3], "orb")])
    save_model(m, {
        "robe": {"colors": ["#2a0606", "#3a0a0a", "#4a0e0e", "#5c1212", "#6e1616"], "pattern": "stripes", "trim": "#ffc531"},
        "hood": {"colors": ["#2a0606", "#3a0a0a"]},
        "skin": {"colors": ["#5a3a30", "#6a4438"]},
        "beard": {"colors": ["#8a8a80", "#a0a098"]},
        "wood": {"colors": ["#3a1a0a", "#4a2410"]},
        "orb": {"colors": ["#ff6a1a", "#ffb02e", "#ffd36b"], "glow": True},
    })
    save_animations("infernal_warlock", {
        "idle": anim(3.0, {"body": {"position": wave(3.0, 0.3, axis=1)},
                           "staff": {"rotation": wave(3.0, 4, axis=2)},
                           "arm_left": {"rotation": wave(3.0, 5, axis=0)}}),
        "walk": anim(1.4, {"body": {"rotation": wave(1.4, 3, axis=2)},
                           "arm_left": {"rotation": wave(1.4, 20)}, "arm_right": {"rotation": wave(1.4, 10, math.pi)}}),
        "cast": anim(0.8, {
            "arm_right": {"rotation": keys((0, [0, 0, 0]), (0.3, [-120, 0, 0]), (0.5, [-70, 0, 0]), (0.8, [0, 0, 0]))},
            "arm_left": {"rotation": keys((0, [0, 0, 0]), (0.3, [-60, 0, -30]), (0.8, [0, 0, 0]))},
        }, loop=False),
        "summon": anim(1.4, {
            "arm_right": {"rotation": keys((0, [0, 0, 0]), (0.4, [-170, 0, 20]), (1.0, [-170, 0, 20]), (1.4, [0, 0, 0]))},
            "arm_left": {"rotation": keys((0, [0, 0, 0]), (0.4, [-170, 0, -20]), (1.0, [-170, 0, -20]), (1.4, [0, 0, 0]))},
            "body": {"position": keys((0, [0, 0, 0]), (0.5, [0, 2, 0]), (1.0, [0, 2, 0]), (1.4, [0, 0, 0]))},
        }, loop=False),
    })


def ember_cow():
    m = Model("ember_cow", 64, 64, (2, 2))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 13, 0], "root", cubes=[
        Cube([-6, 10, -8], [12, 10, 16], "hide"),
        Cube([-2, 9, 4], [4, 1, 3], "udder"),
    ])
    m.bone("head", [0, 17, -8], "body", cubes=[
        Cube([-4, 13, -14], [8, 8, 6], "hide", detail=eyes("#ffd36b", y_frac=0.25, spacing=0.3, size=2)),
        Cube([-3, 13, -15], [6, 3, 1], "snout"),
        Cube([-6, 19, -12], [2, 2, 1], "horn"),
        Cube([4, 19, -12], [2, 2, 1], "horn"),
        Cube([-7, 20, -12], [1, 3, 1], "horn"),
        Cube([6, 20, -12], [1, 3, 1], "horn"),
    ])
    m.bone("tail", [0, 19, 8], "body", rotation=[20, 0, 0], cubes=[Cube([-0.5, 12, 8], [1, 7, 1], "hide"),
                                                                   Cube([-1, 11, 7.5], [2, 2, 2], "glow")])
    for name, x, z in (("leg_front_left", 3.5, -5), ("leg_front_right", -3.5, -5), ("leg_back_left", 3.5, 6), ("leg_back_right", -3.5, 6)):
        m.bone(name, [x, 10, z], "root", cubes=[Cube([x - 2, 0, z - 2], [4, 10, 4], "leg")])
    save_model(m, {
        "hide": {"colors": ["#3a0e06", "#5a1a0e", "#6e2212", "#822a16", "#963418"], "pattern": "patches",
                 "speckle": ("#ffb347", 0.03), "speckle_glow": True},
        "snout": {"colors": ["#d88a6a", "#e8a080"]},
        "udder": {"colors": ["#e8a0a0", "#f0b8b0"]},
        "horn": {"colors": ["#e8d8b0", "#f0e4c4"]},
        "glow": {"colors": ["#ff8a1a", "#ffb347"], "glow": True},
        "leg": {"colors": ["#3a1008", "#4a160c"]},
    })
    legs = {
        "leg_front_left": {"rotation": wave(1.2, 25)}, "leg_back_right": {"rotation": wave(1.2, 25)},
        "leg_front_right": {"rotation": wave(1.2, 25, math.pi)}, "leg_back_left": {"rotation": wave(1.2, 25, math.pi)},
    }
    walk = dict(legs)
    walk["head"] = {"rotation": wave(1.2, 4, axis=0)}
    walk["tail"] = {"rotation": wave(1.2, 10, axis=2, base=[20, 0, 0])}
    save_animations("ember_cow", {
        "idle": anim(4.0, {"head": {"rotation": keys((0, [0, 0, 0]), (1.5, [25, 0, 0]), (2.5, [25, 5, 0]), (4.0, [0, 0, 0]))},
                           "tail": {"rotation": wave(4.0, 12, axis=2, base=[20, 0, 0])}}),
        "walk": anim(1.2, walk),
    })


def cinder_hen():
    m = Model("cinder_hen", 64, 32, (1, 1))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 5, 0], "root", cubes=[Cube([-3, 4, -4], [6, 6, 8], "feather")])
    m.bone("head", [0, 9, -4], "body", cubes=[
        Cube([-2, 9, -6], [4, 5, 3], "feather", detail=eyes("#ffd36b", y_frac=0.2, spacing=0.3)),
        Cube([-1, 10, -8], [2, 2, 2], "beak"),
        Cube([-0.5, 14, -5.5], [1, 2, 3], "crest"),
    ])
    m.bone("wing_left", [3, 9, 0], "body", cubes=[Cube([3, 5, -3], [1, 4, 6], "wing")])
    m.bone("wing_right", [-3, 9, 0], "body", cubes=[Cube([-4, 5, -3], [1, 4, 6], "wing")])
    m.bone("tail", [0, 8, 4], "body", rotation=[-30, 0, 0], cubes=[Cube([-2, 7, 4], [4, 4, 1], "crest")])
    m.bone("leg_left", [1.5, 4, 0], "root", cubes=[Cube([1, 0, -1], [1, 4, 1], "beak")])
    m.bone("leg_right", [-1.5, 4, 0], "root", cubes=[Cube([-2, 0, -1], [1, 4, 1], "beak")])
    save_model(m, {
        "feather": {"colors": ["#3a3432", "#4a4442", "#58514e", "#665e5a", "#78706a"], "pattern": "scales",
                    "speckle": ("#ff8a3a", 0.04), "speckle_glow": True},
        "wing": {"colors": ["#3a3432", "#4a4442"]},
        "beak": {"colors": ["#e8a030", "#f0b840"]},
        "crest": {"colors": ["#ff5a1a", "#ff8a2a"], "glow": True},
    })
    save_animations("cinder_hen", {
        "idle": anim(2.0, {"head": {"rotation": keys((0, [0, 0, 0]), (0.8, [0, 0, 0]), (1.0, [30, 0, 0]), (1.2, [0, 0, 0]), (2.0, [0, 0, 0]))}}),
        "walk": anim(0.6, {"leg_left": {"rotation": wave(0.6, 35)}, "leg_right": {"rotation": wave(0.6, 35, math.pi)},
                           "head": {"position": wave(0.6, 0.4, axis=2)}}),
        "flap": anim(0.3, {"wing_left": {"rotation": wave(0.3, 50, axis=2, offset=-40)},
                           "wing_right": {"rotation": wave(0.3, 50, math.pi, axis=2, offset=40)}}),
    })


def generate():
    ember_cow()
    cinder_hen()
    soul_moth()
    withered_knight()
    withered_knight("withered_champion", ("#3a3424", "#5a4a24", "#7a6428"), "#ffd36b")
    basalt_beetle()
    infernal_warlock()
    ember_stalker()
    ash_wraith()
    onyx_sentinel()
    magma_crab()
