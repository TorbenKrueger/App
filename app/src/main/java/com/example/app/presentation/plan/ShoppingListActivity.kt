package com.example.app.presentation.plan

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.ServiceLocator
import com.example.app.domain.model.ShoppingItem
import com.example.app.presentation.plan.adapter.ShoppingAdapter

/**
 * Extended shopping list with basic management features.
 */
class ShoppingListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LIST = "shopping_list"
    }

    private val items = mutableListOf<ShoppingItem>()
    private val recent = mutableListOf<ShoppingItem>()

    private lateinit var itemAdapter: ShoppingAdapter
    private lateinit var recentAdapter: ShoppingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        // Prepopulate examples if first launch
        if (items.isEmpty() && recent.isEmpty()) {
            items += listOf(
                ShoppingItem(1, "Apples", "500 g"),
                ShoppingItem(2, "Whole Grain Bread", "1 loaf"),
                ShoppingItem(3, "Milk", "1 l"),
                ShoppingItem(4, "Tomatoes", "4 pcs")
            )
        }

        itemAdapter = ShoppingAdapter(items, { onItemClicked(it, false) }, { view, pos -> showEditDialog(view, pos, false) }, ::onItemChecked)
        recentAdapter = ShoppingAdapter(recent, { onItemClicked(it, true) }, { view, pos -> showEditDialog(view, pos, true) }, ::onRecentChecked)

        findViewById<RecyclerView>(R.id.shopping_list).apply {
            layoutManager = LinearLayoutManager(this@ShoppingListActivity)
            adapter = itemAdapter
        }
        findViewById<RecyclerView>(R.id.recent_list).apply {
            layoutManager = LinearLayoutManager(this@ShoppingListActivity)
            adapter = recentAdapter
        }

        val sortSpinner = findViewById<Spinner>(R.id.sort_spinner)
        sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Name", "Date", "Status"))
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applySort()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        findViewById<Button>(R.id.add_button).setOnClickListener { addItem() }
        findViewById<Button>(R.id.move_button).setOnClickListener { moveSelected() }
        findViewById<Button>(R.id.delete_button).setOnClickListener { deleteSelected() }

        findViewById<Button>(R.id.nav_recipes).setOnClickListener {
            startActivity(android.content.Intent(this, com.example.app.presentation.list.MainActivity::class.java))
        }
        findViewById<Button>(R.id.nav_plan).setOnClickListener {
            startActivity(android.content.Intent(this, MealPlanActivity::class.java))
        }
        findViewById<Button>(R.id.nav_shopping).setOnClickListener { }
    }

    private fun addItem() {
        val input = findViewById<EditText>(R.id.input_item)
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        val id = (items + recent).maxOfOrNull { it.id }?.plus(1) ?: 1
        items += ShoppingItem(id, text)
        applySort()
        input.text.clear()
    }

    private fun onItemClicked(position: Int, fromRecent: Boolean) {
        val src = if (fromRecent) recent else items
        val dst = if (fromRecent) items else recent
        val item = src.removeAt(position)
        item.isRecent = !fromRecent
        dst += item
        applySort()
    }

    private fun onItemChecked(position: Int, checked: Boolean) {
        items[position].isSelected = checked
    }

    private fun onRecentChecked(position: Int, checked: Boolean) {
        recent[position].isSelected = checked
    }

    private fun showEditDialog(anchor: View, position: Int, fromRecent: Boolean) {
        val item = if (fromRecent) recent[position] else items[position]
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val qty = EditText(this).apply { hint = "Quantity"; setText(item.quantity) }
        val notes = EditText(this).apply { hint = "Notes"; setText(item.notes) }
        val remind = CheckBox(this).apply {
            text = "Reminder"
            isChecked = item.remind
        }
        layout.addView(qty)
        layout.addView(notes)
        layout.addView(remind)
        AlertDialog.Builder(this)
            .setTitle("Edit Item")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                item.quantity = qty.text.toString()
                item.notes = notes.text.toString()
                item.remind = remind.isChecked
                if (item.remind) {
                    val msg = getString(R.string.reminder_set, item.name)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                applySort()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun moveSelected() {
        val toRecent = items.filter { it.isSelected }
        val toMain = recent.filter { it.isSelected }
        items.removeAll(toRecent)
        recent.removeAll(toMain)
        toRecent.forEach { it.isSelected = false; it.isRecent = true }
        toMain.forEach { it.isSelected = false; it.isRecent = false }
        recent.addAll(toRecent)
        items.addAll(toMain)
        applySort()
    }

    private fun deleteSelected() {
        items.removeAll { it.isSelected }
        recent.removeAll { it.isSelected }
        applySort()
    }

    private fun applySort() {
        val criteria = findViewById<Spinner>(R.id.sort_spinner).selectedItemPosition
        val comparator = when (criteria) {
            0 -> compareBy<ShoppingItem> { it.name }
            1 -> compareBy { it.added }
            else -> compareBy { it.isRecent }
        }
        items.sortWith(comparator)
        recent.sortWith(comparator)
        itemAdapter.updateData(items)
        recentAdapter.updateData(recent)
    }
}
