package dev.enro.tests.application.samples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.closeAndReplaceWith
import dev.enro.tests.application.samples.loan.CreateLoanSampleDestination
import dev.enro.tests.application.samples.recipes.RecipesSampleDestination
import dev.enro.tests.application.samples.travel.TravelSampleDestination
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

@Serializable
object SelectSampleDestination : NavigationKey

@NavigationDestination(SelectSampleDestination::class)
val selectSampleDestination = navigationDestination<SelectSampleDestination>(
    metadata = {
        directOverlay()
    }
) {
    Dialog(
        onDismissRequest = { navigation.close() }
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Samples",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SampleDestinationItem(
                            title = "Loan Application",
                            description = "Sample loan application flow",
                            onClick = { navigation.closeAndReplaceWith(CreateLoanSampleDestination) }
                        )
                    }
                    item {
                        SampleDestinationItem(
                            title = "Recipe Browser",
                            description = "Browse and view recipes",
                            onClick = { navigation.closeAndReplaceWith(RecipesSampleDestination) }
                        )
                    }
                    item {
                        SampleDestinationItem(
                            title = "Travel Planner",
                            description = "Plan your next adventure",
                            onClick = { navigation.closeAndReplaceWith(TravelSampleDestination) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SampleDestinationItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(bottom = 2.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxWidth()
                .padding(16.dp)

        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
