package dev.enro

/**
 * The `enro` module re-exports the other Enro modules and declares no other code.
 * Kotlin/Native produces no klib for a target without sources, and publishing a target
 * without a klib fails. EnroModuleMarker is a source declaration for every target.
 */
@Suppress("unused")
internal object EnroModuleMarker
