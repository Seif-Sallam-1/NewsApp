package com.example.newapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.newapp.data.FavoriteArticle
import com.example.newapp.data.LocalFavoritesDatabase
import com.example.newapp.databinding.ActivityFavoritesBinding
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesAdapter: NewsAdapter
    private lateinit var localDb: LocalFavoritesDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        setupRecyclerView()

        // Back button
        binding.topAppBar.setNavigationOnClickListener { finish() }

        // Load banner
        val bannerUrl = "https://example.com/banner.jpg"
        Glide.with(this)
            .load(bannerUrl)
            .into(binding.bannerImage)

        // Initialize local database
        localDb = LocalFavoritesDatabase(this)
    }

    override fun onResume() {
        super.onResume()
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
        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun loadFavorites() {
        binding.progressBar.isVisible = true
        binding.emptyText.isVisible = false
        binding.favoritesRecyclerView.isVisible = false

        lifecycleScope.launch {
            val favoriteArticles: List<Article> = if (isNetworkAvailable()) {
                val articles = FirestoreManager.getFavorites()
                articles.forEach { article ->
                    localDb.addFavorite(
                        FavoriteArticle(
                            id = article.url,
                            title = article.title,
                            description = "",
                            imageUrl = article.image
                        )
                    )
                    article.isFavorite = true
                }
                articles
            } else {
                localDb.getFavorites().map { fav ->
                    Article(
                        url = fav.id,
                        isFavorite = true,
                        title = fav.title,
                        image = fav.imageUrl
                    )
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

    private fun removeFavorite(article: Article) {
        localDb.deleteFavorite(article.url)
        loadFavorites()
    }
}
