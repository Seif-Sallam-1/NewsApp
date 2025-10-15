package com.example.newapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newapp.databinding.ActivityFavoritesBinding
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity( ) {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // Handle back button click in the toolbar
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Load favorites every time the user comes to this screen
        // to make sure it's up-to-date.
        loadFavorites()
    }

    private fun setupRecyclerView() {
        // Initialize with an empty list
        favoritesAdapter = NewsAdapter(this, arrayListOf())
        binding.favoritesRecyclerView.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
        }
    }

    private fun loadFavorites() {
        binding.progressBar.isVisible = true
        binding.emptyText.isVisible = false
        binding.favoritesRecyclerView.isVisible = false

        lifecycleScope.launch {
            val favoriteArticles = FirestoreManager.getFavorites()

            // Mark each article as a favorite for the star icon logic
            favoriteArticles.forEach { it.isFavorite = true }

            binding.progressBar.isVisible = false
            if (favoriteArticles.isEmpty()) {
                binding.emptyText.isVisible = true
            } else {
                binding.favoritesRecyclerView.isVisible = true
                favoritesAdapter.updateArticles(ArrayList(favoriteArticles))
            }
        }
    }
}
