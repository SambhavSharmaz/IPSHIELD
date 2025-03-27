package com.example.ipshield

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrackIPActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_ip)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val ipTitleEditText = findViewById<EditText>(R.id.ipTitleEditText)
        val ipTypeEditText = findViewById<EditText>(R.id.ipTypeEditText)
        val saveButton = findViewById<Button>(R.id.saveIPButton)

        saveButton.setOnClickListener {
            val title = ipTitleEditText.text.toString()
            val type = ipTypeEditText.text.toString()
            val userId = auth.currentUser?.uid ?: ""

            val ipData = hashMapOf(
                "title" to title,
                "type" to type,
                "ownerId" to userId,
                "status" to "Pending"
            )

            db.collection("ip_records").add(ipData)
                .addOnSuccessListener {
                    Toast.makeText(this, "IP Added!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving IP", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
