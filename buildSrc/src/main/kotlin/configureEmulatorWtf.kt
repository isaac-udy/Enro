import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.konan.properties.hasProperty
import wtf.emulator.EwExtension
import java.io.FileInputStream
import java.util.Properties

fun Project.configureEmulatorWtf() {
    extensions.configure<EwExtension> {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(rootProject.file("local.properties")))
        }

        when {
            project.hasProperty("ewApiToken") -> {
                token.set(project.properties["ewApiToken"].toString())
            }
            localProperties.hasProperty("ewApiToken") -> {
                token.set(localProperties["ewApiToken"].toString())
            }
            else -> {
                token.set(java.lang.System.getenv()["EW_API_TOKEN"])
            }
        }

        numShards.set(2)

        devices.set(
            listOf(
                mapOf(
                    "model" to "Pixel2", "version" to 35
                ),
                mapOf(
                    "model" to "Pixel2", "version" to 34
                ),
                mapOf(
                    "model" to "Pixel2", "version" to 33
                ),
                mapOf(
                    "model" to "Pixel2", "version" to 30
                ),
                mapOf(
                    "model" to "Pixel2", "version" to 27
                ),
                mapOf(
                    "model" to "Pixel2", "version" to 23
                ),
            )
        )
    }
}