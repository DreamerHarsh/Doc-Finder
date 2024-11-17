package com.example.docfinder

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AddRemarksActivity : AppCompatActivity() {

    private lateinit var remarksInput: EditText
    private lateinit var saveButton: Button
    private lateinit var db: DatabaseHelper
    private var patientId: Int = -1
    private var doctorId: Int = -1
    private lateinit var remarksRecyclerView: RecyclerView
    private lateinit var remarksAdapter: RemarksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_remarks)

        db = DatabaseHelper(this)
        remarksInput = findViewById(R.id.remarksInput)
        saveButton = findViewById(R.id.saveButton)
        remarksRecyclerView = findViewById(R.id.remarksRecyclerView)
        remarksAdapter = RemarksAdapter(mutableListOf())
        remarksRecyclerView.layoutManager = LinearLayoutManager(this)
        remarksRecyclerView.adapter = remarksAdapter


        // Get data from intent
        patientId = intent.getIntExtra("PATIENT_ID", -1)
        doctorId = intent.getIntExtra("DOCTOR_ID", -1)

        saveButton.setOnClickListener {
            val remarks = remarksInput.text.toString()
            saveRemarks(patientId, doctorId, remarks)
            finish()
        }

        loadDoctorRemarks()

    }

    private fun loadDoctorRemarks() {
        val remarks = db.getDoctorRemarks(doctorId, patientId)
        remarksAdapter.updateRemarks(remarks)
    }

    private fun saveRemarks(patientId: Int, doctorId: Int, remarks: String) {
        db.saveDoctorRemarks(patientId, doctorId, remarks)
    }
}
