package com.lawrence.data.networking

import com.lawrence.data.networking.data.Endpoints
import com.lawrence.data.model.LatestRatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface PenguinNetworkService {
    @GET(Endpoints.GET_LATEST)
    suspend fun getLatestExchangeRate(
        @Query("app_id") appId: String,
        @Query("base") base: String,
        @Query("symbols") symbols: List<String>
    ): Response<LatestRatesResponse>
}