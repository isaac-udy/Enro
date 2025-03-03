import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.enro.tests.application.compose.common.TitledColumn


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Enro Test Application",
    ) {
        TitledColumn("Destinations") {
            Text("Nothing to see here")
            Text(
                modifier = Modifier.alpha(0.33f),
                text = "(yet...)"
            )
            Button(onClick = ::exitApplication) {
                Text("Exit")
            }
        }
    }
}