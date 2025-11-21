package dev.enro.compiler

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object EnroNames {


    object Runtime {
        val rootPackage = FqName("dev.enro")
        val controllerPackage = rootPackage.child(Name.identifier("controller"))

        val installNavigationController = CallableId(
            packageName = rootPackage,
            callableName = Name.identifier("installNavigationController"),
        )

        val navigationModule =  ClassId(controllerPackage, Name.identifier("NavigationModule"))
        val navigationModuleBuilderScope = navigationModule.createNestedClassId(Name.identifier("BuilderScope"))

        val internalCreateEnroController = CallableId(
            packageName = controllerPackage,
            callableName = Name.identifier("internalCreateEnroController"),
        )

        val enroController = ClassId(rootPackage, Name.identifier("EnroController"))

        val navigationKey = ClassId(rootPackage, Name.identifier("NavigationKey"))
    }

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