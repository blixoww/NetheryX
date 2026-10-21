"""Donnees du contenu 1.2.0 : butins, recettes, tags, progres, modificateurs de butin."""
import json

from .common import MOD_ID, data_path, mid, write_json
from .data import (DECAY, FORTUNE_ORE, KILLED_BY_PLAYER, LOOTING, SILK_TOUCH, SURVIVES, advancement, block_table, both,
                   chest_table, cooking, count, enchanted, entity_table, has_items, item, loot_modifier, m, pool, shaped,
                   shapeless, stonecutting, tag, book)

WOODS = ("marrow", "wartstalk")


def wood_names(prefix):
    return [f"{prefix}_stem", f"stripped_{prefix}_stem", f"{prefix}_hyphae", f"stripped_{prefix}_hyphae", f"{prefix}_planks",
            f"{prefix}_stairs", f"{prefix}_slab", f"{prefix}_fence", f"{prefix}_fence_gate", f"{prefix}_door",
            f"{prefix}_trapdoor", f"{prefix}_button", f"{prefix}_pressure_plate"]


def slab_table(slab):
    block_table(slab, [{"rolls": 1, "bonus_rolls": 0, "entries": [item(slab, functions=[
        {"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [
            {"condition": "minecraft:block_state_property", "block": mid(slab), "properties": {"type": "double"}}]},
        DECAY])]}])


def silk_or(name, drop, functions):
    block_table(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        item(name, conditions=[SILK_TOUCH]), item(drop, functions=functions)]}]}])


def silk_only(name):
    block_table(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [item(name)], "conditions": [SILK_TOUCH]}])


SHEARS_OR_SILK = {"condition": "minecraft:any_of", "terms": [
    {"condition": "minecraft:match_tool", "predicate": {"items": ["minecraft:shears"]}}, SILK_TOUCH]}


def generate_block_loot():
    for prefix in WOODS:
        slab_table(f"{prefix}_slab")
        door = f"{prefix}_door"
        block_table(door, [{"rolls": 1, "bonus_rolls": 0, "entries": [item(door)], "conditions": [
            {"condition": "minecraft:block_state_property", "block": mid(door), "properties": {"half": "lower"}}, SURVIVES]}])
    slab_table("rimestone_brick_slab")
    silk_or("rich_quartz_ore", "minecraft:quartz", [count(2, 5), FORTUNE_ORE, DECAY])
    silk_or("rime_crystal", "rime_shard", [count(2, 4), FORTUNE_ORE, DECAY])
    for nylium in ("ossified_nylium", "wart_moss"):
        silk_or(nylium, "minecraft:netherrack", [])
    for glass in ("quartz_glass", "quartz_glass_pane"):
        silk_only(glass)
    # Lianes de moelle : aux cisailles / toucher de soie, sinon une chance sur trois
    for name, drop in (("marrow_vines", "marrow_vines"), ("marrow_vines_plant", "marrow_vines"),
                       ("ember_vines", "ember_vines"), ("ember_vines_plant", "ember_vines")):
        block_table(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            item(drop, conditions=[SHEARS_OR_SILK]),
            item(drop, conditions=[{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                                              "chances": [0.33, 0.55, 0.77, 1.0]}])]}]}])
    # Jungle infernale : fougere (moitie basse uniquement) et herbe ardente aux cisailles
    block_table("hellfern", [{"rolls": 1, "bonus_rolls": 0, "entries": [item("hellfern")], "conditions": [
        SHEARS_OR_SILK, {"condition": "minecraft:block_state_property", "block": mid("hellfern"), "properties": {"half": "lower"}}]}])
    block_table("blazing_grass", [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        item("blazing_grass", conditions=[SHEARS_OR_SILK]),
        item("fire_berries", conditions=[{"condition": "minecraft:random_chance", "chance": 0.1}])]}]}])
    # Verruliane : seulement les fruits
    for name in ("wartvine", "wartvine_plant"):
        block_table(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [item("wartfruit", functions=[count(1, 2)])], "conditions": [
            {"condition": "minecraft:block_state_property", "block": mid(name), "properties": {"fruit": "true"}}]}])
    block_table("marrow_cap", [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        item("marrow_cap", conditions=[SILK_TOUCH]),
        item("minecraft:bone_meal", functions=[count(1, 3)], conditions=[{"condition": "minecraft:random_chance", "chance": 0.3}]),
        item("marrow_cap")]}]}])


def generate_entity_loot():
    smelt = {"function": "minecraft:furnace_smelt", "conditions": [
        {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"flags": {"is_on_fire": True}}}]}
    entity_table("magmafish", [
        {"rolls": 1, "entries": [item("magmafish", functions=[smelt])]},
        {"rolls": 1, "entries": [item("magmafish_scale", functions=[count(0, 1), LOOTING])]},
    ])
    entity_table("piglin_merchant", [{"rolls": 1, "entries": [item("minecraft:gold_nugget", functions=[count(3, 8)])]}])
    entity_table("mimic", [
        {"rolls": 1, "entries": [{"type": "minecraft:loot_table", "name": mid("chests/mimic")}]},
    ])
    entity_table("basalt_burrower", [
        {"rolls": 1, "entries": [item("burrower_fang", functions=[count(1, 1), LOOTING])], "conditions": [KILLED_BY_PLAYER]},
        {"rolls": 2, "entries": [item("minecraft:gold_nugget", weight=6, functions=[count(2, 5)]),
                                 item("minecraft:quartz", weight=6, functions=[count(1, 4)]),
                                 item("onyx_shard", weight=2, functions=[count(1, 2)]),
                                 item("minecraft:basalt", weight=4, functions=[count(1, 3)])]},
    ])
    entity_table("lantern_bearer", [{"rolls": 1, "entries": [item("soul_dust", functions=[count(0, 2), LOOTING])]}])
    entity_table("cinder_lizard", [{"rolls": 1, "entries": [item("minecraft:leather", functions=[count(0, 2), LOOTING])]},
                                   {"rolls": 1, "entries": [item("magma_shell", functions=[count(0, 1), LOOTING])]}])


def generate_final_boss():
    entity_table("nether_heart", [
        {"rolls": 1, "entries": [item("abyssal_upgrade_template")]},
        {"rolls": 1, "entries": [item("abyssal_shard", functions=[count(4, 6), LOOTING])]},
        {"rolls": 1, "entries": [item("onyx", functions=[count(6, 12)])]},
        {"rolls": 1, "entries": [item("onyx_apple", functions=[count(1, 2)])]},
        {"rolls": 1, "entries": [item("minecraft:netherite_ingot")]},
        {"rolls": 1, "entries": [item("ash_wyvern_egg")]},
    ])
    # Les quatre trophees : Sentinelle, Sorcier, Champion et Golem de basalte
    shaped("sealed_heart", ["OCO", "SXW", "OHO"], {"O": "onyx_block", "C": "minecraft:crying_obsidian", "S": "sentinel_eye",
                                                  "X": "champion_crown", "W": "warlock_sigil", "H": "basalt_heartstone"},
           "sealed_heart", 1)
    shaped("onyx_apple", ["SSS", "SAS", "SSS"], {"S": "onyx_shard", "A": "minecraft:golden_apple"}, "onyx_apple", 1, "food")
    shaped("abyssal_upgrade_template", ["#T#", "#O#", "###"], {"#": "onyx_shard", "T": "abyssal_upgrade_template", "O": "abyssal_shard"},
           "abyssal_upgrade_template", 2)
    for piece in ("helmet", "chestplate", "leggings", "boots"):
        write_json(data_path("recipes", f"abyssal_{piece}_smithing.json"), {
            "type": "minecraft:smithing_transform", "template": {"item": mid("abyssal_upgrade_template")},
            "base": {"item": mid(f"onyx_{piece}")}, "addition": {"item": mid("abyssal_shard")},
            "result": {"item": mid(f"abyssal_{piece}")}})
    advancement("nether_heart", "sealed_heart", "challenge", "root", {"kill": {
        "trigger": "minecraft:player_killed_entity", "conditions": {"entity": [{"condition": "minecraft:entity_properties",
                                                                                 "entity": "this", "predicate": {"type": mid("nether_heart")}}]}}})


def generate_chest_loot():
    write_json(data_path("loot_tables", "gameplay", "lava_trap.json"), {"type": "minecraft:chest", "pools": [{"rolls": 1, "entries": [
        item("magmafish", weight=60),
        item("magmafish_scale", weight=14),
        item("minecraft:magma_cream", weight=10),
        item("minecraft:gold_nugget", weight=8, functions=[count(2, 5)]),
        item("minecraft:blaze_powder", weight=4),
        item("onyx_shard", weight=2),
        item("minecraft:netherite_scrap", weight=1),
    ]}]})
    chest_table("mimic", [
        pool(1, 1, [item("onyx_infusion_template", weight=3), item("minecraft:enchanted_golden_apple", weight=1),
                    item("minecraft:diamond", weight=6, functions=[count(2, 4)]), item("onyx", weight=6, functions=[count(1, 3)])]),
        pool(3, 5, [
            item("minecraft:gold_ingot", weight=12, functions=[count(3, 8)]),
            item("minecraft:golden_apple", weight=6),
            item("onyx_shard", weight=10, functions=[count(2, 6)]),
            item("minecraft:diamond", weight=4, functions=[count(1, 2)]),
            item("minecraft:experience_bottle", weight=6, functions=[count(3, 8)]),
            enchanted("minecraft:book", 25, 35, 6, True),
            enchanted("onyx_sword", 20, 30, 2, True),
        ]),
    ])
    chest_table("nomad_caravan", [
        pool(4, 7, [
            item("minecraft:gold_ingot", weight=14, functions=[count(2, 6)]),
            item("sulfur", weight=10, functions=[count(3, 8)]),
            item("wartfruit", weight=10, functions=[count(2, 6)]),
            item("grilled_magmafish", weight=8, functions=[count(1, 4)]),
            item("rime_shard", weight=6, functions=[count(2, 4)]),
            item("blaze_pepper_seeds", weight=6, functions=[count(2, 5)]),
            item("onyx_shard", weight=6, functions=[count(1, 3)]),
            item("minecraft:saddle", weight=4),
            item("obsidian_bucket", weight=2),
            item("onyx_infusion_template", weight=2),
            book(4),
        ]),
    ])


def generate_recipes():
    b = "building"
    for prefix in WOODS:
        shapeless(f"{prefix}_planks", [f"#netheryx:{prefix}_stems"], f"{prefix}_planks", 4, b)
        shaped(f"{prefix}_hyphae", ["##", "##"], {"#": f"{prefix}_stem"}, f"{prefix}_hyphae", 3, b)
        shaped(f"stripped_{prefix}_hyphae", ["##", "##"], {"#": f"stripped_{prefix}_stem"}, f"stripped_{prefix}_hyphae", 3, b)
        planks = f"{prefix}_planks"
        shaped(f"{prefix}_stairs", ["#  ", "## ", "###"], {"#": planks}, f"{prefix}_stairs", 4, b)
        shaped(f"{prefix}_slab", ["###"], {"#": planks}, f"{prefix}_slab", 6, b)
        shaped(f"{prefix}_fence", ["W#W", "W#W"], {"W": planks, "#": "minecraft:stick"}, f"{prefix}_fence", 3, "misc")
        shaped(f"{prefix}_fence_gate", ["#W#", "#W#"], {"W": planks, "#": "minecraft:stick"}, f"{prefix}_fence_gate", 1, "redstone")
        shaped(f"{prefix}_door", ["##", "##", "##"], {"#": planks}, f"{prefix}_door", 3, "redstone")
        shaped(f"{prefix}_trapdoor", ["###", "###"], {"#": planks}, f"{prefix}_trapdoor", 2, "redstone")
        shapeless(f"{prefix}_button", [planks], f"{prefix}_button", 1, "redstone")
        shaped(f"{prefix}_pressure_plate", ["##"], {"#": planks}, f"{prefix}_pressure_plate", 1, "redstone")
    shaped("rimestone_bricks", ["##", "##"], {"#": "rimestone"}, "rimestone_bricks", 4, b)
    shaped("rimestone_brick_stairs", ["#  ", "## ", "###"], {"#": "rimestone_bricks"}, "rimestone_brick_stairs", 4, b)
    shaped("rimestone_brick_slab", ["###"], {"#": "rimestone_bricks"}, "rimestone_brick_slab", 6, b)
    shaped("rimestone_brick_wall", ["###", "###"], {"#": "rimestone_bricks"}, "rimestone_brick_wall", 6, "misc")
    for src in ("rimestone", "rimestone_bricks"):
        stonecutting(f"rimestone_brick_stairs_from_{src}_stonecutting", src, "rimestone_brick_stairs")
        stonecutting(f"rimestone_brick_slab_from_{src}_stonecutting", src, "rimestone_brick_slab", 2)
        stonecutting(f"rimestone_brick_wall_from_{src}_stonecutting", src, "rimestone_brick_wall")
    stonecutting("rimestone_bricks_from_rimestone_stonecutting", "rimestone", "rimestone_bricks")
    shaped("rime_ice", ["##", "##"], {"#": "rime_shard"}, "rime_ice", 1, b)
    shaped("quartz_glass", [" Q ", "QGQ", " Q "], {"Q": "minecraft:quartz", "G": "minecraft:glass"}, "quartz_glass", 4, b)
    shaped("quartz_glass_pane", ["###", "###"], {"#": "quartz_glass"}, "quartz_glass_pane", 16, "misc")
    shaped("quartz_lamp", [" G ", "QLQ", " G "], {"G": "quartz_glass", "Q": "minecraft:quartz_block", "L": "minecraft:glowstone"},
           "quartz_lamp", 2, b)
    shapeless("marrow_meal", ["marrow_cap"], "marrow_meal", 3)
    shapeless("bone_meal_from_marrow_meal", ["marrow_meal"], "minecraft:bone_meal", 3)
    shapeless("wartfruit_tart", ["wartfruit", "wartfruit", "cinder_wheat", "ember_egg"], "wartfruit_tart", 1, "food")
    cooking("grilled_magmafish", "smelting", "magmafish", "grilled_magmafish", 0.35, 200, "food")
    cooking("grilled_magmafish_from_smoking", "smoking", "magmafish", "grilled_magmafish", 0.35, 100, "food")
    cooking("grilled_magmafish_from_campfire_cooking", "campfire_cooking", "magmafish", "grilled_magmafish", 0.35, 600, "food")
    shaped("wraith_cloth", ["EWE"], {"E": "wraith_essence", "W": "minecraft:white_wool"}, "wraith_cloth", 2)
    e = "equipment"
    for material, prefix, names in (("magma_shell", "ember", ("ember_helmet", "ember_chestplate", "ember_leggings", "ember_boots")),
                                    ("wraith_cloth", "wraith", ("wraith_hood", "wraith_robe", "wraith_leggings", "wraith_boots"))):
        shaped(names[0], ["###", "# #"], {"#": material}, names[0], category=e)
        shaped(names[1], ["# #", "###", "###"], {"#": material}, names[1], category=e)
        shaped(names[2], ["###", "# #", "# #"], {"#": material}, names[2], category=e)
        shaped(names[3], ["# #", "# #"], {"#": material}, names[3], category=e)
    shaped("carapace_helmet", ["###", "# #"], {"#": "basalt_carapace"}, "carapace_helmet", category=e)
    shaped("carapace_leggings", ["###", "# #", "# #"], {"#": "basalt_carapace"}, "carapace_leggings", category=e)
    shaped("carapace_boots", ["# #", "# #"], {"#": "basalt_carapace"}, "carapace_boots", category=e)
    shaped("onyx_infusion_template", ["#T#", "#O#", "###"], {"#": "minecraft:blackstone", "T": "onyx_infusion_template", "O": "onyx"},
           "onyx_infusion_template", 2)
    shaped("lava_trap", ["ICI", "C C", "ICI"], {"I": "minecraft:iron_bars", "C": "minecraft:chain"}, "lava_trap", 1)
    shaped("infernal_cauldron", ["B B", "BMB", "BBB"], {"B": "minecraft:polished_blackstone", "M": "minecraft:magma_block"},
           "infernal_cauldron", 1)
    shaped("soul_altar", ["DCD", " P ", "PPP"], {"D": "soul_dust", "C": "minecraft:crying_obsidian", "P": "minecraft:polished_blackstone"},
           "soul_altar", 1)
    shaped("ward_lantern", ["OEO", "ELE", "OEO"], {"O": "onyx_shard", "E": "wraith_essence", "L": "minecraft:soul_lantern"},
           "ward_lantern", 1)
    shaped("fire_veil", [" C ", "CMC", " C "], {"C": "wraith_cloth", "M": "minecraft:magma_cream"}, "fire_veil", 1)
    for addition, infusion in (("minecraft:magma_cream", "smelting"), ("burrower_fang", "reach"), ("onyx", "resilience")):
        write_json(data_path("recipes", f"infusion_{infusion}.json"), {
            "type": mid("infusion"), "template": {"item": mid("onyx_infusion_template")},
            "addition": {"item": mid(addition)}, "infusion": infusion})


def generate_tags():
    mc, fg = "minecraft", "forge"
    for prefix in WOODS:
        names = wood_names(prefix)
        tag("blocks", mc, "mineable/axe", m(names))
        both(MOD_ID, f"{prefix}_stems", m(names[:4]))
        both(mc, "logs", [f"#netheryx:{prefix}_stems"])
        both(mc, "planks", m([f"{prefix}_planks"]))
        both(mc, "wooden_stairs", m([f"{prefix}_stairs"]))
        both(mc, "wooden_slabs", m([f"{prefix}_slab"]))
        both(mc, "wooden_fences", m([f"{prefix}_fence"]))
        both(mc, "fence_gates", m([f"{prefix}_fence_gate"]))
        both(mc, "wooden_doors", m([f"{prefix}_door"]))
        both(mc, "wooden_trapdoors", m([f"{prefix}_trapdoor"]))
        both(mc, "wooden_buttons", m([f"{prefix}_button"]))
        both(mc, "wooden_pressure_plates", m([f"{prefix}_pressure_plate"]))
        tag("items", mc, "non_flammable_wood", m(names))
    rimestone = ["rimestone", "rimestone_bricks", "rimestone_brick_stairs", "rimestone_brick_slab", "rimestone_brick_wall"]
    tag("blocks", mc, "mineable/pickaxe", m(rimestone + ["rime_ice", "rime_crystal", "rich_quartz_ore", "quartz_lamp", "lava_trap",
                                                      "infernal_cauldron", "soul_altar", "ward_lantern", "ossified_nylium", "wart_moss"]))
    tag("blocks", mc, "needs_stone_tool", m(["rimestone", "rimestone_bricks", "rich_quartz_ore"]))
    tag("blocks", mc, "mineable/hoe", m(["marrow_cap", "wart_bulb"]))
    both(mc, "stairs", m(["rimestone_brick_stairs"]))
    both(mc, "slabs", m(["rimestone_brick_slab"]))
    both(mc, "walls", m(["rimestone_brick_wall"]))
    tag("blocks", mc, "nylium", m(["ossified_nylium", "wart_moss"]))
    tag("blocks", mc, "wart_blocks", m(["marrow_cap"]))
    tag("blocks", mc, "climbable", m(["marrow_vines", "marrow_vines_plant", "wartvine", "wartvine_plant", "ember_vines",
                                      "ember_vines_plant"]))
    tag("blocks", mc, "replaceable", m(["hellfern", "blazing_grass"]))
    tag("blocks", mc, "replaceable_by_trees", m(["hellfern", "blazing_grass", "ember_vines", "ember_vines_plant"]))
    tag("blocks", mc, "sword_efficient", m(["hellfern", "blazing_grass", "ember_vines", "ember_vines_plant"]))
    both(mc, "tall_flowers", m(["hellfern"]))
    tag("blocks", mc, "ice", m(["rime_ice"]))
    tag("blocks", mc, "crystal_sound_blocks", m(["rime_crystal"]))
    tag("blocks", mc, "impermeable", m(["quartz_glass"]))
    tag("blocks", mc, "base_stone_nether", m(["rimestone"]))
    tag("blocks", mc, "nether_carver_replaceables", m(["rimestone", "ossified_nylium", "wart_moss"]))
    tag("blocks", mc, "replaceable_by_trees", m(["marrow_sprouts"]))
    tag("blocks", mc, "dragon_immune", m(["soul_altar"]))
    both(fg, "ores/quartz", m(["rich_quartz_ore"]))
    both(fg, "glass", m(["quartz_glass"]))
    both(fg, "glass/colorless", m(["quartz_glass"]))
    both(fg, "glass_panes", m(["quartz_glass_pane"]))
    tag("items", fg, "raw_fishes", m(["magmafish"]))
    tag("items", fg, "cooked_fishes", m(["grilled_magmafish"]))
    tag("items", fg, "armors/helmets", m(["ember_helmet", "wraith_hood", "carapace_helmet"]))
    tag("items", fg, "armors/chestplates", m(["ember_chestplate", "wraith_robe"]))
    tag("items", fg, "armors/leggings", m(["ember_leggings", "wraith_leggings", "carapace_leggings"]))
    tag("items", fg, "armors/boots", m(["ember_boots", "wraith_boots", "carapace_boots"]))
    tag("items", mc, "trimmable_armor", m(["ember_helmet", "ember_chestplate", "ember_leggings", "ember_boots", "wraith_hood",
                                           "wraith_robe", "wraith_leggings", "wraith_boots", "carapace_helmet",
                                           "carapace_leggings", "carapace_boots"]))
    tag("items", mc, "piglin_loved", m(["onyx_infusion_template"]))
    # Curios API (optionnel) : le Voile ignifuge se porte en breloque
    tag("items", "curios", "charm", m(["fire_veil"]))
    write_json(data_path("curios", "entities", "netheryx_player.json"), {"entities": ["player"], "slots": ["charm"]})
    tag("items", MOD_ID, "cinder_lizard_food", m(["fire_berries"]))
    tag("items", MOD_ID, "lantern_bearer_food", m(["glowcap"]))
    tag("items", MOD_ID, "lava_trap_bait", m(["stalker_meat", "crab_leg", "infernal_beef", "infernal_chicken"]) + ["minecraft:rotten_flesh"])
    tag("blocks", MOD_ID, "burrower_diggable", ["minecraft:netherrack", "minecraft:basalt", "minecraft:smooth_basalt",
                                               "minecraft:blackstone", "minecraft:soul_soil", mid("ash_block"), mid("sulfur_rock")])
    tag("blocks", MOD_ID, "cinder_lizard_fast_ground", ["minecraft:basalt", "minecraft:polished_basalt", "minecraft:smooth_basalt",
                                                       "minecraft:blackstone", "minecraft:magma_block"])
    tag("blocks", MOD_ID, "ember_stride_ground", ["#minecraft:base_stone_nether", "#minecraft:nylium", "minecraft:soul_soil",
                                                 "minecraft:soul_sand", "minecraft:magma_block", "minecraft:smooth_basalt",
                                                 mid("ash_block"), mid("sulfur_rock"), mid("withered_soil")])
    tag("blocks", MOD_ID, "nether_vegetation_ground", ["#minecraft:nylium", mid("nether_grass_block")])
    tag("blocks", MOD_ID, "nether_plant_soil", m(["ossified_nylium", "wart_moss", "rimestone"]))
    tag("blocks", MOD_ID, "nether_animals_spawnable_on", m(["ossified_nylium", "wart_moss", "rimestone"]))
    new_biomes = m(["marrow_thicket", "rimefrost_chasm", "wartvine_tangle", "quartz_reef", "hellwood_jungle"])
    tag("worldgen/biome", mc, "is_nether", new_biomes)
    tag("worldgen/biome", fg, "is_hot/nether", m(["marrow_thicket", "wartvine_tangle", "quartz_reef", "hellwood_jungle"]))
    tag("worldgen/biome", fg, "is_cold/nether", m(["rimefrost_chasm"]))
    tag("worldgen/biome", fg, "is_lush", m(["wartvine_tangle", "hellwood_jungle"]))
    tag("worldgen/biome", fg, "is_dense", m(["hellwood_jungle"]))
    tag("worldgen/biome", fg, "is_spooky", m(["marrow_thicket"]))
    tag("worldgen/biome", mc, "has_structure/bastion_remnant", m(["wartvine_tangle", "quartz_reef"]))
    tag("worldgen/biome", mc, "has_structure/nether_fossil", m(["marrow_thicket"]))
    tag("worldgen/biome", MOD_ID, "has_structure/withered_catacombs", m(["marrow_thicket"]))
    tag("worldgen/biome", MOD_ID, "has_structure/onyx_citadel", m(["rimefrost_chasm", "quartz_reef"]))
    tag("worldgen/biome", MOD_ID, "has_structure/infernal_sanctum", m(["wartvine_tangle", "hellwood_jungle"]))
    tag("worldgen/biome", MOD_ID, "has_structure/nether_garden", m(["hellwood_jungle"]))
    tag("worldgen/biome", mc, "has_structure/bastion_remnant", m(["hellwood_jungle"]))
    tag("worldgen/biome", MOD_ID, "has_structure/nomad_caravan", ["#minecraft:is_nether"])
    tag("worldgen/structure", MOD_ID, "soul_compass_targets", m(["nomad_caravan", "hidden_cache"]))


def generate_advancements():
    advancement("lava_trap", "magmafish", "task", "root", {"fish": has_items("magmafish")})
    advancement("rime_ice", "rime_ice", "task", "root", {"ice": has_items("rime_ice")})
    advancement("soul_altar", "soul_altar", "task", "root", {"altar": has_items("soul_altar")})
    advancement("ride_lizard", "cinder_lizard_spawn_egg", "goal", "root", {"ride": {
        "trigger": "minecraft:started_riding", "conditions": {"player": [{"condition": "minecraft:entity_properties", "entity": "this",
                                                                          "predicate": {"vehicle": {"type": mid("cinder_lizard")}}}]}}})
    advancement("infusion", "onyx_infusion_template", "task", "root", {"template": has_items("onyx_infusion_template")})


def generate_loot_modifiers():
    entries = [
        loot_modifier("bastion_treasure_infusion_template", "minecraft:chests/bastion_treasure", "onyx_infusion_template", 0.35),
        loot_modifier("bastion_other_infusion_template", "minecraft:chests/bastion_other", "onyx_infusion_template", 0.08),
        loot_modifier("fortress_rime_shard", "minecraft:chests/nether_bridge", "rime_shard", 0.3, 1, 4),
        loot_modifier("ruined_portal_magmafish", "minecraft:chests/ruined_portal", "grilled_magmafish", 0.3, 1, 3),
    ]
    write_json(data_path("loot_modifiers", "infusion_smelting.json"), {"type": mid("infusion_smelting"), "conditions": [
        {"condition": "minecraft:match_tool", "predicate": {"nbt": '{NetheryXInfusion:"smelting"}'}}]})
    entries.append(mid("infusion_smelting"))
    path = data_path("loot_modifiers", "global_loot_modifiers.json", namespace="forge")
    existing = json.loads(path.read_text(encoding="utf-8"))
    existing["entries"].extend(e for e in entries if e not in existing["entries"])
    write_json(path, existing)


def generate():
    generate_final_boss()
    generate_block_loot()
    generate_entity_loot()
    generate_chest_loot()
    generate_recipes()
    generate_tags()
    generate_advancements()
    generate_loot_modifiers()
    from . import data_v124
    data_v124.generate()
    from . import loot_balance
    loot_balance.generate()
