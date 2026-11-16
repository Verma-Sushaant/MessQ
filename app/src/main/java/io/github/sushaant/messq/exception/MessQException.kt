package io.github.sushaant.messq.exception

sealed class MessQException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String = "Network connection failed. Please check your internet.", cause: Throwable? = null) : MessQException(message, cause)
    class AuthException(message: String = "Authentication failed. Please log in again.", cause: Throwable? = null) : MessQException(message, cause)
    class DatabaseException(message: String = "Database or caching operation failed.", cause: Throwable? = null) : MessQException(message, cause)
    class UnknownException(message: String = "An unexpected error occurred.", cause: Throwable? = null) : MessQException(message, cause)
}