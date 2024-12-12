package app.wesplit.data.firebase

import app.wesplit.domain.model.AnalyticsManager
import app.wesplit.domain.model.experiment.Experiment
import app.wesplit.domain.model.experiment.ExperimentRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.get
import dev.gitlive.firebase.remoteconfig.remoteConfig
import org.koin.core.annotation.Single

@Single
class ExperimentFirebaseRepository(
    val analyticsManager: AnalyticsManager,
) : ExperimentRepository {
    override fun get(experiment: Experiment): Long = Firebase.remoteConfig.get<Long>(experiment.configName)

    override suspend fun refresh() {
        try {
            // This method remains in case experiments moves into other place.
            //  RemotConfig init happens also in FeatureFirebaseRepository.
        } catch (e: Exception) {
            analyticsManager.log(e)
        }
    }
}
