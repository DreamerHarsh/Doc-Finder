package com.example.docfinder

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "DoctorBookingDB.db", null, 5) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
        CREATE TABLE User (
            userId INTEGER PRIMARY KEY AUTOINCREMENT, 
            name TEXT, 
            age INTEGER, 
            phone TEXT UNIQUE, 
            password TEXT, 
            userType TEXT, 
            specialization TEXT,
            photo BLOB,
            hospitalName TEXT,
            hospitalAddress TEXT
        )
    """)
        db.execSQL("""
        CREATE TABLE Appointment (
            appointmentId INTEGER PRIMARY KEY AUTOINCREMENT, 
            patientId INTEGER, 
            doctorId INTEGER, 
            symptoms TEXT, 
            date TEXT, 
            FOREIGN KEY(patientId) REFERENCES User(userId), 
            FOREIGN KEY(doctorId) REFERENCES User(userId)
        )
    """)
        db.execSQL("""
            CREATE TABLE DoctorPhotos (
                doctor_id INTEGER,
                photo BLOB,
                FOREIGN KEY(doctor_id) REFERENCES User(userId)
            )
        """)
        db.execSQL("""
        CREATE TABLE PatientReviews (
            reviewId INTEGER PRIMARY KEY AUTOINCREMENT, 
            doctor_id INTEGER, 
            patient_id INTEGER, 
            review TEXT, 
            FOREIGN KEY(doctor_id) REFERENCES User(userId), 
            FOREIGN KEY(patient_id) REFERENCES User(userId)
        )
    """)
        db.execSQL("""
        CREATE TABLE remarks (
            remarksId INTEGER PRIMARY KEY AUTOINCREMENT,
            patientId INTEGER,
            doctorId INTEGER,
            remarks TEXT,
            FOREIGN KEY(patientId) REFERENCES User(userId),
            FOREIGN KEY(doctorId) REFERENCES User(userId)
        )
    """)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS Appointment")
        db.execSQL("DROP TABLE IF EXISTS DoctorPhotos")
        db.execSQL("DROP TABLE IF EXISTS PatientReviews")
        db.execSQL("DROP TABLE IF EXISTS remarks")

        onCreate(db)
    }

    // Insert a new user (doctor or patient)
    fun insertUser(name: String, age: Int, phone: String, password: String, userType: String, specialization: String? = null, photo: ByteArray?, hospitalName: String?, hospitalAddress: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", name)
            put("age", age)
            put("phone", phone)
            put("password", password)
            put("userType", userType)
            put("specialization", specialization)
            put("photo", photo)
            put("hospitalName", hospitalName)
            put("hospitalAddress", hospitalAddress)
        }
        val result = db.insert("User", null, contentValues)
        return result != -1L
    }

    // Retrieve a user profile based on user ID
    fun getUserProfile(userId: Int): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            "User",
            null,
            "userId = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                age = cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("userType")),
                specialization = cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                photo = cursor.getBlob(cursor.getColumnIndexOrThrow("photo")),
                hospitalName = cursor.getString(cursor.getColumnIndexOrThrow("hospitalName")),
                hospitalAddress = cursor.getString(cursor.getColumnIndexOrThrow("hospitalAddress"))
            )
        }
        cursor.close()
        return user
    }

    // Update a user profile based on user ID
    fun updateUserProfile(userId: Int, name: String, age: Int, photo: ByteArray?, specialization: String?, hospitalName: String?, hospitalAddress: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", name)
            put("age", age)
            put("photo", photo)
            put("specialization", specialization)
            put("hospitalName", hospitalName)
            put("hospitalAddress", hospitalAddress)
        }
        val result = db.update("User", contentValues, "userId = ?", arrayOf(userId.toString()))
        return result > 0
    }

    // Delete a user profile based on user ID
    fun deleteUserProfile(userId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete("User", "userId = ?", arrayOf(userId.toString()))
        return result > 0
    }

    fun saveDoctorPhoto(doctorId: Int, photo: ByteArray) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("doctor_id", doctorId)
            put("photo", photo)
        }
        db.insert("DoctorPhotos", null, contentValues)
    }

    fun getDoctorPhotos(doctorId: Int): List<ByteArray> {
        val photos = mutableListOf<ByteArray>()
        val db = readableDatabase
        val cursor = db.query(
            "DoctorPhotos",          // Table name
            arrayOf("photo"),        // Columns to fetch
            "doctor_id = ?",         // Selection (WHERE clause)
            arrayOf(doctorId.toString()),  // Selection args (doctor_id)
            null,                     // Group by (optional)
            null,                     // Having (optional)
            null                      // Order by (optional)
        )

        while (cursor.moveToNext()) {
            val photoData = cursor.getBlob(cursor.getColumnIndexOrThrow("photo"))
            photos.add(photoData)
        }

        cursor.close()

        return photos
    }


    fun addPatientReview(doctorId: Int, patientId: Int, review: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("doctor_id", doctorId)
            put("patient_id", patientId)
            put("review", review)
        }
        val result = db.insert("PatientReviews", null, contentValues)
        return result != -1L
    }

    fun getPatientReviewsWithDetails(doctorId: Int): List<ReviewDetail> {
        val reviews = mutableListOf<ReviewDetail>()
        val db = this.readableDatabase

        val query = """
        SELECT PatientReviews.review, User.name AS patientName, User.photo AS patientPhoto
        FROM PatientReviews
        JOIN User ON PatientReviews.patient_id = User.userId
        WHERE PatientReviews.doctor_id = ?
    """

        val cursor = db.rawQuery(query, arrayOf(doctorId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val reviewText = cursor.getString(cursor.getColumnIndexOrThrow("review"))
                val patientName = cursor.getString(cursor.getColumnIndexOrThrow("patientName"))
                val patientPhoto = cursor.getBlob(cursor.getColumnIndexOrThrow("patientPhoto"))

                reviews.add(ReviewDetail(reviewText, patientName, patientPhoto))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return reviews
    }




    // Check user credentials and return user type
    @SuppressLint("Range")
    fun checkUser(phone: String, password: String): Triple<Boolean, String?, Int?> {
        val db = this.readableDatabase
        val cursor = db.query(
            "User",
            arrayOf("userType", "userId"),
            "phone=? AND password=?",
            arrayOf(phone, password),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val userType = cursor.getString(cursor.getColumnIndex("userType"))
            val userId = cursor.getInt(cursor.getColumnIndex("userId")) // Get user ID
            Triple(true, userType, userId) // Return success, userType, and userId
        } else {
            Triple(false, null, null) // Return failure status with null values
        }
    }




    // Get a list of doctors based on specialization
    @SuppressLint("Range")
    fun getDoctorsBySpecialization(specialization: String): List<User> {
        val db = this.readableDatabase
        val doctors = mutableListOf<User>()
        val cursor = db.query(
            "User", null, "userType=? AND specialization=?", arrayOf("Doctor", specialization), null, null, null
        )
        if (cursor.moveToFirst()) {
            do {
                doctors.add(
                    User(
                        userId = cursor.getInt(cursor.getColumnIndex("userId")),
                        name = cursor.getString(cursor.getColumnIndex("name")),
                        age = cursor.getInt(cursor.getColumnIndex("age")),
                        phone = cursor.getString(cursor.getColumnIndex("phone")),
                        password = cursor.getString(cursor.getColumnIndex("password")),
                        userType = cursor.getString(cursor.getColumnIndex("userType")),
                        specialization = cursor.getString(cursor.getColumnIndex("specialization")),
                        photo = cursor.getBlob(cursor.getColumnIndexOrThrow("photo")), // Retrieve photo
                        hospitalName = cursor.getString(cursor.getColumnIndexOrThrow("hospitalName")), // Retrieve hospital name
                        hospitalAddress = cursor.getString(cursor.getColumnIndexOrThrow("hospitalAddress")) // Retrieve hospital address
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return doctors
    }


    // Book an appointment
    fun bookAppointment(patientId: Int, doctorId: Int, symptoms: String, date: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("patientId", patientId)
            put("doctorId", doctorId)
            put("symptoms", symptoms)
            put("date", date)
        }
        val result = db.insert("Appointment", null, contentValues)
        return result != -1L
    }

    @SuppressLint("Range")
    fun getHospitalDetails(userId: Int): Pair<String, String>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT hospitalName, hospitalAddress FROM User WHERE userId = ?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndex("hospitalName"))
            val address = cursor.getString(cursor.getColumnIndex("hospitalAddress"))
            Pair(name, address)
        } else {
            null
        }.also {
            cursor.close()
        }
    }


    // Method to get the list of doctors based on a specialization
    fun getDoctorsList(specialization: String): List<User> {
        val doctors = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM User WHERE userType = ? AND specialization = ?",
            arrayOf("Doctor", specialization)
        )

        if (cursor.moveToFirst()) {
            do {
                val doctor = User(
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    age = cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    userType = cursor.getString(cursor.getColumnIndexOrThrow("userType")),
                    specialization = cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                    photo = cursor.getBlob(cursor.getColumnIndexOrThrow("photo")), // Retrieve photo
                    hospitalName = cursor.getString(cursor.getColumnIndexOrThrow("hospitalName")), // Retrieve hospital name
                    hospitalAddress = cursor.getString(cursor.getColumnIndexOrThrow("hospitalAddress")) // Retrieve hospital address
                )
                doctors.add(doctor)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return doctors
    }


    // Method to get the patient profile by patient ID
    fun getPatientProfile(patientId: Int): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM User WHERE userId = ? AND userType = ?",
            arrayOf(patientId.toString(), "Patient")
        )

        var patient: User? = null
        if (cursor.moveToFirst()) {
            patient = User(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                age = cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("userType")),
                specialization = null, // Patients do not have specialization
                photo = cursor.getBlob(cursor.getColumnIndexOrThrow("photo")), // Retrieve photo
                hospitalName = null, // Patients do not have hospital name
                hospitalAddress = null // Patients do not have hospital address
            )
        }
        cursor.close()
        return patient
    }



    // Method to get the doctor profile by doctor ID
    fun getDoctorProfile(doctorId: Int): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM User WHERE userId = ? AND userType = ?",
            arrayOf(doctorId.toString(), "Doctor")
        )

        var doctor: User? = null
        if (cursor.moveToFirst()) {
            doctor = User(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                age = cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("userType")),
                specialization = cursor.getString(cursor.getColumnIndexOrThrow("specialization")),
                photo = cursor.getBlob(cursor.getColumnIndexOrThrow("photo")), // Retrieve photo
                hospitalName = cursor.getString(cursor.getColumnIndexOrThrow("hospitalName")), // Retrieve hospital name
                hospitalAddress = cursor.getString(cursor.getColumnIndexOrThrow("hospitalAddress")) // Retrieve hospital address
            )
        }
        cursor.close()
        return doctor
    }


    // Method to get the list of appointments for a specific doctor
    fun getDoctorAppointments(doctorId: Int): List<Appointment> {
        val appointments = mutableListOf<Appointment>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Appointment WHERE doctorId = ?", arrayOf(doctorId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val appointment = Appointment(
                    appointmentId = cursor.getInt(cursor.getColumnIndexOrThrow("appointmentId")),
                    patientId = cursor.getInt(cursor.getColumnIndexOrThrow("patientId")),
                    doctorId = cursor.getInt(cursor.getColumnIndexOrThrow("doctorId")),
                    symptoms = cursor.getString(cursor.getColumnIndexOrThrow("symptoms")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                )
                appointments.add(appointment)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return appointments
    }

    fun saveDoctorRemarks(patientId: Int, doctorId: Int, remarks: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("patientId", patientId)
            put("doctorId", doctorId)
            put("remarks", remarks)
        }

        val result = db.insert("remarks", null, contentValues)
        db.close()

        return result != -1L
    }

    fun getDoctorRemarks(doctorId: Int, patientId: Int): List<String> {
        val remarks = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT remarks FROM remarks WHERE doctorId = ? AND patientId = ?",
            arrayOf(doctorId.toString(), patientId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                remarks.add(cursor.getString(cursor.getColumnIndexOrThrow("remarks")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return remarks
    }





    // Method to get all appointments for a patient and include doctor names
    fun getPatientAppointmentsWithDoctors(patientId: Int): List<AppointmentDetail> {
        val appointmentDetails = mutableListOf<AppointmentDetail>()
        val db = this.readableDatabase

        val query = """
        SELECT Appointment.*, User.name AS doctorName, User.age AS doctorAge 
        FROM Appointment 
        JOIN User ON Appointment.doctorId = User.userId 
        WHERE Appointment.patientId = ?
    """

        val cursor = db.rawQuery(query, arrayOf(patientId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val appointment = Appointment(
                    appointmentId = cursor.getInt(cursor.getColumnIndexOrThrow("appointmentId")),
                    patientId = cursor.getInt(cursor.getColumnIndexOrThrow("patientId")),
                    doctorId = cursor.getInt(cursor.getColumnIndexOrThrow("doctorId")),
                    symptoms = cursor.getString(cursor.getColumnIndexOrThrow("symptoms")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                )

                // Retrieve doctor name and age
                val doctorName = cursor.getString(cursor.getColumnIndexOrThrow("doctorName"))
                val doctorAge = cursor.getInt(cursor.getColumnIndexOrThrow("doctorAge"))
                val doctorId = cursor.getInt(cursor.getColumnIndexOrThrow("doctorId"))
                val patientId = cursor.getInt(cursor.getColumnIndexOrThrow("patientId"))


                val appointmentDetail = AppointmentDetail(appointment,patientId, doctorName, doctorAge,doctorId)
                appointmentDetails.add(appointmentDetail)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return appointmentDetails
    }
}

data class User(
    val userId: Int,
    val name: String,
    val age: Int,
    val phone: String,
    val password: String,
    val userType: String,
    val specialization: String?,
    val photo: ByteArray?,
    val hospitalName: String?,
    val hospitalAddress: String?
)



data class Appointment(
    val appointmentId: Int,
    val patientId: Int,
    val doctorId: Int,
    val symptoms: String,
    val date: String
)

data class AppointmentDetail(
    val appointment: Appointment,
    val patientId: Int,
    val patientName: String,
    val patientAge: Int,
    val doctorId: Int?
)

data class ReviewDetail(
    val reviewText: String,
    val patientName: String,
    val patientPhoto: ByteArray?
)

data class Review(
    val patientId: Int,
    val doctorId: Int,
    val reviewText: String
)



