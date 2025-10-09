package dev.enro.test

import dev.enro.NavigationKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

object NavigationKeyFixtures {
    @Serializable
    data class SimpleKey(
        val keyId: String = Uuid.random().toString()
    ) : NavigationKey

    @Serializable
    class StringResultKey : NavigationKey.WithResult<String>
}