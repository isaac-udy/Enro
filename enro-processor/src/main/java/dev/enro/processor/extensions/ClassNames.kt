package dev.enro.processor.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.javapoet.ClassName as JavaClassName


object ClassNames {
    object Java {
        val kotlinFunctionOne = JavaClassName.get(
            "kotlin.jvm.functions",
            "Function1"
        )
        val navigationModuleScope = JavaClassName.get(
            "dev.enro.core.controller",
            "NavigationModuleScope"
        )
        val jvmClassMappings = JavaClassName.get(
            "kotlin.jvm",
            "JvmClassMappingKt"
        )
        val unit = JavaClassName.get(
            "kotlin",
            "Unit"
        )
        val componentActivity = JavaClassName.get(
            "androidx.activity",
            "ComponentActivity"
        )
        val activityNavigationBindingKt = JavaClassName.get(
            "dev.enro.core.activity",
            "ActivityNavigationBindingKt"
        )
        val fragment = JavaClassName.get(
            "androidx.fragment.app",
            "Fragment"
        )
        val fragmentNavigationBindingKt = JavaClassName.get(
            "dev.enro.core.fragment",
            "FragmentNavigationBindingKt"
        )

        val syntheticDestination = JavaClassName.get(
            "dev.enro.core.synthetic",
            "SyntheticDestination"
        )
        val syntheticNavigationBindingKt = JavaClassName.get(
            "dev.enro.core.synthetic",
            "SyntheticNavigationBindingKt"
        )

        val composableDestination = JavaClassName.get(
            "dev.enro.core.compose",
            "ComposableDestination"
        )
        val composeNavigationBindingKt = JavaClassName.get(
            "dev.enro.core.compose",
            "ComposableNavigationBindingKt"
        )
    }

    object Kotlin {
        val unit = ClassName(
            "kotlin",
            "Unit"
        )
        val navigationModuleScope = ClassName(
            "dev.enro.core.controller",
            "NavigationModuleScope"
        )
        val navigationDestination = ClassName("dev.enro.annotations", "NavigationDestination")
        val navigationComponent = ClassName("dev.enro.annotations", "NavigationComponent")
        val generatedNavigationBinding = ClassName("dev.enro.annotations", "GeneratedNavigationBinding")
        val generatedNavigationModule = ClassName("dev.enro.annotations", "GeneratedNavigationModule")

        val legacyDialogDestination = ClassName("dev.enro.core.compose.dialog","DialogDestination")
        val legacyBottomSheetDestination = ClassName("dev.enro.core.compose.dialog","BottomSheetDestination")

        val optIn = ClassName("kotlin", "OptIn")
        val experimentalMaterialApi = ClassName("androidx.compose.material", "ExperimentalMaterialApi")
    }
}