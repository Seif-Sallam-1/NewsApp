package com.example.newapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class FavoriteArticle(
    val id: String?,
    val title: String?,
    val description: String?,
    val imageUrl: String?
)

class LocalFavoritesDatabase(context: Context) :
    SQLiteOpenHelper(context, "favorites.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
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
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_deletions (
                url TEXT PRIMARY KEY
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pending_deletions (
                    url TEXT PRIMARY KEY
                )
                """.trimIndent()
            )
        } else {
            db.execSQL("DROP TABLE IF EXISTS favorites")
            db.execSQL("DROP TABLE IF EXISTS pending_deletions")
            onCreate(db)
        }
    }
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

    fun deleteFavorite(id: String?) {
        writableDatabase.delete("favorites", "id = ?", arrayOf(id))
    }

    fun deleteAllFavorites() {
        writableDatabase.delete("favorites", null, null)
    }

    fun addPendingDeletion(url: String) {
        val values = ContentValues().apply {
            put("url", url)
        }
        writableDatabase.insertWithOnConflict(
            "pending_deletions",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getPendingDeletions(): List<String> {
        val list = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM pending_deletions", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow("url")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun clearPendingDeletions() {
        writableDatabase.delete("pending_deletions", null, null)
    }
}
