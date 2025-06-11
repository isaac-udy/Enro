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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.tests.application.samples.loan.GetOtherApplicants
import dev.enro.tests.application.samples.loan.domain.LoanApplication

@NavigationDestination(GetOtherApplicants::class)
@Composable
fun GetOtherApplicantsScreen() {
    val navigation = navigationHandle<GetOtherApplicants>()
    var applicants by remember { mutableStateOf(listOf<LoanApplication.Applicant>()) }
    var newApplicantName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Other Applicants",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newApplicantName,
                onValueChange = { newApplicantName = it },
                label = { Text("Applicant Name") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (newApplicantName.isNotBlank()) {
                        applicants = applicants + LoanApplication.Applicant(newApplicantName)
                        newApplicantName = ""
                    }
                },
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
}
