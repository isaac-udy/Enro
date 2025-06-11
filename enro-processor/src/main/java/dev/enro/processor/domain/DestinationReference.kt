package dev.enro.processor.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import dev.enro.processor.extensions.ClassNames

@OptIn(KspExperimental::class)
class DestinationReference(
    resolver: Resolver,
    val declaration: KSDeclaration,
) {
    val isClass = declaration is KSClassDeclaration
    val isActivity = declaration is KSClassDeclaration && run {
        val type = (declaration as KSClassDeclaration).asStarProjectedType()
        val activityType = resolver.getClassDeclarationByName("android.app.Activity")?.asStarProjectedType()
        if (activityType == null) return@run false
        activityType.isAssignableFrom(type.starProjection())
    }

    val isFragment = declaration is KSClassDeclaration && run {
        val type = (declaration as KSClassDeclaration).asStarProjectedType()
        val fragmentType = resolver.getClassDeclarationByName("androidx.fragment.app.Fragment")?.asStarProjectedType()
        if (fragmentType == null) return@run false
        fragmentType.isAssignableFrom(type.starProjection())
    }

    val isProperty = declaration is KSPropertyDeclaration && run {
        val type = (declaration as KSPropertyDeclaration).type.resolve()
        val providerType =
            resolver.getClassDeclarationByName("dev.enro.ui.NavigationDestinationProvider")!!.asStarProjectedType()
        providerType.isAssignableFrom(type.starProjection())
    }

    val keyTypeFromPropertyProvider: KSType? = run {
        if (!isProperty) return@run null
        val type = (declaration as KSPropertyDeclaration).type.resolve()
        val providerDeclaration = type.declaration as? KSClassDeclaration
        if (providerDeclaration == null) return@run null

        if (providerDeclaration.qualifiedName?.asString() == "dev.enro.ui.NavigationDestinationProvider") {
            return@run type.arguments.firstOrNull()?.type?.resolve()?.starProjection()
        }

        providerDeclaration.superTypes
            .firstOrNull {
                val resolved = it.resolve()
                resolved.declaration.qualifiedName?.asString() == "dev.enro.ui.NavigationDestinationProvider"
            }
            ?.resolve()
            ?.arguments
            ?.firstOrNull()
            ?.type
            ?.resolve()
            ?.starProjection()
    }

    val isFunction = declaration is KSFunctionDeclaration

    val isComposable = declaration is KSFunctionDeclaration && declaration.annotations
        .any { it.shortName.asString() == "Composable" }

    var isPlatformOverride = false
        private set

    val annotation = declaration
        .annotations.firstOrNull {
            val names = listOf("NavigationDestination", "PlatformOverride")
            val isValid = it.shortName.getShortName() in names
            if (!isValid) return@firstOrNull false
            val qualifiedName = it.annotationType.resolve().declaration.qualifiedName?.asString()
            if (qualifiedName == null) return@firstOrNull false
            if (it.shortName.getShortName() == "PlatformOverride") {
                isPlatformOverride = true
            }
            return@firstOrNull qualifiedName in listOf(
                "dev.enro.annotations.NavigationDestination",
                "dev.enro.annotations.NavigationDestination.PlatformOverride"
            )
        }
        ?: error("${declaration.simpleName} is not annotated with @NavigationDestination")

    val keyType: KSClassDeclaration = run {
        requireNotNull(
            annotation.arguments.first { it.name?.asString() == "key" }
                .let { it.value as KSType }
                .declaration as KSClassDeclaration
        )
    }

    val keyIsKotlinSerializable = keyType.annotations
        .any { it.toAnnotationSpec().typeName == ClassNames.Kotlin.kotlinxSerializable }

    val bindingName = requireNotNull(declaration.qualifiedName).asString()
        .replace(".", "_")
        .let { "_${it}_GeneratedNavigationBinding" }

    fun toClassName() = (declaration as KSClassDeclaration).toClassName()
}