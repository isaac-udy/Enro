package dev.enro3

import dev.enro3.ui.NavigationDestinationProvider
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

public class NavigationBinding<K : NavigationKey>(
    public val keyType: KClass<K>,
    public val keySerializer: KSerializer<K>,
    public val provider: NavigationDestinationProvider<K>,
    public val isPlatformOverride: Boolean = false,
) {

    public companion object {
        internal object UseOriginalBindingKey : NavigationKey.MetadataKey<Boolean>(default = false)

        internal fun setUsesOriginalBinding(instance: NavigationKey.Instance<*>) {
            instance.metadata.set(UseOriginalBindingKey, true)
        }

        internal fun usesOriginalBinding(instance: NavigationKey.Instance<*>): Boolean {
            return instance.metadata.get(UseOriginalBindingKey)
        }
    }
}
