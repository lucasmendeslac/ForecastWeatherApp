package com.example.forecastweatherapp

import android.app.Application
import com.example.forecastweatherapp.data.database.WeatherDatabase
import com.example.forecastweatherapp.data.location.LocationService
import com.example.forecastweatherapp.data.location.LocationServiceImpl
import com.example.forecastweatherapp.data.network.WeatherApiService
import com.example.forecastweatherapp.data.network.WeatherApiServiceImpl
import com.example.forecastweatherapp.data.repository.WeatherRepository
import com.example.forecastweatherapp.data.repository.WeatherRepositoryImpl

class WeatherApplication : Application() {
    
    private val database by lazy { WeatherDatabase.getInstance(this) }
    private val weatherDao by lazy { database.weatherDao }
    
    // Network
    val weatherApiService: WeatherApiService by lazy { WeatherApiServiceImpl() }
    
    // Location
    val locationService: LocationService by lazy { LocationServiceImpl(this) }
    
    // Repository
    val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(weatherApiService, weatherDao, this)
    }
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
    
    companion object {
        private lateinit var INSTANCE: WeatherApplication
        
        fun getInstance(): WeatherApplication = INSTANCE
    }
} 