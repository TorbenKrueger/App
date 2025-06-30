package com.example.app.presentation.detail

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.usecase.GetRecipeUseCase

/**
 * Displays details for a selected recipe.
 */
class RecipeDetailActivity : AppCompatActivity() {

    private val viewModel: RecipeDetailViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val useCase = GetRecipeUseCase(ServiceLocator.recipeRepository)
                @Suppress("UNCHECKED_CAST")
                return RecipeDetailViewModel(useCase) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val id = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        viewModel.recipe.observe(this) { recipe ->
            title = recipe.name
            findViewById<TextView>(R.id.servings_value).text = recipe.servings.toString()

            val ingredientLayout = findViewById<LinearLayout>(R.id.ingredient_container)
            ingredientLayout.removeAllViews()
            recipe.ingredients.forEach { ingredient ->
                val tv = TextView(this)
                tv.text = "${ingredient.name}: ${ingredient.quantityPerServing * recipe.servings} ${ingredient.unit}"
                ingredientLayout.addView(tv)
            }

            val stepLayout = findViewById<LinearLayout>(R.id.step_container)
            stepLayout.removeAllViews()
            recipe.steps.forEach { step ->
                val stepTv = TextView(this)
                stepTv.text = step.description
                stepLayout.addView(stepTv)
                step.ingredients.forEach { si ->
                    val ingTv = TextView(this)
                    val amount = si.amountPerServing * recipe.servings
                    ingTv.text = "- ${si.ingredient.name}: $amount ${si.ingredient.unit}"
                    stepLayout.addView(ingTv)
                }
            }
        }

        findViewById<TextView>(R.id.update_servings).setOnClickListener {
            val servingsInput = findViewById<EditText>(R.id.servings_input)
            val newServings = servingsInput.text.toString().toIntOrNull() ?: return@setOnClickListener
            viewModel.updateServings(newServings)
        }

        viewModel.loadRecipe(id)
    }

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }
}
