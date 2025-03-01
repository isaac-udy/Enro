import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.FileInputStream
import java.util.*

class ConfigurePublishing : Plugin<Project> {
    override fun apply(target: Project) {
        val publishType = when {
            target.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") -> PublishType.MULTIPLATFORM
            target.plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> PublishType.JAVA
            target.plugins.hasPlugin("com.android.library") -> PublishType.ANDROID
            else -> throw IllegalArgumentException("Unsupported project type for publishing")
        }
        target.configurePublishingInternal(
            publishType = publishType,
            groupAndModuleName = "dev.enro:${target.projectName.kebabCase}"
        )
    }
}

private fun Project.configurePublishingInternal(
    publishType: PublishType,
    groupAndModuleName: String,
) {
    plugins.apply("maven-publish")
    plugins.apply("signing")

    val splitName = groupAndModuleName.split(":")
    require(splitName.size == 2)

    val groupName = splitName[0]
    val moduleName = splitName[1]

    val versionProperties = Properties()
    versionProperties.load(FileInputStream(rootProject.file("version.properties")))

    val versionCode = versionProperties.getProperty("versionCode").toInt()
    val versionName = versionProperties.getProperty("versionName")

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

    when (publishType) {
        PublishType.ANDROID -> {
            extensions.configure<LibraryExtension> {
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                    }
                }
            }
        }
        PublishType.MULTIPLATFORM -> {
            // Kotlin Multiplatform already sets up most of the publications,
            // but we do still need to configure the Android publishing on the library extensions
            // The publications will be named after the targets (e.g., androidRelease, jvm, etc.)
            // We'll configure them in the afterEvaluate block
            extensions.configure<LibraryExtension> {
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                    }
                }
            }
        }
        PublishType.JAVA -> {
            extensions.configure<JavaPluginExtension> {
                withJavadocJar()
                withSourcesJar()
            }
            val java = extensions.getByType<JavaPluginExtension>()
            val mainSrc = java.sourceSets["main"]
            tasks.withType<Javadoc> {
                source = mainSrc.allJava
                classpath = configurations.getByName("compileClasspath")
                options {
                    setMemberLevel(JavadocMemberLevel.PUBLIC)
                }
            }
        }
    }

    afterEvaluate {
        group = groupName
        version = versionName

        extensions.configure<PublishingExtension> {
            when (publishType) {
                PublishType.MULTIPLATFORM -> {
                    // For Kotlin Multiplatform, we need to configure each publication
                    // that was created by the Kotlin Multiplatform plugin
                    publications.withType<MavenPublication>().configureEach {
                        val targetPublication = this

                        // Set the artifactId with the target name
                        if (targetPublication.name != "kotlinMultiplatform") {
                            targetPublication.artifactId = "${moduleName}-${targetPublication.name}"
                        } else {
                            targetPublication.artifactId = moduleName
                        }

                        configurePom(targetPublication, groupName, moduleName, versionName)
                    }
                    publications.create<MavenPublication>("android") {
                        from(components.getByName("release"))

                        groupId = groupName
                        artifactId = moduleName
                        version = versionName

                        configurePom(this, groupName, moduleName, versionName)
                    }
                    tasks.named("sourceReleaseJar").configure {
                        val resourceCollectors = tasks.findByName("generateActualResourceCollectorsForAndroidMain")
                        if (resourceCollectors != null) dependsOn(resourceCollectors)
                    }
                }
                else -> {
                    publications.create<MavenPublication>("release") {
                        when (publishType) {
                            PublishType.ANDROID -> {
                                from(components.getByName("release"))
                            }
                            else -> {
                                from(components.getByName("java"))
                            }
                        }

                        groupId = groupName
                        artifactId = moduleName
                        version = versionName

                        configurePom(this, groupName, moduleName, versionName)
                    }
                }
            }

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

private fun configurePom(
    publication: MavenPublication,
    groupName: String,
    moduleName: String,
    versionName: String
) {
    publication.pom {
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

enum class PublishType {
    ANDROID,
    JAVA,
    MULTIPLATFORM
}
