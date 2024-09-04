package com.lawrence.data.model

data class LatestRatesResponse(
    val base: String,
    val disclaimer: String,
    val license: String,
    val rates: Rates,
    val timestamp: Int
)