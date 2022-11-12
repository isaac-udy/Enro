package dev.enro

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction
import com.tngtech.archunit.lang.syntax.elements.ClassesThat
import com.tngtech.archunit.library.Architectures
import org.junit.Test

private const val API_PACKAGE = "dev.enro.core"
private const val ACTIVITY_PACKAGE = "dev.enro.core.activity.."
private const val COMPOSE_PACKAGE = "dev.enro.core.compose.."
private const val CONTAINER_PACKAGE = "dev.enro.core.container.."
private const val CONTROLLER_PACKAGE = "dev.enro.core.controller.."
private const val FRAGMENT_PACKAGE = "dev.enro.core.fragment.."
private const val HOST_PACKAGE = "dev.enro.core.hosts.."
private const val INTERNAL_PACKAGE = "dev.enro.core.internal.."
private const val PLUGINS_PACKAGE = "dev.enro.core.plugins.."
private const val RESULTS_PACKAGE = "dev.enro.core.result.."
private const val SYNTHETIC_PACKAGE = "dev.enro.core.synthetic.."

private const val EXTENSIONS_PACKAGE = "dev.enro.extensions.."
private const val VIEWMODEL_PACKAGE = "dev.enro.viewmodel.."

private val destinationPackages = listOf(
    ACTIVITY_PACKAGE,
    COMPOSE_PACKAGE,
    FRAGMENT_PACKAGE,
    SYNTHETIC_PACKAGE,
)


internal class ProjectArchitecture {

    private val classes = ClassFileImporter().importPackages("dev.enro")

    private val architecture = Architectures.layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("dev.enro..")
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
    fun extensionsShouldNotDependOnEnro() {
        architecture
            .whereLayer(EnroLayer.EXTENSIONS)
            .mayNotAccessAnyLayer()
            .check(classes)
    }

    @Test
    fun allClassesAreContainerInArchitecture() {
        architecture
            .ensureAllClassesAreContainedInArchitectureIgnoring(isTestSource)
            .check(classes)
    }

    @Test
    fun destinationLayers() {
        val allowableDestinationDependencies = arrayOf(
            EnroLayer.PUBLIC,
            EnroLayer.CONTROLLER,
            EnroLayer.DEPENDENCY_INJECTION,
        )

        architecture
            .whereLayers(*EnroLayer.destinationLayers) { mayOnlyAccessLayers(*allowableDestinationDependencies) }
            .check(classes)
    }
}

internal fun <C : Any> ClassesThat<C>.resideInDestinationPackage(): C {
    return resideInAnyPackage(*destinationPackages.toTypedArray())
}

internal fun ClassesThat<ClassesShouldConjunction>.resideInDestinationPackageExcept(
    exceptedPackage: String
): ClassesShouldConjunction {
    val packages = destinationPackages
        .filter { !it.startsWith(exceptedPackage) }
        .toTypedArray()

    require(packages.isNotEmpty())
    return resideInAnyPackage(*packages)
}