import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.konan.properties.hasProperty
import wtf.emulator.DeviceModel
import wtf.emulator.EwExtension
import java.io.FileInputStream
import java.util.*

fun Project.configureEmulatorWtf(numShards: Int = 2) {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val apiToken: String? = when {
        project.hasProperty("ewApiToken") -> project.properties["ewApiToken"].toString()
        localProperties.hasProperty("ewApiToken") -> localProperties["ewApiToken"].toString()
        else -> java.lang.System.getenv()["EW_API_TOKEN"]
    }

    extensions.configure<EwExtension> {
        token.set(apiToken)

        this.numShards.set(numShards)

        device {
            model.set(DeviceModel.PIXEL_2_ATD)
            version.set(36)
        }
        device {
            model.set(DeviceModel.PIXEL_2_ATD)
            version.set(35)
        }
        device {
            model.set(DeviceModel.PIXEL_2_ATD)
            version.set(34)
        }
        device {
            model.set(DeviceModel.PIXEL_2_ATD)
            version.set(33)
        }
        device {
            model.set(DeviceModel.PIXEL_2_ATD)
            version.set(30)
        }
        device {
            model.set(DeviceModel.PIXEL_2)
            version.set(27)
        }
        device {
            model.set(DeviceModel.PIXEL_2)
            version.set(23)
        }
    }

    // Without a token the emulator.wtf CLI fails the build with "Http error 403:
    // Invalid apiToken". That is the normal state for pull_request CI runs from
    // forks (GitHub withholds repository secrets from them) and for local
    // checkouts without a token, so skip the device tests — loudly — rather than
    // failing every other check in the same run. The test APKs still build, so the
    // instrumented test sources are still compile-checked. Maintainers get a full
    // device-test run by pushing the branch to this repository.
    if (apiToken.isNullOrBlank()) {
        tasks.matching { it.name.endsWith("WithEmulatorWtf") }.configureEach {
            onlyIf("no emulator.wtf API token is configured") {
                logger.warn(
                    "Skipping $path: no emulator.wtf API token is configured " +
                        "(set ewApiToken in local.properties, pass -PewApiToken, or set EW_API_TOKEN). " +
                        "Device tests do not run for pull requests from forks because repository " +
                        "secrets are unavailable to them; push the branch to this repository for a full run."
                )
                false
            }
        }
    }
}
