package dev.enro.compiler

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object EnroNames {

    object Annotations {
        val annotationsPackage = FqName("dev.enro.annotations")
        val navigationDestination = ClassId(annotationsPackage, Name.identifier("NavigationDestination"))
        val generatedNavigationBinding = ClassId(annotationsPackage, Name.identifier("GeneratedNavigationBinding"))
    }

    object Generated {
        val generatedPackage = FqName("enro_generated_bindings")

        val bindFunction = CallableId(
            packageName = generatedPackage,
            callableName = Name.identifier("bind"),
        )
    }

}