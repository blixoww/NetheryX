"""Equilibrage 1.2.1 : butins des coffres nettement reduits (les anciens etaient trop genereux)."""
from .common import mid
from .data import book, chest_table, count, enchanted, item, loot_modifier, pool

EMPTY = {"type": "minecraft:empty"}


def empty(weight):
    return dict(EMPTY, weight=weight)


def generate():
    # Petites structures : surtout des ressources de base, un objet rare de temps en temps
    chest_table("small_treasure", [
        pool(1, 1, [
            enchanted("minecraft:iron_pickaxe", 5, 15, 8),
            enchanted("minecraft:iron_sword", 5, 15, 8),
            enchanted("minecraft:diamond_pickaxe", 5, 15, 2),
            item("minecraft:golden_apple", weight=3),
            item("soul_compass", weight=2),
            item("recall_stone", weight=1),
            empty(10),
        ]),
        pool(3, 5, [
            item("onyx_shard", weight=8, functions=[count(1, 3)]),
            item("minecraft:diamond", weight=2),
            item("minecraft:gold_ingot", weight=12, functions=[count(1, 4)]),
            item("minecraft:iron_ingot", weight=12, functions=[count(1, 4)]),
            item("cooked_infernal_beef", weight=10, functions=[count(1, 4)]),
            item("cinder_bread", weight=10, functions=[count(1, 3)]),
            item("sulfur", weight=8, functions=[count(2, 5)]),
            item("minecraft:arrow", weight=8, functions=[count(4, 10)]),
            book(2),
        ]),
    ])
    # Coffres des tours / salles des donjons
    for name, flavour in (("onyx_citadel", [item("onyx_shard", weight=12, functions=[count(2, 5)]), item("soul_compass", weight=2)]),
                          ("infernal_sanctum", [item("minecraft:blaze_rod", weight=10, functions=[count(1, 4)]),
                                                item("minecraft:magma_cream", weight=10, functions=[count(1, 3)]),
                                                item("glimmer_elixir", weight=4)])):
        chest_table(name, [
            pool(1, 1, [
                enchanted("onyx_sword", 5, 20, 2),
                item("magma_treads", weight=2),
                item("minecraft:golden_apple", weight=5),
                item("recall_stone", weight=2),
                empty(12),
            ]),
            pool(3, 5, flavour + [
                item("onyx", weight=3),
                item("minecraft:diamond", weight=3),
                item("minecraft:gold_ingot", weight=12, functions=[count(2, 5)]),
                item("minecraft:ancient_debris", weight=1),
                book(4),
            ]),
        ])
    chest_table("withered_catacombs", [
        pool(3, 5, [
            item("withered_bone", weight=16, functions=[count(1, 4)]),
            item("soul_dust", weight=12, functions=[count(1, 3)]),
            item("onyx_shard", weight=10, functions=[count(1, 3)]),
            item("onyx", weight=2),
            item("minecraft:gold_ingot", weight=10, functions=[count(1, 4)]),
            item("minecraft:iron_ingot", weight=10, functions=[count(1, 4)]),
            item("wraith_essence", weight=6, functions=[count(1, 2)]),
            enchanted("minecraft:iron_sword", 5, 15, 3),
            book(3),
        ]),
    ])
    # Tresors des boss : un objet unique garanti, le reste reste raisonnable
    common_unique = [item("minecraft:netherite_upgrade_smithing_template", weight=4), item("minecraft:enchanted_golden_apple", weight=1),
                     item("onyx_infusion_template", weight=4)]
    vaults = {
        "onyx_citadel_vault": [item("soul_scythe", weight=4), item("onyx_hammer", weight=4), item("sentinel_core", weight=3)],
        "infernal_sanctum_vault": [item("warlock_staff", weight=5), item("magma_treads", weight=4),
                                   item("minecraft:snout_armor_trim_smithing_template", weight=2)],
        "withered_catacombs_vault": [item("withered_blade", weight=5), item("soul_scythe", weight=3),
                                     item("minecraft:music_disc_pigstep", weight=2), item("minecraft:wither_skeleton_skull", weight=3)],
    }
    for name, uniques in vaults.items():
        chest_table(name, [
            pool(1, 1, uniques + common_unique),
            pool(1, 1, [
                enchanted("onyx_sword", 20, 30, 3, True), enchanted("onyx_pickaxe", 20, 30, 3, True),
                enchanted("onyx_chestplate", 20, 30, 2, True), enchanted("minecraft:book", 20, 30, 4, True), empty(4),
            ]),
            pool(3, 5, [
                item("onyx", weight=8, functions=[count(1, 3)]),
                item("minecraft:netherite_scrap", weight=3),
                item("minecraft:ancient_debris", weight=4),
                item("minecraft:diamond", weight=6, functions=[count(1, 3)]),
                item("minecraft:gold_ingot", weight=10, functions=[count(2, 6)]),
                item("minecraft:golden_apple", weight=5),
                item("minecraft:experience_bottle", weight=6, functions=[count(2, 6)]),
            ]),
        ])
    chest_table("mimic", [
        pool(1, 1, [item("onyx_infusion_template", weight=2), item("minecraft:diamond", weight=5, functions=[count(1, 2)]),
                    item("onyx", weight=5), item("minecraft:golden_apple", weight=6)]),
        pool(2, 4, [
            item("minecraft:gold_ingot", weight=12, functions=[count(2, 5)]),
            item("onyx_shard", weight=10, functions=[count(1, 4)]),
            item("minecraft:experience_bottle", weight=6, functions=[count(2, 5)]),
            book(3),
        ]),
    ])
    # Ajouts aux coffres vanilla, plus rares
    loot_modifier("bastion_treasure_onyx", "minecraft:chests/bastion_treasure", "onyx", 0.4, 1, 2)
    loot_modifier("bastion_other_onyx_shard", "minecraft:chests/bastion_other", "onyx_shard", 0.35, 1, 3)
    loot_modifier("bastion_treasure_infusion_template", "minecraft:chests/bastion_treasure", "onyx_infusion_template", 0.2)
    loot_modifier("bastion_other_infusion_template", "minecraft:chests/bastion_other", "onyx_infusion_template", 0.04)
