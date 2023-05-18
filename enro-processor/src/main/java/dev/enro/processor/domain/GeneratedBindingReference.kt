package dev.enro.processor.domain

import com.google.devtools.ksp.processing.Resolver

sealed interface GeneratedBindingReference {
    val binding: String
    val destination: String
    val navigationKey: String

    class Kotlin(
        val resolver: Resolver,
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