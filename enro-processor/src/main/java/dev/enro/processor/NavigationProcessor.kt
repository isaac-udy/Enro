package dev.enro.processor

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
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
            platform = ResolverPlatform.getPlatform(resolver)
        }

        resolver.getDeclarationsFromPackage(EnroLocation.GENERATED_PACKAGE)
            .filterIsInstance<KSClassDeclaration>()
            .filter { !generatedBindings.containsKey(it.qualifiedName?.asString()) }
            .filter { it.isAnnotationPresent(GeneratedNavigationBinding::class) }
            .forEach { declaration ->
                GeneratedBindingReference.fromDeclaration(declaration).let { binding ->
                    generatedBindings[binding.qualifiedName] = binding
                }
            }

        // A GeneratedNavigationBinding generated in an earlier round of this module is added
        // to generatedBindings, which "finish" reads.
        resolver
            .getSymbolsWithAnnotation(ClassNames.Kotlin.generatedNavigationBinding.canonicalName)
            .toList()
            .filterIsInstance<KSClassDeclaration>()
            .onEach { declaration ->
                GeneratedBindingReference.fromDeclaration(declaration).let { binding ->
                    generatedBindings[binding.qualifiedName] = binding
                }
            }

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

                // Workaround: on iOS and wasm targets, the "getDeclarationsFromPackage" call above
                // appears to return no declarations unless the module under compilation declares
                // a class in the package. The Sentinel class is that declaration, and causes a
                // second round of "process" in a module that declares a NavigationComponent and
                // no NavigationDestination. Test iOS and wasm targets before removing the Sentinel.
                val typeSpec = TypeSpec.classBuilder("_${name.replace(".", "_")}Sentinel")
                    .addModifiers(KModifier.PRIVATE)
                    .build()

                FileSpec
                    .builder(EnroLocation.GENERATED_PACKAGE, requireNotNull(typeSpec.name))
                    .addType(typeSpec)
                    .build()
                    .writeTo(
                        codeGenerator = environment.codeGenerator,
                        dependencies = Dependencies(
                            aggregating = false,
                            sources = arrayOf(requireNotNull(it.containingFile)),
                        )
                    )
            }

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
        // Components are generated in "finish", after every round of "process", because a
        // GeneratedNavigationComponent references every GeneratedNavigationBinding.
        componentsToProcess.values.forEach {
            NavigationComponentGenerator.generate(
                environment = environment,
                platform = requireNotNull(platform),
                component = it,
                allBindings = generatedBindings.values.toList(),
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