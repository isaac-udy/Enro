
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.buildConfig)
}

java { toolchain { languageVersion.set(libs.versions.jdk.map(JavaLanguageVersion::of)) } }

tasks.withType<JavaCompile>().configureEach {
    options.release.set(libs.versions.jvmTarget.map(String::toInt))
}

val Project.enroVersionName: String get() {
    val versionPropertiesFile = rootProject.file("../version.properties")
    val versionProperties = Properties()
    versionProperties.load(FileInputStream(versionPropertiesFile))
    return versionProperties.getProperty("versionName")
}

buildConfig {
    buildConfigField("String", "VERSION", "\"$enroVersionName\"")
    packageName("dev.enro.gradle")
    useKotlinOutput {
        topLevelConstants = false
        internalVisibility = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(libs.versions.jvmTarget.map(JvmTarget::fromTarget))

        // Lower version for Gradle compat
        progressiveMode.set(false)
        @Suppress("DEPRECATION") languageVersion.set(KotlinVersion.KOTLIN_1_9)
        @Suppress("DEPRECATION") apiVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}

gradlePlugin {
    plugins {
        create("dev.enro.gradle") {
            group = "dev.enro"
            id = "dev.enro.gradle"
            implementationClass = "dev.enro.gradle.EnroGradleSubplugin"
        }
    }
}

kotlin { explicitApi() }

dependencies {
    compileOnly(libs.kotlin.gradle)
    compileOnly(libs.kotlin.gradle.api)
    compileOnly(libs.kotlin.stdLib)
}

configure<MavenPublishBaseExtension> { publishToMavenCentral(automaticRelease = false) }
