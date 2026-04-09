# PREPARATION SOUTENANCE - FESTIVAL JEU MOBILE

## 1. Pitch rapide du projet

Festival Jeu Mobile est une application Android native en Kotlin + Jetpack Compose.
Son objectif est de remplacer une partie du front web par un front mobile pour la gestion d'un festival de jeux.

Le projet consomme un backend existant en Node.js / Express / Prisma, non modifiable.

Les points forts a mettre en avant :
- architecture propre en 3 couches
- gestion d'authentification par cookies JWT
- mode offline avec Room
- navigation typee avec Navigation 3
- gestion des roles et des droits d'acces
- CRUD complets sur plusieurs modules metier

Phrase de synthese possible :

> J'ai concu une application mobile offline-first qui s'appuie sur un backend existant, avec une separation nette entre l'UI, la logique metier et l'acces aux donnees.

---

## 2. Ce que fait concretement l'application

Fonctionnalites principales :
- authentification par login/password
- lecture de festivals
- creation, modification, suppression de festivals
- gestion des zones tarifaires d'un festival
- lecture de jeux
- creation, modification, suppression de jeux
- lecture de reservations
- creation, modification, suppression de reservations
- administration des utilisateurs
- adaptation des droits selon le role
- fonctionnement partiel hors ligne grace au cache local Room

Roles pris en charge :
- Benevole
- Organisateur
- SuperOrganisateur
- Admin

---

## 3. Architecture reelle du projet

### 3.1 Vue d'ensemble

Le projet suit une architecture en 3 couches :

1. UI layer
2. Domain layer
3. Data layer

Flux global :

UI Compose -> ViewModel -> Repository interface -> Repository implementation -> API / Room

### 3.2 UI layer

Cette couche contient :
- les composables Compose
- les ViewModels
- la navigation

Exemples :
- `ui/screens/festival/FestivalScreen.kt`
- `ui/screens/reservation/ReservationScreen.kt`
- `ui/navigation/AppNavHost.kt`
- `viewModel/festival/FestivalListViewModel.kt`

Responsabilites :
- afficher l'etat de l'ecran
- transmettre les actions utilisateur
- observer un `StateFlow`
- declencher les operations via le ViewModel

Important a dire :

> Les composables ne discutent jamais directement avec Retrofit ou Room. Ils passent toujours par le ViewModel.

### 3.3 Domain layer

Cette couche contient :
- les modeles metier
- les interfaces Repository

Exemples :
- `domain/model/Festival.kt`
- `domain/model/Reservation.kt`
- `domain/repository/FestivalRepository.kt`

Responsabilite :
- definir le contrat metier
- isoler l'application des details techniques

Important a dire :

> Le domaine ne depend pas d'Android, ni de Room, ni de Retrofit. Cela le rend plus stable et testable.

### 3.4 Data layer

Cette couche contient :
- les implementations concretes des repositories
- les DTOs reseau
- les entites Room
- les DAO
- Retrofit et OkHttp

Exemples :
- `data/repository/FestivalRepositoryImpl.kt`
- `data/remote/dto/FestivalDto.kt`
- `data/local/entity/FestivalEntity.kt`
- `data/local/dao/FestivalDao.kt`

Responsabilite :
- appeler l'API
- lire/ecrire dans Room
- mapper les donnees entre les couches

---

## 4. Pourquoi separer Model, DTO, Entity et DAO

### Model

Le Model represente l'objet metier utilise par l'application.

Exemple :
- `Festival`
- `Reservation`
- `Jeu`

Il est propre, lisible, et independant de Retrofit et de Room.

### DTO

Le DTO represente la forme exacte des donnees envoyees ou recues par l'API.

Exemple :
- `FestivalDto`
- `ReservationDto`

Il sert a respecter le contrat backend.

### Entity

L'Entity represente la structure de stockage dans Room.

Exemple :
- `FestivalEntity`
- `ReservationEntity`
- `JeuEntity`

Elle correspond a la structure de la base locale.

### DAO

Le DAO est l'interface d'acces a la base.

Exemple :
- `FestivalDao`
- `ReservationDao`
- `JeuDao`

Il encapsule les requetes SQL Room.

Formulation simple pour la soutenance :

> Le DTO parle au backend, l'Entity parle a Room, le Model parle au reste de l'application, et le DAO parle a la base locale.

---

## 5. Injection de dependances dans ce projet

Le projet n'utilise pas Hilt.
Il utilise une injection manuelle centralisee dans `FestivalApp.kt`.

Le fichier `FestivalApp.kt` joue le role de conteneur d'application.

Il cree une seule fois :
- `cookieDataStore`
- `festivalDatabase`
- `festivalApi`
- `authApi`
- `reservationApi`
- `reservantApi`
- `jeuApi`
- `userApi`
- `authRepository`
- `festivalRepository`
- `reservationRepository`
- `jeuRepository`
- `userRepository`

Pourquoi ce choix est defendable :
- projet scolaire de taille moyenne
- moins de complexite qu'un framework DI
- dependencies centralisees
- evite de construire Room ou Retrofit dans les ecrans

Important :

> L'objectif etait de garder une injection simple, lisible et maitrisee, sans ajouter la complexite d'un framework de DI.

---

## 6. Navigation

Le projet utilise Navigation 3.

Particularites :
- les destinations sont typees
- plusieurs routes sont des objets ou data classes `@Serializable`
- l'etat du back stack est gere par `rememberNavBackStack`

Exemples dans `AppDestination.kt` :
- `Login`
- `Festivals`
- `FestivalForm(...)`
- `FestivalDetails(...)`
- `Reservations`
- `ReservationForm(...)`
- `Jeux`
- `UserList`

Pourquoi c'est interessant :
- navigation plus sure
- moins d'erreurs qu'avec des routes String
- transport facile de petits objets serialisables dans certaines routes

Exemple defensif :

> Pour ouvrir l'ecran de modification d'un festival, j'envoie une destination typee contenant les donnees necessaires, plutot qu'une simple String de route.

---

## 7. Authentification

### 7.1 Principe backend

Le backend utilise :
- un `access_token`
- un `refresh_token`

Ces deux tokens sont envoyes en cookies HTTP-only.

### 7.2 Probleme cote mobile

Sur mobile, Retrofit ne gere pas automatiquement une vraie session persistante avec cookies comme un navigateur.

J'ai donc mis en place :
- un `PersistentCookieJar`
- un `CookieDataStore`
- un `TokenRefreshAuthenticator`

Fichiers importants :
- `data/local/preferences/CookieDataStore.kt`
- `data/local/preferences/PersistentCookieJar.kt`
- `data/remote/RetrofitInstance.kt`
- `data/remote/TokenRefreshAuthenticator.kt`

### 7.3 Ce que fait chaque composant

`CookieDataStore.kt`
- persiste les cookies dans DataStore
- persiste aussi le role utilisateur en cache

`PersistentCookieJar.kt`
- donne les cookies a OkHttp
- recupere les cookies depuis les reponses

`TokenRefreshAuthenticator.kt`
- intercepte une erreur 401
- appelle `/api/auth/refresh`
- laisse OkHttp rejouer la requete si le refresh reussit

### 7.4 Pourquoi stocker aussi le role en DataStore

Le role est stocke separement des cookies pour :
- filtrer rapidement le menu
- gerer le mode offline
- eviter de reinterroger le backend a chaque affichage

Important a preciser :

> Le role n'est pas stocke dans le cookie lui-meme. Il est stocke separement dans DataStore comme cache applicatif.

---

## 8. Pourquoi il y a un mode offline

Le projet suit une logique offline-first sur les modules principaux :
- festivals
- reservations
- jeux

Le principe est :
- lecture immediate depuis Room
- tentative de refresh reseau
- si le reseau echoue mais que le cache existe, l'utilisateur continue a voir les donnees

Cela se voit surtout dans :
- `FestivalRepositoryImpl`
- `ReservationRepositoryImpl`
- `JeuRepositoryImpl`

Pattern utilise :
- `Flow<List<T>>` pour observer Room
- `refresh()` ou `refreshJeux()` pour synchroniser depuis l'API
- `OfflineException` pour distinguer un vrai mode offline d'une autre erreur

Formulation claire :

> Room est la source de verite locale pour l'affichage, et le reseau vient rafraichir cette source quand il est disponible.

---

## 9. Detail de l'offline-first par module

### Festivals

Dans `FestivalRepositoryImpl.kt` :
- `getAll()` observe `festivalDao.observeAll()`
- `refresh()` telecharge les festivals
- pour chaque festival, l'application tente aussi de recuperer ses zones tarifaires
- les festivals et les zones sont ensuite stockes en local

Si le reseau est indisponible :
- les festivals en cache sont affiches
- l'UI passe `isOffline = true`

### Reservations

Dans `ReservationRepositoryImpl.kt` :
- `getAll()` observe `reservationDao.observeAll()`
- `refresh()` remplace le cache local par la version serveur
- les zones tarifaires d'un festival peuvent etre lues depuis Room si l'API ne repond pas

### Jeux

Dans `JeuRepositoryImpl.kt` :
- `getAllJeux()` observe Room
- `refreshJeux()` met a jour le cache local
- les suppressions et modifications mettent aussi Room a jour

---

## 10. Gestion des roles et des permissions

Le projet distingue quatre roles :
- Benevole
- Organisateur
- SuperOrganisateur
- Admin

La logique de filtrage du menu est dans `AppNavHost.kt` avec `NavBarDestination.isVisibleFor(role)`.

Regles principales :
- Admin : acces a tout
- SuperOrganisateur : tout sauf gestion des utilisateurs
- Organisateur : jeux en CRUD, festivals et reservations en lecture seule
- Benevole : acces plus limite

Important a dire :

> Le filtrage dans la navigation est une protection UX, pas une securite absolue. La vraie securite reste cote backend.

Tres bonne phrase pour impresssionner :

> J'ai volontairement separe les permissions d'affichage cote client et la verification d'autorisation cote serveur, pour ne pas confondre ergonomie et securite.

---

## 11. Ecrans et logique de formulaire

### 11.1 Festival

Le formulaire festival permet :
- creation
- modification
- lecture seule

Particularite :
- les zones tarifaires sont editees dans le meme ecran
- `nbTables` n'est pas saisi librement
- `nbTables` est calcule comme la somme des tables des zones tarifaires

C'est un bon point a expliquer :

> J'ai remonte une contrainte metier dans le formulaire : le nombre total de tables depend des zones tarifaires, il n'est donc pas saisi manuellement.

### 11.2 Reservation

Le formulaire reservation gere :
- choix du reservant
- choix du festival
- choix des zones tarifaires
- choix des jeux
- etat de suivi
- facturation et paiement

Particularite technique :
- le backend attend une valeur precise pour `etatDeSuivi`
- un mapping a ete mis en place pour normaliser les valeurs avant envoi

### 11.3 Jeu

Le module jeu possede :
- liste
- recherche
- tri
- creation
- modification
- suppression

---

## 12. Pourquoi utiliser StateFlow

Les ViewModels exposent un `StateFlow<UiState>`.

Exemple :
- `FestivalListUiState`
- `ReservationListUiState`
- `JeuListUiState`

Pourquoi `StateFlow` :
- reactif
- garde toujours la derniere valeur
- s'integre naturellement avec les coroutines
- s'observe facilement depuis Compose avec `collectAsState()`

Pourquoi pas LiveData :
- `StateFlow` est plus coherent avec les coroutines
- plus moderne
- plus facile a combiner et transformer

Formulation :

> J'ai choisi StateFlow pour avoir une gestion d'etat reactive, moderne et coherente avec les coroutines utilisees partout dans la couche data.

---

## 13. Pourquoi utiliser des coroutines et des suspend functions

Les appels reseau et base de donnees ne doivent pas bloquer le thread UI.

Donc :
- les appels API sont `suspend`
- les repositories utilisent `withContext(Dispatchers.IO)`
- les ViewModels utilisent `viewModelScope.launch`

Benefices :
- pas de freeze UI
- code sequentiel lisible
- pas de callback hell

Exemple de justification orale :

> Les coroutines me permettent d'ecrire du code asynchrone lisible comme du code synchrone, tout en gardant l'application fluide.

---

## 14. Pourquoi utiliser Room

Room sert a :
- persister les donnees localement
- supporter le mode offline
- observer automatiquement les changements de donnees

Exemples de tables locales :
- `FestivalEntity`
- `ZoneTarifaireEntity`
- `ReservationEntity`
- `JeuEntity`

Points techniques importants :
- DAOs observes via `Flow`
- `@TypeConverters` pour les types complexes
- `fallbackToDestructiveMigration()` dans ce projet

Question probable :

### Pourquoi `fallbackToDestructiveMigration()` ?

Reponse :

> Parce que le projet a beaucoup evolue pendant le developpement et l'objectif principal etait la stabilite fonctionnelle. En production, j'ecrirais de vraies migrations versionnees pour conserver les donnees utilisateur.

---

## 15. Choix techniques defendables

### 15.1 Pourquoi ne pas avoir utilise Hilt ?

Reponse defendable :

> J'ai privilegie une injection manuelle centralisee dans `FestivalApp` parce que le projet restait de taille moyenne et que cela me permettait de garder une architecture claire sans ajouter une couche de complexite supplementaire.

### 15.2 Pourquoi Room + Retrofit + Repository ?

Reponse :

> Ce trio est pertinent car Retrofit gere le reseau, Room gere le cache local, et le Repository choisit intelligemment quelle source utiliser et comment synchroniser les deux.

### 15.3 Pourquoi Navigation 3 ?

Reponse :

> Navigation 3 permet une navigation typee, plus sure et mieux adaptee a une architecture Compose recente.

### 15.4 Pourquoi ne pas stocker uniquement les tokens ?

Reponse :

> Le backend travaille deja avec des cookies HTTP-only. J'ai donc respecte ce mecanisme au lieu de transformer artificiellement l'authentification en Bearer token.

---

## 16. Questions techniques tres probables du professeur

### Q1. Pourquoi MVVM ?

Reponse :

> MVVM me permet de separer clairement l'affichage, l'etat UI et l'acces aux donnees. Le ViewModel survit aussi mieux aux changements de configuration et centralise la logique de presentation.

### Q2. Pourquoi un repository ?

Reponse :

> Le repository isole la source des donnees. Le ViewModel ne sait pas si les donnees viennent de l'API ou de Room.

### Q3. Pourquoi un `Flow` depuis Room ?

Reponse :

> Parce que Room peut reemettre automatiquement lorsqu'une table change. L'UI reste synchronisee sans recharger manuellement la liste.

### Q4. Comment gelez-vous les droits ?

Reponse :

> Cote mobile, je filtre l'UI selon le role pour eviter les actions non autorisees. Mais la validation finale reste sur le serveur.

### Q5. Que se passe-t-il si le reseau coupe ?

Reponse :

> Si le cache existe, l'application reste utilisable en lecture sur les modules supportes. Sinon, elle affiche une erreur explicite.

### Q6. Comment gerez-vous une erreur 401 ?

Reponse :

> OkHttp passe par un `Authenticator` qui appelle le endpoint de refresh et rejoue la requete si la session est recuperable.

### Q7. Pourquoi stocker aussi le role en local ?

Reponse :

> Pour afficher rapidement le bon menu, conserver l'etat de permissions et supporter le mode offline sans dependre d'un appel reseau systematique.

### Q8. Difference entre DTO et Entity ?

Reponse :

> Le DTO suit la forme de l'API, l'Entity suit la forme de la base locale. Ce ne sont pas les memes contraintes, donc je les separe.

### Q9. Pourquoi AndroidViewModel pour certains modules et ViewModel simple pour d'autres ?

Reponse honnnete :

> Les modules historiques festivals et reservations recuperent encore leur repository via `FestivalApp`, donc ils utilisent `AndroidViewModel` pour acceder a l'application. Le module jeux, ajoute plus tard dans une forme plus propre, recoit directement son repository dans le constructeur. Une amelioration future serait d'uniformiser tous les ViewModels vers l'injection constructeur.

Cette reponse est importante car elle montre que tu connais aussi les limites actuelles.

---

## 17. Limites actuelles du projet

Ne surtout pas dire "il n'y a aucune limite". Il faut montrer de la maturite technique.

Limites reelles et defendables :
- certains ViewModels utilisent encore `AndroidViewModel` au lieu d'une injection constructeur uniforme
- `fallbackToDestructiveMigration()` n'est pas adapte a une vraie production
- tous les modules ne sont pas ecrits avec exactement le meme niveau de factorisation
- le role est mis en cache localement, donc une strategie plus evoluee pourrait etre envisagee pour des cas de revocation complexes
- l'offline est fort sur la lecture, plus limite sur l'ecriture

Bonne formulation :

> J'ai priorise la robustesse fonctionnelle et l'experience utilisateur. Les prochaines evolutions seraient surtout l'uniformisation de l'injection, des migrations Room explicites et un durcissement supplementaire de la gestion de session.

---

## 18. Ce qu'il faut mettre en avant comme vraies difficultes techniques

Tu peux dire que les difficultes les plus interessantes n'etaient pas l'affichage pur mais :
- adapter un backend non modifiable
- faire cohabiter authentification par cookies et mobile
- gerer le refresh du token
- rendre l'application utilisable hors ligne
- maintenir la coherence entre Room et le backend
- gerer les droits d'acces selon les roles
- faire des formulaires complexes comme festival + zones tarifaires et reservation + jeux

Tres bonne phrase :

> La vraie difficulte du projet n'etait pas de dessiner des ecrans, mais de construire une application mobile robuste autour d'un backend existant, avec authentification par cookies, cache local et droits differencies.

---

## 19. Mini script de demo orale

### Introduction

> Mon application est un front Android natif pour la gestion d'un festival de jeux. Elle s'appuie sur un backend existant que je n'avais pas le droit de modifier. Mon travail a donc surtout consiste a concevoir une architecture mobile propre, gerer l'authentification par cookies JWT, supporter le mode hors ligne et adapter les droits selon les roles.

### Architecture

> J'ai structure l'application en trois couches : UI, Domain et Data. Les composables parlent aux ViewModels, les ViewModels parlent a des interfaces de repository, et les implementations de repository gerent Retrofit et Room.

### Offline

> L'application est offline-first sur plusieurs modules. Les listes sont d'abord lues depuis Room, puis rafraichies depuis l'API si le reseau est disponible. Cela permet de continuer a consulter festivals, reservations et jeux sans connexion si les donnees ont deja ete synchronisees.

### Auth

> Le backend utilise des cookies JWT. J'ai donc mis en place un CookieJar persistant, un DataStore pour garder la session et le role en cache, et un Authenticator OkHttp pour rafraichir automatiquement le token.

### Roles

> L'application adapte l'interface selon le role. Par exemple, un organisateur peut gerer les jeux mais seulement lire les festivals et reservations, alors qu'un administrateur a tous les droits, y compris la gestion des utilisateurs.

### Conclusion

> Ce projet m'a surtout appris a construire une application mobile reelle autour de contraintes backend fortes, en priorisant la separation des responsabilites, la robustesse de la session et la continuitE de service hors ligne.

---

## 20. Reponses courtes si le professeur insiste

### "Pourquoi pas une seule classe pour tout ?"

> Parce que les contraintes de l'API, de la base locale et du metier ne sont pas les memes.

### "Pourquoi Room au lieu de SharedPreferences ?"

> Parce que je gere des structures relationnelles et des listes observables, pas juste quelques preferences simples.

### "Pourquoi DataStore en plus de Room ?"

> DataStore sert aux petites donnees de session et de configuration. Room sert aux vraies donnees metier structurees.

### "Pourquoi l'UI change automatiquement ?"

> Parce que Compose observe des `StateFlow`, eux-memes alimentes par des `Flow` Room.

### "Pourquoi ne pas tout faire en direct depuis l'API ?"

> Parce que je perds l'offline, le cache local, et une partie de la stabilite de l'application.

---

## 21. Derniers conseils de soutenance

- Ne recite pas le fichier mot a mot.
- Explique en partant d'un exemple concret.
- Si on te demande "pourquoi", parle de responsabilite, robustesse, testabilite et maintenabilite.
- Si on te demande "comment", cite un fichier reel du projet.
- Si on te demande une faiblesse, reponds honnetement puis donne la piste d'amelioration.

Les fichiers a connaitre par coeur :
- `FestivalApp.kt`
- `AppNavHost.kt`
- `RetrofitInstance.kt`
- `TokenRefreshAuthenticator.kt`
- `CookieDataStore.kt`
- `FestivalRepositoryImpl.kt`
- `ReservationRepositoryImpl.kt`
- `JeuRepositoryImpl.kt`
- `FestivalDatabase.kt`
- `AppDestination.kt`

---

## 22. Resume ultra-court a memoriser

> Application Android native en Kotlin et Compose, architecture 3 couches, repositories entre UI et donnees, authentification par cookies JWT avec refresh automatique, cache local Room pour le mode offline, et gestion des permissions selon le role utilisateur.
