package com.example.app.presentation.add

import androidx.lifecycle.ViewModel
import com.example.app.domain.model.Recipe
import com.example.app.domain.usecase.AddRecipeUseCase
import com.example.app.domain.usecase.UpdateRecipeUseCase

/**
 * ViewModel for creating a new recipe.
 */
class AddRecipeViewModel(
    private val addRecipeUseCase: AddRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase
) : ViewModel() {

    fun addRecipe(recipe: Recipe) {
        addRecipeUseCase.invoke(recipe)
    }

    fun updateRecipe(recipe: Recipe) {
        updateRecipeUseCase.invoke(recipe)
    }
}
