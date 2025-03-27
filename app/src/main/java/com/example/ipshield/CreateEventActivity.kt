package com.example.ipshield

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var eventTitle: EditText
    private lateinit var eventDate: TextView
    private lateinit var eventDescription: EditText
    private lateinit var createEventButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        db = FirebaseFirestore.getInstance()

        eventTitle = findViewById(R.id.event_title)
        eventDate = findViewById(R.id.event_date)
        eventDescription = findViewById(R.id.event_description)
        createEventButton = findViewById(R.id.create_event_button)

        // Open Date Picker
        eventDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                eventDate.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day)
            datePicker.show()
        }

        // Save event to Firestore
        createEventButton.setOnClickListener {
            val title = eventTitle.text.toString().trim()
            val date = eventDate.text.toString().trim()
            val description = eventDescription.text.toString().trim()

            if (title.isEmpty() || date.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val event = hashMapOf(
                "title" to title,
                "date" to date,
                "description" to description,
                "likes" to 0,
                "comments" to emptyList<String>()
            )

            db.collection("events").add(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event Created Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error creating event!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
