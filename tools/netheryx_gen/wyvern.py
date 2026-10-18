"""Wyverne de cendre (monture) : grand modele GeckoLib articule et animations d'ailes a plusieurs segments.

Conventions (format Bedrock / GeckoLib) : +x = cote gauche de la creature, -z = avant, y = haut, unites en pixels.
Rotations des os en degres. Pour une aile gauche, z negatif la leve et y negatif la replie vers l'arriere ;
l'aile droite utilise les signes opposes (symetrie).
"""
import math

from .entities import Cube, Model, anim, eyes, keys, save_animations, save_model

NAME = "ash_wyvern"


def mirror(cube, material=None):
    """Cube symetrique (x -> -x) pour le cote droit."""
    (x, y, z), (w, h, d) = cube
    return [-(x + w), y, z], [w, h, d]


def build():
    m = Model(NAME, 256, 256, (8, 5))
    m.bone("root", [0, 0, 0])
    m.bone("body", [0, 17, 1], "root", cubes=[
        Cube([-7, 12, -10], [14, 12, 12], "scales"),
        Cube([-6, 11, -9], [12, 1, 10], "belly"),
        Cube([-6, 13, 2], [12, 10, 10], "scales"),
        Cube([-5, 12, 2], [10, 1, 9], "belly"),
        Cube([-4.5, 24, -7], [9, 1, 9], "harness"),
        Cube([-1.5, 25, 0], [3, 2, 1.5], "harness"),
    ] + [Cube([-0.75, 24 if z < 2 else 23, z], [1.5, 3, 2], "spine", rotation=[-25, 0, 0], pivot=[0, 24, z + 1])
         for z in (-9, 3, 6, 9)])

    # Cou en deux segments et tete articulee (machoire mobile)
    m.bone("neck1", [0, 20, -10], "body", rotation=[-28, 0, 0], cubes=[
        Cube([-3.5, 17, -18], [7, 7, 9], "scales"),
        Cube([-0.75, 24, -16], [1.5, 2.5, 2], "spine", rotation=[-25, 0, 0], pivot=[0, 24, -15])])
    m.bone("neck2", [0, 21, -17], "neck1", rotation=[18, 0, 0], cubes=[
        Cube([-3, 18, -25], [6, 6, 8], "scales"),
        Cube([-2.5, 17.5, -24], [5, 1, 7], "belly")])
    m.bone("head", [0, 21, -24], "neck2", rotation=[12, 0, 0], cubes=[
        Cube([-4.5, 17, -33], [9, 7, 9], "scales", detail=eyes("#ffb030", y_frac=0.25, spacing=0.36, size=1)),
        Cube([-3.5, 18, -40], [7, 4, 7], "scales"),
        Cube([-3, 22, -39], [6, 1, 5], "scales"),
        Cube([-4, 23, -31], [1.5, 1.5, 9], "horn", rotation=[28, 0, 8], pivot=[-3, 23.5, -31]),
        Cube([2.5, 23, -31], [1.5, 1.5, 9], "horn", rotation=[28, 0, -8], pivot=[3, 23.5, -31]),
        Cube([-5.5, 20, -27], [1, 1, 5], "horn", rotation=[10, 25, 0], pivot=[-5, 20.5, -27]),
        Cube([4.5, 20, -27], [1, 1, 5], "horn", rotation=[10, -25, 0], pivot=[5, 20.5, -27]),
    ])
    m.bone("jaw", [0, 18, -31], "head", cubes=[
        Cube([-3.5, 15.5, -40], [7, 2.5, 9], "belly"),
        Cube([-3, 18, -39.5], [6, 0.5, 7], "teeth"),
    ])

    # Ailes : bras, avant-bras (griffe) et doigt, chacun portant sa membrane
    for side, sign in (("left", 1), ("right", -1)):
        def c(origin, size, material, **kw):
            o, s = ([origin[0], origin[1], origin[2]], size) if sign > 0 else mirror((origin, size))
            return Cube(o, s, material, **kw)
        # Chaque membrane est un os a part : repliee, l'aile l'ecrase contre le bras (echelle en z)
        m.bone(f"wing_{side}", [7 * sign, 23, -6], "body", cubes=[c([7, 22, -7.5], [14, 2.5, 3], "scales")])
        m.bone(f"membrane_{side}", [7 * sign, 23, -4.5], f"wing_{side}", cubes=[c([7, 22.75, -4.5], [14, 0.5, 15], "membrane")])
        m.bone(f"wing_{side}_mid", [21 * sign, 23, -6], f"wing_{side}", cubes=[
            c([21, 22.25, -7], [15, 2, 2], "scales"),
            c([20.5, 24, -8], [2, 2, 2], "horn"),
        ])
        m.bone(f"membrane_{side}_mid", [21 * sign, 23, -5], f"wing_{side}_mid", cubes=[
            c([21, 22.8, -5], [15, 0.5, 17], "membrane")])
        m.bone(f"wing_{side}_tip", [36 * sign, 23, -6], f"wing_{side}_mid", cubes=[c([36, 22.5, -6.5], [14, 1.5, 1.5], "scales")])
        m.bone(f"membrane_{side}_tip", [36 * sign, 23, -5], f"wing_{side}_tip", cubes=[
            c([36, 22.85, -5], [14, 0.5, 13], "membrane_tip"),
            c([36, 22.6, 6], [8, 0.5, 6], "membrane_tip"),
        ])
        # Pattes arriere : cuisse, jambe, pied griffu
        m.bone(f"thigh_{side}", [6 * sign, 15, 6], "body", cubes=[c([4, 8, 3], [5, 8, 6], "scales")])
        m.bone(f"shin_{side}", [6.5 * sign, 9, 6], f"thigh_{side}", rotation=[-20, 0, 0], cubes=[c([4.5, 2, 4], [4, 7, 4], "scales")])
        m.bone(f"foot_{side}", [6.5 * sign, 2, 5], f"shin_{side}", rotation=[20, 0, 0], cubes=[
            c([4, 0, 1], [5, 2, 6], "scales"), c([4, 0, -0.5], [1, 1, 1.5], "horn"), c([6, 0, -0.5], [1, 1, 1.5], "horn"),
            c([8, 0, -0.5], [1, 1, 1.5], "horn")])

    # Queue en quatre segments, avec une nageoire en bout
    m.bone("tail1", [0, 19, 12], "body", cubes=[Cube([-4, 15, 12], [8, 7, 10], "scales"),
                                                Cube([-0.75, 22, 15], [1.5, 2, 2], "spine")])
    m.bone("tail2", [0, 18.5, 22], "tail1", cubes=[Cube([-3, 16, 22], [6, 5, 10], "scales"),
                                                   Cube([-0.75, 21, 25], [1.5, 1.5, 2], "spine")])
    m.bone("tail3", [0, 18, 32], "tail2", cubes=[Cube([-2, 16.5, 32], [4, 3.5, 10], "scales")])
    m.bone("tail4", [0, 18, 42], "tail3", cubes=[Cube([-1, 17, 42], [2, 2, 8], "scales"),
                                                 Cube([-4, 17.75, 46], [8, 0.5, 7], "membrane_tip")])

    save_model(m, {
        "scales": {"colors": ["#1e1c1c", "#2a2727", "#363232", "#433e3c", "#514a46"], "pattern": "scales",
                   "speckle": ("#ff7a2a", 0.035), "speckle_glow": True},
        "belly": {"colors": ["#6e6258", "#857766", "#9a8a76", "#ab9a84"], "pattern": "stripes"},
        "membrane": {"colors": ["#3a1e18", "#4a2820", "#5a3226", "#6a3c2c"], "pattern": "plates", "edge": False,
                     "speckle": ("#ff6a2a", 0.015), "speckle_glow": True},
        "membrane_tip": {"colors": ["#3a1e18", "#4a2820", "#5a3226"], "pattern": "tatters", "edge": False},
        "spine": {"colors": ["#ff5a1a", "#ff8a2a", "#ffc04a"], "glow": True, "edge": False},
        "horn": {"colors": ["#b8ac94", "#d0c4aa", "#e6dcc4"]},
        "teeth": {"colors": ["#f0ece0"], "edge": False},
        "harness": {"colors": ["#4a2c16", "#5a361c", "#6a4022"], "trim": "#c89a20"},
    })


# --------------------------------------------------------------------------------------------------
# Animations
# --------------------------------------------------------------------------------------------------

def frames(length, steps, fn):
    """Keyframes calculees : fn(phase 0..1) -> [x, y, z]."""
    return {f"{round(length * i / steps, 4)}": [round(v, 2) for v in fn(i / steps)] for i in range(steps + 1)}


def wing_pose(left, mid, tip, membrane=1.0):
    """Applique une pose (rotations de l'aile gauche) aux deux ailes, l'aile droite en miroir ; membrane < 1 la replie."""
    out = {}
    for side, sign in (("left", 1), ("right", -1)):
        for bone, rot in ((f"wing_{side}", left), (f"wing_{side}_mid", mid), (f"wing_{side}_tip", tip)):
            out[bone] = {"rotation": keys((0, [rot[0], rot[1] * sign, rot[2] * sign]))}
        for bone in (f"membrane_{side}", f"membrane_{side}_mid", f"membrane_{side}_tip"):
            out[bone] = {"scale": keys((0, [1, 1, membrane]))}
    return out


def flap_wings(length, amplitude, sweep=0.0):
    """Battement : l'epaule mene, l'avant-bras et le doigt suivent avec un retard ; au relevement le bout se replie."""
    out = {}
    w = 2 * math.pi
    for side, sign in (("left", 1), ("right", -1)):
        out[f"wing_{side}"] = {"rotation": frames(length, 16, lambda p: [
            0, sign * (-sweep - 6 * math.sin(w * p)), sign * (-amplitude * math.cos(w * p) - 5)])}
        out[f"wing_{side}_mid"] = {"rotation": frames(length, 16, lambda p: [
            0, sign * (-14 * max(0.0, math.sin(w * p))), sign * (-amplitude * 0.6 * math.cos(w * p - 0.7) + 4)])}
        out[f"wing_{side}_tip"] = {"rotation": frames(length, 16, lambda p: [
            0, sign * (-22 * max(0.0, math.sin(w * p - 0.4))), sign * (-amplitude * 0.5 * math.cos(w * p - 1.3) + 6)])}
    return out


def tail_wave(length, amplitude, axis=1, base=(0, 0, 0)):
    out = {}
    for i in range(1, 5):
        def fn(p, i=i):
            v = list(base)
            v[axis] += amplitude * (0.6 + 0.25 * i) * math.sin(2 * math.pi * p - i * 0.6)
            return v
        out[f"tail{i}"] = {"rotation": frames(length, 12, fn)}
    return out


def legs_walk(length, amplitude):
    out = {}
    for side, phase in (("left", 0.0), ("right", math.pi)):
        out[f"thigh_{side}"] = {"rotation": frames(length, 12, lambda p, ph=phase: [amplitude * math.sin(2 * math.pi * p + ph), 0, 0])}
        out[f"shin_{side}"] = {"rotation": frames(length, 12, lambda p, ph=phase: [
            -20 + amplitude * 0.6 * max(0.0, math.sin(2 * math.pi * p + ph + 1.2)), 0, 0])}
    return out


FOLDED = wing_pose([0, -80, -20], [0, 170, -10], [0, -170, 0], membrane=0.12)


def generate_animations():
    neck_idle = {"neck1": {"rotation": frames(4.0, 8, lambda p: [-28 + 4 * math.sin(2 * math.pi * p), 0, 0])},
                 "head": {"rotation": frames(4.0, 8, lambda p: [12, 14 * math.sin(2 * math.pi * p * 2), 0])},
                 "body": {"scale": frames(4.0, 8, lambda p: [1, 1 + 0.025 * math.sin(2 * math.pi * p * 2), 1])}}
    idle = dict(FOLDED)
    idle.update(tail_wave(4.0, 6))
    idle.update(neck_idle)

    walk = dict(FOLDED)
    walk.update(legs_walk(1.2, 32))
    walk.update(tail_wave(1.2, 8))
    walk["body"] = {"position": frames(1.2, 12, lambda p: [0, 0.5 * abs(math.sin(2 * math.pi * p)), 0])}
    walk["neck1"] = {"rotation": frames(1.2, 12, lambda p: [-28 + 5 * math.sin(4 * math.pi * p), 0, 0])}

    fly = flap_wings(1.1, 38, sweep=4)
    fly.update(tail_wave(1.1, 6, axis=0))
    fly["body"] = {"position": frames(1.1, 16, lambda p: [0, 1.8 * math.cos(2 * math.pi * p), 0]),
                   "rotation": frames(1.1, 16, lambda p: [2 * math.sin(2 * math.pi * p), 0, 0])}
    fly["neck1"] = {"rotation": frames(1.1, 16, lambda p: [-12 - 4 * math.cos(2 * math.pi * p), 0, 0])}
    fly["neck2"] = {"rotation": keys((0, [8, 0, 0]))}
    for side in ("left", "right"):
        fly[f"thigh_{side}"] = {"rotation": keys((0, [55, 0, 0]))}
        fly[f"shin_{side}"] = {"rotation": keys((0, [30, 0, 0]))}
        fly[f"foot_{side}"] = {"rotation": keys((0, [40, 0, 0]))}

    glide = {}
    for side, sign in (("left", 1), ("right", -1)):
        glide[f"wing_{side}"] = {"rotation": frames(3.0, 8, lambda p, s=sign: [0, s * -4, s * (-6 + 3 * math.sin(2 * math.pi * p))])}
        glide[f"wing_{side}_mid"] = {"rotation": frames(3.0, 8, lambda p, s=sign: [0, 0, s * (4 + 2 * math.sin(2 * math.pi * p - 0.6))])}
        glide[f"wing_{side}_tip"] = {"rotation": frames(3.0, 8, lambda p, s=sign: [0, s * -6, s * (8 + 3 * math.sin(2 * math.pi * p - 1.2))])}
        glide[f"thigh_{side}"] = {"rotation": keys((0, [55, 0, 0]))}
        glide[f"shin_{side}"] = {"rotation": keys((0, [30, 0, 0]))}
        glide[f"foot_{side}"] = {"rotation": keys((0, [40, 0, 0]))}
    glide.update(tail_wave(3.0, 4))
    glide["neck1"] = {"rotation": keys((0, [-12, 0, 0]))}
    glide["body"] = {"rotation": frames(3.0, 8, lambda p: [0, 0, 3 * math.sin(2 * math.pi * p)])}

    sit = dict(FOLDED)
    sit["body"] = {"position": keys((0, [0, -6, 0]))}
    for side in ("left", "right"):
        sit[f"thigh_{side}"] = {"rotation": keys((0, [-70, 0, 0]))}
        sit[f"shin_{side}"] = {"rotation": keys((0, [60, 0, 0]))}
        sit[f"foot_{side}"] = {"rotation": keys((0, [10, 0, 0]))}
    sit["neck1"] = {"rotation": keys((0, [-5, 0, 0]))}
    sit["head"] = {"rotation": keys((0, [25, 0, 0]))}
    sit.update(tail_wave(5.0, 4))

    roar = dict(FOLDED)
    roar["jaw"] = {"rotation": keys((0, [0, 0, 0]), (0.3, [35, 0, 0]), (1.2, [35, 0, 0]), (1.5, [0, 0, 0]))}
    roar["neck1"] = {"rotation": keys((0, [-28, 0, 0]), (0.3, [-50, 0, 0]), (1.2, [-50, 0, 0]), (1.5, [-28, 0, 0]))}
    roar["head"] = {"rotation": keys((0, [12, 0, 0]), (0.3, [-10, 0, 0]), (1.2, [-10, 0, 0]), (1.5, [12, 0, 0]))}

    save_animations(NAME, {"idle": anim(4.0, idle), "walk": anim(1.2, walk), "fly": anim(1.1, fly), "glide": anim(3.0, glide),
                           "sit": anim(5.0, sit), "roar": anim(1.5, roar, loop=False)})


def generate():
    build()
    generate_animations()
