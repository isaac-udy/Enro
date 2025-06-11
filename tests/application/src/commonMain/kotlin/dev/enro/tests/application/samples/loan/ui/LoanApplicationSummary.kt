package dev.enro.tests.application.samples.loan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.flow.getNavigationFlow
import dev.enro.result.flow.getStep
import dev.enro.tests.application.samples.loan.GetLoanPurposeScreen
import dev.enro.tests.application.samples.loan.GetRepaymentFrequencyScreen
import dev.enro.tests.application.samples.loan.GetRepaymentTypeScreen
import dev.enro.tests.application.samples.loan.LoanApplicationSummary
import dev.enro.tests.application.samples.loan.domain.LoanApplication

@NavigationDestination(LoanApplicationSummary::class)
@Composable
fun LoanApplicationSummaryScreen() {
    val navigation = navigationHandle<LoanApplicationSummary>()
    val application = navigation.key.application
    val flowReference = remember(navigation.key.flowReference) {
        navigation.getNavigationFlow(navigation.key.flowReference)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Loan Application Summary",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EditableSummaryRow(
                    label = "Primary Applicant",
                    value = application.owner.name,
                    onEdit = {
                        flowReference.getStep<GetPrimaryApplicantInfo>()
                            ?.editStep()
                    }
                )

                val ownershipText = when (application.ownership) {
                    is LoanApplication.Ownership.Sole -> "Sole Ownership"
                    is LoanApplication.Ownership.Joint -> "Joint Ownership (${application.ownership.applicants.size} applicants)"
                }
                EditableSummaryRow(
                    label = "Ownership Type",
                    value = ownershipText,
                    onEdit = {
                        flowReference
                            .getStep { it == SelectOwnershipType }
                            ?.editStep()
                    }
                )

                if (application.ownership is LoanApplication.Ownership.Joint) {
                    application.ownership.applicants.forEachIndexed { index, applicant ->
                        SummaryRow("Co-Applicant ${index + 1}", applicant.name)
                    }
                }

                HorizontalDivider()

                // Format number with thousands separator
                val formattedAmount = application.amount.toString()
                    .reversed()
                    .chunked(3)
                    .joinToString(",")
                    .reversed()
                EditableSummaryRow(
                    label = "Loan Amount",
                    value = "$$formattedAmount",
                    onEdit = {
                        flowReference.getStep<GetLoanAmount>()?.editStep()
                    }
                )

                val years = application.termInMonths / 12
                val months = application.termInMonths % 12
                val termText = when {
                    years > 0 && months > 0 -> "$years year${if (years > 1) "s" else ""} $months month${if (months > 1) "s" else ""}"
                    years > 0 -> "$years year${if (years > 1) "s" else ""}"
                    else -> "$months month${if (months > 1) "s" else ""}"
                }
                EditableSummaryRow(
                    label = "Loan Term",
                    value = termText,
                    onEdit = {
                        flowReference.getStep<GetLoanTerm>()?.editStep()
                    }
                )

                val frequencyText = when (application.repaymentFrequency) {
                    is LoanApplication.RepaymentFrequency.Fortnightly -> "Fortnightly"
                    is LoanApplication.RepaymentFrequency.Monthly -> "Monthly"
                    is LoanApplication.RepaymentFrequency.Quarterly -> "Quarterly"
                }
                EditableSummaryRow(
                    label = "Repayment Frequency",
                    value = frequencyText,
                    onEdit = {
                        flowReference.getStep { it == GetRepaymentFrequencyScreen } ?.editStep()
                    }
                )

                val repaymentTypeText = when (application.repaymentType) {
                    is LoanApplication.RepaymentType.PrincipalAndInterest -> "Principal and Interest"
                    is LoanApplication.RepaymentType.InterestOnly -> "Interest Only"
                }
                EditableSummaryRow(
                    label = "Repayment Type",
                    value = repaymentTypeText,
                    onEdit = {
                        flowReference.getStep { it == GetRepaymentTypeScreen } ?.editStep()
                    }
                )

                val purposeText = when (application.purpose) {
                    is LoanApplication.LoanPurpose.Car -> "Car"
                    is LoanApplication.LoanPurpose.Property -> {
                        val propertyPurpose = when (application.purpose.purpose) {
                            is LoanApplication.PropertyPurpose.Investment -> "Investment Property"
                            is LoanApplication.PropertyPurpose.OwnerOccupied -> "Owner Occupied Property"
                        }
                        propertyPurpose
                    }
                }
                EditableSummaryRow(
                    label = "Purpose",
                    value = purposeText,
                    onEdit = {
                        flowReference.getStep { it == GetLoanPurposeScreen } ?.editStep()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navigation.complete(application) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Application")
        }
    }
}

@Composable
private fun EditableSummaryRow(
    label: String,
    value: String,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
