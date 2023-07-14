plugins {
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
    id("wtf.emulator.gradle")
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}
configureAndroidApp("dev.enro.test.application")
configureCompose()
configureEmulatorWtf()

dependencies {
    implementation(project(":tests:module-one"))

    implementation(project(":enro"))
    if (project.hasProperty("enroExampleUseKapt")) {
        kapt(project(":enro-processor"))
    }
    else {
        ksp(project(":enro-processor"))
    }

    lintChecks(project(":enro-lint"))

    implementation(libs.compose.material)
    implementation(libs.compose.accompanist.systemUiController)

    implementation(libs.kotlin.reflect)

    implementation(libs.kotlin.stdLib)
    implementation(libs.androidx.core)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)

    implementation(libs.material)

    androidTestImplementation(project(":enro-test"))

    androidTestImplementation(libs.testing.junit)

    androidTestImplementation(libs.kotlin.reflect)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.fragment)
    androidTestImplementation(libs.androidx.activity)
    androidTestImplementation(libs.androidx.recyclerview)

    androidTestImplementation(libs.testing.androidx.fragment)
    androidTestImplementation(libs.testing.androidx.junit)
    androidTestImplementation(libs.testing.androidx.espresso)
    androidTestImplementation(libs.testing.androidx.espressoRecyclerView)
    androidTestImplementation(libs.testing.androidx.espressoIntents)
    androidTestImplementation(libs.testing.androidx.runner)

    androidTestImplementation(libs.testing.androidx.compose)
}

fun Task.runBuildTest(
    block: () -> Unit
) {
    fun runBuild() {
        val home = System.getProperty("java.home")
        exec {
            environment("JAVA_HOME", home)
            commandLine(
                "$rootDir/gradlew",
                ":tests:application:assembleDebug",
            )
        }
    }

    doFirst {
        rootProject.exec {
            commandLine(
                "git",
                "stash",
                "push",
                "--include-untracked"
            )
        }
        val result = runCatching {
            runBuild()
            block()
            runBuild()
        }
        rootProject.exec {
            commandLine(
                "git",
                "stash",
                "pop",
            )
        }
        result.getOrThrow()
    }
}

task("testApplicationBuildTests") {
    runBuildTest {
        val editableFile = file("./src/main/java/dev/enro/tests/application/TestApplicationEditableDestination.kt")
        val editedContent = """
package dev.enro.tests.application

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
internal class TestApplicationEditableDestination_Edited : NavigationKey.SupportsPush

@Composable
@NavigationDestination(TestApplicationEditableDestination_Edited::class)
internal fun TestApplicationEditableScreen_Edited() {
    Text("Test Screen (Edited)")
}
""".trimIndent()

        editableFile.writeText(editedContent)
        val renamedFile = file("./src/main/java/dev/enro/tests/application/TestApplicationEditableDestination_Edited.kt")
        editableFile.renameTo(renamedFile)
    }
}
