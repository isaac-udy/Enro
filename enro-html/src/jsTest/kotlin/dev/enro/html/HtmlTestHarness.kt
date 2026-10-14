package dev.enro.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import dev.enro.test.EnroTest
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.coroutines.resume

class HtmlTestScope(val root: HTMLElement) {
    private var composition: Composition? = null

    fun setContent(content: @Composable () -> Unit) {
        check(composition == null) { "setContent was already called in this test" }
        composition = renderComposable(root) { content() }
    }

    fun disposeContent() {
        composition?.dispose()
        composition = null
    }

    fun element(selector: String): HTMLElement? =
        root.querySelector(selector) as HTMLElement?

    fun requireElement(selector: String): HTMLElement =
        requireNotNull(element(selector)) { "No element matches '$selector' in ${root.innerHTML}" }

    fun click(selector: String) {
        requireElement(selector).click()
    }

    suspend fun awaitFrame() {
        suspendCancellableCoroutine { continuation ->
            window.requestAnimationFrame { continuation.resume(Unit) }
        }
    }

    suspend fun awaitUntil(
        description: String,
        timeoutMillis: Double = 1_000.0,
        condition: () -> Boolean,
    ) {
        val start = window.performance.now()
        while (!condition()) {
            check(window.performance.now() - start < timeoutMillis) {
                "Timed out waiting until $description. DOM: ${root.innerHTML}"
            }
            awaitFrame()
        }
    }
}

fun runHtmlTest(block: suspend HtmlTestScope.() -> Unit): dynamic = MainScope().promise {
    EnroTest.installNavigationController()
    val root = document.createElement("div") as HTMLElement
    document.body!!.appendChild(root)
    val scope = HtmlTestScope(root)
    try {
        scope.block()
    } finally {
        scope.disposeContent()
        root.remove()
        EnroTest.uninstallNavigationController()
    }
}
