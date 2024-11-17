package com.example.docfinder

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates

class DoctorPhotosReviewsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewsEditText: EditText
    private lateinit var submitReviewButton: Button
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    private lateinit var hospitalNameTextView: TextView
    private lateinit var hospitalAddressTextView: TextView
    private lateinit var remarksRecyclerView: RecyclerView
    private lateinit var remarksAdapter: RemarksAdapter


    private var doctorId by Delegates.notNull<Int>()
    private var patientId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_photos_reviews)

        // Initialize DatabaseHelper and Views
        db = DatabaseHelper(this)
        photosRecyclerView = findViewById(R.id.photosRecyclerView)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)
        reviewsEditText = findViewById(R.id.reviewsEditText)
        submitReviewButton = findViewById(R.id.submitReviewButton)
        hospitalNameTextView = findViewById(R.id.hospitalNameTextView)
        hospitalAddressTextView = findViewById(R.id.hospitalAddressTextView)
        remarksRecyclerView = findViewById(R.id.remarksRecyclerView)
        remarksAdapter = RemarksAdapter(mutableListOf())
        remarksRecyclerView.layoutManager = LinearLayoutManager(this)
        remarksRecyclerView.adapter = remarksAdapter

        // Get doctor and patient IDs
        doctorId = intent.getIntExtra("DOCTOR_ID", -1)
        patientId = intent.getIntExtra("PATIENT_ID", -1)

        // Initialize adapters and RecyclerViews
        photoAdapter = PhotoAdapter(this, mutableListOf())
        photosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        photosRecyclerView.adapter = photoAdapter

        reviewAdapter = ReviewAdapter(mutableListOf())
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsRecyclerView.adapter = reviewAdapter

        // Load data
        loadDoctorPhotos()
        loadPatientReviews()
        loadHospitalDetails()  // New method to load hospital details
        loadDoctorRemarks()



        // Set up button to submit reviews
        submitReviewButton.setOnClickListener {
            val reviewText = reviewsEditText.text.toString()
            if (reviewText.isNotEmpty()) {
                saveReview(reviewText)
                loadPatientReviews()  // Refresh reviews list after adding new review
                reviewsEditText.text.clear()
            }
        }
    }

    // New method to load hospital details
    private fun loadHospitalDetails() {
        val hospitalDetails = db.getHospitalDetails(doctorId)
        if (hospitalDetails != null) {
            hospitalNameTextView.text = hospitalDetails.first
            hospitalAddressTextView.text = hospitalDetails.second
        } else {
            hospitalNameTextView.text = "No hospital information available"
            hospitalAddressTextView.text = ""
        }
    }

    private fun loadDoctorRemarks() {
        val remarks = db.getDoctorRemarks(doctorId, patientId)
        remarksAdapter.updateRemarks(remarks)
    }

    // Load all photos for the doctor and update adapter
    private fun loadDoctorPhotos() {
        val photos = db.getDoctorPhotos(doctorId)
        photoAdapter.updatePhotos(photos)
    }

    // Load all reviews for the doctor and update adapter
    private fun loadPatientReviews() {
        val reviews = db.getPatientReviewsWithDetails(doctorId)
        val reviewAdapter = ReviewAdapter(reviews)
        reviewsRecyclerView.adapter = reviewAdapter
    }

    // Save review to the database
    private fun saveReview(reviewText: String) {
        db.addPatientReview(doctorId, patientId, reviewText)
    }
}
