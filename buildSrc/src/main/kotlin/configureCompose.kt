import com.android.build.gradle.BaseExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

fun Project.configureCompose() {
    val libs = the<LibrariesForLibs>()
    plugins.apply("org.jetbrains.kotlin.plugin.compose")
    extensions.configure<BaseExtension> {
        buildFeatures.compose = true
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.10"
        }
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