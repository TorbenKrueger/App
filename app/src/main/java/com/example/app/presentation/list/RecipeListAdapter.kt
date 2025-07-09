package com.example.app.presentation.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.domain.model.Recipe

/**
 * Adapter displaying available recipes.
 */
class RecipeListAdapter(
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    private val items = mutableListOf<Recipe>()

    fun submitList(recipes: List<Recipe>) {
        items.clear()
        items.addAll(recipes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.recipe_name)
        private val image: ImageView = view.findViewById(R.id.recipe_image)

        init {
            view.setOnClickListener { onClick(items[adapterPosition]) }
        }

        fun bind(recipe: Recipe) {
            name.text = recipe.name
            if (recipe.imageUri != null) {
                image.setImageURI(android.net.Uri.parse(recipe.imageUri))
            } else {
                image.setImageResource(recipe.imageRes)
            }
        }
    }
}
