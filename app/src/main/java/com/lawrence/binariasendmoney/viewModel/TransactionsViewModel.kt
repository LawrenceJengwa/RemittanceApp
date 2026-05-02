package com.lawrence.binariasendmoney.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawrence.binariasendmoney.domain.ConvertBinaryUseCase
import com.lawrence.data.networking.data.Endpoints.APP_ID
import com.lawrence.binariasendmoney.repo.PenguinRepository
import com.lawrence.binariasendmoney.utility.Constants.BASE_CURRENCY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: PenguinRepository,
    private val convertBinaryUseCase: ConvertBinaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewState())
    val uiState: StateFlow<ViewState> = _uiState.asStateFlow()

    fun onFirstNameChange(name: String) {
        _uiState.update { it.copy(firstName = name) }
    }

    fun onSurnameChange(surname: String) {
        _uiState.update { it.copy(surname = surname) }
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun onCountrySelected(country: String) {
        _uiState.update { it.copy(selectedCountry = country) }
        // Trigger conversion update when country changes
        updateConvertedAmount(_uiState.value.amount, country)
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
        updateConvertedAmount(amount, _uiState.value.selectedCountry)
    }

    private fun updateConvertedAmount(binaryAmount: String, country: String) {
        if (isBinaryInput(binaryAmount) && country.isNotEmpty()) {
            val converted = calculateConvertedBinaryAmount(binaryAmount, country)
            _uiState.update { it.copy(convertedAmount = converted) }
        } else {
            _uiState.update { it.copy(convertedAmount = "") }
        }
    }

    private fun calculateConvertedBinaryAmount(binary: String, country: String): String {
        val currencyCode = getCurrencyCode(country)
        val rate = _uiState.value.rates[currencyCode] ?: 0.0
        return convertBinaryUseCase(binary, rate)
    }

    private fun getCurrencyCode(country: String) = when (country) {
        "Kenya" -> "KES"
        "Nigeria" -> "NGN"
        "Tanzania" -> "TZS"
        "Uganda" -> "UGX"
        else -> ""
    }

    fun getLatestExchangeRate() {
        val currencies = listOf("KES", "NGN", "TZS", "UGX")
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = repository.getExchangeRate(APP_ID, BASE_CURRENCY, currencies)
                if (result.isSuccessful) {
                    val rates = result.body()?.rates ?: emptyMap()
                    _uiState.update { it.copy(isLoading = false, rates = rates) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to fetch rates") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    fun isBinaryInput(input: String): Boolean {
        return input.all { it == '0' || it == '1' } && input.isNotEmpty()
    }

    fun getValidCountryCode(country: String) = when (country) {
        "Kenya" -> "+254"
        "Nigeria" -> "+234"
        "Tanzania" -> "+255"
        "Uganda" -> "+256"
        else -> ""
    }

    fun isValidPhone(country: String, phone: String): Boolean {
        return when (country) {
            "Kenya", "Tanzania" -> phone.matches(Regex("\\d{9}"))
            "Nigeria", "Uganda" -> phone.matches(Regex("\\d{7}"))
            else -> false
        }
    }

    fun validateFields(): Boolean {
        val s = _uiState.value
        return s.firstName.isNotEmpty() &&
                s.surname.isNotEmpty() &&
                isBinaryInput(s.amount) &&
                isValidPhone(s.selectedCountry, s.phoneNumber)
    }

    data class ViewState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val firstName: String = "",
        val surname: String = "",
        val phoneNumber: String = "",
        val amount: String = "",
        val convertedAmount: String = "",
        val selectedCountry: String = "Kenya",
        val rates: Map<String, Double> = emptyMap()
    )

    fun countriesList() = listOf("Kenya", "Nigeria", "Tanzania", "Uganda")
}
