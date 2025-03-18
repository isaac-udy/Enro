package dev.enro.annotations

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class GeneratedNavigationBinding(
    val destination: String,
    val navigationKey: String
)