package dev.enro

import dev.enro.serialization.serializerForNavigationKey
import dev.enro.ui.NavigationDestinationProvider
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
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
            val serializer = serializerForNavigationKey<K>()
            return NavigationBinding(
                keyType = K::class,
                serializerModule = {
                    contextual(K::class, serializer)
                    polymorphic(Any::class) {
                        subclass(K::class, serializer)
                    }
                    polymorphic(NavigationKey::class) {
                        subclass(K::class, serializer)
                    }
                },
                provider = provider,
                isPlatformOverride = isPlatformOverride,
            )
        }
    }
}

public fun <T : NavigationKey> T.asCommonDestination(): NavigationKey.WithMetadata<T> {
    return withMetadata(NavigationBinding.Companion.UseOriginalBindingKey, true)
}

public fun <T : NavigationKey> NavigationKey.WithMetadata<T>.asCommonDestination(): NavigationKey.WithMetadata<T> {
    return withMetadata(NavigationBinding.Companion.UseOriginalBindingKey, true)
}

public fun <T : NavigationKey> NavigationKey.Instance<T>.asCommonDestination(): NavigationKey.Instance<T> {
    val commonInstance = NavigationKey.Instance<T>(
        key = this.key,
        id = this.id,
        metadata = this.metadata.copy(),
    )
    NavigationBinding.setUsesOriginalBinding(commonInstance)
    return commonInstance
}
