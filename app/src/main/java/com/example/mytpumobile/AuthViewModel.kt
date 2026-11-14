package com.example.mytpumobile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytpumobile.ui.theme.AppThemeMode
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
        // Проверка сохранённого токена при запуске приложения
        checkExistingToken()
    }

    fun retryAuth() {
        _uiState.value = UiState.Loading
        checkExistingToken()
    }

    // Первичный вход в систему
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

            // Запрос к API
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

    // Получение данных пользователя
    private suspend fun loadUserInfo(token: String) {
        if (isMockMode) {
            _uiState.value = UiState.Success(
                UserSession(
                    name = "Анна Ковалёва",
                    token = token,
                    email = "ivanov@tpu.ru",
                    group = "8К31"
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

    // Проверка токена при запуске
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

    // Обновление токена
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

    // Обработка кодов ошибок
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

    // Временное хранение токенов в памяти
    private suspend fun saveTokens(tokens: AuthTokens) {
        currentAccessToken = tokens.access_token
        currentRefreshToken = tokens.refresh_token
    }

    private suspend fun getSavedAccessToken(): String? = currentAccessToken
    private suspend fun getSavedRefreshToken(): String? = currentRefreshToken

    // --- БЛОК ЧАТА С НЕЙРОСЕТЬЮ (ПОМОЩНИК) ---

    var mlUserInput by mutableStateOf("")
        private set
    var mlResponse by mutableStateOf("")
        private set
    var isMLProcessing by mutableStateOf(false)
        private set

    var chatMessages by mutableStateOf(listOf<ChatMessage>())
        private set

    fun updateMLInput(text: String) {
        mlUserInput = text
    }

    fun clearChat() {
        chatMessages = emptyList()
        mlUserInput = ""
        mlResponse = ""
    }

    fun logout() {
        currentAccessToken = null
        currentRefreshToken = null
        clearChat()
        _uiState.value = UiState.NotAuthenticated
    }

    fun sendToNeuralNetwork() {
        if (mlUserInput.isBlank()) return

        val currentQuery = mlUserInput
        chatMessages = chatMessages + ChatMessage(text = currentQuery, isUser = true)
        mlUserInput = "" // сразу очищаем поле ввода
        isMLProcessing = true
        mlResponse = ""

        viewModelScope.launch {
            if (isMockModeML) {
                delay(800)
                val reply = "Это заглушка ответа нейросети на запрос: \"$currentQuery\"."
                mlResponse = reply
                chatMessages = chatMessages + ChatMessage(text = reply, isUser = false)
                isMLProcessing = false
                return@launch
            }
            try {
                val request = MLRequest(message = currentQuery)
                val response = MLApiClient.api.sendMessage(request)

                val replyText = if (response.isSuccessful && response.body() != null) {
                    response.body()!!.message
                } else {
                    "Ошибка API: ${response.code()}"
                }
                mlResponse = replyText
                chatMessages = chatMessages + ChatMessage(text = replyText, isUser = false)
            } catch (e: Exception) {
                val errText = "Ошибка сети: ${e.message}"
                mlResponse = errText
                chatMessages = chatMessages + ChatMessage(text = errText, isUser = false)
            }
            isMLProcessing = false
        }
    }

    // --- БЛОК ТЕМЫ И НАСТРОЙКИ ДОСКИ ---

    var themeMode by mutableStateOf(AppThemeMode.LIGHT)
        private set

    fun updateTheme(mode: AppThemeMode) {
        themeMode = mode
    }

    var dashboardWidgets by mutableStateOf(
        listOf(
            DashboardWidgetConfig(DashboardWidgetType.SCHEDULE, WidgetSize.L, isEnabled = true),
            DashboardWidgetConfig(DashboardWidgetType.DEADLINES, WidgetSize.S, isEnabled = true),
            DashboardWidgetConfig(DashboardWidgetType.GRADES, WidgetSize.S, isEnabled = true),
            DashboardWidgetConfig(DashboardWidgetType.TICKETS, WidgetSize.M, isEnabled = true),
            DashboardWidgetConfig(DashboardWidgetType.MAIL_MOODLE, WidgetSize.M, isEnabled = true),
            DashboardWidgetConfig(DashboardWidgetType.INSTRUCTIONS, WidgetSize.M, isEnabled = false),
            DashboardWidgetConfig(DashboardWidgetType.MY_COURSES, WidgetSize.M, isEnabled = false)
        )
    )
        private set

    fun changeWidgetSize(type: DashboardWidgetType, newSize: WidgetSize) {
        dashboardWidgets = dashboardWidgets.map {
            if (it.type == type) it.copy(size = newSize) else it
        }
    }

    fun toggleWidgetEnabled(type: DashboardWidgetType, enabled: Boolean) {
        dashboardWidgets = dashboardWidgets.map {
            if (it.type == type) it.copy(isEnabled = enabled) else it
        }
    }

    fun moveEnabledWidget(fromIndex: Int, toIndex: Int) {
        val enabledList = dashboardWidgets.filter { it.isEnabled }.toMutableList()
        val disabledList = dashboardWidgets.filter { !it.isEnabled }
        if (fromIndex in enabledList.indices && toIndex in enabledList.indices) {
            val item = enabledList.removeAt(fromIndex)
            enabledList.add(toIndex, item)
            dashboardWidgets = enabledList + disabledList
        }
    }


    var tickets by mutableStateOf(MockTicketsData.initialTickets)
        private set

    var activeTicketDraft by mutableStateOf<TicketDraft?>(null)
        private set

    fun addTicketComment(ticketId: String, message: String) {
        if (message.isBlank()) return
        tickets = tickets.map { ticket ->
            if (ticket.id == ticketId) {
                val newComment = TicketComment("Вы", "сейчас", message, isStudent = true)
                ticket.copy(
                    status = TicketStatus.IN_PROGRESS,
                    lastMessagePreview = "«$message»",
                    lastUpdateTime = "сейчас",
                    comments = ticket.comments + newComment
                )
            } else ticket
        }
    }

    fun resolveTicket(ticketId: String) {
        tickets = tickets.map {
            if (it.id == ticketId) it.copy(status = TicketStatus.RESOLVED) else it
        }
    }

    fun withdrawTicket(ticketId: String) {
        tickets = tickets.filterNot { it.id == ticketId }
    }

    fun createDormitoryTicket(dorm: String, room: String, category: String, description: String) {
        val newId = (102852..102999).random().toString()
        val newTicket = TpuTicket(
            id = newId,
            department = "кампус",
            title = "$category · Общежитие $dorm, комн. $room",
            status = TicketStatus.IN_PROGRESS,
            lastMessagePreview = "«$description»",
            lastUpdateTime = "только что",
            deadlineText = "срок 2–5 дней",
            timeline = listOf(
                TicketTimelineStep("Зарегистрирована", "Сегодня, только что", isCompleted = true, isCurrent = true),
                TicketTimelineStep("Назначена службе кампуса", "", isCompleted = false),
                TicketTimelineStep("В работе", "", isCompleted = false),
                TicketTimelineStep("Решена", "", isCompleted = false)
            ),
            comments = listOf(
                TicketComment("Вы", "только что", description, isStudent = true)
            )
        )
        tickets = listOf(newTicket) + tickets
    }

    fun prepareDraftFromAssistant() {
        activeTicketDraft = TicketDraft(
            serviceName = "Доступ к системе электронного обучения (Moodle)",
            description = "Moodle пишет «неверный пароль». По инструкции devdocs выходил везде, чистил cookie и входил через ТПУ — не помогло.",
            attachmentName = "скриншот-ошибки.png · 240 КБ"
        )
    }

    fun confirmTicketDraft() {
        val draft = activeTicketDraft ?: return
        val newTicket = TpuTicket(
            id = "102855",
            department = "ИТ-служба",
            title = "Доступ к Moodle",
            status = TicketStatus.IN_PROGRESS,
            lastMessagePreview = "«${draft.description}»",
            lastUpdateTime = "только что",
            deadlineText = "ответ до 30.10",
            timeline = listOf(
                TicketTimelineStep("Зарегистрирована", "Сегодня, только что", isCompleted = true, isCurrent = true),
                TicketTimelineStep("Назначена ИТ-службе", "", isCompleted = false),
                TicketTimelineStep("В работе", "", isCompleted = false)
            ),
            comments = listOf(
                TicketComment("Вы · через помощника", "только что", draft.description, isStudent = true)
            )
        )
        tickets = listOf(newTicket) + tickets
        activeTicketDraft = null
        chatMessages = chatMessages + ChatMessage(
            text = "Готово, заявка №102855 зарегистрирована в help.tpu.ru. Напишу, когда исполнитель ответит.",
            isUser = false
        )
    }

    fun cancelTicketDraft() {
        activeTicketDraft = null
    }
}