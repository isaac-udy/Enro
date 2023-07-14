package dev.enro.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.generator.NavigationComponentGenerator
import dev.enro.processor.generator.NavigationDestinationGenerator
import dev.enro.processor.generator.NavigationModuleGenerator

class NavigationProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val processed = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val destinations = resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.navigationDestination.canonicalName)
            .filterIsInstance<KSDeclaration>()

        val bindings = resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.generatedNavigationBinding.canonicalName)
            .filterIsInstance<KSDeclaration>()

        val modules = resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.generatedNavigationModule.canonicalName)
            .filterIsInstance<KSDeclaration>()

        val components = resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.navigationComponent.canonicalName)
            .filterIsInstance<KSDeclaration>()

        val processedDestinations = destinations.filter { !processed.contains(it.qualifiedName?.asString()) }
            .onEach {
                processed.add(it.qualifiedName?.asString().orEmpty())
                NavigationDestinationGenerator.generateKotlin(
                    environment = environment,
                    resolver = resolver,
                    declaration = it
                )
            }
            .count()

        if (processedDestinations > 0) return (
            components +
            destinations +
            bindings +
            modules
        ).toList()
        val bindingsToProcess = bindings.toList()

        if (!processed.contains("module")) {
            NavigationModuleGenerator.generateKotlin(
                environment = environment,
                bindings = bindingsToProcess,
                destinations = destinations,
            )
            processed.add("module")
            return (
                components +
                destinations +
                bindings +
                modules
            ).toList()
        }

        components.forEach {
            NavigationComponentGenerator.generateKotlin(
                environment = environment,
                resolver = resolver,
                declaration = it,
                resolverModules = modules.toList(),
                resolverBindings = destinations.toList(),
            )
        }

        return emptyList()
    }
}

@AutoService(SymbolProcessorProvider::class)
class NavigationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return NavigationProcessor(
            environment = environment
        )
    }
}