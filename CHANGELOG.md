# Changelog

Toutes les évolutions notables de NetheryX sont documentées ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et le projet respecte le
[versionnage sémantique](https://semver.org/lang/fr/) (`MAJEUR.MINEUR.CORRECTIF`).

Chaque tag Git (`git tag 1.0.0 && git push origin 1.0.0`) déclenche la compilation et la publication du `.jar`
dans une Release GitHub ; les notes de version sont extraites de ce fichier.

## [1.2.0] - 2026-10-17

### Ajouté
- **4 nouveaux biomes**, chacun avec une ressource utile :
  - **Fourré de moelle** : arbres de moelle géants (piliers soudés à la voûte), lianes de moelle grimpables et lumineuses,
    bois de moelle complet, **farine de moelle** (poudre d'os sur 3x3).
  - **Gouffre de givre** : pierre de givre, pics et stalactites de **glace de givre**, cristaux de givre.
    La glace de givre fond en **source d'eau** contre une source de chaleur : enfin de l'eau dans le Nether.
  - **Enchevêtrement de verrues** : verrues géantes à bulbes lumineux, **verrulianes** dont les **fruits repoussent**
    (nourriture renouvelable, tarte aux fruits de verrue), bois de verrutige complet.
  - **Récifs de quartz** : aiguilles de quartz naturelles, **minerai de quartz riche**, verre, vitre et lampe de quartz.
- **6 créatures** (GeckoLib) :
  - **Poisson de magme** : nage dans la lave ; chair grillée (résistance au feu) et écailles.
  - **Lézard de cendre** : monture apprivoisable (baies de feu) et sellable, marche sur la lave, plus rapide sur le basalte.
  - **Porteur de lanterne** : compagnon apprivoisable (luminelles) qui éclaire vraiment (lumière dynamique).
  - **Fouisseur de basalte** : ver neutre qui creuse des tunnels et digère les minerais.
  - **Marchand piglin** : commerce en or et en onyx (graines rares, gabarits, livres du Nether, boussoles accordées).
  - **Coffre mimique** : se cache dans les donjons, bien meilleur butin qu'un coffre.
- **Caravane nomade** : nouvelle structure avec le marchand piglin, un enclos à lézards et des coffres.
- **Nasse à magma** : pêche automatique dans la lave (appâts, compatible entonnoirs).
- **Potion de plongée en lave** (écaille de poisson de magme) : vision de lave + résistance au feu.
- **Chaudron infernal** : potions en masse (chauffé par dessous, chaque soufre ajoute 2 doses).
- **Autel des âmes** et **3 enchantements du Nether** : Pas de braise, Récolte d'âmes, Ancrage.
- **Infusions d'onyx** (table de forgeron) : fonte, portée ou résilience sur n'importe quel outil, arme ou armure.
- **Armures thématiques** : armure de braise, armure spectrale, armure de carapace complète (bonus d'ensemble).
- **Lanterne de garde** : empêche l'apparition naturelle des monstres dans un rayon de 24 blocs.
- **Événements du Nether** : pluie de cendres, tempête de soufre, éruption (brouillard, particules, effets),
  commande `/netheryx event start|stop`.
- **Boussole du créateur** (créatif / opérateurs, non fabricable) : téléporte vers n'importe quelle structure ou biome.
- **Voile ignifuge** : masque les flammes plein écran quand on brûle (dans l'inventaire ou en breloque Curios, optionnel).
- **Nouvelles attaques des boss**, toutes annoncées visuellement :
  - Sentinelle d'onyx : bond écrasant, pluie de cristaux, carapace d'onyx, appel de cubes d'onyx sous 25 %.
  - Sorcier infernal : pluie de météores, clignement derrière le joueur, bouclier de braise tant que ses serviteurs vivent.
  - Champion flétri : balayage spectral, chaînes d'âmes, anneaux de pointes d'os.
- **Verticalité des donjons** : tour du trésor au sommet de la Citadelle d'onyx et balcons au-dessus de l'arène ;
  flèche du trésor sur le toit du Sanctuaire infernal.
- 2 particules (poussière de givre, éclat de quartz), 5 progrès, nouvelles pages du Codex (tactiques des boss).
- **Boss final : le Cœur du Nether** (600 PV, vole) : salves de boules de feu, pulsation qui repousse, pluies de météores
  annoncées, vagues de gardes avec bouclier à 66 % et 33 %, geysers de lave en dernière phase.
  Chaque boss de donjon lâche un **trophée unique** (Œil de sentinelle, Sceau du sorcier, Couronne du champion) ;
  les trois forment le **Cœur scellé**, qui invoque le boss par clic droit (**uniquement dans le Nether**).
- **Armure abyssale** (butin du boss final, amélioration de l'armure en onyx à la table de forgeron) : résistance au feu,
  Résistance, Force dans le Nether et immunité au Flétrissement.
- **Pomme d'onyx** : Régénération II, Absorption II, Résistance et longue résistance au feu.
- **Jungle infernale** : nouveau biome forestier, arbres infernaux géants à contreforts et cimes embrasées,
  **fougères infernales** (2 blocs), **herbe ardente** et **lianes de braise** lumineuses et grimpables.
- **Donjons repensés sur plusieurs niveaux** :
  - Citadelle d'onyx : donjon central à trois étages (grand hall, armurerie et bibliothèque, arène de la Sentinelle sous le toit).
  - Sanctuaire infernal : temple en surface et grand escalier vers une nef souterraine où attend le Sorcier.
  - Catacombes flétries : crypte supérieure (galerie, cryptes, chapelle) et crypte profonde (ossuaires, tombeau du Champion).
- **Façades des donjons** : tours et corps de garde à toits pointus, herse, tourelles et flèche du donjon, pilastres,
  corniches et vitraux de quartz (Citadelle) ; portique à colonnes, obélisques ardents, flamme sommitale et braseros
  (Sanctuaire) ; cimetière clos avec tombes, arbres morts, lanternes et bougies autour d'un mausolée à toit à deux pans
  (Catacombes).
- **Boss cloisonnés dans leur arène** (Sentinelle, Sorcier, Champion et Cœur du Nether) : ils y retournent s'ils en
  sortent, y sont ramenés s'ils s'en éloignent trop, abandonnent une cible qui fuit et se soignent hors combat.
- **Nids de wyverne** : pitons rocheux coiffés d'un nid d'os, avec un œuf garanti et une wyverne sauvage qui le garde
  (repérables avec la Boussole des âmes) ; des wyvernes sauvages errent aussi dans les Terres cendrées, les deltas de
  basalte et le Lac de verre en fusion, et s'apprivoisent avec de la viande.
- **Nids de wyverne** : l'œuf n'est plus dans le coffre ; il faut vaincre la wyverne gardienne (farouche, 120 PV),
  qui laisse son œuf en mourant. **Une seule wyverne par joueur.**
- **Wyverne** : suit son maître au sol ou en vol (elle décolle si on s'éloigne, vole ou monte), téléportée si elle est
  trop loin ; vrai jet de flammes (particules projetées, braises, fumée, gerbe à l'impact).
- **Forge de basalte** : nouveau repaire du Golem de basalte (cercle de colonnes, forge antique, coffre), repérable avec
  la boussole ; le golem reste dans sa forge.
- **Récifs de quartz** améliorés : amas de quartz au sol et sous la voûte, sols de quartz, briques de quartz et pierre
  noire polie, plus d'aiguilles, nouvelles créatures et lucioles.
- **Campement nomade** et **Jardin du Nether** entièrement refaits (tentes à rayures, chariot bâché, enclos, place du feu
  de camp ; haies, pergola, carrés de culture surélevés, fontaine de givre, serre en verre de quartz).
- **Voile ignifuge** : donne aussi la résistance au feu.
- Textures refaites : **briques de magma** (style briques du Nether), **minerai de quartz riche** (proche du vanilla).
- Lucioles plus grandes et plus nombreuses ; plus d'animaux et de créatures un peu partout.
- **Wyverne fidèle** : 160 PV une fois apprivoisée (régénération hors combat) ; elle suit son maître à travers les
  portails ; le **Sifflet de wyverne** la rappelle de n'importe où (zone non chargée ou autre dimension comprise) ; à sa
  mort, son œuf revient au maître avec son nom.
- **Boussoles** : les structures NetheryX (dont les nids de wyverne) apparaissent en tête de liste.
- **Wyverne alliée en combat** : elle s'envole seule, tourne autour de l'ennemi, crache du feu (créatures enflammées,
  aucun bloc touché) et pique pour mordre ; montée, elle crache du feu sur ce que le cavalier attaque.
- **Coffres-forts scellés** : le trésor de chaque donjon est dans un petit coffre-fort dont les six faces sont en
  maçonnerie scellée (indestructible, insensible aux explosions), fermé par une porte de Sceaux du gardien qui ne s'ouvre
  qu'à la mort du boss. Vérifié automatiquement : aucun accès possible avant.
- **6 nouvelles créatures** (GeckoLib) :
  - **Golem de basalte** (mini-boss, 160 PV, barre de boss) : frappe au sol annoncée, salves d'éclats de magma,
    résiste aux projectiles, cœur en fusion sous 50 % (Failles de magma, deltas de basalte, Lac de verre en fusion).
  - **Wyverne de cendre** : grand modèle articulé (près de 6 blocs d'envergure, ailes en trois segments avec membranes
    repliables, cou, mâchoire et queue articulés) et animations de battement d'ailes, vol plané, marche, repos et
    rugissement ; monture volante qui éclot de l'œuf lâché par le Cœur du Nether ; on la dirige du regard,
    elle obéit à son propriétaire (attendre / suivre), chute lente si l'on descend en vol.
  - **Lucioles de braise** et **lucioles des âmes** : essaims lumineux ; une fiole les capture dans un **bocal** qui
    éclaire comme une lanterne.
  - **Cochon de blaze** : animal paisible, poudre de blaze renouvelable, côtelettes (grillées : résistance au feu).
  - **Escargot de cristal** : se tond aux cisailles pour des éclats d'onyx qui repoussent.
- **2 nouveaux biomes** :
  - **Lac de verre en fusion** : bassins de lave cerclés d'obsidienne, îlots d'obsidienne sur la mer de lave,
    aiguilles d'obsidienne, sols de **verre en fusion** et **cristaux en fusion**.
  - **Forêt pétrifiée d'onyx** : arbres en **bois pétrifié** couronnés de **feuillage cristallisé** et d'amas d'onyx.
- **Lanterne du Nether** : révèle à travers les murs les minerais à 16 blocs pendant 10 s (contours colorés par type).
- 3 progrès (wyverne, golem, bocal de lucioles) et pages du Codex.
- **Lame abyssale** : arme ultime (13 de dégâts, 6000 de durabilité), embrase, flétrit et soigne à chaque coup ; clic droit :
  vague abyssale en cône. Recette très chère (netherite, éclats abyssaux, étoile du Nether, blocs d'onyx, épée en onyx).
- **Documentation complète** (`docs/DOCUMENTATION.md`), générée à partir des données du mod : chaque page du Codex, chaque
  bloc et objet avec sa recette et ses sources, chaque créature avec ses biomes d'apparition et son butin, les options de
  configuration.
- **Coffres mimiques dans chaque donjon** (dont les balcons de la Citadelle, qui ont aussi chacun un coffre).

### Modifié
- **Structures** : une fois le terrain autour terminé, chaque structure neuve est remise en état (blocs et air du
  gabarit) : le feuillage des arbres, les colonnes et deltas de basalte des chunks voisins n'y débordent plus. Seules
  les structures jamais visitées sont concernées (rien ne change dans une structure déjà explorée).
- **Onglet créatif** : tout le contenu est uniquement dans l'onglet NetheryX (plus d'ajouts dans les onglets vanilla).
- **Armures** : icônes et textures portées refaites dans l'esprit des armures vanilla (silhouettes simples, quatre
  tons, un seul détail de couleur par pièce) pour l'onyx, la braise, le spectre, la carapace, l'abysse et les
  bottes de magma.
- **Structures** : nouveau placement. La structure est centrée sur son point d'apparition et toute son emprise est
  vérifiée (sol à une hauteur proche et volume dégagé jusqu'à 3 blocs au-dessus de son sommet) : plus de structure
  enterrée dans la roche, coupée par un plafond ou un mur, ou suspendue au-dessus d'un ravin.
- **Cristal de wyverne** (nouvel objet) : range sa wyverne pour l'emporter partout (clic droit sur elle), la libère
  d'un clic droit sur un bloc ; nom et vie conservés, elle reste la wyverne du joueur. Détruit, il la libère ; tombé
  dans le vide, il revient à son maître.
- **Intégration JEI** (facultative) : catégories « Autel des âmes » et « Infusion d'onyx », pages d'information sur
  les objets particuliers (œuf, sifflet et cristal de wyverne, Cœur scellé, fruits, voile ignifuge…).
- **Autel des âmes** : nouvelle interface, comme une machine : emplacement de l'objet et de l'offrande, guide des
  offrandes (l'offrande reconnue est surlignée), compteur de flammes des âmes, coût, message clair sur ce qui manque,
  bouton « Infuser » et rituel de 3 secondes avec jauge (rien n'est consommé s'il est interrompu).
- **Golem de basalte** : attaques revues, toutes annoncées et contrables : onde de choc qui se propage (on saute
  par-dessus), charge en ligne droite (il reste étourdi et vulnérable s'il percute un mur), éruptions de magma sous les
  joueurs, éclats de magma à distance.
- **Wyverne** : trois nouvelles attaques : grande boule de feu (elle prend son souffle puis crache une sphère de feu qui
  explose sans casser de blocs), rafale d'ailes (repousse et ralentit) et coup de queue (tour complet). Montée, elle
  lance la boule de feu sur les cibles lointaines et balaie de la queue ce qui est sur ses flancs.
- **Fruits des arbres** : figues de braise (arbres de braise, résistance au feu) et prunes flétries (arbres flétris,
  guérissent le flétrissement) pendent sous les houppiers, mûrissent, se cueillent d'un clic droit et repoussent ; un
  fruit se replante sous un bloc de verrue, et casser un houppier peut en donner un.
- **Lézard de cendre** apprivoisé : flaire les débris antiques et le minerai d'onyx (traînée d'étincelles et message),
  protège son cavalier du feu, mue en laissant parfois une carapace de magma.
- **Lucioles** : nouveau modèle (tête, antennes, élytres, ailes, pattes, lanterne qui pulse) et beaucoup plus nombreuses
  (essaims de 8 à 14, avec des compagnes à l'apparition).
- **Enclume d'onyx** : nouvelles textures (onyx poli brossé, veines violettes, gemme incrustée sur la table).
- **Cœur du Nether** : ne régénère plus sa vie.

- **Jet de flammes de la wyverne** : un vrai flot de feu continu, façon « boule de feu suprême ». Nouvelles particules
  (`dragon_flame`, `dragon_flame_core`, `dragon_smoke`) : grandes bouffées qui gonflent, tournoient et rougissent sur un
  cœur blanc incandescent, dessinées en mélange additif (elles brillent), qui s'écrasent contre les murs en gerbe de feu
  et laissent une fumée épaisse.
- **Golem de basalte** : lâche désormais un trophée unique, la **Pierre-cœur de basalte**, nécessaire au **Cœur scellé**
  (recette `OCO` / `SXW` / `OHO`, quatre trophées de boss).
- **Jardin du Nether** refait pour s'y promener : parc ouvert de 27 x 27 avec haie basse et quatre entrées sous arches,
  allée de ronde, allées en croix sous pergolas, place centrale autour de la fontaine, serre ouverte sur deux côtés et
  carrés de culture au ras du sol.
- Infobulle du **Cœur de sentinelle** : il sert à fabriquer le Cœur infernal.
- **Onyx plus fort que la netherite** : bonus de dégâts 5 (épée : 9), vitesse 10 ; armure 4/9/7/4, robustesse 4.
  L'armure complète donne aussi la **résistance au feu**.
- **Biomes plus fréquents** : poids de région **33** par défaut (nouvelle clé `netherRegionWeightV2`) et deux points
  climatiques par biome.
- **Biomes plus organiques** : rochers et troncs couchés propres à chaque biome, sol de la Jungle varié.
- **Apparitions augmentées** : lucioles, vaches de braise, poules de cendre, cochons de blaze, escargots, crabes de magma,
  scarabées de basalte, lézards, porteurs de lanterne, ghasts de feu, traqueurs, spectres et chevaliers ; probabilité
  d'apparition des animaux à la génération des chunks triplée dans les biomes NetheryX.
- **Biomes trop proches du vanilla retirés de la génération** : Profondeurs d'onyx, Marais des âmes et Faille de magma
  (remplacés par la Forêt pétrifiée d'onyx, la Vallée des âmes et le Lac de verre en fusion ; toujours enregistrés pour
  que les anciens mondes se chargent).
- **Champion flétri** : ses coups infligent Lenteur, Faiblesse et Cécité.
- **Cache secrète** : trappe sertie dans un dallage au ras du sol (elle ne flotte plus), beaucoup plus fréquente, signalée par une colonne de fumée bleue (feu de camp des âmes) et
  repérable avec la Boussole des âmes.
- **Boussoles** (des âmes et du créateur) : pagination adaptée à la taille de l'écran, défilement à la molette.
- Citadelle d'onyx : lanternes des balcons accrochées au plafond, plus de blocs d'onyx dans la salle du boss.
- **Icônes d'armure v3** : silhouette propre à chaque set (cimier de cristal, flammes, capuche, cornes de scarabée,
  cornes abyssales), ombrage et pierres d'accent.
- **Butins des coffres fortement réduits** (donjons, trésors des boss, petites structures, coffres mimiques, coffres vanilla).
- Enclume et four en onyx retexturés d'après les textures vanilla.
- Les minerais d'onyx, de soufre et de quartz riche ainsi que les flancs des nyliums et de l'herbe du Nether sont dessinés
  par-dessus la **netherrack vanilla** (raccord parfait, y compris avec les packs de ressources).
- Icônes refaites : toutes les armures, chaudron infernal, cœur infernal, carapace de basalte, boussole des âmes,
  pierre de rappel, lame flétrie, faux des âmes, arc en onyx ; pagaies du bateau d'obsidienne en obsidienne.
- Onglet créatif : tous les objets et blocs (boussole du créateur comprise), armure de carapace regroupée.

### Corrigé
- **Nid de wyverne** : la gardienne n'apparaît plus coincée dans les blocs (bloc de magma retiré, centre du nid dégagé) ;
  une wyverne coincée dans un bloc s'en dégage seule et ne subit plus de dégâts de suffocation ni de sol brûlant.
- La wyverne suit son maître et le rejoint dans une autre dimension de façon fiable.
- Escaliers du Sanctuaire infernal et des Catacombes : la première marche part du niveau du sol (plus de marche décalée).
- Wyverne : la tête ne se lève plus au sol (les animations du cou s'ajoutaient deux fois à la pose du modèle).
- Collision du cube d'onyx (boîte de collision alignée sur le modèle).
- Les passagers du bateau d'obsidienne ne brûlent plus sur la lave.
- Animations des boss avec courbes d'accélération (GeckoLib).
- **Textures façon diamant** : outils et armes en onyx (manche du Nether et pommeau en gemme), icônes de toutes les armures
  (onyx, braise, spectrale, carapace, semelles de magma) et armures portées redessinées en plaques facettées.
- **Bloc d'onyx** redessiné dans l'esprit du bloc d'émeraude (cadre biseauté et gemme taillée).
- Gabarits d'infusion ajoutés aux trésors des donjons et des bastions.
- Protocole réseau passé en version 3.

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
