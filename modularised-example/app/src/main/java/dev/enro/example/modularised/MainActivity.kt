package dev.enro.example.modularised

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.synthetic.SyntheticDestination
import dev.enro.example.core.data.UserRepository
import dev.enro.example.core.navigation.*
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
class MainKey : NavigationKey

@HiltViewModel
class ExampleHiltViewModel @Inject constructor(): ViewModel()

@AndroidEntryPoint
@NavigationDestination(MainKey::class)
class MainActivity : AppCompatActivity() {
    private val hiltViewModel by viewModels<ExampleHiltViewModel>()
    private val navigation by navigationHandle<MainKey> {
        defaultKey(MainKey())
    }

    override fun onResume() {
        super.onResume()
        findViewById<View>(android.R.id.content)
            .animate()
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    navigation.replace(LaunchKey)
                }
            })
            .start()
    }

    override fun onPause() {
        super.onPause()
        findViewById<View>(android.R.id.content)
            .animate()
            .apply {
                start()
            }
            .cancel()
    }
}

@NavigationDestination(LaunchKey::class)
class LaunchDestination : SyntheticDestination<LaunchKey> {
    override fun process(
        navigationContext: NavigationContext<out Any>,
        key: LaunchKey,
        instruction: NavigationInstruction.Open
    ) {
        val navigation = navigationContext.activity.getNavigationHandle()
        val userRepo = UserRepository.instance
        val nextKey = when (val user = userRepo.activeUser) {
            null -> LoginKey()
            else -> DashboardKey(user)
        }
        navigation.replaceRoot(nextKey)
    }
}