import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.FileInputStream
import java.util.*

class ConfigurePublishing : Plugin<Project> {
    override fun apply(target: Project) {
        val versionProperties = Properties()
        versionProperties.load(FileInputStream(target.rootProject.file("version.properties")))
        val versionName = versionProperties.getProperty("versionName")

        val groupName = "dev.enro"
        val moduleName = target.projectName.kebabCase

        target.group = groupName
        target.version = versionName

        with(target) {
            with(pluginManager) {
                apply("com.vanniktech.maven.publish")
            }
            configurePublishSigning()

            configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
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
    }
}

private fun Project.configurePublishSigning() {
    plugins.apply("signing")

    val privateProperties = Properties()
    val privatePropertiesFile = rootProject.file("private.properties")
    if (privatePropertiesFile.exists()) {
        privateProperties.load(FileInputStream(rootProject.file("private.properties")))
    } else {
        privateProperties.setProperty(
            "githubUser",
            System.getenv("PUBLISH_GITHUB_USER") ?: "MISSING"
        )
        privateProperties.setProperty(
            "githubToken",
            System.getenv("PUBLISH_GITHUB_TOKEN") ?: "MISSING"
        )

        privateProperties.setProperty(
            "sonatypeUser",
            System.getenv("PUBLISH_SONATYPE_USER") ?: "MISSING"
        )
        privateProperties.setProperty(
            "sonatypePassword",
            System.getenv("PUBLISH_SONATYPE_PASSWORD") ?: "MISSING"
        )

        privateProperties.setProperty(
            "signingKeyId",
            System.getenv("PUBLISH_SIGNING_KEY_ID") ?: "MISSING"
        )
        privateProperties.setProperty(
            "signingKeyPassword",
            System.getenv("PUBLISH_SIGNING_KEY_PASSWORD") ?: "MISSING"
        )
        privateProperties.setProperty(
            "signingKeyLocation",
            System.getenv("PUBLISH_SIGNING_KEY_LOCATION") ?: "MISSING"
        )
    }

    extraProperties["signing.keyId"] = privateProperties["signingKeyId"]
    extraProperties["signing.password"] = privateProperties["signingKeyPassword"]
    extraProperties["signing.secretKeyRingFile"] = privateProperties["signingKeyLocation"]

    afterEvaluate {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/isaac-udy/Enro")
                    credentials {
                        username = privateProperties["githubUser"].toString()
                        password = privateProperties["githubToken"].toString()
                    }
                }
            }
            repositories {
                maven {
                    // This is an arbitrary name, you may also use "mavencentral" or
                    // any other name that's descriptive for you
                    name = "sonatype"
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = privateProperties["sonatypeUser"].toString()
                        password = privateProperties["sonatypePassword"].toString()
                    }
                }
            }
        }

        if (privateProperties["signingKeyId"] != "MISSING") {
            extensions.configure<SigningExtension> {
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }
    }
}
