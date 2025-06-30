package com.example.app.domain.usecase

import com.example.app.domain.model.Recipe
import com.example.app.domain.repository.RecipeRepository

/**
 * Returns a recipe identified by its id.
 */
class GetRecipeUseCase(private val repository: RecipeRepository) {
    operator fun invoke(id: Int): Recipe? = repository.getRecipe(id)
}
