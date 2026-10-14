package dev.enro.html

import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.w3c.dom.Element

internal suspend fun Element.awaitTransitions() {
    val style = window.getComputedStyle(this)
    val longest = longestTransitionMillis(
        durations = style.getPropertyValue("transition-duration"),
        delays = style.getPropertyValue("transition-delay"),
    )
    if (longest > 0) delay(longest.toLong())
}

/**
 * The time in milliseconds until the last transition described by the computed
 * `transition-duration` and `transition-delay` lists ends. A shorter delay list repeats, as it
 * does in CSS.
 */
internal fun longestTransitionMillis(durations: String, delays: String): Double {
    val durationMillis = durations.toMillisList()
    val delayMillis = delays.toMillisList().ifEmpty { listOf(0.0) }
    return durationMillis.indices.maxOfOrNull { index ->
        durationMillis[index] + delayMillis[index % delayMillis.size]
    } ?: 0.0
}

private fun String.toMillisList(): List<Double> = split(',')
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .map { value ->
        when {
            value.endsWith("ms") -> value.removeSuffix("ms").toDoubleOrNull() ?: 0.0
            value.endsWith("s") -> (value.removeSuffix("s").toDoubleOrNull() ?: 0.0) * 1000
            else -> 0.0
        }
    }
