package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable

@Serializable
data class CaseData(
    val comparison: String,
    val output: String
)
