package com.example.forecastweatherapp.ui.model

import com.example.forecastweatherapp.data.database.FavoriteCity
import com.example.forecastweatherapp.data.model.Condition
import com.example.forecastweatherapp.data.model.Current
import com.example.forecastweatherapp.data.model.Day
import com.example.forecastweatherapp.data.model.ForecastDay
import com.example.forecastweatherapp.data.model.Hour
import com.example.forecastweatherapp.data.model.WeatherResponse
import com.example.forecastweatherapp.data.model.SearchLocation
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

data class WeatherUiState(
    val isLoading: Boolean = false,
    val currentWeather: CurrentWeatherUiState? = null,
    val forecast: ForecastUiState? = null,
    val errorMessage: String? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isCurrentLocationFromGPS: Boolean = false,
    val gpsLocationCity: CurrentWeatherUiState? = null,
    val favoriteCities: List<FavoriteCity> = emptyList(),
    val isCurrentCityFavorite: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<LocationSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val timeZoneId: String = ZoneId.systemDefault().id,
    val localTime: String = "",
    val locationUiState: LocationUiState? = null
)

data class CurrentWeatherUiState(
    val cityName: String,
    val region: String,
    val country: String,
    val temperature: Double,
    val maxTemperature: Double,
    val minTemperature: Double,
    val condition: String,
    val conditionIcon: String,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: String,
    val uv: Double,
    val feelsLike: Double,
    val airQualityIndex: Int? = null,
    val isDay: Boolean,
    val localTime: String = "",
    val timeZoneId: String = ZoneId.systemDefault().id
)

data class ForecastUiState(
    val hourlyForecast: List<HourlyForecastUiState>,
    val dailyForecast: List<DailyForecastUiState>,
    val timeZoneId: String = ZoneId.systemDefault().id
)

data class HourlyForecastUiState(
    val time: String,
    val temperature: Double,
    val maxTemperature: Double,
    val minTemperature: Double,
    val condition: String,
    val conditionIcon: String,
    val chanceOfRain: Int,
    val humidity: Int,
    val windSpeed: Double,
    val airQualityIndex: Int?,
    val epochTime: Long
)

data class DailyForecastUiState(
    val date: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val condition: String,
    val conditionIcon: String,
    val chanceOfRain: Int,
    val sunrise: String,
    val sunset: String,
    val uv: Double
)

data class LocationSearchResult(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double
)

/**
 * Representa o estado de uma localização para a UI.
 * Usado para representar cidades favoritas e resultados de pesquisa de localização.
 */
data class LocationUiState(
    val id: String = "",
    val name: String = "",
    val region: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isFavorite: Boolean = false
)

// Funções de extensão para converter objetos do modelo em objetos de UI
fun WeatherResponse.toUiState(): WeatherUiState {
    val zoneId = ZoneId.of(location.tzId)
    
    // Obter temperaturas máxima e mínima do dia atual
    val maxTemp = this.forecast?.forecastday?.firstOrNull()?.day?.maxtempC ?: current.tempC
    val minTemp = this.forecast?.forecastday?.firstOrNull()?.day?.mintempC ?: current.tempC
    
    val currentWeatherUiState = CurrentWeatherUiState(
        cityName = location.name,
        region = location.region,
        country = location.country,
        temperature = current.tempC,
        maxTemperature = maxTemp,
        minTemperature = minTemp,
        condition = current.condition.text,
        conditionIcon = current.condition.icon,
        humidity = current.humidity,
        windSpeed = current.windKph,
        windDirection = current.windDir,
        uv = current.uv,
        feelsLike = current.feelslikeC,
        airQualityIndex = current.airQuality?.usEpaIndex,
        isDay = current.isDay == 1,
        localTime = location.localtime,
        timeZoneId = location.tzId
    )
    
    val forecastUiState = this.forecast?.let { forecast ->
        // Obter o horário atual na zona de horário da cidade pesquisada
        val currentTimeInCity = try {
            val localTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            LocalDateTime.parse(location.localtime, localTimeFormat)
                .atZone(zoneId)
                .toInstant()
                .epochSecond
        } catch (e: Exception) {
            System.currentTimeMillis() / 1000 // Fallback para horário do sistema em caso de erro
        }
        
        // Filtrar apenas as horas futuras em relação ao horário atual da cidade
        val hourlyForecast = forecast.forecastday.flatMap { day -> 
            day.hour.filter { hour -> 
                hour.timeEpoch > currentTimeInCity
            }.map { hour ->
                // Para cada hora, usamos as temperaturas máxima e mínima do dia correspondente
                hour.toUiState(zoneId, day.day.maxtempC, day.day.mintempC)
            }
        }.sortedBy { 
            it.epochTime
        }.take(12) // Obter mais horas para garantir que tenhamos pelo menos 6 após o filtro
        
        val dailyForecast = forecast.forecastday.map { it.toUiState(zoneId) }
        
        ForecastUiState(
            hourlyForecast = hourlyForecast,
            dailyForecast = dailyForecast,
            timeZoneId = location.tzId
        )
    }
    
    return WeatherUiState(
        isLoading = false,
        currentWeather = currentWeatherUiState,
        forecast = forecastUiState,
        errorMessage = null,
        timeZoneId = location.tzId,
        localTime = location.localtime
    )
}

fun Hour.toUiState(zoneId: ZoneId, maxTemperature: Double, minTemperature: Double): HourlyForecastUiState {
    // Formatação da hora considerando o fuso horário da cidade
    val formatter = DateTimeFormatterBuilder()
        .appendPattern("HH:mm")
        .toFormatter()
        .withZone(zoneId)
    
    val formattedTime = formatter.format(
        java.time.Instant.ofEpochSecond(timeEpoch)
    )

    return HourlyForecastUiState(
        time = formattedTime,
        temperature = tempC,
        maxTemperature = maxTemperature,
        minTemperature = minTemperature,
        condition = condition.text,
        conditionIcon = condition.icon,
        chanceOfRain = chanceOfRain,
        humidity = humidity,
        windSpeed = windKph,
        airQualityIndex = airQuality?.usEpaIndex,
        epochTime = timeEpoch
    )
}

fun ForecastDay.toUiState(zoneId: ZoneId): DailyForecastUiState {
    // Formatação da data considerando o fuso horário da cidade
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM", Locale("pt", "BR"))
        .withZone(zoneId)
    
    val dateInstant = java.time.Instant.ofEpochSecond(dateEpoch)
    val formattedDate = dateFormatter.format(dateInstant)

    return DailyForecastUiState(
        date = formattedDate,
        maxTemperature = day.maxtempC,
        minTemperature = day.mintempC,
        condition = day.condition.text,
        conditionIcon = day.condition.icon,
        chanceOfRain = day.dailyChanceOfRain,
        sunrise = astro.sunrise,
        sunset = astro.sunset,
        uv = day.uv
    )
}

fun com.example.forecastweatherapp.data.model.SearchLocation.toSearchResult(): LocationSearchResult {
    return LocationSearchResult(
        name = name,
        region = region,
        country = country,
        lat = lat,
        lon = lon
    )
} 