"""Donnees 1.2.0 (suite) : golem de basalte, wyverne, lucioles, cochon de blaze, escargot de cristal,
Lac de verre en fusion, Foret petrifiee d'onyx et Lanterne du Nether."""
import json

from .common import MOD_ID, data_path, mid, write_json
from .data import (DECAY, FORTUNE_ORE, KILLED_BY_PLAYER, LOOTING, SILK_TOUCH, advancement, block_table, both, count,
                   entity_table, food_cooking, has_items, item, m, shaped, shapeless, tag)
from .data_v120 import SHEARS_OR_SILK, silk_or

SMELT_IF_ON_FIRE = {"function": "minecraft:furnace_smelt", "conditions": [
    {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"flags": {"is_on_fire": True}}}]}


def generate_loot():
    silk_or("molten_crystal", "molten_shard", [count(2, 4), FORTUNE_ORE, DECAY])
    block_table("onyx_crystal_leaves", [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        item("onyx_crystal_leaves", conditions=[SHEARS_OR_SILK]),
        item("onyx_shard", conditions=[{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                                        "chances": [0.08, 0.12, 0.18, 0.25]}])]}]}])
    entity_table("basalt_golem", [
        {"rolls": 1, "entries": [item("basalt_carapace", functions=[count(2, 4), LOOTING])]},
        {"rolls": 1, "entries": [item("molten_shard", functions=[count(2, 5), LOOTING])]},
        {"rolls": 1, "entries": [item("magma_shell", functions=[count(1, 2)])]},
        {"rolls": 1, "entries": [item("onyx", functions=[count(1, 3)])], "conditions": [KILLED_BY_PLAYER]},
        {"rolls": 1, "entries": [item("minecraft:book", functions=[{"function": "minecraft:enchant_randomly"}])],
         "conditions": [KILLED_BY_PLAYER, {"condition": "minecraft:random_chance", "chance": 0.3}]},
    ])
    entity_table("blaze_pig", [
        {"rolls": 1, "entries": [item("blaze_pork", functions=[count(1, 3), SMELT_IF_ON_FIRE, LOOTING])]},
        {"rolls": 1, "entries": [item("minecraft:blaze_powder", functions=[count(0, 1), LOOTING])]},
    ])
    entity_table("crystal_snail", [{"rolls": 1, "entries": [item("onyx_shard", functions=[count(0, 2), LOOTING])]}])
    entity_table("ember_firefly", [{"rolls": 1, "entries": [item("minecraft:glowstone_dust", functions=[count(0, 1)])]}])
    entity_table("soul_firefly", [{"rolls": 1, "entries": [item("soul_dust", functions=[count(0, 1)])]}])
    entity_table("ash_wyvern", [{"rolls": 1, "entries": [item("minecraft:phantom_membrane", functions=[count(1, 3)])]}])


def generate_recipes():
    food_cooking("cooked_blaze_pork", "blaze_pork", "cooked_blaze_pork")
    shaped("nether_lantern", [" O ", "MLM", " M "], {"O": "onyx", "M": "molten_shard", "L": "minecraft:soul_lantern"},
           "nether_lantern", 1, "equipment")
    shaped("molten_glass", ["MM", "MM"], {"M": "molten_shard"}, "molten_glass", 1, "building")


def generate_tags():
    mc, fg = "minecraft", "forge"
    tag("blocks", mc, "mineable/pickaxe", m(["molten_glass", "molten_crystal", "petrified_log"]))
    tag("blocks", mc, "mineable/hoe", m(["onyx_crystal_leaves"]))
    tag("blocks", mc, "needs_stone_tool", m(["petrified_log"]))
    tag("blocks", mc, "infiniburn_nether", m(["molten_glass"]))
    tag("blocks", mc, "crystal_sound_blocks", m(["molten_crystal", "onyx_crystal_leaves"]))
    tag("blocks", MOD_ID, "lantern_reveals", ["minecraft:ancient_debris", "minecraft:gilded_blackstone",
                                             "minecraft:nether_gold_ore", "minecraft:nether_quartz_ore"]
        + m(["onyx_ore", "sulfur_ore", "rich_quartz_ore"]))
    tag("items", MOD_ID, "blaze_pig_food", m(["blaze_pepper", "fire_berries"]))
    tag("items", MOD_ID, "crystal_snail_food", m(["glowcap"]))
    tag("items", MOD_ID, "ash_wyvern_food", m(["stalker_meat", "cooked_stalker_meat", "infernal_beef", "cooked_infernal_beef",
                                              "blaze_pork", "cooked_blaze_pork", "magmafish", "grilled_magmafish"])
        + ["minecraft:porkchop", "minecraft:cooked_porkchop", "minecraft:beef", "minecraft:cooked_beef"])
    tag("items", fg, "raw_meats", m(["blaze_pork"]))
    tag("items", fg, "cooked_meats", m(["cooked_blaze_pork"]))
    both(mc, "logs", m(["petrified_log"]))
    biomes = m(["molten_glass_lake", "petrified_onyx_forest"])
    tag("worldgen/biome", mc, "is_nether", biomes)
    tag("worldgen/biome", fg, "is_hot/nether", biomes)
    tag("worldgen/biome", fg, "is_hot", biomes)
    tag("worldgen/biome", fg, "is_dry/nether", biomes)
    tag("worldgen/biome", fg, "is_spooky", m(["petrified_onyx_forest"]))
    tag("worldgen/biome", MOD_ID, "has_structure/onyx_citadel", m(["petrified_onyx_forest"]))
    tag("worldgen/biome", MOD_ID, "has_structure/infernal_sanctum", m(["molten_glass_lake"]))
    tag("worldgen/biome", mc, "has_structure/nether_fossil", m(["petrified_onyx_forest"]))


def generate_advancements():
    advancement("ride_wyvern", "ash_wyvern_egg", "challenge", "nether_heart", {"ride": {
        "trigger": "minecraft:started_riding", "conditions": {"player": [{"condition": "minecraft:entity_properties", "entity": "this",
                                                                          "predicate": {"vehicle": {"type": mid("ash_wyvern")}}}]}}})
    advancement("basalt_golem", "basalt_golem_spawn_egg", "goal", "root", {"kill": {
        "trigger": "minecraft:player_killed_entity", "conditions": {"entity": [{"condition": "minecraft:entity_properties",
                                                                                 "entity": "this", "predicate": {"type": mid("basalt_golem")}}]}}})
    advancement("firefly_jar", "ember_firefly_jar", "task", "root", {
        "ember": has_items("ember_firefly_jar"), "soul": has_items("soul_firefly_jar")})
    # Une seule des deux especes suffit
    path = data_path("advancements", "firefly_jar.json")
    data = json.loads(path.read_text(encoding="utf-8"))
    data["requirements"] = [["ember", "soul"]]
    write_json(path, data)


def generate():
    generate_loot()
    generate_recipes()
    generate_tags()
    generate_advancements()
