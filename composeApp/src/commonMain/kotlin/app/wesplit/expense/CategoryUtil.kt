package app.wesplit.expense

import androidx.compose.runtime.Composable
import app.wesplit.domain.model.expense.Category
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.category_beauty
import split.composeapp.generated.resources.category_books
import split.composeapp.generated.resources.category_car
import split.composeapp.generated.resources.category_cleaning
import split.composeapp.generated.resources.category_clothes
import split.composeapp.generated.resources.category_coffee
import split.composeapp.generated.resources.category_concert
import split.composeapp.generated.resources.category_electricity
import split.composeapp.generated.resources.category_entertainment
import split.composeapp.generated.resources.category_fast_food
import split.composeapp.generated.resources.category_fee
import split.composeapp.generated.resources.category_flight
import split.composeapp.generated.resources.category_food_drink
import split.composeapp.generated.resources.category_furnishing
import split.composeapp.generated.resources.category_garbage
import split.composeapp.generated.resources.category_gifts
import split.composeapp.generated.resources.category_groceries
import split.composeapp.generated.resources.category_health
import split.composeapp.generated.resources.category_health_beauty
import split.composeapp.generated.resources.category_hobby
import split.composeapp.generated.resources.category_housing
import split.composeapp.generated.resources.category_internet
import split.composeapp.generated.resources.category_movie
import split.composeapp.generated.resources.category_none
import split.composeapp.generated.resources.category_parking
import split.composeapp.generated.resources.category_public
import split.composeapp.generated.resources.category_recycling
import split.composeapp.generated.resources.category_rent
import split.composeapp.generated.resources.category_repair
import split.composeapp.generated.resources.category_restaurant
import split.composeapp.generated.resources.category_security
import split.composeapp.generated.resources.category_shoes
import split.composeapp.generated.resources.category_shopping
import split.composeapp.generated.resources.category_sport
import split.composeapp.generated.resources.category_sport_event
import split.composeapp.generated.resources.category_tax
import split.composeapp.generated.resources.category_taxi
import split.composeapp.generated.resources.category_technology
import split.composeapp.generated.resources.category_tolls
import split.composeapp.generated.resources.category_transport_travel
import split.composeapp.generated.resources.category_transportation
import split.composeapp.generated.resources.category_utilities
import split.composeapp.generated.resources.category_water
import split.composeapp.generated.resources.ic_cat_beauty
import split.composeapp.generated.resources.ic_cat_books
import split.composeapp.generated.resources.ic_cat_car
import split.composeapp.generated.resources.ic_cat_cleaning
import split.composeapp.generated.resources.ic_cat_clothes
import split.composeapp.generated.resources.ic_cat_coffee
import split.composeapp.generated.resources.ic_cat_concert
import split.composeapp.generated.resources.ic_cat_electricity
import split.composeapp.generated.resources.ic_cat_entertainment
import split.composeapp.generated.resources.ic_cat_fastfood
import split.composeapp.generated.resources.ic_cat_fee
import split.composeapp.generated.resources.ic_cat_flight
import split.composeapp.generated.resources.ic_cat_food_drink
import split.composeapp.generated.resources.ic_cat_furnishing
import split.composeapp.generated.resources.ic_cat_garbage
import split.composeapp.generated.resources.ic_cat_gift
import split.composeapp.generated.resources.ic_cat_groceries
import split.composeapp.generated.resources.ic_cat_health
import split.composeapp.generated.resources.ic_cat_hobby
import split.composeapp.generated.resources.ic_cat_housing
import split.composeapp.generated.resources.ic_cat_internet
import split.composeapp.generated.resources.ic_cat_movie
import split.composeapp.generated.resources.ic_cat_none
import split.composeapp.generated.resources.ic_cat_parking
import split.composeapp.generated.resources.ic_cat_property_tax
import split.composeapp.generated.resources.ic_cat_public_transport
import split.composeapp.generated.resources.ic_cat_recycle
import split.composeapp.generated.resources.ic_cat_rent
import split.composeapp.generated.resources.ic_cat_repair
import split.composeapp.generated.resources.ic_cat_restaurant
import split.composeapp.generated.resources.ic_cat_security
import split.composeapp.generated.resources.ic_cat_shoes
import split.composeapp.generated.resources.ic_cat_shopping
import split.composeapp.generated.resources.ic_cat_sport
import split.composeapp.generated.resources.ic_cat_sport_event
import split.composeapp.generated.resources.ic_cat_taxi
import split.composeapp.generated.resources.ic_cat_technology
import split.composeapp.generated.resources.ic_cat_toll
import split.composeapp.generated.resources.ic_cat_transport
import split.composeapp.generated.resources.ic_cat_transport_travel
import split.composeapp.generated.resources.ic_cat_utilities
import split.composeapp.generated.resources.ic_cat_water
import split.composeapp.generated.resources.ic_health_beauty

@Composable
fun Category.uiTitle(): String =
    stringResource(
        when (this) {
            Category.None -> Res.string.category_none
            Category.Housing -> Res.string.category_housing
            Category.Utilities -> Res.string.category_utilities
            Category.Electricity -> Res.string.category_electricity
            Category.Internet -> Res.string.category_internet
            Category.Water -> Res.string.category_water
            Category.Recycling -> Res.string.category_recycling
            Category.Garbage -> Res.string.category_garbage
            Category.Repair -> Res.string.category_repair
            Category.Cleaning -> Res.string.category_cleaning
            Category.Rent -> Res.string.category_rent
            Category.Tax -> Res.string.category_tax
            Category.Furnishing -> Res.string.category_furnishing
            Category.Security -> Res.string.category_security
            Category.FoodDrink -> Res.string.category_food_drink
            Category.FastFood -> Res.string.category_fast_food
            Category.Coffee -> Res.string.category_coffee
            Category.Restaurant -> Res.string.category_restaurant
            Category.Groceries -> Res.string.category_groceries
            Category.TransportTravel -> Res.string.category_transport_travel
            Category.Transportation -> Res.string.category_transportation
            Category.Taxi -> Res.string.category_taxi
            Category.Flight -> Res.string.category_flight
            Category.Public -> Res.string.category_public
            Category.Car -> Res.string.category_car
            Category.Parking -> Res.string.category_parking
            Category.Tolls -> Res.string.category_tolls
            Category.Fee -> Res.string.category_fee
            Category.Gifts -> Res.string.category_gifts
            Category.Shopping -> Res.string.category_shopping
            Category.Technology -> Res.string.category_technology
            Category.Clothes -> Res.string.category_clothes
            Category.Shoes -> Res.string.category_shoes
            Category.Entertainment -> Res.string.category_entertainment
            Category.Movie -> Res.string.category_movie
            Category.Concert -> Res.string.category_concert
            Category.Books -> Res.string.category_books
            Category.SportEvent -> Res.string.category_sport_event
            Category.Hobby -> Res.string.category_hobby
            Category.HealthBeauty -> Res.string.category_health_beauty
            Category.Health -> Res.string.category_health
            Category.Beauty -> Res.string.category_beauty
            Category.Sport -> Res.string.category_sport
        },
    )

@Composable
fun Category.categoryIconRes(): DrawableResource =
    when (this) {
        Category.None -> Res.drawable.ic_cat_none
        Category.Housing -> Res.drawable.ic_cat_housing
        Category.Utilities -> Res.drawable.ic_cat_utilities
        Category.Electricity -> Res.drawable.ic_cat_electricity
        Category.Internet -> Res.drawable.ic_cat_internet
        Category.Water -> Res.drawable.ic_cat_water
        Category.Recycling -> Res.drawable.ic_cat_recycle
        Category.Garbage -> Res.drawable.ic_cat_garbage
        Category.Repair -> Res.drawable.ic_cat_repair
        Category.Cleaning -> Res.drawable.ic_cat_cleaning
        Category.Rent -> Res.drawable.ic_cat_rent
        Category.Tax -> Res.drawable.ic_cat_property_tax
        Category.Furnishing -> Res.drawable.ic_cat_furnishing
        Category.Security -> Res.drawable.ic_cat_security
        Category.FoodDrink -> Res.drawable.ic_cat_food_drink
        Category.FastFood -> Res.drawable.ic_cat_fastfood
        Category.Coffee -> Res.drawable.ic_cat_coffee
        Category.Restaurant -> Res.drawable.ic_cat_restaurant
        Category.Groceries -> Res.drawable.ic_cat_groceries
        Category.TransportTravel -> Res.drawable.ic_cat_transport_travel
        Category.Transportation -> Res.drawable.ic_cat_transport
        Category.Taxi -> Res.drawable.ic_cat_taxi
        Category.Flight -> Res.drawable.ic_cat_flight
        Category.Public -> Res.drawable.ic_cat_public_transport
        Category.Car -> Res.drawable.ic_cat_car
        Category.Parking -> Res.drawable.ic_cat_parking
        Category.Tolls -> Res.drawable.ic_cat_toll
        Category.Fee -> Res.drawable.ic_cat_fee
        Category.Gifts -> Res.drawable.ic_cat_gift
        Category.Shopping -> Res.drawable.ic_cat_shopping
        Category.Technology -> Res.drawable.ic_cat_technology
        Category.Clothes -> Res.drawable.ic_cat_clothes
        Category.Shoes -> Res.drawable.ic_cat_shoes
        Category.Entertainment -> Res.drawable.ic_cat_entertainment
        Category.Movie -> Res.drawable.ic_cat_movie
        Category.Concert -> Res.drawable.ic_cat_concert
        Category.Books -> Res.drawable.ic_cat_books
        Category.SportEvent -> Res.drawable.ic_cat_sport_event
        Category.Hobby -> Res.drawable.ic_cat_hobby
        Category.HealthBeauty -> Res.drawable.ic_health_beauty
        Category.Health -> Res.drawable.ic_cat_health
        Category.Beauty -> Res.drawable.ic_cat_beauty
        Category.Sport -> Res.drawable.ic_cat_sport
    }
