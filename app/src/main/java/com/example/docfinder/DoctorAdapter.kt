package com.example.docfinder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DoctorAdapter(context: Context, private val doctors: List<User>) : ArrayAdapter<User>(context, 0, doctors) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val doctor = doctors[position]

        val listItemView = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)

        val nameTextView = listItemView.findViewById<TextView>(android.R.id.text1)
        val specializationTextView = listItemView.findViewById<TextView>(android.R.id.text2)

        nameTextView.text = doctor.name
        specializationTextView.text = doctor.specialization ?: "No specialization"

        return listItemView
    }
}
