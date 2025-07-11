package com.example.app.domain.model

/**
 * Simple model for a shopping list item.
 */
data class ShoppingItem(
    val id: Int,
    var name: String,
    var quantity: String = "",
    var notes: String = "",
    var isRecent: Boolean = false,
    var isSelected: Boolean = false,
    val added: Long = System.currentTimeMillis(),
    var remind: Boolean = false
)
