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
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val processedDestinations = resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.navigationDestination.canonicalName)
            .filterIsInstance<KSDeclaration>()
            .onEach {
                NavigationDestinationGenerator.generateKotlin(
                    environment = environment,
                    resolver = resolver,
                    declaration = it
                )
            }
            .count()

        val components = resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.navigationComponent.canonicalName)
            .filterIsInstance<KSDeclaration>()

        val bindings = resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.generatedNavigationBinding.canonicalName)
            .filterIsInstance<KSDeclaration>()

        if (processedDestinations > 0) return (components + bindings).toList()
        val bindingsToProcess = bindings.toList()

        if (bindingsToProcess.isNotEmpty()) {
            NavigationModuleGenerator.generateKotlin(
                environment = environment,
                bindings = bindingsToProcess
            )
            return components.toList()
        }

        components.forEach {
            NavigationComponentGenerator.generateKotlin(
                environment = environment,
                resolver = resolver,
                declaration = it
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