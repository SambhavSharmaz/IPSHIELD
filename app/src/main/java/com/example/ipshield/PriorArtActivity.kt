package com.example.ipsheild

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipshield.R
import com.google.firebase.firestore.FirebaseFirestore

class PriorArtActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prior_art)

        db = FirebaseFirestore.getInstance()

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val resultsTextView = findViewById<TextView>(R.id.resultsTextView)

        searchButton.setOnClickListener {
            val searchQuery = searchEditText.text.toString()

            db.collection("ip_records")
                .whereEqualTo("title", searchQuery)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        resultsTextView.text = "Similar IP found: \n\n"
                        for (doc in documents) {
                            resultsTextView.append("${doc.data}\n\n")
                        }
                    } else {
                        resultsTextView.text = "No matching IP found."
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Search failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
