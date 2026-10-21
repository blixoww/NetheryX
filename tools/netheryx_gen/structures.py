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
import math
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

    def vault(self, x0, y0, z0, x1, y1, z1, variant, door):
        """Coffre-fort du donjon : coque de maconnerie scellee sur les 6 faces, sans aucune exception, et une porte de
        Sceaux du gardien (door = x0, y0, z0, x1, y1, z1). Rien ne peut y entrer avant la mort du boss."""
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    if x in (x0, x1) or y in (y0, y1) or z in (z0, z1):
                        self.set(x, y, z, "warded_bricks", variant=variant)
                    else:
                        self.set(x, y, z, AIR)
        self.seal(*door)

    def stairs_run(self, xs, z0, y0, steps, dz, dy, block, fill):
        """Escalier droit le long de z (dz = +1/-1), montant (dy = +1) ou descendant (dy = -1) ; colonnes xs.
        Les marches sont pleines dessous et degagees sur 4 blocs au-dessus."""
        ascend = "south" if (dz > 0) == (dy > 0) else "north"
        for i in range(steps):
            z, y = z0 + i * dz, y0 + i * dy
            for x in xs:
                self.stair(x, y, z, block, ascend)
                for yy in range(y + 1, y + 5):
                    self.set(x, yy, z, AIR)
                for yy in range(min(y0, y0 + (steps - 1) * dy) - 1, y):
                    self.set(x, yy, z, self.pick(fill))

    def stairs_run_x(self, x0, zs, y0, steps, dx, block, fill):
        """Escalier droit montant le long de x (dx = +1 vers l'est)."""
        ascend = "east" if dx > 0 else "west"
        for i in range(steps):
            x, y = x0 + i * dx, y0 + i
            for z in zs:
                self.stair(x, y, z, block, ascend)
                for yy in range(y + 1, y + 5):
                    self.set(x, yy, z, AIR)
                for yy in range(y0 - 1, y):
                    self.set(x, yy, z, self.pick(fill))

    def rail(self, x0, y, z0, x1, z1, block):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for z in range(min(z0, z1), max(z0, z1) + 1):
                self.set(x, y, z, block, up="true")

    def guard(self, x, y, z, entity):
        self.entity(x + 0.5, y, z + 0.5, entity)

    def roof_pyramid(self, x0, z0, x1, z1, y, stairs, full, cap=None):
        """Toit en pyramide d'escaliers (les angles en bloc plein) ; renvoie la hauteur du sommet."""
        k = 0
        while True:
            xa, xb, za, zb, yy = x0 + k, x1 - k, z0 + k, z1 - k, y + k
            if xa > xb or za > zb:
                top = yy - 1
                break
            if xb - xa < 2 or zb - za < 2:
                self.box(xa, yy, za, xb, yy, zb, full)
                top = yy
                break
            for x in range(xa + 1, xb):
                self.stair(x, yy, za, stairs, "south")
                self.stair(x, yy, zb, stairs, "north")
            for z in range(za + 1, zb):
                self.stair(xa, yy, z, stairs, "east")
                self.stair(xb, yy, z, stairs, "west")
            for (x, z) in ((xa, za), (xa, zb), (xb, za), (xb, zb)):
                self.set(x, yy, z, full)
            k += 1
        if cap:
            self.set((x0 + x1) // 2, top + 1, (z0 + z1) // 2, cap, hanging="false", waterlogged="false")
        return top

    def window_x(self, x, y0, y1, z, pane):
        """Fenetre verticale dans un mur oriente nord/sud (le mur court le long de x)."""
        for y in range(y0, y1 + 1):
            self.set(x, y, z, pane, east="true", west="true", north="false", south="false", waterlogged="false")

    def window_z(self, x, y0, y1, z, pane):
        """Fenetre verticale dans un mur oriente est/ouest (le mur court le long de z)."""
        for y in range(y0, y1 + 1):
            self.set(x, y, z, pane, north="true", south="true", east="false", west="false", waterlogged="false")

    def cornice(self, x0, z0, x1, z1, y, stairs):
        """Corniche : anneau d'escaliers renverses colles a l'exterieur d'un batiment (x0..x1, z0..z1)."""
        for x in range(x0, x1 + 1):
            self.stair(x, y, z0 - 1, stairs, "south", "top")
            self.stair(x, y, z1 + 1, stairs, "north", "top")
        for z in range(z0, z1 + 1):
            self.stair(x0 - 1, y, z, stairs, "east", "top")
            self.stair(x1 + 1, y, z, stairs, "west", "top")

    def connect_fences(self):
        """Relie les barrieres a leurs voisines (barrieres, portillons, blocs pleins)."""
        def solid(pos):
            b = self.blocks.get(pos)
            if b is None:
                return False
            n = b[0]
            return not (n.endswith(":air") or any(k in n for k in ("lantern", "carpet", "torch", "chain", "flower", "bloom", "daisy",
                                                                  "tulip", "glowcap", "crop", "bush", "roots", "sprouts", "grass",
                                                                  "lily", "fungus", "slab", "stairs", "bed", "campfire")))
        for (x, y, z), (name, props) in list(self.blocks.items()):
            if "fence" not in name or "gate" in name:
                continue
            p = dict(props)
            for face, (dx, dz) in (("north", (0, -1)), ("south", (0, 1)), ("east", (1, 0)), ("west", (-1, 0))):
                nb = self.blocks.get((x + dx, y, z + dz))
                p[face] = str(nb is not None and ("fence" in nb[0] or solid((x + dx, y, z + dz)))).lower()
            p["waterlogged"] = "false"
            self.set(x, y, z, name, nbt=self.block_nbt.get((x, y, z)), **p)

    def candle(self, x, y, z, count=None):
        self.set(x, y, z, "minecraft:candle", candles=str(count or self.rng.randint(1, 4)), lit="true", waterlogged="false")


# ------------------------------------------------------------------------------------------------
# Citadelle d'onyx : remparts et tours, puis un donjon a trois etages qu'on gravit.
#   Etage 1 : grand hall gardé.  Etage 2 : armurerie, bibliotheque et coffre-fort scelle.
#   Etage 3 : arene de la Sentinelle sous le toit, avec balcons.
# ------------------------------------------------------------------------------------------------

def onyx_citadel():
    S = 45
    c = S // 2
    d = Dungeon(S, 38, S, "onyx_citadel")
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
        for k in range(0, 9, 2):
            for (x, z) in ((tx + k, tz), (tx + k, tz + 8), (tx, tz + k), (tx + 8, tz + k)):
                d.set(x, 16, z, "onyx_brick_wall", up="true")
        d.set(tx + 4, 16, tz + 4, "onyx_lantern", hanging="false", waterlogged="false")
        door_x = tx + 8 if tx == 0 else tx
        d.air(door_x, 1, tz + 3, door_x, 3, tz + 5)

    # Porte d'entree (sud) et ses gardes
    d.air(c - 2, 1, S - 2, c + 2, 5, S - 1)
    for x in (c - 3, c + 3):
        d.pillar(x, S - 1, 1, 7, cap="onyx_lantern")
    d.box(c - 3, 6, S - 2, c + 3, 6, S - 1, "polished_onyx")
    d.guard(c - 2, 1, S - 6, "withered_knight")
    d.guard(c + 2, 1, S - 6, "withered_knight")

    # Cour : allee, bassins de lave, lampadaires, amas d'onyx
    d.box(c - 2, 0, 33, c + 2, 0, S - 3, "polished_onyx")
    for bx in (3, S - 10):
        d.box(bx, 1, 30, bx + 6, 1, 35, "onyx_brick_slab", type="bottom", waterlogged="false")
        d.box(bx + 1, 1, 31, bx + 5, 1, 34, AIR)
        d.box(bx + 1, 0, 31, bx + 5, 0, 34, "minecraft:lava", level="0")
    for (x, z) in ((c - 4, 36), (c + 4, 36), (c - 3, 39), (c + 3, 39), (5, 20), (S - 6, 20), (c, 4)):
        d.box(x, 1, z, x, 3, z, "onyx_brick_wall", up="true")
        d.lantern(x, 4, z, "onyx_lantern")
    for (x, z) in ((4, 14), (S - 5, 14), (4, 26), (S - 5, 26), (14, 4), (30, 4)):
        d.set(x, 1, z, "onyx_cluster", facing="up", waterlogged="false")
    d.spawner(c - 3, 1, 4, "ember_stalker")

    # Donjon : trois etages (sols a y = 0, 8 et 16), toit a y = 28
    kx0, kx1, kz0, kz1 = 11, 33, 8, 32
    d.hall(kx0, 0, kz0, kx1, 28, kz1, ONYX_WALL, ONYX_FLOOR, "polished_onyx")
    d.box(kx0 + 1, 8, kz0 + 1, kx1 - 1, 8, kz1 - 1, ONYX_FLOOR)
    d.box(kx0 + 1, 16, kz0 + 1, kx1 - 1, 16, kz1 - 1, ONYX_FLOOR)
    for k in range(kx0, kx1 + 1, 2):
        d.set(k, 29, kz0, "onyx_brick_wall", up="true")
        d.set(k, 29, kz1, "onyx_brick_wall", up="true")
    for k in range(kz0, kz1 + 1, 2):
        d.set(kx0, 29, k, "onyx_brick_wall", up="true")
        d.set(kx1, 29, k, "onyx_brick_wall", up="true")
    for (x, z) in ((kx0, kz0), (kx1, kz0), (kx0, kz1), (kx1, kz1)):
        d.pillar(x, z, 1, 29, cap="onyx_lantern")
    # Meurtrieres a chaque etage
    for y in (4, 12, 22):
        for z in (14, 20, 26):
            d.set(kx0, y, z, "minecraft:iron_bars", north="true", south="true", east="false", west="false", waterlogged="false")
            d.set(kx1, y, z, "minecraft:iron_bars", north="true", south="true", east="false", west="false", waterlogged="false")
    d.air(c - 1, 1, kz1, c + 1, 4, kz1)
    d.stair(c - 2, 4, kz1, "onyx_brick_stairs", "west", "top")
    d.stair(c + 2, 4, kz1, "onyx_brick_stairs", "east", "top")

    # Etage 1 : grand hall, piliers, generateurs de traqueurs, chevaliers
    for (x, z) in ((16, 13), (28, 13), (16, 20), (28, 20), (16, 27), (28, 27)):
        d.pillar(x, z, 1, 7, "minecraft:polished_basalt")
    for (x, z) in ((19, 12), (25, 12), (19, 24), (25, 24), (c, 18)):
        d.hanging_lantern(x, 8, z, 1, "onyx_lantern")
    d.box(c - 1, 0, 12, c + 1, 0, kz1 - 1, "polished_onyx")
    d.spawner(kx1 - 2, 1, kz0 + 2, "ember_stalker")
    d.spawner(c, 1, kz0 + 2, "ember_stalker")
    d.guard(c - 3, 1, 29, "withered_knight")
    d.guard(c + 3, 1, 29, "withered_knight")
    # Escalier etage 1 -> 2 (mur ouest, vers le nord)
    d.stairs_run(range(kx0 + 1, kx0 + 4), 28, 1, 8, -1, 1, "onyx_brick_stairs", ONYX_WALL)
    d.air(kx0 + 1, 8, 22, kx0 + 3, 8, 28)
    d.rail(kx0 + 4, 9, 22, kx0 + 4, 29, "onyx_brick_wall")
    d.rail(kx0 + 1, 9, 29, kx0 + 3, 29, "onyx_brick_wall")

    # Etage 2 : noyau central contenant le coffre-fort, couloir circulaire, armurerie et bibliotheque
    d.box(18, 9, 14, 26, 15, 22, ONYX_WALL)
    d.vault(19, 9, 15, 25, 14, 21, "onyx", (21, 10, 21, 23, 12, 22))
    for x in (21, 23):
        d.chest(x, 10, 16, "onyx_citadel_vault", facing="south")
    d.set(22, 10, 16, "onyx_cluster", facing="up", waterlogged="false")
    d.lantern(20, 10, 16, "onyx_lantern")
    d.lantern(24, 10, 16, "onyx_lantern")
    for x in (20, 24):
        d.pillar(x, 22, 9, 15, "minecraft:polished_basalt")
    d.set(c, 13, 22, "ember_lamp")
    d.guard(c - 2, 9, 25, "withered_knight")
    d.guard(c + 2, 9, 25, "withered_knight")
    # Armurerie (ouest)
    d.chest(kx0 + 1, 9, 10, "onyx_citadel", facing="east")
    d.guard(kx0 + 2, 9, 12, "mimic")
    for z in (14, 16, 18):
        d.set(kx0 + 1, 9, z, "minecraft:polished_blackstone_slab", type="bottom", waterlogged="false")
        d.skull(kx0 + 1, 10, z, wither=z == 16)
    # Bibliotheque (est) et generateurs
    for z in range(19, 31):
        for y in (9, 10, 11):
            d.set(kx1 - 1, y, z, "minecraft:bookshelf")
    d.chest(kx1 - 2, 9, 24, "onyx_citadel", facing="west")
    d.spawner(28, 9, 26, "ash_wraith")
    d.spawner(c, 9, 10, "ember_stalker")
    for (x, z) in ((14, 11), (30, 22), (c, 30), (14, 24)):
        d.hanging_lantern(x, 16, z, 1, "onyx_lantern")
    # Escalier etage 2 -> 3 (mur est, vers le sud)
    d.stairs_run(range(kx1 - 3, kx1), 10, 9, 8, 1, 1, "onyx_brick_stairs", ONYX_WALL)
    d.air(kx1 - 3, 16, 10, kx1 - 1, 16, 16)
    d.rail(kx1 - 4, 17, 9, kx1 - 4, 16, "onyx_brick_wall")
    d.rail(kx1 - 3, 17, 9, kx1 - 1, 9, "onyx_brick_wall")

    # Etage 3 : arene de la Sentinelle
    for x in range(kx0 + 1, kx1):
        for z in range(kz0 + 1, kz1):
            r2 = (x - c) ** 2 + (z - 21) ** 2
            if r2 <= 20:
                d.set(x, 16, z, "onyx_bricks")
            elif r2 <= 30:
                d.set(x, 16, z, "minecraft:crying_obsidian")
    for (x, z) in ((16, 14), (27, 14), (16, 27), (27, 27)):
        d.box(x, 17, z, x + 1, 27, z + 1, "polished_onyx")
        d.set(x, 22, z, "ember_lamp")
        d.set(x + 1, 22, z + 1, "ember_lamp")
    for (x, z) in ((16, 21), (c, 13), (c, 29), (28, 21)):
        d.hanging_lantern(x, 28, z, 3, "onyx_lantern")
    d.guard(c, 17, 21, "onyx_sentinel")
    # Balcons au-dessus de l'arene : spectres, coffre et mimique de chaque cote
    for (bx0, bx1, rail_x) in ((kx0 + 1, kx0 + 3, kx0 + 3), (kx1 - 3, kx1 - 1, kx1 - 3)):
        d.box(bx0, 22, kz0 + 1, bx1, 22, kz1 - 1, "polished_onyx")
        d.rail(rail_x, 23, kz0 + 1, rail_x, kz1 - 2, "onyx_brick_wall")
        d.spawner((bx0 + bx1) // 2, 23, 20, "ash_wraith")
        ladder_x = bx0 if bx0 == kx0 + 1 else bx1
        d.set(ladder_x, 22, kz1 - 1, AIR)
        d.set(rail_x, 23, kz1 - 1, AIR)
        for y in range(17, 23):
            d.set(ladder_x, y, kz1 - 1, "minecraft:ladder", facing="north", waterlogged="false")
        chest_x = (bx0 + bx1) // 2
        d.chest(chest_x, 23, kz0 + 1, "onyx_citadel", facing="south")
        d.guard(chest_x, 23, kz0 + 3, "mimic")
        d.hanging_lantern(chest_x, 28, 14, 2, "onyx_lantern")
        d.hanging_lantern(chest_x, 28, 27, 2, "onyx_lantern")
    decorate_citadel(d, S, c, kx0, kx1, kz0, kz1)
    d.save("onyx_citadel")


def decorate_citadel(d, S, c, kx0, kx1, kz0, kz1):
    """Habillage : toits pointus des tours, tourelles et fleche du donjon, pilastres, corniches, vitraux,
    corps de garde a herse, jardins de la cour et tapis du grand hall."""
    pane = "quartz_glass_pane"
    # Tours d'angle : fenetres et toits en pyramide
    for (tx, tz) in ((0, 0), (S - 9, 0), (0, S - 9), (S - 9, S - 9)):
        d.cornice(tx, tz, tx + 8, tz + 8, 15, "onyx_brick_stairs")
        d.roof_pyramid(tx, tz, tx + 8, tz + 8, 16, "onyx_brick_stairs", "polished_onyx", cap="onyx_lantern")
        for y0 in (3, 10):
            d.window_x(tx + 4, y0, y0 + 2, tz if tz == 0 else tz + 8, pane)
            d.window_z(tx if tx == 0 else tx + 8, y0, y0 + 2, tz + 4, pane)
    # Remparts : soubassement et bandeau
    for x in range(S):
        for z in range(S):
            if min(x, z, S - 1 - x, S - 1 - z) == 0 and (x, 5, z) in d.blocks and "onyx_bricks" in d.blocks[(x, 5, z)][0]:
                d.set(x, 5, z, "polished_onyx")
                d.set(x, 1, z, "minecraft:polished_blackstone_bricks")
    # Corps de garde : deux tours a toit pointu encadrent la porte, herse au-dessus du passage
    for gx in (c - 7, c + 3):
        d.box(gx, 1, S - 5, gx + 4, 13, S - 1, ONYX_WALL)
        d.cornice(gx, S - 5, gx + 4, S - 2, 13, "onyx_brick_stairs")
        d.roof_pyramid(gx, S - 5, gx + 4, S - 1, 14, "onyx_brick_stairs", "polished_onyx", cap="onyx_lantern")
        d.window_x(gx + 2, 8, 10, S - 1, pane)
    d.box(c - 2, 6, S - 2, c + 2, 13, S - 1, ONYX_WALL)
    for x in range(c - 2, c + 3):
        d.set(x, 5, S - 1, "minecraft:iron_bars", east=str(x < c + 2).lower(), west=str(x > c - 2).lower(), north="false",
              south="false", waterlogged="false")
    for x in range(c - 2, c + 3, 2):
        d.set(x, 13, S - 1, "onyx_brick_wall", up="true")
    # Donjon : pilastres, corniches a chaque etage, hautes fenetres
    for x in range(kx0 + 2, kx1 - 1, 4):
        for z in (kz0 - 1, kz1 + 1):
            if abs(x - c) > 2:
                d.pillar(x, z, 1, 28, "minecraft:polished_basalt")
    for z in range(kz0 + 2, kz1 - 1, 4):
        for x in (kx0 - 1, kx1 + 1):
            d.pillar(x, z, 1, 28, "minecraft:polished_basalt")
    for y in (8, 16, 27):
        d.cornice(kx0, kz0, kx1, kz1, y, "onyx_brick_stairs")
    for x in range(kx0 + 4, kx1 - 2, 4):
        for z in (kz0, kz1):
            if abs(x - c) > 2:
                d.window_x(x, 11, 13, z, pane)
            d.window_x(x, 19, 24, z, pane)
    for z in range(kz0 + 4, kz1 - 2, 4):
        for x in (kx0, kx1):
            d.window_z(x, 19, 24, z, pane)
    # Tourelles d'angle du donjon et fleche centrale sur le toit
    for (tx, tz) in ((kx0 - 2, kz0 - 2), (kx1, kz0 - 2), (kx0 - 2, kz1), (kx1, kz1)):
        d.box(tx, 1, tz, tx + 2, 31, tz + 2, "polished_onyx")
        d.roof_pyramid(tx, tz, tx + 2, tz + 2, 32, "onyx_brick_stairs", "polished_onyx", cap="onyx_lantern")
    d.walls(c - 2, 29, 18, c + 2, 33, 22, ONYX_WALL)
    d.air(c - 1, 29, 19, c + 1, 33, 21)
    d.window_x(c, 30, 32, 18, pane)
    d.window_x(c, 30, 32, 22, pane)
    d.window_z(c - 2, 30, 32, 20, pane)
    d.window_z(c + 2, 30, 32, 20, pane)
    d.set(c, 29, 20, "ember_lamp")
    d.roof_pyramid(c - 2, 18, c + 2, 22, 34, "onyx_brick_stairs", "polished_onyx", cap="onyx_lantern")
    # Cour : jardins de braise et de luminelles
    for (x0, z0) in ((3, 15), (3, 23), (S - 9, 15), (S - 9, 23)):
        d.box(x0, 0, z0, x0 + 5, 0, z0 + 2, "ember_nylium")
        for x in range(x0, x0 + 6):
            for z in range(z0, z0 + 3):
                roll = d.rng.random()
                if roll < 0.18:
                    d.set(x, 1, z, "ember_fungus")
                elif roll < 0.4:
                    d.set(x, 1, z, "glowcap")
                elif roll < 0.6:
                    d.set(x, 1, z, "ember_roots")
    # Grand hall : tapis rouge de la porte a l'escalier du fond
    d.box(c - 1, 1, 12, c + 1, 1, kz1 - 1, "minecraft:red_carpet")


# ------------------------------------------------------------------------------------------------
# Sanctuaire infernal : douves de lave et temple en surface, puis un grand escalier descend vers la nef
# souterraine ou attend le Sorcier ; le coffre-fort scelle s'ouvre dans le mur nord de la nef.
# ------------------------------------------------------------------------------------------------

SANCTUM_SINK = 10


def infernal_sanctum():
    S = 41
    c = S // 2
    G = SANCTUM_SINK  # y du sol du Nether dans le gabarit
    d = Dungeon(S, G + 22, S, "infernal_sanctum")
    # Sous-sol massif : la nef ne communique pas avec les grottes alentour
    d.box(0, 0, 0, S - 1, G - 1, S - 1, MAGMA_WALL)
    for x in range(S):
        for z in range(S):
            edge = min(x, z, S - 1 - x, S - 1 - z)
            if edge == 0:
                d.set(x, G, z, d.pick(MAGMA_WALL))
                d.set(x, G + 1, z, "magma_brick_wall", up="true")
            elif edge <= 3:
                d.set(x, G, z, "minecraft:lava", level="0")
            else:
                d.set(x, G, z, d.pick(MAGMA_FLOOR))
            for y in range(G + (2 if edge == 0 else 1), G + 15):
                d.set(x, y, z, AIR)
    # Pont d'entree (sud)
    d.box(c - 2, G, S - 4, c + 2, G, S - 1, "magma_bricks")
    d.air(c - 2, G + 1, S - 1, c + 2, G + 1, S - 1)
    d.rail(c - 3, G + 1, S - 4, c - 3, S - 1, "magma_brick_wall")
    d.rail(c + 3, G + 1, S - 4, c + 3, S - 1, "magma_brick_wall")
    d.set(c - 3, G + 2, S - 4, "minecraft:lantern", hanging="false", waterlogged="false")
    d.set(c + 3, G + 2, S - 4, "minecraft:lantern", hanging="false", waterlogged="false")

    # Cour exterieure : autels a blazes, braseros
    for (x, z) in ((7, 7), (S - 8, 7)):
        d.box(x - 1, G + 1, z - 1, x + 1, G + 1, z + 1, "magma_bricks")
        d.spawner(x, G + 2, z, "minecraft:blaze")
    for (x, z) in ((6, 20), (S - 7, 20), (6, S - 8), (S - 7, S - 8)):
        d.pillar(x, z, G + 1, G + 4, "magma_bricks")
        d.set(x, G + 5, z, "minecraft:netherrack")
        d.set(x, G + 6, z, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false")
    d.guard(c - 5, G + 1, S - 7, "ember_stalker")
    d.guard(c + 5, G + 1, S - 7, "ember_stalker")

    # Temple (rez-de-chaussee) : salles rituelles laterales, nef de magma, escalier vers le sous-sol
    tx0, tx1, tz0, tz1 = 9, 31, 8, 33
    d.hall(tx0, G, tz0, tx1, G + 12, tz1, MAGMA_WALL, MAGMA_FLOOR)
    for step, inset in enumerate((1, 3, 5)):
        d.box(tx0 + inset, G + 13 + step, tz0 + inset, tx1 - inset, G + 13 + step, tz1 - inset, MAGMA_WALL)
    for (x, z) in ((tx0, tz0), (tx1, tz0), (tx0, tz1), (tx1, tz1)):
        d.pillar(x, z, G + 1, G + 13, "minecraft:polished_basalt")
        d.set(x, G + 14, z, "ember_lamp")
    d.set(c, G + 16, 20, "ember_lamp")
    d.air(c - 1, G + 1, tz1, c + 1, G + 4, tz1)
    for z in range(20, tz1 - 1):
        for x in (17, 23):
            d.set(x, G, z, "minecraft:magma_block")
    for side, (wx, rx0, rx1, mob) in {"west": (16, tx0 + 1, 15, "minecraft:magma_cube"),
                                      "east": (24, 25, tx1 - 1, "ember_stalker")}.items():
        d.box(wx, G + 1, tz0 + 1, wx, G + 11, 18, MAGMA_WALL)
        d.box(rx0, G + 1, 18, rx1, G + 11, 18, MAGMA_WALL)
        d.air(wx, G + 1, 12, wx, G + 3, 13)
        d.box(rx0, G, tz0 + 1, rx1, G, 17, "minecraft:polished_blackstone")
        d.spawner((rx0 + rx1) // 2, G + 1, 13, mob)
        d.chest((rx0 + rx1) // 2, G + 1, tz0 + 1, "infernal_sanctum", facing="south")
        d.guard((rx0 + rx1) // 2 + (2 if side == "east" else -2), G + 1, tz0 + 1, "mimic")
        d.hanging_lantern((rx0 + rx1) // 2, G + 12, 14, 4, "minecraft:lantern")
    d.spawner(c, G + 1, 12, "ember_stalker")
    for (x, z) in ((c, 16), (c, 26)):
        d.hanging_lantern(x, G + 12, z, 4, "minecraft:lantern")
    # Grand escalier (mur ouest du temple) : du rez-de-chaussee (sol y = G) a la nef (sol y = 1)
    # La premiere marche est au niveau du sol du temple (y = G), la derniere au niveau du sol de la nef (y = 1)
    d.stairs_run(range(tx0 + 1, tx0 + 4), 30, G, G, -1, -1, "minecraft:polished_blackstone_brick_stairs", MAGMA_WALL)
    d.air(tx0 + 1, G, 21, tx0 + 3, G, 29)
    d.rail(tx0 + 4, G + 1, 21, tx0 + 4, 29, "magma_brick_wall")
    d.rail(tx0 + 1, G + 1, 20, tx0 + 3, 20, "magma_brick_wall")

    # Nef souterraine : arene du Sorcier (sol y = 1, voute y = G)
    nx0, nx1, nz0, nz1 = 6, 34, 8, 34
    d.box(nx0, 1, nz0, nx1, 1, nz1, MAGMA_FLOOR)
    d.air(nx0, 2, nz0, nx1, G - 1, nz1)
    d.stairs_run(range(tx0 + 1, tx0 + 4), 30, G, G, -1, -1, "minecraft:polished_blackstone_brick_stairs", MAGMA_WALL)
    for x in range(nx0, nx1 + 1):
        for z in range(nz0, nz1 + 1):
            r2 = (x - c) ** 2 + (z - 21) ** 2
            if r2 <= 14:
                d.set(x, 1, z, "onyx_bricks")
            elif r2 <= 26:
                d.set(x, 1, z, "minecraft:magma_block")
    for x in (nx0 + 1, nx1 - 1):
        d.box(x, 1, nz0 + 2, x, 1, nz1 - 2, "minecraft:lava", level="0")
    for (x, z) in ((13, 13), (27, 13), (13, 29), (27, 29)):
        d.box(x, 2, z, x + 1, G - 1, z + 1, "magma_bricks")
        d.set(x, 5, z, "ember_lamp")
        d.set(x + 1, 5, z + 1, "ember_lamp")
    for (x, z) in ((c, 13), (c, 29), (13, 21), (28, 21)):
        d.hanging_lantern(x, G, z, 2, "minecraft:lantern")
    for (x, z) in ((nx1 - 2, nz1 - 2), (nx1 - 2, nz0 + 2)):
        d.spawner(x, 2, z, "minecraft:magma_cube")
    d.guard(c, 2, 21, "infernal_warlock")

    # Coffre-fort scelle : dans le mur nord de la nef
    d.vault(15, 1, 1, 25, 7, 7, "magma", (19, 2, 7, 21, 4, 7))
    for x in (18, 22):
        d.chest(x, 2, 2, "infernal_sanctum_vault", facing="south")
    d.set(c, 2, 2, "minecraft:gold_block")
    d.lantern(16, 2, 2, "minecraft:lantern")
    d.lantern(24, 2, 2, "minecraft:lantern")
    for x in (17, 23):
        d.pillar(x, nz0, 2, G - 1, "minecraft:polished_basalt")
    d.set(c, 6, nz0, "ember_lamp")
    decorate_sanctum(d, S, c, G, tx0, tx1, tz0, tz1, nx0, nx1, nz0, nz1)
    d.save("infernal_sanctum")


def decorate_sanctum(d, S, c, G, tx0, tx1, tz0, tz1, nx0, nx1, nz0, nz1):
    """Habillage : portique a colonnes, fenetres grillagees, corniches, obelisques ardents, flamme sommitale,
    tapis rituel et braseros de la nef."""
    # Portique devant la porte sud
    pz0, pz1 = tz1 + 1, tz1 + 3
    d.box(c - 5, G, pz0, c + 5, G, pz1, "magma_bricks")
    for x in (c - 5, c - 3, c + 3, c + 5):
        d.pillar(x, pz1, G + 1, G + 7, "minecraft:polished_basalt")
        d.set(x, G + 1, pz1, "minecraft:chiseled_polished_blackstone")
    d.box(c - 5, G + 8, pz0, c + 5, G + 8, pz1, "magma_brick_slab", type="top", waterlogged="false")
    for x in range(c - 5, c + 6):
        d.stair(x, G + 9, pz1, "magma_brick_stairs", "north")
    for x in (c - 4, c + 4):
        d.set(x, G + 7, pz1, "ember_lamp")
    for x in (c - 1, c + 1):
        d.stair(x, G, pz1 + 1, "magma_brick_stairs", "north")
    d.stair(c, G, pz1 + 1, "magma_brick_stairs", "north")
    # Facade : soubassement, corniches, fenetres grillagees
    for y in (G + 6, G + 12):
        d.cornice(tx0, tz0, tx1, tz1, y, "magma_brick_stairs")
    for z in range(tz0 + 3, tz1 - 1, 5):
        for x in (tx0, tx1):
            for y in range(G + 3, G + 6):
                d.set(x, y, z, "minecraft:iron_bars", north="true", south="true", east="false", west="false", waterlogged="false")
    for x in range(tx0 + 3, tx1 - 1, 5):
        if abs(x - c) > 2:
            for y in range(G + 3, G + 6):
                d.set(x, y, tz1, "minecraft:iron_bars", east="true", west="true", north="false", south="false", waterlogged="false")
    # Flamme sommitale au-dessus de la ziggourat
    d.box(c - 1, G + 16, 19, c + 1, G + 16, 21, "magma_bricks")
    d.pillar(c, 20, G + 17, G + 18, "minecraft:polished_basalt")
    d.set(c, G + 19, 20, "minecraft:netherrack")
    d.set(c, G + 20, 20, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false")
    # Obelisques ardents dans la cour (remplacent les braseros)
    for (x, z) in ((6, 20), (S - 7, 20), (6, S - 8), (S - 7, S - 8)):
        d.box(x - 1, G + 1, z - 1, x + 1, G + 1, z + 1, "minecraft:polished_blackstone_bricks")
        d.pillar(x, z, G + 2, G + 7, "minecraft:chiseled_nether_bricks")
        for (dx, dz, f) in ((0, -1, "north"), (0, 1, "south"), (-1, 0, "west"), (1, 0, "east")):
            d.stair(x + dx, G + 2, z + dz, "magma_brick_stairs", {"north": "south", "south": "north", "west": "east", "east": "west"}[f])
        d.set(x, G + 8, z, "minecraft:netherrack")
        d.set(x, G + 9, z, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false")
    # Interieur : tapis rituel et braseros de la nef
    d.box(c - 1, G + 1, 20, c + 1, G + 1, tz1 - 1, "minecraft:red_carpet")
    for (x, z) in ((nx0 + 3, nz0 + 3), (nx1 - 3, nz0 + 3), (nx0 + 3, nz1 - 3), (nx1 - 3, nz1 - 3)):
        d.set(x, 2, z, "minecraft:campfire", lit="true", facing="north", signal_fire="false", waterlogged="false")


# ------------------------------------------------------------------------------------------------
# Catacombes fletries : mausolee en surface, crypte superieure (galerie, cryptes laterales), puis un
# escalier descend vers la crypte profonde : ossuaires, tombeau du Champion et coffre-fort scelle.
# ------------------------------------------------------------------------------------------------

CATACOMB_SINK = 16


def withered_catacombs():
    S = 39
    G = CATACOMB_SINK  # y du sol du Nether dans le gabarit
    U = 9              # sol de la crypte superieure
    d = Dungeon(S, G + 13, S, "withered_catacombs")
    # Roche massive : rien ne doit communiquer avec les grottes alentour
    d.box(0, 0, 0, S - 1, G - 1, S - 1, CRYPT_WALL)

    # --- Crypte superieure : galerie circulaire (3 de large) -----------------------------------
    for (x0, z0, x1, z1) in ((7, 7, 31, 9), (7, 29, 31, 31), (7, 7, 9, 31), (29, 7, 31, 31)):
        d.air(x0, U + 1, z0, x1, U + 5, z1)
        d.box(x0, U, z0, x1, U, z1, CRYPT_FLOOR)
    for k in range(7, 32, 3):
        for (x, z) in ((k, 6), (k, 32), (6, k), (32, k)):
            d.set(x, U + 2, z, AIR)
            if d.rng.random() < 0.6:
                d.skull(x, U + 2, z, wither=d.rng.random() < 0.15)
            else:
                d.set(x, U + 2, z, "minecraft:bone_block", axis="y")
    for (x, z) in ((8, 8), (30, 8), (8, 30), (30, 30), (19, 8), (8, 19), (30, 19)):
        d.hanging_lantern(x, U + 6, z, 1)
    for (x, z) in ((12, 8), (26, 30), (8, 24), (30, 13)):
        d.set(x, U + 4, z, "minecraft:cobweb")
    d.guard(8, U + 1, 15, "withered_knight")
    d.guard(30, U + 1, 24, "withered_knight")
    d.guard(19, U + 1, 8, "withered_knight")
    # Cryptes laterales (generateur, coffre, sarcophages)
    rooms = [((1, 15, 5, 23), (6, 18, 6, 20), "withered_knight"),
             ((33, 15, 37, 23), (32, 18, 32, 20), "ash_wraith"),
             ((15, 1, 23, 5), (18, 6, 20, 6), "withered_knight"),
             ((1, 33, 12, 37), (8, 32, 8, 32), "ash_wraith")]
    for (x0, z0, x1, z1), (dx0, dz0, dx1, dz1), mob in rooms:
        d.air(x0, U + 1, z0, x1, U + 5, z1)
        d.box(x0, U, z0, x1, U, z1, CRYPT_FLOOR)
        d.air(dx0, U + 1, dz0, dx1, U + 3, dz1)
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        d.spawner(cx, U + 1, cz, mob)
        d.chest(x0, U + 1, z0, "withered_catacombs", facing="south")
        if (x0, z0) in ((1, 15), (1, 33)):
            d.guard(x0 + 2, U + 1, z0, "mimic")
        d.box(x1, U + 1, z1, x1, U + 1, z1 - 2 if z1 - z0 > 4 else z1, "minecraft:polished_blackstone_slab",
              type="bottom", waterlogged="false")
        d.skull(x1, U + 2, z1)
        d.hanging_lantern(cx, U + 6, cz + 1, 1)
        d.set(x0 + 1, U + 4, z1, "minecraft:cobweb")
    # Chapelle centrale de la crypte superieure (au-dessus du tombeau)
    d.air(13, U + 1, 13, 25, U + 5, 25)
    d.box(13, U, 13, 25, U, 25, CRYPT_FLOOR)
    d.air(18, U + 1, 10, 20, U + 3, 12)
    d.air(18, U + 1, 26, 20, U + 3, 28)
    for (x, z) in ((14, 14), (24, 14), (14, 24), (24, 24)):
        d.pillar(x, z, U + 1, U + 5, "minecraft:polished_basalt")
    d.spawner(19, U + 1, 19, "ash_wraith")
    d.hanging_lantern(19, U + 6, 16, 2)
    d.hanging_lantern(19, U + 6, 22, 2)

    # Escalier crypte superieure -> crypte profonde (descend vers le nord depuis la galerie sud)
    d.stairs_run(range(18, 21), 29, U, U, -1, -1, "minecraft:polished_blackstone_brick_stairs", CRYPT_WALL)
    d.rail(17, U + 1, 25, 17, 29, "minecraft:polished_blackstone_brick_wall")
    d.rail(21, U + 1, 25, 21, 29, "minecraft:polished_blackstone_brick_wall")
    d.rail(18, U + 1, 24, 20, 24, "minecraft:polished_blackstone_brick_wall")

    # --- Crypte profonde : tombeau du Champion (sol y = 1) -------------------------------------
    ax0, ax1, az0, az1 = 12, 26, 15, 26
    d.air(ax0, 2, az0, ax1, 7, az1)
    d.box(ax0, 1, az0, ax1, 1, az1, CRYPT_FLOOR)
    d.stairs_run(range(18, 21), 29, U, U, -1, -1, "minecraft:polished_blackstone_brick_stairs", CRYPT_WALL)
    for (x, z) in ((13, 16), (25, 16), (13, 25), (25, 25)):
        d.pillar(x, z, 2, 7, "minecraft:polished_basalt")
        d.set(x, 2, z, "minecraft:chiseled_polished_blackstone")
    for (x, z) in ((15, 18), (23, 18), (15, 23), (23, 23)):
        d.set(x, 1, z, "minecraft:soul_soil")
        d.set(x, 2, z, "minecraft:soul_fire")
    for x in (16, 22):
        d.box(x, 2, 20, x + 1, 2, 21, "minecraft:polished_blackstone_slab", type="bottom", waterlogged="false")
        d.skull(x, 3, 20, wither=True)
    d.box(18, 1, 19, 20, 1, 22, "withered_planks")
    d.hanging_lantern(19, 8, 18, 2)
    d.guard(19, 2, 20, "withered_champion")
    # Ossuaires lateraux (generateurs, coffres, mimique)
    for (ox0, ox1, door_x, mob) in ((3, 10, 11, "withered_knight"), (28, 35, 27, "ash_wraith")):
        d.air(ox0, 2, 17, ox1, 6, 24)
        d.box(ox0, 1, 17, ox1, 1, 24, CRYPT_FLOOR)
        d.air(door_x, 2, 20, door_x, 4, 21)
        d.spawner((ox0 + ox1) // 2, 2, 20, mob)
        d.chest(ox0, 2, 17, "withered_catacombs", facing="east")
        d.guard(ox0, 2, 19, "mimic")
        for z in range(17, 25, 2):
            d.set(ox1 if ox0 == 3 else ox0, 3, z, "minecraft:bone_block", axis="y")
            d.skull(ox1 if ox0 == 3 else ox0, 4, z)
        d.hanging_lantern((ox0 + ox1) // 2, 7, 22, 1)
    # Coffre-fort scelle (au nord du tombeau)
    d.vault(14, 1, 8, 24, 7, 13, "blackstone", (18, 2, 13, 20, 4, 13))
    d.air(18, 2, 14, 20, 4, 14)
    for x in (16, 22):
        d.chest(x, 2, 9, "withered_catacombs_vault", facing="south")
    d.set(19, 2, 9, "minecraft:gilded_blackstone")
    d.lantern(15, 2, 9)
    d.lantern(23, 2, 9)

    # --- Acces depuis la surface ----------------------------------------------------------------
    d.air(18, U + 1, 32, 23, U + 4, 35)
    d.box(18, U, 32, 23, U, 35, CRYPT_FLOOR)
    d.stairs_run_x(24, (33, 34, 35), U + 1, G - U, 1, "minecraft:polished_blackstone_brick_stairs", CRYPT_WALL)
    d.hanging_lantern(20, U + 5, 34, 1)

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
    d.stairs_run_x(24, (33, 34, 35), U + 1, G - U, 1, "minecraft:polished_blackstone_brick_stairs", CRYPT_WALL)
    d.air(33, G + 1, mz1, 35, G + 3, mz1)
    d.lantern(32, G + 1, mz1 - 1)
    d.lantern(36, G + 1, mz1 - 1)
    d.set(36, G + 1, 31, "soul_lily")
    d.set(27, G + 1, 31, "soul_lily")
    decorate_catacombs(d, S, G, U, mx0, mx1, mz0, mz1)
    d.save("withered_catacombs")


GRAVE_GROUND = [("minecraft:soul_soil", 40), ("minecraft:netherrack", 25), ("minecraft:blackstone", 20), ("withered_soil", 15)]


def dead_tree(d, x, y, z, height):
    for k in range(height):
        d.set(x, y + k, z, "withered_stem", axis="y")
    for (dx, dz, axis) in ((1, 0, "x"), (-1, 0, "x"), (0, 1, "z"), (0, -1, "z")):
        if d.rng.random() < 0.7:
            by = y + height - 1 - d.rng.randint(0, 2)
            for k in (1, 2):
                d.set(x + dx * k, by + (k - 1), z + dz * k, "withered_stem", axis=axis)


def decorate_catacombs(d, S, G, U, mx0, mx1, mz0, mz1):
    """Surface : cimetiere clos (tombes, arbres morts, lanternes, bougies) autour d'un mausolee a toit a deux pans.
    Sous terre : bougies sur les sarcophages et dans les cryptes."""
    # Sol du cimetiere (couche superieure du bloc de roche)
    for x in range(S):
        for z in range(S):
            if not (mx0 <= x <= mx1 and mz0 <= z <= mz1):
                d.set(x, G - 1, z, d.pick(GRAVE_GROUND))
    # Muret d'enceinte a piliers et lanternes, portail au sud-ouest
    for k in range(S):
        for (x, z) in ((k, 0), (0, k), (S - 1, k), (k, S - 1)):
            if mx0 <= x <= mx1 and mz0 <= z <= mz1:
                continue
            if z == S - 1 and 6 <= x <= 10:
                continue
            d.set(x, G, z, "minecraft:polished_blackstone_brick_wall", up="true")
            if k % 6 == 0:
                d.pillar(x, z, G, G + 1, "minecraft:polished_blackstone_bricks")
                d.lantern(x, G + 2, z)
    for x in (5, 11):
        d.pillar(x, S - 1, G, G + 2, "minecraft:polished_blackstone_bricks")
        d.set(x, G + 3, S - 1, "minecraft:soul_campfire", lit="true", facing="south", signal_fire="false", waterlogged="false")
    # Allee vers le mausolee
    for x in range(8, mx0):
        d.set(x, G - 1, 34, "minecraft:polished_blackstone_bricks")
    for z in range(34, S - 1):
        d.set(8, G - 1, z, "minecraft:polished_blackstone_bricks")
    # Tombes : stele, dalle et parfois une bougie
    for gx in range(3, 24, 4):
        for gz in range(4, 29, 5):
            if d.rng.random() < 0.2:
                continue
            d.set(gx, G, gz, "minecraft:polished_blackstone_brick_wall", up="true")
            if d.rng.random() < 0.5:
                d.skull(gx, G + 1, gz)
            d.set(gx, G, gz + 1, "minecraft:polished_blackstone_slab", type="bottom", waterlogged="false")
            d.set(gx, G, gz + 2, "minecraft:polished_blackstone_slab", type="bottom", waterlogged="false")
            if d.rng.random() < 0.4:
                d.set(gx, G - 1, gz + 2, "minecraft:soul_soil")
                d.set(gx + 1, G, gz + 1, "soul_lily")
            elif d.rng.random() < 0.5:
                d.candle(gx, G + 1, gz + 1)
    for (x, z, h) in ((27, 6, 5), (32, 16, 6), (16, 30, 4), (34, 25, 5)):
        dead_tree(d, x, G, z, h)
    # Mausolee : toit a deux pans en escaliers fletris, pignons, portique
    for k in range(5):
        y = G + 7 + k
        for x in range(mx0 - 1, mx1 + 2):
            if 0 <= x < S:
                d.stair(x, y, mz0 + k, "withered_stairs", "south")
                if mz1 - k != mz0 + k:
                    d.stair(x, y, mz1 - k, "withered_stairs", "north")
        for x in (mx0, mx1):
            d.box(x, y, mz0 + k + 1, x, y, mz1 - k - 1, "withered_planks")
    for x in range(mx0 - 1, mx1 + 2):
        if 0 <= x < S:
            d.set(x, G + 12, (mz0 + mz1) // 2, "withered_slab", type="bottom", waterlogged="false")
    for z in range(mz0 + 2, mz1 - 1, 3):
        for x in (mx0, mx1):
            d.set(x, G + 3, z, "minecraft:iron_bars", north="true", south="true", east="false", west="false", waterlogged="false")
            d.set(x, G + 4, z, "minecraft:iron_bars", north="true", south="true", east="false", west="false", waterlogged="false")
    # Sous terre : bougies sur les sarcophages du tombeau et dans les cryptes
    for (x, z) in ((16, 21), (17, 21), (22, 21), (23, 21)):
        d.candle(x, 3, z)
    for (x, z) in ((2, 22), (36, 22), (16, 4), (2, 36)):
        d.candle(x, U + 1, z)


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
    """Jardin du Nether : un parc ouvert ou l'on se promene librement. Haie basse percee de quatre entrees sous des
    arches, allee de ronde, allees en croix sous des pergolas, place centrale autour d'une fontaine de givre, serre
    ouverte sur deux cotes et trois carres de culture au ras du sol (on marche entre les plants)."""
    S, c = 27, 13
    d = Dungeon(S, 10, S, "nether_garden")
    d.air(0, 1, 0, S - 1, 9, S - 1)
    d.box(0, 0, 0, S - 1, 0, S - 1, "nether_grass_block")
    path = [("minecraft:polished_blackstone_bricks", 70), ("minecraft:cracked_polished_blackstone_bricks", 12),
            ("minecraft:polished_blackstone", 18)]
    flowers = [("ember_bloom", 3), ("ash_daisy", 3), ("crimson_bellflower", 3), ("sulfur_tulip", 3), ("glowcap", 2),
               ("blazing_grass", 2), ("hellfern", 1)]

    def flower(x, z):
        f = d.pick(flowers)
        if f == "hellfern":
            d.set(x, 1, z, f, half="lower")
            d.set(x, 2, z, f, half="upper")
        else:
            d.set(x, 1, z, f)

    def post(x, z, top=2):
        d.set(x, 1, z, "ember_stem", axis="y")
        for y in range(2, top + 1):
            d.set(x, y, z, "ember_fence")
        d.lantern(x, top + 1, z, "minecraft:lantern")

    # Allee de ronde et allees en croix (3 blocs de large, ouvertes jusqu'aux quatre entrees)
    d.box(2, 0, 2, S - 3, 0, S - 3, path)
    d.box(3, 0, 3, S - 4, 0, S - 4, "nether_grass_block")
    d.box(c - 1, 0, 0, c + 1, 0, S - 1, path)
    d.box(0, 0, c - 1, S - 1, 0, c + 1, path)
    # Haie basse (on voit par-dessus), poteaux a lanternes et quatre arches d'entree
    for k in range(S):
        for (x, z) in ((k, 0), (k, S - 1), (0, k), (S - 1, k)):
            if abs(k - c) <= 1:
                continue
            if k in (0, 7, S - 8, S - 1):
                post(x, z)
            else:
                d.set(x, 1, z, "ember_wart_block")
    for side in range(4):
        for k in (c - 2, c + 2):
            x, z = [(k, 0), (k, S - 1), (0, k), (S - 1, k)][side]
            d.pillar(x, z, 1, 3, "ember_stem")
        for k in range(c - 2, c + 3):
            x, z = [(k, 0), (k, S - 1), (0, k), (S - 1, k)][side]
            d.set(x, 4, z, "ember_slab", type="bottom", waterlogged="false")
        x, z = [(c, 0), (c, S - 1), (0, c), (S - 1, c)][side]
        d.lantern(x, 3, z, "minecraft:lantern", hanging=True)
    # Bordure fleurie le long de la haie
    for k in range(1, S - 1):
        for (x, z) in ((k, 1), (k, S - 2), (1, k), (S - 2, k)):
            if abs(k - c) > 1 and d.rng.random() < 0.6:
                flower(x, z)
    # Pergolas au-dessus des allees en croix (hors de la place)
    for t in (3, 7, S - 8, S - 4):
        for u in (c - 2, c + 2):
            d.pillar(u, t, 1, 3, "ember_stem")
            d.pillar(t, u, 1, 3, "ember_stem")
        for u in range(c - 2, c + 3):
            d.set(u, 4, t, "ember_slab", type="bottom", waterlogged="false")
            d.set(t, 4, u, "ember_slab", type="bottom", waterlogged="false")
        d.set(c, 3, t, "ember_firefly_jar", hanging="true", waterlogged="false")
        d.set(t, 3, c, "ember_firefly_jar", hanging="true", waterlogged="false")
    # Place centrale (9 x 9) et fontaine de givre : on en fait le tour par une allee de 2 blocs
    d.box(c - 4, 0, c - 4, c + 4, 0, c + 4, path)
    for (x, z) in ((c - 4, c - 4), (c - 4, c + 4), (c + 4, c - 4), (c + 4, c + 4)):
        post(x, z)
    d.box(c - 2, 1, c - 2, c + 2, 1, c + 2, "rimestone_brick_wall", up="true")
    d.box(c - 1, 1, c - 1, c + 1, 1, c + 1, "rime_ice")
    d.pillar(c, c, 2, 3, "rimestone_bricks")
    d.set(c, 4, c, "quartz_lamp")
    for (x, z) in ((c - 1, c - 1), (c + 1, c + 1), (c - 1, c + 1), (c + 1, c - 1)):
        d.set(x, 2, z, "rime_crystal", facing="up", waterlogged="false")
    # Bancs tournes vers la fontaine, au bord de la place
    for k in (c - 3, c + 3):
        d.stair(c - 4, 1, k, "ember_stairs", "west")
        d.stair(c + 4, 1, k, "ember_stairs", "east")
        d.stair(k, 1, c - 4, "ember_stairs", "north")
        d.stair(k, 1, c + 4, "ember_stairs", "south")
    # Carres de culture au ras du sol, bordes de troncs couches, une lanterne a chaque coin
    beds = [((S - 9, 3), "cinder_wheat_crop", "nether_grass_block"), ((3, S - 9), "soul_tuber_crop", "minecraft:soul_soil"),
            ((S - 9, S - 9), "blaze_pepper_crop", "minecraft:soul_soil")]
    for (x0, z0), crop, soil in beds:
        x1, z1 = x0 + 5, z0 + 5
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x in (x0, x1) or z in (z0, z1):
                    d.set(x, 0, z, "ember_stem", axis="x" if z in (z0, z1) else "z")
                else:
                    d.set(x, 0, z, soil)
                    if crop == "blaze_pepper_crop" and x == x0 + 2:
                        d.set(x, 1, z, "fire_berry_bush", age="3")
                    else:
                        d.set(x, 1, z, crop, age=str(d.rng.randrange(2, 4)))
        for (x, z) in ((x0, z0), (x1, z1)):
            post(x, z, top=1)
    # Serre en verre de quartz (quart nord-ouest), ouverte vers l'allee est et vers le sud
    gx0, gx1, gz0, gz1 = 3, 8, 3, 8
    for x in range(gx0, gx1 + 1):
        for z in range(gz0, gz1 + 1):
            d.set(x, 0, z, "minecraft:soul_soil")
            edge_x, edge_z = x in (gx0, gx1), z in (gz0, gz1)
            for y in range(1, 4):
                if edge_x and edge_z:
                    d.set(x, y, z, "ember_stem", axis="y")
                elif edge_z:
                    d.window_x(x, y, y, z, "quartz_glass_pane")
                elif edge_x:
                    d.window_z(x, y, y, z, "quartz_glass_pane")
                else:
                    d.set(x, y, z, AIR)
            d.set(x, 4, z, "ember_stem" if edge_x and edge_z else "quartz_glass")
    d.air(gx1, 1, 5, gx1, 2, 6)
    d.air(5, 1, gz1, 6, 2, gz1)
    d.box(gx0 + 1, 0, gz0 + 1, gx1 - 1, 0, gz1 - 1, path)
    for (x, z) in ((4, 5), (4, 6), (5, 4), (6, 4)):
        d.set(x, 1, z, d.pick([("glowcap", 3), ("crimson_bellflower", 1), ("soul_lily", 1)]))
        d.set(x, 0, z, "minecraft:soul_soil")
    d.chest(gx0 + 1, 1, gz0 + 1, "nether_garden", facing="south")
    d.set(gx1 - 1, 1, gz0 + 1, "minecraft:composter", level="4")
    d.set(gx0 + 1, 1, gz1 - 1, "potted_glowcap")
    d.set(5, 3, 5, "soul_firefly_jar", hanging="true", waterlogged="false")
    d.set(gx1 - 1, 1, gz1 - 1, "potted_soul_lily")
    # Fleurs semees dans les pelouses (elles ne genent pas le passage)
    for x in range(3, S - 3):
        for z in range(3, S - 3):
            if d.blocks.get((x, 0, z), ("",))[0].endswith("nether_grass_block") \
                    and d.blocks[(x, 1, z)][0].endswith(":air") and d.rng.random() < 0.18:
                flower(x, z)
    d.connect_fences()
    d.save("nether_garden")


def hidden_cache():
    """Cache secrete : une trappe sertie dans un petit dallage mene, par une echelle, a une salle au tresor enterree."""
    G = 6  # y du sol du Nether dans le gabarit (la structure est enfoncee de G + 1 blocs)
    d = Dungeon(7, G + 3, 7, "hidden_cache")
    d.box(0, 0, 0, 6, G - 1, 6, CRYPT_WALL)
    d.air(1, 1, 1, 5, 3, 5)
    d.box(1, 0, 1, 5, 0, 5, "minecraft:polished_blackstone_bricks")
    for y in range(1, G):
        d.set(3, y, 5, "minecraft:ladder", facing="north", waterlogged="false")
    # Dallage au ras du sol : la trappe y est sertie, elle ne flotte jamais au-dessus du terrain
    d.box(0, G, 0, 6, G, 6, [("minecraft:polished_blackstone_bricks", 60), ("minecraft:cracked_polished_blackstone_bricks", 30),
                             ("minecraft:blackstone", 10)])
    d.set(3, G, 5, "withered_trapdoor", facing="north", half="top", open="false", powered="false", waterlogged="false")
    d.chest(1, 1, 1, "small_treasure", facing="south")
    d.chest(5, 1, 1, "small_treasure", facing="south")
    d.set(3, 1, 1, "minecraft:gold_block")
    d.lantern(3, 2, 1, "onyx_lantern")
    d.set(1, 1, 4, "minecraft:barrel", facing="up", open="false")
    d.skull(5, 1, 4)
    # Reperes bien visibles : feu de camp des ames sur une botte de foin (haute colonne de fumee) et cairn a lanterne
    d.set(1, G, 1, "minecraft:hay_block", axis="y")
    d.set(1, G + 1, 1, "minecraft:soul_campfire", facing="north", lit="true", signal_fire="true", waterlogged="false")
    d.set(5, G + 1, 4, "minecraft:polished_blackstone_brick_wall", up="true")
    d.lantern(5, G + 2, 4)
    d.save("hidden_cache")


def nomad_caravan():
    """Caravane nomade : grande tente du marchand a rayures, tente de repos, chariot bache, enclos a lezards,
    feu de camp entoure de bancs, lampadaires et etals."""
    S = 25
    d = Dungeon(S, 9, S, "nomad_caravan")
    d.air(0, 1, 0, S - 1, 8, S - 1)
    d.box(0, 0, 0, S - 1, 0, S - 1, [("minecraft:netherrack", 35), ("wart_moss", 25), ("minecraft:soul_soil", 25), ("minecraft:blackstone", 15)])
    d.box(11, 0, 1, 13, 0, S - 2, "minecraft:soul_soil")
    d.box(1, 0, 11, S - 2, 0, 13, "minecraft:soul_soil")

    def striped_tent(x0, z0, width, depth, colors, open_face="south"):
        """Tente en pignon : la toile monte par paliers vers le faitage, rayee de deux couleurs."""
        half = width // 2
        for k in range(half + 1):
            y = 1 + k
            for z in range(z0, z0 + depth):
                color = colors[(z - z0) % 2]
                for x in (x0 + k, x0 + width - 1 - k):
                    d.set(x, y, z, color)
                    for yy in range(1, y):
                        if k == 0:
                            d.set(x, yy, z, color)
        # Pignon du fond ferme, avant ouvert avec deux mats
        for k in range(half + 1):
            for x in range(x0 + k, x0 + width - k):
                for y in range(1, 2 + k):
                    if open_face == "south":
                        d.set(x, y, z0, colors[0])
        for x in (x0, x0 + width - 1):
            d.pillar(x, z0 + depth, 1, 3, "wartstalk_fence")
        d.box(x0 + 1, 0, z0 + 1, x0 + width - 2, 0, z0 + depth - 1, "wartstalk_planks")
        d.box(x0 + 2, 1, z0 + 1, x0 + width - 3, 1, z0 + depth - 2, "minecraft:red_carpet")

    # Grande tente du marchand (nord-ouest) : comptoir, marchandises, coffre
    striped_tent(1, 1, 9, 8, ("minecraft:red_wool", "minecraft:white_wool"))
    for x in range(2, 9):
        d.set(x, 1, 8, "minecraft:barrel", facing="up", open="false") if x in (2, 8) else d.set(x, 1, 8, "wartstalk_slab", type="top", waterlogged="false")
    d.chest(2, 1, 2, "nomad_caravan", facing="south")
    d.barrel(8, 1, 2, "nomad_caravan")
    d.set(3, 1, 2, "infernal_cauldron", doses="0")
    d.set(7, 1, 2, "minecraft:barrel", facing="up", open="false")
    d.set(5, 4, 4, "minecraft:lantern", hanging="true", waterlogged="false")
    d.set(5, 5, 4, "minecraft:chain", axis="y", waterlogged="false")
    d.guard(5, 1, 5, "piglin_merchant")
    # Tente de repos (nord-est) : deux lits, coffre
    striped_tent(15, 1, 9, 8, ("minecraft:orange_wool", "minecraft:yellow_wool"))
    for x in (17, 21):
        d.set(x, 1, 3, "minecraft:red_bed", facing="south", part="head", occupied="false")
        d.set(x, 1, 4, "minecraft:red_bed", facing="south", part="foot", occupied="false")
    d.chest(19, 1, 2, "nomad_caravan", facing="south")
    d.set(19, 4, 5, "minecraft:soul_lantern", hanging="true", waterlogged="false")
    # Feu de camp central et bancs
    d.set(12, 1, 12, "minecraft:campfire", lit="true", facing="north", signal_fire="false", waterlogged="false")
    for (x, z, axis) in ((9, 12, "z"), (15, 12, "z"), (12, 9, "x"), (12, 15, "x")):
        for k in (-1, 0, 1):
            dx, dz = (0, k) if axis == "z" else (k, 0)
            d.set(x + dx, 1, z + dz, "stripped_wartstalk_stem", axis=axis)
    # Chariot bache (sud-ouest)
    d.box(3, 1, 16, 10, 1, 20, "wartstalk_planks")
    for x in (4, 9):
        for z in (15, 21):
            d.set(x, 1, z, "wartstalk_stem", axis="z")
            d.set(x, 0, z, "wartstalk_stem", axis="z")
    for x in range(3, 11):
        for y in (2, 3):
            d.set(x, y, 16, "minecraft:white_wool")
            d.set(x, y, 20, "minecraft:white_wool")
        d.box(x, 4, 16, x, 4, 20, "minecraft:white_wool")
        if x % 3 == 0:
            d.box(x, 2, 16, x, 4, 16, "minecraft:brown_wool")
            d.box(x, 2, 20, x, 4, 20, "minecraft:brown_wool")
            d.box(x, 4, 17, x, 4, 19, "minecraft:brown_wool")
    for (x, z) in ((5, 17), (6, 19), (8, 18)):
        d.set(x, 2, z, "minecraft:barrel", facing="up", open="false")
    d.set(7, 2, 17, "minecraft:hay_block", axis="x")
    d.chest(9, 2, 18, "nomad_caravan", facing="west")
    d.box(1, 1, 18, 2, 1, 18, "wartstalk_fence")
    # Enclos a lezards (sud-est)
    for x in range(15, 24):
        for z in (15, 23):
            d.set(x, 1, z, "wartstalk_fence")
    for z in range(15, 24):
        for x in (15, 23):
            d.set(x, 1, z, "wartstalk_fence")
    d.set(15, 1, 19, "wartstalk_fence_gate", facing="west", in_wall="false", open="false", powered="false")
    for (x, z) in ((21, 17), (22, 21)):
        d.set(x, 1, z, "fire_berry_bush", age="3")
    d.set(17, 1, 21, "minecraft:composter", level="7")
    d.set(22, 1, 16, "minecraft:hay_block", axis="y")
    d.guard(19, 1, 18, "cinder_lizard")
    d.guard(20, 1, 20, "cinder_lizard")
    # Lampadaires aux quatre coins de la place et etal
    for (x, z) in ((10, 10), (14, 10), (10, 14), (14, 14)):
        d.pillar(x, z, 1, 2, "wartstalk_fence")
        d.lantern(x, 3, z, "minecraft:lantern")
    d.set(16, 1, 11, "lava_trap")
    d.lantern(4, 1, 11, "ward_lantern")
    d.set(1, 1, 13, "crimson_bellflower")
    d.set(23, 1, 10, "minecraft:crimson_roots")
    d.connect_fences()
    d.save("nomad_caravan")


def basalt_forge():
    """Forge de basalte : repaire du Golem de basalte. Un cercle de colonnes brisees autour d'une forge antique
    (bassin de lave, enclume d'onyx, hauts fourneaux, cheminee fumante) ; le golem y monte la garde."""
    S = 25
    c = S // 2
    d = Dungeon(S, 14, S, "basalt_forge")
    d.air(0, 1, 0, S - 1, 13, S - 1)
    floor = [("minecraft:polished_blackstone", 35), ("minecraft:blackstone", 25), ("minecraft:smooth_basalt", 25),
             ("minecraft:polished_blackstone_bricks", 15)]
    for x in range(S):
        for z in range(S):
            r = ((x - c) ** 2 + (z - c) ** 2) ** 0.5
            if r <= 11.5:
                d.set(x, 0, z, "minecraft:magma_block" if 7.5 < r <= 8.3 and d.rng.random() < 0.6 else d.pick(floor))
    # Colonnes de basalte en cercle, certaines brisees, avec braseros
    for i in range(10):
        a = i * 2 * math.pi / 10
        x, z = c + round(math.cos(a) * 10), c + round(math.sin(a) * 10)
        h = 8 if i % 3 else 4 + d.rng.randint(0, 2)
        for y in range(1, h + 1):
            d.set(x, y, z, "minecraft:polished_basalt", axis="y")
        if i % 3:
            d.set(x, h + 1, z, "minecraft:netherrack")
            d.set(x, h + 2, z, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false")
        else:
            d.set(x + 1, 1, z, "minecraft:basalt", axis="x")
    # Forge centrale : estrade, bassin de lave, enclume, hauts fourneaux et cheminee
    d.box(c - 3, 1, c - 3, c + 3, 1, c + 3, "minecraft:polished_blackstone_bricks")
    for (x0, x1, z0, z1, face) in ((c - 3, c + 3, c + 4, c + 4, "north"), (c - 3, c + 3, c - 4, c - 4, "south"),
                                   (c + 4, c + 4, c - 3, c + 3, "west"), (c - 4, c - 4, c - 3, c + 3, "east")):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                d.stair(x, 1, z, "minecraft:polished_blackstone_brick_stairs", face)
    d.box(c - 1, 1, c - 1, c + 1, 1, c + 1, "minecraft:lava", level="0")
    d.set(c - 2, 2, c + 2, "onyx_anvil", facing="east")
    for x in (c - 2, c + 2):
        d.set(x, 2, c - 2, "minecraft:blast_furnace", facing="south", lit="true")
    d.pillar(c, c - 3, 2, 7, "minecraft:polished_basalt")
    d.set(c, 8, c - 3, "minecraft:campfire", lit="true", facing="north", signal_fire="true", waterlogged="false")
    d.set(c, 7, c - 4, "minecraft:hay_block", axis="y")
    d.chest(c + 2, 2, c + 2, "basalt_forge", facing="west")
    for (x, z) in ((c - 3, c - 3), (c + 3, c - 3), (c - 3, c + 3), (c + 3, c + 3)):
        d.pillar(x, z, 2, 3, "minecraft:polished_blackstone_brick_wall")
        d.lantern(x, 4, z)
    # Le golem garde la forge
    d.guard(c, 2, c + 6, "basalt_golem")
    d.save("basalt_forge")


def wyvern_nest():
    """Nid de wyverne : piton de basalte et de pierre noire, nid d'os et de bois fletri au sommet, un oeuf dans
    un coffre et une wyverne sauvage qui le garde (on peut l'apprivoiser avec de la viande)."""
    d = Dungeon(13, 17, 13, "wyvern_nest")
    c = 6
    rock = [("minecraft:basalt", 45), ("minecraft:blackstone", 35), ("minecraft:smooth_basalt", 20)]
    for y in range(0, 12):
        radius = 5.2 - y * 0.25 + (0.6 if y < 2 else 0.0)
        for x in range(13):
            for z in range(13):
                r = ((x - c) ** 2 + (z - c) ** 2) ** 0.5
                if r <= radius + d.rng.uniform(-0.4, 0.4):
                    block = d.pick(rock)
                    if "basalt" in block and "smooth" not in block:
                        d.set(x, y, z, block, axis="y")
                    else:
                        d.set(x, y, z, block)
    # Coupe du nid : couronne d'os et de tiges fletries, fond de cendre
    for x in range(13):
        for z in range(13):
            r = ((x - c) ** 2 + (z - c) ** 2) ** 0.5
            if r <= 3.0:
                d.set(x, 11, z, "ash_block")
                for y in range(12, 15):
                    d.set(x, y, z, AIR)
            elif r <= 4.6:
                d.set(x, 12, z, "minecraft:bone_block" if d.rng.random() < 0.55 else "withered_stem", axis="y")
                if d.rng.random() < 0.45:
                    d.set(x, 13, z, "withered_stem" if d.rng.random() < 0.6 else "minecraft:bone_block", axis="x")
    d.chest(c, 12, c, "wyvern_nest", facing="south")
    d.set(c - 1, 12, c + 1, "minecraft:skeleton_skull", rotation="3")
    d.set(c + 1, 12, c - 1, "minecraft:magma_block")
    # Prises pour grimper : quelques corniches en spirale
    for k, y in enumerate(range(1, 11, 2)):
        angle = k * 1.3
        # Premiere case vide en partant du centre : la prise est toujours collee a la roche
        reach = 0.0
        while True:
            reach += 0.5
            x = c + round(math.cos(angle) * reach)
            z = c + round(math.sin(angle) * reach)
            if (x, y, z) not in d.blocks:
                break
        d.set(x, y, z, "minecraft:polished_blackstone_slab", type="top", waterlogged="false")
    # Le gardien du nid : il faut le vaincre pour obtenir son oeuf
    d.entity(c + 2.5, 12, c + 0.5, "ash_wyvern", extra={"NestGuardian": Byte(1)})
    d.save("wyvern_nest")


def generate():
    nomad_caravan()
    basalt_forge()
    wyvern_nest()
    traveler_camp()
    nether_garden()
    hidden_cache()
    onyx_citadel()
    infernal_sanctum()
    withered_catacombs()
