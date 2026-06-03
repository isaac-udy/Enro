plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "RecipesApp"
            isStatic = true
        }
        iosTarget.compilerOptions {
            optIn.addAll(
                "dev.enro.annotations.AdvancedEnroApi",
                "dev.enro.annotations.ExperimentalEnroApi",
                "kotlin.uuid.ExperimentalUuidApi",
                "kotlin.experimental.ExperimentalObjCName",
            )
        }
    }
    sourceSets {
        iosMain.dependencies {
            implementation(project(":recipes:common"))
            implementation(compose.runtime)
        }
    }
}
