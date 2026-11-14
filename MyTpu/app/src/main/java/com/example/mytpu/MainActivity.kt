package com.example.mytpu


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mytpu.ui.theme.MyTpuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTpuTheme {
                val viewModel: AuthViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                when (uiState) {
                    is UiState.Loading -> {
                        // Экран загрузки
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    is UiState.NotAuthenticated -> {
                        // Экран входа
                        LoginScreen(viewModel = viewModel)
                    }
                    is UiState.Success -> {
                        // Главная страница
                        val user = (uiState as UiState.Success).user
                        MainScreen(user = user, viewModel = viewModel)
                    }
                    is UiState.Error -> {
                        // Экран ошибки с кнопкой "Попробовать снова"
                        ErrorScreen(
                            message = (uiState as UiState.Error).message,
                            onRetry = { viewModel.retryAuth() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(user: UserSession, viewModel: AuthViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Главная страница", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Данные из авторизации:", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text("Имя: ${user.name}")
                Text("Email: ${user.email ?: "Не указан"}")
                Text("Группа: ${user.group ?: "Не указана"}")
                Spacer(Modifier.height(16.dp))
                Text("Token: ${user.token.take(20)}...", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { /* Реализация выхода */ }) {
            Text("Выйти")
        }

        Spacer(Modifier.height(32.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("Спросить нейросеть:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // Поле ввода
        OutlinedTextField(
            value = viewModel.mlUserInput,
            onValueChange = { viewModel.updateMLInput(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите ваш запрос...") },
            enabled = !viewModel.isMLProcessing
        )

        Spacer(Modifier.height(8.dp))

        // Кнопка отправки
        Button(
            onClick = { viewModel.sendToNeuralNetwork() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isMLProcessing && viewModel.mlUserInput.isNotBlank()
        ) {
            if (viewModel.isMLProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(if (viewModel.isMLProcessing) "Думаю..." else "Отправить запрос")
        }

        // Отображение ответа
        if (viewModel.mlResponse.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = viewModel.mlResponse,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ошибка", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(message)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Попробовать снова")
        }
    }
}

