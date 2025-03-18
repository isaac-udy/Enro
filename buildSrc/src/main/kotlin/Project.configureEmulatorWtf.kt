import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.konan.properties.hasProperty
import wtf.emulator.EwExtension
import java.io.FileInputStream
import java.util.*

fun Project.configureEmulatorWtf(numShards: Int = 2) {
    extensions.configure<EwExtension> {

        val privateProperties = Properties()
        val privatePropertiesFile = rootProject.file("private.properties")
        if (privatePropertiesFile.exists()) {
            privateProperties.load(FileInputStream(rootProject.file("private.properties")))
        }

        when {
            project.hasProperty("ewApiToken") -> {
                token.set(project.properties["ewApiToken"].toString())
            }
            privateProperties.hasProperty("ewApiToken") -> {
                token.set(privateProperties["ewApiToken"].toString())
            }
            else -> {
                token.set(java.lang.System.getenv()["EW_API_TOKEN"])
            }
        }

        this.numShards.set(numShards)

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