package com.lawrence.data.model

data class LatestRatesResponse(
    val base: String,
    val disclaimer: String,
    val license: String,
    val rates: Map<String, Double>,
    val timestamp: Int
)
