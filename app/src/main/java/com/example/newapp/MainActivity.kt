package com.example.newapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.newapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
         binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadNews()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.swiprRefresh.setOnRefreshListener {
            loadNews()
        }
    }
    private fun loadNews(){
        val category = intent.getStringExtra("ApiCategory")?: "general"
        val retrofit = Retrofit
            .Builder()
            .baseUrl("https://newsapi.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val c = retrofit.create(NewsCallable::class.java)
        c.getNews(category).enqueue(object : Callback<News>{
            override fun onResponse(call: Call<News?>, response: Response<News?>) {
                val news = response.body()
                val article = news?.articles!!

                article.removeAll {
                    it.title=="[Removed]"
                }
//                Log.d("trace","Articles:$article")
                showNews(article)
                binding.progress.isVisible=false
                binding.swiprRefresh.isRefreshing=false
            }

            override fun onFailure(call: Call<News?>, t: Throwable) {
                Log.d("trace","Error:${t.message}")
                binding.progress.isVisible=false
                binding.swiprRefresh.isRefreshing=false
            }
        })
    }
    private fun showNews(articles : ArrayList<Artical>){
        val  adapter = NewsAdapter(this ,articles)
        binding.newsList.adapter=adapter

    }
}