package dev.enro.processor.extensions

import com.squareup.kotlinpoet.ClassName


object ClassNames {

    object Kotlin {
        val composable = ClassName(
            "androidx.compose.runtime",
            "Composable"
        )
        val unit = ClassName(
            "kotlin",
            "Unit"
        )
        val navigationModuleScope = ClassName(
            "dev.enro.controller",
            "NavigationModuleScope"
        )
        val navigationDestination = ClassName("dev.enro.annotations", "NavigationDestination")
        val navigationComponent = ClassName("dev.enro.annotations", "NavigationComponent")
        val generatedNavigationBinding = ClassName("dev.enro.annotations", "GeneratedNavigationBinding")
        val generatedNavigationModule = ClassName("dev.enro.annotations", "GeneratedNavigationModule")

        val optIn = ClassName("kotlin", "OptIn")
        val experimentalMaterialApi = ClassName("androidx.compose.material", "ExperimentalMaterialApi")

        val experimentalObjCName = ClassName("kotlin.experimental", "ExperimentalObjCName")
        val objCName = ClassName("kotlin.native", "ObjCName")

        val navigationController = ClassName(
            "dev.enro",
            "EnroController"
        )

        val navigationKey = ClassName(
            "dev.enro",
            "NavigationKey"
        )

        val uiViewController = ClassName(
            "platform.UIKit",
            "UIViewController"
        )

        val enroIosExtensions = ClassName(
            "dev.enro",
            "Enro"
        )

        val navigationComponentConfiguration = ClassName(
            "dev.enro.controller",
            "NavigationComponentConfiguration"
        )

        val kotlinxSerializable = ClassName(
            "kotlinx.serialization",
            "Serializable"
        )
    }
}