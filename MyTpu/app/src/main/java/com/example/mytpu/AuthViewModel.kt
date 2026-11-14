package com.example.mytpu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val api = ApiClient.api
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val isMockMode = true
    private val isMockModeML = false

    private var currentAccessToken: String? = null
    private var currentRefreshToken: String? = null

    init {
        // Проверка сохранёного токена при запуске приложения
        checkExistingToken()
    }

    fun retryAuth() {
        _uiState.value = UiState.Loading
        checkExistingToken()
    }

    //Первичный вход в систему
    fun oauthLogin(network: String, socialToken: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            if (isMockMode) {
                val mockTokens = AuthTokens(
                    access_token = "mock_access_token_12345",
                    refresh_token = "mock_refresh_token_67890",
                    expires_in = 86400
                )
                saveTokens(mockTokens)
                loadUserInfo(mockTokens.access_token)
                return@launch
            }

            //запрос к API
            try {
                val request = OAuthRequest(
                    network = network,
                    accessToken = socialToken
                )
                val response = api.oauthLogin(request)

                if (response.isSuccessful && response.body() != null) {
                    saveTokens(response.body()!!)
                    loadUserInfo(response.body()!!.access_token)
                } else {
                    handleError(response.code())
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Ошибка сети: ${e.message}")
            }
        }
    }

    //Получение данных пользователя
    private suspend fun loadUserInfo(token: String) {
        if (isMockMode) {
            _uiState.value = UiState.Success(
                UserSession(
                    name = "Иванов Иван Иванович",
                    token = token,
                    email = "ivanov@tpu.ru",
                    group = "ИВТ-1234"
                )
            )
            return
        }

        try {
            val response = api.getUserInfo("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val info = response.body()!!
                _uiState.value = UiState.Success(
                    UserSession(
                        name = info.name,
                        token = token,
                        email = info.email,
                        group = info.group
                    )
                )
            } else {
                _uiState.value = UiState.Error("Не удалось получить данные пользователя")
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error("Ошибка загрузки данных: ${e.message}")
        }
    }

    //Проверка токена при запуске
    private fun checkExistingToken() {
        viewModelScope.launch {
            if (isMockMode) {
                if (currentAccessToken != null) {
                    loadUserInfo(currentAccessToken!!)
                } else {
                    _uiState.value = UiState.NotAuthenticated
                }
                return@launch
            }

            val savedToken = getSavedAccessToken()
            if (savedToken != null) {
                try {
                    val response = api.validateToken(savedToken)
                    if (response.isSuccessful) {
                        loadUserInfo(savedToken) // Токен действителен
                    } else {
                        refreshExistingToken() // Токен устарел, попытка обновить
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.NotAuthenticated
                }
            } else {
                _uiState.value = UiState.NotAuthenticated
            }
        }
    }

    //Обновление токена
    private fun refreshExistingToken() {
        viewModelScope.launch {
            val refreshToken = getSavedRefreshToken()
            if (refreshToken != null) {
                try {
                    val response = api.refreshToken(refreshToken)
                    if (response.isSuccessful && response.body() != null) {
                        saveTokens(response.body()!!)
                        loadUserInfo(response.body()!!.access_token)
                    } else {
                        _uiState.value = UiState.NotAuthenticated
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.NotAuthenticated
                }
            } else {
                _uiState.value = UiState.NotAuthenticated
            }
        }
    }

    //Обработка кодов ошибок
    private fun handleError(code: Int) {
        when (code) {
            400 -> _uiState.value = UiState.Error("Неверные данные для входа")
            401 -> _uiState.value = UiState.Error("Неавторизован. Проверьте данные.")
            423 -> _uiState.value = UiState.Error("Приложение отключено администратором")
            426 -> _uiState.value = UiState.Error("Требуется обновление приложения")
            429 -> _uiState.value = UiState.Error("Слишком много запросов. Подождите.")
            else -> _uiState.value = UiState.Error("Ошибка авторизации (Код: $code)")
        }
    }

    //Временное хранение токенов в памяти
    private suspend fun saveTokens(tokens: AuthTokens) {
        currentAccessToken = tokens.access_token
        currentRefreshToken = tokens.refresh_token
        // реализация сохранения
    }

    private suspend fun getSavedAccessToken(): String? = currentAccessToken
    private suspend fun getSavedRefreshToken(): String? = currentRefreshToken

    var mlUserInput by mutableStateOf("")
        private set
    var mlResponse by mutableStateOf("")
        private set
    var isMLProcessing by mutableStateOf(false)
        private set

    // Функция обновления текста из UI
    fun updateMLInput(text: String) {
        mlUserInput = text
    }

    fun sendToNeuralNetwork() {
        if (mlUserInput.isBlank()) return

        isMLProcessing = true
        mlResponse = "" // очистка старого ответа

        viewModelScope.launch {
            if (isMockModeML) {
                mlResponse = "Это заглушка ответа нейросети на запрос: \"$mlUserInput\". В реальном режиме здесь будет ответ от API."
                isMLProcessing = false
                return@launch
            }
            try {
                val request = MLRequest(message = mlUserInput)
                val response = MLApiClient.api.sendMessage(request)

                if (response.isSuccessful && response.body() != null) {
                    mlResponse = response.body()!!.message
                } else {
                    mlResponse = "Ошибка API: ${response.code()}"
                }
            } catch (e: Exception) {
                mlResponse = "Ошибка сети: ${e.message}"
            }
            isMLProcessing = false
        }
    }
}


