package com.example.ipshield

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LicensingActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var ipTitleEditText: EditText
    private lateinit var licenseeEditText: EditText
    private lateinit var termsEditText: EditText
    private lateinit var saveLicenseButton: Button
    private lateinit var licenseContainer: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var sortButton: Button

    private val licenseList = mutableListOf<LicenseModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licensing)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ipTitleEditText = findViewById(R.id.ipTitleEditText)
        licenseeEditText = findViewById(R.id.licenseeEditText)
        termsEditText = findViewById(R.id.termsEditText)
        saveLicenseButton = findViewById(R.id.saveLicenseButton)
        licenseContainer = findViewById(R.id.licenseContainer)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        sortButton = findViewById(R.id.sortButton)

        fetchLicensingData()

        saveLicenseButton.setOnClickListener {
            saveLicenseAgreement()
        }

        searchButton.setOnClickListener {
            filterLicenses(searchEditText.text.toString().trim())
        }

        sortButton.setOnClickListener {
            sortLicenses()
        }
    }

    private fun saveLicenseAgreement() {
        val title = ipTitleEditText.text.toString().trim()
        val licensee = licenseeEditText.text.toString().trim()
        val terms = termsEditText.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (title.isEmpty() || licensee.isEmpty() || terms.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        saveLicenseButton.isEnabled = false

        val licenseData = hashMapOf(
            "title" to title,
            "licensee" to licensee,
            "terms" to terms,
            "ownerId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("licensing")
            .add(licenseData)
            .addOnSuccessListener { documentReference ->
                saveLicenseButton.isEnabled = true

                Toast.makeText(this, "License Saved Successfully!", Toast.LENGTH_SHORT).show()
                Log.d("LicensingActivity", "License saved with ID: ${documentReference.id}")

                val newLicense = LicenseModel(documentReference.id, title, licensee, terms)
                licenseList.add(newLicense)
                addLicenseToView(newLicense)

                ipTitleEditText.text.clear()
                licenseeEditText.text.clear()
                termsEditText.text.clear()
            }
            .addOnFailureListener { e ->
                saveLicenseButton.isEnabled = true
                Toast.makeText(this, "Error saving license: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("LicensingActivity", "Error saving license", e)
            }
    }

    private fun fetchLicensingData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("licensing")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                licenseList.clear()
                licenseContainer.removeAllViews()

                for (document in documents) {
                    val id = document.id
                    val title = document.getString("title") ?: "No Title"
                    val licensee = document.getString("licensee") ?: "Unknown"
                    val terms = document.getString("terms") ?: "No Terms"

                    val licenseModel = LicenseModel(id, title, licensee, terms)
                    licenseList.add(licenseModel)
                    addLicenseToView(licenseModel)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch licenses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addLicenseToView(license: LicenseModel) {
        val licenseView = layoutInflater.inflate(R.layout.item_license, null)

        val titleTextView = licenseView.findViewById<TextView>(R.id.licenseTitle)
        val licenseeTextView = licenseView.findViewById<TextView>(R.id.licenseeName)
        val termsTextView = licenseView.findViewById<TextView>(R.id.licenseTerms)
        val editButton = licenseView.findViewById<Button>(R.id.editButton)
        val deleteButton = licenseView.findViewById<Button>(R.id.deleteButton)

        titleTextView.text = "IP Title: ${license.title}"
        licenseeTextView.text = "Licensee: ${license.licensee}"
        termsTextView.text = "Terms: ${license.terms}"

        editButton.setOnClickListener { showEditDialog(license) }
        deleteButton.setOnClickListener { deleteLicense(license.id, licenseView) }

        licenseContainer.addView(licenseView)
    }

    private fun filterLicenses(query: String) {
        licenseContainer.removeAllViews()
        for (license in licenseList) {
            if (license.title.contains(query, true) || license.licensee.contains(query, true)) {
                addLicenseToView(license)
            }
        }
    }

    private fun sortLicenses() {
        licenseList.sortBy { it.title }
        licenseContainer.removeAllViews()
        for (license in licenseList) {
            addLicenseToView(license)
        }
    }

    private fun showEditDialog(license: LicenseModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_license, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()

        val titleEditText = dialogView.findViewById<EditText>(R.id.editTitle)
        val licenseeEditText = dialogView.findViewById<EditText>(R.id.editLicensee)
        val termsEditText = dialogView.findViewById<EditText>(R.id.editTerms)
        val saveButton = dialogView.findViewById<Button>(R.id.saveEditButton)

        titleEditText.setText(license.title)
        licenseeEditText.setText(license.licensee)
        termsEditText.setText(license.terms)

        saveButton.setOnClickListener {
            val updatedData = mapOf(
                "title" to titleEditText.text.toString().trim(),
                "licensee" to licenseeEditText.text.toString().trim(),
                "terms" to termsEditText.text.toString().trim()
            )

            db.collection("licensing").document(license.id).update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                    fetchLicensingData()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun deleteLicense(id: String, licenseView: View) {
        db.collection("licensing").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted Successfully!", Toast.LENGTH_SHORT).show()
                licenseContainer.removeView(licenseView)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Delete Failed!", Toast.LENGTH_SHORT).show()
            }
    }
}

data class LicenseModel(
    val id: String,
    val title: String,
    val licensee: String,
    val terms: String
)
