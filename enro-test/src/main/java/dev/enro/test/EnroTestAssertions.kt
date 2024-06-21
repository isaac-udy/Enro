package dev.enro.test

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
internal fun Any?.shouldBeEqualTo(expected: Any?, message: EnroAssertionContext.() -> String) {
    if (this != expected) {
        val assertionContext = EnroAssertionContext(
            expected = expected,
            actual = this
        )
        throw EnroTestAssertionException(message(assertionContext))
    }
}

@PublishedApi
internal fun Any?.shouldNotBeEqualTo(expected: Any?, message: EnroAssertionContext.() -> String) {
    if (this == expected) {
        val assertionContext = EnroAssertionContext(
            expected = expected,
            actual = this
        )
        throw EnroTestAssertionException(message(assertionContext))
    }
}