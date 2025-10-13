package com.example.newapp

import retrofit2.Call
import retrofit2.http.GET

interface NewsCallable {
    @GET("/v2/top-headlines?country=us&category=general&apiKey=82eaadb0d6fa454fbdf68ce26a6378d6&pageSize=30")
    fun getNews(): Call<News>
}