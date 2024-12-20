package app.wesplit.quicksplit

import app.wesplit.domain.model.group.Participant
import app.wesplit.domain.model.user.User
import app.wesplit.domain.model.user.participant
import app.wesplit.utils.UserRepositoryMock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickSplitViewModelTest {
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun clean() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_state_empty_data() =
        runTest {
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )
            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                amount.value shouldBe 0.0
                amount.currencyCode shouldBe "USD"
                undistributedValue shouldBe 0.0
                participants.size shouldBe 0
                selectedParticipants.size shouldBe 0
                items.size shouldBe 0
            }
        }

    @Test
    fun init_state_with_user_avail() =
        runTest {
            val user = User(name = "abc", lastUsedCurrency = "EUR", id = "def")
            val participant = user.participant()
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(user),
                )
            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                amount.value shouldBe 0.0
                amount.currencyCode shouldBe "EUR"
                undistributedValue shouldBe 0.0
                participants.size shouldBe 1
                participants.first() shouldBe participant
                selectedParticipants.size shouldBe 1
                selectedParticipants.first() shouldBe participant
                items.size shouldBe 0
            }
        }

    @Test
    fun update_participant_updates() =
        runTest {
            val u1 = User(name = "abc", lastUsedCurrency = "EUR", id = "u1")
            val u2 = User(name = "def", lastUsedCurrency = "EUR", id = "u2")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )
            vm.update(
                UpdateAction.UpdateExpenseParticipants(
                    newParticipants = setOf(u1.participant(), u2.participant()),
                ),
            )
            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                amount.value shouldBe 0.0
                amount.currencyCode shouldBe "USD"
                undistributedValue shouldBe 0.0
                participants.size shouldBe 2
                selectedParticipants.size shouldBe 2
                items.size shouldBe 0
            }
        }

    @Test
    fun remove_user_from_share_will_remoce_its_shares() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Lunch",
                    priceValue = 100.0,
                )
            val participant1 = Participant("p1", "John")
            val participant2 = Participant("p2", "Doe")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )
            val initialParticipants = setOf(participant1, participant2)

            vm.update(
                UpdateAction.AddItem(shareItem, initialParticipants),
            )
            vm.update(
                UpdateAction.UpdateExpenseParticipants(setOf(participant1)),
            )
            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                items[shareItem]?.get(participant1) shouldBe 1
                items[shareItem]?.get(participant2) shouldBe null
            }
        }

    @Test
    fun update_share_participants_updates_correctly() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Lunch",
                    priceValue = 100.0,
                )
            val participant1 = Participant("p1", "John")
            val participant2 = Participant("p2", "Doe")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )
            val initialParticipants = setOf(participant1)
            val updatedParticipants = setOf(participant1, participant2)

            vm.update(
                UpdateAction.AddItem(shareItem, initialParticipants),
            )
            vm.update(
                UpdateAction.UpdateShareParticipants(shareItem, updatedParticipants, shareDx = 1),
            )
            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                items[shareItem]?.get(participant1) shouldBe 2
                items[shareItem]?.get(participant2) shouldBe 1
            }
        }

    @Test
    fun update_shares_to_0_remoes_share() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Lunch",
                    priceValue = 100.0,
                )
            val participant1 = Participant("p1", "John")
            val participant2 = Participant("p2", "Doe")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )
            val initialParticipants = setOf(participant1, participant2)
            val updatedParticipants = setOf(participant1)

            vm.update(
                UpdateAction.AddItem(shareItem, initialParticipants),
            )
            vm.update(
                UpdateAction.UpdateShareParticipants(shareItem, updatedParticipants, shareDx = -1),
            )
            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                items[shareItem]?.get(participant1) shouldBe null
                items[shareItem]?.get(participant2) shouldBe 1
            }
        }

    @Test
    fun add_item_updates_state() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Dinner",
                    priceValue = 50.0,
                )
            val participant = Participant("p1", "Alice")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.AddItem(shareItem, setOf(participant)))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                items.size shouldBe 1
                items[shareItem]?.size shouldBe 1
                items[shareItem]?.get(participant) shouldBe 1
            }
        }

    @Test
    fun remove_item_updates_state() =
        runTest {
            val shareItem1 =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Groceries",
                    priceValue = 20.0,
                )
            val shareItem2 =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Fuel",
                    priceValue = 30.0,
                )
            val participant = Participant("p1", "Alice")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.AddItem(shareItem1, setOf(participant)))
            vm.update(UpdateAction.AddItem(shareItem2, setOf(participant)))

            vm.update(UpdateAction.RemoveItem(shareItem1))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                items.size shouldBe 1
                items.containsKey(shareItem1) shouldBe false
                items.containsKey(shareItem2) shouldBe true
            }
        }

    @Test
    fun update_amount_updates_state() =
        runTest {
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.UpdateAmountCurrency("EUR"))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                amount.currencyCode shouldBe "EUR"
            }
        }

    @Test
    fun update_share_participants_negative_dx() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Gift",
                    priceValue = 50.0,
                )
            val participant = Participant("p1", "John")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.AddItem(shareItem, setOf(participant)))
            vm.update(UpdateAction.UpdateShareParticipants(shareItem, setOf(participant), shareDx = -1))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                items[shareItem]?.get(participant) shouldBe null // Ensures shares don't go below 0
            }
        }

    @Test
    fun update_expense_participants_removes_unshared_participants() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Event",
                    priceValue = 100.0,
                )
            val participant1 = Participant("p1", "Alice")
            val participant2 = Participant("p2", "Bob")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.AddItem(shareItem, setOf(participant1, participant2)))
            vm.update(UpdateAction.UpdateExpenseParticipants(setOf(participant1)))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                participants.size shouldBe 1
                participants.first() shouldBe participant1
                items[shareItem]?.containsKey(participant2) shouldBe false
            }
        }

    @Test
    fun undistributed_value_updates_correctly() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Dinner",
                    priceValue = 150.0,
                )
            val participant = Participant("p1", "Alice")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.AddItem(shareItem, setOf(participant)))
            vm.update(UpdateAction.UpdateAmountValue(200.0))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                undistributedValue shouldBe 50.0 // 200 - 150
            }
        }

    @Test
    fun undistributed_value_remains_for_undistributed_items() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Dinner",
                    priceValue = 150.0,
                )
            val finalItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Dinner",
                    priceValue = 50.0,
                )
            val participant = Participant("p1", "Alice")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.UpdateAmountValue(200.0))
            vm.update(UpdateAction.AddItem(shareItem, setOf(participant)))
            vm.update(UpdateAction.AddItem(finalItem, emptySet()))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                undistributedValue shouldBe 50.0 // 200 - 150
            }
        }

    @Test
    fun undistributed_value_clears_for_distributed_items() =
        runTest {
            val shareItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Dinner",
                    priceValue = 150.0,
                )
            val finalItem =
                QuickSplitViewModel.State.Data.ShareItem(
                    title = "Dinner",
                    priceValue = 50.0,
                )
            val participant = Participant("p1", "Alice")
            val vm =
                QuickSplitViewModel(
                    userRepository = UserRepositoryMock(),
                )

            vm.update(UpdateAction.UpdateAmountValue(200.0))
            vm.update(UpdateAction.AddItem(shareItem, setOf(participant)))
            vm.update(UpdateAction.AddItem(finalItem, setOf(participant)))

            vm.state.value.shouldBeInstanceOf<QuickSplitViewModel.State.Data>()
            with(vm.state.value as QuickSplitViewModel.State.Data) {
                undistributedValue shouldBe 0.0 // 200 - 150 - 50
            }
        }
}
