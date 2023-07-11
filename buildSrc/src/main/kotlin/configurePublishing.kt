import com.android.build.gradle.LibraryExtension
import groovy.namespace.QName
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.FileInputStream
import java.net.URI
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

    if (isAndroid) {
        extensions.configure<LibraryExtension> {
            publishing {
                singleVariant("release") {
                    withSourcesJar()
                }
            }
        }
    } else {
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

    afterEvaluate {
        group = groupName
        version = versionName

        extensions.configure<PublishingExtension> {
            publications.create<MavenPublication>("release") {
                if (isAndroid) {
                    from(components.getByName("release"))
                } else {
                    from(components.getByName("java"))
                }

                groupId = groupName
                artifactId = moduleName
                version = versionName

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

        tasks.findByName("publishAllPublicationsToSonatypeRepository")
            ?.dependsOn("publishToMavenLocal")
    }
}