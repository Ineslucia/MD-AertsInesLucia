package be.odisee.ineslucia.foodflix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import be.odisee.ineslucia.foodflix.model.Recipe
import be.odisee.ineslucia.foodflix.network.LikeUser
import be.odisee.ineslucia.foodflix.ui.Comment
import be.odisee.ineslucia.foodflix.ui.RecipeViewModel
import be.odisee.ineslucia.foodflix.ui.SortOption
import be.odisee.ineslucia.foodflix.ui.theme.FoodflixTheme

/**
 * Main activity of the Foodflix application.
 * Acts as the entry point that initializes edge-to-edge display and hosts the root Compose UI with theme switching support.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            FoodflixTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecipeScreen(
                        darkTheme = darkTheme,
                        onThemeToggle = { darkTheme = !darkTheme }
                    )
                }
            }
        }
    }
}

/**
 * Wrapper composable that connects the [RecipeViewModel] state and actions
 * to the stateless [RecipeScreenContent].
 */
@Composable
fun RecipeScreen(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeViewModel = viewModel()
) {
    RecipeScreenContent(
        recipes = viewModel.sortedRecipes,
        trendingRecipes = viewModel.trendingRecipes,
        quickRecipes = viewModel.quickRecipes,
        myRecipes = viewModel.myRecipes,
        favoriteRecipes = viewModel.favoriteRecipes,
        searchResults = viewModel.searchResults,
        searchQuery = viewModel.searchQuery,
        commentsMap = viewModel.commentsMap,
        recipeLikesMap = viewModel.recipeLikesMap,
        userAlias = viewModel.userAlias,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        sortOption = viewModel.sortOption,
        onSortOptionSelected = { viewModel.updateSortOption(it) },
        onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
        onLikeClick = { viewModel.toggleLike(it) },
        onAddRecipe = { titel, ing, stap, tijd, afbeelding, onSuccess ->
            viewModel.addRecipe(titel, ing, stap, tijd, afbeelding, onSuccess)
        },
        onDeleteRecipe = { viewModel.deleteRecipe(it) },
        onUpdateAlias = { viewModel.updateUserAlias(it) },
        onAddComment = { recipeId, text, author -> viewModel.addComment(recipeId, text, author) },
        onEditComment = { recipeId, commentId, text -> viewModel.editComment(recipeId, commentId, text) },
        onDeleteComment = { recipeId, commentId -> viewModel.deleteComment(recipeId, commentId) },
        onRetry = { viewModel.fetchRecipes() },
        darkTheme = darkTheme,
        onThemeToggle = onThemeToggle,
        modifier = modifier
    )
}

/**
 * Main UI content layout containing bottom navigation tabs (Home, Discover, Search, Favorites, Profile),
 * top app bar, theme toggle, floating action buttons, and screen switching logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreenContent(
    recipes: List<Recipe>,
    trendingRecipes: List<Recipe>,
    quickRecipes: List<Recipe>,
    myRecipes: List<Recipe>,
    favoriteRecipes: List<Recipe>,
    searchResults: List<Recipe>,
    searchQuery: String,
    commentsMap: Map<Int, List<Comment>>,
    recipeLikesMap: Map<Int, List<LikeUser>>,
    userAlias: String,
    isLoading: Boolean,
    errorMessage: String?,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onLikeClick: (Recipe) -> Unit,
    onAddRecipe: (String, String, String, Int, String, () -> Unit) -> Unit,
    onDeleteRecipe: (Int) -> Unit,
    onUpdateAlias: (String) -> Unit,
    onAddComment: (Int, String, String) -> Unit,
    onEditComment: (Int, String, String) -> Unit,
    onDeleteComment: (Int, String) -> Unit,
    onRetry: () -> Unit,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Home, 1 = Discover, 2 = Search, 3 = Favorites, 4 = Profile

    when {
        selectedRecipe != null -> {
            RecipeDetailScreen(
                recipe = selectedRecipe!!,
                userAlias = userAlias,
                comments = commentsMap[selectedRecipe!!.id] ?: emptyList(),
                likes = recipeLikesMap[selectedRecipe!!.id] ?: emptyList(),
                onBack = { selectedRecipe = null },
                onLikeClick = onLikeClick,
                onAddComment = { text, author -> onAddComment(selectedRecipe!!.id, text, author) },
                onEditComment = { commentId, text -> onEditComment(selectedRecipe!!.id, commentId, text) },
                onDeleteComment = { commentId -> onDeleteComment(selectedRecipe!!.id, commentId) }
            )
        }
        else -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                when (selectedTab) {
                                    0 -> "Foodflix Recipes"
                                    1 -> "Foodflix Discover"
                                    2 -> "Search Recipes"
                                    3 -> "Favorite Recipes"
                                    else -> "My Profile"
                                }
                            )
                        },
                        actions = {
                            IconButton(onClick = onThemeToggle) {
                                Icon(
                                    imageVector = if (darkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                            label = { Text("Discover") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search") },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                            label = { Text("Favorites") },
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 }
                        )
                    }
                },
                floatingActionButton = {
                    if (selectedTab == 0) {
                        FloatingActionButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        0 -> {
                            when {
                                isLoading -> {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                                errorMessage != null -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Error: $errorMessage",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = onRetry) {
                                            Text("Retry")
                                        }
                                    }
                                }
                                recipes.isEmpty() -> {
                                    Text(
                                        text = "No recipes found",
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                else -> {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        SortSection(
                                            selectedOption = sortOption,
                                            onOptionSelected = onSortOptionSelected
                                        )
                                        RecipeListContent(
                                            recipes = recipes,
                                            onLikeClick = onLikeClick,
                                            onItemClick = { recipe -> selectedRecipe = recipe }
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            DiscoverTabContent(
                                recipes = recipes,
                                trendingRecipes = trendingRecipes,
                                quickRecipes = quickRecipes,
                                onLikeClick = onLikeClick,
                                onItemClick = { recipe -> selectedRecipe = recipe }
                            )
                        }
                        2 -> {
                            SearchTabContent(
                                searchQuery = searchQuery,
                                onQueryChanged = onSearchQueryChanged,
                                searchResults = searchResults,
                                onLikeClick = onLikeClick,
                                onItemClick = { recipe -> selectedRecipe = recipe }
                            )
                        }
                        3 -> {
                            FavoritesTabContent(
                                favoriteRecipes = favoriteRecipes,
                                onLikeClick = onLikeClick,
                                onItemClick = { recipe -> selectedRecipe = recipe }
                            )
                        }
                        else -> {
                            ProfileTabContent(
                                userAlias = userAlias,
                                myRecipes = myRecipes,
                                onDeleteRecipe = onDeleteRecipe,
                                onUpdateAlias = onUpdateAlias,
                                darkTheme = darkTheme,
                                onThemeToggle = onThemeToggle,
                                requestCount = 0,
                                userErvaring = 0
                            )
                        }
                    }

                    if (showAddDialog) {
                        AddRecipeDialog(
                            onDismiss = { showAddDialog = false },
                            onSave = { titel, ing, stap, tijd, afbeelding ->
                                onAddRecipe(titel, ing, stap, tijd, afbeelding) {
                                    showAddDialog = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Netflix-style Discover / Browse feed with featured banner, horizontal category rows, and recipe feeds.
 */
@Composable
fun DiscoverTabContent(
    recipes: List<Recipe>,
    trendingRecipes: List<Recipe>,
    quickRecipes: List<Recipe>,
    onLikeClick: (Recipe) -> Unit,
    onItemClick: (Recipe) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(
                text = "What's on the menu today?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (recipes.isNotEmpty()) {
                        onItemClick(recipes.random())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎲 Surprise me")
            }
        }

        // Featured Banner (Top recipe)
        if (trendingRecipes.isNotEmpty()) {
            val featured = trendingRecipes.first()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { onItemClick(featured) }
            ) {
                if (!featured.afbeelding.isNullOrEmpty()) {
                    AsyncImage(
                        model = featured.afbeelding,
                        contentDescription = featured.titel,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔥 Featured Recipe",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = featured.titel,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "⏱️ ${featured.tijd} min | ❤️ ${featured.likes} likes",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Section: Trending / Popular
        if (trendingRecipes.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🔥 Trending Now",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingRecipes) { recipe ->
                        NetflixRecipeCard(recipe = recipe, onLikeClick = onLikeClick, onClick = { onItemClick(recipe) })
                    }
                }
            }
        }

        // Section: Quick & Easy
        if (quickRecipes.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⏱️ Quick & Easy (30 mins or less)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(quickRecipes) { recipe ->
                        NetflixRecipeCard(recipe = recipe, onLikeClick = onLikeClick, onClick = { onItemClick(recipe) })
                    }
                }
            }
        }
    }
}

/**
 * Netflix-style vertical thumbnail card for horizontal carousels.
 */
@Composable
fun NetflixRecipeCard(recipe: Recipe, onLikeClick: (Recipe) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (!recipe.afbeelding.isNullOrEmpty()) {
                    AsyncImage(
                        model = recipe.afbeelding,
                        contentDescription = recipe.titel,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Foodflix")
                    }
                }
                IconButton(
                    onClick = { onLikeClick(recipe) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (recipe.gebruikerGeliked == 1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (recipe.gebruikerGeliked == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = recipe.titel,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${recipe.tijd} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Composable for the Search tab, featuring a search input field and filtering recipes by name.
 */
@Composable
fun SearchTabContent(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    searchResults: List<Recipe>,
    onLikeClick: (Recipe) -> Unit,
    onItemClick: (Recipe) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChanged,
            label = { Text("Search recipes by name...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Type a recipe name above to search.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recipes found matching \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            RecipeListContent(
                recipes = searchResults,
                onLikeClick = onLikeClick,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Composable for the Favorites tab, displaying bookmarked recipes or an empty state message.
 */
@Composable
fun FavoritesTabContent(
    favoriteRecipes: List<Recipe>,
    onLikeClick: (Recipe) -> Unit,
    onItemClick: (Recipe) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (favoriteRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorite recipes yet.\nTap the heart icon on any recipe to add it here!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            RecipeListContent(
                recipes = favoriteRecipes,
                onLikeClick = onLikeClick,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Composable for the Profile tab, showing user info, statistics, created recipes, edit alias option, and theme switch.
 */
@Composable
fun ProfileTabContent(
    userAlias: String,
    myRecipes: List<Recipe>,
    onDeleteRecipe: (Int) -> Unit,
    onUpdateAlias: (String) -> Unit,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    requestCount: Int,
    userErvaring: Int
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👤 $userAlias",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "User ID: 291 | Added Recipes: ${myRecipes.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { showEditDialog = true }) {
                    Text("Edit Name")
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick ={ showEditDialog = true}) {
                Text("Edit Ervaring")
    }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ervaring: $userErvaring",
                    style = MaterialTheme.typography.titleMedium
                )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { onThemeToggle() }
                )
            }
        }

        Text(
            text = "My Added Recipes",
            style = MaterialTheme.typography.titleLarge
        )

        if (myRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("You haven't added any recipes yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myRecipes) { recipe ->
                    MyRecipeItemCard(recipe = recipe, onDelete = { onDeleteRecipe(recipe.id) })
                }
            }
        }

        if (showEditDialog) {
            EditNameDialog(
                currentName = userAlias,
                onDismiss = { showEditDialog = false },
                onSave = { newName ->
                    onUpdateAlias(newName)
                    showEditDialog = false
                }
            )
        }
    }
}
        //Composable to edit the expirience of the user in years from 0 to 99
@Composable
fun EditUserErvaring(
            currentErvaring: Int,
            onDismiss: () -> Unit,
            onSave: (String) -> Unit,
            isNotBlank: Int.() -> Boolean
        ) {
    var ervaring by remember { mutableStateOf(value = currentErvaring) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Ervaring") },
        text = {
            OutlinedTextField(
            value = ervaring,
            onValueChange = { ervaring = it},
            label = {Text("Ervaring/Expirience") },
            modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ervaring.isNotBlank()) {
                        onSave(ervaring)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
                }
            )
        }
    )
}

    }
/**
 * Dialog composable allowing users to update their user name or alias.
 */
@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name / Alias") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Card item for recipes created by the current user in the Profile tab, with a delete action.
 */
@Composable
fun MyRecipeItemCard(recipe: Recipe, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.titel,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tijd: ${recipe.tijd} min | Likes: ${recipe.likes}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Recipe",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Detailed view screen for a selected recipe, showing image, metadata, ingredients, preparation steps,
 * likes breakdown, comments section, and sharing/liking actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    userAlias: String,
    comments: List<Comment>,
    likes: List<LikeUser>,
    onBack: () -> Unit,
    onLikeClick: (Recipe) -> Unit,
    onAddComment: (String, String) -> Unit,
    onEditComment: (String, String) -> Unit,
    onDeleteComment: (String) -> Unit
) {
    val context = LocalContext.current
    var newCommentText by remember { mutableStateOf("") }
    var editingCommentId by remember { mutableStateOf<String?>(null) }
    var editingCommentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.titel) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = "Check out this delicious recipe on Foodflix!\n\n" +
                            "🍳 ${recipe.titel}\n" +
                            "⏱️ Cooking Time: ${recipe.tijd} min\n" +
                            "❤️ Likes: ${recipe.likes}\n\n" +
                            "Ingredients:\n${recipe.ingredienten ?: "N/A"}\n\n" +
                            "Steps:\n${recipe.stappen ?: "N/A"}"

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share recipe via")
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Recipe")
                    }
                    IconButton(onClick = { onLikeClick(recipe) }) {
                        Icon(
                            imageVector = if (recipe.gebruikerGeliked == 1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (recipe.gebruikerGeliked == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!recipe.afbeelding.isNullOrEmpty()) {
                AsyncImage(
                    model = recipe.afbeelding,
                    contentDescription = recipe.titel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = recipe.titel,
                style = MaterialTheme.typography.headlineMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "⏱️ Tijd: ${recipe.tijd} min", style = MaterialTheme.typography.bodyLarge)
                Text(text = "❤️ Likes: ${recipe.likes}", style = MaterialTheme.typography.bodyLarge)
            }

            if (!recipe.gebruikerAangemaaktAlias.isNullOrEmpty()) {
                Text(
                    text = "Aangemaakt door: ${recipe.gebruikerAangemaaktAlias}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Liked by Section
            HorizontalDivider()

            Text(
                text = "Geliked door (${likes.size})",
                style = MaterialTheme.typography.titleMedium
            )
            if (likes.isEmpty()) {
                Text(
                    text = "Nog geen likes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = likes.joinToString(", ") { it.alias.trim() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            Text(
                text = "Ingrediënten",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = recipe.ingredienten ?: "Geen ingrediënten opgegeven",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text(
                text = "Bereidingsstappen",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = recipe.stappen ?: "Geen stappen opgegeven",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            // Comments Section
            Text(
                text = "Reacties (${comments.size})",
                style = MaterialTheme.typography.titleLarge
            )

            // Add comment input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    label = { Text("Schrijf een reactie...") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            onAddComment(newCommentText, userAlias)
                            newCommentText = ""
                        }
                    }
                ) {
                    Text("Plaats")
                }
            }

            // List of comments
            if (comments.isEmpty()) {
                Text(
                    text = "Nog geen reacties. Wees de eerste!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    comments.forEach { comment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = comment.author,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingCommentId = comment.id
                                                editingCommentText = comment.text
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Comment", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onDeleteComment(comment.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Comment", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingCommentId != null) {
        AlertDialog(
            onDismissRequest = { editingCommentId = null },
            title = { Text("Bewerk reactie") },
            text = {
                OutlinedTextField(
                    value = editingCommentText,
                    onValueChange = { editingCommentText = it },
                    label = { Text("Reactie") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingCommentText.isNotBlank()) {
                            onEditComment(editingCommentId!!, editingCommentText)
                            editingCommentId = null
                        }
                    }
                ) {
                    Text("Opslaan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCommentId = null }) {
                    Text("Annuleren")
                }
            }
        )
    }
}

/**
 * Sorting section component with filter chips for switching sort criteria (Default, Likes, Newest).
 */
@Composable
fun SortSection(selectedOption: SortOption, onOptionSelected: (SortOption) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Sort by:", style = MaterialTheme.typography.bodyMedium)
        FilterChip(
            selected = selectedOption == SortOption.DEFAULT,
            onClick = { onOptionSelected(SortOption.DEFAULT) },
            label = { Text("Default") }
        )
        FilterChip(
            selected = selectedOption == SortOption.LIKES,
            onClick = { onOptionSelected(SortOption.LIKES) },
            label = { Text("Likes") }
        )
        FilterChip(
            selected = selectedOption == SortOption.DATE,
            onClick = { onOptionSelected(SortOption.DATE) },
            label = { Text("Newest") }
        )
    }
}

/**
 * Dialog composable for entering details and image URL to add a new recipe.
 */
@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, String) -> Unit
) {
    var titel by remember { mutableStateOf("") }
    var ingredienten by remember { mutableStateOf("") }
    var stappen by remember { mutableStateOf("") }
    var tijdStr by remember { mutableStateOf("") }
    var afbeelding by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Recipe") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titel,
                    onValueChange = { titel = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredienten,
                    onValueChange = { ingredienten = it },
                    label = { Text("Ingredients") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stappen,
                    onValueChange = { stappen = it },
                    label = { Text("Steps") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tijdStr,
                    onValueChange = { tijdStr = it },
                    label = { Text("Time (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = afbeelding,
                    onValueChange = { afbeelding = it },
                    label = { Text("Image URL (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tijd = tijdStr.toIntOrNull() ?: 15
                    if (titel.isNotBlank()) {
                        onSave(titel, ingredienten, stappen, tijd, afbeelding)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * List container for rendering multiple recipe items in a lazy column.
 */
@Composable
fun RecipeListContent(
    recipes: List<Recipe>,
    onLikeClick: (Recipe) -> Unit,
    onItemClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(recipes) { recipe ->
            RecipeItemCard(recipe = recipe, onLikeClick = onLikeClick, onClick = { onItemClick(recipe) })
        }
    }
}

/**
 * Card item representing an individual recipe in lists, showing its image, title, like button, and metadata.
 */
@Composable
fun RecipeItemCard(recipe: Recipe, onLikeClick: (Recipe) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!recipe.afbeelding.isNullOrEmpty()) {
                AsyncImage(
                    model = recipe.afbeelding,
                    contentDescription = recipe.titel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.titel,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onLikeClick(recipe) }) {
                    Icon(
                        imageVector = if (recipe.gebruikerGeliked == 1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Recipe",
                        tint = if (recipe.gebruikerGeliked == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tijd: ${recipe.tijd} min | Likes: ${recipe.likes}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!recipe.ingredienten.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ingrediënten: ${recipe.ingredienten}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * Preview for [RecipeScreenContent] with mock data for Android Studio Compose Preview.
 */
@Preview(showBackground = true)
@Composable
fun RecipeScreenContentPreview() {
    val sampleRecipes = listOf(
        Recipe(
            id = 1,
            titel = "Pasta Carbonara",
            ingredienten = "Pasta, Eieren, Spek, Parmigiano",
            stappen = "Kook pasta, bak spek, meng alles.",
            tijd = 20,
            datumAangemaakt = "2025-01-01",
            gebruikerAangemaaktId = 288,
            gebruikerAangemaaktAlias = "Chef Ines",
            afbeelding = null,
            likes = 12,
            commentaar = 3,
            gebruikerGeliked = 1
        ),
        Recipe(
            id = 2,
            titel = "Margherita Pizza",
            ingredienten = "Deeg, Tomatensaus, Mozzarella, Basilicum",
            stappen = "Beleg deeg en bak in de oven op 220 graden.",
            tijd = 30,
            datumAangemaakt = "2025-01-02",
            gebruikerAangemaaktId = 288,
            gebruikerAangemaaktAlias = "Chef Lucia",
            afbeelding = null,
            likes = 8,
            commentaar = 1,
            gebruikerGeliked = 0
        )
    )

    FoodflixTheme {
        RecipeScreenContent(
            recipes = sampleRecipes,
            trendingRecipes = sampleRecipes,
            quickRecipes = sampleRecipes,
            myRecipes = sampleRecipes,
            favoriteRecipes = sampleRecipes,
            searchResults = emptyList(),
            searchQuery = "",
            commentsMap = emptyMap(),
            recipeLikesMap = emptyMap(),
            userAlias = "Test User",
            isLoading = false,
            errorMessage = null,
            sortOption = SortOption.DEFAULT,
            onSortOptionSelected = {},
            onSearchQueryChanged = {},
            onLikeClick = {},
            onAddRecipe = { _, _, _, _, _, _ -> },
            onDeleteRecipe = {},
            onUpdateAlias = {},
            onAddComment = { _, _, _ -> },
            onEditComment = { _, _, _ -> },
            onDeleteComment = { _, _ -> },
            onRetry = {},
            darkTheme = false,
            onThemeToggle = {}
        )
    }
}

/**
 * Preview for individual [RecipeItemCard] with mock data.
 */
@Preview(showBackground = true)
@Composable
fun RecipeItemCardPreview() {
    val sampleRecipe = Recipe(
        id = 1,
        titel = "Pasta Carbonara",
        ingredienten = "Pasta, Eieren, Spek, Parmigiano",
        stappen = "Kook pasta, bak spek, meng alles.",
        tijd = 20,
        datumAangemaakt = "2025-01-01",
        gebruikerAangemaaktId = 288,
        gebruikerAangemaaktAlias = "Chef Ines",
        afbeelding = null,
        likes = 12,
        commentaar = 3,
        gebruikerGeliked = 1
    )

    FoodflixTheme {
        RecipeItemCard(
            recipe = sampleRecipe,
            onLikeClick = {},
            onClick = {}
        )
    }
}

