package dev.enro.core.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.activity
import dev.enro.core.addOpenInstruction
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.backstackOf
import dev.enro.core.container.components.ContainerAcceptPolicy
import dev.enro.core.container.components.ContainerContextProvider
import dev.enro.core.container.components.ContainerEmptyPolicy
import dev.enro.core.container.components.ContainerRenderer
import dev.enro.core.container.components.ContainerState
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationBinding
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.getNavigationHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class ActivityNavigationContainer internal constructor(
    activityContext: NavigationContext<out ComponentActivity>,
) : NavigationContainer(
    key = NavigationContainerKey.FromName("ActivityNavigationContainer"),
    context = activityContext,
    interceptor = { },
    animations = { },
    initialBackstack = backstackOf(activityContext.getNavigationHandle().instruction),
    acceptPolicy = ContainerAcceptPolicy.Default(
        context = activityContext,
        acceptsContextType = Fragment::class,
        acceptsNavigationKey = { true },
    ),
    emptyPolicy = ContainerEmptyPolicy.Default(
        context = activityContext,
        emptyBehavior = EmptyBehavior.AllowEmpty,
    ),
    containerRenderer = object : ContainerRenderer, Component {
        override val isVisible: Boolean
            get() = true
        private var renderJob: Job? = null

        override fun create(state: ContainerState) {
            fun onBackstackUpdated(transition: NavigationBackstackTransition): Boolean {
                val rootInstruction = activityContext.getNavigationHandle().instruction
                if (transition.activeBackstack.singleOrNull()?.instructionId == rootInstruction.instructionId) return true
                state.setBackstack(backstackOf(rootInstruction))

                val activeInstructionIsPresent =
                    transition.activeBackstack.any { it.instructionId == rootInstruction.instructionId }
                if (!activeInstructionIsPresent) {
                    Log.e("Render", "FINISHACTIVITY ${rootInstruction.navigationKey::class.java.name}")
                    ActivityCompat.finishAfterTransition(activityContext.activity)
//                    val animations = getNavigationAnimations.closing(
//                        exiting = rootInstruction,
//                        entering = transition.activeBackstack.active,
//                    )
//                    activityContext.activity.overridePendingTransition(
//                        animations.entering.asResource(childContext.activity.theme).id,
//                        animations.exiting.asResource(childContext.activity.theme).id
//                    )
                }

                val instructionToOpen = transition.activeBackstack
                    .filter { it.instructionId != rootInstruction.instructionId }
                    .also {
                        require(it.size <= 2) { transition.activeBackstack.joinToString { it.navigationKey.toString() } }
                    }
                    .firstOrNull() ?: return true

                val instructionToOpenHosted =
                    activityContext.controller.dependencyScope.get<HostInstructionAs>()
                        .invoke<Activity>(
                            activityContext,
                            instructionToOpen
                        )
                val binding = requireNotNull(
                    activityContext.controller.dependencyScope.get<GetNavigationBinding>()
                        .invoke(instructionToOpenHosted)
                ) { "Could not open ${instructionToOpenHosted.navigationKey::class.java.simpleName}: No NavigationBinding was found" }

                val intent = Intent(activityContext.activity, binding.destinationType.java)
                    .addOpenInstruction(instructionToOpenHosted)

                if (instructionToOpen.navigationDirection == NavigationDirection.ReplaceRoot) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }

                val activity = activityContext.activity

//                val animations = getNavigationAnimations.opening(
//                    exiting = rootInstruction,
//                    entering = instructionToOpenHosted
//                )
//
//                val options = ActivityOptionsCompat.makeCustomAnimation(
//                    activity,
//                    animations.entering.asResource(activityContext.activity.theme).id,
//                    animations.exiting.asResource(activityContext.activity.theme).id
//                )
                activity.startActivity(intent)

                return true
            }
            if (renderJob != null) error("FragmentContainerRenderer is already bound")
            renderJob = activityContext.lifecycleOwner.lifecycleScope.launch {
                activityContext.lifecycle.withCreated {}
                state.backstackFlow.collectLatest {
                    val transition = NavigationBackstackTransition(it to it)
                    while (!onBackstackUpdated(transition) && isActive) {
                        delay(16)
                    }
                }
            }
        }

        override fun restore(bundle: Bundle) {
            super.restore(bundle)
        }

        override fun destroy() {
            renderJob?.cancel()
            renderJob = null
        }
    },
    containerContextProvider = object : ContainerContextProvider<ComponentActivity> {
        override fun getActiveNavigationContext(backstack: NavigationBackstack): NavigationContext<out ComponentActivity>? {
            return activityContext
        }

        override fun getContext(instruction: AnyOpenInstruction): ComponentActivity? {
            TODO("Not yet implemented")
        }

        override fun createContext(instruction: AnyOpenInstruction): ComponentActivity {
            TODO("Not yet implemented")
        }
    }
) {
//    override val childContext: NavigationContext<*>
//        get() = context
//
//    override val isVisible: Boolean
//        get() = context.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
//
//    private val rootInstruction: AnyOpenInstruction
//        get() = childContext.getNavigationHandle().instruction
//
//    init {
//        setBackstack(backstackOf(rootInstruction))
//    }
//
//    private val getNavigationAnimations = dependencyScope.get<GetNavigationAnimations>()
//
//    override
}