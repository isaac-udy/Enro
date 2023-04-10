package dev.enro.example

import android.app.AlertDialog
import androidx.activity.ComponentActivity
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.activity
import dev.enro.core.close
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.getNavigationHandle

object ExampleInterceptor : NavigationInstructionInterceptor {

    private var closeIsConfirmed = false

    override fun intercept(instruction: NavigationInstruction.Close, context: NavigationContext<*>): NavigationInstruction? {
        if (context.contextReference is ComponentActivity && !closeIsConfirmed) {
            val activity = context.activity
            AlertDialog.Builder(activity).apply {
                setTitle("Exit")
                setMessage("Are you sure you'd like to exit the Enro example application?")
                setNegativeButton("Cancel") { _, _ -> }
                setPositiveButton("Exit") {_, _ ->
                    closeIsConfirmed = true
                    activity
                        .getNavigationHandle()
                        .close()
                }
                show()
            }
            return null
        }
        if (instruction is NavigationInstruction.Close.WithResult && instruction.result is String) {
            val result = instruction.result as String
            if(result.equals("intercept", ignoreCase = true)) {
                return NavigationInstruction.Close.WithResult("This result was intercepted and changed!")
            }
        }
        return instruction
    }
}