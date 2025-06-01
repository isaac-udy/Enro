package dev.enro3

import dev.enro3.ui.NavigationDestinationProvider
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

public class NavigationBinding<K : NavigationKey> @PublishedApi internal constructor(
    public val keyType: KClass<K>,
    public val serializerModule: SerializersModuleBuilder.() -> Unit,
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

        public inline fun <reified K : NavigationKey> create(
            provider: NavigationDestinationProvider<K>,
            isPlatformOverride: Boolean = false,
        ): NavigationBinding<K> {
            return NavigationBinding(
                keyType = K::class,
                serializerModule = {
                    contextual(K::class, serializer<K>())
                    polymorphic(Any::class) {
                        subclass(K::class, serializer<K>())
                    }
                    polymorphic(NavigationKey::class) {
                        subclass(K::class, serializer<K>())
                    }
                },
                provider = provider,
                isPlatformOverride = isPlatformOverride,
            )
        }
    }
}
