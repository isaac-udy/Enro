package dev.enro.annotations


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
@ExperimentalEnroApi
public annotation class NavigationPath(
    val pattern: String,
)