package dev.enro.compiler

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object EnroNames {

    object Compose {
        val composableAnnotation = ClassId(
            FqName("androidx.compose.runtime"),
            Name.identifier("Composable")
        )
    }

    object Runtime {
        val rootPackage = FqName("dev.enro")
        val controllerPackage = rootPackage.child(Name.identifier("controller"))

        val installNavigationController = CallableId(
            packageName = rootPackage,
            callableName = Name.identifier("installNavigationController"),
        )

        val navigationModule =  ClassId(controllerPackage, Name.identifier("NavigationModule"))
        val navigationModuleBuilderScope = navigationModule.createNestedClassId(Name.identifier("BuilderScope"))

        object NavigationModuleBuilderScope {
            val destinationFunction = CallableId(
                classId = navigationModuleBuilderScope,
                callableName = Name.identifier("destination"),
            )
        }

        val internalCreateEnroController = CallableId(
            packageName = controllerPackage,
            callableName = Name.identifier("internalCreateEnroController"),
        )

        val enroController = ClassId(rootPackage, Name.identifier("EnroController"))

        val navigationKey = ClassId(rootPackage, Name.identifier("NavigationKey"))

        object Ui {
            val uiPackage = rootPackage.child(Name.identifier("ui"))
            val navigationDestinationProvider = ClassId(
                uiPackage,
                Name.identifier("NavigationDestinationProvider")
            )

            val navigationDestinationScope = ClassId(
                uiPackage,
                Name.identifier("NavigationDestinationScope")
            )

            val navigationDestination = ClassId(
                uiPackage,
                Name.identifier("NavigationDestination")
            )

            val metadataBuilder = navigationDestination.createNestedClassId(
                Name.identifier("MetadataBuilder")
            )
        }
    }

    object Annotations {
        val annotationsPackage = FqName("dev.enro.annotations")
        val navigationDestination = ClassId(annotationsPackage, Name.identifier("NavigationDestination"))
        val generatedNavigationBinding = ClassId(annotationsPackage, Name.identifier("GeneratedNavigationBinding"))
    }

    object Generated {
        val generatedPackage = FqName.ROOT

        fun bindFunction(
            classId: ClassId,
        ): CallableId {
            return CallableId(
                classId = classId,
                callableName = Name.identifier("bind"),
            )
        }

        val bindingReferenceFunction = CallableId(
            packageName = generatedPackage,
            callableName = Name.identifier("enroGeneratedBindingReference"),
        )
    }

}