package com.example.ipsheild

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipshield.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class DocRepositoryActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc_repository)

        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val uploadButton = findViewById<Button>(R.id.uploadButton)

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { fileUri ->
                uploadDocument(fileUri)
            }
        }
    }

    private fun uploadDocument(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: ""
        val fileRef = storage.reference.child("documents/$userId/${fileUri.lastPathSegment}")

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val docData = hashMapOf(
                        "userId" to userId,
                        "fileUrl" to uri.toString()
                    )
                    db.collection("documents").add(docData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Document Uploaded!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload Failed!", Toast.LENGTH_SHORT).show()
            }
    }
}
