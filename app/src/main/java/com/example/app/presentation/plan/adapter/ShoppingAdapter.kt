package com.example.app.presentation.plan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.domain.model.ShoppingItem

/** Adapter displaying shopping list items. */
class ShoppingAdapter(
    private val items: MutableList<ShoppingItem>,
    private val onItemClick: (Int) -> Unit,
    private val onEdit: (View, Int) -> Unit,
    private val onChecked: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<ShoppingAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        val details = buildString {
            if (item.quantity.isNotEmpty()) append(item.quantity)
            if (item.notes.isNotEmpty()) {
                if (isNotEmpty()) append(" - ")
                append(item.notes)
            }
        }
        holder.details.text = details
        holder.check.isChecked = item.isSelected
        holder.itemView.setOnClickListener { onItemClick(position) }
        holder.itemView.setOnLongClickListener {
            onEdit(it, position)
            true
        }
        holder.check.setOnCheckedChangeListener { _, checked -> onChecked(position, checked) }
        holder.menu.setOnClickListener { onEdit(it, position) }
    }

    fun updateData(list: List<ShoppingItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val details: TextView = view.findViewById(R.id.details)
        val check: CheckBox = view.findViewById(R.id.check)
        val menu: ImageButton = view.findViewById(R.id.menu)
    }
}
