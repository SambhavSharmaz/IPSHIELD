package com.example.ipsheild

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipshield.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SecurityActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val roleSpinner = findViewById<Spinner>(R.id.roleSpinner)
        val saveRoleButton = findViewById<Button>(R.id.saveRoleButton)

        saveRoleButton.setOnClickListener {
            val role = roleSpinner.selectedItem.toString()
            val userId = auth.currentUser?.uid ?: ""

            val roleData = hashMapOf(
                "userId" to userId,
                "role" to role
            )

            db.collection("security_access").document(userId).set(roleData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Role Updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error updating role", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
