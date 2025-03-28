package dev.enro.example.destinations.result.compose

import android.os.Parcelable
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
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.example.core.data.Noun
import dev.enro.example.core.data.Words
import dev.enro.example.core.ui.WordCard
import kotlinx.parcelize.Parcelize

@Parcelize
object SelectNoun : Parcelable, NavigationKey.SupportsPush.WithResult<Noun>, NavigationKey.SupportsPresent.WithResult<Noun>

@Composable
@NavigationDestination(SelectNoun::class)
fun SelectNounDestination() {
    val navigation = navigationHandle<SelectNoun>()
    val requestCustom = registerForNavigationResult<String>() {
        navigation.closeWithResult(Noun(it))
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
                    text = "Select Noun",
                    style = MaterialTheme.typography.h4
                )
                TextButton(
                    onClick = {
                        requestCustom.present(
                            GetString("Other Noun")
                        )
                    },
                    content = { Text("Other") }
                )
            }
        }
        items(Words.nouns) { word ->
            WordCard(
                word = word,
                onClick = {
                    navigation.closeWithResult(word)
                }
            )
        }
    }
}