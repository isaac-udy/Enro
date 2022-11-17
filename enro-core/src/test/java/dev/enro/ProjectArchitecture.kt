package dev.enro

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.properties.HasName
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import org.junit.Assert.fail
import org.junit.Assume.assumeFalse
import org.junit.Test

internal class ProjectArchitecture {

    private val classes = ClassFileImporter().importPackages("dev.enro")

    private val architecture = Architectures.layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("dev.enro..")
        .ignoreDependency(
            isArchitectureException,
            describe("any class") { true },
        )
        .let {
            EnroLayer.values().fold(it) { architecture, layer ->
                architecture.layer(layer)
            }
        }

    /**
     * This test exists to ensure that new packages are not added to Enro without being included
     * in these architecture rules. This test checks that all classes that are in packages under
     * "dev.enro" belong to a specific subset of packages. If a new package is added to Enro without
     * updating this test, the test will fail.
     */
    @Test
    fun newPackagesShouldBeAddedToTheArchitectureRules() {
        val rule = ArchRuleDefinition.classes()
            .that()
            .resideInAPackage("dev.enro..")
            .should()
            .resideInAnyPackage(
                "dev.enro.core.test..",
                "dev.enro",
                *EnroPackage.values().map { it.packageName }.toTypedArray()
            )

        rule.check(classes)
    }

    /**
     * Classes in the dev.enro.extensions package should only exist to simplify access to
     * functionality that exists outside of Enro, such as getting the resourceId from a Theme
     * @see [dev.enro.extensions.getAttributeResourceId] as an example
     */
    @Test
    fun extensionsLayer() {
        architecture
            .whereLayer(EnroLayer.EXTENSIONS)
            .mayNotAccessAnyLayer()
            .check(classes)
    }

    @Test
    fun allClassesAreContainedInArchitecture() {
        architecture
            .ensureAllClassesAreContainedInArchitectureIgnoring(isTestSource)
            .check(classes)
    }

    @Test
    fun activityLayer() {
        architecture
            .whereLayer(EnroLayer.ACTIVITY)
            .mayOnlyAccessLayers(*EnroLayer.featureLayerDependencies)
            .check(classes)
    }

    @Test
    fun composeLayer() = proposedArchitectureRule {
        architecture
            .whereLayer(EnroLayer.COMPOSE)
            .mayOnlyAccessLayers(*EnroLayer.featureLayerDependencies)
            .check(classes)
    }

    @Test
    fun fragmentLayer() = proposedArchitectureRule {
        architecture
            .whereLayer(EnroLayer.FRAGMENT)
            .mayOnlyAccessLayers(*EnroLayer.featureLayerDependencies)
            .check(classes)
    }

    @Test
    fun syntheticLayer() {
        architecture
            .whereLayer(EnroLayer.SYNTHETIC)
            .mayOnlyAccessLayers(*EnroLayer.featureLayerDependencies)
            .check(classes)
    }

    @Test
    fun viewModelLayer() {
        architecture
            .whereLayers(EnroLayer.VIEW_MODEL) { mayOnlyAccessLayers(*EnroLayer.featureLayerDependencies) }
            .check(classes)
    }

    @Test
    fun publicLayer() = proposedArchitectureRule {
        val allowableDependencies = arrayOf(
            EnroLayer.EXTENSIONS,
        )

        architecture
            .whereLayer(EnroLayer.PUBLIC)
            .mayOnlyAccessLayers(*allowableDependencies)
            .ignoreDependency(
                describe("is public api for results") {
                    it.name == "dev.enro.core.result.EnroResultExtensionsKt"
                },
                describe("is internal results class") {
                    JavaClass.Predicates.resideInAPackage(EnroPackage.RESULTS_INTERNAL_PACKAGE.packageName).test(it)
                }
            )
            .check(classes)
    }

    @Test
    fun hostLayer() {
        val allowableDependencies = arrayOf(
            EnroLayer.PUBLIC,
            EnroLayer.EXTENSIONS,
            EnroLayer.ACTIVITY,
            EnroLayer.COMPOSE,
            EnroLayer.FRAGMENT,
        )

        architecture
            .ignoreDependency(
                HasName.Predicates.nameStartingWith("dev.enro.core.hosts.HostComponentKt"),
                HasName.Predicates.nameStartingWith("dev.enro.core.controller.NavigationComponentBuilder"),
            )
            .whereLayer(EnroLayer.HOSTS)
            .mayOnlyAccessLayers(*allowableDependencies)
            .check(classes)
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