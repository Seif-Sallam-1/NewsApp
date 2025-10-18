package com.example.newapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// ✅ Represents one saved favorite article
data class FavoriteArticle(
    val id: String?,
    val title: String?,
    val description: String?,
    val imageUrl: String?
)

// ✅ SQLite helper for saving favorites locally (offline)
class LocalFavoritesDatabase(context: Context) :
    SQLiteOpenHelper(context, "favorites.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create table when database is first created
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorites (
                id TEXT PRIMARY KEY,
                title TEXT,
                description TEXT,
                imageUrl TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // If schema changes, recreate the table
        db.execSQL("DROP TABLE IF EXISTS favorites")
        onCreate(db)
    }

    // ✅ Add or update a favorite article
    fun addFavorite(article: FavoriteArticle) {
        val values = ContentValues().apply {
            put("id", article.id)
            put("title", article.title)
            put("description", article.description)
            put("imageUrl", article.imageUrl)
        }
        writableDatabase.insertWithOnConflict(
            "favorites",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // ✅ Get all favorite articles
    fun getFavorites(): List<FavoriteArticle> {
        val list = mutableListOf<FavoriteArticle>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM favorites", null)

        if (cursor.moveToFirst()) {
            do {
                val article = FavoriteArticle(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("imageUrl"))
                )
                list.add(article)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    // ✅ Delete one favorite by ID
    fun deleteFavorite(id: String?) {
        writableDatabase.delete("favorites", "id = ?", arrayOf(id))
    }
    fun deleteAllFavorite() {
        writableDatabase.delete("favorites", null, null)
    }

    // ✅ Check if an article exists in favorites
    fun isFavorite(id: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM favorites WHERE id = ?",
            arrayOf(id)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}
