package dev.enro.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.processor.domain.ComponentReference
import dev.enro.processor.domain.GeneratedBindingReference
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.generator.NavigationBindingGenerator
import dev.enro.processor.generator.NavigationComponentGenerator
import dev.enro.processor.generator.ResolverPlatform

class NavigationProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val processedDestinations = mutableSetOf<String>()

    private var platform: ResolverPlatform? = null

    private val componentsToProcess = mutableMapOf<String, ComponentReference>()
    private val generatedBindings = mutableMapOf<String, GeneratedBindingReference>()

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (platform == null) {
            // If platform is null, that means this is the first time we've run the processor,
            // so we're going to load the platform information, and then we're also going to load all of the
            // GeneratedNavigationBindings from the EnroLocation.GENERATED_PACKAGE package,
            // and put them into the "bindings" map so they can be referenced in the "finish" function
            platform = ResolverPlatform.getPlatform(resolver)
            resolver.getDeclarationsFromPackage(EnroLocation.GENERATED_PACKAGE)
                .filterIsInstance<KSClassDeclaration>()
                .filter { declaration ->
                    declaration.isAnnotationPresent(GeneratedNavigationBinding::class)
                }
                .toList()
                .map { declaration ->
                    GeneratedBindingReference.fromDeclaration(declaration).let { binding ->
                        generatedBindings[binding.qualifiedName] = binding
                    }
                }
        }

        // Whenever we see a new class annotated with GeneratedNavigationBinding, we're also going to add this
        // to the bindings map, so that it can be referenced in the "finish" function
        resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.generatedNavigationBinding.canonicalName)
            .toList()
            .filterIsInstance<KSClassDeclaration>()
            .onEach { declaration ->
                GeneratedBindingReference.fromDeclaration(declaration).let { binding ->
                    generatedBindings[binding.qualifiedName] = binding
                }
            }

        // Whenever we see a class annotated with NavigationComponent, we're going to add that to the
        // processedComponents. The processedComponents list is used to generate the GeneratedNavigationComponent
        // classes in the "finish" function.
        resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.navigationComponent.canonicalName)
            .toList()
            .filterIsInstance<KSDeclaration>()
            .onEach {
                val name = it.qualifiedName?.asString()
                if (name == null) {
                    val error = "Failed to process class ${it.simpleName} annotated with NavigationComponent because it does not have a qualified name."
                    environment.logger.error(error, it)
                    error(error)
                }
                componentsToProcess[name] = ComponentReference.fromDeclaration(environment, it)
            }

        // Whenever we see a class, function or property annotated with NavigationDestination, we're going to grab tha
        // declaration and check if a GeneratedNavigationBinding has been created for that NavigationDestination yet
        // (if it's qualified name is in processedNavigationDestinations, it's been processed already), and if it has not
        // been generated yet, we'll use NavigationBindingGenerator to create a GeneratedNavigationBinding for the declaration
        resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.navigationDestination.canonicalName)
            .toList()
            .filterIsInstance<KSDeclaration>()
            .plus(
                resolver
                    .getSymbolsWithAnnotation(ClassNames.Kotlin.navigationDestinationPlatformOverride.canonicalName)
                    .filterIsInstance<KSDeclaration>()
            )
            .filter { !processedDestinations.contains(it.qualifiedName?.asString()) }
            .onEach { destinationDeclaration ->
                processedDestinations.add(destinationDeclaration.qualifiedName?.asString().orEmpty())
                NavigationBindingGenerator.generate(
                    environment = environment,
                    resolver = resolver,
                    destinationDeclaration = destinationDeclaration
                )
            }

        return emptyList()
    }

    override fun finish() {
        // After we've finished all rounds of processing for this module, we're going to process the NavigationComponent
        // objects that we found and stored in componentsToProcess. We always need to do this as the final step in
        // "finish" because the GeneratedNavigationComponent needs to reference all of the GeneratedNavigationBindings
        componentsToProcess.values.forEach {
            NavigationComponentGenerator.generate(
                environment = environment,
                platform = requireNotNull(platform),
                component = it,
                bindings = generatedBindings.values.toList(),
            )
        }
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