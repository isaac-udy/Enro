package dev.enro.tests.application.compose.results

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.completeFrom
import dev.enro.core.closeWithResult
import dev.enro.navigationHandle
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.tests.application.compose.common.TitledColumn

@NavigationDestination(ComposeEmbeddedResultFlow.Activity::class)
class ComposeEmbeddedResultFlowActivity : AppCompatActivity() {

    private val navigation by navigationHandle<ComposeEmbeddedResultFlow.Activity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Content()
            }
        }
        applyInsetsForContentView()
    }

    @Composable
    private fun Content() {
        TitledColumn(title = "Embedded Result Flow Activity") {
            Button(onClick = {
                navigation.completeFrom(
                    ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act x")
                )
            }) {
                Text("Navigate Activity (x)")
            }

            Button(onClick = {
                navigation.completeFrom(
                    ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act y")
                )
            }) {
                Text("Navigate Activity (y)")
            }

            Button(onClick = {
                navigation.closeWithResult(navigation.key.currentResult)
            }) {
                Text("Finish")
            }
        }
    }
}