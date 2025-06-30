package com.example.app.domain.usecase

import com.example.app.domain.model.Recipe
import com.example.app.domain.repository.RecipeRepository

/**
 * Returns all available recipes.
 */
class GetRecipesUseCase(private val repository: RecipeRepository) {
    operator fun invoke(): List<Recipe> = repository.getRecipes()
}
