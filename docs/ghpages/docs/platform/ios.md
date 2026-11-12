---
title: iOS
parent: Platform-Specific Guides
nav_order: 2
---

# iOS

Enro runs on iOS through Compose Multiplatform. Installation has two
parts: the Swift app calls the generated `installNavigationController`
extension at launch, and a Kotlin function returns the `UIViewController`
that the Swift app displays.

## Installation

### From Swift

In the `UIApplicationDelegate`, call `installNavigationController` on the
`NavigationComponent` object once at launch:

```swift
import UIKit
import shared

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        MyComponent.shared.installNavigationController(
            application: application,
        )
        return true
    }
}
```

Kotlin/Native exports a Kotlin `object` to Swift with a `shared` accessor.
Enro's KSP processor generates the `installNavigationController`
extension for the iOS target.

### From Kotlin

Declare a function that returns a `UIViewController`.
`EnroUIViewController { }` creates a Compose `UIViewController` and
provides the root navigation context to the content:

<!-- snippet: installation/ios -->
```kotlin
fun MainViewController(): UIViewController = EnroUIViewController {
    val container = rememberNavigationContainer(
        backstack = backstackOf(Home.asInstance()),
    )
    NavigationDisplay(state = container)
}
```

In Swift, call `MainViewController()` and use the returned view
controller as the root view controller. A UIKit app sets the view
controller as the window's root or adds the view controller as a child:

```swift
import UIKit
import shared

class ViewController: UIViewController {
    override func loadView() {
        let kotlinVc = MainViewControllerKt.MainViewController()
        addChild(kotlinVc)
        view = kotlinVc.view
        kotlinVc.didMove(toParent: self)
    }
}
```

A SwiftUI app wraps the Kotlin view controller in a
`UIViewControllerRepresentable`.

## UIViewController destinations

`uiViewControllerDestination` binds a key to a `UIViewController`. The
`metadata` flag sets how Enro shows the view controller:

| Flag | Shown |
|---|---|
| `SupportsCompose` | Embedded in the Enro container, like a Composable destination. |
| `SupportsPresent()` | Presented modally over the app. |
| `SupportsUINavigationView` | Pushed onto the `UINavigationController` that hosts the app. |

<!-- snippet: ios/view-controller-destinations -->
```kotlin
// Plain UIKit, embedded in the Enro container.
@NavigationDestination(UIKitLabel::class)
val uiKitLabelDestination = uiViewControllerDestination<UIKitLabel>(
    metadata = { listOf(UIViewControllerDestination.SupportsCompose) },
    viewController = { instance ->
        UIViewController().apply {
            view.backgroundColor = UIColor.whiteColor
            val label = UILabel().apply {
                text = instance.key.text
                textAlignment = NSTextAlignmentCenter
                numberOfLines = 0
                setFrame(view.bounds)
                autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
            }
            view.addSubview(label)
        }
    },
)

// Presented modally. The content is Compose, so it can close itself.
@NavigationDestination(PresentedViewController::class)
val presentedViewControllerDestination = uiViewControllerDestination<PresentedViewController>(
    metadata = { listOf(UIViewControllerDestination.SupportsPresent()) },
    viewController = {
        EnroUIViewController {
            val navigation = navigationHandle<PresentedViewController>()
            ViewControllerContent(
                title = "Presented modally",
                onClose = { navigation.close() },
            )
        }
    },
)

// Pushed onto the UINavigationController that hosts the app, which shows the
// system navigation bar and back button.
@NavigationDestination(PushedViewController::class)
val pushedViewControllerDestination = uiViewControllerDestination<PushedViewController>(
    metadata = { listOf(UIViewControllerDestination.SupportsUINavigationView) },
    viewController = {
        EnroUIViewController {
            val navigation = navigationHandle<PushedViewController>()
            ViewControllerContent(
                title = "Pushed onto the UINavigationController",
                onClose = { navigation.close() },
            )
        }
    },
)
```

`SupportsUINavigationView` requires the app's root view controller to be
inside a `UINavigationController`. The [recipes Xcode project][ios-xcode]
creates the `UINavigationController` in `MainViewControllerView.swift`.

See the [UIViewController recipe][uiviewcontroller-recipe].

## What Enro provides on iOS

- The system back gesture calls `requestClose` on the `NavigationHandle`
  of each destination that the back gesture removes.
- The common API: `NavigationKey`, `NavigationKey.WithResult<T>`,
  `navigationHandle<T>()`, `registerForNavigationResult`,
  `NavigationDisplay`, scene strategies, plugins, decorators.

## Notes

- Enro does not use `UINavigationController` for Composable destinations.
  The containers inside an `EnroUIViewController` store the backstack. For
  a navigation bar, build the bar in Compose (the recipes app uses
  Material 3's `TopAppBar`), or put the `EnroUIViewController` inside a
  `UINavigationController` and configure the bar in Swift.
- Composable destinations behave on iOS as on the other platforms. A
  `UIViewController` is bound to a key as described in
  [UIViewController destinations](#uiviewcontroller-destinations).

## See also

- [Installation](../getting-started/installation.md) — installation on every platform.
- [Recipes iOS entrypoint][ios-recipe] — the `MainViewController()` of
  the recipes app.

[ios-xcode]: https://github.com/isaac-udy/Enro/tree/main/recipes/app/ios/xcode/EnroRecipes
[uiviewcontroller-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/common/src/iosMain/kotlin/dev/enro/recipes/interop/UIViewControllerInterop.kt
[ios-recipe]: https://github.com/isaac-udy/Enro/blob/main/recipes/app/ios/src/iosMain/kotlin/dev/enro/recipes/MainViewController.kt
