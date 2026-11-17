package com.example.supergymanglosajonforza

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST("login")
    suspend fun login(@Body r: LoginReq): retrofit2.Response<Usuario>

    @POST("registro")
    suspend fun registro(@Body r: RegistroReq): retrofit2.Response<Usuario>

    @GET("clases")
    suspend fun clases(): retrofit2.Response<List<Clase>>

    @GET("usuarios/{id}/estatus")
    suspend fun estatus(@Path("id") id: Int): retrofit2.Response<Estatus>

    @POST("reservas")
    suspend fun reservar(@Body r: ReservaReq): retrofit2.Response<Mensaje>

    @GET("reservas/{id}")
    suspend fun misReservas(@Path("id") id: Int): retrofit2.Response<List<Reserva>>

    @DELETE("reservas/{id}")
    suspend fun cancelar(@Path("id") id: Int): retrofit2.Response<Mensaje>
}

class FallbackUrlInterceptor(private val baseUrls: List<String>) : Interceptor {
    @Volatile
    private var workingIndex = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val size = baseUrls.size
        var lastException: IOException? = null

        for (i in 0 until size) {
            val index = (workingIndex + i) % size
            val baseUrl = baseUrls[index]
            val baseHttpUrl = HttpUrl.get(baseUrl)

            val newUrl = originalRequest.url().newBuilder()
                .scheme(baseHttpUrl.scheme())
                .host(baseHttpUrl.host())
                .port(baseHttpUrl.port())
                .build()

            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            try {
                val response = chain.proceed(newRequest)
                workingIndex = index // Remember the working URL for subsequent requests
                return response
            } catch (e: IOException) {
                lastException = e
            }
        }
        throw lastException ?: IOException("All fallback URLs failed.")
    }
}

object RetrofitClient {
    private val BASE_URLS = listOf(
        "http://10.0.2.2:8000/",      // 1. Emulator URL
        "http://192.168.1.81:8000/",   // 2. Local IP
        "http://187.241.140.63:8000/"  // 3. Public IP (IPv4)
    )

    private val client = OkHttpClient.Builder()
        .addInterceptor(FallbackUrlInterceptor(BASE_URLS))
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URLS[0])
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
