package dev.enro.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationComponent
import dev.enro.processor.domain.GeneratedModuleReference
import dev.enro.processor.extensions.EnroLocation

object NavigationComponentGenerator {
    @OptIn(KspExperimental::class)
    fun generate(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        declaration: KSDeclaration,
        resolverBindings: List<KSDeclaration>,
        resolverModules: List<KSDeclaration>,
    ) {
        if (declaration !is KSClassDeclaration) {
            val message = "@NavigationComponent can only be applied to objects"
            environment.logger.error(message, declaration)
            error(message)
        }

        val isObject = declaration.classKind == ClassKind.OBJECT
        val isNavigationComponentConfiguration = declaration
            .getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "dev.enro.controller.NavigationControllerConfiguration" }

        val isLegacyComponent = declaration
            .getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "dev.enro.core.NavigationComponentConfiguration" }

        if (isLegacyComponent) return

        if (!isObject) {
            val message = "@NavigationComponent can only be applied to objects"
            environment.logger.error(message, declaration)
            error(message)
        }

        if (!isNavigationComponentConfiguration) {
            val message = "@NavigationComponent can only be applied to objects that extend " +
                    "NavigationComponentConfiguration"
            environment.logger.error(message, declaration)
            error(message)
        }

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
                ClassName("dev.enro.controller", "NavigationModuleAction")
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .receiver(ClassName("dev.enro.controller", "NavigationModule.BuilderScope"))
                    .returns(Unit::class.java)
                    .apply {
                        bindings.forEach {
                            addStatement(
                                "%T().apply { invoke() }",
                                ClassName(
                                    EnroLocation.GENERATED_PACKAGE,
                                    it.binding.split(".").last()
                                )
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
            .addImport("dev.enro.controller", "NavigationModule")
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
}