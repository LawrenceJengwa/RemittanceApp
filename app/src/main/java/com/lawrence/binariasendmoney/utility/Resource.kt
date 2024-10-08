package com.lawrence.binariasendmoney.utility

import retrofit2.Response

sealed class Resource<T> (val data: T?, val message: String?) {
    class Success<T>(data: T?):  Resource<T> (data = data, message = null)
    class Error<T>(message: String?) : Resource<T> (data = null, message = message)
}