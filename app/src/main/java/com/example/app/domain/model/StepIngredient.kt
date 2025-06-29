package com.example.app.domain.model

/**
 * Ingredient usage in a recipe step.
 *
 * @property ingredient Reference to the ingredient
 * @property amountPerServing Amount used per serving
 */
data class StepIngredient(
    val ingredient: Ingredient,
    val amountPerServing: Double
)
