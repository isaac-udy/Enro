package dev.enro

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.library.Architectures.LayeredArchitecture

internal enum class EnroPackage(val packageName: String) {
    // Public packages
    API_PACKAGE("dev.enro.core"),
    PLUGINS_PACKAGE("dev.enro.core.plugins.."),
    CONTAINER_PACKAGE("dev.enro.core.container.."),
    INTERCEPTOR_PACKAGE("dev.enro.core.controller.interceptor.."),
    CONTROLLER_PACKAGE("dev.enro.core.controller"),
    RESULTS_PACKAGE("dev.enro.core.result"),

    // Feature packages
    ACTIVITY_PACKAGE("dev.enro.core.activity.."),
    COMPOSE_PACKAGE("dev.enro.core.compose.."),
    FRAGMENT_PACKAGE("dev.enro.core.fragment.."),
    SYNTHETIC_PACKAGE("dev.enro.core.synthetic.."),
    HOST_PACKAGE("dev.enro.core.hosts.."),
    VIEWMODEL_PACKAGE("dev.enro.viewmodel.."),

    // Implemetation packages
    INTERNAL_PACKAGE("dev.enro.core.internal.."),
    RESULTS_INTERNAL_PACKAGE("dev.enro.core.result.internal.."),
    CONTROLLER_INTERNAL_PACKAGE("dev.enro.core.controller.*.."),

    EXTENSIONS_PACKAGE("dev.enro.extensions.."),
}

internal enum class EnroLayer(
    private val block: (JavaClass) -> Boolean
) {
    PUBLIC({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.API_PACKAGE.packageName).test(it) ||
                JavaClass.Predicates.resideInAnyPackage(EnroPackage.CONTAINER_PACKAGE.packageName).test(it) ||
                JavaClass.Predicates.resideInAnyPackage(EnroPackage.PLUGINS_PACKAGE.packageName).test(it) ||
                JavaClass.Predicates.resideInAnyPackage(EnroPackage.INTERCEPTOR_PACKAGE.packageName).test(it) ||
                JavaClass.Predicates.resideInAnyPackage(EnroPackage.RESULTS_PACKAGE.packageName).test(it)  ||
                JavaClass.Predicates.resideInAnyPackage(EnroPackage.CONTROLLER_PACKAGE.packageName).test(it)
    }),
    ACTIVITY({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.ACTIVITY_PACKAGE.packageName).test(it)
    }),
    FRAGMENT({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.FRAGMENT_PACKAGE.packageName).test(it)
    }),
    COMPOSE({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.COMPOSE_PACKAGE.packageName).test(it)
    }),
    SYNTHETIC({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.SYNTHETIC_PACKAGE.packageName).test(it)
    }),
    HOSTS({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.HOST_PACKAGE.packageName).test(it)
    }),
    VIEW_MODEL({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.VIEWMODEL_PACKAGE.packageName).test(it)
    }),
    EXTENSIONS({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.EXTENSIONS_PACKAGE.packageName).test(it)
    }),
    IMPLEMENTATION({
        JavaClass.Predicates.resideInAnyPackage(EnroPackage.CONTROLLER_INTERNAL_PACKAGE.packageName).test(it) ||
                JavaClass.Predicates.resideInAnyPackage(EnroPackage.RESULTS_INTERNAL_PACKAGE.packageName).test(it) ||
                JavaClass.Predicates.resideInAnyPackage(EnroPackage.INTERNAL_PACKAGE.packageName).test(it)
    });

    val predicate = describe<JavaClass>("is $name layer") {
        block(it)
    }

    companion object {
        val featureLayers = arrayOf(
            ACTIVITY,
            FRAGMENT,
            HOSTS,
            COMPOSE,
            VIEW_MODEL,
            SYNTHETIC,
        )

        val featureLayerDependencies = arrayOf(
            PUBLIC,
            EXTENSIONS,
        )
    }
}


internal fun LayeredArchitecture.layer(enroLayer: EnroLayer): LayeredArchitecture {
    return layer(enroLayer.name).definedBy(enroLayer.predicate)
}

internal fun LayeredArchitecture.whereLayer(enroLayer: EnroLayer): LayeredArchitecture.LayerDependencySpecification {
    return whereLayer(enroLayer.name)
}

internal fun LayeredArchitecture.whereLayers(vararg layers: EnroLayer, block: LayeredArchitecture.LayerDependencySpecification.() -> LayeredArchitecture): LayeredArchitecture {
    return layers.fold(this) { architecture, layer ->
        architecture.whereLayer(layer).run(block)
    }
}

internal fun LayeredArchitecture.LayerDependencySpecification.mayOnlyBeAccessedByLayers(vararg layers: EnroLayer): LayeredArchitecture {
    return mayOnlyBeAccessedByLayers(*(layers.map { it.name }.toTypedArray()))
}

internal fun LayeredArchitecture.LayerDependencySpecification.mayOnlyAccessLayers(vararg layers: EnroLayer): LayeredArchitecture {
    return mayOnlyAccessLayers(*(layers.map { it.name }.toTypedArray()))
}