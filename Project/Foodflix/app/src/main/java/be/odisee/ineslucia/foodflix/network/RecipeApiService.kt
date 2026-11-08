package be.odisee.ineslucia.foodflix.network

import be.odisee.ineslucia.foodflix.model.RecipeResponse
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class RecipeAddResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("id") val id: String?
)

interface RecipeApiService {
    @GET("recepten")
    suspend fun getAllRecipes(
        @Header("Authorization") token: String? = null
    ): RecipeResponse

    @FormUrlEncoded
    @POST("recepten")
    suspend fun addRecipe(
        @Header("Authorization") token: String,
        @Field("titel") titel: String,
        @Field("ingredienten") ingredienten: String,
        @Field("stappen") stappen: String,
        @Field("tijd") tijd: Int
    ): RecipeAddResponse

    @POST("recepten/{id}/like")
    suspend fun likeRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): RecipeAddResponse

    @DELETE("recepten/{id}/like")
    suspend fun unlikeRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): RecipeAddResponse

    @DELETE("recepten/{id}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): RecipeAddResponse

    companion object {
        private const val BASE_URL = "https://stevenop.be/recipes/api/"

        fun create(): RecipeApiService {
            val client = OkHttpClient.Builder().build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeApiService::class.java)
        }
    }
}
