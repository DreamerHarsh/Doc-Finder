package com.example.docfinder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DatabaseHelper(this)

        val sharedPrefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false)
        val userType = sharedPrefs.getString("userType", "")
        val userId = sharedPrefs.getInt("userId", -1)

        if (isLoggedIn && userId != -1) {
            val intent = if (userType == "Doctor") {
                Intent(this, DoctorDashboardActivity::class.java)
            } else {
                Intent(this, PatientDashboardActivity::class.java)
            }
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            finish()
        }

        val phoneField = findViewById<EditText>(R.id.phone)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerbutton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            val phone = phoneField.text.toString()
            val password = passwordField.text.toString()
            val (success, userType, userId) = db.checkUser(phone, password)

            if (success) {
                // Save login status and user data in SharedPreferences
                with(sharedPrefs.edit()) {
                    putBoolean("isLoggedIn", true)
                    putString("userType", userType)
                    if (userId != null) {
                        putInt("userId", userId)
                    }
                    apply()
                }

                val intent = if (userType == "Doctor") {
                    Intent(this, DoctorDashboardActivity::class.java)
                } else {
                    Intent(this, PatientDashboardActivity::class.java)
                }
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid Login", Toast.LENGTH_SHORT).show()
            }
        }

        registerbutton.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}
