package dev.enro.example.destinations.result.flow.embedded

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.result.deliverResultFromPush
import dev.enro.example.core.data.Adjective
import dev.enro.example.core.data.Adverb
import dev.enro.example.core.data.Sentence
import dev.enro.example.core.data.Words
import dev.enro.example.core.ui.WordCard
import dev.enro.example.destinations.result.compose.GetString
import kotlinx.parcelize.Parcelize

@Parcelize
class EmbeddedSelectAdjective(
    val adverb: Adverb
) : NavigationKey.SupportsPush.WithResult<Sentence>, NavigationKey.SupportsPresent.WithResult<Sentence>

@Composable
@NavigationDestination(EmbeddedSelectAdjective::class)
fun EmbeddedSelectAdjectiveDestination() {
    val navigation = navigationHandle<EmbeddedSelectAdjective>()
    fun continueFlow(adjective: Adjective) {
        navigation.deliverResultFromPush(
            EmbeddedSelectNoun(
                adverb = navigation.key.adverb,
                adjective = adjective
            )
        )
    }
    val requestCustom = registerForNavigationResult<String>() {
        continueFlow(Adjective(it))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "Select Adjective",
                    style = MaterialTheme.typography.h4
                )
                TextButton(
                    onClick = {
                        requestCustom.present(
                            GetString("Other Adjective")
                        )
                    },
                    content = { Text("Other") }
                )
            }
        }
        items(Words.adjectives) { word ->
            WordCard(
                word = word,
                onClick = {
                    continueFlow(word)
                }
            )
        }
    }
}