# Changelog

Toutes les evolutions notables de NetheryX (anciennement NetherX) sont documentees ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et le projet respecte le
[versionnage semantique](https://semver.org/lang/fr/) (`MAJEUR.MINEUR.CORRECTIF`).

## [2.0.0] - 2026-10-17

### Modifié (incompatible)
- **Le mod s'appelle désormais NetheryX** : identifiant `netheryx`, package `com.netheryx`, configuration
  `netheryx-common.toml`, livre `netheryx:codex`. Les mondes créés avec NetherX ne sont pas compatibles (nouvel identifiant).

### Ajouté
- **Enclume en onyx** : ne s'abîme jamais, chaque opération coûte deux fois moins d'expérience, recette moins chère.
- **Four en onyx** : cuit deux fois plus vite, pour toutes les recettes, compatible entonnoirs.
- Boss uniques : rage de la Sentinelle (crocs de cristal), nova de flammes du Sorcier, ruée des âmes du Champion.
- Traqueur de braise : manteau de fumée (invisibilité brève quand il est touché).

### Amélioré
- Textures des créatures : motifs (rayures, plaques, fissures lumineuses, écailles, taches, lambeaux, ocelles),
  contours nets, dégradés, vrais visages (orbites, iris, crocs).
- Onyx redessiné dans le style du diamant (gemme, éclats, minerai, bloc).
- Cube d'onyx : texture au patron exact du modèle, avec des yeux ; Ghast de feu redessiné.
- Minerai d'onyx aussi fréquent que le diamant ; Ghast de feu et Cube d'onyx bien plus fréquents.

## [1.3.1] - 2026-10-17

### Corrigé
- Les structures étaient recouvertes par la décoration du biome (deltas de lave, colonnes de basalte, champignons
  géants, végétation, cristaux) : elles sont maintenant placées **après** toutes les décorations
  (étape `top_layer_modification`).
- Meilleure intégration au terrain : adaptation `beard_box` pour les donjons (le terrain est comblé dessous et
  dégagé autour) et vérification de l'emprise (le sol doit exister à une hauteur proche sous les quatre quarts
  de la structure, sinon elle n'est pas placée au-dessus d'un ravin ou contre une falaise).
- Les structures ne se superposent plus aux bastions et forteresses.

### Modifié
- Structures beaucoup plus fréquentes : un donjon environ tous les 300 blocs, une petite structure environ tous
  les 130 blocs ; chaque donjon peut aussi apparaître dans des biomes vanilla.

## [1.3.0] - 2026-10-17

### Ajouté
- **Boussole des âmes avec interface** : liste (avec recherche) de toutes les structures vanilla et moddées pouvant
  exister dans la dimension actuelle ; on choisit celle à localiser.
- **Arc en onyx** : flèches +50 % de dégâts et enflammées, 2400 de durabilité, animation de tension.
- **Prairie de braise** : biome accueillant à l'herbe dorée (nouveau bloc d'herbe du Nether), fleurs et luminelles.
- Fleurs : fleur de braise, marguerite de cendre, campanule carmin, tulipe de soufre (teintures) ; luminelle (champignon lumineux).
- Culture : blé de cendre (pain de cendre) ; pots de fleurs pour toutes les plantes du mod.
- Créatures : **Vache de braise** (se trait, bœuf infernal), **Poule de cendre** (pond des œufs de braise),
  **Ghast de feu**, **Cube d'onyx**.
- Nourritures : steak et poulet infernaux, œuf de braise au plat, ragoût de luminelles, pain de cendre.
- Petites structures : **Campement de voyageur**, **Jardin du Nether**, **Cache secrète** (onyx, outils, diamants,
  nourriture, pommes d'or).
- Les créatures « standard » apparaissent dans tout le Nether (biomes vanilla et moddés compris).
- Fleurs, herbe et luminelles décorent aussi les biomes vanilla.
- Les œufs d'apparition sont aussi dans l'onglet vanilla « Œufs d'apparition » ; l'équipement en onyx dans « Combat » et « Outils ».

### Modifié
- Durabilité de l'onyx fortement augmentée : outils 4500 (netherite 2031), marteau 7000, armure x55 (netherite x37).
- Textures des arbres flétris entièrement refaites (feuillage, tronc, bois, planches, terre).

## [1.2.0] - 2026-10-17

### Ajouté
- **3 grands donjons** à plusieurs salles, chacun avec gardes, générateurs de monstres, salles annexes, arène de boss
  et salle au trésor scellée :
  - **Citadelle d'onyx** (45x45) : remparts, 4 tours, grand hall, arène de la Sentinelle d'onyx.
  - **Sanctuaire infernal** (41x41) : douves de lave, autels à blazes, salles rituelles, arène du Sorcier infernal.
  - **Catacombes flétries** : mausolée en surface, escalier vers une galerie souterraine, 4 cryptes, tombeau du Champion.
- **Sceau du gardien** : bloc indestructible qui ferme les trésors et se brise à la mort du boss.
- **Champion flétri** : nouveau boss (200 PV, appelle des renforts à 66 % et 33 % de vie).
- Salles au trésor : équipement en onyx enchanté (niveaux 30-39), netherite, totems, pommes enchantées, objets uniques.

### Modifié
- Le Sorcier infernal devient un vrai boss (150 PV) ; tous les boss ouvrent leur trésor en mourant.
- Le Codex décrit chaque donjon salle par salle.

### Retiré
- Les piliers ajoutés dans le monde (netherrack, basalte, onyx, feu des âmes).
- Les petites structures : sanctuaire de braise, avant-poste sulfureux, crypte cendrée, ruines des âmes,
  tour de guet flétrie, forge en ruine.

## [1.1.0] - 2026-10-17

### Ajouté
- **Codex NetherX** : livre-guide bilingue (anglais/français selon la langue du jeu) via Patchouli. Il décrit tout le
  contenu du mod et affiche les recettes. Donné à la première connexion ; fabrication : livre + netherrack.
- 3 biomes : Marais des âmes, Forêt flétrie, Faille de magma.
- 4 créatures : Phalène des âmes, Chevalier flétri, Scarabée de basalte, Sorcier infernal (mini-boss).
- 4 structures : Ruines des âmes, Tour de guet flétrie, Sanctuaire infernal, Forge en ruine.
- Bois flétri complet, briques de magma, lys des âmes, lanterne d'onyx, lampe de braise.
- Objets : Pierre de rappel, Élixir de lueur, Lame flétrie, Bâton du sorcier, Plastron de carapace,
  poussière d'âme, os flétri, carapace de basalte.
- Le Nether vanilla change aussi : plantes, pics, spires de feu des âmes et nouvelles créatures dans les 5 biomes vanilla.

### Modifié
- Poids par défaut de la région de biomes NetherX : 4 → 10 (les nouveaux biomes sont bien plus fréquents).
- Nouvelle dépendance : Patchouli.

## [1.0.0] - 2026-10-17

### Ajoute
- Minerai, gemme, eclats et blocs d'**Onyx** (palier entre diamant et netherite).
- Outils en onyx (epee, pioche, hache, pelle, houe, **marteau 3x3**) et armure en onyx (bonus d'ensemble : Resistance I).
- 4 biomes du Nether via TerraBlender : Bosquet de braise, Terres cendrees, Profondeurs d'onyx, Sources sulfureuses.
- 4 creatures animees (GeckoLib) : Traqueur de braise, Spectre de cendre, Crabe de magma et le boss Sentinelle d'onyx.
- 4 structures : Citadelle d'onyx, Sanctuaire de braise, Crypte cendree, Avant-poste sulfureux.
- Bois de braise complet, cultures (piment de blaze, tubercule d'ame, baies de feu) et nourritures.
- Objets : Boussole des ames, Semelles de magma, Coeur infernal, Bombe de soufre, Faux des ames, soufre-engrais.
- Butin injecte dans les coffres vanilla via des Global Loot Modifiers.
- Traductions anglaise et francaise, progres, configuration `netherx-common.toml`.
