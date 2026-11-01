"""Donnees 1.4.0 : butin de la chambre des clefs, recompense garantie au fond de chaque aile."""
from .common import MOD_ID
from .data import chest_table, enchanted, item, m, pool, tag


def generate():
    # La Forge et les Nids dependaient de biomes devenus minoritaires dans la nouvelle repartition du climat
    tag("worldgen/biome", MOD_ID, "has_structure/wyvern_nest", m(["quartz_reef", "wartvine_tangle"]) + ["minecraft:basalt_deltas", "minecraft:nether_wastes"])
    tag("worldgen/biome", MOD_ID, "has_structure/basalt_forge", m(["sulfur_springs", "quartz_reef", "hellwood_jungle"]) + ["minecraft:nether_wastes"])
    chest_table("rune_key_chamber", [
        {"rolls": 1, "bonus_rolls": 0, "entries": [item("rune_key")]},
        pool(2, 4, [
            item("onyx_shard", weight=8), item("sulfur", weight=8), item("minecraft:gold_ingot", weight=6),
            item("onyx", weight=3), item("glimmer_elixir", weight=3), item("minecraft:golden_apple", weight=2),
            enchanted("onyx_pickaxe", 20, 30, 1)]),
    ])
