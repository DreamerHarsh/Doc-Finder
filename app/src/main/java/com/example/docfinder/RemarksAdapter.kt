package com.example.docfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RemarksAdapter(private var remarks: MutableList<String>) : RecyclerView.Adapter<RemarksAdapter.RemarksViewHolder>() {

    // ViewHolder class for the RemarksAdapter
    class RemarksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val remarkTextView: TextView = itemView.findViewById(R.id.remarkTextView)
    }

    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemarksViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_remark, parent, false)
        return RemarksViewHolder(view)
    }

    // Called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: RemarksViewHolder, position: Int) {
        holder.remarkTextView.text = remarks[position]
    }

    // Returns the total number of items in the data set held by the adapter
    override fun getItemCount(): Int = remarks.size

    // Method to update remarks list and notify the adapter about data changes
    fun updateRemarks(newRemarks: List<String>) {
        remarks.clear()
        remarks.addAll(newRemarks)
        notifyDataSetChanged()
    }
}
