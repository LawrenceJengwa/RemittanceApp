package com.lawrence.binariasendmoney.repo

import com.lawrence.data.networking.PenguinNetworkService
import com.lawrence.data.model.LatestRatesResponse
import retrofit2.Response
import javax.inject.Inject

class PenguinRepoImpl @Inject constructor(
    private val networkService: PenguinNetworkService,
) : PenguinRepository {

    override suspend fun getExchangeRate(
        appId: String,
        base: String,
        symbols: List<String>
    ): Response<LatestRatesResponse> {
       return networkService.getLatestExchangeRate(appId, base, symbols)
    }
}