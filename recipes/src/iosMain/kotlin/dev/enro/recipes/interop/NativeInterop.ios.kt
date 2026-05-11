package dev.enro.recipes.interop

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UILabel
import platform.UIKit.UITextAlignmentCenter

@Composable
actual fun PlatformInteropContent() {
    UIKitView(
        factory = {
            UILabel().apply {
                text = "This is a UIKit UILabel embedded inside an Enro destination."
                textAlignment = UITextAlignmentCenter
                numberOfLines = 0
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
    )
    Text("(rendered via `androidx.compose.ui.viewinterop.UIKitView`)")
}
