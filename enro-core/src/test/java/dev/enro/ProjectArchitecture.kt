package dev.enro

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction
import com.tngtech.archunit.lang.syntax.elements.ClassesThat
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

                API_PACKAGE,
                ACTIVITY_PACKAGE,
                COMPOSE_PACKAGE,
                CONTAINER_PACKAGE,
                CONTROLLER_PACKAGE,
                FRAGMENT_PACKAGE,
                HOST_PACKAGE,
                INTERNAL_PACKAGE,
                PLUGINS_PACKAGE,
                RESULTS_PACKAGE,
                SYNTHETIC_PACKAGE,
                EXTENSIONS_PACKAGE,
                VIEWMODEL_PACKAGE,
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
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(EXTENSIONS_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("dev.enro.core..")

        rule.check(classes)
    }

    /**
     * Enro includes several destination implementations, for Activities, Fragments, Composables
     * and Synthetic destintations. These implementation should be independent of one-another,
     * and this test checks that this is the case.
     */
    @Test
    fun destinationsShouldNotDependOnOtherDestinations() {
        destinationPackages.forEach { destinationPackage ->
            val rule = ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(destinationPackage)
                .should()
                .dependOnClassesThat()
                .resideInDestinationPackageExcept(destinationPackage)

            rule.check(classes)
        }
    }

    @Test
    fun destinationsShouldNotDependOnHosts() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage(*destinationPackages.toTypedArray())
            .should()
            .dependOnClassesThat()
            .resideInAPackage(HOST_PACKAGE)

        rule.check(classes)
    }

    @Test
    fun resultsShouldNotDependOnDestinations() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(RESULTS_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInDestinationPackage()

        rule.check(classes)
    }

    @Test
    fun destinationsShouldNotDependOnResults() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInDestinationPackage()
            .should()
            .dependOnClassesThat()
            .resideInAPackage(RESULTS_PACKAGE)

        rule.check(classes)
    }

    @Test
    fun viewmodelShouldNotDependOnDestinations() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(VIEWMODEL_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInDestinationPackage()

        rule.check(classes)
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