# Shell Scene recipe

The Shell Scene recipe is a multi-pane app shell that changes layout with
the window size and the destination metadata. The recipe combines a scene
strategy, a scene decorator and an overlay strategy. The
`scenedecoration/simple` recipe is a smaller example with one sidebar or
bottom bar.

The recipe renders seven layouts: four desktop layouts (left pane, right
pane, main content only, overlay) and three mobile layouts (bottom
navigation, no bottom navigation, modal overlay). Two inputs select the
layout: the window breakpoint, and the metadata of the destinations at
the top of the backstack.

## Metadata tags

A destination declares the slots that the destination can fill with
metadata tags. The recipe has **no** `primary()` tag. A destination
without a tag fills the main slot.

| Tag | Meaning |
|--|--|
| `leftPane()` | The destination renders in the **left slot** when another destination is directly above the destination in the backstack. `[leftPane]` alone renders the destination in the main slot. `[leftPane, X]` renders the destination in the left slot and `X` in the main slot. |
| `rightPane()` | The destination renders in the **right slot** when the destination is the top entry. `[X, rightPane]` renders the destination in the right slot and `X` in the main slot. `[rightPane]` alone, where the destination is the only entry in the backstack, renders the destination in the main slot. |
| `fullScreen()` | The destination fills the whole content area. The strategy renders no left or right pane, and the decorator hides the bottom chrome on mobile. |
| `directOverlay()` | The destination renders as an overlay above the scene for the rest of the backstack. On desktop, the overlay is a right-side drawer with a scrim over the whole window. On mobile, the overlay is a bottom sheet over the main content. |

A destination can have more than one tag, for example a settings panel
with `rightPane()` and `fullScreen()`. The strategy already renders only
the top entry on mobile, so a second tag for mobile is rarely required.

## Slot resolution

The strategy takes the visible backstack and the window breakpoint, and
selects the destination for each of three slots: **left**, **main** and
**right**. Step 3 differs for each breakpoint:

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

Examples, where `L` has `leftPane()`, `R` has `rightPane()` and `P` has no tag:

| Backstack | Mobile | Medium | Wide |
|--|--|--|--|
| `[P]` | `P` | `P` | `P` |
| `[L]` | `L` | `L` | `L` |
| `[L, P]` | `P` (top only) | `L \| P` (left + main) | `L \| P` (no right candidate) |
| `[P, L]` | `L` (top only) | `L` (top only; the entry below `L` has no `leftPane()`) | `L` (top only; the entry below `L` has no `leftPane()`) |
| `[P, L, R]` | `R` (top only) | `L \| R` (`L` is main, `R` is right) | `L \| R` (the entry below `L` has no `leftPane()`) |
| `[L, P, R]` | `R` (top only) | `P \| R` (the right pane has priority on medium) | `L \| P \| R` (all three slots) |
| `[P, fullScreen]` | `fullScreen` | `fullScreen` | `fullScreen` |
| `[…, directOverlay]` | bottom sheet over `…` | right drawer scrim over `…` | right drawer scrim over `…` |

## Scene strategy chain

The container tries the strategies in order and uses the first scene
that a strategy returns:

1. **`ShellOverlaySceneStrategy`** — returns a scene when the top entry
   has `directOverlay()` metadata. The scene is a
   `NavigationScene.Overlay` whose `overlaidEntries` are the rest of the
   backstack, so the scene for the rest of the backstack renders under
   the overlay. The overlay content is a right-side drawer with a
   full-window scrim on desktop and a bottom sheet on mobile.

2. **`ShellPaneSceneStrategy`** — runs the slot resolution above and
   returns a scene with a `Row` (wide), a `Row` or `Column` (medium), or
   only the top entry (mobile). The scene's `entries` list contains every
   destination that the scene renders, so the runtime does not render
   those destinations again in another scene.

3. **`SinglePaneSceneStrategy`** (built-in) — returns a scene when
   neither strategy above returns a scene.

## Chrome: `ShellSceneDecorator`

`ShellSceneDecorator` is a `SceneDecoratorStrategy` that wraps every
non-overlay scene in the app shell. `ShellSceneDecorator` has the same
structure as `simple/AdaptiveNavigationSceneDecorator`:

- The key of the wrapping scene is derived from the key of the inner
  scene, so the outer `AnimatedContent` runs the configured transitions
  for the inner content.
- The chrome (top bar, left rail, mobile bottom navigation, mobile
  search) is `movableContentOf` content with `Modifier.sharedElement`,
  rendered only when `transition.targetState == EnterExitState.Visible`.
  The chrome therefore stays in place during the outer transition.

Chrome elements for each breakpoint:

| Element | Mobile | Medium | Wide |
|--|--|--|--|
| Top bar (app icon + cart + profile) | always | part of the desktop top bar | part of the desktop top bar |
| Top search bar | hidden (shown at the bottom) | always | always |
| Left nav rail | hidden | always | always |
| Bottom search bar | shown when the top destination has none of `fullScreen()`, `leftPane()`, `rightPane()`, or when the top destination is the only entry in the backstack (a section root) | hidden | hidden |
| Bottom nav row (section switcher) | same conditions as the bottom search bar | hidden | hidden |

`ShellSceneDecorator` reads the top destination's metadata to decide
whether to show the bottom chrome. On mobile, a top destination with
`fullScreen()`, `leftPane()` or `rightPane()` hides the bottom chrome,
except when the top destination is the only entry in the backstack. A
section root with a `leftPane()` tag, such as `ProductList`, therefore
shows the bottom chrome, and the user can switch sections from the
section root:

```
showBottomChrome = breakpoint == Mobile &&
    (scene.previousEntries.isEmpty() ||                              // section root
        !(top.isFullScreen || top.isLeftPane || top.isRightPane))    // untagged
```

Each section icon in the left rail executes
`NavigationOperation.SetBackstack` on the container, as in the simple
recipe.

## Breakpoints

The default breakpoints:

| Name | Width range | Slot rule | Chrome |
|--|--|--|--|
| Mobile | `< 600.dp` | top only | mobile shell |
| Medium | `600.dp .. < 1200.dp` | main + at most one pane | desktop shell |
| Wide | `>= 1200.dp` | left + main + right | desktop shell |

`ShellPaneSceneStrategy` takes the thresholds as a `ShellBreakpoints`
parameter.

## File layout

```
complex/
├── README.md                              # this file
├── ShellSceneNavigation.kt                # @NavigationDestination(ShellSceneRecipe) — assembles the strategies + container
├── ShellBreakpoint.kt                     # Mobile / Medium / Wide enum + rememberShellBreakpoint() composable
├── ShellMetadata.kt                       # leftPane(), rightPane(), fullScreen(), directOverlay() builders + keys
├── ShellPaneSceneStrategy.kt              # the slot-resolution strategy
├── ShellOverlaySceneStrategy.kt           # directOverlay handler (drawer / sheet)
├── ShellSceneDecorator.kt                 # chrome wrapper
└── destinations/
    ├── ShellHomeDestination.kt            # untagged — fills main
    ├── ProductListDestination.kt          # leftPane() — left slot when another destination is above ProductList
    ├── ProductDetailDestination.kt        # untagged — fills main when above ProductList
    ├── ProductFiltersDestination.kt       # rightPane() — opened from ProductDetail, right slot
    ├── ImmersiveImageDestination.kt       # fullScreen() — fills the whole content area
    └── CartOverlayDestination.kt          # directOverlay() — drawer on desktop / sheet on mobile
```

Each destination file declares the destination's `NavigationKey`,
`@NavigationDestination` binding and content composable.
