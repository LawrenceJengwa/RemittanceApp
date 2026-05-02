package com.lawrence.binariasendmoney

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lawrence.binariasendmoney.domain.ConvertBinaryUseCase
import com.lawrence.data.model.LatestRatesResponse
import com.lawrence.binariasendmoney.repo.PenguinRepository
import com.lawrence.binariasendmoney.viewModel.TransactionsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var repo: PenguinRepository

    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionsViewModel(repo, ConvertBinaryUseCase())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isBinaryInput should return true if valid binary input is passed`() {
        val validBinary = "101010"
        val result = viewModel.isBinaryInput(validBinary)
        assertTrue(result)
    }

    @Test
    fun `isBinaryInput should return false if invalid binary input is passed`() {
        val invalidBinary = "22213"
        val result = viewModel.isBinaryInput(invalidBinary)
        assertFalse(result)
    }

    @Test
    fun `isValidPhone for Kenya with valid phone number should return true`() {
        val isValid = viewModel.isValidPhone("Kenya", "123456789")
        assertTrue(isValid)
    }

    @Test
    fun `onCountrySelected should update selectedCountry in uiState`() {
        val selectedCountry = "Nigeria"
        viewModel.onCountrySelected(selectedCountry)
        assertEquals(selectedCountry, viewModel.uiState.value.selectedCountry)
    }

    @Test
    fun `getLatestExchangeRate success should update rates in uiState`() = runTest {
        val ratesData = mapOf(
            "KES" to 150.0,
            "NGN" to 800.0,
            "TZS" to 2500.0,
            "UGX" to 3800.0
        )
        val response = LatestRatesResponse(
            base = "USD",
            disclaimer = "",
            license = "",
            timestamp = 123456,
            rates = ratesData
        )
        whenever(repo.getExchangeRate(any(), any(), any())).thenReturn(
            Response.success(response)
        )

        viewModel.getLatestExchangeRate()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(ratesData, viewModel.uiState.value.rates)
    }

    @Test
    fun `onAmountChange should calculate convertedAmount correctly`() = runTest {
        // Setup rates
        val ratesData = mapOf("KES" to 10.0)
        val response = LatestRatesResponse("USD", "", "", ratesData, 1234)
        whenever(repo.getExchangeRate(any(), any(), any())).thenReturn(Response.success(response))
        
        viewModel.getLatestExchangeRate()
        viewModel.onCountrySelected("Kenya")
        
        // 10 in binary is 1010. 
        // 1010 (binary) = 10 (decimal)
        // 10 (decimal) * 10 (rate) = 100
        // 100 in binary is 1100100
        // length is 7 (odd), so should prepend 0 -> 01100100
        
        viewModel.onAmountChange("1010")
        
        assertEquals("01100100", viewModel.uiState.value.convertedAmount)
    }
}
