package dev.enro

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass

internal fun <T> describe(description: String, predicate: (T) -> Boolean): DescribedPredicate<T> =
    object : DescribedPredicate<T>(description) {
        override fun test(item: T): Boolean {
            return predicate(item)
        }
    }

internal fun unwrapEnclosingTypes(cls: JavaClass): List<JavaClass> {
    val enclosing = cls.enclosingClass.map { unwrapEnclosingTypes(it) }.orElse(emptyList())
    return listOf(cls) + enclosing
}

internal fun DescribedPredicate<JavaClass>.includingEnclosing(): DescribedPredicate<JavaClass> {
    val wrapped = this
    return describe("$description (including enclosing)") { cls ->
        val enclosing = unwrapEnclosingTypes(cls)
        return@describe enclosing.any { wrapped.test(it) }
    }
}

internal val isTestSource: DescribedPredicate<JavaClass> = describe("is in test sources") { cls ->
    val fileName = cls.source.takeIf { it.isPresent }
        ?.get()
        ?.uri
        ?.toString() ?: return@describe false
    return@describe fileName.contains("UnitTest")
}
