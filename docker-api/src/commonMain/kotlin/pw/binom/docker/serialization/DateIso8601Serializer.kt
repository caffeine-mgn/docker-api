package pw.binom.docker.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.Date
import pw.binom.date.iso8601
import pw.binom.date.parseIso8601Date

object DateIso8601Serializer : KSerializer<Date> {
    override fun deserialize(decoder: Decoder): Date{
        val str = decoder.decodeString()
        return str.parseIso8601Date()?:throw SerializationException("Can't parse \"$str\" to date ISO-8601")
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(value.iso8601())
    }
}