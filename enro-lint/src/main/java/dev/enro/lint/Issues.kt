@file:Suppress("UnstableApiUsage")

package dev.enro.lint

import com.android.tools.lint.detector.api.*

val incorrectlyTypedNavigationHandle = Issue.create(
    id = "IncorrectlyTypedNavigationHandle",
    briefDescription = "Incorrectly Typed Navigation Handle",
    explanation = "NavigationHandleProperty is expecting a NavigationKey that is different to the NavigationKey of the NavigationDestination",
    category = Category.PRODUCTIVITY,
    priority = 5,
    severity = Severity.ERROR,
    implementation = Implementation(EnroIssueDetector::class.java, Scope.JAVA_FILE_SCOPE)
)

val missingNavigationDestinationAnnotation = Issue.create(
    id = "MissingNavigationDestinationAnnotation",
    briefDescription = "Missing Navigation Destination Annotation",
    explanation = "Attempting to create a NavigationHandleProperty inside a class that is not marked as a NavigationDestination",
    category = Category.PRODUCTIVITY,
    priority = 5,
    severity = Severity.ERROR,
    implementation = Implementation(EnroIssueDetector::class.java, Scope.JAVA_FILE_SCOPE)
)

val missingExperimentalComposableDestinationOptIn = Issue.create(
    id = "MissingExperimentalComposableDestinationOptIn",
    briefDescription = "Using @NavigationDestination on @Composable functions is not enabled",
    explanation = "You must explicitly opt-in to using @NavigationDestination on @Composable functions using @ExperimentalComposableDestination or by passing the argument 'dev.enro.experimentalComposableDestinations' as 'enabled' to kapt",
    category = Category.MESSAGES,
    priority = 5,
    severity = Severity.ERROR,
    implementation = Implementation(EnroIssueDetector::class.java, Scope.JAVA_FILE_SCOPE)
)