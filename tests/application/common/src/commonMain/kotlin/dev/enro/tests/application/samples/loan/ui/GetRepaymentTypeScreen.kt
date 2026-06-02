package dev.enro.tests.application.samples.loan.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class RepaymentTypeOption : MultiChoiceDestination.Item() {
    @Serializable
    class PrincipalAndInterest : RepaymentTypeOption() {
        override val title: String = "Principal and Interest"
    }

    @Serializable
    class InterestOnly : RepaymentTypeOption() {
        override val title: String = "Interest Only"
    }
}

val GetRepaymentTypeScreen = MultiChoiceDestination(
    title = "Repayment style",
    subtitle = "How would you like to structure your repayments?",
    items = listOf(
        RepaymentTypeOption.PrincipalAndInterest(),
        RepaymentTypeOption.InterestOnly()
    )
)