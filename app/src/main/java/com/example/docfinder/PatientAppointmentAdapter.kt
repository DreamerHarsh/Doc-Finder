package com.example.docfinder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class PatientAppointmentAdapter(context: Context, appointments: List<AppointmentDetail>) :
    ArrayAdapter<AppointmentDetail>(context, 0, appointments) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val appointmentDetail = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.patient_item_appointment, parent, false)

        val doctorNameTextView = view.findViewById<TextView>(R.id.doctorNameTextView)
        val symptomsTextView = view.findViewById<TextView>(R.id.symptomsTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)

        doctorNameTextView.text = appointmentDetail?.patientName
        symptomsTextView.text = appointmentDetail?.appointment?.symptoms
        dateTextView.text = appointmentDetail?.appointment?.date

        return view
    }
}
