package dev.enro.recipes.interop

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel

@Composable
actual fun PlatformInteropContent() {
    SwingPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        factory = {
            JPanel().apply {
                background = Color(245, 245, 245)
                add(JLabel("This is a Swing JLabel embedded inside an Enro destination."))
            }
        },
    )
    Text("(rendered via `androidx.compose.ui.awt.SwingPanel`)")
}
