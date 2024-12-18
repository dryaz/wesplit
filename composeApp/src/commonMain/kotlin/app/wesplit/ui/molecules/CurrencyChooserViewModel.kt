package app.wesplit.ui.molecules

import androidx.lifecycle.ViewModel
import app.wesplit.domain.model.currency.CurrencyRepository
import org.koin.core.component.KoinComponent

class CurrencyChooserViewModel(
    private val currencyRepository: CurrencyRepository,
) : ViewModel(), KoinComponent {
    val currencies = currencyRepository.getAvailableCurrencyCodes()
}
