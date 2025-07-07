package com.example.app.presentation.detail

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.usecase.GetRecipeUseCase
import com.example.app.domain.usecase.DeleteRecipeUseCase
import com.example.app.presentation.add.AddRecipeActivity
import com.example.app.domain.model.Step

/**
 * Displays details for a selected recipe.
 */
class RecipeDetailActivity : AppCompatActivity() {

    private val viewModel: RecipeDetailViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val get = GetRecipeUseCase(ServiceLocator.recipeRepository)
                val delete = DeleteRecipeUseCase(ServiceLocator.recipeRepository)
                @Suppress("UNCHECKED_CAST")
                return RecipeDetailViewModel(get, delete) as T
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
                stepTv.text = formatStep(step, recipe.servings)
                stepLayout.addView(stepTv)
            }
        }

        findViewById<TextView>(R.id.update_servings).setOnClickListener {
            val servingsInput = findViewById<EditText>(R.id.servings_input)
            val newServings = servingsInput.text.toString().toIntOrNull() ?: return@setOnClickListener
            viewModel.updateServings(newServings)
        }

        findViewById<Button>(R.id.edit_button).setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            intent.putExtra(AddRecipeActivity.EXTRA_RECIPE_ID, id)
            startActivity(intent)
        }

        findViewById<Button>(R.id.delete_button).setOnClickListener {
            viewModel.deleteRecipe(id)
            finish()
        }

        findViewById<Button>(R.id.back_button).setOnClickListener { finish() }

        viewModel.loadRecipe(id)
    }

    private fun formatStep(step: Step, servings: Int): String {
        var text = step.description
        step.ingredients.forEach { si ->
            val token = "{{${si.ingredient.name}}}"
            val amount = si.amountPerServing * servings
            text = text.replace(token, "$amount ${si.ingredient.unit}")
        }
        return text
    }

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }
}
