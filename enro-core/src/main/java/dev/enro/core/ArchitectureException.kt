package dev.enro.core

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
internal annotation class ArchitectureException(
    val reason: String
)