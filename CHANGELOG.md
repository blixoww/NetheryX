# Changelog

Toutes les évolutions notables de NetheryX sont documentées ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et le projet respecte le
[versionnage sémantique](https://semver.org/lang/fr/) (`MAJEUR.MINEUR.CORRECTIF`).

Chaque tag Git (`git tag 1.0.0 && git push origin 1.0.0`) déclenche la compilation et la publication du `.jar`
dans une Release GitHub ; les notes de version sont extraites de ce fichier.

## [1.1.0] - 2026-10-17

### Ajouté
- **Bateau d'obsidienne** : navigue sur la lave comme sur l'eau, ne se brise jamais (chutes, coups) et retourne
  directement dans l'inventaire quand on le frappe. Ses passagers sont protégés du feu ; la coque est rehaussée sur la lave.
- **Seau d'obsidienne** : fait disparaître une source de n'importe quel fluide (lave, eau, fluides moddés, blocs inondés)
  sans jamais se remplir.
- **Particules** : 7 particules animées (étincelle de braise, flocon de cendre, feu follet d'âme, reflet d'onyx,
  bouffée de soufre, pollen de braise, spore flétrie).
  - Ambiance de chacun des 8 biomes.
  - Blocs : minerais d'onyx et de soufre, amas, nylium, herbe du Nether, fleurs, champignons, verrues, briques de magma,
    lampe de braise, lanterne d'onyx, lave croûtée ; les cultures **mûres** scintillent (prêtes à récolter).
  - Toutes les créatures et tous les boss ont leurs particules.
- Progrès « Eaux brûlantes » (fabriquer un bateau d'obsidienne).

### Modifié
- **Pierre de rappel** : ramène désormais au **portail du Nether** emprunté. Elle retient les 8 derniers portails
  (toutes dimensions, conservés à la mort) ; avec plusieurs portails, une interface permet de choisir la destination,
  d'oublier un portail ou de rejoindre le point de réapparition. Un portail détruit est oublié automatiquement.
- Concentration de la Pierre de rappel : spirale de particules qui se resserre.
- Protocole réseau passé en version 2 (client et serveur doivent avoir la même version du mod).

## [1.0.0] - 2026-10-17

Première version publique de NetheryX pour Minecraft 1.20.1 (Forge 47).

### Matériaux et équipement
- **Onyx** : minerai aussi fréquent que le diamant, gemme, éclats, blocs (poli, briques, escaliers, dalles, murets).
- Outils en onyx (4500 de durabilité, entre diamant et netherite), **marteau 3x3**, **arc en onyx**, armure en onyx
  (bonus d'ensemble : Résistance I), plastron de carapace, semelles de magma.
- **Enclume en onyx** (ne s'abîme jamais, coûte deux fois moins d'XP) et **four en onyx** (deux fois plus rapide).

### Monde
- 8 biomes (TerraBlender) : Prairie de braise, Bosquet de braise, Terres cendrées, Profondeurs d'onyx,
  Sources sulfureuses, Marais des âmes, Forêt flétrie, Faille de magma. Les biomes vanilla sont aussi décorés.
- 3 grands donjons avec boss et trésor scellé : Citadelle d'onyx, Sanctuaire infernal, Catacombes flétries.
- 3 petites structures : Campement de voyageur, Jardin du Nether, Cache secrète.

### Créatures (GeckoLib)
- Traqueur de braise, Spectre de cendre, Crabe de magma, Phalène des âmes, Chevalier flétri, Scarabée de basalte,
  Vache de braise, Poule de cendre, Ghast de feu, Cube d'onyx.
- Boss : Sentinelle d'onyx (rage et crocs de cristal), Sorcier infernal (nova de flammes), Champion flétri (ruée des âmes).

### Objets utiles
- Boussole des âmes (interface de choix de structure, vanilla et moddée), Pierre de rappel, Cœur infernal,
  Élixir de lueur, Bombe de soufre, soufre-engrais, Faux des âmes, Lame flétrie, Bâton du sorcier.

### Cultures et nourriture
- Piment de blaze, tubercule d'âme, baies de feu, blé de cendre, luminelle, 4 fleurs du Nether, pots de fleurs.
- Steak et poulet infernaux, œuf de braise, pain de cendre, ragoûts.

### Divers
- **Codex NetheryX** (Patchouli) en français et en anglais, donné à la première connexion.
- Compatibilité : tags Forge, biome modifiers, Global Loot Modifiers, configuration `netheryx-common.toml`.

## [0.1.0] - 2026-10-17

Pré-alpha : prototypes successifs ayant mené à la 1.0.0 (non publiés).
