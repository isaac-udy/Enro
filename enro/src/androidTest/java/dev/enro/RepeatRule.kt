package dev.enro

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
annotation class RepeatTest(val value: Int = 1)

class RepeatRule : TestRule {

    private class RepeatStatement(private val statement: Statement, private val repeat: Int) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            for (i in 0..repeat - 1) {
                statement.evaluate()
            }
        }
    }

    override fun apply(statement: Statement, description: Description): Statement {
        var result = statement
        val repeat = description.getAnnotation(RepeatTest::class.java)
        if (repeat != null) {
            val times = repeat.value
            result = RepeatStatement(statement, times)
        }
        return result
    }
}