"""Structures 1.3.4 : salles thematiques des variantes de donjons, et indices d'approche autour des donjons.

Variantes : chaque grand donjon existe en plusieurs gabarits (main, variant_1, variant_2, ...). Le noyau (arene du
boss, coffre-fort scelle, escaliers, acces) est identique ; les salles annexes recoivent un theme tire au sort dans un
pool propre au donjon (bibliotheque, armurerie, ossuaire, prison, sanctuaire, forge) et leurs generateurs changent de
creature. Le pool de gabarits du donjon en tire un a chaque generation : deux mondes n'ont pas les memes donjons.

Les meubles ne sont poses que contre un mur et sur un sol plein (jamais sur une marche, devant une ouverture ou sur
une case reservee) : les passages, portes et escaliers restent libres.
"""
from .common import data_path
from .nbt import Byte, TypedList, TAG_STRING
from .structures import AIR, CRYPT_FLOOR, CRYPT_WALL, MAGMA_FLOOR, MAGMA_WALL, ONYX_FLOOR, ONYX_WALL, Dungeon

VARIANTS = 4  # gabarit "main" (le donjon historique) + 3 variantes

DIRS = {(1, 0): "east", (-1, 0): "west", (0, 1): "south", (0, -1): "north"}
OPPOSITE = {"east": "west", "west": "east", "south": "north", "north": "south"}

CRYPT_THEMES = ("ossuary", "library", "prison", "shrine")
ONYX_THEMES = ("library", "armory", "prison", "shrine")
MAGMA_THEMES = ("forge", "shrine", "prison", "armory")

CRYPT_MOBS = ("withered_knight", "ash_wraith", "minecraft:wither_skeleton")
ONYX_MOBS = ("ash_wraith", "ember_stalker", "withered_knight")
MAGMA_MOBS = ("minecraft:magma_cube", "ember_stalker", "minecraft:blaze")


def template_file(variant):
    return "main" if variant == 0 else f"variant_{variant}"


def _name(d, pos):
    entry = d.blocks.get(pos)
    return entry[0] if entry else None


def _solid(d, pos):
    x, y, z = pos
    if not (0 <= x < d.size[0] and 0 <= y < d.size[1] and 0 <= z < d.size[2]):
        return True
    name = _name(d, pos)
    return name is not None and name not in (AIR, "minecraft:lava") and "fire" not in name


def _free(d, pos):
    name = _name(d, pos)
    return name is None or name == AIR


def furnish(d, x0, y, z0, x1, z1, top, theme, loot, pedestal, keep=()):
    """Meuble une salle (sol a y, interieur de y + 1 a top) selon un theme, contre les murs uniquement."""
    keep = set(keep)
    barrels = 0
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            floor = (x, y, z)
            if (x, z) in keep or not _solid(d, floor) or "stairs" in (_name(d, floor) or ""):
                continue
            if not _free(d, (x, y + 1, z)) or not _free(d, (x, y + 2, z)):
                continue
            walls, opening = [], False
            for (dx, dz), direction in DIRS.items():
                nx, nz = x + dx, z + dz
                if x0 <= nx <= x1 and z0 <= nz <= z1:
                    continue
                if _solid(d, (nx, y + 1, nz)) and _solid(d, (nx, y + 2, nz)):
                    walls.append(direction)
                else:
                    opening = True
            if opening or not walls:
                continue
            facing = OPPOSITE[walls[0]]
            barrels += _decor(d, x, y, z, top, theme, facing, loot, pedestal, barrels)
    # Toiles et chaines au plafond, partout dans la salle
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if (x, z) in keep or not _free(d, (x, top, z)) or not _solid(d, (x, top + 1, z)):
                continue
            roll = d.rng.random()
            if roll < 0.05:
                d.set(x, top, z, "minecraft:cobweb")
            elif roll < 0.09 and theme in ("prison", "armory", "forge") and _free(d, (x, top - 1, z)):
                d.set(x, top, z, "minecraft:chain", axis="y", waterlogged="false")
                d.set(x, top - 1, z, "minecraft:chain", axis="y", waterlogged="false")


def _decor(d, x, y, z, top, theme, facing, loot, pedestal, barrels):
    """Pose un element du theme en (x, y + 1, z) ; renvoie 1 si un tonneau a ete pose."""
    r = d.rng.random()
    up = y + 1
    height = max(1, min(3, top - y - 1))
    if theme == "library":
        if r < 0.7:
            for k in range(height):
                d.set(x, up + k, z, "minecraft:bookshelf")
        elif r < 0.8:
            d.set(x, up, z, "minecraft:lectern", facing=facing, has_book="false", powered="false")
        elif r < 0.9:
            d.set(x, up, z, pedestal)
            d.set(x, up + 1, z, "minecraft:candle", candles=str(1 + d.rng.randrange(3)), lit="true", waterlogged="false")
    elif theme == "armory":
        if r < 0.2:
            d.set(x, up, z, "minecraft:anvil", facing=facing)
        elif r < 0.32:
            d.set(x, up, z, "minecraft:smithing_table")
        elif r < 0.44:
            d.set(x, up, z, "minecraft:grindstone", face="floor", facing=facing)
        elif r < 0.6:
            # Ratelier : poteau de barreaux
            for k in range(min(2, height)):
                d.set(x, up + k, z, "minecraft:iron_bars", north="false", south="false", east="false", west="false",
                      waterlogged="false")
        elif r < 0.68 and barrels == 0:
            d.barrel(x, up, z, loot)
            return 1
    elif theme == "ossuary":
        if r < 0.45:
            stack = 1 + d.rng.randrange(min(2, height))
            for k in range(stack):
                d.set(x, up + k, z, "minecraft:bone_block", axis="y")
            if up + stack <= top:
                d.skull(x, up + stack, z, wither=d.rng.random() < 0.15)
        elif r < 0.7:
            d.skull(x, up, z)
        elif r < 0.78:
            d.lantern(x, up, z)
    elif theme == "prison":
        if r < 0.5:
            for k in range(height):
                d.set(x, up + k, z, "minecraft:iron_bars", north="false", south="false", east="false", west="false",
                      waterlogged="false")
        elif r < 0.7:
            d.skull(x, up, z)
        elif r < 0.78 and barrels == 0:
            d.barrel(x, up, z, loot)
            return 1
    elif theme == "shrine":
        if r < 0.4:
            d.set(x, up, z, "minecraft:candle", candles=str(1 + d.rng.randrange(4)), lit="true", waterlogged="false")
        elif r < 0.6:
            d.set(x, up, z, pedestal)
            d.set(x, up + 1, z, "minecraft:candle", candles=str(1 + d.rng.randrange(4)), lit="true", waterlogged="false")
        elif r < 0.72:
            d.set(x, y, z, "minecraft:soul_soil")
            d.set(x, up, z, "minecraft:soul_fire")
    elif theme == "forge":
        if r < 0.18:
            d.set(x, up, z, "minecraft:blast_furnace", facing=facing, lit="false")
        elif r < 0.3:
            d.set(x, up, z, "minecraft:smoker", facing=facing, lit="false")
        elif r < 0.42:
            d.set(x, up, z, "minecraft:lava_cauldron")
        elif r < 0.54:
            d.set(x, up, z, "minecraft:anvil", facing=facing)
        elif r < 0.7:
            d.set(x, y, z, "minecraft:magma_block")
        elif r < 0.76 and barrels == 0:
            d.barrel(x, up, z, loot)
            return 1
    return 0


# ------------------------------------------------------------------------------------------------
# Indices d'approche : ruines, restes d'explorateurs et panneaux disperses a quelques dizaines de blocs du donjon
# ------------------------------------------------------------------------------------------------

APPROACH = {
    # donjon : (mur, sol, pilier, lanterne, panneau)
    "onyx_citadel": (ONYX_WALL, ONYX_FLOOR, "onyx_bricks", "onyx_lantern", "minecraft:crimson_sign"),
    "infernal_sanctum": (MAGMA_WALL, MAGMA_FLOOR, "magma_bricks", "minecraft:lantern", "minecraft:crimson_sign"),
    "withered_catacombs": (CRYPT_WALL, CRYPT_FLOOR, "minecraft:polished_blackstone_bricks", "minecraft:soul_lantern",
                           "minecraft:warped_sign"),
}
APPROACH_KINDS = ("ruin", "remains", "signpost")


def _sign_text(dungeon, line_count=4):
    lines = [f'{{"translate":"sign.netheryx.{dungeon}.{i}"}}' for i in range(1, line_count + 1)]
    side = {"messages": TypedList(TAG_STRING, lines), "color": "white", "has_glowing_text": Byte(1)}
    empty = {"messages": TypedList(TAG_STRING, ['{"text":""}'] * 4), "color": "black", "has_glowing_text": Byte(0)}
    return {"id": "minecraft:sign", "front_text": side, "back_text": empty, "is_waxed": Byte(1)}


def approach_ruin(dungeon):
    wall, floor, pillar, lantern, _ = APPROACH[dungeon]
    d = Dungeon(7, 7, 7, f"{dungeon}_ruin")
    d.box(0, 0, 0, 6, 0, 6, floor)
    for (x, z) in ((1, 1), (5, 1), (1, 5), (5, 5)):
        if d.rng.random() < 0.75:
            height = 1 + d.rng.randrange(5)
            for y in range(1, height + 1):
                d.set(x, y, z, d.pick(wall))
    # Gravats et une lanterne renversee au pied du pilier le plus haut
    for _ in range(6):
        d.set(d.rng.randrange(7), 1, d.rng.randrange(7), d.pick(wall))
    d.lantern(3, 1, 3, lantern)
    d.set(3, 1, 4, pillar)
    return d


def approach_remains(dungeon):
    wall, floor, _, lantern, _ = APPROACH[dungeon]
    d = Dungeon(5, 3, 5, f"{dungeon}_remains")
    d.box(0, 0, 0, 4, 0, 4, floor)
    d.set(1, 1, 2, "minecraft:bone_block", axis="x")
    d.set(2, 1, 2, "minecraft:bone_block", axis="x")
    d.skull(3, 1, 2)
    d.set(1, 1, 1, "minecraft:campfire", facing="north", lit="false", signal_fire="false", waterlogged="false")
    d.barrel(3, 1, 3, "small_treasure")
    d.set(0, 1, 4, "minecraft:cobweb")
    return d


def approach_signpost(dungeon):
    wall, floor, pillar, lantern, sign = APPROACH[dungeon]
    d = Dungeon(3, 4, 3, f"{dungeon}_signpost")
    d.box(0, 0, 0, 2, 0, 2, floor)
    d.set(1, 1, 1, pillar)
    d.set(1, 2, 1, sign, rotation="8", waterlogged="false", nbt=_sign_text(dungeon))
    d.lantern(1, 1, 2, lantern)
    return d


def generate_approach():
    for dungeon in APPROACH:
        for kind, build in (("ruin", approach_ruin), ("remains", approach_remains), ("signpost", approach_signpost)):
            build(dungeon).save(f"{dungeon}_approach", kind)


def save_check():
    """Garde-fou : les fichiers de variantes doivent exister apres la generation."""
    for dungeon in APPROACH:
        for variant in range(VARIANTS):
            path = data_path("structures", dungeon, f"{template_file(variant)}.nbt")
            assert path.is_file(), path


def generate_gametest():
    """Aire des GameTests (src/main/java/com/netheryx/gametest) : un plancher et 25 blocs de vide au-dessus."""
    d = Dungeon(7, 26, 7, "gametest")
    d.box(0, 0, 0, 6, 0, 6, "minecraft:polished_blackstone")
    d.air(0, 1, 0, 6, 25, 6)
    d.save("gametest", "column")
