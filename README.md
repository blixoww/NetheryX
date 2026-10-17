# NetheryX

**NetheryX** est une grosse mise a jour du Nether pour **Minecraft 1.20.1 (Forge)**.

| | |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.1+ (developpe avec 47.3.0) |
| Java | 17 |
| Dependances | [GeckoLib 4](https://github.com/bernie-g/geckolib) (animations), [TerraBlender](https://github.com/Glitchfiend/TerraBlender) (biomes), [Patchouli](https://github.com/VazkiiMods/Patchouli) (livre-guide) |

## Contenu

### Codex NetheryX (livre-guide)
Un livre Patchouli **en anglais ou en français selon la langue du jeu** décrit tout le mod : recettes (affichées en
grille), objets, blocs, créatures (modèle 3D animé), biomes, structures, outils et configuration. Il est offert à la
première connexion et se fabrique avec **un livre + un bloc de netherrack**.

### En chiffres
8 biomes · 13 créatures (dont 3 boss) · 3 grands donjons + 3 petites structures · 2 bois complets · ~50 blocs · ~50 objets · ~110 recettes.
Les 5 biomes vanilla sont aussi décorés (plantes, créatures) : le Nether a un aspect différent partout.

### L'Onyx
Le **minerai d'onyx** (noir, reflets violets) est aussi frequent que le diamant dans tout le Nether entre y=5 et y=56,
et en abondance dans les **Profondeurs d'onyx**. Il exige une pioche en diamant. L'onyx se situe entre le
diamant et la netherite (via `TierSortingRegistry`) :

| | Diamant | **Onyx** | Netherite |
|---|---|---|---|
| Durabilite outils | 1561 | **4500** | 2031 |
| Vitesse | 8.0 | **9.5** | 9.0 |
| Bonus de degats | 3.0 | **3.5** | 4.0 |
| Armure (total) | 20 | **20** | 20 |
| Robustesse | 2.0 | **2.5** | 3.0 |

Bonus d'ensemble : l'armure complete donne **Resistance I**. Les manches des outils sont en **baton de blaze**.

### Biomes (TerraBlender)
- **Prairie de braise** : herbe du Nether dorée, fleurs, luminelles, blé de cendre. *Vaches de braise, poules de cendre.*
- **Marais des âmes** : terre des âmes, pics de feu bleu, lys des âmes lumineux. *Phalènes des âmes.*
- **Forêt flétrie** : champignons géants gris ornés d'os, terre flétrie. *Chevaliers flétris.*
- **Faille de magma** : deltas de lave, sols de magma, pics de basalte. *Scarabées de basalte, Sorcier infernal.*
- **Bosquet de braise** : nylium et champignons geants de braise, racines lumineuses, piments sauvages. *Traqueurs de braise.*
- **Terres cendrees** : sol de cendre, arbustes calcines, particules de cendre. *Spectres de cendre.*
- **Profondeurs d'onyx** : blackstone, colonnes de basalte, amas d'onyx lumineux, onyx abondant.
- **Sources sulfureuses** : roche sulfureuse, minerai et amas de soufre. *Crabes de magma.*

### Creatures (GeckoLib)
| Creature | Comportement | Butin |
|---|---|---|
| Traqueur de braise | Predateur, bondit et enflamme | Viande de traqueur |
| Spectre de cendre | Volant, aveugle et affaiblit | Essence de spectre |
| Crabe de magma | Paisible, elevable aux baies de feu | Pattes de crabe, carapace de magma |
| Phalène des âmes | Paisible, volante, lumineuse | Poussière d'âme |
| Chevalier flétri | Guerrier, inflige le Flétrissement | Os flétri, Lame flétrie (rare) |
| Scarabée de basalte | Neutre, très cuirassé, charge en troupeau | Carapace de basalte |
| **Sorcier infernal** (boss) | Boules de feu, téléportation, invocations | Bâton du sorcier, Pierre de rappel |
| Vache de braise | Paisible, se trait, s'élève au blé de cendre | Bœuf infernal, cuir, crème de magma |
| Poule de cendre | Paisible, pond des œufs de braise | Poulet infernal, plumes |
| Ghast de feu | Ghast embrasé, explosions plus fortes | Boules de feu, larmes de ghast, éclats d'onyx |
| Cube d'onyx | Cube de magma cristallisé, se divise | Crème de magma, éclats d'onyx |
| **Champion flétri** (boss) | Chevalier géant, appelle des renforts | Lame flétrie, crâne de wither |
| **Sentinelle d'onyx** (boss) | Barre de boss, coups de poing, onde de choc de zone | Coeur de sentinelle, onyx |

### Donjons
Trois grands donjons, chacun avec gardes, générateurs de monstres, salles annexes avec coffres, une **arène de boss**
et une **salle au trésor fermée par des Sceaux du gardien** (indestructibles, ils se brisent à la mort du boss) :
- **Sanctuaire infernal** (Faille de magma, Bosquet de braise, Sources sulfureuses) : douves de lave, autels à blazes,
  salles rituelles, puis le **Sorcier infernal**.
- **Catacombes flétries** (Forêt flétrie, Terres cendrées, Marais des âmes, Vallée des âmes) : mausolée, escalier de
  12 blocs, galerie bordée de crânes, 4 cryptes, puis le **Champion flétri**.
- **Citadelle d'onyx** (Profondeurs d'onyx, Deltas de basalte) : remparts, 4 tours, grand hall, puis la **Sentinelle d'onyx**.

Petites structures (sans boss) : **Campement de voyageur**, **Jardin du Nether**, **Cache secrète**.

Les bastions et forteresses contiennent aussi du butin NetheryX (Global Loot Modifiers).

### Objets utiles
- **Marteau en onyx** : mine en 3x3 (accroupi = un seul bloc), respecte les protections de terrain.
- **Boussole des âmes** : ouvre une interface pour choisir n'importe quelle structure (vanilla ou moddée) de la dimension, puis guide le joueur.
- **Arc en onyx** : flèches plus puissantes et enflammées.
- **Semelles de magma** : solidifient la lave sous vos pas (equivalent "Frost Walker" pour la lave).
- **Coeur infernal** (fabrique avec le coeur du boss) : resistance au feu permanente dans le Nether.
- **Bombe de soufre** : explosif lancable (aussi via distributeur).
- **Faux des ames** : vol de vie a chaque coup (tresor uniquement).
- **Pierre de rappel** : 3 secondes de concentration pour rejoindre un portail du Nether emprunté ; avec plusieurs portails, une interface demande lequel (ou le point de réapparition).
- **Bateau d'obsidienne** : navigue sur la lave, ne se casse jamais et revient directement dans l'inventaire quand on le frappe.
- **Seau d'obsidienne** : fait disparaître n'importe quelle source de fluide sans jamais se remplir.
- **Élixir de lueur** : vision nocturne et résistance au feu.
- **Lame flétrie**, **Bâton du sorcier** (boules de feu), **Plastron de carapace** (résistance au recul).
- **Soufre** : engrais pour les cultures du Nether, y compris les verrues ; donne aussi de la poudre a canon.

### Particules
7 particules animées propres au mod (étincelles de braise, flocons de cendre, feux follets d'âme, reflets d'onyx,
bouffées de soufre, pollen de braise, spores flétries) utilisées par l'ambiance des biomes, les minerais, plantes et
cultures mûres, et toutes les créatures.

### Ateliers en onyx
- **Enclume en onyx** : ne s'abime jamais et chaque operation coute deux fois moins d'experience.
- **Four en onyx** : cuit deux fois plus vite, pour toutes les recettes.

### Cultures et nourriture
Piment de blaze (resistance au feu), tubercule d'ame (sable/terre des ames), baies de feu, viande de traqueur,
pattes de crabe, ragout infernal.

## Compatibilite
- Biomes injectes via **TerraBlender** : cohabitent avec Biomes O' Plenty et les autres mods de biomes.
- Minerai d'onyx ajoute par **biome modifier Forge** a tout `#minecraft:is_nether` (biomes moddes compris).
- Tags **Forge** (`forge:gems/onyx`, `forge:ores/sulfur`, `forge:dusts/sulfur`, `forge:storage_blocks/*`...) pour
  l'interoperabilite avec les mods techniques.
- Tags vanilla (`minecraft:logs`, `nylium`, `beacon_base_blocks`, `trimmable_armor`...) : les garnitures d'armure fonctionnent sur l'onyx.
- Comportement des sols, cultures et apparitions pilote par des **tags de donnees** (`data/netheryx/tags`), modifiables par datapack.
- Aucun mixin : uniquement des API Forge et des registres differes.

## Configuration
`config/netheryx-common.toml` : poids de la region de biomes, bonus d'armure, minage 3x3, rayon de la boussole,
destruction de blocs par les bombes, points de vie du boss, rayon des Semelles de magma.

## Developpement

```bash
./gradlew runClient      # lancer le jeu de developpement
./gradlew build          # produire build/libs/netheryx-1.20.1-<version>.jar
```

### Ressources generees
Textures, modeles, blockstates, tables de butin, recettes, tags, biomes, structures (NBT) et modeles/animations
GeckoLib sont produits par un generateur Python deterministe :

```bash
pip install pillow
python3 tools/generate_resources.py
```

Le contenu est decrit dans `tools/netheryx_gen/content.py`, le livre dans `tools/netheryx_gen/book.py`.
`python3 tools/check_resources.py` verifie que chaque modele, texture, traduction, recette et page du livre
reference quelque chose qui existe ; la CI lance les deux scripts.
Les fichiers `.png` peuvent ensuite etre retouches a la main (pensez a ne plus regenerer ceux-la, ou a modifier le generateur).

### Architecture
```
com.netheryx
├── NetheryX                 point d'entree, branchement des registres
├── config/                 ForgeConfigSpec
├── registry/               DeferredRegister (blocs, objets, entites, onglet, structures, loot modifiers), paliers, materiaux
├── block/ item/ entity/    contenu de jeu
├── worldgen/               region TerraBlender, regles de surface, type de structure "nether_floor"
├── loot/                   Global Loot Modifier generique
├── event/                  evenements (attributs, apparitions, marteau, bonus d'armure)
└── client/                 rendus GeckoLib (couche emissive *_glowmask)
```

### Publier une version
Chaque tag Git déclenche la CI (`.github/workflows/release.yml`) : elle compile le mod avec la version du tag et
publie le `.jar` dans une Release GitHub, avec les notes extraites du `CHANGELOG.md`.

```bash
git tag 1.0.0
git push origin 1.0.0
```

### Monter de version
1. Mettre a jour `minecraft_version`, `forge_version`, `geckolib_version`, `terrablender_version` dans `gradle.properties`.
2. Augmenter `mod_version` (SemVer) et completer `CHANGELOG.md`.
3. Regenerer les ressources et lancer `./gradlew build`.

## Licence
GNU GPL - voir [LICENSE](LICENSE).
