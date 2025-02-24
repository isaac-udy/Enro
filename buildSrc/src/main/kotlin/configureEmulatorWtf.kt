import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import wtf.emulator.EwExtension

fun Project.configureEmulatorWtf() {
    extensions.configure<EwExtension> {
        if(project.hasProperty("ewApiToken")) {
            token.set(project.properties["ewApiToken"].toString())
        } else {
            token.set(java.lang.System.getenv()["EW_API_TOKEN"])
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