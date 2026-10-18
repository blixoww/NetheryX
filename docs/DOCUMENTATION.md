# NetheryX — Documentation complète

> Ce fichier est généré automatiquement par `tools/generate_resources.py` à partir des données du mod (Codex, recettes, butins, apparitions). Ne le modifiez pas à la main.

NetheryX transforme le Nether de Minecraft 1.20.1 (Forge) en une dimension à explorer : nouveaux biomes organiques, créatures, donjons à plusieurs niveaux avec boss, boss final, équipement en onyx, cultures et objets utiles.

## Sommaire

- [Premiers pas](#getting_started)
- [Onyx](#onyx)
- [Biomes](#biomes)
- [Créatures](#creatures)
- [Donjons](#structures)
- [Outils et armures](#equipment)
- [Objets utiles](#utility)
- [Cultures et nourriture](#farming)
- [Blocs de construction](#building)
- [Catalogue des blocs et objets](#catalogue)
- [Créatures : apparitions et butin](#creatures-detail)
- [Configuration](#configuration)

<a id="getting_started"></a>
## Premiers pas

### Bienvenue dans NetheryX

**NetheryX**

NetheryX transforme le Nether : **12 biomes organiques**, **26 créatures** dont trois boss de donjon, un mini-boss et un boss final, **3 grands donjons** et de petits campements, le matériau *Onyx*, de nouvelles cultures, nourritures, outils et objets utiles.

Les biomes vanilla changent aussi : nouvelles plantes et créatures un peu partout.


**Le Codex**

Ce codex est offert une fois à chaque joueur. Perdu ? Fabriquez-en un nouveau avec un *Livre* et un bloc de *Netherrack*.

Il suit la langue de votre jeu (anglais ou français).


- Établi (sans forme) : Livre + Netherrack → **Codex NetheryX**
  - Livre + Netherrack.

### Par où commencer

1. Entrez dans le Nether et cherchez les **biomes colorés** : bosquets orange, cendres grises, jungles embrasées...
2. Minez du *Soufre* et de l'*Onyx* (pioche en diamant).
3. Pillez une *Forteresse du Nether* pour les bâtons de blaze : ils servent de manche aux outils en onyx.
4. Fabriquez une *Boussole des âmes* pour trouver les structures.


**Progression**

Trois donjons, trois boss, du plus facile au plus difficile :
- **Sanctuaire infernal** : Sorcier infernal
- **Catacombes flétries** : Champion flétri
- **Citadelle d'onyx** : Sentinelle d'onyx (donne le *Cœur de sentinelle* du *Cœur infernal*)

Chaque boss lâche aussi un trophée : les trois réunis invoquent le **Cœur du Nether**, le boss final.


### Configuration

Fichier **config/netheryx-common.toml** :
- poids de la région de biomes
- bonus d'armure en onyx
- minage 3x3 du marteau
- rayon de la boussole
- destruction de blocs par les bombes
- points de vie des boss
- recharge de la Pierre de rappel
- remise de ce codex


### Événements du Nether

**Événements**

Toutes les 20 à 45 minutes, un événement secoue le Nether pendant quelques minutes :

- **Pluie de cendres** : épais brouillard gris, Spectres et Chevaliers flétris renforcés.
- **Tempête de soufre** : du soufre tombe de la voûte, ramassez-le !
- **Éruption** : dans les Lacs de verre en fusion et les deltas de basalte, bombes volcaniques, magma et minerais pleuvent.


L'*armure spectrale* complète permet de voir à travers la pluie de cendres.

Les opérateurs peuvent lancer ou arrêter un événement avec **/netheryx event start <type>** et **/netheryx event stop**. Les événements se désactivent dans la configuration.


<a id="onyx"></a>
## Onyx

### Minerai d'onyx

- **Minerai d'onyx** : Aussi fréquent que le diamant, partout dans le Nether entre Y=5 et Y=56, et plus encore dans la **Forêt pétrifiée d'onyx**. Nécessite une *pioche en diamant*. Fortune fonctionne.

- Four : Minerai d'onyx → **Onyx**
- Haut fourneau : Minerai d'onyx → **Onyx**

### Gemme d'onyx

- **Onyx** : Gemme noire entre le diamant et la netherite. Sert aux outils, armures, à la *Lanterne d'onyx* et au *Cœur infernal*. Acceptée par les balises.

- Établi : `###` / `###` / `###` (`#` = Éclat d'onyx) → **Onyx**
- Établi : `###` / `###` / `###` (`#` = Onyx) → **Bloc d'onyx**
  - 9 éclats d'onyx (des amas d'onyx) donnent aussi une gemme.

- **Amas d'onyx** : Cristaux lumineux de la Forêt pétrifiée d'onyx. Ils donnent des *éclats d'onyx* (Toucher de soie garde l'amas).

### Outils en onyx

**Plus fort que la netherite** : durabilité **4500** (netherite 2031), vitesse **10** (9), bonus de dégâts **5** (4) : une épée en onyx inflige **9**. Le marteau : 7000. Les manches sont des *bâtons de blaze*. Les objets en onyx ne brûlent pas dans la lave.


- Établi : `O` / `O` / `R` (`O` = Onyx, `R` = Bâton de blaze) → **Épée en onyx**
- Établi : `OOO` / `·R·` / `·R·` (`O` = Onyx, `R` = Bâton de blaze) → **Pioche en onyx**

- Établi : `OO` / `OR` / `·R` (`O` = Onyx, `R` = Bâton de blaze) → **Hache en onyx**
- Établi : `O` / `R` / `R` (`O` = Onyx, `R` = Bâton de blaze) → **Pelle en onyx**

- Établi : `OO` / `·R` / `·R` (`O` = Onyx, `R` = Bâton de blaze) → **Houe en onyx**

### Arc en onyx

- **Arc en onyx** : Les flèches infligent **50 % de dégâts en plus +1** et sont enflammées. 2400 de durabilité, enchantable comme l'onyx, se répare avec de l'onyx.

- Établi : `·OS` / `R·S` / `·OS` (`O` = Onyx, `R` = Bâton de blaze, `S` = Ficelle) → **Arc en onyx**

### Marteau en onyx

- **Marteau en onyx** : Mine une zone de **3x3** face à vous. Accroupissez-vous pour un seul bloc. Respecte les mods de protection.

- Établi : `OBO` / `·R·` / `·R·` (`O` = Onyx, `B` = Bloc d'onyx, `R` = Bâton de blaze) → **Marteau en onyx**

### Armure en onyx

Mieux que la netherite : protection **4/9/7/4** (netherite 3/8/6/3), robustesse **4** (3), durabilité 55x (37x). **Bonus d'ensemble** : *Résistance I* et *Résistance au feu* permanentes. Compatible avec les garnitures d'armure.


- Établi : `OOO` / `O·O` (`O` = Onyx) → **Casque en onyx**
- Établi : `O·O` / `OOO` / `OOO` (`O` = Onyx) → **Plastron en onyx**

- Établi : `OOO` / `O·O` / `O·O` (`O` = Onyx) → **Jambières en onyx**
- Établi : `O·O` / `O·O` (`O` = Onyx) → **Bottes en onyx**

### Blocs d'onyx

- Établi : `·B·` / `BSB` / `·B·` (`B` = Pierre noire polie, `S` = Éclat d'onyx) → **Onyx poli** ×4
- Établi : `##` / `##` (`#` = Onyx poli) → **Briques d'onyx** ×4

- Établi : `#··` / `##·` / `###` (`#` = Briques d'onyx) → **Escalier en briques d'onyx** ×4
- Établi : `###` (`#` = Briques d'onyx) → **Dalle en briques d'onyx** ×6

- Établi : `###` / `###` (`#` = Briques d'onyx) → **Muret en briques d'onyx** ×6

- Tailleur de pierre : Briques d'onyx → **Escalier en briques d'onyx**
- Tailleur de pierre : Briques d'onyx → **Dalle en briques d'onyx** ×2

<a id="biomes"></a>
## Biomes

### Bosquet de braise

Nylium orange, *champignons de braise* géants, racines lumineuses, *piments de blaze* et *baies de feu* sauvages.

Créatures : **Traqueurs de braise**. Donjon : **Sanctuaire infernal**.


### Terres cendrées

Dunes de cendre grise sur du basalte, arbustes calcinés et pluie de cendres.

Créatures : **Spectres de cendre**, ghasts. Donjon : **Catacombes flétries**.


### Prairie de braise

L'endroit le plus accueillant du Nether : *herbe du Nether* dorée, fleurs, luminelles et blé de cendre sauvage.

Créatures : **Vaches de braise** et **Poules de cendre** paisibles. Idéal pour installer une ferme !


### Sources sulfureuses

Roche sulfureuse jaune, *minerai de soufre* et amas de cristaux.

Créatures : **Crabes de magma** paisibles. Donjon : **Sanctuaire infernal**.


### Forêt flétrie

Champignons géants gris ornés d'os sur la *terre flétrie*.

Créatures : **Chevaliers flétris**, squelettes wither. Donjon : **Catacombes flétries**.


### Biomes vanilla

NetheryX décore aussi le Nether vanilla :
- **Désolation du Nether** : arbustes calcinés
- **Forêt carmin** : baies de feu, traqueurs
- **Forêt biscornue** : lys et phalènes des âmes
- **Vallée des âmes** : lys, tubercules, chevaliers
- **Deltas de basalte** : soufre, crabes
- Minerai d'onyx partout


### Fourré de moelle

**Fourré de moelle**

Une forêt pâle d'**arbres de moelle** sur du nylium ossifié. Quand la voûte est proche, les troncs s'y soudent en piliers naturels. Des *Lianes de moelle* lumineuses pendent des chapeaux et s'escaladent.

Ressources : bois de moelle, *Chapeaux de moelle* (transformés en *Farine de moelle*), os.


- **Farine de moelle** : De la poudre d'os sur toute une zone de **3x3** en un seul usage.

### Gouffre de givre

**Gouffre de givre**

Une plaie gelée du Nether : pierre de givre, pics de **glace de givre** dressés au sol et suspendus à la voûte, et *Cristaux de givre*. Squelettes et spectres y rôdent.


- **Glace de givre** : **La seule eau du Nether.** La glace de givre ne fond jamais seule ; placée contre une source de chaleur (lave, feu, bloc de magma, feu de camp allumé) elle fond lentement en **source d'eau**.

- Établi : `##` / `##` (`#` = Éclat de givre) → **Glace de givre**
  - 4 éclats de givre, issus des cristaux de givre.

### Enchevêtrement de verrues

**Enchevêtrement**

Des verrues géantes sur des troncs de verrutige, constellées de bulbes lumineux. Des *Verrulianes* pendent partout, chargées de **Fruits de verrue** : la nourriture renouvelable du Nether. Hoglins, piglins et **Lézards de cendre** y vivent.


- **Fruit de verrue** : Clic droit sur une liane lumineuse pour cueillir ses fruits : ils repoussent. Posez un fruit sous un plafond pour planter une liane.

### Récifs de quartz

**Récifs de quartz**

Des **aiguilles de quartz** naturelles percent les plaines de pierre noire près des mers de lave, veinées de *Minerai de quartz riche* (2 à 5 quartz chacun). La lave alentour grouille de **Poissons de magme**.


- Établi : `·Q·` / `QGQ` / `·Q·` (`Q` = Quartz du Nether, `G` = Verre) → **Verre de quartz** ×4
- Établi : `·G·` / `QLQ` / `·G·` (`G` = Verre de quartz, `Q` = Quartz block, `L` = Glowstone) → **Lampe de quartz** ×2

### Jungle infernale

**Jungle infernale**

La forêt des Enfers : d'immenses **arbres infernaux** aux racines en contreforts et aux cimes embrasées, de hautes *Fougères infernales*, des vagues d'*Herbe ardente* et de longues *Lianes de braise* lumineuses qui pendent de la voûte (elles s'escaladent).

Créatures : Traqueurs de braise, Ghasts de feu, beaucoup de Poules de cendre et de Vaches de braise.


### Lac de verre en fusion

**Lac de verre en fusion**

Une région de lave lumineuse : bassins de lave cerclés d'obsidienne, **îlots d'obsidienne** sur la mer de lave, aiguilles d'obsidienne et d'obsidienne pleureuse, et sols de *Verre en fusion* (brûlant comme le magma).

Les *Cristaux en fusion* donnent des *Éclats de verre en fusion*, utilisés pour la **Lanterne du Nether**. Attention aux **Golems de basalte**.


### Forêt pétrifiée d'onyx

**Forêt pétrifiée**

Une forêt silencieuse changée en pierre : des *Bûches pétrifiées* tordues, couronnées de *Feuillage cristallisé* et d'amas d'onyx, sur de la calcite et du basalte lisse. Riche en minerai d'onyx.

Créatures : **Escargots de cristal**, lucioles des âmes, cubes d'onyx, spectres.


<a id="creatures"></a>
## Créatures

### Traqueur de braise

- **Traqueur de braise** : Félin hostile (30 PV). Bondit et vous enflamme ; touché, il peut disparaître dans un **nuage de fumée**. Lâche de la *viande de traqueur*.

### Spectre de cendre

- **Spectre de cendre** : Esprit volant (22 PV). Son contact inflige *Cécité* et *Faiblesse*. Lâche de l'*essence de spectre*.

### Crabe de magma

- **Crabe de magma** : Paisible. S'élève avec des *baies de feu*. Lâche des *pattes de crabe* et parfois une *carapace de magma*.

### Phalène des âmes

- **Phalène des âmes** : Insecte lumineux et inoffensif des vallées et forêts du Nether. Lâche de la *poussière d'âme*.

### Scarabée de basalte

- **Scarabée de basalte** : Neutre et très cuirassé (30 PV, 10 d'armure). Tout le troupeau charge si l'un est frappé. S'élève avec de la *crème de magma*. Lâche des *carapaces de basalte*.

### Chevalier flétri

- **Chevalier flétri** : Guerrier mort-vivant (34 PV, 8 d'armure). Sa lame inflige le *Flétrissement*. Lâche rarement la *Lame flétrie* ou un crâne de squelette wither.

### Sorcier infernal (boss)

- **Sorcier infernal** : 150 PV. Lance des salves de boules de feu, se téléporte quand on le frappe, déclenche une **nova de flammes** au contact et invoque deux **Traqueurs** à mi-vie. Lâche le *Bâton du sorcier* et une *Pierre de rappel*.

**Tactique**

- **Pluie de météores** : un cercle de flammes marque le sol, des boules de feu y tombent.
- **Clignement** : réapparaît derrière les joueurs éloignés et explose en nova.
- À 50 % et 25 % : invoque des Traqueurs ; tant qu'ils vivent, son **bouclier de braise** absorbe 70 % des dégâts. Tuez-les d'abord !


### Vache de braise

- **Vache de braise** : Paisible. Se **trait au seau**, s'élève avec du *blé de cendre*. Donne du bœuf infernal (steak : 10 de faim + Résistance au feu), du cuir et parfois de la crème de magma.

### Poule de cendre

- **Poule de cendre** : Paisible. Pond un *œuf de braise* toutes les 5 à 10 minutes, plane en tombant. S'élève avec des graines du Nether.

### Ghast de feu

- **Ghast de feu** : Un ghast embrasé de 30 PV dont les boules de feu explosent plus fort. Lâche des boules de feu, des larmes de ghast et des éclats d'onyx.

### Cube d'onyx

- **Cube d'onyx** : Un cube de magma cristallisé, plus cuirassé. Se divise en cubes plus petits. Lâche de la crème de magma et des éclats d'onyx.

### Champion flétri (boss)

- **Champion flétri** : 200 PV, 12 d'armure. Un chevalier géant dont la lame flétrit. Il **fonce** sur les cibles éloignées et appelle deux Chevaliers flétris à 66 % et 33 % de vie. Garde les Catacombes flétries.

**Tactique**

- **Balayage spectral** : grand arc devant lui, inflige Flétrissement II. Restez derrière lui.
- **Chaînes d'âmes** : ramène les fuyards et les ralentit.
- **Ruée des âmes** vers les cibles éloignées.
- Sous 50 % : des **pointes d'os** jaillissent en deux anneaux autour de lui. Reculez !


### Sentinelle d'onyx (boss)

- **Sentinelle d'onyx** : 250 PV, insensible au recul. Coups, **onde de choc** et, sous la moitié de sa vie, une **rage** : plus rapide, elle fait jaillir des crocs de cristal vers vous. Lâche le *Cœur de sentinelle* et beaucoup d'onyx.

**Tactique**

- **Bond** : saute sur les joueurs éloignés et frappe le sol à la réception.
- **Pluie de cristaux** : des cercles scintillants apparaissent sous les joueurs, des crocs jaillissent 2 secondes plus tard. Bougez !
- **Carapace d'onyx** (rage) : bras croisés, 75 % de dégâts en moins pendant 4 s. Attendez.
- Sous 25 % : appelle des Cubes d'onyx.


### Poisson de magme

- **Poisson de magme cru** : Nage dans les mers de lave. Chassez-le ou attrapez-le avec une *Nasse à magma*. Ses écailles servent à brasser la *Potion de plongée en lave*.

### Lézard de cendre

- **Lézard de cendre** : Une **monture** du Nether. Apprivoisez-le avec des *Baies de feu*, sellez-le et chevauchez-le. Il marche sur la lave et court encore plus vite sur le basalte et la pierre noire.

### Porteur de lanterne

- **Porteur de lanterne** : Sa lanterne des âmes **éclaire vraiment** les alentours. Apprivoisez-le avec des *Luminelles* : il vous suit et éclaire votre chemin. Clic droit pour qu'il reste sur place.

### Fouisseur de basalte

- **Fouisseur de basalte** : Un énorme ver de roche neutre qui creuse de longs **tunnels** dans la netherrack et le basalte. Il digère les minerais : pépites, quartz, onyx, et son *Croc* (infusion de portée).

### Marchand piglin

- **Marchand piglin** : Voyage avec les **Caravanes nomades**. Commerce en *or* et en *onyx* : graines rares, nasses, gabarits d'infusion, livres d'enchantements du Nether et Boussoles des âmes déjà accordées sur un donjon.

### Coffre mimique

- **Coffre mimique** : Se cache parmi les coffres des donjons. Approchez **accroupi** pour le démasquer... ou combattez-le : son butin est bien meilleur que celui d'un coffre.

### Cœur du Nether (boss final)

**Boss final**

Chacun des trois boss de donjon lâche un trophée unique : l'*Œil de sentinelle* (Sentinelle d'onyx), le *Sceau du sorcier* (Sorcier infernal) et la *Couronne du champion* (Champion flétri). Ensemble, ils forment le *Cœur scellé*.

Clic droit avec, **uniquement dans le Nether** : le Cœur du Nether s'éveille au-dessus de vous.


- Établi : `OCO` / `SXW` / `OCO` (`O` = Bloc d'onyx, `C` = Obsidienne pleureuse, `S` = Œil de la Sentinelle, `X` = Couronne du Champion, `W` = Sceau du Sorcier) → **Cœur scellé**

- **Cœur du Nether** : 600 PV, vole. Salves de boules de feu, une **pulsation** ardente qui repousse, des **pluies de météores** annoncées. À 66 % et 33 %, il invoque des gardes et s'abrite derrière un bouclier tant qu'ils vivent. Dernière phase : **geysers de lave**.

### Golem de basalte (mini-boss)

- **Golem de basalte** : 160 PV, rôde dans les deltas de basalte et les Lacs de verre en fusion. Une **frappe au sol** annoncée par un nuage de poussière, des salves d'**éclats de magma** à distance, moitié moins de dégâts des projectiles. Sous 50 %, son cœur entre en fusion : plus rapide, il enflamme ceux qui le frappent.

### Wyverne de cendre (monture)

- **Wyverne de cendre** : Où la trouver : les **Nids de wyverne** (pitons rocheux coiffés d'un nid d'os, dans les Terres cendrées, les deltas de basalte, les Lacs de verre en fusion, les Sources sulfureuses et la Jungle infernale) contiennent un *œuf* et une wyverne sauvage ; d'autres errent dans ces biomes. Apprivoisez une wyverne sauvage avec de la *viande*, ou faites éclore l'œuf : elle vous appartient.

**Compagnon**

- Clic droit : la monter. 
- Accroupi + clic droit : attendre / suivre. 
- La viande la soigne.

**En combat**, elle s'envole seule, tourne autour de votre ennemi, crache du feu (qui enflamme les créatures, jamais les blocs) et pique pour mordre. Quand vous la montez, elle crache du feu sur ce que vous attaquez.


**Vol**

**Voler** : avancez en regardant vers le haut pour décoller, vers le bas pour piquer, relâchez pour planer doucement. Si vous descendez en plein vol, vous obtenez quelques secondes de **Chute lente**.


### Lucioles

- **Luciole de braise** : Les **Lucioles de braise** volent en essaims dans la Jungle infernale, le Bosquet et la Prairie de braise ; les **Lucioles des âmes** dans les vallées des âmes et la Forêt pétrifiée. Inoffensives.

- **Bocal de lucioles de braise** : Clic droit sur une luciole avec une *Fiole vide* pour l'attraper dans un **bocal** : une lanterne qui éclaire vraiment (braise : 15, âmes : 12).

### Cochon de blaze

- **Cochon de blaze** : Un petit cochon paisible hérissé de bâtonnets de blaze. S'élève avec des *Piments de blaze* ou des baies de feu. Il perd un peu de **poudre de blaze** toutes les 5 à 10 minutes, et donne des *Côtelettes de blaze* (grillées : résistance au feu).

### Escargot de cristal

- **Escargot de cristal** : Lent et cuirassé. Des cristaux d'onyx poussent sur sa coquille : **tondez-le** pour des éclats d'onyx, ils repoussent en quelques minutes. S'élève avec des *Luminelles*.

<a id="structures"></a>
## Donjons

### Fonctionnement des donjons

**Donjons**

Les donjons NetheryX sont bâtis sur plusieurs niveaux : gardes, générateurs et salles annexes avec coffres, puis l'**arène du boss**. Le trésor est dans un petit **coffre-fort** entièrement en *Maçonnerie scellée* (indestructible sur ses six faces) et fermé par une porte de *Sceaux du gardien* : il ne s'ouvre qu'à la mort du boss.

La *Boussole des âmes* vous mène aux donjons. Prévoyez armure, nourriture et une *Pierre de rappel* !


- **Sceau du gardien** : Indestructible. Se brise tout seul quand le boss du donjon est vaincu ; la maçonnerie du coffre-fort redevient alors de la brique ordinaire. Les boss ne quittent jamais leur arène : si vous fuyez, ils y retournent et se soignent lentement.

### Citadelle d'onyx

Forêt pétrifiée d'onyx, Forêt flétrie, Terres cendrées, Gouffre de givre, Récifs de quartz et plusieurs biomes vanilla. Une forteresse dont les quatre tours abritent des générateurs de Spectres et des coffres. Au centre se dresse un **donjon à trois étages** qu'on gravit :


**1.** Grand hall : chevaliers et générateurs de Traqueurs ; escalier le long du mur ouest. **2.** Armurerie, bibliothèque (coffres, une mimique) et le **coffre-fort scellé** au centre. **3.** Sous le toit, l'arène de la **Sentinelle d'onyx**, avec des balcons (spectres, coffres, mimiques). Vainquez-la, puis redescendez au coffre-fort.


### Sanctuaire infernal

Bosquet de braise, Sources sulfureuses, Prairie de braise, Enchevêtrement, Jungle infernale, Lac de verre en fusion et biomes vanilla. Un temple entouré de **douves de lave** : traversez le pont et survivez aux autels à Blazes de la cour. À l'intérieur, deux salles rituelles (générateurs, coffres, mimiques).


Un grand escalier **descend** vers la nef souterraine éclairée par la lave, où attend le **Sorcier infernal**. Le coffre-fort scellé s'ouvre dans le mur nord de la nef.


### Catacombes flétries

Forêt flétrie, Terres cendrées, Fourré de moelle et biomes vanilla. Seul un **mausolée** est visible en surface. Son escalier mène à la **crypte supérieure** : une galerie circulaire bordée de crânes, quatre cryptes et une chapelle (générateurs, coffres, mimiques).


Depuis la galerie sud, un second escalier descend vers la **crypte profonde** : deux ossuaires et le tombeau du **Champion flétri**. Le coffre-fort scellé est au nord du tombeau.


### Campement de voyageur

Partout dans le Nether. Une tente, un feu de camp, un coffre et un tonneau : onyx, outils, diamants, nourriture et pommes d'or.


### Jardin du Nether

Prairies, bosquets et forêts. Un potager clôturé avec une cabane : graines, nourriture, pommes d'or et houe en onyx.


### Butin vanilla

Les bastions, forteresses et portails en ruine contiennent aussi du butin NetheryX : onyx, éclats, boussoles des âmes, Pierres de rappel, élixirs, carapaces de magma, bombes de soufre et graines.


### Caravane nomade

**Caravanes**

Deux tentes, un enclos à lézards et un feu de camp : les Caravanes nomades abritent un **Marchand piglin**. Leurs coffres contiennent or, soufre, nourriture et parfois un *Gabarit d'infusion d'onyx*.


### Maçonnerie scellée

**Coffres-forts**

Chaque coffre-fort de donjon est une petite salle dont les six faces sont en *Maçonnerie scellée* : des briques marquées d'une rune discrète, indestructibles et insensibles aux explosions. Impossible de creuser autour ! À la mort du boss, les sceaux de la porte disparaissent et la maçonnerie redevient de la brique ordinaire.

Les donjons cachent aussi des **Mimiques** parmi leurs coffres.


### Cache secrète

**Cache secrète**

Petites salles au trésor enterrées, désormais fréquentes. Cherchez une **colonne de fumée bleue** qui s'élève d'un feu de camp des âmes posé sur un petit dallage de pierre noire, près d'un cairn surmonté d'une lanterne : la trappe est sertie dans le dallage. La *Boussole des âmes* peut aussi les pister.


<a id="equipment"></a>
## Outils et armures

### Faux des âmes

- **Faux des âmes** : Trésor uniquement (Citadelle d'onyx, Catacombes flétries). Chaque coup vous **soigne** d'un cœur.

### Lame flétrie

- **Lame flétrie** : Lâchée par les Chevaliers flétris. Inflige *Flétrissement II* pendant 5 secondes.

### Bâton du sorcier

- **Bâton du sorcier** : Lâché par le Sorcier infernal. Lance une *boule de feu* (256 utilisations).

### Semelles de magma

- **Semelles de magma** : Bottes qui changent la lave sous vos pieds en *croûte* pendant quelques secondes : traversez les lacs de lave !

### Plastron de carapace

- **Plastron de carapace** : Fait de carapaces de scarabée : 7 d'armure, robustesse 3 et **20 % de résistance au recul**.

### Armures thématiques

- **Plastron de braise** : **Armure de braise** (carapaces de magma). Ensemble : insensible au feu et aux sols brûlants, plus rapide sur l'herbe et le nylium.

- **Robe spectrale** : **Armure spectrale** (étoffe spectrale). Ensemble : les créatures ne vous repèrent que de près, et vous voyez à travers les cendres.

- **Casque de carapace** : **Armure de carapace** (carapaces de basalte). Ensemble : grande résistance au recul et renvoie 25 % des dégâts de mêlée.

### Infusions d'onyx

**Infusions**

À la *Table de forgeron*, un *Gabarit d'infusion d'onyx* ajoute un bonus permanent à **n'importe quel** outil, arme ou armure :
- *Crème de magma* : **Fonte**, les blocs minés sortent fondus.
- *Croc de fouisseur* : **Portée**, +1,5 bloc.
- *Onyx* : **Résilience**, l'objet se répare lentement.


- Établi : `#T#` / `#O#` / `###` (`#` = Pierre noire, `T` = Gabarit d'infusion d'onyx, `O` = Onyx) → **Gabarit d'infusion d'onyx** ×2
  - Les gabarits se trouvent dans les donjons, bastions et caravanes, puis se dupliquent.

### Autel des âmes

**Autel des âmes**

Posez un objet (ou un livre) sur l'autel, allumez au moins **2 flammes des âmes** à proximité (feu, torches, lanternes ou feux de camp des âmes), puis clic droit avec une offrande. Chaque niveau coûte 5 niveaux d'expérience.

- 2 *Carapaces de magma* : **Pas de braise** (bottes).
- 4 *Poussières d'âme* : **Récolte d'âmes** (armes).
- 2 *Carapaces de basalte* : **Ancrage** (plastron, jambières).


- Établi : `DCD` / `·P·` / `PPP` (`D` = Poussière d'âme, `C` = Obsidienne pleureuse, `P` = Pierre noire polie) → **Autel des âmes**

### Armure abyssale

**Armure abyssale**

Le Cœur du Nether lâche des *Éclats abyssaux* et un *Gabarit d'amélioration abyssale*. À la Table de forgeron, gabarit + pièce d'armure en onyx + éclat = pièce abyssale, la meilleure armure du mod.

**Ensemble complet** : résistance au feu, Résistance, Force dans le Nether et immunité au Flétrissement.


- **Amélioration abyssale** : Se duplique avec 7 éclats d'onyx et 1 éclat abyssal.

### Lame abyssale

- **Lame abyssale** : L'arme ultime : 13 de dégâts, 6000 de durabilité. Chaque coup embrase, inflige Flétrissement II et vous rend un cœur. **Clic droit** : une vague abyssale de feu devant vous (12 de dégâts, recharge 8 s).

- Établi : `NAN` / `AXA` / `OSO` (`N` = Lingot de netherite, `A` = Éclat abyssal, `X` = Étoile du Nether, `O` = Bloc d'onyx, `S` = Épée en onyx) → **Lame abyssale**
  - 2 lingots de netherite, 3 éclats abyssaux, une étoile du Nether, 2 blocs d'onyx et une épée en onyx.

<a id="utility"></a>
## Objets utiles

### Boussole des âmes

- **Boussole des âmes** : Clic droit pour ouvrir son **menu** : il liste toutes les structures (vanilla et moddées) qui peuvent exister dans votre dimension, avec une barre de recherche. Choisissez-en une et la boussole trouve la plus proche.

En main, une flèche et la distance indiquent le chemin. La structure choisie est inscrite dans l'infobulle. Fonctionne dans toutes les dimensions !


### Pierre de rappel

- **Pierre de rappel** : Maintenez l'utilisation **3 secondes** pour rejoindre un **portail du Nether** que vous avez emprunté. Avec plusieurs portails, un menu vous laisse choisir, ou votre point de réapparition. Réutilisable, avec un temps de recharge.

La pierre retient les **8 derniers portails** par lesquels vous êtes arrivé, dans toutes les dimensions. Le bouton **x** en oublie un. Un portail détruit est oublié automatiquement. Sans portail, la pierre vous ramène à votre lit ou ancre.


- Établi : `·D·` / `DOD` / `·D·` (`D` = Poussière d'âme, `O` = Obsidienne pleureuse) → **Pierre de rappel**

### Bateau d'obsidienne

- **Bateau d'obsidienne** : Un bateau qui navigue sur la **lave** comme sur l'eau. Il ne se brise jamais, même après une chute, et revient **directement dans l'inventaire** quand on le frappe. Ses passagers sont protégés de la chaleur.

- Établi : `O·O` / `OOO` (`O` = Obsidienne) → **Bateau d'obsidienne**

### Seau d'obsidienne

- **Seau d'obsidienne** : Supprime une source de **n'importe quel fluide** (lave, eau, fluides d'autres mods, blocs inondés) sans jamais se remplir. Parfait pour assécher un lac de lave avant de construire.

- Établi : `O·O` / `·C·` (`O` = Obsidienne, `C` = Obsidienne pleureuse) → **Seau d'obsidienne**

### Cœur infernal

- **Cœur infernal** : Tant qu'il est dans l'inventaire, donne la *Résistance au feu* dans le Nether. Demande le *Cœur de sentinelle* du boss.

### Élixir de lueur

- **Élixir de lueur** : À boire : *Vision nocturne* (3 min) et *Résistance au feu* (30 s).

### Soufre

- **Soufre** : S'utilise comme engrais sur les cultures du Nether (y compris la *verrue du Nether*). Donne aussi de la poudre à canon et des boules de feu.

- Établi (sans forme) : Soufre + n'importe quel(le) coals → **Poudre à canon** ×2
- Établi (sans forme) : Soufre + Poudre de blaze + n'importe quel(le) coals → **Boule de feu** ×3

- Four : Minerai de soufre → **Soufre**

### Bombe de soufre

- **Bombe de soufre** : Explosif à lancer, idéal pour creuser la netherrack. Fonctionne aussi dans les distributeurs.

### Matériaux de trésor

*Essence de spectre* : Boussole des âmes.
*Carapace de magma* : Semelles de magma.
*Carapace de basalte* : Plastron de carapace.
*Poussière d'âme* : Pierre de rappel, Élixir de lueur.
*Os flétri* : poudre d'os, teinture noire.
*Cœur de sentinelle* : Cœur infernal.


- Établi (sans forme) : Os flétri → **Poudre d'os** ×3
- Établi (sans forme) : Os flétri + Os flétri → **Black dye**

### Nasse à magma

- **Nasse à magma** : Immergez-la dans un **lac de lave** : elle attrape seule des poissons de magme et des trésors de la lave. Plus il y a de lave, plus elle est rapide ; de la *viande crue* en appât double la vitesse. Compatible entonnoirs.

- Établi : `ICI` / `C·C` / `ICI` (`I` = Iron bars, `C` = Chaîne) → **Nasse à magma**

**Plongée en lave**

*Écaille de poisson de magme* + *Potion étrange* = **Potion de plongée en lave** : résistance au feu et **Vision de lave** (voir clair et nager vite dans la lave). La redstone la prolonge.


### Chaudron infernal

- **Chaudron infernal** : Des potions en masse : versez jusqu'à 9 doses d'une potion, **chauffez-le par dessous**, puis ajoutez du *Soufre* : chaque soufre ajoute 2 doses. Remplissez des fioles pour les récupérer.

- Établi : `B·B` / `BMB` / `BBB` (`B` = Pierre noire polie, `M` = Magma block) → **Chaudron infernal**

### Voile ignifuge

- **Voile ignifuge** : Tant qu'il est **n'importe où dans l'inventaire** (ou porté dans un **emplacement breloque de Curios**), les flammes qui couvrent l'écran quand vous brûlez sont masquées. Purement visuel : vous brûlez toujours, associez-le à la résistance au feu.

- Établi : `·C·` / `CMC` / `·C·` (`C` = Étoffe spectrale, `M` = Crème de magma) → **Voile ignifuge**

### Lanterne de garde

- **Lanterne de garde** : Aucune créature hostile n'apparaît naturellement à moins de **24 blocs**. Idéale pour sécuriser une base ou une ferme du Nether.

- Établi : `OEO` / `ELE` / `OEO` (`O` = Éclat d'onyx, `E` = Essence de spectre, `L` = Lanterne des âmes) → **Lanterne de garde**

### Lanterne du Nether

- **Lanterne du Nether** : Clic droit : chaque minerai à moins de **16 blocs** est entouré **à travers les murs** pendant 10 secondes, avec une couleur par type (débris, or, onyx, soufre, quartz). Recharge de 30 s, 64 utilisations, se répare avec des éclats de verre en fusion.

- Établi : `·O·` / `MLM` / `·M·` (`O` = Onyx, `M` = Éclat de verre en fusion, `L` = Lanterne des âmes) → **Lanterne du Nether**

<a id="farming"></a>
## Cultures et nourriture

### Piment de blaze

- **Piment de blaze** : Pousse sur le *nylium de braise* ou la *terre des âmes*, sans lumière. Le manger donne la *Résistance au feu* (30 s).

- Établi (sans forme) : Piment de blaze → **Graines de piment de blaze** ×2

### Tubercule d'âme

- **Tubercule d'âme** : Se plante sur le *sable* ou la *terre des âmes*. Pousse à l'état sauvage dans la Vallée des âmes. À cuire !

- Four : Tubercule d'âme → **Tubercule d'âme cuit**

### Baies de feu

- **Baies de feu** : Buisson qui pousse sur la plupart des sols du Nether. Récolte au clic droit. Nourrit et fait se reproduire les crabes de magma.

### Blé de cendre

- **Blé de cendre** : Les graines s'obtiennent en cassant de l'*herbe du Nether* (ou du blé sauvage de la Prairie de braise). Pousse sur le nylium, l'herbe du Nether ou la terre des âmes sans lumière. Donne du pain et nourrit les vaches de braise.

- Établi : `###` (`#` = Blé de cendre) → **Pain de cendre**
- Établi (sans forme) : Blé de cendre → **Graines de blé de cendre** ×2

### Luminelle

- **Luminelle** : Un champignon lumineux présent dans tout le Nether. À manger pour une courte vision nocturne, ou en ragoût.

- Établi (sans forme) : Bol + Luminelle + Luminelle + Tubercule d'âme → **Ragoût de luminelles**

### Fleurs

Quatre fleurs du Nether poussent dans la prairie et dans les biomes vanilla : *fleur de braise*, *marguerite de cendre*, *campanule carmin* et *tulipe de soufre*. Elles donnent des teintures et vont en pot (comme les lys des âmes, luminelles et champignons).


- Établi (sans forme) : Fleur de braise → **Orange dye** ×2
- Établi (sans forme) : Marguerite de cendre → **Light gray dye** ×2

- Établi (sans forme) : Campanule carmin → **Red dye** ×2
- Établi (sans forme) : Tulipe de soufre → **Yellow dye** ×2

### Produits animaux

- Four : Bœuf infernal cru → **Steak infernal**
- Four : Poulet infernal cru → **Poulet infernal cuit**

- Four : Œuf de braise → **Œuf de braise au plat**

*Steak infernal* : 10 de faim + résistance au feu.
*Œuf de braise au plat* : peut donner Résistance.
Les vaches de braise donnent aussi du *lait*.


### Repas

- Établi (sans forme) : Bol + Piment de blaze + Tubercule d'âme cuit + Crimson fungus + Warped fungus → **Ragoût infernal**
  - Ragoût infernal : 10 de faim, Résistance au feu (1 min) et Régénération.

- Four : Viande de traqueur crue → **Viande de traqueur cuite**
- Four : Patte de crabe crue → **Patte de crabe cuite**

La *viande de traqueur cuite* rend 8 de faim et peut donner *Force*.


### Tarte aux fruits de verrue

- Établi (sans forme) : Fruit de verrue + Fruit de verrue + Blé de cendre + Œuf de braise → **Tarte aux fruits de verrue**
  - Un repas copieux qui peut donner la Régénération.

- Four : Poisson de magme cru → **Poisson de magme grillé**

### Pomme d'onyx

- Établi : `SSS` / `SAS` / `SSS` (`S` = Éclat d'onyx, `A` = Pomme dorée) → **Pomme en onyx**
  - Une pomme dorée enrobée d'onyx : Régénération II, Absorption II, Résistance et longue résistance au feu.

<a id="building"></a>
## Blocs de construction

### Bois de braise

Issu des *champignons de braise* (poudre d'os sur du nylium de braise). Ininflammable.


- Établi (sans forme) : n'importe quel(le) ember stems → **Planches de braise** ×4
- Établi : `#··` / `##·` / `###` (`#` = Planches de braise) → **Escalier de braise** ×4

- Établi : `##` / `##` / `##` (`#` = Planches de braise) → **Porte de braise** ×3
- Établi : `###` / `###` (`#` = Planches de braise) → **Trappe de braise** ×2

- Établi : `W#W` / `W#W` (`W` = Planches de braise, `#` = Bâton) → **Barrière de braise** ×3
- Établi : `#W#` / `#W#` (`W` = Planches de braise, `#` = Bâton) → **Portillon de braise**

### Bois flétri

Issu des *champignons flétris* plantés sur la *terre flétrie*. Gris, sinistre et ininflammable.


- Établi (sans forme) : n'importe quel(le) withered stems → **Planches flétries** ×4
- Établi : `#··` / `##·` / `###` (`#` = Planches flétries) → **Escalier flétri** ×4

- Établi : `##` / `##` / `##` (`#` = Planches flétries) → **Porte flétrie** ×3
- Établi : `###` / `###` (`#` = Planches flétries) → **Trappe flétrie** ×2

- Établi : `W#W` / `W#W` (`W` = Planches flétries, `#` = Bâton) → **Barrière flétrie** ×3
- Établi : `#W#` / `#W#` (`W` = Planches flétries, `#` = Bâton) → **Portillon flétri**

### Herbe du Nether

Une herbe dorée qui rend le Nether accueillant. La poudre d'os y fait pousser herbe et fleurs ; on peut y planter cultures et champignons. 4 touffes d'herbe du Nether donnent un bloc.


- Établi : `GG` / `GG` (`G` = Herbe du Nether) → **Bloc d'herbe du Nether**

### Enclume en onyx

- **Enclume en onyx** : Fonctionne comme une enclume mais **ne s'abîme jamais** et chaque opération coûte **deux fois moins d'expérience**. Moins chère : 3 onyx et 4 lingots de fer.

### Four en onyx

- **Four en onyx** : Cuit **deux fois plus vite** qu'un four, pour toutes les recettes (contrairement au haut fourneau ou au fumoir). Compatible avec les entonnoirs.

### Briques de magma

Briques lumineuses faites de blocs de magma. Contrairement au magma, elles ne brûlent pas les pieds.


- Établi : `##` / `##` (`#` = Magma block) → **Briques de magma** ×4
- Établi : `#··` / `##·` / `###` (`#` = Briques de magma) → **Escalier en briques de magma** ×4

- Établi : `###` (`#` = Briques de magma) → **Dalle en briques de magma** ×6
- Établi : `###` / `###` (`#` = Briques de magma) → **Muret en briques de magma** ×6

### Éclairages

- Établi : `SSS` / `STS` / `SSS` (`S` = Éclat d'onyx, `T` = Soul torch) → **Lanterne d'onyx**
- Établi : `·W·` / `WGW` / `·W·` (`W` = Bloc de verrue de braise, `G` = Glowstone) → **Lampe de braise** ×2

Le *nylium de braise*, les *amas d'onyx*, les *lys des âmes* et les *amas de soufre* éclairent aussi.


### Autres blocs

- Établi : `###` / `###` / `###` (`#` = Soufre) → **Bloc de soufre**
- Établi : `###` / `###` / `###` (`#` = Onyx) → **Bloc d'onyx**

Le *bloc de cendre* tombe comme le sable. La *lave croûtée* est créée par les Semelles de magma et redevient de la lave.


### Bois de moelle

- Établi (sans forme) : n'importe quel(le) marrow stems → **Planches de moelle** ×4
- Établi : `##` / `##` / `##` (`#` = Planches de moelle) → **Porte de moelle** ×3
  - Un bois pâle, blanc comme l'os.

### Bois de verrutige

- Établi (sans forme) : n'importe quel(le) wartstalk stems → **Planches de verrutige** ×4
- Établi : `##` / `##` / `##` (`#` = Planches de verrutige) → **Porte de verrutige** ×3
  - Un bois rouge profond de l'Enchevêtrement.

### Pierre de givre

- Établi : `##` / `##` (`#` = Pierre de givre) → **Briques de pierre de givre** ×4
- Établi : `###` / `###` (`#` = Briques de pierre de givre) → **Muret en briques de givre** ×6

<a id="catalogue"></a>
## Catalogue des blocs et objets

Chaque bloc et objet du mod, avec la façon de l'obtenir.

### Blocs

- **Minerai d'onyx** (`netheryx:onyx_ore`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bloc d'onyx** (`netheryx:onyx_block`)
  - Établi : `###` / `###` / `###` (`#` = Onyx) → **Bloc d'onyx**
- **Onyx poli** (`netheryx:polished_onyx`)
  - Établi : `·B·` / `BSB` / `·B·` (`B` = Pierre noire polie, `S` = Éclat d'onyx) → **Onyx poli** ×4
- **Briques d'onyx** (`netheryx:onyx_bricks`)
  - Établi : `##` / `##` (`#` = Onyx poli) → **Briques d'onyx** ×4
  - Tailleur de pierre : Onyx poli → **Briques d'onyx**
- **Escalier en briques d'onyx** (`netheryx:onyx_brick_stairs`)
  - Établi : `#··` / `##·` / `###` (`#` = Briques d'onyx) → **Escalier en briques d'onyx** ×4
  - Tailleur de pierre : Briques d'onyx → **Escalier en briques d'onyx**
  - Tailleur de pierre : Onyx poli → **Escalier en briques d'onyx**
- **Dalle en briques d'onyx** (`netheryx:onyx_brick_slab`)
  - Établi : `###` (`#` = Briques d'onyx) → **Dalle en briques d'onyx** ×6
  - Tailleur de pierre : Briques d'onyx → **Dalle en briques d'onyx** ×2
  - Tailleur de pierre : Onyx poli → **Dalle en briques d'onyx** ×2
- **Muret en briques d'onyx** (`netheryx:onyx_brick_wall`)
  - Établi : `###` / `###` (`#` = Briques d'onyx) → **Muret en briques d'onyx** ×6
  - Tailleur de pierre : Briques d'onyx → **Muret en briques d'onyx**
  - Tailleur de pierre : Onyx poli → **Muret en briques d'onyx**
- **Amas d'onyx** (`netheryx:onyx_cluster`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bloc de cendre** (`netheryx:ash_block`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Arbuste calciné** (`netheryx:charred_shrub`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Nylium de braise** (`netheryx:ember_nylium`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige de braise écorcée** (`netheryx:stripped_ember_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige de braise** (`netheryx:ember_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Hyphes de braise écorcées** (`netheryx:stripped_ember_hyphae`)
  - Établi : `##` / `##` (`#` = Tige de braise écorcée) → **Hyphes de braise écorcées** ×3
- **Hyphes de braise** (`netheryx:ember_hyphae`)
  - Établi : `##` / `##` (`#` = Tige de braise) → **Hyphes de braise** ×3
- **Planches de braise** (`netheryx:ember_planks`)
  - Établi (sans forme) : n'importe quel(le) ember stems → **Planches de braise** ×4
- **Escalier de braise** (`netheryx:ember_stairs`)
  - Établi : `#··` / `##·` / `###` (`#` = Planches de braise) → **Escalier de braise** ×4
- **Dalle de braise** (`netheryx:ember_slab`)
  - Établi : `###` (`#` = Planches de braise) → **Dalle de braise** ×6
- **Barrière de braise** (`netheryx:ember_fence`)
  - Établi : `W#W` / `W#W` (`W` = Planches de braise, `#` = Bâton) → **Barrière de braise** ×3
- **Portillon de braise** (`netheryx:ember_fence_gate`)
  - Établi : `#W#` / `#W#` (`W` = Planches de braise, `#` = Bâton) → **Portillon de braise**
- **Porte de braise** (`netheryx:ember_door`)
  - Établi : `##` / `##` / `##` (`#` = Planches de braise) → **Porte de braise** ×3
- **Trappe de braise** (`netheryx:ember_trapdoor`)
  - Établi : `###` / `###` (`#` = Planches de braise) → **Trappe de braise** ×2
- **Bouton de braise** (`netheryx:ember_button`)
  - Établi (sans forme) : Planches de braise → **Bouton de braise**
- **Plaque de pression de braise** (`netheryx:ember_pressure_plate`)
  - Établi : `##` (`#` = Planches de braise) → **Plaque de pression de braise**
- **Bloc de verrue de braise** (`netheryx:ember_wart_block`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Champignon de braise** (`netheryx:ember_fungus`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Racines de braise** (`netheryx:ember_roots`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Roche sulfureuse** (`netheryx:sulfur_rock`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Minerai de soufre** (`netheryx:sulfur_ore`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bloc de soufre** (`netheryx:sulfur_block`)
  - Établi : `###` / `###` / `###` (`#` = Soufre) → **Bloc de soufre**
- **Amas de soufre** (`netheryx:sulfur_cluster`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Terre flétrie** (`netheryx:withered_soil`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige flétrie écorcée** (`netheryx:stripped_withered_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige flétrie** (`netheryx:withered_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Hyphes flétries écorcées** (`netheryx:stripped_withered_hyphae`)
  - Établi : `##` / `##` (`#` = Tige flétrie écorcée) → **Hyphes flétries écorcées** ×3
- **Hyphes flétries** (`netheryx:withered_hyphae`)
  - Établi : `##` / `##` (`#` = Tige flétrie) → **Hyphes flétries** ×3
- **Planches flétries** (`netheryx:withered_planks`)
  - Établi (sans forme) : n'importe quel(le) withered stems → **Planches flétries** ×4
- **Escalier flétri** (`netheryx:withered_stairs`)
  - Établi : `#··` / `##·` / `###` (`#` = Planches flétries) → **Escalier flétri** ×4
- **Dalle flétrie** (`netheryx:withered_slab`)
  - Établi : `###` (`#` = Planches flétries) → **Dalle flétrie** ×6
- **Barrière flétrie** (`netheryx:withered_fence`)
  - Établi : `W#W` / `W#W` (`W` = Planches flétries, `#` = Bâton) → **Barrière flétrie** ×3
- **Portillon flétri** (`netheryx:withered_fence_gate`)
  - Établi : `#W#` / `#W#` (`W` = Planches flétries, `#` = Bâton) → **Portillon flétri**
- **Porte flétrie** (`netheryx:withered_door`)
  - Établi : `##` / `##` / `##` (`#` = Planches flétries) → **Porte flétrie** ×3
- **Trappe flétrie** (`netheryx:withered_trapdoor`)
  - Établi : `###` / `###` (`#` = Planches flétries) → **Trappe flétrie** ×2
- **Bouton flétri** (`netheryx:withered_button`)
  - Établi (sans forme) : Planches flétries → **Bouton flétri**
- **Plaque de pression flétrie** (`netheryx:withered_pressure_plate`)
  - Établi : `##` (`#` = Planches flétries) → **Plaque de pression flétrie**
- **Bloc de verrue flétrie** (`netheryx:withered_wart_block`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Champignon flétri** (`netheryx:withered_fungus`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Lys des âmes** (`netheryx:soul_lily`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Briques de magma** (`netheryx:magma_bricks`)
  - Établi : `##` / `##` (`#` = Magma block) → **Briques de magma** ×4
- **Escalier en briques de magma** (`netheryx:magma_brick_stairs`)
  - Établi : `#··` / `##·` / `###` (`#` = Briques de magma) → **Escalier en briques de magma** ×4
  - Tailleur de pierre : Briques de magma → **Escalier en briques de magma**
- **Dalle en briques de magma** (`netheryx:magma_brick_slab`)
  - Établi : `###` (`#` = Briques de magma) → **Dalle en briques de magma** ×6
  - Tailleur de pierre : Briques de magma → **Dalle en briques de magma** ×2
- **Muret en briques de magma** (`netheryx:magma_brick_wall`)
  - Établi : `###` / `###` (`#` = Briques de magma) → **Muret en briques de magma** ×6
  - Tailleur de pierre : Briques de magma → **Muret en briques de magma**
- **Lanterne d'onyx** (`netheryx:onyx_lantern`)
  - Établi : `SSS` / `STS` / `SSS` (`S` = Éclat d'onyx, `T` = Soul torch) → **Lanterne d'onyx**
- **Lampe de braise** (`netheryx:ember_lamp`)
  - Établi : `·W·` / `WGW` / `·W·` (`W` = Bloc de verrue de braise, `G` = Glowstone) → **Lampe de braise** ×2
- **Sceau du gardien** (`netheryx:guardian_seal`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bloc d'herbe du Nether** (`netheryx:nether_grass_block`)
  - Établi : `GG` / `GG` (`G` = Herbe du Nether) → **Bloc d'herbe du Nether**
- **Herbe du Nether** (`netheryx:nether_grass`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Fleur de braise** (`netheryx:ember_bloom`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Marguerite de cendre** (`netheryx:ash_daisy`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Campanule carmin** (`netheryx:crimson_bellflower`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tulipe de soufre** (`netheryx:sulfur_tulip`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Luminelle** (`netheryx:glowcap`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Enclume en onyx** (`netheryx:onyx_anvil`)
  - Établi : `OOO` / `·I·` / `III` (`O` = Onyx, `I` = Lingot de fer) → **Enclume en onyx**
- **Four en onyx** (`netheryx:onyx_furnace`)
  - Établi : `PPP` / `PFP` / `PPP` (`P` = Onyx poli, `F` = Fourneau) → **Four en onyx**
- **Nylium ossifié** (`netheryx:ossified_nylium`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Chapeau de moelle** (`netheryx:marrow_cap`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Lianes de moelle** (`netheryx:marrow_vines`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Pousses de moelle** (`netheryx:marrow_sprouts`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige de moelle écorcée** (`netheryx:stripped_marrow_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige de moelle** (`netheryx:marrow_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Hyphes de moelle écorcées** (`netheryx:stripped_marrow_hyphae`)
  - Établi : `##` / `##` (`#` = Tige de moelle écorcée) → **Hyphes de moelle écorcées** ×3
- **Hyphes de moelle** (`netheryx:marrow_hyphae`)
  - Établi : `##` / `##` (`#` = Tige de moelle) → **Hyphes de moelle** ×3
- **Planches de moelle** (`netheryx:marrow_planks`)
  - Établi (sans forme) : n'importe quel(le) marrow stems → **Planches de moelle** ×4
- **Escalier de moelle** (`netheryx:marrow_stairs`)
  - Établi : `#··` / `##·` / `###` (`#` = Planches de moelle) → **Escalier de moelle** ×4
- **Dalle de moelle** (`netheryx:marrow_slab`)
  - Établi : `###` (`#` = Planches de moelle) → **Dalle de moelle** ×6
- **Barrière de moelle** (`netheryx:marrow_fence`)
  - Établi : `W#W` / `W#W` (`W` = Planches de moelle, `#` = Bâton) → **Barrière de moelle** ×3
- **Portillon de moelle** (`netheryx:marrow_fence_gate`)
  - Établi : `#W#` / `#W#` (`W` = Planches de moelle, `#` = Bâton) → **Portillon de moelle**
- **Porte de moelle** (`netheryx:marrow_door`)
  - Établi : `##` / `##` / `##` (`#` = Planches de moelle) → **Porte de moelle** ×3
- **Trappe de moelle** (`netheryx:marrow_trapdoor`)
  - Établi : `###` / `###` (`#` = Planches de moelle) → **Trappe de moelle** ×2
- **Bouton de moelle** (`netheryx:marrow_button`)
  - Établi (sans forme) : Planches de moelle → **Bouton de moelle**
- **Plaque de pression de moelle** (`netheryx:marrow_pressure_plate`)
  - Établi : `##` (`#` = Planches de moelle) → **Plaque de pression de moelle**
- **Pierre de givre** (`netheryx:rimestone`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Briques de pierre de givre** (`netheryx:rimestone_bricks`)
  - Établi : `##` / `##` (`#` = Pierre de givre) → **Briques de pierre de givre** ×4
  - Tailleur de pierre : Pierre de givre → **Briques de pierre de givre**
- **Escalier en briques de givre** (`netheryx:rimestone_brick_stairs`)
  - Établi : `#··` / `##·` / `###` (`#` = Briques de pierre de givre) → **Escalier en briques de givre** ×4
  - Tailleur de pierre : Briques de pierre de givre → **Escalier en briques de givre**
  - Tailleur de pierre : Pierre de givre → **Escalier en briques de givre**
- **Dalle en briques de givre** (`netheryx:rimestone_brick_slab`)
  - Établi : `###` (`#` = Briques de pierre de givre) → **Dalle en briques de givre** ×6
  - Tailleur de pierre : Briques de pierre de givre → **Dalle en briques de givre** ×2
  - Tailleur de pierre : Pierre de givre → **Dalle en briques de givre** ×2
- **Muret en briques de givre** (`netheryx:rimestone_brick_wall`)
  - Établi : `###` / `###` (`#` = Briques de pierre de givre) → **Muret en briques de givre** ×6
  - Tailleur de pierre : Briques de pierre de givre → **Muret en briques de givre**
  - Tailleur de pierre : Pierre de givre → **Muret en briques de givre**
- **Glace de givre** (`netheryx:rime_ice`)
  - Établi : `##` / `##` (`#` = Éclat de givre) → **Glace de givre**
- **Cristal de givre** (`netheryx:rime_crystal`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Mousse de verrue** (`netheryx:wart_moss`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bulbe de verrue** (`netheryx:wart_bulb`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige de verrutige écorcée** (`netheryx:stripped_wartstalk_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Tige de verrutige** (`netheryx:wartstalk_stem`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Hyphes de verrutige écorcées** (`netheryx:stripped_wartstalk_hyphae`)
  - Établi : `##` / `##` (`#` = Tige de verrutige écorcée) → **Hyphes de verrutige écorcées** ×3
- **Hyphes de verrutige** (`netheryx:wartstalk_hyphae`)
  - Établi : `##` / `##` (`#` = Tige de verrutige) → **Hyphes de verrutige** ×3
- **Planches de verrutige** (`netheryx:wartstalk_planks`)
  - Établi (sans forme) : n'importe quel(le) wartstalk stems → **Planches de verrutige** ×4
- **Escalier de verrutige** (`netheryx:wartstalk_stairs`)
  - Établi : `#··` / `##·` / `###` (`#` = Planches de verrutige) → **Escalier de verrutige** ×4
- **Dalle de verrutige** (`netheryx:wartstalk_slab`)
  - Établi : `###` (`#` = Planches de verrutige) → **Dalle de verrutige** ×6
- **Barrière de verrutige** (`netheryx:wartstalk_fence`)
  - Établi : `W#W` / `W#W` (`W` = Planches de verrutige, `#` = Bâton) → **Barrière de verrutige** ×3
- **Portillon de verrutige** (`netheryx:wartstalk_fence_gate`)
  - Établi : `#W#` / `#W#` (`W` = Planches de verrutige, `#` = Bâton) → **Portillon de verrutige**
- **Porte de verrutige** (`netheryx:wartstalk_door`)
  - Établi : `##` / `##` / `##` (`#` = Planches de verrutige) → **Porte de verrutige** ×3
- **Trappe de verrutige** (`netheryx:wartstalk_trapdoor`)
  - Établi : `###` / `###` (`#` = Planches de verrutige) → **Trappe de verrutige** ×2
- **Bouton de verrutige** (`netheryx:wartstalk_button`)
  - Établi (sans forme) : Planches de verrutige → **Bouton de verrutige**
- **Plaque de pression de verrutige** (`netheryx:wartstalk_pressure_plate`)
  - Établi : `##` (`#` = Planches de verrutige) → **Plaque de pression de verrutige**
- **Minerai de quartz riche** (`netheryx:rich_quartz_ore`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Verre de quartz** (`netheryx:quartz_glass`)
  - Établi : `·Q·` / `QGQ` / `·Q·` (`Q` = Quartz du Nether, `G` = Verre) → **Verre de quartz** ×4
- **Vitre de quartz** (`netheryx:quartz_glass_pane`)
  - Établi : `###` / `###` (`#` = Verre de quartz) → **Vitre de quartz** ×16
- **Lampe de quartz** (`netheryx:quartz_lamp`)
  - Établi : `·G·` / `QLQ` / `·G·` (`G` = Verre de quartz, `Q` = Quartz block, `L` = Glowstone) → **Lampe de quartz** ×2
- **Nasse à magma** (`netheryx:lava_trap`)
  - Établi : `ICI` / `C·C` / `ICI` (`I` = Iron bars, `C` = Chaîne) → **Nasse à magma**
- **Chaudron infernal** (`netheryx:infernal_cauldron`)
  - Établi : `B·B` / `BMB` / `BBB` (`B` = Pierre noire polie, `M` = Magma block) → **Chaudron infernal**
- **Autel des âmes** (`netheryx:soul_altar`)
  - Établi : `DCD` / `·P·` / `PPP` (`D` = Poussière d'âme, `C` = Obsidienne pleureuse, `P` = Pierre noire polie) → **Autel des âmes**
- **Lanterne de garde** (`netheryx:ward_lantern`)
  - Établi : `OEO` / `ELE` / `OEO` (`O` = Éclat d'onyx, `E` = Essence de spectre, `L` = Lanterne des âmes) → **Lanterne de garde**
- **Fougere infernale** (`netheryx:hellfern`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Herbe ardente** (`netheryx:blazing_grass`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Lianes de braise** (`netheryx:ember_vines`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bocal de lucioles de braise** (`netheryx:ember_firefly_jar`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bocal de lucioles des âmes** (`netheryx:soul_firefly_jar`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Verre en fusion** (`netheryx:molten_glass`)
  - Établi : `MM` / `MM` (`M` = Éclat de verre en fusion) → **Verre en fusion**
- **Cristal en fusion** (`netheryx:molten_crystal`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Bûche pétrifiée** (`netheryx:petrified_log`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.
- **Feuillage cristallisé** (`netheryx:onyx_crystal_leaves`)
  - Se trouve dans le monde (génération, structures) ou se récupère en le minant.

### Objets

- **Lame abyssale** (`netheryx:abyssal_blade`)
  - Établi : `NAN` / `AXA` / `OSO` (`N` = Lingot de netherite, `A` = Éclat abyssal, `X` = Étoile du Nether, `O` = Bloc d'onyx, `S` = Épée en onyx) → **Lame abyssale**
- **Bottes abyssales** (`netheryx:abyssal_boots`)
  - Table de forgeron : Amélioration abyssale + Bottes en onyx + Éclat abyssal → **Bottes abyssales**
- **Plastron abyssal** (`netheryx:abyssal_chestplate`)
  - Table de forgeron : Amélioration abyssale + Plastron en onyx + Éclat abyssal → **Plastron abyssal**
- **Casque abyssal** (`netheryx:abyssal_helmet`)
  - Table de forgeron : Amélioration abyssale + Casque en onyx + Éclat abyssal → **Casque abyssal**
- **Jambières abyssales** (`netheryx:abyssal_leggings`)
  - Table de forgeron : Amélioration abyssale + Jambières en onyx + Éclat abyssal → **Jambières abyssales**
- **Éclat abyssal** (`netheryx:abyssal_shard`)
  - Trouvé dans : butin de Cœur du Nether
- **Amélioration abyssale** (`netheryx:abyssal_upgrade_template`)
  - Établi : `#T#` / `#O#` / `###` (`#` = Éclat d'onyx, `T` = Amélioration abyssale, `O` = Éclat abyssal) → **Amélioration abyssale** ×2
  - Trouvé dans : butin de Cœur du Nether
- **Boussole du créateur** (`netheryx:admin_compass`)
- **Œuf de wyverne de cendre** (`netheryx:ash_wyvern_egg`)
  - Trouvé dans : butin de Cœur du Nether, coffres : wyvern nest
- **Tubercule d'âme cuit** (`netheryx:baked_soul_tuber`)
  - Feu de camp : Tubercule d'âme → **Tubercule d'âme cuit**
  - Four : Tubercule d'âme → **Tubercule d'âme cuit**
  - Fumoir : Tubercule d'âme → **Tubercule d'âme cuit**
- **Carapace de basalte** (`netheryx:basalt_carapace`)
  - Trouvé dans : butin de Golem de basalte, butin de Scarabée de basalte
- **Piment de blaze** (`netheryx:blaze_pepper`)
- **Graines de piment de blaze** (`netheryx:blaze_pepper_seeds`)
  - Établi (sans forme) : Piment de blaze → **Graines de piment de blaze** ×2
  - Trouvé dans : butin de Traqueur de braise, coffres : nether garden, coffres : nomad caravan
- **Côtelette de blaze crue** (`netheryx:blaze_pork`)
  - Trouvé dans : butin de Cochon de blaze
- **Croc de fouisseur** (`netheryx:burrower_fang`)
  - Trouvé dans : butin de Fouisseur de basalte
- **Bottes de carapace** (`netheryx:carapace_boots`)
  - Établi : `#·#` / `#·#` (`#` = Carapace de basalte) → **Bottes de carapace**
- **Plastron de carapace** (`netheryx:carapace_chestplate`)
  - Établi : `C·C` / `CCC` / `CCC` (`C` = Carapace de basalte) → **Plastron de carapace**
- **Casque de carapace** (`netheryx:carapace_helmet`)
  - Établi : `###` / `#·#` (`#` = Carapace de basalte) → **Casque de carapace**
- **Jambières de carapace** (`netheryx:carapace_leggings`)
  - Établi : `###` / `#·#` / `#·#` (`#` = Carapace de basalte) → **Jambières de carapace**
- **Couronne du Champion** (`netheryx:champion_crown`)
  - Trouvé dans : butin de Champion flétri
- **Pain de cendre** (`netheryx:cinder_bread`)
  - Établi : `###` (`#` = Blé de cendre) → **Pain de cendre**
  - Trouvé dans : coffres : nether garden, coffres : small treasure
- **Blé de cendre** (`netheryx:cinder_wheat`)
- **Graines de blé de cendre** (`netheryx:cinder_wheat_seeds`)
  - Établi (sans forme) : Blé de cendre → **Graines de blé de cendre** ×2
  - Trouvé dans : coffres : nether garden
- **Côtelette de blaze grillée** (`netheryx:cooked_blaze_pork`)
  - Feu de camp : Côtelette de blaze crue → **Côtelette de blaze grillée**
  - Four : Côtelette de blaze crue → **Côtelette de blaze grillée**
  - Fumoir : Côtelette de blaze crue → **Côtelette de blaze grillée**
  - Trouvé dans : coffres : wyvern nest
- **Patte de crabe cuite** (`netheryx:cooked_crab_leg`)
  - Feu de camp : Patte de crabe crue → **Patte de crabe cuite**
  - Four : Patte de crabe crue → **Patte de crabe cuite**
  - Fumoir : Patte de crabe crue → **Patte de crabe cuite**
- **Steak infernal** (`netheryx:cooked_infernal_beef`)
  - Feu de camp : Bœuf infernal cru → **Steak infernal**
  - Four : Bœuf infernal cru → **Steak infernal**
  - Fumoir : Bœuf infernal cru → **Steak infernal**
  - Trouvé dans : coffres : small treasure
- **Poulet infernal cuit** (`netheryx:cooked_infernal_chicken`)
  - Feu de camp : Poulet infernal cru → **Poulet infernal cuit**
  - Four : Poulet infernal cru → **Poulet infernal cuit**
  - Fumoir : Poulet infernal cru → **Poulet infernal cuit**
  - Trouvé dans : coffres : nether garden
- **Viande de traqueur cuite** (`netheryx:cooked_stalker_meat`)
  - Feu de camp : Viande de traqueur crue → **Viande de traqueur cuite**
  - Four : Viande de traqueur crue → **Viande de traqueur cuite**
  - Fumoir : Viande de traqueur crue → **Viande de traqueur cuite**
- **Patte de crabe crue** (`netheryx:crab_leg`)
  - Trouvé dans : butin de Crabe de magma
- **Bottes de braise** (`netheryx:ember_boots`)
  - Établi : `#·#` / `#·#` (`#` = Carapace de magma) → **Bottes de braise**
- **Plastron de braise** (`netheryx:ember_chestplate`)
  - Établi : `#·#` / `###` / `###` (`#` = Carapace de magma) → **Plastron de braise**
- **Œuf de braise** (`netheryx:ember_egg`)
  - Trouvé dans : butin de Poule de cendre
- **Casque de braise** (`netheryx:ember_helmet`)
  - Établi : `###` / `#·#` (`#` = Carapace de magma) → **Casque de braise**
- **Jambières de braise** (`netheryx:ember_leggings`)
  - Établi : `###` / `#·#` / `#·#` (`#` = Carapace de magma) → **Jambières de braise**
- **Baies de feu** (`netheryx:fire_berries`)
  - Trouvé dans : coffres : nether garden
- **Voile ignifuge** (`netheryx:fire_veil`)
  - Établi : `·C·` / `CMC` / `·C·` (`C` = Étoffe spectrale, `M` = Crème de magma) → **Voile ignifuge**
- **Œuf de braise au plat** (`netheryx:fried_ember_egg`)
  - Feu de camp : Œuf de braise → **Œuf de braise au plat**
  - Four : Œuf de braise → **Œuf de braise au plat**
  - Fumoir : Œuf de braise → **Œuf de braise au plat**
  - Trouvé dans : coffres : nether garden
- **Élixir de lueur** (`netheryx:glimmer_elixir`)
  - Établi (sans forme) : Fiole + Poussière d'âme + Poudre lumineuse + Baies de feu → **Élixir de lueur**
  - Trouvé dans : coffres : infernal sanctum
- **Ragoût de luminelles** (`netheryx:glowcap_stew`)
  - Établi (sans forme) : Bol + Luminelle + Luminelle + Tubercule d'âme → **Ragoût de luminelles**
- **Poisson de magme grillé** (`netheryx:grilled_magmafish`)
  - Four : Poisson de magme cru → **Poisson de magme grillé**
  - Feu de camp : Poisson de magme cru → **Poisson de magme grillé**
  - Fumoir : Poisson de magme cru → **Poisson de magme grillé**
  - Trouvé dans : coffres : nomad caravan
- **Bœuf infernal cru** (`netheryx:infernal_beef`)
  - Trouvé dans : butin de Vache de braise
- **Poulet infernal cru** (`netheryx:infernal_chicken`)
  - Trouvé dans : butin de Poule de cendre
- **Cœur infernal** (`netheryx:infernal_heart`)
  - Établi : `OMO` / `MCM` / `OMO` (`O` = Onyx, `M` = Crème de magma, `C` = Cœur de sentinelle) → **Cœur infernal**
- **Ragoût infernal** (`netheryx:infernal_stew`)
  - Établi (sans forme) : Bol + Piment de blaze + Tubercule d'âme cuit + Crimson fungus + Warped fungus → **Ragoût infernal**
- **Carapace de magma** (`netheryx:magma_shell`)
  - Trouvé dans : butin de Crabe de magma, butin de Golem de basalte, butin de Lézard de cendre, coffres : wyvern nest
- **Semelles de magma** (`netheryx:magma_treads`)
  - Établi : `M·M` / `M·M` (`M` = Carapace de magma) → **Semelles de magma**
  - Trouvé dans : coffres : infernal sanctum, coffres : infernal sanctum vault, coffres : onyx citadel
- **Poisson de magme cru** (`netheryx:magmafish`)
  - Trouvé dans : butin de Poisson de magme cru, pêche à la nasse
- **Écaille de poisson de magme** (`netheryx:magmafish_scale`)
  - Trouvé dans : butin de Poisson de magme cru, pêche à la nasse
- **Farine de moelle** (`netheryx:marrow_meal`)
  - Établi (sans forme) : Chapeau de moelle → **Farine de moelle** ×3
- **Éclat de verre en fusion** (`netheryx:molten_shard`)
  - Trouvé dans : butin de Golem de basalte
- **Lanterne du Nether** (`netheryx:nether_lantern`)
  - Établi : `·O·` / `MLM` / `·M·` (`O` = Onyx, `M` = Éclat de verre en fusion, `L` = Lanterne des âmes) → **Lanterne du Nether**
- **Bateau d'obsidienne** (`netheryx:obsidian_boat`)
  - Établi : `O·O` / `OOO` (`O` = Obsidienne) → **Bateau d'obsidienne**
- **Seau d'obsidienne** (`netheryx:obsidian_bucket`)
  - Établi : `O·O` / `·C·` (`O` = Obsidienne, `C` = Obsidienne pleureuse) → **Seau d'obsidienne**
  - Trouvé dans : coffres : nomad caravan
- **Onyx** (`netheryx:onyx`)
  - Haut fourneau : Minerai d'onyx → **Onyx**
  - Établi (sans forme) : Bloc d'onyx → **Onyx** ×9
  - Établi : `###` / `###` / `###` (`#` = Éclat d'onyx) → **Onyx**
  - Four : Minerai d'onyx → **Onyx**
  - Trouvé dans : butin de Champion flétri, butin de Cœur du Nether, butin de Golem de basalte, butin de Sentinelle d'onyx, butin de Sorcier infernal, coffres : infernal sanctum, coffres : infernal sanctum vault, coffres : mimic, coffres : onyx citadel, coffres : onyx citadel vault, coffres : withered catacombs, coffres : withered catacombs vault
- **Pomme en onyx** (`netheryx:onyx_apple`)
  - Établi : `SSS` / `SAS` / `SSS` (`S` = Éclat d'onyx, `A` = Pomme dorée) → **Pomme en onyx**
  - Trouvé dans : butin de Cœur du Nether
- **Hache en onyx** (`netheryx:onyx_axe`)
  - Établi : `OO` / `OR` / `·R` (`O` = Onyx, `R` = Bâton de blaze) → **Hache en onyx**
- **Bottes en onyx** (`netheryx:onyx_boots`)
  - Établi : `O·O` / `O·O` (`O` = Onyx) → **Bottes en onyx**
- **Arc en onyx** (`netheryx:onyx_bow`)
  - Établi : `·OS` / `R·S` / `·OS` (`O` = Onyx, `R` = Bâton de blaze, `S` = Ficelle) → **Arc en onyx**
- **Plastron en onyx** (`netheryx:onyx_chestplate`)
  - Établi : `O·O` / `OOO` / `OOO` (`O` = Onyx) → **Plastron en onyx**
  - Trouvé dans : coffres : infernal sanctum vault, coffres : onyx citadel vault, coffres : withered catacombs vault
- **Marteau en onyx** (`netheryx:onyx_hammer`)
  - Établi : `OBO` / `·R·` / `·R·` (`O` = Onyx, `B` = Bloc d'onyx, `R` = Bâton de blaze) → **Marteau en onyx**
  - Trouvé dans : coffres : onyx citadel vault
- **Casque en onyx** (`netheryx:onyx_helmet`)
  - Établi : `OOO` / `O·O` (`O` = Onyx) → **Casque en onyx**
- **Houe en onyx** (`netheryx:onyx_hoe`)
  - Établi : `OO` / `·R` / `·R` (`O` = Onyx, `R` = Bâton de blaze) → **Houe en onyx**
  - Trouvé dans : coffres : nether garden
- **Gabarit d'infusion d'onyx** (`netheryx:onyx_infusion_template`)
  - Établi : `#T#` / `#O#` / `###` (`#` = Pierre noire, `T` = Gabarit d'infusion d'onyx, `O` = Onyx) → **Gabarit d'infusion d'onyx** ×2
  - Trouvé dans : coffres : infernal sanctum vault, coffres : mimic, coffres : nomad caravan, coffres : onyx citadel vault, coffres : withered catacombs vault
- **Jambières en onyx** (`netheryx:onyx_leggings`)
  - Établi : `OOO` / `O·O` / `O·O` (`O` = Onyx) → **Jambières en onyx**
- **Pioche en onyx** (`netheryx:onyx_pickaxe`)
  - Établi : `OOO` / `·R·` / `·R·` (`O` = Onyx, `R` = Bâton de blaze) → **Pioche en onyx**
  - Trouvé dans : coffres : infernal sanctum vault, coffres : onyx citadel vault, coffres : withered catacombs vault
- **Éclat d'onyx** (`netheryx:onyx_shard`)
  - Trouvé dans : butin de Cube d'onyx, butin de Escargot de cristal, butin de Fouisseur de basalte, butin de Ghast de feu, butin de Sentinelle d'onyx, butin de Spectre de cendre, coffres : mimic, coffres : nether garden, coffres : nomad caravan, coffres : onyx citadel, coffres : small treasure, coffres : withered catacombs, coffres : wyvern nest, pêche à la nasse
- **Pelle en onyx** (`netheryx:onyx_shovel`)
  - Établi : `O` / `R` / `R` (`O` = Onyx, `R` = Bâton de blaze) → **Pelle en onyx**
- **Épée en onyx** (`netheryx:onyx_sword`)
  - Établi : `O` / `O` / `R` (`O` = Onyx, `R` = Bâton de blaze) → **Épée en onyx**
  - Trouvé dans : coffres : infernal sanctum, coffres : infernal sanctum vault, coffres : onyx citadel, coffres : onyx citadel vault, coffres : withered catacombs vault
- **Pierre de rappel** (`netheryx:recall_stone`)
  - Établi : `·D·` / `DOD` / `·D·` (`D` = Poussière d'âme, `O` = Obsidienne pleureuse) → **Pierre de rappel**
  - Trouvé dans : butin de Sorcier infernal, coffres : infernal sanctum, coffres : onyx citadel, coffres : small treasure
- **Éclat de givre** (`netheryx:rime_shard`)
  - Trouvé dans : coffres : nomad caravan
- **Cœur scellé** (`netheryx:sealed_heart`)
  - Établi : `OCO` / `SXW` / `OCO` (`O` = Bloc d'onyx, `C` = Obsidienne pleureuse, `S` = Œil de la Sentinelle, `X` = Couronne du Champion, `W` = Sceau du Sorcier) → **Cœur scellé**
- **Cœur de sentinelle** (`netheryx:sentinel_core`)
  - Trouvé dans : butin de Sentinelle d'onyx, coffres : onyx citadel vault
- **Œil de la Sentinelle** (`netheryx:sentinel_eye`)
  - Trouvé dans : butin de Sentinelle d'onyx
- **Boussole des âmes** (`netheryx:soul_compass`)
  - Établi : `·E·` / `ECE` / `·E·` (`E` = Essence de spectre, `C` = Boussole) → **Boussole des âmes**
  - Trouvé dans : coffres : onyx citadel, coffres : small treasure
- **Poussière d'âme** (`netheryx:soul_dust`)
  - Établi (sans forme) : Lys des âmes → **Poussière d'âme**
  - Trouvé dans : butin de Luciole des âmes, butin de Phalène des âmes, butin de Porteur de lanterne, coffres : withered catacombs
- **Faux des âmes** (`netheryx:soul_scythe`)
  - Trouvé dans : coffres : onyx citadel vault, coffres : withered catacombs vault
- **Tubercule d'âme** (`netheryx:soul_tuber`)
  - Trouvé dans : coffres : nether garden
- **Viande de traqueur crue** (`netheryx:stalker_meat`)
  - Trouvé dans : butin de Traqueur de braise
- **Soufre** (`netheryx:sulfur`)
  - Haut fourneau : Minerai de soufre → **Soufre**
  - Four : Minerai de soufre → **Soufre**
  - Établi (sans forme) : Bloc de soufre → **Soufre** ×9
  - Trouvé dans : coffres : nomad caravan, coffres : small treasure
- **Bombe de soufre** (`netheryx:sulfur_bomb`)
  - Établi (sans forme) : Soufre + Soufre + Poudre à canon + Ficelle → **Bombe de soufre** ×2
- **Sceau du Sorcier** (`netheryx:warlock_sigil`)
  - Trouvé dans : butin de Sorcier infernal
- **Bâton du sorcier** (`netheryx:warlock_staff`)
  - Trouvé dans : butin de Sorcier infernal, coffres : infernal sanctum vault
- **Fruit de verrue** (`netheryx:wartfruit`)
  - Trouvé dans : coffres : nomad caravan
- **Tarte aux fruits de verrue** (`netheryx:wartfruit_tart`)
  - Établi (sans forme) : Fruit de verrue + Fruit de verrue + Blé de cendre + Œuf de braise → **Tarte aux fruits de verrue**
- **Lame flétrie** (`netheryx:withered_blade`)
  - Trouvé dans : butin de Champion flétri, butin de Chevalier flétri, coffres : withered catacombs vault
- **Os flétri** (`netheryx:withered_bone`)
  - Trouvé dans : butin de Champion flétri, butin de Chevalier flétri, coffres : withered catacombs
- **Bottes spectrales** (`netheryx:wraith_boots`)
  - Établi : `#·#` / `#·#` (`#` = Étoffe spectrale) → **Bottes spectrales**
- **Étoffe spectrale** (`netheryx:wraith_cloth`)
  - Établi : `EWE` (`E` = Essence de spectre, `W` = White wool) → **Étoffe spectrale** ×2
- **Essence de spectre** (`netheryx:wraith_essence`)
  - Trouvé dans : butin de Spectre de cendre, coffres : withered catacombs
- **Capuche spectrale** (`netheryx:wraith_hood`)
  - Établi : `###` / `#·#` (`#` = Étoffe spectrale) → **Capuche spectrale**
- **Jambières spectrales** (`netheryx:wraith_leggings`)
  - Établi : `###` / `#·#` / `#·#` (`#` = Étoffe spectrale) → **Jambières spectrales**
- **Robe spectrale** (`netheryx:wraith_robe`)
  - Établi : `#·#` / `###` / `###` (`#` = Étoffe spectrale) → **Robe spectrale**

### Œufs d'apparition (créatif)

- **Œuf d'apparition de traqueur de braise** (`netheryx:ember_stalker_spawn_egg`)
- **Œuf d'apparition de spectre de cendre** (`netheryx:ash_wraith_spawn_egg`)
- **Œuf d'apparition de sentinelle d'onyx** (`netheryx:onyx_sentinel_spawn_egg`)
- **Œuf d'apparition de crabe de magma** (`netheryx:magma_crab_spawn_egg`)
- **Œuf d'apparition de phalène des âmes** (`netheryx:soul_moth_spawn_egg`)
- **Œuf d'apparition de chevalier flétri** (`netheryx:withered_knight_spawn_egg`)
- **Œuf d'apparition de scarabée de basalte** (`netheryx:basalt_beetle_spawn_egg`)
- **Œuf d'apparition de sorcier infernal** (`netheryx:infernal_warlock_spawn_egg`)
- **Œuf d'apparition de champion flétri** (`netheryx:withered_champion_spawn_egg`)
- **Œuf d'apparition de vache de braise** (`netheryx:ember_cow_spawn_egg`)
- **Œuf d'apparition de poule de cendre** (`netheryx:cinder_hen_spawn_egg`)
- **Œuf d'apparition de ghast de feu** (`netheryx:fire_ghast_spawn_egg`)
- **Œuf d'apparition de cube d'onyx** (`netheryx:onyx_cube_spawn_egg`)
- **Œuf d'apparition de poisson de magme** (`netheryx:magmafish_spawn_egg`)
- **Œuf d'apparition de marchand piglin** (`netheryx:piglin_merchant_spawn_egg`)
- **Œuf d'apparition de coffre mimique** (`netheryx:mimic_spawn_egg`)
- **Œuf d'apparition de fouisseur de basalte** (`netheryx:basalt_burrower_spawn_egg`)
- **Œuf d'apparition de porteur de lanterne** (`netheryx:lantern_bearer_spawn_egg`)
- **Œuf d'apparition de lézard de cendre** (`netheryx:cinder_lizard_spawn_egg`)
- **Œuf d'apparition du Cœur du Nether** (`netheryx:nether_heart_spawn_egg`)
- **Œuf d'apparition de golem de basalte** (`netheryx:basalt_golem_spawn_egg`)
- **Œuf d'apparition de wyverne de cendre** (`netheryx:ash_wyvern_spawn_egg`)
- **Œuf d'apparition de luciole de braise** (`netheryx:ember_firefly_spawn_egg`)
- **Œuf d'apparition de luciole des âmes** (`netheryx:soul_firefly_spawn_egg`)
- **Œuf d'apparition de cochon de blaze** (`netheryx:blaze_pig_spawn_egg`)
- **Œuf d'apparition d'escargot de cristal** (`netheryx:crystal_snail_spawn_egg`)

<a id="creatures-detail"></a>
## Créatures : apparitions et butin

### Traqueur de braise
- Apparition : Bosquet de braise, Jungle infernale, tout le Nether
- Butin : Graines de piment de blaze, Viande de traqueur crue

### Spectre de cendre
- Apparition : Forêt pétrifiée d'onyx, Gouffre de givre, Terres cendrées, tout le Nether
- Butin : Essence de spectre, Éclat d'onyx

### Sentinelle d'onyx
- Apparition : structures, invocation ou objet spécial uniquement
- Butin : Cœur de sentinelle, Onyx, Éclat d'onyx, Œil de la Sentinelle

### Crabe de magma
- Apparition : Récifs de quartz, Sources sulfureuses, tout le Nether
- Butin : Carapace de magma, Patte de crabe crue

### Phalène des âmes
- Apparition : Forêt flétrie, Fourré de moelle, Jungle infernale, Prairie de braise, tout le Nether
- Butin : Poussière d'âme

### Chevalier flétri
- Apparition : Forêt flétrie, Fourré de moelle, tout le Nether
- Butin : Charbon, Crâne de wither squelette, Lame flétrie, Os flétri

### Scarabée de basalte
- Apparition : tout le Nether
- Butin : Carapace de basalte, Crème de magma

### Sorcier infernal
- Apparition : structures, invocation ou objet spécial uniquement
- Butin : Bâton de blaze, Bâton du sorcier, Onyx, Pierre de rappel, Sceau du Sorcier

### Champion flétri
- Apparition : structures, invocation ou objet spécial uniquement
- Butin : Couronne du Champion, Crâne de wither squelette, Lame flétrie, Onyx, Os flétri

### Vache de braise
- Apparition : Enchevêtrement de verrues, Jungle infernale, Prairie de braise, Sources sulfureuses, tout le Nether
- Butin : Bœuf infernal cru, Crème de magma, Cuir

### Poule de cendre
- Apparition : Enchevêtrement de verrues, Jungle infernale, Prairie de braise, tout le Nether
- Butin : Feather, Poulet infernal cru, Œuf de braise

### Ghast de feu
- Apparition : Bosquet de braise, Jungle infernale, Lac de verre en fusion, Récifs de quartz, Terres cendrées, tout le Nether
- Butin : Boule de feu, Larme de ghast, Éclat d'onyx

### Cube d'onyx
- Apparition : Forêt pétrifiée d'onyx, tout le Nether
- Butin : Crème de magma, Éclat d'onyx

### Poisson de magme cru
- Apparition : Lac de verre en fusion, Récifs de quartz, tout le Nether
- Butin : Poisson de magme cru, Écaille de poisson de magme

### Marchand piglin
- Apparition : structures, invocation ou objet spécial uniquement
- Butin : Pépite d'or

### Coffre mimique
- Apparition : structures, invocation ou objet spécial uniquement

### Fouisseur de basalte
- Apparition : Deltas de basalte, Fourré de moelle, Terres cendrées
- Butin : Basalte, Croc de fouisseur, Pépite d'or, Quartz du Nether, Éclat d'onyx

### Porteur de lanterne
- Apparition : Forêt flétrie, Fourré de moelle, Gouffre de givre, Vallée des âmes
- Butin : Poussière d'âme

### Lézard de cendre
- Apparition : Deltas de basalte, Enchevêtrement de verrues, Jungle infernale, Prairie de braise
- Butin : Carapace de magma, Cuir

### Cœur du Nether
- Apparition : structures, invocation ou objet spécial uniquement
- Butin : Amélioration abyssale, Lingot de netherite, Onyx, Pomme en onyx, Éclat abyssal, Œuf de wyverne de cendre

### Golem de basalte
- Apparition : Deltas de basalte, Lac de verre en fusion
- Butin : Carapace de basalte, Carapace de magma, Livre, Onyx, Éclat de verre en fusion

### Wyverne de cendre
- Apparition : Deltas de basalte, Lac de verre en fusion, Terres cendrées
- Butin : Membrane de phantom

### Luciole de braise
- Apparition : Bosquet de braise, Forêt carmin, Jungle infernale, Lac de verre en fusion, Prairie de braise, tout le Nether
- Butin : Poudre lumineuse

### Luciole des âmes
- Apparition : Forêt biscornue, Forêt pétrifiée d'onyx, Vallée des âmes
- Butin : Poussière d'âme

### Cochon de blaze
- Apparition : Bosquet de braise, Désolation du Nether, Jungle infernale, Lac de verre en fusion, tout le Nether
- Butin : Côtelette de blaze crue, Poudre de blaze

### Escargot de cristal
- Apparition : Forêt pétrifiée d'onyx
- Butin : Éclat d'onyx

<a id="configuration"></a>
## Configuration

Fichier `config/netheryx-common.toml` (créé au premier lancement).

| Option | Défaut | Description |
|---|---|---|
| `onyxSetBonus` | `true` | L'armure complete en Onyx donne l'effet Resistance I. |
| `hammerAreaMining` | `true` | Le marteau en Onyx mine une zone de 3x3 (s'accroupir pour miner un seul bloc). |
| `soulCompassSearchRadius` | `64` | Rayon de recherche (en chunks) de la Boussole des ames. |
| `sulfurBombBreaksBlocks` | `true` | Les bombes de soufre detruisent les blocs. |
| `magmaTreadsRadius` | `2` | Rayon de solidification de la lave par les Semelles de magma. |
| `giveGuideOnFirstJoin` | `true` | Donne le Codex NetheryX (livre-guide Patchouli) a la premiere connexion d'un joueur. |
| `recallStoneCooldown` | `60` | Temps de recharge (en secondes) de la Pierre de rappel. |
| `sentinelMaxHealth` | `250.0D` | Points de vie de la Sentinelle d'Onyx (boss). |
| `warlockMaxHealth` | `150.0D` | Points de vie du Sorcier infernal (mini-boss). |
| `championMaxHealth` | `200.0D` | Points de vie du Champion fletri (boss des Catacombes fletries). |
| `burrowerDigs` | `true` | Le Fouisseur de basalte creuse des tunnels (respecte aussi la regle mobGriefing). |
| `netherEvents` | `true` | Active les evenements dynamiques du Nether. |
| `minDelayMinutes` | `20` | Delai minimal (en minutes) entre deux evenements. |
| `maxDelayMinutes` | `45` | Delai maximal (en minutes) entre deux evenements. |
| `wardLanternRadius` | `24` | Rayon (en blocs) dans lequel une Lanterne de garde empeche l'apparition naturelle des monstres. |

## Biomes retirés de la génération

Les biomes *Profondeurs d'onyx*, *Marais des âmes* et *Faille de magma*, trop proches des biomes vanilla, ne sont plus générés. Ils restent enregistrés pour que les mondes existants se chargent sans erreur ; leurs rôles sont repris par la Forêt pétrifiée d'onyx, la Vallée des âmes vanilla et le Lac de verre en fusion.
