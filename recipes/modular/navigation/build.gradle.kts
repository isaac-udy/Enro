import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

// The Modular Navigation recipe's shared keys. Feature modules depend on this
// module, never on each other. It needs only enro-common: keys are plain
// serializable classes, with no Compose and no annotation processing.
plugins {
    id("configure-library")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    sourceSets {
        commonMain.dependencies {
            api("dev.enro:enro-common:${project.enroVersionName}")
            implementation(libs.kotlinx.serialization)
        }
    }
}
