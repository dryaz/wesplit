package app.wesplit.group.detailed.expense

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.wesplit.domain.model.expense.Category
import app.wesplit.domain.model.expense.Expense
import app.wesplit.expense.category.uiTitle
import app.wesplit.theme.extraColorScheme
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.generateHueColorPalette
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.other

private const val MAX_CATS = 4

@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalLayoutApi::class)
@Composable
fun PieSampleView(expenses: List<Expense>) {
    val data =
        remember(expenses) {
            expenses
                .filterNot { it.category == Category.Magic }
                .groupBy { it.category } // Group expenses by category
                .mapValues { (_, groupedExpenses) ->
                    groupedExpenses.sumOf { it.baseAmount.value } // Sum the amount for each category
                }
                .toList()
                .sortedByDescending { it.second } // Sort by the total sum (value)
        }

    val uiData =
        remember(data) {
            val topCategories =
                data.take(MAX_CATS) // Take the first MAX_CATS categories
                    .map { (category, value) ->
                        UiCategory(category = category, value = value)
                    }

            val otherValue =
                data.drop(MAX_CATS) // Sum the remaining categories
                    .sumOf { it.second }

            if (otherValue > 0) {
                topCategories + UiCategory(category = null, value = otherValue)
            } else {
                topCategories
            }
        }

    val colors =
        remember(data) {
            if (data.isNotEmpty()) {
                generateHueColorPalette(uiData.size)
            } else {
                listOf(Color.Red)
            }
        }

    LaunchedEffect(uiData) {
        uiData.forEach {
            println(
                "${it.category?.name} : ${it.value}",
            )
        }
    }

    AnimatedVisibility(
        visible = uiData.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(1f).height(132.dp).padding(horizontal = 16.dp),
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
                            color = if (i < colors.size - 1) colors[i] else Color.Red,
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
                modifier = Modifier.weight(1f).fillMaxHeight(0.95f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                uiData.mapIndexed { index, uiCategory ->
                    Text(
                        modifier =
                            Modifier.clip(RoundedCornerShape(20.dp)).background(colors[index])
                                .padding(horizontal = 8.dp, vertical = 1.dp),
                        text = uiCategory.category?.uiTitle() ?: stringResource(Res.string.other),
                        color = MaterialTheme.extraColorScheme.onInfoContainer,
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
