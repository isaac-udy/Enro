# Android Navigation 3 (Nav3) vs Enro: Comprehensive Comparison

## 1. Executive Summary

**Android Navigation 3 (Nav3)** is Google's new Compose-first navigation library, designed as a clean break from the Navigation Component (Nav2). Its philosophy centers on developer ownership of navigation state: the backstack is a plain `MutableList` that you create and mutate directly. Nav3 provides rendering infrastructure (`NavDisplay`, `Scene`, `SceneStrategy`) but deliberately avoids opinions about how navigation is triggered, how results are passed, or how deep links are resolved. It ships as two artifacts: `navigation3-runtime` (core types) and `navigation3-ui` (Compose rendering).

**Enro** is a multiplatform navigation framework (Android, iOS, Desktop, WASM) that provides a more opinionated, full-featured navigation system. Its philosophy centers on type-safe contracts between screens: `NavigationKey` defines inputs and outputs, `NavigationHandle` provides a unified API for navigation operations, and the framework manages backstack state, result delivery, deep linking, interception, and multi-container coordination. Destinations are registered via KSP annotation processing or programmatic modules, and the framework provides extensive test utilities.

Both libraries share a remarkably similar rendering architecture (scenes, scene strategies, overlay patterns, `AnimatedContent` with `SeekableTransitionState`), but they diverge significantly in scope: Nav3 is a focused rendering and state primitive, while Enro is a comprehensive navigation framework.

---

## 2. Architecture Comparison Table

| Concept | Nav3 | Enro |
|---|---|---|
| **Route type** | `NavKey` (Serializable marker interface) | `NavigationKey` (interface, with `WithResult<T>` variant) |
| **Route instance** | `NavKey` used directly in backstack | `NavigationKey.Instance<T>` (wraps key + UUID id + metadata) |
| **Entry/Destination** | `NavEntry` (key + content + metadata) | `NavigationDestination<T>` (instance + metadata + content) |
| **Backstack** | `MutableList<NavKey>` via `rememberNavBackStack` | `NavigationBackstack` (list of `NavigationKey.Instance`) managed by `NavigationContainer` |
| **Main display** | `NavDisplay` composable | `NavigationDisplay` composable |
| **Scene** | `Scene` (content scenes) / `OverlayScene` | `NavigationScene` / `NavigationScene.Overlay` |
| **Scene strategy** | `SceneStrategy` (fun interface) | `NavigationSceneStrategy` (fun interface) |
| **Entry decorator** | `NavEntryDecorator` (lifecycle, ViewModel, savedState) | `NavigationDestinationDecorator` (wraps destination content) |
| **Navigation controller** | None (developer-owned backstack) | `EnroController` (central controller) |
| **Navigation handle** | None (direct backstack mutation) | `NavigationHandle<T>` (provides `open`, `close`, `complete`, `requestClose`) |
| **Container** | None (single backstack concept) | `NavigationContainer` (keyed, with filters, interceptors, empty behavior) |
| **Entry provider** | `entryProvider` DSL with `entry<T> { }` | `@NavigationDestination` annotation + KSP, or `NavigationDestinationProvider<T>` |
| **Metadata** | Map on `NavEntry` | `NavigationKey.Metadata` (typed key-value store) + `NavigationDestination.metadata` |
| **Animations** | Per-`NavDisplay` transition specs | `NavigationAnimations` data class (transition, pop, predictiveBack, container specs) |
| **Result passing** | None built-in | `NavigationResultChannel<T>`, `registerForNavigationResult` |
| **Deep linking** | None built-in | `NavigationPathBinding` with pattern matching |
| **Interception** | None built-in | `NavigationInterceptor` (intercept open/close/complete) |
| **Multiplatform** | KMP structure, primarily Android | Android, iOS, Desktop, WASM |

---

## 3. Navigation Display & Scene Rendering

Both libraries use a nearly identical rendering architecture, which is no coincidence -- they share conceptual DNA around scene-based rendering with `AnimatedContent` and `SeekableTransitionState`.

### Nav3: NavDisplay

```kotlin
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider { ... },
    sceneStrategy = SinglePaneSceneStrategy(),
    transitionSpec = { ... },
)
```

`NavDisplay` takes a developer-owned backstack, resolves entries via the `entryProvider`, calculates scenes via `SceneStrategy`, and renders them with `AnimatedContent`. Overlay scenes (dialogs) are rendered above the main animated content. Z-index management tracks forward/back navigation for correct layering.

### Enro: NavigationDisplay

```kotlin
NavigationDisplay(
    state = containerState,
    sceneStrategy = NavigationSceneStrategy.from(
        DialogSceneStrategy(),
        DirectOverlaySceneStrategy(),
        SinglePaneSceneStrategy(),
    ),
    animations = NavigationAnimations.Default,
)
```

`NavigationDisplay` takes a `NavigationContainerState` (which includes the resolved destinations, not just keys), calculates scenes via `NavigationSceneStrategy`, and renders them with the same `AnimatedContent` + `SeekableTransitionState` pattern. The implementation is structurally very similar to `NavDisplay`: scene maps, z-index tracking, predictive back seek handling, and overlay rendering above the main transition.

### Key Differences

- **Input**: Nav3 takes a raw key list + entry provider; Enro takes pre-resolved container state with destinations already created.
- **Scene calculation**: Both use a chain-of-strategies pattern (`SceneStrategy` / `NavigationSceneStrategy.from()`). Both support overlay scenes that recursively peel off the top entry.
- **SharedTransitionLayout**: Both wrap content in `SharedTransitionLayout` for shared element transitions. Enro provides `LocalNavigationSharedTransitionScope` and `LocalNavigationAnimatedVisibilityScope` composition locals so destinations can participate in shared transitions.
- **Predictive back**: Both libraries handle predictive back via `SeekableTransitionState.seekTo()` with gesture progress, calculating a "previous scene" to show underneath. The implementation pattern is nearly identical.

---

## 4. Back Stack Management

### Nav3: Developer-Owned List

```kotlin
val backStack = rememberNavBackStack(startKey)
// Navigate
backStack.add(DetailKey(id))
// Go back
backStack.removeLastOrNull()
```

The backstack is a plain `MutableList<NavKey>` with save/restore support via `rememberNavBackStack`. There is no controller, no container abstraction, and no operation system. Navigation is achieved by directly mutating the list. This gives maximum flexibility but means every navigation pattern (conditional navigation, result passing, tab management) must be implemented manually.

### Enro: Managed Container

```kotlin
// Navigation via handle
val navigation = navigationHandle<MyKey>()
navigation.open(DetailKey(id))
navigation.close()
navigation.complete(result)

// Or direct backstack manipulation
container.updateBackstack(context) { currentBackstack ->
    currentBackstack + newInstance
}
```

Enro's `NavigationContainer` wraps the backstack with:
- **Filters**: Control which keys a container accepts (`NavigationContainerFilter`)
- **Interceptors**: Modify or cancel operations before they execute (`NavigationInterceptor`)
- **Empty behavior**: Control what happens when the backstack becomes empty (`EmptyInterceptor`)
- **Operation processing**: Navigation goes through `NavigationOperation` types (Open, Close, Complete, AggregateOperation, SetBackstack) which are processed through the interceptor chain

### Trade-offs

| Aspect | Nav3 | Enro |
|---|---|---|
| Simplicity | Direct list mutation, easy to understand | Operation-based, more concepts to learn |
| Flexibility | Full control, but must implement patterns manually | Rich built-in patterns, but more opinionated |
| State consistency | Developer responsible for valid states | Framework validates through filters/interceptors |
| Testability | Test list mutations directly | `TestNavigationHandle` captures operations for assertions |
| Multi-container | Manual per-container state management | Built-in container keys and parent/child relationships |

---

## 5. Destination Registration

### Nav3: entryProvider DSL

```kotlin
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider {
        entry<HomeKey> { key ->
            HomeScreen(key)
        }
        entry<DetailKey> { key ->
            DetailScreen(key.id)
        }
    }
)
```

Registration is local to the `NavDisplay` call site. For modular apps, you combine entry providers using Hilt/Koin multibinding or similar DI patterns.

### Enro: @NavigationDestination + KSP

```kotlin
@NavigationDestination(key = DetailKey::class)
@Composable
fun DetailScreen() {
    val navigation = navigationHandle<DetailKey>()
    // ...
}
```

Or programmatic registration:

```kotlin
val module = createNavigationModule {
    destination<DetailKey>(
        navigationDestination {
            DetailScreen()
        }
    )
    // Also supports interceptors, paths, decorators, plugins
}
```

KSP generates `NavigationModule` instances that are installed into `EnroController`. The `@NavigationDestination` annotation is processed at compile time, producing type-safe bindings.

### Trade-offs

| Aspect | Nav3 | Enro |
|---|---|---|
| Locality | Destinations defined at call site | Destinations defined at declaration site, resolved globally |
| Modularity | Manual DI wiring | KSP generates modules; `NavigationModule` composes modules |
| Type safety | `entry<T>` is type-safe at call site | Annotation + KSP ensures compile-time binding |
| Flexibility | Easy to have different content for same key | One binding per key type (with platform overrides) |
| Build overhead | None | KSP processing step |

---

## 6. Dialog & Overlay Support

Both libraries handle dialogs and overlays through nearly identical scene patterns.

### Nav3

```kotlin
// Mark an entry as a dialog
entry<DialogKey>(
    metadata = NavEntry.Metadata(SceneStrategy.DialogKey to true)
) { key ->
    DialogContent(key)
}
```

`DialogScene` wraps content in a `Dialog` composable. `OverlayScene` is the base interface for scenes rendered in separate windows. The `DialogSceneStrategy` checks metadata to identify dialog entries.

### Enro

```kotlin
// Mark via metadata in destination provider
navigationDestination<DialogKey>(
    metadata = { dialog(DialogProperties()) }
) {
    DialogContent()
}
```

`DialogScene` wraps content in a `Dialog` composable. `DirectOverlayScene` renders content directly as an overlay without a separate window. Both implement `NavigationScene.Overlay`, which requires `overlaidEntries` -- the entries that should be rendered underneath.

### Comparison

The overlay architecture is essentially the same:
- Both use an `Overlay` interface with `overlaidEntries` for recursive scene calculation
- Both render overlays above the main `AnimatedContent`
- Both have `DialogScene` implementations wrapping `Dialog` composable
- Enro additionally has `DirectOverlayScene` for overlays rendered directly in the composition tree (not in a separate window), which is useful for bottom sheets or custom overlay animations

---

## 7. Animation & Transitions

### Nav3

Animations are specified per-`NavDisplay` via `transitionSpec`:

```kotlin
NavDisplay(
    transitionSpec = { action, _, _ ->
        when (action) {
            is NavAction.Navigate -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            is NavAction.PopBack -> slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            else -> fadeIn() togetherWith fadeOut()
        }
    }
)
```

Per-destination overrides are possible through entry metadata.

### Enro

Animations are specified via the `NavigationAnimations` data class:

```kotlin
NavigationDisplay(
    state = containerState,
    animations = NavigationAnimations(
        transitionSpec = { /* forward navigation */ },
        popTransitionSpec = { /* back navigation */ },
        predictivePopTransitionSpec = { /* predictive back gesture */ },
        containerTransitionSpec = { /* container switches / empty transitions */ },
        emptyUsesContainerTransition = true,
    )
)
```

Enro separates animation specs into four distinct categories:
1. **transitionSpec**: Forward navigation
2. **popTransitionSpec**: Back navigation (pop)
3. **predictivePopTransitionSpec**: Predictive back gesture (can differ from regular pop)
4. **containerTransitionSpec**: Used when switching between containers or transitioning to/from empty

Both libraries use `AnimatedContent` with `SeekableTransitionState` under the hood, and both support predictive back via `seekTo` with gesture progress. The transition data (`SceneTransitionData` in Enro) includes the visible and previously-visible entries, plus the container key, enabling transitions to adapt based on what is entering and exiting.

---

## 8. State Saving & Restoration

### Nav3: NavEntryDecorator Pattern

```kotlin
NavDisplay(
    decorators = listOf(
        rememberSavedStateNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    )
)
```

Nav3 uses a composable decorator chain. Each `NavEntryDecorator` wraps entry content to provide cross-cutting concerns. Decorators are explicitly composed and ordered by the developer, giving full control over the decoration stack. Key decorators include:
- `SavedStateNavEntryDecorator` for saved instance state
- `ViewModelStoreNavEntryDecorator` for ViewModelStore per entry
- `movableContentOf` decorator for preserving content across transitions

### Enro: NavigationDestinationDecorator Pattern

```kotlin
val module = createNavigationModule {
    decorator {
        navigationDestinationDecorator(
            onRemove = { instance -> /* cleanup */ },
            decorator = { destination ->
                // Wrap destination.content()
                destination.content()
            }
        )
    }
}
```

Enro's `NavigationDestinationDecorator` serves a similar purpose but is registered globally via `NavigationModule` rather than per-display. Decorators are applied via `decorateNavigationDestination`, which folds them right-to-left over the destination. Each decorator has an `onRemove` callback for cleanup when a destination leaves the backstack.

Enro provides built-in lifecycle, ViewModel, and saved state support through its `NavigationContext` hierarchy (which implements `LifecycleOwner`, `ViewModelStoreOwner`, and `HasDefaultViewModelProviderFactory`) rather than through explicit decorator composition.

### Key Differences

- **Explicitness**: Nav3 requires decorators to be explicitly listed at each `NavDisplay` call site. Enro registers decorators globally.
- **Built-in support**: Enro bakes lifecycle/ViewModel/savedState into its context hierarchy. Nav3 treats these as opt-in decorators.
- **Ordering control**: Nav3 gives the developer explicit control over decorator ordering. Enro applies them in registration order.

---

## 9. ViewModel Integration

### Nav3: ViewModelStoreNavEntryDecorator

```kotlin
NavDisplay(
    decorators = listOf(
        rememberViewModelStoreNavEntryDecorator(),
    )
)

// In entry content:
val viewModel: MyViewModel = viewModel()
```

Nav3 provides a `ViewModelStore` per entry through a decorator. The decorator creates and manages `ViewModelStoreOwner` instances scoped to each entry's lifecycle. Without this decorator, `viewModel()` calls would be scoped to the parent activity/fragment.

### Enro: Built-in via NavigationContext

```kotlin
@NavigationDestination(key = DetailKey::class)
@Composable
fun DetailScreen() {
    val navigation = navigationHandle<DetailKey>()
    // ViewModel scoped to this destination's lifecycle
    val viewModel: DetailViewModel = viewModel()
}
```

Enro's `NavigationContext` hierarchy implements `ViewModelStoreOwner`, so every destination automatically gets its own `ViewModelStore`. No explicit decorator is needed.

Additionally, Enro provides the `navigationHandle()` delegate for ViewModels:

```kotlin
class DetailViewModel : ViewModel() {
    private val navigation by navigationHandle<DetailKey>()
    
    fun onItemClicked(id: String) {
        navigation.open(ItemKey(id))
    }
}
```

This allows ViewModels to perform navigation without holding references to Compose state.

---

## 10. Result Passing

### Nav3: No Built-in Support

Nav3 does not include a built-in result passing mechanism. Google's recipes suggest patterns using:
- `Channel` or `SharedFlow` for one-shot results
- `StateMap` stored in a shared scope
- Custom result-passing infrastructure

```kotlin
// Recipe approach (not built-in):
val resultChannel = remember { Channel<DateResult>() }
backStack.add(SelectDateKey(resultChannel))
```

### Enro: Built-in registerForNavigationResult

```kotlin
// Declaring a result-producing key
@Serializable
data class SelectDate(
    val minDate: LocalDate?,
    val maxDate: LocalDate?,
) : NavigationKey.WithResult<LocalDate>

// Registering for results
val selectDateResult = registerForNavigationResult<SelectDate, LocalDate> { result ->
    // Handle the date that was selected
    selectedDate = result
}

// Opening the result screen
selectDateResult.open(SelectDate(minDate = today, maxDate = null))

// Completing with a result from the destination
val navigation = navigationHandle<SelectDate>()
navigation.complete(selectedLocalDate)
```

Enro's result system is deeply integrated:
- **Type safety**: `NavigationKey.WithResult<T>` enforces that a destination must complete with type `T`
- **Compile-time checks**: Attempting to `complete()` a `WithResult` key without a result is a compile error
- **Close vs Complete**: `close()` dismisses without a result; `complete(result)` delivers the result
- **Result channels**: `NavigationResultChannel` handles result delivery through the `NavigationResult` sealed class (Closed or Completed)
- **completeFrom**: A destination can delegate completion to another destination via `completeFrom(anotherKey)`

This is one of the most significant feature gaps between the two libraries.

---

## 11. Deep Linking

### Nav3: No Built-in API

Nav3 does not include a deep linking API. Recipes suggest parsing URIs manually and constructing the appropriate backstack:

```kotlin
// Recipe approach:
fun handleDeepLink(uri: Uri): List<NavKey> {
    return when {
        uri.path?.startsWith("/profile/") == true -> {
            val userId = uri.lastPathSegment
            listOf(HomeKey, ProfileKey(userId))
        }
        else -> listOf(HomeKey)
    }
}
```

### Enro: NavigationPathBinding

```kotlin
val profilePathBinding = NavigationPathBinding(
    keyType = ProfileKey::class,
    pattern = "/profile/{userId}",
    deserialize = { ProfileKey(userId = get("userId")) },
    serialize = { key -> set("userId", key.userId) },
)

// Register in a module
val module = createNavigationModule {
    path(profilePathBinding)
}
```

Enro provides:
- **Pattern matching**: Path patterns with named parameters (`{userId}`)
- **Bidirectional**: Both deserialize (path to key) and serialize (key to path)
- **Module registration**: Path bindings registered alongside destinations
- **Type-safe extraction**: `PathData` provides typed parameter access

---

## 12. Multi-Pane / Adaptive Layouts

### Nav3: Custom SceneStrategy

```kotlin
class ListDetailSceneStrategy : SceneStrategy {
    override fun calculateScene(
        entries: List<NavEntry<*>>,
        previousEntries: List<NavEntry<*>>
    ): Scene? {
        if (entries.size < 2) return null
        val listEntry = entries[entries.size - 2]
        val detailEntry = entries.last()
        return Scene(
            key = ...,
            entries = listOf(listEntry, detailEntry),
            previousEntries = previousEntries,
            content = {
                Row {
                    Box(Modifier.weight(1f)) { listEntry.content() }
                    Box(Modifier.weight(1f)) { detailEntry.content() }
                }
            }
        )
    }
}
```

Nav3 also has integration with Material3 Adaptive (e.g., `ListDetailSceneStrategy`) that works with `NavigationSuiteScaffold` and adaptive layouts.

### Enro: Custom NavigationSceneStrategy

```kotlin
class ListDetailSceneStrategy : NavigationSceneStrategy {
    @Composable
    override fun calculateScene(
        entries: List<NavigationDestination<NavigationKey>>,
    ): NavigationScene? {
        if (entries.size < 2) return null
        val listEntry = entries[entries.size - 2]
        val detailEntry = entries.last()
        return object : NavigationScene {
            override val key = "list-detail-${listEntry.id}-${detailEntry.id}"
            override val entries = listOf(listEntry, detailEntry)
            override val previousEntries = entries.dropLast(1)
            override val content: @Composable () -> Unit = {
                Row {
                    Box(Modifier.weight(1f)) { listEntry.content() }
                    Box(Modifier.weight(1f)) { detailEntry.content() }
                }
            }
        }
    }
}
```

The pattern is nearly identical. Both libraries use the same strategy pattern where a `calculateScene` function inspects the entry list and returns a multi-pane scene or `null` to fall through to the next strategy.

### Key Difference

Nav3 has first-party Material3 Adaptive integration (`material3-adaptive-navigation3`), which provides ready-made adaptive scene strategies. Enro would need custom implementations for the same adaptive behavior.

---

## 13. Tab / Multi-Stack Navigation

### Nav3: Manual Per-Tab State

```kotlin
// Manually manage per-tab backstacks
val tabBackStacks = remember {
    mutableMapOf(
        Tab.Home to mutableStateListOf<NavKey>(HomeKey),
        Tab.Search to mutableStateListOf<NavKey>(SearchKey),
        Tab.Profile to mutableStateListOf<NavKey>(ProfileKey),
    )
}
var currentTab by remember { mutableStateOf(Tab.Home) }

NavDisplay(
    backStack = tabBackStacks[currentTab]!!,
    ...
)
```

There is no built-in abstraction for managing multiple backstacks. The developer manually maintains a map of tab to backstack and switches between them.

### Enro: NavigationContext Hierarchy with Multiple Containers

Enro's architecture naturally supports multi-container navigation through `NavigationContext.WithContainerChildren`:

```kotlin
// Each tab gets its own NavigationContainer with a unique key
NavigationDisplay(
    state = rememberNavigationContainerState(
        container = remember { NavigationContainer(key = NavigationContainer.Key("home"), ...) },
        ...
    )
)

NavigationDisplay(
    state = rememberNavigationContainerState(
        container = remember { NavigationContainer(key = NavigationContainer.Key("search"), ...) },
        ...
    )
)
```

The `NavigationContext` hierarchy (`RootContext` -> `ContainerContext` -> `DestinationContext`) tracks parent-child relationships between containers. `setActiveContainer` controls which container is currently active. Each container independently manages its own backstack, filters, and interceptors.

The `NavigationContainerFilter` system allows each container to accept only specific key types, ensuring navigation operations are routed to the correct tab.

---

## 14. Navigation Interception / Conditional Navigation

### Nav3: Manual Pattern

```kotlin
// Recipe approach:
fun navigate(key: NavKey) {
    if (!isAuthenticated && key is ProtectedKey) {
        backStack.add(LoginKey(redirectTo = key))
    } else {
        backStack.add(key)
    }
}
```

Nav3 does not have a built-in interception mechanism. Conditional navigation logic must be implemented in the navigation call sites or wrapped in helper functions.

### Enro: Built-in NavigationInterceptor

```kotlin
class AuthInterceptor(
    private val authRepository: AuthRepository,
) : NavigationInterceptor() {
    override fun intercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operation: NavigationOperation.Open<NavigationKey>,
    ): NavigationOperation? {
        val key = operation.instance.key
        if (key is ProtectedKey && !authRepository.isAuthenticated) {
            return NavigationOperation.Open(
                LoginKey(redirectTo = key).asInstance()
            )
        }
        return operation
    }
}

// Register globally
val module = createNavigationModule {
    interceptor(AuthInterceptor(authRepository))
}
```

Enro's interceptor system:
- Intercepts `Open`, `Close`, and `Complete` operations individually
- `beforeIntercept` can modify the entire operation list before individual processing
- Can return `null` to consume/cancel an operation
- Can return a different operation to redirect
- Can return an `AggregateOperation` to expand into multiple operations
- Registered per-container or globally via `NavigationModule`
- Processed through `AggregateNavigationInterceptor` which combines container-level and global interceptors

---

## 15. Dependency Injection / Modularization

### Nav3: DI Multibinding Pattern

```kotlin
// Using Hilt multibinding
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureNavigationModule {
    @Binds
    @IntoSet
    abstract fun bindEntries(impl: FeatureEntryProvider): EntryProvider
}

// Collect and merge at NavDisplay
NavDisplay(
    entryProvider = mergedEntryProviders(injectedProviders),
    ...
)
```

Nav3 relies on existing DI patterns for modularization. Each feature module provides its entry registrations, and they are combined at the app level.

### Enro: KSP-Generated NavigationModule

```kotlin
// In feature module - just annotate
@NavigationDestination(key = FeatureKey::class)
@Composable
fun FeatureScreen() { ... }

// KSP generates a NavigationModule for each module

// At app level - install modules
val controller = EnroController {
    module(featureNavigationModule()) // KSP-generated
    module(anotherFeatureModule())    // KSP-generated
    
    // Or manually compose modules
    module(createNavigationModule {
        module(featureNavigationModule())
        interceptor(globalInterceptor)
        decorator { ... }
    })
}
```

Enro's `NavigationModule` is a comprehensive unit that bundles:
- Destination bindings
- Interceptors
- Decorators
- Path bindings
- Serializers modules
- Plugins

Modules compose via the `module()` function in the builder scope, and KSP generates modules automatically from annotated destinations.

---

## 16. Multiplatform Support

### Nav3

Nav3 has a Kotlin Multiplatform project structure but is primarily Android-focused. The core types (`NavKey`, `NavBackStack`) are in common code, but the UI layer (`NavDisplay`, `Scene`) depends on Android Compose. There is no published iOS, Desktop, or WASM support.

### Enro

Enro has full multiplatform support:

| Platform | Support |
|---|---|
| Android | Full support (Compose + Fragment/Activity compat) |
| iOS | Compose Multiplatform support |
| Desktop (JVM) | Compose Multiplatform support |
| WASM/JS | Compose Multiplatform support |

The codebase is structured with:
- `commonMain` source sets for core types (`NavigationKey`, `NavigationContainer`, `NavigationHandle`, `NavigationDisplay`, scenes, interceptors, results, etc.)
- Platform-specific source sets for platform integrations (e.g., `androidMain` for Fragment/Activity compatibility in `enro-compat`)
- The `@NavigationDestination` annotation has platform-specific expect/actual declarations

---

## 17. Features Nav3 Has That Enro Does Not

### Explicit Decorator Composition Control

Nav3's `NavEntryDecorator` list at each `NavDisplay` gives developers explicit, per-display control over which cross-cutting concerns apply and in what order. This makes the decoration stack transparent and customizable at each usage site. Enro registers decorators globally, which is less flexible per-display but more consistent.

### Material3 Adaptive Navigation3 Integration

Nav3 has a first-party `material3-adaptive-navigation3` artifact that provides ready-made `ListDetailSceneStrategy` and integration with `NavigationSuiteScaffold`. This gives out-of-the-box adaptive layout support following Material Design guidelines.

### SharedTransitionLayout at NavDisplay Level

While both libraries support `SharedTransitionLayout`, Nav3's is directly integrated into the `NavDisplay` API with explicit scope exposure. Enro also provides this via `LocalNavigationSharedTransitionScope`, so the difference is minor.

### Retained Values via Compose Retain API

Nav3 integrates with Compose's retain API for keeping values alive across configuration changes without ViewModels.

### movableContentOf for Scene Transitions

Nav3 uses `movableContentOf` to preserve composable content identity when entries move between scenes (e.g., from single-pane to list-detail layout). This prevents content from being recreated during layout transitions. Enro may support similar behavior through its decorator system, but it is not as explicitly documented.

### No Build-Time Processing

Nav3 requires no annotation processing or code generation. Everything is runtime-configured, which means faster builds and no KSP dependency.

---

## 18. Features Enro Has That Nav3 Does Not

### Built-in Result Passing

`NavigationKey.WithResult<T>` + `NavigationResultChannel` + `registerForNavigationResult` provides type-safe, compile-time-checked result passing between screens. This is arguably the most impactful feature gap. Nav3 requires developers to build this from scratch.

### Managed Flow Destinations

Enro supports declarative multi-step flows where results from sub-destinations are automatically collected. The `completeFrom` operation allows a destination to delegate its result to another screen, enabling chains like: Screen A opens Screen B which completes from Screen C.

### Built-in Deep Linking

`NavigationPathBinding` with pattern matching, bidirectional serialization, and module registration. Nav3 has no deep linking support.

### Built-in Navigation Interception

`NavigationInterceptor` with `beforeIntercept`, per-operation interception (`Open`, `Close`, `Complete`), and support for operation transformation, cancellation, and expansion. Registered globally or per-container.

### NavigationContainer with Filters and Empty Behavior

`NavigationContainer` provides:
- `NavigationContainerFilter` for controlling which keys are accepted
- `EmptyInterceptor` for controlling what happens when the backstack becomes empty (allow, deny, or deny-and-perform-action)
- Container-scoped interceptors
- Keyed containers for multi-container scenarios

### Fragment / Activity Compatibility

The `enro-compat` module provides backward compatibility with Android Fragments and Activities, allowing incremental migration from traditional Android navigation.

### Full Multiplatform Support

Production support for iOS, Desktop, and WASM in addition to Android.

### KSP Annotation Processing for Destination Registration

`@NavigationDestination` annotation with KSP processing generates type-safe bindings and modules, reducing boilerplate and ensuring compile-time correctness.

### Built-in Test Utilities

- `TestNavigationHandle<T>`: Captures all navigation operations for assertion
- `assertOpened<T>()`: Verify that a key was opened
- `assertClosed()`: Verify that the handle was closed
- `assertCompleted<T>(result)`: Verify completion with a specific result
- `createTestNavigationHandle(key)`: Factory for test handles
- Operation history tracking and clearing

### Navigation Operations as First-Class Types

`NavigationOperation` is a sealed class hierarchy:
- `Open<T>`: Open a new destination
- `Close<T>`: Close a destination (with optional silent mode)
- `Complete<T>`: Complete with a result
- `AggregateOperation`: Compose multiple operations atomically
- `SideEffect`: Execute a side effect during operation processing
- `SetBackstack`: Replace the entire backstack
- `CompleteFrom`: Delegate completion to another key

This operation model enables interceptors, logging, testing, and undo/redo patterns that are impossible with direct list mutation.

### NavigationKey.Metadata

A typed, serializable key-value store (`MetadataKey<T>`, `TransientMetadataKey<T>`) attached to each navigation instance. Supports both persistent (survives save/restore) and transient (lost on process death) metadata.

---

## 19. Summary & Recommendations

### When to Choose Nav3

- **New Compose-only projects** that want minimal abstraction and maximum control
- **Simple navigation patterns** without result passing, deep linking, or interception needs
- **Teams that prefer explicit state management** and are comfortable building navigation patterns from primitives
- **Projects that want first-party Google support** and Material3 Adaptive integration
- **Projects that want to avoid build-time processing** (no KSP)
- **Learning/prototyping**: Nav3's simplicity makes it easy to get started quickly

### When to Choose Enro

- **Multiplatform projects** targeting iOS, Desktop, or WASM in addition to Android
- **Apps with result passing needs**: Enro's built-in typed result system is significantly more productive than building one from scratch
- **Apps with complex navigation patterns**: Conditional navigation, multi-step flows, deep linking, and interception are all built-in
- **Large apps with many modules**: KSP-generated modules reduce boilerplate and ensure compile-time correctness
- **Apps migrating from Fragments/Activities**: The compat module enables incremental migration
- **Teams that want comprehensive test utilities** for navigation logic

### Architectural Convergence

It is worth noting how similar the rendering layer is between the two libraries. Both use:
- Scene strategies as a chain-of-responsibility pattern
- Overlay scenes with recursive entry processing
- `AnimatedContent` with `SeekableTransitionState` for transitions
- Z-index tracking for forward/back navigation layering
- `SharedTransitionLayout` for shared element transitions
- Predictive back gesture handling via `seekTo`

This convergence suggests that the scene-based rendering model is becoming the standard approach for Compose navigation. The real differentiation is in the layers above and below: Nav3 is deliberately minimal (just rendering + state), while Enro provides a full framework (operations, results, interception, containers, deep linking, testing).

### For Existing Enro Users

Nav3 validates many of Enro's architectural decisions (scene strategies, overlay patterns, `SeekableTransitionState` animations). Enro's advantage lies in its higher-level features (results, interception, deep linking, multiplatform) that Nav3 does not attempt to provide. There is no compelling reason to migrate from Enro to Nav3 unless you specifically want to reduce framework dependency or need Material3 Adaptive integration.

### For Teams Evaluating Both

If your app is Android-only, Compose-only, and has simple navigation needs, Nav3 is a solid choice with the backing of Google. If your app needs result passing, deep linking, conditional navigation, multi-container management, multiplatform support, or comprehensive testing tools, Enro provides these out of the box while sharing the same modern rendering architecture.
