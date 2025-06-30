package com.example.app.presentation.add

import androidx.lifecycle.ViewModel
import com.example.app.domain.model.Recipe
import com.example.app.domain.usecase.AddRecipeUseCase

/**
 * ViewModel for creating a new recipe.
 */
class AddRecipeViewModel(private val addRecipeUseCase: AddRecipeUseCase) : ViewModel() {

    fun addRecipe(recipe: Recipe) {
        addRecipeUseCase.invoke(recipe)
    }
}
