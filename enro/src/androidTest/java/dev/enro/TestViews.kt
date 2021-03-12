package dev.enro

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import nav.enro.core.getNavigationHandle

abstract class TestActivity : AppCompatActivity() {

    val layout by lazy {
        val key = try {
            getNavigationHandle().key
        } catch(t: Throwable) {}

        Log.e("TestActivity", "Opened $key")

        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            addView(TextView(this@TestActivity).apply {
                text = this@TestActivity::class.java.simpleName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 32.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(TextView(this@TestActivity).apply {
                text = key.toString()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(TextView(this@TestActivity).apply {
                id = debugText
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(FrameLayout(this@TestActivity).apply {
                id = primaryFragmentContainer
                setBackgroundColor(0x22FF0000)
                setPadding(50)
            })

            addView(FrameLayout(this@TestActivity).apply {
                id = secondaryFragmentContainer
                setBackgroundColor(0x220000FF)
                setPadding(50)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
    }

    companion object {
        val debugText = View.generateViewId()
        val primaryFragmentContainer = View.generateViewId()
        val secondaryFragmentContainer = View.generateViewId()
    }
}

abstract class TestFragment : Fragment() {

    lateinit var layout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val key = try {
            getNavigationHandle().key
        } catch(t: Throwable) {}

        Log.e("TestFragment", "Opened $key")

        layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            addView(TextView(requireContext()).apply {
                text = this@TestFragment::class.java.simpleName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 32.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(TextView(requireContext()).apply {
                text = key.toString()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(TextView(requireContext()).apply {
                id = debugText
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
            })

            addView(FrameLayout(requireContext()).apply {
                id = primaryFragmentContainer
                setPadding(50)
                setBackgroundColor(0x22FF0000)
            })

            addView(FrameLayout(requireContext()).apply {
                id = secondaryFragmentContainer
                setPadding(50)
                setBackgroundColor(0x220000FF)
            })
        }

        return layout
    }

    companion object {
        val debugText = View.generateViewId()
        val primaryFragmentContainer = View.generateViewId()
        val secondaryFragmentContainer = View.generateViewId()

    }
}

