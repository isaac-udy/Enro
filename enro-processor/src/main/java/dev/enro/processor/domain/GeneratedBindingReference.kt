package dev.enro.processor.domain

sealed interface GeneratedBindingReference {
    val binding: String
    val destination: String
    val navigationKey: String

    class Kotlin(
        override val binding: String,
        override val destination: String,
        override val navigationKey: String,
    ) : GeneratedBindingReference

    class Java(
        override val binding: String,
        override val destination: String,
        override val navigationKey: String,
    ) : GeneratedBindingReference
}