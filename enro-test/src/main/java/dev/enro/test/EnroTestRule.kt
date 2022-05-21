package dev.enro.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class EnroTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                runEnroTest { base.evaluate() }
            }
        }
    }
}

fun runEnroTest(block: () -> Unit) {
    EnroTest.installNavigationController()
    try {
        block()
    } finally {
        EnroTest.uninstallNavigationController()
    }
}