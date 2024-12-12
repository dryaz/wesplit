package app.wesplit.domain.model.experiment

interface ExperimentRepository {
    fun get(experiment: Experiment): Long

    suspend fun refresh()
}

enum class Experiment(val configName: String) {
    PAYWALL_TYPE("paywall_type"),
    PAYWALL_ITEM_TYPE("paywall_item_type"),
    PAYWALL_SHOW_WEEKLY("paywall_show_weekly"),
    PAYWALL_PRICE_PLACEMENT("paywall_pricing_placement"),
}
