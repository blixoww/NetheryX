"""Generation du monde : biomes, features, modificateurs de biomes, structures (JSON)."""
from .common import MOD_ID, data_path, mid, write_json

# Sous-ensembles ordonnes des features vanilla (meme ordre relatif que nether_wastes / basalt_deltas
# pour eviter toute erreur "feature order cycle").
VANILLA_UNDERGROUND = ["spring_open", "patch_fire", "patch_soul_fire", "glowstone_extra", "glowstone",
                       "ore_magma", "spring_closed", "ore_gravel_nether", "ore_blackstone", "ore_gold_nether",
                       "ore_quartz_nether", "ore_ancient_debris_large", "ore_debris_small"]


def vanilla(*exclude):
    return [f"minecraft:{f}" for f in VANILLA_UNDERGROUND if f not in exclude]


def color(hex_value):
    return int(hex_value.lstrip("#"), 16)


def state(name, **props):
    data = {"Name": mid(name)}
    if props:
        data["Properties"] = {k: str(v).lower() for k, v in props.items()}
    return data


def configured(name, feature_type, config):
    write_json(data_path("worldgen", "configured_feature", f"{name}.json"), {"type": feature_type, "config": config})


def placed(name, feature, placement):
    write_json(data_path("worldgen", "placed_feature", f"{name}.json"), {"feature": mid(feature), "placement": placement})


BIOME_FILTER = {"type": "minecraft:biome"}


def every_layer(count):
    return {"type": "minecraft:count_on_every_layer", "count": count}


def uniform_height(lo, hi):
    return {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": lo, "max_inclusive": hi}}


def ore(name, size, discard, targets, count, lo, hi):
    configured(name, "minecraft:ore", {"size": size, "discard_chance_on_air_exposure": discard, "targets": targets})
    placed(name, name, [{"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
                        uniform_height(lo, hi), BIOME_FILTER])


def patch(name, block_state, tries, xz, y, per_layer, rarity=None):
    configured(name, "minecraft:random_patch", {
        "tries": tries, "xz_spread": xz, "y_spread": y,
        "feature": {"feature": {"type": "minecraft:simple_block", "config": {
            "to_place": {"type": "minecraft:simple_state_provider", "state": block_state}}},
            "placement": [{"type": "minecraft:block_predicate_filter",
                           "predicate": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}]}})
    placement = [every_layer(per_layer)]
    if rarity:
        placement.append({"type": "minecraft:rarity_filter", "chance": rarity})
    placement.append(BIOME_FILTER)
    placed(name, name, placement)


def generate_features():
    nether_stone = {"predicate_type": "minecraft:tag_match", "tag": "minecraft:base_stone_nether"}
    netherrack = {"predicate_type": "minecraft:block_match", "block": "minecraft:netherrack"}

    # Onyx : rare partout dans le Nether (modificateur de biome), abondant dans les Profondeurs d'onyx
    # Aussi frequent que le diamant dans l'Overworld (7 filons de 4 par chunk, moitie supprimes au contact de l'air)
    ore("ore_onyx", 4, 0.5, [{"target": nether_stone, "state": state("onyx_ore")}], 7, {"above_bottom": 5}, {"absolute": 56})
    ore("ore_onyx_rich", 6, 0.3, [{"target": nether_stone, "state": state("onyx_ore")}], 10, {"above_bottom": 5}, {"absolute": 100})
    ore("ore_sulfur", 10, 0.0, [{"target": nether_stone, "state": state("sulfur_ore")}], 14, {"above_bottom": 10}, {"below_top": 10})
    ore("ore_sulfur_rock", 33, 0.0, [{"target": netherrack, "state": state("sulfur_rock")}], 6, {"above_bottom": 5}, {"below_top": 10})

    # Bosquet de braise
    replaceable = {"type": "minecraft:matching_blocks", "blocks": ["minecraft:crimson_roots", "minecraft:warped_roots",
                                                                   "minecraft:nether_sprouts", mid("ember_roots"),
                                                                   mid("charred_shrub")]}
    for name, planted_flag in (("ember_fungus", False), ("ember_fungus_planted", True)):
        configured(name, "minecraft:huge_fungus", {
            "valid_base_block": state("ember_nylium"),
            "stem_state": state("ember_stem", axis="y"),
            "hat_state": state("ember_wart_block"),
            "decor_state": {"Name": "minecraft:shroomlight"},
            "replaceable_blocks": replaceable,
            "planted": planted_flag})
    placed("ember_fungus", "ember_fungus", [every_layer(8), BIOME_FILTER])

    vegetation = {"type": "minecraft:weighted_state_provider", "entries": [
        {"weight": 85, "data": state("ember_roots")},
        {"weight": 11, "data": state("ember_fungus")},
        {"weight": 4, "data": state("fire_berry_bush", age=3)}]}
    configured("ember_vegetation", "minecraft:nether_forest_vegetation",
               {"state_provider": vegetation, "spread_width": 8, "spread_height": 4})
    placed("ember_vegetation", "ember_vegetation", [every_layer(6), BIOME_FILTER])
    configured("ember_vegetation_bonemeal", "minecraft:nether_forest_vegetation",
               {"state_provider": vegetation, "spread_width": 3, "spread_height": 1})
    patch("wild_blaze_pepper", state("blaze_pepper_crop", age=3), 24, 5, 2, 1, rarity=6)

    # Terres cendrees
    patch("charred_shrub_patch", state("charred_shrub"), 40, 7, 3, 2)
    patch("fire_berry_patch", state("fire_berry_bush", age=3), 20, 5, 2, 1, rarity=10)

    # Profondeurs d'onyx
    patch("onyx_cluster_patch", state("onyx_cluster", facing="up"), 32, 7, 3, 3)

    # Sources sulfureuses
    patch("sulfur_cluster_patch", state("sulfur_cluster", facing="up"), 32, 7, 3, 3)

    # Tubercules sauvages ajoutes a la Vallee des ames vanilla
    patch("wild_soul_tuber", state("soul_tuber_crop", age=3), 24, 5, 2, 1, rarity=5)

    # --- v1.1 ---------------------------------------------------------------------------------
    for name, planted_flag in (("withered_fungus", False), ("withered_fungus_planted", True)):
        configured(name, "minecraft:huge_fungus", {
            "valid_base_block": state("withered_soil"),
            "stem_state": state("withered_stem", axis="y"),
            "hat_state": state("withered_wart_block"),
            "decor_state": {"Name": "minecraft:bone_block", "Properties": {"axis": "y"}},
            "replaceable_blocks": replaceable,
            "planted": planted_flag})
    placed("withered_fungus", "withered_fungus", [every_layer(6), BIOME_FILTER])
    configured("withered_vegetation", "minecraft:nether_forest_vegetation", {"state_provider": {
        "type": "minecraft:weighted_state_provider", "entries": [
            {"weight": 70, "data": state("charred_shrub")},
            {"weight": 20, "data": state("withered_fungus")},
            {"weight": 10, "data": state("soul_lily")}]}, "spread_width": 8, "spread_height": 4})
    placed("withered_vegetation", "withered_vegetation", [every_layer(4), BIOME_FILTER])

    patch("soul_lily_patch", state("soul_lily"), 32, 6, 2, 2)

    # --- v1.3 : Prairie de braise et flore ------------------------------------------------------
    meadow = {"type": "minecraft:weighted_state_provider", "entries": [
        {"weight": 60, "data": state("nether_grass")},
        {"weight": 8, "data": state("ember_bloom")},
        {"weight": 8, "data": state("ash_daisy")},
        {"weight": 8, "data": state("crimson_bellflower")},
        {"weight": 8, "data": state("sulfur_tulip")},
        {"weight": 4, "data": state("glowcap")},
        {"weight": 4, "data": state("fire_berry_bush", age=3)}]}
    configured("meadow_vegetation", "minecraft:nether_forest_vegetation",
               {"state_provider": meadow, "spread_width": 8, "spread_height": 4})
    placed("meadow_vegetation", "meadow_vegetation", [every_layer(10), BIOME_FILTER])
    configured("meadow_vegetation_bonemeal", "minecraft:nether_forest_vegetation",
               {"state_provider": meadow, "spread_width": 3, "spread_height": 1})
    patch("wild_cinder_wheat", state("cinder_wheat_crop", age=3), 32, 6, 2, 1, rarity=3)
    placed("meadow_ember_fungus", "ember_fungus", [every_layer(1), {"type": "minecraft:rarity_filter", "chance": 2}, BIOME_FILTER])
    # Touches de vie dans les biomes vanilla (herbe et fleurs sur la netherrack, luminelles un peu partout)
    configured("nether_flora", "minecraft:random_patch", {
        "tries": 48, "xz_spread": 6, "y_spread": 2,
        "feature": {"feature": {"type": "minecraft:simple_block", "config": {"to_place": meadow}},
                    "placement": [{"type": "minecraft:block_predicate_filter",
                                   "predicate": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}]}})
    placed("nether_flora", "nether_flora", [every_layer(1), {"type": "minecraft:rarity_filter", "chance": 3}, BIOME_FILTER])
    patch("glowcap_patch", state("glowcap"), 16, 4, 2, 1, rarity=4)

    configured("magma_delta", "minecraft:delta_feature", {
        "contents": {"Name": "minecraft:lava", "Properties": {"level": "0"}},
        "rim": {"Name": "minecraft:magma_block"},
        "size": {"type": "minecraft:uniform", "value": {"min_inclusive": 3, "max_inclusive": 7}},
        "rim_size": {"type": "minecraft:uniform", "value": {"min_inclusive": 0, "max_inclusive": 2}}})
    placed("magma_delta", "magma_delta", [every_layer(30), BIOME_FILTER])



def generate_features_v120():
    """Features des biomes 1.2.0 (arbres et aiguilles sont des features Java du mod)."""
    for name, feature, per_layer, rarity in (("marrow_tree", "marrow_tree", 6, None), ("wart_tree", "wart_tree", 5, None),
                                             ("quartz_spire", "quartz_spire", 5, None), ("rime_spike", "rime_spike", 3, None)):
        configured(name, mid(feature), {})
        placement = [every_layer(per_layer)]
        if rarity:
            placement.append({"type": "minecraft:rarity_filter", "chance": rarity})
        placement.append(BIOME_FILTER)
        placed(name, name, placement)
    marrow = {"type": "minecraft:weighted_state_provider", "entries": [
        {"weight": 80, "data": state("marrow_sprouts")}, {"weight": 12, "data": state("charred_shrub")},
        {"weight": 8, "data": state("glowcap")}]}
    configured("marrow_vegetation", "minecraft:nether_forest_vegetation", {"state_provider": marrow, "spread_width": 8, "spread_height": 4})
    placed("marrow_vegetation", "marrow_vegetation", [every_layer(6), BIOME_FILTER])
    configured("marrow_vegetation_bonemeal", "minecraft:nether_forest_vegetation",
               {"state_provider": marrow, "spread_width": 3, "spread_height": 1})
    wart = {"type": "minecraft:weighted_state_provider", "entries": [
        {"weight": 50, "data": {"Name": "minecraft:crimson_roots"}}, {"weight": 25, "data": state("crimson_bellflower")},
        {"weight": 15, "data": state("fire_berry_bush", age=3)}, {"weight": 10, "data": {"Name": "minecraft:crimson_fungus"}}]}
    configured("wart_vegetation", "minecraft:nether_forest_vegetation", {"state_provider": wart, "spread_width": 8, "spread_height": 4})
    placed("wart_vegetation", "wart_vegetation", [every_layer(8), BIOME_FILTER])
    configured("wart_vegetation_bonemeal", "minecraft:nether_forest_vegetation", {"state_provider": wart, "spread_width": 3, "spread_height": 1})
    patch("rime_crystal_patch", state("rime_crystal", facing="up"), 24, 6, 3, 2)
    ore("ore_rich_quartz", 8, 0.0, [{"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:base_stone_nether"},
                                     "state": state("rich_quartz_ore")}], 12, {"above_bottom": 10}, {"below_top": 10})
    ore("ore_rimestone", 40, 0.0, [{"target": {"predicate_type": "minecraft:block_match", "block": "minecraft:netherrack"},
                                    "state": state("rimestone")}], 8, {"above_bottom": 5}, {"below_top": 5})
    # Verrulianes au plafond de l'Enchevetrement (comme les lianes pleureuses vanilla, mais avec des fruits)
    configured("wartvine_ceiling", "minecraft:block_column", {
        "direction": "down", "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
        "prioritize_tip": True,
        "layers": [{"height": {"type": "minecraft:uniform", "value": {"min_inclusive": 1, "max_inclusive": 6}},
                    "provider": {"type": "minecraft:weighted_state_provider", "entries": [
                        {"weight": 3, "data": state("wartvine_plant", fruit=False)}, {"weight": 1, "data": state("wartvine_plant", fruit=True)}]}},
                   {"height": 1, "provider": {"type": "minecraft:simple_state_provider", "state": state("wartvine", age=20, fruit=True)}}]})
    placed("wartvine_ceiling", "wartvine_ceiling", [
        {"type": "minecraft:count", "count": 40}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"above_bottom": 0},
                                                      "max_inclusive": {"below_top": 0}}},
        {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 12,
         "target_condition": {"type": "minecraft:solid"}, "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
        {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1}, BIOME_FILTER])
    configured("marrow_vines_ceiling", "minecraft:block_column", {
        "direction": "down", "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
        "prioritize_tip": True,
        "layers": [{"height": {"type": "minecraft:uniform", "value": {"min_inclusive": 2, "max_inclusive": 9}},
                    "provider": {"type": "minecraft:simple_state_provider", "state": state("marrow_vines_plant")}},
                   {"height": 1, "provider": {"type": "minecraft:simple_state_provider", "state": state("marrow_vines", age=20)}}]})
    placed("marrow_vines_ceiling", "marrow_vines_ceiling", [
        {"type": "minecraft:count", "count": 30}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"above_bottom": 0},
                                                      "max_inclusive": {"below_top": 0}}},
        {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 12,
         "target_condition": {"type": "minecraft:solid"}, "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
        {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1}, BIOME_FILTER])


def spawner(entity, weight, lo, hi):
    return {"type": mid(entity), "weight": weight, "minCount": lo, "maxCount": hi}


# Reliefs propres a chaque biome (rochers, troncs couches) : des features distinctes par biome, placees a l'etape
# "local_modifications", pour casser les grandes etendues plates sans risque de cycle d'ordre des features.
ORGANIC = {
    # biome : (bloc des rochers, rarete des rochers, tronc couche ou None, rarete des troncs)
    "ember_meadow": ("minecraft:blackstone", 5, None, 0),
    "ember_grove": ("minecraft:netherrack", 4, "ember_stem", 2),
    "ashen_wastes": ("minecraft:basalt", 2, None, 0),
    "onyx_depths": ("minecraft:blackstone", 2, None, 0),
    "sulfur_springs": ("sulfur_rock", 2, None, 0),
    "soulfire_marsh": ("minecraft:basalt", 4, "withered_stem", 6),
    "withered_forest": ("minecraft:blackstone", 4, "withered_stem", 2),
    "magma_rift": ("minecraft:smooth_basalt", 3, None, 0),
    "marrow_thicket": ("minecraft:bone_block", 3, "marrow_stem", 3),
    "rimefrost_chasm": ("rimestone", 2, None, 0),
    "wartvine_tangle": ("minecraft:netherrack", 5, "wartstalk_stem", 2),
    "quartz_reef": ("minecraft:smooth_quartz", 4, None, 0),
    "hellwood_jungle": ("minecraft:blackstone", 4, "ember_stem", 1),
    "molten_glass_lake": ("minecraft:obsidian", 3, None, 0),
    "petrified_onyx_forest": ("minecraft:smooth_basalt", 3, "petrified_log", 2),
}


def block_state(name, **props):
    data = {"Name": name if ":" in name else mid(name)}
    if props:
        data["Properties"] = {k: str(v).lower() for k, v in props.items()}
    return data


def generate_organic_features():
    for biome_name, (rock, rock_rarity, log, log_rarity) in ORGANIC.items():
        props = {"axis": "y"} if rock in ("minecraft:basalt", "minecraft:bone_block") else {}
        configured(f"boulder_{biome_name}", mid("boulder"), {"state": block_state(rock, **props)})
        placed(f"boulder_{biome_name}", f"boulder_{biome_name}", [
            every_layer(1), {"type": "minecraft:rarity_filter", "chance": rock_rarity}, BIOME_FILTER])
        if log:
            configured(f"fallen_log_{biome_name}", mid("fallen_log"), {"state": block_state(log, axis="x")})
            placed(f"fallen_log_{biome_name}", f"fallen_log_{biome_name}", [
                every_layer(1), {"type": "minecraft:rarity_filter", "chance": log_rarity}, BIOME_FILTER])


def organic(name):
    if name not in ORGANIC:
        return []
    features = [mid(f"boulder_{name}")]
    if ORGANIC[name][2]:
        features.append(mid(f"fallen_log_{name}"))
    return features


def generate_hellwood_features():
    """Jungle infernale : grands arbres de braise, fougeres, herbe ardente et lianes de braise."""
    configured("hell_tree", mid("hell_tree"), {})
    placed("hell_tree", "hell_tree", [every_layer(4), BIOME_FILTER])
    jungle = {"type": "minecraft:weighted_state_provider", "entries": [
        {"weight": 60, "data": state("blazing_grass")}, {"weight": 18, "data": state("nether_grass")},
        {"weight": 8, "data": state("ember_bloom")}, {"weight": 6, "data": state("crimson_bellflower")},
        {"weight": 5, "data": state("ember_roots")}, {"weight": 3, "data": state("fire_berry_bush", age=3)}]}
    configured("hellwood_vegetation", "minecraft:nether_forest_vegetation", {"state_provider": jungle, "spread_width": 8, "spread_height": 4})
    placed("hellwood_vegetation", "hellwood_vegetation", [every_layer(10), BIOME_FILTER])
    configured("hellwood_vegetation_bonemeal", "minecraft:nether_forest_vegetation",
               {"state_provider": jungle, "spread_width": 3, "spread_height": 1})
    patch("hellfern_patch", state("hellfern", half="lower"), 40, 7, 3, 3)
    configured("ember_vines_ceiling", "minecraft:block_column", {
        "direction": "down", "allowed_placement": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
        "prioritize_tip": True,
        "layers": [{"height": {"type": "minecraft:uniform", "value": {"min_inclusive": 3, "max_inclusive": 12}},
                    "provider": {"type": "minecraft:simple_state_provider", "state": state("ember_vines_plant")}},
                   {"height": 1, "provider": {"type": "minecraft:simple_state_provider", "state": state("ember_vines", age=20)}}]})
    placed("ember_vines_ceiling", "ember_vines_ceiling", [
        {"type": "minecraft:count", "count": 60}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"above_bottom": 0},
                                                      "max_inclusive": {"below_top": 0}}},
        {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 14,
         "target_condition": {"type": "minecraft:solid"}, "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
        {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1}, BIOME_FILTER])


def generate_v124_features():
    """Lac de verre en fusion (ilots, aiguilles d'obsidienne, cristaux) et Foret petrifiee d'onyx."""
    configured("obsidian_delta", "minecraft:delta_feature", {
        "contents": {"Name": "minecraft:lava", "Properties": {"level": "0"}},
        "rim": {"Name": "minecraft:obsidian"},
        "size": {"type": "minecraft:uniform", "value": {"min_inclusive": 3, "max_inclusive": 8}},
        "rim_size": {"type": "minecraft:uniform", "value": {"min_inclusive": 1, "max_inclusive": 2}}})
    placed("obsidian_delta", "obsidian_delta", [every_layer(25), BIOME_FILTER])
    configured("lava_island", mid("lava_island"), {})
    placed("lava_island", "lava_island", [{"type": "minecraft:count", "count": 5}, {"type": "minecraft:in_square"},
                                          {"type": "minecraft:height_range", "height": {"type": "minecraft:constant", "value": {"absolute": 31}}},
                                          BIOME_FILTER])
    configured("obsidian_spire", mid("obsidian_spire"), {})
    placed("obsidian_spire", "obsidian_spire", [every_layer(2), BIOME_FILTER])
    patch("molten_crystal_patch", state("molten_crystal", facing="up"), 24, 6, 3, 2)
    # Recifs de quartz : amas de quartz au sol et sous la voute
    patch("quartz_cluster_patch", state("quartz_cluster", facing="up"), 32, 7, 3, 3)
    configured("quartz_cluster_ceiling", "minecraft:simple_block", {
        "to_place": {"type": "minecraft:simple_state_provider", "state": state("quartz_cluster", facing="down")}})
    placed("quartz_cluster_ceiling", "quartz_cluster_ceiling", [
        {"type": "minecraft:count", "count": 70}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"above_bottom": 0},
                                                      "max_inclusive": {"below_top": 0}}},
        {"type": "minecraft:environment_scan", "direction_of_search": "up", "max_steps": 12,
         "target_condition": {"type": "minecraft:solid"}, "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}},
        {"type": "minecraft:random_offset", "xz_spread": 0, "y_spread": -1}, BIOME_FILTER])
    configured("petrified_tree", mid("petrified_tree"), {})
    placed("petrified_tree", "petrified_tree", [every_layer(5), BIOME_FILTER])


def biome(name, fog, particle, probability, ambient, music, monsters, creatures, underground, vegetal, ambient_mobs=()):
    features = [[] for _ in range(11)]
    features[2] = organic(name)
    features[7] = underground
    features[9] = vegetal
    effects = {
        "fog_color": color(fog), "sky_color": color("#330808"), "water_color": 4159204, "water_fog_color": 329011,
        "particle": {"options": {"type": particle}, "probability": probability},
        "ambient_sound": f"minecraft:ambient.{ambient}.loop",
        "mood_sound": {"sound": f"minecraft:ambient.{ambient}.mood", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0},
        "additions_sound": {"sound": f"minecraft:ambient.{ambient}.additions", "tick_chance": 0.0111},
        "music": {"sound": f"minecraft:music.nether.{music}", "min_delay": 12000, "max_delay": 24000, "replace_current_music": False},
    }
    write_json(data_path("worldgen", "biome", f"{name}.json"), {
        "has_precipitation": False, "temperature": 2.0, "downfall": 0.0,
        "effects": effects,
        "spawners": {"monster": monsters, "creature": creatures, "ambient": list(ambient_mobs), "axolotls": [],
                     "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []},
        "spawn_costs": {},
        "creature_spawn_probability": 0.3,
        "carvers": {"air": "minecraft:nether_cave"},
        "features": features,
    })


def generate_biomes():
    strider = spawner("minecraft:strider", 60, 1, 2)
    biome("ember_grove", "#5c1a08", "netheryx:ember_spark", 0.02, "crimson_forest", "crimson_forest",
          [spawner("ember_stalker", 50, 1, 2), spawner("fire_ghast", 30, 1, 1), spawner("minecraft:zombified_piglin", 10, 2, 4),
           spawner("minecraft:magma_cube", 3, 1, 2)],
          [strider],
          vanilla("patch_soul_fire"),
          [mid("ember_fungus"), mid("ember_vegetation"), mid("wild_blaze_pepper")])
    biome("ashen_wastes", "#5a5654", "netheryx:ash_flake", 0.06, "basalt_deltas", "basalt_deltas",
          [spawner("ash_wraith", 45, 1, 2), spawner("fire_ghast", 30, 1, 1), spawner("minecraft:ghast", 10, 1, 1), spawner("minecraft:skeleton", 15, 2, 3),
           spawner("minecraft:enderman", 1, 2, 4)],
          [strider],
          vanilla("ore_gravel_nether", "ore_blackstone"),
          [mid("charred_shrub_patch"), mid("fire_berry_patch")])
    biome("onyx_depths", "#1a1024", "netheryx:onyx_glint", 0.012, "soul_sand_valley", "soul_sand_valley",
          [spawner("minecraft:magma_cube", 30, 2, 4), spawner("onyx_cube", 40, 1, 3), spawner("ash_wraith", 10, 1, 1), spawner("minecraft:ghast", 10, 1, 1),
           spawner("minecraft:enderman", 1, 4, 4)],
          [strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("ore_onyx_rich")],
          [mid("onyx_cluster_patch"), mid("glowcap_patch")])
    biome("sulfur_springs", "#6b5a14", "netheryx:sulfur_puff", 0.008, "nether_wastes", "nether_wastes",
          [spawner("minecraft:magma_cube", 40, 2, 4), spawner("minecraft:ghast", 20, 1, 1),
           spawner("minecraft:zombified_piglin", 30, 2, 4)],
          [spawner("magma_crab", 50, 2, 5), spawner("ember_cow", 15, 2, 3), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("ore_sulfur_rock"), mid("ore_sulfur")],
          [mid("sulfur_cluster_patch")])
    biome("ember_meadow", "#6a2e16", "netheryx:ember_pollen", 0.012, "crimson_forest", "crimson_forest",
          [spawner("minecraft:zombified_piglin", 10, 2, 3), spawner("minecraft:magma_cube", 3, 1, 2)],
          [spawner("ember_cow", 60, 2, 5), spawner("cinder_hen", 50, 2, 5), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("meadow_ember_fungus"), mid("meadow_vegetation"), mid("wild_cinder_wheat")],
          ambient_mobs=[spawner("soul_moth", 6, 1, 2)])
    biome("soulfire_marsh", "#0f3a44", "netheryx:soul_wisp", 0.008, "soul_sand_valley", "soul_sand_valley",
          [spawner("minecraft:ghast", 30, 1, 1), spawner("minecraft:skeleton", 15, 2, 3), spawner("ash_wraith", 10, 1, 1),
           spawner("minecraft:enderman", 1, 2, 4)],
          [strider],
          vanilla("patch_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("soul_lily_patch"), mid("glowcap_patch")],
          ambient_mobs=[spawner("soul_moth", 30, 2, 4)])
    biome("withered_forest", "#2a2a2e", "netheryx:wither_spore", 0.03, "warped_forest", "warped_forest",
          [spawner("withered_knight", 50, 1, 3), spawner("minecraft:wither_skeleton", 5, 1, 2),
           spawner("minecraft:enderman", 5, 1, 2)],
          [strider],
          vanilla("patch_soul_fire", "ore_gravel_nether"),
          [mid("withered_fungus"), mid("withered_vegetation"), mid("glowcap_patch")],
          ambient_mobs=[spawner("soul_moth", 8, 1, 2)])
    biome("magma_rift", "#7a1e06", "netheryx:ember_spark", 0.012, "basalt_deltas", "basalt_deltas",
          [spawner("minecraft:magma_cube", 50, 2, 4), spawner("fire_ghast", 40, 1, 1), spawner("onyx_cube", 15, 1, 2),
           spawner("minecraft:blaze", 10, 1, 2),
           spawner("ember_stalker", 10, 1, 1), spawner("minecraft:ghast", 10, 1, 1)],
          [spawner("basalt_beetle", 40, 2, 4), strider],
          ["minecraft:small_basalt_columns", "minecraft:large_basalt_columns"]
          + vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("magma_delta")],
          [])

    # --- 1.2.0 -------------------------------------------------------------------------------------
    biome("marrow_thicket", "#3a3428", "minecraft:white_ash", 0.04, "soul_sand_valley", "soul_sand_valley",
          [spawner("minecraft:skeleton", 30, 2, 3), spawner("withered_knight", 15, 1, 2), spawner("minecraft:ghast", 10, 1, 1),
           spawner("basalt_burrower", 10, 1, 1), spawner("minecraft:enderman", 2, 1, 2)],
          [spawner("lantern_bearer", 10, 1, 2), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("marrow_tree"), mid("marrow_vegetation"), mid("marrow_vines_ceiling"), mid("glowcap_patch")],
          ambient_mobs=[spawner("soul_moth", 6, 1, 2)])
    biome("rimefrost_chasm", "#2a3e52", "netheryx:frost_mote", 0.02, "soul_sand_valley", "soul_sand_valley",
          [spawner("minecraft:ghast", 20, 1, 1), spawner("minecraft:skeleton", 25, 2, 3), spawner("ash_wraith", 15, 1, 2),
           spawner("minecraft:enderman", 2, 1, 2)],
          [spawner("lantern_bearer", 10, 1, 2), strider],
          vanilla("patch_fire", "patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("ore_rimestone")],
          [mid("rime_spike"), mid("rime_crystal_patch")])
    biome("wartvine_tangle", "#4a0e0c", "minecraft:crimson_spore", 0.03, "crimson_forest", "crimson_forest",
          [spawner("minecraft:hoglin", 30, 2, 4), spawner("minecraft:piglin", 20, 2, 4), spawner("minecraft:zombified_piglin", 10, 2, 3)],
          [spawner("cinder_lizard", 20, 1, 3), spawner("ember_cow", 25, 2, 4), spawner("cinder_hen", 20, 2, 4), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("wart_tree"), mid("wart_vegetation"), mid("wartvine_ceiling")])
    biome("quartz_reef", "#5a4650", "netheryx:quartz_glint", 0.03, "nether_wastes", "nether_wastes",
          [spawner("minecraft:magma_cube", 30, 2, 4), spawner("minecraft:ghast", 20, 1, 1), spawner("fire_ghast", 10, 1, 1),
           spawner("minecraft:blaze", 5, 1, 2)],
          [spawner("magmafish", 30, 3, 6), spawner("magma_crab", 35, 2, 4), spawner("crystal_snail", 20, 2, 3),
           spawner("blaze_pig", 15, 2, 3), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("ore_rich_quartz")],
          [mid("quartz_spire"), mid("quartz_cluster_patch"), mid("quartz_cluster_ceiling")],
          ambient_mobs=[spawner("ember_firefly", 30, 5, 8)])
    biome("hellwood_jungle", "#6e1c0a", "netheryx:ember_pollen", 0.02, "crimson_forest", "crimson_forest",
          [spawner("ember_stalker", 35, 1, 2), spawner("fire_ghast", 30, 1, 1), spawner("minecraft:zombified_piglin", 10, 2, 3)],
          [spawner("cinder_hen", 50, 2, 5), spawner("ember_cow", 45, 2, 5), spawner("cinder_lizard", 20, 1, 3), spawner("blaze_pig", 25, 2, 4), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("hell_tree"), mid("hellfern_patch"), mid("hellwood_vegetation"), mid("ember_vines_ceiling")],
          ambient_mobs=[spawner("soul_moth", 10, 1, 3), spawner("ember_firefly", 60, 6, 10)])
    biome("molten_glass_lake", "#8a2c08", "netheryx:ember_spark", 0.02, "basalt_deltas", "basalt_deltas",
          [spawner("minecraft:magma_cube", 40, 2, 4), spawner("fire_ghast", 25, 1, 1), spawner("minecraft:blaze", 10, 1, 2),
           spawner("basalt_golem", 3, 1, 1)],
          [spawner("blaze_pig", 40, 2, 5), spawner("magmafish", 40, 3, 5), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("obsidian_delta"), mid("lava_island"), mid("obsidian_spire"), mid("molten_crystal_patch")],
          ambient_mobs=[spawner("ember_firefly", 35, 5, 8)])
    biome("petrified_onyx_forest", "#241a30", "netheryx:onyx_glint", 0.015, "soul_sand_valley", "soul_sand_valley",
          [spawner("onyx_cube", 30, 1, 3), spawner("ash_wraith", 20, 1, 2), spawner("minecraft:skeleton", 15, 2, 3),
           spawner("minecraft:enderman", 5, 1, 2)],
          [spawner("crystal_snail", 45, 2, 5), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("ore_onyx_rich")],
          [mid("petrified_tree"), mid("onyx_cluster_patch"), mid("glowcap_patch")],
          ambient_mobs=[spawner("soul_firefly", 55, 6, 10)])


def biome_modifier(name, data):
    write_json(data_path("forge", "biome_modifier", f"{name}.json"), data)


def generate_biome_modifiers():
    biome_modifier("add_onyx_ore", {"type": "forge:add_features", "biomes": "#minecraft:is_nether",
                                    "features": mid("ore_onyx"), "step": "underground_decoration"})
    # Le Nether vanilla change aussi visiblement avec NetheryX. Un seul modificateur par biome, dans un ordre
    # compatible avec celui des biomes NetheryX (evite toute erreur "feature order cycle").
    decorations = {
        "nether_wastes": ["charred_shrub_patch", "nether_flora", "glowcap_patch"],
        "crimson_forest": ["fire_berry_patch", "nether_flora", "glowcap_patch"],
        "warped_forest": ["soul_lily_patch", "nether_flora", "glowcap_patch"],
        "soul_sand_valley": ["wild_soul_tuber", "soul_lily_patch", "glowcap_patch"],
        "basalt_deltas": ["sulfur_cluster_patch", "glowcap_patch"],
    }
    for biome_name, features in decorations.items():
        biome_modifier(f"decorate_{biome_name}", {"type": "forge:add_features", "biomes": f"minecraft:{biome_name}",
                                                  "features": [mid(f) for f in features], "step": "vegetal_decoration"})
    # Les creatures "standard" (pas les boss) apparaissent ici ou la dans tout le Nether, biomes moddes compris
    biome_modifier("add_netheryx_spawns_everywhere", {"type": "forge:add_spawns", "biomes": "#minecraft:is_nether", "spawners": [
        spawner("ember_stalker", 14, 1, 2), spawner("ash_wraith", 10, 1, 2), spawner("withered_knight", 10, 1, 2),
        spawner("fire_ghast", 24, 1, 1), spawner("onyx_cube", 10, 1, 3),
        spawner("magma_crab", 32, 2, 4), spawner("basalt_beetle", 26, 2, 4), spawner("ember_cow", 40, 2, 5),
        spawner("cinder_hen", 40, 2, 5), spawner("soul_moth", 14, 2, 4), spawner("magmafish", 18, 2, 5),
        spawner("blaze_pig", 20, 2, 4), spawner("ember_firefly", 16, 6, 10)]})
    biome_modifier("add_v120_vanilla_spawns_basalt", {"type": "forge:add_spawns", "biomes": "minecraft:basalt_deltas", "spawners": [
        spawner("basalt_burrower", 15, 1, 1), spawner("cinder_lizard", 6, 1, 2)]})
    biome_modifier("add_v120_vanilla_spawns_soul", {"type": "forge:add_spawns", "biomes": "minecraft:soul_sand_valley", "spawners": [
        spawner("lantern_bearer", 18, 1, 3)]})
    biome_modifier("add_v120_modded_spawns", {"type": "forge:add_spawns", "biomes": [mid(b) for b in ("ashen_wastes", "onyx_depths")],
                                              "spawners": [spawner("basalt_burrower", 12, 1, 1)]})
    biome_modifier("add_v120_lantern_bearers", {"type": "forge:add_spawns", "biomes": [mid(b) for b in ("soulfire_marsh", "withered_forest")],
                                                "spawners": [spawner("lantern_bearer", 18, 1, 3)]})
    biome_modifier("add_fireflies_ember", {"type": "forge:add_spawns", "biomes": [mid("ember_grove"), mid("ember_meadow"),
                                                                                  "minecraft:crimson_forest"],
                                           "spawners": [spawner("ember_firefly", 50, 6, 10)]})
    biome_modifier("add_fireflies_soul", {"type": "forge:add_spawns", "biomes": [mid("soulfire_marsh"), "minecraft:soul_sand_valley",
                                                                                 "minecraft:warped_forest"],
                                          "spawners": [spawner("soul_firefly", 50, 6, 10)]})
    biome_modifier("add_blaze_pigs", {"type": "forge:add_spawns", "biomes": [mid("magma_rift"), mid("ember_grove"), mid("hellwood_jungle"),
                                                                             "minecraft:nether_wastes"],
                                      "spawners": [spawner("blaze_pig", 35, 2, 5)]})
    biome_modifier("add_crystal_snails", {"type": "forge:add_spawns", "biomes": [mid("onyx_depths")],
                                          "spawners": [spawner("crystal_snail", 25, 2, 4)]})
    # Wyvernes sauvages : rares, neutres, apprivoisables avec de la viande
    biome_modifier("add_wild_wyverns", {"type": "forge:add_spawns", "biomes": [mid("ashen_wastes"), mid("molten_glass_lake"),
                                                                               "minecraft:basalt_deltas"],
                                        "spawners": [spawner("ash_wyvern", 2, 1, 1)]})
    biome_modifier("add_basalt_golems", {"type": "forge:add_spawns", "biomes": [mid("magma_rift"), "minecraft:basalt_deltas"],
                                         "spawners": [spawner("basalt_golem", 2, 1, 1)]})
    biome_modifier("add_v120_lizards", {"type": "forge:add_spawns", "biomes": [mid(b) for b in ("magma_rift", "ember_meadow")],
                                        "spawners": [spawner("cinder_lizard", 18, 1, 3)]})


STRUCTURES = {
    # nom : (espacement, separation, sel, air requis, y min, enfoncement, emprise, adaptation au terrain)
    # Espacements reduits : un donjon tous les ~300 blocs environ, une petite structure tous les ~130 blocs.
    "onyx_citadel": (20, 8, 761234019, 14, 33, 0, 44, "beard_box"),
    "infernal_sanctum": (20, 8, 663901247, 10, 33, 10, 40, "none"),
    "withered_catacombs": (18, 7, 518230457, 8, 45, 16, 38, "none"),
    "traveler_camp": (9, 3, 734029118, 6, 33, 0, 12, "beard_thin"),
    "nether_garden": (10, 4, 192837465, 7, 33, 0, 27, "beard_thin"),
    "hidden_cache": (5, 2, 556172839, 4, 40, 7, 6, "none"),
    "nomad_caravan": (14, 5, 902817364, 7, 33, 0, 25, "beard_thin"),
    "basalt_forge": (18, 6, 640918273, 12, 33, 0, 25, "beard_thin"),
    "wyvern_nest": (16, 6, 418273650, 18, 33, 1, 12, "beard_thin"),
}

PROCESSORS = {
}


def generate_structures():
    for name, (spacing, separation, salt, air, min_y, sink, footprint, adaptation) in STRUCTURES.items():
        processors = "minecraft:empty"
        if name in PROCESSORS:
            processors = mid(f"{name}_decay")
            write_json(data_path("worldgen", "processor_list", f"{name}_decay.json"), {"processors": [{
                "processor_type": "minecraft:rule",
                "rules": [{"input_predicate": {"predicate_type": "minecraft:random_block_match", "block": block, "probability": p},
                           "location_predicate": {"predicate_type": "minecraft:always_true"},
                           "output_state": {"Name": out}} for (block, p, out) in PROCESSORS[name]]}]})
        write_json(data_path("worldgen", "template_pool", name, "start.json"), {
            "fallback": "minecraft:empty",
            "elements": [{"weight": 1, "element": {"element_type": "minecraft:single_pool_element",
                                                   "location": mid(f"{name}/main"), "projection": "rigid",
                                                   "processors": processors}}]})
        write_json(data_path("worldgen", "structure", f"{name}.json"), {
            "type": mid("nether_floor"),
            "biomes": f"#{MOD_ID}:has_structure/{name}",
            # Placee apres toutes les decorations du biome (deltas, basalte, champignons, vegetation) :
            # elles ne peuvent plus recouvrir les sols et les couloirs.
            "step": "top_layer_modification",
            "spawn_overrides": {},
            "terrain_adaptation": adaptation,
            "start_pool": mid(f"{name}/start"),
            "size": 1,
            "max_y": 100,
            "min_y": min_y,
            "sink": sink,
            "footprint": footprint,
            "required_air": air,
            "max_distance_from_center": 80,
        })
        write_json(data_path("worldgen", "structure_set", f"{name}.json"), {
            "structures": [{"structure": mid(name), "weight": 1}],
            "placement": {"type": "minecraft:random_spread", "spacing": spacing, "separation": separation, "salt": salt,
                          # Jamais par-dessus un bastion ou une forteresse
                          "exclusion_zone": {"other_set": "minecraft:nether_complexes", "chunk_count": 3 if footprint > 20 else 2}}})


def generate():
    generate_features()
    generate_features_v120()
    generate_organic_features()
    generate_hellwood_features()
    generate_v124_features()
    generate_biomes()
    generate_biome_modifiers()
    generate_structures()
