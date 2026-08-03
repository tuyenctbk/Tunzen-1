package com.example.data

import com.example.audio.MusicUtils
import com.example.model.InstrumentPreset
import com.example.model.TuningString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TuningRepository(private val dao: TuningDao) {

    val customPresetsFlow: Flow<List<InstrumentPreset>> = dao.getAllCustomPresets().map { list ->
        list.map { parseEntityToPreset(it) }
    }

    val historyLogsFlow: Flow<List<TuningHistoryEntity>> = dao.getTuningHistory()

    suspend fun saveCustomPreset(preset: InstrumentPreset) {
        val entity = parsePresetToEntity(preset)
        dao.insertCustomPreset(entity)
    }

    suspend fun deleteCustomPreset(id: String) {
        dao.deleteCustomPresetById(id)
    }

    suspend fun logTuningSession(
        instrumentName: String,
        targetNote: String,
        detectedFrequency: Double,
        centsOffset: Double,
        inTune: Boolean
    ) {
        val log = TuningHistoryEntity(
            instrumentName = instrumentName,
            targetNote = targetNote,
            detectedFrequency = detectedFrequency,
            centsOffset = centsOffset,
            inTune = inTune
        )
        dao.insertHistoryLog(log)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    private fun parseEntityToPreset(entity: TuningPresetEntity): InstrumentPreset {
        val strings = entity.stringsData.split("|").mapNotNull { str ->
            val parts = str.split(":")
            if (parts.size >= 5) {
                TuningString(
                    name = parts[0],
                    noteName = parts[1],
                    octave = parts[2].toIntOrNull() ?: 4,
                    targetFrequency = parts[3].toDoubleOrNull() ?: 440.0,
                    stringIndex = parts[4].toIntOrNull() ?: 1
                )
            } else null
        }
        return InstrumentPreset(
            id = entity.id,
            name = entity.name,
            category = entity.category,
            strings = strings,
            isCustom = true
        )
    }

    private fun parsePresetToEntity(preset: InstrumentPreset): TuningPresetEntity {
        val stringsData = preset.strings.joinToString("|") { str ->
            "${str.name}:${str.noteName}:${str.octave}:${str.targetFrequency}:${str.stringIndex}"
        }
        return TuningPresetEntity(
            id = preset.id,
            name = preset.name,
            category = preset.category,
            stringsData = stringsData
        )
    }
}
