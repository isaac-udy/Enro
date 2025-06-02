package dev.enro3.result.flow

import dev.enro.annotations.ExperimentalEnroApi
import kotlinx.serialization.Serializable

/**
 * NavigationFlowReference is a reference to a NavigationFlow, and is available in NavigationFlowScope when building a
 * NavigationFlow. It can be passed to a NavigationKey to allow the screen that the NavigationKey represents to interact
 * with the navigation flow and perform actions such as returning to previous steps within the flow to edit items.
 */
@Serializable
@ExperimentalEnroApi
public class NavigationFlowReference internal constructor(
    internal val id: String,
)