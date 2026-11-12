package dev.enro.ui.destinations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestinationContent
import platform.UIKit.UIModalPresentationAutomatic
import platform.UIKit.UIModalPresentationStyle
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import kotlin.reflect.KClass

public object UIViewControllerDestination {

    // `flags` lists the presentations that the UIViewController supports, in order of preference.
    // The first flag that can be applied is used: with SupportsCompose first, the UIViewController
    // is hosted in Compose; with SupportsPresent first, the UIViewController is presented.
    // A NavigationDestination.MetadataBuilder<T> returns the flags, so the flags can depend on
    // the NavigationKey.Instance of the destination.
    public class Configuration(
        internal val flags: List<Flag>,
        internal val constructor: (NavigationKey.Instance<NavigationKey>) -> UIViewController
    ) {
        public sealed interface Flag
    }

    // The UIViewController can be hosted in Compose, inside a NavigationContainer.
    public object SupportsCompose : Configuration.Flag

    // The UIViewController can be presented from another UIViewController.
    public class SupportsPresent(
        public val style: UIModalPresentationStyle = UIModalPresentationAutomatic,
    ) : Configuration.Flag

    // The UIViewController can be pushed onto a UINavigationController.
    public object SupportsUINavigationView : Configuration.Flag

    internal const val ConfigurationKey: String = " dev.enro.ui.destinations.UIViewControllerDestination.Configuration"
    internal object IgnoreComposeKey : NavigationKey.MetadataKey<Boolean>(false)

    internal fun getConfiguration(
        controller: EnroController,
        instance: NavigationKey.Instance<NavigationKey>,
    ): Configuration? {
        val binding = controller.bindings.bindingFor(instance)
        val metadata = binding.provider.peekMetadata(instance)
        return metadata[ConfigurationKey] as? Configuration
    }

    internal fun executePresentationAction(
        configuration: Configuration,
        instance: NavigationKey.Instance<NavigationKey>,
        uiViewController: UIViewController,
        uiNavigationController: UINavigationController?
    ) {
        configuration.flags.forEach {
            when(it) {
                is SupportsCompose -> {
                    return
                }
                is SupportsPresent -> {
                    uiViewController.presentViewController(
                        viewControllerToPresent = configuration.constructor(instance),
                        animated = true,
                        completion = null,
                    )
                    return
                }
                is SupportsUINavigationView -> {
                    if (uiNavigationController == null) return@forEach
                    uiNavigationController.pushViewController(
                        viewController = configuration.constructor(instance),
                        animated = true,
                    )
                    return
                }
            }
        }
    }
}

public inline fun <reified T: NavigationKey> uiViewControllerDestination(
    noinline metadata: NavigationDestination.MetadataBuilder<T>.() -> List<UIViewControllerDestination.Configuration.Flag>,
    noinline viewController: (NavigationKey.Instance<T>) -> UIViewController,
) : NavigationDestinationProvider<T> {
    return uiViewControllerDestination(
        keyType = T::class,
        metadata = metadata,
        viewController = viewController,
    )
}

public fun <T: NavigationKey> uiViewControllerDestination(
    keyType: KClass<T>,
    metadata: NavigationDestination.MetadataBuilder<T>.() -> List<UIViewControllerDestination.Configuration.Flag>,
    viewController: (NavigationKey.Instance<T>) -> UIViewController,
) : NavigationDestinationProvider<T> {
    return navigationDestinationContent(
        metadata = {
            val flags = metadata().filter {
                // An instance with IgnoreComposeKey set does not use the SupportsCompose flag.
                if (instance.metadata.get(UIViewControllerDestination.IgnoreComposeKey)) {
                    return@filter it !is UIViewControllerDestination.SupportsCompose
                }
                return@filter true
            }
            val config = UIViewControllerDestination.Configuration(
                flags = flags,
                constructor = { instance ->
                    @Suppress("UNCHECKED_CAST")
                    viewController(instance as NavigationKey.Instance<T>)
                }
            )
            add(UIViewControllerDestination.ConfigurationKey, config)
            if (flags.firstOrNull() != UIViewControllerDestination.SupportsCompose) {
                rootContextDestination()
            }
        }
    ) {
        val config = remember(destinationMetadata) {
            val configuration = destinationMetadata[UIViewControllerDestination.ConfigurationKey] as? UIViewControllerDestination.Configuration
            requireNotNull(configuration) {
                "No UIViewControllerDestination.Configuration found for ${keyType.simpleName}"
            }
            require(configuration.flags.any { it is UIViewControllerDestination.SupportsCompose }) {
                "UIViewControllerDestination for ${keyType.simpleName} does not support being hosted in Compose"
            }
            return@remember configuration
        }
        UIKitViewController(
            modifier = Modifier.fillMaxSize(),
            factory = { config.constructor(navigation.instance) },
        )
    }
}