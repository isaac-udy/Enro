# Test Harness

The Gradle modules under `tests/` are the test harness for Enro. The `:tests:application` module contains tests of Enro, including tests of navigation between destinations and tests of the annotation processor with incremental changes.

Use the `:tests:application` app to reproduce an Enro bug, then write a test that fails until the bug is fixed.

The destination selection screen shows an explicit list of NavigationKeys. To add a destination for testing:

1. Create the NavigationKey and the NavigationKey's destination.
2. If the destination runs on every platform, add the key to the list in `SelectDestination.kt` (`tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/SelectDestination.kt`). Register a platform-specific destination from the platform's source set with `SelectDestination.registerSelectableDestinations(...)`, as `TestApplication.kt` does on Android.
3. Launch the application and open the key from the destination selection screen.

The "Samples" entry opens three larger sample apps (Loan Application, Recipe Browser, Travel Planner) under `tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples`.
