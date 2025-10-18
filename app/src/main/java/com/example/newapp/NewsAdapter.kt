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

class NewsAdapter(
    val activity: Activity,
    private var articles: ArrayList<Article>,
    private val onFavoriteClicked: (article: Article) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(val binding: ArticleListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ArticleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]

        holder.binding.articleText?.text = article.title

        Glide.with(holder.binding.articleImage.context)
            .load(article.image)
            .error(R.drawable.broken_image)
            .transition(DrawableTransitionOptions.withCrossFade(1000))
            .into(holder.binding.articleImage)

        updateFavoriteIcon(holder.binding.favoriteButton, article.isFavorite)
        holder.binding.favoriteButton?.setOnClickListener {
            onFavoriteClicked(article)
        }
        val url = article.url
        holder.binding.articleCont.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, url?.toUri())
            activity.startActivity(intent)
        }

        holder.binding.shareFab?.setOnClickListener {
            ShareCompat.IntentBuilder(activity)
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
    fun getArticles(): List<Article> {
        return articles
    }
}
