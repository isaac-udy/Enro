package dev.enro.compat

/**
 * Every other declaration in `enro-compat` is Android-only, and the module also declares
 * the other targets. Kotlin/Native produces no klib for a target without sources, and
 * publishing a target without a klib fails. EnroCompatModuleMarker is a source declaration
 * for every target.
 */
@Suppress("unused")
internal object EnroCompatModuleMarker
