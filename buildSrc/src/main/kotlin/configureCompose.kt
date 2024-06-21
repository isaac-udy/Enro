import com.android.build.gradle.BaseExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

fun Project.configureCompose() {
    val libs = the<LibrariesForLibs>()
    extensions.configure<BaseExtension> {
        buildFeatures.compose = true
    }
    val composePlugin = project.plugins.firstOrNull {
        it::class.java.name.startsWith("org.jetbrains.kotlin.compose.compiler.gradle.")
    }
    if (composePlugin == null) {
        error("Compose plugin not found: you forgot to add `alias(libs.plugins.compose.compiler)` to the plugin block in your build.gradle.kts file.")
    }

    dependencies {
        add("implementation", libs.compose.compiler)
        add("implementation", libs.compose.foundation)
        add("implementation", libs.compose.foundationLayout)
        add("implementation", libs.compose.ui)
        add("implementation", libs.compose.uiTooling)
        add("implementation", libs.compose.runtime)
        add("implementation", libs.compose.viewmodel)
        add("implementation", libs.compose.livedata)
        add("implementation", libs.compose.activity)
        add("implementation", libs.compose.material)
    }
}