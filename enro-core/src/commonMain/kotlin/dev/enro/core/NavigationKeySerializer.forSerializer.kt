package dev.enro.core

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

public inline fun <reified T: NavigationKey> NavigationKeySerializer.Companion.forKotlinSerializer(): NavigationKeySerializer<T> {
    return forKotlinSerializer(T::class)
}

public fun <T : NavigationKey> NavigationKeySerializer.Companion.forKotlinSerializer(
    cls: KClass<T>,
): NavigationKeySerializer<T> {
    return NavigationKeySerializerForKotlinxSerializer(cls)
}

@OptIn(InternalSerializationApi::class)
private class NavigationKeySerializerForKotlinxSerializer<T : NavigationKey>(
    cls: KClass<T>,
) : NavigationKeySerializer<T>(cls) {
    private val kotlinSerializer: kotlinx.serialization.KSerializer<T> = cls.serializer()
    override fun serialize(key: T): String {
        return Json.encodeToString(kotlinSerializer, key)
    }

    override fun deserialize(data: String): T {
        return Json.decodeFromString(kotlinSerializer, data)
    }
}
