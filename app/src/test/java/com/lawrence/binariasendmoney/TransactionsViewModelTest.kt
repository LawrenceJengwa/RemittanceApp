package com.lawrence.binariasendmoney

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lawrence.data.model.LatestRatesResponse
import com.lawrence.binariasendmoney.repo.PenguinRepoImpl
import com.lawrence.binariasendmoney.viewModel.TransactionsViewModel
import com.lawrence.data.model.Rates
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import retrofit2.Response

class TransactionsViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repo: PenguinRepoImpl

    private lateinit var viewModel: TransactionsViewModel

    private lateinit var spyViewModel: TransactionsViewModel


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = TransactionsViewModel(repo)
        spyViewModel = spy(viewModel)
    }

    @Test
    fun `test doubleToBinary with a valid double input`() {
        val input = 22.2
        val expected = "10110"
        val result = viewModel.exchangeAmountToBinary(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test doubleToBinary with Infinite input`() {
        val input = Double.POSITIVE_INFINITY
        assertThrows(IllegalArgumentException::class.java) {
            viewModel.exchangeAmountToBinary(input)
        }
    }

    @Test
    fun `test doubleToBinary with zero input`() {
        val input = 0.0
        val expected = "0"
        val result = viewModel.exchangeAmountToBinary(input)
        assertEquals(expected, result)
    }

    @Test
    fun `isBinaryInput should return true if valid binary input is passed`() {
        val validBinary = "101010"
        val result = viewModel.isBinaryInput(validBinary)
        assertTrue(result)
    }

    @Test
    fun `isBinaryInput should return false if invalid binary input is passed`() {
        val validBinary = "22213"
        val result = viewModel.isBinaryInput(validBinary)
        assertFalse(result)
    }

    @Test
    fun `isBinaryInput should return false if empty binary input is passed`() {
        val validBinary = ""
        val result = viewModel.isBinaryInput(validBinary)
        assertFalse(result)
    }

    @Test
    fun `isBinaryInput should return false if non-binary input is passed`() {
        val validBinary = "abc"
        val result = viewModel.isBinaryInput(validBinary)
        assertFalse(result)
    }

    @Test
    fun `isValidPhoneByCountry for Kenya with valid phone number should return true`() {
        val isValid = viewModel.isValidPhoneByCountry("Kenya", "123456789")
        assertTrue(isValid)
    }

    @Test
    fun `isValidPhoneByCountry for Kenya with invalid phone number should return false`() {
        val isInvalid = viewModel.isValidPhoneByCountry("Kenya", "12345")
        assertFalse(isInvalid)
    }

    @Test
    fun `isValidPhoneByCountry for Nigeria with valid phone number should return true`() {
        val isValid = viewModel.isValidPhoneByCountry("Nigeria", "1234567")
        assertTrue(isValid)
    }

    @Test
    fun `isValidPhoneByCountry for Nigeria with invalid phone number should return false`() {
        val isInvalid = viewModel.isValidPhoneByCountry("Nigeria", "123456789")
        assertFalse(isInvalid)
    }

    @Test
    fun `isValidPhoneByCountry for Tanzania with valid phone number should return true`() {
        val isValid = viewModel.isValidPhoneByCountry("Tanzania", "123456789")
        assertTrue(isValid)
    }

    @Test
    fun `isValidPhoneByCountry for Tanzania with invalid phone number should return false`() {
        val isInvalid = viewModel.isValidPhoneByCountry("Tanzania", "12345")
        assertFalse(isInvalid)
    }

    @Test
    fun `isValidPhoneByCountry for Uganda with valid phone number should return true`() {
        val isValid = viewModel.isValidPhoneByCountry("Uganda", "1234567")
        assertTrue(isValid)
    }

    @Test
    fun `isValidPhoneByCountry for Uganda with invalid phone number should return false`() {
        val isInvalid = viewModel.isValidPhoneByCountry("Uganda", "123456789")
        assertFalse(isInvalid)
    }

    @Test
    fun `isValidPhoneByCountry with unsupported country`() {
        val isInvalid = viewModel.isValidPhoneByCountry("InvalidCountry", "1234567")
        assertFalse(isInvalid)
    }

    @Test
    fun `setSelected should update sendTransactionUiState to selectedSate if item is selected`() {
        val initialUiState = viewModel.sendTransactionUiState.value.copy(isSuccess = true)
        viewModel._sendTransactionUiState.value = initialUiState
        val selectedCountry = "Kenya"

        viewModel.setSelected(selectedCountry)

        val updatedUiState = viewModel.sendTransactionUiState.value
        assertEquals(selectedCountry, updatedUiState.setSelected)
    }

    @Test
    fun `setSelected should set selectedCountry value correctly`() {
        val selectedCountry = "Kenya"

        viewModel.setSelected(selectedCountry)

        val selectedValue = viewModel.getSelected().value
        assertEquals(selectedCountry, selectedValue)
    }


    @Test
    fun `getLatestExchangeRate success`() {
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
            whenever(repo.getExchangeRate(any(), any(), any())).thenReturn(
                Response.success(response)
            )

            spyViewModel.getLatestExchangeRate()

            val uiState = spyViewModel.sendTransactionUiState.value
            assert(uiState.isLoading)
        }
    }


    @Test
    fun `updateUIWithRates should set data passed correct and update sendTransactionUiState to success`() {
        val delta = 0.00
        val initialUiState = viewModel.sendTransactionUiState.value.copy(isLoading = true)
        viewModel._sendTransactionUiState.value = initialUiState

        val rates = Rates(10.0, 20.0, 30.0, 40.0)
        viewModel.updateUIWithRates(rates)

        val updatedUiState = viewModel._sendTransactionUiState.value
        assertTrue( updatedUiState.isSuccess)
        assertEquals(10.0, updatedUiState.kenyanShilling, delta)
        assertEquals(20.0, updatedUiState.nigerianNaira, delta)
        assertEquals(30.0, updatedUiState.tanzanianShilling, delta)
        assertEquals(40.0, updatedUiState.ugandanShilling, delta)
    }

}