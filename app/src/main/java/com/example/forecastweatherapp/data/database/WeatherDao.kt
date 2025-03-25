package com.example.forecastweatherapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(favoriteCity: FavoriteCity)

    @Delete
    suspend fun deleteFavoriteCity(favoriteCity: FavoriteCity)

    @Query("SELECT * FROM favorite_city ORDER BY timestamp DESC")
    fun getAllFavoriteCities(): Flow<List<FavoriteCity>>

    @Query("SELECT * FROM favorite_city WHERE cityName = :cityName LIMIT 1")
    suspend fun getFavoriteCity(cityName: String): FavoriteCity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_city WHERE cityName = :cityName LIMIT 1)")
    suspend fun isCityFavorite(cityName: String): Boolean
} 