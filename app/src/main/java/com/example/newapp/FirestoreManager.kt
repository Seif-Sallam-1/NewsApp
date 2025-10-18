package com.example.newapp

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirestoreManager {

    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_FAVORITES = "favorites"

    private fun getCurrentUserId(): String? {
        return Firebase.auth.currentUser?.uid
    }

    // --- MODIFIED TO RETURN THE NEW DOCUMENT'S ID ---
    suspend fun addFavorite(article: Article): String? {
        val userId = getCurrentUserId() ?: return null
        val favoritesCollection = Firebase.firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)

        // Use .add() which returns a Task<DocumentReference>
        val documentReference = favoritesCollection.add(article).await()

        // Return the ID of the newly created document
        return documentReference.id
    }

    // This function is already correct
    suspend fun removeFavorite(firestoreId: String) {
        val userId = getCurrentUserId() ?: return
        Firebase.firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(firestoreId)
            .delete()
            .await()
    }

    // This function is also correct
    suspend fun getFavorites(): List<Article> {
        val userId = getCurrentUserId() ?: return emptyList()

        val snapshot = Firebase.firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(Article::class.java)?.apply {
                firestoreId = document.id
            }
        }
    }
}
