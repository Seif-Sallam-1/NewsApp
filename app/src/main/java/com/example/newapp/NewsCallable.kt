package com.example.newapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsCallable {
    @GET("news")
    fun getNews(
        @Query("access_key") apiKey: String,
        @Query("countries") country: String,
        @Query("categories") category: String,
        @Query("limit") limit: Int = 30
    ): Call<News>
}