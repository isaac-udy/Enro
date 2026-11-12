import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

// A feature module of the Modular Navigation recipe. feature-profile depends
// on the shared keys, not on :recipes:modular:feature-home.
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

dependencies {
    val enroProcessor = "dev.enro:enro-processor:${project.enroVersionName}"
    add("kspCommonMainMetadata", enroProcessor)
    add("kspAndroid", enroProcessor)
    add("kspDesktop", enroProcessor)
    add("kspWasmJs", enroProcessor)
    add("kspIosArm64", enroProcessor)
    add("kspIosSimulatorArm64", enroProcessor)
}
