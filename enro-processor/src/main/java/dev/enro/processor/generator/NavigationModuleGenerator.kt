package dev.enro.processor.generator

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationModule
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.getElementName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import com.squareup.javapoet.AnnotationSpec as JavaAnnotationSpec
import com.squareup.javapoet.TypeSpec as JavaTypeSpec

object NavigationModuleGenerator {
    fun generateKotlin(
        environment: SymbolProcessorEnvironment,
        bindings: List<KSDeclaration>
    ) {
        if (bindings.isEmpty()) return
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
                    sources = bindings.mapNotNull { it.containingFile }.toTypedArray()
                )
            )
    }

    fun generateJava(
        processingEnv: ProcessingEnvironment,
        bindings: List<Element>
    ): String? {
        if(bindings.isEmpty()) return null
        val moduleId = bindings
            .map { it.getElementName(processingEnv) }
            .toModuleId()
        val moduleName = getModuleName(moduleId)

        val bindingsClassNames = bindings.map { "${it.simpleName}.class" }
        val bindingsArray = "{\n${bindingsClassNames.joinToString(separator = ",\n")}\n}"
        val generatedModule = JavaTypeSpec.classBuilder(moduleName)
            .apply {
                bindings.forEach {
                    addOriginatingElement(it)
                }
            }
            .addAnnotation(
                JavaAnnotationSpec.builder(GeneratedNavigationModule::class.java)
                    .addMember("bindings", bindingsArray)
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .build()

        JavaFile
            .builder(
                EnroLocation.GENERATED_PACKAGE,
                generatedModule
            )
            .build()
            .writeTo(processingEnv.filer)

        return "${EnroLocation.GENERATED_PACKAGE}.$moduleName"
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