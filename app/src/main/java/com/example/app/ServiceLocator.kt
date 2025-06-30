package com.example.app

import com.example.app.data.repository.InMemoryRecipeRepository
import com.example.app.domain.repository.RecipeRepository

/**
 * Simple service locator providing shared dependencies.
 */
object ServiceLocator {
    val recipeRepository: RecipeRepository by lazy { InMemoryRecipeRepository() }
}
