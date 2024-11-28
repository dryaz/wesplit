package app.wesplit.domain.model.feature

import kotlinx.coroutines.flow.Flow

interface FeatureRepository {
    suspend fun get(feature: Feature): Flow<FeatureAvailability>

    suspend fun refresh()

    // TODO: Setting of the feature could also be here
}

enum class Feature(val configName: String) {
    QUICK_ADD("quick_add"),
}

enum class FeatureAvailability(val value: Long) {
    HIDE(0),
    PAYWAL(1),
    AVAIL(2),
}

fun Long.toFeatureAvail(): FeatureAvailability {
    return FeatureAvailability.entries.firstOrNull { it.value == this } ?: FeatureAvailability.HIDE
}
