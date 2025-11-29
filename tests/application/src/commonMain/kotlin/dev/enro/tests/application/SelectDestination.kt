package dev.enro.tests.application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.tests.application.compose.ComposeSharedElementTransitions
import dev.enro.tests.application.compose.DialogScene
import dev.enro.tests.application.compose.HorizontalPager
import dev.enro.tests.application.compose.common.TitledLazyColumn
import dev.enro.tests.application.compose.results.ComposeAsyncManagedResultFlow
import dev.enro.tests.application.compose.results.ComposeEmbeddedResultFlow
import dev.enro.tests.application.compose.results.ComposeManagedResultFlow
import dev.enro.tests.application.compose.results.ComposeManagedResultsWithNestedFlowAndEmptyRoot
import dev.enro.tests.application.compose.results.ComposeMixedResultTypes
import dev.enro.tests.application.compose.results.ComposeNestedResults
import dev.enro.tests.application.compose.results.ResultsWithMetadata
import dev.enro.tests.application.samples.SelectSampleDestination
import dev.enro.tests.application.serialization.CommonSerialization
import dev.enro.tests.application.window.SimpleWindow
import kotlinx.serialization.Serializable

@Serializable
internal class SelectDestination() : NavigationKey {
    internal companion object {
        internal val selectableDestinations = run {
            val commonDestinations = listOf<NavigationKey>(
                CommonSerialization,
                ComposeAsyncManagedResultFlow,
                ComposeEmbeddedResultFlow,
                ComposeManagedResultFlow,
                ComposeManagedResultsWithNestedFlowAndEmptyRoot,
                ComposeMixedResultTypes,
                ComposeNestedResults,
                ComposeSharedElementTransitions,
                DialogScene,
                HorizontalPager,
                ResultsWithMetadata,
                SimpleWindow,
            )

            mutableStateOf(
                commonDestinations
                    .map { SelectableDestination(it) }
                    .sortedBy { it.title }
            )
        }

        fun registerSelectableDestinations(
            vararg destinations: NavigationKey,
        ) {
            selectableDestinations.value = (selectableDestinations.value.plus(
                destinations.toList()
                    .map { SelectableDestination(it) }
            )).sortedBy { it.title }
        }
    }
}

@Composable
@NavigationDestination(SelectDestination::class)
fun SelectDestinationScreen() {
    val destinations = SelectDestination.selectableDestinations.value

    TitledLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("SelectDestinationLazyColumn"),
        title = "Select Destination"
    ) {
        item {
            Text(
                text = "Samples",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectableDestinationCard(
                selectableDestination = SelectableDestination(
                    title = "Samples",
                    key = SelectSampleDestination
                )
            )
        }
        item {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
        item {
            Text(
                text = "Tests",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        destinations.forEach {
            item {
                SelectableDestinationCard(selectableDestination = it)
            }
        }
    }
}

@Composable
fun SelectableDestinationCard(
    selectableDestination: SelectableDestination,
) {
    val navigation = navigationHandle()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = selectableDestination.title,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )

            TextButton(
                modifier = Modifier.widthIn(min = 56.dp),
                onClick = {
                    navigation.open(selectableDestination.key)
                }
            ) {
                Text("Open")
            }
        }
    }
}

data class SelectableDestination(
    val key: NavigationKey,
    val title: String,
)

fun SelectableDestination(
    key: NavigationKey,
): SelectableDestination {
    val title = key::class.simpleName!!.toCharArray()
        .mapIndexed { index, c ->
            if (index > 0 && c.isUpperCase()) {
                return@mapIndexed " $c"
            }
            return@mapIndexed c.toString()
        }
        .joinToString(separator = "")

    return SelectableDestination(
        key = key,
        title = title,
    )
}
