package dev.enro.tests.application.window

import dev.enro.annotations.NavigationPath
import dev.enro.core.NavigationKey
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/simple-window")
object SimpleWindow : NavigationKey.SupportsPresent
