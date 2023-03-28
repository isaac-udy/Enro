package dev.enro.example

import android.os.Bundle
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.container.emptyBackstack
import dev.enro.core.container.push
import dev.enro.core.container.setBackstack
import dev.enro.core.requireRootContainer
import dev.enro.core.synthetic.syntheticDestination
import kotlinx.parcelize.Parcelize

@Parcelize
class SaveRootState : NavigationKey.SupportsPresent

@NavigationDestination(SaveRootState::class)
val saveRootState = syntheticDestination<SaveRootState> {
    val root = navigationContext.requireRootContainer()
    val savedState = root.save()
    root.setBackstack { emptyBackstack().push(ExampleLockedScreenKey(savedState)) }
}


@Parcelize
data class RestoreRootState(val state: Bundle) : NavigationKey.SupportsPresent

@NavigationDestination(RestoreRootState::class)
val restoreRootState = syntheticDestination<RestoreRootState> {
    navigationContext
        .requireRootContainer()
        .restore(key.state)
}