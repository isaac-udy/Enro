package dev.enro.processor.generator

import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.getElementName
import dev.enro.processor.extensions.kotlinReceiverTypes
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.tools.StandardLocation

object ComposableWrapperGenerator {
    fun generate(
        processingEnv: ProcessingEnvironment,
        element: ExecutableElement,
        keyType: Element
    ): String {
        val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
        val composableWrapperName =
            element.getElementName(processingEnv).split(".").last() + "Destination"

        val receiverTypes = element.kotlinReceiverTypes(processingEnv)
        val additionalInterfaces = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.destination.compose.dialog.DialogDestination" -> "DialogDestination"
                "dev.enro.destination.compose.dialog.BottomSheetDestination" -> "BottomSheetDestination"
                else -> null
            }
        }.joinToString(separator = "") { ", $it" }

        val typeParameter = if (element.typeParameters.isEmpty()) "" else "<$composableWrapperName>"

        val additionalImports = receiverTypes.flatMap {
            when (it) {
                "dev.enro.destination.compose.dialog.DialogDestination" -> listOf(
                    "dev.enro.destination.compose.dialog.DialogDestination",
                    "dev.enro.destination.compose.dialog.DialogConfiguration"
                )
                "dev.enro.destination.compose.dialog.BottomSheetDestination" -> listOf(
                    "dev.enro.destination.compose.dialog.BottomSheetDestination",
                    "dev.enro.destination.compose.dialog.BottomSheetConfiguration",
                    "androidx.compose.material.ExperimentalMaterialApi"
                )
                else -> emptyList()
            }
        }.joinToString(separator = "") { "\n                import $it" }

        val additionalAnnotations = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.destination.compose.dialog.BottomSheetDestination" ->
                    """
                        @OptIn(ExperimentalMaterialApi::class)
                    """.trimIndent()
                else -> null
            }
        }.joinToString(separator = "") { "\n                  $it" }

        val additionalBody = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.destination.compose.dialog.DialogDestination" ->
                    """
                        override val dialogConfiguration: DialogConfiguration = DialogConfiguration()
                    """.trimIndent()
                "dev.enro.destination.compose.dialog.BottomSheetDestination" ->
                    """
                        override val bottomSheetConfiguration: BottomSheetConfiguration = BottomSheetConfiguration()
                    """.trimIndent()
                else -> null
            }
        }.joinToString(separator = "") { "\n                    $it" }

        processingEnv.filer
            .createResource(
                StandardLocation.SOURCE_OUTPUT,
                EnroLocation.GENERATED_PACKAGE,
                "$composableWrapperName.kt",
                element
            )
            .openWriter()
            .append(
                """
                package $packageName
                
                import androidx.compose.runtime.Composable
                import dev.enro.annotations.NavigationDestination
                $additionalImports
                
                import ${element.getElementName(processingEnv)}
                import ${ClassNames.Java.composableDestination}
                import ${keyType.getElementName(processingEnv)}
                
                $additionalAnnotations
                public class $composableWrapperName : ComposableDestination()$additionalInterfaces {
                    $additionalBody
                    
                    @Composable
                    override fun Render() {
                        ${element.simpleName}$typeParameter()
                    }
                }
                """.trimIndent()
            )
            .close()

        return "$packageName.$composableWrapperName"
    }
}