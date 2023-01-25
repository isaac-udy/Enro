package dev.enro.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.enro.core.compose.navigationHandle
import dev.enro.core.parentContainer
import dev.enro.example.R
import dev.enro.example.data.toSentenceId

@Composable
fun ExampleScreenTemplate(
    title: String,
    modifier: Modifier = Modifier,
    buttons: List<Pair<String, () -> Unit>> = emptyList()
) {
    val scrollState = rememberScrollState()
    val navigation = navigationHandle()
    val backstackState by parentContainer?.backstackFlow?.collectAsState() ?: mutableStateOf(null)

    var backstackItems by remember { mutableStateOf(listOf<String>()) }

    DisposableEffect(backstackState) {
        val backstackState = backstackState ?: return@DisposableEffect onDispose {  }
        backstackItems = backstackState.backstack
            .takeWhile { it != navigation.instruction }
            .map { instruction ->
                instruction.instructionId.toSentenceId()
            }
            .reversed()
        onDispose {  }
    }

    Surface(
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
        ) {
            Column(
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(R.string.example_content),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Current Destination:",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = navigation.instruction.instructionId.toSentenceId(),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Backstack:",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.h6
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    backstackItems.forEach {
                        key(it) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                buttons.forEach { button ->
                    OutlinedButton(
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                        onClick = {
                            button.second()
                        }) {
                        Text(button.first)
                    }
                }
            }
        }
    }
}