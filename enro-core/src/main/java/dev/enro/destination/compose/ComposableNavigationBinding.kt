package dev.enro.core.compose

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationKey
import dev.enro.core.compose.dialog.BottomSheetConfiguration
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogConfiguration
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.controller.NavigationModuleScope
import kotlin.reflect.KClass

public class ComposableNavigationBinding<KeyType : NavigationKey, ComposableType : ComposableDestination> @PublishedApi internal constructor(
    override val keyType: KClass<KeyType>,
    override val destinationType: KClass<ComposableType>,
    internal val constructDestination: () -> ComposableType = { destinationType.java.newInstance() }
) : NavigationBinding<KeyType, ComposableType> {
    override val baseType: KClass<in ComposableType> = ComposableDestination::class
}

public fun <KeyType : NavigationKey, ComposableType : ComposableDestination> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    composableType: Class<ComposableType>
): NavigationBinding<KeyType, ComposableType> {
    return ComposableNavigationBinding(
        keyType = keyType.kotlin,
        destinationType = composableType.kotlin
    )
}

@PublishedApi
internal fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: KClass<KeyType>,
    content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    class Destination : ComposableDestination() {
        @Composable
        override fun Render() {
            content()
        }
    }
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = Destination()::class as KClass<ComposableDestination>,
        constructDestination = { Destination() }
    )
}

public inline fun <reified KeyType : NavigationKey> createComposableNavigationBinding(
    noinline content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableNavigationBinding(
        KeyType::class,
        content
    )
}

public fun <KeyType : NavigationKey> createComposableNavigationBinding(
    keyType: Class<KeyType>,
    content: @Composable () -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    val destination = object : ComposableDestination() {
        @Composable
        override fun Render() {
            content()
        }
    }
    return ComposableNavigationBinding(
        keyType = keyType.kotlin,
        destinationType = destination::class
    ) as NavigationBinding<KeyType, ComposableDestination>
}

public inline fun <reified KeyType : NavigationKey, reified ComposableType : ComposableDestination> createComposableNavigationBinding(): NavigationBinding<KeyType, ComposableType> {
    return createComposableNavigationBinding(
        KeyType::class.java,
        ComposableType::class.java
    )
}

public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> NavigationModuleScope.composableDestination() {
    binding(createComposableNavigationBinding<KeyType, DestinationType>())
}

public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.composableDestination(noinline content: @Composable () -> Unit) {
    binding(createComposableNavigationBinding<KeyType>(content))
}

@PublishedApi
internal fun <KeyType : NavigationKey> createComposableDialogNavigationBinding(
    keyType: KClass<KeyType>,
    content: @Composable DialogDestination.() -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    class Destination : ComposableDestination(), DialogDestination {
        override val dialogConfiguration: DialogConfiguration = DialogConfiguration()

        @Composable
        override fun Render() {
            content()
        }
    }
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = Destination()::class as KClass<ComposableDestination>,
        constructDestination = { Destination() }
    )
}

public inline fun <reified KeyType : NavigationKey> createComposableDialogNavigationBinding(
    noinline content: @Composable DialogDestination.() -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableDialogNavigationBinding(
        KeyType::class,
        content
    )
}

@Deprecated("See BottomSheetDestination interface")
public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.legacyComposableDialogDestination(noinline content: @Composable DialogDestination.() -> Unit) {
    binding(createComposableDialogNavigationBinding<KeyType>(content))
}

@OptIn(ExperimentalMaterialApi::class)
@PublishedApi
internal fun <KeyType : NavigationKey> createComposableBottomSheetNavigationBinding(
    keyType: KClass<KeyType>,
    content: @Composable BottomSheetDestination.() -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    class Destination : ComposableDestination(), BottomSheetDestination {
        override val bottomSheetConfiguration: BottomSheetConfiguration = BottomSheetConfiguration()

        @Composable
        override fun Render() {
            content()
        }
    }
    return ComposableNavigationBinding(
        keyType = keyType,
        destinationType = Destination()::class as KClass<ComposableDestination>,
        constructDestination = { Destination() }
    )
}

@OptIn(ExperimentalMaterialApi::class)
public inline fun <reified KeyType : NavigationKey> createComposableBottomSheetNavigationBinding(
    noinline content: @Composable BottomSheetDestination.() -> Unit
): NavigationBinding<KeyType, ComposableDestination> {
    return createComposableBottomSheetNavigationBinding(
        KeyType::class,
        content
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Deprecated("See BottomSheetDestination interface")
public inline fun <reified KeyType : NavigationKey> NavigationModuleScope.legacyComposableBottomSheetDestination(noinline content: @Composable BottomSheetDestination.() -> Unit) {
    binding(createComposableBottomSheetNavigationBinding<KeyType>(content))
}