package com.example.app.presentation.detail

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.usecase.GetRecipeUseCase
import com.example.app.domain.usecase.DeleteRecipeUseCase
import com.example.app.domain.usecase.UpdateRecipeUseCase
import com.example.app.domain.model.Step
import com.example.app.domain.model.Ingredient
import com.example.app.domain.model.Recipe
import com.example.app.presentation.detail.adapter.IngredientAdapter
import com.example.app.presentation.detail.adapter.StepEditAdapter
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

/**
 * Displays details for a selected recipe.
 */
class RecipeDetailActivity : AppCompatActivity() {

    private val viewModel: RecipeDetailViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val get = GetRecipeUseCase(ServiceLocator.recipeRepository)
                val delete = DeleteRecipeUseCase(ServiceLocator.recipeRepository)
                val update = UpdateRecipeUseCase(ServiceLocator.recipeRepository)
                @Suppress("UNCHECKED_CAST")
                return RecipeDetailViewModel(get, delete, update) as T
            }
        }
    }

    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var stepAdapter: StepEditAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private var selectedImageUri: Uri? = null
    private var editMode = false
    private var currentRecipe: Recipe? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                selectedImageUri = it
                Glide.with(this)
                    .load(it)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(findViewById(R.id.dish_image))
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Image load failed", Snackbar.LENGTH_LONG).show()
                findViewById<ImageView>(R.id.dish_image).setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }
    }

    private var photoUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openCamera()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Camera permission required", Snackbar.LENGTH_LONG).show()
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            photoUri?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(findViewById(R.id.dish_image))
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Image capture failed", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun openCamera() {
        val file = java.io.File.createTempFile("recipe_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
        takePhotoLauncher.launch(photoUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val id = intent.getIntExtra(EXTRA_RECIPE_ID, -1)

        val nameView = findViewById<TextView>(R.id.dish_name_view)
        val nameEdit = findViewById<EditText>(R.id.dish_name_edit)
        val image = findViewById<ImageView>(R.id.dish_image)
        val ingredientsView = findViewById<RecyclerView>(R.id.ingredients_list)
        val stepsView = findViewById<RecyclerView>(R.id.steps_list)
        val addStepButton = findViewById<Button>(R.id.add_step_button)
        val editSwitch = findViewById<SwitchCompat>(R.id.edit_switch)

        nameView.setOnClickListener {
            if (editMode) {
                nameView.visibility = View.GONE
                nameEdit.visibility = View.VISIBLE
                nameEdit.requestFocus()
            }
        }

        ingredientAdapter = IngredientAdapter(mutableListOf()) { view, index, ingredient ->
            if (editMode) showIngredientMenu(view, index, ingredient)
        }
        ingredientsView.layoutManager = LinearLayoutManager(this)
        ingredientsView.adapter = ingredientAdapter

        stepAdapter = StepEditAdapter(mutableListOf(), { vh -> itemTouchHelper.startDrag(vh) }) { view, index, step ->
            if (editMode) showStepMenu(view, index, step)
        }
        stepAdapter.showHandles = editMode
        stepsView.layoutManager = LinearLayoutManager(this)
        stepsView.adapter = stepAdapter

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                stepAdapter.swap(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean = false
        })
        itemTouchHelper.attachToRecyclerView(stepsView)

        addStepButton.setOnClickListener {
            if (editMode) {
                showStepDialog(Step("", emptyList())) { newStep ->
                    stepAdapter.addStep(newStep)
                }
            }
        }

        viewModel.recipe.observe(this) { recipe ->
            currentRecipe = recipe
            populateFields(recipe)
        }

        findViewById<TextView>(R.id.update_servings).setOnClickListener {
            val servingsInput = findViewById<EditText>(R.id.servings_input)
            val newServings = servingsInput.text.toString().toIntOrNull() ?: return@setOnClickListener
            viewModel.updateServings(newServings)
        }

        editSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editMode = true
                nameView.visibility = View.VISIBLE
                nameEdit.visibility = View.GONE
                addStepButton.visibility = View.VISIBLE
            } else {
                editMode = false
                val recipe = currentRecipe ?: return@setOnCheckedChangeListener
                val updated = recipe.copy(
                    name = nameEdit.text.toString(),
                    imageUri = selectedImageUri?.toString(),
                    ingredients = ingredientAdapter.getItems(),
                    steps = stepAdapter.getSteps()
                )
                nameView.visibility = View.VISIBLE
                nameEdit.visibility = View.GONE
                addStepButton.visibility = View.GONE
                if (updated != recipe) {
                    android.app.AlertDialog.Builder(this)
                        .setMessage("Save changes?")
                        .setPositiveButton("Yes") { _, _ ->
                            viewModel.updateRecipe(updated)
                        }
                        .setNegativeButton("No") { _, _ ->
                            populateFields(recipe)
                        }
                        .show()
                } else {
                    populateFields(recipe)
                }
            }
            stepAdapter.showHandles = editMode
            stepAdapter.notifyDataSetChanged()
        }

        image.setOnClickListener {
            if (!editMode) return@setOnClickListener
            PopupMenu(this, it).apply {
                menu.add("Camera")
                menu.add("Gallery")
                setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Camera" -> {
                            if (ContextCompat.checkSelfPermission(this@RecipeDetailActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                openCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        "Gallery" -> pickImageLauncher.launch("image/*")
                    }
                    true
                }
                show()
            }
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
            val amount = (si.amountPerServing * servings).toInt()
            text = text.replace(token, "$amount ${si.ingredient.unit}", ignoreCase = true)
        }
        return text
    }

    private fun showIngredientMenu(anchor: View, index: Int, ingredient: Ingredient) {
        PopupMenu(this, anchor).apply {
            menu.add("Edit")
            menu.add("Delete")
            setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Edit" -> showIngredientDialog(ingredient) { updated ->
                        ingredientAdapter.update(index, updated)
                    }
                    "Delete" -> ingredientAdapter.remove(index)
                }
                true
            }
            show()
        }
    }

    private fun showIngredientDialog(current: Ingredient?, onResult: (Ingredient) -> Unit) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val nameInput = EditText(this)
        nameInput.hint = "Name"
        val qtyInput = EditText(this)
        qtyInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        qtyInput.hint = "Menge"
        val unitInput = EditText(this)
        unitInput.hint = "Einheit"

        current?.let {
            nameInput.setText(it.name)
            qtyInput.setText(it.quantityPerServing.toInt().toString())
            unitInput.setText(it.unit)
        }

        layout.addView(nameInput)
        layout.addView(qtyInput)
        layout.addView(unitInput)

        android.app.AlertDialog.Builder(this)
            .setTitle(if (current == null) "Ingredient" else "Edit Ingredient")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val qty = qtyInput.text.toString().toIntOrNull() ?: 0
                val ing = Ingredient(nameInput.text.toString(), unitInput.text.toString(), qty.toDouble())
                onResult(ing)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun populateFields(recipe: Recipe) {
        val nameView = findViewById<TextView>(R.id.dish_name_view)
        val nameEdit = findViewById<EditText>(R.id.dish_name_edit)
        val image = findViewById<ImageView>(R.id.dish_image)
        title = recipe.name
        nameView.text = recipe.name
        nameEdit.setText(recipe.name)
        selectedImageUri = recipe.imageUri?.let { Uri.parse(it) }
        Glide.with(this)
            .load(selectedImageUri ?: recipe.imageRes)
            .placeholder(recipe.imageRes)
            .error(android.R.drawable.ic_menu_report_image)
            .into(image)

        findViewById<TextView>(R.id.servings_value).text = recipe.servings.toString()
        ingredientAdapter.setItems(recipe.ingredients)
        stepAdapter = StepEditAdapter(recipe.steps.toMutableList(), { vh -> itemTouchHelper.startDrag(vh) }) { view, index, step ->
            if (editMode) showStepMenu(view, index, step)
        }
        stepAdapter.showHandles = editMode
        findViewById<RecyclerView>(R.id.steps_list).adapter = stepAdapter
    }

    private fun showStepMenu(anchor: View, index: Int, step: Step) {
        PopupMenu(this, anchor).apply {
            menu.add("Edit")
            menu.add("Delete")
            setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Edit" -> showStepDialog(step) { updated ->
                        stepAdapter.update(index, updated)
                    }
                    "Delete" -> stepAdapter.remove(index)
                }
                true
            }
            show()
        }
    }

    private fun showStepDialog(current: Step, onResult: (Step) -> Unit) {
        val input = EditText(this)
        input.setText(current.description)
        android.app.AlertDialog.Builder(this)
            .setTitle("Edit Step")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                onResult(current.copy(description = input.text.toString()))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
    }
}
