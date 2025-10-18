package com.example.newapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletions")
data class PendingDeletion(
    @PrimaryKey val url: String
)
