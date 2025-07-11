package com.example.app.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.model.Recipe
import com.example.app.domain.usecase.GetRecipesUseCase

/** Fragment containing the meal plan screen. */
class MealPlanFragment : Fragment() {

    data class Selection(val recipeSpinner: Spinner, val personSpinner: Spinner)

    private val selections = mutableListOf<Selection>()
    private lateinit var recipes: List<Recipe>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_meal_plan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipes = GetRecipesUseCase(ServiceLocator.recipeRepository).invoke()

        val containerLayout = view.findViewById<LinearLayout>(R.id.meal_container)
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val meals = listOf("Morgens", "Mittags", "Abends")

        days.forEach { day ->
            val dayTv = TextView(requireContext())
            dayTv.text = day
            containerLayout.addView(dayTv)
            meals.forEach { meal ->
                val row = layoutInflater.inflate(R.layout.item_meal_plan, containerLayout, false)
                row.findViewById<TextView>(R.id.meal_label).text = meal
                val recipeSpinner = row.findViewById<Spinner>(R.id.spinner_recipe)
                val personSpinner = row.findViewById<Spinner>(R.id.spinner_persons)
                recipeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf("-") + recipes.map { it.name })
                personSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (1..10).toList())
                containerLayout.addView(row)
                selections += Selection(recipeSpinner, personSpinner)
            }
        }

        view.findViewById<Button>(R.id.clear_button).setOnClickListener {
            selections.forEach { it.recipeSpinner.setSelection(0); it.personSpinner.setSelection(0) }
        }

        view.findViewById<Button>(R.id.create_list_button).setOnClickListener {
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
            ServiceLocator.shoppingList = listText
            // switch to shopping list page
            (requireActivity() as? HomeActivity)?.setCurrentPage(2)
        }
    }
}
