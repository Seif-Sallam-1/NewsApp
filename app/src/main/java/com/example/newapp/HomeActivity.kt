package com.example.newapp

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newapp.databinding.ActivityHomePageBinding
import com.example.newapp.databinding.AdProductItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.core.net.toUri

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupProductAds()

        val categories = arrayListOf(
            Category("Business", "business", R.drawable.business),
            Category("Entertainment", "entertainment", R.drawable.entertainment),
            Category("General", "general", R.drawable.general),
            Category("Health", "health", R.drawable.health),
            Category("Science", "science", R.drawable.science),
            Category("Technology", "technology", R.drawable.technology),
            Category("Sports", "sports", R.drawable.sports)
        )

        val adapter = CategoryAdapter(this, categories)
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoryRecyclerView.adapter = adapter
    }

    private fun setupProductAds() {
        val products = listOf(
            mapOf("image" to R.drawable.product_1, "price" to "$49.99", "url" to "https://www.amazon.com/s?k=headphones" ),
            mapOf("image" to R.drawable.product_2, "price" to "$129.50", "url" to "https://www.amazon.com/s?k=smart+watch" ),
            mapOf("image" to R.drawable.product_3, "price" to "$14.95", "url" to "https://www.amazon.com/s?k=bestseller+books" ),
            mapOf("image" to R.drawable.product_4, "price" to "$22.00", "url" to "https://www.amazon.com/s?k=coffee+mug" ),
            mapOf("image" to R.drawable.product_5, "price" to "$35.75", "url" to "https://www.amazon.com/s?k=indoor+plant" )
        )

        val container = binding.productAdsContainer
        val inflater = LayoutInflater.from(this)

        for (product in products) {
            val adItemBinding = AdProductItemBinding.inflate(inflater, container, false)
            adItemBinding.productImage.setImageResource(product["image"] as Int)
            adItemBinding.productPrice.text = product["price"] as String
            adItemBinding.root.setOnClickListener {
                val url = product["url"] as String
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
                }
            }
            container.addView(adItemBinding.root)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                startActivity(Intent(this, FavoritesActivity::class.java))
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
}
