package com.example.newapp

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newapp.databinding.ActivityFavoritesBinding
import com.example.newapp.databinding.AdProductItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesAdapter: NewsAdapter
    private lateinit var localDb: LocalFavoritesDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Enable Edge-to-Edge display
        enableEdgeToEdge()
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Set the custom toolbar as the main ActionBar
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.title = "Favorites" // Set title programmatically
        setupProductAds()

        // 3. Add padding to prevent UI from overlapping with the status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize local database
        localDb = LocalFavoritesDatabase(this)

        // Setup RecyclerView
        setupRecyclerView()

        // Load banner image (if you have one)
        // val bannerUrl = "https://example.com/banner.jpg"
        // Glide.with(this ).load(bannerUrl).into(binding.bannerImage)
    }

    override fun onResume() {
        super.onResume()
        // Load favorites every time the screen is shown to keep it updated
        loadFavorites()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = NewsAdapter(this, arrayListOf())
        binding.favoritesRecyclerView.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun loadFavorites() {
        binding.progressBar.isVisible = true
        binding.emptyText.isVisible = false
        binding.favoritesRecyclerView.isVisible = false

        lifecycleScope.launch {
            val favoriteArticles: List<Article> = if (isNetworkAvailable()) {
                // Online: Fetch from Firestore, then update local DB
                withContext(Dispatchers.IO) {
                    val firestoreFavorites = FirestoreManager.getFavorites()
                    // Clear old local favorites to ensure sync

                    localDb.deleteAllFavorite()
                    firestoreFavorites.forEach { article ->
                        // Add each article to the local database
                        val favorite = FavoriteArticle(
                            id = article.url ?: "", // Use URL as a unique ID
                            title = article.title,
                            imageUrl = article.image,
                            description = article.description
                            )
                        localDb.addFavorite(favorite)
                        article.isFavorite = true // Mark for UI
                    }
                    firestoreFavorites // Return the list to be displayed
                }
            } else {
                // Offline: Fetch from local DB
                Toast.makeText(this@FavoritesActivity, "Offline mode: Showing saved favorites", Toast.LENGTH_SHORT).show()
                withContext(Dispatchers.IO) {
                    localDb.getFavorites().map { fav ->
                        Article(
                            url = fav.id,title = fav.title,
                            description = fav.description, // <-- Add this line
                            image = fav.imageUrl,
                            isFavorite = true,
                            firestoreId = null
                        )
                    }
                }
            }

            binding.progressBar.isVisible = false
            if (favoriteArticles.isEmpty()) {
                binding.emptyText.isVisible = true
            } else {
                binding.favoritesRecyclerView.isVisible = true
                favoritesAdapter.updateArticles(ArrayList(favoriteArticles))
            }
        }
    }

    // 4. Add the functions to create and handle the options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                // User is already on this screen
                Toast.makeText(this, "You are already on the Favorites screen", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                Firebase.auth.signOut()
                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun setupProductAds() {
        // Define our 5 static "products"
        val products = listOf(
            mapOf("image" to R.drawable.product_1, "price" to "$49.99", "url" to "https://www.amazon.com/s?k=headphones" ),
            mapOf("image" to R.drawable.product_2, "price" to "$129.50", "url" to "https://www.amazon.com/s?k=smart+watch" ),
            mapOf("image" to R.drawable.product_3, "price" to "$14.95", "url" to "https://www.amazon.com/s?k=bestseller+books" ),
            mapOf("image" to R.drawable.product_4, "price" to "$22.00", "url" to "https://www.amazon.com/s?k=coffee+mug" ),
            mapOf("image" to R.drawable.product_5, "price" to "$35.75", "url" to "https://www.amazon.com/s?k=indoor+plant" )
        )

        // Find the container from the included layout
        val container = findViewById<LinearLayout>(R.id.product_ads_container)
        val inflater = LayoutInflater.from(this)

        // Clear any existing views to prevent duplicates
        container.removeAllViews()

        for (product in products) {
            val adItemBinding = AdProductItemBinding.inflate(inflater, container, false)
            adItemBinding.productImage.setImageResource(product["image"] as Int)
            adItemBinding.productPrice.text = product["price"] as String
            adItemBinding.root.setOnClickListener {
                val url = product["url"] as String
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
                }
            }
            container.addView(adItemBinding.root)
        }
    }
}
