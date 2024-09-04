package com.lawrence.binariasendmoney.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawrence.data.networking.data.Endpoints.APP_ID
import com.lawrence.data.model.Rates
import com.lawrence.binariasendmoney.repo.PenguinRepository
import com.lawrence.binariasendmoney.utility.Constants.BASE_CURRENCY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: PenguinRepository
) : ViewModel() {

    var _sendTransactionUiState = MutableStateFlow(ViewState())
    var sendTransactionUiState: StateFlow<ViewState> = _sendTransactionUiState.asStateFlow()

    private var selected = MutableLiveData("")

    fun getSelected() = selected

    var amount by mutableStateOf("")
        private set

    private fun updateAmount(input: String) {
        amount = input
    }

    var firstName by mutableStateOf("")
        private set

    fun updateFirstname(input: String) {
        firstName = input
    }

    var surname by mutableStateOf("")
        private set

    fun updateSurname(input: String) {
        surname = input
    }

    var phoneNumberPrefix by mutableStateOf("")
        private set

    private fun updatePhoneNumberPrefix(prefix: String) {
        phoneNumberPrefix = prefix
    }

    var phoneNumber by mutableStateOf("")
        private set

    fun updatePhoneNumber(phone: String) {
        phoneNumber = phone
    }

    var convertedAmount by mutableStateOf("")
        private set

    private fun updateConvertAmount(amount: String) {
        convertedAmount = amount
    }

    suspend fun scopeFunc() {
        CoroutineScope(Dispatchers.IO).launch {  }
        supervisorScope {

        }
    }

    fun getLatestExchangeRate() {
        val currencies = listOf("KES", "NGN", "TZS", "UGX")
        _sendTransactionUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getExchangeRate(APP_ID, BASE_CURRENCY, currencies)
            _sendTransactionUiState.update { it.copy(isLoading = false) }
            if (result.isSuccessful) {
                    val data = result.body()?.rates
                    if (data != null) {
                        updateUIWithRates(data = data)
                    } else {
                        _sendTransactionUiState.update { it.copy(isError = true) }
                }
            } else {
                _sendTransactionUiState.update { it.copy(isError = true) }
            }
        }
    }

    fun updateUIWithRates(data: Rates) {
        val kes = data.KES
        val ngn = data.NGN
        val tzn = data.TZS
        val ugn = data.UGX
        _sendTransactionUiState.update { it.copy(
            isSuccess = true,
            kenyanShilling = kes,
            nigerianNaira = ngn,
            tanzanianShilling = tzn,
            ugandanShilling = ugn)
        }
    }

    fun setSelected(selectedCountry: String) {
        _sendTransactionUiState.update { currentState ->
            currentState.copy(setSelected = selectedCountry)
        }
        selected.value = selectedCountry
        updatePhoneNumberPrefix(selectedCountry)
    }


    fun countriesList() = listOf("Kenya", "Nigeria", "Tanzania", "Uganda")

    fun getValidCountryCode(selectedCountry: String): String {

        when (selectedCountry) {
            "Kenya" -> {
                return "+254"
            }

            "Nigeria" -> {
                return "+234"
            }

            "Tanzania" -> {
                return "+255"
            }

            "Uganda" -> {
                return "+256"
            }
        }
        return ""
    }

    fun isValidPhoneByCountry(selectedCountry: String, phoneNumber: String): Boolean {
        when (selectedCountry) {
            "Kenya" -> {
                if (phoneNumber.matches(Regex("\\d{9}"))) {
                    return true
                }
            }

            "Nigeria" -> {
                if (phoneNumber.matches(Regex("\\d{7}"))) {
                    return true
                }
            }

            "Tanzania" -> {
                if (phoneNumber.matches(Regex("\\d{9}"))) {
                    return true
                }
            }

            "Uganda" -> {
                if (phoneNumber.matches(Regex("\\d{7}"))) {
                    return true
                }
            }
        }
        return false
    }

    fun updateRecipientAmountBasedOnTheSenderAmount(amount: String) {
        if (isBinaryInput(amount)) {
            val exchangeAmount = getExchangeAmount(amount)
            if (isOddNumberOfCharacters(exchangeAmount.length)) {
                updateConvertAmount("0${getExchangeAmount(amount)}")
            } else {
                updateConvertAmount(getExchangeAmount(amount))
            }
        } else {
            updateConvertAmount("")
        }
    }

    private fun isOddNumberOfCharacters(value: Int) = value % 2 == 1

    private fun getExchangeAmount(amount: String): String {
        val exchangeAmount = calculateExchangeAmount(amount, getSelected().value!!)
        return exchangeAmountToBinary(exchangeAmount)
    }

    fun exchangeAmountToBinary(input: Double): String {
        if (input.isNaN() || input.isInfinite()) {
            throw IllegalArgumentException("Input is not a finite number")
        }
        return Integer.toBinaryString(input.toInt())
    }

    private fun calculateExchangeAmount(binary: String, selectedCountry: String): Double {
        val exchangeRate = when (selectedCountry) {
            "Kenya" -> _sendTransactionUiState.value.kenyanShilling
            "Nigeria" -> _sendTransactionUiState.value.nigerianNaira
            "Tanzania" -> _sendTransactionUiState.value.tanzanianShilling
            "Uganda" -> _sendTransactionUiState.value.ugandanShilling
            else -> 0.0
        }
        return binaryToDouble(binary) * exchangeRate
    }

    private fun binaryToDouble(binary: String): Double {
        if (binary.isEmpty() || !isBinaryInput(binary)) {
            return 0.0
        }
        return binary.toInt(2).toDouble()
    }

    fun isBinaryInput(input: String): Boolean {
        val binaryPattern = Regex("^[01]+\$")
        updateAmount(input)
        return binaryPattern.matches(input)
    }

    fun validateFields(): Boolean {
        val selectedCountry = getSelected().value.toString()
        return firstName.isNotEmpty() &&
                surname.isNotEmpty() &&
                amount.isNotEmpty() &&
                phoneNumber.isNotEmpty()
                && isValidPhoneByCountry(selectedCountry, phoneNumber)
    }

    data class ViewState(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val isSuccess: Boolean = false,
        val setSelected: String = "",
        val kenyanShilling: Double = 0.0,
        val nigerianNaira: Double = 0.0,
        val tanzanianShilling: Double = 0.0,
        val ugandanShilling: Double = 0.0
    )
}