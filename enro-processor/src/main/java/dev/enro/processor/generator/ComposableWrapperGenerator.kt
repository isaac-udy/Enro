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
                "dev.enro.core.compose.dialog.DialogDestination" -> "DialogDestination"
                "dev.enro.core.compose.dialog.BottomSheetDestination" -> "BottomSheetDestination"
                else -> null
            }
        }.plus("EnroGeneratedClassMarker")
            .joinToString(separator = "") { ", $it" }

        val typeParameter = if (element.typeParameters.isEmpty()) "" else "<$composableWrapperName>"

        val additionalImports = receiverTypes.flatMap {
            when (it) {
                "dev.enro.core.compose.dialog.DialogDestination" -> listOf(
                    "dev.enro.core.compose.dialog.DialogDestination",
                    "dev.enro.core.compose.dialog.DialogConfiguration"
                )
                "dev.enro.core.compose.dialog.BottomSheetDestination" -> listOf(
                    "dev.enro.core.compose.dialog.BottomSheetDestination",
                    "dev.enro.core.compose.dialog.BottomSheetConfiguration",
                    "androidx.compose.material.ExperimentalMaterialApi"
                )
                else -> emptyList()
            }
        }.plus("dev.enro.core.EnroGeneratedClassMarker")
            .joinToString(separator = "") { "\n                import $it" }

        val additionalAnnotations = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.core.compose.dialog.BottomSheetDestination" ->
                    """
                        @OptIn(ExperimentalMaterialApi::class)
                    """.trimIndent()
                else -> null
            }
        }.joinToString(separator = "") { "\n                  $it" }

        val additionalBody = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.core.compose.dialog.DialogDestination" ->
                    """
                        override val dialogConfiguration: DialogConfiguration = DialogConfiguration()
                    """.trimIndent()
                "dev.enro.core.compose.dialog.BottomSheetDestination" ->
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