package dev.enro.tests.application.samples.loan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.tests.application.samples.loan.domain.LoanApplication
import kotlinx.serialization.Serializable

@Serializable
object GetOtherApplicants : NavigationKey.WithResult<List<LoanApplication.Applicant>>

@NavigationDestination(GetOtherApplicants::class)
@Composable
fun GetOtherApplicantsScreen() {
    val navigation = navigationHandle<GetOtherApplicants>()
    var applicants by rememberSaveable { mutableStateOf(listOf<LoanApplication.Applicant>()) }
    var newApplicantName by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    fun addApplicant() {
        if (newApplicantName.isNotBlank()) {
            applicants = applicants + LoanApplication.Applicant(newApplicantName)
            newApplicantName = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Who else is applying?",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add any co-applicants who'll share this loan with you",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newApplicantName,
                onValueChange = { newApplicantName = it },
                label = { Text("Co-applicant name") },
                placeholder = { Text("e.g., Jane Doe") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { addApplicant() }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { addApplicant() },
                enabled = newApplicantName.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Applicant")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(applicants) { applicant ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(applicant.name)
                        IconButton(
                            onClick = {
                                applicants = applicants.filter { it != applicant }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }

            if (applicants.isEmpty()) {
                item {
                    Text(
                        text = "No co-applicants added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                navigation.complete(applicants)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = applicants.isNotEmpty()
        ) {
            Text("Continue")
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
