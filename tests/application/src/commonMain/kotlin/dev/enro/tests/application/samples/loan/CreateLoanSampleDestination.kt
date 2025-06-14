package dev.enro.tests.application.samples.loan

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.flow.registerForFlowResult
import dev.enro.result.flow.rememberNavigationContainerForFlow
import dev.enro.tests.application.samples.loan.domain.LoanApplication
import dev.enro.tests.application.samples.loan.ui.GetLoanAmount
import dev.enro.tests.application.samples.loan.ui.GetLoanPurposeScreen
import dev.enro.tests.application.samples.loan.ui.GetLoanTerm
import dev.enro.tests.application.samples.loan.ui.GetOtherApplicants
import dev.enro.tests.application.samples.loan.ui.GetPrimaryApplicantInfo
import dev.enro.tests.application.samples.loan.ui.GetPropertyPurposeScreen
import dev.enro.tests.application.samples.loan.ui.GetRepaymentFrequencyScreen
import dev.enro.tests.application.samples.loan.ui.GetRepaymentTypeScreen
import dev.enro.tests.application.samples.loan.ui.LoanApplicationSummary
import dev.enro.tests.application.samples.loan.ui.LoanPurposeOption
import dev.enro.tests.application.samples.loan.ui.OwnershipOption
import dev.enro.tests.application.samples.loan.ui.PropertyPurposeOption
import dev.enro.tests.application.samples.loan.ui.RepaymentFrequencyOption
import dev.enro.tests.application.samples.loan.ui.RepaymentTypeOption
import dev.enro.tests.application.samples.loan.ui.SelectOwnershipType
import dev.enro.ui.NavigationDisplay
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.serialization.Serializable

@Serializable
object CreateLoanSampleDestination : NavigationKey.WithResult<LoanApplication>

class LoanApplicationFlowViewModel : ViewModel() {
    private val navigation by navigationHandle<CreateLoanSampleDestination>()

    val flow by registerForFlowResult(
        flow = {
            val applicant = open { GetPrimaryApplicantInfo }
            val ownershipType = open { SelectOwnershipType }
            val ownership = when (ownershipType) {
                is OwnershipOption.Sole -> LoanApplication.Ownership.Sole()
                is OwnershipOption.Partner -> LoanApplication.Ownership.Joint(
                    listOf(LoanApplication.Applicant("${applicant.name}'s partner"))
                )
                is OwnershipOption.Other -> {
                    val otherApplicants = open { GetOtherApplicants }
                    LoanApplication.Ownership.Joint(otherApplicants.toMutableList())
                }
            }
            val amount = open { GetLoanAmount }
            val term = open { GetLoanTerm }

            val loanPurposeOption = open { GetLoanPurposeScreen }
            val loanPurpose = when (loanPurposeOption) {
                is LoanPurposeOption.Car -> LoanApplication.LoanPurpose.Car()
                is LoanPurposeOption.Property -> {
                    val propertyPurposeOption = open {
                        dependsOn(loanPurposeOption)
                        GetPropertyPurposeScreen
                    }
                    val propertyPurpose = when (propertyPurposeOption) {
                        is PropertyPurposeOption.Investment -> LoanApplication.PropertyPurpose.Investment()
                        is PropertyPurposeOption.OwnerOccupied -> LoanApplication.PropertyPurpose.OwnerOccupied()
                    }
                    LoanApplication.LoanPurpose.Property(propertyPurpose)
                }
            }

            val interestOnlyAvailable = loanPurpose is LoanApplication.LoanPurpose.Property &&
                    loanPurpose.purpose is LoanApplication.PropertyPurpose.Investment

            val repaymentType = when {
                interestOnlyAvailable -> {
                    val repaymentTypeOption = open {
                        dependsOn(interestOnlyAvailable)
                        GetRepaymentTypeScreen
                    }
                    when (repaymentTypeOption) {
                        is RepaymentTypeOption.PrincipalAndInterest -> LoanApplication.RepaymentType.PrincipalAndInterest()
                        is RepaymentTypeOption.InterestOnly -> LoanApplication.RepaymentType.InterestOnly()
                    }
                }
                else -> LoanApplication.RepaymentType.PrincipalAndInterest()
            }

            val repaymentFrequencyOption = open { GetRepaymentFrequencyScreen }
            val repaymentFrequency = when (repaymentFrequencyOption) {
                is RepaymentFrequencyOption.Fortnightly -> LoanApplication.RepaymentFrequency.Fortnightly()
                is RepaymentFrequencyOption.Monthly -> LoanApplication.RepaymentFrequency.Monthly()
                is RepaymentFrequencyOption.Quarterly -> LoanApplication.RepaymentFrequency.Quarterly()
            }

            val application = LoanApplication(
                owner = applicant,
                amount = amount,
                termInMonths = term,
                ownership = ownership,
                repaymentFrequency = repaymentFrequency,
                repaymentType = repaymentType,
                purpose = loanPurpose,
            )

            open {
                LoanApplicationSummary(
                    application = application,
                    flowReference = navigationFlowReference,
                )
            }
        },
        onCompleted = { result ->
            navigation.complete(result)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@NavigationDestination(CreateLoanSampleDestination::class)
@Composable
fun CreateLoanApplicationScreen() {
    val viewModel = viewModel<LoanApplicationFlowViewModel> {
        createEnroViewModel {
            LoanApplicationFlowViewModel()
        }
    }
    val flowContainer = rememberNavigationContainerForFlow(viewModel.flow)
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Create Loan Application") },
                    navigationIcon = {
                        if (flowContainer.backstack.size > 1) {
                            IconButton(
                                onClick = { flowContainer.execute(NavigationOperation.Close(flowContainer.backstack.last())) }
                            ) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                )
                LinearProgressIndicator(
                    progress = animateFloatAsState(progressForBackstack(flowContainer.backstack)).value,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) {
        Box(Modifier.padding(it)) {
            NavigationDisplay(flowContainer)
        }
    }
}

@Composable
private fun progressForBackstack(
    backstack: NavigationBackstack,
): Float {
    val top = backstack.lastOrNull()
    val progress = remember { mutableStateOf(0f) }
    val updatedProgress = when (top?.key) {
        GetPrimaryApplicantInfo -> 0f
        SelectOwnershipType -> 0.2f
        GetOtherApplicants -> 0.3f
        GetLoanAmount -> 0.4f
        GetLoanTerm -> 0.5f
        GetLoanPurposeScreen -> 0.6f
        GetPropertyPurposeScreen -> 0.7f
        GetRepaymentTypeScreen -> 0.8f
        GetRepaymentFrequencyScreen -> 0.9f
        is LoanApplicationSummary -> 1f
        null -> 0f
        else -> null
    }
    LaunchedEffect(updatedProgress) {
        if (updatedProgress != null) progress.value = updatedProgress
    }
    return progress.value
}
