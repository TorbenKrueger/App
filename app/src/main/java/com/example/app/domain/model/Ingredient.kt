package com.example.app.domain.model

/**
 * Represents an ingredient used in a recipe.
 *
 * @property name Display name of the ingredient
 * @property unit Unit the ingredient is measured in
 * @property quantityPerServing Quantity required for one serving
 */
data class Ingredient(
    val name: String,
    val unit: String,
    val quantityPerServing: Double
)
