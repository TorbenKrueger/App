package com.example.app.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.app.domain.model.Recipe
import com.example.app.domain.usecase.GetRecipesUseCase

/**
 * ViewModel for the recipe list screen.
 */
class MainViewModel(private val getRecipes: GetRecipesUseCase) : ViewModel() {
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    fun loadRecipes() {
        _recipes.value = getRecipes()
    }
}
