package com.example.docfinder

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class SymptomsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var patientId: Int = -1
    private var selectedDateTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms)

        db = DatabaseHelper(this)

        patientId = intent.getIntExtra("PATIENT_ID", -1)

        val dateTimeButton = findViewById<Button>(R.id.dateTimeButton)
        dateTimeButton.setOnClickListener {
            showDatePickerDialog()
        }

        val symptomToSpecializationMap = mapOf(
            "Fever" to "General Practitioner",
            "Cough" to "General Practitioner",
            "Headache" to "Neurologist",
            "Fatigue" to "General Practitioner",
            "Chest Pain" to "Cardiologist",
            "Shortness of Breath" to "Cardiologist",
            "Heart Palpitations" to "Cardiologist",
            "Swelling in Legs" to "General Practitioner",
            "Rash or Redness" to "Dermatologist",
            "Acne" to "Dermatologist",
            "Dry or Itchy Skin" to "Dermatologist",
            "Hair Loss" to "Dermatologist",
            "Migraines" to "Neurologist",
            "Seizures" to "Neurologist",
            "Numbness or Tingling" to "Neurologist",
            "Memory Loss" to "Neurologist",
            "Fever (Child)" to "Pediatrician",
            "Ear Pain" to "Pediatrician",
            "Sore Throat" to "Pediatrician",
            "Vomiting or Nausea" to "Gastroenterologist",
            "Anxiety" to "Psychiatrist",
            "Depression" to "Psychiatrist",
            "Insomnia" to "Psychiatrist",
            "Mood Swings" to "Psychiatrist",
            "Joint Pain" to "Orthopedic",
            "Back Pain" to "Orthopedic",
            "Muscle Weakness" to "Neurologist",
            "Swelling in Joints" to "Rheumatologist"
        )


        val selectedSpecializations = mutableSetOf<String>()

        // Define all CheckBoxes by symptom ID
        val symptomCheckBoxes = listOf(
            findViewById<CheckBox>(R.id.symptomFever),
            findViewById<CheckBox>(R.id.symptomCough),
            findViewById<CheckBox>(R.id.symptomHeadache),
            findViewById<CheckBox>(R.id.symptomFatigue),
            findViewById<CheckBox>(R.id.symptomChestPain),
            findViewById<CheckBox>(R.id.symptomShortnessOfBreath),
            findViewById<CheckBox>(R.id.symptomPalpitations),
            findViewById<CheckBox>(R.id.symptomSwellingInLegs),
            findViewById<CheckBox>(R.id.symptomRash),
            findViewById<CheckBox>(R.id.symptomAcne),
            findViewById<CheckBox>(R.id.symptomDrySkin),
            findViewById<CheckBox>(R.id.symptomHairLoss),
            findViewById<CheckBox>(R.id.symptomMigraines),
            findViewById<CheckBox>(R.id.symptomSeizures),
            findViewById<CheckBox>(R.id.symptomNumbness),
            findViewById<CheckBox>(R.id.symptomMemoryLoss),
            findViewById<CheckBox>(R.id.symptomFeverChild),
            findViewById<CheckBox>(R.id.symptomEarPain),
            findViewById<CheckBox>(R.id.symptomSoreThroat),
            findViewById<CheckBox>(R.id.symptomVomiting),
            findViewById<CheckBox>(R.id.symptomAnxiety),
            findViewById<CheckBox>(R.id.symptomDepression),
            findViewById<CheckBox>(R.id.symptomInsomnia),
            findViewById<CheckBox>(R.id.symptomMoodSwings),
            findViewById<CheckBox>(R.id.symptomJointPain),
            findViewById<CheckBox>(R.id.symptomBackPain),
            findViewById<CheckBox>(R.id.symptomMuscleWeakness),
            findViewById<CheckBox>(R.id.symptomSwellingInJoints)
        )


        // Get the button and set the click listener for searching doctors
        val searchDoctorsButton = findViewById<Button>(R.id.searchDoctorsButton)
        searchDoctorsButton.setOnClickListener {
            if (selectedDateTime == null) {
                Toast.makeText(this, "Please select date and time first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedSpecializations.clear()
            for (checkBox in symptomCheckBoxes) {
                if (checkBox.isChecked) {
                    val specialization = symptomToSpecializationMap[checkBox.text.toString()]
                    specialization?.let { selectedSpecializations.add(it) }
                }
            }

            if (selectedSpecializations.isEmpty()) {
                Toast.makeText(this, "Please select at least one symptom", Toast.LENGTH_SHORT).show()
            } else {
                // Convert to ArrayList to pass as an intent extra
                val specializationsList = ArrayList(selectedSpecializations)
                val intent = Intent(this, DoctorSelectionActivity::class.java)
                intent.putStringArrayListExtra("SPECIALIZATIONS", specializationsList)
                intent.putExtra("PATIENT_ID", patientId) // Pass the patient ID
                intent.putExtra("APPOINTMENT_DATETIME", selectedDateTime) // Pass the selected date and time
                startActivity(intent)
            }
        }
    }

    private fun showDatePickerDialog() {
        val upcomingWeekdays = getUpcomingWeekdays()
        val dateOptions = upcomingWeekdays.map {
            SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(it.time)
        }.toTypedArray()

        AlertDialog.Builder(this).apply {
            setTitle("Select Date")
            setItems(dateOptions) { _, which ->
                val selectedDate = dateOptions[which]
                showTimePickerDialog(selectedDate)
            }
            show()
        }
    }

    private fun showTimePickerDialog(selectedDate: String) {
        val times = generateTimes()
        AlertDialog.Builder(this).apply {
            setTitle("Select Time")
            setItems(times) { _, which ->
                val selectedTime = times[which]
                selectedDateTime = "$selectedDate $selectedTime"  // Store full date-time with year
                findViewById<TextView>(R.id.dateTimeTextView).text = "Selected Date and Time: $selectedDateTime"
            }
            show()
        }
    }

    private fun getUpcomingWeekdays(): List<Calendar> {
        val weekdays = mutableListOf<Calendar>()
        val calendar = Calendar.getInstance()

        while (weekdays.size < 6) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                weekdays.add(calendar.clone() as Calendar)
            }
        }
        return weekdays
    }

    private fun generateTimes(): Array<String> {
        val times = mutableListOf<String>()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 0)
        }

        while (calendar.before(endCalendar) || calendar == endCalendar) {
            times.add(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time))
            calendar.add(Calendar.MINUTE, 15)
        }
        return times.toTypedArray()
    }
}
