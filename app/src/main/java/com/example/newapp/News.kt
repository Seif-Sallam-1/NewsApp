package com.example.newapp

// --- FIX: REMOVED THE INCORRECT IMPORT ---
// import com.google.firebase.database.Exclude  <-- DELETE THIS LINE
import com.google.firebase.firestore.Exclude // <-- KEEP THIS LINE

data class News(
    val data: ArrayList<Article>
)

data class Article(
    val title: String? = null,
    val image: String? = null,
    val url: String? = null,

    // These annotations now work correctly because the import is fixed.
    @get:Exclude @set:Exclude var isFavorite: Boolean = false,
    @get:Exclude @set:Exclude var firestoreId: String? = null
) {
    // A no-argument constructor is required for Firestore.
    constructor() : this(null, null, null)
}
