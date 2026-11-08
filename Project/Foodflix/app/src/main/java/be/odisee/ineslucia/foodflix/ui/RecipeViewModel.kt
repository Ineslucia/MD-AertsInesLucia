package be.odisee.ineslucia.foodflix.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.odisee.ineslucia.foodflix.model.Recipe
import be.odisee.ineslucia.foodflix.network.RecipeApiService
import kotlinx.coroutines.launch

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
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to update like"
            }
        }
    }
}
