package dev.enro.tests.application.samples.loan.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class LoanPurposeOption : MultiChoiceDestination.Item() {
    @Serializable
    class Car : LoanPurposeOption() {
        override val title: String = "Car"
    }

    @Serializable
    class Property : LoanPurposeOption() {
        override val title: String = "Property"
    }
}

val GetLoanPurposeScreen = MultiChoiceDestination(
    title = "What's it for?",
    subtitle = "What are you planning to use this loan for?",
    items = listOf(
        LoanPurposeOption.Car(),
        LoanPurposeOption.Property()
    )
)