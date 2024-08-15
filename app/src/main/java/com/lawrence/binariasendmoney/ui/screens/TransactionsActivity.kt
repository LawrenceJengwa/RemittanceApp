package com.lawrence.binariasendmoney.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lawrence.binariasendmoney.ui.theme.BinariaSendMoneyTheme
import com.lawrence.binariasendmoney.viewModel.TransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionsActivity : ComponentActivity() {
    private val viewModel by viewModels<TransactionsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setUpViewModel()
            BinariaSendMoneyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PenguinTransactionScreen(
                        viewModel = viewModel,
                        setSelected = {viewModel.setSelected(it)})
                }
            }
        }
    }

    private fun setUpViewModel() {
        viewModel.apply {
            getLatestExchangeRate()
        }
    }
}

