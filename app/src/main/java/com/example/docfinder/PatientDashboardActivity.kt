package com.example.docfinder
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class PatientDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var patientName: TextView
    private lateinit var patientAge: TextView
    private lateinit var doctorsListView: ListView
    private lateinit var logoutButton: Button
    private lateinit var appointmentsAdapter: PatientAppointmentAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var patientPhoto: ImageView
    private lateinit var editProfile: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        editProfile = findViewById(R.id.editProfile)

        db = DatabaseHelper(this)
        patientName = findViewById(R.id.patientName)
        patientAge = findViewById(R.id.patientAge)
        doctorsListView=findViewById(R.id.appointmentList)
        patientPhoto = findViewById(R.id.patientPhoto)

        val patientId = intent.getIntExtra("USER_ID", -1)
        val userType = sharedPreferences.getString("userType", "")

        loadPatientProfile(patientId)
        loadPatientAppointments(patientId)

        val selectSymptomsButton = findViewById<Button>(R.id.selectSymptomsButton)
        selectSymptomsButton.setOnClickListener {
            val intent = Intent(this, SymptomsActivity::class.java)
            intent.putExtra("PATIENT_ID", patientId)
            startActivity(intent)
        }

        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            logout()
        }

        editProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("USER_ID", patientId)
            intent.putExtra("USER_TYPE", userType)
            startActivity(intent)
        }

        doctorsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDoctorId = appointmentsAdapter.getItem(position)?.doctorId
            if (selectedDoctorId != null) {
                val intent = Intent(this, DoctorPhotosReviewsActivity::class.java)
                intent.putExtra("DOCTOR_ID", selectedDoctorId)
                intent.putExtra("PATIENT_ID", patientId)
                startActivity(intent)
            }
        }
    }

    private fun loadPatientProfile(patientId: Int) {
        val patient = db.getPatientProfile(patientId)
        if (patient != null) {
            patientName.text = patient.name
            patientAge.text = "Age: ${patient.age}"

            patient.photo?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                patientPhoto.setImageBitmap(bitmap)
            }
        }
    }

    private fun loadPatientAppointments(patientId: Int) {
        val appointments = db.getPatientAppointmentsWithDoctors(patientId)
        appointmentsAdapter = PatientAppointmentAdapter(this, appointments)
        doctorsListView.adapter = appointmentsAdapter
    }

    private fun logout() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
