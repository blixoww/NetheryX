"""Gabarits NBT des structures, construits bloc par bloc (y = 0 correspond au sol du Nether)."""
from .common import MOD_ID, data_path, mid
from .nbt import Byte, Double, Short, TypedList, TAG_COMPOUND, write_nbt

DATA_VERSION = 3465  # Minecraft 1.20.1


class Template:
    def __init__(self, sx, sy, sz):
        self.size = (sx, sy, sz)
        self.blocks = {}
        self.block_nbt = {}
        self.entities = []

    def set(self, x, y, z, name, nbt=None, **props):
        if 0 <= x < self.size[0] and 0 <= y < self.size[1] and 0 <= z < self.size[2]:
            self.blocks[(x, y, z)] = (mid(name), tuple(sorted((k, str(v).lower()) for k, v in props.items())))
            if nbt is not None:
                self.block_nbt[(x, y, z)] = nbt
            else:
                self.block_nbt.pop((x, y, z), None)

    def fill(self, x0, y0, z0, x1, y1, z1, name, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, name, **props)

    def hollow(self, x0, y0, z0, x1, y1, z1, name, **props):
        """Murs d'une boite (sans sol ni plafond)."""
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, z0, name, **props)
                self.set(x, y, z1, name, **props)
            for z in range(z0, z1 + 1):
                self.set(x0, y, z, name, **props)
                self.set(x1, y, z, name, **props)

    def chest(self, x, y, z, loot, facing="north"):
        self.set(x, y, z, "minecraft:chest", nbt={"id": "minecraft:chest", "LootTable": mid(f"chests/{loot}")},
                 facing=facing, type="single", waterlogged="false")

    def barrel(self, x, y, z, loot):
        self.set(x, y, z, "minecraft:barrel", nbt={"id": "minecraft:barrel", "LootTable": mid(f"chests/{loot}")},
                 facing="up", open="false")

    def spawner(self, x, y, z, entity):
        data = {"entity": {"id": mid(entity)}}  # mid() garde les identifiants deja qualifies
        self.set(x, y, z, "minecraft:spawner", nbt={
            "id": "minecraft:mob_spawner", "SpawnData": data,
            "SpawnPotentials": TypedList(TAG_COMPOUND, [{"weight": 1, "data": {"entity": {"id": mid(entity)}}}]),
            "Delay": Short(20), "MinSpawnDelay": Short(200), "MaxSpawnDelay": Short(600), "SpawnCount": Short(2),
            "MaxNearbyEntities": Short(4), "RequiredPlayerRange": Short(16), "SpawnRange": Short(4)})

    def entity(self, x, y, z, entity_id, extra=None):
        nbt = {"id": mid(entity_id), "PersistenceRequired": Byte(1)}
        nbt.update(extra or {})
        self.entities.append({"pos": [Double(x), Double(y), Double(z)], "blockPos": [int(x), int(y), int(z)], "nbt": nbt})

    def save(self, name):
        palette, palette_index, blocks = [], {}, []
        for pos in sorted(self.blocks, key=lambda p: (p[1], p[2], p[0])):
            key = self.blocks[pos]
            if key not in palette_index:
                palette_index[key] = len(palette)
                entry = {"Name": key[0]}
                if key[1]:
                    entry["Properties"] = {k: v for k, v in key[1]}
                palette.append(entry)
            block = {"pos": list(pos), "state": palette_index[key]}
            if pos in self.block_nbt:
                block["nbt"] = self.block_nbt[pos]
            blocks.append(block)
        root = {
            "DataVersion": DATA_VERSION,
            "size": list(self.size),
            "palette": palette,
            "blocks": blocks,
            "entities": TypedList(TAG_COMPOUND, self.entities),
        }
        write_nbt(data_path("structures", name, "main.nbt"), root)


# ------------------------------------------------------------------------------------------------
# Outils de construction des donjons
# ------------------------------------------------------------------------------------------------
import random

ONYX_WALL = [("onyx_bricks", 70), ("polished_onyx", 8), ("minecraft:polished_blackstone_bricks", 14),
             ("minecraft:cracked_polished_blackstone_bricks", 8)]
ONYX_FLOOR = [("polished_onyx", 55), ("onyx_bricks", 30), ("minecraft:polished_blackstone", 15)]
MAGMA_WALL = [("magma_bricks", 72), ("minecraft:red_nether_bricks", 14), ("minecraft:nether_bricks", 10),
              ("minecraft:cracked_nether_bricks", 4)]
MAGMA_FLOOR = [("minecraft:polished_blackstone", 50), ("minecraft:blackstone", 25), ("magma_bricks", 25)]
CRYPT_WALL = [("minecraft:polished_blackstone_bricks", 50), ("minecraft:cracked_polished_blackstone_bricks", 25),
              ("minecraft:blackstone", 12), ("minecraft:gilded_blackstone", 1), ("minecraft:polished_blackstone", 12)]
CRYPT_FLOOR = [("minecraft:polished_blackstone", 45), ("minecraft:polished_blackstone_bricks", 35),
               ("minecraft:soul_soil", 10), ("minecraft:smooth_basalt", 10)]
AIR = "minecraft:air"


class Dungeon(Template):
    def __init__(self, sx, sy, sz, seed):
        super().__init__(sx, sy, sz)
        self.rng = random.Random(seed)

    def pick(self, palette):
        if isinstance(palette, str):
            return palette
        total = sum(w for _, w in palette)
        roll = self.rng.uniform(0, total)
        for block, weight in palette:
            roll -= weight
            if roll <= 0:
                return block
        return palette[-1][0]

    def box(self, x0, y0, z0, x1, y1, z1, palette, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, self.pick(palette), **props)

    def air(self, x0, y0, z0, x1, y1, z1):
        self.box(x0, y0, z0, x1, y1, z1, AIR)

    def walls(self, x0, y0, z0, x1, y1, z1, palette):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, z0, self.pick(palette))
                self.set(x, y, z1, self.pick(palette))
            for z in range(z0, z1 + 1):
                self.set(x0, y, z, self.pick(palette))
                self.set(x1, y, z, self.pick(palette))

    def hall(self, x0, y0, z0, x1, y1, z1, wall, floor, ceiling=None):
        """Piece fermee : sol, murs, plafond et interieur vide."""
        self.box(x0, y0, z0, x1, y0, z1, floor)
        self.walls(x0, y0 + 1, z0, x1, y1 - 1, z1, wall)
        self.box(x0, y1, z0, x1, y1, z1, ceiling or wall)
        self.air(x0 + 1, y0 + 1, z0 + 1, x1 - 1, y1 - 1, z1 - 1)

    def pillar(self, x, z, y0, y1, block="minecraft:polished_basalt", cap=None):
        for y in range(y0, y1 + 1):
            if "basalt" in block:
                self.set(x, y, z, block, axis="y")
            else:
                self.set(x, y, z, block)
        if cap:
            self.set(x, y1 + 1, z, cap)

    def lantern(self, x, y, z, block="minecraft:soul_lantern", hanging=False):
        self.set(x, y, z, block, hanging=str(hanging).lower(), waterlogged="false")

    def hanging_lantern(self, x, y_ceiling, z, length=2, block="minecraft:soul_lantern"):
        for y in range(y_ceiling - length, y_ceiling):
            self.set(x, y, z, "minecraft:chain", axis="y", waterlogged="false")
        self.lantern(x, y_ceiling - length - 1, z, block, hanging=True)

    def stair(self, x, y, z, block, facing, half="bottom"):
        self.set(x, y, z, block, facing=facing, half=half, shape="straight", waterlogged="false")

    def skull(self, x, y, z, wither=False):
        self.set(x, y, z, "minecraft:wither_skeleton_skull" if wither else "minecraft:skeleton_skull",
                 rotation=str(self.rng.randrange(16)))

    def seal(self, x0, y0, z0, x1, y1, z1):
        self.box(x0, y0, z0, x1, y1, z1, "guardian_seal")

    def guard(self, x, y, z, entity):
        self.entity(x + 0.5, y, z + 0.5, entity)


# ------------------------------------------------------------------------------------------------
# Citadelle d'onyx : remparts, tours a generateurs, donjon, arene de la Sentinelle, salle au tresor
# ------------------------------------------------------------------------------------------------

def onyx_citadel():
    S = 45
    d = Dungeon(S, 32, S, "onyx_citadel")
    d.box(0, 0, 0, S - 1, 0, S - 1, ONYX_FLOOR)
    d.air(2, 1, 2, S - 3, 13, S - 3)

    # Remparts (epaisseur 2) et creneaux
    for t in range(2):
        d.walls(t, 1, t, S - 1 - t, 9, S - 1 - t, ONYX_WALL)
    for k in range(0, S, 2):
        for (x, z) in ((k, 0), (k, S - 1), (0, k), (S - 1, k)):
            d.set(x, 10, z, "onyx_brick_wall", up="true")

    # Quatre tours : generateur de spectres en bas, coffre a l'etage
    for (tx, tz) in ((0, 0), (S - 9, 0), (0, S - 9), (S - 9, S - 9)):
        d.hall(tx, 0, tz, tx + 8, 15, tz + 8, ONYX_WALL, ONYX_FLOOR)
        d.box(tx + 1, 7, tz + 1, tx + 7, 7, tz + 7, "polished_onyx")
        d.air(tx + 6, 7, tz + 7, tx + 6, 7, tz + 7)
        for y in range(1, 8):
            d.set(tx + 6, y, tz + 7, "minecraft:ladder", facing="north", waterlogged="false")
        d.spawner(tx + 3, 1, tz + 3, "ash_wraith")
        d.chest(tx + 2, 8, tz + 2, "onyx_citadel", facing="south")
        if (tx + tz) % 2 == 0:
            d.guard(tx + 4, 8, tz + 2, "mimic")
        d.lantern(tx + 4, 8, tz + 4, "onyx_lantern")
        d.hanging_lantern(tx + 4, 7, tz + 1, 1)
        for k in range(0, 9, 2):
            for (x, z) in ((tx + k, tz), (tx + k, tz + 8), (tx, tz + k), (tx + 8, tz + k)):
                d.set(x, 16, z, "onyx_brick_wall", up="true")
        d.set(tx + 4, 16, tz + 4, "onyx_lantern", hanging="false", waterlogged="false")
        # Porte vers la cour
        door_x = tx + 8 if tx == 0 else tx
        d.air(door_x, 1, tz + 3, door_x, 3, tz + 5)

    # Porte d'entree (sud) et ses gardes
    c = S // 2
    d.air(c - 2, 1, S - 2, c + 2, 5, S - 1)
    for x in (c - 3, c + 3):
        d.pillar(x, S - 1, 1, 7, cap="onyx_lantern")
    d.box(c - 3, 6, S - 2, c + 3, 6, S - 1, "polished_onyx")
    d.guard(c - 4, 1, S - 5, "withered_knight")
    d.guard(c + 4, 1, S - 5, "withered_knight")

    # Cour : allee, bassins de lave, lampadaires, champignons
    d.box(c - 2, 0, 33, c + 2, 0, S - 3, "polished_onyx")
    for bx in (3, S - 10):
        d.box(bx, 1, 30, bx + 6, 1, 35, "onyx_brick_slab", type="bottom", waterlogged="false")
        d.box(bx + 1, 1, 31, bx + 5, 1, 34, AIR)
        d.box(bx + 1, 0, 31, bx + 5, 0, 34, "minecraft:lava", level="0")
    for (x, z) in ((c - 4, 36), (c + 4, 36), (c - 4, 40), (c + 4, 40), (5, 20), (S - 6, 20), (c, 3)):
        d.box(x, 1, z, x, 3, z, "onyx_brick_wall", up="true")
        d.lantern(x, 4, z, "onyx_lantern")
    for (x, z) in ((4, 14), (S - 5, 14), (4, 26), (S - 5, 26), (14, 4), (30, 4)):
        d.set(x, 1, z, "onyx_cluster", facing="up", waterlogged="false")
    d.spawner(c, 1, 4, "ember_stalker")

    # Donjon central
    kx0, kx1, kz0, kz1 = 11, 33, 6, 32
    d.hall(kx0, 0, kz0, kx1, 16, kz1, ONYX_WALL, ONYX_FLOOR)
    for k in range(kx0, kx1 + 1, 2):
        d.set(k, 17, kz0, "onyx_brick_wall", up="true")
        d.set(k, 17, kz1, "onyx_brick_wall", up="true")
    for k in range(kz0, kz1 + 1, 2):
        d.set(kx0, 17, k, "onyx_brick_wall", up="true")
        d.set(kx1, 17, k, "onyx_brick_wall", up="true")
    for (x, z) in ((kx0, kz0), (kx1, kz0), (kx0, kz1), (kx1, kz1)):
        d.pillar(x, z, 1, 17, cap="onyx_lantern")
    d.air(c - 1, 1, kz1, c + 1, 4, kz1)
    d.stair(c - 2, 4, kz1, "onyx_brick_stairs", "west", "top")
    d.stair(c + 2, 4, kz1, "onyx_brick_stairs", "east", "top")

    # Grand hall : piliers, generateurs de traqueurs, gardes
    hz0 = 27
    d.walls(kx0, 1, hz0 - 1, kx1, 15, hz0 - 1, ONYX_WALL)
    d.box(kx0 + 1, 1, hz0 - 1, kx1 - 1, 15, hz0 - 1, ONYX_WALL)
    d.air(c - 1, 1, hz0 - 1, c + 1, 5, hz0 - 1)
    for x in (14, 18, 26, 30):
        d.pillar(x, 29, 1, 6, cap="onyx_lantern")
    d.box(c - 1, 0, hz0, c + 1, 0, kz1 - 1, "onyx_block")
    d.spawner(kx0 + 1, 1, kz1 - 1, "ember_stalker")
    d.spawner(kx1 - 1, 1, kz1 - 1, "ember_stalker")
    d.guard(c - 3, 1, 28, "withered_knight")
    d.guard(c + 3, 1, 28, "withered_knight")

    # Arene de la Sentinelle
    az0, az1 = 12, hz0 - 2
    for (x, z) in ((15, 14), (29, 14), (15, 23), (29, 23)):
        d.box(x, 1, z, x + 1, 12, z + 1, "polished_onyx")
        d.set(x, 13, z, "ember_lamp")
    for x in range(kx0 + 1, kx1):
        for z in range(az0, az1 + 1):
            if (x - c) ** 2 + (z - 18.5) ** 2 <= 20:
                d.set(x, 0, z, "onyx_block")
            elif (x - c) ** 2 + (z - 18.5) ** 2 <= 30:
                d.set(x, 0, z, "minecraft:crying_obsidian")
    for (x, z) in ((kx0 + 1, az0), (kx1 - 1, az0), (kx0 + 1, az1), (kx1 - 1, az1)):
        d.set(x, 1, z, "onyx_cluster", facing="up", waterlogged="false")
    for x in (16, c, 28):
        d.hanging_lantern(x, 16, 18, 3, "onyx_lantern")
    d.guard(c, 1, 18, "onyx_sentinel")

    # Balcons au-dessus de l'arene : des spectres y apparaissent et harcelent les joueurs d'en haut
    for (bx0, bx1, rail) in ((kx0 + 1, kx0 + 3, kx0 + 3), (kx1 - 3, kx1 - 1, kx1 - 3)):
        d.box(bx0, 6, az0, bx1, 6, az1, "polished_onyx")
        for z in range(az0, az1 + 1):
            d.set(rail, 7, z, "onyx_brick_wall", up="true")
        d.spawner((bx0 + bx1) // 2, 7, (az0 + az1) // 2, "ash_wraith")
        ladder_x = bx0 if bx0 == kx0 + 1 else bx1
        d.air(ladder_x, 6, az1, ladder_x, 6, az1)
        for y in range(1, 7):
            d.set(ladder_x, y, az1, "minecraft:ladder", facing="north", waterlogged="false")
        d.hanging_lantern((bx0 + bx1) // 2, 15, az0 + 2, 2, "onyx_lantern")

    # Salle scellee : un puits mene a la tour du tresor, au-dessus du donjon
    d.box(kx0 + 1, 1, kz0 + 1, kx1 - 1, 15, az0 - 1, ONYX_WALL)
    d.air(17, 1, kz0 + 1, 27, 5, az0 - 2)
    d.box(16, 0, kz0 + 1, 28, 0, az0 - 2, "minecraft:gilded_blackstone")
    d.seal(c - 2, 1, az0 - 1, c + 2, 4, az0 - 1)
    for x in (17, 27):
        d.lantern(x, 1, kz0 + 1, "onyx_lantern")
    shaft_x = 18
    d.air(shaft_x, 6, kz0 + 1, shaft_x, 16, kz0 + 1)
    for y in range(1, 17):
        d.set(shaft_x, y, kz0 + 1, "minecraft:ladder", facing="south", waterlogged="false")

    # Tour du tresor : posee sur le toit du donjon, fenetres, creneaux, salle aux deux coffres
    tx0, tx1, tz0, tz1, ty0, ty1 = 16, 28, kz0, kz0 + 7, 16, 27
    d.walls(tx0, ty0 + 1, tz0, tx1, ty1 - 1, tz1, ONYX_WALL)
    d.box(tx0, ty1, tz0, tx1, ty1, tz1, "polished_onyx")
    d.air(tx0 + 1, ty0 + 1, tz0 + 1, tx1 - 1, ty1 - 1, tz1 - 1)
    d.box(tx0 + 1, ty0, tz0 + 1, tx1 - 1, ty0, tz1 - 1, "minecraft:gilded_blackstone")
    d.set(shaft_x, ty0, kz0 + 1, "minecraft:ladder", facing="south", waterlogged="false")
    for k in range(tx0, tx1 + 1, 2):
        d.set(k, ty1 + 1, tz0, "onyx_brick_wall", up="true")
        d.set(k, ty1 + 1, tz1, "onyx_brick_wall", up="true")
    for k in range(tz0, tz1 + 1, 2):
        d.set(tx0, ty1 + 1, k, "onyx_brick_wall", up="true")
        d.set(tx1, ty1 + 1, k, "onyx_brick_wall", up="true")
    for (x, z) in ((tx0, tz0), (tx1, tz0), (tx0, tz1), (tx1, tz1)):
        d.pillar(x, z, ty0 + 1, ty1 + 1, cap="onyx_lantern")
    for x in (tx0 + 3, c, tx1 - 3):
        d.set(x, ty0 + 3, tz1, "minecraft:iron_bars", north="false", south="false", east="true", west="true", waterlogged="false")
        d.set(x, ty0 + 4, tz1, "minecraft:iron_bars", north="false", south="false", east="true", west="true", waterlogged="false")
    for x in (c - 2, c + 2):
        d.chest(x, ty0 + 1, tz1 - 1, "onyx_citadel_vault", facing="north")
    d.set(c, ty0 + 1, tz1 - 1, "onyx_cluster", facing="up", waterlogged="false")
    d.hanging_lantern(c, ty1, tz0 + 3, 2, "onyx_lantern")
    d.save("onyx_citadel")


# ------------------------------------------------------------------------------------------------
# Sanctuaire infernal : douves de lave, cour des blazes, temple, arene du Sorcier, tresor scelle
# ------------------------------------------------------------------------------------------------

def infernal_sanctum():
    S = 41
    c = S // 2
    d = Dungeon(S, 31, S, "infernal_sanctum")
    for x in range(S):
        for z in range(S):
            edge = min(x, z, S - 1 - x, S - 1 - z)
            if edge == 0:
                d.set(x, 0, z, d.pick(MAGMA_WALL))
                d.set(x, 1, z, "magma_brick_wall", up="true")
            elif edge <= 3:
                d.set(x, 0, z, "minecraft:lava", level="0")
            else:
                d.set(x, 0, z, d.pick(MAGMA_FLOOR))
            for y in range(2 if edge == 0 else 1, 15):
                d.set(x, y, z, AIR)
    # Pont d'entree (sud)
    d.box(c - 2, 0, S - 4, c + 2, 0, S - 1, "magma_bricks")
    d.air(c - 2, 1, S - 1, c + 2, 1, S - 1)
    for z in range(S - 4, S):
        d.set(c - 3, 1, z, "magma_brick_wall", up="true")
        d.set(c + 3, 1, z, "magma_brick_wall", up="true")
    d.set(c - 3, 2, S - 4, "minecraft:lantern", hanging="false", waterlogged="false")
    d.set(c + 3, 2, S - 4, "minecraft:lantern", hanging="false", waterlogged="false")

    # Cour exterieure : autels a blazes, braseros
    for (x, z) in ((7, 7), (S - 8, 7)):
        d.box(x - 1, 1, z - 1, x + 1, 1, z + 1, "magma_bricks")
        d.spawner(x, 2, z, "minecraft:blaze")
    for (x, z) in ((6, 20), (S - 7, 20), (6, S - 8), (S - 7, S - 8)):
        d.pillar(x, z, 1, 4, "magma_bricks")
        d.set(x, 5, z, "minecraft:netherrack")
        d.set(x, 6, z, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false")
    d.guard(c - 5, 1, S - 7, "ember_stalker")
    d.guard(c + 5, 1, S - 7, "ember_stalker")

    # Temple
    tx0, tx1, tz0, tz1 = 9, 31, 8, 33
    d.hall(tx0, 0, tz0, tx1, 14, tz1, MAGMA_WALL, MAGMA_FLOOR)
    for step, inset in enumerate((1, 3, 5)):
        d.box(tx0 + inset, 15 + step, tz0 + inset, tx1 - inset, 15 + step, tz1 - inset, MAGMA_WALL)
    for (x, z) in ((tx0, tz0), (tx1, tz0), (tx0, tz1), (tx1, tz1)):
        d.pillar(x, z, 1, 15, "minecraft:polished_basalt")
        d.set(x, 16, z, "ember_lamp")
    d.set(c, 18, 20, "ember_lamp")
    d.air(c - 1, 1, tz1, c + 1, 4, tz1)

    # Antichambre : canaux de lave, salles rituelles laterales
    az = 27
    d.box(tx0 + 1, 1, az - 1, tx1 - 1, 13, az - 1, MAGMA_WALL)
    d.air(c - 1, 1, az - 1, c + 1, 5, az - 1)
    for x in (17, 23):
        d.box(x, 0, az + 1, x, 0, tz1 - 2, "minecraft:lava", level="0")
    for side, (wx, rx0, rx1, mob, loot) in {"west": (15, tx0 + 1, 14, "minecraft:magma_cube", "infernal_sanctum"),
                                              "east": (25, 26, tx1 - 1, "ember_stalker", "infernal_sanctum")}.items():
        d.box(wx, 1, az, wx, 13, tz1 - 1, MAGMA_WALL)
        d.air(wx, 1, 29, wx, 3, 30)
        d.box(rx0, 0, az, rx1, 0, tz1 - 1, "minecraft:polished_blackstone")
        d.spawner((rx0 + rx1) // 2, 1, 30, mob)
        d.chest((rx0 + rx1) // 2, 1, tz1 - 1, loot, facing="north")
        if side == "east":
            d.guard((rx0 + rx1) // 2 + 2, 1, tz1 - 1, "mimic")
        d.hanging_lantern((rx0 + rx1) // 2, 13, 29, 4, "minecraft:lantern")
    d.spawner(c, 1, 30, "ember_stalker")

    # Arene du Sorcier
    rz0, rz1 = 13, az - 2
    for (x, z) in ((12, 15), (28, 15), (12, 23), (28, 23)):
        d.box(x, 1, z, x + 1, 11, z + 1, "magma_bricks")
        d.set(x, 6, z, "ember_lamp")
        d.set(x + 1, 6, z + 1, "ember_lamp")
    for x in range(tx0 + 1, tx1):
        for z in range(rz0, rz1 + 1):
            r2 = (x - c) ** 2 + (z - 19) ** 2
            if r2 <= 12:
                d.set(x, 0, z, "onyx_bricks")
            elif r2 <= 20:
                d.set(x, 0, z, "minecraft:magma_block")
    for (x, z) in ((tx0 + 1, rz0), (tx1 - 2, rz0)):
        d.box(x, 0, z, x + 1, 0, z + 1, "minecraft:lava", level="0")
    d.guard(c, 1, 19, "infernal_warlock")

    # Antichambre scellee : une echelle grimpe jusqu'a la fleche du temple, ou se trouve le tresor
    d.box(tx0 + 1, 1, tz0 + 1, tx1 - 1, 13, rz0 - 1, MAGMA_WALL)
    d.air(14, 1, tz0 + 1, 26, 5, rz0 - 2)
    d.box(14, 0, tz0 + 1, 26, 0, rz0 - 2, "minecraft:gilded_blackstone")
    d.seal(c - 2, 1, rz0 - 1, c + 2, 4, rz0 - 1)
    for x in (16, 24):
        d.set(x, 1, tz0 + 1, "ember_lamp")
    ladder = (14, tz0 + 2)
    d.air(ladder[0], 6, ladder[1], ladder[0], 16, ladder[1])
    for y in range(1, 17):
        d.set(ladder[0], y, ladder[1], "minecraft:ladder", facing="east", waterlogged="false")

    # Fleche du temple : 4 niveaux plus haut, balcon de pierre noire, braseros et tresor
    sx0, sx1, sz0, sz1, sy0, sy1 = 11, 18, tz0 + 1, tz0 + 8, 16, 28
    d.walls(sx0, sy0, sz0, sx1, sy1 - 1, sz1, MAGMA_WALL)
    d.air(sx0 + 1, sy0 + 1, sz0 + 1, sx1 - 1, sy1 - 1, sz1 - 1)
    d.box(sx0 + 1, sy0, sz0 + 1, sx1 - 1, sy0, sz1 - 1, "minecraft:polished_blackstone_bricks")
    d.set(ladder[0], sy0, ladder[1], "minecraft:ladder", facing="east", waterlogged="false")
    for step, inset in enumerate((0, 1, 2)):
        d.box(sx0 + inset, sy1 + step, sz0 + inset, sx1 - inset, sy1 + step, sz1 - inset, MAGMA_WALL)
    d.set((sx0 + sx1) // 2, sy1 + 2, (sz0 + sz1) // 2, "ember_lamp")
    for (x, z) in ((sx0, sz0), (sx1, sz0), (sx0, sz1), (sx1, sz1)):
        d.pillar(x, z, sy0 + 1, sy1 - 1, "minecraft:polished_basalt")
    for z in (sz0 + 2, sz1 - 2):
        for y in (sy0 + 2, sy0 + 3):
            d.set(sx1, y, z, "minecraft:iron_bars", north="true", south="true", east="false", west="false", waterlogged="false")
    for z in (sz0 + 2, sz0 + 5):
        d.chest(sx0 + 1, sy0 + 1, z, "infernal_sanctum_vault", facing="east")
    d.set(sx1 - 1, sy0 + 1, sz1 - 1, "minecraft:netherrack")
    d.set(sx1 - 1, sy0 + 2, sz1 - 1, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false")
    d.hanging_lantern((sx0 + sx1) // 2, sy1 - 1, (sz0 + sz1) // 2, 2, "minecraft:lantern")
    d.save("infernal_sanctum")


# ------------------------------------------------------------------------------------------------
# Catacombes fletries : mausolee en surface, escalier, galeries, cryptes, tombeau du Champion
# ------------------------------------------------------------------------------------------------

CATACOMB_SINK = 12


def withered_catacombs():
    S = 39
    G = CATACOMB_SINK  # y du sol du Nether dans le gabarit
    d = Dungeon(S, G + 10, S, "withered_catacombs")
    # Roche massive : rien ne doit communiquer avec les grottes alentour
    d.box(0, 0, 0, S - 1, G - 1, S - 1, CRYPT_WALL)

    # Galerie circulaire (3 de large)
    for (x0, z0, x1, z1) in ((7, 7, 31, 9), (7, 29, 31, 31), (7, 7, 9, 31), (29, 7, 31, 31)):
        d.air(x0, 1, z0, x1, 5, z1)
        d.box(x0, 0, z0, x1, 0, z1, CRYPT_FLOOR)
    for k in range(7, 32, 3):
        for (x, z) in ((k, 6), (k, 32), (6, k), (32, k)):
            d.set(x, 2, z, "minecraft:air")
            if d.rng.random() < 0.6:
                d.skull(x, 2, z, wither=d.rng.random() < 0.15)
            else:
                d.set(x, 2, z, "minecraft:bone_block", axis="y")
    for (x, z) in ((8, 8), (30, 8), (8, 30), (30, 30), (19, 8), (8, 19), (30, 19)):
        d.hanging_lantern(x, 6, z, 1)
    for (x, z) in ((12, 8), (26, 30), (8, 24), (30, 13)):
        d.set(x, 4, z, "minecraft:cobweb")
    d.guard(8, 1, 15, "withered_knight")
    d.guard(30, 1, 24, "withered_knight")
    d.guard(19, 1, 8, "withered_knight")

    # Cryptes (generateur + coffre + sarcophages)
    rooms = [((1, 15, 5, 23), (6, 18, 6, 20), "withered_knight"),
             ((33, 15, 37, 23), (32, 18, 32, 20), "ash_wraith"),
             ((15, 1, 23, 5), (18, 6, 20, 6), "withered_knight"),
             ((1, 33, 12, 37), (8, 32, 8, 32), "ash_wraith")]
    for (x0, z0, x1, z1), (dx0, dz0, dx1, dz1), mob in rooms:
        d.air(x0, 1, z0, x1, 5, z1)
        d.box(x0, 0, z0, x1, 0, z1, CRYPT_FLOOR)
        d.air(dx0, 1, dz0, dx1, 3, dz1)
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        d.spawner(cx, 1, cz, mob)
        d.chest(x0, 1, z0, "withered_catacombs", facing="south")
        if (x0 + z0) % 3 == 0:
            d.guard(x0 + 2, 1, z0, "mimic")
        d.box(x1, 1, z1, x1, 1, z1 - 2 if z1 - z0 > 4 else z1, "minecraft:polished_blackstone_slab",
              type="bottom", waterlogged="false")
        d.skull(x1, 2, z1)
        d.hanging_lantern(cx, 6, cz + 1, 1)
        d.set(x0 + 1, 4, z1, "minecraft:cobweb")

    # Tombeau du Champion
    ax0, ax1, az0, az1 = 12, 26, 15, 26
    d.air(ax0, 1, az0, ax1, 8, az1)
    d.box(ax0, 0, az0, ax1, 0, az1, CRYPT_FLOOR)
    d.air(18, 1, 27, 20, 4, 28)
    for (x, z) in ((13, 16), (25, 16), (13, 25), (25, 25)):
        d.pillar(x, z, 1, 8, "minecraft:polished_basalt")
        d.set(x, 1, z, "minecraft:chiseled_polished_blackstone")
    for (x, z) in ((15, 18), (23, 18), (15, 23), (23, 23)):
        d.set(x, 0, z, "minecraft:soul_soil")
        d.set(x, 1, z, "minecraft:soul_fire")
    for x in (16, 22):
        d.box(x, 1, 20, x + 1, 1, 21, "minecraft:polished_blackstone_slab", type="bottom", waterlogged="false")
        d.skull(x, 2, 20, wither=True)
    d.box(18, 0, 19, 20, 0, 22, "withered_planks")
    d.hanging_lantern(19, 9, 20, 3)
    d.guard(19, 1, 21, "withered_champion")

    # Tresor scelle (au nord du tombeau)
    d.air(15, 1, 11, 23, 4, 13)
    d.box(15, 0, 11, 23, 0, 13, "minecraft:gilded_blackstone")
    d.seal(17, 1, 14, 21, 4, 14)
    for x in (16, 19, 22):
        d.chest(x, 1, 11, "withered_catacombs_vault", facing="south")
    d.set(15, 1, 11, "minecraft:gold_block")
    d.set(23, 1, 11, "minecraft:gold_block")
    d.lantern(15, 2, 11)
    d.lantern(23, 2, 11)

    # Escalier vers la surface (descend vers l'ouest)
    for i in range(G):
        x = 20 + i
        d.air(x, i + 1, 33, x, i + 5, 35)
        for z in (33, 34, 35):
            d.stair(x, i + 1, z, "minecraft:polished_blackstone_brick_stairs", "east")
            d.box(x, 0, z, x, i, z, CRYPT_WALL)
    d.air(17, 1, 33, 19, 4, 35)
    d.air(18, 1, 32, 20, 4, 32)
    d.hanging_lantern(19, 5, 34, 1)

    # Mausolee en surface
    mx0, mx1, mz0, mz1 = 26, 37, 30, 38
    d.box(mx0, G, mz0, mx1, G, mz1, "minecraft:polished_blackstone")
    d.walls(mx0, G + 1, mz0, mx1, G + 6, mz1, CRYPT_WALL)
    d.air(mx0 + 1, G + 1, mz0 + 1, mx1 - 1, G + 6, mz1 - 1)
    d.box(mx0, G + 7, mz0, mx1, G + 7, mz1, "withered_planks")
    d.box(mx0 + 1, G + 8, mz0 + 1, mx1 - 1, G + 8, mz1 - 1, "withered_slab", type="bottom", waterlogged="false")
    for (x, z) in ((mx0, mz0), (mx1, mz0), (mx0, mz1), (mx1, mz1)):
        d.pillar(x, z, G + 1, G + 8, "withered_stem")
        d.skull(x, G + 9, z, wither=True)
    d.air(mx0 + 1, G, 33, 31, G, 35)
    for i in range(6, G):
        for z in (33, 34, 35):
            d.stair(20 + i, i + 1, z, "minecraft:polished_blackstone_brick_stairs", "east")
    d.air(33, G + 1, mz1, 35, G + 3, mz1)
    d.lantern(32, G + 1, mz1 - 1)
    d.lantern(36, G + 1, mz1 - 1)
    d.set(36, G + 1, 31, "soul_lily")
    d.set(27, G + 1, 31, "soul_lily")
    d.save("withered_catacombs")


# ------------------------------------------------------------------------------------------------
# Petites structures : quelques coffres et de jolies recompenses, sans boss
# ------------------------------------------------------------------------------------------------

def traveler_camp():
    """Campement de voyageur : tente en laine, feu de camp, bancs en rondins, coffre et tonneau."""
    d = Dungeon(13, 7, 13, "traveler_camp")
    d.air(0, 1, 0, 12, 6, 12)
    d.box(1, 0, 1, 11, 0, 11, [("nether_grass_block", 50), ("minecraft:netherrack", 30), ("minecraft:soul_soil", 20)])
    # Tente (prisme triangulaire)
    for z in range(2, 8):
        for (y, xs) in ((1, (2, 8)), (2, (3, 7)), (3, (4, 6))):
            for x in xs:
                d.set(x, y, z, "minecraft:red_wool")
        d.set(5, 4, z, "minecraft:red_wool")
    for (y, xs) in ((1, (2, 8)), (2, (3, 7)), (3, (4, 6))):
        for x in xs:
            d.set(x, y, 2, "minecraft:white_wool")
    d.set(5, 4, 2, "minecraft:white_wool")
    d.box(3, 0, 3, 7, 0, 7, "ember_planks")
    d.box(3, 1, 4, 4, 1, 6, "minecraft:brown_carpet")
    d.chest(6, 1, 3, "small_treasure", facing="south")
    d.barrel(4, 1, 3, "small_treasure")
    d.lantern(6, 1, 6, "minecraft:lantern")
    # Feu de camp et bancs
    d.set(5, 1, 10, "minecraft:campfire", lit="true", facing="north", signal_fire="false", waterlogged="false")
    d.set(3, 1, 10, "ember_stem", axis="z")
    d.set(7, 1, 10, "ember_stem", axis="z")
    d.set(5, 1, 12, "ember_stem", axis="x")
    d.set(9, 1, 3, "minecraft:crafting_table")
    d.set(10, 1, 8, "ember_fence", north="false", south="false", east="false", west="false", waterlogged="false")
    d.lantern(10, 2, 8, "minecraft:lantern")
    d.set(1, 1, 9, "ember_bloom")
    d.set(11, 1, 11, "sulfur_tulip")
    d.set(2, 1, 11, "nether_grass")
    d.save("traveler_camp")


def nether_garden():
    """Jardin du Nether : potager cloture (ble de cendre, piments, baies) et cabane du jardinier."""
    d = Dungeon(15, 7, 15, "nether_garden")
    d.air(0, 1, 0, 14, 6, 14)
    d.box(0, 0, 0, 14, 0, 14, "nether_grass_block")
    for k in range(15):
        for (x, z) in ((k, 0), (k, 14), (0, k), (14, k)):
            d.set(x, 1, z, "ember_fence", north=str(z != 0 and x in (0, 14)).lower(), south=str(z != 14 and x in (0, 14)).lower(),
                  east=str(x != 14 and z in (0, 14)).lower(), west=str(x != 0 and z in (0, 14)).lower(), waterlogged="false")
    d.air(7, 1, 14, 7, 1, 14)
    d.set(7, 1, 14, "ember_fence_gate", facing="south", in_wall="false", open="false", powered="false")
    # Rangs de cultures
    for z, crop, soil in ((2, "cinder_wheat_crop", "nether_grass_block"), (4, "blaze_pepper_crop", "minecraft:soul_soil"),
                          (6, "cinder_wheat_crop", "nether_grass_block"), (8, "soul_tuber_crop", "minecraft:soul_soil")):
        for x in range(2, 8):
            d.set(x, 0, z, soil)
            d.set(x, 1, z, crop, age=str(d.rng.randrange(4)))
    for x in (2, 4, 6):
        d.set(x, 1, 11, "fire_berry_bush", age="3")
    for (x, z) in ((1, 12), (8, 12), (1, 1), (8, 1)):
        d.set(x, 1, z, d.pick([("ember_bloom", 1), ("ash_daisy", 1), ("crimson_bellflower", 1), ("sulfur_tulip", 1)]))
    # Cabane
    d.box(9, 0, 2, 13, 0, 7, "withered_planks")
    d.walls(9, 1, 2, 13, 3, 7, "withered_planks")
    d.air(10, 1, 3, 12, 3, 6)
    for (x, z) in ((9, 2), (13, 2), (9, 7), (13, 7)):
        d.pillar(x, z, 1, 3, "ember_stem")
    d.box(9, 4, 2, 13, 4, 7, "ember_slab", type="bottom", waterlogged="false")
    d.set(11, 1, 7, "ember_door", facing="south", half="lower", hinge="left", open="false", powered="false")
    d.set(11, 2, 7, "ember_door", facing="south", half="upper", hinge="left", open="false", powered="false")
    d.set(9, 2, 4, "minecraft:air")
    d.set(13, 2, 5, "minecraft:air")
    d.chest(12, 1, 3, "nether_garden", facing="south")
    d.set(10, 1, 3, "minecraft:composter", level="3")
    d.set(10, 1, 6, "minecraft:barrel", facing="up", open="false")
    d.hanging_lantern(11, 4, 5, 1, "minecraft:lantern")
    d.set(12, 1, 6, "potted_glowcap")
    d.save("nether_garden")


def hidden_cache():
    """Cache secrete : une trappe et une echelle menent a une petite salle au tresor enterree."""
    G = 6
    d = Dungeon(7, G + 3, 7, "hidden_cache")
    d.box(0, 0, 0, 6, G - 1, 6, CRYPT_WALL)
    d.air(1, 1, 1, 5, 3, 5)
    d.box(1, 0, 1, 5, 0, 5, "minecraft:polished_blackstone_bricks")
    for y in range(1, G):
        d.set(3, y, 5, "minecraft:ladder", facing="north", waterlogged="false")
    # La trappe affleure au niveau du sol
    d.set(3, G, 5, "withered_trapdoor", facing="north", half="top", open="false", powered="false", waterlogged="false")
    d.chest(1, 1, 1, "small_treasure", facing="south")
    d.chest(5, 1, 1, "small_treasure", facing="south")
    d.set(3, 1, 1, "minecraft:gold_block")
    d.lantern(3, 2, 1, "onyx_lantern")
    d.set(1, 1, 4, "minecraft:barrel", facing="up", open="false")
    d.skull(5, 1, 4)
    # Repere en surface : un petit cairn avec une lanterne des ames
    d.set(5, G + 1, 4, "minecraft:polished_blackstone_brick_wall", up="true")
    d.lantern(5, G + 2, 4)
    d.save("hidden_cache")


def nomad_caravan():
    """Caravane nomade : deux tentes de verrutige, un enclos a lezards, un feu de camp et le marchand piglin."""
    d = Dungeon(19, 8, 15, "nomad_caravan")
    d.air(0, 1, 0, 18, 7, 14)
    d.box(1, 0, 1, 17, 0, 13, [("minecraft:netherrack", 40), ("wart_moss", 25), ("minecraft:soul_soil", 15), ("minecraft:blackstone", 20)])
    for (x0, color) in ((2, "minecraft:red_wool"), (11, "minecraft:orange_wool")):
        for z in range(2, 8):
            for (y, xs) in ((1, (x0, x0 + 6)), (2, (x0 + 1, x0 + 5)), (3, (x0 + 2, x0 + 4))):
                for x in xs:
                    d.set(x, y, z, color)
            d.set(x0 + 3, 4, z, "wartstalk_planks")
        for y in range(1, 5):
            d.set(x0 + 3, y, 8, "wartstalk_fence", north="true", south="false", east="false", west="false", waterlogged="false")
        d.box(x0 + 1, 0, 3, x0 + 5, 0, 6, "wartstalk_planks")
        d.set(x0 + 3, 1, 3, "minecraft:magenta_carpet" if x0 == 2 else "minecraft:yellow_carpet")
    d.chest(3, 1, 3, "nomad_caravan", facing="south")
    d.barrel(7, 1, 3, "nomad_caravan")
    d.set(12, 1, 3, "infernal_cauldron", doses="0")
    d.set(16, 1, 3, "lava_trap")
    d.lantern(4, 1, 6, "ward_lantern")
    d.lantern(14, 1, 6, "minecraft:soul_lantern")
    # Feu de camp central et bancs
    d.set(9, 1, 11, "minecraft:campfire", lit="true", facing="north", signal_fire="false", waterlogged="false")
    d.set(7, 1, 11, "wartstalk_stem", axis="z")
    d.set(11, 1, 11, "wartstalk_stem", axis="z")
    # Enclos a lezards
    for x in range(13, 18):
        for z in (10, 13):
            d.set(x, 1, z, "wartstalk_fence", north="false", south="false", east=str(x < 17).lower(), west=str(x > 13).lower(),
                  waterlogged="false")
    for z in range(10, 14):
        for x in (13, 17):
            d.set(x, 1, z, "wartstalk_fence", north=str(z > 10).lower(), south=str(z < 13).lower(), east="false", west="false",
                  waterlogged="false")
    d.set(13, 1, 11, "wartstalk_fence_gate", facing="west", in_wall="false", open="false", powered="false")
    d.guard(15, 1, 11, "cinder_lizard")
    d.guard(9, 1, 8, "piglin_merchant")
    d.set(1, 1, 12, "crimson_bellflower")
    d.set(6, 1, 13, "minecraft:crimson_roots")
    d.save("nomad_caravan")


def generate():
    nomad_caravan()
    traveler_camp()
    nether_garden()
    hidden_cache()
    onyx_citadel()
    infernal_sanctum()
    withered_catacombs()
