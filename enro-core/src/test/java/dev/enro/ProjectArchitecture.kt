package dev.enro

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction
import com.tngtech.archunit.lang.syntax.elements.ClassesThat
import org.junit.Test

private const val ACTIVITY_PACKAGE = "dev.enro.core.activity.."
private const val COMPOSE_PACKAGE = "dev.enro.core.compose.."
private const val FRAGMENT_PACKAGE = "dev.enro.core.fragment.."
private const val SYNTHETIC_PACKAGE = "dev.enro.core.synthetic.."

private val destinationPackages = listOf(
    ACTIVITY_PACKAGE,
    COMPOSE_PACKAGE,
    FRAGMENT_PACKAGE,
    SYNTHETIC_PACKAGE,
)


internal class ProjectArchitecture {

    private val classes = ClassFileImporter().importPackages("dev.enro")


    /**
     * Classes in the dev.enro.extensions package should only exist to simplify access to
     * functionality that exists outside of Enro, such as getting the resourceId from a Theme
     * @see ResourceTheme.getAttributeResourceId.kt as an example
     */
    @Test
    fun extensionsShouldNotDependOnEnro() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("dev.enro.extensions")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("dev.enro.core..")

        rule.check(classes)
    }

    @Test
    fun syntheticPackageShouldNotDependOnOtherDestinations() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(SYNTHETIC_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInDestinationPackageExcept(SYNTHETIC_PACKAGE)

        rule.check(classes)
    }

    @Test
    fun composePackageShouldNotDependOnOtherDestinations() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(COMPOSE_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInDestinationPackageExcept(COMPOSE_PACKAGE)

        rule.check(classes)
    }

    @Test
    fun activityPackageShouldNotDependOnOtherDestinations() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(ACTIVITY_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInDestinationPackageExcept(ACTIVITY_PACKAGE)

        rule.check(classes)
    }

    @Test
    fun fragmentPackageShouldNotDependOnOtherDestinations() {
        val rule = ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(FRAGMENT_PACKAGE)
            .should()
            .dependOnClassesThat()
            .resideInDestinationPackageExcept(FRAGMENT_PACKAGE)

        rule.check(classes)
    }
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