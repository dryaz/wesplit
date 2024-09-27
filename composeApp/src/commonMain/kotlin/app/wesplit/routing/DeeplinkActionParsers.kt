package app.wesplit.routing

import com.motorro.keeplink.deeplink.ActionParser
import com.motorro.keeplink.deeplink.BranchActionParser
import com.motorro.keeplink.deeplink.DefaultActionParser
import com.motorro.keeplink.deeplink.SegmentCheckParser
import com.motorro.keeplink.uri.data.getValue

internal val ProfileParser =
    SegmentCheckParser(
        DeeplinkAction.Profile.SEGMENT,
        DefaultActionParser { DeeplinkAction.Profile() },
    )

internal val GroupIdParser =
    ActionParser { components, pathIndex ->
        if (pathIndex != components.getPath().size - 1) return@ActionParser null

        components.getPath().getOrNull(pathIndex)?.takeIf { it.isNotBlank() }?.let {
            DeeplinkAction.Group.Details(
                groupId = it,
                token = components.getSearch().getValue(DeeplinkAction.Group.Details.TOKEN),
            )
        }
    }

internal val ExpenseIdParser =
    ActionParser { components, pathIndex ->
        if (pathIndex == components.getPath().size - 1) return@ActionParser null
        val groupId = components.getPath().getOrNull(pathIndex)

        val nextPathElement = components.getPath().getOrNull(pathIndex + 1)
        if (nextPathElement != "expense") return@ActionParser null

        val expenseId = components.getPath().getOrNull(pathIndex + 2)
        return@ActionParser if (groupId != null && expenseId != null) {
            DeeplinkAction.Group.Expense(groupId, expenseId)
        } else {
            null
        }
    }

internal val GroupDetailsParser =
    SegmentCheckParser(
        DeeplinkAction.Group.SEGMENT,
        BranchActionParser(GroupIdParser),
    )

internal val ExpenseDetailsParser =
    SegmentCheckParser(
        DeeplinkAction.Group.SEGMENT,
        BranchActionParser(ExpenseIdParser),
    )

private val rootParsers =
    listOf(
        ProfileParser,
        GroupDetailsParser,
        ExpenseDetailsParser,
    )

val RootActionParser =
    BranchActionParser(rootParsers) { components, _ ->
        if (components.getPath().isEmpty()) DeeplinkAction.Home() else DeeplinkAction.Unknown(components)
    }
