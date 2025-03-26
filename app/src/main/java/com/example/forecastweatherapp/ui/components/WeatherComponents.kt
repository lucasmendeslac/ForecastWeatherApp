package com.example.forecastweatherapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.forecastweatherapp.R
import com.example.forecastweatherapp.data.database.FavoriteCity
import com.example.forecastweatherapp.ui.model.*
import com.example.forecastweatherapp.ui.theme.Purple40
import java.time.LocalDate

/**
 * Arquivo que contém todos os componentes de UI relacionados à exibição de dados meteorológicos.
 * Implementa a interface de usuário para exibição de previsão do tempo atual, horária e diária,
 * bem como componentes auxiliares como barra de pesquisa, gerenciamento de cidades favoritas,
 * e informações detalhadas sobre índice UV.
 */

/**
 * Exibe uma tela de carregamento com um indicador de progresso circular.
 * Esta tela é mostrada enquanto os dados meteorológicos estão sendo carregados.
 */
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

/**
 * Exibe uma tela de erro quando ocorre algum problema no carregamento dos dados.
 * 
 * @param message A mensagem de erro a ser exibida para o usuário
 */
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

/**
 * Barra de pesquisa para localização de cidades.
 * Permite ao usuário digitar e pesquisar por cidades para obter informações meteorológicas.
 * 
 * @param searchQuery Texto atual da pesquisa
 * @param onSearchQueryChange Callback chamado quando o texto da pesquisa é alterado
 * @param onSearch Callback chamado quando o usuário confirma a pesquisa
 * @param isSearching Indica se a pesquisa está em andamento
 * @param searchResults Lista de resultados da pesquisa
 * @param onSearchActiveChange Callback chamado quando o estado ativo da pesquisa muda
 * @param onSearchResultSelected Callback chamado quando um resultado é selecionado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    isSearching: Boolean,
    searchResults: List<LocationSearchResult>,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchResultSelected: (LocationSearchResult) -> Unit = {}
) {
    var active by remember { mutableStateOf(false) }
    
    // Garantir que o valor de active seja controlado tanto internamente quanto externamente
    LaunchedEffect(active) {
        onSearchActiveChange(active)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Usar um Card para criar um efeito de elevação mais uniforme e agradável
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { newQuery ->
                    // Garante que a mudança de texto é processada e atualizada corretamente
                    onSearchQueryChange(newQuery)
                },
                onSearch = { 
                    // Evita pesquisas com string vazia que podem causar erros
                    if (it.isNotBlank()) {
                        try {
                            onSearch(it)
                        } catch (e: Exception) {
                            // Captura exceções para evitar crashes por coroutines canceladas
                        }
                    }
                },
                active = active,
                onActiveChange = { newActiveState ->
                    active = newActiveState
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Buscar cidade...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pesquisar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (active && searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    dividerColor = MaterialTheme.colorScheme.surfaceVariant,
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface
                        )
                        .padding(16.dp)
                ) {
                    if (isSearching) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pesquisando \"$searchQuery\"...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Nenhuma cidade encontrada",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Tente um termo diferente de \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (searchResults.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { result ->
                                SearchResultItem(
                                    result = result,
                                    onClick = {
                                        // Primeiro processa a seleção e depois fecha a barra
                                        onSearchResultSelected(result)
                                        
                                        // Desativa a barra após um pequeno atraso para garantir que a seleção foi processada
                                        active = false
                                    }
                                )
                            }
                        }
                    } else {
                        // Estado vazio quando não há pesquisa
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Digite o nome de uma cidade para pesquisar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item individual de resultado de pesquisa de cidade.
 * 
 * @param result Dados do resultado da pesquisa
 * @param onClick Callback para quando o item é clicado
 */
@Composable
private fun SearchResultItem(
    result: LocationSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                // Garantir que o onClick seja chamado mesmo em caso de erros
                try {
                    onClick()
                } catch (e: Exception) {
                    // Log de erro apenas por segurança
                    e.printStackTrace()
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${result.region}, ${result.country}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Selecionar",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Card principal que exibe as informações do clima atual.
 * Mostra temperatura, condição climática, sensação térmica e outros dados relevantes.
 * Também apresenta um botão para adicionar/remover a cidade dos favoritos.
 * 
 * @param currentWeather Objeto contendo todos os dados da previsão atual
 * @param isFavorite Indica se a cidade está na lista de favoritos
 * @param onFavoriteClick Callback chamado quando o usuário clica no botão de favorito
 */
@Composable
fun CurrentWeatherCard(
    currentWeather: CurrentWeatherUiState,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    // Determina a cor de fundo baseada no período do dia e condição climática
    val isDay = currentWeather.isDay
    val isRainy = currentWeather.condition.lowercase().contains("rain") || 
                  currentWeather.condition.lowercase().contains("shower") ||
                  currentWeather.condition.lowercase().contains("drizzle")
    val isClear = currentWeather.condition.lowercase().contains("clear") || 
                 currentWeather.condition.lowercase().contains("sunny")
    val isCloudy = currentWeather.condition.lowercase().contains("cloud") || 
                  currentWeather.condition.lowercase().contains("overcast")
    
    // Define cores para o gradiente com base nas condições
    val startColor = when {
        !isDay -> Color(0xFF1A237E) // Noite - azul escuro
        isRainy -> Color(0xFF0D47A1) // Chuva - azul médio
        isCloudy -> Color(0xFF546E7A) // Nublado - cinza azulado
        isClear -> Color(0xFF039BE5) // Dia claro - azul céu
        else -> Color(0xFF42A5F5) // Padrão - azul médio
    }
    
    val endColor = when {
        !isDay -> Color(0xFF283593)  // Noite - roxo escuro
        isRainy -> Color(0xFF1565C0)  // Chuva - azul mais escuro
        isCloudy -> Color(0xFF78909C)  // Nublado - cinza mais claro
        isClear -> Color(0xFF29B6F6)  // Dia claro - azul claro
        else -> Color(0xFF64B5F6)  // Padrão - azul claro
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp, // Elevação aumentada para mais profundidade
            pressedElevation = 12.dp // Elevação quando pressionado
        ),
        shape = RoundedCornerShape(24.dp) // Bordas mais arredondadas
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(startColor, endColor)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp), // Padding maior para mais espaço
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
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${currentWeather.region}, ${currentWeather.country}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Hora local: ${currentWeather.localTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (isFavorite) Color(0x33FF0000) else Color(0x33FFFFFF),
                                shape = CircleShape
                            )
                            .clickable(onClick = onFavoriteClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
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
                    
                    // Verificar se é uma condição de chuva forte
                    val isHeavyRainCondition = currentWeather.condition.lowercase().contains("heavy rain") ||
                                              currentWeather.condition.lowercase().contains("chuva forte") ||
                                              currentWeather.condition.lowercase().contains("thunderstorm") ||
                                              currentWeather.condition.lowercase().contains("tempestade")
                    
                    val isNightCondition = currentWeather.conditionIcon.contains("night")
                    
                    // Ícone sem efeitos de sombra
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .padding(4.dp)
                    ) {
                        if (isClearCondition) {
                            // Usar ícones personalizados para céu limpo
                            Icon(
                                painter = painterResource(
                                    id = if (isNightCondition) R.drawable.ic_moon else R.drawable.ic_sun
                                ),
                                contentDescription = currentWeather.condition,
                                modifier = Modifier.size(76.dp),
                                tint = if (isNightCondition) Color(0xFFFFFFE0) else Color(0xFFFFEB3B)
                            )
                        } else if (isHeavyRainCondition) {
                            // Usar ícone personalizado para chuva forte
                            Icon(
                                painter = painterResource(id = R.drawable.ic_heavy_rain),
                                contentDescription = currentWeather.condition,
                                modifier = Modifier.size(76.dp),
                                tint = Color(0xFF1976D2)
                            )
                        } else {
                            // Usar o ícone da API para outras condições
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https:${currentWeather.conditionIcon}")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = currentWeather.condition,
                                modifier = Modifier.size(76.dp)
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${currentWeather.temperature.toInt()}°",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = translateWeatherCondition(currentWeather.condition),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${currentWeather.minTemperature.toInt()}° / ${currentWeather.maxTemperature.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Linha de divisão estilizada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Indicadores de clima com ícones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetailItem(
                        iconText = "🌡️", 
                        label = "Sensação", 
                        value = "${currentWeather.feelsLike.toInt()}°C"
                    )
                    WeatherDetailItem(
                        iconText = "💧", 
                        label = "Umidade", 
                        value = "${currentWeather.humidity}%"
                    )
                    WeatherDetailItem(
                        iconText = "🌬️", 
                        label = "Vento", 
                        value = "${currentWeather.windSpeed} km/h"
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherInfoItem(
                        label = "UV", 
                        value = currentWeather.uv.toString(),
                        description = uvIndexToDescription(currentWeather.uv),
                        textColor = Color.White
                    )
                    currentWeather.airQualityIndex?.let { index ->
                        WeatherInfoItem(
                            label = "Qualidade do Ar", 
                            value = aqiToText(index),
                            textColor = Color.White
                        )
                    } ?: WeatherInfoItem(
                        label = "Qualidade do Ar", 
                        value = "Indisponível",
                        textColor = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Componente que exibe um detalhe do clima com ícone, rótulo e valor.
 * Usado para exibir informações como sensação térmica, umidade e vento.
 * 
 * @param iconText Emoji ou texto a ser usado como ícone
 * @param label Nome/rótulo do detalhe
 * @param value Valor a ser exibido
 */
@Composable
fun WeatherDetailItem(
    iconText: String,
    label: String, 
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = iconText,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f) // Sempre branco com transparência para modo claro/escuro
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White // Sempre branco para garantir legibilidade no modo claro/escuro
        )
    }
}

/**
 * Componente que exibe uma informação meteorológica específica com rótulo e valor.
 * Quando o tipo de informação é o índice UV, permite clicar para ver detalhes adicionais.
 * 
 * @param label Nome/rótulo do tipo de informação (ex: "Umidade", "UV", etc)
 * @param description Descrição opcional (usada para o índice UV)
 * @param textColor Cor do texto (padrão é a cor de texto do tema atual)
 */
@Composable
fun WeatherInfoItem(
    label: String, 
    value: String,
    description: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var showDetails by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (label == "UV" || label == "Qualidade do Ar") Modifier.clickable { showDetails = !showDetails } else Modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.8f) // Alta opacidade para melhor visibilidade
        )
        
        // Para índice UV, adicionamos destaque especial com fundo colorido
        if (label == "UV") {
            // Determinar se é uma interface clara ou escura para ajustar a visibilidade
            val isLightColor = textColor == Color.White
            
            // Caixa com fundo colorido para destacar o valor do UV
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = getUvIndexColor(label, value).copy(alpha = if (isLightColor) 0.3f else 0.2f),
                        shape = RoundedCornerShape(8.dp) // Bordas mais arredondadas
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp) // Padding maior
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy( // Fonte maior
                        fontWeight = FontWeight.ExtraBold,
                        shadow = Shadow( // Sombra para melhorar contraste em qualquer fundo
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    ),
                    color = if (isLightColor) Color.White else getUvIndexColor(label, value) // Cor adaptativa
                )
            }
        } else if (label == "Qualidade do Ar") {
            // Para qualidade do ar, apenas texto em negrito sem fundo colorido
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (value != "Indisponível") aqiToColor(aqiFromText(value)).copy(alpha = 0.9f) else textColor
            )
        } else {
            // Para outros valores, exibição normal
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy( // Fonte maior para descrição
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow( // Sombra para legibilidade
                        color = Color.Black.copy(alpha = 0.2f),
                        offset = Offset(0.5f, 0.5f),
                        blurRadius = 1f
                    )
                ),
                color = if (textColor == Color.White) Color.White else getUvIndexColor(label, value),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp) // Padding maior
            )
        }
        
        if (label == "UV" && showDetails) {
            val uvValue = value.toDoubleOrNull() ?: 0.0
            PopupInfoDialog(
                title = "Índice UV: ${getUvIndexLevel(uvValue)}",
                valueCircle = uvValue.toString(),
                circleColor = getUvIndexColor(label, value),
                content = getUvDetailedDescription(uvValue),
                onDismiss = { showDetails = false }
            )
        } else if (label == "Qualidade do Ar" && showDetails) {
            // Diálogo para informações detalhadas de qualidade do ar
            val aqiValue = aqiFromText(value)
            PopupInfoDialog(
                title = "Qualidade do Ar: $value",
                valueCircle = aqiValue.toString(),
                circleColor = aqiToColor(aqiValue),
                content = getAqiHealthImpact(aqiValue) + "\n\n" + getAqiRecommendation(aqiValue),
                onDismiss = { showDetails = false }
            )
        }
    }
}

/**
 * Seção que exibe a previsão horária para as próximas horas.
 * Mostra uma lista horizontal com temperatura e condição climática para cada hora,
 * incluindo informações detalhadas como umidade, vento, chance de chuva e índice UV/qualidade do ar.
 * 
 * @param hourlyForecast Lista de objetos contendo a previsão para cada hora
 */
@Composable
fun HourlyForecastSection(hourlyForecast: List<HourlyForecastUiState>) {
    // Card com degradê suave no fundo e bordas arredondadas para aparência mais moderna
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp)), // Sombra mais suave para efeito de profundidade
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título com indicador de dia/noite em formato de degradê para representação visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximas Horas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Pequeno indicador visual que representa a transição dia/noite com degradê
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF64B5F6), // Azul claro para dia
                                    Color(0xFF1A237E)  // Azul escuro para noite
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (hourlyForecast.isEmpty()) {
                Text(
                    text = "Previsão horária indisponível",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Removido o texto "Arraste para ver mais horas →"
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp), // Aumentando o espaçamento entre os cartões
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp) // Adicionando padding externo
                ) {
                    items(hourlyForecast.take(12)) { hour -> // Mostrando até 12 horas para dar mais opções
                        HourlyForecastItem(hour = hour)
                    }
                }
            }
        }
    }
}

/**
 * Item individual da previsão horária.
 * Exibe hora, temperatura, ícone da condição climática e informações detalhadas como
 * umidade, velocidade do vento, índice UV e qualidade do ar.
 * 
 * @param hour Objeto contendo os dados de previsão para uma hora específica
 */
@Composable
fun HourlyForecastItem(hour: HourlyForecastUiState) {
    // Determinar cores baseadas no período (dia/noite) para criar cards personalizados
    val isNight = hour.conditionIcon.contains("night")
    // Criando um degradê vertical que vai de uma cor mais intensa para uma mais clara
    val containerStartColor = if (isNight) Color(0xFF1A237E).copy(alpha = 0.8f) else Color(0xFF42A5F5).copy(alpha = 0.8f)
    val containerEndColor = if (isNight) Color(0xFF283593).copy(alpha = 0.8f) else Color(0xFF90CAF9).copy(alpha = 0.8f)
    
    Card(
        modifier = Modifier
            .width(160.dp) // Card mais largo para acomodar mais informações
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Fundo transparente para mostrar o degradê
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(containerStartColor, containerEndColor)
                    )
                )
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cabeçalho com hora
                Text(
                    text = hour.time,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.2f),
                            offset = Offset(1f, 1f),
                            blurRadius = 1f
                        )
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Linha com ícone e temperatura
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Verificar condição para usar ícone adequado
                    val isClearCondition = hour.condition.lowercase().contains("clear") || 
                                        hour.condition.lowercase().contains("limpo") ||
                                        hour.condition.lowercase().contains("sunny") ||
                                        hour.condition.lowercase().contains("ensolarado")
                    
                    val isNightCondition = hour.conditionIcon.contains("night")
                    
                    // Círculo de fundo com degradê radial para o ícone
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f), // Mais opaco no centro
                                        Color.Transparent // Transparente nas bordas
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        if (isClearCondition) {
                            // Emoji com tamanho adequado para evitar distorções
                            Text(
                                text = if (isNightCondition) "🌙" else "☀️",
                                style = MaterialTheme.typography.headlineMedium, // Tamanho ajustado para evitar sombras estranhas
                                modifier = Modifier.padding(4.dp)
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
                    
                    // Temperatura com Min/Max
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${hour.temperature.toInt()}°C",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${hour.minTemperature.toInt()}° / ${hour.maxTemperature.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Divisor
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    color = Color.White.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Informações adicionais em duas colunas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Coluna 1: Umidade e Vento
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Umidade
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "💧",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${hour.humidity}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Vento
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🌬️",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${hour.windSpeed.toInt()} km/h",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Coluna 2: Chance de Chuva e Índice UV/AQI
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        // Chance de chuva
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${hour.chanceOfRain}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                            Text(
                                text = "🌧️",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Qualidade do ar OU Índice UV
                        hour.airQualityIndex?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = aqiToColor(it).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "AQI: ${aqiToShortText(it)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        } ?: Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "UV",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                            // Usar o valor do índice UV da previsão diária (aproximação)
                            // Na prática, esta informação deveria vir da API
                            Text(
                                text = "~",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Seção que exibe a previsão diária para os próximos dias.
 * Mostra uma lista vertical com dados meteorológicos para cada dia.
 * 
 * @param dailyForecast Lista de objetos contendo a previsão para cada dia
 */
@Composable
fun DailyForecastSection(dailyForecast: List<DailyForecastUiState>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(20.dp), // Bordas mais arredondadas
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título com ícone de calendário
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Próximos 7 Dias",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Linha decorativa gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            dailyForecast.forEachIndexed { index, day ->
                DailyForecastItem(day = day)
                if (index < dailyForecast.size - 1) {
                    // Separador mais sutil
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * Item individual da previsão diária.
 * Exibe data, temperatura mínima e máxima, ícone da condição climática, chance de chuva e índice UV.
 * Inclui detalhes como umidade, velocidade do vento e índice de qualidade do ar.
 * O índice UV é clicável e mostra informações detalhadas sobre proteção.
 * 
 * @param day Objeto contendo os dados de previsão para um dia específico
 */
@Composable
fun DailyForecastItem(day: DailyForecastUiState) {
    var showUvDetails by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Parte principal - sempre visível
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coluna para data e condição
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                ) {
                    Text(
                        text = day.date,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = translateWeatherCondition(day.condition),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Ícone de condição climática centralizado
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.3f)
                        .size(56.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    // Verificar se é uma condição de céu limpo
                    val isClearCondition = day.condition.lowercase().contains("clear") || 
                                          day.condition.lowercase().contains("limpo") ||
                                          day.condition.lowercase().contains("sunny") ||
                                          day.condition.lowercase().contains("ensolarado")
                    
                    val isNightCondition = day.conditionIcon.contains("night")
                    
                    // Círculo de fundo para o ícone
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        if (isNightCondition) 
                                            Color(0xFF1A237E).copy(alpha = 0.2f) 
                                        else 
                                            Color(0xFF64B5F6).copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    if (isClearCondition) {
                        // Usar emojis para céu limpo
                        Text(
                            text = if (isNightCondition) "🌙" else "☀️",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(4.dp)
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https:${day.conditionIcon}")
                                .crossfade(true)
                                .build(),
                            contentDescription = day.condition,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                
                // Informações de temperatura e chuva à direita
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .weight(0.35f)
                ) {
                    // Temperatura com ícone de termômetro
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🌡️",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // Temperaturas com visualização de gradiente
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(24.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF64B5F6), // Azul para mínima
                                            Color(0xFFF44336)  // Vermelho para máxima
                                        ),
                                        startX = 0f,
                                        endX = 90f
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day.minTemperature.toInt()}° / ${day.maxTemperature.toInt()}°",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Chance de chuva com visualização
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Box com transparência baseada na chance de chuva
                        val rainOpacity = (day.chanceOfRain.toFloat() / 100f).coerceIn(0.3f, 0.9f)
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(24.dp)
                                .background(
                                    color = Color(0xFF1565C0).copy(alpha = rainOpacity),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🌧️",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${day.chanceOfRain}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Parte expandida - visível apenas quando o card é clicado
            if (expanded) {
                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Título da seção
                    Text(
                        text = "Informações Detalhadas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Grid de informações detalhadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Coluna esquerda
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            DetailItem(
                                icon = "🌞",
                                label = "Nascer do Sol",
                                value = day.sunrise
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            DetailItem(
                                icon = "🌙",
                                label = "Pôr do Sol",
                                value = day.sunset
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Índice UV com indicador visual (clicável)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showUvDetails = true }
                            ) {
                                Text(
                                    text = "☀️",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(28.dp)
                                )
                                
                                Column {
                                    Text(
                                        text = "Índice UV",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val uvColor = when {
                                            day.uv < 3 -> Color(0xFF4CAF50) // Verde - Baixo
                                            day.uv < 6 -> Color(0xFFFFD600) // Amarelo mais vibrante - Moderado
                                            day.uv < 8 -> Color(0xFFFF9800) // Laranja - Alto
                                            day.uv < 11 -> Color(0xFFF44336) // Vermelho - Muito Alto
                                            else -> Color(0xFF9C27B0) // Roxo - Extremo
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .width(38.dp)
                                                .height(18.dp)
                                                .background(
                                                    color = uvColor.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "UV ${day.uv.toInt()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = uvColor
                                            )
                                        }
                                        
                                        Text(
                                            text = uvIndexToDescription(day.uv),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = uvColor
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Coluna direita (dados estimados com base na previsão do dia)
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Umidade estimada (já que não temos esse dado na API por dia)
                            DetailItem(
                                icon = "💧",
                                label = "Umidade (Est.)",
                                value = "75%"
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Velocidade do vento estimada
                            DetailItem(
                                icon = "🌬️",
                                label = "Vento (Est.)",
                                value = "12 km/h"
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Qualidade do ar estimada
                            DetailItem(
                                icon = "🌫️",
                                label = "Qualidade do Ar",
                                value = "Boa"
                            )
                        }
                    }
                    
                    // Dica para o usuário
                    Text(
                        text = "Toque no card para recolher",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Mostrar o diálogo de informações UV quando o usuário clica no valor UV
    if (showUvDetails) {
        PopupInfoDialog(
            title = "Índice UV: ${getUvIndexLevel(day.uv)}",
            valueCircle = day.uv.toString(),
            circleColor = when {
                day.uv < 3 -> Color(0xFF4CAF50) // Verde - Baixo
                day.uv < 6 -> Color(0xFFFFD600) // Amarelo mais vibrante - Moderado
                day.uv < 8 -> Color(0xFFFF9800) // Laranja - Alto
                day.uv < 11 -> Color(0xFFF44336) // Vermelho - Muito Alto
                else -> Color(0xFF9C27B0) // Roxo - Extremo
            },
            content = getUvDetailedDescription(day.uv),
            onDismiss = { showUvDetails = false }
        )
    }
}

/**
 * Item de detalhe para informações meteorológicas na visualização expandida.
 * 
 * @param icon Emoji ou texto a ser usado como ícone
 * @param label Rótulo descritivo para o item
 * @param value Valor a ser exibido
 */
@Composable
private fun DetailItem(
    icon: String,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(28.dp)
        )
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Diálogo de informações sobre o índice UV.
 * Exibe detalhes sobre o nível de risco UV e recomendações de proteção.
 * 
 * @param uvIndex Valor do índice UV a ser analisado
 * @param onDismiss Callback chamado quando o usuário fecha o diálogo
 */
@Composable
fun UvInfoDialog(
    uvIndex: Double,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Determina a cor de fundo baseada no nível UV
        val startColor = when {
            uvIndex < 3 -> Color(0xFF4CAF50) // Verde - Baixo
            uvIndex < 6 -> Color(0xFFFFC107) // Amarelo - Moderado
            uvIndex < 8 -> Color(0xFFFF9800) // Laranja - Alto
            uvIndex < 11 -> Color(0xFFF44336) // Vermelho - Muito Alto
            else -> Color(0xFF9C27B0) // Roxo - Extremo
        }
        
        val endColor = when {
            uvIndex < 3 -> Color(0xFF81C784) // Verde mais claro
            uvIndex < 6 -> Color(0xFFFFD54F) // Amarelo mais claro
            uvIndex < 8 -> Color(0xFFFFB74D) // Laranja mais claro
            uvIndex < 11 -> Color(0xFFE57373) // Vermelho mais claro
            else -> Color(0xFFCE93D8) // Roxo mais claro
        }
        
        Card(
            modifier = Modifier
                .padding(16.dp)
                .width(320.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                startColor.copy(alpha = 0.9f),
                                endColor.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Círculo com valor UV
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Text(
                                text = "${uvIndex.toInt()}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = startColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nível de risco
                    Text(
                        text = "Índice UV: ${uvIndexToDescription(uvIndex)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Linha decorativa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.7f))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recomendações
                    Text(
                        text = getUvDetailedDescription(uvIndex),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botão de fechar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onDismiss() }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Fechar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exibe uma lista de cidades favoritas e a localização atual do GPS (quando disponível).
 * Permite ao usuário selecionar uma cidade para visualizar suas informações meteorológicas.
 * 
 * @param cities Lista de cidades favoritas a serem exibidas
 * @param onCityClick Callback chamado quando uma cidade é selecionada
 * @param currentCity Nome da cidade atualmente selecionada (para destacá-la)
 * @param gpsLocationCity Dados da cidade atual baseada em GPS (quando disponível)
 */
@Composable
fun FavoriteCitiesList(
    cities: List<FavoriteCity>,
    onCityClick: (String) -> Unit,
    currentCity: String? = null,
    gpsLocationCity: CurrentWeatherUiState? = null
) {
    // Card com visual modernizado usando gradientes e sombras
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 6.dp, 
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) // Sombra mais colorida
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp // Maior elevação
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // Transparente para mostrar o gradiente
        )
    ) {
        // Fundo com degradê mais vibrante para efeito visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3949AB), // Azul mais intenso
                            Color(0xFF303F9F),
                            Color(0xFF283593),
                            Color(0xFF1A237E)  // Azul profundo
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Título com fundo de vidro (glassmorphism)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ícone com efeito de brilho
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.3f),
                                            Color.White.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Minhas Cidades",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Text(
                                text = "Locais salvos para consulta rápida",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Linha decorativa com efeito de brilho
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.7f),
                                    Color.White,
                                    Color.White.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mostrar a cidade do GPS no topo sempre que disponível
                if (gpsLocationCity != null) {
                    // Card de vidro para cidade GPS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onCityClick(gpsLocationCity.cityName) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ícone com efeito de brilho
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF4FC3F7).copy(alpha = 0.5f),
                                                Color(0xFF03A9F4).copy(alpha = 0.2f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_location),
                                    contentDescription = "Localização atual",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = gpsLocationCity.cityName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${gpsLocationCity.region}, ${gpsLocationCity.country}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            if (currentCity == gpsLocationCity.cityName) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF64B5F6),
                                                    Color(0xFF2196F3)
                                                )
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Atual",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    if (cities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Separador com efeito de brilho
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.3f),
                                            Color.White.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                if (cities.isEmpty() && gpsLocationCity == null) {
                    // Estado vazio com efeito de vidro e luz
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Ícone com efeito de brilho
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Nenhuma cidade favorita adicionada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Toque no ícone de coração para adicionar à sua lista",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    // Lista de cidades favoritas
                    cities.forEachIndexed { index, city ->
                        // Não mostrar a cidade novamente se ela já foi exibida como cidade do GPS
                        if (gpsLocationCity != null && city.cityName == gpsLocationCity.cityName) {
                            return@forEachIndexed
                        }
                        
                        val isCurrentCity = currentCity == city.cityName
                        
                        // Card de cidade com efeito vidro
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    color = if (isCurrentCity) 
                                        Color.White.copy(alpha = 0.2f) 
                                    else 
                                        Color.White.copy(alpha = 0.07f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onCityClick(city.cityName) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Ícone com efeito coração
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFE57373).copy(alpha = 0.7f),
                                                    Color(0xFFEF5350).copy(alpha = 0.3f)
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = city.cityName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${city.region}, ${city.country}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                
                                if (isCurrentCity) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF64B5F6),
                                                        Color(0xFF2196F3)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Atual",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (index < cities.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// Funções utilitárias

/**
 * Converte o índice de qualidade do ar (AQI) para texto descritivo.
 * 
 * @param aqi Valor numérico do índice de qualidade do ar
 * @return Texto descritivo correspondente ao nível de qualidade do ar
 */
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

/**
 * Converte o índice de qualidade do ar (AQI) para texto abreviado.
 * Útil para espaços limitados na interface.
 * 
 * @param aqi Valor numérico do índice de qualidade do ar
 * @return Texto abreviado correspondente ao nível de qualidade do ar
 */
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

/**
 * Determina a cor correspondente ao índice de qualidade do ar.
 * Usado para representação visual do nível de qualidade.
 * 
 * @param aqi Valor numérico do índice de qualidade do ar
 * @return Objeto Color representando a cor associada ao nível de qualidade
 */
private fun aqiToColor(aqi: Int?): Color {
    return when (aqi) {
        1 -> Color.Green
        2 -> Color.Yellow
        3 -> Color.Magenta
        4 -> Color.Red
        5 -> Color(0xFF8B0000)  // Vermelho escuro
        6 -> Color(0xFF800080)  // Roxo
        else -> Color.Gray
    }
}

/**
 * Formata uma data para exibição simplificada (mês e dia).
 * 
 * @param date String contendo a data no formato "yyyy-MM-dd"
 * @return String formatada no padrão "dia de mês" (ex: "15 de Agosto")
 */
private fun formatDate(date: String): String {
    // Simplificado para exemplo, uma implementação real usaria DateFormat
    return date.substring(5)  // Retorna apenas MM-DD
}

/**
 * Traduz as condições climáticas do inglês para o português.
 * 
 * @param condition String contendo a descrição da condição em inglês
 * @return String com a tradução da condição para português
 */
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
        normalizedCondition.contains("patchy rain nearby") || normalizedCondition.contains("patchy rain possible") -> "Chuva isolada"
        normalizedCondition.contains("patchy snow possible") -> "Neve isolada"
        normalizedCondition.contains("patchy sleet possible") -> "Granizo isolado"
        normalizedCondition.contains("patchy freezing drizzle possible") -> "Garoa congelante"
        normalizedCondition.contains("thundery outbreaks possible") -> "Possível trovoada"
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

/**
 * Interpreta o índice UV e retorna uma descrição de nível de risco.
 * 
 * @param uvIndex Valor numérico do índice UV
 * @return String contendo a descrição do nível de risco (Baixo, Moderado, Alto, etc.)
 */
private fun uvIndexToDescription(uvIndex: Double): String {
    return when {
        uvIndex < 3 -> "Baixo"
        uvIndex < 6 -> "Moderado"
        uvIndex < 8 -> "Alto"
        uvIndex < 11 -> "Muito Alto"
        else -> "Extremo"
    }
}

/**
 * Fornece recomendações detalhadas de proteção baseadas no índice UV.
 * 
 * @param uvIndex Valor numérico do índice UV
 * @return String contendo recomendações específicas de proteção solar
 */
private fun getUvDetailedDescription(uvIndex: Double): String {
    return when {
        uvIndex < 3 -> "Baixo risco. Proteção mínima necessária para atividades ao ar livre."
        uvIndex < 6 -> "Risco moderado. Use protetor solar, chapéu e óculos de sol."
        uvIndex < 8 -> "Risco alto. Reduza a exposição entre 10h e 16h. Use protetor solar FPS 30+."
        uvIndex < 11 -> "Risco muito alto. Evite exposição entre 10h e 16h. Use proteção máxima."
        else -> "Risco extremo. Evite qualquer exposição ao sol. Proteção total necessária."
    }
}

/**
 * Determina a cor correspondente ao índice UV para representação visual.
 * 
 * @param label String descritiva do tipo de informação (utilizada para verificar se é UV)
 * @param value String contendo o valor ou descrição a ser analisado
 * @return Objeto Color representando a cor associada ao nível de UV
 */
private fun getUvIndexColor(label: String, value: String): Color {
    if (label != "UV") return Color.Gray
    
    return try {
        val uvValue = value.toDoubleOrNull() ?: 0.0
        when {
            uvValue < 3 -> Color(0xFF4CAF50) // Verde - Baixo
            uvValue < 6 -> Color(0xFFFFD600) // Amarelo mais vibrante - Moderado
            uvValue < 8 -> Color(0xFFFF9800) // Laranja - Alto
            uvValue < 11 -> Color(0xFFF44336) // Vermelho - Muito Alto
            else -> Color(0xFF9C27B0) // Roxo - Extremo
        }
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * Converte o texto descritivo da qualidade do ar para o valor numérico do índice.
 * 
 * @param aqiText Texto descritivo da qualidade do ar
 * @return Valor numérico do índice de qualidade do ar (1-6)
 */
private fun aqiFromText(aqiText: String): Int {
    return when (aqiText) {
        "Bom" -> 1
        "Moderado" -> 2
        "Insalubre para sensíveis" -> 3
        "Insalubre" -> 4
        "Muito insalubre" -> 5
        "Perigoso" -> 6
        else -> 0
    }
}

/**
 * Fornece informações sobre impactos à saúde baseados no índice de qualidade do ar.
 * 
 * @param aqi Valor numérico do índice de qualidade do ar
 * @return String descrevendo os possíveis impactos à saúde
 */
private fun getAqiHealthImpact(aqi: Int): String {
    return when (aqi) {
        1 -> "Nenhum ou mínimo impacto na saúde. Qualidade do ar considerada satisfatória com risco mínimo ou nenhum para a população em geral."
        2 -> "Pessoas muito sensíveis podem apresentar sintomas leves. Qualidade do ar aceitável, mas pode haver preocupação moderada para um número muito pequeno de indivíduos."
        3 -> "Membros de grupos sensíveis (pessoas com doenças respiratórias ou cardíacas, idosos e crianças) podem experimentar efeitos na saúde."
        4 -> "Toda a população pode começar a sentir efeitos na saúde. Membros de grupos sensíveis podem experimentar efeitos mais sérios."
        5 -> "Alerta de saúde: todos podem experimentar efeitos mais sérios na saúde. Risco significativo de reações adversas para toda a população."
        6 -> "Alerta de saúde de emergência: toda a população tem maior risco de experimentar efeitos graves na saúde. Condições consideradas perigosas."
        else -> "Informação indisponível sobre impactos à saúde."
    }
}

/**
 * Fornece recomendações baseadas no índice de qualidade do ar.
 * 
 * @param aqi Valor numérico do índice de qualidade do ar
 * @return String com recomendações de proteção
 */
private fun getAqiRecommendation(aqi: Int): String {
    return when (aqi) {
        1 -> "Pode-se realizar atividades ao ar livre normalmente."
        2 -> "Pessoas extremamente sensíveis devem considerar limitar esforços prolongados ao ar livre."
        3 -> "Pessoas com problemas respiratórios devem limitar atividades ao ar livre. Crianças e idosos devem moderar esforços prolongados."
        4 -> "Pessoas sensíveis devem evitar atividades ao ar livre. Todos os outros devem limitar atividades prolongadas ao ar livre."
        5 -> "Todos devem evitar atividades ao ar livre. Grupos sensíveis devem permanecer em ambientes fechados com ar filtrado."
        6 -> "Todos devem evitar qualquer atividade ao ar livre. Se possível, permaneça em ambientes fechados com ar filtrado e janelas fechadas."
        else -> "Recomendações indisponíveis."
    }
}

/**
 * Fornece informações sobre a composição típica do ar baseada no índice de qualidade.
 * Esta é uma aproximação simplificada para fins educativos.
 * 
 * @param aqi Valor numérico do índice de qualidade do ar
 * @return String descrevendo a composição típica do ar
 */
private fun getAirComposition(aqi: Int): String {
    return when (aqi) {
        1 -> "Baixa concentração de poluentes como PM2.5, PM10, ozônio (O₃), dióxido de nitrogênio (NO₂), dióxido de enxofre (SO₂) e monóxido de carbono (CO)."
        2 -> "Presença moderada de partículas finas (PM2.5, PM10) e outros poluentes como ozônio (O₃) e óxidos de nitrogênio (NOx)."
        3 -> "Níveis elevados de pelo menos um poluente, geralmente PM2.5, PM10 ou ozônio (O₃). Presença significativa de outros poluentes."
        4 -> "Concentrações altas de múltiplos poluentes, especialmente material particulado (PM2.5, PM10) e gases como dióxido de nitrogênio (NO₂) e ozônio (O₃)."
        5 -> "Concentrações muito altas de material particulado e gases tóxicos. Possível presença de fumaça visível e neblina de poluição (smog)."
        6 -> "Concentrações extremamente altas de material particulado e gases tóxicos. Visibilidade significativamente reduzida devido à poluição severa."
        else -> "Composição de ar indisponível."
    }
}

/**
 * Componente de diálogo pop-up com fundo translúcido para mostrar informações detalhadas.
 * 
 * @param title Título do diálogo
 * @param valueCircle Valor a ser exibido no círculo central (índice UV ou AQI)
 * @param circleColor Cor do círculo baseada no valor
 * @param content Conteúdo textual a ser exibido no diálogo
 * @param onDismiss Callback chamado quando o usuário fecha o diálogo
 */
@Composable
fun PopupInfoDialog(
    title: String,
    valueCircle: String,
    circleColor: Color,
    content: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Círculo com valor
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = circleColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = circleColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = valueCircle,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = circleColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Linha divisória
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Conteúdo
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Botão de fechar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = circleColor.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Fechar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Retorna o nível descritivo do índice UV como texto.
 */
private fun getUvIndexLevel(uvIndex: Double): String {
    return when {
        uvIndex < 3 -> "Baixo"
        uvIndex < 6 -> "Moderado"
        uvIndex < 8 -> "Alto"
        uvIndex < 11 -> "Muito Alto"
        else -> "Extremo"
    }
}