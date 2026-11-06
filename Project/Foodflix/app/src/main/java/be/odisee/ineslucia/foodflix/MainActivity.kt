package be.odisee.ineslucia.foodflix

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import be.odisee.ineslucia.foodflix.model.Recipe
import be.odisee.ineslucia.foodflix.ui.RecipeViewModel
import be.odisee.ineslucia.foodflix.ui.SortOption
import be.odisee.ineslucia.foodflix.ui.theme.FoodflixTheme

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    modifier: Modifier = Modifier,
    viewModel: RecipeViewModel = viewModel()
) {
    RecipeScreenContent(
        recipes = viewModel.sortedRecipes,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        sortOption = viewModel.sortOption,
        onSortOptionSelected = { viewModel.updateSortOption(it) },
        onLikeClick = { viewModel.toggleLike(it) },
        onAddRecipe = { titel, ing, stap, tijd, onSuccess ->
            viewModel.addRecipe(titel, ing, stap, tijd, onSuccess)
        },
        onRetry = { viewModel.fetchRecipes() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreenContent(
    recipes: List<Recipe>,
    isLoading: Boolean,
    errorMessage: String?,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    onLikeClick: (Recipe) -> Unit,
    onAddRecipe: (String, String, String, Int, () -> Unit) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    if (selectedRecipe != null) {
        RecipeDetailScreen(
            recipe = selectedRecipe!!,
            onBack = { selectedRecipe = null },
            onLikeClick = onLikeClick
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Foodflix Recipes") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onLikeClick: (Recipe) -> Unit
) {
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
fun RecipeDetailScreenPreview() {
    FoodflixTheme {
        RecipeDetailScreen(
            recipe = Recipe(
                id = 1,
                titel = "Lasagna",
                ingredienten = "2 uien, 2 teentjes knoflook, tomatensaus, lasagnebladen, mozzarella",
                stappen = "Bak het gehakt, snijd groenten, voeg saus toe en bak in de oven.",
                tijd = 60,
                datumAangemaakt = "2024-12-05",
                gebruikerAangemaaktId = 1,
                gebruikerAangemaaktAlias = "Chef Ines",
                afbeelding = "",
                likes = 3,
                commentaar = 0,
                gebruikerGeliked = 1
            ),
            onBack = {},
            onLikeClick = {}
        )
    }
}
