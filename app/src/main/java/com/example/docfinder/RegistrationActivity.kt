package com.example.docfinder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class RegistrationActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var specializationSpinner: Spinner
    private lateinit var photoImageView: ImageView
    private var selectedPhoto: ByteArray? = null
    private lateinit var hospitalNameField: EditText
    private lateinit var hospitalAddressField: EditText

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        db = DatabaseHelper(this)
        selectedPhoto = null

        val nameField = findViewById<EditText>(R.id.name)
        val ageField = findViewById<EditText>(R.id.age)
        val phoneField = findViewById<EditText>(R.id.phone)
        val passwordField = findViewById<EditText>(R.id.password)
        val userTypeGroup = findViewById<RadioGroup>(R.id.userTypeGroup)
        specializationSpinner = findViewById(R.id.specialization)
        photoImageView = findViewById(R.id.profilePicture)
        hospitalNameField = findViewById(R.id.hospitalName)
        hospitalAddressField = findViewById(R.id.hospitalAddress)

        val selectPhotoButton = findViewById<Button>(R.id.selectPhotoButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        ArrayAdapter.createFromResource(
            this,
            R.array.specializations_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            specializationSpinner.adapter = adapter
        }

        // Show specialization spinner and hospital fields only if the user is a doctor
        userTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioDoctor) {
                specializationSpinner.visibility = View.VISIBLE
                hospitalNameField.visibility = View.VISIBLE
                hospitalAddressField.visibility = View.VISIBLE
            } else {
                specializationSpinner.visibility = View.GONE
                hospitalNameField.visibility = View.GONE
                hospitalAddressField.visibility = View.GONE
            }
        }

        selectPhotoButton.setOnClickListener {
            // Launch camera intent to capture photo
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        registerButton.setOnClickListener {
            val name = nameField.text.toString()
            val age = ageField.text.toString().toIntOrNull() ?: 0
            val phone = phoneField.text.toString()
            val password = passwordField.text.toString()
            val userType = if (userTypeGroup.checkedRadioButtonId == R.id.radioDoctor) "Doctor" else "Patient"
            val specialization = if (userType == "Doctor") specializationSpinner.selectedItem.toString() else null
            val hospitalName = if (userType == "Doctor") hospitalNameField.text.toString() else null
            val hospitalAddress = if (userType == "Doctor") hospitalAddressField.text.toString() else null

            // Insert user into database
            if (db.insertUser(name, age, phone, password, userType, specialization, selectedPhoto, hospitalName, hospitalAddress)) {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                photoImageView.setImageBitmap(imageBitmap)
                selectedPhoto = bitmapToByteArray(imageBitmap)
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
