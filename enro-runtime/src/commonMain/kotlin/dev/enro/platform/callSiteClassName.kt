package dev.enro.platform

import kotlin.reflect.KClass

// Returns qualifiedName, or toString() when qualifiedName is null. Returns null on Kotlin/JS.
// The name is not unique: wasm and native give every anonymous class in a declaration the same
// name, so an id built from the name requires a counter.
@PublishedApi
internal expect fun callSiteClassName(kClass: KClass<*>): String?
