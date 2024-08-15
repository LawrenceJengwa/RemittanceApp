package com.lawrence.binariasendmoney.ui.screens

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenguinTransactionScreen(
    viewModel: TransactionsViewModel,
    setSelected: (selected: String) -> Unit,
) {
    val uiState by viewModel.sendTransactionUiState.collectAsState()
    val scaffoldState = rememberScrollState()

    Scaffold(
        topBar = {
            when {
                uiState.isLoading -> {
                    penguinAppBar(title = stringResource(R.string.transactions_title))
                }

                uiState.isError -> {
                    penguinAppBar(
                        title = stringResource(id = R.string.error)
                        )
                }

                uiState.isSuccess -> {
                    penguinAppBar(title =
                    stringResource(id = R.string.transactions_title))
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ProgressView()
            }

            uiState.isError -> {
                Text(
                    text = stringResource(id = R.string.error), color = Color.Red,
                    modifier = Modifier.padding(bottom = 10.dp), fontSize = 16.sp
                )
            }

            uiState.isSuccess -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(state = scaffoldState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BinariaScreenContent(
                        viewModel = viewModel,
                        setSelected = setSelected
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun penguinAppBar(title: String) {
    TopAppBar(
        title = {
            Text(title)
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinariaScreenContent(
    viewModel: TransactionsViewModel,
    setSelected: (selected: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.transaction), color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp), fontSize = 16.sp
        )
        OutlinedTextField(
            value = viewModel.firstName,
            onValueChange = { firstname -> viewModel.updateFirstname(firstname) },
            label = { Text(text = stringResource(id = R.string.first_name)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.surname,
            onValueChange = { surname -> viewModel.updateSurname(surname) },
            label = { Text(text = stringResource(id = R.string.surname)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        )

        Text(
            text = stringResource(id = R.string.recipient_country),
            modifier = Modifier.padding(bottom = 10.dp), fontSize = 16.sp
        )

        CountriesRadioGroup(viewModel.countriesList(), setSelected)

        val interactionSource = remember { MutableInteractionSource() }

        var isValidPhoneNumber by remember {
            mutableStateOf(false)
        }
        var recipientPhoneNumber by remember {
            mutableStateOf("")
        }

        val selectedCountry = viewModel.getSelected().value!!
        OutlinedTextField(
            value = viewModel.phoneNumber,
            onValueChange = { phoneNumber ->
                recipientPhoneNumber = phoneNumber
                viewModel.updatePhoneNumber(phoneNumber)
                isValidPhoneNumber = viewModel.isValidPhoneByCountry(
                    phoneNumber = phoneNumber,
                    selectedCountry = selectedCountry
                )
            },
            label = { Text(text = stringResource(id = R.string.recipient_cellphone)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            interactionSource = interactionSource,
            leadingIcon =
            {
                Text(
                    text = viewModel.getValidCountryCode(viewModel.phoneNumberPrefix),
                    color = Color.Black,
                    modifier = Modifier.padding(start = 24.dp, end = 8.dp)
                )

            }
        )

        if (!isValidPhoneNumber && recipientPhoneNumber.isNotEmpty()) {
            Text(text = stringResource(id = R.string.enter_valid_phone_number), color = Color.Red)
        }

        var isValidBinary by remember {
            mutableStateOf(false)
        }

        var textInput by remember {
            mutableStateOf("")
        }

        OutlinedTextField(
            value = viewModel.amount,
            onValueChange = { amount ->
                textInput = amount
                viewModel.updateRecipientAmountBasedOnTheSenderAmount(amount)
                isValidBinary = viewModel.isBinaryInput(amount)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(text = stringResource(id = R.string.sender_amount)) },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        )
        if (!isValidBinary && textInput.isNotEmpty()) {
            Text(text = stringResource(id = R.string.validation_error_message), color = Color.Red)
        }

        var converted by remember {
            mutableStateOf("")
        }

        OutlinedTextField(
            value = viewModel.convertedAmount,
            onValueChange = { amount ->
                converted = amount
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text(text = stringResource(id = R.string.receiver_amount)) },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        )
    }
    val context = LocalContext.current
    val sending = stringResource(id = R.string.sending_transaction)

    Button(
        onClick = {
            if (viewModel.validateFields()) Toast.makeText(
                context,
                sending,
                Toast.LENGTH_LONG
            ).show()
        },
        modifier = Modifier
            .padding(all = 16.dp)
            .fillMaxWidth()
    ) {
        Text(text = stringResource(id = R.string.send))
    }
}

@Composable
fun ProgressView(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun CountriesRadioGroup(
    countries: List<String>,
    setSelected: (selected: String) -> Unit
) {
    var selected by rememberSaveable { mutableStateOf("") }
    (Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        countries.take(countries.size).forEach { country ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == country,
                    onClick = {
                        selected = country
                        setSelected(country)
                    },
                    enabled = true,
                )
                Text(
                    text = country,
                    modifier = Modifier.padding(all = 12.dp)
                )
            }
        }
    })
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BinariaSendMoneyTheme {
        //BinariaScreen()
        //TODO: Build preview dummy data for UI testing
    }
}