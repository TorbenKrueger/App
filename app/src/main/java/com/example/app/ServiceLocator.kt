package com.example.app

import android.content.Context
import com.example.app.data.repository.PersistentRecipeRepository
import com.example.app.domain.repository.RecipeRepository

/**
 * Simple service locator providing shared dependencies.
 */
object ServiceLocator {
    private lateinit var applicationContext: Context
    val recipeRepository: RecipeRepository by lazy { PersistentRecipeRepository(applicationContext) }

    /** Last generated shopping list text. */
    var shoppingList: String = ""

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}
