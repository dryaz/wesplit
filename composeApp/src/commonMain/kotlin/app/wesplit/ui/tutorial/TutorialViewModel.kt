package app.wesplit.ui.tutorial

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.wesplit.domain.model.account.Account
import app.wesplit.domain.model.account.AccountRepository
import app.wesplit.domain.model.user.Setting
import app.wesplit.domain.model.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class TutorialViewModel(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
) : ViewModel(), KoinComponent {
    val state: StateFlow<TutorialState>
        get() = _state

    private val _state = MutableStateFlow<TutorialState>(TutorialState.None)

    private var requestedSteps: List<TutorialStep> = emptyList()
    private var currentStepIndex: Int = 0
    private val targetPosition: MutableMap<TutorialStep, Rect?> = mutableMapOf()

    fun requestSteps(steps: List<TutorialStep>) {
        viewModelScope.launch {
            val account = accountRepository.get().first { it !is Account.Unknown }
            val requiredSteps: List<TutorialStep> =
                when (account) {
                    is Account.Authorized -> {
                        val user = account.user.filterNotNull().first()
                        steps.filter { it.onboardingStep !in user.completedOnboardingSteps }
                    }
                    Account.Anonymous,
                    Account.Restricted,
                    -> emptyList()

                    Account.Unknown -> throw IllegalStateException("Try to show tutorial for unknown acc")
                }

            if (requiredSteps.isNotEmpty()) {
                currentStepIndex = 0
                requestedSteps = requiredSteps
                invalidateState()
            }
        }
    }

    fun onPositionReceived(
        step: TutorialStep,
        rect: Rect,
    ) {
        targetPosition[step] = rect
        invalidateState()
    }

    fun nextStep() {
        currentStepIndex++
        invalidateState()
    }

    private fun invalidateState() {
        if (currentStepIndex > requestedSteps.size - 1) {
            userRepository.update(Setting.CompletedOnboardedSteps(requestedSteps.map { it.onboardingStep }))
            _state.update { TutorialState.None }
        } else {
            val step = requestedSteps[minOf(currentStepIndex, requestedSteps.size - 1)]
            val targetRect = targetPosition[step]
            _state.update { TutorialState.Step(step, targetRect) }
        }
    }

    sealed interface TutorialState {
        data object None : TutorialState

        data class Step(val step: TutorialStep, val position: Rect?) : TutorialState
    }
}
