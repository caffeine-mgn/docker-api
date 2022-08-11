package pw.binom.docker.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProcessConfig(
    val tty: Boolean,
    val entrypoint: String,
    val arguments: List<String>? = emptyList(),
    val privileged: Boolean = false
)
