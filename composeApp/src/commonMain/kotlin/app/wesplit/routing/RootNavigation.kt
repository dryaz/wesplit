package app.wesplit.routing

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import app.wesplit.DoublePaneNavigation
import app.wesplit.NavigationMenuItem
import app.wesplit.ShareData
import app.wesplit.ShareDelegate
import app.wesplit.ShortcutDelegate
import app.wesplit.account.ProfileAction
import app.wesplit.account.ProfileRoute
import app.wesplit.account.ProfileViewModel
import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.AppReviewManager
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.currency.CurrencyRepository
import app.wesplit.domain.model.expense.ExpenseRepository
import app.wesplit.domain.model.group.GroupRepository
import app.wesplit.expense.AddExpenseAction
import app.wesplit.expense.ExpenseDetailsScreen
import app.wesplit.expense.ExpenseDetailsViewModel
import app.wesplit.group.detailed.GroupInfoAction
import app.wesplit.group.detailed.GroupInfoScreen
import app.wesplit.group.detailed.GroupInfoViewModel
import app.wesplit.group.detailed.NoGroupScreen
import app.wesplit.group.list.GroupListAction
import app.wesplit.group.list.GroupListRoute
import app.wesplit.group.list.GroupListViewModel
import app.wesplit.group.settings.GroupSettingsAction
import app.wesplit.group.settings.GroupSettingsScreen
import app.wesplit.group.settings.GroupSettingsViewModel
import app.wesplit.paywall.PaywallAction
import app.wesplit.paywall.PaywallRoute
import com.motorro.keeplink.deeplink.deepLink
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.koin.compose.koinInject
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.groups
import split.composeapp.generated.resources.ic_group
import split.composeapp.generated.resources.ic_profile
import split.composeapp.generated.resources.profile

private const val SHARE_EVENT = "share"

private const val SCREEN_VIEW = "screen_view"
private const val SCREEN_NAME = "screen_name"
private const val SCREEN_CLASS = "screen_class"

private const val SUBS_EVENT = "paywall"
private const val SUBS_SOURCE = "source"

private const val PROFILE_PAYWALL_SOURCE = "profile"

sealed class PaneNavigation(
    val route: String,
) {
    open fun destination(): String = route
}

sealed class LeftPane(
    route: String,
) : PaneNavigation(route) {
    data object GroupList : LeftPane("groups")

    data object Profile : LeftPane("profile")
}

sealed class RightPane(
    route: String,
) : PaneNavigation(route) {
    data object Empty : RightPane("empty")

    data object Paywall : RightPane("paywall")

    data object Group : RightPane("group/{${Param.GROUP_ID.paramName}}?${Param.TOKEN.paramName}={${Param.TOKEN.paramName}}") {
        enum class Param(
            val paramName: String,
        ) {
            GROUP_ID("group_id"),
            TOKEN("token"),
        }

        fun destination(
            groupId: String,
            token: String? = null,
        ): String {
            val base = "group/$groupId"
            return if (token != null) {
                base + "?${Param.TOKEN.paramName}=$token"
            } else {
                base
            }
        }

        override fun destination(): String = throw IllegalArgumentException("Must use destination(groupId) instead")
    }

    data object NewGroup : RightPane("newGroup")

    data object GroupSettings : RightPane("group/{${Param.GROUP_ID.paramName}}/settings") {
        enum class Param(
            val paramName: String,
        ) {
            GROUP_ID("group_id"),
        }

        fun destination(groupId: String): String = "group/$groupId/settings"

        override fun destination(): String = throw IllegalArgumentException("Must use destination(groupId) instead")
    }

    data object ExpenseDetails : RightPane("group/{${Param.GROUP_ID.paramName}}/expense/{${Param.EXPENSE_ID.paramName}}") {
        enum class Param(
            val paramName: String,
        ) {
            GROUP_ID("group_id"),
            EXPENSE_ID("expense_id"),
        }

        fun destination(
            groupId: String,
            expenseId: String? = null,
        ): String = "group/$groupId/expense/$expenseId"

        override fun destination(): String = throw IllegalArgumentException("Must use destination(groupId, expenseId) instead")
    }
}

sealed class MenuItem : NavigationMenuItem {
    data object Group : MenuItem() {
        override val icon: DrawableResource
            get() = Res.drawable.ic_group
        override val title: StringResource
            get() = Res.string.groups
    }

    data object Profile : MenuItem() {
        override val icon: DrawableResource
            get() = Res.drawable.ic_profile
        override val title: StringResource
            get() = Res.string.profile
    }
}

@Composable
fun RootNavigation(
    firstPaneNavController: NavHostController,
    secondPaneNavController: NavHostController,
    selectedMenuItem: NavigationMenuItem,
    onSelectMenuItem: (NavigationMenuItem) -> Unit,
) {
    var secondNavControllerEmpty by remember { mutableStateOf(false) }
    val analyticsManager: AnalyticsManager = koinInject()
    val accountRepository: AccountRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val shareDelegate: ShareDelegate = koinInject()
    val clipboardManager = LocalClipboardManager.current

    val onSubscriptionRequest: (String) -> Unit =
        remember {
            {
                analyticsManager.track(
                    SUBS_EVENT,
                    mapOf(
                        SUBS_SOURCE to it,
                    ),
                )
                secondPaneNavController.navigate(
                    RightPane.Paywall.destination(),
                    navOptions = navOptions { launchSingleTop = true },
                )
            }
        }

    val menuItems =
        remember {
            mutableStateListOf(
                MenuItem.Profile,
                MenuItem.Group,
            )
        }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    fun trackScreen(
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        val param = mutableMapOf<String, String>()
        destination.arguments.forEach {
            param.put(it.key, it.value.toString())
        }
        arguments?.let { args ->
            param.putAll(args.keySet().mapNotNull { it }.filter { !it.isNullOrBlank() }.associateWith { args.getString(it) ?: "" })
        }
        val screenName = destination.route ?: destination.displayName
        param.put(SCREEN_NAME, screenName)
        param.put(SCREEN_CLASS, destination.route ?: destination.displayName)

        if (screenName != "empty") {
            analyticsManager.track(SCREEN_VIEW, param)
        }
    }

    LaunchedEffect(firstPaneNavController) {
        firstPaneNavController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?,
                ) {
                    trackScreen(destination, arguments)
                }
            },
        )
    }

    LaunchedEffect(secondPaneNavController) {
        secondPaneNavController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?,
                ) {
                    trackScreen(destination, arguments)
                    secondNavControllerEmpty = controller.previousBackStackEntry == null
                }
            },
        )
    }

    DoublePaneNavigation(
        secondNavhostEmpty = secondNavControllerEmpty,
        menuItems = menuItems,
        selectedMenuItem = selectedMenuItem,
        onMenuItemClick = { menuItem ->
            onSelectMenuItem(menuItem)
            when (menuItem) {
                is MenuItem.Group ->
                    firstPaneNavController.navigate(
                        LeftPane.GroupList.route,
                        navOptions =
                            navOptions {
                                launchSingleTop = true
                                popUpTo(
                                    LeftPane.GroupList.route,
                                    popUpToBuilder = { inclusive = true },
                                )
                            },
                    )

                is MenuItem.Profile ->
                    firstPaneNavController.navigate(
                        LeftPane.Profile.route,
                        navOptions =
                            navOptions {
                                launchSingleTop = true
                                popUpTo(
                                    LeftPane.GroupList.route,
                                    popUpToBuilder = { inclusive = false },
                                )
                            },
                    )
            }
            coroutineScope.launch { drawerState.close() }
        },
        firstNavhost = { modifier ->
            NavHost(
                modifier = modifier,
                navController = firstPaneNavController,
                startDestination = LeftPane.GroupList.route,
            ) {
                // TODO: App navigation could be one per navhost + action controlled by composable
                composable(route = LeftPane.Profile.route) {
                    val viewModel =
                        viewModel {
                            ProfileViewModel(
                                accountRepository = accountRepository,
                            )
                        }

                    ProfileRoute(
                        viewModel = viewModel,
                        onAction = { action ->
                            when (action) {
                                is ProfileAction.LoginWith -> accountRepository.login(action.login)
                                ProfileAction.Logout -> {
                                    accountRepository.logout()
                                    secondPaneNavController.navigate(
                                        RightPane.Empty.destination(),
                                        navOptions =
                                            navOptions {
                                                launchSingleTop = true
                                                popUpTo(
                                                    RightPane.Empty.route,
                                                    popUpToBuilder = { inclusive = true },
                                                )
                                            },
                                    )
                                }

                                ProfileAction.OpenMenu -> coroutineScope.launch { drawerState.open() }
                                ProfileAction.Paywall -> {
                                    onSubscriptionRequest(PROFILE_PAYWALL_SOURCE)
                                }
                            }
                        },
                    )
                }

                composable(route = LeftPane.GroupList.route) {
                    val callback: (GroupListAction) -> Unit =
                        remember {
                            { action ->
                                when (action) {
                                    is GroupListAction.Select ->
                                        secondPaneNavController.navigate(
                                            RightPane.Group.destination(action.group.id),
                                            navOptions =
                                                navOptions {
                                                    launchSingleTop = true
                                                    popUpTo(
                                                        RightPane.Empty.route,
                                                        popUpToBuilder = { inclusive = false },
                                                    )
                                                },
                                        )

                                    is GroupListAction.LoginWith -> {
                                        accountRepository.login(action.login)
                                    }

                                    GroupListAction.CreateNewGroup -> {
                                        secondPaneNavController.navigate(
                                            RightPane.NewGroup.destination(),
                                            navOptions =
                                                navOptions {
                                                    launchSingleTop = true
                                                    popUpTo(
                                                        RightPane.Empty.route,
                                                        popUpToBuilder = { inclusive = false },
                                                    )
                                                },
                                        )
                                    }

                                    GroupListAction.OpenMenu -> coroutineScope.launch { drawerState.open() }
                                }
                            }
                        }

                    val groupRepository: GroupRepository = koinInject()
                    val ioDispatcher: CoroutineDispatcher = koinInject()

                    val viewModel: GroupListViewModel =
                        viewModel {
                            GroupListViewModel(
                                accountRepository,
                                groupRepository,
                                ioDispatcher,
                                analyticsManager,
                            )
                        }

                    GroupListRoute(
                        viewModel = viewModel,
                        onAction = callback,
                    )
                }
            }
        },
        drawerState = drawerState,
        secondNavhost = { modifier ->
            NavHost(
                modifier = modifier,
                navController = secondPaneNavController,
                startDestination = RightPane.Empty.route,
            ) {
                composable(route = RightPane.Empty.route) {
                    NoGroupScreen()
                }

                composable(
                    route = RightPane.Paywall.route,
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it * 2 },
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it * 2 },
                        )
                    },
                ) {
                    PaywallRoute { action ->
                        when (action) {
                            PaywallAction.Back -> secondPaneNavController.popBackStack()
                        }
                    }
                }

                composable(
                    route = RightPane.Group.route,
                    arguments =
                        listOf(
                            navArgument(RightPane.Group.Param.GROUP_ID.paramName) {
                                type = NavType.StringType
                            },
                            navArgument(RightPane.Group.Param.TOKEN.paramName) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) {
                    val groupRepository: GroupRepository = koinInject()
                    val groupId =
                        checkNotNull(
                            it.arguments?.getString(
                                RightPane
                                    .Group
                                    .Param
                                    .GROUP_ID
                                    .paramName,
                            ),
                        )
                    val viewModel: GroupInfoViewModel =
                        viewModel(
                            key = "GroupInfoViewModel $groupId",
                        ) {
                            GroupInfoViewModel(
                                SavedStateHandle.createHandle(null, it.arguments),
                                groupRepository,
                                accountRepository,
                                analyticsManager,
                            )
                        }
                    GroupInfoScreen(
                        viewModel = viewModel,
                        shareDelegate = shareDelegate,
                    ) { action ->
                        when (action) {
                            GroupInfoAction.Back -> secondPaneNavController.navigateUp()
                            is GroupInfoAction.Share -> {
                                analyticsManager.track(SHARE_EVENT)
                                val detailsAction =
                                    DeeplinkAction.Group.Details(
                                        groupId = action.group.id,
                                        token = action.group.publicToken,
                                    )
                                val link = deepLink(detailsAction)
                                val groupDetailsUrl = DeeplinkBuilders.PROD.build(link)
                                if (shareDelegate.supportPlatformSharing()) {
                                    shareDelegate.share(ShareData.Link(groupDetailsUrl))
                                } else {
                                    clipboardManager.setText(
                                        annotatedString =
                                            buildAnnotatedString {
                                                append(text = groupDetailsUrl)
                                            },
                                    )
                                }
                            }

                            is GroupInfoAction.AddExpense -> {
                                secondPaneNavController.navigate(
                                    RightPane.ExpenseDetails.destination(action.group.id),
                                    navOptions =
                                        navOptions {
                                            launchSingleTop = true
                                        },
                                )
                            }

                            is GroupInfoAction.OpenExpenseDetails -> {
                                secondPaneNavController.navigate(
                                    RightPane.ExpenseDetails.destination(groupId, action.expense.id),
                                    navOptions =
                                        navOptions {
                                            launchSingleTop = true
                                        },
                                )
                            }

                            is GroupInfoAction.Edit -> {
                                secondPaneNavController.navigate(
                                    RightPane.GroupSettings.destination(action.group.id),
                                    navOptions =
                                        navOptions {
                                            launchSingleTop = true
                                        },
                                )
                            }

                            is GroupInfoAction.Invite -> TODO("We support only sharing of the group yet")
                        }
                    }
                }

                composable(
                    route = RightPane.NewGroup.route,
                ) {
                    val groupRepository: GroupRepository = koinInject()

                    val viewModel: GroupSettingsViewModel =
                        viewModel {
                            GroupSettingsViewModel(
                                SavedStateHandle.createHandle(null, null),
                                groupRepository,
                                accountRepository,
                                analyticsManager,
                            )
                        }

                    GroupSettingsScreen(viewModel = viewModel) { action ->
                        when (action) {
                            GroupSettingsAction.Back -> secondPaneNavController.navigateUp()
                            GroupSettingsAction.Home ->
                                secondPaneNavController.navigate(
                                    RightPane.Empty.destination(),
                                    navOptions =
                                        navOptions {
                                            launchSingleTop = true
                                            popUpTo(
                                                RightPane.Empty.route,
                                                popUpToBuilder = { inclusive = true },
                                            )
                                        },
                                )
                        }
                    }
                }

                composable(
                    route = RightPane.GroupSettings.route,
                    arguments =
                        listOf(
                            navArgument(RightPane.GroupSettings.Param.GROUP_ID.paramName) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) {
                    val groupRepository: GroupRepository = koinInject()

                    val groupId =
                        it.arguments?.getString(
                            RightPane
                                .GroupSettings
                                .Param
                                .GROUP_ID
                                .paramName,
                        )

                    val viewModel: GroupSettingsViewModel =
                        viewModel(key = "GroupSettingsViewModel $groupId") {
                            GroupSettingsViewModel(
                                SavedStateHandle.createHandle(null, it.arguments),
                                groupRepository,
                                accountRepository,
                                analyticsManager,
                            )
                        }

                    GroupSettingsScreen(viewModel = viewModel) { action ->
                        when (action) {
                            GroupSettingsAction.Back -> secondPaneNavController.navigateUp()
                            GroupSettingsAction.Home ->
                                secondPaneNavController.navigate(
                                    RightPane.Empty.destination(),
                                    navOptions =
                                        navOptions {
                                            launchSingleTop = true
                                            popUpTo(
                                                RightPane.Empty.route,
                                                popUpToBuilder = { inclusive = true },
                                            )
                                        },
                                )
                        }
                    }
                }

                composable(
                    route = RightPane.ExpenseDetails.route,
                    arguments =
                        listOf(
                            navArgument(RightPane.ExpenseDetails.Param.GROUP_ID.paramName) {
                                type = NavType.StringType
                            },
                            navArgument(RightPane.ExpenseDetails.Param.EXPENSE_ID.paramName) {
                                type = NavType.StringType
                                nullable = true
                            },
                        ),
                ) {
                    // TODO: Accorgin ti github koin starts to support navigation args in savedstate in VM, POC
                    val groupRepository: GroupRepository = koinInject()
                    val expenseRepository: ExpenseRepository = koinInject()
                    val shortcutDelegate: ShortcutDelegate = koinInject()
                    val currencyRepository: CurrencyRepository = koinInject()
                    val settings: Settings = koinInject()
                    val appReview: AppReviewManager = koinInject()

                    val groupId =
                        checkNotNull(
                            it.arguments?.getString(
                                RightPane
                                    .ExpenseDetails
                                    .Param
                                    .GROUP_ID
                                    .paramName,
                            ),
                        )

                    val expenseId =
                        it.arguments?.getString(
                            RightPane
                                .ExpenseDetails
                                .Param
                                .EXPENSE_ID
                                .paramName,
                        )

                    val viewModel: ExpenseDetailsViewModel =
                        viewModel(
                            // TODO: Provide arguments extension to probably check changes based on generic internals
                            key = "ExpenseDetailsViewModel ${groupId + expenseId}",
                        ) {
                            ExpenseDetailsViewModel(
                                SavedStateHandle.createHandle(null, it.arguments),
                                groupRepository,
                                expenseRepository,
                                currencyRepository,
                                analyticsManager,
                                shortcutDelegate,
                                settings,
                                appReview,
                                accountRepository,
                                onSubscriptionRequest,
                            )
                        }

                    ExpenseDetailsScreen(
                        viewModel = viewModel,
                    ) { action ->
                        when (action) {
                            AddExpenseAction.Back -> secondPaneNavController.navigateUp()
                        }
                    }
                }
            }
        },
    )
}
