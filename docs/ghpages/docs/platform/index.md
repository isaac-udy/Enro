---
title: Platform-Specific Guides
nav_order: 5
has_children: true
---

# Platform-Specific Guides

How to install and use Enro on each supported platform. The core API is
the same everywhere; these pages cover the install bootstrap and the
platform-specific notes worth knowing.

- [Android](android.md) — `Application.onCreate` install, hosting a
  container from a `ComponentActivity`, and the `enro-compat` story for
  migrating Fragment/Activity destinations incrementally.
- [iOS](ios.md) — `installNavigationController(application:)` from
  Swift's `UIApplicationDelegate`, exposing a `UIViewController` via
  `EnroUIViewController { }`, and embedding it in your app.
- [Desktop](desktop.md) — `controller.openWindow(...)` with
  `GenericRootWindow`, `EnroApplicationContent`, and the multi-window
  pattern.
- [Web](web.md) — `installNavigationController(document)`, the
  `EnroBrowserContent` host, and `InstallWebHistoryPlugin` for browser
  back/forward.
