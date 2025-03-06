package dev.enro.test

/**
 * runEnroTest is a way to perform the same behaviour as that of the EnroTestRule, but without
 * using a JUnit TestRule. It is designed to wrap the entire block of a test, as in:
 * ```
 * @Test
 * fun exampleTest() = runEnroTest { ... }
 * ```
 *
 * See the documentation for [EnroTestRule] for more information.
 */
fun runEnroTest(block: () -> Unit) {
    EnroTest.installNavigationController()
    try {
        block()
    } finally {
        EnroTest.uninstallNavigationController()
    }
}