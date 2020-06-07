package aculix.core.helper

/**
 * An app wide wrapper class that is returned by the Livedata as a result
 * and can be used with when expression in an activity or fragment
 */
sealed class Result {

    object Loading : Result()

    data class  Error(val errorMessage: String) : Result()

    data class Success<T>(val data: T) : Result()
}