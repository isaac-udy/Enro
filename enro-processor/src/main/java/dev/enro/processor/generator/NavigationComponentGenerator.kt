package dev.enro.processor.generator

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.annotations.GeneratedNavigationComponent
import dev.enro.processor.domain.GeneratedBindingReference
import dev.enro.processor.domain.GeneratedModuleReference
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.getElementName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import com.squareup.javapoet.AnnotationSpec as JavaAnnotationSpec
import com.squareup.javapoet.ClassName as JavaClassName
import com.squareup.javapoet.CodeBlock as JavaCodeBlock
import com.squareup.javapoet.MethodSpec as JavaMethodSpec
import com.squareup.javapoet.ParameterSpec as JavaParameterSpec
import com.squareup.javapoet.TypeSpec as JavaTypeSpec

object NavigationComponentGenerator {
    fun generateKotlin(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        declaration: KSDeclaration,
        resolverBindings: List<KSDeclaration>,
        resolverModules: List<KSDeclaration>,
    ) {
        val modules = GeneratedModuleReference.load(resolver)
        val bindings = modules.flatMap { it.bindings }

        val moduleNames = modules.joinToString(separator = ",\n") {
            "${it.qualifiedName}::class"
        }
        val bindingNames = bindings.joinToString(separator = ",\n") {
            "${it.binding}::class"
        }

        val generatedName = "${declaration.simpleName.asString()}Navigation"
        val generatedComponent = TypeSpec.classBuilder(generatedName)
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationComponent::class.java)
                    .addMember("bindings = [\n$bindingNames\n]")
                    .addMember("modules = [\n$moduleNames\n]")
                    .build()
            )
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(
                ClassName("kotlin", "Function1")
                    .parameterizedBy(
                        ClassNames.Kotlin.navigationModuleScope,
                        ClassNames.Kotlin.unit
                    )
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .returns(Unit::class.java)
                    .addParameter(
                        ParameterSpec
                            .builder(
                                "navigationModuleScope",
                                ClassNames.Kotlin.navigationModuleScope,
                            )
                            .build()
                    )
                    .apply {
                        bindings.forEach {
                            addStatement(
                                "%T().invoke(navigationModuleScope)",
                                ClassName(EnroLocation.GENERATED_PACKAGE, it.binding.split(".").last())
                            )
                        }
                    }
                    .build()
            )
            .build()

        FileSpec
            .builder(
                declaration.packageName.asString(),
                requireNotNull(generatedComponent.name)
            )
            .addType(generatedComponent)
            .build()
            .writeTo(
                codeGenerator = environment.codeGenerator,
                dependencies = Dependencies(
                    aggregating = true,
                    sources = (resolverModules + resolverBindings).mapNotNull { it.containingFile }
                        .plus(listOfNotNull(declaration.containingFile))
                        .toTypedArray()
                )
            )

        environment.codeGenerator
            .associateWithClasses(
                classes = modules.map { it.declaration },
                packageName = declaration.packageName.asString(),
                fileName = requireNotNull(generatedComponent.name),
            )
    }

    fun generateJava(
        processingEnv: ProcessingEnvironment,
        component: Element,
        generatedModuleName: String?,
        generatedModuleBindings: List<Element>
    ) {
        val modules = GeneratedModuleReference.load(processingEnv)
        val bindings = modules.flatMap { it.bindings }
            .plus(
                generatedModuleBindings.map {
                    val annotation = it.getAnnotation(GeneratedNavigationBinding::class.java)
                    GeneratedBindingReference.Java(
                        binding = it.getElementName(processingEnv),
                        destination = annotation.destination,
                        navigationKey = annotation.navigationKey
                    )
                }
            )

        val moduleNames = modules
            .map { "${it.qualifiedName}.class" }
            .let {
                if(generatedModuleName != null) {
                    it + "$generatedModuleName.class"
                } else it
            }
            .joinToString(separator = ",\n")

        val bindingNames = bindings.joinToString(separator = ",\n") { "${it.binding}.class" }

        val generatedName = "${component.simpleName}Navigation"
        val classBuilder = JavaTypeSpec.classBuilder(generatedName)
            .addOriginatingElement(component)
            .addOriginatingElement(
                processingEnv.elementUtils
                    .getPackageElement(EnroLocation.GENERATED_PACKAGE)
            )
            .apply {
                modules.forEach {
                    addOriginatingElement(it.element)
                }
            }
            .addAnnotation(
                JavaAnnotationSpec.builder(GeneratedNavigationComponent::class.java)
                    .addMember("bindings", "{\n$bindingNames\n}")
                    .addMember("modules", "{\n$moduleNames\n}")
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassNames.Java.kotlinFunctionOne,
                    ClassNames.Java.navigationModuleScope,
                    JavaClassName.get(Unit::class.java)
                )
            )
            .addMethod(
                JavaMethodSpec.methodBuilder("invoke")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(Unit::class.java)
                    .addParameter(
                        JavaParameterSpec
                            .builder(ClassNames.Java.navigationModuleScope, "navigationModuleScope")
                            .build()
                    )
                    .apply {
                        bindings.forEach {
                            addStatement(
                                JavaCodeBlock.of(
                                    "new $1T().invoke(navigationModuleScope)",
                                    JavaClassName.get(
                                        EnroLocation.GENERATED_PACKAGE,
                                        it.binding.split(".").last()
                                    )
                                )
                            )
                        }
                        addStatement(JavaCodeBlock.of("return kotlin.Unit.INSTANCE"))
                    }
                    .build()
            )
            .build()

        JavaFile
            .builder(
                processingEnv.elementUtils.getPackageOf(component).toString(),
                classBuilder
            )
            .build()
            .writeTo(processingEnv.filer)
    }
}

