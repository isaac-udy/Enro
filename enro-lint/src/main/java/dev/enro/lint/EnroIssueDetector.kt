package dev.enro.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiJvmModifiersOwner
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClassLiteralExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.getContainingUFile
import org.jetbrains.uast.toUElementOfType

@Suppress("UnstableApiUsage")
class EnroIssueDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UCallExpression::class.java, UMethod::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        fun PsiJvmModifiersOwner.getNavigationDestinationType(): PsiType? {
            return getAnnotation("dev.enro.annotations.NavigationDestination")
                ?.findAttributeValue("key")
                .toUElementOfType<UClassLiteralExpression>()
                ?.type
        }


        val navigationHandlePropertyType = PsiType.getTypeByName(
            "dev.enro.core.NavigationHandleProperty",
            context.project.ideaProject,
            GlobalSearchScope.allScope(context.project.ideaProject)
        )

        fun visitNavigationHandlePropertyCall(node: UCallExpression) {
            val returnType = node.returnType as? PsiClassType ?: return
            if (!navigationHandlePropertyType.isAssignableFrom(returnType)) return

            val navigationHandleGenericType = returnType.parameters.first()

            val receiverClass = PsiUtil.resolveClassInType(node.receiverType) ?: return
            val navigationDestinationType = receiverClass.getNavigationDestinationType()

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

        val viewModelNavigationHandlePropertyType = PsiType.getTypeByName(
            "dev.enro.viewmodel.NavigationHandleProperty",
            context.project.ideaProject,
            GlobalSearchScope.allScope(context.project.ideaProject)
        )

        val typedNavigationHandleType = PsiType.getTypeByName(
            "dev.enro.core.TypedNavigationHandle",
            context.project.ideaProject,
            GlobalSearchScope.allScope(context.project.ideaProject)
        )

        val navigationKeyType = PsiType.getTypeByName(
            "dev.enro.core.NavigationKey",
            context.project.ideaProject,
            GlobalSearchScope.allScope(context.project.ideaProject)
        )

        fun getComposableFunctionParent(node: UElement): UMethod? {
            val parent = node.uastParent ?: return null
            if (parent !is UMethod) {
                return getComposableFunctionParent(parent)
            }
            parent.getAnnotation("androidx.compose.runtime.Composable")
                ?: return getComposableFunctionParent(parent)

            return parent
        }

        fun visitComposableNavigationHandleCall(node: UCallExpression) {
            val composableParent = getComposableFunctionParent(node) ?: return

            val returnType = node.returnType as? PsiClassType ?: return
            if (!typedNavigationHandleType.isAssignableFrom(returnType)) return

            val navigationHandleGenericType = returnType.parameters.first()
            val navigationDestinationType = composableParent.getNavigationDestinationType()

            if (navigationDestinationType == null) {
                // allow references like navigationHandle<NavigationKey> because these aren't dangerous
                if (navigationHandleGenericType == navigationKeyType) return

                val functionSource = composableParent.sourceElement?.text
                context.report(
                    issue = missingNavigationDestinationAnnotation,
                    location = context.getLocation(node),
                    message = "${composableParent.name} is not marked as a NavigationDestination.\nRequesting a TypedNavigationHandle in this way may be cause a crash if ${composableParent.name} is called from a NavigationDestination that is not bound to '${navigationHandleGenericType.presentableText}'",
                    quickfixData = fix()
                        .name("Add NavigationDestination to ${composableParent.name}")
                        .replace()
                        .range(context.getLocation(element = composableParent))
                        .text("$functionSource")
                        .with("@dev.enro.annotations.NavigationDestination(${navigationHandleGenericType.presentableText}::class)\n$functionSource")
                        .shortenNames()
                        .build()
                )
                return
            }

            if (!navigationHandleGenericType.isAssignableFrom(navigationDestinationType)) {
                context.report(
                    issue = incorrectlyTypedNavigationHandle,
                    location = context.getLocation(node),
                    message = "${composableParent.name} expects a NavigationKey of type '${navigationDestinationType.presentableText}', which cannot be cast to '${navigationHandleGenericType.presentableText}'",
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

        return object : UElementHandler() {

            override fun visitMethod(node: UMethod) {}

            override fun visitCallExpression(node: UCallExpression) {
                visitNavigationHandlePropertyCall(node)
                visitComposableNavigationHandleCall(node)
            }
        }
    }
}