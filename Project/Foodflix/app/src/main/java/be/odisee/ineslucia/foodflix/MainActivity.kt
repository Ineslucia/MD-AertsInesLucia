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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeViewModel = viewModel()
) {
    RecipeScreenContent(
        recipes = viewModel.sortedRecipes,
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
        onAddRecipe = { titel, ing, stap, tijd, onSuccess ->
            viewModel.addRecipe(titel, ing, stap, tijd, onSuccess)
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
 * Main UI content layout containing bottom navigation tabs (Home, Search, Favorites, Profile),
 * top app bar, theme toggle, floating action buttons, and screen switching logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreenContent(
    recipes: List<Recipe>,
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
    onAddRecipe: (String, String, String, Int, () -> Unit) -> Unit,
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
                                    1 -> "Discover"
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
                                onThemeToggle = onThemeToggle
                            )
                        }
                    }

                    if (showAddDialog) {
                        AddRecipeDialog(
                            onDismiss = { showAddDialog = false },
                            onSave = { titel, ing, stap, tijd ->
                                onAddRecipe(titel, ing, stap, tijd) {
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
    onThemeToggle: () -> Unit
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
                    text = "User ID: 288 | Added Recipes: ${myRecipes.size}",
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
 * Dialog composable for entering details to add a new recipe.
 */
@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var titel by remember { mutableStateOf("") }
    var ingredienten by remember { mutableStateOf("") }
    var stappen by remember { mutableStateOf("") }
    var tijdStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Recipe") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tijd = tijdStr.toIntOrNull() ?: 15
                    if (titel.isNotBlank()) {
                        onSave(titel, ingredienten, stappen, tijd)
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

@Preview(showBackground = true)
@Composable
fun RecipeItemCardPreview() {
    FoodflixTheme {
        RecipeItemCard(
            recipe = Recipe(
                id = 1,
                titel = "Spaghetti Bolognese",
                ingredienten = "Pasta, minced meat, tomato sauce, garlic, onions",
                stappen = "1. Cook pasta until al dente.\n2. Brown minced meat with onions and garlic.\n3. Add tomato sauce and simmer.",
                tijd = 30,
                datumAangemaakt = "2025-01-01",
                gebruikerAangemaaktId = 288,
                gebruikerAangemaaktAlias = "Ines",
                afbeelding = null,
                likes = 12,
                commentaar = 3,
                gebruikerGeliked = 1
            ),
            onLikeClick = {},
            onClick = {}
        )
    }
}

/**
 * Composable for the Discover tab, designed with a mobile Netflix-style layout
 * featuring a featured hero banner and horizontal category rows.
 */
@Composable
fun DiscoverTabContent(
    recipes: List<Recipe>,
    onLikeClick: (Recipe) -> Unit,
    onItemClick: (Recipe) -> Unit
) {
    val trendingRecipes = recipes.sortedByDescending { it.likes }
    val quickRecipes = recipes.filter { it.tijd <= 20 }
    val featuredRecipe = recipes.maxByOrNull { it.likes } ?: recipes.firstOrNull()
    val chefRecipes = recipes.filter { !it.gebruikerAangemaaktAlias.isNullOrEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Netflix Hero Banner
        if (featuredRecipe != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clickable { onItemClick(featuredRecipe) }
            ) {
                if (!featuredRecipe.afbeelding.isNullOrEmpty()) {
                    AsyncImage(
                        model = featuredRecipe.afbeelding,
                        contentDescription = featuredRecipe.titel,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = featuredRecipe.titel,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { onLikeClick(featuredRecipe) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (featuredRecipe.gebruikerGeliked == 1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (featuredRecipe.gebruikerGeliked == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🔥 FEATURED DISCOVERY",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = featuredRecipe.titel,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ ${featuredRecipe.tijd} min",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "❤️ ${featuredRecipe.likes} Likes",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Button(
                            onClick = { onItemClick(featuredRecipe) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("▶ Start Cooking")
                        }
                    }
                }
            }
        }

        // Horizontal Category Rows (Netflix style)
        RecipeCategoryRow(
            categoryTitle = "Trending Now",
            recipes = trendingRecipes,
            onLikeClick = onLikeClick,
            onItemClick = onItemClick
        )

        if (quickRecipes.isNotEmpty()) {
            RecipeCategoryRow(
                categoryTitle = "Quick Bites (Under 20 min)",
                recipes = quickRecipes,
                onLikeClick = onLikeClick,
                onItemClick = onItemClick
            )
        }

        if (chefRecipes.isNotEmpty()) {
            RecipeCategoryRow(
                categoryTitle = "Community Creations",
                recipes = chefRecipes,
                onLikeClick = onLikeClick,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
fun RecipeCategoryRow(
    categoryTitle: String,
    recipes: List<Recipe>,
    onLikeClick: (Recipe) -> Unit,
    onItemClick: (Recipe) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = categoryTitle,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                NetflixRecipeCard(recipe = recipe, onLikeClick = onLikeClick, onClick = { onItemClick(recipe) })
            }
        }
    }
}

@Composable
fun NetflixRecipeCard(recipe: Recipe, onLikeClick: (Recipe) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(180.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!recipe.afbeelding.isNullOrEmpty()) {
                AsyncImage(
                    model = recipe.afbeelding,
                    contentDescription = recipe.titel,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = recipe.titel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = { onLikeClick(recipe) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (recipe.gebruikerGeliked == 1) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (recipe.gebruikerGeliked == 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(6.dp)
                ) {
                    Text(
                        text = recipe.titel,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    Text(
                        text = "${recipe.tijd}m • ❤️ ${recipe.likes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecipeScreenPreview() {
    var darkTheme by remember { mutableStateOf(false) }
    FoodflixTheme(darkTheme = darkTheme) {
        RecipeScreenContent(
            recipes = listOf(
                Recipe(
                    id = 1,
                    titel = "Spaghetti Bolognese",
                    ingredienten = "Pasta, minced meat, tomato sauce",
                    stappen = "Cook pasta. Fry meat. Mix.",
                    tijd = 30,
                    datumAangemaakt = "2025-01-01",
                    gebruikerAangemaaktId = 288,
                    gebruikerAangemaaktAlias = "Ines",
                    afbeelding = null,
                    likes = 12,
                    commentaar = 1,
                    gebruikerGeliked = 1
                ),
                Recipe(
                    id = 2,
                    titel = "Pancakes",
                    ingredienten = "Flour, milk, eggs, sugar",
                    stappen = "Mix ingredients. Fry on pan.",
                    tijd = 15,
                    datumAangemaakt = "2025-01-02",
                    gebruikerAangemaaktId = 288,
                    gebruikerAangemaaktAlias = "Ines",
                    afbeelding = null,
                    likes = 8,
                    commentaar = 0,
                    gebruikerGeliked = 0
                )
            ),
            myRecipes = listOf(
                Recipe(
                    id = 2,
                    titel = "Pancakes",
                    ingredienten = "Flour, milk, eggs, sugar",
                    stappen = "Mix ingredients. Fry on pan.",
                    tijd = 15,
                    datumAangemaakt = "2025-01-02",
                    gebruikerAangemaaktId = 288,
                    gebruikerAangemaaktAlias = "Ines",
                    afbeelding = null,
                    likes = 8,
                    commentaar = 0,
                    gebruikerGeliked = 0
                )
            ),
            favoriteRecipes = listOf(
                Recipe(
                    id = 1,
                    titel = "Spaghetti Bolognese",
                    ingredienten = "Pasta, minced meat, tomato sauce",
                    stappen = "Cook pasta. Fry meat. Mix.",
                    tijd = 30,
                    datumAangemaakt = "2025-01-01",
                    gebruikerAangemaaktId = 288,
                    gebruikerAangemaaktAlias = "Ines",
                    afbeelding = null,
                    likes = 12,
                    commentaar = 1,
                    gebruikerGeliked = 1
                )
            ),
            searchResults = emptyList(),
            searchQuery = "",
            commentsMap = mapOf(1 to listOf(Comment(recipeId = 1, author = "Lucia", text = "Lekker!"))),
            recipeLikesMap = mapOf(1 to listOf(LikeUser(id = 1, gebruikerId = 101, alias = "Lucia", datum = "2025-01-01"))),
            userAlias = "Ines",
            isLoading = false,
            errorMessage = null,
            sortOption = SortOption.DEFAULT,
            onSortOptionSelected = {},
            onSearchQueryChanged = {},
            onLikeClick = {},
            onAddRecipe = { _, _, _, _, onSuccess -> onSuccess() },
            onDeleteRecipe = {},
            onUpdateAlias = {},
            onAddComment = { _, _, _ -> },
            onEditComment = { _, _, _ -> },
            onDeleteComment = { _, _ -> },
            onRetry = {},
            darkTheme = darkTheme,
            onThemeToggle = { darkTheme = !darkTheme }
        )
    }
}
