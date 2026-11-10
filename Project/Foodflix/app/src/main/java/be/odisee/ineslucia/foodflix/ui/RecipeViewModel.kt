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

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val recipeId: Int,
    val author: String,
    val text: String,
    val date: String = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", java.util.Date()).toString()
)

enum class SortOption {
    DEFAULT,
    LIKES,
    DATE
}

class RecipeViewModel : ViewModel() {
    private val apiService = RecipeApiService.create()

    var recipes by mutableStateOf<List<Recipe>>(emptyList())
        private set

    var sortOption by mutableStateOf(SortOption.DEFAULT)
        private set

    val sortedRecipes: List<Recipe>
        get() = when (sortOption) {
            SortOption.DEFAULT -> recipes
            SortOption.LIKES -> recipes.sortedByDescending { it.likes }
            SortOption.DATE -> recipes.sortedByDescending { it.datumAangemaakt ?: "" }
        }

    val myRecipes: List<Recipe>
        get() = recipes.filter { it.gebruikerAangemaaktId == 288 || it.gebruikerAangemaaktAlias == "Learn Kotlin today?" }

    val favoriteRecipes: List<Recipe>
        get() = recipes.filter { it.gebruikerGeliked == 1 }

    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    val searchResults: List<Recipe>
        get() = if (searchQuery.isBlank()) {
            recipes
        } else {
            recipes.filter { it.titel.contains(searchQuery, ignoreCase = true) }
        }

    private var customUserAlias by mutableStateOf<String?>(null)

    val userAlias: String
        get() = customUserAlias ?: (recipes.firstOrNull { it.gebruikerAangemaaktId == 288 }?.gebruikerAangemaaktAlias ?: "Foodflix User")

    fun updateUserAlias(newName: String) {
        customUserAlias = newName
    }

    var commentsMap by mutableStateOf<Map<Int, List<Comment>>>(emptyMap())
        private set

    var recipeLikesMap by mutableStateOf<Map<Int, List<LikeUser>>>(emptyMap())
        private set

    fun fetchLikesForRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                val response = apiService.getRecipeLikes(token, recipeId)
                recipeLikesMap = recipeLikesMap + (recipeId to response.data)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun addComment(recipeId: Int, text: String, author: String) {
        val currentList = commentsMap[recipeId] ?: emptyList()
        val newComment = Comment(recipeId = recipeId, author = author, text = text)
        commentsMap = commentsMap + (recipeId to (currentList + newComment))
    }

    fun editComment(recipeId: Int, commentId: String, newText: String) {
        val currentList = commentsMap[recipeId] ?: return
        val updatedList = currentList.map { if (it.id == commentId) it.copy(text = newText) else it }
        commentsMap = commentsMap + (recipeId to updatedList)
    }

    fun deleteComment(recipeId: Int, commentId: String) {
        val currentList = commentsMap[recipeId] ?: return
        val updatedList = currentList.filter { it.id != commentId }
        commentsMap = commentsMap + (recipeId to updatedList)
    }

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchRecipes()
    }

    fun updateSortOption(option: SortOption) {
        sortOption = option
    }

    fun fetchRecipes() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                val response = apiService.getAllRecipes(token)
                recipes = response.data
                // Refresh likes for any recipes currently loaded
                recipes.forEach { fetchLikesForRecipe(it.id) }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    fun addRecipe(
        titel: String,
        ingredienten: String,
        stappen: String,
        tijd: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = "Bearer yeg6q15g0ipj66tei8fla0hw91ws7aw"
                apiService.addRecipe(token, titel, ingredienten, stappen, tijd)
                fetchRecipes()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to add recipe"
            }
        }
    }

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
