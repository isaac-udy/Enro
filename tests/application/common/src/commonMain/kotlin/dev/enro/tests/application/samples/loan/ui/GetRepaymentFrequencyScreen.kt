package dev.enro.tests.application.samples.loan.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class RepaymentFrequencyOption : MultiChoiceDestination.Item() {
    @Serializable
    class Fortnightly : RepaymentFrequencyOption() {
        override val title: String = "Fortnightly"
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Fortnightly

            return title == other.title
        }

        override fun hashCode(): Int {
            return title.hashCode()
        }


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