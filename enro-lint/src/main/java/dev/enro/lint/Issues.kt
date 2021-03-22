@file:Suppress("UnstableApiUsage")

package dev.enro.lint

import com.android.tools.lint.detector.api.*

val incorrectlyTypedNavigationHandle = Issue.create(
    id = "IncorrectlyTypedNavigationHandle",
    briefDescription = "Incorrectly Typed Navigation Handle",
    explanation = "TypedNavigationHandle is expecting a NavigationKey that is different to the NavigationKey expected by the NavigationDestination",
    category = Category.PRODUCTIVITY,
    priority = 5,
    severity = Severity.ERROR,
    implementation = Implementation(EnroIssueDetector::class.java, Scope.JAVA_FILE_SCOPE)
)