---
title: Platform-Specific Guides
nav_order: 5
has_children: true
---

# Platform-Specific Guides

Each page describes how to install Enro on one platform and the APIs that
exist only on that platform. The core API is the same on every platform.

- [Android](android.md) — installing from `Application.onCreate`, hosting a
  container in a `ComponentActivity`, Fragment and Activity destinations,
  and `enro-compat` for Enro 2 apps.
- [iOS](ios.md) — calling `installNavigationController(application:)` from
  the Swift `UIApplicationDelegate`, creating a `UIViewController` with
  `EnroUIViewController { }`, and `UIViewController` destinations.
- [Desktop](desktop.md) — `controller.openWindow(...)`,
  `GenericRootWindow`, `EnroApplicationContent`, and window destinations.
- [Web](web.md) — `installNavigationController(document)`,
  `EnroBrowserContent`, and `InstallWebHistoryPlugin` for the browser
  back and forward buttons.
