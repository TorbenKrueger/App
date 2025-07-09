package com.example.app.presentation.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.usecase.GetRecipesUseCase
import com.example.app.presentation.detail.RecipeDetailActivity
import com.example.app.presentation.add.AddRecipeActivity

/**
 * Shows a list of all recipes.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val useCase = GetRecipesUseCase(ServiceLocator.recipeRepository)
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(useCase) as T
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val recyclerView = findViewById<RecyclerView>(R.id.recipe_list)
        val adapter = RecipeListAdapter { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.add_recipe_button).setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        findViewById<Button>(R.id.back_button).setOnClickListener { finish() }

        findViewById<Button>(R.id.nav_recipes).setOnClickListener { }
        findViewById<Button>(R.id.nav_plan).setOnClickListener {
            startActivity(Intent(this, com.example.app.presentation.plan.MealPlanActivity::class.java))
        }

        viewModel.recipes.observe(this) { adapter.submitList(it) }
        viewModel.loadRecipes()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecipes()
    }


}
