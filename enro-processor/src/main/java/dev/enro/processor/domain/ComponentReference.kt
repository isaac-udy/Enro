package dev.enro.processor.domain

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName

class ComponentReference private constructor(
    val simpleName: String,
    val className: ClassName,
    val containingFile: KSFile?,
) {

    companion object {
        fun fromDeclaration(
            environment: SymbolProcessorEnvironment,
            declaration: KSDeclaration,
        ): ComponentReference {
            if (declaration !is KSClassDeclaration) {
                val message = "@NavigationComponent can only be applied to objects"
                environment.logger.error(message, declaration)
                error(message)
            }

            val isObject = declaration.classKind == ClassKind.OBJECT
            if (!isObject) {
                val message = "@NavigationComponent can only be applied to objects"
                environment.logger.error(message, declaration)
                error(message)
            }

            val isNavigationComponentConfiguration = declaration
                .getAllSuperTypes()
                .any { it.declaration.qualifiedName?.asString() == "dev.enro.controller.NavigationComponentConfiguration" }

            if (!isNavigationComponentConfiguration) {
                val message = "@NavigationComponent can only be applied to objects that extend " +
                        "NavigationComponentConfiguration"
                environment.logger.error(message, declaration)
                error(message)
            }

            return ComponentReference(
                simpleName = declaration.simpleName.asString(),
                className = ClassName(
                    declaration.packageName.asString(),
                    declaration.simpleName.asString()
                ),
                containingFile = declaration.containingFile,
            )
        }
    }
}