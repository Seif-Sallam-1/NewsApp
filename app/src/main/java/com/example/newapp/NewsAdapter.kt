package com.example.newapp

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.newapp.databinding.ArticleListItemBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsAdapter(val a: Activity, private var articles: ArrayList<Article>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(val binding: ArticleListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val b = ArticleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(b)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]

        holder.binding.articleText?.text = article.title

        Glide
            .with(holder.binding.articleImage.context)
            .load(article.image)
            .error(R.drawable.broken_image)
            .transition(DrawableTransitionOptions.withCrossFade(1000))
            .into(holder.binding.articleImage)

        // --- FAVORITES LOGIC WITH THE DEFINITIVE FIX ---
        updateFavoriteIcon(holder.binding.favoriteButton, article.isFavorite)

        holder.binding.favoriteButton?.setOnClickListener {
            // Instantly update UI for responsiveness
            article.isFavorite = !article.isFavorite
            updateFavoriteIcon(holder.binding.favoriteButton, article.isFavorite)

            // Perform DB operation in the background
            CoroutineScope(Dispatchers.IO).launch {
                if (article.isFavorite) {
                    // We are ADDING a favorite. Call the function that returns the new ID.
                    val newId = FirestoreManager.addFavorite(article)

                    // CRUCIAL FIX: Update the article object in memory with the new ID.
                    withContext(Dispatchers.Main) {
                        article.firestoreId = newId
                    }
                } else {
                    // We are REMOVING a favorite. The ID will now be correct.
                    article.firestoreId?.let { id ->
                        FirestoreManager.removeFavorite(id)
                    }
                }
            }
        }

        // --- Other listeners (unchanged) ---
        val url = article.url
        holder.binding.articleCont.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, url?.toUri())
            a.startActivity(i)
        }

        holder.binding.shareFab?.setOnClickListener {
            ShareCompat
                .IntentBuilder(a)
                .setType("text/plain")
                .setChooserTitle("Share article with : ")
                .setText(url)
                .startChooser()
        }
    }

    private fun updateFavoriteIcon(fab: FloatingActionButton?, isFavorite: Boolean) {
        if (isFavorite) {
            fab?.setImageResource(R.drawable.ic_star_filled)
        } else {
            fab?.setImageResource(R.drawable.ic_star_outline)
        }
    }

    override fun getItemCount() = articles.size

    fun updateArticles(newArticles: ArrayList<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
