package com.example.mytpu

data class OAuthRequest(
    val network: String,
    val accessToken: String,
    val email: String? = null
)

data class AuthTokens(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int = 86400
)

data class UserInfo(
    val id: String,
    val name: String,
    val email: String?,
    val group: String? = null // Добавили группу
)

data class UserSession(
    val name: String,
    val token: String,
    val email: String? = null,
    val group: String? = null
)

sealed class UiState {
    object Loading : UiState()
    object NotAuthenticated : UiState()
    data class Success(val user: UserSession) : UiState()
    data class Error(val message: String) : UiState()
}