package dev.enro.annotations

// Library code
@RequiresOptIn(message = "This is an advanced API, and should be used with care. The advanced APIs are designed to build advanced functionality on top of Enro, and may change without warning.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class AdvancedEnroApi

// Library code
@RequiresOptIn(message = "This is an experimental API, and should be used with care. Experimental APIs may change without warning, or be removed entirely.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalEnroApi