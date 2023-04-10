package dev.enro.example.destinations.result.flow.embedded

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.requestClose
import dev.enro.example.core.data.Adjective
import dev.enro.example.core.data.Adverb
import dev.enro.example.core.data.Noun
import dev.enro.example.core.data.Sentence
import dev.enro.example.core.ui.WordCard
import kotlinx.parcelize.Parcelize

@Parcelize
class EmbeddedConfirmSentence(
    val adverb: Adverb,
    val adjective: Adjective,
    val noun: Noun
) : NavigationKey.SupportsPresent.WithResult<Sentence>

@Composable
@NavigationDestination(EmbeddedConfirmSentence::class)
fun EmbeddedConfirmSentenceDestination() = DialogDestination {
    val navigation = navigationHandle<EmbeddedConfirmSentence>()
    Dialog(
        onDismissRequest = {
            navigation.requestClose()
        },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = .5.dp)
                    .background(
                        color = MaterialTheme.colors.surface,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(
                        top = 16.dp,
                        bottom = 16.dp
                    )
            ) {
                Text(
                    text = "Confirm Sentence",
                    style = MaterialTheme.typography.h5,
                )
                Spacer(modifier = Modifier.height(12.dp))
                WordCard(word = navigation.key.adverb)
                WordCard(word = navigation.key.adjective)
                WordCard(word = navigation.key.noun)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        navigation.closeWithResult(
                            Sentence(
                                adverb = navigation.key.adverb,
                                adjective = navigation.key.adjective,
                                noun = navigation.key.noun,
                            )
                        )
                    }
                ) {
                    Text(text = "Confirm")
                }
            }
        }
    )
}