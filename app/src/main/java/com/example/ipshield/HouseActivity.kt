package com.example.ipshield

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HouseActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var likeCountTextView: TextView
    private lateinit var likeButton: ImageView
    private lateinit var sidebarMenu: LinearLayout
    private var hasLiked: Boolean = false

    private lateinit var menuUsername: TextView
    private lateinit var menuEmail: TextView
    private lateinit var menuProfileImage: ImageView
    private lateinit var viewPager: ViewPager2
    private lateinit var closeSidebarButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private val eventId = "event_2025"  // Ensure this is initialized

    private val images = listOf(
        R.drawable.fea_allsix,
        R.drawable.fea_application,
        R.drawable.fea_prior_search,
        R.drawable.fea_security,
        R.drawable.fea_monetization,
        R.drawable.fea_documentation,
        R.drawable.fea_manage
    )

    private val imageSliderRunnable = object : Runnable {
        override fun run() {
            val current = viewPager.currentItem
            val next = (current + 1) % images.size
            viewPager.setCurrentItem(next, true)
            handler.postDelayed(this, 6000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Sidebar
        sidebarMenu = findViewById(R.id.sidebar_menu)
        menuUsername = findViewById(R.id.menu_username)
        menuEmail = findViewById(R.id.menu_email)
        menuProfileImage = findViewById(R.id.menu_profile_image)
        closeSidebarButton = findViewById(R.id.close_sidebar)

        // Sidebar toggle
        findViewById<ImageView>(R.id.profile_image).setOnClickListener {
            toggleSidebar()
        }

        closeSidebarButton.setOnClickListener {
            closeSidebar()
        }

        val licensingact = findViewById<LinearLayout>(R.id.licensingButton)
        licensingact.setOnClickListener {
            startActivity(Intent(this, LicensingActivity::class.java))
        }

        setupMenuClickListeners()
        loadUserDetails()

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ImageSliderAdapter(images)
        handler.postDelayed(imageSliderRunnable, 6000)

        val createEventButton = findViewById<Button>(R.id.create_event_button)
        likeButton = findViewById(R.id.like_button)
        likeCountTextView = findViewById(R.id.like_count)
        val commentButton = findViewById<ImageView>(R.id.comment_button)
        val shareButton = findViewById<ImageView>(R.id.share_button)

        createEventButton.setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }

        likeButton.setOnClickListener {
            if (!hasLiked) {
                updateLikes()
            } else {
                Toast.makeText(this, "You have already liked this event.", Toast.LENGTH_SHORT).show()
            }
        }

        commentButton.setOnClickListener {
            val intent = Intent(this, CommentsActivity::class.java)
            intent.putExtra("event_id", eventId) // Ensure event_id is passed
            startActivity(intent)
        }


        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this event!")
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Join the IPSHIELD Innovation Conference 2025 on July 12 at 1 p.m. in the NB Lobby."
            )
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(imageSliderRunnable)
    }

    private fun toggleSidebar() {
        if (sidebarMenu.isVisible) {
            closeSidebar()
        } else {
            sidebarMenu.translationX = -sidebarMenu.width.toFloat()
            sidebarMenu.visibility = View.VISIBLE
            sidebarMenu.animate().translationX(0f).setDuration(300).start()
        }
    }

    private fun closeSidebar() {
        sidebarMenu.animate().translationX(-sidebarMenu.width.toFloat()).setDuration(300).withEndAction {
            sidebarMenu.visibility = View.GONE
        }.start()
    }

    private fun setupMenuClickListeners() {
        findViewById<Button>(R.id.edit_profile_button).setOnClickListener {
            navigateTo(EditProfileActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav_settings).setOnClickListener {
            navigateTo(SettingsActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav_help).setOnClickListener {
            navigateTo(HelpSupportActivity::class.java)
        }

        findViewById<Button>(R.id.logout_button).setOnClickListener {
            auth.signOut()
            navigateTo(LoginActivity::class.java, true)
        }
    }

    private fun navigateTo(destination: Class<*>, finishActivity: Boolean = false) {
        startActivity(Intent(this, destination))
        closeSidebar()
        if (finishActivity) finish()
    }

    private fun updateLikes() {
        likeCountTextView.text = (likeCountTextView.text.toString().toInt() + 1).toString()
        likeButton.alpha = 0.5f
        likeButton.isEnabled = false
        hasLiked = true
    }

    private fun loadUserDetails() {
        val user = auth.currentUser
        if (user != null) {
            menuUsername.text = user.displayName ?: "User"
            menuEmail.text = user.email ?: "No Email"

            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(menuProfileImage)
        }
    }
}
