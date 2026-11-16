package io.github.sushaant.messq.common

import io.github.sushaant.messq.exception.MessQException
import io.ktor.client.network.sockets.ConnectTimeoutException
import okio.IOException

// Standard state wrapper for UI
sealed interface Resource<out T> {
    data class Success<T> (val data: T): Resource<T>
    data class Error(val exception: MessQException): Resource<Nothing>
    object Loading: Resource<Nothing>
}

// Global safe-call Interceptor
inline fun <T> safeApiCall(apiCall: () -> T): Resource<T> {
    return try {
        Resource.Success(apiCall())
    } catch (e: MessQException) {
        Resource.Error(e)
    } catch (e: IOException) {
        Resource.Error(MessQException.NetworkException(cause = e))
    } catch (e: ConnectTimeoutException) {
        Resource.Error(MessQException.NetworkException("Connection timed out.", cause = e))
    } catch (e: Exception) {
        Resource.Error(MessQException.UnknownException(e.localizedMessage ?: "Unknow Error", cause = e))
    }
}