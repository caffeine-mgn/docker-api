package pw.binom.docker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Container(
    @SerialName("Id")
    val id: String,
    @SerialName("Names")
    val names: List<String>,
    @SerialName("Image")
    val image: String? = null,
    @SerialName("ImageID")
    val ImageID: String? = null,
    @SerialName("Command")
    val Command: String? = null,
    @SerialName("Created")
    val Created: Long? = null,
    @SerialName("State")
    val state: String? = null,
    @SerialName("Status")
    val status:String?=null,
    @SerialName("Ports")
    val ports: List<Port>? = null,
    @SerialName("Labels")
    val labels: Map<String, String>? = null,
    @SerialName("SizeRw")
    val sizeRw: Int? = null,
    @SerialName("SizeRootFs")
    val sizeRootFs: Int? = null,
    @SerialName("HostConfig")
    val hostConfig: HostConfig? = null,
    @SerialName("NetworkSettings")
    val networkSettings: NetworkSettings? = null,

    ) {
    @Serializable
    data class HostConfig(
        val NetworkMode: String,
    )

    @Serializable
    data class NetworkSettings(
        val Networks: Map<String, Network>? = null
    )

    @Serializable
    data class Network(
        @SerialName("IPAMConfig")
        val iPAMConfig:IPAMConfig?=null,
        @SerialName("NetworkID")
        val networkID: String,
        @SerialName("EndpointID")
        val endpointID: String,
        @SerialName("Gateway")
        val gateway: String,
        @SerialName("IPAddress")
        val iPAddress: String,
        @SerialName("IPPrefixLen")
        val iPPrefixLen: Int,
        @SerialName("IPv6Gateway")
        val iPv6Gateway: String,
        @SerialName("GlobalIPv6Address")
        val globalIPv6Address: String,
        @SerialName("GlobalIPv6PrefixLen")
        val globalIPv6PrefixLen: Int,
        @SerialName("MacAddress")
        val macAddress: String,
    )

    @Serializable
    data class Port(
        @SerialName("IP")
        val IP:String?=null,
        @SerialName("PrivatePort")
        val PrivatePort: Int?=null,
        @SerialName("PublicPort")
        val PublicPort: Int?=null,
        @SerialName("Type")
        val Type: String?=null,
    )
}