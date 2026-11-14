package com.example.mytpumobile
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

object ApiClient {
    private const val BASE_URL = "https://api.tpu.ru/"

    val api: TpuApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TpuApi::class.java)
    }
}

interface TpuApi {

    //Первичная авторизация через OAuth
    @POST("auth/oauth")
    suspend fun oauthLogin(
        @Body request: OAuthRequest
    ): Response<AuthTokens>

    //Проверка/продление токена
    @GET("auth/token")
    suspend fun validateToken(
        @Query("access_token") accessToken: String
    ): Response<AuthTokens>

    //Получение данных пользователя
    @GET("auth/user")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<UserInfo>

    //Обновление токена
    @PUT("auth/token")
    suspend fun refreshToken(
        @Query("refresh_token") refreshToken: String
    ): Response<AuthTokens>
}

