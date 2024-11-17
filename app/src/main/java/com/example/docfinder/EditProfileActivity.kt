package com.example.docfinder

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var editName: EditText
    private lateinit var editAge: EditText
    private lateinit var editSpecialization: EditText
    private lateinit var editHospitalName: EditText
    private lateinit var editHospitalAddress: EditText
    private lateinit var profilePhoto: ImageView
    private lateinit var uploadPhotoButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    private val REQUEST_CODE_CAPTURE_IMAGE = 101
    private var photoBlob: ByteArray? = null // To store the photo blob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        // Initialize database
        db = DatabaseHelper(this)

        // Initialize views
        editName = findViewById(R.id.editName)
        editAge = findViewById(R.id.editAge)
        editSpecialization = findViewById(R.id.editSpecialization)
        editHospitalName = findViewById(R.id.editHospitalName)
        editHospitalAddress = findViewById(R.id.editHospitalAddress)
        profilePhoto = findViewById(R.id.profilePhoto)
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)

        val userId = intent.getIntExtra("USER_ID", -1)
        val userType = intent.getStringExtra("USER_TYPE")

        loadUserProfile(userId, userType)

        uploadPhotoButton.setOnClickListener {
            captureImageFromCamera()
        }

        saveButton.setOnClickListener {
            updateUserProfile(userId, userType)
        }

        deleteButton.setOnClickListener {
            deleteUserProfile(userId)
        }
    }

    private fun loadUserProfile(userId: Int, userType: String?) {
        val user = db.getUserProfile(userId)

        user?.let {
            editName.setText(it.name)
            editAge.setText(it.age.toString())
            photoBlob = it.photo // Retrieve the photo blob from the database
            loadImage(photoBlob) // Load the profile photo if available

            if (userType == "Doctor") {
                editSpecialization.visibility = View.VISIBLE
                editHospitalName.visibility = View.VISIBLE
                editHospitalAddress.visibility = View.VISIBLE

                editSpecialization.setText(it.specialization)
                editHospitalName.setText(it.hospitalName)
                editHospitalAddress.setText(it.hospitalAddress)
            }
        }
    }

    private fun loadImage(photoBlob: ByteArray?) {
        photoBlob?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            profilePhoto.setImageBitmap(bitmap)
        }
    }

    private fun captureImageFromCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CODE_CAPTURE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            profilePhoto.setImageBitmap(imageBitmap)
            photoBlob = bitmapToBlob(imageBitmap)
        }
    }

    private fun bitmapToBlob(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun updateUserProfile(userId: Int, userType: String?) {
        val name = editName.text.toString()
        val age = editAge.text.toString().toIntOrNull()
        val specialization = if (userType == "Doctor") editSpecialization.text.toString() else null
        val hospitalName = if (userType == "Doctor") editHospitalName.text.toString() else null
        val hospitalAddress = if (userType == "Doctor") editHospitalAddress.text.toString() else null

        if (age != null) {
            val success = db.updateUserProfile(userId, name, age, photoBlob, specialization, hospitalName, hospitalAddress)
            if (success) {
                if (userType == "Doctor"){
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DoctorDashboardActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
                }
                else{
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PatientDashboardActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                }
                finish()
            } else {
                Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter a valid age.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteUserProfile(userId: Int) {
        val success = db.deleteUserProfile(userId)
        if (success) {
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Failed to delete profile. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
