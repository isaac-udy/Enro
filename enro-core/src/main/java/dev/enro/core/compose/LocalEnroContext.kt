package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.internal.handle.getNavigationHandleViewModel

internal val localEnroContext @Composable get() = LocalViewModelStoreOwner.current!!.getNavigationHandleViewModel().navigationContext!!