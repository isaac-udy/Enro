package dev.enro.platform

import kotlin.reflect.KClass

// Kotlin/JS has no qualifiedName, so callers fall back to stableClassName.
@PublishedApi
internal actual fun callSiteClassName(kClass: KClass<*>): String? = null
