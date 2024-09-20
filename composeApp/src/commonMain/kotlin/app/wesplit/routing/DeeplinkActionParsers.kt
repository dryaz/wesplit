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

internal val GropuDetailsIdParser =
    ActionParser { components, pathIndex ->
        components.getPath().getOrNull(pathIndex)?.takeIf { it.isNotBlank() }?.let {
            DeeplinkAction.GroupDetails(
                groupId = it,
                token = components.getSearch().getValue(DeeplinkAction.GroupDetails.TOKEN),
            )
        }
    }

internal val GroupDetailsParser =
    SegmentCheckParser(
        DeeplinkAction.GroupDetails.SEGMENT,
        BranchActionParser(GropuDetailsIdParser),
    )

private val rootParsers =
    listOf(
        ProfileParser,
        GroupDetailsParser,
    )

val RootActionParser =
    BranchActionParser(rootParsers) { components, _ ->
        if (components.getPath().isEmpty()) DeeplinkAction.Home() else DeeplinkAction.Unknown(components)
    }
