package app.wesplit

sealed interface KotlinPlatform {
    sealed interface Mobile : KotlinPlatform

    data object Android : Mobile

    data object Ios : Mobile

    data object Web : KotlinPlatform

    data object Desktop : KotlinPlatform
}

expect val currentPlatform: KotlinPlatform
