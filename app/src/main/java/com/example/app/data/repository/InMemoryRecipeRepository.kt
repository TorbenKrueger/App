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
        // Add sample recipes on startup
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
            imageUri = null,
            servings = 2,
            ingredients = listOf(flour, egg, milk),
            steps = steps
        )

        // Second recipe
        val pasta = Ingredient("Pasta", "g", 100.0)
        val tomato = Ingredient("Tomato", "pcs", 2.0)
        val cheese = Ingredient("Cheese", "g", 50.0)

        val pastaSteps = listOf(
            Step(
                "Cook pasta",
                listOf(StepIngredient(pasta, 100.0))
            ),
            Step(
                "Add sauce",
                listOf(
                    StepIngredient(tomato, 2.0),
                    StepIngredient(cheese, 50.0)
                )
            )
        )

        recipes += Recipe(
            id = 2,
            name = "Pasta",
            imageRes = android.R.drawable.ic_menu_gallery,
            imageUri = null,
            servings = 1,
            ingredients = listOf(pasta, tomato, cheese),
            steps = pastaSteps
        )
    }

    override fun getRecipes(): List<Recipe> = recipes

    override fun getRecipe(id: Int): Recipe? = recipes.find { it.id == id }

    override fun addRecipe(recipe: Recipe) {
        recipes += recipe.copy(id = (recipes.maxOfOrNull { it.id } ?: 0) + 1)
    }

    override fun updateRecipe(recipe: Recipe) {
        val index = recipes.indexOfFirst { it.id == recipe.id }
        if (index != -1) {
            recipes[index] = recipe
        }
    }

    override fun deleteRecipe(id: Int) {
        recipes.removeAll { it.id == id }
    }
}
