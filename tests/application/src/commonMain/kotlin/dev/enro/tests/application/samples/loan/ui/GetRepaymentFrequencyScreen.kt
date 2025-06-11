package dev.enro.tests.application.samples.loan.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class RepaymentFrequencyOption : MultiChoiceDestination.Item() {
    @Serializable
    class Fortnightly : RepaymentFrequencyOption() {
        override val title: String = "Fortnightly"
    }

    @Serializable
    class Monthly : RepaymentFrequencyOption() {
        override val title: String = "Monthly"
    }

    @Serializable
    class Quarterly : RepaymentFrequencyOption() {
        override val title: String = "Quarterly"
    }
}

val GetRepaymentFrequencyScreen = MultiChoiceDestination(
    title = "Payment schedule",
    subtitle = "How often would you like to make payments?",
    items = listOf(
        RepaymentFrequencyOption.Fortnightly(),
        RepaymentFrequencyOption.Monthly(),
        RepaymentFrequencyOption.Quarterly()
    )
)