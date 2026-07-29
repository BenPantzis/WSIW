package com.bsp.wsiw.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.ui.component.ErrorContent
import com.bsp.wsiw.core.ui.component.RemoteImage
import com.bsp.wsiw.core.ui.component.shimmerEffect
import com.bsp.wsiw.core.ui.theme.AppTheme
import com.bsp.wsiw.core.ui.util.formatTmdbDate

private val Gold = Color(0xFFE8A020)

@Composable
fun ReviewsScreen(
    movieId: Int,
    movieTitle: String,
    onBack: () -> Unit,
    viewModel: ReviewsViewModel = hiltViewModel<ReviewsViewModel, ReviewsViewModel.Factory>(
        creationCallback = { it.create(movieId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewsContent(
        movieTitle = movieTitle,
        uiState = uiState,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
private fun ReviewsContent(
    movieTitle: String,
    uiState: ReviewsUiState,
    onAction: (ReviewsAction) -> Unit,
    onBack: () -> Unit,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            uiState.canLoadMore && total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) onAction(ReviewsAction.LoadNextPage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ReviewsTopBar(title = movieTitle, onBack = onBack)

        when {
            uiState.isLoading -> ReviewsShimmer()
            uiState.error != null && uiState.reviews.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorContent(message = uiState.error!!, onRetry = { onAction(ReviewsAction.Retry) })
            }
            else -> {
                SortChipRow(
                    selected = uiState.sort,
                    onSelect = { onAction(ReviewsAction.SelectSort(it)) },
                )
                val sorted = uiState.sorted
                if (sorted.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.reviews_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = AppTheme.spacing.md),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(sorted, key = { _, r -> r.id }) { index, review ->
                            ReviewCard(review = review)
                            if (index < sorted.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                )
                            }
                        }
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .height(80.dp)
                                        .shimmerEffect(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Transparent, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Column {
            Text(
                text = stringResource(R.string.reviews_screen_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SortChipRow(selected: ReviewSort, onSelect: (ReviewSort) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ReviewSort.entries) { sort ->
            FilterChip(
                selected = sort == selected,
                onClick = { onSelect(sort) },
                label = { Text(stringResource(sort.labelRes)) },
            )
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    val spacing = AppTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (review.avatarUrl != null) {
                RemoteImage(
                    url = review.avatarUrl,
                    contentDescription = review.author,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = review.author.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = formatTmdbDate(review.createdAt.take(10)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (review.rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "★", color = Gold, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${review.rating}/10",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        Text(
            text = review.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        )
    }
}

@Composable
private fun ReviewsShimmer() {
    val spacing = AppTheme.spacing
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = spacing.md)) {
        repeat(4) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).shimmerEffect())
                Spacer(Modifier.width(spacing.md))
                Column {
                    Box(modifier = Modifier.width(120.dp).height(14.dp).shimmerEffect())
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.width(80.dp).height(11.dp).shimmerEffect())
                }
            }
            Spacer(Modifier.height(spacing.md))
            repeat(4) {
                Box(modifier = Modifier.fillMaxWidth().height(13.dp).shimmerEffect())
                Spacer(Modifier.height(5.dp))
            }
            Spacer(Modifier.height(spacing.xl))
        }
    }
}
