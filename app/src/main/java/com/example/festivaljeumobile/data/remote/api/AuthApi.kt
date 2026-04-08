package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.LoginRequestDto
import com.example.festivaljeumobile.data.remote.dto.LoginResponseDto
import com.example.festivaljeumobile.data.remote.dto.WhoAmIResponseWrapper
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("/api/auth/whoami")
    suspend fun whoAmI(): Response<WhoAmIResponseWrapper>
}