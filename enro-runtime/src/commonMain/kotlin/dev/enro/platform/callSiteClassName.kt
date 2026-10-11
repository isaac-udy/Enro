package dev.enro.platform

import kotlin.reflect.KClass

// qualifiedName, falling back to toString(). Not unique: wasm and native give every anonymous class
// in a declaration the same name, so ids built from it need a counter. Null on Kotlin/JS.
@PublishedApi
internal expect fun callSiteClassName(kClass: KClass<*>): String?
