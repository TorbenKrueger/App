package com.example.app.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.app.domain.model.Recipe
import com.example.app.domain.usecase.GetRecipeUseCase

/**
 * ViewModel for displaying a single recipe.
 */
class RecipeDetailViewModel(
    private val getRecipe: GetRecipeUseCase
) : ViewModel() {

    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> = _recipe

    fun loadRecipe(id: Int) {
        _recipe.value = getRecipe(id)
    }
}
