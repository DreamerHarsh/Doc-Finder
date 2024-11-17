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
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DoctorDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var doctorName: TextView
    private lateinit var doctorSpecialization: TextView
    private lateinit var upcomingAppointmentsList: ListView
    private lateinit var pastAppointmentsList: ListView
    private lateinit var logoutButton: Button
    private lateinit var photosReviewsButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var doctorPhoto: ImageView
    private lateinit var hospitalNameTextView: TextView
    private lateinit var hospitalAddressTextView: TextView
    private lateinit var editDoctorProfile: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        db = DatabaseHelper(this)
        doctorName = findViewById(R.id.doctorName)
        doctorSpecialization = findViewById(R.id.doctorSpecialization)
        upcomingAppointmentsList = findViewById(R.id.upcomingAppointmentsList)
        pastAppointmentsList = findViewById(R.id.pastAppointmentsList)
        doctorPhoto = findViewById(R.id.doctorPhoto)
        photosReviewsButton = findViewById(R.id.photosReviewsButton)
        hospitalNameTextView = findViewById(R.id.hospitalName)
        hospitalAddressTextView = findViewById(R.id.hospitalAddress)

        val doctorId = intent.getIntExtra("USER_ID", -1)
        val userType = sharedPreferences.getString("userType", "")

        editDoctorProfile = findViewById(R.id.editDoctorProfile)

        loadDoctorProfile(doctorId)
        loadAppointments(doctorId)

        upcomingAppointmentsList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedAppointment = (upcomingAppointmentsList.adapter as AppointmentAdapter).getItem(position) as AppointmentDetail
            openAddRemarksActivity(selectedAppointment.patientId, doctorId)
        }

        // Set click listener for past appointments
        pastAppointmentsList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedAppointment = (pastAppointmentsList.adapter as AppointmentAdapter).getItem(position) as AppointmentDetail
            openAddRemarksActivity(selectedAppointment.patientId, doctorId)
        }

        photosReviewsButton.setOnClickListener {
            val intent = Intent(this, PhotosReviewsActivity::class.java)
            intent.putExtra("DOCTOR_ID", doctorId)
            startActivity(intent)
        }

        editDoctorProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("USER_ID", doctorId)
            intent.putExtra("USER_TYPE", userType)
            startActivity(intent)
        }

        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun openAddRemarksActivity(patientId: Int, doctorId: Int) {
        val intent = Intent(this, AddRemarksActivity::class.java)
        intent.putExtra("PATIENT_ID", patientId)
        intent.putExtra("DOCTOR_ID", doctorId)
        startActivity(intent)
    }

    private fun loadDoctorProfile(doctorId: Int) {
        val doctor = db.getDoctorProfile(doctorId)
        doctorName.text = doctor?.name
        doctorSpecialization.text = doctor?.specialization

        // Load hospital name and address
        val hospitalName = doctor?.hospitalName ?: "Unknown Hospital"
        val hospitalAddress = doctor?.hospitalAddress ?: "Unknown Address"
        val userType = doctor?.userType
        hospitalNameTextView.text = hospitalName
        hospitalAddressTextView.text = hospitalAddress

        if (doctor != null) {
            doctor.photo?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                doctorPhoto.setImageBitmap(bitmap)
            }
        }
    }

    fun parseAppointmentDate(dateString: String): Date? {
        return try {
            // Updated date format to correctly parse the date string with the year included
            val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", Locale.ENGLISH)
            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata") // Set time zone to India
            val parsedDate = dateFormat.parse(dateString)

            // Debugging log to verify parsed date and milliseconds
            Log.i("DoctorDashboard", "Parsed Appointment Date: $parsedDate (${parsedDate?.time})")

            parsedDate
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }



    private fun loadAppointments(doctorId: Int) {
        val allAppointments = db.getDoctorAppointments(doctorId)
        val upcomingAppointments = mutableListOf<AppointmentDetail>()
        val pastAppointments = mutableListOf<AppointmentDetail>()

        val currentTime = System.currentTimeMillis()
        Log.i("DoctorDashboard", "Current Time: $currentTime (${Date(currentTime)})")

        for (appointment in allAppointments) {
            val patient = db.getPatientProfile(appointment.patientId)
            if (patient != null) {
                val appointmentDate = parseAppointmentDate(appointment.date)
                if (appointmentDate != null) {
                    val appointmentDetail = AppointmentDetail(appointment, patient.userId, patient.name, patient.age, doctorId)

                    // Compare appointment date with current time
                    if (appointmentDate.time >= currentTime) {
                        Log.i("DoctorDashboard", "Adding to upcoming appointments")
                        upcomingAppointments.add(appointmentDetail)
                    } else {
                        Log.i("DoctorDashboard", "Adding to past appointments")
                        pastAppointments.add(appointmentDetail)
                    }
                }
            }
        }

        // Sort both lists by date and time
        upcomingAppointments.sortBy { it.appointment.date }
        pastAppointments.sortBy { it.appointment.date }

        // Set adapters for both lists
        val upcomingAdapter = AppointmentAdapter(this, upcomingAppointments)
        upcomingAppointmentsList.adapter = upcomingAdapter

        val pastAdapter = AppointmentAdapter(this, pastAppointments)
        pastAppointmentsList.adapter = pastAdapter
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
