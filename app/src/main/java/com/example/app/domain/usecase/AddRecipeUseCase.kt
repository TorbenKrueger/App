package com.example.app.domain.usecase

import com.example.app.domain.model.Recipe
import com.example.app.domain.repository.RecipeRepository

/**
 * Persists a new recipe.
 */
class AddRecipeUseCase(private val repository: RecipeRepository) {
    operator fun invoke(recipe: Recipe) = repository.addRecipe(recipe)
}
