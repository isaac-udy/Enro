package dev.enro.recipes

import android.app.Application

class RecipesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RecipesComponent.installNavigationController(this)
    }
}
