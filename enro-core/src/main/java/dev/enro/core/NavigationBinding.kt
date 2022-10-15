package dev.enro.core

import kotlin.reflect.KClass

public interface NavigationBinding<KeyType : NavigationKey, ContextType : Any> {
    public val keyType: KClass<KeyType>
    public val destinationType: KClass<ContextType>
}