package com.example.docfinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DoctorSelectionActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var specializationSpinner: Spinner
    private lateinit var doctorListView: ListView
    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var doctors: List<User>

    private var appointmentDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_selection)

        db = DatabaseHelper(this)
        specializationSpinner = findViewById(R.id.specializationSpinner)
        doctorListView = findViewById(R.id.doctorListView)

        val selectedSpecializations = intent.getStringArrayListExtra("SPECIALIZATIONS") ?: arrayListOf()
        appointmentDate = intent.getStringExtra("APPOINTMENT_DATETIME")

        setupSpecializationSpinner(selectedSpecializations)

        doctorListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDoctor = doctors[position]
            val patientId = intent.getIntExtra("PATIENT_ID", -1)

            if (patientId != -1) {
                val symptoms = "Fever"
                if (appointmentDate != null) {
                    bookAppointment(selectedDoctor.userId, patientId, symptoms, appointmentDate!!)
                } else {
                    Toast.makeText(this, "Appointment date is invalid", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Patient ID is invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpecializationSpinner(selectedSpecializations: ArrayList<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, selectedSpecializations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specializationSpinner.adapter = adapter

        specializationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedSpecialization = selectedSpecializations[position]
                doctors = db.getDoctorsBySpecialization(selectedSpecialization)
                doctorAdapter = DoctorAdapter(this@DoctorSelectionActivity, doctors)
                doctorListView.adapter = doctorAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun bookAppointment(doctorId: Int, patientId: Int, symptoms: String, date: String) {
        val success = db.bookAppointment(patientId, doctorId, symptoms, date)
        if (success) {
            Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PatientDashboardActivity::class.java)
            intent.putExtra("USER_ID", patientId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Failed to book appointment.", Toast.LENGTH_SHORT).show()
        }
    }
}
