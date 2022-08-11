package pw.binom.docker.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.DateTime
import pw.binom.date.iso8601
import pw.binom.date.parseIso8601Date

object DateTimeIso8601Serializer : KSerializer<DateTime> {
    override fun deserialize(decoder: Decoder): DateTime {
        val str = decoder.decodeString()
        return str.parseIso8601Date() ?: throw SerializationException("Can't parse \"$str\" to date ISO-8601")
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(value.iso8601())
    }
}
