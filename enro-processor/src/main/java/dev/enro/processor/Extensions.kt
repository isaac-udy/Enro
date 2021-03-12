package dev.enro.processor

import com.squareup.javapoet.ClassName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

internal object EnroProcessor {
    const val GENERATED_PACKAGE = "enro_generated_bindings"

}

internal object ClassNames {
    val navigationComponentBuilderCommand = ClassName.get("nav.enro.core.controller", "NavigationComponentBuilderCommand")
    val navigationComponentBuilder = ClassName.get("nav.enro.core.controller", "NavigationComponentBuilder")

    val jvmClassMappings = ClassName.get("kotlin.jvm", "JvmClassMappingKt")
    val unit = ClassName.get("kotlin", "Unit")

    val fragmentActivity = ClassName.get( "androidx.fragment.app", "FragmentActivity")
    val activityNavigatorKt = ClassName.get("nav.enro.core.activity","ActivityNavigatorKt")

    val fragment = ClassName.get("androidx.fragment.app","Fragment")
    val fragmentNavigatorKt = ClassName.get("nav.enro.core.fragment","FragmentNavigatorKt")

    val syntheticDestination = ClassName.get("nav.enro.core.synthetic","SyntheticDestination")
    val syntheticNavigatorKt = ClassName.get("nav.enro.core.synthetic","SyntheticNavigatorKt")
}

internal fun getNameFromKClass(block: () -> KClass<*>) : String {
    try {
        return block().java.name
    }
    catch (ex: MirroredTypeException) {
        return ClassName.get(ex.typeMirror).toString()
    }
}