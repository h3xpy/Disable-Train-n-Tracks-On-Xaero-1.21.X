# Plan de publication — Hide Train Map (Modrinth + CurseForge)

Fichier de travail. Les textes à coller sur les sites sont **en anglais** (public international) ; le plan est en français.

---

## 0. Ce qu'il reste à faire avant de publier

| # | Action | Pourquoi |
|---|--------|----------|
| 1 | **Nom affiché** : garder `Hide Train Map` ou passer à `Create: Hide Train Map` (convention des addons Create sur CurseForge, meilleur référencement). Si tu changes, modifier `mod_name` dans `gradle.properties` et le titre du `README.md`. | Le nom sur la page doit correspondre au nom dans le jeu. |
| 2 | **Faire le plan de test du spec** sur le vrai serveur (section 6). | Ne pas publier un mod obligatoire sans avoir vérifié qu'un client sans le mod est bien refusé. |
| 3 | **Captures d'écran** (section 4). | Les deux sites mettent en avant la galerie ; c'est ce qui convainc les admins de serveur. |
| 4 | **Tag Git + release GitHub** : `git tag v1.0.0 && git push origin v1.0.0`, puis créer la release sur GitHub et y attacher le jar. | Traçabilité : chaque jar publié correspond à un commit. |

Déjà fait :
- licence **MIT** (`LICENSE`, `mod_license=MIT`) ;
- `README.md` = page du mod ;
- version `1.0.0+mc1.21.1` → fichier `build/libs/hidetrainmap-1.0.0+mc1.21.1.jar` ;
- icône (`branding/icon_512.png`, aussi embarquée dans le jar) ;
- `authors`, `displayURL`, `issueTrackerURL`, `logoFile` dans `neoforge.mods.toml` ;
- la CI GitHub construit le jar à chaque push et le met en téléchargement (onglet *Actions* → *Build* → *Artifacts*).

---

## 1. Fiche du projet (identique sur les deux sites)

| Champ | Valeur |
|-------|--------|
| Nom | Hide Train Map *(ou Create: Hide Train Map)* |
| Slug / URL | `hide-train-map` (libre sur Modrinth au 06/10/2026) |
| Loader | NeoForge |
| Version de Minecraft | 1.21.1 |
| Côtés | **Client : requis — Serveur : requis** |
| Icône | `branding/icon_512.png` (512×512) |
| Source | https://github.com/h3xpy/Disable-Train-n-Tracks-On-Xaero-1.21.X |
| Issues | https://github.com/h3xpy/Disable-Train-n-Tracks-On-Xaero-1.21.X/issues |
| Licence | MIT |

**Dépendances à déclarer**

| Mod | Type |
|-----|------|
| Create | Requis |
| Xaero's World Map | Optionnel |
| JourneyMap | Incompatible |
| FTB Chunks | Incompatible |
| Xaero Train Map | Incompatible |
| Create Track Map | Incompatible |
| Create - Xaero's map / Sable Sublevels on Xaero's Maps (`sablexaeromaps`) | Incompatible |
| Antique Atlas: Create Train Networks | Incompatible |

*(Sur CurseForge, `sablexaeromaps` est publié sous le nom « Create - Xaero's map » ; dans le jeu il s'appelle « Sable Sublevels on Xaero's Maps ».)*

---

## 2. Résumé court (≤ 200 caractères)

```
Hides Create's train map on Xaero's World Map and stops the server from sending train positions. Server-enforced: required on both sides.
```

---

## 3. Description longue (à coller sur Modrinth et CurseForge)

C'est le contenu de `README.md`, sans l'en-tête centré ni les sections *Building* et *License*. Version prête à coller :

```markdown
# Hide Train Map

**Hide Train Map** removes Create's built-in *train map* overlay from Xaero's World Map, for multiplayer servers where the rail network of other players should stay secret.

With this mod installed, players no longer see on their maps:
- the pink/red lines of every train track in the world,
- the trains and their positions, owners and destinations,
- the stations,
- the button that turns the overlay on and off.

Rails stay visible as normal blocks on the map, exactly like any other block you have explored.

## How it works

- **Client:** Create's Xaero World Map integration is turned into a no-op. Nothing is drawn, nothing is requested from the server, and the toggle button can no longer be hovered or clicked. Editing `create-client.toml` does not bring it back.
- **Server:** the server ignores train map requests, so it never sends train positions to anyone, even to a client that asks for them.
- **Required on both sides:** a client without the mod cannot join a server that has it.

## Protection against bypasses

At launch, the game refuses to start (with a clear error message) if a mod that can show Create trains or tracks on a map is installed:

- JourneyMap and FTB Chunks (Create draws its own train map on them)
- Xaero Train Map
- Create Track Map
- Create - Xaero's map / Sable Sublevels on Xaero's Maps (draws Create contraptions, including trains, on Xaero's maps)
- Antique Atlas: Create Train Networks
- any other map addon that reads Create's rail network

If a Create update ever changes the code this mod patches, the game also refuses to start, instead of silently showing the train map again.

## What is NOT affected

Everything else in Create works as usual: building and driving trains, schedules, stations, signals (including signal section colouring), track placement, and train addons. Create's rail network sync is not touched.

Performance: the mod only removes work (no map overlay rendering, no train position packets).

## Compatibility

- Minecraft 1.21.1, NeoForge 21.1.x
- Create 6.0.x (tested with 6.0.10)
- Xaero's World Map and Xaero's Minimap (optional)

## Limits

This mod is meant for normal players on vanilla-launcher setups. It is not an anti-cheat: a deliberately modified client can always read data the server sends (such as the rail network itself, which Create syncs to every client).

## For modpack makers

Put the same jar in the server and in the client pack. Remove the mods listed above first, or the game will refuse to start.
```

---

## 4. Galerie (captures à faire)

1. **Avant** : world map Xaero (M) avec la train map de Create activée (traits roses, trains, stations, bouton).
   → Fais-la dans une instance **sans** le mod.
2. **Après** : la même zone avec le mod — plus rien.
3. **L'écran d'erreur** au lancement avec un mod interdit (ex. JourneyMap ajouté) — montre que le blocage est clair.
4. **Le refus de connexion** d'un client sans le mod.

Titres conseillés : « Before », « After », « Forbidden mod detected at launch », « Client without the mod is refused ».

---

## 5. Pas à pas

### Modrinth (https://modrinth.com → Create a project)

1. **Create a project** : type *Mod*, nom, slug `hide-train-map`, visibilité *Private* pendant la préparation, résumé (section 2).
2. **Icon** : `branding/icon_512.png`.
3. **Description** : section 3.
4. **Tags** : *Utility*, *Management*, *Transportation* (Modrinth en met jusqu'à 3 en avant).
5. **Environment** : *Client and server* — requis des deux côtés.
6. **Links** : Source, Issues (section 1).
7. **License** : MIT.
8. **Content disclosure** : coche *Contains AI-generated content*. Le code et l'icône ont été produits avec une IA, et les règles de Modrinth exigent de le déclarer.
9. **Gallery** : captures de la section 4.
10. **Upload a version** :
    - Fichier : `build/libs/hidetrainmap-1.0.0+mc1.21.1.jar`
    - Nom : `1.0.0`, numéro de version : `1.0.0+mc1.21.1`, canal : *Release*
    - Loader : NeoForge, versions : 1.21.1
    - Dépendances : section 1 (Modrinth gère *required*, *optional*, *incompatible*)
    - Changelog : section 7
11. **Submit for review**. La modération Modrinth prend généralement de quelques heures à quelques jours.

### CurseForge (https://authors.curseforge.com → Create Project)

1. **Game** : Minecraft, **Class** : Mods.
2. Nom, résumé (section 2), description (section 3 ; l'éditeur accepte le Markdown).
3. **Avatar** : `branding/icon_512.png`.
4. **Categories** : *Server Utility* (principale), *Map and Information*. S'il existe une sous-catégorie d'addons Create, ajoute-la.
5. **License** : MIT. **Source** et **Issues** : liens GitHub.
6. Crée le projet, puis **Upload File** :
    - Fichier : le jar, type *Release*
    - Game versions : 1.21.1, NeoForge, Java 21
    - Display name : `Hide Train Map 1.0.0`
    - Changelog : section 7
    - **Related projects** : Create → *Required Dependency* ; Xaero's World Map → *Optional Dependency* ; JourneyMap, FTB Chunks, Xaero Train Map, Create Track Map, Create - Xaero's map, Antique Atlas: Create Train Networks → *Incompatible*
7. Ajoute les images dans l'onglet **Images**.
8. Soumets. La modération CurseForge prend souvent plusieurs heures, parfois plus.

*Pas besoin d'automatiser pour une première version. Si tu publies souvent ensuite : plugin Gradle `com.modrinth.minotaur` pour Modrinth, ou la GitHub Action `Kir-Antipov/mc-publish` qui publie sur les deux sites à chaque release GitHub.*

---

## 6. Plan de test avant publication (rappel du spec)

- [ ] Le serveur démarre avec le mod ; le log contient `Create train map disabled`.
- [ ] Client avec le mod : sur la world map (M), aucun trait, aucun train, aucune station, aucun bouton.
- [ ] Client **sans** le mod : la connexion est refusée avec un message clair.
- [ ] Client avec JourneyMap ajouté : le jeu refuse de démarrer et cite JourneyMap.
- [ ] Create fonctionne : assemblage et conduite de trains, horaires, stations, signaux (avec la coloration des sections), pose de voies.
- [ ] Les addons de trains du pack fonctionnent (Create Railways Navigator, Create Train Parts…).
- [ ] FPS et TPS inchangés.

---

## 7. Changelog 1.0.0

```markdown
Initial release.
- Hides Create's train map overlay (tracks, trains, stations, toggle button) on Xaero's World Map.
- Server no longer sends train positions for the train map.
- Required on both client and server.
- Refuses to start if a mod that can show Create trains or tracks on a map is installed.
- Refuses to start if a Create update changes the patched code.
```

---

## 8. Après la publication

- **Modpack** : si ton modpack est sur CurseForge, ajoute le mod depuis CurseForge une fois approuvé (plutôt que le jar en *overrides*), et retire `sablexaeromaps`.
- **Mises à jour de Create** : à chaque nouvelle version de Create, lance le jeu une fois. Si Hide Train Map refuse de démarrer avec « Incompatible Create version », il faut adapter les mixins puis publier une nouvelle version.
- **Projet similaire** : *Create: No Track Map* (Modrinth, `create-no-track-map`, ~600 téléchargements) oblige les joueurs à désactiver l'overlay. Hide Train Map va plus loin : il supprime l'overlay, coupe l'envoi des positions par le serveur et bloque les mods de contournement. Mets cette différence en avant si quelqu'un compare les deux.
