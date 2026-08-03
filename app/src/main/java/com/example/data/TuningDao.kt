package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TuningDao {

    @Query("SELECT * FROM custom_presets ORDER BY name ASC")
    fun getAllCustomPresets(): Flow<List<TuningPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPreset(preset: TuningPresetEntity)

    @Query("DELETE FROM custom_presets WHERE id = :id")
    suspend fun deleteCustomPresetById(id: String)

    @Query("SELECT * FROM tuning_history ORDER BY timestamp DESC LIMIT 100")
    fun getTuningHistory(): Flow<List<TuningHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryLog(log: TuningHistoryEntity)

    @Query("DELETE FROM tuning_history")
    suspend fun clearHistory()
}
