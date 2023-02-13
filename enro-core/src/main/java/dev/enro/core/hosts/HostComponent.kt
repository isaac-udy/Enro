package dev.enro.core.hosts

import dev.enro.core.activity.createActivityNavigationBinding
import dev.enro.core.controller.createNavigationComponent
import dev.enro.core.fragment.createFragmentNavigationBinding
import dev.enro.core.navigationHostFactory

internal val hostComponent = createNavigationComponent {
    navigationHostFactory(ActivityHost())
    navigationHostFactory(FragmentHost())
    navigationHostFactory(DialogFragmentHost())

    binding(createActivityNavigationBinding<OpenInstructionInActivity, ActivityHostForAnyInstruction>())
    binding(createFragmentNavigationBinding<OpenComposableInFragment, FragmentHostForComposable>())
    binding(createFragmentNavigationBinding<OpenPresentableFragmentInFragment, FragmentHostForPresentableFragment>())

    // These Hilt based navigation bindings will fail to be created if Hilt is not on the class path,
    // which is acceptable/allowed, so we'll attempt to add them, but not worry if they fail to be added
    runCatching {
        binding(createActivityNavigationBinding<OpenInstructionInHiltActivity, HiltActivityHostForAnyInstruction>())
    }

    runCatching {
        binding(createFragmentNavigationBinding<OpenComposableInHiltFragment, HiltFragmentHostForComposable>())
    }

    runCatching {
        binding(createFragmentNavigationBinding<OpenPresentableFragmentInHiltFragment, HiltFragmentHostForPresentableFragment>())
    }
}