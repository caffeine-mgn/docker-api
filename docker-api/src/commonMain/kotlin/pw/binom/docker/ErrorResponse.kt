package pw.binom.docker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ErrorResponse(
    @SerialName("message")
    val msg: String
) {
}