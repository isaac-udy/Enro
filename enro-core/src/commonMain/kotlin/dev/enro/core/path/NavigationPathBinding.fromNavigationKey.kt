package dev.enro.core.path

import dev.enro.core.NavigationKey
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

@PublishedApi
internal inline fun <reified P> checkParameterIsSupported(
    property: KProperty1<*, P>,
    elements: Set<String>,
    nullableElements: Set<String>,
) {
    val isSupportedType = when (P::class) {
        String::class -> true
        Int::class -> true
        Long::class -> true
        Float::class -> true
        Double::class -> true
        Short::class -> true
        Byte::class -> true
        Char::class -> true
        Boolean::class -> true
        else -> false
    }
    require(isSupportedType) {
        "Property ${property.name} of type ${P::class} is not supported as a path parameter. Must be a primitive."
    }
    if (typeOf<P>().isMarkedNullable) {
        return require(nullableElements.contains(property.name)) {
            "Property ${property.name} of type ${P::class} is nullable, but the path parameter ${property.name} is not marked as optional."
        }
    }
    return require(elements.contains(property.name)) {
        "Property ${property.name} was not found in the path pattern."
    }
}

public inline fun <reified P, T : NavigationKey> PathData.Builder.set(
    navigationKey: T,
    property: KProperty1<T, P>,
) {
    val stringValue = property.get(navigationKey)?.toString() ?: return
    set(property.name, stringValue)
}

public inline fun <reified P> PathData.get(
    property: KProperty1<*, P>,
): P {
    val stringValue = optional(property.name)
    if (stringValue == null) {
        val isNullable = typeOf<P>().isMarkedNullable
        if (isNullable) return null as P
        else error("Property ${property.name} is not nullable, but no value was found")
    }
    return when (P::class) {
        String::class -> stringValue as P
        Int::class -> stringValue.toInt() as P
        Long::class -> stringValue.toLong() as P
        Float::class -> stringValue.toFloat() as P
        Double::class -> stringValue.toDouble() as P
        Short::class -> stringValue.toShort() as P
        Byte::class -> stringValue.toByte() as P
        Char::class -> stringValue.first() as P
        Boolean::class -> stringValue.toBoolean() as P
        else -> error("Type ${P::class} is not supported")
    }
}


public inline fun <reified T : NavigationKey> NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    crossinline constructor: () -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    require(parameterNames.size == 0) {
        "Path pattern must not have any parameters, but found ${parameterNames.size}"
    }

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pattern,
        deserialize = { constructor() },
        serialize = { }
    )
}

public inline fun <
        reified P1,
        reified T : NavigationKey
        > NavigationPathBinding.Companion.createPathBinding(

    pattern: String,
    propertyOne: KProperty1<T, P1>,
    crossinline constructor: (P1) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 1) {
        "Path pattern must have exactly one parameter, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
        }
    )
}

public inline fun <reified P1, reified P2, reified T : NavigationKey>
        NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    propertyOne: KProperty1<T, P1>,
    propertyTwo: KProperty1<T, P2>,
    crossinline constructor: (P1, P2) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 2) {
        "Path pattern must have exactly two parameters, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)
    checkParameterIsSupported<P2>(propertyTwo, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne),
                it.get(propertyTwo)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
            propertyTwo.get(it)?.let {
                set(propertyTwo.name, it.toString())
            }
        }
    )
}

public inline fun <reified P1, reified P2, reified P3, reified T : NavigationKey>
        NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    propertyOne: KProperty1<T, P1>,
    propertyTwo: KProperty1<T, P2>,
    propertyThree: KProperty1<T, P3>,
    crossinline constructor: (P1, P2, P3) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 3) {
        "Path pattern must have exactly three parameters, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)
    checkParameterIsSupported<P2>(propertyTwo, parameterNames, nullableParameters)
    checkParameterIsSupported<P3>(propertyThree, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne),
                it.get(propertyTwo),
                it.get(propertyThree)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
            propertyTwo.get(it)?.let {
                set(propertyTwo.name, it.toString())
            }
            propertyThree.get(it)?.let {
                set(propertyThree.name, it.toString())
            }
        }
    )
}

public inline fun <reified P1, reified P2, reified P3, reified P4, reified T : NavigationKey>
        NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    propertyOne: KProperty1<T, P1>,
    propertyTwo: KProperty1<T, P2>,
    propertyThree: KProperty1<T, P3>,
    propertyFour: KProperty1<T, P4>,
    crossinline constructor: (P1, P2, P3, P4) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 4) {
        "Path pattern must have exactly four parameters, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)
    checkParameterIsSupported<P2>(propertyTwo, parameterNames, nullableParameters)
    checkParameterIsSupported<P3>(propertyThree, parameterNames, nullableParameters)
    checkParameterIsSupported<P4>(propertyFour, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne),
                it.get(propertyTwo),
                it.get(propertyThree),
                it.get(propertyFour)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
            propertyTwo.get(it)?.let {
                set(propertyTwo.name, it.toString())
            }
            propertyThree.get(it)?.let {
                set(propertyThree.name, it.toString())
            }
            propertyFour.get(it)?.let {
                set(propertyFour.name, it.toString())
            }
        }
    )
}

public inline fun <reified P1, reified P2, reified P3, reified P4, reified P5, reified T : NavigationKey>
        NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    propertyOne: KProperty1<T, P1>,
    propertyTwo: KProperty1<T, P2>,
    propertyThree: KProperty1<T, P3>,
    propertyFour: KProperty1<T, P4>,
    propertyFive: KProperty1<T, P5>,
    crossinline constructor: (P1, P2, P3, P4, P5) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 5) {
        "Path pattern must have exactly five parameters, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)
    checkParameterIsSupported<P2>(propertyTwo, parameterNames, nullableParameters)
    checkParameterIsSupported<P3>(propertyThree, parameterNames, nullableParameters)
    checkParameterIsSupported<P4>(propertyFour, parameterNames, nullableParameters)
    checkParameterIsSupported<P5>(propertyFive, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne),
                it.get(propertyTwo),
                it.get(propertyThree),
                it.get(propertyFour),
                it.get(propertyFive)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
            propertyTwo.get(it)?.let {
                set(propertyTwo.name, it.toString())
            }
            propertyThree.get(it)?.let {
                set(propertyThree.name, it.toString())
            }
            propertyFour.get(it)?.let {
                set(propertyFour.name, it.toString())
            }
            propertyFive.get(it)?.let {
                set(propertyFive.name, it.toString())
            }
        }
    )
}


public inline fun <reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified T : NavigationKey>
        NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    propertyOne: KProperty1<T, P1>,
    propertyTwo: KProperty1<T, P2>,
    propertyThree: KProperty1<T, P3>,
    propertyFour: KProperty1<T, P4>,
    propertyFive: KProperty1<T, P5>,
    propertySix: KProperty1<T, P6>,
    crossinline constructor: (P1, P2, P3, P4, P5, P6) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 6) {
        "Path pattern must have exactly six parameters, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)
    checkParameterIsSupported<P2>(propertyTwo, parameterNames, nullableParameters)
    checkParameterIsSupported<P3>(propertyThree, parameterNames, nullableParameters)
    checkParameterIsSupported<P4>(propertyFour, parameterNames, nullableParameters)
    checkParameterIsSupported<P5>(propertyFive, parameterNames, nullableParameters)
    checkParameterIsSupported<P6>(propertySix, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne),
                it.get(propertyTwo),
                it.get(propertyThree),
                it.get(propertyFour),
                it.get(propertyFive),
                it.get(propertySix)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
            propertyTwo.get(it)?.let {
                set(propertyTwo.name, it.toString())
            }
            propertyThree.get(it)?.let {
                set(propertyThree.name, it.toString())
            }
            propertyFour.get(it)?.let {
                set(propertyFour.name, it.toString())
            }
            propertyFive.get(it)?.let {
                set(propertyFive.name, it.toString())
            }
            propertySix.get(it)?.let {
                set(propertySix.name, it.toString())
            }
        }
    )
}

public inline fun <reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified T : NavigationKey>
        NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    propertyOne: KProperty1<T, P1>,
    propertyTwo: KProperty1<T, P2>,
    propertyThree: KProperty1<T, P3>,
    propertyFour: KProperty1<T, P4>,
    propertyFive: KProperty1<T, P5>,
    propertySix: KProperty1<T, P6>,
    propertySeven: KProperty1<T, P7>,
    crossinline constructor: (P1, P2, P3, P4, P5, P6, P7) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 7) {
        "Path pattern must have exactly seven parameters, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)
    checkParameterIsSupported<P2>(propertyTwo, parameterNames, nullableParameters)
    checkParameterIsSupported<P3>(propertyThree, parameterNames, nullableParameters)
    checkParameterIsSupported<P4>(propertyFour, parameterNames, nullableParameters)
    checkParameterIsSupported<P5>(propertyFive, parameterNames, nullableParameters)
    checkParameterIsSupported<P6>(propertySix, parameterNames, nullableParameters)
    checkParameterIsSupported<P7>(propertySeven, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne),
                it.get(propertyTwo),
                it.get(propertyThree),
                it.get(propertyFour),
                it.get(propertyFive),
                it.get(propertySix),
                it.get(propertySeven)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
            propertyTwo.get(it)?.let {
                set(propertyTwo.name, it.toString())
            }
            propertyThree.get(it)?.let {
                set(propertyThree.name, it.toString())
            }
            propertyFour.get(it)?.let {
                set(propertyFour.name, it.toString())
            }
            propertyFive.get(it)?.let {
                set(propertyFive.name, it.toString())
            }
            propertySix.get(it)?.let {
                set(propertySix.name, it.toString())
            }
            propertySeven.get(it)?.let {
                set(propertySeven.name, it.toString())
            }
        }
    )
}

public inline fun <reified P1, reified P2, reified P3, reified P4, reified P5, reified P6, reified P7, reified P8, reified T : NavigationKey>
        NavigationPathBinding.Companion.createPathBinding(
    pattern: String,
    propertyOne: KProperty1<T, P1>,
    propertyTwo: KProperty1<T, P2>,
    propertyThree: KProperty1<T, P3>,
    propertyFour: KProperty1<T, P4>,
    propertyFive: KProperty1<T, P5>,
    propertySix: KProperty1<T, P6>,
    propertySeven: KProperty1<T, P7>,
    propertyEight: KProperty1<T, P8>,
    crossinline constructor: (P1, P2, P3, P4, P5, P6, P7, P8) -> T,
): NavigationPathBinding<T> {
    val pathPattern = PathPattern.fromString(pattern)

    val parameterNames = pathPattern.pathElements
        .filterIsInstance<PathPattern.PathElement.PathParam>()
        .map { it.name }
        .plus(pathPattern.queryElements.map { it.paramName })
        .toSet()

    val nullableParameters = pathPattern.queryElements
        .filterIsInstance<PathPattern.QueryElement.OptionalQueryParam>()
        .map { it.paramName }
        .toSet()

    require(parameterNames.size == 8) {
        "Path pattern must have exactly eight parameters, but found ${parameterNames.size}"
    }

    checkParameterIsSupported<P1>(propertyOne, parameterNames, nullableParameters)
    checkParameterIsSupported<P2>(propertyTwo, parameterNames, nullableParameters)
    checkParameterIsSupported<P3>(propertyThree, parameterNames, nullableParameters)
    checkParameterIsSupported<P4>(propertyFour, parameterNames, nullableParameters)
    checkParameterIsSupported<P5>(propertyFive, parameterNames, nullableParameters)
    checkParameterIsSupported<P6>(propertySix, parameterNames, nullableParameters)
    checkParameterIsSupported<P7>(propertySeven, parameterNames, nullableParameters)
    checkParameterIsSupported<P8>(propertyEight, parameterNames, nullableParameters)

    return NavigationPathBinding(
        keyType = T::class,
        pattern = pathPattern,
        deserialize = {
            constructor(
                it.get(propertyOne),
                it.get(propertyTwo),
                it.get(propertyThree),
                it.get(propertyFour),
                it.get(propertyFive),
                it.get(propertySix),
                it.get(propertySeven),
                it.get(propertyEight)
            )
        },
        serialize = {
            propertyOne.get(it)?.let {
                set(propertyOne.name, it.toString())
            }
            propertyTwo.get(it)?.let {
                set(propertyTwo.name, it.toString())
            }
            propertyThree.get(it)?.let {
                set(propertyThree.name, it.toString())
            }
            propertyFour.get(it)?.let {
                set(propertyFour.name, it.toString())
            }
            propertyFive.get(it)?.let {
                set(propertyFive.name, it.toString())
            }
            propertySix.get(it)?.let {
                set(propertySix.name, it.toString())
            }
            propertySeven.get(it)?.let {
                set(propertySeven.name, it.toString())
            }
            propertyEight.get(it)?.let {
                set(propertyEight.name, it.toString())
            }
        }
    )
}
