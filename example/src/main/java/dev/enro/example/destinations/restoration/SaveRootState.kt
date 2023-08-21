package dev.enro.example.destinations.restoration

import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.push
import dev.enro.core.container.setBackstack
import dev.enro.core.requireRootContainer
import dev.enro.destination.synthetic.syntheticDestination
import kotlinx.parcelize.Parcelize

@Parcelize
class SaveRootState : NavigationKey.SupportsPresent

@NavigationDestination(SaveRootState::class)
val saveRootState = syntheticDestination<SaveRootState> {
    val root = navigationContext.requireRootContainer()
    val savedState = root.save()
    root.setBackstack { emptyBackstack().push(WaitForRestoration(savedState)) }
}