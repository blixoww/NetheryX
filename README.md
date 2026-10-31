# NetheryX — Minecraft 1.7.10

Portage de NetheryX pour **Minecraft 1.7.10** (Forge 10.13.4.1614), réécrit de zéro depuis la version 1.20.1
(branche `main`). La 1.7.10 n'a ni fichiers de données (recettes, butins, biomes : tout est en Java), ni
« aplatissement » des blocs (les variantes passent par les métadonnées), ni les bibliothèques de la version moderne
(GeckoLib, TerraBlender, Patchouli, JEI). Tout le contenu de la 1.20.1 y est porté, adapté aux possibilités de la 1.7.10 (voir « Différences » plus bas).

## Compiler

```bash
./gradlew build
```

Le `.jar` se trouve dans `build/libs/`. La compilation utilise **RetroFuturaGradle** (l'outil de référence des mods
1.7.10) et **Gradle 9.2** : il faut Java 17 ou plus pour lancer Gradle, et le JDK 8 de Minecraft 1.7.10 est téléchargé
automatiquement. `./gradlew runClient` lance le jeu avec le mod.

## Contenu (1.0.0)

- **Matériaux** : onyx (minerai, gemme, éclats, blocs, onyx poli, briques), soufre, quartz riche, pierre de givre,
  briques de magma, cendre, éclats de givre, poussière d'âme ; quatre bois (braise, flétri, moelle, verrutige).
- **12 biomes du Nether** (le fournisseur de biomes du Nether est remplacé, désactivable) : prairie et bosquet de
  braise, désert de cendre, sources de soufre, marais des âmes, forêt flétrie, faille de magma, fourré de moelle,
  gouffre de givre, enchevêtrement de verrues, récif de quartz, forêt pétrifiée ; sols, végétation, cristaux, mares.
- **26 créatures** aux modèles de la 1.20.1 (fichiers de géométrie lus au lancement, animations procédurales) :
  monstres, animaux d'élevage, lucioles et phalènes, porteur de lanterne et lézard de cendre (apprivoisables, le lézard
  se selle et se monte), marchand piglin, mimic, ghast de feu, cube d'onyx, poisson de magme (dans la lave).
- **Structures** (gabarits convertis depuis la 1.20.1) : campement, caravane, jardin, cache secrète, nid de wyverne,
  forge de basalte, et trois donjons — citadelle d'onyx, sanctuaire infernal, catacombes flétries — avec coffres,
  générateurs, gardiens et trésor muré de briques scellées qui se brisent à la mort du gardien.
- **Gardiens** : Sentinelle d'onyx, Sorcier infernal, Champion flétri, Golem de basalte (barre de vie, attaques
  spéciales, trophées). Leurs quatre trophées forment le **Cœur scellé**, qui réveille le **Cœur du Nether**.
- **Fin de jeu** : éclats abyssaux, armure et lame abyssales, **wyverne de cendre** (œuf, monture volante, souffle de
  feu, sifflet, cristal de rangement).
- **Équipement** : armures de braise, du spectre, de carapace et d'onyx avec bonus d'ensemble, semelles de magma,
  marteau 3 x 3, arc en onyx, faux des âmes, lame flétrie, bâton du sorcier, voile ignifuge, cœur infernal, bombes de
  soufre, élixir de lueur.
- **Autel des âmes** (interface de la 1.20.1) et ses trois enchantements : Pas de braise, Récolte d'âmes, Ancrage.
- **Infusions d'onyx** : fonte, célérité, résilience.
- **Boussole des âmes**, **pierre de rappel**, **Codex** (livre du joueur, en français ou en anglais selon le jeu).
- Cultures du Nether (piment de blaze, blé de cendre, tubercule d'âme, baies de feu) et une vingtaine de plats.
- Réglages dans `config/netheryx.cfg` ; traductions française et anglaise.

## Différences avec la version 1.20.1

| 1.20.1 | 1.7.10 |
|---|---|
| Table de forgeron (infusions, amélioration abyssale) | Gabarit chargé à l'établi puis appliqué à l'enclume ; amélioration abyssale à l'établi |
| Infusion de portée | Infusion de **célérité** (minage 40 % plus rapide) : la portée n'est pas modifiable en 1.7.10 |
| Flammes des âmes autour de l'autel | Feux (sur netherrack) ou lampes de braise |
| Boussole des âmes avec interface et flèche | Clic droit : direction et distance dans le tchat ; accroupi : type de structure |
| Pierre de rappel avec choix du portail | Retour près du dernier portail emprunté (ou au lit) |
| Sifflet : toutes dimensions | Sifflet : wyvernes chargées de la dimension |
| Codex Patchouli, JEI | Codex en livre écrit ; pas de JEI (NEI affiche les recettes Java) |
| Portes, trappes, escaliers, dalles, enclume et four d'onyx, chaudron infernal, lanternes, bateau et seau d'obsidienne, événements du Nether | Non portés |
