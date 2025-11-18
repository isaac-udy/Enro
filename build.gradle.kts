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

            substitute(module("dev.enro:enro-compiler-plugin"))
                .using(project(":enro-compiler-plugin"))

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
            tasks.findByName("testDebugWithEmulatorWtf")?.let { continuousIntegration.dependsOn(it) }
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

tasks.register("publishEnroLocal") {
    group = "publishing"
    description = "Publishes Enro libraries to Maven Local"

    doLast {
        exec {
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
                "publishIosX64PublicationToMavenLocal",

                "--no-parallel", "-Dorg.gradle.workers.max=1"
            )
        }
    }
}
