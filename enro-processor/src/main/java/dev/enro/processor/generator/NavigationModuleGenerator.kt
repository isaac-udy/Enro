package dev.enro.processor.generator

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationModule
import dev.enro.processor.extensions.EnroLocation

object NavigationModuleGenerator {
    fun generate(
        environment: SymbolProcessorEnvironment,
        bindings: List<KSDeclaration>,
        destinations: Sequence<KSDeclaration>,
    ) {
        val moduleId = bindings
            .map { requireNotNull(it.qualifiedName).asString() }
            .toModuleId()
        val moduleName = getModuleName(moduleId)

        val bindingClassNames = bindings.map { "${it.simpleName.asString()}::class" }
        val bindingsArray = "[\n${bindingClassNames.joinToString(separator = ",\n") { "\t$it" }}\n]"
        val generatedModule = TypeSpec.classBuilder(moduleName)
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationModule::class.java)
                    .addMember("bindings = $bindingsArray")
                    .build()
            )
            .addModifiers(KModifier.PUBLIC)
            .build()

        FileSpec
            .builder(EnroLocation.GENERATED_PACKAGE, moduleName)
            .addType(generatedModule)
            .build()
            .writeTo(
                codeGenerator = environment.codeGenerator,
                dependencies = Dependencies(
                    aggregating = true,
                    sources = destinations.mapNotNull { it.containingFile }.toList().toTypedArray()
                )
            )
    }

    private fun List<String>.toModuleId(): String {
        return fold(0) { acc, it -> acc + it.hashCode() }
            .toString()
            .replace("-", "")
            .padStart(10, '0')
    }

    private fun getModuleName(moduleId: String): String {
        return "_dev_enro_processor_ModuleSentinel_$moduleId"
    }
}