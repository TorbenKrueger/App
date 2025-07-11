package com.example.app.presentation.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.domain.model.Ingredient

/** Adapter for displaying ingredients. */
class IngredientAdapter(
    private val items: MutableList<Ingredient>,
    private val onClick: (View, Int, Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ing = items[position]
        val text = "${ing.quantityPerServing.toInt()} ${ing.unit} ${ing.name}"
        holder.name.text = text
        holder.itemView.setOnClickListener { onClick(holder.itemView, position, ing) }
    }

    fun setItems(list: List<Ingredient>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun update(index: Int, ingredient: Ingredient) {
        items[index] = ingredient
        notifyItemChanged(index)
    }

    fun remove(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun getItems(): List<Ingredient> = items.toList()

    class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.ingredient_name)
    }
}
