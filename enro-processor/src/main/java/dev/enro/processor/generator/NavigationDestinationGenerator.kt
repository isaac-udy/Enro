package dev.enro.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.processor.domain.DestinationReference
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.toDisplayString

object NavigationDestinationGenerator {

    @OptIn(KspExperimental::class)
    fun generate(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        declaration: KSDeclaration,
    ) {
        val destination = DestinationReference(resolver, declaration)

        if (destination.isProperty) {
            val propertyClassDeclaration = destination.keyTypeFromPropertyProvider
            if (propertyClassDeclaration == null) {
                environment.logger.error("Cannot find property type for ${declaration.simpleName.asString()}")
                return
            }
            val propertyType = propertyClassDeclaration
            if (!destination.keyType.asStarProjectedType().isAssignableFrom(propertyType)) {
                environment.logger.error(
                    message = "${declaration.simpleName.asString()} is annotated with @NavigationDestination(${destination.keyType.toDisplayString()}::class) but is a NavigationDestinationProvider<${propertyType.toDisplayString()}>",
                    symbol = declaration,
                )
                return
            }
        }

        val typeSpec = TypeSpec.classBuilder(destination.bindingName)
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(
                ClassName("dev.enro.controller", "NavigationModuleAction")
            )
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationBinding::class.java)
                    .addMember(
                        "destination = %L",
                        CodeBlock.of("\"${requireNotNull(declaration.qualifiedName).asString()}\"")
                    )
                    .addMember(
                        "navigationKey = %L",
                        CodeBlock.of("\"${requireNotNull(destination.keyType.qualifiedName).asString()}\"")
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .receiver(ClassName("dev.enro.controller", "NavigationModule.BuilderScope"))
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .returns(Unit::class.java)
                    .addNavigationDestination(
                        environment = environment,
                        destination = destination,
                    )
                    .build()
            )
            .build()

        FileSpec
            .builder(EnroLocation.GENERATED_PACKAGE, requireNotNull(typeSpec.name))
            .addType(typeSpec)
            .addImport(
                declaration.packageName.asString(),
                requireNotNull(declaration.qualifiedName).asString()
                    .removePrefix(declaration.packageName.asString())
            )
            .addImport("dev.enro.controller", "NavigationModule")
            .addImport("dev.enro.ui", "navigationDestination")
            .apply {
                when {
                    destination.isActivity -> addImport("dev.enro.ui.destinations", "activityDestination")
                    destination.isFragment -> addImport("dev.enro.ui.destinations", "fragmentDestination")
                }
            }
            .build()
            .writeTo(
                codeGenerator = environment.codeGenerator,
                dependencies = Dependencies(
                    aggregating = false,
                    sources = arrayOf(requireNotNull(declaration.containingFile)),
                )
            )
    }

    private fun FunSpec.Builder.addNavigationDestination(
        environment: SymbolProcessorEnvironment,
        destination: DestinationReference,
    ): FunSpec.Builder {
        val formatting = LinkedHashMap<String, Any>()
        formatting["keyType"] = destination.keyType.asStarProjectedType().toTypeName()
        formatting["keyName"] = destination.keyType.toClassName()
        
        val destinationName = when {
            destination.isClass -> {
                formatting["destinationType"] = destination.toClassName()
                "%destinationType:T"
            }
            destination.isProperty -> {
                formatting["destinationProperty"] = destination.declaration.simpleName.asString()
                "%destinationProperty:L"
            }
            destination.isFunction -> {
                formatting["destinationFun"] = destination.declaration.simpleName.asString()
                "%destinationFun:L"
            }

            else -> {
                environment.logger.error(
                    "Could not generate NavigationDestination for ${destination.declaration.qualifiedName?.asString()}. " +
                            "This is likely because the destination is not a class or function."
                )
                "INVALID_DESTINATION"
            }
        }
        val platformOverride = when(destination.isPlatformOverride) {
            true -> ", isPlatformOverride = true"
            else -> ""
        }
        when {
            destination.isClass -> when {
                destination.isFragment -> addNamedCode(
                    "destination(fragmentDestination(%keyType:T::class, %destinationType:T::class)$platformOverride)",
                    formatting,
                )
                destination.isActivity -> addNamedCode(
                    "destination(activityDestination(%keyType:T::class, %destinationType:T::class)$platformOverride)",
                    formatting,
                )
                else ->  environment.logger.error(
                    "${destination.declaration.qualifiedName?.asString()} is not a valid enro class destination."
                )
            }
            destination.isProperty -> addNamedCode(
                "destination($destinationName$platformOverride)",
                formatting,
            )
            destination.isComposable -> addNamedCode(
                "destination(navigationDestination<%keyType:T> { $destinationName() }$platformOverride)",
                formatting,
            )
            else -> {
                environment.logger.error(
                    "${destination.declaration.qualifiedName?.asString()} is not a valid enro3 destination."
                )
            }
        }

        return this
    }
}
