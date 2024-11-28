package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.LogLevel
import app.wesplit.domain.model.feature.Feature
import app.wesplit.domain.model.feature.FeatureAvailability
import app.wesplit.domain.model.feature.FeatureRepository
import app.wesplit.domain.model.feature.toFeatureAvail
import app.wesplit.domain.model.user.UserRepository
import app.wesplit.domain.model.user.isPlus
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.get
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.days

@Single
class FeatureFirebaseRepository(
    val userRepository: UserRepository,
    val analyticsManager: AnalyticsManager,
) : FeatureRepository {
    override suspend fun get(feature: Feature): Flow<FeatureAvailability> =
        userRepository.get().map { user ->
            val isPlus = user.isPlus()
            val availability = Firebase.remoteConfig.get<Long>(feature.configName).toFeatureAvail()
            println("Feature $feature avail: $availability | isPlus: $isPlus")
            Firebase.remoteConfig.all.forEach {
                println("Config value: ${it.key}: ${it.value}")
            }
            return@map if (availability == FeatureAvailability.PAYWAL && isPlus) {
                FeatureAvailability.AVAIL
            } else {
                availability
            }
        }

    override suspend fun refresh() {
        try {
            Firebase.remoteConfig.reset()
            Firebase.remoteConfig.fetch(minimumFetchInterval = 5.days)
            Firebase.remoteConfig.activate()

            analyticsManager.log("Remote Config values fetched and activated!", LogLevel.DEBUG)
        } catch (e: Exception) {
            analyticsManager.log(e)
        }
    }
}
