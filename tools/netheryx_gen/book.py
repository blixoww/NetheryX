"""Codex NetheryX : livre-guide Patchouli bilingue (anglais / francais), choisi selon la langue du jeu."""
from .common import MOD_ID, ASSETS, data_path, mid, write_json

BOOK = "codex"
LANGS = ("en_us", "fr_fr")


def T(en, fr):
    return {"en_us": en, "fr_fr": fr}


def text(en, fr, title=None):
    page = {"type": "patchouli:text", "text": T(en, fr)}
    if title:
        page["title"] = title
    return page


def spot(item, en, fr, link=True):
    return {"type": "patchouli:spotlight", "item": mid(item), "text": T(en, fr), "link_recipe": link}


def craft(recipe, recipe2=None, en=None, fr=None):
    page = {"type": "patchouli:crafting", "recipe": mid(recipe)}
    if recipe2:
        page["recipe2"] = mid(recipe2)
    if en:
        page["text"] = T(en, fr)
    return page


def smelt(recipe, recipe2=None):
    page = {"type": "patchouli:smelting", "recipe": mid(recipe)}
    if recipe2:
        page["recipe2"] = mid(recipe2)
    return page


def cut(recipe, recipe2=None):
    page = {"type": "patchouli:stonecutting", "recipe": mid(recipe)}
    if recipe2:
        page["recipe2"] = mid(recipe2)
    return page


def mob(entity, en, fr, scale=1.0, offset=0.0):
    return {"type": "patchouli:entity", "entity": mid(entity), "scale": scale, "offset": offset, "text": T(en, fr)}


CATEGORIES = [
    ("getting_started", "minecraft:book", T("Getting Started", "Premiers pas"),
     T("What NetheryX is and how to use this codex.", "Ce qu'est NetheryX et comment utiliser ce codex.")),
    ("onyx", "netheryx:onyx", T("Onyx", "Onyx"),
     T("The black gem stronger than diamond.", "La gemme noire plus solide que le diamant.")),
    ("biomes", "netheryx:ember_nylium", T("Biomes", "Biomes"),
     T("Twelve new Nether biomes, and changes to the vanilla ones.", "Douze nouveaux biomes du Nether, et les changements des biomes vanilla.")),
    ("creatures", "netheryx:ember_stalker_spawn_egg", T("Creatures", "Créatures"),
     T("Friends, foes and bosses of the new Nether.", "Amis, ennemis et boss du nouveau Nether.")),
    ("structures", "netheryx:onyx_bricks", T("Dungeons", "Donjons"),
     T("Great dungeons, their bosses, camps and caravans.", "Grands donjons, boss, campements et caravanes.")),
    ("equipment", "netheryx:onyx_sword", T("Tools & Armor", "Outils et armures"),
     T("Weapons, tools and armor.", "Armes, outils et armures.")),
    ("utility", "netheryx:soul_compass", T("Useful Items", "Objets utiles"),
     T("Items that make Nether life easier.", "Des objets qui facilitent la vie dans le Nether.")),
    ("farming", "netheryx:blaze_pepper", T("Farming & Food", "Cultures et nourriture"),
     T("Nether crops and recipes.", "Cultures du Nether et recettes.")),
    ("building", "netheryx:ember_planks", T("Building Blocks", "Blocs de construction"),
     T("Woods, bricks and lights.", "Bois, briques et éclairages.")),
]

ENTRIES = []


def entry(category, name_en, name_fr, icon, pages, priority=False):
    ENTRIES.append({"category": category, "name": T(name_en, name_fr), "icon": mid(icon), "pages": pages, "priority": priority})


# ---------------------------------------------------------------- Premiers pas
entry("getting_started", "Welcome to NetheryX", "Bienvenue dans NetheryX", "minecraft:book", [
    text("NetheryX overhauls the Nether: $(l)12 new biomes$(), $(l)19 creatures$() including three bosses, $(l)3 great dungeons$() and small camps, "
         "the $(item)Onyx$() material, new crops, foods, tools and many useful items.$(br2)"
         "The vanilla biomes change too: new plants and creatures appear everywhere.",
         "NetheryX transforme le Nether : $(l)12 nouveaux biomes$(), $(l)19 créatures$() dont trois boss, $(l)3 grands donjons$() et de petits campements, "
         "le matériau $(item)Onyx$(), de nouvelles cultures, nourritures, outils et objets utiles.$(br2)"
         "Les biomes vanilla changent aussi : nouvelles plantes et créatures un peu partout.", "NetheryX"),
    text("This codex is given once to every player. Lost it? Craft a new one with a $(item)Book$() and a $(item)Netherrack$() "
         "block.$(br2)It follows your game language (English or French).",
         "Ce codex est offert une fois à chaque joueur. Perdu ? Fabriquez-en un nouveau avec un $(item)Livre$() et un bloc de "
         "$(item)Netherrack$().$(br2)Il suit la langue de votre jeu (anglais ou français).", "Le Codex"),
    craft("codex", en="Book + Netherrack.", fr="Livre + Netherrack."),
], priority=True)
entry("getting_started", "Where to Start", "Par où commencer", "minecraft:flint_and_steel", [
    text("1. Enter the Nether and look for the $(l)coloured biomes$(): orange groves, grey ash, blue marshes...$(br)"
         "2. Mine $(item)Sulfur$() and $(item)Onyx$() (diamond pickaxe).$(br)"
         "3. Raid a $(item)Nether Fortress$() for blaze rods: Onyx tools use them as handles.$(br)"
         "4. Craft a $(item)Soul Compass$() to find structures.",
         "1. Entrez dans le Nether et cherchez les $(l)biomes colorés$() : bosquets orange, cendres grises, marais bleus...$(br)"
         "2. Minez du $(item)Soufre$() et de l'$(item)Onyx$() (pioche en diamant).$(br)"
         "3. Pillez une $(item)Forteresse du Nether$() pour les bâtons de blaze : ils servent de manche aux outils en onyx.$(br)"
         "4. Fabriquez une $(item)Boussole des âmes$() pour trouver les structures."),
    text("Three dungeons, three bosses, from easiest to hardest:$(li)$(l)Infernal Sanctum$(): Infernal Warlock$(li)$(l)Withered "
         "Catacombs$(): Withered Champion$(li)$(l)Onyx Citadel$(): Onyx Sentinel (drops the $(item)Sentinel Core$() for the "
         "$(item)Infernal Heart$())",
         "Trois donjons, trois boss, du plus facile au plus difficile :$(li)$(l)Sanctuaire infernal$() : Sorcier infernal$(li)"
         "$(l)Catacombes flétries$() : Champion flétri$(li)$(l)Citadelle d'onyx$() : Sentinelle d'onyx (donne le $(item)Cœur de "
         "sentinelle$() du $(item)Cœur infernal$())", "Progression"),
])
entry("getting_started", "Configuration", "Configuration", "minecraft:comparator", [
    text("File $(l)config/netheryx-common.toml$():$(li)biome region weight$(li)Onyx set bonus$(li)hammer 3x3 mining"
         "$(li)compass search radius$(li)bombs breaking blocks$(li)boss health$(li)Recall Stone cooldown$(li)giving this codex",
         "Fichier $(l)config/netheryx-common.toml$() :$(li)poids de la région de biomes$(li)bonus d'armure en onyx$(li)minage 3x3 du marteau"
         "$(li)rayon de la boussole$(li)destruction de blocs par les bombes$(li)points de vie des boss$(li)recharge de la Pierre de rappel"
         "$(li)remise de ce codex"),
])

# ---------------------------------------------------------------- Onyx
entry("onyx", "Onyx Ore", "Minerai d'onyx", "netheryx:onyx_ore", [
    spot("onyx_ore", "As common as diamond, everywhere in the Nether between Y=5 and Y=56, even more in the $(l)Onyx Depths$(). "
                     "Needs a $(item)diamond pickaxe$(). Fortune works.",
         "Aussi fréquent que le diamant, partout dans le Nether entre Y=5 et Y=56, et plus encore dans les $(l)Profondeurs d'onyx$(). "
         "Nécessite une $(item)pioche en diamant$(). Fortune fonctionne.", link=False),
    smelt("onyx_from_smelting", "onyx_from_blasting"),
], priority=True)
entry("onyx", "Onyx Gem", "Gemme d'onyx", "netheryx:onyx", [
    spot("onyx", "Black gem between diamond and netherite. Used for tools, armor, the $(item)Onyx Lantern$() and the "
                 "$(item)Infernal Heart$(). Accepted by beacons.",
         "Gemme noire entre le diamant et la netherite. Sert aux outils, armures, à la $(item)Lanterne d'onyx$() et au "
         "$(item)Cœur infernal$(). Acceptée par les balises."),
    craft("onyx_from_shards", "onyx_block", en="9 Onyx Shards (from Onyx Clusters) also make a gem.",
          fr="9 éclats d'onyx (des amas d'onyx) donnent aussi une gemme."),
    spot("onyx_cluster", "Glowing crystals of the Onyx Depths. Mine them for $(item)Onyx Shards$() (Silk Touch keeps the cluster).",
         "Cristaux lumineux des Profondeurs d'onyx. Ils donnent des $(item)éclats d'onyx$() (Toucher de soie garde l'amas).", link=False),
])
entry("onyx", "Onyx Tools", "Outils en onyx", "netheryx:onyx_pickaxe", [
    text("$(l)Stronger than netherite$(): durability $(l)4500$() (netherite 2031), speed $(l)10$() (9), damage bonus $(l)5$() (4): an onyx sword deals $(l)9$(). The hammer has 7000. Handles are $(item)Blaze Rods$(). "
         "Onyx items do not burn in lava.",
         "$(l)Plus fort que la netherite$() : durabilité $(l)4500$() (netherite 2031), vitesse $(l)10$() (9), bonus de dégâts $(l)5$() (4) : une épée en onyx inflige $(l)9$(). Le marteau : 7000. Les manches sont des "
         "$(item)bâtons de blaze$(). Les objets en onyx ne brûlent pas dans la lave."),
    craft("onyx_sword", "onyx_pickaxe"),
    craft("onyx_axe", "onyx_shovel"),
    craft("onyx_hoe"),
])
entry("onyx", "Onyx Bow", "Arc en onyx", "netheryx:onyx_bow", [
    spot("onyx_bow", "Arrows deal $(l)50% more damage +1$() and are set on fire. 2400 durability, enchantable like Onyx, "
                     "repaired with Onyx.",
         "Les flèches infligent $(l)50 % de dégâts en plus +1$() et sont enflammées. 2400 de durabilité, enchantable comme l'onyx, "
         "se répare avec de l'onyx."),
    craft("onyx_bow"),
])
entry("onyx", "Onyx Hammer", "Marteau en onyx", "netheryx:onyx_hammer", [
    spot("onyx_hammer", "Mines a $(l)3x3$() area facing you. Sneak to mine a single block. Respects claim and protection mods.",
         "Mine une zone de $(l)3x3$() face à vous. Accroupissez-vous pour un seul bloc. Respecte les mods de protection."),
    craft("onyx_hammer"),
])
entry("onyx", "Onyx Armor", "Armure en onyx", "netheryx:onyx_chestplate", [
    text("Better than netherite: protection $(l)4/9/7/4$() (netherite 3/8/6/3), toughness $(l)4$() (3), durability 55x (37x). "
         "$(l)Full set bonus$(): permanent $(item)Resistance I$() and $(item)Fire Resistance$(). Works with armor trims.",
         "Mieux que la netherite : protection $(l)4/9/7/4$() (netherite 3/8/6/3), robustesse $(l)4$() (3), durabilité 55x (37x). $(l)Bonus d'ensemble$() : "
         "$(item)Résistance I$() et $(item)Résistance au feu$() permanentes. Compatible avec les garnitures d'armure."),
    craft("onyx_helmet", "onyx_chestplate"),
    craft("onyx_leggings", "onyx_boots"),
])
entry("onyx", "Onyx Blocks", "Blocs d'onyx", "netheryx:onyx_bricks", [
    craft("polished_onyx", "onyx_bricks"),
    craft("onyx_brick_stairs", "onyx_brick_slab"),
    craft("onyx_brick_wall"),
    cut("onyx_brick_stairs_from_onyx_bricks_stonecutting", "onyx_brick_slab_from_onyx_bricks_stonecutting"),
])

# ---------------------------------------------------------------- Biomes
BIOME_PAGES = [
    ("Ember Grove", "Bosquet de braise", "netheryx:ember_nylium",
     "Orange nylium, giant $(item)Ember Fungi$(), glowing roots, wild $(item)Blaze Peppers$() and $(item)Fire Berries$().$(br2)"
     "Creatures: $(l)Ember Stalkers$(). Dungeon: $(l)Infernal Sanctum$().",
     "Nylium orange, $(item)champignons de braise$() géants, racines lumineuses, $(item)piments de blaze$() et $(item)baies de feu$() "
     "sauvages.$(br2)Créatures : $(l)Traqueurs de braise$(). Donjon : $(l)Sanctuaire infernal$()."),
    ("Ashen Wastes", "Terres cendrées", "netheryx:ash_block",
     "Grey ash dunes over basalt, charred shrubs and a constant fall of ash.$(br2)Creatures: $(l)Ash Wraiths$(), ghasts. "
     "Dungeon: $(l)Withered Catacombs$().",
     "Dunes de cendre grise sur du basalte, arbustes calcinés et pluie de cendres.$(br2)Créatures : $(l)Spectres de cendre$(), ghasts. "
     "Donjon : $(l)Catacombes flétries$()."),
    ("Onyx Depths", "Profondeurs d'onyx", "netheryx:onyx_cluster",
     "A dark biome of blackstone and basalt with glowing $(item)Onyx Clusters$() and plenty of $(item)Onyx Ore$().$(br2)"
     "Dungeon: the $(l)Onyx Citadel$() and its boss.",
     "Biome sombre de blackstone et de basalte, $(item)amas d'onyx$() lumineux et beaucoup de $(item)minerai d'onyx$().$(br2)"
     "Donjon : la $(l)Citadelle d'onyx$() et son boss."),
    ("Ember Meadow", "Prairie de braise", "netheryx:nether_grass_block",
     "The most welcoming place of the Nether: golden $(item)Nether Grass$(), flowers, glowcaps and wild cinder wheat.$(br2)"
     "Creatures: peaceful $(l)Ember Cows$() and $(l)Cinder Hens$(). Ideal to settle a farm!",
     "L'endroit le plus accueillant du Nether : $(item)herbe du Nether$() dorée, fleurs, luminelles et blé de cendre sauvage.$(br2)"
     "Créatures : $(l)Vaches de braise$() et $(l)Poules de cendre$() paisibles. Idéal pour installer une ferme !"),
    ("Sulfur Springs", "Sources sulfureuses", "netheryx:sulfur_rock",
     "Yellow sulfur rock, $(item)Sulfur Ore$() and crystal clusters.$(br2)Creatures: peaceful $(l)Magma Crabs$(). "
     "Dungeon: $(l)Infernal Sanctum$().",
     "Roche sulfureuse jaune, $(item)minerai de soufre$() et amas de cristaux.$(br2)Créatures : $(l)Crabes de magma$() paisibles. "
     "Donjon : $(l)Sanctuaire infernal$()."),
    ("Soulfire Marsh", "Marais des âmes", "netheryx:soul_lily",
     "Soul soil plains lit by blue fire and glowing $(item)Soul Lilies$().$(br2)Creatures: $(l)Soul Moths$(), skeletons. "
     "Dungeon: $(l)Withered Catacombs$().",
     "Plaines de terre des âmes éclairées par le feu bleu et des $(item)lys des âmes$() lumineux.$(br2)Créatures : "
     "$(l)Phalènes des âmes$(), squelettes. Structure : $(l)Ruines des âmes$()."),
    ("Withered Forest", "Forêt flétrie", "netheryx:withered_stem",
     "Grey giant fungi with bone decorations on $(item)Withered Soil$().$(br2)Creatures: $(l)Withered Knights$(), wither skeletons. "
     "Dungeon: $(l)Withered Catacombs$().",
     "Champignons géants gris ornés d'os sur la $(item)terre flétrie$().$(br2)Créatures : $(l)Chevaliers flétris$(), squelettes wither. "
     "Donjon : $(l)Catacombes flétries$()."),
    ("Magma Rift", "Faille de magma", "minecraft:magma_block",
     "Lava deltas and magma floors. Watch your step!$(br2)Creatures: $(l)Basalt Beetles$(), blazes, magma cubes. "
     "Dungeon: the $(l)Infernal Sanctum$().",
     "Deltas de lave et sols de magma. Attention où vous marchez !$(br2)Créatures : $(l)Scarabées de basalte$(), "
     "blazes, cubes de magma. Structure : le $(l)Sanctuaire infernal$()."),
]
for en, fr, icon, ten, tfr in BIOME_PAGES:
    entry("biomes", en, fr, icon, [text(ten, tfr, fr if False else None)])
entry("biomes", "Vanilla Biomes", "Biomes vanilla", "minecraft:netherrack", [
    text("NetheryX also decorates the vanilla Nether:$(li)$(l)Nether Wastes$(): charred shrubs$(li)$(l)Crimson Forest$(): "
         "fire berries, ember stalkers$(li)$(l)Warped Forest$(): soul lilies, soul moths$(li)$(l)Soul Sand Valley$(): lilies, "
         "soul tubers, knights$(li)$(l)Basalt Deltas$(): sulfur, crabs$(li)Onyx ore everywhere",
         "NetheryX décore aussi le Nether vanilla :$(li)$(l)Désolation du Nether$() : arbustes calcinés$(li)$(l)Forêt "
         "carmin$() : baies de feu, traqueurs$(li)$(l)Forêt biscornue$() : lys et phalènes des âmes$(li)$(l)Vallée des âmes$() : lys, "
         "tubercules, chevaliers$(li)$(l)Deltas de basalte$() : soufre, crabes$(li)Minerai d'onyx partout"),
])

# ---------------------------------------------------------------- Creatures
CREATURES = [
    ("ember_stalker", "Ember Stalker", "Traqueur de braise", 1.0,
     "Hostile feline (30 HP). Pounces and sets you on fire; when hit it may vanish in a $(l)cloud of smoke$(). Drops $(item)Stalker Meat$().",
     "Félin hostile (30 PV). Bondit et vous enflamme ; touché, il peut disparaître dans un $(l)nuage de fumée$(). Lâche de la $(item)viande de traqueur$()."),
    ("ash_wraith", "Ash Wraith", "Spectre de cendre", 0.8,
     "Flying spirit (22 HP). Its touch causes $(item)Blindness$() and $(item)Weakness$(). Drops $(item)Wraith Essence$().",
     "Esprit volant (22 PV). Son contact inflige $(item)Cécité$() et $(item)Faiblesse$(). Lâche de l'$(item)essence de spectre$()."),
    ("magma_crab", "Magma Crab", "Crabe de magma", 1.0,
     "Peaceful. Breed with $(item)Fire Berries$(). Drops $(item)Crab Legs$() and sometimes a $(item)Magma Shell$().",
     "Paisible. S'élève avec des $(item)baies de feu$(). Lâche des $(item)pattes de crabe$() et parfois une $(item)carapace de magma$()."),
    ("soul_moth", "Soul Moth", "Phalène des âmes", 1.2,
     "Harmless glowing insect of the Soulfire Marsh. Drops $(item)Soul Dust$().",
     "Insecte lumineux et inoffensif du Marais des âmes. Lâche de la $(item)poussière d'âme$()."),
    ("basalt_beetle", "Basalt Beetle", "Scarabée de basalte", 0.7,
     "Neutral, heavily armored (30 HP, 10 armor). The whole herd charges if one is hit. Breed with $(item)Magma Cream$(). "
     "Drops $(item)Basalt Carapace$().",
     "Neutre et très cuirassé (30 PV, 10 d'armure). Tout le troupeau charge si l'un est frappé. S'élève avec de la $(item)crème de "
     "magma$(). Lâche des $(item)carapaces de basalte$()."),
    ("withered_knight", "Withered Knight", "Chevalier flétri", 0.6,
     "Undead warrior (34 HP, 8 armor). Its blade inflicts $(item)Wither$(). Rarely drops the $(item)Withered Blade$() or a "
     "wither skeleton skull.",
     "Guerrier mort-vivant (34 PV, 8 d'armure). Sa lame inflige le $(item)Flétrissement$(). Lâche rarement la $(item)Lame flétrie$() "
     "ou un crâne de squelette wither."),
    ("infernal_warlock", "Infernal Warlock (boss)", "Sorcier infernal (boss)", 0.6,
     "150 HP. Fires volleys of fireballs, teleports when hit, unleashes a $(l)flame nova$() if you stay close and summons two $(l)Ember Stalkers$() at half health. Drops the "
     "$(item)Warlock Staff$() and a $(item)Recall Stone$().",
     "150 PV. Lance des salves de boules de feu, se téléporte quand on le frappe, déclenche une $(l)nova de flammes$() au contact et invoque deux $(l)Traqueurs$() à mi-vie. "
     "Lâche le $(item)Bâton du sorcier$() et une $(item)Pierre de rappel$()."),
    ("ember_cow", "Ember Cow", "Vache de braise", 0.8,
     "Peaceful. $(l)Milk it$() with a bucket, breed it with $(item)Cinder Wheat$(). Drops Infernal Beef (steak: 10 hunger + "
     "Fire Resistance), leather and sometimes magma cream.",
     "Paisible. Se $(l)trait au seau$(), s'élève avec du $(item)blé de cendre$(). Donne du bœuf infernal (steak : 10 de faim + "
     "Résistance au feu), du cuir et parfois de la crème de magma."),
    ("cinder_hen", "Cinder Hen", "Poule de cendre", 1.4,
     "Peaceful. Lays an $(item)Ember Egg$() every 5-10 minutes, glides when falling. Breed it with Nether seeds.",
     "Paisible. Pond un $(item)œuf de braise$() toutes les 5 à 10 minutes, plane en tombant. S'élève avec des graines du Nether."),
    ("fire_ghast", "Fire Ghast", "Ghast de feu", 0.25,
     "A burning ghast with 30 HP whose fireballs explode harder. Drops fire charges, ghast tears and Onyx shards.",
     "Un ghast embrasé de 30 PV dont les boules de feu explosent plus fort. Lâche des boules de feu, des larmes de ghast et des éclats d'onyx."),
    ("onyx_cube", "Onyx Cube", "Cube d'onyx", 0.6,
     "A crystallized magma cube, more armored. Splits into smaller cubes. Drops magma cream and Onyx shards.",
     "Un cube de magma cristallisé, plus cuirassé. Se divise en cubes plus petits. Lâche de la crème de magma et des éclats d'onyx."),
    ("withered_champion", "Withered Champion (boss)", "Champion flétri (boss)", 0.45,
     "200 HP, 12 armor. A giant knight whose blade withers. It $(l)dashes$() at distant targets and calls two Withered Knights at 66% and 33% health. "
     "Guards the Withered Catacombs.",
     "200 PV, 12 d'armure. Un chevalier géant dont la lame flétrit. Il $(l)fonce$() sur les cibles éloignées et appelle deux Chevaliers flétris à 66 % et 33 % de vie. "
     "Garde les Catacombes flétries."),
    ("onyx_sentinel", "Onyx Sentinel (boss)", "Sentinelle d'onyx (boss)", 0.35,
     "250 HP, immune to knockback. Punches, a $(l)shockwave$(), and below half health a $(l)rage$(): faster, it makes crystal fangs erupt toward you. "
     "Drops the $(item)Sentinel Core$() and lots of Onyx.",
     "250 PV, insensible au recul. Coups, $(l)onde de choc$() et, sous la moitié de sa vie, une $(l)rage$() : plus rapide, elle fait jaillir des crocs de cristal vers vous. "
     "Lâche le $(item)Cœur de sentinelle$() et beaucoup d'onyx."),
]
BOSS_TACTICS = {
    "onyx_sentinel": text(
        "$(li)$(l)Leap$(): jumps on distant players and slams on landing.$(li)$(l)Crystal rain$(): glittering circles appear under "
        "players, crystal fangs burst out 2 seconds later. Move!$(li)$(l)Onyx shell$() (rage): arms crossed, takes 75% less damage "
        "for 4 s. Wait it out.$(li)Below 25%: calls Onyx Cubes.",
        "$(li)$(l)Bond$() : saute sur les joueurs éloignés et frappe le sol à la réception.$(li)$(l)Pluie de cristaux$() : des cercles "
        "scintillants apparaissent sous les joueurs, des crocs jaillissent 2 secondes plus tard. Bougez !$(li)$(l)Carapace d'onyx$() "
        "(rage) : bras croisés, 75 % de dégâts en moins pendant 4 s. Attendez.$(li)Sous 25 % : appelle des Cubes d'onyx.", "Tactique"),
    "infernal_warlock": text(
        "$(li)$(l)Meteor shower$(): a ring of flames marks the ground, fireballs fall into it.$(li)$(l)Blink$(): reappears behind distant "
        "players and explodes in a nova.$(li)At 50% and 25%: summons Ember Stalkers; while they live, his $(l)ember shield$() absorbs "
        "70% of the damage. Kill the stalkers first!",
        "$(li)$(l)Pluie de météores$() : un cercle de flammes marque le sol, des boules de feu y tombent.$(li)$(l)Clignement$() : "
        "réapparaît derrière les joueurs éloignés et explose en nova.$(li)À 50 % et 25 % : invoque des Traqueurs ; tant qu'ils vivent, "
        "son $(l)bouclier de braise$() absorbe 70 % des dégâts. Tuez-les d'abord !", "Tactique"),
    "withered_champion": text(
        "$(li)$(l)Spectral sweep$(): wide arc in front of him, inflicts Wither II. Stay behind him.$(li)$(l)Soul chains$(): pulls back "
        "players who flee and slows them.$(li)$(l)Soul dash$() toward distant targets.$(li)Below 50%: $(l)bone spikes$() burst in two rings "
        "around him. Back away!",
        "$(li)$(l)Balayage spectral$() : grand arc devant lui, inflige Flétrissement II. Restez derrière lui.$(li)$(l)Chaînes d'âmes$() : "
        "ramène les fuyards et les ralentit.$(li)$(l)Ruée des âmes$() vers les cibles éloignées.$(li)Sous 50 % : des $(l)pointes d'os$() "
        "jaillissent en deux anneaux autour de lui. Reculez !", "Tactique"),
}
for eid, en, fr, scale, ten, tfr in CREATURES:
    pages = [mob(eid, ten, tfr, scale=scale)]
    if eid in BOSS_TACTICS:
        pages.append(BOSS_TACTICS[eid])
    entry("creatures", en, fr, f"{eid}_spawn_egg", pages)

# ---------------------------------------------------------------- Structures
DUNGEON_INTRO = text(
    "NetheryX dungeons are multi-room fortresses: guards and monster spawners on the way, side rooms with chests, then the "
    "$(l)boss arena$(). The treasure room is closed by $(item)Guardian Seals$(), unbreakable blocks that only crumble when "
    "the boss dies.$(br2)A dungeon lies roughly every 300 blocks: use the $(item)Soul Compass$() to find one. Bring armor, "
    "food and a $(item)Recall Stone$()!",
    "Les donjons NetheryX sont des forteresses à plusieurs salles : gardes et générateurs de monstres sur le chemin, salles "
    "annexes avec coffres, puis l'$(l)arène du boss$(). La salle au trésor est fermée par des $(item)Sceaux du gardien$(), "
    "des blocs indestructibles qui ne se brisent qu'à la mort du boss.$(br2)Il y a un donjon environ tous les 300 blocs : "
    "la $(item)Boussole des âmes$() vous y mène. Prévoyez armure, nourriture et une "
    "$(item)Pierre de rappel$() !", "Donjons")
entry("structures", "How Dungeons Work", "Fonctionnement des donjons", "netheryx:guardian_seal", [
    DUNGEON_INTRO,
    spot("guardian_seal", "Unbreakable. Breaks by itself when the dungeon boss is defeated.",
         "Indestructible. Se brise tout seul quand le boss du donjon est vaincu.", link=False),
], priority=True)

STRUCTURES = [
    ("Onyx Citadel", "Citadelle d'onyx", "netheryx:onyx_bricks", [
        ("Onyx Depths, Withered Forest, Ashen Wastes, Basalt Deltas, Nether Wastes, Soul Sand Valley. A 45x45 walled fortress. $(l)1.$() The gate is guarded by Withered Knights. $(l)2.$() Four towers "
         "hold Ash Wraith spawners on the ground floor and a chest upstairs (ladder).",
         "Profondeurs d'onyx, Forêt flétrie, Terres cendrées, Deltas de basalte, Désolation du Nether, Vallée des âmes. Forteresse de 45x45. $(l)1.$() La porte est gardée par des Chevaliers flétris. "
         "$(l)2.$() Quatre tours abritent un générateur de Spectres au rez-de-chaussée et un coffre à l'étage (échelle)."),
        ("$(l)3.$() The keep's great hall: Ember Stalker spawners and two more knights. $(l)4.$() The arena of the $(l)Onyx "
         "Sentinel$(). $(l)5.$() Behind the seals: three vault chests (enchanted Onyx gear, netherite, Soul Scythe, totem...).",
         "$(l)3.$() Le grand hall du donjon : générateurs de Traqueurs et deux chevaliers. $(l)4.$() L'arène de la $(l)Sentinelle "
         "d'onyx$(). $(l)5.$() Derrière les sceaux : trois coffres au trésor (équipement en onyx enchanté, netherite, Faux des âmes, "
         "totem...)."),
    ]),
    ("Infernal Sanctum", "Sanctuaire infernal", "netheryx:magma_bricks", [
        ("Magma Rift, Ember Grove, Sulfur Springs, Ember Meadow, Crimson Forest, Nether Wastes. A temple surrounded by a $(l)lava moat$(): cross the bridge, survive the "
         "Blaze altars and the Ember Stalkers of the courtyard.",
         "Faille de magma, Bosquet de braise, Sources sulfureuses, Prairie de braise, Forêt carmin, Désolation du Nether. Un temple entouré de $(l)douves de lave$() : traversez le pont, "
         "survivez aux autels à Blazes et aux Traqueurs de la cour."),
        ("The antechamber has lava channels and two ritual rooms (Magma Cube and Stalker spawners, chests). Then the arena of the "
         "$(l)Infernal Warlock$(), and the sealed treasury (Warlock Staff, Magma Treads, netherite...).",
         "L'antichambre a des canaux de lave et deux salles rituelles (générateurs de cubes de magma et de Traqueurs, coffres). "
         "Ensuite l'arène du $(l)Sorcier infernal$() et le trésor scellé (Bâton du sorcier, Semelles de magma, netherite...)."),
    ]),
    ("Withered Catacombs", "Catacombes flétries", "minecraft:wither_skeleton_skull", [
        ("Withered Forest, Ashen Wastes, Soulfire Marsh, Soul Sand Valley, Warped Forest, Nether Wastes. Only a $(l)mausoleum$() shows on the surface: its "
         "staircase leads 12 blocks down into the catacombs.",
         "Forêt flétrie, Terres cendrées, Marais des âmes, Vallée des âmes, Forêt biscornue, Désolation du Nether. Seul un $(l)mausolée$() est visible en surface : son "
         "escalier descend de 12 blocs dans les catacombes."),
        ("A ring gallery lined with skulls, patrolled by knights, leads to four crypts (spawners + chests). The tomb in the "
         "centre holds the $(l)Withered Champion$(); its treasury lies north, behind the seals.",
         "Une galerie circulaire bordée de crânes et gardée par des chevaliers mène à quatre cryptes (générateurs + coffres). Le "
         "tombeau central abrite le $(l)Champion flétri$() ; son trésor est au nord, derrière les sceaux."),
    ]),
]
for en, fr, icon, pages in STRUCTURES:
    entry("structures", en, fr, icon, [text(ten, tfr) for ten, tfr in pages])
for en, fr, icon, ten, tfr in [
    ("Traveler's Camp", "Campement de voyageur", "minecraft:campfire",
     "Anywhere in the Nether. A tent, a campfire, a chest and a barrel: Onyx, tools, diamonds, food and golden apples.",
     "Partout dans le Nether. Une tente, un feu de camp, un coffre et un tonneau : onyx, outils, diamants, nourriture et pommes d'or."),
    ("Nether Garden", "Jardin du Nether", "netheryx:cinder_wheat",
     "Meadows, groves and forests. A fenced vegetable garden with a hut: seeds, food, golden apples and an Onyx hoe.",
     "Prairies, bosquets et forêts. Un potager clôturé avec une cabane : graines, nourriture, pommes d'or et houe en onyx."),
    ("Hidden Cache", "Cache secrète", "minecraft:soul_lantern",
     "A small cairn with a soul lantern marks a trapdoor: below, a buried room with two treasure chests.",
     "Un petit cairn avec une lanterne des âmes signale une trappe : en dessous, une salle enterrée avec deux coffres au trésor."),
]:
    entry("structures", en, fr, icon, [text(ten, tfr)])
entry("structures", "Vanilla Loot", "Butin vanilla", "minecraft:chest", [
    text("Bastions, fortresses and ruined portals also contain NetheryX loot: Onyx, shards, Soul Compasses, Recall Stones, "
         "Glimmer Elixirs, Magma Shells, Sulfur Bombs and seeds.",
         "Les bastions, forteresses et portails en ruine contiennent aussi du butin NetheryX : onyx, éclats, boussoles des âmes, "
         "Pierres de rappel, élixirs, carapaces de magma, bombes de soufre et graines."),
])

# ---------------------------------------------------------------- Equipement
entry("equipment", "Soul Scythe", "Faux des âmes", "netheryx:soul_scythe", [
    spot("soul_scythe", "Treasure only (Onyx Citadel, Withered Catacombs). Every hit $(l)heals you$() by one heart.",
         "Trésor uniquement (Citadelle d'onyx, Catacombes flétries). Chaque coup vous $(l)soigne$() d'un cœur.", link=False),
])
entry("equipment", "Withered Blade", "Lame flétrie", "netheryx:withered_blade", [
    spot("withered_blade", "Dropped by Withered Knights. Inflicts $(item)Wither II$() for 5 seconds.",
         "Lâchée par les Chevaliers flétris. Inflige $(item)Flétrissement II$() pendant 5 secondes.", link=False),
])
entry("equipment", "Warlock Staff", "Bâton du sorcier", "netheryx:warlock_staff", [
    spot("warlock_staff", "Dropped by the Infernal Warlock. Shoots a $(item)fireball$() (256 uses).",
         "Lâché par le Sorcier infernal. Lance une $(item)boule de feu$() (256 utilisations).", link=False),
])
entry("equipment", "Magma Treads", "Semelles de magma", "netheryx:magma_treads", [
    spot("magma_treads", "Boots that turn lava under your feet into a $(item)crust$() for a few seconds: walk across lava lakes!",
         "Bottes qui changent la lave sous vos pieds en $(item)croûte$() pendant quelques secondes : traversez les lacs de lave !"),
])
entry("equipment", "Carapace Chestplate", "Plastron de carapace", "netheryx:carapace_chestplate", [
    spot("carapace_chestplate", "Made from Basalt Beetle carapaces: 7 armor, toughness 3 and $(l)20% knockback resistance$().",
         "Fait de carapaces de scarabée : 7 d'armure, robustesse 3 et $(l)20 % de résistance au recul$()."),
])

# ---------------------------------------------------------------- Objets utiles
entry("utility", "Soul Compass", "Boussole des âmes", "netheryx:soul_compass", [
    spot("soul_compass", "Right-click to open its $(l)menu$(): it lists every structure (vanilla and modded) that can exist "
                         "in your current dimension, with a search bar. Pick one and the compass finds the nearest.",
         "Clic droit pour ouvrir son $(l)menu$() : il liste toutes les structures (vanilla et moddées) qui peuvent exister dans "
         "votre dimension, avec une barre de recherche. Choisissez-en une et la boussole trouve la plus proche."),
    text("While held, an arrow and the distance show the way in the action bar. The chosen structure is written in the "
         "tooltip. Works in every dimension!",
         "En main, une flèche et la distance indiquent le chemin. La structure choisie est inscrite dans l'infobulle. "
         "Fonctionne dans toutes les dimensions !"),
])
entry("utility", "Recall Stone", "Pierre de rappel", "netheryx:recall_stone", [
    spot("recall_stone", "Hold use for $(l)3 seconds$() to return to a $(l)Nether portal$() you went through. "
         "With several portals, a menu lets you pick one, or your spawn point. Reusable, with a cooldown.",
         "Maintenez l'utilisation $(l)3 secondes$() pour rejoindre un $(l)portail du Nether$() que vous avez emprunté. "
         "Avec plusieurs portails, un menu vous laisse choisir, ou votre point de réapparition. Réutilisable, avec un temps de recharge."),
    text("The stone remembers the last $(l)8 portals$() you arrived through, in every dimension. Use the $(l)x$() button to forget one. "
         "A destroyed portal is forgotten automatically. Without any portal, the stone brings you back to your bed or anchor.",
         "La pierre retient les $(l)8 derniers portails$() par lesquels vous êtes arrivé, dans toutes les dimensions. Le bouton $(l)x$() "
         "en oublie un. Un portail détruit est oublié automatiquement. Sans portail, la pierre vous ramène à votre lit ou ancre."),
    craft("recall_stone"),
])
entry("utility", "Obsidian Boat", "Bateau d'obsidienne", "netheryx:obsidian_boat", [
    spot("obsidian_boat", "A boat that sails on $(l)lava$() as well as water. It never breaks, even after a fall, and goes "
         "$(l)straight back to your inventory$() when you punch it. Passengers are protected from the heat.",
         "Un bateau qui navigue sur la $(l)lave$() comme sur l'eau. Il ne se brise jamais, même après une chute, et revient "
         "$(l)directement dans l'inventaire$() quand on le frappe. Ses passagers sont protégés de la chaleur."),
    craft("obsidian_boat"),
])
entry("utility", "Obsidian Bucket", "Seau d'obsidienne", "netheryx:obsidian_bucket", [
    spot("obsidian_bucket", "Removes a source block of $(l)any fluid$() (lava, water, modded fluids, waterlogged blocks) "
         "and never fills up. Perfect to dry a lava lake before building.",
         "Supprime une source de $(l)n'importe quel fluide$() (lave, eau, fluides d'autres mods, blocs inondés) "
         "sans jamais se remplir. Parfait pour assécher un lac de lave avant de construire."),
    craft("obsidian_bucket"),
])
entry("utility", "Infernal Heart", "Cœur infernal", "netheryx:infernal_heart", [
    spot("infernal_heart", "While in your inventory, grants $(item)Fire Resistance$() in the Nether. Needs the boss' $(item)Sentinel Core$().",
         "Tant qu'il est dans l'inventaire, donne la $(item)Résistance au feu$() dans le Nether. Demande le $(item)Cœur de sentinelle$() du boss."),
])
entry("utility", "Glimmer Elixir", "Élixir de lueur", "netheryx:glimmer_elixir", [
    spot("glimmer_elixir", "Drink for $(item)Night Vision$() (3 min) and $(item)Fire Resistance$() (30 s).",
         "À boire : $(item)Vision nocturne$() (3 min) et $(item)Résistance au feu$() (30 s)."),
])
entry("utility", "Sulfur", "Soufre", "netheryx:sulfur", [
    spot("sulfur", "Use on Nether crops (including $(item)Nether Wart$()) as fertilizer. Also crafts gunpowder and fire charges.",
         "S'utilise comme engrais sur les cultures du Nether (y compris la $(item)verrue du Nether$()). Donne aussi de la poudre à "
         "canon et des boules de feu."),
    craft("gunpowder_from_sulfur", "fire_charge_from_sulfur"),
    smelt("sulfur_from_smelting"),
])
entry("utility", "Sulfur Bomb", "Bombe de soufre", "netheryx:sulfur_bomb", [
    spot("sulfur_bomb", "Throwable explosive, great for digging through netherrack. Also works in dispensers.",
         "Explosif à lancer, idéal pour creuser la netherrack. Fonctionne aussi dans les distributeurs."),
])
entry("utility", "Treasure Materials", "Matériaux de trésor", "netheryx:wraith_essence", [
    text("$(item)Wraith Essence$(): Soul Compass.$(br)$(item)Magma Shell$(): Magma Treads.$(br)$(item)Basalt Carapace$(): "
         "Carapace Chestplate.$(br)$(item)Soul Dust$(): Recall Stone, Glimmer Elixir.$(br)$(item)Withered Bone$(): bone meal, black dye."
         "$(br)$(item)Sentinel Core$(): Infernal Heart.",
         "$(item)Essence de spectre$() : Boussole des âmes.$(br)$(item)Carapace de magma$() : Semelles de magma.$(br)$(item)Carapace de "
         "basalte$() : Plastron de carapace.$(br)$(item)Poussière d'âme$() : Pierre de rappel, Élixir de lueur.$(br)$(item)Os flétri$() : "
         "poudre d'os, teinture noire.$(br)$(item)Cœur de sentinelle$() : Cœur infernal."),
    craft("bone_meal_from_withered_bone", "black_dye_from_withered_bone"),
])

# ---------------------------------------------------------------- Cultures
entry("farming", "Blaze Pepper", "Piment de blaze", "netheryx:blaze_pepper", [
    spot("blaze_pepper", "Grows on $(item)Ember Nylium$() or $(item)Soul Soil$(), no light needed. Eating it gives $(item)Fire "
                         "Resistance$() (30 s).",
         "Pousse sur le $(item)nylium de braise$() ou la $(item)terre des âmes$(), sans lumière. Le manger donne la $(item)Résistance au "
         "feu$() (30 s).", link=False),
    craft("blaze_pepper_seeds"),
])
entry("farming", "Soul Tuber", "Tubercule d'âme", "netheryx:soul_tuber", [
    spot("soul_tuber", "Plant on $(item)Soul Sand$() or $(item)Soul Soil$(). Found wild in the Soul Sand Valley. Bake it!",
         "Se plante sur le $(item)sable$() ou la $(item)terre des âmes$(). Pousse à l'état sauvage dans la Vallée des âmes. À cuire !", link=False),
    smelt("baked_soul_tuber_from_smelting"),
])
entry("farming", "Fire Berries", "Baies de feu", "netheryx:fire_berries", [
    spot("fire_berries", "Bush that grows on most Nether soils. Harvest with right click. Feeds and breeds Magma Crabs.",
         "Buisson qui pousse sur la plupart des sols du Nether. Récolte au clic droit. Nourrit et fait se reproduire les crabes de magma.",
         link=False),
])
entry("farming", "Cinder Wheat", "Blé de cendre", "netheryx:cinder_wheat", [
    spot("cinder_wheat", "Seeds come from breaking $(item)Nether Grass$() (or wild wheat in the Ember Meadow). Grows on nylium, "
                         "Nether grass or soul soil without light. Makes bread and feeds Ember Cows.",
         "Les graines s'obtiennent en cassant de l'$(item)herbe du Nether$() (ou du blé sauvage de la Prairie de braise). Pousse sur "
         "le nylium, l'herbe du Nether ou la terre des âmes sans lumière. Donne du pain et nourrit les vaches de braise.", link=False),
    craft("cinder_bread", "cinder_wheat_seeds"),
])
entry("farming", "Glowcap", "Luminelle", "netheryx:glowcap", [
    spot("glowcap", "A glowing mushroom found all over the Nether. Eat it for a short Night Vision, or cook a stew.",
         "Un champignon lumineux présent dans tout le Nether. À manger pour une courte vision nocturne, ou en ragoût.", link=False),
    craft("glowcap_stew"),
])
entry("farming", "Flowers", "Fleurs", "netheryx:ember_bloom", [
    text("Four Nether flowers grow in the meadow and across the vanilla biomes: $(item)Ember Bloom$(), $(item)Ash Daisy$(), "
         "$(item)Crimson Bellflower$() and $(item)Sulfur Tulip$(). They give dyes and fit in flower pots (like Soul Lilies, "
         "Glowcaps and fungi).",
         "Quatre fleurs du Nether poussent dans la prairie et dans les biomes vanilla : $(item)fleur de braise$(), $(item)marguerite "
         "de cendre$(), $(item)campanule carmin$() et $(item)tulipe de soufre$(). Elles donnent des teintures et vont en pot (comme "
         "les lys des âmes, luminelles et champignons)."),
    craft("orange_dye_from_ember_bloom", "light_gray_dye_from_ash_daisy"),
    craft("red_dye_from_crimson_bellflower", "yellow_dye_from_sulfur_tulip"),
])
entry("farming", "Animal Products", "Produits animaux", "netheryx:cooked_infernal_beef", [
    smelt("cooked_infernal_beef_from_smelting", "cooked_infernal_chicken_from_smelting"),
    smelt("fried_ember_egg_from_smelting"),
    text("$(item)Infernal Steak$(): 10 hunger + Fire Resistance.$(br)$(item)Fried Ember Egg$(): may grant Resistance.$(br)"
         "Ember Cows also give $(item)milk$().",
         "$(item)Steak infernal$() : 10 de faim + résistance au feu.$(br)$(item)Œuf de braise au plat$() : peut donner Résistance.$(br)"
         "Les vaches de braise donnent aussi du $(item)lait$()."),
])
entry("farming", "Meals", "Repas", "netheryx:infernal_stew", [
    craft("infernal_stew", en="Infernal Stew: 10 hunger, Fire Resistance (1 min) and Regeneration.",
          fr="Ragoût infernal : 10 de faim, Résistance au feu (1 min) et Régénération."),
    smelt("cooked_stalker_meat_from_smelting", "cooked_crab_leg_from_smelting"),
    text("$(item)Cooked Stalker Meat$() restores 8 hunger and may grant $(item)Strength$().",
         "La $(item)viande de traqueur cuite$() rend 8 de faim et peut donner $(item)Force$()."),
])

# ---------------------------------------------------------------- Construction
entry("building", "Ember Wood", "Bois de braise", "netheryx:ember_planks", [
    text("From $(item)Ember Fungi$() (grow them with bone meal on Ember Nylium). Does not burn.",
         "Issu des $(item)champignons de braise$() (poudre d'os sur du nylium de braise). Ininflammable."),
    craft("ember_planks", "ember_stairs"),
    craft("ember_door", "ember_trapdoor"),
    craft("ember_fence", "ember_fence_gate"),
])
entry("building", "Withered Wood", "Bois flétri", "netheryx:withered_planks", [
    text("From $(item)Withered Fungi$() grown on $(item)Withered Soil$(). Grey, eerie and fireproof.",
         "Issu des $(item)champignons flétris$() plantés sur la $(item)terre flétrie$(). Gris, sinistre et ininflammable."),
    craft("withered_planks", "withered_stairs"),
    craft("withered_door", "withered_trapdoor"),
    craft("withered_fence", "withered_fence_gate"),
])
entry("building", "Nether Grass", "Herbe du Nether", "netheryx:nether_grass_block", [
    text("A golden grass that makes the Nether feel like home. Bone meal grows grass and flowers on it; crops and fungi can be "
         "planted on it. 4 tufts of Nether Grass craft a block.",
         "Une herbe dorée qui rend le Nether accueillant. La poudre d'os y fait pousser herbe et fleurs ; on peut y planter cultures "
         "et champignons. 4 touffes d'herbe du Nether donnent un bloc."),
    craft("nether_grass_block"),
])
entry("building", "Onyx Anvil", "Enclume en onyx", "netheryx:onyx_anvil", [
    spot("onyx_anvil", "Works like an anvil but $(l)never wears out$() and every operation costs $(l)half the experience$(). "
                       "Cheaper to craft: 3 Onyx and 4 iron ingots.",
         "Fonctionne comme une enclume mais $(l)ne s'abîme jamais$() et chaque opération coûte $(l)deux fois moins d'expérience$(). "
         "Moins chère : 3 onyx et 4 lingots de fer."),
])
entry("building", "Onyx Furnace", "Four en onyx", "netheryx:onyx_furnace", [
    spot("onyx_furnace", "Smelts $(l)twice as fast$() as a furnace, for every smelting recipe (unlike the blast furnace or the "
                         "smoker). Works with hoppers.",
         "Cuit $(l)deux fois plus vite$() qu'un four, pour toutes les recettes (contrairement au haut fourneau ou au fumoir). "
         "Compatible avec les entonnoirs."),
])
entry("building", "Magma Bricks", "Briques de magma", "netheryx:magma_bricks", [
    text("Glowing bricks made from magma blocks. Unlike magma, they do not burn your feet.",
         "Briques lumineuses faites de blocs de magma. Contrairement au magma, elles ne brûlent pas les pieds."),
    craft("magma_bricks", "magma_brick_stairs"),
    craft("magma_brick_slab", "magma_brick_wall"),
])
entry("building", "Lights", "Éclairages", "netheryx:onyx_lantern", [
    craft("onyx_lantern", "ember_lamp", en=None),
    text("$(item)Ember Nylium$(), $(item)Onyx Clusters$(), $(item)Soul Lilies$() and $(item)Sulfur Clusters$() also emit light.",
         "Le $(item)nylium de braise$(), les $(item)amas d'onyx$(), les $(item)lys des âmes$() et les $(item)amas de soufre$() éclairent aussi."),
])
entry("building", "Other Blocks", "Autres blocs", "netheryx:sulfur_block", [
    craft("sulfur_block", "onyx_block"),
    text("$(item)Ash Block$() falls like sand. $(item)Crusted Lava$() is created by Magma Treads and melts back into lava.",
         "Le $(item)bloc de cendre$() tombe comme le sable. La $(item)lave croûtée$() est créée par les Semelles de magma et redevient de la lave."),
])


# ---------------------------------------------------------------- 1.2.0
entry("getting_started", "Nether Events", "Événements du Nether", "netheryx:sulfur", [
    text("Every 20 to 45 minutes, an event shakes the Nether for a few minutes:$(br)"
         "$(li)$(l)Ash fall$(): thick grey fog, Ash Wraiths and Withered Knights grow stronger.$(li)$(l)Sulfur storm$(): sulfur falls "
         "from the ceiling, pick it up!$(li)$(l)Eruption$(): in Magma Rifts and Basalt Deltas, volcanic bombs, magma and ores rain down.",
         "Toutes les 20 à 45 minutes, un événement secoue le Nether pendant quelques minutes :$(br)"
         "$(li)$(l)Pluie de cendres$() : épais brouillard gris, Spectres et Chevaliers flétris renforcés.$(li)$(l)Tempête de soufre$() : "
         "du soufre tombe de la voûte, ramassez-le !$(li)$(l)Éruption$() : dans les Failles de magma et les deltas de basalte, "
         "bombes volcaniques, magma et minerais pleuvent.", "Événements"),
    text("The full $(item)Wraith armor$() lets you see through the ash fall.$(br2)Server operators can start or stop an event with "
         "$(l)/netheryx event start <type>$() and $(l)/netheryx event stop$(). Events can be disabled in the config.",
         "L'$(item)armure spectrale$() complète permet de voir à travers la pluie de cendres.$(br2)Les opérateurs peuvent lancer ou "
         "arrêter un événement avec $(l)/netheryx event start <type>$() et $(l)/netheryx event stop$(). Les événements se désactivent "
         "dans la configuration."),
])

entry("biomes", "Marrow Thicket", "Fourré de moelle", "netheryx:ossified_nylium", [
    text("A pale forest of $(l)marrow trees$() on ossified nylium. When the ceiling is close, trunks fuse with it into natural pillars. "
         "Glowing $(item)Marrow Vines$() hang from the caps and can be climbed.$(br2)Resources: Marrow wood, $(item)Marrow Caps$() "
         "(turned into $(item)Marrow Meal$()), bones.",
         "Une forêt pâle d'$(l)arbres de moelle$() sur du nylium ossifié. Quand la voûte est proche, les troncs s'y soudent en piliers "
         "naturels. Des $(item)Lianes de moelle$() lumineuses pendent des chapeaux et s'escaladent.$(br2)Ressources : bois de moelle, "
         "$(item)Chapeaux de moelle$() (transformés en $(item)Farine de moelle$()), os.", "Fourré de moelle"),
    spot("marrow_meal", "Bone meal on a whole $(l)3x3$() area in one use.", "De la poudre d'os sur toute une zone de $(l)3x3$() en un seul usage."),
])
entry("biomes", "Rimefrost Chasm", "Gouffre de givre", "netheryx:rime_ice", [
    text("A frozen wound in the Nether: rimestone, spikes of $(l)Rime Ice$() rising from the floor and hanging from the ceiling, "
         "and $(item)Rime Crystals$(). Skeletons and wraiths roam here.",
         "Une plaie gelée du Nether : pierre de givre, pics de $(l)glace de givre$() dressés au sol et suspendus à la voûte, "
         "et $(item)Cristaux de givre$(). Squelettes et spectres y rôdent.", "Gouffre de givre"),
    spot("rime_ice", "$(l)The only water of the Nether.$() Rime Ice never melts by itself; placed against heat (lava, fire, "
                     "magma block, lit campfire) it slowly melts into a $(l)water source$().",
         "$(l)La seule eau du Nether.$() La glace de givre ne fond jamais seule ; placée contre une source de chaleur (lave, feu, "
         "bloc de magma, feu de camp allumé) elle fond lentement en $(l)source d'eau$()."),
    craft("rime_ice", en="4 Rime Shards from Rime Crystals.", fr="4 éclats de givre, issus des cristaux de givre."),
])
entry("biomes", "Wartvine Tangle", "Enchevêtrement de verrues", "netheryx:wartfruit", [
    text("Giant wart bulbs on wartstalk trunks, studded with glowing bulbs. $(item)Wartvines$() hang everywhere, loaded with "
         "$(l)Wartfruits$(): the renewable food of the Nether. Hoglins, piglins and $(l)Cinder Lizards$() live here.",
         "Des verrues géantes sur des troncs de verrutige, constellées de bulbes lumineux. Des $(item)Verrulianes$() pendent partout, "
         "chargées de $(l)Fruits de verrue$() : la nourriture renouvelable du Nether. Hoglins, piglins et $(l)Lézards de cendre$() y vivent.",
         "Enchevêtrement"),
    spot("wartfruit", "Right-click a glowing vine to pick its fruits: they grow back. Place a fruit under a ceiling to plant a new vine.",
         "Clic droit sur une liane lumineuse pour cueillir ses fruits : ils repoussent. Posez un fruit sous un plafond pour planter une liane."),
])
entry("biomes", "Quartz Reef", "Récifs de quartz", "netheryx:rich_quartz_ore", [
    text("Natural $(l)quartz spires$() pierce blackstone flats near the lava seas, veined with $(item)Rich Quartz Ore$() (2 to 5 quartz "
         "each). The lava around is full of $(l)Magmafish$().",
         "Des $(l)aiguilles de quartz$() naturelles percent les plaines de pierre noire près des mers de lave, veinées de "
         "$(item)Minerai de quartz riche$() (2 à 5 quartz chacun). La lave alentour grouille de $(l)Poissons de magme$().", "Récifs de quartz"),
    craft("quartz_glass", "quartz_lamp"),
])

entry("creatures", "Magmafish", "Poisson de magme", "netheryx:magmafish", [
    mob("magmafish", "Swims in lava seas. Hunt it, or catch it with a $(item)Lava Trap$(). Its scales brew the "
                     "$(item)Potion of Lava Diving$().",
        "Nage dans les mers de lave. Chassez-le ou attrapez-le avec une $(item)Nasse à magma$(). Ses écailles servent à brasser la "
        "$(item)Potion de plongée en lave$().", 1.5),
])
entry("creatures", "Cinder Lizard", "Lézard de cendre", "netheryx:cinder_lizard_spawn_egg", [
    mob("cinder_lizard", "A Nether $(l)mount$(). Tame it with $(item)Fire Berries$(), put a $(item)Saddle$() on it and ride it. "
                         "It walks on lava and runs even faster on basalt and blackstone.",
        "Une $(l)monture$() du Nether. Apprivoisez-le avec des $(item)Baies de feu$(), sellez-le et chevauchez-le. "
        "Il marche sur la lave et court encore plus vite sur le basalte et la pierre noire.", 0.7),
])
entry("creatures", "Lantern Bearer", "Porteur de lanterne", "netheryx:lantern_bearer_spawn_egg", [
    mob("lantern_bearer", "Its soul lantern truly $(l)lights up$() the area. Tame it with $(item)Glowcaps$(): it follows you and "
                          "lights your way. Right-click to make it stay.",
        "Sa lanterne des âmes $(l)éclaire vraiment$() les alentours. Apprivoisez-le avec des $(item)Luminelles$() : il vous suit et "
        "éclaire votre chemin. Clic droit pour qu'il reste sur place."),
])
entry("creatures", "Basalt Burrower", "Fouisseur de basalte", "netheryx:basalt_burrower_spawn_egg", [
    mob("basalt_burrower", "A huge neutral rock worm that digs long $(l)tunnels$() through netherrack and basalt. It digests ores: "
                           "nuggets, quartz, onyx, and its $(item)Fang$() (reach infusion).",
        "Un énorme ver de roche neutre qui creuse de longs $(l)tunnels$() dans la netherrack et le basalte. Il digère les minerais : "
        "pépites, quartz, onyx, et son $(item)Croc$() (infusion de portée).", 0.6),
])
entry("creatures", "Piglin Merchant", "Marchand piglin", "netheryx:piglin_merchant_spawn_egg", [
    mob("piglin_merchant", "Travels with the $(l)Nomad Caravans$(). Trades in $(item)gold$() and $(item)onyx$(): rare seeds, lava traps, "
                           "infusion templates, Nether enchanted books and Soul Compasses already tuned to a dungeon.",
        "Voyage avec les $(l)Caravanes nomades$(). Commerce en $(item)or$() et en $(item)onyx$() : graines rares, nasses, gabarits "
        "d'infusion, livres d'enchantements du Nether et Boussoles des âmes déjà accordées sur un donjon.", 0.8),
])
entry("creatures", "Mimic", "Coffre mimique", "netheryx:mimic_spawn_egg", [
    mob("mimic", "Hides among dungeon chests. Approach while $(l)sneaking$() to tell it apart... or fight it: its loot is much better "
                 "than a chest's.",
        "Se cache parmi les coffres des donjons. Approchez $(l)accroupi$() pour le démasquer... ou combattez-le : son butin est bien "
        "meilleur que celui d'un coffre."),
])

entry("structures", "Nomad Caravan", "Caravane nomade", "netheryx:wartstalk_planks", [
    text("Two tents, a lizard pen and a campfire: Nomad Caravans host a $(l)Piglin Merchant$(). Their chests hold gold, sulfur, "
         "food and sometimes an $(item)Onyx Infusion Template$().",
         "Deux tentes, un enclos à lézards et un feu de camp : les Caravanes nomades abritent un $(l)Marchand piglin$(). Leurs coffres "
         "contiennent or, soufre, nourriture et parfois un $(item)Gabarit d'infusion d'onyx$().", "Caravanes"),
])

entry("equipment", "Themed Armors", "Armures thématiques", "netheryx:ember_chestplate", [
    spot("ember_chestplate", "$(l)Ember armor$() (Magma Shells). Full set: immune to fire and hot floors, faster on Nether grass and nylium.",
         "$(l)Armure de braise$() (carapaces de magma). Ensemble : insensible au feu et aux sols brûlants, plus rapide sur l'herbe et le nylium."),
    spot("wraith_robe", "$(l)Wraith armor$() (Wraith Cloth). Full set: creatures only notice you up close, and you see through ash falls.",
         "$(l)Armure spectrale$() (étoffe spectrale). Ensemble : les créatures ne vous repèrent que de près, et vous voyez à travers les cendres."),
    spot("carapace_helmet", "$(l)Carapace armor$() (Basalt Carapaces). Full set: heavy knockback resistance and reflects 25% of melee damage.",
         "$(l)Armure de carapace$() (carapaces de basalte). Ensemble : grande résistance au recul et renvoie 25 % des dégâts de mêlée."),
])
entry("equipment", "Onyx Infusions", "Infusions d'onyx", "netheryx:onyx_infusion_template", [
    text("At a $(item)Smithing Table$(), an $(item)Onyx Infusion Template$() adds a permanent bonus to $(l)any$() tool, weapon or armor:"
         "$(li)$(item)Magma Cream$(): $(l)Smelting$(), mined blocks come out smelted.$(li)$(item)Burrower Fang$(): $(l)Reach$(), +1.5 blocks."
         "$(li)$(item)Onyx$(): $(l)Resilience$(), the item slowly repairs itself.",
         "À la $(item)Table de forgeron$(), un $(item)Gabarit d'infusion d'onyx$() ajoute un bonus permanent à $(l)n'importe quel$() outil, "
         "arme ou armure :$(li)$(item)Crème de magma$() : $(l)Fonte$(), les blocs minés sortent fondus.$(li)$(item)Croc de fouisseur$() : "
         "$(l)Portée$(), +1,5 bloc.$(li)$(item)Onyx$() : $(l)Résilience$(), l'objet se répare lentement.", "Infusions"),
    craft("onyx_infusion_template", en="Templates are found in dungeons, bastions and caravans, then duplicated.",
          fr="Les gabarits se trouvent dans les donjons, bastions et caravanes, puis se dupliquent."),
])
entry("equipment", "Soul Altar", "Autel des âmes", "netheryx:soul_altar", [
    text("Place an item (or a book) on the altar, light at least $(l)2 soul flames$() nearby (soul fire, torches, lanterns or "
         "campfires), then right-click with an offering. Each level costs 5 experience levels.$(br)"
         "$(li)2 $(item)Magma Shells$(): $(l)Ember Stride$() (boots).$(li)4 $(item)Soul Dust$(): $(l)Soul Harvest$() (weapons)."
         "$(li)2 $(item)Basalt Carapaces$(): $(l)Anchoring$() (chest, legs).",
         "Posez un objet (ou un livre) sur l'autel, allumez au moins $(l)2 flammes des âmes$() à proximité (feu, torches, lanternes ou "
         "feux de camp des âmes), puis clic droit avec une offrande. Chaque niveau coûte 5 niveaux d'expérience.$(br)"
         "$(li)2 $(item)Carapaces de magma$() : $(l)Pas de braise$() (bottes).$(li)4 $(item)Poussières d'âme$() : $(l)Récolte d'âmes$() (armes)."
         "$(li)2 $(item)Carapaces de basalte$() : $(l)Ancrage$() (plastron, jambières).", "Autel des âmes"),
    craft("soul_altar"),
])

entry("utility", "Lava Trap", "Nasse à magma", "netheryx:lava_trap", [
    spot("lava_trap", "Sink it in a $(l)lava lake$(): it catches Magmafish and lava treasures on its own. The more lava around, the "
                      "faster; $(item)raw meat$() as bait doubles the speed. Works with hoppers.",
         "Immergez-la dans un $(l)lac de lave$() : elle attrape seule des poissons de magme et des trésors de la lave. Plus il y a de lave, "
         "plus elle est rapide ; de la $(item)viande crue$() en appât double la vitesse. Compatible entonnoirs."),
    craft("lava_trap"),
    text("$(item)Magmafish Scale$() + $(item)Awkward Potion$() = $(l)Potion of Lava Diving$(): Fire Resistance and $(l)Lava Vision$() "
         "(see clearly and swim fast in lava). Redstone extends it.",
         "$(item)Écaille de poisson de magme$() + $(item)Potion étrange$() = $(l)Potion de plongée en lave$() : résistance au feu et "
         "$(l)Vision de lave$() (voir clair et nager vite dans la lave). La redstone la prolonge.", "Plongée en lave"),
])
entry("utility", "Infernal Cauldron", "Chaudron infernal", "netheryx:infernal_cauldron", [
    spot("infernal_cauldron", "Mass potions: pour up to 9 doses of one potion, $(l)heat it from below$(), then add $(item)Sulfur$(): each "
                              "sulfur adds 2 doses. Fill glass bottles to take them back.",
         "Des potions en masse : versez jusqu'à 9 doses d'une potion, $(l)chauffez-le par dessous$(), puis ajoutez du $(item)Soufre$() : "
         "chaque soufre ajoute 2 doses. Remplissez des fioles pour les récupérer."),
    craft("infernal_cauldron"),
])
entry("utility", "Fire Veil", "Voile ignifuge", "netheryx:fire_veil", [
    spot("fire_veil", "While it is $(l)anywhere in your inventory$() (or worn in a $(l)Curios charm slot$()), the flames covering "
                      "your screen when you burn are hidden. Purely visual: you still burn, so pair it with Fire Resistance.",
         "Tant qu'il est $(l)n'importe où dans l'inventaire$() (ou porté dans un $(l)emplacement breloque de Curios$()), les flammes qui "
         "couvrent l'écran quand vous brûlez sont masquées. Purement visuel : vous brûlez toujours, associez-le à la résistance au feu."),
    craft("fire_veil"),
])
entry("utility", "Ward Lantern", "Lanterne de garde", "netheryx:ward_lantern", [
    spot("ward_lantern", "No hostile creature spawns naturally within $(l)24 blocks$(). Perfect to secure a Nether base or farm.",
         "Aucune créature hostile n'apparaît naturellement à moins de $(l)24 blocs$(). Idéale pour sécuriser une base ou une ferme du Nether."),
    craft("ward_lantern"),
])

entry("farming", "Wartfruit Tart", "Tarte aux fruits de verrue", "netheryx:wartfruit_tart", [
    craft("wartfruit_tart", en="A filling meal that may grant Regeneration.", fr="Un repas copieux qui peut donner la Régénération."),
    smelt("grilled_magmafish"),
])

entry("building", "Marrow Wood", "Bois de moelle", "netheryx:marrow_planks", [
    craft("marrow_planks", "marrow_door", en="A pale, bone-white wood.", fr="Un bois pâle, blanc comme l'os."),
])
entry("building", "Wartstalk Wood", "Bois de verrutige", "netheryx:wartstalk_planks", [
    craft("wartstalk_planks", "wartstalk_door", en="A deep red wood from the Wartvine Tangle.", fr="Un bois rouge profond de l'Enchevêtrement."),
])
entry("building", "Rimestone", "Pierre de givre", "netheryx:rimestone_bricks", [
    craft("rimestone_bricks", "rimestone_brick_wall"),
])


def slug(name):
    out = []
    for ch in name.lower():
        if ch.isalnum():
            out.append(ch)
        elif out and out[-1] != "_":
            out.append("_")
    return "".join(out).strip("_")


def generate():
    write_json(data_path("patchouli_books", BOOK, "book.json"), {
        "name": f"book.{MOD_ID}.{BOOK}.name",
        "landing_text": f"book.{MOD_ID}.{BOOK}.landing",
        "subtitle": f"book.{MOD_ID}.{BOOK}.subtitle",
        "version": "2",
        "model": "patchouli:book_red",
        "book_texture": "patchouli:textures/gui/book_red.png",
        "creative_tab": f"{MOD_ID}:{MOD_ID}",
        "advancements_tab": f"{MOD_ID}:root",
        "use_resource_pack": True,
        "show_progress": False,
        "text_overflow_mode": "resize",
    })
    write_json(data_path("recipes", f"{BOOK}.json"), {
        "type": "patchouli:shapeless_book_recipe",
        "ingredients": [{"item": "minecraft:book"}, {"item": "minecraft:netherrack"}],
        "book": f"{MOD_ID}:{BOOK}"})
    for lang in LANGS:
        root = ASSETS / "patchouli_books" / BOOK / lang
        for sort, (cid, icon, name, desc) in enumerate(CATEGORIES):
            write_json(root / "categories" / f"{cid}.json",
                       {"name": name[lang], "description": desc[lang], "icon": icon, "sortnum": sort})
        for sort, e in enumerate(ENTRIES):
            pages = []
            for page in e["pages"]:
                localized = {}
                for key, value in page.items():
                    localized[key] = value[lang] if isinstance(value, dict) and lang in value else value
                pages.append(localized)
            data = {"name": e["name"][lang], "icon": e["icon"], "category": f"{MOD_ID}:{e['category']}",
                    "sortnum": sort, "pages": pages}
            if e["priority"]:
                data["priority"] = True
            write_json(root / "entries" / e["category"] / f"{slug(e['name']['en_us'])}.json", data)
