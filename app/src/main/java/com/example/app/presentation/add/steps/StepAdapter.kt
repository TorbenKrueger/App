package com.example.app.presentation.add.steps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R

/**
 * Simple RecyclerView adapter for editing recipe steps.
 */
class StepAdapter(private val items: MutableList<String>) : RecyclerView.Adapter<StepAdapter.StepViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false)
        return StepViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.text.text = "${position + 1}. ${items[position]}"
    }

    fun swap(from: Int, to: Int) {
        if (from == to) return
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }

    fun addStep(step: String) {
        items.add(step)
        notifyItemInserted(items.size - 1)
    }

    fun getSteps(): List<String> = items.toList()

    inner class StepViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.step_text)
        val handle: View = view.findViewById(R.id.drag_handle)
    }
}
