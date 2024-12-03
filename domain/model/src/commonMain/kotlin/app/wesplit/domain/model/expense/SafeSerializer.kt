package app.wesplit.domain.model.expense

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CategorySafeSerializer : SafeSerializer<Category>(Category.serializer(), Category.Magic)

open class SafeSerializer<T>(
    private val serializer: KSerializer<T>,
    private val default: T,
) : KSerializer<T> {
    override val descriptor = serializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) = encoder.encodeSerializableValue(serializer, value!!)

    override fun deserialize(decoder: Decoder): T =
        try {
            decoder.decodeSerializableValue(serializer)
        } catch (_: Exception) {
            default
        }
}
