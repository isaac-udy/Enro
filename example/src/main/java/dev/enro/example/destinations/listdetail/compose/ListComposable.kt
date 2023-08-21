package dev.enro.example.destinations.listdetail.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.destination.compose.navigationHandle
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.push
import dev.enro.core.container.setBackstack
import dev.enro.core.onContainer
import dev.enro.example.core.data.Words
import dev.enro.example.core.ui.WordCard
import kotlinx.parcelize.Parcelize

@Parcelize
class ListComposable : NavigationKey.SupportsPush

@Composable
@NavigationDestination(ListComposable::class)
fun ListComposeScreen() {
    val navigation = navigationHandle()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        item {
            Text(
                text = "Words",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.h4
            )
        }
        items(Words.all) { word ->
            WordCard(
                word = word,
                onClick = {
                    navigation.onContainer(detailContainerKey) {
                        setBackstack { emptyBackstack().push(DetailComposable(word)) }
                    }
                }
            )
        }
    }
}
