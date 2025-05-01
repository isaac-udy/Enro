package dev.enro.core

import kotlinx.serialization.KSerializer

public expect inline fun <reified T : NavigationKey> NavigationKey.Companion.defaultSerializer(): KSerializer<T>

//public expect ?eySerializer<T>