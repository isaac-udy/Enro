package dev.enro.tests.application.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import dev.enro.core.present
import dev.enro.core.result.registerForNavigationResult
import dev.enro.tests.application.R
import dev.enro.tests.application.activity.applyInsetsForContentView
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.*

/**
 * This module implements the presentation tests for Fragments. It tests several scenarios:
 * - Present from Fragment to Composable
 * - Present from Fragment to another Fragment
 * - Present from Fragment to Activity
 * - Testing result delivery for all these cases
 */
@Serializable
object FragmentPresentation : NavigationKey.SupportsPresent {
    @Parcelize
    @Serializable
    object Root : Parcelable, NavigationKey.SupportsPush, NavigationKey.SupportsPresent

    @Parcelize
    @Serializable
    data class TestResult(val id: String = UUID.randomUUID().toString()) : Parcelable

    @Parcelize
    @Serializable
    object PresentableComposable : Parcelable, NavigationKey.SupportsPresent.WithResult<TestResult>

    @Parcelize
    @Serializable
    object PresentableFragment : Parcelable, NavigationKey.SupportsPresent.WithResult<TestResult>

    @Parcelize
    @Serializable
    object PresentableActivity : Parcelable, NavigationKey.SupportsPresent.WithResult<TestResult>
    
    @Parcelize
    @Serializable
    object PresentableDialogComposable : Parcelable, NavigationKey.SupportsPresent.WithResult<TestResult>
    
    @Parcelize
    @Serializable
    object PresentableDialogFragment : Parcelable, NavigationKey.SupportsPresent.WithResult<TestResult>
}

@NavigationDestination(FragmentPresentation::class)
class FragmentPresentationActivity : androidx.appcompat.app.AppCompatActivity() {
    private val container by navigationContainer(
        containerId = R.id.fragment_container,
        root = {
            FragmentPresentation.Root
        },
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_presentation)
        applyInsetsForContentView()
    }
}

@NavigationDestination(FragmentPresentation.Root::class)
class FragmentPresentationRoot : Fragment() {
    
    val navigation by navigationHandle<FragmentPresentation.Root>()
    val resultChannel by registerForNavigationResult<FragmentPresentation.TestResult> { result ->
        // Update the result text when we resume
        view?.findViewById<TextView>(R.id.fragment_presentation_result_text)?.apply {
            text = "Last result: ${result.id}"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundResource(R.color.white)
        }
        
        // Add a title
        val title = TextView(requireContext()).apply {
            text = "Fragment Presentation Tests"
            textSize = 24f
            setPadding(16, 32, 16, 32)
        }
        layout.addView(title)
        
        // Add test buttons
        layout.addView(createButton("Present Composable") {
            navigation.present(FragmentPresentation.PresentableComposable)
        })
        
        layout.addView(createButton("Present Composable For Result") {
            resultChannel.present(FragmentPresentation.PresentableComposable)
        })
        
        layout.addView(createButton("Present Fragment") {
            navigation.present(FragmentPresentation.PresentableFragment)
        })
        
        layout.addView(createButton("Present Fragment For Result") {
            resultChannel.present(FragmentPresentation.PresentableFragment)
        })
        
        layout.addView(createButton("Present Activity") {
            navigation.present(FragmentPresentation.PresentableActivity)
        })
        
        layout.addView(createButton("Present Activity For Result") {
            resultChannel.present(FragmentPresentation.PresentableActivity)
        })
        
        // Add dialog test buttons
        layout.addView(createButton("Present Dialog Composable") {
            navigation.present(FragmentPresentation.PresentableDialogComposable)
        })
        
        layout.addView(createButton("Present Dialog Composable For Result") {
            resultChannel.present(FragmentPresentation.PresentableDialogComposable)
        })
        
        layout.addView(createButton("Present Dialog Fragment") {
            navigation.present(FragmentPresentation.PresentableDialogFragment)
        })
        
        layout.addView(createButton("Present Dialog Fragment For Result") {
            resultChannel.present(FragmentPresentation.PresentableDialogFragment)
        })
        
        // Display the last received result
        val resultText = TextView(requireContext()).apply {
            id = R.id.fragment_presentation_result_text
            text = "No result received yet"
            setPadding(16, 32, 16, 32)
        }
        layout.addView(resultText)
        
        return layout
    }
    
    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(requireContext()).apply {
            this.text = text
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
        }
    }
}

@Composable
@NavigationDestination(FragmentPresentation.PresentableComposable::class)
fun FragmentPresentationComposable() {
    val navigation = navigationHandle<FragmentPresentation.PresentableComposable>()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Presentable Composable",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Button(
            onClick = { navigation.close() },
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("Close")
        }
        
        Button(
            onClick = { navigation.closeWithResult(FragmentPresentation.TestResult()) },
        ) {
            Text("Close With Result")
        }
    }
}

@NavigationDestination(FragmentPresentation.PresentableFragment::class)
class FragmentPresentationPresentable : Fragment() {
    
    val navigation by navigationHandle<FragmentPresentation.PresentableFragment>()
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.color.white)
        }
        
        // Add a title
        val title = TextView(requireContext()).apply {
            text = "Presentable Fragment"
            textSize = 24f
            setPadding(16, 32, 16, 32)
        }
        layout.addView(title)
        
        // Add action buttons
        layout.addView(Button(requireContext()).apply {
            text = "Close"
            setOnClickListener { navigation.close() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        })
        
        layout.addView(Button(requireContext()).apply {
            text = "Close With Result"
            setOnClickListener { 
                navigation.closeWithResult(FragmentPresentation.TestResult())
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        })
        
        return layout
    }
}

@NavigationDestination(FragmentPresentation.PresentableActivity::class)
class FragmentPresentationActivityPresentableActivity : androidx.appcompat.app.AppCompatActivity() {
    
    val navigation by navigationHandle<FragmentPresentation.PresentableActivity>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.color.white)
        }
        
        // Add a title
        val title = TextView(this).apply {
            text = "Presentable Activity"
            textSize = 24f
            setPadding(16, 32, 16, 32)
        }
        layout.addView(title)
        
        // Add action buttons
        layout.addView(Button(this).apply {
            text = "Close"
            setOnClickListener { navigation.close() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        })
        
        layout.addView(Button(this).apply {
            text = "Close With Result"
            setOnClickListener { 
                navigation.closeWithResult(FragmentPresentation.TestResult())
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        })
        
        setContentView(layout)
        applyInsetsForContentView()
    }
}

@Composable
@NavigationDestination(FragmentPresentation.PresentableDialogComposable::class)
fun FragmentPresentationDialogComposable() {
    val navigation = navigationHandle<FragmentPresentation.PresentableDialogComposable>()
    
    dev.enro.core.compose.dialog.DialogDestination {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { navigation.close() }
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dialog Composable",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { navigation.close() },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Close")
                }
                
                Button(
                    onClick = { navigation.closeWithResult(FragmentPresentation.TestResult()) }
                ) {
                    Text("Close With Result")
                }
            }
        }
    }
}

@NavigationDestination(FragmentPresentation.PresentableDialogFragment::class)
class FragmentPresentationDialogFragment : androidx.fragment.app.DialogFragment() {
    
    private val navigation by navigationHandle<FragmentPresentation.PresentableDialogFragment>()
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.color.white)
        }
        
        // Add a title
        val title = TextView(requireContext()).apply {
            text = "Dialog Fragment"
            textSize = 24f
            setPadding(16, 32, 16, 32)
        }
        layout.addView(title)
        
        // Add action buttons
        layout.addView(Button(requireContext()).apply {
            text = "Close"
            setOnClickListener { navigation.close() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        })
        
        layout.addView(Button(requireContext()).apply {
            text = "Close With Result"
            setOnClickListener { 
                navigation.closeWithResult(FragmentPresentation.TestResult())
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        })
        
        return layout
    }
}