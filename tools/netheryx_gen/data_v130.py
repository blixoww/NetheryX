"""Donnees 1.3.0 : fruits des arbres du Nether (figues de braise, prunes fletries) et leur generation."""
from .common import mid
from .data import DECAY, SILK_TOUCH, SURVIVES, block_table, count, item, tag
from .worldgen import BIOME_FILTER, biome_modifier, configured, placed, state

FRUITS = {
    # grappe : (fruit, houppiers, biomes ou poussent les arbres)
    "ember_fig_cluster": ("ember_fig", [mid("ember_wart_block"), "minecraft:nether_wart_block"],
                          [mid("ember_grove"), mid("hellwood_jungle"), mid("ember_meadow")]),
    "withered_plum_cluster": ("withered_plum", [mid("withered_wart_block"), "minecraft:warped_wart_block"],
                              [mid("withered_forest")]),
}
NO_SILK = {"condition": "minecraft:inverted", "term": SILK_TOUCH}


def generate_fruit_loot():
    for cluster, (fruit, _, _) in FRUITS.items():
        # Mure : 1 a 3 fruits ; sinon un seul (de quoi la replanter)
        block_table(cluster, [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            item(fruit, functions=[count(1, 3), DECAY], conditions=[
                {"condition": "minecraft:block_state_property", "block": mid(cluster), "properties": {"age": "2"}}]),
            item(fruit, functions=[DECAY])]}]}])
    # Les houppiers des arbres lachent parfois un fruit quand on les casse (comme les pommes des chenes)
    for canopy, fruit in (("ember_wart_block", "ember_fig"), ("withered_wart_block", "withered_plum")):
        block_table(canopy, [
            {"rolls": 1, "bonus_rolls": 0, "entries": [item(canopy)], "conditions": [SURVIVES]},
            {"rolls": 1, "bonus_rolls": 0, "entries": [item(fruit)], "conditions": [
                NO_SILK, {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune",
                          "chances": [0.04, 0.06, 0.08, 0.12]}]},
        ])


def generate_fruit_tags():
    for cluster, (_, canopies, _) in FRUITS.items():
        fruit = cluster.replace("_cluster", "")
        tag("blocks", "netheryx", f"{fruit}_canopy", canopies)
    tag("blocks", "minecraft", "mineable/hoe", [mid(c) for c in FRUITS])
    tag("blocks", "netheryx", "cinder_lizard_scents", ["minecraft:ancient_debris", mid("onyx_ore")])


def generate_fruit_features():
    """Grappes suspendues sous les houppiers, deja plus ou moins mures."""
    for cluster, (_, canopies, biomes) in FRUITS.items():
        ages = {"type": "minecraft:weighted_state_provider", "entries": [
            {"weight": 2, "data": state(cluster, age=0)}, {"weight": 3, "data": state(cluster, age=1)},
            {"weight": 5, "data": state(cluster, age=2)}]}
        configured(cluster, "minecraft:simple_block", {"to_place": ages})
        placed(cluster, cluster, [
            {"type": "minecraft:count", "count": 60}, {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"above_bottom": 0},
                                                          "max_inclusive": {"below_top": 0}}},
            {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 12,
             "target_condition": {"type": "minecraft:matching_blocks", "blocks": canopies},
             "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
            {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1},
            {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
            BIOME_FILTER])
        biome_modifier(f"add_{cluster}", {"type": "forge:add_features", "biomes": biomes,
                                          "features": [mid(cluster)], "step": "vegetal_decoration"})


def generate():
    generate_fruit_loot()
    generate_fruit_tags()
    generate_fruit_features()
