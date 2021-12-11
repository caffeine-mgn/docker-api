package pw.binom.docker.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.Date
import pw.binom.docker.serialization.DateIso8601Serializer

@Serializable
data class ImageInfo(
    @SerialName("Id")
    val id: String,
    @SerialName("RepoTags")
    val repoTags: List<String> = emptyList(),
    @SerialName("RepoDigests")
    val repoDigests: List<String> = emptyList(),
    @SerialName("Parent")
    val parent: String,
    @SerialName("Comment")
    val comment: String,
    @SerialName("Created")
    @Serializable(DateIso8601Serializer::class)
    val created: Date,
    @SerialName("Container")
    val container: String,
    /**
     * Configuration for a container that is portable between hosts
     */
    @SerialName("ContainerConfig")
    val containerConfig: ContainerInfo.Config,
    @SerialName("DockerVersion")
    val dockerVersion: String,
    @SerialName("Author")
    val author: String,
    @SerialName("Config")
    val config: ContainerInfo.Config,
    @SerialName("Architecture")
    val architecture: String,
    @SerialName("Os")
    val os: String,
    /**
     * Version of the host's operating system
     */
    @SerialName("OsVersion")
    val osVersion: String? = null,
    @SerialName("Size")
    val size: Long,
    @SerialName("VirtualSize")
    val virtualSize: Long,
    @SerialName("GraphDriver")
    val graphDriver: GraphDriverData,
    @SerialName("RootFS")
    val rootFS: RootFS,
    @SerialName("Metadata")
    val metadata: Metadata?
)