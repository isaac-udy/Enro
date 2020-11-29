package nav.enro

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nav.enro.core.NavigationKey
import nav.enro.core.getNavigationHandle

abstract class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val key = try {
            getNavigationHandle().key<NavigationKey>()
        } catch (t: Throwable) {
            Log.e("TestActivity", "Failed to open!", t)
            return
        }
        Log.e("TestActivity", "Opened $key")

        setContentView(
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
            }
        )
    }
}

abstract class TestFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val key = try {
            getNavigationHandle().key<NavigationKey>()
        } catch (t: Throwable) {
            Log.e("TestFragment", "Failed to open!", t)
            return null
        }
        Log.e("TestFragment", "Opened $key")

        return LinearLayout(requireContext()).apply {
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
        }
    }
}

