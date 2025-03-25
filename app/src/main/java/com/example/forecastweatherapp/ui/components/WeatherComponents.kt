package com.example.forecastweatherapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.forecastweatherapp.R
import com.example.forecastweatherapp.data.database.FavoriteCity
import com.example.forecastweatherapp.ui.model.CurrentWeatherUiState
import com.example.forecastweatherapp.ui.model.DailyForecastUiState
import com.example.forecastweatherapp.ui.model.HourlyForecastUiState
import com.example.forecastweatherapp.ui.model.LocationSearchResult
import com.example.forecastweatherapp.ui.theme.Purple40

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = Purple40
        )
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Erro",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onLocationClick: () -> Unit,
    searchResults: List<LocationSearchResult>,
    onResultClick: (LocationSearchResult) -> Unit,
    isSearching: Boolean,
    onClearSearch: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit
) {
    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onSearch(query) },
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text("Pesquisar cidade...") },
        leadingIcon = { 
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ícone de pesquisa"
            )
        },
        trailingIcon = {
            IconButton(onClick = onLocationClick) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Usar localização atual"
                )
            }
        }
    ) {
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = Purple40
                )
            }
        } else if (searchResults.isEmpty() && query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum resultado encontrado")
            }
        } else {
            LazyColumn {
                items(searchResults) { result ->
                    SearchResultItem(result = result, onClick = { onResultClick(result) })
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: LocationSearchResult,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = result.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${result.region}, ${result.country}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Divider()
}

@Composable
fun CurrentWeatherCard(
    currentWeather: CurrentWeatherUiState,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentWeather.cityName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentWeather.region}, ${currentWeather.country}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Hora local: ${currentWeather.localTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verificar se é uma condição de céu limpo
                val isClearCondition = currentWeather.condition.lowercase().contains("clear") || 
                                      currentWeather.condition.lowercase().contains("limpo") ||
                                      currentWeather.condition.lowercase().contains("sunny") ||
                                      currentWeather.condition.lowercase().contains("ensolarado")
                
                val isNightCondition = currentWeather.conditionIcon.contains("night")
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    if (isClearCondition) {
                        // Usar ícones personalizados para céu limpo
                        Icon(
                            painter = painterResource(
                                id = if (isNightCondition) R.drawable.ic_moon else R.drawable.ic_sun
                            ),
                            contentDescription = currentWeather.condition,
                            modifier = Modifier.size(72.dp),
                            tint = if (isNightCondition) Color(0xFFFFFFE0) else Color(0xFFFFEB3B)
                        )
                    } else {
                        // Usar o ícone da API para outras condições
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https:${currentWeather.conditionIcon}")
                                .crossfade(true)
                                .build(),
                            contentDescription = currentWeather.condition,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentWeather.temperature.toInt()}°C",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = translateWeatherCondition(currentWeather.condition),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${currentWeather.minTemperature.toInt()}° / ${currentWeather.maxTemperature.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem(label = "Sensação", value = "${currentWeather.feelsLike.toInt()}°C")
                WeatherInfoItem(label = "Umidade", value = "${currentWeather.humidity}%")
                WeatherInfoItem(label = "Vento", value = "${currentWeather.windSpeed} km/h")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem(label = "UV", value = currentWeather.uv.toString())
                currentWeather.airQualityIndex?.let { index ->
                    WeatherInfoItem(
                        label = "Qualidade do Ar", 
                        value = aqiToText(index)
                    )
                } ?: WeatherInfoItem(
                    label = "Qualidade do Ar", 
                    value = "Indisponível"
                )
            }
        }
    }
}

@Composable
fun WeatherInfoItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HourlyForecastSection(hourlyForecast: List<HourlyForecastUiState>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Próximas Horas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (hourlyForecast.isEmpty()) {
                Text(
                    text = "Previsão horária indisponível",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(hourlyForecast.take(6)) { hour ->
                        HourlyForecastItem(hour = hour)
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(hour: HourlyForecastUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = hour.time,
            style = MaterialTheme.typography.bodySmall
        )
        
        // Verificar se é uma condição de céu limpo
        val isClearCondition = hour.condition.lowercase().contains("clear") || 
                              hour.condition.lowercase().contains("limpo") ||
                              hour.condition.lowercase().contains("sunny") ||
                              hour.condition.lowercase().contains("ensolarado")
        
        val isNightCondition = hour.conditionIcon.contains("night")
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(48.dp)
        ) {
            if (isClearCondition) {
                // Usar ícones personalizados para céu limpo
                Icon(
                    painter = painterResource(
                        id = if (isNightCondition) R.drawable.ic_moon else R.drawable.ic_sun
                    ),
                    contentDescription = translateWeatherCondition(hour.condition),
                    modifier = Modifier.size(44.dp),
                    tint = if (isNightCondition) Color(0xFFFFFFE0) else Color(0xFFFFEB3B) // Cores correspondentes aos ícones
                )
            } else {
                // Usar o ícone da API para outras condições
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https:${hour.conditionIcon}")
                        .crossfade(true)
                        .build(),
                    contentDescription = translateWeatherCondition(hour.condition),
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        
        Text(
            text = "${hour.temperature.toInt()}°C",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "${hour.minTemperature.toInt()}° / ${hour.maxTemperature.toInt()}°",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_water_drop),
                contentDescription = "Chance de chuva",
                modifier = Modifier.size(14.dp),
                tint = Color.Blue
            )
            Text(
                text = "${hour.chanceOfRain}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        hour.airQualityIndex?.let {
            Text(
                text = "AQI: ${aqiToShortText(it)}",
                style = MaterialTheme.typography.bodySmall,
                color = aqiToColor(it)
            )
        }
    }
}

@Composable
fun DailyForecastSection(dailyForecast: List<DailyForecastUiState>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Próximos 7 Dias",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            dailyForecast.forEachIndexed { index, day ->
                DailyForecastItem(day = day)
                if (index < dailyForecast.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DailyForecastItem(day: DailyForecastUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Coluna para data e condição, posicionada à esquerda
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(160.dp)
        ) {
            Text(
                text = formatDate(day.date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = translateWeatherCondition(day.condition),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Verificar se é uma condição de céu limpo
        val isClearCondition = day.condition.lowercase().contains("clear") || 
                              day.condition.lowercase().contains("limpo") ||
                              day.condition.lowercase().contains("sunny") ||
                              day.condition.lowercase().contains("ensolarado")
        
        val isNightCondition = day.conditionIcon.contains("night")
        
        // Ícone centralizado horizontalmente
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.Center)
        ) {
            if (isClearCondition) {
                // Usar ícones personalizados para céu limpo
                Icon(
                    painter = painterResource(
                        id = if (isNightCondition) R.drawable.ic_moon else R.drawable.ic_sun
                    ),
                    contentDescription = day.condition,
                    modifier = Modifier.size(40.dp),
                    tint = if (isNightCondition) Color(0xFFFFFFE0) else Color(0xFFFFEB3B)
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https:${day.conditionIcon}")
                        .crossfade(true)
                        .build(),
                    contentDescription = day.condition,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        // Informações de temperatura e chuva à direita
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        ) {
            Text(
                text = "🌡️ ${day.minTemperature.toInt()}° / ${day.maxTemperature.toInt()}°",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "💧 ${day.chanceOfRain}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FavoriteCitiesList(
    cities: List<FavoriteCity>,
    onCityClick: (String) -> Unit,
    currentCity: String? = null,
    gpsLocationCity: CurrentWeatherUiState? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Cidades",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mostrar a cidade do GPS no topo sempre que disponível
            if (gpsLocationCity != null) {
                FavoriteCityItem(
                    cityName = gpsLocationCity.cityName,
                    region = gpsLocationCity.region,
                    country = gpsLocationCity.country,
                    onClick = { onCityClick(gpsLocationCity.cityName) },
                    isLocationCity = true,
                    isCurrentCity = currentCity == gpsLocationCity.cityName
                )
                
                if (cities.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            if (cities.isEmpty() && gpsLocationCity == null) {
                Text(
                    text = "Nenhuma cidade favorita adicionada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                cities.forEachIndexed { index, city ->
                    // Não mostrar a cidade novamente se ela já foi exibida como cidade do GPS
                    if (gpsLocationCity != null && city.cityName == gpsLocationCity.cityName) {
                        return@forEachIndexed
                    }
                    
                    val isCurrentCity = currentCity == city.cityName
                    FavoriteCityItem(
                        cityName = city.cityName,
                        region = city.region,
                        country = city.country,
                        onClick = { onCityClick(city.cityName) },
                        isLocationCity = false,
                        isCurrentCity = isCurrentCity
                    )
                    if (index < cities.size - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteCityItem(
    cityName: String,
    region: String,
    country: String,
    onClick: () -> Unit,
    isLocationCity: Boolean = false,
    isCurrentCity: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLocationCity) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = "Localização atual",
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$region, $country",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isCurrentCity) {
            Text(
                text = "Atual",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// Funções utilitárias
private fun aqiToText(aqi: Int?): String {
    return when (aqi) {
        1 -> "Bom"
        2 -> "Moderado"
        3 -> "Insalubre para sensíveis"
        4 -> "Insalubre"
        5 -> "Muito insalubre"
        6 -> "Perigoso"
        else -> "Desconhecido"
    }
}

private fun aqiToShortText(aqi: Int?): String {
    return when (aqi) {
        1 -> "Bom"
        2 -> "Mod"
        3 -> "Ins"
        4 -> "Ins+"
        5 -> "M.Ins"
        6 -> "Perig"
        else -> "?"
    }
}

private fun aqiToColor(aqi: Int?): Color {
    return when (aqi) {
        1 -> Color.Green
        2 -> Color.Yellow
        3 -> Color.Magenta
        4 -> Color.Red
        5 -> Color(0xFF8B0000)  // Dark Red
        6 -> Color(0xFF800080)  // Purple
        else -> Color.Gray
    }
}

private fun formatDate(date: String): String {
    // Simplificado para exemplo, uma implementação real usaria DateFormat
    return date.substring(5)  // Retorna apenas MM-DD
}

// Função para traduzir condições climáticas do inglês para o português
private fun translateWeatherCondition(condition: String): String {
    // Normaliza o texto para minúsculo e remove espaços extras
    val normalizedCondition = condition.trim().lowercase()
    
    return when {
        normalizedCondition.contains("sunny") -> "Ensolarado"
        normalizedCondition.contains("clear") -> "Céu limpo"
        normalizedCondition.contains("partly cloudy") -> "Parcialmente nublado"
        normalizedCondition.contains("cloudy") && !normalizedCondition.contains("partly") -> "Nublado"
        normalizedCondition.contains("overcast") -> "Encoberto"
        normalizedCondition.contains("mist") -> "Névoa"
        normalizedCondition.contains("patchy rain nearby") || normalizedCondition.contains("patchy rain possible") -> "Possibilidade de chuva isolada"
        normalizedCondition.contains("patchy snow possible") -> "Possibilidade de neve isolada"
        normalizedCondition.contains("patchy sleet possible") -> "Possibilidade de granizo isolado"
        normalizedCondition.contains("patchy freezing drizzle possible") -> "Possibilidade de garoa congelante isolada"
        normalizedCondition.contains("thundery outbreaks possible") -> "Possibilidade de trovoadas"
        normalizedCondition.contains("blowing snow") -> "Neve com ventos fortes"
        normalizedCondition.contains("blizzard") -> "Nevasca"
        normalizedCondition.contains("fog") && !normalizedCondition.contains("freezing") -> "Nevoeiro"
        normalizedCondition.contains("freezing fog") -> "Nevoeiro congelante"
        normalizedCondition.contains("patchy light drizzle") -> "Garoa leve isolada"
        normalizedCondition.contains("light drizzle") && !normalizedCondition.contains("patchy") -> "Garoa leve"
        normalizedCondition.contains("freezing drizzle") && !normalizedCondition.contains("heavy") -> "Garoa congelante"
        normalizedCondition.contains("heavy freezing drizzle") -> "Garoa congelante forte"
        normalizedCondition.contains("patchy light rain") -> "Chuva leve isolada"
        normalizedCondition.contains("light rain") && !normalizedCondition.contains("patchy") && !normalizedCondition.contains("thunder") -> "Chuva leve"
        normalizedCondition.contains("moderate rain at times") -> "Chuva moderada em períodos"
        normalizedCondition.contains("moderate rain") && !normalizedCondition.contains("at times") -> "Chuva moderada"
        normalizedCondition.contains("heavy rain at times") -> "Chuva forte em períodos"
        normalizedCondition.contains("heavy rain") && !normalizedCondition.contains("at times") -> "Chuva forte"
        normalizedCondition.contains("light freezing rain") -> "Chuva congelante leve"
        normalizedCondition.contains("moderate or heavy freezing rain") -> "Chuva congelante moderada a forte"
        normalizedCondition.contains("light sleet") && !normalizedCondition.contains("shower") -> "Granizo leve"
        normalizedCondition.contains("moderate or heavy sleet") && !normalizedCondition.contains("shower") -> "Granizo moderado a forte"
        normalizedCondition.contains("patchy light snow") -> "Neve leve isolada"
        normalizedCondition.contains("light snow") && !normalizedCondition.contains("patchy") && !normalizedCondition.contains("shower") -> "Neve leve"
        normalizedCondition.contains("patchy moderate snow") -> "Neve moderada isolada"
        normalizedCondition.contains("moderate snow") && !normalizedCondition.contains("patchy") -> "Neve moderada"
        normalizedCondition.contains("patchy heavy snow") -> "Neve forte isolada"
        normalizedCondition.contains("heavy snow") && !normalizedCondition.contains("patchy") -> "Neve forte"
        normalizedCondition.contains("ice pellets") && !normalizedCondition.contains("shower") -> "Pelotas de gelo"
        normalizedCondition.contains("light rain shower") -> "Pancada de chuva leve"
        normalizedCondition.contains("moderate or heavy rain shower") -> "Pancada de chuva moderada a forte"
        normalizedCondition.contains("torrential rain shower") -> "Pancada de chuva torrencial"
        normalizedCondition.contains("light sleet showers") -> "Pancada de granizo leve"
        normalizedCondition.contains("moderate or heavy sleet showers") -> "Pancada de granizo moderada a forte"
        normalizedCondition.contains("light snow showers") -> "Pancada de neve leve"
        normalizedCondition.contains("moderate or heavy snow showers") -> "Pancada de neve moderada a forte"
        normalizedCondition.contains("light showers of ice pellets") -> "Pancada leve de pelotas de gelo"
        normalizedCondition.contains("moderate or heavy showers of ice pellets") -> "Pancada moderada a forte de pelotas de gelo"
        normalizedCondition.contains("patchy light rain with thunder") -> "Chuva leve isolada com trovoadas"
        normalizedCondition.contains("moderate or heavy rain with thunder") -> "Chuva moderada a forte com trovoadas"
        normalizedCondition.contains("patchy light snow with thunder") -> "Neve leve isolada com trovoadas"
        normalizedCondition.contains("moderate or heavy snow with thunder") -> "Neve moderada a forte com trovoadas"
        else -> condition // Retorna o original se não houver tradução
    }
} 