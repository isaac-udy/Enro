package dev.enro.tests.application.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
object LazyColumn : NavigationKey.SupportsPush

@Composable
@NavigationDestination(LazyColumn::class)
fun LazyColumnScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Lazy Column",
                style = MaterialTheme.typography.h6
            )
        }
        items(1000) { number ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                Text(
                    text = "$number",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }
    }
}