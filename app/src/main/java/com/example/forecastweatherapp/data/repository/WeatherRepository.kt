package com.example.forecastweatherapp.data.repository

import android.content.Context
import android.location.Location
import com.example.forecastweatherapp.data.database.FavoriteCity
import com.example.forecastweatherapp.data.database.WeatherDao
import com.example.forecastweatherapp.data.model.SearchLocation
import com.example.forecastweatherapp.data.model.WeatherResponse
import com.example.forecastweatherapp.data.network.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface WeatherRepository {
    // API
    suspend fun getCurrentWeather(location: String): Result<WeatherResponse>
    suspend fun getCurrentWeatherByCoordinates(lat: Double, lon: Double): Result<WeatherResponse>
    suspend fun getForecast(location: String, days: Int): Result<WeatherResponse>
    suspend fun searchLocation(query: String): Result<List<SearchLocation>>
    
    // Database
    suspend fun addFavoriteCity(city: FavoriteCity)
    suspend fun removeFavoriteCity(city: FavoriteCity)
    fun getFavoriteCities(): Flow<List<FavoriteCity>>
    suspend fun isCityFavorite(cityName: String): Boolean

    fun saveLastCity(cityName: String)
    fun getLastCity(): String?
}

class WeatherRepositoryImpl(
    private val weatherApiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val context: Context
) : WeatherRepository {
    
    companion object {
        private const val PREFS_NAME = "WeatherAppPreferences"
        private const val KEY_LAST_CITY = "last_city"
    }
    
    override suspend fun getCurrentWeather(location: String): Result<WeatherResponse> {
        return try {
            val response = weatherApiService.getCurrentWeather(location)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentWeatherByCoordinates(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val location = "$lat,$lon"
            val response = weatherApiService.getCurrentWeather(location)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getForecast(location: String, days: Int): Result<WeatherResponse> {
        return try {
            val response = weatherApiService.getForecast(location, days)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchLocation(query: String): Result<List<SearchLocation>> {
        return try {
            val response = weatherApiService.searchLocation(query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addFavoriteCity(city: FavoriteCity) {
        weatherDao.insertFavoriteCity(city)
    }
    
    override suspend fun removeFavoriteCity(city: FavoriteCity) {
        weatherDao.deleteFavoriteCity(city)
    }
    
    override fun getFavoriteCities(): Flow<List<FavoriteCity>> {
        return weatherDao.getAllFavoriteCities()
    }
    
    override suspend fun isCityFavorite(cityName: String): Boolean {
        return weatherDao.isCityFavorite(cityName)
    }

    override fun saveLastCity(cityName: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_LAST_CITY, cityName).apply()
    }

    override fun getLastCity(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_LAST_CITY, null)
    }
} 