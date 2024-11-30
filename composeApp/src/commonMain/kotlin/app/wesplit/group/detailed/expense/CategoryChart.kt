package app.wesplit.group.detailed.expense

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.expense.Category
import app.wesplit.domain.model.expense.Expense
import app.wesplit.expense.category.uiTitle
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.Done
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowDown
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowUp
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.generateHueColorPalette
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.other

private const val MAX_CATS = 4

private const val FILTER_CLICK_EVENT = "filter_click"
private const val FILTER_CLICK_PARAM = "filter"

@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalLayoutApi::class)
@Composable
fun PieSampleView(
    expenses: List<Expense>,
    selectedCategories: List<Category>,
    onCategoryToggle: (Category, Boolean) -> Unit,
) {
    val analyticsManager: AnalyticsManager = koinInject()

    var otherCollapsed by remember(expenses) { mutableStateOf(true) }
    val maxCats by remember(otherCollapsed) {
        derivedStateOf {
            if (otherCollapsed) MAX_CATS else expenses.size
        }
    }

    val data =
        remember(expenses, selectedCategories) {
            // Group and sum the expenses by category
            val groupedExpenses =
                expenses
                    .filterNot { it.category == Category.Magic }
                    .groupBy { it.category }
                    .mapValues { (_, groupedExpenses) ->
                        groupedExpenses.sumOf { it.baseAmount.value }
                    }

            // Combine categories from both the expenses and selectedCategories
            val allCategories = (groupedExpenses.keys + selectedCategories).toSet()

            // Create a complete data map ensuring every category has a value
            val completeData =
                allCategories.associateWith { category ->
                    groupedExpenses[category] ?: 0.01 // Use existing value if present, otherwise 0
                }

            // Convert to a list and sort by value in descending order
            completeData
                .toList()
                .sortedByDescending { it.second }
        }

    val uiData =
        remember(data, maxCats) {
            val topCategories =
                data.take(maxCats) // Take the first MAX_CATS categories
                    .map { (category, value) ->
                        UiCategory(category = category, value = value)
                    }

            val otherValue =
                data.drop(maxCats) // Sum the remaining categories
                    .sumOf { it.second }

            if (otherValue > 0 || topCategories.size > MAX_CATS) {
                topCategories.take(MAX_CATS) +
                    UiCategory(category = null, value = otherValue) +
                    topCategories.drop(MAX_CATS)
            } else {
                topCategories
            }
        }

    val colors =
        remember(uiData) {
            if (uiData.isNotEmpty()) {
                generateHueColorPalette(uiData.size, 0.65f, 0.5f)
            } else {
                listOf(Color.Red)
            }
        }

    Row(
        modifier = Modifier.fillMaxWidth(1f).wrapContentHeight().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChartLayout(
            modifier = Modifier.width(100.dp),
        ) {
            PieChart(
                values = uiData.map { it.value.toFloat() },
                modifier = Modifier,
                slice = { i: Int ->
                    DefaultSlice(
                        color = if (i < colors.size) colors[i] else Color.Cyan,
                    )
                },
                holeSize = 0.65f,
                maxPieDiameter = 100.dp,
                labelConnector = {},
                pieAnimationSpec = snap(),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            uiData.mapIndexed { index, uiCategory ->
                if (uiCategory.category != null) {
                    val selected = uiCategory.category in selectedCategories
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                analyticsManager.track(
                                    FILTER_CLICK_EVENT,
                                    mapOf(FILTER_CLICK_PARAM to ("CATEGORY")),
                                )
                            }
                            onCategoryToggle(uiCategory.category, !selected)
                        },
                        leadingIcon =
                            if (selected) {
                                {
                                    Icon(
                                        modifier = Modifier.size(12.dp),
                                        imageVector = AdaptiveIcons.Outlined.Done,
                                        contentDescription = uiCategory.category.uiTitle(),
                                    )
                                }
                            } else {
                                null
                            },
                        label = {
                            Text(uiCategory.category.uiTitle())
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = colors[index],
                                selectedContainerColor = colors[index],
                                iconColor = if (colors[index].luminance() > 0.5) Color.Black else Color.White,
                                labelColor = if (colors[index].luminance() > 0.5) Color.Black else Color.White,
                                selectedLabelColor = if (colors[index].luminance() > 0.5) Color.Black else Color.White,
                                selectedLeadingIconColor = if (colors[index].luminance() > 0.5) Color.Black else Color.White,
                            ),
                    )
                } else {
                    FilterChip(
                        selected = otherCollapsed,
                        onClick = {
                            if (otherCollapsed) {
                                analyticsManager.track(
                                    FILTER_CLICK_EVENT,
                                    mapOf(FILTER_CLICK_PARAM to ("OTHER")),
                                )
                            }
                            otherCollapsed = !otherCollapsed
                        },
                        trailingIcon = {
                            Icon(
                                modifier = Modifier.size(12.dp),
                                imageVector =
                                    if (otherCollapsed) {
                                        AdaptiveIcons.Outlined.KeyboardArrowDown
                                    } else {
                                        AdaptiveIcons.Outlined.KeyboardArrowUp
                                    },
                                contentDescription = uiCategory.category?.uiTitle() ?: stringResource(Res.string.other),
                            )
                        },
                        label = {
                            Text(stringResource(Res.string.other))
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors[index],
                                selectedLabelColor = if (colors[index].luminance() > 0.5) Color.Black else Color.White,
                                selectedLeadingIconColor = if (colors[index].luminance() > 0.5) Color.Black else Color.White,
                            ),
                    )
                }
            }
        }
    }
}

private data class UiCategory(
    val category: Category?,
    val value: Double,
)
