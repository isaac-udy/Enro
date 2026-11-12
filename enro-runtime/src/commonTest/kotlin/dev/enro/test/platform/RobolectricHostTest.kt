package dev.enro.test.platform

/**
 * Base class for a common test that calls Android framework classes (Bundle, SavedState).
 *
 * On the Android host-test target, the framework classes are android.jar stubs, and
 * `isReturnDefaultValues = true` makes each stub return a default value. For example,
 * `Bundle.getBundle(...)` returns null for a value that was written, and a later call fails
 * with "No valid saved state was found for the key ...". A subclass of [RobolectricHostTest]
 * runs under Robolectric on the Android host-test target, where Robolectric provides working
 * framework classes. On every other target [RobolectricHostTest] does nothing.
 */
expect abstract class RobolectricHostTest()
