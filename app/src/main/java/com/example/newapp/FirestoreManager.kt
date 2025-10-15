package com.example.newapp

// --- FIX: REMOVED DUPLICATE AND UNUSED IMPORTS ---
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object FirestoreManager {

    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_FAVORITES = "favorites"

    // Gets the currently logged-in user's ID
    private fun getCurrentUserId(): String? {
        return Firebase.auth.currentUser?.uid
    }

    // Adds an article to the current user's favorites
    suspend fun addFavorite(article: Article) {
        val userId = getCurrentUserId() ?: return // Exit if no user is logged in

        // --- FIX: Correct way to get the collection reference ---
        val favoritesCollection = Firebase.firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)

        // Firestore will auto-generate an ID for the document
        favoritesCollection.add(article).await()
    }

    // Removes an article from the current user's favorites
    suspend fun removeFavorite(firestoreId: String) {
        val userId = getCurrentUserId() ?: return

        // --- FIX: Correct way to get the document reference ---
        Firebase.firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(firestoreId)
            .delete()
            .await()
    }

    // Gets all favorite articles for the current user
    suspend fun getFavorites(): List<Article> {
        val userId = getCurrentUserId() ?: return emptyList() // Return empty list if no user

        // --- FIX: Correct way to get the collection reference ---
        val snapshot = Firebase.firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .get()
            .await()

        // Convert documents to Article objects and store their Firestore ID
        return snapshot.documents.mapNotNull { document ->
            document.toObject(Article::class.java)?.apply {
                firestoreId = document.id
            }
        }
    }
}
