package com.example.forecastweatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.forecastweatherapp.data.database.FavoriteCity
import com.example.forecastweatherapp.data.location.LocationService
import com.example.forecastweatherapp.data.repository.WeatherRepository
import com.example.forecastweatherapp.ui.model.WeatherUiState
import com.example.forecastweatherapp.ui.model.toSearchResult
import com.example.forecastweatherapp.ui.model.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationService: LocationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeatherUiState(isLoading = true))
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    private var currentLocation: String? = null
    
    val favoriteCities = weatherRepository.getFavoriteCities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        checkLocationPermission()
        loadFavoriteCities()
        loadLastCity()
    }
    
    private fun checkLocationPermission() {
        _uiState.update { currentState ->
            currentState.copy(
                isLocationPermissionGranted = locationService.hasLocationPermission(),
                isLocationEnabled = locationService.isLocationEnabled()
            )
        }
        
        // Não carregamos o clima aqui mais, isso é feito em loadLastCity()
    }
    
    private fun loadFavoriteCities() {
        viewModelScope.launch {
            weatherRepository.getFavoriteCities().collectLatest { cities ->
                _uiState.update { it.copy(favoriteCities = cities) }
            }
        }
    }
    
    private fun loadLastCity() {
        val lastCity = weatherRepository.getLastCity()
        
        // Sempre carrega a localização atual do GPS em segundo plano para ter disponível na lista
        if (locationService.hasLocationPermission() && locationService.isLocationEnabled()) {
            updateGpsLocationInBackground()
        }
        
        // Carrega a última cidade pesquisada como cidade atual
        if (lastCity != null) {
            getWeatherForCity(lastCity)
        } else if (locationService.hasLocationPermission() && locationService.isLocationEnabled()) {
            // Se não houver cidade salva, usa a do GPS como cidade principal
            getWeatherFromCurrentLocation()
        }
    }
    
    /**
     * Atualiza a cidade do GPS em segundo plano sem mudar a cidade atualmente exibida
     */
    private fun updateGpsLocationInBackground() {
        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                
                if (location != null) {
                    val result = weatherRepository.getCurrentWeatherByCoordinates(
                        location.latitude, location.longitude
                    )
                    
                    result.onSuccess { response ->
                        _uiState.update { currentState ->
                            // Atualiza apenas o gpsLocationCity sem alterar a cidade atual exibida
                            val updatedState = response.toUiState()
                            currentState.copy(
                                gpsLocationCity = updatedState.currentWeather
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignora erros em segundo plano
            }
        }
    }
    
    fun getWeatherFromCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val location = locationService.getCurrentLocation()
                
                if (location != null) {
                    val result = weatherRepository.getCurrentWeatherByCoordinates(
                        location.latitude, location.longitude
                    )
                    
                    result.onSuccess { response ->
                        _uiState.update { 
                            val uiState = response.toUiState()
                            currentLocation = response.location.name
                            
                            // Salvar a cidade como última consultada
                            weatherRepository.saveLastCity(response.location.name)
                            
                            checkIfCityIsFavorite(response.location.name)
                            
                            loadForecast(response.location.name)
                            
                            // Marcar como vindo do GPS e armazenar a cidade do GPS
                            uiState.copy(
                                isCurrentLocationFromGPS = true,
                                gpsLocationCity = uiState.currentWeather
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                errorMessage = "Erro ao obter clima: ${error.localizedMessage}"
                            ) 
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Não foi possível obter a localização atual"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Erro: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }
    
    fun getWeatherForCity(cityName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = weatherRepository.getCurrentWeather(cityName)
                
                result.onSuccess { response ->
                    _uiState.update { currentState -> 
                        val uiState = response.toUiState()
                        currentLocation = response.location.name
                        
                        // Salvar a cidade como última consultada
                        weatherRepository.saveLastCity(response.location.name)
                        
                        checkIfCityIsFavorite(response.location.name)
                        
                        loadForecast(response.location.name)
                        
                        // Marcar como NÃO vindo do GPS (seleção manual)
                        // Manter o gpsLocationCity se já existir
                        uiState.copy(
                            isCurrentLocationFromGPS = false,
                            gpsLocationCity = currentState.gpsLocationCity
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Erro ao obter clima: ${error.localizedMessage}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Erro: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }
    
    private fun loadForecast(location: String) {
        viewModelScope.launch {
            try {
                val result = weatherRepository.getForecast(location, 7)
                
                result.onSuccess { response ->
                    _uiState.update { currentState ->
                        val weatherUiState = response.toUiState()
                        currentState.copy(
                            forecast = weatherUiState.forecast,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Erro ao obter previsão: ${error.localizedMessage}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Erro: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }
    
    fun searchLocations(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        // Cancelar pesquisa anterior
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
            return
        }
        
        searchJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSearching = true) }
                
                // Pequeno atraso para evitar muitas requisições enquanto digita
                delay(500)
                
                // Verificar se a coroutine foi cancelada antes de fazer a requisição
                if (!currentCoroutineContext().isActive) return@launch
                
                val result = weatherRepository.searchLocation(query)
                
                // Verificar novamente se a coroutine foi cancelada após o resultado
                if (!currentCoroutineContext().isActive) return@launch
                
                result.onSuccess { locations ->
                    _uiState.update { 
                        it.copy(
                            isSearching = false,
                            searchResults = locations.map { location -> location.toSearchResult() }
                        )
                    }
                }.onFailure { error ->
                    // Ignorar erros de cancelamento, que são esperados
                    if (error is kotlinx.coroutines.CancellationException) {
                        return@launch
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isSearching = false, 
                            errorMessage = "Erro na pesquisa: ${error.localizedMessage}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                // Ignorar exceções de cancelamento
                if (e is kotlinx.coroutines.CancellationException) {
                    return@launch
                }
                
                _uiState.update { 
                    it.copy(
                        isSearching = false, 
                        errorMessage = "Erro: ${e.localizedMessage}"
                    ) 
                }
            }
        }
    }
    
    fun toggleFavoriteCity() {
        val currentWeather = _uiState.value.currentWeather ?: return
        val isFavorite = _uiState.value.isCurrentCityFavorite
        
        viewModelScope.launch {
            if (isFavorite) {
                // Remover dos favoritos
                val city = FavoriteCity(
                    cityName = currentWeather.cityName,
                    region = currentWeather.region,
                    country = currentWeather.country,
                    latitude = 0.0, // Esses valores seriam preenchidos corretamente na implementação real
                    longitude = 0.0
                )
                weatherRepository.removeFavoriteCity(city)
            } else {
                // Adicionar aos favoritos
                val city = FavoriteCity(
                    cityName = currentWeather.cityName,
                    region = currentWeather.region,
                    country = currentWeather.country,
                    latitude = 0.0, // Esses valores seriam preenchidos corretamente na implementação real
                    longitude = 0.0
                )
                weatherRepository.addFavoriteCity(city)
            }
            
            _uiState.update { it.copy(isCurrentCityFavorite = !isFavorite) }
        }
    }
    
    private fun checkIfCityIsFavorite(cityName: String) {
        viewModelScope.launch {
            val isFavorite = weatherRepository.isCityFavorite(cityName)
            _uiState.update { it.copy(isCurrentCityFavorite = isFavorite) }
        }
    }
    
    fun clearSearchResults() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }
    
    class Factory(
        private val weatherRepository: WeatherRepository,
        private val locationService: LocationService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(weatherRepository, locationService) as T
            }
            throw IllegalArgumentException("ViewModel desconhecido")
        }
    }
} 