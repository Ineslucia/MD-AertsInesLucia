package be.odisee.ineslucia.foodflix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import be.odisee.ineslucia.foodflix.ui.RecipeViewModel
import be.odisee.ineslucia.foodflix.ui.SortOption
import be.odisee.ineslucia.foodflix.ui.theme.FoodflixTheme

/**
 * Main activity of the Foodflix application.
 * Acts as the entry point that initializes edge-to-edge display and hosts the root Compose UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodflixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecipeScreen()
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
    modifier: Modifier = Modifier,
    viewModel: RecipeViewModel = viewModel()
) {
    RecipeScreenContent(
        recipes = viewModel.sortedRecipes,
        myRecipes = viewModel.myRecipes,
        favoriteRecipes = viewModel.favoriteRecipes,
        searchResults = viewModel.searchResults,
        searchQuery = viewModel.searchQuery,
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
        onRetry = { viewModel.fetchRecipes() },
        modifier = modifier
    )
}

/**
 * Main UI content layout containing bottom navigation tabs (Home, Search, Favorites, Profile),
 * top app bar, floating action buttons, and screen switching logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreenContent(
    recipes: List<Recipe>,
    myRecipes: List<Recipe>,
    favoriteRecipes: List<Recipe>,
    searchResults: List<Recipe>,
    searchQuery: String,
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
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Home, 1 = Search, 2 = Favorites, 3 = Profile

    when {
        selectedRecipe != null -> {
            RecipeDetailScreen(
                recipe = selectedRecipe!!,
                onBack = { selectedRecipe = null },
                onLikeClick = onLikeClick
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
                                    1 -> "Search Recipes"
                                    2 -> "Favorite Recipes"
                                    else -> "My Profile"
                                }
                            )
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
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            label = { Text("Search") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                            label = { Text("Favorites") },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 }
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
                            SearchTabContent(
                                searchQuery = searchQuery,
                                onQueryChanged = onSearchQueryChanged,
                                searchResults = searchResults,
                                onLikeClick = onLikeClick,
                                onItemClick = { recipe -> selectedRecipe = recipe }
                            )
                        }
                        2 -> {
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
                                onUpdateAlias = onUpdateAlias
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
 * Composable for the Profile tab, showing user info, statistics, created recipes, and edit alias option.
 */
@Composable
fun ProfileTabContent(
    userAlias: String,
    myRecipes: List<Recipe>,
    onDeleteRecipe: (Int) -> Unit,
    onUpdateAlias: (String) -> Unit
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
 * Detailed view screen for a selected recipe, showing image, metadata, ingredients, and preparation steps,
 * with options to like/unlike and share the recipe via external messaging apps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onLikeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current

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
                    // Share button to share recipe details via message/other apps
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
        }
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
fun RecipeScreenPreview() {
    FoodflixTheme {
        RecipeScreenContent(
            recipes = listOf(
                Recipe(
                    id = 1,
                    titel = "Spaghetti Bolognese",
                    ingredienten = "Pasta, minced meat, tomato sauce",
                    stappen = "Cook pasta and mix with sauce.",
                    tijd = 20,
                    datumAangemaakt = "2023-01-01",
                    gebruikerAangemaaktId = 1,
                    gebruikerAangemaaktAlias = "Chef Ines",
                    afbeelding = null,
                    likes = 5,
                    commentaar = 0,
                    gebruikerGeliked = 1
                )
            ),
            myRecipes = emptyList(),
            favoriteRecipes = emptyList(),
            searchResults = emptyList(),
            searchQuery = "",
            userAlias = "Chef Ines",
            isLoading = false,
            errorMessage = null,
            sortOption = SortOption.DEFAULT,
            onSortOptionSelected = {},
            onSearchQueryChanged = {},
            onLikeClick = {},
            onAddRecipe = { _, _, _, _, _ -> },
            onDeleteRecipe = {},
            onUpdateAlias = {},
            onRetry = {}
        )
    }
}

