package com.lawrence.binariasendmoney.domain

import javax.inject.Inject

class ConvertBinaryUseCase @Inject constructor() {
    operator fun invoke(binary: String, rate: Double): String {
        if (binary.isEmpty() || binary.any { it != '0' && it != '1' }) return ""
        
        val decimalAmount = try {
            binary.toInt(2) * rate
        } catch (e: Exception) {
            0.0
        }
        
        val binaryResult = Integer.toBinaryString(decimalAmount.toInt())
        return if (binaryResult.length % 2 != 0) "0$binaryResult" else binaryResult
    }
}
