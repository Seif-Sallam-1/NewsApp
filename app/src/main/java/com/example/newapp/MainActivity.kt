package com.example.newapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager // Added import
import com.example.newapp.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PREFS = "user_prefs"
    private val KEY_COUNTRY = "country_code"

    // --- FIX: Create a single, reusable Retrofit instance ---
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

        // --- FIX: Set the LayoutManager for the RecyclerView ---
        binding.newsList.layoutManager = LinearLayoutManager(this)

        loadNews()
        binding.swiprRefresh.setOnRefreshListener {
            loadNews()
        }

        // Insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadNews() {
        // --- FIX: Show the progress bar before starting the request ---
        if (!binding.swiprRefresh.isRefreshing) {
            binding.progress.isVisible = true
        }

        // Read saved country from SharedPreferences (default = "us")
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val country = prefs.getString(KEY_COUNTRY, "us") ?: "us"

        // Read category (default = general)
        val category = intent.getStringExtra("ApiCategory") ?: "general"

        // API call using the single service instance
        service.getNews(
            apiKey = "f08c4601a830b52b41356f98b554ab94",
            country = country,
            category = category
        ).enqueue(object : Callback<News> {
            override fun onResponse(call: Call<News>, response: Response<News>) {
                if (response.isSuccessful) {
                    val news = response.body()
                    val articles = news?.data ?: arrayListOf()
                    showNews(articles)
                } else {
                    Log.e("MainActivity", "API Error Response: ${response.errorBody()?.string()}")
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

    private fun showNews(articles: ArrayList<Article>) {
        val adapter = NewsAdapter(this, articles)
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
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_favorites -> {
                // TODO: Add favorites screen later if needed
                Toast.makeText(this, "Favorites coming soon!", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout ->{
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

    // --- FIX: Removed onResume() to prevent double-loading news ---
}
