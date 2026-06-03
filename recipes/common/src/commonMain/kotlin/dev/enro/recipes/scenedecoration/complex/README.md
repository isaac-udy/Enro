# Shell Scene recipe

A metadata-driven, multi-pane shell that adapts to window size. Sits next to
the simpler `scenedecoration/simple` recipe (single sidebar / bottom-bar
chrome) and demonstrates how to compose Enro's scene-strategy, scene-decorator,
and overlay primitives into a coherent "app shell" pattern.

The design started from a sketch laid out as a grid of seven layouts: four
desktop variants (left pane, right pane, primary content only, with overlay)
and three mobile variants (with bottom nav, no bottom nav, modal overlay).
Each panel is the same underlying scene rendered differently because of (a)
the window breakpoint and (b) the metadata on the destinations on top of the
backstack.

## Metadata vocabulary

Destinations declare what they can be displayed as via metadata tags. There
is **no** `primary()` tag — anything without a tag occupies the main slot
when it's the right one to do so.

| Tag | Meaning |
|--|--|
| `leftPane()` | "I can render as a **left companion** when something is pushed on top of me." Pure `[leftPane]` renders as main content; `[leftPane, X]` puts me in the left slot with X in main. |
| `rightPane()` | "I can render as a **right companion** when I'm on top." `[X, rightPane]` puts me in the right slot with X in main. `[rightPane]` alone — i.e. I'm the only thing on the backstack — renders as main content. |
| `fullScreen()` | "I claim the entire content area: no pane companions even if they're eligible, and on mobile, hide the bottom chrome." |
| `directOverlay()` | "Render me as an overlay above whatever is underneath." Desktop wide: right-side drawer covering the whole window with a scrim behind it. Mobile: bottom sheet over the underlying primary content. |

A destination can carry more than one tag (e.g. a settings panel that's a
`rightPane()` companion but also `fullScreen()` on mobile-shaped windows —
though the breakpoint logic in the strategy handles the mobile fallback
automatically, so dual-tagging shouldn't usually be necessary).

## Slot resolution

Given the visible backstack and the window breakpoint, the strategy resolves
which destination fills which of three slots: **left**, **main**, **right**.
Mobile and wide treat the resolution slightly differently:

```
INPUT: entries (visible backstack, oldest → newest), breakpoint
OUTPUT: { left?, main, right? }

Step 1 — identify the main destination and an optional right pane.
  top = entries.last()
  if top has rightPane() metadata AND entries.size >= 2:
    right = top
    main  = entries[size - 2]
  else:
    right = null
    main  = top

Step 2 — identify an optional left pane.
  the destination immediately below `main` in the backstack
  (skipping the right pane if there is one) is the leftCandidate.
  if leftCandidate has leftPane() metadata:
    left = leftCandidate
  else:
    left = null

Step 3 — collapse for breakpoint.
  if breakpoint is MOBILE:
    result = { main = top }                       # only the top entry
  else if breakpoint is MEDIUM:
    if right != null:
      result = { main, right }                    # right takes precedence
    else if left != null:
      result = { left, main }
    else:
      result = { main }
  else (WIDE):
    result = { left, main, right }                # all three if available

Step 4 — fullScreen short-circuit.
  if top has fullScreen() metadata:
    result = { main = top }                       # overrides everything
```

Worked examples (assume `leftPane()` on `L`, `rightPane()` on `R`, nothing on `P`):

| Backstack | Mobile | Medium | Wide |
|--|--|--|--|
| `[P]` | `P` | `P` | `P` |
| `[L]` | `L` | `L` | `L` |
| `[L, P]` | `P` (top only) | `L \| P` (left + main) | `L \| P` (no right candidate) |
| `[P, L]` | `L` (top only) | `L` (top only; nothing below `L` has `leftPane()`) | `L` (same) |
| `[P, L, R]` | `R` (top only) | `L \| R` (`L` is main, `R` is right) | `L \| R` (no `leftPane()` below `L`) |
| `[L, P, R]` | `R` (top only) | `P \| R` (right wins on medium) | `L \| P \| R` (all three) |
| `[P, fullScreen]` | `fullScreen` | `fullScreen` | `fullScreen` |
| `[…, directOverlay]` | bottom sheet over `…` | right drawer scrim over `…` | right drawer scrim over `…` |

## Scene strategy chain

Composed in order — first match wins, otherwise we fall through:

1. **`ShellOverlaySceneStrategy`** — fires when the top entry has
   `directOverlay()` metadata. Returns a `NavigationScene.Overlay` whose
   `overlaidEntries` are the rest of the backstack, so the underlying scene
   continues to render beneath. The overlay's content positions itself per
   breakpoint: right-side drawer (with full-window scrim) on desktop,
   bottom sheet on mobile.

2. **`ShellPaneSceneStrategy`** — runs the slot-resolution algorithm above
   and returns a scene with a `Row` (wide), a `Row`-or-`Column` collapse
   (medium), or just the top entry (mobile). The scene's `entries` list
   contains every destination it renders so the runtime's exclusion logic
   knows not to double-render them in a fallback scene.

3. **`SinglePaneSceneStrategy`** (built-in) — fallback when nothing above
   produced a scene (e.g. an empty `leftPane()`-only stack on mobile).

## Chrome (the `ShellSceneDecorator`)

A `SceneDecoratorStrategy` wraps every non-overlay scene with the app shell.
Same shape as `simple/AdaptiveNavigationSceneDecorator`:

- The wrapper key is derived from the inner scene's identity so the outer
  `AnimatedContent` still runs the configured transitions for inner content.
- The chrome (top bar, left rail, mobile bottom nav, mobile search) is
  `movableContentOf` + `Modifier.sharedElement`, gated on the
  `transition.targetState == EnterExitState.Visible` check, so it stays
  visually pinned during the outer transition.

Chrome composition per breakpoint:

| Element | Mobile | Medium | Wide |
|--|--|--|--|
| Top bar (app icon + cart + profile) | always | (folds into desktop top bar) | (folds into desktop top bar) |
| Top search bar | hidden (moves to bottom) | always | always |
| Left nav rail | hidden | always | always |
| Bottom search bar | only when top destination is *not* `fullScreen()` / `leftPane()` / `rightPane()`, OR the destination is the only entry on the backstack (section root) | hidden | hidden |
| Bottom nav row (section switcher) | same conditions as bottom search | hidden | hidden |

The bottom-chrome visibility is driven by inspecting the top destination's
metadata at decoration time. The pane strategy never picks a top entry with
`fullScreen()`/`leftPane()`/`rightPane()` to put in `main` *on mobile*
**unless** that's literally the only thing on the backstack, and in that
case we keep the chrome visible so the user can still switch sections (a
section root that happens to be tagged `leftPane()` — e.g. `ProductList` in
the demo — would otherwise strand the user). Concretely:

```
showBottomChrome = breakpoint == Mobile &&
    (scene.previousEntries.isEmpty() ||                              // section root
        !(top.isFullScreen || top.isLeftPane || top.isRightPane))    // untagged
```

The left rail's section icons drive navigation by dispatching
`NavigationOperation.SetBackstack` directly on the container, the same way
the simple recipe does.

## Breakpoints

Defaults match Material 3 Adaptive's compact / medium / expanded:

| Name | Width range | Slot rule | Chrome |
|--|--|--|--|
| Mobile | `< 600.dp` | top only | mobile shell |
| Medium | `600.dp .. < 1200.dp` | main + at most one pane | desktop shell |
| Wide | `>= 1200.dp` | left + main + right | desktop shell |

The recipe parameterizes the thresholds on the strategy / decorator so an
app can override.

## File layout

```
complex/
├── README.md                              # this file
├── ShellSceneNavigation.kt                # @NavigationDestination(ShellSceneRecipe) — assembles the strategies + container
├── ShellBreakpoint.kt                     # Mobile / Medium / Wide enum + currentBreakpoint() composable
├── ShellMetadata.kt                       # leftPane(), rightPane(), fullScreen(), directOverlay() builders + keys
├── ShellPaneSceneStrategy.kt              # the slot-resolution strategy
├── ShellOverlaySceneStrategy.kt           # directOverlay handler (drawer / sheet)
├── ShellSceneDecorator.kt                 # chrome wrapper
└── destinations/
    ├── ShellHomeDestination.kt            # untagged — fills main
    ├── ProductListDestination.kt          # leftPane() — list pane when something's pushed on top
    ├── ProductDetailDestination.kt        # untagged — fills main when on top of ProductList
    ├── ProductFiltersDestination.kt       # rightPane() — opens from detail, sits to its right
    ├── ImmersiveImageDestination.kt       # fullScreen() — claims the whole content area
    └── CartOverlayDestination.kt          # directOverlay() — drawer on desktop / sheet on mobile
```

Each destination file owns its `NavigationKey`, `@NavigationDestination`
binding, and content composable, so there's only one place to look when
understanding how a given destination integrates with the shell.
