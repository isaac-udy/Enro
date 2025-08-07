
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
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

    afterEvaluate {
        group = groupName
        version = versionName

        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(automaticRelease = false)

            if (System.getenv("ORG_GRADLE_PROJECT_mavenCentralUsername") != null) {
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