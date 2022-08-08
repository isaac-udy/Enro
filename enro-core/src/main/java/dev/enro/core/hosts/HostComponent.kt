package dev.enro.core.hosts

import dev.enro.core.activity.createActivityNavigator
import dev.enro.core.controller.createNavigationComponent
import dev.enro.core.fragment.createFragmentNavigator

internal val hostComponent = createNavigationComponent {
    navigator(createActivityNavigator<OpenInstructionInActivity, ActivityHostForAnyInstruction>())
    navigator(createFragmentNavigator<OpenComposableInFragment, FragmentHostForComposable>())
    navigator(createFragmentNavigator<OpenComposableDialogInFragment, FragmentHostForComposableDialog>())
    navigator(createFragmentNavigator<OpenPresentableFragmentInFragment, FragmentHostForPresentableFragment>())

    // These Hilt based navigators will fail to be created if Hilt is not on the class path,
    // which is acceptable/allowed, so we'll attempt to add them, but not worry if they fail to be added
    runCatching {
        navigator(createActivityNavigator<OpenInstructionInHiltActivity, HiltActivityHostForAnyInstruction>())
    }

    runCatching {
        navigator(createFragmentNavigator<OpenComposableInHiltFragment, HiltFragmentHostForComposable>())
    }

    runCatching {
        navigator(createFragmentNavigator<OpenComposableDialogInHiltFragment, HiltFragmentHostForComposableDialog>())
    }

    runCatching {
        navigator(createFragmentNavigator<OpenPresentableFragmentInHiltFragment, HiltFragmentHostForPresentableFragment>())
    }
}