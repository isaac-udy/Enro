package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.tests.application.compose.ComposeSharedElementTransitions
import dev.enro.tests.application.compose.DialogScene
import dev.enro.tests.application.compose.HorizontalPager
import dev.enro.tests.application.compose.results.ComposeAsyncManagedResultFlow
import dev.enro.tests.application.compose.results.ComposeEmbeddedResultFlow
import dev.enro.tests.application.compose.results.ComposeManagedResultFlow
import dev.enro.tests.application.compose.results.ComposeManagedResultsWithNestedFlowAndEmptyRoot
import dev.enro.tests.application.compose.results.ComposeMixedResultTypes
import dev.enro.tests.application.compose.results.ComposeNestedResults
import dev.enro.tests.application.compose.results.ResultsWithMetadata
import dev.enro.tests.application.samples.loan.CreateLoanSample
import dev.enro.tests.application.samples.recipes.RecipesSample
import dev.enro.tests.application.samples.travel.TravelSample
import dev.enro.tests.application.serialization.CommonSerialization
import dev.enro.tests.application.window.SimpleWindow
import kotlinx.serialization.Serializable

@Serializable
internal class SelectDestination(
    val showAsDialog: Boolean = false
) : NavigationKey {
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
                RecipesSample,
                TravelSample,
                CreateLoanSample,
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
    val navigation = navigationHandle<SelectDestination>()
    val isPresented = navigation.key.showAsDialog
    val destinations = SelectDestination.selectableDestinations.value

    val destinationsList = remember {
        movableContentOf {
            destinations.forEach {
                ReflectedDestinationCard(selectableDestination = it)
            }
        }
    }

    if (isPresented) {
        Dialog(
            onDismissRequest = { navigation.close() }
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .shadow(16.dp)
                    .background(
                        color = MaterialTheme.colors.background,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Destinations",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                destinationsList()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Destinations",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            destinationsList()
        }
    }
}

@Composable
fun ReflectedDestinationCard(
    selectableDestination: SelectableDestination
) {
    val navigation = navigationHandle()
    Card(
        elevation = 4.dp
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
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.button,
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
