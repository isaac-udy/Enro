package dev.enro.example

import android.app.Application
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.CompositionLocalProvider
import dagger.hilt.android.HiltAndroidApp
import dev.enro.annotations.NavigationComponent
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.plugins.EnroLogger

@HiltAndroidApp
@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = installNavigationController(this) {
        plugin(EnroLogger())
        interceptor(ExampleInterceptor)
        composeEnvironment { content ->
            EnroExampleTheme {
                CompositionLocalProvider(
                    LocalContentColor provides contentColorFor(MaterialTheme.colors.surface),
                ) {
                    content()
                }
            }
        }
    }
}
