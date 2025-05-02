package dev.enro.core.serialization

import dev.enro.core.NavigationKey
import kotlinx.serialization.KSerializer

public expect inline fun <reified T : NavigationKey> NavigationKey.Companion.defaultSerializer(): KSerializer<T>