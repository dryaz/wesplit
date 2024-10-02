package app.wesplit

interface ShareDelegate {
    fun supportPlatformSharing(): Boolean

    fun share(data: ShareData)

    fun open(data: ShareData)
}

sealed interface ShareData {
    data class Link(val value: String) : ShareData
}

object DefaultShareDelegate : ShareDelegate {
    override fun supportPlatformSharing(): Boolean = false

    override fun share(data: ShareData) {
        throw NotImplementedError("DefaultShareDelegate can't customly share data")
    }

    override fun open(data: ShareData) {
        throw NotImplementedError("DefaultShareDelegate can't customly open data")
    }
}
