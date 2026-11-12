# Test Harness

This set of Gradle modules is used as a test harness for Enro. The `:tests:application` module is a module which hosts tests for Enro, such as testing navigation between destinations and how the annotation processor works with incremental changes. 

This application can be used as a place to create situations that reproduce bugs with Enro, and then tests can be written to resolve those bugs. 

The destination selection screen shows an explicit list of NavigationKeys. To add a destination for testing:

1. Create the NavigationKey and its destination.
2. Add the key to the list in `SelectDestination.kt` (`tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/SelectDestination.kt`) if it works on every platform. For a platform-specific destination, register it from that platform's source set with `SelectDestination.registerSelectableDestinations(...)`, as `TestApplication.kt` does on Android.
3. Launch the application and open the key from the destination selection screen.

The "Samples" entry opens three larger sample apps (Loan Application, Recipe Browser, Travel Planner) under `tests/application/common/src/commonMain/kotlin/dev/enro/tests/application/samples`.
