"""Donnees 1.4.2 : flore des biomes, animaux, equipement des boss, Atlas du Nether, nouvelles creatures."""
import json

from .common import MOD_ID, data_path, mid, write_json
from .data import count, entity_table, item, m, tag, LOOTING
from .worldgen import biome_modifier, patch, spawner, state

# Flore dense, comme les grands packs de biomes : (bloc, essais, etalement, tirages par etage)
FLORA = {
    "ember_meadow": [("ember_bloom", 24, 6, 3), ("ash_daisy", 16, 6, 2), ("blazing_grass", 48, 7, 5), ("minecraft:crimson_roots", 24, 6, 2)],
    "ember_grove": [("blazing_grass", 40, 7, 4), ("ember_roots", 32, 6, 4), ("ember_bloom", 12, 5, 1)],
    "hellwood_jungle": [("blazing_grass", 64, 7, 6), ("ember_roots", 32, 6, 3), ("crimson_bellflower", 12, 5, 2)],
    "wartvine_tangle": [("minecraft:crimson_roots", 48, 7, 5), ("minecraft:crimson_fungus", 12, 6, 2), ("crimson_bellflower", 16, 5, 2)],
    "sulfur_springs": [("sulfur_tulip", 24, 6, 3), ("charred_shrub", 12, 5, 1)],
    "quartz_reef": [("glowcap", 16, 6, 2)],
    "marrow_thicket": [("marrow_sprouts", 48, 7, 5), ("glowcap", 12, 6, 2)],
    "rimefrost_chasm": [("minecraft:snow", 64, 7, 5)],
    "withered_forest": [("charred_shrub", 24, 6, 3), ("withered_fungus", 16, 6, 2), ("minecraft:warped_roots", 16, 6, 1)],
    "petrified_onyx_forest": [("glowcap", 24, 6, 3), ("soul_lily", 12, 6, 1)],
    "ashen_wastes": [("charred_shrub", 32, 7, 3), ("ash_daisy", 12, 6, 1)],
    "molten_glass_lake": [],
}

# Animaux : chaque biome du mod en a au moins un, par petits troupeaux
ANIMALS = {
    "ember_grove": [("blaze_pig", 40, 3, 5), ("cinder_hen", 30, 3, 5)],
    "ember_meadow": [("ember_cow", 60, 3, 6), ("cinder_hen", 50, 3, 6)],
    "hellwood_jungle": [("ember_cow", 40, 3, 5), ("cinder_hen", 40, 3, 5)],
    "ashen_wastes": [("blaze_pig", 40, 2, 4), ("crystal_snail", 20, 2, 3)],
    "withered_forest": [("crystal_snail", 40, 2, 4), ("cinder_hen", 20, 2, 4)],
    "marrow_thicket": [("ember_cow", 40, 3, 5), ("crystal_snail", 25, 2, 3)],
    "rimefrost_chasm": [("crystal_snail", 50, 3, 5)],
    "quartz_reef": [("crystal_snail", 40, 2, 4), ("magma_crab", 40, 3, 5)],
    "molten_glass_lake": [("blaze_pig", 40, 3, 5)],
    "petrified_onyx_forest": [("crystal_snail", 50, 3, 5)],
    "sulfur_springs": [("magma_crab", 50, 3, 6)],
    "wartvine_tangle": [("ember_cow", 40, 3, 5)],
}

# Equipement des boss : du bon materiel, sans plus. Il tombe une fois, un seul objet sur deux possibles.
BOSS_GEAR = {
    "basalt_golem": [("minecraft:diamond_boots", "basalt_boots", {"minecraft:protection": 2, "minecraft:feather_falling": 3}),
                     ("minecraft:diamond_sword", "basalt_edge", {"minecraft:sharpness": 2, "minecraft:knockback": 1})],
    "infernal_warlock": [("minecraft:diamond_sword", "warlock_blade", {"minecraft:sharpness": 3, "minecraft:fire_aspect": 2}),
                         ("minecraft:diamond_chestplate", "warlock_mantle", {"minecraft:fire_protection": 4, "minecraft:unbreaking": 2})],
    "withered_champion": [("minecraft:diamond_sword", "champion_blade", {"minecraft:smite": 5, "minecraft:looting": 2}),
                          ("minecraft:diamond_helmet", "champion_helm", {"minecraft:protection": 3, "minecraft:unbreaking": 3})],
    "onyx_sentinel": [("minecraft:netherite_sword", "sentinel_edge", {"minecraft:sharpness": 3, "minecraft:unbreaking": 2}),
                      ("minecraft:netherite_chestplate", "sentinel_plate", {"minecraft:protection": 3, "minecraft:unbreaking": 2})],
    "nether_heart": [("minecraft:netherite_sword", "heart_blade", {"minecraft:sharpness": 5, "minecraft:fire_aspect": 2, "minecraft:looting": 2}),
                     ("minecraft:netherite_leggings", "heart_greaves", {"minecraft:protection": 4, "minecraft:fire_protection": 0, "minecraft:unbreaking": 3})],
}


def generate_flora():
    tag("blocks", MOD_ID, "nether_plant_soil", ["minecraft:calcite", "minecraft:smooth_basalt", "minecraft:quartz_block", mid("sulfur_block"), mid("sulfur_rock"),
                                                mid("withered_soil"), "minecraft:packed_ice", "minecraft:snow_block", "minecraft:brown_terracotta"])
    for biome_name, plants in FLORA.items():
        features = []
        for block, tries, spread, per_layer in plants:
            name = f"flora_{biome_name}_{block.split(':')[-1]}"
            props = {"layers": 1} if block == "minecraft:snow" else {}
            patch(name, state(block, **props), tries, spread, 3, per_layer)
            features.append(mid(name))
        if features:
            biome_modifier(f"flora_{biome_name}", {"type": "forge:add_features", "biomes": mid(biome_name), "features": features, "step": "vegetal_decoration"})


def generate_animals():
    tag("blocks", MOD_ID, "nether_animals_spawnable_on", [mid("ash_block"), mid("withered_soil"), mid("sulfur_block"), mid("nether_grass_block"), mid("ossified_nylium"),
                                                          "minecraft:snow_block", "minecraft:snow", "minecraft:packed_ice"])
    for biome_name, herds in ANIMALS.items():
        biome_modifier(f"animals_{biome_name}", {"type": "forge:add_spawns", "biomes": mid(biome_name),
                                                 "spawners": [spawner(n, w, lo, hi) for n, w, lo, hi in herds]})


def generate_boss_gear():
    for boss, choices in BOSS_GEAR.items():
        path = data_path("loot_tables", "entities", f"{boss}.json")
        table = json.loads(path.read_text(encoding="utf-8")) if path.is_file() else {"type": "minecraft:entity", "pools": []}
        entries = []
        for gear, key, enchants in choices:
            entries.append({"type": "minecraft:item", "name": gear, "functions": [
                {"function": "minecraft:set_enchantments", "enchantments": {k: v for k, v in enchants.items() if v > 0}},
                {"function": "minecraft:set_name", "name": {"translate": f"item.netheryx.boss_gear.{key}", "color": "gold", "italic": False}}]})
        table["pools"].append({"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:killed_by_player"}], "entries": entries})
        write_json(path, table)


def generate_atlas():
    write_json(data_path("recipes", "nether_atlas.json"), {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": "minecraft:map"}, {"item": mid("onyx_shard")}, {"item": mid("soul_dust")}],
        "result": {"item": mid("nether_atlas")}})


def generate_creatures():
    entity_table("rime_lurker", [
        {"rolls": 1, "entries": [item("rime_shard", functions=[count(1, 3), LOOTING])]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": 0.15}], "entries": [item("minecraft:blue_ice")]},
    ])
    entity_table("quartz_crawler", [
        {"rolls": 1, "entries": [item("minecraft:quartz", functions=[count(1, 3), LOOTING])]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": 0.12}], "entries": [item("onyx_shard")]},
    ])
    entity_table("rune_warden", [
        {"rolls": 1, "entries": [item("withered_bone", functions=[count(2, 4), LOOTING])]},
        {"rolls": 1, "entries": [item("onyx_shard", functions=[count(1, 3)])]},
    ])
    biome_modifier("add_rime_lurkers", {"type": "forge:add_spawns", "biomes": mid("rimefrost_chasm"), "spawners": [spawner("rime_lurker", 60, 1, 2)]})
    biome_modifier("add_quartz_crawlers", {"type": "forge:add_spawns", "biomes": mid("quartz_reef"), "spawners": [spawner("quartz_crawler", 70, 2, 4)]})


def generate_structure_biomes():
    """Biomes plus etendus : les structures s'ouvrent a plus de contrees pour rester a portee."""
    tag("worldgen/biome", MOD_ID, "has_structure/withered_catacombs", m(["petrified_onyx_forest", "rimefrost_chasm"]))
    tag("worldgen/biome", MOD_ID, "has_structure/wyvern_nest", m(["ember_grove", "hellwood_jungle", "withered_forest"]))
    tag("worldgen/biome", MOD_ID, "has_structure/lava_forge", m(["ember_grove", "ashen_wastes", "hellwood_jungle"]))
    # Pied des contreforts des forteresses : prolonge jusqu'au sol a la generation
    tag("blocks", MOD_ID, "keep_foundation", [mid("polished_onyx"), mid("magma_bricks"), "minecraft:polished_blackstone_bricks"])


def generate():
    generate_structure_biomes()
    generate_flora()
    generate_animals()
    generate_boss_gear()
    generate_atlas()
    generate_creatures()
