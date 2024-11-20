package app.wesplit.domain.model.expense

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    @SerialName("-1")
    Magic,

    @SerialName("0")
    None,

    // Housing
    @SerialName("1")
    Housing,

    // - Utilities
    @SerialName("2")
    Utilities,

    @SerialName("3")
    Electricity,

    @SerialName("4")
    Internet,

    @SerialName("5")
    Water,

    @SerialName("6")
    Recycling,

    @SerialName("7")
    Garbage,

    // - Maintenance
    @SerialName("8")
    Repair,

    @SerialName("9")
    Cleaning,

    // - Other
    @SerialName("10")
    Rent,

    @SerialName("11")
    Tax,

    @SerialName("12")
    Furnishing,

    @SerialName("13")
    Security,

    // Food and Drink
    @SerialName("14")
    FoodDrink,

    // - Cafes
    @SerialName("15")
    FastFood,

    @SerialName("16")
    Coffee,

    @SerialName("17")
    Restaurant,

    // - Other
    @SerialName("18")
    Groceries,

    // Transport and Travel
    @SerialName("19")
    TransportTravel,

    // - Transportation
    @SerialName("20")
    Transportation,

    @SerialName("21")
    Taxi,

    @SerialName("22")
    Flight,

    @SerialName("23")
    Public,

    // - Car
    @SerialName("24")
    Car,

    @SerialName("25")
    Parking,

    @SerialName("26")
    Tolls,

    @SerialName("27")
    Fee,

    // Gifts
    @SerialName("28")
    Gifts,

    // Shopping
    @SerialName("29")
    Shopping,

    @SerialName("30")
    Technology,

    @SerialName("31")
    Clothes,

    @SerialName("32")
    Shoes,

    // Entertainment
    @SerialName("33")
    Entertainment,

    @SerialName("34")
    Movie,

    @SerialName("35")
    Concert,

    @SerialName("36")
    Books,

    @SerialName("37")
    SportEvent,

    @SerialName("38")
    Hobby,

    // Health and Beauty
    @SerialName("39")
    HealthBeauty,

    // - Health
    @SerialName("40")
    Health,

    // - Beauty
    @SerialName("41")
    Beauty,

    // - Beauty
    @SerialName("42")
    Sport,

    // Money
    @SerialName("43")
    MoneyTransfer,

    // - Cash
    @SerialName("44")
    Cash,

    // - BT
    @SerialName("45")
    BankTransfer,

    // - Crypto
    @SerialName("46")
    Crypto,
}

val categories: Map<Category, List<Category>> =
    mapOf(
        // Housing
        Category.Housing to
            listOf(
                Category.Housing,
                Category.Utilities,
                Category.Electricity,
                Category.Internet,
                Category.Water,
                Category.Recycling,
                Category.Garbage,
                Category.Repair,
                Category.Cleaning,
                Category.Rent,
                Category.Tax,
                Category.Furnishing,
                Category.Security,
            ),
        // Food and Drink
        Category.FoodDrink to
            listOf(
                Category.FoodDrink,
                Category.FastFood,
                Category.Coffee,
                Category.Restaurant,
                Category.Groceries,
            ),
        // Transport and Travel
        Category.TransportTravel to
            listOf(
                Category.TransportTravel,
                Category.Transportation,
                Category.Taxi,
                Category.Flight,
                Category.Public,
                Category.Car,
                Category.Parking,
                Category.Tolls,
                Category.Fee,
            ),
        // Shopping
        Category.Shopping to
            listOf(
                Category.Shopping,
                Category.Technology,
                Category.Clothes,
                Category.Shoes,
            ),
        // Entertainment
        Category.Entertainment to
            listOf(
                Category.Entertainment,
                Category.Movie,
                Category.Concert,
                Category.Books,
                Category.SportEvent,
                Category.Hobby,
            ),
        // Money transfer
        Category.MoneyTransfer to
            listOf(
                Category.MoneyTransfer,
                Category.Cash,
                Category.BankTransfer,
                Category.Crypto,
            ),
        // Health and Beauty
        Category.HealthBeauty to
            listOf(
                Category.HealthBeauty,
                Category.Health,
                Category.Beauty,
            ),
        // Sport
        Category.Sport to listOf(Category.Sport),
        // Gifts
        Category.Gifts to listOf(Category.Gifts),
    )

val freeCategories =
    setOf(
        Category.None,
        Category.Housing,
        Category.FoodDrink,
        Category.TransportTravel,
        Category.Shopping,
        Category.MoneyTransfer,
    )
