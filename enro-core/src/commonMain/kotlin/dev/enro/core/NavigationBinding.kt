package dev.enro.core

import kotlin.reflect.KClass

public abstract class NavigationBinding<KeyType : NavigationKey, ContextType : Any> {
    public abstract val keyType: KClass<KeyType>
    public abstract val keySerializer: NavigationKeySerializer<KeyType>
    public abstract val destinationType: KClass<ContextType>
    public abstract val baseType: KClass<in ContextType>

    internal var isPlatformOverride: Boolean = false
}