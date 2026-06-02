package dev.enro.recipes.interop

import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun PlatformInteropContent() {
    AndroidView(
        factory = { context ->
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(
                    TextView(context).apply {
                        text = "This is a classic Android View embedded inside an Enro destination. " +
                            "Fragments can be hosted the same way via enro-compat's Fragment APIs."
                    },
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
