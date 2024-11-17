package com.example.docfinder

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class AppointmentAdapter(
    context: Context,
    private val appointments: List<AppointmentDetail>
) : ArrayAdapter<AppointmentDetail>(context, 0, appointments) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.appointment_item, parent, false)
        val appointmentDetail = getItem(position)

        val patientNameTextView = view.findViewById<TextView>(R.id.patientNameTextView)
        val patientAgeTextView = view.findViewById<TextView>(R.id.patientAgeTextView)
        val appointmentDateTextView = view.findViewById<TextView>(R.id.appointmentDateTextView)

        patientNameTextView.text = appointmentDetail?.patientName ?: "Unknown"
        patientAgeTextView.text = appointmentDetail?.patientAge?.toString() ?: "N/A"
        appointmentDateTextView.text = appointmentDetail?.appointment?.date ?: "No Date"

        Log.d("AppointmentAdapter", "Position: $position, Doctor ID: ${appointmentDetail?.doctorId}")

        return view
    }

    override fun getItem(position: Int): AppointmentDetail? {
        return appointments[position]
    }
}
