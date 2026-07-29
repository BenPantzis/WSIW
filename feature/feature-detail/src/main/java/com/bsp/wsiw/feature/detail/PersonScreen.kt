package com.bsp.wsiw.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.domain.model.Movie
import com.bsp.wsiw.core.domain.model.PersonDetail
import com.bsp.wsiw.core.ui.UiText
import com.bsp.wsiw.core.ui.component.ErrorContent
import com.bsp.wsiw.core.ui.component.RemoteImage
import com.bsp.wsiw.core.ui.component.shimmerEffect
import com.bsp.wsiw.core.ui.theme.AppTheme
import com.bsp.wsiw.core.ui.util.formatTmdbDate

private val ProfileHeight = 320.dp
private val Gold = Color(0xFFE8A020)

@Composable
fun PersonScreen(
    personId: Int,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: PersonViewModel = hiltViewModel<PersonViewModel, PersonViewModel.Factory>(
        creationCallback = { it.create(personId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PersonContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBack = onBack,
        onMovieClick = onMovieClick,
    )
}

@Composable
private fun PersonContent(
    uiState: PersonUiState,
    onAction: (PersonAction) -> Unit,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            uiState.isLoading -> PersonLoadingContent()
            uiState.error != null -> ErrorContent(
                message = uiState.error!!.asString(),
                onRetry = { onAction(PersonAction.Retry) },
                modifier = Modifier.align(Alignment.Center),
            )
            uiState.person != null -> PersonDetailContent(
                person = uiState.person!!,
                onMovieClick = onMovieClick,
            )
        }
        // Back button always visible
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x99000000), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun PersonDetailContent(person: PersonDetail, onMovieClick: (Int) -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        // Profile photo header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProfileHeight),
        ) {
            if (person.profileUrl != null) {
                RemoteImage(
                    url = person.profileUrl,
                    contentDescription = person.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = person.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // Gradient fade into background at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.5f to Color.Transparent,
                                1.0f to MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val spacing = AppTheme.spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = if (person.filmography.isEmpty()) 48.dp else 0.dp),
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = person.knownForDepartment,
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold,
                )

                if (person.birthday != null || person.placeOfBirth != null) {
                    Spacer(Modifier.height(spacing.lg))
                    PersonInfoRow(person)
                }

                if (person.biography.isNotBlank()) {
                    Spacer(Modifier.height(spacing.xl))
                    Text(
                        text = stringResource(R.string.person_section_biography),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = person.biography,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    )
                }

                if (person.filmography.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.xl))
                    Text(
                        text = stringResource(R.string.person_section_known_for),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(spacing.sm))
                }
            }
        }

        if (person.filmography.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
                modifier = Modifier.padding(bottom = 48.dp),
            ) {
                items(person.filmography, key = { it.id }) { movie ->
                    FilmographyCard(movie = movie, onClick = { onMovieClick(movie.id) })
                }
            }
        }
    }
}

@Composable
private fun PersonInfoRow(person: PersonDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val birthday = person.birthday
        val placeOfBirth = person.placeOfBirth
        if (birthday != null) InfoLine(label = stringResource(R.string.person_info_born), value = formatTmdbDate(birthday))
        if (placeOfBirth != null) InfoLine(label = stringResource(R.string.person_info_from), value = placeOfBirth)
    }
}


@Composable
private fun InfoLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun FilmographyCard(movie: Movie, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .aspectRatio(2f / 3f)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(Modifier.clickable(onClick = onClick)),
    ) {
        RemoteImage(
            url = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.5f to Color.Transparent,
                            1.0f to Color(0xCC0D0D0D),
                        ),
                    ),
                ),
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
        )
    }
}

@Composable
private fun PersonLoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 64.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProfileHeight)
                .shimmerEffect(),
        )
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.55f).height(28.dp).shimmerEffect())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.3f).height(16.dp).shimmerEffect())
            Spacer(Modifier.height(24.dp))
            repeat(6) {
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).shimmerEffect())
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}
