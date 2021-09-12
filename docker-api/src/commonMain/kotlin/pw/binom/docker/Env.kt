package pw.binom.docker

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(EnvSerializer::class)
data class Env(val name: String, val value: String) {
    init {
        require(name.isNotBlank()) { "Name is empty" }
        require(" " !in name) { "Invalid name \"$name\". Name should not contains space" }
    }
}

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