import java.io.FileInputStream
import java.util.*

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
            substitute(module("dev.enro:enro-core"))
                .using(project(":enro-core"))

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