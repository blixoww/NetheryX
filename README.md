# NetheryX

**NetheryX** est une grosse mise à jour du Nether pour **Minecraft 1.20.1 (Forge)**.

| | |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.1+ (développé avec 47.3.0) |
| Java | 17 |
| Dépendances | [GeckoLib 4](https://github.com/bernie-g/geckolib) (animations), [TerraBlender](https://github.com/Glitchfiend/TerraBlender) (biomes), [Patchouli](https://github.com/VazkiiMods/Patchouli) (livre-guide) |
| Facultatif | [JEI](https://github.com/mezz/JustEnoughItems) (recettes de l'autel et des infusions), [Curios](https://github.com/TheIllusiveC4/Curios) (voile ignifuge en breloque) |

La référence complète (chaque bloc, objet, recette, créature, butin et option) est dans
[`docs/DOCUMENTATION.md`](docs/DOCUMENTATION.md), générée à partir des données du mod. L'historique des versions est
dans [`CHANGELOG.md`](CHANGELOG.md).

## Contenu

### Codex NetheryX (livre-guide)
Un livre Patchouli **en anglais ou en français selon la langue du jeu** décrit tout le mod : recettes (affichées en
grille), objets, blocs, créatures (modèle 3D animé), biomes, structures, outils et configuration. Il est offert à la
première connexion et se fabrique avec **un livre + un bloc de netherrack**.

### En chiffres
12 biomes · 26 créatures (dont 3 boss de donjon, 1 mini-boss et 1 boss final) · 3 grands donjons + 6 petites structures · 4 bois complets · ~50 blocs · ~50 objets · ~110 recettes.
Les 5 biomes vanilla sont aussi décorés (plantes, créatures) : le Nether a un aspect différent partout.

### L'Onyx
Le **minerai d'onyx** (noir, reflets violets) est aussi fréquent que le diamant dans tout le Nether entre y=5 et y=56,
et abonde dans la **Forêt pétrifiée d'onyx**. Il exige une pioche en diamant. L'onyx se place **au-dessus de la
netherite** dans la hiérarchie des paliers (`TierSortingRegistry`, compatible avec les paliers des autres mods) :

| | Diamant | Netherite | **Onyx** |
|---|---|---|---|
| Durabilité des outils | 1561 | 2031 | **4500** |
| Vitesse de minage | 8.0 | 9.0 | **10.0** |
| Bonus de dégâts | 3.0 | 4.0 | **5.0** |
| Armure (total) | 20 | 20 | **24** |
| Robustesse | 2.0 | 3.0 | **4.0** |
| Résistance au recul | 0 | 0.1 | **0.15** |

L'armure complète donne **Résistance I** et la **résistance au feu**. Les manches des outils sont en **bâton de blaze**
et les objets en onyx ne brûlent pas dans la lave. Au-delà : l'**équipement abyssal** (butin du boss final).

### Biomes (TerraBlender)
- **Bosquet de braise** : nylium et champignons géants de braise, racines lumineuses, piments sauvages. *Traqueurs de braise.*
- **Terres cendrées** : dunes de cendre sur du basalte, arbustes calcinés, pluie de cendres. *Spectres de cendre.*
- **Prairie de braise** : herbe du Nether dorée, fleurs, luminelles, blé de cendre. *Vaches de braise, poules de cendre.*
- **Sources sulfureuses** : roche sulfureuse, minerai et amas de soufre. *Crabes de magma.*
- **Forêt flétrie** : champignons géants gris ornés d'os, terre flétrie. *Chevaliers flétris.*
- **Fourré de moelle** : arbres de moelle soudés à la voûte, lianes lumineuses, farine de moelle (engrais 3x3).
- **Gouffre de givre** : pierre et glace de givre ; la glace fond en **source d'eau** près d'une source de chaleur.
- **Enchevêtrement de verrues** : verrues géantes, verrulianes dont les fruits repoussent.
- **Récifs de quartz** : aiguilles et amas de quartz, minerai de quartz riche, verre et lampes de quartz.
- **Jungle infernale** : arbres infernaux géants à contreforts, fougères infernales, lianes de braise.
- **Lac de verre en fusion** : bassins de lave cerclés d'obsidienne, verre et cristaux en fusion.
- **Forêt pétrifiée d'onyx** : arbres en bois pétrifié couronnés de feuillage cristallisé, onyx abondant.

### Créatures (GeckoLib)
- **Boss** : Sentinelle d'onyx, Sorcier infernal et Champion flétri (un par donjon), **Cœur du Nether** (boss final,
  invoqué avec le Cœur scellé), Golem de basalte (mini-boss, Forge de basalte). Toutes leurs attaques sont annoncées au sol et se
  contrent : cercles à quitter, ondes à sauter, lignes à éviter, faisceaux arrêtés par les piliers. Leur vie s'adapte au nombre de joueurs.
- **Montures et compagnons** : Wyverne de cendre (monture volante, une par joueur), Lézard de cendre (monture qui
  marche sur la lave), Porteur de lanterne (lumière dynamique).
- **Hostiles** : Traqueur de braise (tourne autour de sa proie avant de bondir), Spectre de cendre (fuit la lumière),
  Chevalier flétri (en patrouille derrière un capitaine), Ghast de feu, Cube d'onyx, Coffre mimique.
- **Neutres et paisibles** : Scarabée et Fouisseur de basalte, Crabe de magma, Phalène des âmes, Vache de braise,
  Poule de cendre, Cochon de blaze, Escargot de cristal, Poisson de magme, lucioles de braise et des âmes, Marchand piglin.

### Structures
- **Citadelle d'onyx**, **Sanctuaire infernal**, **Catacombes flétries** : complexes souterrains sous une petite entrée, trahis
  en surface par des **indices** (ruines, restes d'explorateurs, panneaux). Un grand escalier mène à l'**arène du boss** ; **trois ailes**
  facultatives cachent de beaux trésors. Le **coffre-fort**, aux murs indestructibles camouflés en briques, ne s'ouvre qu'à la mort
  du boss. Le plan et les salles changent d'un donjon à l'autre.
- **Petites trouvailles** partout dans le Nether : deux ou trois ennemis, un coffre.
- **Atlas du Nether** : la carte de vos explorations, biomes et structures compris.
- Petites structures : Forge de basalte, Nids de wyverne, Caravane nomade, Campement de voyageur, Jardin du Nether,
  Cache secrète. Toutes se retrouvent avec la **Boussole des âmes**.
- Sous la voûte : **nids de spectres** et **cages d'explorateurs** suspendus au-dessus des grands vides.
- Les bastions et forteresses contiennent aussi du butin NetheryX (Global Loot Modifiers).

### Objets et ateliers
- **Outils** : marteau en onyx (minage 3x3, respecte les protections), arc en onyx, Lame abyssale, Faux des âmes.
- **Exploration** : Boussole des âmes, Pierre de rappel (retour à un portail emprunté), Lanterne du Nether (révèle
  les minerais), bateau et seau d'obsidienne, Semelles de magma, Voile ignifuge.
- **Ateliers** : enclume et four en onyx, Autel des âmes (enchantements du Nether), Chaudron infernal (potions en
  masse), Nasse à magma (pêche dans la lave), Lanterne de garde (empêche l'apparition des monstres), infusions d'onyx.
- **Événements du Nether** : pluie de cendres, tempête de soufre, éruption.
- **Cultures et nourriture** : piment de blaze, tubercule d'âme, baies de feu, blé de cendre, fruits des arbres
  (figues de braise, prunes flétries), ragoûts, tartes et pomme d'onyx.

## Compatibilité
- Biomes injectés via **TerraBlender** : cohabitent avec Biomes O' Plenty et les autres mods de biomes.
- Minerai d'onyx ajouté par **biome modifier Forge** à tout `#minecraft:is_nether` (biomes moddés compris).
- Tags **Forge** (`forge:gems/onyx`, `forge:ores/sulfur`, `forge:dusts/sulfur`, `forge:storage_blocks/*`...) pour
  l'interopérabilité avec les mods techniques.
- Tags vanilla (`minecraft:logs`, `nylium`, `beacon_base_blocks`, `trimmable_armor`...) : les garnitures d'armure fonctionnent sur l'onyx.
- Comportement des sols, cultures et apparitions piloté par des **tags de données** (`data/netheryx/tags`), modifiables par datapack.
- Aucun mixin : uniquement des API Forge et des registres différés.

## Configuration
`config/netheryx-common.toml` : poids de la région de biomes, bonus d'armure, minage 3x3, rayon de la boussole,
destruction de blocs par les bombes, points de vie du boss, rayon des Semelles de magma. Le détail de chaque option
est dans [`docs/DOCUMENTATION.md`](docs/DOCUMENTATION.md#configuration).

## Développement

```bash
./gradlew runClient      # lancer le jeu de développement (avec JEI)
./gradlew build          # produire build/libs/netheryx-1.20.1-<version>.jar
./gradlew runGameTestServer   # tests en jeu, dans un monde neuf (combats de boss, donjon réel), aussi lancés par la CI
```

Le plan de test en jeu est dans [`docs/PLAN_DE_TEST.md`](docs/PLAN_DE_TEST.md).

### Ressources générées
Textures, modèles, blockstates, tables de butin, recettes, tags, biomes, structures (NBT), modèles/animations
GeckoLib, le Codex et `docs/DOCUMENTATION.md` sont produits par un générateur Python déterministe :

```bash
pip install pillow
python3 tools/generate_resources.py
```

Le contenu est décrit dans `tools/netheryx_gen/content.py`, le livre dans `tools/netheryx_gen/book.py`.
`python3 tools/check_resources.py` vérifie que chaque modèle, texture, traduction, recette et page du livre
référence quelque chose qui existe ; la CI lance les deux scripts.

**Textures dérivées du vanilla** : `tools/diamond_armor.py` (armures) et `tools/vanilla_textures.py` (armes, nourritures, bois...)
recolorent les textures de Minecraft dans `art/` à partir du jar du cache Gradle.

**Vérification des donjons** : `structures_v140.py` parcourt chaque gabarit à pied (trésors et arène accessibles, coffre-fort fermé
sur ses six faces avant la mort du boss, aucun bloc sans support) ; un plan invalide fait échouer la génération.

**Aperçus** : `tools/render_preview.py` dessine un gabarit (`structure nether_garden/v2 sortie.png`) ou les biomes réellement générés
(`NETHERYX_DUMP=1 ./gradlew runGameTestServer`, puis `dump run-gametest/biome_dump sortie.png`).

**Textures faites main** : placez-les dans `art/assets/netheryx/...` (même chemin que dans les ressources). Le
générateur les copie par-dessus les textures générées, elles ne sont donc jamais écrasées (voir [`art/README.md`](art/README.md)).

### Architecture
```
com.netheryx
├── NetheryX                 point d'entrée, branchement des registres
├── config/                 ForgeConfigSpec
├── registry/               DeferredRegister (blocs, objets, entités, onglet, structures, loot modifiers), paliers, matériaux
├── block/ blockentity/     blocs et blocs-entités (autel, chaudron, nasse...)
├── item/ entity/ effect/   contenu de jeu ; entity/ai : comportements partagés
├── enchantment/ recipe/    enchantements du Nether, infusions d'onyx
├── menu/ network/          interfaces et paquets (boussoles, pierre de rappel, événements)
├── worldgen/               région TerraBlender, règles de surface, features, type de structure "nether_floor"
├── world/                  événements du Nether (pluie de cendres, tempête de soufre, éruption)
├── loot/                   Global Loot Modifier générique
├── event/                  événements (attributs, apparitions, marteau, bonus d'armure)
├── compat/jei/             intégration JEI facultative
└── client/                 rendus GeckoLib (couche émissive *_glowmask), particules, écrans
```

### Publier une version
1. Augmenter `mod_version` (SemVer) dans `gradle.properties`.
2. Ajouter la section `## [x.y.z] - AAAA-MM-JJ` dans `CHANGELOG.md`.
3. Lancer `.\gitpush` sous Windows (PowerShell ou cmd), ou `./gitpush.sh` sous Linux/macOS/Git Bash ;
   un message personnalisé se passe en argument, et `.\gitpush --dry-run` affiche les commandes sans rien faire.
   Le script fait le commit, le push, puis le tag de la version.

Le tag déclenche la CI (`.github/workflows/release.yml`) : elle vérifie que le tag correspond à `mod_version` et a
ses notes dans le changelog, revérifie les ressources générées, compile le mod et publie le `.jar` dans une Release
GitHub avec ces notes.

### Monter de version de Minecraft ou des dépendances
1. Mettre à jour `minecraft_version`, `forge_version`, `geckolib_version`, `terrablender_version` et leurs plages
   (`*_version_range`) dans `gradle.properties`.
2. Régénérer les ressources et lancer `./gradlew build`.

## Licence
GNU GPL - voir [LICENSE](LICENSE).
