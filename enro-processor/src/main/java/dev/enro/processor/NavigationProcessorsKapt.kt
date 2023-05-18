package dev.enro.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.processor.generator.NavigationComponentGenerator
import dev.enro.processor.generator.NavigationDestinationGenerator
import dev.enro.processor.generator.NavigationModuleGenerator
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class NavigationDestinationProcessorKapt : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            NavigationDestination::class.java.name
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        roundEnv.getElementsAnnotatedWith(NavigationDestination::class.java)
            .forEach { element ->
                NavigationDestinationGenerator.generateJava(processingEnv, element)
            }
        return false
    }
}

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@AutoService(Processor::class)
class NavigationComponentProcessorKapt : AbstractProcessor() {

    private val components = mutableListOf<Element>()
    private val bindings = mutableListOf<Element>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            NavigationComponent::class.java.name,
            GeneratedNavigationBinding::class.java.name
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        components += roundEnv.getElementsAnnotatedWith(NavigationComponent::class.java)
        bindings += roundEnv.getElementsAnnotatedWith(GeneratedNavigationBinding::class.java)
        if (roundEnv.processingOver()) {
            val generatedModule = NavigationModuleGenerator.generateJava(
                processingEnv = processingEnv,
                bindings = bindings
            )
            components.forEach {
                NavigationComponentGenerator.generateJava(
                    processingEnv = processingEnv,
                    component = it,
                    generatedModuleName = generatedModule,
                    generatedModuleBindings = bindings
                )
            }
        }
        return true
    }
}