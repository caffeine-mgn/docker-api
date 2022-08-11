package pw.binom.docker.dto

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
    val status: String? = null,
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
    val networkSettings: NetworkSettings? = null

) {
    @Serializable
    data class HostConfig(
        val NetworkMode: String
    )

    @Serializable
    data class NetworkSettings(
        val Networks: Map<String, Network>? = null
    )

    @Serializable
    data class Network(
        /**
         * EndpointIPAMConfig represents an endpoint's IPAM configuration.
         */
        @SerialName("IPAMConfig")
        val iPAMConfig: IPAMConfig? = null,

        /**
         * Unique ID of the network.
         */
        @SerialName("NetworkID")
        val networkID: String,

        /**
         * Unique ID for the service endpoint in a Sandbox.
         */
        @SerialName("EndpointID")
        val endpointID: String,

        /**
         * Gateway address for this network.
         */
        @SerialName("Gateway")
        val gateway: String,

        /**
         * IPv4 address.
         */
        @SerialName("IPAddress")
        val iPAddress: String,

        /**
         * Mask length of the IPv4 address.
         */
        @SerialName("IPPrefixLen")
        val iPPrefixLen: Int,

        /**
         * IPv6 gateway address.
         */
        @SerialName("IPv6Gateway")
        val iPv6Gateway: String,

        /**
         * Global IPv6 address.
         */
        @SerialName("GlobalIPv6Address")
        val globalIPv6Address: String,

        /**
         * Mask length of the global IPv6 address.
         */
        @SerialName("GlobalIPv6PrefixLen")
        val globalIPv6PrefixLen: Int,

        /**
         * MAC address for the endpoint on this network.
         */
        @SerialName("MacAddress")
        val macAddress: String,

        /**
         * DriverOpts is a mapping of driver options and values. These options are passed directly to the driver
         * and are driver specific.
         */
        @SerialName("DriverOpts")
        val driverOpts: Map<String, String>?
    )

    @Serializable
    data class Port(
        @SerialName("IP")
        val IP: String? = null,
        @SerialName("PrivatePort")
        val PrivatePort: Int? = null,
        @SerialName("PublicPort")
        val PublicPort: Int? = null,
        @SerialName("Type")
        val Type: String? = null
    )
}
