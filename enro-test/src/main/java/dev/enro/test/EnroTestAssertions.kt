package dev.enro.test

import dev.enro.core.NavigationKey

class EnroTestAssertionException(message: String) : AssertionError(message)

@PublishedApi
internal fun enroAssertionError(message: String): Nothing {
    throw EnroTestAssertionException(message)
}

data class EnroAssertionContext(
    val expected: Any?,
    val actual: Any?,
)

@PublishedApi
internal fun <T> T.shouldBeEqualTo(expected: Any?, message: EnroAssertionContext.() -> String): T {
    if (this != expected) {
        val assertionContext = EnroAssertionContext(
            expected = expected,
            actual = this
        )
        throw EnroTestAssertionException(message(assertionContext))
    }
    return this
}

@PublishedApi
internal fun <T> T.shouldNotBeEqualTo(expected: Any?, message: EnroAssertionContext.() -> String): T {
    if (this == expected) {
        val assertionContext = EnroAssertionContext(
            expected = expected,
            actual = this
        )
        throw EnroTestAssertionException(message(assertionContext))
    }
    return this
}

@PublishedApi
internal fun <T> T.shouldMatchPredicate(predicate: (T) -> Boolean, message: EnroAssertionContext.() -> String): T {
    val predicateResult = predicate(this)
    if (!predicateResult) {
        val assertionContext = EnroAssertionContext(
            expected = null,
            actual = this
        )
        throw EnroTestAssertionException(message(assertionContext))
    }
    return this
}

@PublishedApi
internal fun <T> T.shouldNotMatchPredicate(predicate: (T) -> Boolean, message: EnroAssertionContext.() -> String): T {
    val predicateResult = predicate(this)
    if (predicateResult) {
        val assertionContext = EnroAssertionContext(
            expected = null,
            actual = this
        )
        throw EnroTestAssertionException(message(assertionContext))
    }
    return this
}

@PublishedApi
internal fun <T: Any> T?.shouldMatchPredicateNotNull(predicate: (T) -> Boolean, message: EnroAssertionContext.() -> String): T {
    if (this == null) {
        throw EnroTestAssertionException("Expected a non-null value, but was null.")
    }

    val predicateResult = predicate(this)
    if (!predicateResult) {
        val assertionContext = EnroAssertionContext(
            expected = null,
            actual = this
        )
        throw EnroTestAssertionException(message(assertionContext))
    }
    return this
}

@PublishedApi
internal inline fun <reified T> Any?.shouldBeInstanceOf(): T {
    if (this == null) {
        throw EnroTestAssertionException("Expected a non-null value, but was null.")
    }

    val isCorrectType = this is T
    if (!isCorrectType) {
        val assertionContext = EnroAssertionContext(
            expected = T::class,
            actual = this::class
        )
        throw EnroTestAssertionException("Expected type ${T::class.simpleName}, but was ${this::class.simpleName}")
    }
    return this as T
}

@PublishedApi
internal fun <T : NavigationKey> Any?.shouldBeInstanceOf(
    cls: Class<T>,
) : T {
    if (this == null) {
        throw EnroTestAssertionException("Expected a non-null value, but was null.")
    }

    val isCorrectType = cls.isAssignableFrom(this::class.java)
    if (!isCorrectType) {
        throw EnroTestAssertionException("Expected type ${cls.simpleName}, but was ${this::class.simpleName}")
    }
    return this as T
}