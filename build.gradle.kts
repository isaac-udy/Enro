import java.io.FileInputStream
import java.util.Properties

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.kotlin.serialization.gradle)
        classpath(libs.processing.ksp.gradle)
        classpath(libs.emulator.wtf.gradle)
        classpath(libs.maven.publish.gradle)
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("dev.enro:enro-common"))
                .using(project(":enro-common"))

            substitute(module("dev.enro:enro-runtime"))
                .using(project(":enro-runtime"))

            substitute(module("dev.enro:enro-test"))
                .using(project(":enro-test"))

            substitute(module("dev.enro:enro-compat"))
                .using(project(":enro-compat"))

            substitute(module("dev.enro:enro-annotations"))
                .using(project(":enro-annotations"))

            substitute(module("dev.enro:enro-processor"))
                .using(project(":enro-processor"))

            substitute(module("dev.enro:enro"))
                .using(project(":enro"))
        }
    }
}

subprojects {
    afterEvaluate {
        tasks.register("continuousIntegration") {
            val continuousIntegration = this
            tasks.findByName("lintDebug")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("testDebugUnitTest")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("desktopTest")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("wasmJsBrowserTest")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("testDebugWithEmulatorWtf")?.let { continuousIntegration.dependsOn(it) }
            // Compile-only fallbacks so modules without tests (e.g. recipes)
            // are still build-checked by CI. For modules with tests, these are
            // no-ops — the test tasks above already depend on compilation.
            tasks.findByName("compileKotlinDesktop")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("compileDebugKotlinAndroid")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("compileKotlinWasmJs")?.let { continuousIntegration.dependsOn(it) }
        }

        // Separate aggregate task for the macOS CI runner — runs only the
        // iOS-platform tests (the rest already runs on Linux via
        // continuousIntegration). Keeps the macOS minutes spend tight.
        tasks.register("continuousIntegrationMacOs") {
            val ci = this
            tasks.findByName("iosSimulatorArm64Test")?.let { ci.dependsOn(it) }
            // Compile-only fallback for modules without iOS tests, so a
            // K/Native compile failure still shows up.
            tasks.findByName("compileKotlinIosSimulatorArm64")?.let { ci.dependsOn(it) }
        }
    }
}

tasks.register("updateVersion") {
    doLast {
        if (!project.hasProperty("versionName")) {
            error("The updateVersion task requires a versionName property to be passed as an argument")
        }
        val versionPropertiesFile = rootProject.file("version.properties")
        val existingProperties = Properties()
        existingProperties.load(FileInputStream(versionPropertiesFile))

        val versionName = project.properties["versionName"]
        val versionCode = (existingProperties["versionCode"].toString().toInt()) + 1

        if(versionName == existingProperties["versionName"]) {
            error("The versionName '$versionName' is the current versionName")
        }

        versionPropertiesFile.writeText("versionName=$versionName\nversionCode=$versionCode")
    }
}

tasks.register<Exec>("publishEnroLocal") {
    group = "publishing"
    description = "Publishes Enro libraries to Maven Local"

    workingDir = rootProject.projectDir
    commandLine(
        "./gradlew",
        ":enro-processor:publishMavenPublicationToMavenLocal",
        ":enro-annotations:publishAndroidReleasePublicationToMavenLocal",
        ":enro-annotations:publishDesktopPublicationToMavenLocal",

        "publishKotlinMultiplatformPublicationToMavenLocal",
        "publishAndroidReleasePublicationToMavenLocal",
        "publishDesktopPublicationToMavenLocal",
//                "publishFrontendJsPublicationToMavenLocal",
        "publishIosArm64PublicationToMavenLocal",
        "publishIosSimulatorArm64PublicationToMavenLocal",

        "--no-parallel", "-Dorg.gradle.workers.max=1"
    )
}
