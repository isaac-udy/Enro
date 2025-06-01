package dev.enro3.serialization

import dev.enro3.NavigationKey
import kotlinx.serialization.KSerializer

public expect inline fun <reified T : NavigationKey> NavigationKey.Companion.defaultSerializer(): KSerializer<T>