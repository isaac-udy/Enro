package dev.enro

import dev.enro.platform.callSiteClassName
import kotlin.reflect.KClass

// For error and log messages: unlike stableClassName, this never throws.
internal val KClass<*>.displayName: String
    get() = callSiteClassName(this) ?: simpleName ?: toString()
