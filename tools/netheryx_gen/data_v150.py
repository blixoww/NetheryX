"""Donnees 1.5.0 : silhouettes et lumiere des biomes, apparitions des animaux, petites trouvailles."""
from .common import MOD_ID, mid
from .data import chest_table, enchanted, item, m, pool, tag
from .worldgen import BIOME_FILTER, biome_modifier, configured, placed, spawner, state

# Roches des biomes : les cavernes creusent dedans, et les minerais s'y forment
BIOME_STONES = ["minecraft:terracotta", "minecraft:orange_terracotta", "minecraft:red_terracotta", "minecraft:brown_terracotta",
                "minecraft:yellow_terracotta", "minecraft:white_terracotta", "minecraft:light_gray_terracotta", "minecraft:calcite",
                "minecraft:diorite", "minecraft:tuff", "minecraft:deepslate", "minecraft:dripstone_block", "minecraft:packed_ice",
                "minecraft:nether_wart_block", "minecraft:amethyst_block", "minecraft:smooth_basalt", "minecraft:snow_block",
                "minecraft:quartz_block", "minecraft:bone_block", "minecraft:magma_block"]
SOILS = m(["sulfur_block", "ember_nylium"]) + ["minecraft:crimson_nylium"]

SHROOM = "minecraft:shroomlight"
# biome : (colonne (roche, anneau, taille, rarete), cristaux (corps, pointe, taille, rarete), lumiere (masse, eclat, taille, nombre))
DECOR = {
    "ember_meadow": (("minecraft:orange_terracotta", "minecraft:yellow_terracotta", 3, 9), None, ("ember_wart_block", SHROOM, 2, 3)),
    "ember_grove": (("minecraft:red_terracotta", "minecraft:orange_terracotta", 4, 7), None, ("ember_wart_block", SHROOM, 2, 5)),
    "hellwood_jungle": (("minecraft:brown_terracotta", "ember_wart_block", 4, 6), None, ("ember_wart_block", SHROOM, 3, 6)),
    "wartvine_tangle": (("minecraft:nether_wart_block", "minecraft:red_terracotta", 3, 6), None, ("minecraft:nether_wart_block", SHROOM, 3, 6)),
    "sulfur_springs": (("sulfur_rock", "sulfur_block", 3, 7), ("sulfur_block", "minecraft:ochre_froglight", 3, 5), ("sulfur_rock", "minecraft:ochre_froglight", 2, 4)),
    "quartz_reef": (("minecraft:calcite", "minecraft:quartz_block", 4, 5), ("minecraft:quartz_block", "minecraft:pearlescent_froglight", 4, 3), ("minecraft:calcite", "minecraft:pearlescent_froglight", 2, 4)),
    "marrow_thicket": (("minecraft:bone_block", "minecraft:calcite", 3, 5), None, ("minecraft:bone_block", "minecraft:ochre_froglight", 2, 5)),
    "rimefrost_chasm": (("rimestone", "minecraft:packed_ice", 3, 6), ("minecraft:packed_ice", "minecraft:sea_lantern", 4, 3), ("minecraft:packed_ice", "minecraft:sea_lantern", 2, 5)),
    "withered_forest": (("minecraft:deepslate", "minecraft:tuff", 4, 6), None, ("minecraft:deepslate", "minecraft:verdant_froglight", 2, 4)),
    "petrified_onyx_forest": (("minecraft:blackstone", "minecraft:amethyst_block", 4, 5), ("minecraft:amethyst_block", "minecraft:crying_obsidian", 4, 3), ("minecraft:blackstone", "minecraft:crying_obsidian", 2, 5)),
    "ashen_wastes": (("minecraft:basalt", "minecraft:tuff", 4, 6), None, ("minecraft:tuff", "minecraft:glowstone", 1, 3)),
    "molten_glass_lake": (("minecraft:blackstone", "minecraft:magma_block", 4, 5), ("molten_glass", "molten_crystal", 3, 3), ("minecraft:blackstone", "minecraft:magma_block", 2, 5)),
}


def decor_state(name):
    if name == "minecraft:basalt":
        return state(name, axis="y")
    if name == "minecraft:bone_block":
        return state(name, axis="y")
    if name == "molten_crystal":
        return state(name, facing="up", waterlogged="false")
    return state(name)


def generate_decor():
    for biome_name, (pillar, crystal, glow) in DECOR.items():
        features = []
        if pillar:
            main, accent, size, rarity = pillar
            configured(f"{biome_name}_pillar", mid("biome_pillar"), {"main": decor_state(main), "accent": decor_state(accent), "size": size})
            placed(f"{biome_name}_pillar", f"{biome_name}_pillar", [
                {"type": "minecraft:rarity_filter", "chance": rarity},
                {"type": "minecraft:count_on_every_layer", "count": 1}, BIOME_FILTER])
            features.append(mid(f"{biome_name}_pillar"))
        if crystal:
            main, accent, size, rarity = crystal
            configured(f"{biome_name}_crystals", mid("crystal_burst"), {"main": decor_state(main), "accent": decor_state(accent), "size": size})
            placed(f"{biome_name}_crystals", f"{biome_name}_crystals", [
                {"type": "minecraft:rarity_filter", "chance": rarity},
                {"type": "minecraft:count_on_every_layer", "count": 1}, BIOME_FILTER])
            features.append(mid(f"{biome_name}_crystals"))
        if glow:
            main, accent, size, count = glow
            configured(f"{biome_name}_glow", mid("glow_cluster"), {"main": decor_state(main), "accent": decor_state(accent), "size": size})
            placed(f"{biome_name}_glow", f"{biome_name}_glow", [
                {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
                {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 36}, "max_inclusive": {"below_top": 8}}},
                {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 24,
                 "target_condition": {"type": "minecraft:solid"}, "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
                {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1}, BIOME_FILTER])
            features.append(mid(f"{biome_name}_glow"))
        biome_modifier(f"decor_{biome_name}", {"type": "forge:add_features", "biomes": mid(biome_name), "features": features, "step": "vegetal_decoration"})


def generate_stones():
    tag("blocks", "minecraft", "base_stone_nether", BIOME_STONES)
    tag("blocks", "minecraft", "nether_carver_replaceables", BIOME_STONES + SOILS)


def generate_spawns():
    """Les animaux vivent la ou ils ont un sens (leur biome et quelques cousins vanilla), au lieu de partout."""
    biome_modifier("add_animals_crimson", {"type": "forge:add_spawns", "biomes": "minecraft:crimson_forest", "spawners": [
        spawner("ember_cow", 14, 2, 3), spawner("cinder_hen", 12, 2, 3)]})
    biome_modifier("add_animals_wastes", {"type": "forge:add_spawns", "biomes": "minecraft:nether_wastes", "spawners": [
        spawner("blaze_pig", 10, 2, 3), spawner("magma_crab", 10, 1, 3)]})
    biome_modifier("add_animals_deltas", {"type": "forge:add_spawns", "biomes": "minecraft:basalt_deltas", "spawners": [
        spawner("magma_crab", 12, 1, 3)]})


def generate_finds():
    tags = {
        "scorched_outpost": m(["ember_meadow", "ember_grove", "hellwood_jungle", "wartvine_tangle"]) + ["minecraft:crimson_forest", "minecraft:nether_wastes"],
        "bone_shrine": m(["withered_forest", "ashen_wastes", "marrow_thicket"]) + ["minecraft:soul_sand_valley", "minecraft:warped_forest"],
        "crystal_hollow": m(["quartz_reef", "petrified_onyx_forest", "rimefrost_chasm"]),
        "lava_forge": m(["molten_glass_lake", "sulfur_springs"]) + ["minecraft:basalt_deltas", "minecraft:nether_wastes"],
    }
    for name, biomes in tags.items():
        tag("worldgen/biome", MOD_ID, f"has_structure/{name}", biomes)
    chest_table("wayside_cache", [
        pool(2, 4, [
            item("onyx_shard", weight=10), item("sulfur", weight=10), item("minecraft:gold_nugget", weight=8), item("minecraft:quartz", weight=8),
            item("magma_shell", weight=5), item("fire_berries", weight=8), item("soul_dust", weight=6), item("glimmer_elixir", weight=2),
            item("minecraft:gold_ingot", weight=4), item("minecraft:ender_pearl", weight=2)]),
        pool(0, 1, [
            item("onyx", weight=4), item("minecraft:netherite_scrap", weight=1), item("nether_lantern", weight=1), item("recall_stone", weight=1),
            enchanted("onyx_pickaxe", 10, 20, 1), enchanted("onyx_sword", 10, 20, 1)]),
    ])


def generate():
    generate_stones()
    generate_decor()
    generate_spawns()
    generate_finds()
