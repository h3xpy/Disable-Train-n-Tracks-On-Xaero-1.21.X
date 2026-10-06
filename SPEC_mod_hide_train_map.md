# Spec : mod « Hide Train Map » (NeoForge 1.21.1)

## Contexte

- Serveur multijoueur sur le modpack **All of Create Aeronautics** (CurseForge).
- **Minecraft 1.21.1**, **NeoForge 21.1.x**, **Java 21**.
- Mods concernés : **Create 6.0.x** (branche mc1.21.1), **Xaero's Minimap** et **Xaero's World Map**.
- Le fair-play de Xaero est déjà forcé côté serveur (datapack « Xaero's Map Server Utils », autorun à la connexion) : radar d'entités et mode cave désactivés. Ne pas y toucher.

## Objectif

Empêcher les joueurs de voir sur leurs cartes la « train map » intégrée à Create :
les gros traits rouges/roses (voies), les icônes de trains, les stations, et le bouton on/off de cet overlay.

Hypothèse : aucun joueur n'utilise de client modifié. En revanche, ce sont des joueurs expérimentés : un simple réglage de config ou un clic ne doit pas suffire à réactiver l'overlay.

## Contraintes

- **Ne casser aucune autre fonctionnalité** de Create, de Xaero ou des addons de trains du pack (signaux, stations, horaires, coloration des sections de signaux, etc.).
- **Ne PAS toucher à la synchronisation du réseau ferré** (`TrackGraphSync`, `GlobalRailwayManager`, `AddTrainPacket`, `TrackGraphRollCallPacket`…) : d'autres mods du pack l'utilisent probablement côté client.
- **Aucun coût de performance perceptible** (le mod doit plutôt en retirer).
- **Le mod doit être obligatoire** : le même `.jar` sur le serveur et chez les clients. Un client sans le mod doit être refusé à la connexion.
- Si une cible de mixin disparaît après une mise à jour de Create, le jeu doit **planter au lancement avec un message clair**, plutôt que de réafficher l'overlay sans prévenir.

## Ce qui a été vérifié dans le code source de Create (branche `mc1.21.1`)

Sources brutes :
- https://raw.githubusercontent.com/Creators-of-Create/Create/mc1.21.1/dev/src/main/java/com/simibubi/create/compat/trainmap/XaeroTrainMap.java
- https://raw.githubusercontent.com/Creators-of-Create/Create/mc1.21.1/dev/src/main/java/com/simibubi/create/compat/trainmap/TrainMapManager.java
- https://raw.githubusercontent.com/Creators-of-Create/Create/mc1.21.1/dev/src/main/java/com/simibubi/create/compat/trainmap/TrainMapSync.java

### Côté client : `com.simibubi.create.compat.trainmap.XaeroTrainMap`

- `public static void tick()` : si `AllConfigs.client().showTrainMapOverlay` est vrai et que la world map est ouverte, appelle `TrainMapManager.tick()` puis `TrainMapSyncClient.requestData()`, qui demande au serveur la position de tous les trains.
- `public static void onRender(GuiGraphics, GuiMap, int, int, float)` : appelé par le mixin de Create sur la world map plein écran de Xaero (`XaeroFullscreenMapMixin`). Il dessine les voies, stations et trains, plus le bouton on/off.
- `public static void mouseClick(InputEvent.MouseButton.Pre)` : gère le clic sur le bouton on/off.
- Le tracé n'est branché que sur la **world map plein écran**, pas sur la minimap de Xaero.

### Côté client : `com.simibubi.create.compat.trainmap.TrainMapManager`

- `public static boolean handleToggleWidgetClick(int, int, int, int)` : **inverse** `showTrainMapOverlay` dans la config client. Mettre `false` dans `create-client.toml` ne suffit donc pas.
- `public static boolean isToggleWidgetHovered(int, int, int, int)`.
- `public static void renderToggleWidget(GuiGraphics, int, int)`.
- Les voies et stations viennent de `CreateClient.RAILWAYS.trackNetworks`. Les positions des trains viennent de `TrainMapSyncClient.currentData`.

### Côté serveur : `com.simibubi.create.compat.trainmap.TrainMapSync`

- `public static void requestReceived(ServerPlayer sender)` : ajoute le joueur à `requestingPlayers`.
- `send(...)` envoie ensuite toutes les 5 ticks un `TrainMapSyncPacket` avec la position, le propriétaire et la destination de **tous** les trains à chaque joueur de `requestingPlayers`. Si la liste est vide, `send` ne fait rien.
- Ces données ne servent qu'à la train map.

## Ce qu'il faut implémenter

### 1. Mixins client (dans la section `client` du fichier mixins, pour qu'ils ne se chargent jamais sur un serveur dédié)

Sur `XaeroTrainMap` :
- `tick()` : `@Inject` à `HEAD`, `cancellable = true`, puis `ci.cancel()`. Le client ne demande plus jamais les positions et ne prépare plus le rendu.
- `onRender(...)` : `@Inject` à `HEAD`, cancel. Ni tracé, ni trains, ni stations, ni bouton.
- `mouseClick(...)` : `@Inject` à `HEAD`, cancel. Le bouton ne peut plus être cliqué.

Sur `TrainMapManager`, en filet de sécurité qui couvre aussi les autres intégrations :
- `handleToggleWidgetClick(...)` : retourne toujours `false`.
- `isToggleWidgetHovered(...)` : retourne toujours `false`.

Optionnel : au démarrage du client, forcer `AllConfigs.client().showTrainMapOverlay.set(false)`.

**Intégration JourneyMap** : Create en a une aussi, probablement une classe du même package `compat.trainmap`, nom non vérifié. Lister les classes de ce package dans le jar de Create du pack :
- si JourneyMap est dans le pack, neutraliser ses points d'entrée de rendu et de tick de la même façon ;
- sinon, ne pas ajouter ces mixins.

### 2. Mixin serveur (section `common`/`server`)

Sur `TrainMapSync.requestReceived(ServerPlayer)` : `@Inject` à `HEAD`, cancel. Le serveur n'envoie alors plus jamais les positions des trains, même à un client qui les demanderait. Ça n'a aucun effet sur le reste, puisque ces données ne servent qu'à la train map.

### 3. Rendre le mod obligatoire

- Enregistrer un payload réseau via `RegisterPayloadHandlersEvent`, **sans** `.optional()`. Un client sans le mod doit être déconnecté avec un message clair.
- Vérifier ce comportement par un test réel : client sans le mod → connexion refusée.

### 4. Détails techniques

- Projet basé sur le **MDK NeoForge 1.21.1** (ModDevGradle), Java 21.
- Cibler les classes de Create **par chaîne** (`@Mixin(targets = "com.simibubi.create.compat.trainmap.XaeroTrainMap")`) et les méthodes **par nom**. On évite ainsi toute dépendance de compilation vers Create et Xaero. Si une méthode est surchargée, préciser la signature complète.
- Dans le fichier mixins : `"required": true` et `"injectors": { "defaultRequire": 1 }`, pour planter si une cible disparaît.
- Dans `neoforge.mods.toml` :
  - dépendance obligatoire sur `create`, côté `BOTH`, ordre `AFTER` ;
  - `displayTest` / comportement côté serveur : le mod doit être requis des deux côtés.
- `mod_id` suggéré : `hidetrainmap`.

## Avant de coder : vérifications sur le pack réel

1. Me demander, ou lire dans le dossier `mods`, la **version exacte de Create** du pack.
2. Ouvrir le jar de Create, par exemple avec `javap -p`, et confirmer que les classes et méthodes ci-dessus existent avec ces noms et signatures dans **cette** version.
3. Lister le contenu de `com/simibubi/create/compat/trainmap/` pour repérer l'intégration JourneyMap ou d'autres intégrations.

## Plan de test

1. Le serveur démarre avec le mod, et le log confirme que les mixins sont appliqués, sans erreur.
2. Client avec le mod : sur la world map de Xaero (M), aucun trait rouge, aucun train, aucune station, aucun bouton on/off.
3. Client sans le mod : connexion refusée.
4. Create fonctionne normalement :
   - assemblage et conduite de trains ;
   - horaires ;
   - stations ;
   - signaux, y compris la coloration des sections de signaux ;
   - pose de voies.
5. Les autres addons de trains du pack fonctionnent normalement.
6. Aucun changement de FPS ou de TPS.

## Hors périmètre

- Les rails visibles comme blocs de terrain à ciel ouvert sur Xaero. C'est normal, comme dans le jeu.
- Le radar et le mode cave, déjà gérés par le fair-play.
- Les aéronefs Sable / Create Aeronautics. Xaero ne les affiche que via des addons, par exemple « Create - Xaero's map » ou « Sable x Xaero Bridge ». Vérifier simplement qu'aucun n'est dans le pack.

## Question ouverte

Le joueur pense voir aussi des traits rouges sur la **minimap**. D'après le code de Create, l'overlay n'est branché que sur la world map plein écran. Si des traits apparaissent sur la minimap, c'est un autre mod qui les dessine : l'identifier dans la liste des mods du pack avant de conclure.
