# NetheryX — Plan de test

Ce guide sert à vérifier tout le contenu du mod en jeu, rapidement et dans l'ordre. Cochez au fur et à mesure.
La liste complète des blocs, objets, recettes et butins est dans [`DOCUMENTATION.md`](DOCUMENTATION.md).

## 0. Préparation

1. **Instance de test propre** : Forge 47.3+, GeckoLib, TerraBlender, Patchouli, NetheryX. Ajoutez **JEI** (Just Enough
   Items) : il affiche toutes les recettes du mod (clic sur un objet → `R` pour ses recettes, `U` pour ses usages).
   Les autres mods de votre pack se testent dans une seconde étape (voir §9).
2. Créez un **nouveau monde** (les structures et biomes ne se génèrent que dans des chunks neufs) :
   mode **Créatif**, triche activée, type « Par défaut ».
3. Commandes utiles une fois dans le monde :
   ```
   /gamerule keepInventory true
   /gamerule doDaylightCycle false
   /gamerule doWeatherCycle false
   /effect give @s minecraft:fire_resistance infinite 0 true
   /effect give @s minecraft:night_vision infinite 0 true
   ```
4. Entrez dans le Nether : `/execute in minecraft:the_nether run tp @s ~ 80 ~`

## 1. Onglet créatif et objets

- [ ] L'onglet **NetheryX** contient tous les blocs, objets et œufs (aucune texture violette et noire manquante).
- [ ] Chaque objet a un nom traduit (français et anglais : changez la langue dans les options).
- [ ] Le **Codex** (livre) s'ouvre ; toutes les catégories et pages s'affichent.
- Obtenir un objet précis : `/give @s netheryx:abyssal_blade` (remplacez par l'identifiant voulu, voir la documentation).

## 2. Recettes (avec JEI)

En survie, ou en créatif en vérifiant dans JEI que chaque recette existe et fabrique le bon objet :

- [ ] Onyx : bloc, briques, escaliers, dalles, murets, outils, arc, marteau, armure, pomme en onyx.
- [ ] Bois (braise, flétri, moelle, verrutige) : planches, escaliers, portes, trappes, barrières…
- [ ] Nourriture (four, fumoir, feu de camp) : viandes, œufs, côtelettes de blaze, tartes, ragoûts.
- [ ] Objets utiles : Boussole des âmes, Pierre de rappel, Lanterne du Nether, Voile ignifuge, Nasse à magma,
      Chaudron infernal, Autel des âmes, Lanterne de garde, Sifflet de wyverne.
- [ ] Table de forgeron : infusions d'onyx, **armure abyssale** (gabarit + pièce en onyx + éclat abyssal).
- [ ] Recettes de fin de jeu : **Cœur scellé** (3 trophées de boss), **Lame abyssale** (très chère).
- Astuce : `/give @s netheryx:sentinel_eye`, `netheryx:warlock_sigil`, `netheryx:champion_crown` pour tester le Cœur scellé.

## 3. Biomes

Téléportez-vous dans chaque biome avec la **Boussole du créateur** (onglet biomes) ou par commande :
```
/locate biome netheryx:hellwood_jungle
/tp @s <x> 80 <z>
```
Biomes à vérifier : `ember_meadow`, `ember_grove`, `ashen_wastes`, `sulfur_springs`, `withered_forest`, `marrow_thicket`,
`rimefrost_chasm`, `wartvine_tangle`, `quartz_reef`, `hellwood_jungle`, `molten_glass_lake`, `petrified_onyx_forest`.

- [ ] Sol, végétation, arbres, rochers et particules propres au biome.
- [ ] Créatures du biome présentes (voir la documentation, section « Créatures : apparitions et butin »).
- [ ] Biomes vanilla décorés (fleurs, plantes, lucioles).

## 4. Structures et donjons

Avec la **Boussole des âmes** (structures NetheryX en tête de liste) ou par commande :
```
/locate structure netheryx:onyx_citadel
/locate structure netheryx:infernal_sanctum
/locate structure netheryx:withered_catacombs
/locate structure netheryx:wyvern_nest
/locate structure netheryx:hidden_cache
/locate structure netheryx:nomad_caravan
/locate structure netheryx:traveler_camp
/locate structure netheryx:nether_garden
```
Pour chaque donjon, **en survie** (`/gamemode survival`) avec une bonne armure :
- [ ] Parcours complet : escaliers praticables (Sanctuaire : descente vers la nef ; Catacombes : deux niveaux).
- [ ] Coffres et **mimiques** (approchez accroupi).
- [ ] Le coffre-fort est **impossible à ouvrir ou à contourner** avant la mort du boss (essayez de creuser autour).
- [ ] Le boss **reste dans son arène** : fuyez, il doit y retourner et se soigner lentement.
- [ ] À la mort du boss : les sceaux disparaissent, la maçonnerie redevient de la brique, le trophée tombe.
- [ ] Cache secrète : trappe au ras du dallage, fumée bleue visible de loin.
- [ ] Nid de wyverne : coffre avec un œuf, une wyverne sauvage, prises pour grimper.

Butins sans explorer : `/loot give @s loot netheryx:chests/onyx_citadel_vault` (et les autres tables du dossier
`data/netheryx/loot_tables/chests`).

## 5. Créatures

Invoquez-les avec leurs œufs (onglet NetheryX) ou `/summon netheryx:<nom>` :
- [ ] Animaux : `ember_cow` (se trait), `cinder_hen` (pond), `blaze_pig` (poudre de blaze), `crystal_snail` (cisailles),
      `magma_crab`, `basalt_beetle`, `magmafish` (dans la lave), `lantern_bearer` (apprivoiser avec une luminelle),
      `cinder_lizard` (apprivoiser avec des baies de feu, puis selle).
- [ ] Lucioles `ember_firefly` / `soul_firefly` : clic droit avec une fiole vide → bocal qui éclaire.
- [ ] Hostiles : `ember_stalker`, `ash_wraith`, `withered_knight`, `fire_ghast`, `onyx_cube`, `basalt_burrower`, `mimic`.
- [ ] Mini-boss `basalt_golem` : frappe au sol, éclats de magma, phase en fusion sous 50 %.
- [ ] Marchand piglin (`piglin_merchant`) : échanges.

## 6. Wyverne de cendre

1. Trouvez un nid (`/locate structure netheryx:wyvern_nest`) ou `/give @s netheryx:ash_wyvern_egg`.
2. Posez l'œuf au sol : la wyverne éclot, apprivoisée.
- [ ] 160 PV (`/data get entity @e[type=netheryx:ash_wyvern,limit=1,sort=nearest] Health`).
- [ ] Clic droit : la monter ; avancer en regardant vers le haut → décollage ; vers le bas → piqué ; relâcher → vol plané.
- [ ] Accroupi + clic droit : attendre / suivre.
- [ ] Combat : `/summon netheryx:ember_stalker ~5 ~ ~` puis frappez-le : elle décolle, tourne, crache du feu
      (aucun bloc ne brûle), pique pour mordre, puis se repose.
- [ ] Montée : frappez une créature devant vous → elle crache du feu dessus.
- [ ] Prenez un portail du Nether : elle vous suit dans l'autre dimension.
- [ ] Éloignez-vous de 2 000 blocs (`/tp`), puis utilisez le **Sifflet** : elle revient.
- [ ] `/kill` la wyverne : son œuf revient dans votre inventaire.
- [ ] Wyverne sauvage (œuf d'apparition) : s'apprivoise avec de la viande crue ou cuite.

## 7. Boss et fin de jeu

```
/summon netheryx:onyx_sentinel
/summon netheryx:infernal_warlock
/summon netheryx:withered_champion
```
- [ ] Attaques annoncées visuellement, barres de boss, trophée lâché.
- [ ] Cœur scellé (dans le Nether uniquement) → Cœur du Nether : réveil, vagues de gardes avec bouclier, geysers.
- [ ] Butin : œuf de wyverne, gabarit et éclats abyssaux → armure abyssale (bonus d'ensemble) et Lame abyssale
      (clic droit : vague abyssale).

## 8. Objets et mécaniques

- [ ] Lanterne du Nether : contours des minerais à travers les murs pendant 10 s.
- [ ] Pierre de rappel, Voile ignifuge, Bateau et Seau d'obsidienne (naviguer sur la lave sans brûler).
- [ ] Autel des âmes (flammes des âmes autour, offrande) → enchantements du Nether.
- [ ] Nasse à magma dans un lac de lave, Chaudron infernal (potion + chaleur dessous + soufre).
- [ ] Événements : `/netheryx event start ashfall` (puis `sulfur_storm`, `eruption`) et `/netheryx event stop`.
- [ ] Progrès : onglet NetheryX des progrès (`L`).

## 9. Compatibilité

Ajoutez ensuite les mods de votre pack par petits groupes. En cas de crash, ouvrez `logs/latest.log` et cherchez la
dernière ligne `Caused by:` : elle désigne l'exception et, souvent, le mod responsable.

## 10. Configuration

`config/netheryx-common.toml` : poids des biomes, PV des boss, événements, rayon de la boussole… Modifiez une valeur,
relancez le jeu et vérifiez l'effet (la liste complète est dans la documentation, section « Configuration »).
