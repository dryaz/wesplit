package app.wesplit.expense.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.expense.Category
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveCircularProgressIndicator
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowDown
import io.github.alexzhirkevich.cupertino.adaptive.icons.KeyboardArrowRight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.collapsed
import split.composeapp.generated.resources.expanded
import split.composeapp.generated.resources.ic_plus

private const val CATEGORY_EXPAND_EVENT = "cat_expand"
private const val CATEGORY_COLLAPSE_EVENT = "cat_collapse"
private const val CATEGORY_SELECT_EVENT = "cat_select"

private const val CATEGORY_PARAM = "cat"

data class CategorySelection(
    val category: Category,
    val isUnlocked: Boolean,
)

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun ExpenseCategoryPicker(
    onDismiss: () -> Unit,
    onConfirm: (CategorySelection) -> Unit,
) {
    val accountRepository: AccountRepository = koinInject()
    val analyticsManager: AnalyticsManager = koinInject()

    val viewModel =
        viewModel {
            ExpenseCategoryPickerViewModel(
                accountRepository,
            )
        }

    val state = viewModel.state.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier.padding(16.dp).widthIn(max = 300.dp).heightIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            when (val uiState = state.value) {
                ExpenseCategoryPickerViewModel.State.Loading -> AdaptiveCircularProgressIndicator()
                is ExpenseCategoryPickerViewModel.State.Data ->
                    CategoryList(
                        data = uiState,
                        onConfirm = { cat ->
                            analyticsManager.track(
                                CATEGORY_SELECT_EVENT,
                                mapOf(
                                    CATEGORY_PARAM to cat.category.name,
                                ),
                            )
                            onConfirm(cat)
                            onDismiss()
                        },
                        analyticsManager = analyticsManager,
                    )
            }
        }
    }
}

@Composable
private fun CategoryList(
    data: ExpenseCategoryPickerViewModel.State.Data,
    analyticsManager: AnalyticsManager,
    onConfirm: (CategorySelection) -> Unit,
) {
    val expanded = mutableStateOf<Set<Category>>(emptySet())

    LazyColumn(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        item(key = Category.None.name) {
            ListItem(
                modifier = Modifier.clickable { onConfirm(CategorySelection(Category.None, true)) },
                colors =
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                headlineContent = { Category.None.Title() },
                leadingContent = { Category.None.Icon() },
            )
        }

        data.categories.forEach { entry ->
            val isExpanded = entry.key in expanded.value
            item(key = entry.key.name) {
                ListItem(
                    modifier =
                        Modifier.clickable {
                            if (isExpanded) {
                                analyticsManager.track(
                                    CATEGORY_COLLAPSE_EVENT,
                                    mapOf(
                                        CATEGORY_PARAM to entry.key.name,
                                    ),
                                )
                                expanded.value = expanded.value - entry.key
                            } else {
                                analyticsManager.track(
                                    CATEGORY_EXPAND_EVENT,
                                    mapOf(
                                        CATEGORY_PARAM to entry.key.name,
                                    ),
                                )
                                expanded.value = expanded.value + entry.key
                            }
                        },
                    colors =
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    headlineContent = { entry.key.Title() },
                    leadingContent = { entry.key.Icon() },
                    trailingContent = {
                        Icon(
                            imageVector =
                                if (isExpanded) {
                                    AdaptiveIcons.Outlined.KeyboardArrowDown
                                } else {
                                    AdaptiveIcons.Outlined.KeyboardArrowRight
                                },
                            contentDescription =
                                stringResource(
                                    if (isExpanded) {
                                        Res.string.expanded
                                    } else {
                                        Res.string.collapsed
                                    },
                                ),
                        )
                    },
                )
            }

            if (isExpanded) {
                items(entry.value, key = { "${entry.key.name}:${it.name}" }) { category ->
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                onConfirm(CategorySelection(category, category in data.unlocked))
                            },
                        colors =
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        headlineContent = { category.Title() },
                        leadingContent = { category.Icon() },
                        trailingContent = {
                            if (category !in data.unlocked) {
                                Image(
                                    modifier = Modifier.height(18.dp),
                                    painter = painterResource(Res.drawable.ic_plus),
                                    contentDescription = "Plus badge",
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
