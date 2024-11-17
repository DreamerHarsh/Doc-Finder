package com.example.docfinder

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayOutputStream

class PhotosReviewsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var uploadPhotoButton: Button
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var reviewsRecyclerView: RecyclerView
    private val doctorId by lazy { intent.getIntExtra("DOCTOR_ID", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos_reviews)

        db = DatabaseHelper(this)
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        photosRecyclerView = findViewById(R.id.photosRecyclerView)
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView)

        photosRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadDoctorPhotos()
        loadPatientReviews()

        uploadPhotoButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startForResult.launch(intent)
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
            savePhotoToDatabase(photo)
            loadDoctorPhotos()
        }
    }

    private fun loadDoctorPhotos() {
        val photos = db.getDoctorPhotos(doctorId)
        Log.d("PhotosReviewsActivity", "Loaded ${photos.size} photos")
        val photoAdapter = PhotoAdapter(this, photos)
        photosRecyclerView.adapter = photoAdapter
    }

    private fun loadPatientReviews() {
        val reviews = db.getPatientReviewsWithDetails(doctorId)
        val reviewAdapter = ReviewAdapter(reviews)
        reviewsRecyclerView.adapter = reviewAdapter
    }

//    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val photoUri = result.data?.data // Get the URI for the captured photo
//            photoUri?.let {
//                val photo = MediaStore.Images.Media.getBitmap(contentResolver, it)
//                savePhotoToDatabase(photo)
//                loadDoctorPhotos()
//            }
//        }
//    }

    private fun savePhotoToDatabase(photo: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)  // PNG for best quality
        val photoBytes = byteArrayOutputStream.toByteArray()
        db.saveDoctorPhoto(doctorId, photoBytes)
    }

}