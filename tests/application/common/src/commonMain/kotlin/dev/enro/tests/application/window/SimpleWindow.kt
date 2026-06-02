package dev.enro.tests.application.window

import dev.enro.NavigationKey
import dev.enro.annotations.NavigationPath
import kotlinx.serialization.Serializable

@Serializable
@NavigationPath("/simple-window")
object SimpleWindow : NavigationKey
