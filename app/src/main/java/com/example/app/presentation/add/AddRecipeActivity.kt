package com.example.app.presentation.add

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.*
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.model.Ingredient
import com.example.app.domain.model.Recipe
import com.example.app.domain.model.Step
import com.example.app.domain.model.StepIngredient
import com.example.app.domain.util.commonFoods
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

    private var selectedImageUri: Uri? = null

    // State for interactive creation dialog
    private var wizardName: String = ""
    private var wizardServings: Int = 1
    private var wizardIngredientIndex: Int = 0
    private val wizardIngredients = mutableListOf<Ingredient>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            findViewById<ImageView>(R.id.image_preview).setImageURI(it)
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
        bmp?.let {
            val uri = MediaStore.Images.Media.insertImage(contentResolver, it, "recipe", null)
            selectedImageUri = Uri.parse(uri)
            findViewById<ImageView>(R.id.image_preview).setImageURI(selectedImageUri)
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
            recipe.imageUri?.let { uriStr ->
                selectedImageUri = Uri.parse(uriStr)
                findViewById<ImageView>(R.id.image_preview).setImageURI(selectedImageUri)
            }
        } ?: startWizard()

        findViewById<Button>(R.id.select_image).setOnClickListener {
            val options = arrayOf("Camera", "Gallery")
            AlertDialog.Builder(this)
                .setItems(options) { _, which ->
                    if (which == 0) {
                        takePhotoLauncher.launch(null)
                    } else {
                        pickImageLauncher.launch("image/*")
                    }
                }
                .show()
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
            if (ingredients.isEmpty()) {
                Toast.makeText(this, "Add at least one ingredient", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stepsLines = findViewById<EditText>(R.id.input_steps).text.toString()
                .split('\n')
                .filter { it.isNotBlank() }
            val steps = stepsLines.map { line ->
                val stepIngs = ingredients.filter { line.contains("{{${it.name}}}") }
                    .map { StepIngredient(it, it.quantityPerServing) }
                Step(line, stepIngs)
            }

            val recipe = Recipe(
                recipeId.takeIf { it != -1 } ?: 0,
                name,
                android.R.drawable.ic_menu_gallery,
                selectedImageUri?.toString(),
                servings,
                ingredients,
                steps
            )
            if (existingRecipe != null) {
                viewModel.updateRecipe(recipe)
            } else {
                viewModel.addRecipe(recipe)
            }
            finish()
        }

        findViewById<Button>(R.id.back_button).setOnClickListener { finish() }
    }

    private fun startWizard() {
        wizardIngredientIndex = 0
        askName()
    }

    private fun askName() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Name des Gerichtes")
            .setView(input)
            .setPositiveButton("Weiter") { _, _ ->
                wizardName = input.text.toString()
                askServings()
            }
            .setNegativeButton("Abbrechen") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun askServings() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(this)
            .setTitle("Anzahl Portionen")
            .setView(input)
            .setPositiveButton("Weiter") { _, _ ->
                wizardServings = input.text.toString().toIntOrNull() ?: 1
                askIngredient()
            }
            .setNegativeButton("Abbrechen") { _, _ -> finalizeWizard() }
            .setCancelable(false)
            .show()
    }

    private fun askIngredient() {
        if (wizardIngredientIndex >= commonFoods.size) {
            finalizeWizard()
            return
        }
        val ingredient = commonFoods[wizardIngredientIndex]
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        AlertDialog.Builder(this)
            .setTitle("Menge ${ingredient.name} (${ingredient.unit}) pro Portion")
            .setView(input)
            .setPositiveButton("Weiter") { _, _ ->
                val qty = input.text.toString().toDoubleOrNull()
                if (qty != null) {
                    wizardIngredients.add(ingredient.copy(quantityPerServing = qty))
                }
                wizardIngredientIndex++
                askIngredient()
            }
            .setNegativeButton("Abbrechen") { _, _ -> finalizeWizard() }
            .setCancelable(false)
            .show()
    }

    private fun finalizeWizard() {
        findViewById<EditText>(R.id.input_name).setText(wizardName)
        val spinner = findViewById<Spinner>(R.id.spinner_servings)
        spinner.setSelection((wizardServings - 1).coerceAtLeast(0))
        val ingredientsText = wizardIngredients.joinToString("\n") {
            "${it.name},${it.quantityPerServing},${it.unit}"
        }
        findViewById<EditText>(R.id.input_ingredients).setText(ingredientsText)
    }
}
