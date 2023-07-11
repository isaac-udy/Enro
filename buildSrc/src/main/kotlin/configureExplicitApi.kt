import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.configureExplicitApi() {
    tasks.withType<KotlinCompile>() {
        kotlinOptions {
            freeCompilerArgs += "-Xexplicit-api=strict"
        }
    }
}