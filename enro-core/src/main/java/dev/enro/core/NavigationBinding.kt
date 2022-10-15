package dev.enro.core

import kotlin.reflect.KClass

interface NavigationBinding<KeyType : NavigationKey, ContextType : Any> {
    val keyType: KClass<KeyType>
    val destinationType: KClass<ContextType>
}