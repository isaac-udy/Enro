package nav.enro.processor

import com.squareup.javapoet.ClassName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

internal object EnroProcessor {
    const val GENERATED_PACKAGE = "nav.enro.generated"

    val builderActionType = ClassName.get("nav.enro.core.controller", "NavigationComponentBuilderCommand")
    val builderType = ClassName.get("nav.enro.core.controller", "NavigationComponentBuilder")
}

internal fun getNameFromKClass(block: () -> KClass<*>) : String {
    try {
        return block().java.name
    }
    catch (ex: MirroredTypeException) {
        return ClassName.get(ex.typeMirror).toString()
    }
}
