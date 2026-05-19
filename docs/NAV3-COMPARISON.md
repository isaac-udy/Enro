# Enro ↔ AndroidX Navigation3 alignment

Enro's rendering, scene model, and decorator infrastructure is intentionally
kept close to [AndroidX Navigation3](https://developer.android.com/jetpack/androidx/releases/navigation3)
so that:

1. Nav3 patterns translate to Enro almost line-for-line — if you know how a
   Nav3 `Scene`, `SceneStrategy`, or `NavEntryDecorator` works, you can
   write the Enro equivalent without re-learning the shape.
2. Bug fixes and performance improvements in Nav3 can be ported into Enro
   with minimal translation work.

What Enro **adds on top of** Nav3 — typed `NavigationHandle`s, typed results,
`NavigationOperation`s, interceptors, plugins, a controller-mediated binding
system, `NavigationContainer` hierarchy, `NavigationFlow`s — is documented
separately and not addressed here.

## How to read this document

For each subsystem, this doc maps the Nav3 type/API onto its Enro
counterpart, and lists every place Enro **deliberately diverges**. If you
find a divergence in code that isn't listed here, treat it as a candidate
for alignment — either fix it, or add it to this doc with a justification.

Code that knowingly diverges from Nav3 carries an inline comment of the form:

```kotlin
// Differs from Nav3's <name>: <one-line reason>.
// See docs/NAV3-COMPARISON.md#<section>.
```

## Type mapping (quick reference)

| Nav3 | Enro | Notes |
|--|--|--|
| `NavKey` | `NavigationKey` | Marker interface for keys. |
| `NavEntry<T>` | `NavigationDestination<T>` | Carries metadata + content. Enro wraps `NavigationKey.Instance<T>` instead of taking a raw key. |
| `NavEntry.contentKey` | `NavigationDestination.id` (from `instance.id`) | Stable id used for `movableContent`, `sharedElement` keys, etc. |
| `NavEntry.Content()` | `NavigationDestination.Content()` | Capital C; identical invocation pattern. |
| `NavEntryDecorator<T>` | `NavigationDestinationDecorator<T>` | Same constructor shape: `onPop` + `decorate`. |
| `rememberDecoratedNavEntries(...)` | `rememberDecoratedDestinations(...)` | Same fold-right decorator chain. |
| `Scene<T>` | `NavigationScene` | Same surface: `key`, `entries`, `previousEntries`, `content`. Enro `NavigationScene` is non-generic because all entries are `NavigationDestination<NavigationKey>`. |
| `OverlayScene<T>` | `NavigationScene.Overlay` | Same `overlaidEntries` concept. |
| `SceneStrategy<T>` | `NavigationSceneStrategy` | Same `calculateScene(entries)` shape. |
| `SinglePaneSceneStrategy<T>` | `dev.enro.ui.scenes.SinglePaneSceneStrategy` | Same fallback semantics. |
| `DialogSceneStrategy<T>` | `dev.enro.ui.scenes.DialogSceneStrategy` | Same metadata-driven activation. |
| `NavDisplay(...)` | `NavigationDisplay(state, ...)` | Same `AnimatedContent`-driven rendering. |
| `AnimatedSceneKey` | `SceneIdentity` (private) | `(class, key, containerKey)`. Used as the `AnimatedContent.contentKey`. |
| `SceneState<T>` | `NavigationSceneState` | Hoistable scene-hierarchy snapshot. Same fields: `entries`, `overlayScenes`, `currentScene`, `previousScenes`. |
| `rememberSceneState` | `rememberNavigationSceneState` | Computes the state; consumed internally by `NavigationDisplay` if not hoisted. |
| `SceneStrategyScope<T>` | `SceneStrategyScope` | Carries the `onBack` callback for scene-internal back affordances. Receiver of `NavigationSceneStrategy.calculateScene`. |
| `SceneDecoratorStrategy<T>` | `SceneDecoratorStrategy` | Wraps a non-overlay scene in another scene to add chrome (drawer, app bar, nav rail). Passed via `NavigationDisplay(sceneDecoratorStrategies = ...)`. |
| `SceneDecoratorStrategyScope<T>` | `SceneDecoratorStrategyScope` | Receiver of `SceneDecoratorStrategy.decorateScene`. Extends `SceneStrategyScope`. |
| `Scene.metadata` | `NavigationScene.metadata` | Defaults to the last entry's metadata. Used by `NavigationDisplay` to look up per-scene transition overrides. |
| `NavMetadataKey<T>` (for scene metadata) | `NavigationScene.MetadataKey<T>` | Parallel to `NavigationDestination.MetadataKey<T>`; same shape, scoped to scene-level metadata. |
| `NavDisplay.TransitionKey` etc. | `NavigationDisplay.TransitionKey` / `PopTransitionKey` / `PredictivePopTransitionKey` | Per-scene transition overrides. Looked up before falling back to `NavigationAnimations` defaults. |
| `SceneInfo<T>` | `NavigationSceneInfo` | Wraps the current scene as a `NavigationEventInfo` for predictive back handlers. |
| `OverlayScene.onRemove()` (suspend) | `NavigationScene.Overlay.onRemove()` | Runs after the overlay pops from the backstack, before it leaves composition. The display awaits it. |
| `predictivePopTransitionSpec(swipeEdge)` | `predictivePopTransitionSpec` in `NavigationAnimations` (and the `PredictivePopTransitionKey` value) now take a `swipeEdge: Int`. | Mirrors Nav3's swipe-edge plumbing. |
| `LocalNavAnimatedContentScope` | `LocalNavigationAnimatedVisibilityScope` | Same `AnimatedVisibilityScope` provided. |
| `SharedEntryInSceneNavEntryDecorator` | `sharedElementDecorator()` | Auto-wraps entries in `Modifier.sharedElement` keyed by instance id. |
| `SceneSetupNavEntryDecorator` | `movableContentDecorator()` | Wraps entries in `movableContentOf` and gates rendering by exclusion. |
| `BackStackAwareLifecycleNavEntryDecorator` | `navigationLifecycleDecorator(...)` | Provides per-destination `LifecycleOwner` capped at the destination's state. |
| `SaveableStateHolderNavEntryDecorator` | `savedStateDecorator(...)` | Saveable + saved state registries. Enro's is more capable but the factory shape mirrors Nav3. |
| `NavMetadataKey<T>` | `NavigationDestination.MetadataKey<T>` | Typed metadata keys, aligned with the existing `NavigationKey.MetadataKey<T>`. `metadata.get(key)` and `metadata.contains(key)` operators are extensions on `Map<String, Any>` in `dev.enro.ui` — import them via `import dev.enro.ui.get` / `import dev.enro.ui.contains` outside that package. |

## Deliberate divergences (and why)

This list is the source of truth for "we don't match Nav3 here, on purpose."

> _Each subsequent batch of the alignment work adds entries to this list as
> the relevant code lands._

- **`NavigationScene` is non-generic.** Nav3 carries `T : Any` through
  `Scene<T>` so the scene knows the key type. Enro's key system is closed
  (`NavigationKey` hierarchy) and the typed information lives in the
  `NavigationKey.Instance<T>` carried by each `NavigationDestination<T>`.
  Adding a type parameter to `NavigationScene` doesn't buy anything.

- **`SceneTransitionData` carries `containerKey` / `visible` /
  `previouslyVisible`.** Nav3 passes the raw `Scene<T>` through the
  `transition.AnimatedContent` so transition specs see the whole scene
  (and have access to `entries`). Enro deliberately keeps the
  transition-spec signature on `SceneTransitionData` (which exposes only
  `NavigationKey.Instance`s, not the full `NavigationDestination`s) — the
  transition spec should reason about visible **keys**, not destinations.

- **`NavigationDestinationDecorator` is `open class`, not `Immutable`.**
  Nav3 marks decorators `@Immutable`; Enro doesn't, to allow subclassing
  for decorator-author ergonomics. The decorator's stored callbacks are
  still effectively immutable.

- **`savedStateDecorator` uses a custom `NavigationSavedStateHolder`.**
  Nav3's `rememberSaveableStateHolderNavEntryDecorator` wraps each entry
  with `SaveableStateProvider`. Enro maintains both a `SavedStateRegistry`
  and a `SaveableStateRegistry` per destination so that AndroidX libraries
  expecting a `SavedStateRegistryOwner` (Fragments, libraries persisting
  through `SavedStateRegistry`) work without extra wiring.

- **`navigationContextDecorator` is Enro-only.** Provides each destination
  with a `NavigationHandle<T>`, a `NavigationContext`, and the result/flow
  routing. This is the core of Enro's API surface and has no Nav3 analog.

- **`EmptyBehavior`, `NavigationContainer`, `NavigationOperation`,
  `NavigationInterceptor`, `NavigationPlugin`, `EnroController`,
  `NavigationKey.WithResult<T>`, `registerForNavigationResult`,
  `NavigationFlow`** — these are Enro features Nav3 doesn't have. Out of
  scope for this alignment.

## Out-of-scope (Nav3 features Enro hasn't ported)

- Deep-link request / matcher (Nav3 has `DeepLinkRequest`/`DeepLinkMatcher`).
  Enro has its own path/deeplink system in `enro-runtime/.../path/`. Not
  comparable line-for-line.
- `EntryProvider` DSL. Enro uses `@NavigationDestination` annotations +
  KSP-generated bindings, registered through `EnroController`.

## Alignment progress

| Batch | Status | Commit |
|--|--|--|
| 1 — renames + scenes as data classes + cleanup | landed | `1d5e06c9` |
| 2 — typed metadata (`NavigationDestination.MetadataKey<T>`) | landed | `9056dd22` |
| 3 — invert exclusion polarity to match Nav3 | landed | `b307882f` |
| 4 — hoistable scene state | landed | `b74214b4` |
| 5 — Nav3-style flexibility additions (scene metadata, decorator strategies, etc.) | landed | `2776d09f` |
| 6 — internal refactors (`PrepareBackStack` + optional `sharedTransitionScope` param) | in progress | — |

## Remaining alignment work

- **Factory-style scene strategies.** Nav3's `SceneStrategy<T>` is a plain (non-`@Composable`) interface; strategies that need Compose state expose a `rememberXxxStrategy()` factory that captures it. Enro's `NavigationSceneStrategy.calculateScene` is still `@Composable`. Migrating would mean removing the `@Composable` annotation and updating the in-tree recipe strategies (`ListDetailSceneStrategy`, `TwoPaneSceneStrategy`) to use a factory pattern. Deferred from Batch 6 — meaningful API churn, separately commit-worthy.
