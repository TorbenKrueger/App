package com.example.app.data.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.example.app.domain.model.Ingredient
import com.example.app.domain.model.Recipe
import com.example.app.domain.model.Step
import com.example.app.domain.model.StepIngredient
import com.example.app.domain.repository.RecipeRepository

/**
 * Simple file based implementation of [RecipeRepository]. Recipes are stored as
 * JSON in the app's private storage.
 */
class PersistentRecipeRepository(private val context: Context) : RecipeRepository {

    private val fileName = "recipes.json"
    private val recipes = mutableListOf<Recipe>()

    init {
        load()
        if (recipes.isEmpty()) {
            addSampleRecipes()
            save()
        }
    }

    override fun getRecipes(): List<Recipe> = recipes

    override fun getRecipe(id: Int): Recipe? = recipes.find { it.id == id }

    override fun addRecipe(recipe: Recipe) {
        val newId = (recipes.maxOfOrNull { it.id } ?: 0) + 1
        recipes += recipe.copy(id = newId)
        save()
    }

    override fun updateRecipe(recipe: Recipe) {
        val index = recipes.indexOfFirst { it.id == recipe.id }
        if (index != -1) {
            recipes[index] = recipe
            save()
        }
    }

    private fun load() {
        try {
            val jsonStr = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                recipes += obj.toRecipe()
            }
        } catch (_: Exception) {
            // ignore, start with empty list
        }
    }

    private fun save() {
        val array = JSONArray()
        recipes.forEach { array.put(it.toJson()) }
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { it.write(array.toString().toByteArray()) }
    }
}

private fun JSONObject.toIngredient(): Ingredient =
    Ingredient(getString("name"), getString("unit"), getDouble("quantityPerServing"))

private fun JSONObject.toStep(ingredients: Map<Int, Ingredient>): Step {
    val desc = getString("description")
    val ingArr = getJSONArray("ingredients")
    val stepIngredients = mutableListOf<StepIngredient>()
    for (i in 0 until ingArr.length()) {
        val o = ingArr.getJSONObject(i)
        val ingId = o.getInt("ingredientIndex")
        val ingredient = ingredients[ingId] ?: continue
        stepIngredients += StepIngredient(ingredient, o.getDouble("amountPerServing"))
    }
    return Step(desc, stepIngredients)
}

private fun JSONArray.toIngredientList(): List<Ingredient> {
    val list = mutableListOf<Ingredient>()
    for (i in 0 until length()) {
        list += getJSONObject(i).toIngredient()
    }
    return list
}

private fun JSONArray.toStepList(ingredients: List<Ingredient>): List<Step> {
    val list = mutableListOf<Step>()
    val ingMap = ingredients.withIndex().associate { it.index to it.value }
    for (i in 0 until length()) {
        list += getJSONObject(i).toStep(ingMap)
    }
    return list
}

private fun Recipe.toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("name", name)
    obj.put("imageRes", imageRes)
    obj.put("servings", servings)

    val ingredientsArray = JSONArray()
    ingredients.forEach { ing ->
        val o = JSONObject()
        o.put("name", ing.name)
        o.put("unit", ing.unit)
        o.put("quantityPerServing", ing.quantityPerServing)
        ingredientsArray.put(o)
    }
    obj.put("ingredients", ingredientsArray)

    val stepsArray = JSONArray()
    steps.forEach { step ->
        val stepObj = JSONObject()
        stepObj.put("description", step.description)
        val ingArr = JSONArray()
        step.ingredients.forEach { si ->
            val ingIndex = ingredients.indexOfFirst { it == si.ingredient }
            val siObj = JSONObject()
            siObj.put("ingredientIndex", ingIndex)
            siObj.put("amountPerServing", si.amountPerServing)
            ingArr.put(siObj)
        }
        stepObj.put("ingredients", ingArr)
        stepsArray.put(stepObj)
    }
    obj.put("steps", stepsArray)

    return obj
}

private fun JSONObject.toRecipe(): Recipe {
    val ingredients = getJSONArray("ingredients").toIngredientList()
    val steps = getJSONArray("steps").toStepList(ingredients)

    return Recipe(
        id = getInt("id"),
        name = getString("name"),
        imageRes = getInt("imageRes"),
        servings = getInt("servings"),
        ingredients = ingredients,
        steps = steps
    )
}

private fun PersistentRecipeRepository.addSampleRecipes() {
    val flour = Ingredient("Flour", "g", 100.0)
    val egg = Ingredient("Egg", "pcs", 1.0)
    val milk = Ingredient("Milk", "ml", 150.0)

    val pancakeSteps = listOf(
        Step(
            "Mix ingredients",
            listOf(
                StepIngredient(flour, 100.0),
                StepIngredient(egg, 1.0),
                StepIngredient(milk, 150.0)
            )
        ),
        Step("Bake in pan", emptyList())
    )

    recipes += Recipe(
        id = 1,
        name = "Pancakes",
        imageRes = android.R.drawable.ic_menu_gallery,
        servings = 2,
        ingredients = listOf(flour, egg, milk),
        steps = pancakeSteps
    )

    val pasta = Ingredient("Pasta", "g", 100.0)
    val tomato = Ingredient("Tomato", "pcs", 2.0)
    val cheese = Ingredient("Cheese", "g", 50.0)

    val pastaSteps = listOf(
        Step("Cook pasta", listOf(StepIngredient(pasta, 100.0))),
        Step(
            "Add sauce",
            listOf(
                StepIngredient(tomato, 2.0),
                StepIngredient(cheese, 50.0)
            )
        )
    )

    recipes += Recipe(
        id = 2,
        name = "Pasta",
        imageRes = android.R.drawable.ic_menu_gallery,
        servings = 1,
        ingredients = listOf(pasta, tomato, cheese),
        steps = pastaSteps
    )
}

