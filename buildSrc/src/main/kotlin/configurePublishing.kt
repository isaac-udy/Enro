
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.FileInputStream
import java.util.Properties

fun Project.configureAndroidPublishing(
    name: String,
) = configurePublishing(
    isAndroid = true,
    groupAndModuleName = name
)

fun Project.configureJavaPublishing(
    name: String,
) = configurePublishing(
    isAndroid = false,
    groupAndModuleName = name
)

private fun Project.configurePublishing(
    isAndroid: Boolean,
    groupAndModuleName: String,
) {
    plugins.apply("com.vanniktech.maven.publish")
    plugins.apply("signing")

    val splitName = groupAndModuleName.split(":")
    require(splitName.size == 2)
    val groupName = splitName[0]
    val moduleName = splitName[1]

    val versionProperties = Properties()
    versionProperties.load(FileInputStream(rootProject.file("version.properties")))

    val versionCode = versionProperties.getProperty("versionCode").toInt()
    val versionName = versionProperties.getProperty("versionName")

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(rootProject.file("local.properties")))
    } else {
        localProperties.setProperty(
            "githubUser",
            System.getenv("PUBLISH_GITHUB_USER") ?: "MISSING"
        )
        localProperties.setProperty(
            "githubToken",
            System.getenv("PUBLISH_GITHUB_TOKEN") ?: "MISSING"
        )

        localProperties.setProperty(
            "sonatypeUser",
            System.getenv("PUBLISH_SONATYPE_USER") ?: "MISSING"
        )
        localProperties.setProperty(
            "sonatypePassword",
            System.getenv("PUBLISH_SONATYPE_PASSWORD") ?: "MISSING"
        )

        localProperties.setProperty(
            "signingKeyId",
            System.getenv("PUBLISH_SIGNING_KEY_ID") ?: "MISSING"
        )
        localProperties.setProperty(
            "signingKeyPassword",
            System.getenv("PUBLISH_SIGNING_KEY_PASSWORD") ?: "MISSING"
        )
        localProperties.setProperty(
            "signingKeyLocation",
            System.getenv("PUBLISH_SIGNING_KEY_LOCATION") ?: "MISSING"
        )
    }

    extraProperties["signing.keyId"] = localProperties["signingKeyId"]
    extraProperties["signing.password"] = localProperties["signingKeyPassword"]
    extraProperties["signing.secretKeyRingFile"] = localProperties["signingKeyLocation"]

    afterEvaluate {
        group = groupName
        version = versionName

        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(automaticRelease = false)

            if (localProperties["signingKeyId"] != null && localProperties["signingKeyId"] != "MISSING") {
                signAllPublications()
            }

            coordinates(groupName, moduleName, versionName)

            pom {
                name.set(moduleName)
                description.set("A component of Enro, a small navigation library for Android")
                url.set("https://github.com/isaac-udy/Enro")
                licenses {
                    license {
                        name.set("Enro License")
                        url.set("https://github.com/isaac-udy/Enro/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("isaac.udy")
                        name.set("Isaac Udy")
                        email.set("isaac.udy@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/isaac-udy/Enro.git")
                    developerConnection.set("scm:git:ssh://github.com/isaac-udy/Enro.git")
                    url.set("https://github.com/isaac-udy/Enro/tree/main")
                }
            }
        }

        // Set up sonatype credentials
        System.setProperty("mavenCentralUsername", localProperties["sonatypeUser"].toString())
        System.setProperty("mavenCentralPassword", localProperties["sonatypePassword"].toString())

        // Set up signing properties
        extraProperties["signing.keyId"] = localProperties["signingKeyId"]
        extraProperties["signing.password"] = localProperties["signingKeyPassword"]
        extraProperties["signing.secretKeyRingFile"] = localProperties["signingKeyLocation"]
    }

    afterEvaluate {
        if (isAndroid) {
            tasks.findByName("publishToMavenLocal")
                ?.dependsOn("assembleRelease")
        } else {
            tasks.findByName("publishToMavenLocal")
                ?.dependsOn("assemble")
        }

        tasks.findByName("publish")
            ?.dependsOn("publishToMavenLocal")

        tasks.findByName("publishToMavenCentral")
            ?.dependsOn("publishToMavenLocal")
    }
}