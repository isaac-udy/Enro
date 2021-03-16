package dev.enro.test

import dev.enro.core.controller.NavigationController
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
    val navigationController = EnroTest.getCurrentNavigationController()
    navigationController.isInTest = true
    block()
    navigationController.isInTest = false
}

private var NavigationController.isInTest: Boolean
    get() {
        return NavigationController::class.java.getDeclaredField("isInTest")
            .let {
                it.isAccessible = true
                val result = it.get(this) as Boolean
                it.isAccessible = false

                return@let result
            }
    }
    set(value) {
        NavigationController::class.java.getDeclaredField("isInTest")
            .let {
                it.isAccessible = true
                val result = it.set(this, value)
                it.isAccessible = false

                return@let result
            }
    }