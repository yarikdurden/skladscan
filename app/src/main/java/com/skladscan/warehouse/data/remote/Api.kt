package com.skladscan.warehouse.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ProductDto(
    val barcode: String,
    val name: String,
    val sku: String,
    val category: String,
    val unit: String,
    val quantity: Int,
    @SerializedName("min_quantity") val minQuantity: Int,
    val location: String,
    val price: Double,
    @SerializedName("updated_at") val updatedAt: Long,
    val version: Int,
)

data class ProductsResponse(
    val items: List<ProductDto> = emptyList(),
    @SerializedName("server_time") val serverTime: Long = 0L,
)

data class RemoteOperationDto(
    @SerializedName("client_op_id") val clientOpId: String,
    val barcode: String,
    val type: String,
    @SerializedName("quantity_delta") val quantityDelta: Int,
    @SerializedName("quantity_after") val quantityAfter: Int,
    val note: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("device_id") val deviceId: String,
)

data class PushOperationsRequest(
    val operations: List<RemoteOperationDto>,
)

data class PushOperationsResponse(
    val accepted: List<String> = emptyList(),
    val rejected: List<RejectedOperation> = emptyList(),
    @SerializedName("server_time") val serverTime: Long = 0L,
)

data class RejectedOperation(
    @SerializedName("client_op_id") val clientOpId: String,
    val reason: String = "Неизвестная ошибка",
)

data class HealthResponse(
    val status: String = "ok",
    @SerializedName("server_time") val serverTime: Long = 0L,
)

interface WarehouseApi {
    @GET("api/v1/products")
    suspend fun products(@Query("since") since: Long? = null): ProductsResponse

    @POST("api/v1/operations")
    suspend fun pushOperations(@Body body: PushOperationsRequest): PushOperationsResponse

    @GET("api/v1/health")
    suspend fun health(): HealthResponse
}
