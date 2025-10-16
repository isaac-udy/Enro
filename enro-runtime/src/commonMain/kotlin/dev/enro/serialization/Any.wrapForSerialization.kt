@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.serialization

@PublishedApi
internal fun Any?.wrapForSerialization(): Any {
    return this.internalWrapForSerialization()
}