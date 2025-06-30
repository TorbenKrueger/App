package com.example.app.domain.model

/**
 * One preparation step in a recipe.
 *
 * @property description Text describing the step
 * @property ingredients Ingredients and their amounts used in this step
 */
data class Step(
    val description: String,
    val ingredients: List<StepIngredient>
)
