package com.example.newapp

import com.google.firebase.firestore.Exclude

data class News(
    val data: ArrayList<Article>
)

data class Article(
    val title: String? = null,
    val image: String? = null,
    val description: String?,
    val url: String? = null,

    @get:Exclude @set:Exclude var isFavorite: Boolean = false,
    @get:Exclude @set:Exclude var firestoreId: String? = null
) {
    constructor() : this(null, null, null)
}
