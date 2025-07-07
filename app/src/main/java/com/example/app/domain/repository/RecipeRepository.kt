package com.example.app.domain.repository

import com.example.app.domain.model.Recipe

/**
 * Abstraction for accessing recipes.
 */
interface RecipeRepository {
    fun getRecipes(): List<Recipe>
    fun getRecipe(id: Int): Recipe?
    fun addRecipe(recipe: Recipe)
    fun updateRecipe(recipe: Recipe)
    fun deleteRecipe(id: Int)
}
