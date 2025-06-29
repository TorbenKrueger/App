package com.example.app.domain.model

/**
 * Recipe entity containing all relevant data.
 *
 * @property id Unique recipe identifier
 * @property name Display name
 * @property imageRes Resource ID for the recipe image
 * @property servings Default number of servings
 * @property ingredients List of ingredients
 * @property steps List of preparation steps
 */
data class Recipe(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val servings: Int,
    val ingredients: List<Ingredient>,
    val steps: List<Step>
)
