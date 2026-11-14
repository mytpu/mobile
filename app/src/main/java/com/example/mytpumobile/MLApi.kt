package com.example.mytpumobile

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

//модель данных
data class MLRequest(
    val message: String
)

data class MLResponse(
    val message: String
)

interface MLApi {
    @POST("api/ai/chat") // <- тут заменить
    suspend fun sendMessage(@Body request: MLRequest): Response<MLResponse>
}

object MLApiClient {
    private const val BASE_URL = "http://194.107.200.213/" // <- наш url иишки

    val api: MLApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MLApi::class.java)
    }
}