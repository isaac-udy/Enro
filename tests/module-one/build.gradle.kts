import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("configure-library")
    id("com.google.devtools.ksp")
    id("configure-compose")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    sourceSets {
        commonMain.dependencies {
            implementation("dev.enro:enro:${project.enroVersionName}")
            implementation("dev.enro:enro-compat:${project.enroVersionName}")
        }
        androidMain.dependencies {
            implementation(libs.compose.accompanist.systemUiController)
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.viewmodel)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.activity)
            implementation(libs.material)
        }
    }
}

dependencies {
    add("kspAndroid", "dev.enro:enro-processor:${project.enroVersionName}")
    lintChecks(project(":enro-lint"))
}
