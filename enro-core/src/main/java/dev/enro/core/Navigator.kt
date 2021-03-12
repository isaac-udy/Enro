package dev.enro.core

import kotlin.reflect.KClass

interface Navigator<KeyType : NavigationKey, ContextType: Any> {
    val keyType: KClass<KeyType>
    val contextType: KClass<ContextType>
}