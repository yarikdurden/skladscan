package com.skladscan.warehouse.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RemoteDataSource {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        )
        .build()

    @Volatile
    private var cachedBase: String? = null

    @Volatile
    private var cachedApi: WarehouseApi? = null

    private fun api(baseUrl: String): WarehouseApi {
        val normalized = normalize(baseUrl)
        val existing = cachedApi
        if (existing != null && cachedBase == normalized) return existing
        val created = Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WarehouseApi::class.java)
        cachedBase = normalized
        cachedApi = created
        return created
    }

    fun normalize(baseUrl: String): String {
        val trimmed = baseUrl.trim()
        require(trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            "Адрес должен начинаться с http:// или https://"
        }
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    suspend fun fetchProducts(baseUrl: String, since: Long? = null): List<ProductDto> =
        api(baseUrl).products(since).items

    suspend fun pushOperations(
        baseUrl: String,
        operations: List<RemoteOperationDto>,
        deviceId: String,
    ): PushOperationsResponse = api(baseUrl).pushOperations(PushOperationsRequest(operations))

    suspend fun health(baseUrl: String): Long = api(baseUrl).health().serverTime
}
