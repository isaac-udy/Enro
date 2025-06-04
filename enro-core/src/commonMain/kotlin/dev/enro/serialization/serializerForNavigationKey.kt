package dev.enro.serialization

import dev.enro.NavigationKey
import kotlinx.serialization.KSerializer

/**
 * This exists for the purpose of supporting Parcelable NavigationKeys on Android.
 *
 * Platforms other than Android will always delegate to serializer<T>().
 */
@PublishedApi
internal inline expect fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T>