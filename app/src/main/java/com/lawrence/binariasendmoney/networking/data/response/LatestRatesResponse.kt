package com.lawrence.binariasendmoney.networking.data.response

data class LatestRatesResponse(
    val base: String,
    val disclaimer: String,
    val license: String,
    val rates: Rates,
    val timestamp: Int
)