import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

// A feature module of the Modular Navigation recipe. feature-home depends on
// the shared keys and opens the screens of :recipes:modular:feature-profile
// without depending on :recipes:modular:feature-profile.
plugins {
    id("configure-library")
    id("com.google.devtools.ksp")
    id("configure-compose")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    sourceSets {
        commonMain.dependencies {
            implementation(project(":recipes:modular:navigation"))
            implementation("dev.enro:enro:${project.enroVersionName}")
        }
    }
}

// The processor runs in every module that declares destinations. It generates
// a binding per destination; the app's NavigationComponent collects the bindings.
dependencies {
    val enroProcessor = "dev.enro:enro-processor:${project.enroVersionName}"
    add("kspCommonMainMetadata", enroProcessor)
    add("kspAndroid", enroProcessor)
    add("kspDesktop", enroProcessor)
    add("kspWasmJs", enroProcessor)
    add("kspIosArm64", enroProcessor)
    add("kspIosSimulatorArm64", enroProcessor)
}
