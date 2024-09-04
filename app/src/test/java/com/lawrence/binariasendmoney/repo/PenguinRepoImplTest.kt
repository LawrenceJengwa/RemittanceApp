import com.lawrence.data.networking.data.Endpoints.APP_ID
import com.lawrence.data.networking.data.Endpoints.BASE_URL
import com.lawrence.data.model.LatestRatesResponse
import com.lawrence.binariasendmoney.repo.PenguinRepoImpl
import com.lawrence.data.model.Rates
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class PenguinRepoImplTest {

    @Mock
    private lateinit var mockNetworkService: com.lawrence.data.networking.PenguinNetworkService

    private lateinit var repo: PenguinRepoImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repo = PenguinRepoImpl(mockNetworkService)
    }


    @Test
    fun `verify getExchangeRate is invoked`() {
        val currencies = listOf("KES,NGN,TZS,UGX")
        runBlocking {
            val ratesData = Rates(
                KES = 150.9,
                NGN = 787.0,
                TZS = 2503.0,
                UGX = 3784.022492
            )
            val response = LatestRatesResponse(
                base = "USD",
                disclaimer = "Usage subject to terms: https://openexchangerates.org/terms",
                license = "https://openexchangerates.org/license",
                timestamp = 1698836341,
                rates = ratesData
            )
            whenever(mockNetworkService.getLatestExchangeRate(any(), any(), any())).thenReturn(
                Response.success(response)
            )
            repo.getExchangeRate(APP_ID, BASE_URL, currencies)
            verify(mockNetworkService).getLatestExchangeRate(APP_ID, BASE_URL, currencies)
        }
    }

    @Test
    fun `getExchangeRate should return success when the service succeeds`() {
        val currencies = listOf("KES,NGN,TZS,UGX")
        runBlocking {
            val ratesData = Rates(
                KES = 150.9,
                NGN = 787.0,
                TZS = 2503.0,
                UGX = 3784.022492
            )
            val response = LatestRatesResponse(
                base = "USD",
                disclaimer = "Usage subject to terms: https://openexchangerates.org/terms",
                license = "https://openexchangerates.org/license",
                timestamp = 1698836341,
                rates = ratesData
            )
            whenever(mockNetworkService.getLatestExchangeRate(any(), any(), any())).thenReturn(
                Response.success(response)
            )
            val result = repo.getExchangeRate(APP_ID, BASE_URL, currencies)
            assertEquals(result.body(), response)
        }
    }
}
