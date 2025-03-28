package com.example.forecastweatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.forecastweatherapp.ui.components.CurrentWeatherCard
import com.example.forecastweatherapp.ui.components.DailyForecastSection
import com.example.forecastweatherapp.ui.components.ErrorScreen
import com.example.forecastweatherapp.ui.components.FavoriteCitiesList
import com.example.forecastweatherapp.ui.components.HourlyForecastSection
import com.example.forecastweatherapp.ui.components.LoadingScreen
import com.example.forecastweatherapp.ui.components.WeatherSearchBar
import com.example.forecastweatherapp.ui.model.LocationSearchResult
import com.example.forecastweatherapp.ui.theme.ForecastWeatherAppTheme
import com.example.forecastweatherapp.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForecastWeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val snackbarHostState = remember { SnackbarHostState() }
                    val weatherApplication = application as WeatherApplication
                    
                    val weatherViewModel: WeatherViewModel = viewModel(
                        factory = WeatherViewModel.Factory(
                            weatherRepository = weatherApplication.weatherRepository,
                            locationService = weatherApplication.locationService
                        )
                    )
                    
                    // Estado da UI
                    val uiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
                    val favoriteCities by weatherViewModel.favoriteCities.collectAsStateWithLifecycle()
                    
                    // Solicitar permissões de localização
                    val requestPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { permissions ->
                            val allGranted = permissions.all { it.value }
                            if (allGranted) {
                                weatherViewModel.getWeatherFromCurrentLocation()
                            }
                        }
                    )
                    
                    LaunchedEffect(Unit) {
                        if (!uiState.isLocationPermissionGranted) {
                            val fineLocationPermission = ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            
                            val coarseLocationPermission = ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            
                            if (fineLocationPermission != PackageManager.PERMISSION_GRANTED &&
                                coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    }
                    
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { paddingValues ->
                        WeatherAppContent(
                            uiState = uiState,
                            favoriteCities = favoriteCities,
                            onSearchQueryChange = { weatherViewModel.searchLocations(it) },
                            onSearchResultClick = { weatherViewModel.getWeatherForCity(it.name) },
                            onSearchActiveChange = { if (!it) weatherViewModel.clearSearchResults() },
                            onLocationClick = { weatherViewModel.getWeatherFromCurrentLocation() },
                            onFavoriteClick = { weatherViewModel.toggleFavoriteCity() },
                            onCityClick = { weatherViewModel.getWeatherForCity(it) },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherAppContent(
    uiState: com.example.forecastweatherapp.ui.model.WeatherUiState,
    favoriteCities: List<com.example.forecastweatherapp.data.database.FavoriteCity>,
    onSearchQueryChange: (String) -> Unit,
    onSearchResultClick: (LocationSearchResult) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onLocationClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchActive by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            WeatherSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { /*nada, pesquisa em tempo real*/ },
                onLocationClick = onLocationClick,
                searchResults = uiState.searchResults,
                onResultClick = { 
                    onSearchResultClick(it)
                    searchActive = false
                },
                isSearching = uiState.isSearching,
                onClearSearch = { onSearchQueryChange("") },
                active = searchActive,
                onActiveChange = { 
                    searchActive = it
                    onSearchActiveChange(it)
                }
            )
            
            if (uiState.isLoading) {
                LoadingScreen()
            } else if (uiState.errorMessage != null) {
                ErrorScreen(message = uiState.errorMessage)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    uiState.currentWeather?.let { currentWeather ->
                        CurrentWeatherCard(
                            currentWeather = currentWeather,
                            isFavorite = uiState.isCurrentCityFavorite,
                            onFavoriteClick = onFavoriteClick
                        )
                    }
                    
                    uiState.forecast?.let { forecast ->
                        HourlyForecastSection(hourlyForecast = forecast.hourlyForecast)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        DailyForecastSection(dailyForecast = forecast.dailyForecast)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FavoriteCitiesList(
                        cities = favoriteCities,
                        onCityClick = onCityClick,
                        currentCity = uiState.currentWeather?.cityName,
                        gpsLocationCity = uiState.gpsLocationCity
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}