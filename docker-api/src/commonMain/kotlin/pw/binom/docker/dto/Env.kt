package pw.binom.docker.dto

import kotlinx.serialization.Serializable
import pw.binom.docker.serialization.EnvSerializer

@Serializable(EnvSerializer::class)
data class Env(val name: String, val value: String) {
    init {
        require(name.isNotBlank()) { "Name is empty" }
        require(" " !in name) { "Invalid name \"$name\". Name should not contains space" }
    }
}
