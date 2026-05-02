package com.lawrence.binariasendmoney.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lawrence.binariasendmoney.R
import com.lawrence.binariasendmoney.ui.theme.BinariaSendMoneyTheme
import com.lawrence.binariasendmoney.viewModel.TransactionsViewModel

@Composable
fun PenguinTransactionScreen(
    viewModel: TransactionsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    PenguinTransactionContent(
        uiState = uiState,
        onFirstNameChange = viewModel::onFirstNameChange,
        onSurnameChange = viewModel::onSurnameChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onAmountChange = viewModel::onAmountChange,
        onCountrySelected = viewModel::onCountrySelected,
        onSendClick = {
            if (viewModel.validateFields()) {
                // Handle send
            }
        },
        getValidCountryCode = viewModel::getValidCountryCode,
        isValidPhone = viewModel::isValidPhone,
        isBinaryInput = viewModel::isBinaryInput
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenguinTransactionContent(
    uiState: TransactionsViewModel.ViewState,
    onFirstNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCountrySelected: (String) -> Unit,
    onSendClick: () -> Unit,
    getValidCountryCode: (String) -> String,
    isValidPhone: (String, String) -> Boolean,
    isBinaryInput: (String) -> Boolean
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transactions_title)) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                ProgressView()
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.transaction),
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = uiState.firstName,
                        onValueChange = onFirstNameChange,
                        label = { Text(stringResource(R.string.first_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.surname,
                        onValueChange = onSurnameChange,
                        label = { Text(stringResource(R.string.surname)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(R.string.recipient_country),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    CountriesRadioGroup(
                        countries = listOf("Kenya", "Nigeria", "Tanzania", "Uganda"),
                        selectedCountry = uiState.selectedCountry,
                        onCountrySelected = onCountrySelected
                    )

                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        label = { Text(stringResource(R.string.recipient_cellphone)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Text(
                                text = getValidCountryCode(uiState.selectedCountry),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        isError = uiState.phoneNumber.isNotEmpty() && !isValidPhone(uiState.selectedCountry, uiState.phoneNumber)
                    )
                    if (uiState.phoneNumber.isNotEmpty() && !isValidPhone(uiState.selectedCountry, uiState.phoneNumber)) {
                        Text(
                            text = stringResource(R.string.enter_valid_phone_number),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = onAmountChange,
                        label = { Text(stringResource(R.string.sender_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.amount.isNotEmpty() && !isBinaryInput(uiState.amount)
                    )
                    if (uiState.amount.isNotEmpty() && !isBinaryInput(uiState.amount)) {
                        Text(
                            text = stringResource(R.string.validation_error_message),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = uiState.convertedAmount,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.receiver_amount)) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSendClick()
                            Toast.makeText(context, R.string.sending_transaction, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.firstName.isNotEmpty() && uiState.surname.isNotEmpty() &&
                                isBinaryInput(uiState.amount) && isValidPhone(uiState.selectedCountry, uiState.phoneNumber)
                    ) {
                        Text(stringResource(R.string.send))
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun CountriesRadioGroup(
    countries: List<String>,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        countries.forEach { country ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = country == selectedCountry,
                    onClick = { onCountrySelected(country) }
                )
                Text(text = country, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BinariaSendMoneyTheme {
        // Preview with dummy state
    }
}
