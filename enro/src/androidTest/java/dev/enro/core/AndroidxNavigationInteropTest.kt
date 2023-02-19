package dev.enro.core

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.TestFragment
import dev.enro.expectFragment
import dev.enro.expectNoActivity
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class AndroidxNavigationInteropTest {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenMultipleAndroidxNavigationFragments_whenBackButtonIsPressed_thenAndroidxNavigationReceivesBackButtonPress() {
        val scenario = ActivityScenario.launch(JetpackNavigationActivity::class.java)

        expectFragment<JetpackNavigationFragment> {
            it.navigationArgument == 0
        }.openNext(scenario)

        expectFragment<JetpackNavigationFragment> {
            it.navigationArgument == 1
        }.openNext(scenario)

        expectFragment<JetpackNavigationFragment> {
            it.navigationArgument == 2
        }

        Espresso.pressBack()
        expectFragment<JetpackNavigationFragment> {
            it.navigationArgument == 1
        }

        Espresso.pressBack()
        expectFragment<JetpackNavigationFragment> {
            it.navigationArgument == 0
        }
    }

    @Test
    fun givenSingleAndroidxNavigationFragment_whenNavigationBackButtonIsPressed_thenActivityIsClosed() {
        val scenario = ActivityScenario.launch(JetpackNavigationActivity::class.java)
        expectFragment<JetpackNavigationFragment> {
            it.navigationArgument == 0
        }
        scenario.onActivity { it.onBackPressed() }
        expectNoActivity()
    }

    @Test
    fun givenActivityIsLaunched_andFragmentHasCustomBackNavigation_whenBackButtonIsPressed_thenCustomNavigationIsExecuted() {
        val scenario = ActivityScenario.launch<JetpackNavigationActivity>(
            Intent(InstrumentationRegistry.getInstrumentation().context, JetpackNavigationActivity::class.java).apply {
                putExtra("shouldRegisterBackNavigation", true)
            }
        )
        expectFragment<JetpackNavigationFragment> {
            it.navigationArgument == 0
        }
        Espresso.pressBack()
        expectFragment<JetpackNavigationFragment> {
            it.executedCustomBackPressed
        }
    }
}

internal class JetpackNavigationActivity : AppCompatActivity() {
    val shouldRegisterBackNavigation by lazy {
        intent.getBooleanExtra("shouldRegisterBackNavigation", false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(dev.enro.test.R.layout.jetpack_navigation_activity_layout)
    }
}

internal class JetpackNavigationFragment : TestFragment() {
    val navigationArgument by lazy {
        requireArguments().getInt("argument", 0)
    }

    var executedCustomBackPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity() as JetpackNavigationActivity
        if (activity.shouldRegisterBackNavigation && navigationArgument == 0) {
            activity.onBackPressedDispatcher.addCallback(this) {
                executedCustomBackPressed = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val layout = requireView() as LinearLayout
        val title = layout.children.first() as TextView
        title.text = "Jetpack Navigation $navigationArgument"
    }

    fun openNext(activityScenario: ActivityScenario<*>) {
        activityScenario.onActivity {
            findNavController().navigate(
                dev.enro.test.R.id.JetpackNavigationFragment,
                bundleOf("argument" to navigationArgument + 1)
            )
        }
    }
}