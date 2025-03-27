package com.example.ipshield

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var commentsList: ListView
    private lateinit var commentInput: EditText
    private lateinit var sendCommentButton: ImageView
    private lateinit var adapter: CommentAdapter
    private val commentsArray = mutableListOf<Comment>()

    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 🔹 Get eventId from intent
        eventId = intent.getStringExtra("event_id")
        if (eventId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🔹 Initialize UI
        commentsList = findViewById(R.id.comments_list)
        commentInput = findViewById(R.id.comment_input)
        sendCommentButton = findViewById(R.id.send_comment_button)

        // 🔹 Use Custom Adapter
        adapter = CommentAdapter(this, commentsArray)
        commentsList.adapter = adapter

        // 🔹 Load Comments
        loadComments()

        // 🔹 Send Comment Button
        sendCommentButton.setOnClickListener {
            postComment()
        }
    }

    private fun loadComments() {
        db.collection("events").document(eventId!!)
            .collection("comments")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                commentsArray.clear()
                for (document in documents) {
                    val commentText = document.getString("text") ?: ""
                    val username = document.getString("username") ?: "Anonymous"

                    // ✅ Handle both Timestamp and Long formats
                    val timestampField = document.get("timestamp")
                    val timestamp: Timestamp = when (timestampField) {
                        is Timestamp -> timestampField // Firestore Timestamp
                        is Long -> Timestamp(timestampField / 1000, 0) // Convert milliseconds to seconds
                        else -> Timestamp.now() // Default to current time if invalid
                    }

                    // ✅ Add as `Comment` object instead of `String`
                    val comment = Comment(commentText, username, timestamp)
                    commentsArray.add(comment)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("CommentsActivity", "Error loading comments", e)
                Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
    }


    private fun postComment() {
        val commentText = commentInput.text.toString().trim()
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val commentData = hashMapOf(
            "text" to commentText,
            "username" to (user.displayName ?: "Anonymous"),
            "timestamp" to Timestamp.now()
        )

        db.collection("events").document(eventId!!)
            .collection("comments")
            .add(commentData)
            .addOnSuccessListener {
                commentInput.text.clear()
                loadComments()  // ✅ Refresh Comments
            }
            .addOnFailureListener { e ->
                Log.e("IPShield", "Error posting comment", e)
                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
            }
    }
}
