package dev.enro.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A JUnit rule that runs each test in [runEnroTest]. EnroTestRule works in JVM unit tests
 * and in instrumented tests on a device.
 *
 * During a test, a NavigationController is installed for the Enro test extensions, and a
 * [TestNavigationHandle] records the operations executed on the handle. Extensions such as
 * [assertOpened], [assertClosed] and [assertActive] assert on the recorded operations.
 *
 * In an instrumented test, EnroTestRule prevents real navigation, so that a test covers one
 * screen in isolation. A test that performs real navigation does not use EnroTestRule.
 *
 * EnroTestRule must run before the Activity or Fragment under test is created. Order
 * EnroTestRule before a rule that launches an Activity or Fragment.
 */
public class EnroTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                runEnroTest { base.evaluate() }
            }
        }
    }
}