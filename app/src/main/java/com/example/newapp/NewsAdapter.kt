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

class NewsAdapter(val a : Activity, val articals: ArrayList<Article>):
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(val binding : ArticleListItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
      val b = ArticleListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NewsViewHolder(b)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.binding.articleText?.text = articals[position].title

        Glide
            .with(holder.binding.articleImage.context)
            .load(articals[position].image)
            .error(R.drawable.broken_image)
            .transition(DrawableTransitionOptions.withCrossFade(1000))
            .into(holder.binding.articleImage)

        val url = articals[position].url

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

    override fun getItemCount()=articals.size

}