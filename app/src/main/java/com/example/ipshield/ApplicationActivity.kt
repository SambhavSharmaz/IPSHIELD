package com.example.ipsheild

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipshield.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ApplicationActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val typeEditText = findViewById<EditText>(R.id.typeEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val submitButton = findViewById<Button>(R.id.submitApplicationButton)

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val type = typeEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val userId = auth.currentUser?.uid ?: ""

            val applicationData = hashMapOf(
                "title" to title,
                "type" to type,
                "description" to description,
                "ownerId" to userId,
                "status" to "Submitted"
            )

            db.collection("applications").add(applicationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Application Submitted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error submitting application", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
