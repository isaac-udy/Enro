package dev.enro.tests.application.samples.loan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.serialization.enroSaver
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MultiChoiceDestination<T: MultiChoiceDestination.Item>(
    val title: String,
    val subtitle: String,
    @Contextual val items: List<T>,
) : NavigationKey.WithResult<T> {

    @Serializable
    abstract class Item {
        abstract val title: String
    }
}


@NavigationDestination(MultiChoiceDestination::class)
@Composable
fun MultiChoiceDestinationScreen() {
    val navigation = navigationHandle<MultiChoiceDestination<MultiChoiceDestination.Item>>()
    val destination = navigation.key
    var selectedItem by rememberSaveable(saver = enroSaver()) {
        mutableStateOf<MultiChoiceDestination.Item?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = destination.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = destination.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(destination.items) { item ->
                val isSelected = selectedItem?.title == item.title
                Card(
                    onClick = {
                        selectedItem = item
                        navigation.complete(item)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CardDefaults.shape
                                )
                            } else Modifier
                        ),
                    colors = if (isSelected) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else CardDefaults.cardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}
