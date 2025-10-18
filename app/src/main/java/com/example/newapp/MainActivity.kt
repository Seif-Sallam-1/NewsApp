package com.example.newapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.example.newapp.databinding.ActivityMainBinding
import com.example.newapp.databinding.AdProductItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PREFS = "user_prefs"
    private val KEY_COUNTRY = "country_code"

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://api.mediastack.com/v1/" )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(NewsCallable::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        binding.newsList.layoutManager = LinearLayoutManager(this)
        loadNews()
        binding.swiprRefresh.setOnRefreshListener { loadNews() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupProductAds()
    }

    // --- MODIFIED TO SYNC WITH FIRESTORE ---
    private fun loadNews() {
        if (!binding.swiprRefresh.isRefreshing) {
            binding.progress.isVisible = true
        }

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val country = prefs.getString(KEY_COUNTRY, "us") ?: "us"
        val category = intent.getStringExtra("ApiCategory") ?: "general"

        // Use a coroutine to fetch favorites from Firestore in the background
        lifecycleScope.launch {
            val favoriteArticles = withContext(Dispatchers.IO) {
                FirestoreManager.getFavorites()
            }

            // After getting favorites, make the API call for news
            service.getNews(
                apiKey = "f08c4601a830b52b41356f98b554ab94", // Your API Key
                country = country,
                category = category
            ).enqueue(object : Callback<News> {
                override fun onResponse(call: Call<News>, response: Response<News>) {
                    if (response.isSuccessful) {
                        val apiArticles = response.body()?.data ?: arrayListOf()
                        // Pass both lists to be synced before displaying
                        showNews(apiArticles, favoriteArticles)
                    } else {
                        Log.e("MainActivity", "API Error: ${response.errorBody()?.string()}")
                        Toast.makeText(this@MainActivity, "Failed to load news.", Toast.LENGTH_SHORT).show()
                    }
                    binding.progress.isVisible = false
                    binding.swiprRefresh.isRefreshing = false
                }

                override fun onFailure(call: Call<News>, t: Throwable) {
                    Log.e("MainActivity", "API Failure: ${t.message}", t)
                    Toast.makeText(this@MainActivity, "An error occurred.", Toast.LENGTH_SHORT).show()
                    binding.progress.isVisible = false
                    binding.swiprRefresh.isRefreshing = false
                }
            })
        }
    }

    // --- MODIFIED TO SYNC THE LISTS BEFORE DISPLAYING ---
    private fun showNews(apiArticles: ArrayList<Article>, favoriteArticles: List<Article>) {
        // Create a map of favorite URLs to their Firestore IDs for fast checking
        val favoriteUrlToIdMap = favoriteArticles.associateBy({ it.url }, { it.firestoreId })

        // Loop through each article from the API
        apiArticles.forEach { apiArticle ->
            // Check if this article's URL exists in our map of favorites
            if (favoriteUrlToIdMap.containsKey(apiArticle.url)) {
                // If it's a favorite, update its state and give it the correct Firestore ID
                apiArticle.isFavorite = true
                apiArticle.firestoreId = favoriteUrlToIdMap[apiArticle.url]
            }
        }

        // The adapter now receives a list where favorites are correctly marked
        val adapter = NewsAdapter(this, apiArticles)
        binding.newsList.adapter = adapter
    }

    // -------- Menu handling --------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_favorites -> {
                startActivity(Intent(this, FavoritesActivity::class.java))
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
        val products = listOf(
            mapOf("image" to R.drawable.product_1, "price" to "$49.99", "url" to "https://www.amazon.com/s?k=headphones" ),
            mapOf("image" to R.drawable.product_2, "price" to "$129.50", "url" to "https://www.amazon.com/s?k=smart+watch" ),
            mapOf("image" to R.drawable.product_3, "price" to "$14.95", "url" to "https://www.amazon.com/s?k=bestseller+books" ),
            mapOf("image" to R.drawable.product_4, "price" to "$22.00", "url" to "https://www.amazon.com/s?k=coffee+mug" ),
            mapOf("image" to R.drawable.product_5, "price" to "$35.75", "url" to "https://www.amazon.com/s?k=indoor+plant" )
        )
        val container = findViewById<LinearLayout>(R.id.product_ads_container) ?: return
        val inflater = LayoutInflater.from(this)
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
