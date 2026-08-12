package com.bsp.wsiw.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.bsp.wsiw.core.ui.R
import com.bsp.wsiw.core.ui.theme.AppTheme
import java.util.Calendar

data class SortOption(val label: String, val apiValue: String)

data class FilterSheetState(
    val sortOptions: List<SortOption>,
    val selectedSortOption: SortOption,
    val minRating: Float?,
    val year: Int?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    state: FilterSheetState,
    onApply: (FilterSheetState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = AppTheme.spacing

    var selectedSortOption by remember { mutableStateOf(state.selectedSortOption) }
    var minRating by remember { mutableFloatStateOf(state.minRating ?: 0f) }
    var year by remember { mutableStateOf(state.year) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = listOf(null) + (currentYear downTo 1970).toList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xl)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(spacing.xl))

            Text(
                text = stringResource(R.string.filter_sort_by),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                state.sortOptions.forEach { option ->
                    FilterChip(
                        selected = selectedSortOption == option,
                        onClick = { selectedSortOption = option },
                        label = { Text(option.label) },
                    )
                }
            }

            Spacer(Modifier.height(spacing.xl))

            val ratingLabel = if (minRating > 0f) {
                stringResource(R.string.filter_min_rating_value, minRating)
            } else {
                stringResource(R.string.filter_min_rating_any)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.filter_min_rating),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = ratingLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = minRating,
                onValueChange = { minRating = (it * 10).toInt() / 10f },
                valueRange = 0f..9f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(spacing.lg))

            Text(
                text = stringResource(R.string.filter_year),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.sm))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                items(years.size) { index ->
                    val y = years[index]
                    FilterChip(
                        selected = year == y,
                        onClick = { year = y },
                        label = { Text(y?.toString() ?: stringResource(R.string.filter_year_any)) },
                    )
                }
            }

            Spacer(Modifier.height(spacing.xxl))

            Button(
                onClick = {
                    onApply(
                        FilterSheetState(
                            sortOptions = state.sortOptions,
                            selectedSortOption = selectedSortOption,
                            minRating = if (minRating > 0f) minRating else null,
                            year = year,
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.filter_apply))
            }
            Spacer(Modifier.height(spacing.sm))
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.filter_reset))
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
