package com.example.app.data.repository

import com.example.app.domain.model.Ingredient
import com.example.app.domain.model.Recipe
import com.example.app.domain.model.Step
import com.example.app.domain.model.StepIngredient
import com.example.app.domain.repository.RecipeRepository

/**
 * Simple in-memory implementation of [RecipeRepository].
 */
class InMemoryRecipeRepository : RecipeRepository {
    private val recipes = mutableListOf<Recipe>()

    init {
        // Add sample recipe on startup
        val flour = Ingredient("Flour", "g", 100.0)
        val egg = Ingredient("Egg", "pcs", 1.0)
        val milk = Ingredient("Milk", "ml", 150.0)

        val steps = listOf(
            Step(
                "Mix ingredients",
                listOf(
                    StepIngredient(flour, 100.0),
                    StepIngredient(egg, 1.0),
                    StepIngredient(milk, 150.0)
                )
            ),
            Step(
                "Bake in pan",
                emptyList()
            )
        )

        recipes += Recipe(
            id = 1,
            name = "Pancakes",
            imageRes = android.R.drawable.ic_menu_gallery,
            servings = 2,
            ingredients = listOf(flour, egg, milk),
            steps = steps
        )
    }

    override fun getRecipes(): List<Recipe> = recipes

    override fun getRecipe(id: Int): Recipe? = recipes.find { it.id == id }

    override fun addRecipe(recipe: Recipe) {
        recipes += recipe.copy(id = (recipes.maxOfOrNull { it.id } ?: 0) + 1)
    }
}
