package dev.enro.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.uast.*

@Suppress("UnstableApiUsage")
class EnroIssueDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {

        val navigationHandlePropertyType = PsiType.getTypeByName(
            "dev.enro.core.NavigationHandleProperty",
            context.project.ideaProject,
            GlobalSearchScope.allScope(context.project.ideaProject)
        )

        val viewModelNavigationHandlePropertyType = PsiType.getTypeByName(
            "dev.enro.viewmodel.NavigationHandleProperty",
            context.project.ideaProject,
            GlobalSearchScope.allScope(context.project.ideaProject)
        )

        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                val returnType = node.returnType?.cast<PsiClassType>() ?: return
                if (!returnType.isAssignableFrom(navigationHandlePropertyType)) return

                val navigationHandleGenericType = returnType.parameters.first()

                val navigationDestinationType = node.getContainingUClass()
                    ?.getAnnotation("dev.enro.annotations.NavigationDestination")
                    ?.findAttributeValue("key")
                    .toUElementOfType<UClassLiteralExpression>()
                    ?.type

                if(navigationDestinationType == null) {
                    context.report(
                        issue = missingNavigationDestinationAnnotation,
                        location = context.getLocation(node),
                        message = "${node.getContainingUClass()?.name} is not marked as a NavigationDestination"
                    )
                    return
                }

                if (!navigationHandleGenericType.isAssignableFrom(navigationDestinationType)) {
                    context.report(
                        issue = incorrectlyTypedNavigationHandle,
                        location = context.getLocation(node),
                        message = "NavigationDestination defined for this class is of type ${navigationDestinationType.presentableText}, which is not assignable to ${navigationHandleGenericType.presentableText}",
                        quickfixData = fix()
                            .name("Change type to ${navigationDestinationType.presentableText}")
                            .replace()
                            .text(navigationHandleGenericType.presentableText)
                            .with(navigationDestinationType.presentableText)
                            .build()
                    )
                }
            }
        }
    }
}