package com.example.app.presentation.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.domain.model.Step

/** Adapter for editing steps with drag-and-drop support. */
class StepEditAdapter(
    private val items: MutableList<Step>,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
    private val onClick: (View, Int, Step) -> Unit
) : RecyclerView.Adapter<StepEditAdapter.StepViewHolder>() {

    /** Whether drag handles should be shown. */
    var showHandles: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false)
        return StepViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = items[position]
        holder.text.text = "${position + 1}. ${step.description}"
        holder.handle.visibility = if (showHandles) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { onClick(holder.itemView, position, step) }
        holder.handle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN && showHandles) {
                onStartDrag(holder)
            }
            false
        }
    }

    fun swap(from: Int, to: Int) {
        if (from == to) return
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
        val start = kotlin.math.min(from, to)
        val end = kotlin.math.max(from, to)
        notifyItemRangeChanged(start, end - start + 1)
    }

    fun update(index: Int, step: Step) {
        items[index] = step
        notifyItemChanged(index)
    }

    fun remove(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addStep(step: Step) {
        items.add(step)
        notifyItemInserted(items.size - 1)
    }

    fun getSteps(): List<Step> = items.toList()

    class StepViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.step_text)
        val handle: View = view.findViewById(R.id.drag_handle)
    }
}
