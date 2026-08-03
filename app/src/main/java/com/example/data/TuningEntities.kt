package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_presets")
data class TuningPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val stringsData: String // Formatted string: "1st:E:4:329.63:1|2nd:B:3:246.94:2..."
)

@Entity(tableName = "tuning_history")
data class TuningHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val instrumentName: String,
    val targetNote: String,
    val detectedFrequency: Double,
    val centsOffset: Double,
    val inTune: Boolean
)
