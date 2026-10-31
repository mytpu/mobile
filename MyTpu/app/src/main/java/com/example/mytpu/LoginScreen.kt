package com.example.mytpu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Вход в приложение", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.oauthLogin("vkontakte", "mock_token") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти через ВКонтакте")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.oauthLogin("ok", "mock_token") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти через Одноклассники")
        }
    }
}