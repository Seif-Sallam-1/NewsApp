package com.example.newapp

import android.R
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsCallable {
    @GET("/v2/top-headlines?country=us&apiKey=82eaadb0d6fa454fbdf68ce26a6378d6&pageSize=30")
    fun getNews(@Query("category") category : String): Call<News>
}