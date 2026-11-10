package be.odisee.ineslucia.foodflix.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.odisee.ineslucia.foodflix.model.Recipe
import be.odisee.ineslucia.foodflix.network.LikeUser
import be.odisee.ineslucia.foodflix.network.RecipeApiService
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Data model representing a comment on a recipe.
 */
data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: Int,
    val author: String,
    val text: String,
    val date: String = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", java.util.Date()).toString()
)

/**
 * Enum defining available sorting criteria for recipes.
 */
enum class SortOption {
    DEFAULT,
    LIKES,
    DATE
}

/**
 * ViewModel responsible for managing UI state, business logic, Retrofit network requests,
 * favorites, comments, sorting, and user profile data for the Foodflix app.
 */
class RecipeViewModel : ViewModel() {
    private val apiService = RecipeApiService.create()

    // State holding the list of all recipes
    var recipes by mutableStateOf<List<Recipe>>(emptyList())
        private set

    // State holding the active sort option
    var sortOption by mutableStateOf(SortOption.DEFAULT)
        private set

    // Computed property for sorted recipes based on current sort option
    val sortedRecipes: List<Recipe>
        get() = when (sortOption) {
            SortOption.DEFAULT -> recipes
            SortOption.LIKES -> recipes.sortedByDescending { it.likes }
            SortOption.DATE -> recipes.sortedByDescending { it.datumAangemaakt ?: "" }
        }

    // Computed property for trending recipes (sorted by likes)
    val trendingRecipes: List<Recipe>
        get() = recipes.sortedByDescending { it.likes }

    // Computed property for quick recipes (cooking time <= 30 mins)
    val quickRecipes: List<Recipe>
        get() = recipes.filter { it.tijd <= 30 }

    // Computed property for recipes created by the current user
    val myRecipes: List<Recipe>
        get() = recipes.filter { it.gebruikerAangemaaktId == 288 || it.gebruikerAangemaaktAlias == "Learn Kotlin today?" }

    // Computed property for bookmarked favorite recipes
    val favoriteRecipes: List<Recipe>
        get() = recipes.filter { it.gebruikerGeliked == 1 }

    // State holding search query text
    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // Computed property for recipes matching the search query
    val searchResults: List<Recipe>
        get() = if (searchQuery.isBlank()) {
            recipes
        } else {
            recipes.filter { it.titel.contains(searchQuery, ignoreCase = true) }
        }

    private var customUserAlias by mutableStateOf<String?>(null)

    // Current user alias/name
    val userAlias: String
        get() = customUserAlias ?: (recipes.firstOrNull { it.gebruikerAangemaaktId == 288 }?.gebruikerAangemaaktAlias ?: "Foodflix User")

    fun updateUserAlias(newName: String) {
        customUserAlias = newName
    }

    // Map storing comments per recipe ID
    var commentsMap by mutableStateOf<Map<Int, List<Comment>>>(emptyMap())
        private set

    // Map storing likes users list per recipe ID
    var recipeLikesMap by mutableStateOf<Map<Int, List<LikeUser>>>(emptyMap())
        private set

    /** Fetch likes users for a specific recipe */
    fun fetchLikesForRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                val response = apiService.getRecipeLikes(token, recipeId)
                recipeLikesMap = recipeLikesMap + (recipeId to response.data)
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    /** Add a comment to a recipe */
    fun addComment(recipeId: Int, text: String, author: String) {
        val currentList = commentsMap[recipeId] ?: emptyList()
        val newComment = Comment(recipeId = recipeId, author = author, text = text)
        commentsMap = commentsMap + (recipeId to (currentList + newComment))
    }

    /** Edit an existing comment */
    fun editComment(recipeId: Int, commentId: String, newText: String) {
        val currentList = commentsMap[recipeId] ?: return
        val updatedList = currentList.map { if (it.id == commentId) it.copy(text = newText) else it }
        commentsMap = commentsMap + (recipeId to updatedList)
    }

    /** Delete a comment */
    fun deleteComment(recipeId: Int, commentId: String) {
        val currentList = commentsMap[recipeId] ?: return
        val updatedList = currentList.filter { it.id != commentId }
        commentsMap = commentsMap + (recipeId to updatedList)
    }

    // Loading state for network requests
    var isLoading by mutableStateOf(false)
        private set

    // Error message state
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchRecipes()
    }

    fun updateSortOption(option: SortOption) {
        sortOption = option
    }

    /** Fetch all recipes from the backend via Retrofit */
    fun fetchRecipes() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                val response = apiService.getAllRecipes(token)
                recipes = response.data
                recipes.forEach { fetchLikesForRecipe(it.id) }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    /** Add a new recipe */
    fun addRecipe(
        titel: String,
        ingredienten: String,
        stappen: String,
        tijd: Int,
        afbeelding: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                apiService.addRecipe(token, titel, ingredienten, stappen, tijd, afbeelding)
                fetchRecipes()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to add recipe"
            }
        }
    }

    /** Delete a recipe */
    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                apiService.deleteRecipe(token, recipeId)
                fetchRecipes()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to delete recipe"
            }
        }
    }

    /** Toggle like status for a recipe */
    fun toggleLike(recipe: Recipe) {
        viewModelScope.launch {
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                if (recipe.gebruikerGeliked == 1) {
                    apiService.unlikeRecipe(token, recipe.id)
                } else {
                    apiService.likeRecipe(token, recipe.id)
                }
                fetchRecipes()
                fetchLikesForRecipe(recipe.id)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to update like"
            }
        }
    }
}
