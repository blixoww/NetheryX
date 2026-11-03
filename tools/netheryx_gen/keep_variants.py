"""Les trois donjons (1.4.4) : chacun a son architecture, son parcours et ses defis, tires des attaques de son boss.

    Sanctuaire infernal (Sorcier)  : temple a gradins et douves de lave ; dessous, des passerelles sur un lac de lave.
                                     Defis : murs de flammes a breche, pluie de meteores, chaleur.
    Catacombes fletries (Champion) : necropole en ruine ; dessous, un labyrinthe sombre, different a chaque gabarit.
                                     Defis : ruees en couloir, obscurite, embuscades dans le dos.
    Citadelle d'onyx (Sentinelle)  : forteresse a tour geante, ON MONTE : trois etages d'epreuves, le boss au sommet.
                                     Defis : faisceau (s'abriter), ondes (sauter), crocs et cristaux (bouger).

Les mecanismes (bloc dungeon_trap) rejouent les attaques du boss, avec les memes annonces : le donjon apprend le combat.
"""
from .keep_exterior import CX, G, WALK, Exterior, outward
from .nbt import Byte, TypedList, TAG_COMPOUND
from .structures import AIR
from .structures_v134 import furnish
from .structures_v140 import ARENA, ARENA_TOP, BOSS_POS, FLOOR, GROUND, SIZE, TOP, Keep

LAST = SIZE - 1
FIRE, ONYX, SOUL = 0, 1, 2


def trap(keep, x, y, z, mode, facing="north", period=100, damage=4.0, length=12, width=3, charges=0, entity=None):
    from .nbt import Float
    nbt = {"id": "netheryx:dungeon_trap", "Mode": mode, "Period": period, "Damage": Float(damage), "Length": length, "Width": width,
           "Color": keep.color, "Charges": charges}
    if entity:
        nbt["Entity"] = entity if ":" in entity else f"netheryx:{entity}"
    keep.d.set(x, y, z, "dungeon_trap", nbt=nbt, facing=facing)
    keep.traps.append((x, y, z, mode))


# ================================================================================================
# Citadelle d'onyx : on monte
# ================================================================================================

class Citadel(Keep):
    color = ONYX
    dy = G + 26
    height = G + 26 + ARENA_TOP + 8
    max_floor = 70

    def arena_style(self, cx, cz, x0, z0, x1, z1):
        d, t = self.d, self.t
        dy = self.dy
        # Arene ronde et a ciel ouvert, au sommet de la tour
        d.air(x0, dy + ARENA_TOP + 1, z0, x1, dy + ARENA_TOP + 2, z1)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if (x - cx) ** 2 + (z - cz) ** 2 > 16.5 ** 2:
                    d.box(x, dy + 2, z, x, dy + ARENA_TOP, z, t["wall"])
        for (dx, dz) in ((-10, -10), (10, -10), (-10, 10), (10, 10), (-13, 0), (13, 0), (0, -13), (0, 13)):
            d.box(cx + dx, dy + 2, cz + dz, cx + dx + 1, dy + ARENA_TOP, cz + dz + 1, t["pillar"])
            d.lantern(cx + dx, dy + ARENA_TOP + 1, cz + dz, t["lantern"])
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if 36 <= (x - cx) ** 2 + (z - cz) ** 2 <= 49:
                    d.set(x, dy + 1, z, t["accent"])

    def descent(self):
        """Pas de descente ici : le hall devient une salle d'armes, l'arene est tout en haut."""
        d, t = self.d, self.t
        hx0, hz0, hx1, hz1 = 17, 17, 57, 27
        self.room(hx0, hz0, hx1, hz1, None)
        for x in range(hx0 + 3, hx1 - 2, 8):
            d.pillar(x, 18, FLOOR + 1, TOP, t["pillar"])
            d.pillar(x, 26, FLOOR + 1, TOP, t["pillar"])
            d.hanging_lantern(x, TOP + 1, 22, 2, t["lantern"])
        d.chest(37, FLOOR + 1, hz0, t["loot"], facing="south")
        d.spawner(25, FLOOR + 1, 22, self.mob())
        d.spawner(49, FLOOR + 1, 22, self.mob())

    def exterior(self):
        CitadelExterior(self).build()


class CitadelExterior(Exterior):
    L1, L2, L3 = G, G + 9, G + 18

    def keep_tower(self):
        """La tour de l'epreuve : trois etages de 33 x 33 sous l'arene."""
        d, t, keep = self.d, self.t, self.keep
        x0, z0, x1, z1 = ARENA
        dy = keep.dy
        # Fondations et fut, cordons a chaque etage
        d.box(x0 - 2, G + 1, z0 - 2, x1 + 2, dy, z1 + 2, t["wall"])
        for (floor, ceiling) in ((self.L1, self.L2 - 1), (self.L2, self.L3 - 1), (self.L3, dy - 1)):
            d.air(x0, floor + 1, z0, x1, ceiling, z1)
            d.box(x0, floor, z0, x1, floor, z1, t["floor"])
            for k in range(x0 - 2, x1 + 3):
                for (x, z) in ((k, z0 - 2), (k, z1 + 2)):
                    d.set(x, floor, z, t["accent"])
            for k in range(z0 - 2, z1 + 3):
                for (x, z) in ((x0 - 2, k), (x1 + 2, k)):
                    d.set(x, floor, z, t["accent"])
            # Hautes fenetres entre les contreforts : la lumiere des salles se voit de loin
            for k in range(x0 + 3, x1 - 2, 6):
                for (x, z) in ((k, z0 - 2), (k, z0 - 1), (k, z1 + 1), (k, z1 + 2)):
                    if abs(k - CX) > 2:
                        d.air(x, floor + 3, z, x, floor + 6, z)
            for k in range(z0 + 3, z1 - 2, 6):
                for (x, z) in ((x0 - 2, k), (x0 - 1, k), (x1 + 1, k), (x1 + 2, k)):
                    if abs(k - 41) > 2 and abs(k - CX) > 2:
                        d.air(x, floor + 3, z, x, floor + 6, z)
        for x in range(x0 - 2, x1 + 3, 6):
            for z in (z0 - 3, z1 + 3):
                # L'abside du coffre-fort s'adosse au mur sud : aucun contrefort ne doit percer sa coque
                top = dy if z == z1 + 3 and 30 <= x <= 44 else dy + ARENA_TOP - 2
                d.box(x, G + 1, z, x, top, z, t["pillar"])
        for z in range(z0 - 2, z1 + 3, 6):
            for x in (x0 - 3, x1 + 3):
                d.box(x, G + 1, z, x, dy + ARENA_TOP - 2, z, t["pillar"])
        # Couronne : parapet creneau et tourelles
        for k in range(x0 - 2, x1 + 3):
            for (x, z) in ((k, z0 - 2), (k, z1 + 2)):
                d.set(x, dy + ARENA_TOP + 3, z, d.pick(t["wall"]))
                if k % 2 == 0:
                    d.set(x, dy + ARENA_TOP + 4, z, d.pick(t["wall"]))
        for k in range(z0 - 2, z1 + 3):
            for (x, z) in ((x0 - 2, k), (x1 + 2, k)):
                d.set(x, dy + ARENA_TOP + 3, z, d.pick(t["wall"]))
                if k % 2 == 0:
                    d.set(x, dy + ARENA_TOP + 4, z, d.pick(t["wall"]))
        # Abside du coffre-fort : posee sur quatre piles au-dessus de la cour
        for (x, z) in ((31, 61), (43, 61), (31, 69), (43, 69)):
            d.box(x - 1, G + 1, z - 1, x + 1, dy, z + 1, t["pillar"])
        # Portes de la cour, sur les quatre faces
        for (fx0, fz0, fx1, fz1) in ((CX - 1, z0 - 2, CX + 1, z0 - 1), (CX - 1, z1 + 1, CX + 1, z1 + 2), (x0 - 2, 40, x0 - 1, 42), (x1 + 1, 40, x1 + 2, 42)):
            d.air(fx0, G + 1, fz0, fx1, G + 4, fz1)
            d.box(fx0, G, fz0, fx1, G, fz1, t["floor"])
            # Et celles des ponts, qui donnent sur la galerie du premier etage
            d.air(fx0, WALK + 1, fz0, fx1, WALK + 3, fz1)
            d.box(fx0, WALK, fz0, fx1, WALK, fz1, t["floor"])
        self.gallery()
        self.beam_hall()
        self.wave_hall()
        self.fang_hall()
        self.climb()
        # Echelle de service : de la rotonde enterree a la salle du faisceau
        d.air(32, FLOOR + 1, 34, 32, G, 34)
        for y in range(FLOOR + 1, G + 1):
            d.set(32, y, 34, "minecraft:ladder", facing="east", waterlogged="false")
            if y >= G - 2:
                d.set(31, y, 34, d.pick(t["wall"]))
        keep.spots += [(32, FLOOR + 1, 36), (37, self.L1 + 1, 29), (37, self.L2 + 1, 29), (37, self.L3 + 1, 29)]

    def gallery(self):
        """Galerie du premier etage, desservie par les ponts : une echelle y monte depuis la salle."""
        d, t = self.d, self.t
        x0, z0, x1, z1 = ARENA
        for k in range(x0, x1 + 1):
            for (x, z) in ((k, z0), (k, z0 + 1), (k, z1), (k, z1 - 1)):
                d.set(x, WALK, z, d.pick(t["floor"]))
        for k in range(z0, z1 + 1):
            for (x, z) in ((x0, k), (x0 + 1, k), (x1, k), (x1 - 1, k)):
                d.set(x, WALK, z, d.pick(t["floor"]))
        # Entre deux fenetres : l'echelle a besoin d'un mur plein derriere elle
        for y in range(G + 1, WALK + 1):
            d.set(x1, y, 33, "minecraft:ladder", facing="west", waterlogged="false")

    def beam_hall(self):
        """Premier etage : le faisceau tourne, les piliers abritent. Exactement ce qui attend en haut."""
        d, t, keep = self.d, self.t, self.keep
        floor = self.L1
        for (dx, dz) in ((-7, -7), (7, -7), (-7, 7), (7, 7), (-12, 0), (12, 0), (0, -12), (0, 12), (-12, -11), (12, -11), (-12, 11), (12, 11)):
            d.box(CX + dx, floor + 1, 41 + dz, CX + dx + 1, self.L2 - 1, 41 + dz + 1, t["pillar"])
        trap(keep, CX, floor, 41, "beam", period=160, damage=6.0, length=17)
        d.chest(ARENA[2] - 1, floor + 1, 54, t["loot"], facing="west")
        for x in (29, 45):
            d.hanging_lantern(x, self.L2, 41, 2, t["lantern"])
        d.guard(26, floor + 1, 30, keep.mob())
        d.guard(48, floor + 1, 52, keep.mob())

    def wave_hall(self):
        """Deuxieme etage : des ondes partent du centre, on les saute en traversant vers l'escalier."""
        d, t, keep = self.d, self.t, self.keep
        floor = self.L2
        d.box(CX - 1, floor, 40, CX + 1, floor, 42, t["accent"])
        trap(keep, CX, floor, 41, "wave", period=65, damage=6.0, length=16)
        for (x, z) in ((27, 29), (47, 29), (27, 53), (47, 53)):
            d.hanging_lantern(x, self.L3, z, 2, t["lantern"])
        d.chest(CX, floor + 1, 56, t["loot"], facing="north")
        for (x, z) in ((30, 48), (44, 48), (44, 34)):
            d.guard(x, floor + 1, z, keep.mob())

    def fang_hall(self):
        """Troisieme etage : crocs de cristal vers le joueur et pluie annoncee. Il faut bouger sans arret, entoure de gardes."""
        d, t, keep = self.d, self.t, self.keep
        floor = self.L3
        trap(keep, 29, floor, 41, "spikes", period=70, damage=6.0, length=18)
        trap(keep, 45, floor, 41, "spikes", period=90, damage=6.0, length=18)
        trap(keep, CX, floor, 41, "circles", period=110, damage=6.0, width=3)
        for (x, z) in ((29, 33), (45, 33), (29, 49), (45, 49)):
            d.pillar(x, z, floor + 1, keep.dy - 1, t["pillar"])
            d.hanging_lantern(x + 2, keep.dy, z, 2, t["lantern"])
        d.chest(45, floor + 1, 55, t["loot"], facing="north")
        d.spawner(CX, floor + 1, 54, keep.mob())
        d.guard(40, floor + 1, 36, t["warden"])

    def climb(self):
        d, t, keep = self.d, self.t, self.keep
        # Du faisceau aux ondes, le long du mur ouest
        d.stairs_run(range(22, 25), 45, self.L1 + 1, 9, -1, 1, t["stairs"], t["wall"])
        for z in range(37, 42):
            d.set(25, self.L2 + 1, z, t["rail"], up="true")
        # Des ondes aux crocs, le long du mur est
        d.stairs_run(range(50, 53), 37, self.L2 + 1, 9, 1, 1, t["stairs"], t["wall"])
        for z in range(41, 46):
            d.set(49, self.L3 + 1, z, t["rail"], up="true")
        # Des crocs a l'arene : la derniere volee debouche dans le cercle
        d.stairs_run_x(23, (27, 28, 29), self.L3 + 1, keep.dy + 1 - self.L3, 1, t["stairs"], t["wall"])

    def bridges(self):
        self.bridge_z(-1, ARENA[1] - 3)
        self.bridge_z(ARENA[3] + 3, 76)
        self.bridge_x(-1, ARENA[0] - 3)
        self.bridge_x(ARENA[2] + 3, LAST + 1)


# ================================================================================================
# Sanctuaire infernal : un lac de lave sous un temple
# ================================================================================================

class Sanctuary(Keep):
    color = FIRE
    LAKE = FLOOR - 1  # la lave affleure juste sous les passerelles

    def arena_style(self, cx, cz, x0, z0, x1, z1):
        """Une nef : deux caniveaux de lave en bordure, deux rangees de colonnes, et l'espace pour voir venir les murs de flammes."""
        d, t = self.d, self.t
        for z in range(z0, z1 + 1):
            for x in (x0, x1):
                d.set(x, 1, z, "minecraft:lava", level="0")
        for dz in (-14, -7, 7, 14):
            for dx in (-12, 12):
                d.box(cx + dx, 2, cz + dz, cx + dx + 1, ARENA_TOP, cz + dz + 1, t["pillar"])
                d.hanging_lantern(cx + dx, ARENA_TOP + 1, cz + dz + 2, 2, t["lantern"])
        for z in range(z0 + 2, z1 - 1):
            for x in (cx - 1, cx, cx + 1):
                d.set(x, 1, z, t["accent"] if x == cx and z % 4 == 0 else d.pick(t["floor"]))

    def catwalk(self, x0, z0, x1, z1, rails=True):
        d, t = self.d, self.t
        d.box(x0, FLOOR - 1, z0, x1, FLOOR, z1, t["floor"])
        d.air(x0, FLOOR + 1, z0, x1, FLOOR + 4, z1)
        if rails:
            along_x = x1 - x0 > z1 - z0
            for k in range(x0 if along_x else z0, (x1 if along_x else z1) + 1):
                for edge in ((z0, z1) if along_x else (x0, x1)):
                    d.set(k if along_x else edge, FLOOR + 1, edge if along_x else k, t["rail"], up="true")

    def pierce(self, x0, z0, x1, z1):
        """Ouverture de trois blocs de haut dans une coque, a niveau des passerelles."""
        d, t = self.d, self.t
        d.air(x0, FLOOR + 1, z0, x1, FLOOR + 3, z1)
        d.box(x0, FLOOR, z0, x1, FLOOR, z1, t["floor"])

    def house(self, x0, z0):
        """Une casemate de 11 x 11, debout dans le lac."""
        self.room(x0, z0, x0 + 10, z0 + 10, self.d.rng.choice(self.t["themes"]))
        return x0, z0, x0 + 10, z0 + 10

    def fill(self, box, used):
        kinds = [k for k in ("guarded", "dart_hall", "sentinel_pillars", "mimic_vault") if k not in used]
        kind = self.d.rng.choice(kinds)
        used.append(kind)
        getattr(self, kind)(*box)

    def build(self):
        d, t = self.d, self.t
        self.arena()
        # Le lac : un sol, un mur d'enceinte, une voute, de la lave sur un bloc de fond
        d.box(0, FLOOR - 2, 0, LAST, FLOOR - 2, LAST, t["wall"])
        d.box(0, FLOOR - 1, 0, LAST, TOP + 2, LAST, t["wall"])
        d.air(2, FLOOR - 1, 2, LAST - 2, TOP + 1, LAST - 2)
        for x in range(2, LAST - 1):
            for z in range(2, LAST - 1):
                d.set(x, self.LAKE, z, "minecraft:lava", level="0")
        # Escalier vers l'arene, sur son ilot
        self.stair_down()
        d.box(25, FLOOR, 18, 47, FLOOR, 24, t["floor"])
        d.stairs_run_x(29, (20, 21, 22), 2, 16, 1, t["stairs"], t["wall"])
        self.stair_rails()
        for x in range(25, 48):
            for z in (18, 24):
                if x < 39 or x > 43:
                    d.set(x, FLOOR + 1, z, t["rail"], up="true")
        # Plaza centrale : la pluie de meteores, et de quoi souffler
        d.box(28, FLOOR - 1, 28, 47, FLOOR, 46, t["floor"])
        d.air(28, FLOOR + 1, 28, 47, TOP, 46)
        for k in range(28, 48):
            for z in (28, 46):
                if not 34 <= k <= 47 or z == 46 and not 34 <= k <= 40:
                    d.set(k, FLOOR + 1, z, t["rail"], up="true")
        for z in range(28, 47):
            for x in (28, 47):
                if not 35 <= z <= 39:
                    d.set(x, FLOOR + 1, z, t["rail"], up="true")
        for (x, z) in ((31, 42), (44, 42), (44, 32)):
            d.box(x, FLOOR + 1, z, x, TOP, z, t["pillar"])
            d.hanging_lantern(x + 1, TOP + 1, z, 2, t["lantern"])
        trap(self, 37, FLOOR, 37, "circles", period=120, damage=4.0, width=3)
        d.spawner(41, FLOOR + 1, 44, self.mob())
        d.chest(44, FLOOR + 1, 44, t["loot"], facing="north")
        # Du plaza a l'escalier
        self.catwalk(45, 24, 47, 28)
        # La chaussee d'entree : murs de flammes, il faut trouver la breche
        self.catwalk(34, 47, 40, 59)
        d.box(36, FLOOR, 58, 38, FLOOR, 62, t["floor"])
        trap(self, 37, FLOOR, 47, "flame_wall", facing="south", period=110, damage=4.0, length=12, width=3)
        # L'offrande du seuil : on ne traverse pas la lave sans y etre prepare
        d.set(39, FLOOR + 1, 58, "minecraft:chest", nbt={"id": "minecraft:chest", "Items": TypedList(TAG_COMPOUND, [
            {"Slot": Byte(13), "id": "minecraft:potion", "Count": Byte(2), "tag": {"Potion": "minecraft:fire_resistance"}}])},
              facing="west", type="single", waterlogged="false")
        # Casemates : deux ailes et un fond de lac, chacune finit par un tresor
        used = []
        w1 = self.house(5, 32)
        self.fill(w1, used)
        w2 = self.house(5, 6)
        self.key_chamber(*w2)
        e1 = self.house(59, 32)
        self.fill(e1, used)
        e2 = self.house(59, 6)
        self.key_chamber(*e2)
        s1 = self.house(5, 58)
        self.key_chamber(*s1)
        s2 = self.house(59, 58)
        self.mimic_vault(*s2)
        self.catwalk(18, 36, 27, 38)
        self.pierce(16, 36, 17, 38)
        self.catwalk(48, 36, 56, 38)
        self.pierce(57, 36, 58, 38)
        self.catwalk(9, 19, 11, 29)
        self.pierce(9, 18, 11, 18)
        self.pierce(9, 17, 11, 17)
        self.pierce(9, 30, 11, 31)
        self.catwalk(63, 19, 65, 29)
        self.pierce(63, 17, 65, 18)
        self.pierce(63, 30, 65, 31)
        # Depuis la chaussee, deux passerelles vers le fond du lac
        self.catwalk(10, 51, 33, 53)
        self.catwalk(9, 51, 11, 55)
        self.pierce(9, 56, 11, 57)
        self.catwalk(41, 51, 65, 53)
        self.catwalk(63, 51, 65, 55)
        self.pierce(63, 56, 65, 57)
        for z in (51, 52, 53):
            d.air(34, FLOOR + 1, z, 34, FLOOR + 1, z)
            d.air(40, FLOOR + 1, z, 40, FLOOR + 1, z)
            d.air(33, FLOOR + 1, z, 33, FLOOR + 1, z)
            d.air(41, FLOOR + 1, z, 41, FLOOR + 1, z)
        for (x, z, w) in ((9, 51, 11), (63, 51, 65)):
            d.air(x, FLOOR + 1, 51, w, FLOOR + 1, 53)
        for x in (9, 10, 11, 63, 64, 65):
            d.air(x, FLOOR + 1, 53, x, FLOOR + 1, 54)
        # Marches de secours : on se hisse hors de la lave le long des passerelles
        for (x, z) in ((33, 50), (41, 54), (20, 39), (52, 39), (12, 24), (66, 24), (20, 54), (54, 50), (27, 40), (48, 40)):
            d.set(x, self.LAKE, z, d.pick(t["floor"]))
        self.exterior()
        for (x0, z0, x1, z1, theme) in self.rooms:
            cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
            cross = {(x, z) for x in range(x0, x1 + 1) for z in range(z0, z1 + 1) if abs(x - cx) <= 1 or abs(z - cz) <= 1}
            furnish(d, x0, FLOOR, z0, x1, z1, TOP, theme, t["loot"], t["pedestal"], cross)
        self.finish_build()

    def exterior(self):
        TempleExterior(self).build()


class TempleExterior(Exterior):
    """Temple a quatre gradins, entoure de douves de lave. On peut le gravir, ou entrer par le bas."""
    TIERS = ((0, LAST, G + 6), (10, LAST - 10, G + 12), (22, LAST - 22, G + 18), (30, LAST - 30, G + 24))

    def moat(self):
        d, t = self.d, self.t
        # Douves entre le temple et leur parapet, sur des arcades
        for side in "WEN":
            for u in range(-5, LAST + 6):
                for v in (1, 2, 3, 4, 5):
                    x, z = outward(side, u, v)
                    d.set(x, G - 1, z, d.pick(t["wall"]))
                    d.set(x, G, z, "minecraft:lava", level="0")
                    d.air(x, G + 1, z, x, G + 4, z)
        for u in range(-7, LAST + 8):
            for side in "WENS":
                for v in (6, 7):
                    x, z = outward(side, u, v)
                    self.arcade_column(x, z, u, G + (1 if v == 6 else 0))
                x, z = outward(side, u, 6)
                if side != "S" or not 33 <= u <= 41:
                    if u % 4 == 0:
                        d.set(x, G + 2, z, d.pick(t["wall"]))
                    if u % 12 == 0:
                        d.set(x, G + 2, z, t["pillar"])
                        d.set(x, G + 3, z, "minecraft:netherrack")
                        d.set(x, G + 4, z, "minecraft:fire")
        # Le parvis sud
        d.box(-5, G - 1, LAST + 1, LAST + 5, G - 1, LAST + 5, t["wall"])
        d.box(20, G, LAST + 1, LAST - 20, G, LAST + 5, t["floor"])
        for x in list(range(-5, 20)) + list(range(LAST - 19, LAST + 6)):
            for z in range(LAST + 1, LAST + 6):
                d.set(x, G, z, "minecraft:lava", level="0")
        d.air(-5, G + 1, LAST + 1, LAST + 5, G + 4, LAST + 5)

    def arcade_column(self, x, z, u, top):
        d, t = self.d, self.t
        p = (u + 1) % 15
        pier = p <= 2
        span = min(p - 2, 15 - p)
        for y in range(0, top + 1):
            if not pier and y < min(12, 3 + span * 2):
                continue
            d.set(x, y, z, t["pillar"] if pier and y < 12 else d.pick(t["wall"]))

    def tiers(self):
        d, t = self.d, self.t
        previous_top = G
        for (lo, hi, top) in self.TIERS:
            # Murs des gradins, a fruit : chaque etage est en retrait du precedent
            for y in range(previous_top + 1, top):
                for k in range(lo, hi + 1):
                    for (x, z) in ((k, lo), (k, hi), (lo, k), (hi, k)):
                        d.set(x, y, z, t["pillar"] if k % 8 == lo % 8 else d.pick(t["wall"]))
                    for (x, z) in ((k, lo + 1), (k, hi - 1), (lo + 1, k), (hi - 1, k)):
                        d.set(x, y, z, d.pick(t["wall"]))
            d.box(lo, top, lo, hi, top, hi, t["floor"])
            # Terrasse degagee : le relief ne doit pas la boucher
            d.air(lo, top + 1, lo, hi, top + 7, hi)
            # Un filet de magma sous la corniche : le temple rougeoie de loin
            for k in range(lo, hi + 1):
                for (x, z) in ((k, lo), (k, hi), (lo, k), (hi, k)):
                    d.set(x, top - 1, z, t["accent"])
                    d.set(x, top + 1, z, t["rail"], up="true")
            previous_top = top

    def stairs(self):
        d, t = self.d, self.t
        # Deux rampes montent du parvis au premier gradin
        for xs in (range(43, 46), range(29, 32)):
            d.stairs_run(xs, LAST + 5, G + 1, 6, -1, 1, t["stairs"], t["wall"])
            for x in xs:
                d.air(x, G + 7, LAST, x, G + 7, LAST)
        # Puis l'escalier central : chaque volee finit juste devant le gradin suivant, on y entre en enjambant son bord
        for (z0, base) in ((70, G + 7), (58, G + 13), (50, G + 19)):
            d.stairs_run(range(36, 39), z0, base, 6, -1, 1, t["stairs"], t["wall"])
            d.air(36, base + 6, z0 - 6, 38, base + 6, z0 - 6)

    def summit(self):
        """L'autel du sommet : le plus beau point de vue du Nether, et un meteore pour qui s'y attarde."""
        d, t, keep = self.d, self.t, self.keep
        top = G + 24
        cx, cz = CX, 37
        d.box(cx - 3, top + 1, cz - 3, cx + 3, top + 1, cz + 3, t["pillar"])
        d.box(cx - 1, top + 2, cz - 1, cx + 1, top + 2, cz + 1, t["wall"])
        d.set(cx, top + 2, cz, "minecraft:netherrack")
        d.set(cx, top + 3, cz, "minecraft:fire")
        for (dx, dz) in ((-5, -5), (5, -5), (-5, 5), (5, 5)):
            d.box(cx + dx, top + 1, cz + dz, cx + dx, top + 4, cz + dz, t["pillar"])
            d.lantern(cx + dx, top + 5, cz + dz, t["lantern"])
        d.chest(cx, top + 2, cz + 3, t["loot"], facing="south")
        trap(keep, cx - 6, top, cz, "circles", period=130, damage=4.0, width=2)
        d.spawner(cx + 3, top + 2, cz - 3, keep.mob())

    def shafts(self):
        """Passages de service : du sommet au plaza, et du premier gradin vers les casemates des ailes."""
        d, t, keep = self.d, self.t, self.keep
        top = G + 24
        d.box(31, FLOOR + 1, 31, 33, top - 1, 33, t["wall"])
        d.air(32, FLOOR + 1, 32, 32, top, 32)
        d.air(33, FLOOR + 1, 32, 33, FLOOR + 2, 32)
        for y in range(FLOOR + 1, top + 1):
            d.set(32, y, 32, "minecraft:ladder", facing="east", waterlogged="false")
        for (x, back, facing, z) in ((5, 4, "east", 34), (69, 70, "west", 34)):
            low, high = min(x, back), max(x, back)
            d.box(low, TOP + 1, z - 1, high, G + 5, z + 1, t["wall"])
            d.air(x, TOP + 1, z, x, G + 6, z)
            for y in range(FLOOR + 1, G + 7):
                d.set(x, y, z, "minecraft:ladder", facing=facing, waterlogged="false")
            keep.spots.append((x, FLOOR + 1, z))

    def vestibule(self):
        """Le passage du parvis jusqu'au grand escalier, sous le premier gradin."""
        d, t = self.d, self.t
        d.box(33, G + 1, 60, 41, G + 5, LAST, t["wall"])
        d.air(35, G + 1, 73, 39, G + 5, LAST)
        d.air(36, G + 1, 63, 38, G + 4, 72)
        d.box(35, G, 73, 39, G, LAST, t["floor"])
        # Portail monumental : deux pylones de feu encadrent l'entree
        for x in (32, 42):
            d.box(x, G + 1, LAST + 1, x, G + 10, LAST + 1, t["pillar"])
            d.set(x, G + 11, LAST + 1, "minecraft:netherrack")
            d.set(x, G + 12, LAST + 1, "minecraft:fire")
        for x in range(33, 42):
            d.set(x, G + 7, LAST, t["accent"])

    def build(self):
        d, t, keep = self.d, self.t, self.keep
        self.moat()
        self.tiers()
        self.stairs()
        self.vestibule()
        keep.entrance()
        self.summit()
        self.shafts()
        self.gate_bridge(LAST + 5)
        keep.spots += [(37, G + 25, 42), (37, FLOOR + 1, 58)]


# ================================================================================================
# Catacombes fletries : un labyrinthe sous une necropole
# ================================================================================================

class Catacombs(Keep):
    color = SOUL
    PITCH = 6
    CELLS = 12
    CEIL = FLOOR + 5

    def arena_style(self, cx, cz, x0, z0, x1, z1):
        """Une salle de duel basse et sombre : des sarcophages a enjamber, tres peu de lumiere."""
        d, t = self.d, self.t
        for (dx, dz) in ((-10, -10), (10, -10), (-10, 10), (10, 10), (-13, 0), (13, 0), (0, -13), (0, 13)):
            d.box(cx + dx, 2, cz + dz, cx + dx + 2, 2, cz + dz + 1, t["pillar"])
            d.skull(cx + dx + 1, 3, cz + dz, wither=True)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                r2 = (x - cx) ** 2 + (z - cz) ** 2
                if 36 <= r2 <= 49:
                    d.set(x, 1, z, "minecraft:soul_soil")
                elif d.rng.random() < 0.1:
                    d.set(x, 1, z, "minecraft:soul_soil")
        for (dx, dz) in ((-14, -14), (14, -14), (-14, 14), (14, 14)):
            d.hanging_lantern(cx + dx, ARENA_TOP + 1, cz + dz, 4, t["lantern"])

    def span(self, i):
        lo = 2 + i * self.PITCH
        return lo, lo + 2

    def carve(self, x0, z0, x1, z1):
        d, t = self.d, self.t
        d.air(x0, FLOOR + 1, z0, x1, FLOOR + 4, z1)
        d.box(x0, FLOOR, z0, x1, FLOOR, z1, t["floor"])

    def generate_maze(self):
        """Parcours en profondeur, depuis la case de l'entree : chaque case est reliee, et les culs-de-sac sont nombreux."""
        rng = self.d.rng
        n = self.CELLS
        start = (5, 9)
        seen = {start}
        stack = [start]
        links = set()
        depth = {start: 0}
        while stack:
            (i, j) = stack[-1]
            around = [(i + a, j + b) for (a, b) in ((1, 0), (-1, 0), (0, 1), (0, -1)) if 0 <= i + a < n and 0 <= j + b < n and (i + a, j + b) not in seen]
            if not around:
                stack.pop()
                continue
            # On prefere continuer tout droit : de longs couloirs, bons pour les ruees
            straight = [c for c in around if len(stack) > 1 and (c[0] - i, c[1] - j) == (i - stack[-2][0], j - stack[-2][1])]
            cell = rng.choice(straight) if straight and rng.random() < 0.55 else rng.choice(around)
            links.add(frozenset(((i, j), cell)))
            seen.add(cell)
            depth[cell] = depth[(i, j)] + 1
            stack.append(cell)
        # Quelques boucles : un labyrinthe sans boucle se resout en suivant un mur, et interdit de fuir une embuscade
        for i in range(n):
            for j in range(n):
                for cell in ((i + 1, j), (i, j + 1)):
                    if cell in seen and frozenset(((i, j), cell)) not in links and rng.random() < 0.07:
                        links.add(frozenset(((i, j), cell)))
        return links, depth

    def build(self):
        d, t = self.d, self.t
        self.arena()
        # La masse dans laquelle le labyrinthe est taille
        d.box(0, FLOOR - 1, 0, LAST, self.CEIL, LAST, t["wall"])
        links, depth = self.generate_maze()
        n = self.CELLS
        degree = {}
        for i in range(n):
            for j in range(n):
                x0, x1 = self.span(i)
                z0, z1 = self.span(j)
                self.carve(x0, z0, x1, z1)
        for link in links:
            (a, b) = sorted(link)
            degree[a] = degree.get(a, 0) + 1
            degree[b] = degree.get(b, 0) + 1
            ax0, ax1 = self.span(a[0])
            az0, az1 = self.span(a[1])
            bx0, bx1 = self.span(b[0])
            bz0, bz1 = self.span(b[1])
            self.carve(min(ax0, bx0), min(az0, bz0), max(ax1, bx1), max(az1, bz1))
        # Raccords : l'entree, les trois escaliers de service, la salle de la descente
        self.carve(35, 56, 38, 58)
        self.carve(36, 59, 38, 62)
        self.carve(2, 35, 4, 38)
        self.carve(68, 35, 72, 38)
        self.carve(35, 2, 38, 4)
        self.carve(39, 17, 49, 25)
        self.stair_down()
        d.stairs_run_x(29, (20, 21, 22), 2, 16, 1, t["stairs"], t["wall"])
        self.stair_rails()
        self.dress(links, depth, degree)
        self.exterior()
        self.finish_build()

    def dress(self, links, depth, degree):
        d, t = self.d, self.t
        rng = d.rng
        n = self.CELLS

        def middle(cell):
            return self.span(cell[0])[0] + 1, self.span(cell[1])[0] + 1

        def reserved(cell):
            x, z = middle(cell)
            return (39 <= x <= 49 and 17 <= z <= 25) or (34 <= x <= 39 and 54 <= z <= 62) or (x <= 5 and 33 <= z <= 40) or (x >= 67 and 33 <= z <= 40) or (34 <= x <= 40 and z <= 5)

        # Les trois tresors : les culs-de-sac les plus profonds, loin les uns des autres
        ends = sorted((c for c, k in degree.items() if k == 1 and not reserved(c)), key=lambda c: -depth[c])
        chosen = []
        for cell in ends:
            if all(abs(cell[0] - o[0]) + abs(cell[1] - o[1]) >= 5 for o in chosen):
                chosen.append(cell)
            if len(chosen) == 3:
                break
        assert len(chosen) == 3, f"{self.name} : labyrinthe sans assez de culs-de-sac"
        for cell in chosen:
            self.crypt(*middle(cell))
        others = [c for c in ends if c not in chosen]
        rng.shuffle(others)
        for k, cell in enumerate(others):
            x, z = middle(cell)
            if k % 3 == 0:
                d.chest(x, FLOOR + 1, z, t["loot"], facing="north")
                d.guard(x, FLOOR + 1, z - 1 if d.blocks.get((x, FLOOR + 1, z - 1), ("",))[0] == AIR else z + 1, self.mob())
            elif k % 3 == 1:
                d.spawner(x, FLOOR + 1, z, self.mob())
            else:
                d.set(x, FLOOR + 1, z, "minecraft:bone_block", axis="y")
                d.skull(x, FLOOR + 2, z)
        # Ruees : un mecanisme au bout de chaque long couloir droit. On s'ecarte de la ligne, le couloir fait trois de large.
        runs = []
        for j in range(n):
            run = []
            for i in range(n):
                run.append((i, j))
                if i == n - 1 or frozenset(((i, j), (i + 1, j))) not in links:
                    if len(run) >= 4:
                        runs.append((run[0], len(run), "east"))
                    run = []
        for i in range(n):
            run = []
            for j in range(n):
                run.append((i, j))
                if j == n - 1 or frozenset(((i, j), (i, j + 1))) not in links:
                    if len(run) >= 4:
                        runs.append((run[0], len(run), "south"))
                    run = []
        rng.shuffle(runs)
        placed = 0
        for (cell, size, facing) in runs:
            if placed >= 7 or reserved(cell):
                continue
            x, z = middle(cell)
            trap(self, x, FLOOR, z, "line", facing=facing, period=90 + rng.randrange(40), damage=5.0, length=(size - 1) * self.PITCH + 1)
            placed += 1
        # Carrefours : obscurite et embuscades
        crossings = [c for c, k in degree.items() if k >= 3 and not reserved(c)]
        rng.shuffle(crossings)
        for (k, cell) in enumerate(crossings[:9]):
            x, z = middle(cell)
            if d.blocks.get((x, FLOOR, z), ("",))[0].endswith("dungeon_trap"):
                continue
            if k % 2 == 0:
                trap(self, x, FLOOR, z, "ambush", period=160, damage=0.0, charges=2, entity=t["mobs"][0])
            else:
                trap(self, x, FLOOR, z, "gloom", period=200, damage=5.0)
        # Sol qui s'enfonce et toiles : on ne peut pas toujours courir
        cells = [(i, j) for i in range(n) for j in range(n) if not reserved((i, j))]
        rng.shuffle(cells)
        for cell in cells[:18]:
            x, z = middle(cell)
            for (dx, dz) in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
                if rng.random() < 0.7 and d.blocks.get((x + dx, FLOOR + 1, z + dz), ("",))[0] == AIR:
                    d.set(x + dx, FLOOR, z + dz, "minecraft:soul_sand")
        for cell in cells[18:40]:
            x, z = middle(cell)
            d.set(x + rng.choice((-1, 1)), FLOOR + 4, z + rng.choice((-1, 1)), "minecraft:cobweb")
        # Tres peu de lanternes : la nuit fait partie de l'epreuve
        for cell in cells[40:58]:
            x, z = middle(cell)
            if d.blocks.get((x, FLOOR + 1, z), ("",))[0] == AIR:
                d.lantern(x, FLOOR + 4, z, t["lantern"], hanging=True)

    def crypt(self, x, z):
        """Chambre funeraire au fond d'un cul-de-sac : sarcophage, tresor, gardien."""
        d, t = self.d, self.t
        x0, z0 = max(3, x - 4), max(3, z - 4)
        x1, z1 = min(LAST - 3, x0 + 8), min(LAST - 3, z0 + 8)
        self.carve(x0, z0, x1, z1)
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        d.box(cx - 1, FLOOR + 1, cz - 1, cx + 1, FLOOR + 1, cz + 1, t["pedestal"])
        d.chest(cx, FLOOR + 2, cz, "wing_treasure", facing="south")
        self.keys.append((cx, FLOOR + 2, cz))
        d.guard(cx, FLOOR + 1, z0 + 1, "rune_warden")
        for (px, pz) in ((x0, z0), (x1, z0), (x0, z1), (x1, z1)):
            d.box(px, FLOOR + 1, pz, px, FLOOR + 4, pz, "minecraft:bone_block", axis="y")
        d.lantern(cx, FLOOR + 4, cz, t["lantern"], hanging=True)

    def exterior(self):
        NecropolisExterior(self).build()


class NecropolisExterior(Exterior):
    """Une ville des morts en ruine : mur effondre, allees de steles, mausolees. Rien ne monte haut, tout invite a descendre."""

    def ground(self):
        d, t = self.d, self.t
        d.box(-3, G - 1, -3, LAST + 3, G - 1, LAST + 3, t["wall"])
        for x in range(-3, LAST + 4):
            for z in range(-3, LAST + 4):
                d.set(x, G, z, "minecraft:soul_soil" if d.rng.random() < 0.45 else d.pick(t["floor"]))
        d.air(-3, G + 1, -3, LAST + 3, G + 9, LAST + 3)
        for k in range(-3, LAST + 4):
            for w in (-1, 0, 1):
                d.set(CX + w, G, k, "minecraft:polished_blackstone")
                d.set(k, G, CX + w, "minecraft:polished_blackstone")

    def broken_wall(self):
        d, t = self.d, self.t
        for side in "WENS":
            for u in range(-5, LAST + 6):
                p = (u + 1) % 15
                pier = p <= 2
                span = min(p - 2, 15 - p)
                for v in (4, 5):
                    x, z = outward(side, u, v)
                    for y in range(0, G + 1):
                        if not pier and y < min(12, 3 + span * 2):
                            continue
                        d.set(x, y, z, t["pillar"] if pier and y < 12 else d.pick(t["wall"]))
                # Un mur qui s'est ecroule par endroits : la hauteur varie, et il laisse des breches a enjamber
                height = d.rng.choice((0, 1, 1, 2, 3, 4, 5))
                if abs(u - CX) <= 3:
                    height = 0
                for v in (4, 5):
                    x, z = outward(side, u, v)
                    # Les breches restent franchissables, meme si le relief monte jusqu'ici
                    d.air(x, G + 1, z, x, G + 6, z)
                    if height:
                        d.box(x, G + 1, z, x, G + height, z, t["wall"])
                    if d.rng.random() < 0.08:
                        d.set(x, G + height + 1, z, "minecraft:bone_block", axis="y")
                if pier:
                    for v in (6, 7):
                        x, z = outward(side, u, v)
                        for y in range(0, G + 1):
                            d.set(x, y, z, t["pillar"] if y < 12 else d.pick(t["wall"]))
                        d.stair(x, G + 1, z, t["stairs"], {"W": "east", "E": "west", "N": "south", "S": "north"}[side])

    def mausoleum(self, x0, z0, x1, z1, door, loot=True):
        """Petit tombeau a toit pointu : il sert de reperes, et de cachette."""
        d, t, keep = self.d, self.t, self.keep
        height = G + 6
        d.box(x0, G + 1, z0, x1, height, z1, t["wall"])
        d.air(x0 + 1, G + 1, z0 + 1, x1 - 1, height - 1, z1 - 1)
        for (x, z) in ((x0, z0), (x1, z0), (x0, z1), (x1, z1)):
            d.box(x, G + 1, z, x, height + 1, z, "minecraft:bone_block", axis="y")
        d.roof_pyramid(x0, z0, x1, z1, height + 1, t["stairs"], t["pillar"])
        cx, cz = (x0 + x1) // 2, (z0 + z1) // 2
        if door == "south":
            d.air(cx - 1, G + 1, z1, cx + 1, G + 3, z1)
        elif door == "north":
            d.air(cx - 1, G + 1, z0, cx + 1, G + 3, z0)
        elif door == "east":
            d.air(x1, G + 1, cz - 1, x1, G + 3, cz + 1)
        else:
            d.air(x0, G + 1, cz - 1, x0, G + 3, cz + 1)
        d.hanging_lantern(cx, height, cz, 1, t["lantern"])
        if loot:
            d.box(cx - 1, G + 1, z0 + 1, cx + 1, G + 1, z0 + 1, t["pedestal"])
            d.chest(cx, G + 2, z0 + 1, t["loot"], facing="south")
            d.guard(cx, G + 1, cz + 1, keep.mob())

    def graves(self, x0, z0, x1, z1):
        d, t = self.d, self.t
        rng = d.rng
        for x in range(x0, x1 + 1, 3):
            for z in range(z0, z1 + 1, 4):
                if rng.random() < 0.25 or d.blocks.get((x, G + 1, z), (AIR,))[0] != AIR:
                    continue
                d.set(x, G + 1, z, t["pillar"])
                if rng.random() < 0.6:
                    d.set(x, G + 2, z, t["rail"], up="true")
                if rng.random() < 0.3:
                    d.skull(x, G + 1, z + 1)

    def bone_arches(self):
        d = self.d
        for z in (4, 22, 52, 66):
            for x in (CX - 3, CX + 3):
                d.box(x, G + 1, z, x, G + 5, z, "minecraft:bone_block", axis="y")
            for x in range(CX - 2, CX + 3):
                d.set(x, G + 6, z, "minecraft:bone_block", axis="x")
            d.skull(CX, G + 7, z, wither=True)
        for x in (4, 22, 52, 70):
            for z in (CX - 3, CX + 3):
                d.box(x, G + 1, z, x, G + 5, z, "minecraft:bone_block", axis="y")
            for z in range(CX - 2, CX + 3):
                d.set(x, G + 6, z, "minecraft:bone_block", axis="z")
            d.skull(x, G + 7, CX, wither=True)

    def braziers(self):
        d, t = self.d, self.t
        for (x, z) in ((12, 12), (62, 12), (12, 62), (62, 62), (28, 28), (46, 28), (28, 46), (46, 46)):
            d.box(x, G + 1, z, x, G + 2, z, t["pillar"])
            d.set(x, G + 3, z, "minecraft:soul_soil")
            d.set(x, G + 4, z, "minecraft:soul_fire")

    def main_tomb(self):
        """Le tombeau du Champion : sa porte est le grand escalier."""
        d, t = self.d, self.t
        d.box(32, G + 1, 60, 42, G + 8, 74, t["wall"])
        d.air(35, G + 1, 61, 39, G + 7, 73)
        d.air(36, G + 1, 74, 38, G + 4, 74)
        d.box(36, G, 73, 38, G, 75, t["floor"])
        for (x, z) in ((32, 60), (42, 60), (32, 74), (42, 74)):
            d.box(x, G + 1, z, x, G + 12, z, "minecraft:bone_block", axis="y")
            d.skull(x, G + 13, z, wither=True)
        d.roof_pyramid(32, 60, 42, 74, G + 9, t["stairs"], t["pillar"])
        d.hanging_lantern(37, G + 8, 66, 2, t["lantern"])
        d.set(37, G + 5, 74, t["accent"])

    def build(self):
        d, t, keep = self.d, self.t, self.keep
        self.ground()
        self.broken_wall()
        self.main_tomb()
        keep.entrance()
        self.side_stair("W")
        self.side_stair("E")
        self.north_stair()
        self.mausoleum(8, 8, 20, 20, "south")
        self.mausoleum(54, 8, 66, 20, "south")
        self.mausoleum(8, 54, 20, 66, "north")
        self.mausoleum(54, 54, 66, 66, "north", loot=False)
        self.mausoleum(30, 30, 44, 44, "south")
        trap(keep, CX, G, 37, "gloom", period=240, damage=5.0)
        self.graves(-1, 22, 31, 33)
        self.graves(43, 22, 75, 33)
        self.graves(-1, 41, 31, 52)
        self.graves(43, 41, 75, 52)
        self.graves(22, -1, 33, 28)
        self.graves(41, -1, 52, 28)
        self.bone_arches()
        self.braziers()
        self.gate_bridge(LAST + 5)
        keep.spots += [(37, FLOOR + 1, 58), (-2, FLOOR + 1, 37), (LAST + 2, FLOOR + 1, 37), (37, FLOOR + 1, -2)]


KEEP_CLASSES = {"onyx_citadel": Citadel, "infernal_sanctum": Sanctuary, "withered_catacombs": Catacombs}
