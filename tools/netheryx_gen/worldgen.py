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



def spawner(entity, weight, lo, hi):
    return {"type": mid(entity), "weight": weight, "minCount": lo, "maxCount": hi}


def biome(name, fog, particle, probability, ambient, music, monsters, creatures, underground, vegetal, ambient_mobs=()):
    features = [[] for _ in range(11)]
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
        "carvers": {"air": "minecraft:nether_cave"},
        "features": features,
    })


def generate_biomes():
    strider = spawner("minecraft:strider", 60, 1, 2)
    biome("ember_grove", "#5c1a08", "minecraft:crimson_spore", 0.025, "crimson_forest", "crimson_forest",
          [spawner("ember_stalker", 50, 1, 2), spawner("fire_ghast", 15, 1, 1), spawner("minecraft:zombified_piglin", 10, 2, 4),
           spawner("minecraft:magma_cube", 3, 1, 2)],
          [strider],
          vanilla("patch_soul_fire"),
          [mid("ember_fungus"), mid("ember_vegetation"), mid("wild_blaze_pepper")])
    biome("ashen_wastes", "#5a5654", "minecraft:white_ash", 0.12, "basalt_deltas", "basalt_deltas",
          [spawner("ash_wraith", 45, 1, 2), spawner("fire_ghast", 20, 1, 1), spawner("minecraft:ghast", 10, 1, 1), spawner("minecraft:skeleton", 15, 2, 3),
           spawner("minecraft:enderman", 1, 2, 4)],
          [strider],
          vanilla("ore_gravel_nether", "ore_blackstone"),
          [mid("charred_shrub_patch"), mid("fire_berry_patch")])
    biome("onyx_depths", "#1a1024", "minecraft:ash", 0.02, "soul_sand_valley", "soul_sand_valley",
          [spawner("minecraft:magma_cube", 30, 2, 4), spawner("onyx_cube", 40, 1, 3), spawner("ash_wraith", 10, 1, 1), spawner("minecraft:ghast", 10, 1, 1),
           spawner("minecraft:enderman", 1, 4, 4)],
          [strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("ore_onyx_rich")],
          [mid("onyx_cluster_patch"), mid("glowcap_patch")])
    biome("sulfur_springs", "#6b5a14", "minecraft:falling_lava", 0.004, "nether_wastes", "nether_wastes",
          [spawner("minecraft:magma_cube", 40, 2, 4), spawner("minecraft:ghast", 20, 1, 1),
           spawner("minecraft:zombified_piglin", 30, 2, 4)],
          [spawner("magma_crab", 35, 2, 4), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("ore_sulfur_rock"), mid("ore_sulfur")],
          [mid("sulfur_cluster_patch")])
    biome("ember_meadow", "#6a2e16", "minecraft:spore_blossom_air", 0.01, "crimson_forest", "crimson_forest",
          [spawner("minecraft:zombified_piglin", 10, 2, 3), spawner("minecraft:magma_cube", 3, 1, 2)],
          [spawner("ember_cow", 40, 2, 4), spawner("cinder_hen", 35, 2, 4), strider],
          vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("meadow_ember_fungus"), mid("meadow_vegetation"), mid("wild_cinder_wheat")],
          ambient_mobs=[spawner("soul_moth", 6, 1, 2)])
    biome("soulfire_marsh", "#0f3a44", "minecraft:soul", 0.004, "soul_sand_valley", "soul_sand_valley",
          [spawner("minecraft:ghast", 30, 1, 1), spawner("minecraft:skeleton", 15, 2, 3), spawner("ash_wraith", 10, 1, 1),
           spawner("minecraft:enderman", 1, 2, 4)],
          [strider],
          vanilla("patch_fire", "ore_gravel_nether", "ore_blackstone"),
          [mid("soul_lily_patch"), mid("glowcap_patch")],
          ambient_mobs=[spawner("soul_moth", 30, 2, 4)])
    biome("withered_forest", "#2a2a2e", "minecraft:ash", 0.05, "warped_forest", "warped_forest",
          [spawner("withered_knight", 50, 1, 3), spawner("minecraft:wither_skeleton", 5, 1, 2),
           spawner("minecraft:enderman", 5, 1, 2)],
          [strider],
          vanilla("patch_soul_fire", "ore_gravel_nether"),
          [mid("withered_fungus"), mid("withered_vegetation"), mid("glowcap_patch")],
          ambient_mobs=[spawner("soul_moth", 8, 1, 2)])
    biome("magma_rift", "#7a1e06", "minecraft:lava", 0.004, "basalt_deltas", "basalt_deltas",
          [spawner("minecraft:magma_cube", 50, 2, 4), spawner("fire_ghast", 25, 1, 1), spawner("onyx_cube", 15, 1, 2),
           spawner("minecraft:blaze", 10, 1, 2),
           spawner("ember_stalker", 10, 1, 1), spawner("minecraft:ghast", 10, 1, 1)],
          [spawner("basalt_beetle", 25, 1, 3), strider],
          ["minecraft:small_basalt_columns", "minecraft:large_basalt_columns"]
          + vanilla("patch_soul_fire", "ore_gravel_nether", "ore_blackstone") + [mid("magma_delta")],
          [])


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
        spawner("ember_stalker", 8, 1, 2), spawner("ash_wraith", 6, 1, 1), spawner("withered_knight", 6, 1, 2),
        spawner("fire_ghast", 10, 1, 1), spawner("onyx_cube", 8, 1, 3),
        spawner("magma_crab", 6, 1, 3), spawner("basalt_beetle", 4, 1, 2), spawner("ember_cow", 8, 2, 3),
        spawner("cinder_hen", 8, 2, 3), spawner("soul_moth", 6, 1, 3)]})


STRUCTURES = {
    # nom : (espacement, separation, sel, air requis, y min, enfoncement, emprise, adaptation au terrain)
    # Espacements reduits : un donjon tous les ~300 blocs environ, une petite structure tous les ~130 blocs.
    "onyx_citadel": (20, 8, 761234019, 10, 33, 0, 44, "beard_box"),
    "infernal_sanctum": (20, 8, 663901247, 10, 33, 0, 40, "beard_box"),
    "withered_catacombs": (18, 7, 518230457, 8, 45, 12, 38, "beard_box"),
    "traveler_camp": (9, 3, 734029118, 6, 33, 0, 12, "beard_thin"),
    "nether_garden": (10, 4, 192837465, 6, 33, 0, 14, "beard_thin"),
    "hidden_cache": (8, 3, 556172839, 4, 40, 6, 6, "beard_thin"),
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
    generate_biomes()
    generate_biome_modifiers()
    generate_structures()
