package com.example.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log

object FirebaseAnalyticsHelper {
    private const val TAG = "TuneZenAnalytics"

    fun logEvent(context: Context, eventName: String, params: Bundle? = null) {
        try {
            Log.d(TAG, "Log Event: $eventName, params: $params")
            // FirebaseAnalytics.getInstance(context).logEvent(eventName, params)
        } catch (e: Exception) {
            Log.w(TAG, "Analytics log skipped: ${e.message}")
        }
    }

    fun logTuningSession(context: Context, instrumentName: String, targetNote: String, inTune: Boolean) {
        val bundle = Bundle().apply {
            putString("instrument", instrumentName)
            putString("note", targetNote)
            putBoolean("in_tune", inTune)
        }
        logEvent(context, "tuning_session_completed", bundle)
    }

    fun logPresetChanged(context: Context, presetName: String) {
        val bundle = Bundle().apply {
            putString("preset_name", presetName)
        }
        logEvent(context, "preset_selected", bundle)
    }
}
