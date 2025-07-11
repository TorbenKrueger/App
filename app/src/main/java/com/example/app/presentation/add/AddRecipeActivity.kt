package com.example.app.presentation.add

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.model.Ingredient
import com.example.app.domain.model.Recipe
import com.example.app.domain.model.Step
import com.example.app.domain.model.StepIngredient
import com.example.app.presentation.add.steps.StepAdapter
import com.example.app.domain.util.getDefaultUnit
import com.example.app.domain.util.setDefaultUnit
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
    private val ingredients = mutableListOf<Ingredient>()

    private val steps = mutableListOf<String>()
    private lateinit var stepAdapter: StepAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var editMode = false

    private lateinit var ingredientsTable: TableLayout

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

        ingredientsTable = findViewById(R.id.ingredients_table)

        stepAdapter = StepAdapter(steps)
        val stepsView = findViewById<RecyclerView>(R.id.steps_list)
        stepsView.adapter = stepAdapter

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                stepAdapter.swap(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean = editMode
        })
        itemTouchHelper.attachToRecyclerView(stepsView)

        val servingsSpinner = findViewById<Spinner>(R.id.spinner_servings)
        servingsSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, (1..10).toList())

        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, -1)
        val existingRecipe = if (recipeId != -1) GetRecipeUseCase(ServiceLocator.recipeRepository).invoke(recipeId) else null

        existingRecipe?.let { recipe ->
            findViewById<EditText>(R.id.input_name).setText(recipe.name)
            servingsSpinner.setSelection(recipe.servings - 1)
            ingredients.addAll(recipe.ingredients)
            renderIngredientTable()
            steps.addAll(recipe.steps.map { it.description })
            stepAdapter.notifyDataSetChanged()
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

        findViewById<Button>(R.id.button_add_ingredient).setOnClickListener {
            showIngredientDialog()
        }

        val toggleEdit = findViewById<Button>(R.id.toggle_edit_steps)
        val addStepButton = findViewById<Button>(R.id.add_step_button)

        toggleEdit.setOnClickListener {
            editMode = !editMode
            toggleEdit.text = if (editMode) "Fertig" else "Schritte bearbeiten"
            addStepButton.visibility = if (editMode) View.VISIBLE else View.GONE
        }

        addStepButton.setOnClickListener { showStepDialog() }

        findViewById<Button>(R.id.save_recipe).setOnClickListener {
            val name = findViewById<EditText>(R.id.input_name).text.toString()
            val servings = servingsSpinner.selectedItem as Int

            if (ingredients.isEmpty()) {
                Toast.makeText(this, "Add at least one ingredient", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val stepsLines = stepAdapter.getSteps()
            val steps = stepsLines.map { line ->
                val stepIngs = ingredients.filter { line.contains("{{${it.name}}}", ignoreCase = true) }
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
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Zutat eingeben (z.B. 100 g Nudeln oder 100 Nudeln)")
            .setView(input)
            .setPositiveButton("Weiter") { _, _ ->
                handleIngredientInput(input.text.toString())
            }
            .setNegativeButton("Fertig") { _, _ -> finalizeWizard() }
            .setCancelable(false)
            .show()
    }

    private fun handleIngredientInput(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            askIngredient()
            return
        }
        val parts = trimmed.split(Regex("\\s+"))
        val qty = parts.getOrNull(0)?.toIntOrNull()
        if (qty == null) {
            askIngredient()
            return
        }
        if (parts.size >= 3) {
            val unit = parts[1]
            val name = parts.subList(2, parts.size).joinToString(" ")
            setDefaultUnit(name, unit)
            ingredients.add(Ingredient(name, unit, qty.toDouble()))
            renderIngredientTable()
            askIngredient()
        } else if (parts.size == 2) {
            val name = parts[1]
            val unit = getDefaultUnit(name)
            if (unit != null) {
                ingredients.add(Ingredient(name, unit, qty.toDouble()))
                renderIngredientTable()
                askIngredient()
            } else {
                askUnitForIngredient(name) { chosenUnit ->
                    if (chosenUnit.isNotBlank()) {
                        setDefaultUnit(name, chosenUnit)
                        ingredients.add(Ingredient(name, chosenUnit, qty.toDouble()))
                        renderIngredientTable()
                    }
                    askIngredient()
                }
            }
        } else {
            askIngredient()
        }
    }

    private fun askUnitForIngredient(name: String, onUnit: (String) -> Unit) {
        val units = arrayOf("ml", "l", "g", "kg", "Stk", "Sonstiges")
        val displayUnits = arrayOf(
            "Milliliter (ml)",
            "Liter (l)",
            "Gramm (g)",
            "Kilo (kg)",
            "Stück (Stk.)",
            "Sonstiges"
        )
        AlertDialog.Builder(this)
            .setTitle("Einheit für $name")
            .setItems(displayUnits) { _, which ->
                onUnit(units[which])
            }
            .setCancelable(false)
            .show()
    }

    private fun finalizeWizard() {
        findViewById<EditText>(R.id.input_name).setText(wizardName)
        val spinner = findViewById<Spinner>(R.id.spinner_servings)
        spinner.setSelection((wizardServings - 1).coerceAtLeast(0))
        renderIngredientTable()
    }

    private fun renderIngredientTable() {
        ingredientsTable.removeAllViews()
        ingredients.forEachIndexed { index, ing ->
            val row = TableRow(this)
            val amount = TextView(this)
            amount.text = ing.quantityPerServing.toInt().toString()
            val unit = TextView(this)
            unit.text = ing.unit
            val name = TextView(this)
            name.text = ing.name
            row.addView(amount)
            row.addView(unit)
            row.addView(name)
            row.setOnClickListener { showIngredientDialog(ing, index) }
            ingredientsTable.addView(row)
        }
    }

    private fun showIngredientDialog(ingredient: Ingredient? = null, index: Int? = null) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val nameInput = EditText(this)
        nameInput.hint = "Name"
        val qtyInput = EditText(this)
        qtyInput.inputType = InputType.TYPE_CLASS_NUMBER
        qtyInput.hint = "Menge"
        val unitInput = EditText(this)
        unitInput.hint = "Einheit"

        ingredient?.let {
            nameInput.setText(it.name)
            qtyInput.setText(it.quantityPerServing.toInt().toString())
            unitInput.setText(it.unit)
        }

        layout.addView(nameInput)
        layout.addView(qtyInput)
        layout.addView(unitInput)

        AlertDialog.Builder(this)
            .setTitle(if (ingredient == null) "Zutat" else "Zutat bearbeiten")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val qty = qtyInput.text.toString().toIntOrNull() ?: 0
                val newIng = Ingredient(
                    nameInput.text.toString(),
                    unitInput.text.toString(),
                    qty.toDouble()
                )
                if (index == null) {
                    ingredients.add(newIng)
                } else {
                    ingredients[index] = newIng
                }
                renderIngredientTable()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun showStepDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val descInput = EditText(this)
        descInput.hint = "Beschreibung"
        val ingredientNames = listOf("Keine") + ingredients.map { it.name }
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ingredientNames)

        layout.addView(descInput)
        layout.addView(spinner)

        AlertDialog.Builder(this)
            .setTitle("Schritt hinzufügen")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val desc = descInput.text.toString()
                val ingredient = spinner.selectedItem as String
                val text = if (ingredient != "Keine") "$ingredient: $desc" else desc
                stepAdapter.addStep(text)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }
}
