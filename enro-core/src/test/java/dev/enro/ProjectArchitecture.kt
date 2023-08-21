package dev.enro

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import org.junit.Assert.fail
import org.junit.Assume.assumeFalse
import org.junit.Test

internal class ProjectArchitecture {

    private val classes = ClassFileImporter().importPackages("dev.enro")

    private val architecture = Architectures.layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("dev.enro..")
        .let {
            EnroLayer.values().fold(it) { architecture, layer ->
                architecture.layer(layer)
            }
        }

    @Test
    fun packagesDoNotDependOnOtherPackagesInternal() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage("dev.enro..")
            .should(NotDependOnInternalClasses)
            .check(classes)
    }

    @Test
    fun packagesDoNotDependOnOtherPackageGroups() {
        val allowedDependencies = listOf(
            "" to "dev.enro.core",
            "" to "dev.enro.annotation",
            "dev.enro.destination" to "dev.enro.animation",
            // Destinations are allowed to access Android specific functionality,
            // and the Android specific functionality is allowed to access the destinations
            "dev.enro.destination" to "dev.enro.android",
            "dev.enro.android" to "dev.enro.destination",
        )
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage("dev.enro.destination.activity")
            .should(NotDependOnOtherPackageGroups(allowedDependencies))
            .check(classes)
    }
}

internal object NotDependOnInternalClasses : ArchCondition<JavaClass>("not depend on internal classes") {
    override fun check(item: JavaClass, events: ConditionEvents) {
        item.directDependenciesFromSelf
            .forEach { dependency ->
                val originPackage = item.packageName
                val targetPackage = dependency.targetClass.packageName
                if (!targetPackage.startsWith("dev.enro")) return@forEach
                val isInternalAccess = targetPackage.contains("internal") && !targetPackage.startsWith(originPackage)
                val message = if (isInternalAccess) {
                    "${item.simpleName.ifBlank { item.sourceCodeLocation.sourceFileName }} accesses ${dependency.targetClass.simpleName.ifBlank { dependency.targetClass.sourceCodeLocation.sourceFileName }} at ${dependency.sourceCodeLocation}"
                } else { "" }
                events.add(SimpleConditionEvent(item, !isInternalAccess, message))
            }
    }
}

internal class NotDependOnOtherPackageGroups(
    private val allowedDependencies: List<Pair<String, String>>
) : ArchCondition<JavaClass>("not depend on internal classes") {
    override fun check(item: JavaClass, events: ConditionEvents) {
        item.directDependenciesFromSelf
            .forEach { dependency ->
                val originPackage = item.packageName
                    .split(".")
                    .takeWhile { it != "internal" }
                    .joinToString(separator = ".")
                val targetPackage = dependency.targetClass.packageName
                if (!targetPackage.startsWith("dev.enro")) return@forEach

                val isAllowed = allowedDependencies.any {
                    originPackage.startsWith(it.first) &&
                            targetPackage.startsWith(it.second)
                }
                if (isAllowed) return@forEach

                val packageGroupAccess = !targetPackage.startsWith(originPackage)
                val message = if (packageGroupAccess) {
                    "${item.simpleName.ifBlank { item.sourceCodeLocation.sourceFileName }} accesses ${dependency.targetClass.simpleName.ifBlank { dependency.targetClass.sourceCodeLocation.sourceFileName }} at ${dependency.sourceCodeLocation}"
                } else { "" }
                events.add(SimpleConditionEvent(item, !packageGroupAccess, message))
            }
    }
}

/**
 * This marks a class as being a "proposed" architectural rule. Due to the fact that Enro was built
 * without strict enforcement of architectural rules, there are several places where the actual
 * dependencies between different layers of the architecture fall short of the desired architecture.
 *
 * The proposedArchitectureRule function serves as a way to record that a rule should pass, without
 * causing the test to actually fail due to violations. It's basically a fancy way of ignoring a test,
 * while still printing the violations that cause the architecture test to fail.
 *
 * Once a proposedArchitectureRule has no failures (does not throw when ran) then the proposedArchitectureRule
 * will actually fail, to indicate that the proposed rule should be promoted to a "real" rule.
 */
internal fun proposedArchitectureRule(block: () -> Unit) {
    runCatching(block)
        .onFailure {
            println(it.message)
            assumeFalse(true)
        }
        .onSuccess {
            fail("This proposed architecture rule has no violations, and should be promoted to a full architecture rule")
        }
}