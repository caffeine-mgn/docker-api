package pw.binom.docker.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.Date
import pw.binom.docker.serialization.DateIso8601Serializer

@Serializable
data class ContainerInfo(
    /**
     * The ID of the container
     */
    @SerialName("Id")
    val id: String,

    /**
     * The time the container was created
     */
    @SerialName("Created")
    @Serializable(DateIso8601Serializer::class)
    val created: Date,

    /**
     * The path to the command being run
     */
    @SerialName("Path")
    val path: String,

    /**
     * The arguments to the command being run
     */
    @SerialName("Args")
    val args: List<String>,

    /**
     * ContainerState stores container's running state. It's part of ContainerJSONBase and will be
     * returned by the "inspect" command.
     */
    @SerialName("State")
    val state: ContainerState,

    /**
     * The container's image ID
     */
    @SerialName("Image")
    val image: String,

    @SerialName("ResolvConfPath")
    val resolvConfPath: String,

    @SerialName("HostnamePath")
    val hostnamePath: String,

    @SerialName("HostsPath")
    val hostsPath: String,

    @SerialName("LogPath")
    val logPath: String,

    @SerialName("Name")
    val name: String,

    @SerialName("RestartCount")
    val restartCount: Int,

    @SerialName("Driver")
    val driver: String,

    @SerialName("Platform")
    val platform: String,

    @SerialName("MountLabel")
    val mountLabel: String,

    @SerialName("ProcessLabel")
    val processLabel: String,

    @SerialName("AppArmorProfile")
    val appArmorProfile: String,

    /**
     * IDs of exec instances that are running in the container.
     */
    @SerialName("ExecIDs")
    val execIDs: List<String?>? = null,

    /**
     * Container configuration that depends on the host we are running on
     */
    @SerialName("HostConfig")
    val hostConfig: HostConfig? = null,

    /**
     * Information about a container's graph driver.
     */
    @SerialName("GraphDriver")
    val graphDriverData: GraphDriverData? = null,

    /**
     * The size of files that have been created or changed by this container.
     */
    @SerialName("SizeRw")
    val sizeRw: Long? = null,

    /**
     * The total size of all the files in this container.
     */
    @SerialName("SizeRootFs")
    val sizeRootFs: Long? = null,

    /**
     * A mount point inside a container
     */
    @SerialName("Mounts")
    val mounts: List<Mount> = emptyList(),

    /**
     * Configuration for a container that is portable between hosts
     */
    @SerialName("Config")
    val config: Config? = null,

    /**
     * NetworkSettings exposes the network settings in the API
     */
    @SerialName("NetworkSettings")
    val networkSettings: ContainerNetwork? = null,
) {
//    @Serializable
//    data class HostConfig(
//
//    )
    /**
     * TODO rename to ContainerConfig
     */
    @Serializable
    data class Config(
        /**
         * The hostname to use for the container, as a valid RFC 1123 hostname.
         */
        @SerialName("Hostname")
        val hostname: String? = null,

        /**
         * The domain name to use for the container.
         */
        @SerialName("Domainname")
        val domainName: String? = null,

        /**
         * The user that commands are run as inside the container
         */
        @SerialName("User")
        val user: String? = null,

        /**
         * Whether to attach to stdin.
         */
        @SerialName("AttachStdin")
        val attachStdin: Boolean = false,

        /**
         * Whether to attach to stdout.
         */
        @SerialName("AttachStdout")
        val attachStdout: Boolean = true,

        /**
         * Whether to attach to stderr
         */
        @SerialName("AttachStderr")
        val attachStderr: Boolean = true,
        /**
         * An object mapping ports to an empty object in the form:
         *
         * {"<port>/<tcp|udp|sctp>": {}}
         */
        @SerialName("ExposedPorts")
        val exposedPorts: Map<String, Map<String, String>>? = null,

        /**
         * Attach standard streams to a TTY, including stdin if it is not closed.
         */
        @SerialName("Tty")
        val tty: Boolean = false,

        /**
         * Open `stdin`
         */
        @SerialName("OpenStdin")
        val openStdin: Boolean = false,

        /**
         * Close `stdin` after one attached client disconnects
         */
        @SerialName("StdinOnce")
        val stdinOnce: Boolean = false,

        /**
         * A list of environment variables to set inside the container in the form ["VAR=value", ...]. A variable without = is removed from the environment, rather than to have an empty value.
         */
        @SerialName("Env")
        val env: List<String>? = emptyList(),

        /**
         * Command to run specified as a string or an array of strings.
         */
        @SerialName("Cmd")
        val cmd: List<String>? = null,

        /**
         * A test to perform to check that the container is healthy.
         */
        @SerialName("Healthcheck")
        val healthCheck: Healthcheck? = null,

        /**
         * Command is already escaped (Windows only)
         */
        @SerialName("ArgsEscaped")
        val argsEscaped: Boolean? = null,

        /**
         * The name of the image to use when creating the container
         */
        @SerialName("Image")
        val image: String? = null,

        /**
         * An object mapping mount point paths inside the container to empty objects.
         */
        @SerialName("Volumes")
        val volumes: Map<String, Map<String, String>>? = null,

        /**
         * The working directory for commands to run in.
         */
        @SerialName("WorkingDir")
        val workingDir: String? = null,

        /**
         * The entry point for the container as a string or an array of strings.
         * If the array consists of exactly one empty string ([""]) then the entry point is reset to system
         * default (i.e., the entry point used by docker when there is no ENTRYPOINT instruction in the Dockerfile).
         */
        @SerialName("Entrypoint")
        val entrypoint: List<String>? = null,

        /**
         * Disable networking for the container.
         */
        @SerialName("NetworkDisabled")
        val networkDisabled: Boolean? = null,
        /**
         * MAC address of the container.
         */
        @SerialName("MacAddress")
        val macAddress: String? = null,

        /**
         * `ONBUILD` metadata that were defined in the image's `Dockerfile`.
         */
        @SerialName("OnBuild")
        val onBuild: List<String>? = null,

        /**
         * User-defined key/value metadata.
         */
        @SerialName("Labels")
        val labels: Map<String, String>? = null,

        /**
         * Signal to stop a container as a string or unsigned integer.
         */
        @SerialName("StopSignal")
        val stopSignal: String = "SIGTERM",

        /**
         * Timeout to stop a container in seconds.
         */
        @SerialName("StopTimeout")
        val stopTimeout: Int = 10,
        /**
         * Shell for when `RUN`, `CMD`, and `ENTRYPOINT` uses a shell.
         */
        @SerialName("Shell")
        val shell: List<String> = emptyList(),
    )

    @Serializable
    data class Healthcheck(

        /**
         * The test to perform. Possible values are:
         * * `[]` inherit healthcheck from image or parent image
         * * `["NONE"]` disable healthcheck
         * * `["CMD", args...]` exec arguments directly
         * * `["CMD-SHELL", command]` run command with system's default shell
         */
        val Test: List<String>,

        /**
         * The time to wait between checks in nanoseconds. It should be 0 or at least 1000000 (1 ms). 0 means inherit.
         */
        @SerialName("Interval")
        val interval: Int? = null,

        /**
         * The time to wait before considering the check to have hung. It should be 0 or at least 1000000 (1 ms). 0 means inherit.
         */
        @SerialName("Timeout")
        val timeout: Int? = null,

        /**
         * The number of consecutive failures needed to consider a container as unhealthy. 0 means inherit.
         */
        @SerialName("Retries")
        val retries: Int? = null,

        /**
         * Start period for the container to initialize before starting health-retries countdown in nanoseconds. It should be 0 or at least 1000000 (1 ms). 0 means inherit.
         */
        @SerialName("StartPeriod")
        val startPeriod: Int? = null,
    )
}

@Serializable
data class ContainerNetwork(
    /**
     * Name of the network'a bridge (for example, docker0).
     */
    @SerialName("Bridge")
    val bridge: String,

    /**
     * SandboxID uniquely represents a container's network stack.
     */
    @SerialName("SandboxID")
    val sandboxID: String,

    /**
     * Indicates if hairpin NAT should be enabled on the virtual interface.
     */
    @SerialName("HairpinMode")
    val hairpinMode: Boolean,

    /**
     * IPv6 unicast address using the link-local prefix.
     */
    @SerialName("LinkLocalIPv6Address")
    val linkLocalIPv6Address: String,

    /**
     * Prefix length of the IPv6 unicast address.
     */
    @SerialName("LinkLocalIPv6PrefixLen")
    val linkLocalIPv6PrefixLen: Int,

    /**
     * PortMap describes the mapping of container ports to host ports, using the container's port-number
     * and protocolas key in the format <port>/<protocol>, for example, 80/udp.
     * If a container's port is mapped for multiple protocols, separate entries are added to the mapping table.
     */
    @SerialName("Ports")
    val ports: Map<String, List<Map<String, String>>>? = null,

    /**
     * SandboxKey identifies the sandbox
     */
    @SerialName("SandboxKey")
    val sandboxKey: String,

    @SerialName("SecondaryIPAddresses")
    val secondaryIPAddresses: List<Address>? = null,

    @SerialName("SecondaryIPv6Addresses")
    val secondaryIPv6Addresses: List<Address>? = null,

    /**
     * EndpointID uniquely represents a service endpoint in a Sandbox.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("EndpointID")
    val endpointID: String,

    /**
     * Gateway address for the default "bridge" network.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("Gateway")
    val gateway: String,

    /**
     * Global IPv6 address for the default "bridge" network.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("GlobalIPv6Address")
    val globalIPv6Address: String,

    /**
     * Mask length of the global IPv6 address.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("GlobalIPv6PrefixLen")
    val globalIPv6PrefixLen: Int,

    /**
     * IPv4 address for the default "bridge" network.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("IPAddress")
    val ipAddress: String,

    /**
     * Mask length of the IPv4 address.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("IPPrefixLen")
    val ipPrefixLen: Int,

    /**
     * IPv6 gateway address for this network.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("IPv6Gateway")
    val ipV6Gateway: String,

    /**
     * MAC address for the container on the default "bridge" network.
     *
     * Deprecated: This field is only propagated when attached to the default "bridge" network. Use the information
     * from the "bridge" network inside the Networks map instead, which contains the same information. This field was
     * deprecated in Docker 1.9 and is scheduled to be removed in Docker 17.12.0
     */
    @Deprecated(message = "This field is only propagated when attached to the default \"bridge\" network")
    @SerialName("MacAddress")
    val macAddress: String,

    @SerialName("Networks")
    val networks: Map<String, Container.Network>
)

@Serializable
data class Address(
    /**
     * IP address.
     */
    @SerialName("Addr")
    val addr: String,

    /**
     * Mask length of the IP address.
     */
    @SerialName("PrefixLen")
    val prefixLen: Int
)

@Serializable
data class Mount(
    @SerialName("Name")
    val name: String,
    @SerialName("Source")
    val source: String,
    @SerialName("Destination")
    val destination: String,
    @SerialName("Driver")
    val driver: String,
    @SerialName("Mode")
    val mode: String,
    @SerialName("RW")
    val rw: Boolean,
    @SerialName("Propagation")
    val propagation: String,
)

/**
 * ContainerState stores container's running state.
 */
@Serializable
data class ContainerState(
    @SerialName("Status")
    val status: ContainerStateEnum,

    /**
     * Whether this container is running.
     */
    @SerialName("Running")
    val running: Boolean,

    /**
     * Whether this container is paused.
     */
    @SerialName("Paused")
    val paused: Boolean,

    /**
     * Whether this container is restarting.
     */
    @SerialName("Restarting")
    val restarting: Boolean,

    /**
     * Whether this container has been killed because it ran out of memory.
     */
    @SerialName("OOMKilled")
    val oomKilled: Boolean,
    @SerialName("Dead")
    val dead: Boolean,

    /**
     * The process ID of this container
     */
    @SerialName("Pid")
    val pid: Long,

    /**
     * The last exit code of this container
     */
    @SerialName("ExitCode")
    val exitCode: Int? = null,

    @SerialName("Error")
    val error: String? = null,

    /**
     * The time when this container was last started.
     */
    @SerialName("StartedAt")
    @Serializable(DateIso8601Serializer::class)
    val startedAt: Date? = null,

    /**
     * The time when this container last exited.
     */
    @Serializable(DateIso8601Serializer::class)
    @SerialName("FinishedAt")
    val finishedAt: Date? = null,

    /**
     * Health stores information about the container's healthcheck results.
     */
    @SerialName("Health")
    val health: HealthContainer? = null,
)

/**
 * Health stores information about the container's healthcheck results.
 */
@Serializable
data class HealthContainer(
    @SerialName("Status")
    val status: HealthState,

    /**
     * FailingStreak is the number of consecutive failures
     */
    @SerialName("FailingStreak")
    val failingStreak: Int,

    /**
     * Log contains the last few results (oldest first)
     */
    @SerialName("Log")
    val log: List<Log>
)

@Serializable
enum class HealthState {
    /**
     * Indicates there is no healthcheck
     */
    @SerialName("none")
    NONE,

    /**
     * Starting indicates that the container is not yet ready
     */
    @SerialName("starting")
    STARTING,

    /**
     * Healthy indicates that the container is running correctly
     */
    @SerialName("healthy")
    HEALTHY,

    /**
     * Unhealthy indicates that the container has a problem
     */
    @SerialName("unhealthy")
    UNHEALTHY
}

@Serializable
data class Log(
    /**
     * Date and time at which this check started
     */
    @Serializable(DateIso8601Serializer::class)
    @SerialName("Start")
    val start: Date,

    /**
     * Date and time at which this check ended
     */
    @Serializable(DateIso8601Serializer::class)
    @SerialName("End")
    val end: Date,

    /**
     * ExitCode meanings:
     * * 0 - healthy
     * * 1 - unhealthy
     * * 2 - reserved (considered unhealthy)
     *
     * other values: error running probe
     */
    @SerialName("ExitCode")
    val exitCode: Int,

    /**
     * Output from last check
     */
    @SerialName("Output")
    val output: String,
)

@Serializable
enum class ContainerStateEnum {
    @SerialName("created")
    CREATED,

    @SerialName("running")
    RUNNING,

    @SerialName("paused")
    PAUSED,

    @SerialName("restarting")
    RESTARTING,

    @SerialName("removing")
    REMOVING,

    @SerialName("exited")
    EXITED,

    @SerialName("dead")
    DEAD,
}