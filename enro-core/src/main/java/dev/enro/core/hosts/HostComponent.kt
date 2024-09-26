package dev.enro.core.hosts

import dev.enro.core.activity.createActivityNavigationBinding
import dev.enro.core.controller.createNavigationModule
import dev.enro.core.fragment.createFragmentNavigationBinding
import dev.enro.destination.flow.host.FragmentHostForManagedFlow
import dev.enro.destination.flow.host.HiltFragmentHostForManagedFlow
import dev.enro.destination.flow.host.OpenManagedFlowInFragment
import dev.enro.destination.flow.host.OpenManagedFlowInHiltFragment

internal val hostNavigationModule = createNavigationModule {
    navigationHostFactory(ActivityHost())
    navigationHostFactory(FragmentHost())
    navigationHostFactory(DialogFragmentHost())
    navigationHostFactory(ComposableHost())

    binding(createActivityNavigationBinding<OpenInstructionInActivity, ActivityHostForAnyInstruction>())
    binding(createFragmentNavigationBinding<OpenComposableInFragment, FragmentHostForComposable>())
    binding(createFragmentNavigationBinding<OpenPresentableFragmentInFragment, FragmentHostForPresentableFragment>())
    binding(createFragmentNavigationBinding<OpenManagedFlowInFragment, FragmentHostForManagedFlow>())

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

    runCatching {
        binding(createFragmentNavigationBinding<OpenManagedFlowInHiltFragment, HiltFragmentHostForManagedFlow>())
    }
}