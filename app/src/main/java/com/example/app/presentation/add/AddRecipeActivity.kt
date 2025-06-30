package com.example.app.presentation.add

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.model.Ingredient
import com.example.app.domain.model.Recipe
import com.example.app.domain.model.Step
import com.example.app.domain.model.StepIngredient
import com.example.app.domain.usecase.AddRecipeUseCase

/**
 * Minimal screen to add new recipes.
 */
class AddRecipeActivity : AppCompatActivity() {

    private val viewModel: AddRecipeViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val useCase = AddRecipeUseCase(ServiceLocator.recipeRepository)
                @Suppress("UNCHECKED_CAST")
                return AddRecipeViewModel(useCase) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        findViewById<Button>(R.id.save_recipe).setOnClickListener {
            val name = findViewById<EditText>(R.id.input_name).text.toString()
            val servings = findViewById<EditText>(R.id.input_servings).text.toString().toIntOrNull() ?: 1

            // For simplicity we only support one ingredient and one step via text inputs
            val ingredientName = findViewById<EditText>(R.id.input_ingredient).text.toString()
            val quantity = findViewById<EditText>(R.id.input_quantity).text.toString().toDoubleOrNull() ?: 0.0
            val unit = findViewById<EditText>(R.id.input_unit).text.toString()

            val ingredient = Ingredient(ingredientName, unit, quantity)
            val step = Step("Mix ingredients", listOf(StepIngredient(ingredient, quantity)))

            val recipe = Recipe(0, name, android.R.drawable.ic_menu_gallery, servings, listOf(ingredient), listOf(step))
            viewModel.addRecipe(recipe)
            finish()
        }
    }
}
