"""Donnees : tables de butin, recettes, tags, progres, modificateurs de butin globaux."""
from .common import MOD_ID, data_path, mid, write_json
from .content import BLOCKS, BLOCKS_WITHOUT_ITEM

SILK_TOUCH = {"condition": "minecraft:match_tool",
              "predicate": {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}
SURVIVES = {"condition": "minecraft:survives_explosion"}


def item(name, functions=None, conditions=None, weight=None):
    entry = {"type": "minecraft:item", "name": mid(name)}
    if weight is not None:
        entry["weight"] = weight
    if functions:
        entry["functions"] = functions
    if conditions:
        entry["conditions"] = conditions
    return entry


def count(lo, hi=None):
    if hi is None:
        return {"function": "minecraft:set_count", "count": lo}
    return {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}


def age_is(block, age):
    return {"condition": "minecraft:block_state_property", "block": mid(block), "properties": {"age": str(age)}}


FORTUNE_ORE = {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"}
DECAY = {"function": "minecraft:explosion_decay"}


def block_table(name, pools):
    write_json(data_path("loot_tables", "blocks", f"{name}.json"), {
        "type": "minecraft:block", "pools": pools, "random_sequence": f"{MOD_ID}:blocks/{name}"})


def self_drop(name):
    block_table(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [item(name)], "conditions": [SURVIVES]}])


def silk_or(name, drop, functions):
    block_table(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        item(name, conditions=[SILK_TOUCH]), item(drop, functions=functions)]}]}])


def generate_block_loot():
    special = {"onyx_ore", "sulfur_ore", "ember_nylium", "onyx_cluster", "sulfur_cluster", "onyx_brick_slab",
               "ember_slab", "ember_door", "withered_slab", "magma_brick_slab", "withered_door", "guardian_seal", "nether_grass", "cinder_wheat_crop", "blaze_pepper_crop", "soul_tuber_crop", "fire_berry_bush", "crusted_lava"}
    for name, _kind, _extra in BLOCKS:
        if name not in special:
            self_drop(name)

    silk_or("onyx_ore", "onyx", [FORTUNE_ORE, DECAY])
    silk_or("sulfur_ore", "sulfur", [count(2, 5), FORTUNE_ORE, DECAY])
    silk_or("onyx_cluster", "onyx_shard", [count(1, 3), FORTUNE_ORE, DECAY])
    silk_or("sulfur_cluster", "sulfur", [count(2, 4), FORTUNE_ORE, DECAY])
    block_table("ember_nylium", [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        item("ember_nylium", conditions=[SILK_TOUCH]), item("minecraft:netherrack", conditions=[SURVIVES])]}]}])

    for slab in ("onyx_brick_slab", "ember_slab", "withered_slab", "magma_brick_slab"):
        block_table(slab, [{"rolls": 1, "bonus_rolls": 0, "entries": [item(slab, functions=[
            {"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [
                {"condition": "minecraft:block_state_property", "block": mid(slab), "properties": {"type": "double"}}]},
            DECAY])]}])

    for door in ("ember_door", "withered_door"):
        block_table(door, [{"rolls": 1, "bonus_rolls": 0, "entries": [item(door)], "conditions": [
            {"condition": "minecraft:block_state_property", "block": mid(door), "properties": {"half": "lower"}},
            SURVIVES]}])

    block_table("blaze_pepper_crop", [
        {"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            item("blaze_pepper", functions=[count(1, 3)], conditions=[age_is("blaze_pepper_crop", 3)]),
            item("blaze_pepper_seeds")]}]},
        {"rolls": 1, "bonus_rolls": 0, "conditions": [age_is("blaze_pepper_crop", 3)], "entries": [item("blaze_pepper_seeds", functions=[
            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
             "formula": "minecraft:binomial_with_bonus_count", "parameters": {"extra": 2, "probability": 0.5714286}}])]},
    ])
    block_table("soul_tuber_crop", [
        {"rolls": 1, "bonus_rolls": 0, "entries": [item("soul_tuber")]},
        {"rolls": 1, "bonus_rolls": 0, "conditions": [age_is("soul_tuber_crop", 3)], "entries": [item("soul_tuber", functions=[
            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
             "formula": "minecraft:binomial_with_bonus_count", "parameters": {"extra": 3, "probability": 0.5714286}}])]},
    ])
    # Pots de fleurs : le pot + la plante
    for name, _kind, plant in BLOCKS:
        if _kind == "pot":
            block_table(name, [{"rolls": 1, "bonus_rolls": 0, "entries": [item("minecraft:flower_pot")], "conditions": [SURVIVES]},
                               {"rolls": 1, "bonus_rolls": 0, "entries": [item(plant)], "conditions": [SURVIVES]}])
    # Herbe du Nether : elle-meme aux cisailles, sinon parfois des graines de ble de cendre
    shears = {"condition": "minecraft:match_tool", "predicate": {"items": ["minecraft:shears"]}}
    block_table("nether_grass", [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        item("nether_grass", conditions=[shears]),
        item("cinder_wheat_seeds", conditions=[{"condition": "minecraft:random_chance", "chance": 0.125}],
             functions=[{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                         "formula": "minecraft:uniform_bonus_count", "parameters": {"bonusMultiplier": 2}}, DECAY])]}]}])
    block_table("cinder_wheat_crop", [
        {"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            item("cinder_wheat", functions=[count(1, 2)], conditions=[age_is("cinder_wheat_crop", 3)]),
            item("cinder_wheat_seeds")]}]},
        {"rolls": 1, "bonus_rolls": 0, "conditions": [age_is("cinder_wheat_crop", 3)], "entries": [item("cinder_wheat_seeds", functions=[
            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
             "formula": "minecraft:binomial_with_bonus_count", "parameters": {"extra": 3, "probability": 0.5714286}}])]},
    ])
    bonus = {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:uniform_bonus_count",
             "parameters": {"bonusMultiplier": 1}}
    block_table("fire_berry_bush", [
        {"rolls": 1, "bonus_rolls": 0, "conditions": [age_is("fire_berry_bush", 3)],
         "entries": [item("fire_berries", functions=[count(2, 3), bonus])]},
        {"rolls": 1, "bonus_rolls": 0, "conditions": [age_is("fire_berry_bush", 2)],
         "entries": [item("fire_berries", functions=[count(1, 2), bonus])]},
    ])


LOOTING = {"function": "minecraft:looting_enchant", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}
KILLED_BY_PLAYER = {"condition": "minecraft:killed_by_player"}


def entity_table(name, pools):
    write_json(data_path("loot_tables", "entities", f"{name}.json"), {"type": "minecraft:entity", "pools": pools})


def generate_entity_loot():
    entity_table("ember_stalker", [
        {"rolls": 1, "entries": [item("stalker_meat", functions=[count(1, 3), LOOTING])]},
        {"rolls": 1, "conditions": [KILLED_BY_PLAYER, {"condition": "minecraft:random_chance_with_looting", "chance": 0.12, "looting_multiplier": 0.04}],
         "entries": [item("blaze_pepper_seeds", functions=[count(1, 2)])]},
    ])
    entity_table("ash_wraith", [
        {"rolls": 1, "entries": [item("wraith_essence", functions=[count(0, 2), LOOTING])]},
        {"rolls": 1, "conditions": [KILLED_BY_PLAYER, {"condition": "minecraft:random_chance_with_looting", "chance": 0.08, "looting_multiplier": 0.03}],
         "entries": [item("onyx_shard")]},
    ])
    entity_table("onyx_sentinel", [
        {"rolls": 1, "entries": [item("sentinel_core")]},
        {"rolls": 1, "entries": [item("onyx", functions=[count(4, 8), LOOTING])]},
        {"rolls": 1, "entries": [item("onyx_shard", functions=[count(6, 12)])]},
    ])
    entity_table("magma_crab", [
        {"rolls": 1, "entries": [item("crab_leg", functions=[count(1, 2), LOOTING])]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance_with_looting", "chance": 0.35, "looting_multiplier": 0.1}],
         "entries": [item("magma_shell")]},
    ])


def generate_entity_loot_v11():
    entity_table("soul_moth", [{"rolls": 1, "entries": [item("soul_dust", functions=[count(1, 2), LOOTING])]}])
    entity_table("withered_knight", [
        {"rolls": 1, "entries": [item("withered_bone", functions=[count(0, 2), LOOTING])]},
        {"rolls": 1, "entries": [item("minecraft:coal", functions=[count(0, 1), LOOTING])]},
        {"rolls": 1, "conditions": [KILLED_BY_PLAYER, {"condition": "minecraft:random_chance_with_looting", "chance": 0.04, "looting_multiplier": 0.02}],
         "entries": [item("withered_blade")]},
        {"rolls": 1, "conditions": [KILLED_BY_PLAYER, {"condition": "minecraft:random_chance_with_looting", "chance": 0.03, "looting_multiplier": 0.01}],
         "entries": [item("minecraft:wither_skeleton_skull")]},
    ])
    entity_table("basalt_beetle", [
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance_with_looting", "chance": 0.5, "looting_multiplier": 0.15}],
         "entries": [item("basalt_carapace")]},
        {"rolls": 1, "entries": [item("minecraft:magma_cream", functions=[count(0, 1), LOOTING])]},
    ])
    entity_table("ember_cow", [
        {"rolls": 1, "entries": [item("infernal_beef", functions=[count(1, 3), LOOTING])]},
        {"rolls": 1, "entries": [item("minecraft:leather", functions=[count(0, 2), LOOTING])]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance_with_looting", "chance": 0.25, "looting_multiplier": 0.1}],
         "entries": [item("minecraft:magma_cream")]},
    ])
    entity_table("cinder_hen", [
        {"rolls": 1, "entries": [item("infernal_chicken", functions=[LOOTING])]},
        {"rolls": 1, "entries": [item("minecraft:feather", functions=[count(0, 2), LOOTING])]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": 0.3}], "entries": [item("ember_egg")]},
    ])
    entity_table("fire_ghast", [
        {"rolls": 1, "entries": [item("minecraft:ghast_tear", functions=[count(0, 1), LOOTING])]},
        {"rolls": 1, "entries": [item("minecraft:fire_charge", functions=[count(1, 3), LOOTING])]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance_with_looting", "chance": 0.3, "looting_multiplier": 0.1}],
         "entries": [item("onyx_shard", functions=[count(1, 2)])]},
    ])
    entity_table("onyx_cube", [
        {"rolls": 1, "entries": [item("minecraft:magma_cream", functions=[count(0, 1), LOOTING])]},
        {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance_with_looting", "chance": 0.4, "looting_multiplier": 0.1}],
         "entries": [item("onyx_shard")]},
    ])
    entity_table("withered_champion", [
        {"rolls": 1, "entries": [item("withered_blade")]},
        {"rolls": 1, "entries": [item("minecraft:wither_skeleton_skull")]},
        {"rolls": 1, "entries": [item("onyx", functions=[count(3, 6)])]},
        {"rolls": 1, "entries": [item("withered_bone", functions=[count(4, 8)])]},
    ])
    entity_table("infernal_warlock", [
        {"rolls": 1, "entries": [item("warlock_staff")]},
        {"rolls": 1, "entries": [item("recall_stone")]},
        {"rolls": 1, "entries": [item("onyx", functions=[count(2, 5)])]},
        {"rolls": 1, "entries": [item("minecraft:blaze_rod", functions=[count(2, 4)])]},
    ])


def enchanted(name, lo, hi, weight, treasure=False):  # noqa: D103
    return item(name, weight=weight, functions=[{"function": "minecraft:enchant_with_levels", "levels": {
        "type": "minecraft:uniform", "min": lo, "max": hi}, "treasure": treasure}])


def book(weight):
    return item("minecraft:book", weight=weight, functions=[{"function": "minecraft:enchant_randomly"}])


def chest_table(name, pools):
    write_json(data_path("loot_tables", "chests", f"{name}.json"), {"type": "minecraft:chest", "pools": pools})


def pool(rolls_min, rolls_max, entries):
    return {"rolls": {"type": "minecraft:uniform", "min": rolls_min, "max": rolls_max}, "bonus_rolls": 0, "entries": entries}


def generate_chest_loot():
    chest_table("onyx_citadel", [
        pool(1, 2, [
            item("soul_scythe", weight=3),
            enchanted("onyx_sword", 20, 39, 5),
            enchanted("onyx_pickaxe", 20, 39, 5),
            enchanted("onyx_chestplate", 20, 39, 4),
            item("magma_treads", weight=4),
            item("minecraft:netherite_upgrade_smithing_template", weight=4),
            item("minecraft:enchanted_golden_apple", weight=1),
        ]),
        pool(4, 7, [
            item("onyx", weight=10, functions=[count(2, 5)]),
            item("onyx_shard", weight=14, functions=[count(3, 8)]),
            item("minecraft:ancient_debris", weight=5, functions=[count(1, 2)]),
            item("minecraft:netherite_scrap", weight=3),
            item("minecraft:diamond", weight=8, functions=[count(1, 3)]),
            item("minecraft:gold_block", weight=6),
            item("minecraft:golden_apple", weight=8),
            item("soul_compass", weight=4),
            book(8),
        ]),
    ])


# ------------------------------------------------------------------------------------------------
# Recettes
# ------------------------------------------------------------------------------------------------

def generate_chest_loot_v11():
    chest_table("infernal_sanctum", [
        pool(1, 2, [
            item("warlock_staff", weight=3),
            item("recall_stone", weight=5),
            item("magma_treads", weight=4),
            item("minecraft:netherite_upgrade_smithing_template", weight=3),
            item("minecraft:enchanted_golden_apple", weight=1),
        ]),
        pool(4, 7, [
            item("onyx", weight=10, functions=[count(1, 4)]),
            item("minecraft:blaze_rod", weight=12, functions=[count(2, 6)]),
            item("minecraft:magma_cream", weight=12, functions=[count(2, 6)]),
            item("basalt_carapace", weight=8, functions=[count(1, 2)]),
            item("minecraft:gold_block", weight=5),
            item("minecraft:ancient_debris", weight=3),
            item("glimmer_elixir", weight=8),
            book(6),
        ]),
    ])


def vault(name, uniques, extras):
    """Salle au tresor d'un boss : 3 coffres, chacun garantissant de gros lots."""
    chest_table(name, [
        pool(1, 1, uniques),
        pool(2, 3, [
            enchanted("onyx_sword", 30, 39, 6, True),
            enchanted("onyx_pickaxe", 30, 39, 6, True),
            enchanted("onyx_chestplate", 30, 39, 5, True),
            enchanted("onyx_helmet", 30, 39, 5, True),
            enchanted("onyx_leggings", 30, 39, 5, True),
            enchanted("onyx_boots", 30, 39, 5, True),
            enchanted("minecraft:book", 30, 39, 8, True),
        ]),
        pool(5, 8, [
            item("onyx", weight=14, functions=[count(4, 10)]),
            item("onyx_block", weight=4, functions=[count(1, 2)]),
            item("minecraft:netherite_scrap", weight=8, functions=[count(2, 4)]),
            item("minecraft:netherite_ingot", weight=3),
            item("minecraft:ancient_debris", weight=6, functions=[count(2, 4)]),
            item("minecraft:diamond", weight=10, functions=[count(3, 8)]),
            item("minecraft:emerald", weight=6, functions=[count(4, 12)]),
            item("minecraft:gold_block", weight=8, functions=[count(1, 3)]),
            item("minecraft:golden_apple", weight=8, functions=[count(1, 3)]),
            item("minecraft:experience_bottle", weight=8, functions=[count(6, 16)]),
        ] + extras),
    ])


def generate_dungeon_loot():
    common_unique = [item("minecraft:enchanted_golden_apple", weight=4), item("minecraft:netherite_upgrade_smithing_template", weight=6),
                     item("minecraft:totem_of_undying", weight=3)]
    vault("onyx_citadel_vault", [item("soul_scythe", weight=6), item("onyx_hammer", weight=5),
                                 item("minecraft:netherite_ingot", weight=4, functions=[count(1, 2)])] + common_unique,
          [item("sentinel_core", weight=2), item("magma_treads", weight=3)])
    vault("infernal_sanctum_vault", [item("warlock_staff", weight=6), item("magma_treads", weight=6),
                                     item("minecraft:snout_armor_trim_smithing_template", weight=3)] + common_unique,
          [item("recall_stone", weight=4), item("glimmer_elixir", weight=6, functions=[count(2, 4)]),
           item("minecraft:blaze_rod", weight=8, functions=[count(4, 10)])])
    vault("withered_catacombs_vault", [item("withered_blade", weight=6), item("soul_scythe", weight=4),
                                       item("minecraft:music_disc_pigstep", weight=3),
                                       item("minecraft:wither_skeleton_skull", weight=4, functions=[count(1, 3)])] + common_unique,
          [item("soul_compass", weight=4), item("recall_stone", weight=3), item("soul_dust", weight=8, functions=[count(4, 10)])])
    chest_table("withered_catacombs", [
        pool(4, 7, [
            item("withered_bone", weight=16, functions=[count(2, 6)]),
            item("soul_dust", weight=12, functions=[count(2, 5)]),
            item("onyx_shard", weight=12, functions=[count(2, 6)]),
            item("onyx", weight=4, functions=[count(1, 2)]),
            item("minecraft:gold_ingot", weight=10, functions=[count(2, 6)]),
            item("minecraft:iron_ingot", weight=10, functions=[count(2, 6)]),
            item("glimmer_elixir", weight=6),
            item("wraith_essence", weight=8, functions=[count(1, 3)]),
            enchanted("minecraft:iron_sword", 15, 30, 4),
            book(6),
        ]),
    ])


def generate_small_structure_loot():
    """Petites structures : des recompenses sympathiques sans boss (onyx, outils, diamants, nourriture, pommes d'or)."""
    chest_table("small_treasure", [
        pool(1, 2, [
            enchanted("onyx_pickaxe", 5, 20, 3),
            enchanted("onyx_sword", 5, 20, 3),
            enchanted("minecraft:diamond_pickaxe", 10, 25, 4),
            enchanted("minecraft:iron_axe", 5, 20, 6),
            item("onyx_bow", weight=2),
            item("minecraft:golden_apple", weight=6, functions=[count(1, 2)]),
            item("soul_compass", weight=3),
            item("recall_stone", weight=2),
        ]),
        pool(4, 7, [
            item("onyx", weight=8, functions=[count(1, 3)]),
            item("onyx_shard", weight=12, functions=[count(2, 6)]),
            item("minecraft:diamond", weight=8, functions=[count(1, 3)]),
            item("minecraft:gold_ingot", weight=12, functions=[count(2, 7)]),
            item("minecraft:iron_ingot", weight=10, functions=[count(2, 7)]),
            item("cooked_infernal_beef", weight=10, functions=[count(2, 6)]),
            item("cinder_bread", weight=10, functions=[count(2, 5)]),
            item("glowcap_stew", weight=4),
            item("glimmer_elixir", weight=5),
            item("minecraft:arrow", weight=8, functions=[count(6, 16)]),
            book(5),
        ]),
    ])
    chest_table("nether_garden", [
        pool(5, 9, [
            item("cinder_wheat_seeds", weight=14, functions=[count(3, 8)]),
            item("blaze_pepper_seeds", weight=10, functions=[count(2, 5)]),
            item("soul_tuber", weight=10, functions=[count(2, 5)]),
            item("fire_berries", weight=10, functions=[count(3, 8)]),
            item("cinder_bread", weight=12, functions=[count(2, 6)]),
            item("cooked_infernal_chicken", weight=10, functions=[count(2, 5)]),
            item("fried_ember_egg", weight=8, functions=[count(2, 5)]),
            item("minecraft:golden_carrot", weight=6, functions=[count(2, 6)]),
            item("minecraft:golden_apple", weight=4),
            item("minecraft:bone_meal", weight=10, functions=[count(4, 12)]),
            item("onyx_shard", weight=6, functions=[count(1, 4)]),
            item("onyx_hoe", weight=2),
        ]),
    ])


def recipe(name, data):
    write_json(data_path("recipes", f"{name}.json"), data)


def key_of(value):
    if value.startswith("#"):
        return {"tag": value[1:]}
    return {"item": mid(value)}


def shaped(name, pattern, keys, result, result_count=1, category="misc"):
    recipe(name, {"type": "minecraft:crafting_shaped", "category": category, "pattern": pattern,
                  "key": {k: key_of(val) for k, val in keys.items()},
                  "result": {"item": mid(result), "count": result_count}})


def shapeless(name, ingredients, result, result_count=1, category="misc"):
    recipe(name, {"type": "minecraft:crafting_shapeless", "category": category,
                  "ingredients": [key_of(i) for i in ingredients],
                  "result": {"item": mid(result), "count": result_count}})


def cooking(name, kind, ingredient, result, xp, time, category="misc"):
    recipe(name, {"type": f"minecraft:{kind}", "category": category, "ingredient": key_of(ingredient),
                  "result": mid(result), "experience": xp, "cookingtime": time})


def stonecutting(name, ingredient, result, result_count=1):
    recipe(name, {"type": "minecraft:stonecutting", "ingredient": key_of(ingredient), "result": mid(result),
                  "count": result_count})


def food_cooking(base, raw, cooked, xp=0.35):
    cooking(f"{base}_from_smelting", "smelting", raw, cooked, xp, 200, "food")
    cooking(f"{base}_from_smoking", "smoking", raw, cooked, xp, 100, "food")
    cooking(f"{base}_from_campfire_cooking", "campfire_cooking", raw, cooked, xp, 600, "food")


def generate_recipes():
    b = "building"
    # Onyx
    shaped("onyx_block", ["###", "###", "###"], {"#": "onyx"}, "onyx_block", category=b)
    shapeless("onyx_from_onyx_block", ["onyx_block"], "onyx", 9)
    shaped("onyx_from_shards", ["###", "###", "###"], {"#": "onyx_shard"}, "onyx")
    shaped("polished_onyx", [" B ", "BSB", " B "], {"B": "minecraft:polished_blackstone", "S": "onyx_shard"}, "polished_onyx", 4, b)
    shaped("onyx_bricks", ["##", "##"], {"#": "polished_onyx"}, "onyx_bricks", 4, b)
    shaped("onyx_brick_stairs", ["#  ", "## ", "###"], {"#": "onyx_bricks"}, "onyx_brick_stairs", 4, b)
    shaped("onyx_brick_slab", ["###"], {"#": "onyx_bricks"}, "onyx_brick_slab", 6, b)
    shaped("onyx_brick_wall", ["###", "###"], {"#": "onyx_bricks"}, "onyx_brick_wall", 6, "misc")
    for src in ("polished_onyx", "onyx_bricks"):
        stonecutting(f"onyx_brick_stairs_from_{src}_stonecutting", src, "onyx_brick_stairs")
        stonecutting(f"onyx_brick_slab_from_{src}_stonecutting", src, "onyx_brick_slab", 2)
        stonecutting(f"onyx_brick_wall_from_{src}_stonecutting", src, "onyx_brick_wall")
    stonecutting("onyx_bricks_from_polished_onyx_stonecutting", "polished_onyx", "onyx_bricks")
    cooking("onyx_from_smelting", "smelting", "onyx_ore", "onyx", 1.0, 200)
    cooking("onyx_from_blasting", "blasting", "onyx_ore", "onyx", 1.0, 100)

    # Outils et armure (manches en baton de blaze : il faut avoir explore une forteresse)
    keys = {"O": "onyx", "R": "minecraft:blaze_rod"}
    e = "equipment"
    shaped("onyx_sword", ["O", "O", "R"], keys, "onyx_sword", category=e)
    shaped("onyx_pickaxe", ["OOO", " R ", " R "], keys, "onyx_pickaxe", category=e)
    shaped("onyx_axe", ["OO", "OR", " R"], keys, "onyx_axe", category=e)
    shaped("onyx_shovel", ["O", "R", "R"], keys, "onyx_shovel", category=e)
    shaped("onyx_hoe", ["OO", " R", " R"], keys, "onyx_hoe", category=e)
    shaped("onyx_hammer", ["OBO", " R ", " R "], {"O": "onyx", "B": "onyx_block", "R": "minecraft:blaze_rod"}, "onyx_hammer", category=e)
    shaped("onyx_helmet", ["OOO", "O O"], {"O": "onyx"}, "onyx_helmet", category=e)
    shaped("onyx_chestplate", ["O O", "OOO", "OOO"], {"O": "onyx"}, "onyx_chestplate", category=e)
    shaped("onyx_leggings", ["OOO", "O O", "O O"], {"O": "onyx"}, "onyx_leggings", category=e)
    shaped("onyx_boots", ["O O", "O O"], {"O": "onyx"}, "onyx_boots", category=e)
    shaped("magma_treads", ["M M", "M M"], {"M": "magma_shell"}, "magma_treads", category=e)

    # Objets utilitaires
    shaped("soul_compass", [" E ", "ECE", " E "], {"E": "wraith_essence", "C": "minecraft:compass"}, "soul_compass", category=e)
    shaped("infernal_heart", ["OMO", "MCM", "OMO"], {"O": "onyx", "M": "minecraft:magma_cream", "C": "sentinel_core"}, "infernal_heart", category=e)
    shapeless("sulfur_bomb", ["sulfur", "sulfur", "minecraft:gunpowder", "minecraft:string"], "sulfur_bomb", 2, e)
    shapeless("gunpowder_from_sulfur", ["sulfur", "#minecraft:coals"], "minecraft:gunpowder", 2)
    shapeless("fire_charge_from_sulfur", ["sulfur", "minecraft:blaze_powder", "#minecraft:coals"], "minecraft:fire_charge", 3)
    shaped("sulfur_block", ["###", "###", "###"], {"#": "sulfur"}, "sulfur_block", category=b)
    shapeless("sulfur_from_sulfur_block", ["sulfur_block"], "sulfur", 9)
    cooking("sulfur_from_smelting", "smelting", "sulfur_ore", "sulfur", 0.5, 200)
    cooking("sulfur_from_blasting", "blasting", "sulfur_ore", "sulfur", 0.5, 100)

    # Bois de braise
    shapeless("ember_planks", ["#netheryx:ember_stems"], "ember_planks", 4, b)
    shaped("ember_hyphae", ["##", "##"], {"#": "ember_stem"}, "ember_hyphae", 3, b)
    shaped("stripped_ember_hyphae", ["##", "##"], {"#": "stripped_ember_stem"}, "stripped_ember_hyphae", 3, b)
    shaped("ember_stairs", ["#  ", "## ", "###"], {"#": "ember_planks"}, "ember_stairs", 4, b)
    shaped("ember_slab", ["###"], {"#": "ember_planks"}, "ember_slab", 6, b)
    shaped("ember_fence", ["W#W", "W#W"], {"W": "ember_planks", "#": "minecraft:stick"}, "ember_fence", 3, "misc")
    shaped("ember_fence_gate", ["#W#", "#W#"], {"W": "ember_planks", "#": "minecraft:stick"}, "ember_fence_gate", 1, "redstone")
    shaped("ember_door", ["##", "##", "##"], {"#": "ember_planks"}, "ember_door", 3, "redstone")
    shaped("ember_trapdoor", ["###", "###"], {"#": "ember_planks"}, "ember_trapdoor", 2, "redstone")
    shapeless("ember_button", ["ember_planks"], "ember_button", 1, "redstone")
    shaped("ember_pressure_plate", ["##"], {"#": "ember_planks"}, "ember_pressure_plate", 1, "redstone")

    # Nourriture
    shapeless("blaze_pepper_seeds", ["blaze_pepper"], "blaze_pepper_seeds", 2)
    shapeless("infernal_stew", ["minecraft:bowl", "blaze_pepper", "baked_soul_tuber", "minecraft:crimson_fungus",
                                "minecraft:warped_fungus"], "infernal_stew", 1)
    food_cooking("baked_soul_tuber", "soul_tuber", "baked_soul_tuber")
    food_cooking("cooked_stalker_meat", "stalker_meat", "cooked_stalker_meat")
    food_cooking("cooked_crab_leg", "crab_leg", "cooked_crab_leg")

    # v1.4 : ateliers en onyx (moins chers que leurs equivalents vanilla en fer)
    shaped("onyx_anvil", ["OOO", " I ", "III"], {"O": "onyx", "I": "minecraft:iron_ingot"}, "onyx_anvil", category="misc")
    shaped("onyx_furnace", ["PPP", "PFP", "PPP"], {"P": "polished_onyx", "F": "minecraft:furnace"}, "onyx_furnace", category="misc")

    # v1.3
    shaped("onyx_bow", [" OS", "R S", " OS"], {"O": "onyx", "R": "minecraft:blaze_rod", "S": "minecraft:string"}, "onyx_bow", category=e)
    shaped("cinder_bread", ["###"], {"#": "cinder_wheat"}, "cinder_bread", 1, "food")
    shapeless("glowcap_stew", ["minecraft:bowl", "glowcap", "glowcap", "soul_tuber"], "glowcap_stew", 1)
    shapeless("orange_dye_from_ember_bloom", ["ember_bloom"], "minecraft:orange_dye", 2)
    shapeless("light_gray_dye_from_ash_daisy", ["ash_daisy"], "minecraft:light_gray_dye", 2)
    shapeless("red_dye_from_crimson_bellflower", ["crimson_bellflower"], "minecraft:red_dye", 2)
    shapeless("yellow_dye_from_sulfur_tulip", ["sulfur_tulip"], "minecraft:yellow_dye", 2)
    shapeless("cinder_wheat_seeds", ["cinder_wheat"], "cinder_wheat_seeds", 2)
    food_cooking("cooked_infernal_beef", "infernal_beef", "cooked_infernal_beef")
    food_cooking("cooked_infernal_chicken", "infernal_chicken", "cooked_infernal_chicken")
    food_cooking("fried_ember_egg", "ember_egg", "fried_ember_egg")
    shaped("nether_grass_block", ["GG", "GG"], {"G": "nether_grass"}, "nether_grass_block", 1, b)

    # v1.1
    shapeless("withered_planks", ["#netheryx:withered_stems"], "withered_planks", 4, b)
    shaped("withered_hyphae", ["##", "##"], {"#": "withered_stem"}, "withered_hyphae", 3, b)
    shaped("stripped_withered_hyphae", ["##", "##"], {"#": "stripped_withered_stem"}, "stripped_withered_hyphae", 3, b)
    shaped("withered_stairs", ["#  ", "## ", "###"], {"#": "withered_planks"}, "withered_stairs", 4, b)
    shaped("withered_slab", ["###"], {"#": "withered_planks"}, "withered_slab", 6, b)
    shaped("withered_fence", ["W#W", "W#W"], {"W": "withered_planks", "#": "minecraft:stick"}, "withered_fence", 3, "misc")
    shaped("withered_fence_gate", ["#W#", "#W#"], {"W": "withered_planks", "#": "minecraft:stick"}, "withered_fence_gate", 1, "redstone")
    shaped("withered_door", ["##", "##", "##"], {"#": "withered_planks"}, "withered_door", 3, "redstone")
    shaped("withered_trapdoor", ["###", "###"], {"#": "withered_planks"}, "withered_trapdoor", 2, "redstone")
    shapeless("withered_button", ["withered_planks"], "withered_button", 1, "redstone")
    shaped("withered_pressure_plate", ["##"], {"#": "withered_planks"}, "withered_pressure_plate", 1, "redstone")
    shaped("magma_bricks", ["##", "##"], {"#": "minecraft:magma_block"}, "magma_bricks", 4, b)
    shaped("magma_brick_stairs", ["#  ", "## ", "###"], {"#": "magma_bricks"}, "magma_brick_stairs", 4, b)
    shaped("magma_brick_slab", ["###"], {"#": "magma_bricks"}, "magma_brick_slab", 6, b)
    shaped("magma_brick_wall", ["###", "###"], {"#": "magma_bricks"}, "magma_brick_wall", 6, "misc")
    stonecutting("magma_brick_stairs_from_magma_bricks_stonecutting", "magma_bricks", "magma_brick_stairs")
    stonecutting("magma_brick_slab_from_magma_bricks_stonecutting", "magma_bricks", "magma_brick_slab", 2)
    stonecutting("magma_brick_wall_from_magma_bricks_stonecutting", "magma_bricks", "magma_brick_wall")
    shaped("onyx_lantern", ["SSS", "STS", "SSS"], {"S": "onyx_shard", "T": "minecraft:soul_torch"}, "onyx_lantern", 1, "misc")
    shaped("ember_lamp", [" W ", "WGW", " W "], {"W": "ember_wart_block", "G": "minecraft:glowstone"}, "ember_lamp", 2, b)
    shaped("carapace_chestplate", ["C C", "CCC", "CCC"], {"C": "basalt_carapace"}, "carapace_chestplate", category=e)
    shaped("recall_stone", [" D ", "DOD", " D "], {"D": "soul_dust", "O": "minecraft:crying_obsidian"}, "recall_stone", category=e)
    shaped("obsidian_boat", ["O O", "OOO"], {"O": "minecraft:obsidian"}, "obsidian_boat", category="misc")
    shaped("obsidian_bucket", ["O O", " C "], {"O": "minecraft:obsidian", "C": "minecraft:crying_obsidian"}, "obsidian_bucket", category="misc")
    shapeless("glimmer_elixir", ["minecraft:glass_bottle", "soul_dust", "minecraft:glowstone_dust", "fire_berries"], "glimmer_elixir", 1)
    shapeless("bone_meal_from_withered_bone", ["withered_bone"], "minecraft:bone_meal", 3)
    shapeless("soul_dust_from_soul_lily", ["soul_lily"], "soul_dust", 1)
    shapeless("black_dye_from_withered_bone", ["withered_bone", "withered_bone"], "minecraft:black_dye", 1)


# ------------------------------------------------------------------------------------------------
# Tags
# ------------------------------------------------------------------------------------------------

_TAGS = {}


def tag(registry, namespace, path, values):
    """Ajoute des valeurs a un tag (plusieurs appels sur le meme tag sont fusionnes)."""
    key = (registry, namespace, path)
    merged = _TAGS.setdefault(key, [])
    merged.extend(v for v in values if v not in merged)
    write_json(data_path("tags", registry, f"{path}.json", namespace=namespace), {"replace": False, "values": merged})


def m(names):
    return [mid(n) for n in names]


ONYX_STONE = ["onyx_block", "polished_onyx", "onyx_bricks", "onyx_brick_stairs", "onyx_brick_slab", "onyx_brick_wall"]
EMBER_WOOD = ["ember_stem", "stripped_ember_stem", "ember_hyphae", "stripped_ember_hyphae", "ember_planks", "ember_stairs",
              "ember_slab", "ember_fence", "ember_fence_gate", "ember_door", "ember_trapdoor", "ember_button",
              "ember_pressure_plate"]
WITHERED_WOOD = ["withered_stem", "stripped_withered_stem", "withered_hyphae", "stripped_withered_hyphae", "withered_planks",
                 "withered_stairs", "withered_slab", "withered_fence", "withered_fence_gate", "withered_door",
                 "withered_trapdoor", "withered_button", "withered_pressure_plate"]
WITHERED_STEMS = ["withered_stem", "stripped_withered_stem", "withered_hyphae", "stripped_withered_hyphae"]
EMBER_STEMS = ["ember_stem", "stripped_ember_stem", "ember_hyphae", "stripped_ember_hyphae"]


def both(namespace, path, values):
    """Tag commun aux blocs et aux objets."""
    tag("blocks", namespace, path, values)
    tag("items", namespace, path, values)


def generate_tags():
    mc = "minecraft"
    tag("blocks", mc, "mineable/pickaxe", m(["onyx_ore", "onyx_cluster", "sulfur_rock", "sulfur_ore", "sulfur_block",
                                              "sulfur_cluster", "crusted_lava", "ember_nylium"] + ONYX_STONE))
    tag("blocks", mc, "mineable/axe", m(EMBER_WOOD))
    tag("blocks", mc, "mineable/shovel", m(["ash_block"]))
    tag("blocks", mc, "mineable/hoe", m(["ember_wart_block"]))
    tag("blocks", mc, "needs_diamond_tool", m(["onyx_ore", "onyx_block"]))
    tag("blocks", mc, "needs_iron_tool", m(["polished_onyx", "onyx_bricks", "onyx_brick_stairs", "onyx_brick_slab", "onyx_brick_wall"]))
    tag("blocks", mc, "needs_stone_tool", m(["sulfur_ore", "sulfur_block"]))
    tag("blocks", mc, "nylium", m(["ember_nylium"]))
    tag("blocks", mc, "wart_blocks", m(["ember_wart_block"]))
    tag("blocks", mc, "beacon_base_blocks", m(["onyx_block"]))
    tag("blocks", mc, "base_stone_nether", m(["sulfur_rock"]))
    tag("blocks", mc, "nether_carver_replaceables", m(["ash_block", "sulfur_rock"]))
    tag("blocks", mc, "crops", m(["blaze_pepper_crop", "soul_tuber_crop"]))
    tag("blocks", mc, "replaceable_by_trees", m(["charred_shrub", "ember_roots"]))
    tag("blocks", mc, "dragon_immune", m(["onyx_block", "guardian_seal"]))
    tag("blocks", mc, "wither_immune", m(["onyx_block", "guardian_seal"]))
    tag("blocks", mc, "crystal_sound_blocks", m(["onyx_cluster", "sulfur_cluster"]))

    both(mc, "planks", m(["ember_planks"]))
    both(mc, "logs", ["#netheryx:ember_stems"])
    both(MOD_ID, "ember_stems", m(EMBER_STEMS))
    both(mc, "wooden_stairs", m(["ember_stairs"]))
    both(mc, "wooden_slabs", m(["ember_slab"]))
    both(mc, "wooden_fences", m(["ember_fence"]))
    both(mc, "fence_gates", m(["ember_fence_gate"]))
    both(mc, "wooden_doors", m(["ember_door"]))
    both(mc, "wooden_trapdoors", m(["ember_trapdoor"]))
    both(mc, "wooden_buttons", m(["ember_button"]))
    both(mc, "wooden_pressure_plates", m(["ember_pressure_plate"]))
    both(mc, "stairs", m(["onyx_brick_stairs"]))
    both(mc, "slabs", m(["onyx_brick_slab"]))
    both(mc, "walls", m(["onyx_brick_wall"]))

    tag("items", mc, "non_flammable_wood", m(EMBER_WOOD))
    tag("items", mc, "beacon_payment_items", m(["onyx"]))
    tag("items", mc, "trimmable_armor", m(["onyx_helmet", "onyx_chestplate", "onyx_leggings", "onyx_boots", "magma_treads"]))
    tag("items", mc, "swords", m(["onyx_sword", "soul_scythe"]))
    tag("items", mc, "pickaxes", m(["onyx_pickaxe", "onyx_hammer"]))
    tag("items", mc, "axes", m(["onyx_axe"]))
    tag("items", mc, "shovels", m(["onyx_shovel"]))
    tag("items", mc, "hoes", m(["onyx_hoe"]))
    tag("items", mc, "piglin_loved", m(["onyx_block"]))
    tag("items", mc, "fox_food", m(["fire_berries"]))

    # Conventions Forge (partage des materiaux entre mods)
    fg = "forge"
    both(fg, "ores/onyx", m(["onyx_ore"]))
    both(fg, "ores/sulfur", m(["sulfur_ore"]))
    both(fg, "ores", ["#forge:ores/onyx", "#forge:ores/sulfur"])
    both(fg, "ores_in_ground/netherrack", m(["onyx_ore", "sulfur_ore"]))
    both(fg, "ore_rates/singular", m(["onyx_ore"]))
    both(fg, "ore_rates/dense", m(["sulfur_ore"]))
    both(fg, "storage_blocks/onyx", m(["onyx_block"]))
    both(fg, "storage_blocks/sulfur", m(["sulfur_block"]))
    both(fg, "storage_blocks", ["#forge:storage_blocks/onyx", "#forge:storage_blocks/sulfur"])
    tag("items", fg, "gems/onyx", m(["onyx"]))
    tag("items", fg, "gems", ["#forge:gems/onyx"])
    tag("items", fg, "dusts/sulfur", m(["sulfur"]))
    tag("items", fg, "dusts", ["#forge:dusts/sulfur"])
    tag("items", fg, "seeds/blaze_pepper", m(["blaze_pepper_seeds"]))
    tag("items", fg, "seeds", ["#forge:seeds/blaze_pepper"])
    tag("items", fg, "crops/blaze_pepper", m(["blaze_pepper"]))
    tag("items", fg, "crops/soul_tuber", m(["soul_tuber"]))
    tag("items", fg, "crops", ["#forge:crops/blaze_pepper", "#forge:crops/soul_tuber"])
    tag("items", fg, "armors/helmets", m(["onyx_helmet"]))
    tag("items", fg, "armors/chestplates", m(["onyx_chestplate"]))
    tag("items", fg, "armors/leggings", m(["onyx_leggings"]))
    tag("items", fg, "armors/boots", m(["onyx_boots", "magma_treads"]))
    tag("items", fg, "tools/swords", m(["onyx_sword", "soul_scythe"]))
    tag("items", fg, "tools/pickaxes", m(["onyx_pickaxe", "onyx_hammer"]))
    tag("items", fg, "tools/axes", m(["onyx_axe"]))
    tag("items", fg, "tools/shovels", m(["onyx_shovel"]))
    tag("items", fg, "tools/hoes", m(["onyx_hoe"]))
    tag("items", fg, "mushrooms", m(["ember_fungus"]))

    # v1.1
    tag("blocks", mc, "mineable/pickaxe", m(["magma_bricks", "magma_brick_stairs", "magma_brick_slab", "magma_brick_wall", "onyx_lantern"]))
    tag("blocks", mc, "mineable/axe", m(WITHERED_WOOD))
    tag("blocks", mc, "mineable/shovel", m(["withered_soil"]))
    tag("blocks", mc, "mineable/hoe", m(["withered_wart_block", "ember_lamp"]))
    tag("blocks", mc, "nylium", m(["withered_soil"]))
    tag("blocks", mc, "wart_blocks", m(["withered_wart_block"]))
    tag("blocks", mc, "soul_speed_blocks", m(["withered_soil"]))
    tag("blocks", mc, "soul_fire_base_blocks", m(["withered_soil"]))
    tag("blocks", mc, "nether_carver_replaceables", m(["withered_soil"]))
    tag("blocks", mc, "replaceable_by_trees", m(["soul_lily"]))
    both(MOD_ID, "withered_stems", m(WITHERED_STEMS))
    both(mc, "logs", ["#netheryx:withered_stems"])
    both(mc, "planks", m(["withered_planks"]))
    both(mc, "wooden_stairs", m(["withered_stairs"]))
    both(mc, "wooden_slabs", m(["withered_slab"]))
    both(mc, "wooden_fences", m(["withered_fence"]))
    both(mc, "fence_gates", m(["withered_fence_gate"]))
    both(mc, "wooden_doors", m(["withered_door"]))
    both(mc, "wooden_trapdoors", m(["withered_trapdoor"]))
    both(mc, "wooden_buttons", m(["withered_button"]))
    both(mc, "wooden_pressure_plates", m(["withered_pressure_plate"]))
    both(mc, "stairs", m(["magma_brick_stairs"]))
    both(mc, "slabs", m(["magma_brick_slab"]))
    both(mc, "walls", m(["magma_brick_wall"]))
    tag("items", mc, "non_flammable_wood", m(WITHERED_WOOD))
    tag("items", mc, "swords", m(["withered_blade"]))
    tag("items", mc, "trimmable_armor", m(["carapace_chestplate"]))
    tag("items", fg, "tools/swords", m(["withered_blade"]))
    tag("items", fg, "armors/chestplates", m(["carapace_chestplate"]))
    tag("items", fg, "bones", m(["withered_bone"]))
    tag("items", fg, "dusts", ["#forge:dusts/soul"])
    tag("items", fg, "dusts/soul", m(["soul_dust"]))
    tag("blocks", MOD_ID, "basalt_beetle_spawnable_on", ["minecraft:magma_block", "minecraft:blackstone", "minecraft:basalt",
                                                         "minecraft:netherrack"])
    tag("worldgen/biome", mc, "is_nether", m(["soulfire_marsh", "withered_forest", "magma_rift"]))
    tag("worldgen/biome", fg, "is_hot/nether", m(["soulfire_marsh", "withered_forest", "magma_rift"]))
    tag("worldgen/biome", fg, "is_hot", m(["soulfire_marsh", "withered_forest", "magma_rift"]))
    tag("worldgen/biome", fg, "is_dry/nether", m(["withered_forest", "magma_rift"]))
    tag("worldgen/biome", fg, "is_spooky", m(["soulfire_marsh", "withered_forest"]))
    tag("worldgen/biome", mc, "has_structure/nether_fossil", m(["soulfire_marsh"]))
    tag("worldgen/biome", MOD_ID, "has_structure/infernal_sanctum", m(["magma_rift", "ember_grove", "sulfur_springs"])
        + ["minecraft:crimson_forest", "minecraft:nether_wastes"])
    tag("worldgen/biome", MOD_ID, "has_structure/withered_catacombs", m(["withered_forest", "ashen_wastes", "soulfire_marsh"])
        + ["minecraft:soul_sand_valley", "minecraft:warped_forest", "minecraft:nether_wastes"])

    # v1.4
    tag("blocks", mc, "mineable/pickaxe", m(["onyx_anvil", "onyx_furnace"]))
    tag("blocks", mc, "needs_iron_tool", m(["onyx_anvil"]))

    # v1.3
    tag("blocks", mc, "nylium", m(["nether_grass_block"]))
    tag("blocks", mc, "mineable/shovel", m(["nether_grass_block"]))
    tag("blocks", mc, "crops", m(["cinder_wheat_crop"]))
    tag("blocks", mc, "flower_pots", m([b[0] for b in BLOCKS if b[1] == "pot"]))
    tag("blocks", mc, "replaceable_by_trees", m(["nether_grass"]))
    tag("blocks", mc, "nether_carver_replaceables", m(["nether_grass_block"]))
    tag("items", fg, "seeds/cinder_wheat", m(["cinder_wheat_seeds"]))
    tag("items", fg, "seeds", ["#forge:seeds/cinder_wheat"])
    tag("items", fg, "crops/cinder_wheat", m(["cinder_wheat"]))
    tag("items", fg, "crops", ["#forge:crops/cinder_wheat"])
    tag("items", fg, "tools/bows", m(["onyx_bow"]))
    tag("items", fg, "eggs", m(["ember_egg"]))
    tag("items", fg, "mushrooms", m(["glowcap"]))
    tag("items", MOD_ID, "ember_cow_food", m(["cinder_wheat"]))
    tag("items", MOD_ID, "cinder_hen_food", m(["cinder_wheat_seeds", "blaze_pepper_seeds"]))
    tag("blocks", MOD_ID, "cinder_wheat_soil", ["#minecraft:nylium", "minecraft:soul_soil", "minecraft:farmland"])
    tag("blocks", MOD_ID, "nether_animals_spawnable_on", ["#minecraft:nylium", "#minecraft:base_stone_nether", "minecraft:soul_soil",
                                                          "minecraft:soul_sand", "minecraft:magma_block", mid("ash_block"),
                                                          "minecraft:gravel"])
    tag("blocks", MOD_ID, "magma_crab_spawnable_on", ["#netheryx:nether_animals_spawnable_on"])
    tag("blocks", MOD_ID, "basalt_beetle_spawnable_on", ["#netheryx:nether_animals_spawnable_on"])
    tag("blocks", MOD_ID, "sulfur_fertilizable", m(["cinder_wheat_crop", "nether_grass_block"]))
    tag("worldgen/biome", mc, "is_nether", m(["ember_meadow"]))
    tag("worldgen/biome", fg, "is_hot/nether", m(["ember_meadow"]))
    tag("worldgen/biome", fg, "is_hot", m(["ember_meadow"]))
    tag("worldgen/biome", fg, "is_lush", m(["ember_meadow"]))
    tag("worldgen/biome", mc, "has_structure/bastion_remnant", m(["ember_meadow"]))
    tag("worldgen/biome", MOD_ID, "has_structure/infernal_sanctum", m(["ember_meadow"]))
    tag("worldgen/biome", MOD_ID, "has_structure/traveler_camp", ["#minecraft:is_nether"])
    tag("worldgen/biome", MOD_ID, "has_structure/nether_garden", m(["ember_meadow", "ember_grove", "sulfur_springs"])
        + ["minecraft:crimson_forest", "minecraft:nether_wastes", "minecraft:warped_forest"])
    tag("worldgen/biome", MOD_ID, "has_structure/hidden_cache", ["#minecraft:is_nether"])

    # Tags propres au mod (modifiables par les packs de donnees)
    tag("blocks", MOD_ID, "needs_onyx_tool", [])
    tag("blocks", MOD_ID, "nether_plant_soil", ["#minecraft:nylium", "minecraft:netherrack", "minecraft:soul_soil",
                                                "minecraft:soul_sand", "minecraft:blackstone", "minecraft:basalt",
                                                "minecraft:smooth_basalt", mid("ash_block"), mid("sulfur_rock"),
                                                "#minecraft:dirt"])
    tag("blocks", MOD_ID, "blaze_pepper_soil", [mid("ember_nylium"), "minecraft:soul_soil", "minecraft:farmland"])
    tag("blocks", MOD_ID, "soul_tuber_soil", ["minecraft:soul_sand", "minecraft:soul_soil"])
    tag("blocks", MOD_ID, "magma_crab_spawnable_on", [mid("sulfur_rock"), "minecraft:netherrack", "minecraft:basalt",
                                                      "minecraft:magma_block", "minecraft:blackstone"])
    tag("blocks", MOD_ID, "sulfur_fertilizable", m(["blaze_pepper_crop", "soul_tuber_crop", "fire_berry_bush", "ember_fungus",
                                                    "ember_nylium"]) + ["minecraft:nether_wart", "minecraft:crimson_fungus",
                                                                        "minecraft:warped_fungus", "minecraft:crimson_nylium",
                                                                        "minecraft:warped_nylium"])
    tag("items", MOD_ID, "magma_crab_food", m(["fire_berries"]))

    # Monde
    biomes = m(["ember_grove", "ashen_wastes", "onyx_depths", "sulfur_springs"])
    tag("worldgen/biome", mc, "is_nether", biomes)
    tag("worldgen/biome", mc, "has_structure/bastion_remnant", m(["ember_grove", "ashen_wastes", "sulfur_springs"]))
    tag("worldgen/biome", fg, "is_hot/nether", biomes)
    tag("worldgen/biome", fg, "is_hot", biomes)
    tag("worldgen/biome", fg, "is_dry/nether", biomes)
    tag("worldgen/biome", fg, "is_dry", biomes)
    # Chaque donjon peut apparaitre dans plusieurs biomes (y compris vanilla) pour ne jamais etre trop loin du joueur
    tag("worldgen/biome", MOD_ID, "has_structure/onyx_citadel", m(["onyx_depths", "withered_forest", "ashen_wastes"])
        + ["minecraft:basalt_deltas", "minecraft:nether_wastes", "minecraft:soul_sand_valley"])
    tag("worldgen/structure", MOD_ID, "soul_compass_targets", m(["onyx_citadel", "infernal_sanctum", "withered_catacombs"]) + [
        "minecraft:bastion_remnant", "minecraft:fortress"])


# ------------------------------------------------------------------------------------------------
# Progres et modificateurs de butin
# ------------------------------------------------------------------------------------------------

def advancement(name, icon, frame, parent, criteria, background=None, hidden=False):
    display = {"icon": {"item": mid(icon)}, "title": {"translate": f"advancements.{MOD_ID}.{name}.title"},
               "description": {"translate": f"advancements.{MOD_ID}.{name}.description"}, "frame": frame,
               "show_toast": True, "announce_to_chat": True, "hidden": hidden}
    if background:
        display["background"] = background
    data = {"display": display, "criteria": criteria}
    if parent:
        data["parent"] = mid(parent)
    write_json(data_path("advancements", f"{name}.json"), data)


def has_items(*items):
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": m(items)}]}}


def generate_advancements():
    advancement("root", "onyx", "task", None, {"entered_nether": {"trigger": "minecraft:changed_dimension",
                                                                  "conditions": {"to": "minecraft:the_nether"}}},
                background="minecraft:textures/block/blackstone.png")
    advancement("obtain_onyx", "onyx", "task", "root", {"onyx": has_items("onyx")})
    advancement("ember_harvest", "blaze_pepper", "task", "root", {"eat": {"trigger": "minecraft:consume_item",
                                                                          "conditions": {"item": {"items": [mid("blaze_pepper")]}}}})
    advancement("soul_compass", "soul_compass", "task", "root", {"compass": has_items("soul_compass")})
    advancement("onyx_armor", "onyx_chestplate", "goal", "obtain_onyx",
                {"armor": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [
                    {"items": [mid("onyx_helmet")]}, {"items": [mid("onyx_chestplate")]},
                    {"items": [mid("onyx_leggings")]}, {"items": [mid("onyx_boots")]}]}}})
    advancement("kill_sentinel", "sentinel_core", "challenge", "obtain_onyx",
                {"kill": {"trigger": "minecraft:player_killed_entity", "conditions": {"entity": [
                    {"condition": "minecraft:entity_properties", "entity": "this",
                     "predicate": {"type": mid("onyx_sentinel")}}]}}})
    advancement("infernal_heart", "infernal_heart", "challenge", "kill_sentinel", {"heart": has_items("infernal_heart")})
    advancement("kill_warlock", "warlock_staff", "challenge", "root",
                {"kill": {"trigger": "minecraft:player_killed_entity", "conditions": {"entity": [
                    {"condition": "minecraft:entity_properties", "entity": "this",
                     "predicate": {"type": mid("infernal_warlock")}}]}}})
    advancement("recall_stone", "recall_stone", "task", "root", {"stone": has_items("recall_stone")})
    advancement("obsidian_boat", "obsidian_boat", "task", "root", {"boat": has_items("obsidian_boat")})
    advancement("kill_champion", "withered_blade", "challenge", "root",
                {"kill": {"trigger": "minecraft:player_killed_entity", "conditions": {"entity": [
                    {"condition": "minecraft:entity_properties", "entity": "this",
                     "predicate": {"type": mid("withered_champion")}}]}}})


def loot_modifier(name, table, item_name, chance, lo=1, hi=1):
    write_json(data_path("loot_modifiers", f"{name}.json"), {
        "type": mid("add_item"),
        "conditions": [{"condition": "forge:loot_table_id", "loot_table_id": table},
                       {"condition": "minecraft:random_chance", "chance": chance}],
        "item": mid(item_name), "min_count": lo, "max_count": hi})
    return mid(name)


def generate_loot_modifiers():
    entries = [
        loot_modifier("bastion_treasure_onyx", "minecraft:chests/bastion_treasure", "onyx", 0.6, 2, 4),
        loot_modifier("bastion_other_onyx_shard", "minecraft:chests/bastion_other", "onyx_shard", 0.5, 2, 5),
        loot_modifier("bastion_bridge_sulfur_bomb", "minecraft:chests/bastion_bridge", "sulfur_bomb", 0.4, 2, 4),
        loot_modifier("hoglin_stable_magma_shell", "minecraft:chests/bastion_hoglin_stable", "magma_shell", 0.4, 1, 2),
        loot_modifier("fortress_soul_compass", "minecraft:chests/nether_bridge", "soul_compass", 0.15),
        loot_modifier("fortress_blaze_pepper_seeds", "minecraft:chests/nether_bridge", "blaze_pepper_seeds", 0.4, 1, 3),
        loot_modifier("ruined_portal_onyx_shard", "minecraft:chests/ruined_portal", "onyx_shard", 0.25, 1, 3),
        loot_modifier("fortress_withered_bone", "minecraft:chests/nether_bridge", "withered_bone", 0.35, 1, 4),
        loot_modifier("bastion_treasure_recall_stone", "minecraft:chests/bastion_treasure", "recall_stone", 0.2),
        loot_modifier("bastion_other_glimmer_elixir", "minecraft:chests/bastion_other", "glimmer_elixir", 0.3, 1, 2),
        loot_modifier("bartering_fire_berries", "minecraft:gameplay/piglin_bartering", "fire_berries", 0.04, 2, 4),
    ]
    write_json(data_path("loot_modifiers", "global_loot_modifiers.json", namespace="forge"),
               {"replace": False, "entries": entries})


def generate():
    generate_block_loot()
    generate_entity_loot()
    generate_entity_loot_v11()
    generate_chest_loot()
    generate_chest_loot_v11()
    generate_dungeon_loot()
    generate_small_structure_loot()
    generate_recipes()
    generate_tags()
    generate_advancements()
    generate_loot_modifiers()
