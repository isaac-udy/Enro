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
        classpath(libs.hilt.gradle)
        classpath(libs.emulator.wtf.gradle)
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
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
        val versionCode = (existingProperties["versionCode"] as Int) + 1

        if(versionName == existingProperties["versionName"]) {
            error("The versionName '$versionName' is the current versionName")
        }

        versionPropertiesFile.writeText("versionName=$versionName\nversionCode=$versionCode")
    }
}