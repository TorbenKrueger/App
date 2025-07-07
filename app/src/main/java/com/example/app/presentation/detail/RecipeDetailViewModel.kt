package com.example.app.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.app.domain.model.Recipe
import com.example.app.domain.usecase.GetRecipeUseCase
import com.example.app.domain.usecase.DeleteRecipeUseCase

/**
 * ViewModel for displaying a single recipe.
 */
class RecipeDetailViewModel(
    private val getRecipe: GetRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> = _recipe

    fun loadRecipe(id: Int) {
        _recipe.value = getRecipe(id)
    }

    /**
     * Updates the current recipe with a new servings count.
     */
    fun updateServings(newServings: Int) {
        _recipe.value = _recipe.value?.copy(servings = newServings)
    }

    fun deleteRecipe(id: Int) {
        deleteRecipeUseCase(id)
    }
}
