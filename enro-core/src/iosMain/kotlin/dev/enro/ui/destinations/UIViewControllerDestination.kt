package dev.enro.ui.destinations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import platform.UIKit.UIModalPresentationAutomatic
import platform.UIKit.UIModalPresentationStyle
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import kotlin.reflect.KClass

public object UIViewControllerDestination {

    // Represents the configuration for a UIViewControllerDestination, where the list of
    // Configuration.Flags represent what kinds of presentation the UIViewControllerDestination
    // should support; the order of the flags is important, as this indicates the order of
    // preference for presentation styles; for example, if "SupportsCompose" is first,
    // then the UIViewController will prefer being hosted within Compose, and if this is
    // possible, will use that presentation format, but if "SupportsPresent" is first,
    // the UIViewController will prefer being presented as a modal.
    // These flags are provided inside a NavigationDestination.MetadataBuilder<T>, which allows
    // different flags to be returned, depending on the NavigationKey.Instance used for the destination.
    // For example, a NavigationKey could have "present: Boolean" as a property, causing the
    // associated UIViewControllerDestination to return "SupportsPresent" as the first flag.
    public class Configuration(
        internal val flags: List<Flag>,
        internal val constructor: (NavigationKey.Instance<NavigationKey>) -> UIViewController
    ) {
        public sealed interface Flag
    }

    // Whether the UIViewController should be able to be hosted inside
    // Compose (i.e. within an Enro NavigationContainer)
    public object SupportsCompose : Configuration.Flag

    // Whether the UIViewController is able to be presented from another
    // UIViewController, will use the "style" presentation style if presented
    public class SupportsPresent(
        public val style: UIModalPresentationStyle = UIModalPresentationAutomatic,
    ) : Configuration.Flag

    // Whether the UIViewController supports being pushed into a UINavigationView
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
    return navigationDestination(
        metadata = {
            val flags = metadata().filter {
                // If the instance has been set specifically to ignore Compose hosting,
                // we're going to filter out any SupportsCompose flags
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