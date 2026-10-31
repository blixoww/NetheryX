"""Donnees 1.3.4 : structures suspendues sous la voute (nids de spectres, cages d'explorateurs) dans tout le Nether."""
from .common import mid
from .worldgen import BIOME_FILTER, biome_modifier, configured, placed


def generate_ceiling_hangs():
    configured("ceiling_hang", mid("ceiling_hang"), {})
    # Une tentative par chunk sur trois environ : on cherche la voute au-dessus d'un point d'air, entre y = 40 et le
    # plafond ; la feature verifie ensuite qu'un grand vide s'ouvre dessous.
    placed("ceiling_hang", "ceiling_hang", [
        {"type": "minecraft:rarity_filter", "chance": 3},
        {"type": "minecraft:count", "count": 2},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 40},
                                                      "max_inclusive": {"below_top": 10}}},
        {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 24,
         "target_condition": {"type": "minecraft:solid"},
         "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
        {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1},
        BIOME_FILTER])
    biome_modifier("add_ceiling_hangs", {"type": "forge:add_features", "biomes": "#minecraft:is_nether",
                                         "features": [mid("ceiling_hang")], "step": "top_layer_modification"})


def generate():
    generate_ceiling_hangs()
