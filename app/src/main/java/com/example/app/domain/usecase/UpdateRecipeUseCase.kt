package com.example.app.domain.usecase

import com.example.app.domain.model.Recipe
import com.example.app.domain.repository.RecipeRepository

/**
 * Updates an existing recipe.
 */
class UpdateRecipeUseCase(private val repository: RecipeRepository) {
    operator fun invoke(recipe: Recipe) = repository.updateRecipe(recipe)
}
