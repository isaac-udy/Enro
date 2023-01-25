package dev.enro.example

import android.os.Bundle
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.getNavigationHandle
import dev.enro.core.present
import dev.enro.core.push

@AndroidEntryPoint
class PlaygroundActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { getNavigationHandle().present(ExampleDialogKey()) }) {
                    Text(text = "Present Example Dialog")
                }

                Button(onClick = { getNavigationHandle().push(ExampleDialogKey()) }) {
                    Text(text = "Push Example Dialog")
                }

                Button(onClick = {
                    setContentView(TextView(this@PlaygroundActivity).apply {
                        text = "ASDASDASD"
                    })
                }) {
                    Text(text = "Set Content")
                }

                Button(onClick = {
                    getNavigationHandle().present(ExampleFragmentKey())
                }) {
                    Text(text = "Forward Compose")
                }

            }
        }
    }
}