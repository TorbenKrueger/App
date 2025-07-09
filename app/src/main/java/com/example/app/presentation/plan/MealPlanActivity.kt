package com.example.app.presentation.plan

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.model.Recipe
import com.example.app.domain.usecase.GetRecipesUseCase

class MealPlanActivity : AppCompatActivity() {

    data class Selection(val recipeSpinner: Spinner, val personSpinner: Spinner)

    private val selections = mutableListOf<Selection>()
    private lateinit var recipes: List<Recipe>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_plan)

        recipes = GetRecipesUseCase(ServiceLocator.recipeRepository).invoke()

        val container = findViewById<LinearLayout>(R.id.meal_container)
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val meals = listOf("Morgens", "Mittags", "Abends")

        days.forEach { day ->
            val dayTv = TextView(this)
            dayTv.text = day
            container.addView(dayTv)
            meals.forEach { meal ->
                val row = layoutInflater.inflate(R.layout.item_meal_plan, container, false)
                row.findViewById<TextView>(R.id.meal_label).text = meal
                val recipeSpinner = row.findViewById<Spinner>(R.id.spinner_recipe)
                val personSpinner = row.findViewById<Spinner>(R.id.spinner_persons)
                recipeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("-") + recipes.map { it.name })
                personSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, (1..10).toList())
                container.addView(row)
                selections += Selection(recipeSpinner, personSpinner)
            }
        }

        findViewById<Button>(R.id.clear_button).setOnClickListener {
            selections.forEach { it.recipeSpinner.setSelection(0); it.personSpinner.setSelection(0) }
        }

        findViewById<Button>(R.id.create_list_button).setOnClickListener {
            val totals = mutableMapOf<String, Pair<String, Double>>()
            selections.forEach { sel ->
                val index = sel.recipeSpinner.selectedItemPosition - 1
                if (index >= 0) {
                    val persons = sel.personSpinner.selectedItem as Int
                    val recipe = recipes[index]
                    recipe.ingredients.forEach { ing ->
                        val amount = ing.quantityPerServing * persons
                        val entry = totals[ing.name]
                        if (entry == null) {
                            totals[ing.name] = ing.unit to amount
                        } else {
                            totals[ing.name] = entry.first to (entry.second + amount)
                        }
                    }
                }
            }
            val listText = totals.entries.joinToString("\n") { "${it.key}: ${it.value.second} ${it.value.first}" }
            val intent = Intent(this, ShoppingListActivity::class.java)
            intent.putExtra(ShoppingListActivity.EXTRA_LIST, listText)
            startActivity(intent)
        }

        findViewById<Button>(R.id.nav_recipes).setOnClickListener {
            startActivity(Intent(this, com.example.app.presentation.list.MainActivity::class.java))
        }
        findViewById<Button>(R.id.nav_plan).setOnClickListener { }
    }
}
