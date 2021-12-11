package pw.binom.docker.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.docker.dto.Env

object EnvSerializer : KSerializer<Env> {
    override fun deserialize(decoder: Decoder): Env {
        val items = decoder.decodeString().split(' ', limit = 2)
        return Env(
            name = items[0],
            value = items[1],
        )
    }

    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Env) {
        encoder.encodeString("${value.name}=${value.value}")
    }

}