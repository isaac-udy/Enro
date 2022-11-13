package dev.enro

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.properties.HasName
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.Architectures
import dev.enro.core.ArchitectureException
import org.junit.Test

internal class ProjectArchitecture {

    private val classes = ClassFileImporter().importPackages("dev.enro")

    private val architecture = Architectures.layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("dev.enro..")
        .ignoreDependency(
            describe("is architecture exception") { fromClass ->
                fun isArchitectureException(cls: JavaClass): Boolean {
                    return cls.isAnnotatedWith(ArchitectureException::class.java) ||
                            cls.enclosingClass
                                .takeIf { enclosing -> enclosing.isPresent }
                                ?.let { enclosing -> isArchitectureException(enclosing.get()) }
                            ?: false
                }
                return@describe isArchitectureException(fromClass)
            },
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
    fun composeLayer() {
        architecture
            .whereLayer(EnroLayer.COMPOSE)
            .mayOnlyAccessLayers(*EnroLayer.featureLayerDependencies)
            .check(classes)
    }

    @Test
    fun fragmentLayer() {
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
    fun publicLayer() {
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