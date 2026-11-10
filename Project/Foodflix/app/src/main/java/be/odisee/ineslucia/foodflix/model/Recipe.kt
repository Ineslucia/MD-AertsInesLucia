package be.odisee.ineslucia.foodflix.model

import com.google.gson.annotations.SerializedName

/**
 * Wrapper model representing the API response containing a list of recipes and status code.
 */
data class RecipeResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<Recipe>,
)

/**
 * Data model representing a recipe item with attributes such as title, ingredients,
 * preparation steps, cooking time, creation date, creator details, image, likes count, and user like status.
 */
data class Recipe(
    @SerializedName("id") val id: Int,
    @SerializedName("titel") val titel: String,
    @SerializedName("ingredienten") val ingredienten: String?,
    @SerializedName("stappen") val stappen: String?,
    @SerializedName("tijd") val tijd: Int,
    @SerializedName("datumAangemaakt") val datumAangemaakt: String?,
    @SerializedName("gebruikerAangemaaktId") val gebruikerAangemaaktId: Int,
    @SerializedName("gebruikerAangemaaktAlias") val gebruikerAangemaaktAlias: String?,
    @SerializedName("afbeelding") val afbeelding: String?,
    @SerializedName("likes") val likes: Int,
    @SerializedName("commentaar") val commentaar: Int,
    @SerializedName("gebruikerGeliked") val gebruikerGeliked: Int,
)
