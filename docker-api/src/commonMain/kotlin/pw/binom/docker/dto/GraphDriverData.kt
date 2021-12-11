package pw.binom.docker.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about a container's graph driver.
 */
@Serializable
data class GraphDriverData(
    @SerialName("Name")
    val name: String,
    @SerialName("Data")
    val data: Map<String, String>
)