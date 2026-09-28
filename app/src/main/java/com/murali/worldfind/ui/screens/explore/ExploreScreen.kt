package com.murali.worldfind.ui.screens.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.data.mock.CountryData
import com.murali.worldfind.data.models.Product
import com.murali.worldfind.data.remote.CategoryDto
import com.murali.worldfind.ui.components.ProductCard
import com.murali.worldfind.ui.components.WorldFindSearchBar
import com.murali.worldfind.ui.state.UiState
import com.murali.worldfind.ui.viewmodel.MainViewModel

enum class SortOption {
    PRICE_LOW_HIGH, PRICE_HIGH_LOW, RATING, NEWEST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onProductClick: (String) -> Unit,
    mainViewModel: MainViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.NEWEST) }
    var minPrice by remember { mutableFloatStateOf(0f) }
    var maxPrice by remember { mutableFloatStateOf(100000f) }
    var minRating by remember { mutableFloatStateOf(0f) }
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showCountrySheet by remember { mutableStateOf(false) }
    
    val categoryFilter by mainViewModel.exploreCategoryFilter.collectAsState()
    val selectedCountries by mainViewModel.selectedCountries.collectAsState()
    val isINRAdvantageFilter by mainViewModel.isINRAdvantageFilter.collectAsState()
    val wishlistItems by mainViewModel.wishlistItems.collectAsState()
    val searchResultsState by mainViewModel.searchResultsState.collectAsState()
    val categoriesState by mainViewModel.categoriesState.collectAsState()

    val availableCategories: List<CategoryDto> = when (val cState = categoriesState) {
        is UiState.Success -> cState.data
        else -> emptyList()
    }

    LaunchedEffect(searchQuery, sortOption, categoryFilter) {
        val sortParam = when (sortOption) {
            SortOption.PRICE_LOW_HIGH -> "SALE_PRICE_ASC"
            SortOption.PRICE_HIGH_LOW -> "SALE_PRICE_DESC"
            SortOption.RATING -> "EVALUATE_RATE_DESC"
            SortOption.NEWEST -> null
        }
        mainViewModel.searchExploreProducts(
            keywords = searchQuery.ifBlank { null },
            categoryIds = categoryFilter,
            minPrice = if (minPrice > 0) minPrice.toDouble() else null,
            maxPrice = if (maxPrice < 100000) maxPrice.toDouble() else null,
            sort = sortParam
        )
    }

    val liveProducts: List<Product> = when (val state = searchResultsState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    val filteredProducts = remember(liveProducts, searchQuery, sortOption, categoryFilter, selectedCountries, isINRAdvantageFilter, minPrice, maxPrice, minRating) {
        var list = liveProducts.filter { product ->
            val query = searchQuery.trim().lowercase()
            val matchesSearch = searchQuery.isEmpty() || 
                    product.title.lowercase().contains(query) || 
                    product.category.lowercase().contains(query) ||
                    product.countryName.lowercase().contains(query) ||
                    product.countryCode.lowercase().contains(query) ||
                    product.currencyCode.lowercase().contains(query) ||
                    product.description.lowercase().contains(query) ||
                    (product.brand?.lowercase()?.contains(query) ?: false)
            
            val matchesCategory = categoryFilter == null || product.category.equals(categoryFilter, ignoreCase = true) || product.categoryId == categoryFilter
            val matchesCountry = selectedCountries.isEmpty() || selectedCountries.contains(product.countryId)
            val matchesPrice = product.priceInINR >= minPrice && product.priceInINR <= maxPrice
            val matchesRating = product.rating >= minRating
            
            matchesSearch && matchesCategory && matchesCountry && matchesPrice && matchesRating
        }
        
        list = when (sortOption) {
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.priceInINR }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.priceInINR }
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.NEWEST -> list
        }
        list
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Explore", 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontSize = 28.sp, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                WorldFindSearchBar(
                    query = searchQuery, 
                    onQueryChange = { searchQuery = it }, 
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = minPrice > 0 || maxPrice < 100000 || minRating > 0 || categoryFilter != null,
                        onClick = { showFilterSheet = true },
                        label = { Text(text = "Filters") },
                        trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    FilterChip(
                        selected = sortOption != SortOption.NEWEST,
                        onClick = { showSortSheet = true },
                        label = { Text(text = "Sort") },
                        trailingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    FilterChip(
                        selected = selectedCountries.isNotEmpty(),
                        onClick = { showCountrySheet = true },
                        label = { 
                            val label = if (selectedCountries.size == 1) {
                                CountryData.countries.find { it.id == selectedCountries.first() }?.name ?: "Country"
                            } else if (selectedCountries.size > 1) {
                                "Countries (${selectedCountries.size})"
                            } else "Country"
                            Text(text = label) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            when (val state = searchResultsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { mainViewModel.searchExploreProducts(keywords = searchQuery.ifBlank { null }) }) {
                                Text("RETRY")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    if (filteredProducts.isEmpty()) {
                        EmptyExploreState()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(filteredProducts) { product ->
                                ProductCard(
                                    product = product,
                                    isFavorite = wishlistItems.any { it.id == product.id },
                                    onClick = { onProductClick(product.id) },
                                    onFavoriteClick = { mainViewModel.toggleWishlist(product) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }, 
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                FilterContent(
                    categories = availableCategories,
                    currentCategory = categoryFilter,
                    onCategorySelect = { mainViewModel.setExploreCategoryFilter(it) },
                    priceRange = minPrice..maxPrice,
                    onPriceRangeChange = { minPrice = it.start; maxPrice = it.endInclusive },
                    minRating = minRating,
                    onRatingChange = { minRating = it },
                    onApply = { showFilterSheet = false },
                    onClear = { 
                        mainViewModel.clearAllFilters()
                        minPrice = 0f
                        maxPrice = 100000f
                        minRating = 0f
                        showFilterSheet = false 
                    }
                )
            }
        }
        
        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false }, 
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SortContent(
                    selectedOption = sortOption,
                    onOptionSelect = { sortOption = it; showSortSheet = false }
                )
            }
        }
        
        if (showCountrySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCountrySheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CountrySelectionContent(
                    selectedCountries = selectedCountries,
                    onToggleCountry = { mainViewModel.toggleCountrySelection(it) },
                    onClear = { mainViewModel.clearAllFilters(); showCountrySheet = false },
                    onDone = { showCountrySheet = false }
                )
            }
        }
    }
}

@Composable
fun FilterContent(
    categories: List<CategoryDto>,
    currentCategory: String?,
    onCategorySelect: (String?) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    minRating: Float,
    onRatingChange: (Float) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters", 
                color = MaterialTheme.colorScheme.onSurface, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClear) {
                Text(text = "Clear All", color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        if (categories.isNotEmpty()) {
            Text(text = "Category", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allOption = CategoryDto("ALL", "All")
                val categoryList = listOf(allOption) + categories
                categoryList.forEach { cat ->
                    val isSelected = (cat.id == "ALL" && currentCategory == null) || (cat.id == currentCategory) || (cat.name == currentCategory)
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                        modifier = Modifier.clickable { onCategorySelect(if (cat.id == "ALL") null else cat.id) }
                    ) {
                        Text(
                            text = cat.name, 
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(text = "Price Range (INR)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        RangeSlider(
            value = priceRange,
            onValueChange = onPriceRangeChange,
            valueRange = 0f..100000f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary, 
                activeTrackColor = MaterialTheme.colorScheme.primary, 
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "₹${priceRange.start.toInt()}", 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 12.sp
            )
            Text(
                text = "₹${priceRange.endInclusive.toInt()}", 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Minimum Rating", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Slider(
            value = minRating,
            onValueChange = onRatingChange,
            valueRange = 0f..5f,
            steps = 4,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary, 
                activeTrackColor = MaterialTheme.colorScheme.primary, 
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        Text(
            text = "Rating: ${minRating.toInt()}+ Stars", 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, 
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "APPLY FILTERS", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CountrySelectionContent(
    selectedCountries: Set<String>,
    onToggleCountry: (String) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    var countrySearchQuery by remember { mutableStateOf("") }
    
    val filteredCountries = remember(countrySearchQuery) {
        if (countrySearchQuery.isBlank()) {
            CountryData.countries
        } else {
            val query = countrySearchQuery.trim().lowercase()
            CountryData.countries.filter { 
                it.name.lowercase().contains(query) ||
                it.currencyCode.lowercase().contains(query) ||
                it.currencyName.lowercase().contains(query) ||
                it.countryCode.lowercase().contains(query)
            }
        }
    }

    Column(modifier = Modifier.fillMaxHeight(0.8f).padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Country", 
                color = MaterialTheme.colorScheme.onSurface, 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClear) {
                Text(text = "Clear", color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = countrySearchQuery,
            onValueChange = { countrySearchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search country or currency") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (countrySearchQuery.isNotEmpty()) {
                { IconButton(onClick = { countrySearchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) } }
            } else null,
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredCountries) { country ->
                val isSelected = selectedCountries.contains(country.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleCountry(country.id) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = country.flag, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = country.name, 
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("DONE", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SortContent(selectedOption: SortOption, onOptionSelect: (SortOption) -> Unit) {
    Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
        Text(
            text = "Sort By", 
            color = MaterialTheme.colorScheme.onSurface, 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        SortRow("Price: Low to High", selectedOption == SortOption.PRICE_LOW_HIGH) { onOptionSelect(SortOption.PRICE_LOW_HIGH) }
        SortRow("Price: High to Low", selectedOption == SortOption.PRICE_HIGH_LOW) { onOptionSelect(SortOption.PRICE_HIGH_LOW) }
        SortRow("Customer Rating", selectedOption == SortOption.RATING) { onOptionSelect(SortOption.RATING) }
        SortRow("Newest Arrivals", selectedOption == SortOption.NEWEST) { onOptionSelect(SortOption.NEWEST) }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SortRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text, 
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, 
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary, 
                unselectedColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun EmptyExploreState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No products found",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try adjusting your filters or search term",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}
