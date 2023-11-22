// comment the line below to test linting errors
@file:SuppressLint("IncorrectlyTypedNavigationHandle", "MissingNavigationDestinationAnnotation")
package dev.enro.tests.application.linting

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
internal class IncorrectlyTypedNavigationKey : NavigationKey.SupportsPush

@Parcelize
internal class ActivityCorrectNavigationKey : NavigationKey.SupportsPush

@NavigationDestination(ActivityCorrectNavigationKey::class)
internal class ActivityWithIncorrectlyTypedNavigationHandle : AppCompatActivity() {
    val navigationHandle by navigationHandle<IncorrectlyTypedNavigationKey>()
}

@Parcelize
internal class FragmentCorrectNavigationKey : NavigationKey.SupportsPush

@NavigationDestination(FragmentCorrectNavigationKey::class)
internal class FragmentWithIncorrectlyTypedNavigationHandle : Fragment() {
    val navigationHandle by navigationHandle<IncorrectlyTypedNavigationKey>()
}

@Parcelize
internal class ComposableCorrectNavigationKey : NavigationKey.SupportsPush

@Composable
@NavigationDestination(ComposableCorrectNavigationKey::class)
fun ComposableWithIncorrectlyTypedNavigationHandle() {
    // The line below should have a linting error:
    val navigationHandle = navigationHandle<IncorrectlyTypedNavigationKey>()
}

@Parcelize
internal class MissingNavigationKey : NavigationKey.SupportsPush

internal class ActivityWithMissingNavigationDestination : AppCompatActivity() {
    val navigationHandle by navigationHandle<MissingNavigationKey>()
}


internal class FragmentWithMissingNavigationDestination : Fragment() {
    val navigationHandle by navigationHandle<MissingNavigationKey>()
}

@Composable
fun ComposableWithMissingNavigationDestination() {
    // The line below should have a linting error:
    val navigationHandle = navigationHandle<MissingNavigationKey>()
}
