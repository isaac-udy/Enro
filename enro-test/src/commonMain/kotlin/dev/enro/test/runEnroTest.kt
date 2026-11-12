package dev.enro.test

/**
 * Runs [block] with a test NavigationController installed, and uninstalls the controller
 * when [block] returns or throws. runEnroTest wraps the whole body of a test:
 * ```
 * @Test
 * fun exampleTest() = runEnroTest { ... }
 * ```
 *
 * [dev.enro.test.EnroTestRule] is the JUnit rule that calls runEnroTest for each test.
 */
public fun runEnroTest(block: () -> Unit) {
    EnroTest.installNavigationController()
    try {
        block()
    } finally {
        EnroTest.uninstallNavigationController()
    }
}