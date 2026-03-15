package com.partitionsoft.bookshelf.domain.result

sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(val exception: Throwable) : Result<Nothing>()

    data object Loading : Result<Nothing>()

}

val <T> Result<T>.isLoading get() = this is Result.Loading
val <T> Result<T>.isError get() = this is Result.Error

fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}