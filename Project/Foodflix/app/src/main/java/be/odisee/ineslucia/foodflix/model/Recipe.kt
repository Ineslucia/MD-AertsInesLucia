package be.odisee.ineslucia.foodflix.model

import com.google.gson.annotations.SerializedName

data class RecipeResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<Recipe>,
)

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
