@file:Suppress("UnstableApiUsage")

package dev.enro.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

val incorrectlyTypedNavigationHandle = Issue.create(
    id = "IncorrectlyTypedNavigationHandle",
    briefDescription = "Incorrectly Typed Navigation Handle",
    explanation = "NavigationHandle is expecting a NavigationKey that is different to the NavigationKey of the NavigationDestination",
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

val missingNavigationDestinationAnnotationCompose = Issue.create(
    id = "MissingNavigationDestinationAnnotation",
    briefDescription = "Missing Navigation Destination Annotation",
    explanation = "Requesting a TypedNavigationHandle here may cause a crash, " +
            "as there is no guarantee that the nearest NavigationHandle has a NavigationKey of the requested type.\n\n" +
            "This is not always an error, as there may be higher-level program logic that ensures this will succeed, " +
            "but it is important to understand that this works in essentially the same way as an unchecked cast. " +
            "If you do not need a TypedNavigationHandle, you can request an untyped NavigationHandle by removing the type" +
            "arguments provided to the `navigationHandle` function",
    category = Category.PRODUCTIVITY,
    priority = 5,
    severity = Severity.WARNING,
    implementation = Implementation(EnroIssueDetector::class.java, Scope.JAVA_FILE_SCOPE)
)