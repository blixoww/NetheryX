"""Exterieur des donjons (1.4.3) : une vraie forteresse, a la maniere des forteresses du Nether, posee sur les salles enterrees.

Vu du dehors :
    - une ENCEINTE crenelee, a contreforts, qui repose sur des arcades (visibles quand le terrain est plus bas) ;
    - quatre TOURS d'angle et un DONJON central, relies par un chemin de ronde et une croix de PONTS sur arches ;
    - un CHATELET au sud, precede d'un pont : l'entree principale, et trois POTERNES a l'ouest, a l'est et au nord ;
    - quatre COURS a fouiller, chacune avec son decor.
Les salles enterrees ne se voient plus jamais : l'enceinte les habille sur les quatre cotes et la cour les couvre.

Le repere est celui des salles (0..74) : l'enceinte deborde en coordonnees negatives, KeepTemplate.finish() decale le tout.
"""
from .structures import AIR
from .structures_v134 import furnish
from .structures_v140 import FLOOR, GROUND, SIZE

G = GROUND
LAST = SIZE - 1          # 74
MARGIN = 7               # epaisseur de l'enceinte autour des salles
FRAME = 8                # debord du gabarit (corniches des tours comprises)
SOUTH = 13               # pont d'entree, en plus de la marge, au sud
RING = (4, 5)            # profondeur du mur d'enceinte (a partir du bord des salles)
WALK = G + 7             # sol du chemin de ronde et des ponts
HEIGHT = G + 31
ARCH_TOP = 12            # sommet des arcades de fondation
GATE_TIP = LAST + MARGIN + SOUTH - 1
CX = 37                  # axe du donjon


def outward(side, u, v):
    """Coordonnees (x, z) d'un point du cote donne : u le long du mur, v vers l'exterieur (v = 1 : premier bloc hors des salles)."""
    return {"W": (-v, u), "E": (LAST + v, u), "N": (u, -v), "S": (u, LAST + v)}[side]


INWARD = {"W": "east", "E": "west", "N": "south", "S": "north"}
OUTWARD = {"W": "west", "E": "east", "N": "north", "S": "south"}


def flip(direction, flip_x, flip_z):
    if flip_x and direction in ("east", "west"):
        return "west" if direction == "east" else "east"
    if flip_z and direction in ("north", "south"):
        return "south" if direction == "north" else "north"
    return direction


class Exterior:
    def __init__(self, keep):
        self.keep = keep
        self.d = keep.d
        self.t = keep.t
        self.rng = keep.d.rng

    # --- Cour ------------------------------------------------------------------------------------

    def court(self):
        d, t = self.d, self.t
        d.box(-3, G - 1, -3, LAST + 3, G - 1, LAST + 3, t["wall"])
        d.box(-3, G, -3, LAST + 3, G, LAST + 3, t["floor"])
        d.air(-3, G + 1, -3, LAST + 3, G + 12, LAST + 3)
        # Allees pavees vers les quatre portes du donjon
        for k in range(-3, LAST + 4):
            for w in (-1, 0, 1):
                d.set(CX + w, G, k, t["pillar"] if w else d.pick(t["floor"]))
                d.set(k, G, CX + w, t["pillar"] if w else d.pick(t["floor"]))

    # --- Enceinte --------------------------------------------------------------------------------

    def ring(self):
        for side in "WENS":
            for u in range(-3, LAST + 4):
                self.ring_column(side, u)
            self.posts_and_lights(side)

    def ring_column(self, side, u):
        d, t = self.d, self.t
        p = (u + 1) % 15                 # 0, 1, 2 : contrefort ; le reste : travee d'arcade
        buttress = p <= 2
        span = min(p - 2, 15 - p)        # distance au contrefort le plus proche
        for v in RING:
            x, z = outward(side, u, v)
            for y in range(0, WALK):
                if not buttress and y < min(ARCH_TOP, 3 + span * 2):
                    continue             # sous l'arche : rien, le terrain reste tel quel
                # Le pied des contreforts porte la fondation prolongee a la generation
                d.set(x, y, z, t["pillar"] if (buttress and y < ARCH_TOP) or y in (20, G) else d.pick(t["wall"]))
            d.set(x, WALK, z, d.pick(t["floor"]))
        # Chemin de ronde : deux blocs de plus vers l'interieur
        for v in (2, 3):
            x, z = outward(side, u, v)
            d.set(x, WALK, z, d.pick(t["floor"]))
        x, z = outward(side, u, 2)
        d.set(x, WALK + 1, z, t["rail"], up="true")
        x, z = outward(side, u, 1)
        d.stair(x, WALK, z, t["stairs"], OUTWARD[side], "top")
        # Parapet et merlons
        x, z = outward(side, u, 5)
        d.set(x, WALK + 1, z, d.pick(t["wall"]))
        if u % 2 == 0:
            d.set(x, WALK + 2, z, d.pick(t["wall"]))
        # Meurtrieres
        if u % 5 == 2 and not buttress:
            for v in RING:
                x, z = outward(side, u, v)
                d.air(x, G + 3, z, x, G + 4, z)
        if buttress:
            self.buttress(side, u)
        else:
            x, z = outward(side, u, 6)
            d.stair(x, WALK - 1, z, t["stairs"], INWARD[side], "top")

    def buttress(self, side, u):
        d, t = self.d, self.t
        x, z = outward(side, u, 6)
        for y in range(0, WALK):
            d.set(x, y, z, t["pillar"] if y < ARCH_TOP else d.pick(t["wall"]))
        d.stair(x, WALK, z, t["stairs"], INWARD[side])
        x, z = outward(side, u, 7)
        for y in range(0, G + 3):
            d.set(x, y, z, t["pillar"] if y < ARCH_TOP else d.pick(t["wall"]))
        d.stair(x, G + 3, z, t["stairs"], INWARD[side])

    def posts_and_lights(self, side):
        d, t = self.d, self.t
        for u in range(9, LAST - 8, 15):
            x, z = outward(side, u, 3)
            d.lantern(x, WALK + 1, z, t["lantern"])
            # Une lumiere sur le parapet : on repere la forteresse de loin
            x, z = outward(side, u, 5)
            d.set(x, WALK + 2, z, d.pick(t["wall"]))
            d.lantern(x, WALK + 3, z, t["lantern"])

    # --- Tours d'angle ----------------------------------------------------------------------------

    def tower(self, flip_x, flip_z):
        d, t = self.d, self.t

        def at(x, z):
            return (LAST - x if flip_x else x), (LAST - z if flip_z else z)

        def put_box(x0, y0, z0, x1, y1, z1, palette):
            for x in range(x0, x1 + 1):
                for z in range(z0, z1 + 1):
                    px, pz = at(x, z)
                    d.box(px, y0, pz, px, y1, pz, palette)

        top = G + 18 + self.rng.randrange(4)
        # Fut plein sous la cour, seulement hors de l'emprise des salles
        for x in range(-7, 4):
            for z in range(-7, 4):
                if x < 0 or z < 0:
                    px, pz = at(x, z)
                    d.box(px, 0, pz, px, ARCH_TOP - 1, pz, t["pillar"])
                    d.box(px, ARCH_TOP, pz, px, G - 1, pz, t["wall"])
        put_box(-7, G, -7, 3, top, 3, t["wall"])
        put_box(-5, G + 1, -5, 1, top - 1, 1, AIR)
        for y in (G, WALK, G + 14, top):
            put_box(-5, y, -5, 1, y, 1, t["floor"])
        # Cordons et angles dans la pierre d'accent du donjon
        for x in (-7, 3):
            for z in (-7, 3):
                put_box(x, G + 1, z, x, top, z, t["pillar"])
        for k in range(-7, 4):
            for (x, z) in ((k, -7), (k, 3), (-7, k), (3, k)):
                put_box(x, G + 14, z, x, G + 14, z, t["accent"])
        # Portes vers la cour et vers le chemin de ronde, sur les deux faces interieures
        for (a, b) in ((-2, -1), (-4, -3)):
            y0, y1 = (G + 1, G + 3) if a == -2 else (WALK + 1, WALK + 2)
            put_box(2, y0, a, 3, y1, b, AIR)
            put_box(a, y0, 2, b, y1, 3, AIR)
            if a == -4:
                put_box(2, WALK, a, 3, WALK, b, t["floor"])
                put_box(a, WALK, 2, b, WALK, 3, t["floor"])
        # Fenetres de la salle haute
        for k in (-3, -1):
            put_box(k, G + 16, -7, k, G + 17, -6, AIR)
            put_box(-7, G + 16, k, -6, G + 17, k, AIR)
        # Echelle : du sol jusqu'a la terrasse, le long du mur
        for y in range(G + 1, top + 1):
            px, pz = at(1, 1)
            d.set(px, y, pz, "minecraft:ladder", facing=flip("west", flip_x, flip_z), waterlogged="false")
        # Crenelage
        for k in range(-7, 4):
            for (x, z) in ((k, -7), (k, 3), (-7, k), (3, k)):
                px, pz = at(x, z)
                d.set(px, top + 1, pz, d.pick(t["wall"]))
                if (k + 7) % 2 == 0:
                    d.set(px, top + 2, pz, d.pick(t["wall"]))
        for k in range(-6, 3):
            for (x, z, direction) in ((k, -8, "south"), (k, 4, "north"), (-8, k, "east"), (4, k, "west")):
                px, pz = at(x, z)
                d.stair(px, top - 1, pz, t["stairs"], flip(direction, flip_x, flip_z), "top")
        for (x, z) in ((-7, -7), (3, -7), (-7, 3), (3, 3)):
            px, pz = at(x, z)
            d.set(px, top + 2, pz, t["pillar"])
            d.lantern(px, top + 3, pz, t["lantern"])
        self.crown(at, top)
        # Du butin et des gardes pour qui prend la peine de monter
        px, pz = at(-4, -4)
        d.chest(px, G + 15, pz, t["loot"], facing=flip("south", flip_x, flip_z))
        px, pz = at(-1, -4)
        d.spawner(px, WALK + 1, pz, self.keep.mob())
        for (y, ceiling) in ((G + 1, WALK), (WALK + 1, G + 14), (G + 15, top)):
            px, pz = at(-2, -2)
            d.hanging_lantern(px, ceiling, pz, 1, t["lantern"])

    def crown(self, at, top):
        """Sommet de tour, propre a chaque donjon."""
        d, t = self.d, self.t
        name = self.keep.name
        cx, cz = at(-2, -2)
        if name == "onyx_citadel":
            # Fleche d'onyx
            d.box(cx, top + 1, cz, cx, top + 5, cz, t["pillar"])
            d.set(cx, top + 6, cz, t["accent"])
            d.lantern(cx, top + 7, cz, t["lantern"])
        elif name == "infernal_sanctum":
            # Brasier
            d.box(cx - 1, top + 1, cz - 1, cx + 1, top + 1, cz + 1, t["pillar"])
            d.set(cx, top + 1, cz, "minecraft:netherrack")
            d.set(cx, top + 2, cz, "minecraft:fire")
        else:
            # Gibet de cranes et flamme des ames
            d.set(cx, top + 1, cz, "minecraft:soul_soil")
            d.set(cx, top + 2, cz, "minecraft:soul_fire")
            for (dx, dz) in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
                d.set(cx + dx, top + 1, cz + dz, "minecraft:bone_block", axis="y")
                d.skull(cx + dx, top + 2, cz + dz, wither=True)

    # --- Donjon central --------------------------------------------------------------------------

    def keep_tower(self):
        d, t = self.d, self.t
        top = G + 21
        x0, z0, x1, z1 = 30, 30, 44, 44
        d.box(x0, G + 1, z0, x1, top, z1, t["wall"])
        d.air(x0 + 2, G + 1, z0 + 2, x1 - 2, top - 1, z1 - 2)
        for y in (WALK, G + 14, top):
            d.box(x0 + 2, y, z0 + 2, x1 - 2, y, z1 - 2, t["floor"])
        # Contreforts d'angle et cordons
        for (x, z) in ((x0, z0), (x1, z0), (x0, z1), (x1, z1)):
            d.box(x, G + 1, z, x, top + 2, z, t["pillar"])
        for y in (WALK, G + 14):
            for k in range(x0, x1 + 1):
                for (x, z) in ((k, z0), (k, z1), (x0, k), (x1, k)):
                    d.set(x, y, z, t["accent"])
        # Grandes baies, de part et d'autre de chaque porte
        for k in (33, 41):
            for (x, z, wx, wz) in ((k, z0, k, z0 + 1), (k, z1, k, z1 - 1), (x0, k, x0 + 1, k), (x1, k, x1 - 1, k)):
                for (a, b) in ((G + 2, G + 5), (G + 9, G + 12), (G + 16, G + 19)):
                    d.air(x, a, z, x, b, z)
                    d.air(wx, a, wz, wx, b, wz)
                    # Barreaux des baies : on voit dedans, on n'y entre que par les portes
                    for y in range(a, b + 1):
                        d.set(x, y, z, t["rail"], up="true")
        # Portes : cour et ponts, sur les quatre faces
        for (y0, y1) in ((G + 1, G + 3), (WALK + 1, WALK + 3)):
            d.air(CX - 1, y0, z0, CX + 1, y1, z0 + 1)
            d.air(CX - 1, y0, z1 - 1, CX + 1, y1, z1)
            d.air(x0, y0, CX - 1, x0 + 1, y1, CX + 1)
            d.air(x1 - 1, y0, CX - 1, x1, y1, CX + 1)
        for (fx0, fz0, fx1, fz1) in ((CX - 1, z0, CX + 1, z0 + 1), (CX - 1, z1 - 1, CX + 1, z1), (x0, CX - 1, x0 + 1, CX + 1), (x1 - 1, CX - 1, x1, CX + 1)):
            d.box(fx0, WALK, fz0, fx1, WALK, fz1, t["floor"])
        # Crenelage et tourelles
        for k in range(x0, x1 + 1):
            for (x, z) in ((k, z0), (k, z1), (x0, k), (x1, k)):
                d.set(x, top + 1, z, d.pick(t["wall"]))
                if (k - x0) % 2 == 0:
                    d.set(x, top + 2, z, d.pick(t["wall"]))
        for k in range(x0 + 1, x1):
            d.stair(k, top - 1, z0 - 1, t["stairs"], "south", "top")
            d.stair(k, top - 1, z1 + 1, t["stairs"], "north", "top")
            d.stair(x0 - 1, top - 1, k, t["stairs"], "east", "top")
            d.stair(x1 + 1, top - 1, k, t["stairs"], "west", "top")
        for (x, z) in ((x0, z0), (x1, z0), (x0, z1), (x1, z1)):
            d.box(x, top + 1, z, x, top + 4, z, t["pillar"])
            d.lantern(x, top + 5, z, t["lantern"])
        # Fleche centrale : on la voit depuis l'autre bout de la caverne
        d.box(CX - 1, top + 1, CX - 1, CX + 1, top + 4, CX + 1, t["wall"])
        d.box(CX, top + 5, CX, CX, top + 7, CX, t["pillar"])
        d.set(CX, top + 8, CX, t["accent"])
        # Salles meublees : passage en croix libre, comme en bas
        cross = {(x, z) for x in range(x0, x1 + 1) for z in range(z0, z1 + 1) if abs(x - CX) <= 1 or abs(z - CX) <= 1}
        for (floor, ceil) in ((G, WALK - 1), (WALK, G + 13), (G + 14, top - 1)):
            furnish(d, x0 + 2, floor, z0 + 2, x1 - 2, z1 - 2, ceil, self.rng.choice(t["themes"]), t["loot"], t["pedestal"], cross)
        for (x, z) in ((34, 34), (40, 34), (34, 40), (40, 40)):
            for ceiling in (WALK, G + 14, top):
                d.hanging_lantern(x, ceiling, z, 1, t["lantern"])
        d.spawner(40, WALK + 1, 40, self.keep.mob())
        d.spawner(34, G + 15, 40, self.keep.mob())
        d.chest(41, G + 15, 41, t["loot"], facing="west")
        # Echelle : de la rotonde enterree jusqu'a la terrasse. Un raccourci vers le coeur du donjon, pour qui l'a trouve.
        d.air(32, FLOOR + 1, 34, 32, top, 34)
        for y in range(FLOOR + 1, top + 1):
            d.set(32, y, 34, "minecraft:ladder", facing="east", waterlogged="false")
            if y >= G - 2:
                d.set(31, y, 34, d.pick(t["wall"]))

    # --- Ponts sur arches -------------------------------------------------------------------------

    def bridges(self):
        self.bridge_z(-1, 29)
        self.bridge_z(45, 76)
        self.bridge_x(-1, 29)
        self.bridge_x(45, LAST + 1)

    def bridge_z(self, z0, z1):
        d, t = self.d, self.t
        for z in range(z0, z1 + 1):
            d.box(CX - 2, WALK, z, CX + 2, WALK, z, t["floor"])
            d.set(CX - 2, WALK + 1, z, t["rail"], up="true")
            d.set(CX + 2, WALK + 1, z, t["rail"], up="true")
            d.stair(CX - 3, WALK, z, t["stairs"], "east", "top")
            d.stair(CX + 3, WALK, z, t["stairs"], "west", "top")
        for z in range(z0 + 3, z1 - 1, 7):
            for x in (CX - 2, CX + 2):
                d.box(x, G + 1, z, x, WALK - 1, z, t["pillar"])
                d.stair(x, WALK - 1, z - 1, t["stairs"], "south", "top")
                d.stair(x, WALK - 1, z + 1, t["stairs"], "north", "top")
            for (x, direction) in ((CX - 3, "east"), (CX + 3, "west")):
                d.stair(x, G + 1, z, t["stairs"], direction)

    def bridge_x(self, x0, x1):
        d, t = self.d, self.t
        for x in range(x0, x1 + 1):
            d.box(x, WALK, CX - 2, x, WALK, CX + 2, t["floor"])
            d.set(x, WALK + 1, CX - 2, t["rail"], up="true")
            d.set(x, WALK + 1, CX + 2, t["rail"], up="true")
            d.stair(x, WALK, CX - 3, t["stairs"], "south", "top")
            d.stair(x, WALK, CX + 3, t["stairs"], "north", "top")
        for x in range(x0 + 3, x1 - 1, 7):
            for z in (CX - 2, CX + 2):
                d.box(x, G + 1, z, x, WALK - 1, z, t["pillar"])
                d.stair(x - 1, WALK - 1, z, t["stairs"], "east", "top")
                d.stair(x + 1, WALK - 1, z, t["stairs"], "west", "top")
            for (z, direction) in ((CX - 3, "south"), (CX + 3, "north")):
                d.stair(x, G + 1, z, t["stairs"], direction)

    # --- Chatelet et pont d'entree ----------------------------------------------------------------

    def gatehouse(self):
        d, t = self.d, self.t
        south = LAST + MARGIN            # 81 : front des tours du chatelet
        top = WALK + 7
        for (x0, x1) in ((28, 34), (40, 46)):
            for x in range(x0, x1 + 1):
                for z in range(LAST + 1, south + 1):
                    d.box(x, 0, z, x, ARCH_TOP - 1, z, t["pillar"])
                    d.box(x, ARCH_TOP, z, x, G, z, t["wall"])
            d.box(x0, G + 1, LAST, x1, top, south, t["wall"])
            for (x, z) in ((x0, south), (x1, south), (x0, LAST), (x1, LAST)):
                d.box(x, G + 1, z, x, top + 2, z, t["pillar"])
            for x in range(x0, x1 + 1):
                d.set(x, top + 1, south, d.pick(t["wall"]))
                d.set(x, top + 1, LAST, d.pick(t["wall"]))
                if (x - x0) % 2 == 0:
                    d.set(x, top + 2, south, d.pick(t["wall"]))
                    d.set(x, top + 2, LAST, d.pick(t["wall"]))
                d.stair(x, top - 1, south + 1, t["stairs"], "north", "top")
            for z in range(LAST, south + 1):
                d.set(x0, top + 1, z, d.pick(t["wall"]))
                d.set(x1, top + 1, z, d.pick(t["wall"]))
            for (x, z) in ((x0, south), (x1, south), (x0, LAST), (x1, LAST)):
                d.lantern(x, top + 3, z, t["lantern"])
            # Meurtrieres de facade
            mid = (x0 + x1) // 2
            d.air(mid, G + 6, south - 1, mid, G + 9, south)
        # Voute de la porte et salle au-dessus
        d.box(35, G + 6, LAST, 39, top, south, t["wall"])
        d.air(35, G + 1, LAST, 39, G + 5, south + 1)
        d.box(35, G, LAST, 39, G, south, t["floor"])
        # Arc de la porte
        for z in (south, LAST):
            d.stair(35, G + 5, z, t["stairs"], "east", "top")
            d.stair(39, G + 5, z, t["stairs"], "west", "top")
        for x in range(35, 40):
            d.set(x, top + 1, south, d.pick(t["wall"]))
            if x % 2 == 1:
                d.set(x, top + 2, south, d.pick(t["wall"]))
        d.set(37, G + 7, south, t["accent"])
        d.set(36, G + 7, south, t["pillar"])
        d.set(38, G + 7, south, t["pillar"])
        d.hanging_lantern(37, G + 6, LAST + 3, 1, t["lantern"])
        # Le chemin de ronde sud traverse le chatelet
        d.box(28, WALK, LAST + 2, 46, WALK, LAST + 3, t["floor"])
        d.air(28, WALK + 1, LAST + 2, 46, WALK + 3, LAST + 3)
        d.box(36, WALK, LAST, 38, WALK, LAST + 1, t["floor"])
        d.air(36, WALK + 1, LAST, 38, WALK + 3, LAST + 1)
        # Pont d'entree, puis marches jusqu'au sol
        deck_end = GATE_TIP - 3
        d.air(35, G + 1, south + 1, 39, G + 5, GATE_TIP)
        for z in range(south + 1, deck_end + 1):
            d.box(35, G, z, 39, G, z, t["floor"])
            d.set(35, G + 1, z, t["rail"], up="true")
            d.set(39, G + 1, z, t["rail"], up="true")
        for z in (south + 2, deck_end):
            d.box(34, 0, z, 40, ARCH_TOP - 1, z, t["pillar"])
            d.box(34, ARCH_TOP, z, 40, G - 1, z, t["wall"])
        d.stairs_run(range(35, 40), deck_end + 1, G - 1, 3, 1, -1, t["stairs"], t["wall"])
        d.box(34, 0, GATE_TIP, 40, ARCH_TOP - 1, GATE_TIP, t["pillar"])
        d.box(34, ARCH_TOP, GATE_TIP, 40, G - 4, GATE_TIP, t["wall"])

    # --- Poternes et escaliers de service ---------------------------------------------------------

    def postern(self, side):
        """Une porte dans l'enceinte, et juste a cote un escalier qui plonge dans l'aile la plus proche."""
        d, t = self.d, self.t
        for v in RING:
            x0, z0 = outward(side, 50, v)
            x1, z1 = outward(side, 52, v)
            d.air(min(x0, x1), G + 1, min(z0, z1), max(x0, x1), G + 4, max(z0, z1))
        for u in (49, 53):
            for v in (5, 6, 7):
                x, z = outward(side, u, v)
                d.box(x, G + 1, z, x, G + 5, z, t["pillar"])
        for u in (50, 51, 52):
            x, z = outward(side, u, 6)
            d.stair(x, G + 5, z, t["stairs"], INWARD[side], "top")
            x, z = outward(side, u, 5)
            d.set(x, G + 5, z, t["accent"] if u == 51 else t["pillar"])
        if side in "WE":
            self.side_stair(side)
        else:
            self.north_stair()

    def side_stair(self, side):
        """Escalier le long de l'enceinte, de la cour vers la salle (0, 2) ou (4, 2)."""
        d, t = self.d, self.t
        cols = [outward(side, 0, v)[0] for v in (1, 2, 3)]
        lo, hi = min(cols), max(cols)
        wall0, wall1 = (LAST - 1, LAST) if side == "E" else (0, 1)
        d.box(wall0, FLOOR - 1, 34, wall1, G - 1, 50, t["wall"])
        d.box(lo, FLOOR - 1, 34, hi, G - 1, 35, t["wall"])
        d.box(lo, FLOOR - 1, 48, hi, G - 1, 50, t["wall"])
        d.box(lo, FLOOR - 1, 36, hi, FLOOR, 37, t["floor"])
        d.air(lo, FLOOR + 1, 36, hi, FLOOR + 3, 37)
        d.box(lo, FLOOR + 4, 36, hi, G - 1, 37, t["wall"])
        d.stairs_run(cols, 47, G, G - FLOOR, -1, -1, t["stairs"], t["wall"])
        # Ouverture dans la salle
        d.air(wall0, FLOOR + 1, 36, wall1, FLOOR + 3, 38)
        d.box(wall0, FLOOR, 36, wall1, FLOOR, 38, t["floor"])
        # Parapet autour de la tremie : la cour n'est percee qu'au-dessus des cinq dernieres marches
        edge = LAST if side == "E" else 0
        for z in range(42, 48):
            d.set(edge, G + 1, z, t["rail"], up="true")
        for x in cols:
            d.set(x, G + 1, 42, t["rail"], up="true")

    def north_stair(self):
        """Le meme escalier, au nord, vers la salle (2, 0)."""
        d, t = self.d, self.t
        d.box(34, FLOOR - 1, 0, 50, G - 1, 1, t["wall"])
        d.box(34, FLOOR - 1, -3, 35, G - 1, -1, t["wall"])
        d.box(48, FLOOR - 1, -3, 50, G - 1, -1, t["wall"])
        d.box(36, FLOOR - 1, -3, 37, FLOOR, -1, t["floor"])
        d.air(36, FLOOR + 1, -3, 37, FLOOR + 3, -1)
        d.box(36, FLOOR + 4, -3, 37, G - 1, -1, t["wall"])
        d.stairs_run_x(38, (-3, -2, -1), FLOOR + 1, G - FLOOR, 1, t["stairs"], t["wall"])
        d.air(36, FLOOR + 1, 0, 38, FLOOR + 3, 1)
        d.box(36, FLOOR, 0, 38, FLOOR, 1, t["floor"])
        for x in range(42, 48):
            d.set(x, G + 1, 0, t["rail"], up="true")
        for z in (-3, -2, -1):
            d.set(42, G + 1, z, t["rail"], up="true")

    # --- Cours ------------------------------------------------------------------------------------

    def yards(self):
        kinds = ["ruin", "shrine", "camp", "graves"]
        self.rng.shuffle(kinds)
        centers = ((17, 17), (57, 17), (17, 57), (57, 57))
        for (center, kind) in zip(centers, kinds):
            getattr(self, f"yard_{kind}")(*center)

    def yard_ruin(self, cx, cz):
        """Un corps de logis effondre : des pans de mur, un coffre dans les gravats."""
        d, t = self.d, self.t
        for k in range(-6, 7):
            for (x, z) in ((cx + k, cz - 6), (cx + k, cz + 6), (cx - 6, cz + k), (cx + 6, cz + k)):
                if abs(k) <= 1:
                    continue
                height = self.rng.randrange(1, 6)
                d.box(x, G + 1, z, x, G + height, z, t["wall"])
        for _ in range(14):
            x, z = cx + self.rng.randrange(-5, 6), cz + self.rng.randrange(-5, 6)
            d.set(x, G + 1, z, d.pick(t["wall"]))
        d.air(cx - 1, G + 1, cz - 1, cx + 1, G + 2, cz + 1)
        d.chest(cx, G + 1, cz, t["loot"], facing="south")
        d.guard(cx + 2, G + 1, cz + 2, self.keep.mob())
        d.guard(cx - 2, G + 1, cz - 2, self.keep.mob())

    def yard_shrine(self, cx, cz):
        """Un autel a ciel ouvert, garde par un generateur."""
        d, t = self.d, self.t
        d.box(cx - 4, G + 1, cz - 4, cx + 4, G + 1, cz + 4, t["pillar"])
        d.box(cx - 2, G + 2, cz - 2, cx + 2, G + 2, cz + 2, t["wall"])
        d.set(cx, G + 3, cz, t["accent"])
        d.spawner(cx, G + 4, cz, self.keep.mob())
        for (dx, dz) in ((-4, -4), (4, -4), (-4, 4), (4, 4)):
            d.box(cx + dx, G + 2, cz + dz, cx + dx, G + 5, cz + dz, t["pillar"])
            d.lantern(cx + dx, G + 6, cz + dz, t["lantern"])

    def yard_camp(self, cx, cz):
        """Le campement de ceux qui ont essaye avant vous : feu, tonneaux, cranes."""
        d, t = self.d, self.t
        d.set(cx, G + 1, cz, "minecraft:campfire", lit="true", signal_fire="false", waterlogged="false", facing="north")
        for (dx, dz) in ((-3, -1), (3, 1), (-1, 3), (1, -3)):
            d.set(cx + dx, G + 1, cz + dz, "minecraft:barrel", facing="up", open="false")
        d.barrel(cx + 4, G + 1, cz - 3, t["loot"])
        for (dx, dz) in ((-4, 3), (4, 3), (0, -5)):
            d.skull(cx + dx, G + 1, cz + dz)
        d.guard(cx - 2, G + 1, cz - 3, self.keep.mob())

    def yard_graves(self, cx, cz):
        """Des rangees de stèles, et ce qui en sort quand on s'approche."""
        d, t = self.d, self.t
        for dx in range(-6, 7, 4):
            for dz in range(-6, 7, 4):
                if abs(dx) <= 2 and abs(dz) <= 2:
                    continue
                d.box(cx + dx, G + 1, cz + dz, cx + dx, G + 2, cz + dz, t["pillar"])
                d.set(cx + dx, G + 3, cz + dz, t["rail"], up="true")
        d.chest(cx, G + 1, cz, t["loot"], facing="north")
        d.spawner(cx - 2, G + 1, cz - 2, self.keep.mob())

    def lamp_posts(self):
        d, t = self.d, self.t
        for (x, z) in ((10, 33), (64, 33), (10, 41), (64, 41), (33, 10), (41, 10), (33, 64), (41, 64)):
            d.box(x, G + 1, z, x, G + 3, z, t["rail"], up="true")
            d.lantern(x, G + 4, z, t["lantern"])

    def build(self):
        self.court()
        self.ring()
        self.bridges()
        self.keep_tower()
        for flip_x in (False, True):
            for flip_z in (False, True):
                self.tower(flip_x, flip_z)
        self.gatehouse()
        self.keep.entrance()
        for z in range(67, 73):
            for x in (35, 39):
                if self.d.blocks.get((x, G + 1, z), (AIR,))[0] == AIR:
                    self.d.set(x, G + 1, z, self.t["rail"], up="true")
        for x in (36, 37, 38):
            self.d.set(x, G + 1, 67, self.t["rail"], up="true")
        for side in "WEN":
            self.postern(side)
        self.yards()
        self.lamp_posts()


def build_exterior(keep):
    Exterior(keep).build()
