package dev.enro.processor.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.javapoet.ClassName as JavaClassName


object ClassNames {
    object Java {
        val parcelable = JavaClassName.get(
            "android.os",
            "Parcelable"
        )
        val androidApplication = JavaClassName.get(
            "android.app",
            "Application"
        )
        val enroBackConfiguration = JavaClassName.get(
            "dev.enro.core.controller",
            "EnroBackConfiguration"
        )
        val navigationController = JavaClassName.get(
            "dev.enro.core.controller",
            "NavigationController"
        )
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
            "dev.enro.destination.fragment",
            "FragmentNavigationBindingKt"
        )

        val syntheticDestination = JavaClassName.get(
            "dev.enro.destination.synthetic",
            "SyntheticDestination"
        )
        val syntheticNavigationBindingKt = JavaClassName.get(
            "dev.enro.destination.synthetic",
            "SyntheticNavigationBinding_androidKt"
        )

        val managedFlowNavigationBindingKt = JavaClassName.get(
            "dev.enro.destination.flow",
            "ManagedFlowNavigationBinding_androidKt"
        )

        val composableDestination = JavaClassName.get(
            "dev.enro.destination.compose",
            "ComposableDestination"
        )
        val composeNavigationBindingKt = JavaClassName.get(
            "dev.enro.destination.compose",
            "ComposableNavigationBinding_androidKt"
        )
    }

    object Kotlin {
        val composable = ClassName(
            "androidx.compose.runtime",
            "Composable"
        )
        val enroBackConfiguration = ClassName(
            "dev.enro.core.controller",
            "EnroBackConfiguration"
        )
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

        val experimentalObjCName = ClassName("kotlin.experimental", "ExperimentalObjCName")
        val objCName = ClassName("kotlin.native", "ObjCName")

        val navigationController = ClassName(
            "dev.enro.core.controller",
            "NavigationController"
        )

        val navigationInstructionOpen = ClassName(
            "dev.enro.core",
            "NavigationInstruction",
            "Open"
        ).parameterizedBy(STAR)

        val anyOpenInstruction = ClassName(
            "dev.enro.core",
            "AnyOpenInstruction"
        )

        val navigationInstructionOpenPush = ClassName(
            "dev.enro.core",
            "NavigationInstruction",
            "Open"
        ).parameterizedBy(
            ClassName(
                "dev.enro.core",
                "NavigationDirection",
                "Push",
            )
        )

        val navigationInstructionOpenPresent = ClassName(
            "dev.enro.core",
            "NavigationInstruction",
            "Open"
        ).parameterizedBy(
            ClassName(
                "dev.enro.core",
                "NavigationDirection",
                "Present",
            )
        )

        val navigationKey = ClassName(
            "dev.enro.core",
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
            "dev.enro.core",
            "NavigationComponentConfiguration"
        )

        val kotlinxSerializable = ClassName(
            "kotlinx.serialization",
            "Serializable"
        )
    }
}