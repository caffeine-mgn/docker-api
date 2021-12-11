package pw.binom.docker.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateContainerResponse(
    @SerialName("Id")
    val id: String,

    @SerialName("Warnings")
    val warnings: List<String>? = emptyList()
)