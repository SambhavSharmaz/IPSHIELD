package com.example.ipshield

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        profileImageView = findViewById(R.id.profile_image_view)
        usernameEditText = findViewById(R.id.username_edit_text)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)

        loadUserProfile()

        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        saveButton.setOnClickListener {
            updateProfile()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            usernameEditText.setText(user.displayName)
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(profileImageView)
        }
    }

    private fun updateProfile() {
        val newUsername = usernameEditText.text.toString().trim()
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .apply {
                if (imageUri != null) setPhotoUri(imageUri)
            }
            .build()

        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            profileImageView.setImageURI(imageUri)
        }
    }
}
