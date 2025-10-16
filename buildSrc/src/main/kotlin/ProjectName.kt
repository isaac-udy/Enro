import org.gradle.api.Project

/**
 * ProjectName will take a Gradle project path, and make it easy to use this name in different formats. Formats
 * available are `packageName`, `camelCase`, and `pascalCase`.
 *
 * Examples:
 * `:enro-runtime`
 * - packageName: `dev.enro.runtime`
 * - camelCase: `enroRuntime`
 * - pascalCase: `EnroRuntime`
 *
 * `:enro:platforms:android-fragment`
 * - packageName: `dev.enro.platforms.android.fragment`
 * - camelCase: `enroPlatformsAndroidFragment`
 * - pascalCase: `EnroPlatformsAndroidFragment`
 */
@Suppress("CanBeParameter")
class ProjectName(projectPath: String) {

    /**
     * This is the package name of the project, based on the project's gradle path.
     * This is the project's gradle path with colons and dashes replaced with dots.
     *
     * If the project path starts with "enro", it will be replaced with "dev.enro".
     *
     * Examples:
     * `:enro-runtime` -> `dev.enro.runtime`
     * `:enro:platforms:android-fragment` -> `dev.enro.platforms.android.fragment`
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

    /**
     * This is a camelCase version of the project's package name; it is the package name with underscores and dots
     * removed, and the first letter of each word capitalized.
     *
     * Examples:
     * `:enro-runtime` -> `enroRuntime`
     * `:enro:platforms:android-fragment` -> `enroPlatformsAndroidFragment`
     */
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

    /**
     * This is a pascalCase version of the project's package name; it is the camelCase version with the first letter
     * capitalized.
     *
     * Examples:
     * `:enro-runtime` -> `EnroRuntime`
     * `:enro:platforms:android-fragment` -> `EnroPlatformsAndroidFragment`
     */
    val pascalCase = camelCase
        .first()
        .uppercase()
        .plus(camelCase.drop(1))

    /**
     * This is a kebabCase version of the project's package name; it is the package name with dots replaced with dashes.
     *
     * Examples:
     * `:enro-runtime` -> `enro-runtime`
     * `:enro:platforms:android-fragment` -> `enro-platforms-core-fragment`
     */
    val kebabCase = packageName
        .removePrefix("dev.")
        .replace(".", "-")

    companion object {
        /**
         * Creates a ProjectName object from a Gradle project.
         */
        fun fromProject(project: Project): ProjectName {
            return ProjectName(project.path)
        }
    }
}

/**
 * Creates a ProjectName object from a Gradle project.
 */
val Project.projectName: ProjectName
    get() = ProjectName.fromProject(this)

