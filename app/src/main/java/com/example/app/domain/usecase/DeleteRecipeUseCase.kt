package com.example.app.domain.usecase

import com.example.app.domain.repository.RecipeRepository

/**
 * Deletes a recipe identified by its id.
 */
class DeleteRecipeUseCase(private val repository: RecipeRepository) {
    operator fun invoke(id: Int) = repository.deleteRecipe(id)
}
