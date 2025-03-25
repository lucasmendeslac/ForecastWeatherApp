package com.example.forecastweatherapp.data.network

import com.example.forecastweatherapp.data.model.SearchLocation
import com.example.forecastweatherapp.data.model.WeatherResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Serviço de API do Weather usando Ktor
 */
interface WeatherApiService {
    suspend fun getCurrentWeather(location: String): WeatherResponse
    suspend fun getForecast(location: String, days: Int = 7): WeatherResponse
    suspend fun searchLocation(query: String): List<SearchLocation>
}

class WeatherApiServiceImpl : WeatherApiService {
    private val BASE_URL = "api.weatherapi.com"
    private val API_VERSION = "v1"
    private val API_KEY = "5dc2e8cadffe4eebb24193425252403"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    override suspend fun getCurrentWeather(location: String): WeatherResponse {
        return client.get {
            url {
                buildWeatherApiUrl("current.json")
            }
            parameter("q", location)
            parameter("aqi", "yes")
        }.body()
    }

    override suspend fun getForecast(location: String, days: Int): WeatherResponse {
        return client.get {
            url {
                buildWeatherApiUrl("forecast.json")
            }
            parameter("q", location)
            parameter("days", days)
            parameter("aqi", "yes")
            parameter("alerts", "no")
        }.body()
    }

    override suspend fun searchLocation(query: String): List<SearchLocation> {
        return client.get {
            url {
                buildWeatherApiUrl("search.json")
            }
            parameter("q", query)
        }.body()
    }

    private fun URLBuilder.buildWeatherApiUrl(endpoint: String) {
        protocol = URLProtocol.HTTPS
        host = BASE_URL
        appendPathSegments(API_VERSION, endpoint)
        
        parameters.append("key", API_KEY)
    }
} 