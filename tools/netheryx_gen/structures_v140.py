"""Donjons 1.4.0 : de vrais complexes a explorer, creuses sous une petite entree en surface.

Parcours d'un donjon :
    entree en surface -> escalier -> salle d'accueil -> ROTONDE -> hall de la descente -> ARENE du boss
    -> a la mort du boss -> COFFRE-FORT, enferme dans une maconnerie indestructible (camouflee en briques du donjon).
Autour de ce chemin, trois AILES facultatives finissent chacune par une chambre gardee et un beau tresor. Des salles
optionnelles (tresors derriere une porte de fer, mimiques) se greffent sur la salle d'accueil.

Chaque donjon existe en plusieurs gabarits : le plan des ailes et le contenu des salles sont tires au sort a la generation
des ressources, parmi un pool de salles (couloir de pieges, fosse a franchir, salle gardee, tresor aux mimiques...).
verify() parcourt chaque gabarit a pied : un plan invalide fait echouer la generation.
"""
from collections import deque

from .common import mid
from .nbt import Byte, Double, Float, TypedList, TAG_COMPOUND, TAG_STRING
from .structures import (AIR, CRYPT_FLOOR, CRYPT_WALL, MAGMA_FLOOR, MAGMA_WALL, ONYX_FLOOR, ONYX_WALL, Dungeon)
from .structures_v134 import furnish

KEEPS = 3
SIZE = 75
CELL = 15
FLOOR = 17       # sol des salles de l'etage principal
TOP = 24         # dernier bloc d'air des salles
GROUND = 27      # sol du Nether dans le gabarit (enfoncement de la structure)
# Centre de la forteresse dans le gabarit, et emprise a verifier en surface (voir keep_exterior)
FRAME = 8
ENTRANCE = (37 + FRAME, 37 + FRAME)
ENTRANCE_FOOTPRINT = 60
GATE_REACH = 56  # du centre au bout du pont d'entree, vers le sud du gabarit
ARENA = (21, 25, 53, 57)  # x0, z0, x1, z1 de l'interieur de l'arene (rayon 16 autour du boss)
BOSS_POS = (37, 2, 41)
LAST_CELL = SIZE - 1
ARENA_TOP = 13

THEMES = {
    "onyx_citadel": {
        "wall": ONYX_WALL, "floor": ONYX_FLOOR, "pillar": "polished_onyx", "stairs": "onyx_brick_stairs",
        "rail": "onyx_brick_wall", "lantern": "onyx_lantern", "plate": "minecraft:polished_blackstone_pressure_plate",
        "loot": "onyx_citadel", "vault_loot": "onyx_citadel_vault", "warded": "onyx", "pedestal": "polished_onyx",
        "themes": ("library", "armory", "prison", "shrine"), "boss": "onyx_sentinel", "pit": "minecraft:crying_obsidian",
        "mobs": ("ash_wraith", "ember_stalker", "withered_knight"), "warden": "withered_knight", "accent": "minecraft:crying_obsidian",
    },
    "infernal_sanctum": {
        "wall": MAGMA_WALL, "floor": MAGMA_FLOOR, "pillar": "magma_bricks", "stairs": "minecraft:polished_blackstone_brick_stairs",
        "rail": "magma_brick_wall", "lantern": "minecraft:lantern", "plate": "minecraft:polished_blackstone_pressure_plate",
        "loot": "infernal_sanctum", "vault_loot": "infernal_sanctum_vault", "warded": "magma", "pedestal": "magma_bricks",
        "themes": ("forge", "shrine", "prison", "armory"), "boss": "infernal_warlock", "pit": "minecraft:magma_block",
        "mobs": ("minecraft:magma_cube", "ember_stalker", "minecraft:blaze"), "warden": "ember_stalker", "accent": "minecraft:magma_block",
    },
    "withered_catacombs": {
        "wall": CRYPT_WALL, "floor": CRYPT_FLOOR, "pillar": "minecraft:polished_blackstone_bricks",
        "stairs": "minecraft:polished_blackstone_brick_stairs", "rail": "minecraft:polished_blackstone_brick_wall",
        "lantern": "minecraft:soul_lantern", "plate": "minecraft:polished_blackstone_pressure_plate",
        "loot": "withered_catacombs", "vault_loot": "withered_catacombs_vault", "warded": "blackstone",
        "pedestal": "minecraft:chiseled_polished_blackstone", "themes": ("ossuary", "library", "prison", "shrine"),
        "boss": "withered_champion", "pit": "minecraft:soul_soil",
        "mobs": ("withered_knight", "ash_wraith", "minecraft:wither_skeleton"), "warden": "withered_knight", "accent": "minecraft:bone_block",
    },
}

# Ailes : plans possibles, en cases de la grille (la derniere case est la chambre de la clef)
WEST_WINGS = (((1, 2), (0, 2), (0, 1), (0, 0)), ((1, 2), (0, 2), (0, 3), (0, 4)))
EAST_WINGS = (((3, 2), (4, 2), (4, 1), (4, 0)), ((3, 2), (4, 2), (4, 3), (4, 4)))
NORTH_WINGS = (((2, 0), (1, 0)), ((2, 0), (3, 0)))
ROOM_POOL = ("guarded", "dart_hall", "pit_crossing", "mimic_vault", "sentinel_pillars")


def interior(cell):
    """Interieur d'une case : 11 x 11."""
    x0, z0 = cell[0] * CELL + 2, cell[1] * CELL + 2
    return x0, z0, x0 + 10, z0 + 10


class KeepTemplate(Dungeon):
    """Gabarit dont le repere est celui des salles (0..74) : l'exterieur deborde en negatif, finish() decale le tout."""

    def __init__(self, seed, height):
        from .keep_exterior import GATE_TIP
        super().__init__(SIZE, height, SIZE, seed)
        self.x_range = (-FRAME, SIZE + FRAME)
        self.z_range = (-FRAME, GATE_TIP + 1)

    def contains(self, p):
        return self.x_range[0] <= p[0] < self.x_range[1] and 0 <= p[1] < self.size[1] and self.z_range[0] <= p[2] < self.z_range[1]

    def set(self, x, y, z, name, nbt=None, **props):
        if self.contains((x, y, z)):
            self.blocks[(x, y, z)] = (mid(name), tuple(sorted((k, str(v).lower()) for k, v in props.items())))
            if nbt is not None:
                self.block_nbt[(x, y, z)] = nbt
            else:
                self.block_nbt.pop((x, y, z), None)

    def finish(self):
        shift = lambda p: (p[0] + FRAME, p[1], p[2] + FRAME)
        self.blocks = {shift(p): b for p, b in self.blocks.items()}
        self.block_nbt = {shift(p): n for p, n in self.block_nbt.items()}
        for entity in self.entities:
            entity["pos"] = [Double(float(entity["pos"][0]) + FRAME), entity["pos"][1], Double(float(entity["pos"][2]) + FRAME)]
            entity["blockPos"] = [entity["blockPos"][0] + FRAME, entity["blockPos"][1], entity["blockPos"][2] + FRAME]
        self.size = (self.x_range[1] - self.x_range[0], self.size[1], self.z_range[1] - self.z_range[0])


class Keep:
    """Base commune aux trois donjons (keep_variants.py) : salles, ailes, arene et coffre-fort, a la hauteur dy."""
    dy = 0            # hauteur de l'arene dans le gabarit
    height = GROUND + 40
    max_floor = 100   # sol le plus haut ou la structure tient sous la bedrock
    color = 0

    def __init__(self, name, index):
        self.name = name
        self.t = THEMES[name]
        self.d = KeepTemplate(f"{name}_keep_{index}", self.height)
        self.traps = []       # mecanismes poses
        self.spots = []       # lieux que la verification a pied doit atteindre
        self.index = index
        self.rooms = []       # salles a meubler une fois toutes les portes percees
        self.keys = []        # positions des coffres a clef

    # --- Briques de base --------------------------------------------------------------------------

    def room(self, x0, z0, x1, z1, furnish_theme=None):
        d, t = self.d, self.t
        d.box(x0 - 2, FLOOR - 1, z0 - 2, x1 + 2, TOP + 2, z1 + 2, t["wall"])
        d.box(x0, FLOOR, z0, x1, FLOOR, z1, t["floor"])
        d.air(x0, FLOOR + 1, z0, x1, TOP, z1)
        if furnish_theme:
            self.rooms.append((x0, z0, x1, z1, furnish_theme))

    def door(self, a, b):
        """Ouverture de 3 de large et 3 de haut entre deux cases voisines."""
        ax0, az0, ax1, az1 = interior(a)
        bx0, bz0, bx1, bz1 = interior(b)
        if a[0] == b[0]:
            cx = (ax0 + ax1) // 2
            z0, z1 = (az1 + 1, bz0 - 1) if a[1] < b[1] else (bz1 + 1, az0 - 1)
            self.d.air(cx - 1, FLOOR + 1, z0, cx + 1, FLOOR + 3, z1)
            self.d.box(cx - 1, FLOOR, z0, cx + 1, FLOOR, z1, self.t["floor"])
        else:
            cz = (az0 + az1) // 2
            x0, x1 = (ax1 + 1, bx0 - 1) if a[0] < b[0] else (bx1 + 1, ax0 - 1)
            self.d.air(x0, FLOOR + 1, cz - 1, x1, FLOOR + 3, cz + 1)
            self.d.box(x0, FLOOR, cz - 1, x1, FLOOR, cz + 1, self.t["floor"])

    def lights(self, x0, z0, x1, z1):
        for (x, z) in ((x0 + 2, z0 + 2), (x1 - 2, z0 + 2), (x0 + 2, z1 - 2), (x1 - 2, z1 - 2)):
            self.d.hanging_lantern(x, TOP + 1, z, 1, self.t["lantern"])

    def corners(self, x0, z0, x1, z1):
        """Les quatre coins d'une salle (2 x 2), hors du passage en croix qui relie les portes."""
        return ((x0 + 1, z0 + 1), (x1 - 1, z0 + 1), (x0 + 1, z1 - 1), (x1 - 1, z1 - 1))

    def mob(self):
        return self.d.rng.choice(self.t["mobs"])

    # --- Pool de salles ---------------------------------------------------------------------------

    def guarded(self, x0, z0, x1, z1):
        """Salle gardee : piliers qui coupent la vue, generateurs dans les coins, un coffre."""
        d, t = self.d, self.t
        corners = self.corners(x0, z0, x1, z1)
        for (x, z) in corners[:2]:
            d.spawner(x, FLOOR + 1, z, self.mob())
        for (x, z) in corners[2:]:
            d.pillar(x, z, FLOOR + 1, TOP, t["pillar"])
        d.chest(x0, FLOOR + 1, z1 - 3, t["loot"], facing="east")
        self.lights(x0, z0, x1, z1)

    def dart_hall(self, x0, z0, x1, z1):
        """Couloir de fleches : des plaques visibles declenchent les distributeurs du sol. On les enjambe, ou on court."""
        d, t = self.d, self.t
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        arrows = TypedList(TAG_COMPOUND, [{"Slot": Byte(0), "id": "minecraft:arrow", "Count": Byte(32)}])
        for (x, z) in ((cx - 2, cz - 1), (cx + 2, cz + 1), (cx, cz - 3), (cx, cz + 3), (cx - 3, cz + 1), (cx + 3, cz - 1), (cx + 1, cz), (cx - 1, cz + 2)):
            d.set(x, FLOOR, z, "minecraft:dispenser", nbt={"id": "minecraft:dispenser", "Items": arrows}, facing="up", triggered="false")
            d.set(x, FLOOR + 1, z, t["plate"], powered="false")
        for (x, z) in self.corners(x0, z0, x1, z1)[1:3]:
            d.pillar(x, z, FLOOR + 1, TOP, t["pillar"])
        d.chest(x1, FLOOR + 1, z0 + 1, t["loot"], facing="west")
        self.lights(x0, z0, x1, z1)

    def pit_crossing(self, x0, z0, x1, z1):
        """Fosse : on saute de plot en plot. Tomber n'est pas mortel, mais il faut remonter par l'echelle, sous les monstres."""
        d, t = self.d, self.t
        d.box(x0 - 2, FLOOR - 5, z0 - 2, x1 + 2, FLOOR - 1, z1 + 2, t["wall"])
        d.air(x0, FLOOR - 3, z0, x1, FLOOR, z1)
        d.box(x0, FLOOR - 4, z0, x1, FLOOR - 4, z1, t["pit"])
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        # Plots en croix : deux blocs de large, un bloc de vide entre chacun (saut facile, mais pas pendant un combat)
        for k in (-5, -4, -1, 0, 1, 4, 5):
            for w in (-1, 0, 1):
                d.set(cx + k, FLOOR, cz + w, d.pick(t["floor"]))
                d.set(cx + w, FLOOR, cz + k, d.pick(t["floor"]))
        for (x, z) in self.corners(x0, z0, x1, z1):
            d.pillar(x, z, FLOOR - 3, TOP, t["pillar"])
        # Echelles de secours sur chaque mur, jusqu'au plot de la porte
        for (x, z, facing) in ((x0, cz + 2, "east"), (x1, cz - 2, "west"), (cx + 2, z0, "south"), (cx - 2, z1, "north")):
            for y in range(FLOOR - 3, FLOOR + 2):
                d.set(x, y, z, "minecraft:ladder", facing=facing, waterlogged="false")
        d.spawner(cx - 3, FLOOR - 3, cz - 3, self.mob())
        d.chest(cx + 3, FLOOR - 3, cz + 3, t["loot"], facing="north")
        self.lights(x0, z0, x1, z1)

    def mimic_vault(self, x0, z0, x1, z1):
        """Salle aux coffres : un seul est vrai, les mimiques attendent le geste de trop."""
        d, t = self.d, self.t
        spots = list(self.corners(x0, z0, x1, z1))
        d.rng.shuffle(spots)
        d.chest(spots[0][0], FLOOR + 1, spots[0][1], t["loot"], facing="south")
        for (x, z) in spots[1:]:
            d.guard(x, FLOOR + 1, z, "mimic")
            d.set(x, FLOOR, z, t["pedestal"])
        self.lights(x0, z0, x1, z1)

    def sentinel_pillars(self, x0, z0, x1, z1):
        """Quatre gardes immobiles jusqu'a ce qu'on entre : pas de generateur, mais un vrai combat de groupe."""
        d, t = self.d, self.t
        for (x, z) in self.corners(x0, z0, x1, z1):
            d.guard(x, FLOOR + 1, z, t["warden"] if d.rng.random() < 0.5 else self.mob())
        for (x, z) in ((x0 + 3, z0 + 3), (x1 - 3, z0 + 3), (x0 + 3, z1 - 3), (x1 - 3, z1 - 3)):
            d.pillar(x, z, FLOOR + 1, TOP, t["pillar"])
        d.chest(x1, FLOOR + 1, z1 - 3, t["loot"], facing="west")
        self.lights(x0, z0, x1, z1)

    def key_chamber(self, x0, z0, x1, z1):
        """Fin d'aile : un beau tresor sur son autel, un gardien d'elite et ses renforts."""
        d, t = self.d, self.t
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        d.box(cx - 1, FLOOR + 1, cz - 1, cx + 1, FLOOR + 1, cz + 1, t["pedestal"])
        d.chest(cx, FLOOR + 2, cz, "wing_treasure", facing="south")
        self.keys.append((cx, FLOOR + 2, cz))
        d.guard(cx, FLOOR + 1, z0 + 2, "rune_warden")
        for (x, z) in self.corners(x0, z0, x1, z1):
            d.pillar(x, z, FLOOR + 1, TOP, t["pillar"])
            d.set(x, FLOOR + 3, z, t["accent"])
        d.spawner(x0 + 1, FLOOR + 1, cz, self.mob())
        d.spawner(x1 - 1, FLOOR + 1, cz, self.mob())
        self.lights(x0, z0, x1, z1)

    # --- Plan general -----------------------------------------------------------------------------

    def wing(self, path, used):
        previous = (2, 2) if path[0][1] == 2 else (2, 1)
        for i, cell in enumerate(path):
            x0, z0, x1, z1 = interior(cell)
            self.room(x0, z0, x1, z1, self.d.rng.choice(self.t["themes"]))
            self.door(previous, cell)
            if i == len(path) - 1:
                self.key_chamber(x0, z0, x1, z1)
            else:
                kinds = [k for k in ROOM_POOL if k not in used]
                kind = self.d.rng.choice(kinds or ROOM_POOL)
                used.append(kind)
                getattr(self, kind)(x0, z0, x1, z1)
            previous = cell

    def treasure_room(self, cell, door_from):
        """Salle optionnelle : porte de fer, un levier a cote. Pas de clef ici, juste du butin pour qui fouille."""
        d, t = self.d, self.t
        x0, z0, x1, z1 = interior(cell)
        self.room(x0, z0, x1, z1, d.rng.choice(t["themes"]))
        self.door(door_from, cell)
        cz = (z0 + z1) // 2
        # La porte de fer ferme l'ouverture, cote salle d'accueil (les murs lateraux reposent sur le mur existant)
        west = cell[0] < door_from[0]
        door_x = x1 + 2 if west else x0 - 2
        for z in (cz - 1, cz + 1):
            d.box(door_x, FLOOR + 1, z, door_x, FLOOR + 3, z, t["wall"])
        d.set(door_x, FLOOR + 3, cz, d.pick(t["wall"]))
        hinge = "right" if west else "left"
        facing = "east" if west else "west"
        d.set(door_x, FLOOR + 1, cz, "minecraft:iron_door", facing=facing, half="lower", hinge=hinge, open="false", powered="false")
        d.set(door_x, FLOOR + 2, cz, "minecraft:iron_door", facing=facing, half="upper", hinge=hinge, open="false", powered="false")
        lever_x = door_x + 1 if west else door_x - 1
        d.set(lever_x, FLOOR + 2, cz - 1, "minecraft:lever", face="wall", facing=facing, powered="false")
        self.mimic_vault(x0, z0, x1, z1) if d.rng.random() < 0.5 else self.guarded(x0, z0, x1, z1)

    def hub(self):
        d, t = self.d, self.t
        x0, z0, x1, z1 = interior((2, 2))
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        self.room(x0, z0, x1, z1)
        # Sol a motif et braseros : le coeur du donjon
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if (x - cx) ** 2 + (z - cz) ** 2 <= 9:
                    d.set(x, FLOOR, z, t["accent"])
        for (x, z) in ((x0 + 1, z0 + 1), (x1 - 1, z0 + 1), (x0 + 1, z1 - 1), (x1 - 1, z1 - 1)):
            d.pillar(x, z, FLOOR + 1, TOP, t["pillar"])
        for (x, z) in ((cx - 3, cz - 3), (cx + 3, cz - 3), (cx, cz + 3)):
            d.set(x, FLOOR + 1, z, t["pedestal"])
            d.set(x, FLOOR + 2, z, t["lantern"], hanging="false", waterlogged="false")
        d.hanging_lantern(cx, TOP + 1, cz, 2, t["lantern"])

    def stair_down(self):
        """Massif de l'escalier vers l'arene : il monte vers l'est, de l'arene au sol du niveau des salles."""
        self.d.box(25, 0, 18, 47, FLOOR - 1, 24, self.t["wall"])

    def stair_rails(self):
        """Parapet sur le sol plein, autour de la tremie : le sol s'arrete a x = 39, l'arrivee reste libre a l'est."""
        d, t = self.d, self.t
        d.air(26, 2, 20, 28, 5, 22)
        d.box(26, 1, 20, 28, 1, 22, t["floor"])
        d.air(26, 2, 23, 28, 5, 24)
        d.rail(39, FLOOR + 1, 19, 43, 19, t["rail"])
        d.rail(39, FLOOR + 1, 23, 43, 23, t["rail"])
        d.rail(39, FLOOR + 1, 20, 39, 22, t["rail"])

    def descent(self):
        """Hall de la descente : trois cases d'un seul tenant et le grand escalier vers l'arene."""
        d, t = self.d, self.t
        hx0, hz0, hx1, hz1 = interior((1, 1))[0], interior((1, 1))[1], interior((3, 1))[2], interior((3, 1))[3]
        self.room(hx0, hz0, hx1, hz1, None)
        self.stair_down()
        d.stairs_run_x(29, (20, 21, 22), 2, 16, 1, t["stairs"], t["wall"])
        self.stair_rails()
        for x in (hx0 + 3, hx1 - 3):
            d.pillar(x, 18, FLOOR + 1, TOP, t["pillar"])
            d.pillar(x, 26, FLOOR + 1, TOP, t["pillar"])
        for (x, z) in ((hx0 + 8, 22), (hx1 - 8, 22), (37, 18), (37, 26)):
            d.hanging_lantern(x, TOP + 1, z, 2, t["lantern"])

    def arena_style(self, cx, cz, x0, z0, x1, z1):
        """Decor propre a chaque donjon : colonnes, sol, lumiere."""
        raise NotImplementedError

    def arena(self):
        d, t = self.d, self.t
        dy = self.dy
        x0, z0, x1, z1 = ARENA
        cx, cz = BOSS_POS[0], BOSS_POS[2]
        d.box(x0 - 2, dy, z0 - 2, x1 + 2, dy + ARENA_TOP + 2, z1 + 2, t["wall"])
        d.box(x0, dy + 1, z0, x1, dy + 1, z1, t["floor"])
        d.air(x0, dy + 2, z0, x1, dy + ARENA_TOP, z1)
        self.arena_style(cx, cz, x0, z0, x1, z1)
        d.guard(BOSS_POS[0], dy + BOSS_POS[1], BOSS_POS[2], t["boss"])
        # Coffre-fort, ouvert dans le mur sud
        d.air(35, dy + 2, 58, 39, dy + 4, 59)
        d.vault(31, dy + 1, 60, 43, dy + 8, 70, t["warded"], (35, dy + 2, 60, 39, dy + 4, 60))
        for x in (34, 40):
            d.chest(x, dy + 2, 68, t["vault_loot"], facing="north")
        d.set(37, dy + 2, 68, t["accent"])
        d.lantern(32, dy + 2, 62, t["lantern"])
        d.lantern(42, dy + 2, 62, t["lantern"])

    def entrance(self):
        """Grand escalier, de la cour de la forteresse a la salle d'accueil."""
        d, t = self.d, self.t
        d.box(33, FLOOR - 1, 60, 41, GROUND - 1, 74, t["wall"])
        d.air(36, FLOOR + 1, 58, 38, FLOOR + 3, 62)
        d.box(36, FLOOR, 58, 38, FLOOR, 62, t["floor"])
        d.stairs_run(range(36, 39), 72, GROUND, GROUND - FLOOR, -1, -1, t["stairs"], t["wall"])

    def build(self):
        d, t = self.d, self.t
        self.arena()
        self.descent()
        self.hub()
        hall_center = (2, 1)
        self.door((2, 2), hall_center)
        # Salle d'accueil
        x0, z0, x1, z1 = interior((2, 3))
        self.room(x0, z0, x1, z1, d.rng.choice(t["themes"]))
        self.door((2, 3), (2, 2))
        self.lights(x0, z0, x1, z1)
        # Ailes
        used = []
        wings = (d.rng.choice(WEST_WINGS), d.rng.choice(EAST_WINGS), d.rng.choice(NORTH_WINGS))
        for path in wings:
            self.wing(path, used)
        # Salles optionnelles greffees sur la salle d'accueil, si la place est libre
        taken = {cell for path in wings for cell in path}
        for cell in ((1, 3), (3, 3)):
            if cell not in taken:
                self.treasure_room(cell, (2, 3))
        # La forteresse de surface : enceinte, tours, ponts, cours, et tous les acces aux salles
        self.exterior()
        # Meubles en dernier : toutes les portes sont percees, le passage en croix reste libre
        for (x0, z0, x1, z1, theme) in self.rooms:
            cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
            cross = {(x, z) for x in range(x0, x1 + 1) for z in range(z0, z1 + 1) if abs(x - cx) <= 1 or abs(z - cz) <= 1}
            furnish(d, x0, FLOOR, z0, x1, z1, TOP, theme, t["loot"], t["pedestal"], cross)
        self.finish_build()

    def exterior(self):
        raise NotImplementedError

    def finish_build(self):
        verify(self)
        self.d.finish()
        self.d.save(self.name, f"keep_{self.index}")


# ------------------------------------------------------------------------------------------------
# Verification a pied : on marche dans le gabarit comme un joueur
# ------------------------------------------------------------------------------------------------

NON_SOLID = ("minecraft:air", "minecraft:cobweb", "minecraft:ladder", "minecraft:lever", "minecraft:soul_fire", "minecraft:fire",
             "minecraft:candle", "minecraft:skeleton_skull", "minecraft:wither_skeleton_skull")


def _passable(keep, pos):
    name = keep.d.blocks.get(pos)
    if name is None:
        return False  # roche : infranchissable
    name = name[0]
    if name in NON_SOLID or "pressure_plate" in name:
        return True
    if name == "minecraft:iron_door":
        return True  # le levier est juste a cote
    return False


def _standable(keep, pos):
    name = keep.d.blocks.get((pos[0], pos[1] - 1, pos[2]))
    return name is not None and name[0] not in ("minecraft:air", "minecraft:lava") and "fire" not in name[0]


def _walk(keep, start, open_vault):
    """Toutes les cases ou un joueur peut se tenir, a partir de start (marche, chute, saut d'un bloc de vide, echelles)."""
    def free(p):
        if not keep.d.contains(p):
            return False
        name = keep.d.blocks.get(p)
        if open_vault and name is not None and name[0] in ("netheryx:warded_bricks", "netheryx:guardian_seal"):
            return True
        return _passable(keep, p)

    seen = {start}
    queue = deque([start])
    while queue:
        x, y, z = queue.popleft()
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for reach in (1, 2):
                nx, nz = x + dx * reach, z + dz * reach
                if reach == 2 and not (free((x + dx, y, z + dz)) and free((x + dx, y + 1, z + dz))):
                    continue
                for ny in ((y + 1, y, y - 1, y - 2, y - 3) if reach == 1 else (y,)):
                    p = (nx, ny, nz)
                    if p in seen or not free(p) or not free((nx, ny + 1, nz)):
                        continue
                    if ny > y and not free((x, y + 2, z)):
                        continue
                    if ny < y and not all(free((nx, k, nz)) for k in range(ny, y + 2)):
                        continue
                    on_ladder = keep.d.blocks.get(p, ("", ))[0] == "minecraft:ladder"
                    if _standable(keep, p) or on_ladder:
                        seen.add(p)
                        queue.append(p)
        # Echelles : monter et descendre
        if keep.d.blocks.get((x, y, z), ("", ))[0] == "minecraft:ladder":
            for ny in (y + 1, y - 1):
                p = (x, ny, z)
                if p not in seen and free(p) and free((x, ny + 1, z)):
                    seen.add(p)
                    queue.append(p)
    return seen


def _near(reached, target):
    tx, ty, tz = target
    return any(abs(x - tx) <= 2 and abs(z - tz) <= 2 and abs(y - ty) <= 3 for (x, y, z) in reached)


OFFSET = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0)}
NOT_A_SUPPORT = ("minecraft:air", "minecraft:lava", "minecraft:water", "minecraft:cobweb", "minecraft:fire", "minecraft:soul_fire")


def unsupported(d, world_rock=True):
    """Blocs qui tomberaient en objet au chargement : lanternes, bougies, echelles et leviers sans support.
    world_rock : une case laissee vide dans le gabarit garde la roche du monde (salles creusees)."""
    problems = []

    def solid(pos):
        entry = d.blocks.get(pos)
        if entry is None:
            return world_rock
        return entry[0] not in NOT_A_SUPPORT

    for pos, (name, props) in d.blocks.items():
        props = dict(props)
        x, y, z = pos
        if name.endswith("lantern") and "hanging" in props:
            above = d.blocks.get((x, y + 1, z), (None,))[0]
            if props["hanging"] == "true":
                if not solid((x, y + 1, z)) and above != "minecraft:chain":
                    problems.append((name, pos, "suspendue dans le vide"))
            elif not solid((x, y - 1, z)):
                problems.append((name, pos, "posee sur le vide"))
        elif name == "minecraft:chain" and props.get("axis") == "y":
            if not solid((x, y + 1, z)) and d.blocks.get((x, y + 1, z), (None,))[0] != "minecraft:chain":
                pass  # une chaine tient seule : seule la lanterne en dessous compte
        elif name == "minecraft:candle" and not solid((x, y - 1, z)):
            problems.append((name, pos, "posee sur le vide"))
        elif name == "minecraft:ladder":
            dx, _, dz = OFFSET[props["facing"]]
            if not solid((x - dx, y, z - dz)):
                problems.append((name, pos, "sans mur derriere"))
        elif name == "minecraft:lever" and props.get("face") == "wall":
            dx, _, dz = OFFSET[props["facing"]]
            if not solid((x - dx, y, z - dz)):
                problems.append((name, pos, "sans mur derriere"))
    return problems


def verify(keep):
    from .keep_exterior import GATE_TIP
    label = f"{keep.name}/keep_{keep.index}"
    problems = unsupported(keep.d)
    # Un parapet tient sur quelque chose : sinon, il a l'air de voler
    for pos, (name, _) in keep.d.blocks.items():
        if name == mid(keep.t["rail"]) and keep.d.blocks.get((pos[0], pos[1] - 1, pos[2]), (None,))[0] == AIR:
            problems.append((name, pos, "parapet dans le vide"))
    assert not problems, f"{label} : blocs sans support {problems[:8]}"
    start = (37, GROUND - 2, GATE_TIP)
    assert keep.d.blocks.get(start, ("",))[0] == AIR, f"{label} : entree bouchee"
    alive = _walk(keep, start, False)
    assert len(keep.keys) == 3, f"{label} : il faut trois tresors d'aile"
    for key in keep.keys:
        assert _near(alive, key), f"{label} : tresor inaccessible en {key}"
    dy = keep.dy
    assert _near(alive, (BOSS_POS[0], dy + BOSS_POS[1], BOSS_POS[2])), f"{label} : arene inaccessible"
    assert keep.traps, f"{label} : aucun mecanisme"
    for spot in keep.spots:
        assert _near(alive, spot), f"{label} : acces inaccessible en {spot}"
    in_vault = [p for p in alive if 32 <= p[0] <= 42 and 61 <= p[2] <= 69 and dy < p[1] <= dy + 7]
    assert not in_vault, f"{label} : coffre-fort accessible avant la mort du boss ({in_vault[:3]})"
    # Coque du coffre-fort : six faces completes, sans aucun trou
    shell = {p for p, b in keep.d.blocks.items() if b[0] in ("netheryx:warded_bricks", "netheryx:guardian_seal")}
    for x in range(31, 44):
        for y in range(dy + 1, dy + 9):
            for z in range(60, 71):
                if x in (31, 43) or y in (dy + 1, dy + 8) or z in (60, 70):
                    assert (x, y, z) in shell, f"{label} : trou dans la coque du coffre-fort en {(x, y, z)}"
    after = _walk(keep, start, True)
    assert _near(after, (34, dy + 2, 68)), f"{label} : coffre-fort inaccessible apres la mort du boss"
    print(f"NetheryX : {label} verifie ({len(alive)} cases accessibles, {len(keep.traps)} mecanismes)")


def generate():
    from .keep_variants import KEEP_CLASSES
    for name in THEMES:
        for index in range(KEEPS):
            KEEP_CLASSES[name](name, index).build()
