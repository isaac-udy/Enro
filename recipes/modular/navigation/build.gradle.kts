import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

// The Modular Navigation recipe's shared keys. Feature modules depend on this
// module, never on each other. The keys module requires only enro-common: keys
// are serializable classes, with no Compose and no annotation processing.
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
