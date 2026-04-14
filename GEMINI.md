# GEMINI.md — Coddit Android App
# Full build specification for AI-assisted development

> Read this entire file before writing a single line of code.
> Follow every section in order. Ask for clarification only if a requirement is contradictory.
> Do NOT skip sections. Do NOT use deprecated APIs.

---

## 0. What is Coddit?

Coddit is a developer-first social problem-solving platform for Android.
Think Reddit + Stack Overflow + LinkedIn, but cleaner and built for builders.

Core loop:
1. A user posts a technical problem (title + body + optional image + tags)
2. Other users reply with solutions (text + safe links + image)
3. The post author marks the best reply as accepted
4. "Bytes" (reputation points) are awarded automatically

Key differentiators:
- Users link their real professional accounts (GitHub, LinkedIn) — badges appear on their posts/replies
- Links in replies are safety-verified before previews are shown
- "Bytes" replace karma — non-transferable reputation score
- Feed is tag-personalised based on linked account languages/skills

---

## 0.5 Current Implementation Status

**Last Updated:** April 14, 2026
**Version:** v1
**GitHub Repository:** https://github.com/puneeth-vemuri/coddit

### ✅ Completed Features & Fixes

The following issues have been resolved in the current implementation:

1. **Profile photos now update in existing posts**
   - Added `updateAuthorAvatar` method to `PostRemoteSource.kt`
   - When a user changes their avatar, all their existing posts are updated with the new avatar URL
   - Implemented in `UserRepositoryImpl.kt` with proper error handling

2. **Bytes system fully functional**
   - Bytes now properly update in user profiles using `FieldValue.increment()`
   - Added `updateBytes` method to `UserRemoteSource.kt` and `UserRepository`
   - Bytes are awarded for: POST_UPVOTED, REPLY_ACCEPTED, REPLY_UPVOTED, POST_SOLVED

3. **Byte voting system implemented**
   - One vote per user per post/reply (like Instagram/Facebook/Reddit)
   - Vote tracking prevents multiple votes from the same user
   - UI reflects vote status with proper visual feedback

4. **Clickable avatar and username navigation**
   - Post owner avatar and name are now clickable in `PostCard.kt`
   - Added `onAuthorClick` parameter to navigate to user profile
   - Updated `SharedComponents.kt` with clickable `UserAvatar` component

5. **Replies now properly show in posts**
   - Fixed broken `acceptReply` functionality in `ReplyRemoteSource.kt`
   - Added missing DAO methods: `updateReplyAccepted` and `updatePostSolved`
   - Replies display correctly with accepted status indicators

6. **Follow functionality added**
   - Added follow/unfollow methods to `UserRemoteSource.kt`
   - Implemented `followUser`, `unfollowUser`, and `isFollowing` methods
   - Added corresponding use cases in `UserUseCases.kt`
   - Follow button appears on other users' profiles (not on own profile)

7. **Followers list view implemented**
   - Basic follower count display in profile screen
   - Follow/unfollow status tracked in real-time
   - Firebase subcollections used for follower/following relationships

8. **Duplicate UI elements removed**
   - Consolidated duplicate "add account" section in `ProfileScreen.kt`
   - Removed duplicate notification icon from top bar in `FeedScreen.kt`
   - Removed duplicate "+" post button above navigation bar

9. **GitHub repository secured**
   - Updated `.gitignore` to exclude sensitive files:
     - `**/google-services.json`
     - `**/GoogleService-Info.plist`
     - `**/cloudinary-config.properties`
   - All changes pushed to new repository with proper security

10. **Compilation errors fixed**
    - Fixed `Argument type mismatch` in `ReplyRemoteSource.kt`
    - Fixed `Unresolved reference` errors for missing imports and DAO methods
    - Added missing `clickable` import to `SharedComponents.kt`

### 🔧 Technical Implementation Details

- **Repository Pattern**: All data access follows clean architecture with proper separation
- **Firestore Integration**: Real-time updates using Firestore listeners
- **Room Database**: Offline-first caching with proper entity mappings
- **ViewModel Pattern**: State management using `UiState` sealed classes
- **Navigation**: Type-safe routes with Compose Navigation
- **Dependency Injection**: Hilt for clean dependency management

### 📱 Current App State

The app is fully functional with:
- User authentication (Google + GitHub OAuth)
- Post creation with images and tags
- Reply system with voting and acceptance
- Profile management with avatar and skills
- Follow system for user connections
- Byte-based reputation system
- Offline support with local caching

### 🚀 Next Steps (Optional)

Potential enhancements for future versions:
1. Implement Algolia search integration
2. Add push notifications for replies and follows
3. Enhance link safety system with more domains
4. Add post editing with version history
5. Implement advanced filtering and sorting

---

## 1. Tech stack — use EXACTLY these, no substitutions

| Concern              | Technology                          | Notes                          |
|----------------------|-------------------------------------|--------------------------------|
| Language             | Kotlin 2.x                          | No Java                        |
| UI                   | Jetpack Compose (Material Design 3) | No XML layouts                 |
| Architecture         | MVVM + Clean Architecture           | 3 layers: presentation, domain, data |
| Async                | Kotlin Coroutines + Flow            | No RxJava                      |
| DI                   | Hilt                                | No Koin, no manual DI          |
| Local DB             | Room                                | For offline-first caching      |
| Preferences          | DataStore (Proto or Preferences)    | No SharedPreferences           |
| Navigation           | Jetpack Navigation Compose          | Type-safe routes                |
| Remote DB            | Firebase Firestore                  | Primary cloud database         |
| Auth                 | Firebase Auth                       | Google + GitHub OAuth          |
| Push notifications   | Firebase Cloud Messaging (FCM)      |                                |
| File storage         | Cloudinary                          | Avatars, post images (via Cloudinary API) |
| Crash monitoring     | Firebase Crashlytics                |                                |
| Analytics            | Firebase Analytics                  |                                |
| Search               | Algolia (free tier)                 | Full-text post/tag search      |
| Image loading        | Coil 3                              | No Glide, no Picasso           |
| Networking           | Retrofit 2 + OkHttp                 | For Algolia + Safe Browsing    |
| JSON                 | Kotlin Serialization                | No Gson, no Moshi              |
| Link safety          | Google Safe Browsing API v4         | Free, 10k checks/day           |
| Background jobs      | WorkManager                         | Offline sync                   |
| Code highlight       | Compose-Highlight (or custom)       | Syntax highlight in posts      |
| Build system         | Gradle (Kotlin DSL)                 | libs.versions.toml for deps    |

---

## 2. Project structure

Follow feature-based modular structure:

```
app/
├── src/main/
│   ├── java/com/coddit/app/
│   │   ├── di/                         # Hilt modules
│   │   │   ├── AppModule.kt
│   │   │   ├── FirebaseModule.kt
│   │   │   ├── NetworkModule.kt
│   │   │   └── RepositoryModule.kt
│   │   │
│   │   ├── domain/                     # Domain layer — pure Kotlin, no Android imports
│   │   │   ├── model/
│   │   │   │   ├── Post.kt
│   │   │   │   ├── Reply.kt
│   │   │   │   ├── User.kt
│   │   │   │   ├── LinkedAccount.kt
│   │   │   │   ├── BytesEvent.kt
│   │   │   │   └── SafeLink.kt
│   │   │   ├── repository/             # Interfaces only
│   │   │   │   ├── PostRepository.kt
│   │   │   │   ├── UserRepository.kt
│   │   │   │   ├── ReplyRepository.kt
│   │   │   │   └── LinkSafetyRepository.kt
│   │   │   └── usecase/
│   │   │       ├── feed/
│   │   │       │   ├── GetFeedUseCase.kt
│   │   │       │   └── GetPostDetailUseCase.kt
│   │   │       ├── post/
│   │   │       │   ├── CreatePostUseCase.kt
│   │   │       │   ├── VotePostUseCase.kt
│   │   │       │   └── DeletePostUseCase.kt
│   │   │       ├── reply/
│   │   │       │   ├── CreateReplyUseCase.kt
│   │   │       │   ├── AcceptReplyUseCase.kt
│   │   │       │   └── VoteReplyUseCase.kt
│   │   │       ├── user/
│   │   │       │   ├── GetUserProfileUseCase.kt
│   │   │       │   ├── LinkAccountUseCase.kt
│   │   │       │   └── UpdateBytesUseCase.kt
│   │   │       └── link/
│   │   │           └── CheckLinkSafetyUseCase.kt
│   │   │
│   │   ├── data/                       # Data layer
│   │   │   ├── local/
│   │   │   │   ├── db/
│   │   │   │   │   ├── CodditDatabase.kt
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── PostEntity.kt
│   │   │   │   │   │   ├── ReplyEntity.kt
│   │   │   │   │   │   └── UserEntity.kt
│   │   │   │   │   └── dao/
│   │   │   │   │       ├── PostDao.kt
│   │   │   │   │       ├── ReplyDao.kt
│   │   │   │   │       └── UserDao.kt
│   │   │   │   └── datastore/
│   │   │   │       └── SessionDataStore.kt
│   │   │   ├── remote/
│   │   │   │   ├── firestore/
│   │   │   │   │   ├── PostRemoteSource.kt
│   │   │   │   │   ├── ReplyRemoteSource.kt
│   │   │   │   │   └── UserRemoteSource.kt
│   │   │   │   ├── algolia/
│   │   │   │   │   └── AlgoliaSearchSource.kt
│   │   │   │   └── safebrowsing/
│   │   │   │       └── SafeBrowsingSource.kt
│   │   │   ├── repository/             # Implementations
│   │   │   │   ├── PostRepositoryImpl.kt
│   │   │   │   ├── UserRepositoryImpl.kt
│   │   │   │   ├── ReplyRepositoryImpl.kt
│   │   │   │   └── LinkSafetyRepositoryImpl.kt
│   │   │   └── worker/
│   │   │       └── SyncWorker.kt
│   │   │
│   │   └── presentation/               # Presentation layer
│   │       ├── navigation/
│   │       │   ├── NavGraph.kt
│   │       │   └── Screen.kt           # Sealed class of all routes
│   │       ├── theme/
│   │       │   ├── Color.kt
│   │       │   ├── Theme.kt
│   │       │   ├── Type.kt
│   │       │   └── Shape.kt
│   │       ├── components/             # Shared reusable Composables
│   │       │   ├── PostCard.kt
│   │       │   ├── ReplyCard.kt
│   │       │   ├── UserAvatar.kt
│   │       │   ├── BytesPill.kt
│   │       │   ├── LinkedAccountBadge.kt
│   │       │   ├── TagChip.kt
│   │       │   ├── CodeBlock.kt
│   │       │   ├── SafeLinkPreview.kt
│   │       │   └── LoadingOverlay.kt
│   │       └── screens/
│   │           ├── auth/
│   │           │   ├── LoginScreen.kt
│   │           │   └── LoginViewModel.kt
│   │           ├── onboarding/
│   │           │   ├── UsernameScreen.kt
│   │           │   ├── TagPickerScreen.kt
│   │           │   └── OnboardingViewModel.kt
│   │           ├── feed/
│   │           │   ├── FeedScreen.kt
│   │           │   └── FeedViewModel.kt
│   │           ├── post/
│   │           │   ├── PostDetailScreen.kt
│   │           │   ├── PostDetailViewModel.kt
│   │           │   ├── CreatePostScreen.kt
│   │           │   └── CreatePostViewModel.kt
│   │           ├── profile/
│   │           │   ├── ProfileScreen.kt
│   │           │   ├── ProfileViewModel.kt
│   │           │   ├── LinkAccountScreen.kt
│   │           │   └── LinkAccountViewModel.kt
│   │           ├── search/
│   │           │   ├── SearchScreen.kt
│   │           │   └── SearchViewModel.kt
│   │           └── notifications/
│   │               ├── NotificationsScreen.kt
│   │               └── NotificationsViewModel.kt
│   └── res/
│       └── (standard Android resources)
```

---

## 3. Domain models

Define these as pure Kotlin data classes in `domain/model/`. No Android imports.

```kotlin
// Post.kt
@Serializable
data class Post(
    val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val authorLinkedAccounts: List<LinkedAccount> = emptyList(),
    val title: String,
    val body: String,
    val codeSnippet: String?,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String>,
    val upvotes: Int,
    val viewCount: Int,
    val replyCount: Int,
    val solved: Boolean,
    val acceptedReplyId: String?,
    val createdAt: Long   // epoch millis
)

// Reply.kt
@Serializable
data class Reply(
    val replyId: String,
    val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val authorLinkedAccounts: List<LinkedAccount> = emptyList(),
    val body: String,
    val imageUrls: List<String> = emptyList(),
    val links: List<SafeLink>,
    val accepted: Boolean,
    val upvotes: Int,
    val createdAt: Long
)

// User.kt
@Serializable
data class User(
    val uid: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val bytes: Int,
    val postCount: Int,
    val solvedCount: Int,
    val followerCount: Int,
    val linkedAccounts: List<LinkedAccount>,
    val skills: List<String> = emptyList(),
    val createdAt: Long
)

// LinkedAccount.kt
@Serializable
data class LinkedAccount(
    val provider: LinkedAccountProvider,
    val handle: String,
    val profileUrl: String,
    val displayData: String,
    val verified: Boolean
)

enum class LinkedAccountProvider {
    GITHUB, LINKEDIN, GOOGLE, NPM, STACKOVERFLOW, DEVTO
}

// SafeLink.kt
@Serializable
data class SafeLink(
    val url: String,
    val displayUrl: String,
    val title: String?,
    val isVerified: Boolean,
    val isMalicious: Boolean,
    val isOnAllowlist: Boolean
)

// BytesEvent.kt
@Serializable
data class BytesEvent(
    val eventId: String,
    val uid: String,
    val action: BytesAction,
    val delta: Int,
    val reason: String,
    val timestamp: Long
)

enum class BytesAction {
    POST_UPVOTED,
    REPLY_ACCEPTED,
    REPLY_UPVOTED,
    POST_SOLVED,
    LINK_VERIFIED
}
```

---

## 4. Firestore schema

Collections and document shapes. Use these exact field names.

```
/users/{uid}
  uid: String
  username: String
  displayName: String
  avatarUrl: String?
  bytes: Int (default 0)
  postCount: Int (default 0)
  solvedCount: Int (default 0)
  followerCount: Int (default 0)
  skills: List<String>         // user's technical skills
  createdAt: Timestamp

/users/{uid}/linked_accounts/{provider}
  provider: String             // "GITHUB", "LINKEDIN", etc.
  handle: String
  profileUrl: String
  displayData: String
  verified: Boolean
  linkedAt: Timestamp

/users/{uid}/followers/{followerUid}   // For follow system
  followerUid: String
  followedUid: String
  createdAt: Timestamp

/users/{uid}/following/{followedUid}   // For follow system
  followerUid: String
  followedUid: String
  createdAt: Timestamp

/posts/{postId}
  postId: String
  authorUid: String
  authorUsername: String
  authorAvatarUrl: String?
  title: String
  body: String
  codeSnippet: String?
  imageUrls: List<String>      // Cloudinary image URLs
  tags: List<String>
  upvotes: Int (default 0)
  viewCount: Int (default 0)
  replyCount: Int (default 0)
  solved: Boolean (default false)
  acceptedReplyId: String?
  createdAt: Timestamp
  updatedAt: Timestamp

/posts/{postId}/replies/{replyId}
  replyId: String
  postId: String
  authorUid: String
  authorUsername: String
  authorAvatarUrl: String?
  body: String
  imageUrls: List<String>      // Cloudinary image URLs
  links: List<Map>             // serialised SafeLink objects
  accepted: Boolean (default false)
  upvotes: Int (default 0)
  createdAt: Timestamp

/votes/{uid_targetId}          // composite key prevents double-voting
  targetId: String             // postId or replyId
  targetType: String           // "POST" or "REPLY"
  voterUid: String
  createdAt: Timestamp

/bytes_ledger/{uid}/events/{eventId}
  action: String
  delta: Int
  reason: String
  timestamp: Timestamp

/notifications/{uid}/items/{notifId}
  type: String                 // "REPLY_ON_POST", "REPLY_ACCEPTED", "BYTES_AWARDED", "POST_UPVOTED"
  fromUid: String
  postId: String?
  replyId: String?
  message: String
  read: Boolean (default false)
  createdAt: Timestamp
```

---

## 5. Firestore security rules

Write these exactly into `firestore.rules`:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users: anyone can read, only owner can write (except bytes — server only)
    match /users/{uid} {
      allow read: if true;
      allow create: if request.auth != null && request.auth.uid == uid;
      allow update: if request.auth.uid == uid
                    && !('bytes' in request.resource.data.diff(resource.data).affectedKeys())
                    && !('postCount' in request.resource.data.diff(resource.data).affectedKeys())
                    && !('solvedCount' in request.resource.data.diff(resource.data).affectedKeys());
    }

    // Linked accounts: owner only
    match /users/{uid}/linked_accounts/{provider} {
      allow read: if true;
      allow write: if request.auth.uid == uid;
    }

    // Posts: anyone can read, auth users can create, only author can update/delete
    match /posts/{postId} {
      allow read: if true;
      allow create: if request.auth != null
                    && request.resource.data.authorUid == request.auth.uid;
      allow update: if request.auth.uid == resource.data.authorUid;
      allow delete: if request.auth.uid == resource.data.authorUid;

      // Replies: anyone can read, auth users can create, only author or post author can update
      match /replies/{replyId} {
        allow read: if true;
        allow create: if request.auth != null
                      && request.resource.data.authorUid == request.auth.uid;
        allow update: if request.auth.uid == resource.data.authorUid
                      || request.auth.uid == get(/databases/$(database)/documents/posts/$(postId)).data.authorUid;
        allow delete: if request.auth.uid == resource.data.authorUid;
      }
    }

    // Votes: auth user can create/delete their own vote only
    match /votes/{voteId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
                    && request.resource.data.voterUid == request.auth.uid;
      allow delete: if request.auth.uid == resource.data.voterUid;
    }

    // Bytes ledger: read-only from client, written by Cloud Functions only
    match /bytes_ledger/{uid}/events/{eventId} {
      allow read: if request.auth.uid == uid;
      allow write: if false;   // Cloud Functions only
    }

    // Notifications: user can read/update their own only
    match /notifications/{uid}/items/{notifId} {
      allow read, update: if request.auth.uid == uid;
      allow write: if false;   // Cloud Functions only
    }
  }
}
```

---

## 6. Room database

### Entities

```kotlin
// PostEntity.kt
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val title: String,
    val body: String,
    val codeSnippet: String?,
    val tagsJson: String,          // JSON array string
    val upvotes: Int,
    val viewCount: Int,
    val replyCount: Int,
    val solved: Boolean,
    val acceptedReplyId: String?,
    val createdAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

// ReplyEntity.kt
@Entity(tableName = "replies", foreignKeys = [
    ForeignKey(entity = PostEntity::class, parentColumns = ["postId"],
               childColumns = ["postId"], onDelete = ForeignKey.CASCADE)
])
data class ReplyEntity(
    @PrimaryKey val replyId: String,
    val postId: String,
    val authorUid: String,
    val authorUsername: String,
    val body: String,
    val linksJson: String,         // JSON array string
    val accepted: Boolean,
    val upvotes: Int,
    val createdAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

// UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val bytes: Int,
    val postCount: Int,
    val solvedCount: Int,
    val linkedAccountsJson: String, // JSON array string
    val cachedAt: Long = System.currentTimeMillis()
)
```

### DAOs

```kotlin
// PostDao.kt
@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun observeAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE postId = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE tagsJson LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun observePostsByTag(tag: String): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE cachedAt < :threshold")
    suspend fun evictOldPosts(threshold: Long)
}

// ReplyDao.kt
@Dao
interface ReplyDao {
    @Query("SELECT * FROM replies WHERE postId = :postId ORDER BY accepted DESC, upvotes DESC")
    fun observeRepliesForPost(postId: String): Flow<List<ReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplies(replies: List<ReplyEntity>)

    @Query("DELETE FROM replies WHERE postId = :postId")
    suspend fun deleteRepliesForPost(postId: String)
}
```

---

## 7. Repository pattern

Each repository must follow this offline-first strategy:

```
1. Emit cached data from Room immediately (fast, no loading flash)
2. Fetch fresh data from Firestore in background
3. Save fresh data to Room
4. Room Flow automatically re-emits (single source of truth)
5. On network failure: log error, keep showing cached data
```

Example skeleton:

```kotlin
// PostRepositoryImpl.kt
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postRemoteSource: PostRemoteSource
) : PostRepository {

    override fun getFeed(tags: List<String>): Flow<List<Post>> = flow {
        // 1. Emit cached immediately
        postDao.observeAllPosts()
            .map { entities -> entities.map { it.toDomain() } }
            .collect { emit(it) }
    }.also {
        // 2. Refresh from Firestore in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remote = postRemoteSource.fetchFeed(tags)
                postDao.insertPosts(remote.map { it.toEntity() })
            } catch (e: Exception) {
                Crashlytics.recordException(e)
            }
        }
    }
}
```

---

## 8. ViewModels + UI state pattern

Every screen uses a sealed `UiState` class:

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}
```

ViewModel example:

```kotlin
// FeedViewModel.kt
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Post>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Post>>> = _uiState.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()

    init { loadFeed() }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getFeedUseCase(_selectedTags.value)
                .catch { _uiState.value = UiState.Error(it.message ?: "Unknown error") }
                .collect { _uiState.value = if (it.isEmpty()) UiState.Empty else UiState.Success(it) }
        }
    }

    fun onTagSelected(tag: String) {
        _selectedTags.update { current ->
            if (tag in current) current - tag else current + tag
        }
        loadFeed()
    }
}
```

---

## 9. Navigation

Define all routes as a sealed class:

```kotlin
// Screen.kt
sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Username      : Screen("onboarding/username")
    object TagPicker     : Screen("onboarding/tags")
    object Feed          : Screen("feed")
    object Search        : Screen("search")
    object CreatePost    : Screen("post/create")
    data class PostDetail(val postId: String) : Screen("post/{postId}") {
        companion object { const val ROUTE = "post/{postId}" }
    }
    data class Profile(val uid: String) : Screen("profile/{uid}") {
        companion object { const val ROUTE = "profile/{uid}" }
    }
    object MyProfile     : Screen("profile/me")
    object LinkAccount   : Screen("profile/link")
    object Notifications : Screen("notifications")
}
```

---

## 10. Link safety system

The link safety flow must be implemented exactly like this:

```
User pastes URL
        │
        ▼
Is it on the ALLOWLIST?
 ├─ YES → mark isOnAllowlist=true, isVerified=true → show preview
 └─ NO  → call Google Safe Browsing API v4
               │
               ▼
         API result SAFE?
          ├─ YES → mark isVerified=true → show preview with "✓ checked" label
          └─ NO  → mark isMalicious=true → block URL, show warning to user
```

### Allowlist (hardcode these domains as verified):

```kotlin
val SAFE_DOMAINS = setOf(
    "github.com", "gist.github.com",
    "stackoverflow.com",
    "developer.android.com",
    "kotlinlang.org",
    "jetbrains.com",
    "docs.oracle.com",
    "developer.mozilla.org",
    "npmjs.com",
    "pub.dev",
    "medium.com",
    "dev.to",
    "docs.google.com",
    "firebase.google.com",
    "cloud.google.com",
    "reactjs.org", "react.dev",
    "nodejs.org",
    "python.org", "docs.python.org",
    "w3schools.com",
    "geeksforgeeks.org",
    "leetcode.com",
    "hackerrank.com",
    "youtube.com",    // tutorial links
    "arxiv.org"       // research papers
)
```

### Safe Browsing API call:

```kotlin
// SafeBrowsingSource.kt
suspend fun checkUrl(url: String): SafeLink {
    val domain = Uri.parse(url).host?.removePrefix("www.") ?: return unsafeLink(url)

    if (SAFE_DOMAINS.any { domain == it || domain.endsWith(".$it") }) {
        return SafeLink(url = url, displayUrl = domain, isVerified = true,
                        isMalicious = false, isOnAllowlist = true, title = null)
    }

    // Call Safe Browsing API
    val response = safeBrowsingApi.check(
        ThreatMatchesRequest(
            client = ClientInfo(clientId = "coddit-android", clientVersion = "1.0"),
            threatInfo = ThreatInfo(
                threatTypes = listOf("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE"),
                platformTypes = listOf("ANDROID"),
                threatEntryTypes = listOf("URL"),
                threatEntries = listOf(ThreatEntry(url = url))
            )
        )
    )

    val isMalicious = response.matches?.isNotEmpty() == true
    return SafeLink(url = url, displayUrl = domain, isVerified = !isMalicious,
                    isMalicious = isMalicious, isOnAllowlist = false, title = null)
}
```

---

## 11. Bytes system

Bytes must ONLY be awarded by Firebase Cloud Functions, never client-side.
Create these Cloud Functions in `functions/index.js`:

```javascript
// Trigger: when a reply's `accepted` field changes to true
exports.onReplyAccepted = functions.firestore
  .document('posts/{postId}/replies/{replyId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    if (!before.accepted && after.accepted) {
      await awardBytes(after.authorUid, 'REPLY_ACCEPTED', 10, 
                       `Reply accepted on post ${context.params.postId}`);
    }
  });

// Trigger: when a post's upvotes counter increases
exports.onPostVoted = functions.firestore
  .document('posts/{postId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    if (after.upvotes > before.upvotes) {
      await awardBytes(after.authorUid, 'POST_UPVOTED', 2, 
                       `Post ${context.params.postId} upvoted`);
    }
    if (before.viewCount < 100 && after.viewCount >= 100) {
      await awardBytes(after.authorUid, 'POST_100_VIEWS', 5, 
                       `Post ${context.params.postId} hit 100 views`);
    }
  });

async function awardBytes(uid, action, delta, reason) {
  const db = admin.firestore();
  const batch = db.batch();
  
  // Increment user bytes atomically
  batch.update(db.doc(`users/${uid}`), {
    bytes: admin.firestore.FieldValue.increment(delta)
  });
  
  // Log the event
  batch.set(db.collection(`bytes_ledger/${uid}/events`).doc(), {
    action, delta, reason,
    timestamp: admin.firestore.FieldValue.serverTimestamp()
  });
  
  await batch.commit();
}
```

---

## 12. FCM notifications

Send push notifications for these events (via Cloud Functions):

| Event | Recipient | Message |
|---|---|---|
| New reply on your post | Post author | "@{username} replied to your post" |
| Your reply was accepted | Reply author | "Your reply was accepted! +10 bytes" |
| Your post got upvoted | Post author | "Someone found your post helpful +2 bytes" |
| New bytes milestone | User | "You've reached {N} bytes!" |

Notification channels (create in `CodditApp.onCreate()`):

```kotlin
// Channel IDs
const val CHANNEL_REPLIES    = "replies"
const val CHANNEL_BYTES      = "bytes"
const val CHANNEL_SOCIAL     = "social"
```

---

## 13. Algolia search setup

- Index name: `coddit_posts`
- Indexed fields: `title`, `body`, `tags`, `authorUsername`
- Mirror to Algolia via Cloud Function on `posts/{postId}` create/update
- Search query on client via Retrofit hitting `https://{APP_ID}-dsn.algolia.net/1/indexes/coddit_posts/query`

```kotlin
// AlgoliaSearchSource.kt
suspend fun search(query: String, tags: List<String>): List<Post> {
    val filters = if (tags.isNotEmpty()) {
        tags.joinToString(" OR ") { "tags:$it" }
    } else null

    val response = algoliaApi.search(
        AlgoliaSearchRequest(
            query = query,
            filters = filters,
            hitsPerPage = 20
        )
    )
    return response.hits.map { it.toPost() }
}
```

---

## 14. Screens spec

Build every screen below. Each must handle Loading, Success, Error, Empty states.

### 14.1 LoginScreen
- Google Sign-In button (full width, Material 3 styled)
- GitHub OAuth button
- App logo + tagline
- On success → check if user has username → route to UsernameScreen or Feed

### 14.2 UsernameScreen (onboarding step 1)
- Text field for username (validate: 3–20 chars, alphanumeric + underscore, unique check against Firestore)
- Real-time availability indicator (debounce 500ms)
- Continue button

### 14.3 TagPickerScreen (onboarding step 2)
- Grid of tag chips: Android, Kotlin, React, Python, JavaScript, ML, DevOps, Web, iOS, Backend, Database, Security, UI/UX, Open Source, Career
- User selects minimum 3
- "Start coding" CTA

### 14.4 FeedScreen
- Top bar: "coddit" logo, search icon, notification bell with badge
- Horizontal tag filter chips (scrollable)
- LazyColumn of PostCard composables
- FAB for creating new post
- Pull-to-refresh
- Offline banner when no connection

### 14.5 PostCard (component)
- Author avatar + username (both clickable to navigate to user profile) + linked account badges (GitHub, LinkedIn pills)
- Time ago + view count
- Post title (max 2 lines)
- Post body preview (max 3 lines, truncated)
- Code snippet preview if present (syntax-highlighted, scrollable horizontally)
- Image previews if post has imageUrls (Cloudinary URLs)
- Tag chips
- Upvote button + count (one vote per user), Reply count, Share button
- Bytes pill (+N bytes) shown when user earns bytes from this post
- "Solved" green badge if post.solved == true

### 14.6 PostDetailScreen
- Full post content
- Accepted reply pinned at top with green "✓ Accepted" badge
- All other replies sorted by upvotes desc
- ReplyCard for each reply (see below)
- Reply composer at bottom (persistent, like iMessage)
- View count incremented on open (Firestore transaction)

### 14.7 ReplyCard (component)
- Author avatar + username + badges
- Reply body
- Image previews if reply has imageUrls (Cloudinary URLs)
- SafeLinkPreview for each link (show domain, verified badge, title if fetched)
- Upvote button (one vote per user)
- "Accept" button (only visible to post author, only if post not yet solved)
- Code block if reply contains code

### 14.8 CreatePostScreen
- Title field (required, max 120 chars, char counter)
- Body field (required, markdown-lite, min 20 chars)
- Code snippet field (optional, monospace font, language selector dropdown)
- Tag picker (select 1–5 tags)
- Attach image button (Cloudinary upload, show preview)
- Paste link button (triggers SafeLink check immediately)
- Post button (disabled until title + body filled)

### 14.9 ProfileScreen
- Avatar (tappable to change)
- Username, display name, skills list
- Bytes count (large, purple)
- Stats row: Posts | Solved | Followers
- Follow button (only visible when viewing another user's profile)
- Linked accounts section (expandable cards per provider)
- "Add account" button → LinkAccountScreen (only on own profile)
- Tab row: Posts | Replies | Saved

### 14.10 LinkAccountScreen
- List of supported providers with Connect buttons
- GitHub: OAuth via Chrome Custom Tab → get profile + repos via GitHub API
- LinkedIn: OAuth via Chrome Custom Tab → get role + company
- Stack Overflow: API key fetch for rep score
- npm: username lookup via registry API
- On link success: save to Firestore `linked_accounts` sub-collection

### 14.11 SearchScreen
- Search bar (autofocus on open)
- Recent searches (DataStore)
- Algolia-powered results as user types (debounce 300ms)
- Results shown as PostCard list
- Filter by tags chips

### 14.12 NotificationsScreen
- List of notifications newest first
- Group by type (replies, bytes, social)
- Mark all read button
- Tap → navigate to relevant post/profile
- Empty state illustration

---

## 15. UI/UX rules

- Material Design 3 throughout
- Dark mode support — test every screen in dark mode
- Font: use system font (default MD3)
- Minimum tap target: 48dp
- Loading states: use Shimmer effect (not circular progress) for list items
- Error states: show inline snackbar for transient errors, full-screen for fatal errors
- Empty states: friendly illustration + CTA (not just "No posts found")
- Animations: use AnimatedVisibility, animateContentSize for expand/collapse
- All text must support dynamic font scaling (no hardcoded text sizes in sp that break at 200% scale)
- Accessibility: all images need contentDescription, all interactive elements need semantics
- Offline banner: show a thin yellow bar at top when `ConnectivityManager` reports no network

---

## 16. Performance requirements

| Metric | Target |
|---|---|
| Cold start time | < 2 seconds |
| Feed first paint (cached) | < 300ms |
| Feed first paint (network) | < 1.5 seconds |
| APK size | < 30 MB (use App Bundle .aab) |
| RAM usage | < 150 MB steady state |
| Image loading | Coil with memory + disk cache |
| Pagination | Firestore pagination (limit 20, startAfter cursor) |
| Firestore reads | Never fetch all docs — always paginate or query by tag |

---

## 17. Offline-first rules

- Room is the single source of truth — UI always reads from Room, never directly from Firestore
- WorkManager `SyncWorker` runs every 15 minutes when network available to sync pending actions
- Queue these actions for offline: create post, create reply, upvote
- When offline, show stale data with timestamp "Last updated X minutes ago"
- Never show empty state just because network is unavailable — show cached content

---

## 18. Build and dependency versions

Use `libs.versions.toml` (version catalog). Target these approximately:

```toml
[versions]
kotlin = "2.0.0"
agp = "8.4.0"
compose-bom = "2024.05.00"
hilt = "2.51.1"
room = "2.6.1"
navigation = "2.7.7"
firebase-bom = "33.0.0"
coil = "3.0.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
workmanager = "2.9.0"
datastore = "1.1.1"
coroutines = "1.8.1"
lifecycle = "2.8.1"
```

---

## 19. Build order — implement in this exact sequence

1. Project setup: Gradle, `libs.versions.toml`, all dependencies, `google-services.json`, Firebase console setup
2. Hilt setup: `CodditApp.kt`, all `@Module` classes
3. Domain models (pure Kotlin data classes — no Android)
4. Room database: entities, DAOs, `CodditDatabase`
5. Firebase Auth: `LoginScreen` + `LoginViewModel` → Google + GitHub OAuth working
6. Onboarding: `UsernameScreen` → `TagPickerScreen` → save to Firestore
7. Firestore remote sources: `PostRemoteSource`, `UserRemoteSource`
8. Repository implementations with offline-first logic
9. Use cases (thin wrappers, just orchestrate repos)
10. `FeedScreen` + `FeedViewModel` — read from Room, refresh from Firestore
11. `PostDetailScreen` + replies
12. `CreatePostScreen` — write to Firestore + Room
13. Link safety: Safe Browsing integration + allowlist
14. `ProfileScreen` + `LinkAccountScreen` (GitHub + LinkedIn OAuth)
15. Bytes system: Cloud Functions deployment
16. FCM notifications setup + `NotificationsScreen`
17. Algolia search integration + `SearchScreen`
18. WorkManager offline sync
19. Dark mode polish, accessibility pass, performance pass
20. Firebase Crashlytics + Analytics events

---

## 20. Environment variables / secrets

Store in `local.properties` (never commit to Git):

```
ALGOLIA_APP_ID=your_app_id
ALGOLIA_SEARCH_KEY=your_search_only_api_key
SAFE_BROWSING_API_KEY=your_google_api_key
GITHUB_CLIENT_ID=your_github_oauth_client_id
GITHUB_CLIENT_SECRET=your_github_oauth_client_secret
LINKEDIN_CLIENT_ID=your_linkedin_client_id
LINKEDIN_CLIENT_SECRET=your_linkedin_client_secret
```

Access via `BuildConfig` using `buildConfigField` in `build.gradle.kts`.

---

## 21. Testing requirements

- Unit tests for all Use Cases (mock repositories)
- Unit tests for all ViewModels (mock use cases, test StateFlow emissions)
- Integration tests for Room DAOs (use in-memory database)
- UI tests for Login flow and Feed screen (Espresso / Compose Test)
- Test tag: `@SmallTest`, `@MediumTest`, `@LargeTest`

---

## 22. What NOT to do

- Do NOT use XML layouts anywhere
- Do NOT use SharedPreferences (use DataStore)
- Do NOT use AsyncTask (use Coroutines)
- Do NOT access Firestore directly from a Composable (always go through ViewModel → UseCase → Repository)
- Do NOT store API keys in source code
- Do NOT award bytes from the Android client (Cloud Functions only)
- Do NOT show unverified links as clickable — always check first
- Do NOT use `GlobalScope` anywhere
- Do NOT block the main thread
- Do NOT use deprecated Firebase APIs

---

## 23. First message to send after reading this file

After reading and understanding this file, respond with:
1. A summary of what you understood about Coddit
2. Any clarifying questions before starting
3. Then begin with Step 1 from section 19: project setup and Gradle configuration

Generate the complete `build.gradle.kts` (app level), `build.gradle.kts` (project level),
and `libs.versions.toml` as your first output.
