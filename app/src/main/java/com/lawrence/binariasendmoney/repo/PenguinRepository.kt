package com.lawrence.binariasendmoney.repo

import com.lawrence.binariasendmoney.networking.data.response.LatestRatesResponse
import retrofit2.Response

interface PenguinRepository {

    suspend fun getExchangeRate(appId: String, base: String, symbols: List<String>) : Response<LatestRatesResponse>
}