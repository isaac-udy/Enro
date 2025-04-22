package dev.enro.processor.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import dev.enro.annotations.NavigationDestination
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.extends
import dev.enro.processor.extensions.getElementName
import dev.enro.processor.extensions.getNameFromKClass
import dev.enro.processor.extensions.implements
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

sealed class DestinationReference {

    @OptIn(KspExperimental::class)
    class Kotlin(
        resolver: Resolver,
        val declaration: KSDeclaration,
    ) {
        val isActivity = declaration is KSClassDeclaration && declaration.getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "androidx.activity.ComponentActivity" }

        val isFragment = declaration is KSClassDeclaration && declaration.getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "androidx.fragment.app.Fragment" }

        val isSyntheticClass = declaration is KSClassDeclaration && declaration.getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "dev.enro.destination.synthetic.SyntheticDestination" }

        val isSyntheticProvider = declaration is KSPropertyDeclaration &&
                declaration.type.resolve().declaration.qualifiedName?.asString() == "dev.enro.destination.synthetic.SyntheticDestinationProvider"

        val isManagedFlowProvider = declaration is KSPropertyDeclaration &&
                declaration.type.resolve().declaration.qualifiedName?.asString() == "dev.enro.destination.flow.ManagedFlowDestinationProvider"

        val isComposable = declaration is KSFunctionDeclaration && declaration.annotations
            .any { it.shortName.asString() == "Composable" }

        val isDesktopWindow = declaration is KSClassDeclaration &&
                declaration.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == "dev.enro.destination.desktop.DesktopWindow" }

        val annotation = declaration.getAnnotationsByType(NavigationDestination::class)
            .firstOrNull()
            ?: error("${declaration.simpleName} is not annotated with @NavigationDestination")

        val keyType =
            requireNotNull(resolver.getClassDeclarationByName(getNameFromKClass { annotation.key }))

        val bindingName = requireNotNull(declaration.qualifiedName).asString()
            .replace(".", "_")
            .let { "_${it}_GeneratedNavigationBinding" }

        fun toClassName() = (declaration as KSClassDeclaration).toClassName()
    }

    class Java(
        processingEnv: ProcessingEnvironment,
        element: Element,
    ) {
        val isActivity = element is TypeElement &&
                element.extends(processingEnv, ClassNames.Java.componentActivity)

        val isFragment = element is TypeElement &&
                element.extends(processingEnv, ClassNames.Java.fragment)

        val isSyntheticClass = element is TypeElement &&
                element.implements(processingEnv, ClassNames.Java.syntheticDestination)

        val isSyntheticProvider = element is ExecutableElement && runCatching {
            val parent = (element.enclosingElement as TypeElement)
            val actualName = element.simpleName.removeSuffix("\$annotations")
            val syntheticElement = parent.enclosedElements
                .filterIsInstance<ExecutableElement>()
                .firstOrNull { actualName == it.simpleName.toString() && it != element }

            val syntheticProviderMirror = processingEnv.elementUtils
                .getTypeElement("dev.enro.destination.synthetic.SyntheticDestinationProvider")
                .asType()
            val erasedSyntheticProvider = processingEnv.typeUtils.erasure(syntheticProviderMirror)
            val erasedReturnType = processingEnv.typeUtils.erasure(syntheticElement!!.returnType)

            syntheticElement.takeIf {
                processingEnv.typeUtils.isSameType(erasedReturnType, erasedSyntheticProvider)
            }
        }.getOrNull() != null

        val isManagedFlowProvider = element is ExecutableElement && runCatching {
            val parent = (element.enclosingElement as TypeElement)
            val actualName = element.simpleName.removeSuffix("\$annotations")
            val managedFlowElement = parent.enclosedElements
                .filterIsInstance<ExecutableElement>()
                .firstOrNull { actualName == it.simpleName.toString() && it != element }

            val managedFlowProviderMirror = processingEnv.elementUtils
                .getTypeElement("dev.enro.destination.flow.ManagedFlowDestinationProvider")
                .asType()
            val erasedManagedFlowProvider = processingEnv.typeUtils.erasure(managedFlowProviderMirror)
            val erasedReturnType = processingEnv.typeUtils.erasure(managedFlowElement!!.returnType)

            managedFlowElement.takeIf {
                processingEnv.typeUtils.isSameType(erasedReturnType, erasedManagedFlowProvider)
            }
        }.getOrNull() != null

        val isComposable = element is ExecutableElement &&
                element.annotationMirrors
                    .firstOrNull {
                        it.annotationType.asElement()
                            .getElementName(processingEnv) == "androidx.compose.runtime.Composable"
                    } != null

        val annotation = element.getAnnotation(NavigationDestination::class.java)

        val keyType =
            processingEnv.elementUtils.getTypeElement(getNameFromKClass { annotation.key })

        val bindingName = element.getElementName(processingEnv)
            .removeSuffix("\$annotations")
            .replace(".", "_")
            .let { "_${it}_GeneratedNavigationBinding" }

        val originalElement = element
        val element = element.enclosingElement.takeIf {
            isSyntheticProvider || isManagedFlowProvider
        } ?: element

        init {
            if (isComposable || isSyntheticProvider || isManagedFlowProvider) {
                val isStatic = element.modifiers.contains(Modifier.STATIC)
                val parentIsObject = element.enclosingElement.enclosedElements
                    .any { it.simpleName.toString() == "INSTANCE" }

                if (!isStatic && !parentIsObject) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Function ${element.getElementName(processingEnv)} is an instance function, which is not allowed."
                    )
                }
            }
        }
    }
}