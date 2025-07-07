package com.example.app.presentation.add

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.model.Ingredient
import com.example.app.domain.model.Recipe
import com.example.app.domain.model.Step
import com.example.app.domain.usecase.AddRecipeUseCase
import com.example.app.domain.usecase.GetRecipeUseCase
import com.example.app.domain.usecase.UpdateRecipeUseCase

/**
 * Minimal screen to add new recipes.
 */
class AddRecipeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }

    private val viewModel: AddRecipeViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val add = AddRecipeUseCase(ServiceLocator.recipeRepository)
                val update = UpdateRecipeUseCase(ServiceLocator.recipeRepository)
                @Suppress("UNCHECKED_CAST")
                return AddRecipeViewModel(add, update) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        val servingsSpinner = findViewById<Spinner>(R.id.spinner_servings)
        servingsSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, (1..10).toList())

        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        val existingRecipe = if (recipeId != -1) GetRecipeUseCase(ServiceLocator.recipeRepository).invoke(recipeId) else null

        existingRecipe?.let { recipe ->
            findViewById<EditText>(R.id.input_name).setText(recipe.name)
            servingsSpinner.setSelection(recipe.servings - 1)
            val ingredientsText = recipe.ingredients.joinToString("\n") { "${it.name},${it.quantityPerServing},${it.unit}" }
            findViewById<EditText>(R.id.input_ingredients).setText(ingredientsText)
            val stepsText = recipe.steps.joinToString("\n") { it.description }
            findViewById<EditText>(R.id.input_steps).setText(stepsText)
        }

        findViewById<Button>(R.id.save_recipe).setOnClickListener {
            val name = findViewById<EditText>(R.id.input_name).text.toString()
            val servings = servingsSpinner.selectedItem as Int

            val ingredientsLines = findViewById<EditText>(R.id.input_ingredients).text.toString()
                .split('\n')
                .filter { it.isNotBlank() }
            val ingredients = ingredientsLines.mapNotNull { line ->
                val parts = line.split(',')
                if (parts.size == 3) {
                    val quantity = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                    Ingredient(parts[0].trim(), parts[2].trim(), quantity)
                } else null
            }

            val stepsLines = findViewById<EditText>(R.id.input_steps).text.toString()
                .split('\n')
                .filter { it.isNotBlank() }
            val steps = stepsLines.map { Step(it, emptyList()) }

            val recipe = Recipe(recipeId.takeIf { it != -1 } ?: 0, name, android.R.drawable.ic_menu_gallery, servings, ingredients, steps)
            if (existingRecipe != null) {
                viewModel.updateRecipe(recipe)
            } else {
                viewModel.addRecipe(recipe)
            }
            finish()
        }

        findViewById<Button>(R.id.back_button).setOnClickListener { finish() }
    }
}
