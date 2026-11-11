package dev.enro.compat

/**
 * All of `enro-compat`'s code is Android-only, but the module declares the other targets
 * too. Kotlin/Native produces no klib for a target without sources, and publishing that
 * target then fails, so this declaration gives every target something to compile.
 */
@Suppress("unused")
internal object EnroCompatModuleMarker
