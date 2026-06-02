package dev.enro.recipes.interop

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
actual fun PlatformInteropContent() {
    Text(
        "Native interop on the web target typically means embedding HTML elements. " +
            "Compose for WASM does not currently expose a stable `HtmlView` API for this " +
            "but the Enro destination is otherwise identical to other platforms.",
    )
}
