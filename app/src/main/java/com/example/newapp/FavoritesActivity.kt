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
        enableEdgeToEdge()
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.title = "Favorites"
        setupProductAds()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        localDb = LocalFavoritesDatabase(this)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = NewsAdapter(this, arrayListOf()) { article ->
            removeFavorite(article)
        }
        binding.favoritesRecyclerView.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
        }
    }

    private fun removeFavorite(article: Article) {
        val currentList = (binding.favoritesRecyclerView.adapter as? NewsAdapter)?.getArticles()?.toMutableList()
        if (currentList != null && currentList.remove(article)) {
            (binding.favoritesRecyclerView.adapter as? NewsAdapter)?.updateArticles(currentList as ArrayList<Article>)
        }
        if (currentList.isNullOrEmpty()) {
            binding.emptyText.isVisible = true
        }

        lifecycleScope.launch(Dispatchers.IO) {
            article.url?.let { url ->
                localDb.deleteFavorite(url)

                if (isNetworkAvailable()) {
                    article.firestoreId?.let { FirestoreManager.removeFavorite(it) }
                } else {
                    localDb.addPendingDeletion(url)
                }
            }
        }
        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
    }
    private fun loadFavorites() {
        binding.progressBar.isVisible = true
        binding.emptyText.isVisible = false
        binding.favoritesRecyclerView.isVisible = false

        lifecycleScope.launch {
            if (isNetworkAvailable()) {
                withContext(Dispatchers.IO) {
                    val pendingDeletionUrls = localDb.getPendingDeletions()
                    if (pendingDeletionUrls.isNotEmpty()) {
                        val firestoreFavorites = FirestoreManager.getFavorites()
                        val favoritesToDelete = firestoreFavorites.filter { fav ->
                            pendingDeletionUrls.contains(fav.url)
                        }
                        favoritesToDelete.forEach { favToDelete ->
                            favToDelete.firestoreId?.let { FirestoreManager.removeFavorite(it) }
                        }
                        localDb.clearPendingDeletions()
                    }
                }
                val favoriteArticles = withContext(Dispatchers.IO) {
                    val firestoreFavorites = FirestoreManager.getFavorites()
                    localDb.deleteAllFavorites()
                    firestoreFavorites.forEach { article ->
                        localDb.addFavorite(
                            FavoriteArticle(
                                id = article.url ?: "", title = article.title,
                                imageUrl = article.image, description = article.description
                            )
                        )
                        article.isFavorite = true
                    }
                    firestoreFavorites
                }
                displayFavorites(favoriteArticles)

            } else {
                Toast.makeText(this@FavoritesActivity, "Offline mode: Showing saved favorites", Toast.LENGTH_SHORT).show()
                val favoriteArticles = withContext(Dispatchers.IO) {
                    localDb.getFavorites().map { fav ->
                        Article(
                            url = fav.id, title = fav.title, description = fav.description,
                            image = fav.imageUrl, isFavorite = true, firestoreId = null
                        )
                    }
                }
                displayFavorites(favoriteArticles)
            }
        }
    }
    private fun displayFavorites(articles: List<Article>) {
        binding.progressBar.isVisible = false
        if (articles.isEmpty()) {
            binding.emptyText.isVisible = true
            binding.favoritesRecyclerView.isVisible = false
        } else {
            binding.emptyText.isVisible = false
            binding.favoritesRecyclerView.isVisible = true
            favoritesAdapter.updateArticles(ArrayList(articles))
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
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
        val products = listOf(
            mapOf("image" to R.drawable.product_1, "price" to "$49.99", "url" to "https://www.amazon.com/s?k=headphones"  ),
            mapOf("image" to R.drawable.product_2, "price" to "$129.50", "url" to "https://www.amazon.com/s?k=smart+watch"  ),
            mapOf("image" to R.drawable.product_3, "price" to "$14.95", "url" to "https://www.amazon.com/s?k=bestseller+books"  ),
            mapOf("image" to R.drawable.product_4, "price" to "$22.00", "url" to "https://www.amazon.com/s?k=coffee+mug"  ),
            mapOf("image" to R.drawable.product_5, "price" to "$35.75", "url" to "https://www.amazon.com/s?k=indoor+plant"  )
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



