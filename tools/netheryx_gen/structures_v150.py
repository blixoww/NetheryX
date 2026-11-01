"""Structures 1.5.0 : Jardin du Nether et Caravane nomade refaits, et petites trouvailles disseminees dans le Nether.

Une trouvaille tient en quelques blocs : deux ou trois ennemis, un coffre avec une jolie recompense. On en croise souvent
en explorant, elles donnent une raison de quitter le chemin.
"""
from .structures import AIR, Dungeon
from .structures_v140 import unsupported

BRICK_PATH = [("minecraft:polished_blackstone_bricks", 65), ("minecraft:cracked_polished_blackstone_bricks", 15), ("minecraft:polished_blackstone", 20)]
GRASS = [("nether_grass_block", 80), ("ember_nylium", 20)]
FLOWERS = [("ember_bloom", 4), ("ash_daisy", 4), ("crimson_bellflower", 4), ("sulfur_tulip", 4), ("glowcap", 3), ("blazing_grass", 6), ("nether_grass", 6)]
WOOL_STONE = ("minecraft:red_wool", "minecraft:white_wool")


def tree(d, x, y, z, height, stem, crown, glow):
    """Petit arbre : tronc, houppier arrondi parseme de lumiere."""
    d.pillar(x, z, y, y + height, stem) if "stem" not in stem else [d.set(x, yy, z, stem, axis="y") for yy in range(y, y + height + 1)]
    top = y + height
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            for dy in range(-1, 3):
                r = dx * dx + dz * dz + dy * dy * 1.4
                if r <= 5.5 and d.rng.random() < 0.92 and not (dx == 0 and dz == 0 and dy < 1):
                    d.set(x + dx, top + dy, z + dz, glow if d.rng.random() < 0.1 else crown)


def nether_garden():
    """Parc rond et vivant : haie irreguliere, allee circulaire, fontaine a etages avec de la vraie eau, quatre arbres,
    pergolas fleuries, parterres, carres de culture et petit kiosque de verre qui abrite le butin."""
    S, c = 31, 15
    d = Dungeon(S, 13, S, "nether_garden_v2")
    d.air(0, 1, 0, S - 1, 12, S - 1)
    for x in range(S):
        for z in range(S):
            r = ((x - c) ** 2 + (z - c) ** 2) ** 0.5
            if r <= 15.5:
                d.set(x, 0, z, d.pick(GRASS))

    def flower(x, z):
        name = d.pick(FLOWERS)
        d.set(x, 1, z, name)

    # Allee circulaire et croix d'allees vers quatre entrees
    for x in range(S):
        for z in range(S):
            r = ((x - c) ** 2 + (z - c) ** 2) ** 0.5
            if 9.0 <= r <= 11.2 or (r <= 15.5 and (abs(x - c) <= 1 or abs(z - c) <= 1)):
                d.set(x, 0, z, d.pick(BRICK_PATH))
    # Haie vivante, percee aux entrees, de hauteur variable
    for x in range(S):
        for z in range(S):
            r = ((x - c) ** 2 + (z - c) ** 2) ** 0.5
            if 14.0 <= r <= 15.5 and abs(x - c) > 1 and abs(z - c) > 1:
                height = 1 + (d.rng.random() < 0.4)
                for y in range(1, height + 1):
                    d.set(x, y, z, "ember_wart_block")
    # Arches d'entree et lanternes
    for (dx, dz) in ((0, -14), (0, 14), (-14, 0), (14, 0)):
        ax, az = dx != 0, dz != 0
        for k in (-2, 2):
            px, pz = c + dx + (k if az else 0), c + dz + (k if ax else 0)
            d.pillar(px, pz, 1, 4, "ember_stem" if False else "minecraft:polished_blackstone_bricks")
        for k in range(-2, 3):
            px, pz = c + dx + (k if az else 0), c + dz + (k if ax else 0)
            d.set(px, 5, pz, "ember_planks")
        d.set(c + dx, 4, c + dz, "minecraft:lantern", hanging="true", waterlogged="false")
    # Fontaine a etages : un vrai bassin d'eau, rare dans le Nether
    d.box(c - 4, 0, c - 4, c + 4, 0, c + 4, BRICK_PATH)
    d.box(c - 3, 0, c - 3, c + 3, 0, c + 3, "rimestone_bricks")
    for x in range(c - 3, c + 4):
        for z in range(c - 3, c + 4):
            if max(abs(x - c), abs(z - c)) == 3:
                d.set(x, 1, z, "rimestone_brick_wall", up="true")
            else:
                d.set(x, 1, z, "minecraft:water", level="0")
    d.pillar(c, c, 1, 3, "rimestone_bricks")
    d.box(c - 1, 3, c - 1, c + 1, 3, c + 1, "rimestone_bricks")
    d.set(c, 4, c, "minecraft:water", level="0")
    for (x, z) in ((c - 1, c), (c + 1, c), (c, c - 1), (c, c + 1)):
        d.set(x, 4, z, "rimestone_brick_wall", up="true")
    d.set(c - 1, 4, c - 1, "quartz_lamp")
    d.set(c + 1, 4, c + 1, "quartz_lamp")
    d.set(c - 1, 4, c + 1, "quartz_lamp")
    d.set(c + 1, 4, c - 1, "quartz_lamp")
    # Quatre grands arbres dans les quartiers
    for (x, z, stem, crown, glow) in ((8, 8, "ember_stem", "ember_wart_block", "minecraft:shroomlight"),
                                      (22, 8, "withered_stem", "withered_wart_block", "minecraft:shroomlight"),
                                      (8, 22, "marrow_stem", "ember_wart_block", "minecraft:ochre_froglight"),
                                      (22, 22, "ember_stem", "ember_wart_block", "minecraft:shroomlight")):
        tree(d, x, 1, z, 4 + d.rng.randrange(2), stem, crown, glow)
    # Quartiers : fleurs denses, entrecoupees de carres de culture
    beds = {(4, 13, "cinder_wheat_crop", "minecraft:farmland"), (24, 13, "blaze_pepper_crop", "ember_nylium")}
    for x in range(S):
        for z in range(S):
            r = ((x - c) ** 2 + (z - c) ** 2) ** 0.5
            if r < 14.0 and (x, 1, z) not in d.blocks or (r < 14 and d.blocks.get((x, 1, z), ("",))[0] == AIR):
                ground = d.blocks.get((x, 0, z), ("",))[0]
                if ("nether_grass_block" in ground or "ember_nylium" in ground) and d.rng.random() < 0.42:
                    flower(x, z)
    for (x0, z0, crop, soil) in beds:
        d.box(x0 - 1, 0, z0 - 1, x0 + 3, 0, z0 + 3, "minecraft:polished_blackstone")
        for x in range(x0, x0 + 3):
            for z in range(z0, z0 + 3):
                d.set(x, 0, z, soil, **({"moisture": "7"} if "farmland" in soil else {}))
                d.set(x, 1, z, crop, age=str(d.rng.randrange(2, 4)))
    # Baies et lys autour de la fontaine
    for (x, z) in ((c - 7, c - 7), (c + 7, c + 7), (c - 7, c + 7), (c + 7, c - 7)):
        d.set(x, 1, z, "fire_berry_bush", age="3")
    # Kiosque de verre : le tresor du jardin, a l'ecart de l'allee
    kx, kz = 25, 22
    d.box(kx - 2, 0, kz - 2, kx + 2, 0, kz + 2, "polished_onyx")
    for (x, z) in ((kx - 2, kz - 2), (kx + 2, kz - 2), (kx - 2, kz + 2), (kx + 2, kz + 2)):
        d.pillar(x, z, 1, 4, "ember_planks")
    d.air(kx - 1, 1, kz - 1, kx + 1, 4, kz + 1)
    for k in range(-1, 2):
        d.set(kx + k, 1, kz + 2, "quartz_glass")
        d.set(kx + 2, 1, kz + k, "quartz_glass")
        d.set(kx + k, 1, kz - 2, "quartz_glass")
    d.box(kx - 2, 5, kz - 2, kx + 2, 5, kz + 2, "quartz_glass")
    d.set(kx, 5, kz, "ember_lamp")
    d.chest(kx, 1, kz, "nether_garden", facing="west")
    # Habitants : des animaux paisibles
    d.guard(c - 6, 1, c + 2, "ember_cow")
    d.guard(c + 6, 1, c - 2, "cinder_hen")
    d.guard(c + 3, 1, c + 7, "cinder_hen")
    return d


def wagon(d, x0, z0, cover, along_x=True):
    """Chariot bache : roues, plateau, arceaux de toile, chargement."""
    length, width = 6, 3
    for i in range(length):
        for j in range(width):
            x, z = (x0 + i, z0 + j) if along_x else (x0 + j, z0 + i)
            d.set(x, 1, z, "wartstalk_planks")
            for y, inset in ((2, 0), (3, 0), (4, 1)):
                on_edge = j == 0 or j == width - 1
                if inset == 0 and on_edge:
                    d.set(x, y, z, cover if i % 2 == 0 else "minecraft:white_wool")
                elif inset == 1 and not on_edge:
                    d.set(x, y, z, cover if i % 2 == 0 else "minecraft:white_wool")
    for i in (0, length - 1):
        for j in (-1, width):
            x, z = (x0 + i, z0 + j) if along_x else (x0 + j, z0 + i)
            d.set(x, 1, z, "minecraft:stripped_dark_oak_log", axis="x" if not along_x else "z")
    d.barrel(*((x0 + 2, 2, z0 + 1) if along_x else (x0 + 1, 2, z0 + 2)), "nomad_caravan")
    d.set(*((x0 + 3, 2, z0 + 1) if along_x else (x0 + 1, 2, z0 + 3)), "minecraft:barrel", facing="up", open="false")


def tent(d, x0, z0, width, depth, colors):
    half = width // 2
    d.box(x0 + 1, 0, z0 + 1, x0 + width - 2, 0, z0 + depth - 2, "wartstalk_planks")
    for k in range(half + 1):
        y = 1 + k
        for z in range(z0, z0 + depth):
            color = colors[(z - z0) % 2]
            for x in (x0 + k, x0 + width - 1 - k):
                d.set(x, y, z, color)
    for k in range(half + 1):
        for x in range(x0 + k, x0 + width - k):
            for y in range(1, 2 + k):
                d.set(x, y, z0, colors[0])
    for x in (x0, x0 + width - 1):
        d.pillar(x, z0 + depth, 1, 2, "wartstalk_fence")
    d.box(x0 + 2, 1, z0 + 1, x0 + width - 3, 1, z0 + depth - 2, "minecraft:red_carpet")


def nomad_caravan():
    """Campement vivant : trois tentes de couleurs, deux chariots baches, etal du marchand, feu de camp et bancs, enclos a
    lezards, poteaux de lanternes reliees, piste de terre qui traverse le tout."""
    S = 31
    d = Dungeon(S, 11, S, "nomad_caravan_v2")
    d.air(0, 1, 0, S - 1, 10, S - 1)
    ground = [("minecraft:netherrack", 30), ("wart_moss", 25), ("minecraft:soul_soil", 25), ("minecraft:blackstone", 10), ("minecraft:gravel", 10)]
    for x in range(S):
        for z in range(S):
            if ((x - 15) ** 2 + (z - 15) ** 2) ** 0.5 <= 15.5:
                d.set(x, 0, z, d.pick(ground))
    # Pistes de terre battue
    for k in range(S):
        for w in (14, 15, 16):
            if ((k - 15) ** 2 + (w - 15) ** 2) ** 0.5 <= 15.5:
                d.set(k, 0, w, "minecraft:soul_soil")
                d.set(w, 0, k, "minecraft:soul_soil")
    # Trois tentes
    tent(d, 3, 3, 9, 8, ("minecraft:red_wool", "minecraft:white_wool"))
    tent(d, 19, 3, 9, 8, ("minecraft:orange_wool", "minecraft:yellow_wool"))
    tent(d, 3, 19, 9, 7, ("minecraft:purple_wool", "minecraft:black_wool"))
    # Marchand : comptoir et marchandises devant la grande tente
    for x in range(4, 11):
        d.set(x, 1, 11, "wartstalk_slab", type="top", waterlogged="false")
    d.set(4, 2, 11, "minecraft:barrel", facing="up", open="false")
    d.set(10, 2, 11, "minecraft:flower_pot")
    d.chest(5, 1, 4, "nomad_caravan", facing="south")
    d.barrel(9, 1, 4, "nomad_caravan")
    d.set(7, 4, 6, "minecraft:lantern", hanging="true", waterlogged="false")
    d.guard(7, 1, 8, "piglin_merchant")
    # Tente de repos : lits et coffre
    for x in (21, 25):
        d.set(x, 1, 5, "minecraft:red_bed", facing="south", part="head", occupied="false")
        d.set(x, 1, 6, "minecraft:red_bed", facing="south", part="foot", occupied="false")
    d.chest(23, 1, 4, "nomad_caravan", facing="south")
    d.set(23, 4, 6, "minecraft:soul_lantern", hanging="true", waterlogged="false")
    # Tente violette : atelier
    d.set(5, 1, 20, "minecraft:smithing_table")
    d.set(6, 1, 20, "minecraft:cartography_table")
    d.set(7, 1, 20, "minecraft:anvil", facing="south")
    d.set(9, 1, 20, "minecraft:barrel", facing="up", open="false")
    d.set(7, 4, 22, "minecraft:soul_lantern", hanging="true", waterlogged="false")
    # Chariots
    wagon(d, 19, 13, "minecraft:red_wool")
    wagon(d, 20, 24, "minecraft:orange_wool")
    # Feu de camp et bancs
    d.box(12, 0, 12, 18, 0, 18, "minecraft:soul_soil")
    d.set(15, 1, 15, "minecraft:soul_campfire", facing="north", lit="true", signal_fire="false", waterlogged="false")
    for (x, z, facing) in ((13, 15, "east"), (17, 15, "west"), (15, 13, "south"), (15, 17, "north")):
        d.stair(x, 1, z, "wartstalk_stairs", facing)
    for (x, z) in ((12, 13), (18, 13), (12, 17), (18, 17)):
        d.set(x, 1, z, "minecraft:barrel", facing="up", open="false")
    # Enclos a lezards
    for x in range(13, 18):
        for z in (21, 27):
            d.set(x, 1, z, "wartstalk_fence", **{k: "true" for k in ("east", "west")})
    for z in range(21, 28):
        for x in (13, 17):
            d.set(x, 1, z, "wartstalk_fence", **{k: "true" for k in ("north", "south")})
    d.set(15, 1, 21, "wartstalk_fence_gate", facing="north", open="false", powered="false", in_wall="false")
    d.set(14, 1, 26, "fire_berry_bush", age="3")
    d.set(16, 1, 26, "fire_berry_bush", age="3")
    d.guard(15, 1, 24, "cinder_lizard")
    # Poteaux de lanternes
    for (x, z) in ((2, 15), (28, 15), (15, 2), (13, 11), (17, 11), (28, 28), (2, 28)):
        for y in range(1, 4):
            d.set(x, y, z, "wartstalk_fence")
        d.set(x, 4, z, "wartstalk_slab", type="bottom", waterlogged="false")
        d.lantern(x, 5, z, "minecraft:lantern")
    d.guard(23, 1, 18, "lantern_bearer")
    return d


# ------------------------------------------------------------------------------------------------
# Petites trouvailles : 9 x 9 au plus, 2 ou 3 ennemis, un coffre de recompense
# ------------------------------------------------------------------------------------------------

LOOT = "wayside_cache"


def scorched_watch():
    d = Dungeon(9, 8, 9, "scorched_watch")
    d.air(0, 1, 0, 8, 7, 8)
    d.box(1, 0, 1, 7, 0, 7, [("ember_planks", 60), ("minecraft:blackstone", 40)])
    for (x, z) in ((1, 1), (7, 1), (1, 7), (7, 7)):
        d.pillar(x, z, 1, 3, "ember_stem" if False else "minecraft:polished_blackstone_bricks")
    d.box(2, 3, 1, 6, 3, 1, "ember_slab", type="bottom", waterlogged="false")
    d.box(1, 3, 2, 1, 3, 6, "ember_slab", type="bottom", waterlogged="false")
    d.set(4, 1, 4, "minecraft:campfire", facing="north", lit="true", signal_fire="false", waterlogged="false")
    d.chest(6, 1, 2, LOOT, facing="west")
    d.set(5, 1, 2, "minecraft:barrel", facing="up", open="false")
    d.lantern(7, 4, 7, "minecraft:lantern")
    d.guard(3, 1, 6, "ember_stalker")
    d.guard(6, 1, 5, "ember_stalker")
    return d


def overturned_cart():
    d = Dungeon(9, 6, 7, "overturned_cart")
    d.air(0, 1, 0, 8, 5, 6)
    d.box(0, 0, 0, 8, 0, 6, [("minecraft:soul_soil", 50), ("minecraft:gravel", 25), ("minecraft:netherrack", 25)])
    for x in range(2, 7):
        d.set(x, 1, 3, "wartstalk_planks")
        d.set(x, 2, 2, "minecraft:red_wool" if x % 2 else "minecraft:white_wool")
        d.set(x, 1, 2, "minecraft:red_wool" if x % 2 else "minecraft:white_wool")
    d.set(1, 1, 4, "minecraft:stripped_dark_oak_log", axis="y")
    d.set(7, 1, 4, "minecraft:stripped_dark_oak_log", axis="y")
    d.chest(3, 1, 4, LOOT, facing="south")
    d.set(5, 1, 4, "minecraft:barrel", facing="up", open="false")
    d.skull(6, 1, 5)
    d.guard(2, 1, 5, "ember_stalker")
    d.guard(7, 1, 1, "minecraft:zombified_piglin")
    return d


def bone_ring():
    d = Dungeon(9, 8, 9, "bone_ring")
    d.air(0, 1, 0, 8, 7, 8)
    d.box(1, 0, 1, 7, 0, 7, [("minecraft:soul_soil", 60), ("withered_soil", 40)])
    for (x, z) in ((4, 0), (4, 8), (0, 4), (8, 4), (1, 1), (7, 1), (1, 7), (7, 7)):
        height = 2 + d.rng.randrange(3)
        d.set(x, 0, z, "minecraft:soul_soil")
        for y in range(1, height + 1):
            d.set(x, y, z, "minecraft:bone_block", axis="y")
        d.skull(x, height + 1, z, wither=d.rng.random() < 0.2)
    d.set(4, 1, 4, "minecraft:chiseled_polished_blackstone")
    d.chest(4, 2, 4, LOOT, facing="south")
    d.lantern(3, 1, 3)
    d.lantern(5, 1, 5)
    d.guard(2, 1, 5, "withered_knight")
    d.guard(6, 1, 3, "minecraft:skeleton")
    return d


def lone_tomb():
    d = Dungeon(7, 6, 9, "lone_tomb")
    d.air(0, 1, 0, 6, 5, 8)
    d.box(0, 0, 0, 6, 0, 8, [("minecraft:polished_blackstone_bricks", 50), ("minecraft:cracked_polished_blackstone_bricks", 30), ("minecraft:soul_soil", 20)])
    for z in range(2, 7):
        d.set(2, 1, z, "minecraft:polished_blackstone_bricks")
        d.set(4, 1, z, "minecraft:polished_blackstone_bricks")
    d.set(3, 1, 2, "minecraft:polished_blackstone_bricks")
    d.set(3, 1, 6, "minecraft:polished_blackstone_bricks")
    d.chest(3, 1, 4, LOOT, facing="north")
    d.box(2, 2, 2, 4, 2, 6, "minecraft:polished_blackstone_slab", type="bottom", waterlogged="false")
    d.set(3, 2, 4, AIR)
    d.skull(3, 3, 2, wither=True)
    for (x, z) in ((0, 0), (6, 0), (0, 8), (6, 8)):
        d.pillar(x, z, 1, 2, "minecraft:polished_blackstone_bricks")
        d.lantern(x, 3, z)
    d.guard(1, 1, 7, "ash_wraith")
    d.guard(5, 1, 1, "ash_wraith")
    return d


def crystal_nook():
    d = Dungeon(9, 7, 9, "crystal_nook")
    d.air(0, 1, 0, 8, 6, 8)
    d.box(1, 0, 1, 7, 0, 7, [("minecraft:calcite", 45), ("minecraft:amethyst_block", 20), ("minecraft:blackstone", 35)])
    for (x, z, height, block) in ((1, 2, 3, "minecraft:amethyst_block"), (2, 1, 2, "minecraft:amethyst_block"), (7, 6, 4, "minecraft:amethyst_block"),
                                  (6, 7, 2, "minecraft:calcite"), (7, 2, 2, "minecraft:calcite"), (1, 6, 2, "minecraft:calcite")):
        for y in range(1, height + 1):
            d.set(x, y, z, block)
        d.set(x, height + 1, z, "onyx_cluster", facing="up", waterlogged="false")
    d.chest(4, 1, 4, LOOT, facing="south")
    d.set(4, 2, 4, AIR)
    d.guard(2, 1, 4, "onyx_cube")
    d.guard(6, 1, 4, "onyx_cube")
    return d


def mimic_trove():
    d = Dungeon(7, 5, 7, "mimic_trove")
    d.air(0, 1, 0, 6, 4, 6)
    d.box(0, 0, 0, 6, 0, 6, [("polished_onyx", 45), ("minecraft:blackstone", 35), ("minecraft:calcite", 20)])
    d.chest(1, 1, 3, LOOT, facing="east")
    d.guard(5, 1, 3, "mimic")
    d.guard(3, 1, 1, "mimic")
    for (x, z) in ((0, 0), (6, 6), (0, 6), (6, 0)):
        d.set(x, 1, z, "polished_onyx")
        d.lantern(x, 2, z, "onyx_lantern")
    return d


def lava_forge():
    d = Dungeon(9, 7, 7, "lava_forge")
    d.air(0, 1, 0, 8, 6, 6)
    d.box(0, 0, 0, 8, 0, 6, [("minecraft:blackstone", 55), ("magma_bricks", 25), ("minecraft:basalt", 20)])
    d.set(2, 1, 2, "minecraft:lava_cauldron")
    d.set(3, 1, 2, "minecraft:blast_furnace", facing="south", lit="false")
    d.set(4, 1, 2, "minecraft:anvil", facing="south")
    d.set(5, 1, 2, "minecraft:smithing_table")
    for x in (1, 7):
        d.pillar(x, 1, 1, 3, "magma_bricks")
        d.set(x, 4, 1, "minecraft:netherrack")
        d.set(x, 5, 1, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false")
    d.chest(6, 1, 2, LOOT, facing="south")
    d.guard(2, 1, 5, "minecraft:magma_cube")
    d.guard(6, 1, 5, "minecraft:magma_cube")
    return d


def sulfur_vent():
    d = Dungeon(7, 5, 7, "sulfur_vent")
    d.air(0, 1, 0, 6, 4, 6)
    d.box(0, 0, 0, 6, 0, 6, [("sulfur_rock", 70), ("sulfur_block", 20), ("minecraft:blackstone", 10)])
    d.set(3, 0, 3, "minecraft:magma_block")
    d.set(3, 1, 3, "minecraft:fire", age="0", east="false", north="false", south="false", up="false", west="false") if False else None
    d.chest(1, 1, 1, LOOT, facing="east")
    for (x, z) in ((5, 5), (5, 1), (1, 5)):
        d.set(x, 1, z, "sulfur_cluster", facing="up", waterlogged="false")
    d.guard(4, 1, 3, "magma_crab")
    d.guard(2, 1, 4, "minecraft:magma_cube")
    return d


FINDS = {
    "scorched_outpost": (scorched_watch, overturned_cart),
    "bone_shrine": (bone_ring, lone_tomb),
    "crystal_hollow": (crystal_nook, mimic_trove),
    "lava_forge": (lava_forge, sulfur_vent),
}


def generate():
    for name, template in (("nether_garden", nether_garden()), ("nomad_caravan", nomad_caravan())):
        problems = unsupported(template, world_rock=False)
        assert not problems, f"{name} : blocs sans support {problems[:6]}"
        template.save(name, "v2")
    for name, builders in FINDS.items():
        for index, build in enumerate(builders):
            template = build()
            problems = unsupported(template, world_rock=False)
            assert not problems, f"{name}/{index} : blocs sans support {problems[:6]}"
            template.save(name, f"find_{index}")
