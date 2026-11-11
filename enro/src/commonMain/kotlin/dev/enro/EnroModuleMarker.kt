package dev.enro

/**
 * The `enro` module only re-exports the other Enro modules and has no code of its own.
 * Kotlin/Native produces no klib for a target without sources, and publishing that target
 * then fails, so this declaration gives every target something to compile.
 */
@Suppress("unused")
internal object EnroModuleMarker
