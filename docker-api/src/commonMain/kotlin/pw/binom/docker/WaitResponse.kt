package pw.binom.docker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.docker.dto.ErrorResponse

@Serializable
data class WaitResponse(
    @SerialName("StatusCode")
    val statusCode: Int,
    @SerialName("Error")
    val error: ErrorResponse? = null
) {
    @Serializable
    data class Error(
        @SerialName("Message")
        val message: String
    )
}