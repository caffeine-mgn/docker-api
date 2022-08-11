package pw.binom.docker.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.docker.serialization.DateIso8601Serializer

@Serializable
data class Metadata(
    @Serializable(DateIso8601Serializer::class)
    @SerialName("LastTagTime")
    val lastTagTime: DateTime
)
