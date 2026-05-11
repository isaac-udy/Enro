import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("configure-application")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.enro:enro:${project.enroVersionName}")
            implementation("dev.enro:enro-compat:${project.enroVersionName}")
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.lifecycle)
        }

        androidMain.dependencies {
            implementation(libs.compose.material)
            implementation(libs.kotlin.stdLib)
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.viewmodel)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.fragment.compose)
            implementation(libs.androidx.activity)
            implementation(libs.material)
        }

        desktopMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.savedState)
        }
    }
}

dependencies {
    val enroProcessor = "dev.enro:enro-processor:${project.enroVersionName}"
    add("kspCommonMainMetadata", enroProcessor)
    add("kspAndroid", enroProcessor)
    add("kspDesktop", enroProcessor)
    add("kspWasmJs", enroProcessor)
    add("kspIosArm64", enroProcessor)
    add("kspIosSimulatorArm64", enroProcessor)
}
