package com.example.model

data class CalibrationPreset(
    val id: String,
    val name: String,
    val referenceA4: Double,
    val temperament: String,
    val isCustom: Boolean = true
)
