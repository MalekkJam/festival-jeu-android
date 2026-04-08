# Festival Jeu Mobile — Application Android

## Vue d'ensemble

**Festival Jeu Mobile** est une application mobile Android native développée en **Kotlin + Jetpack Compose**, conçue pour digitaliser la gestion opérationnelle d'un festival de jeux de société. Elle constitue le frontend mobile d'une architecture SOA existante, se substituant à un frontend web Angular sans modifier le backend.

L'application permet à différents profils d'utilisateurs (bénévoles, organisateurs, administrateurs) de consulter et gérer les festivals, jeux, réservations et affectations de bénévoles, avec un support hors-ligne complet.

---

## Contexte du projet

| Élément | Valeur |
|---|---|
| Type | Application mobile Android native |
| Langage | Kotlin |
| UI Framework | Jetpack Compose + Material Design 3 |
| Backend | API RESTful existante (Node.js / Express / Prisma / PostgreSQL) — **non modifiable** |
| Architecture mobile | MVVM 3 couches + UDF (Unidirectional Data Flow) |
| Objectif | Soutenance technique (~15 min) |

> **Contrainte forte** : le backend ne doit pas être modifié. L'application consomme les endpoints existants tels quels.

---

## Architecture globale

```
┌─────────────────────────────────────────┐
│           APPLICATION ANDROID           │
│                                         │
│  ┌─────────┐  ┌────────┐  ┌─────────┐  │
│  │   UI    │→ │ Domain │→ │  Data   │  │
│  │ Layer   │  │ Layer  │  │  Layer  │  │
│  └─────────┘  └────────┘  └─────────┘  │
│       ↑              ↑           ↑      │
│  Compose +      UseCases +   Retrofit + │
│  ViewModel      Repository   Room DB   │
│                 Interfaces             │
└─────────────────────────────────────────┘
           ↕ HTTPS / JSON
┌─────────────────────────────────────────┐
│         BACKEND (NON MODIFIABLE)        │
│   Node.js + Express 5 + Prisma + PG    │
│           Port 4000                     │
└─────────────────────────────────────────┘
```

### Principe des 3 couches (MVVM strict)

- **UI Layer** : Composables Jetpack Compose + ViewModels. Expose des `StateFlow<UiState>`. Ne contient aucune logique métier.
- **Domain Layer** : Interfaces `Repository` + `UseCases` optionnels. Entités métier pures (sans annotations Room ou Retrofit).
- **Data Layer** : Implémentations des `Repository`, DAOs Room, services Retrofit, DTOs JSON. Seule couche autorisée à connaître les sources de données.

> **Règle absolue** : aucune dépendance ne remonte vers le haut (Data ne dépend pas de UI, Domain ne dépend pas de Data).

---

## Stack technique — Android

| Technologie | Rôle | Version |
|---|---|---|
| Kotlin | Langage principal | 2.x |
| Jetpack Compose | UI déclarative | BOM stable |
| Material Design 3 | Design system | md3 |
| Navigation 3 | Navigation mono-activité | 1.0.0 |
| Hilt | Injection de dépendances (DIP/SOLID) | 2.x |
| Retrofit + OkHttp | Appels API REST | 2.x |
| Room | Base de données locale (offline-first) | 2.x |
| DataStore Preferences | Persistance légère (token, préférences) | 1.x |
| StateFlow + Coroutines | Programmation réactive + async | stdlib |
| kotlinx.serialization | Sérialisation (destinations Navigation 3) | 2.x |

### Navigation 3 — choix architectural clé

L'application utilise **Navigation 3** (et non Navigation 2 / NavController classique). Ce choix impose :

- Les destinations sont des **objets `@Serializable`** (data objects / data classes), pas des routes `String`.
- Le `backStack` est un `SnapshotStateList<Any>` observable géré manuellement via `rememberNavBackStack`.
- L'affichage est piloté par `NavDisplay { entry<Destination> { ... } }`.
- La `BottomBar` et la `TopAppBar` sont positionnées dans un `Scaffold` global, en dehors du `NavDisplay`.

```kotlin
// Dépendances requises dans libs.versions.toml
nav3Core = "1.0.0"
lifecycleViewmodelNav3 = "2.10.0"
kotlinSerialization = "2.2.21"
kotlinxSerializationCore = "1.9.0"
```

```kotlin
// Exemple de destination typée
@Serializable data object FestivalList : AppDestination
@Serializable data class JeuDetail(val jeuId: Int) : AppDestination
```

---

## Stack technique — Backend (référence, non modifiable)

| Technologie | Rôle |
|---|---|
| Node.js 22 + TypeScript | Runtime serveur |
| Express 5 | Framework HTTP |
| Prisma 6 | ORM PostgreSQL |
| PostgreSQL | Base de données relationnelle |
| JWT (jsonwebtoken) | Authentification stateless |
| bcryptjs | Hachage des mots de passe |
| cookie-parser | Gestion des cookies HTTP-only |

### Authentification

Le backend utilise un système **JWT double token** :

- `access_token` : durée 15 min, transmis en cookie HTTP-only.
- `refresh_token` : durée 7 jours, transmis en cookie HTTP-only.
- L'application mobile doit gérer le **refresh automatique** du token (intercepteur OkHttp).

> **Particularité mobile** : les cookies HTTP-only ne sont pas gérés nativement par Retrofit. Il faut utiliser un `CookieJar` personnalisé avec `OkHttpClient` et persister les cookies dans le `DataStore`.

---

## Structure des dossiers — Android

```
com.example.festivaljeumobile/
├── di/
│   ├── NetworkModule.kt          # Retrofit, OkHttp, CookieJar
│   ├── DatabaseModule.kt         # Room AppDatabase
│   └── RepositoryModule.kt       # Bindings interfaces → implémentations
│
├── ui/
│   ├── navigation/
│   │   ├── AppDestination.kt     # Sealed interface + data objects @Serializable
│   │   ├── AppNavGraph.kt        # NavDisplay + Scaffold global
│   │   └── AppNavBar.kt          # BottomBar role-aware
│   ├── screens/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt
│   │   │   └── LoginViewModel.kt
│   │   ├── festival/
│   │   │   ├── FestivalListScreen.kt
│   │   │   └── FestivalListViewModel.kt
│   │   ├── jeux/
│   │   │   ├── JeuxListScreen.kt
│   │   │   ├── JeuxListViewModel.kt
│   │   │   ├── JeuDetailScreen.kt
│   │   │   └── JeuDetailViewModel.kt
│   │   ├── reservation/
│   │   │   ├── ReservationListScreen.kt
│   │   │   └── ReservationListViewModel.kt
│   │   ├── benevole/
│   │   │   ├── BenevoleScreen.kt
│   │   │   └── BenevoleViewModel.kt
│   │   └── profil/
│   │       ├── ProfilScreen.kt
│   │       └── ProfilViewModel.kt
│   ├── components/
│   │   ├── LoadingIndicator.kt
│   │   ├── ErrorBanner.kt        # Affichage état réseau offline
│   │   ├── EmptyState.kt
│   │   └── TopAppBar.kt
│   └── theme/
│       ├── AppTheme.kt           # MaterialTheme MD3
│       ├── Color.kt
│       ├── Typography.kt
│       └── Shape.kt
│
├── domain/
│   ├── model/                    # Entités métier pures (pas d'annotations)
│   │   ├── Festival.kt
│   │   ├── Jeu.kt
│   │   ├── Reservation.kt
│   │   ├── Acteur.kt
│   │   └── Candidature.kt
│   ├── repository/               # Interfaces (contrats DIP)
│   │   ├── AuthRepository.kt
│   │   ├── FestivalRepository.kt
│   │   ├── JeuRepository.kt
│   │   ├── ReservationRepository.kt
│   │   └── BenevoleRepository.kt
│   └── usecase/                  # Optionnel — logique métier isolée
│       ├── LoginUseCase.kt
│       ├── GetFestivalsUseCase.kt
│       └── GetJeuxUseCase.kt
│
└── data/
    ├── local/
    │   ├── AppDatabase.kt        # Room database singleton
    │   ├── dao/
    │   │   ├── FestivalDao.kt
    │   │   ├── JeuDao.kt
    │   │   └── ReservationDao.kt
    │   └── entity/
    │       ├── FestivalEntity.kt
    │       ├── JeuEntity.kt
    │       └── ReservationEntity.kt
    ├── remote/
    │   ├── api/
    │   │   ├── AuthApi.kt
    │   │   ├── FestivalApi.kt
    │   │   ├── JeuApi.kt
    │   │   ├── ReservationApi.kt
    │   │   └── BenevoleApi.kt
    │   └── dto/
    │       ├── FestivalDto.kt
    │       ├── JeuDto.kt
    │       ├── ReservationDto.kt
    │       └── AuthDto.kt
    └── repository/               # Implémentations concrètes
        ├── AuthRepositoryImpl.kt
        ├── FestivalRepositoryImpl.kt
        ├── JeuRepositoryImpl.kt
        ├── ReservationRepositoryImpl.kt
        └── BenevoleRepositoryImpl.kt
```

---

## Modèle de données — Backend (Prisma Schema)

### Entités principales

```
Acteur          → utilisateur de l'application (login, password, role)
Festival        → événement avec dates, nbTables, zones tarifaires
ZoneTarifaire   → zone d'un festival avec prix/m² et tarifs par type de table
ZonePlan        → zone physique (boutique ou animation) liée à une zone tarifaire
TableType       → type de table (petite, grande, mairie) avec surface m²
TarifTable      → tarif d'un type de table dans une zone tarifaire
Jeu             → jeu de société avec éditeur, mécanismes, type, métadonnées
Editeur         → éditeur de jeux (exposant/distributeur)
TypeJeu         → catégorie de jeu
Mecanism        → mécanisme de jeu (many-to-many avec Jeu)
Reservant       → entité réservant (éditeur, association, boutique…)
Reservation     → réservation d'un reservant pour un festival, contient des ReservationJeu
ReservationJeu  → ligne de réservation : jeu + zone + type de table + nb tables
CandidatureBenevoleFestival → candidature d'un bénévole à un festival
AffectationBenevole → affectation d'un bénévole à un jeu/zone/festival
```

### Enums importants

```kotlin
// Rôles utilisateur (backend Prisma)
enum class Role { Benevole, Organisateur, SuperOrganisateur, Admin }

// Types de table
enum class TypeTable { petite, grande, mairie }

// Types de zone physique
enum class TypeZone { boutique, animation }

// État de suivi d'une réservation
enum class EtatDeSuivi {
    Pas_encore_de_contact, Contact_pris, Discussion_en_cours,
    Refuse, Pas_de_reponse, Present, Facture, Facture_payee
}

// Type de remise sur réservation
enum class Remise { Table, Argent }

// Type de reservant
enum class TypeReservant { Editeur, Prestataire, Association, Boutique, Animation }
```

---

## API REST — Endpoints consommés par l'application

Base URL : `http://<host>:4000`

> Tous les endpoints (sauf `/api/auth/*` et `/api/public`) requièrent un cookie `access_token` valide. Les rôles sont vérifiés côté serveur via `requireRoles()`.

### Authentification — `/api/auth`

| Méthode | Endpoint | Corps | Rôles | Description |
|---|---|---|---|---|
| POST | `/login` | `{ login, password }` | Public | Connexion, pose les cookies JWT |
| POST | `/logout` | — | Authentifié | Supprime les cookies |
| POST | `/register` | `{ prenom, nom, login, password }` | Public | Création compte Bénévole |
| GET | `/whoami` | — | Authentifié | Retourne `{ id, role }` du token |
| POST | `/refresh` | — | Cookie refresh | Renouvelle l'access_token |

### Festivals — `/api/festival`

| Méthode | Endpoint | Rôles | Description |
|---|---|---|---|
| GET | `/getAllFestivals` | Tous | Liste tous les festivals |
| POST | `/addFestival` | SuperOrganisateur, Admin | Crée un festival avec zones |
| PUT | `/updateFestival` | SuperOrganisateur, Admin | Met à jour un festival |
| DELETE | `/deleteFestival` | SuperOrganisateur, Admin | Supprime un festival |

### Jeux — `/api/jeux`

| Méthode | Endpoint | Rôles | Description |
|---|---|---|---|
| GET | `/getAllJeux` | Organisateur+ | Liste tous les jeux |
| POST | `/addJeu` | SuperOrganisateur, Admin | Crée un jeu |
| POST | `/updateJeu` | SuperOrganisateur, Admin | Met à jour un jeu |
| POST | `/deleteJeu` | SuperOrganisateur, Admin | Supprime un jeu |

### Éditeurs — `/api/editeurs`

| Méthode | Endpoint | Rôles | Description |
|---|---|---|---|
| GET | `/getAllEditeurs` | SuperOrganisateur, Admin | Liste tous les éditeurs |
| GET | `/:id` | SuperOrganisateur, Admin | Éditeur par id |
| POST | `/addEditeur` | SuperOrganisateur, Admin | Crée un éditeur |
| POST | `/updateEditeur` | SuperOrganisateur, Admin | Met à jour un éditeur |
| DELETE | `/deleteCascade/:id` | SuperOrganisateur, Admin | Supprime éditeur + ses jeux |

### Réservations — `/api/reservation`

| Méthode | Endpoint | Rôles | Description |
|---|---|---|---|
| GET | `/getAllReservations` | SuperOrganisateur, Admin | Liste toutes les réservations |
| POST | `/addReservation` | SuperOrganisateur, Admin | Crée une réservation avec jeux |
| PUT | `/updateReservation` | SuperOrganisateur, Admin | Met à jour réservation + jeux |
| DELETE | `/deleteReservation/:id` | SuperOrganisateur, Admin | Supprime une réservation |

### Bénévoles — `/api/benevole`

| Méthode | Endpoint | Rôles | Description |
|---|---|---|---|
| GET | `/getAllCandidatures` | Organisateur+ | Candidatures en attente |
| POST | `/addCandidature/:festivalId` | Bénévole+ | Candidater à un festival |
| POST | `/acceptCandidature/:id` | Organisateur+ | Accepter une candidature |
| POST | `/refuseCandidature/:id` | Organisateur+ | Refuser une candidature |
| GET | `/getAllCandidaturesAcceptees` | Organisateur+ | Candidatures acceptées avec zones/jeux |
| POST | `/addBenevoleAffecte/:id` | Organisateur+ | Affecter un bénévole à un jeu/zone |
| GET | `/getAllAffectations` | Organisateur+ | Toutes les affectations |

### Zones tarifaires — `/api/zoneTarifaire`

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/getAllZones/:festivalId` | Zones d'un festival avec tarifs |
| POST | `/addZone/:festivalId` | Crée une zone tarifaire |
| PUT | `/updateZone/:id` | Met à jour une zone + ses tarifs |
| DELETE | `/deleteZone/:id` | Supprime une zone |

### Utilisateurs — `/api/user` (Admin uniquement)

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/getAllUsers` | Liste tous les utilisateurs non-Admin |
| POST | `/addUser` | Crée un utilisateur |
| PUT | `/updateUser` | Met à jour un utilisateur |
| DELETE | `/deleteUser` | Supprime un utilisateur |

---

## Rôles et permissions — Matrice d'accès

| Écran / Feature | Bénévole | Organisateur | SuperOrganisateur | Admin |
|---|:---:|:---:|:---:|:---:|
| Voir les festivals | ✅ | ✅ | ✅ | ✅ |
| Gérer les festivals | ❌ | ❌ | ✅ | ✅ |
| Voir les jeux | ❌ | ✅ | ✅ | ✅ |
| Gérer les jeux | ❌ | ❌ | ✅ | ✅ |
| Candidater bénévolat | ✅ | ✅ | ✅ | ✅ |
| Gérer candidatures | ❌ | ✅ | ✅ | ✅ |
| Affecter bénévoles | ❌ | ✅ | ✅ | ✅ |
| Voir réservations | ❌ | ❌ | ✅ | ✅ |
| Gérer réservations | ❌ | ❌ | ✅ | ✅ |
| Gérer utilisateurs | ❌ | ❌ | ❌ | ✅ |

> La BottomBar est filtrée côté client selon le rôle (défense UX). La vraie sécurité est assurée côté serveur.

---

## Comptes de test (initialisés au démarrage du backend)

| Login | Mot de passe | Rôle |
|---|---|---|
| `admin` | `admin` | Admin |
| `superorga` | `superorga` | SuperOrganisateur |
| `orga` | `orga` | Organisateur |
| `benevole` | `benevole` | Benevole |

---

## Stratégie offline-first

L'application suit le pattern **offline-first** : Room est la **source de vérité unique**.

```
Flux de lecture (UDF) :
  ViewModel → UseCase → Repository
    → Room (émission immédiate via Flow<List<T>>)
    → API (refresh en arrière-plan)
    → Room (mise à jour)
    → UI (recomposition automatique via StateFlow)

Flux d'écriture :
  UI Event → ViewModel → UseCase → Repository
    → API (tentative)
    → Room (sync si succès, queue si offline)
```

L'état réseau est exposé via un `NetworkMonitor` (ConnectivityManager) injecté par Hilt et affiché via le composant `ErrorBanner`.

---

## Pattern UiState

Chaque écran expose un `UiState` typé via `StateFlow` :

```kotlin
// Pattern data class (état continu)
data class FestivalListUiState(
    val festivals: List<Festival> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null
)

// Pattern sealed class (état discret / one-shot)
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object Success : LoginUiState()
}
```

Les événements remontent de l'UI vers le ViewModel via **lambdas** (actions simples) ou **Channel** (événements one-shot comme la navigation post-login).

---

## Injection de dépendances (Hilt)

```kotlin
// Principe DIP appliqué : tous les ViewModel dépendent d'interfaces, jamais d'implémentations

@HiltViewModel
class FestivalListViewModel @Inject constructor(
    private val getFestivalsUseCase: GetFestivalsUseCase, // interface
    private val networkMonitor: NetworkMonitor            // interface
) : ViewModel()

// NetworkModule fournit Retrofit + OkHttp avec gestion des cookies JWT
// DatabaseModule fournit Room AppDatabase
// RepositoryModule bind les interfaces à leurs implémentations
```

---

## Répartition des tâches — Équipe de 3

### Phase 1 — Socle commun (bloquant, à livrer en premier)

| Livrable | Responsable |
|---|---|
| Setup Hilt (NetworkModule, DatabaseModule, RepositoryModule) | Dev Senior |
| Room AppDatabase + entités de base | Dev Senior |
| Retrofit + OkHttp + CookieJar JWT | Dev Senior |
| NavGraph + AppDestination (Navigation 3) | Dev Senior |
| MD3 Theme (Color, Typography, Shape) | Dev Junior |
| Interfaces Repository (contrats DIP) | Dev Senior |

### Phase 2 — Features verticales (chaque dev = full-stack sur sa feature)

**Dev Senior — Auth + Socle offline**
- Login/Logout/Splash, gestion token DataStore
- NetworkMonitor + ErrorBanner global
- Stratégie refresh token (intercepteur OkHttp)

**Dev Intermédiaire — Feature Jeux + Festivals**
- Liste des festivals, liste des jeux, détail jeu
- Pattern offline-first complet (Room + Retrofit)
- JeuDao, FestivalDao, JeuRepositoryImpl, FestivalRepositoryImpl

**Dev Junior — Feature Bénévole + Profil + Design System**
- Candidature à un festival, liste des candidatures
- Écran profil, composants réutilisables (LoadingIndicator, EmptyState)
- Cohérence MD3 sur l'ensemble des écrans

---

## Installation et lancement

### Backend

```bash
cd backend
cp .env.example .env        # Configurer DATABASE_URL, JWT_SECRET
docker-compose up -d        # PostgreSQL
npm install
npx prisma migrate dev
npm run dev                 # Lance sur le port 4000
```

### Application Android

```
1. Ouvrir le projet dans Android Studio Hedgehog ou supérieur
2. Modifier BASE_URL dans NetworkModule.kt avec l'IP du backend
3. Synchroniser Gradle
4. Lancer sur émulateur API 26+ ou appareil physique
```

> Pour les tests sur émulateur Android : utiliser `10.0.2.2` comme adresse du localhost machine hôte.

---

## Conventions de code

- **Naming** : `PascalCase` pour les classes/composables, `camelCase` pour les variables/fonctions, `SCREAMING_SNAKE_CASE` pour les constantes.
- **Un fichier = une responsabilité** : pas de multi-classes par fichier sauf data classes liées.
- **Imports** : toujours explicites, pas de wildcard `import com.example.*`.
- **Coroutines** : `viewModelScope` pour les ViewModels, `Dispatchers.IO` pour les opérations réseau/BDD.
- **Pas de logique dans les Composables** : uniquement de l'affichage + collecte du `UiState`.
- **Commits** : format `feat(scope): description` / `fix(scope): description` / `chore: description`.

---

## Notes pour une IA générative

Si ce README est fourni comme contexte à une IA :

1. **Ne jamais modifier le backend** — tous les changements sont côté Android uniquement.
2. **Utiliser Navigation 3** exclusivement — pas de `NavController`, pas de `NavHost` de Navigation 2.
3. **Les destinations sont des objets `@Serializable`**, pas des routes `String`.
4. **Hilt est obligatoire** pour toute injection — pas de `companion object`, pas de singleton manuel.
5. **Room est la source de vérité** — jamais exposer directement une réponse Retrofit à l'UI.
6. **Les `UiState` sont des `data class` ou `sealed class`** exposées via `StateFlow`, jamais via `LiveData`.
7. **Les cookies JWT sont gérés par un `CookieJar` OkHttp** persisté dans `DataStore<Preferences>`.
8. **Le SDK de compilation minimum est 36** (requis par Navigation 3).
9. **Material Design 3 uniquement** — pas de composants MD2 (`androidx.compose.material`), utiliser `androidx.compose.material3`.
10. **La BottomBar ne s'affiche que sur les destinations racines** (`backStack.size == 1`).
