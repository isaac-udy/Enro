package dev.enro.tests.application.activity

import android.app.Activity
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

fun Activity.applyInsetsForContentView() {
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        // Apply the insets as a margin to the view. This solution sets
        // only the bottom, left, and right dimensions, but you can apply whichever
        // insets are appropriate to your layout. You can also update the view padding
        // if that's more appropriate.
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = insets.top
            leftMargin = insets.left
            bottomMargin = insets.bottom
            rightMargin = insets.right
        }

        // Return CONSUMED if you don't want the window insets to keep passing
        // down to descendant views.
        WindowInsetsCompat.CONSUMED
    }
}