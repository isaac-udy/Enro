package dev.enro.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
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
                val returnType = node.returnType as? PsiClassType ?: return
                if (!navigationHandlePropertyType.isAssignableFrom(returnType)) return

                val navigationHandleGenericType = returnType.parameters.first()

                val containingClass = node.getContainingUClass() ?: return
                val navigationDestinationType = containingClass
                    .getAnnotation("dev.enro.annotations.NavigationDestination")
                    ?.findAttributeValue("key")
                    .toUElementOfType<UClassLiteralExpression>()
                    ?.type

                if(navigationDestinationType == null) {
                    val classSource = containingClass.sourceElement?.text
                    context.report(
                        issue = missingNavigationDestinationAnnotation,
                        location = context.getLocation(node),
                        message = "${containingClass.name} is not a NavigationDestination",
                        quickfixData = fix()
                            .name("Add NavigationDestination for ${navigationHandleGenericType.presentableText} to ${containingClass.name}")
                            .replace()
                            .range(context.getLocation(element = node.getContainingUFile()!!))
                            .text("$classSource")
                            .with("@NavigationDestination(${navigationHandleGenericType.presentableText}::class)\n$classSource")
                            .build()
                    )
                    return
                }

                if (!navigationHandleGenericType.isAssignableFrom(navigationDestinationType)) {
                    context.report(
                        issue = incorrectlyTypedNavigationHandle,
                        location = context.getLocation(node),
                        message = "${containingClass.name} expects a NavigationKey of type '${navigationDestinationType.presentableText}', which cannot be cast to '${navigationHandleGenericType.presentableText}'",
                        quickfixData = fix()
                            .name("Change type to ${navigationDestinationType.presentableText}")
                            .replace()
                            .text(navigationHandleGenericType.presentableText)
                            .with(navigationDestinationType.canonicalText)
                            .shortenNames()
                            .build()
                    )
                }
            }
        }
    }
}