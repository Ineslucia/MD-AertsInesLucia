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
//Project Test - Code: SDFGBRTY
/**
 * Response model returned when adding, liking, unliking, or deleting a recipe.
 */
data class RecipeAddResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("id") val id: String?
)

/**
 * Response model containing the list of users who liked a recipe.
 */
data class LikeUserResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<LikeUser>
)

/**
 * Model representing a user who liked a recipe.
 */
data class LikeUser(
    @SerializedName("id") val id: Int,
    @SerializedName("gebruikerId") val gebruikerId: Int,
    @SerializedName("alias") val alias: String,
    @SerializedName("datum") val datum: String
)

/**
 * Retrofit API interface defining all HTTP endpoints for the Foodflix backend server.
 */
interface RecipeApiService {
    /** Fetch all recipes with optional Authorization token */
    @GET("recepten")
    suspend fun getAllRecipes(
        @Header("Authorization") token: String? = null
    ): RecipeResponse

    /** Add a new recipe to the backend */
    @FormUrlEncoded
    @POST("recepten")
    suspend fun addRecipe(
        @Header("Authorization") token: String,
        @Field("titel") titel: String,
        @Field("ingredienten") ingredienten: String,
        @Field("stappen") stappen: String,
        @Field("tijd") tijd: Int,
        @Field("afbeelding") afbeelding: String
    ): RecipeAddResponse

    /** Like a recipe by its ID */
    @POST("recepten/{id}/like")
    suspend fun likeRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): RecipeAddResponse

    /** Unlike a recipe by its ID */
    @DELETE("recepten/{id}/like")
    suspend fun unlikeRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): RecipeAddResponse

    /** Delete a recipe by its ID */
    @DELETE("recepten/{id}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): RecipeAddResponse

    /** Get users who liked a specific recipe */
    @GET("recepten/{id}/likes")
    suspend fun getRecipeLikes(
        @Header("Authorization") token: String,
        @Path("id") recipeId: Int
    ): LikeUserResponse

    companion object {
        private const val BASE_URL = "https://stevenop.be/recipes/api/"

        /** Factory method to create and configure the Retrofit client instance */
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
