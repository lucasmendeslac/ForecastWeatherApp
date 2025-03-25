package com.example.forecastweatherapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_city")
data class FavoriteCity(
    @PrimaryKey val cityName: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
) 