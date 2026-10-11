package dev.enro.platform

import kotlin.reflect.KClass

@PublishedApi
internal actual fun callSiteClassName(kClass: KClass<*>): String? {
    return kClass.qualifiedName ?: kClass.toString()
}
