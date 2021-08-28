package dev.enro.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import org.jetbrains.uast.*

@Suppress("UnstableApiUsage")
class EnroIssueDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UCallExpression::class.java, UMethod::class.java)
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

            override fun visitMethod(node: UMethod) {
                val isComposable = node.hasAnnotation("androidx.compose.runtime.Composable")

                val isNavigationDestination =
                    node.hasAnnotation("dev.enro.annotations.NavigationDestination")

                val isExperimentalComposableDestinationsEnabled =
                    node.hasAnnotation("dev.enro.annotations.ExperimentalComposableDestination")

                if (isComposable && isNavigationDestination && !isExperimentalComposableDestinationsEnabled) {
                    val annotationLocation =  context.getLocation(element = node.findAnnotation("dev.enro.annotations.NavigationDestination")!!)
                    context.report(
                        issue = missingExperimentalComposableDestinationOptIn,
                        scopeClass = node,
                        location = annotationLocation,
                        message = missingExperimentalComposableDestinationOptIn.getExplanation(
                            TextFormat.TEXT
                        ),
                        quickfixData = fix()
                            .name("Add @NavigationDestination annotation")
                            .replace()
                            .range(annotationLocation)
                            .text("")
                            .with("@dev.enro.annotations.ExperimentalComposableDestination\n")
                            .shortenNames()
                            .build()
                    )
                }
            }

            override fun visitCallExpression(node: UCallExpression) {
                val returnType = node.returnType as? PsiClassType ?: return
                if (!navigationHandlePropertyType.isAssignableFrom(returnType)) return

                val navigationHandleGenericType = returnType.parameters.first()

                val receiverClass = PsiUtil.resolveClassInType(node.receiverType) ?: return
                val navigationDestinationType = receiverClass
                    .getAnnotation("dev.enro.annotations.NavigationDestination")
                    ?.findAttributeValue("key")
                    .toUElementOfType<UClassLiteralExpression>()
                    ?.type

                if (navigationDestinationType == null) {
                    val classSource = receiverClass.sourceElement?.text
                    context.report(
                        issue = missingNavigationDestinationAnnotation,
                        location = context.getLocation(node),
                        message = "${receiverClass.name} is not a NavigationDestination",
                        quickfixData = fix()
                            .name("Add NavigationDestination for ${navigationHandleGenericType.presentableText} to ${receiverClass.name}")
                            .replace()
                            .range(context.getLocation(element = node.getContainingUFile()!!))
                            .text("$classSource")
                            .with("@dev.enro.annotations.NavigationDestination(${navigationHandleGenericType.presentableText}::class)\n$classSource")
                            .shortenNames()
                            .build()
                    )
                    return
                }

                if (!navigationHandleGenericType.isAssignableFrom(navigationDestinationType)) {
                    context.report(
                        issue = incorrectlyTypedNavigationHandle,
                        location = context.getLocation(node),
                        message = "${receiverClass.name} expects a NavigationKey of type '${navigationDestinationType.presentableText}', which cannot be cast to '${navigationHandleGenericType.presentableText}'",
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