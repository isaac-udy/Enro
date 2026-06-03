package dev.enro.tests.application.samples.loan.domain

import kotlinx.serialization.Serializable


@Serializable
class LoanApplication(
    val owner: Applicant,
    val amount: Int,
    val termInMonths: Int,
    val ownership: Ownership,
    val purpose: LoanPurpose,
    val repaymentFrequency: RepaymentFrequency,
    val repaymentType: RepaymentType,
) {

    @Serializable
    class Applicant(
        val name: String,
    )

    @Serializable
    sealed class Ownership {
        @Serializable
        class Sole : Ownership()
        @Serializable
        class Joint(
            val applicants: List<Applicant>
        ) : Ownership()
    }

    @Serializable
    sealed class RepaymentFrequency {
        @Serializable
        class Fortnightly : RepaymentFrequency()
        @Serializable
        class Monthly : RepaymentFrequency()
        @Serializable
        class Quarterly : RepaymentFrequency()
    }

    @Serializable
    sealed class LoanPurpose {
        @Serializable
        class Car : LoanPurpose()
        @Serializable
        class Property(
            val purpose: PropertyPurpose,
        ) : LoanPurpose()
    }

    @Serializable
    sealed class PropertyPurpose {
        @Serializable
        class Investment : PropertyPurpose()
        @Serializable
        class OwnerOccupied : PropertyPurpose()
    }

    @Serializable
    sealed class RepaymentType {
        @Serializable
        class PrincipalAndInterest : RepaymentType()
        @Serializable
        class InterestOnly : RepaymentType()
    }
}