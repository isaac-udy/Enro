package dev.enro.processor

import com.squareup.javapoet.ClassName
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import kotlin.reflect.KClass

internal object EnroProcessor {
    const val GENERATED_PACKAGE = "enro_generated_bindings"

}

internal object ClassNames {
    val kotlinFunctionOne = ClassName.get("kotlin.jvm.functions", "Function1")

    val navigationModuleScope = ClassName.get("dev.enro.core.controller", "NavigationModuleScope")
    val jvmClassMappings = ClassName.get("kotlin.jvm", "JvmClassMappingKt")

    val unit = ClassName.get("kotlin", "Unit")
    val componentActivity = ClassName.get("androidx.activity", "ComponentActivity")
    val activityNavigationBindingKt =
        ClassName.get("dev.enro.core.activity", "ActivityNavigationBindingKt")

    val fragment = ClassName.get("androidx.fragment.app", "Fragment")
    val fragmentNavigationBindingKt =
        ClassName.get("dev.enro.core.fragment", "FragmentNavigationBindingKt")

    val syntheticDestination = ClassName.get("dev.enro.core.synthetic", "SyntheticDestination")
    val syntheticNavigationBindingKt =
        ClassName.get("dev.enro.core.synthetic", "SyntheticNavigationBindingKt")

    val composableDestination = ClassName.get("dev.enro.core.compose", "ComposableDestination")
    val composeNavigationBindingKt =
        ClassName.get("dev.enro.core.compose", "ComposableNavigationBindingKt")
}

internal fun getNameFromKClass(block: () -> KClass<*>) : String {
    try {
        return block().java.name
    }
    catch (ex: MirroredTypeException) {
        return ClassName.get(ex.typeMirror).toString()
    }
}

internal fun getNamesFromKClasses(block: () -> Array<KClass<*>>) : List<String> {
    try {
        return block().map { it.java.name }
    }
    catch (ex: MirroredTypesException) {
        return ex.typeMirrors.map {
            ClassName.get(it).toString()
        }
    }
}