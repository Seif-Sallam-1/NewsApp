package com.example.newapp

data class News(
    val articles : ArrayList<Artical>
)

data class Artical (
   val  title: String,
   val  urlToImage : String,
   val  url : String ,
)
