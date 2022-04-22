package dev.enro.core.controller

import dev.enro.core.activity.createActivityNavigator
import dev.enro.core.compose.ComposeFragmentHost
import dev.enro.core.compose.ComposeFragmentHostKey
import dev.enro.core.compose.HiltComposeFragmentHost
import dev.enro.core.compose.HiltComposeFragmentHostKey
import dev.enro.core.compose.dialog.ComposeDialogFragmentHost
import dev.enro.core.compose.dialog.ComposeDialogFragmentHostKey
import dev.enro.core.compose.dialog.HiltComposeDialogFragmentHost
import dev.enro.core.compose.dialog.HiltComposeDialogFragmentHostKey
import dev.enro.core.controller.interceptor.HiltInstructionInterceptor
import dev.enro.core.controller.interceptor.ExecutorContextInterceptor
import dev.enro.core.controller.interceptor.PreviouslyActiveInterceptor
import dev.enro.core.fragment.createFragmentNavigator
import dev.enro.core.fragment.internal.HiltSingleFragmentActivity
import dev.enro.core.fragment.internal.HiltSingleFragmentKey
import dev.enro.core.fragment.internal.SingleFragmentActivity
import dev.enro.core.fragment.internal.SingleFragmentKey
import dev.enro.core.internal.NoKeyNavigator
import dev.enro.core.result.EnroResult

internal val defaultComponent = createNavigationComponent {
    plugin(EnroResult())

    interceptor(ExecutorContextInterceptor())
    interceptor(PreviouslyActiveInterceptor())
    interceptor(HiltInstructionInterceptor())

    navigator(createActivityNavigator<SingleFragmentKey, SingleFragmentActivity>())
    navigator(NoKeyNavigator())
    navigator(createFragmentNavigator<ComposeFragmentHostKey, ComposeFragmentHost>())
    navigator(createFragmentNavigator<ComposeDialogFragmentHostKey, ComposeDialogFragmentHost>())

    // These Hilt based navigators will fail to be created if Hilt is not on the class path,
    // which is acceptable/allowed, so we'll attempt to add them, but not worry if they fail to be added
    runCatching {
        navigator(createActivityNavigator<HiltSingleFragmentKey, HiltSingleFragmentActivity>())
    }

    runCatching {
        navigator(createFragmentNavigator<HiltComposeFragmentHostKey, HiltComposeFragmentHost>())
    }

    runCatching {
        navigator(createFragmentNavigator<HiltComposeDialogFragmentHostKey, HiltComposeDialogFragmentHost>())
    }
}