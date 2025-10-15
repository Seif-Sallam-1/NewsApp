package com.example.newapp

data class News(
    val data: ArrayList<Article>
)

data class Article(
    val title: String?,
    val image: String?,
    val url: String?
)
