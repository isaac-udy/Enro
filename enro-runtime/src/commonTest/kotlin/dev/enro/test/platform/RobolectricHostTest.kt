package dev.enro.test.platform

/**
 * Base class for common tests that touch Android framework classes (Bundle,
 * SavedState) at runtime.
 *
 * On the Android host-test target, framework classes come from android.jar
 * stubs, and `isReturnDefaultValues = true` makes them silently return
 * defaults — e.g. `Bundle.getBundle(...)` returns null for a value that was
 * "written" moments earlier, which surfaces as confusing downstream errors
 * ("No valid saved state was found for the key ..."). Extending this class
 * runs the test under Robolectric on that target, providing functional
 * framework implementations; on every other target it is a no-op.
 */
expect abstract class RobolectricHostTest()
