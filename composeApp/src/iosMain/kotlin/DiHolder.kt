import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DiHolder : KoinComponent {
    val billingRepository: BillingIosRepositoryController by inject()
}
