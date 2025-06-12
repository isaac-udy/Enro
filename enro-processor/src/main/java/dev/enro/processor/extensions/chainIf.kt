package dev.enro.processor.extensions

fun <T> T.chainIf(predicate: Boolean, block: T.() -> T): T {
    if (!predicate) return this
    return block()
}