package dev.enro.example.multistack

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.example.core.navigation.MultiStackKey
import dev.enro.example.multistack.databinding.MultistackBinding
import kotlinx.parcelize.Parcelize

@Parcelize
class MultiStackItem(
    vararg val data: String
) : NavigationKey


@NavigationDestination(MultiStackKey::class)
class MultiStackActivity : Fragment() {

    private val redFrame by navigationContainer(
        containerId = R.id.redFrame,
        root = { MultiStackItem("Red") },
        emptyBehavior = EmptyBehavior.CloseParent
    )

    private val greenFrame by navigationContainer(
        containerId = R.id.greenFrame,
        root = { MultiStackItem("Green") },
        emptyBehavior = EmptyBehavior.Action {
            childFragmentManager.commitNow {
                setPrimaryNavigationFragment(childFragmentManager.findFragmentById(R.id.redFrame))
            }
            requireView().findViewById<View>(R.id.redFrame).isVisible = true
            requireView().findViewById<View>(R.id.greenFrame).isVisible = false
            requireView().findViewById<View>(R.id.blueFrame).isVisible = false
            true
        }
    )

    private val blueFrame by navigationContainer(
        containerId = R.id.blueFrame,
        root = { MultiStackItem("Blue") },
        emptyBehavior = EmptyBehavior.Action {
            childFragmentManager.commitNow {
                setPrimaryNavigationFragment(childFragmentManager.findFragmentById(R.id.redFrame))
            }
            requireView().findViewById<View>(R.id.redFrame).isVisible = true
            requireView().findViewById<View>(R.id.greenFrame).isVisible = false
            requireView().findViewById<View>(R.id.blueFrame).isVisible = false
            true
        }
    )

    private val navigation by navigationHandle<MultiStackKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        val binding = MultistackBinding.inflate(layoutInflater, container, false)
//        setContentView(binding.root)

        binding.apply {
            redFrame.isVisible = true
            greenFrame.isVisible = false
            blueFrame.isVisible = false

            redNavigationButton.setOnClickListener {
                redFrame.isVisible = true
                greenFrame.isVisible = false
                blueFrame.isVisible = false
            }

            greenNavigationButton.setOnClickListener {
                redFrame.isVisible = false
                greenFrame.isVisible = true
                blueFrame.isVisible = false
            }

            blueNavigationButton.setOnClickListener {
                redFrame.isVisible = false
                greenFrame.isVisible = false
                blueFrame.isVisible = true
            }
        }
        return binding.root
    }
}

@NavigationDestination(MultiStackItem::class)
class MultiStackFragment : Fragment() {

    private val navigation by navigationHandle<MultiStackItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LinearLayout(requireContext()).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

            addView(
                TextView(requireContext()).apply {
                    layoutParams = params
                    text = navigation.key.data.joinToString(" -> ")
                    setPadding(50)
                }
            )

            addView(
                Button(requireContext()).apply {
                    layoutParams = params
                    text = "Forward"
                    setOnClickListener {
                        val dataValue = navigation.key.data.last().toIntOrNull() ?: 0
                        val nextKey = MultiStackItem(*navigation.key.data, (dataValue + 1).toString())
                        navigation.forward(nextKey)
                    }
                }
            )
        }
    }
}