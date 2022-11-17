package dev.enro.core

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
internal annotation class ArchitectureException(val reason: String)