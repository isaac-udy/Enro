import org.gradle.api.Project

/**
 * Formats a Gradle project path as a package name and as camelCase, PascalCase and
 * kebab-case names.
 *
 * `:enro:platforms:android-fragment`
 * - packageName: `dev.enro.platforms.android.fragment`
 * - camelCase: `enroPlatformsAndroidFragment`
 * - pascalCase: `EnroPlatformsAndroidFragment`
 * - kebabCase: `enro-platforms-android-fragment`
 */
@Suppress("CanBeParameter")
class ProjectName(projectPath: String) {

    /**
     * The Gradle path with colons and dashes replaced by dots, prefixed so that the package
     * name starts with `dev.enro`.
     *
     * `:enro-runtime` -> `dev.enro.runtime`
     * `:tests:application` -> `dev.enro.tests.application`
     */
    val packageName = projectPath
        .replace(":", ".")
        .replace("-", ".")
        .dropWhile { it == '.' }
        .let {
            when {
                it.startsWith("dev.enro") -> it
                it.startsWith("enro") -> "dev.$it"
                else -> "dev.enro.$it"
            }
        }

    val camelCase = packageName
        .removePrefix("dev.")
        .fold("") { acc, c ->
            val isUnderscore = acc.lastOrNull() == '_'
            when {
                c.isLetterOrDigit() -> when {
                    isUnderscore -> acc.dropLast(1) + c.uppercase()
                    else -> acc + c
                }

                else -> acc + "_"
            }
        }

    val pascalCase = camelCase
        .first()
        .uppercase()
        .plus(camelCase.drop(1))

    val kebabCase = packageName
        .removePrefix("dev.")
        .replace(".", "-")

    companion object {
        fun fromProject(project: Project): ProjectName {
            return ProjectName(project.path)
        }
    }
}

val Project.projectName: ProjectName
    get() = ProjectName.fromProject(this)

