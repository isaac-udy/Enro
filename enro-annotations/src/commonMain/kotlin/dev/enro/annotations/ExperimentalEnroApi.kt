package dev.enro.annotations

// Library code
@RequiresOptIn(message = "This is an experimental API, and should be used with care. Experimental APIs may change without warning, or be removed entirely.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalEnroApi