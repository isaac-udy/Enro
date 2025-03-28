package dev.enro.core

import kotlin.reflect.KClass

public abstract class NavigationKeySerializer<T : NavigationKey>(
    keyClass: KClass<T>,
) {
    public abstract fun serialize(key: T): String
    public abstract fun deserialize(data: String): T

    init {
        @Suppress("LeakingThis")
        register(keyClass, this)
    }

    public companion object {
        private val serializers: MutableMap<String, NavigationKeySerializer<out NavigationKey>> = mutableMapOf()

        public fun <T : NavigationKey> register(keyClass: KClass<T>, serializer: NavigationKeySerializer<T>) {
            val qualifiedName = keyClass.qualifiedName
                ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

            serializers[qualifiedName] = serializer
        }

        public inline fun <reified T: NavigationKey> register(serializer: NavigationKeySerializer<T>) {
            register(T::class, serializer)
        }

        internal fun serialize(key: NavigationKey): String {
            val qualifiedName = key::class.qualifiedName
                ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

            val serializer = serializers[qualifiedName]

            @Suppress("UNCHECKED_CAST")
            serializer as NavigationKeySerializer<NavigationKey>

            val serialized = serializer.serialize(key)
            return "${key::class.qualifiedName}|$serialized"
        }

        internal fun deserialize(data: String): NavigationKey {
            val (keyType, serialized) = data.split('|', limit = 2)
            val serializer = serializers[keyType] ?: error("No NavigationKeySerializer found for key type $keyType")
            return serializer.deserialize(serialized)
        }
    }
}
