package dev.enro.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * The EnroTestRule can be used in both pure JVM based unit tests and instrumented tests that run on devices.
 *
 * In both cases, this rule is designed to install a NavigationController that is accessible by
 * Enro's test extensions, and allow [TestNavigationHandles] to be created, which will record
 * navigation instructions that are made against the navigation handle. The recorded navigation
 * instructions can then be asserted on, in particular by using extensions such as
 * [expectOpenInstruction], [assertActive], [assertClosed], [assertOpened] and others.
 *
 * When EnroTestRule is used in an instrumented test, it will *prevent* regular navigation from
 * occurring, and is designed for testing individual screens in isolation from one another. If you
 * want to perform "real" navigation in instrumented tests, you do not need any Enro test extensions.
 *
 * If you have other TestRules, particularly those that launch Activities or Fragments, you may need
 * to order this TestRule as the first in the sequence, as the rule will need to be executed before
 * an Activity or Fragment under test has been instantiated.
 */
class EnroTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                runEnroTest { base.evaluate() }
            }
        }
    }
}