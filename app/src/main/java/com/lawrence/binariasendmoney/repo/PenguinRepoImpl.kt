package com.lawrence.binariasendmoney.repo

import com.lawrence.binariasendmoney.networking.PenguinNetworkService
import com.lawrence.binariasendmoney.networking.data.response.LatestRatesResponse
import kotlinx.coroutines.CoroutineDispatcher
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