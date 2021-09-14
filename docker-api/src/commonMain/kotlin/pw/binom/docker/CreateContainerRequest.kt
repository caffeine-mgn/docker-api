package pw.binom.docker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateContainerRequest(
    @SerialName("Hostname")
    val hostName: String? = null,
    @SerialName("Domainname")
    val domainName: String? = null,
    @SerialName("User")
    val user: String? = null,
    @SerialName("AttachStdin")
    val attachStdin: Boolean? = null,
    @SerialName("AttachStdout")
    val AttachStdout: Boolean? = null,
    @SerialName("AttachStderr")
    val attachStderr: Boolean? = null,
    @SerialName("Tty")
    val tty: Boolean? = null,
    @SerialName("OpenStdin")
    val openStdin: Boolean? = null,
    @SerialName("StdinOnce")
    val stdinOnce: Boolean? = null,

    @SerialName("Env")
    val env: List<Env> = emptyList(),

    @SerialName("Cmd")
    val cmd: List<String> = emptyList(),

    @SerialName("Entrypoint")
    val entrypoint: String? = null,

    @SerialName("Image")
    val image: String? = null,

    @SerialName("Labels")
    val labels: Map<String, String> = emptyMap(),

    @SerialName("Volumes")
    val volumes: Map<String, Map<String, String>> = emptyMap(),

    @SerialName("WorkingDir")
    val workingDir: String? = null,

    @SerialName("NetworkDisabled")
    val networkDisabled: Boolean? = null,

    @SerialName("MacAddress")
    val macAddress: String? = null,

    @SerialName("ExposedPorts")
    val exposedPorts: Map<String, Map<String, String>> = emptyMap(),
    @SerialName("StopSignal")
    val stopSignal: String? = null,
    @SerialName("StopTimeout")
    val stopTimeout: Int? = null,
    @SerialName("HostConfig")
    val hostConfig: HostConfig? = null,
    @SerialName("NetworkingConfig")
    val networkingConfig: NetworkingConfig? = null,
)

@Serializable
data class IPAMConfig(
    @SerialName("IPv4Address")
    val ipv4Address: String? = null,
    @SerialName("IPv6Address")
    val ipv6Address: String? = null,
    @SerialName("LinkLocalIPs")
    val linkLocalIPs: List<String>? = null,
)

@Serializable
data class HostConfig(
    @SerialName("Binds")
    val binds: List<String>? = null,

    @SerialName("Links")
    val links: List<String>? = null,

    @SerialName("Memory")
    val memory: Int? = null,

    @SerialName("MemorySwap")
    val memorySwap: Int? = null,

    @SerialName("MemoryReservation")
    val memoryReservation: Int? = null,

    @SerialName("KernelMemory")
    val kernelMemory: Int? = null,

    @SerialName("NanoCpus")
    val nanoCpus: Int? = null,

    @SerialName("MaximumIOps")
    val maximumIOps: Int? = null,

    @SerialName("MaximumIOBps")
    val maximumIOBps: Int? = null,

    @SerialName("BlkioWeight")
    val blkioWeight: Int? = null,

    @SerialName("BlkioWeightDevice")
    val blkioWeightDevice: List<Map<String, String>> = emptyList(),

    @SerialName("BlkioDeviceReadBps")
    val blkioDeviceReadBps: List<Map<String, String>> = emptyList(),

    @SerialName("BlkioDeviceWriteBps")
    val blkioDeviceWriteBps: List<Map<String, String>> = emptyList(),

    @SerialName("BlkioDeviceReadIOps")
    val blkioDeviceReadIOps: List<Map<String, String>> = emptyList(),

    @SerialName("BlkioDeviceWriteIOps")
    val blkioDeviceWriteIOps: List<Map<String, String>> = emptyList(),
    @SerialName("ContainerIDFile")
    val containerIDFile: String? = null,
    @SerialName("CpusetCpus")
    val cpusetCpus: String? = null,
    @SerialName("CpusetMems")
    val cpusetMems: String? = null,
    @SerialName("CpuPercent")
    val cpuPercent: Int? = null,
    @SerialName("CpuShares")
    val cpuShares: Int? = null,
    @SerialName("CpuPeriod")
    val cpuPeriod: Int? = null,
    @SerialName("CpuRealtimePeriod")
    val cpuRealtimePeriod: Int? = null,
    @SerialName("CpuRealtimeRuntime")
    val cpuRealtimeRuntime: Int? = null,
    @SerialName("Devices")
    val devices: List<String>? = null,
    @SerialName("DeviceRequests")
    val deviceRequests: List<Device>? = null,

    @SerialName("IpcMode")
    val ipcMode: String? = null,

    @SerialName("LxcConf")
    val lxcConf: List<String>? = null,



    @SerialName("OomKillDisable")
    val oomKillDisable: Boolean? = null,

    @SerialName("OomScoreAdj")
    val oomScoreAdj: Int? = null,

    @SerialName("NetworkMode")
    val networkMode: String? = null,

    @SerialName("PidMode")
    val pidMode: String? = null,

    @SerialName("PortBindings")
    val portBindings: Map<String, List<PortBind>>? = null,

    @SerialName("PublishAllPorts")
    val publishAllPorts: Boolean? = null,

    @SerialName("Privileged")
    val privileged: Boolean? = null,

    @SerialName("ReadonlyRootfs")
    val readonlyRootfs: Boolean? = null,

    @SerialName("Dns")
    val dns: List<String>? = null,

    @SerialName("DnsOptions")
    val dnsOptions: List<String>? = null,

    @SerialName("DnsSearch")
    val dnsSearch: List<String>? = null,

    @SerialName("VolumesFrom")
    val volumesFrom: List<String>? = null,

    @SerialName("CapAdd")
    val capAdd: List<String>? = null,

    @SerialName("GroupAdd")
    val groupAdd: List<String>? = null,

    @SerialName("RestartPolicy")
    val restartPolicy: RestartPolicy? = null,

    @SerialName("AutoRemove")
    val autoRemove: Boolean? = null,

    @SerialName("LogConfig")
    val logConfig: LogConfig? = null,

    @SerialName("Sysctls")
    val sysctls: Map<String, String>? = null,

    @SerialName("Ulimits")
    val ulimits: List<Map<String, String>>? = null,

    @SerialName("VolumeDriver")
    val volumeDriver: String? = null,

    @SerialName("ShmSize")
    val shmSize: Int? = null,
)

@Serializable
data class RestartPolicy(
    @SerialName("MaximumRetryCount")
    val maximumRetryCount: Int? = null,

    @SerialName("Name")
    val Name: String? = null,
)

@Serializable
data class LogConfig(
    @SerialName("Type")
    val type: String? = null,
)

@Serializable
data class PortBind(
    @SerialName("HostPort")
    val hostPort: String,
)

@Serializable
data class NetworkInterface(
    @SerialName("IPAMConfig")
    val ipAMConfig: IPAMConfig,
    @SerialName("Links")
    val links: List<String>,
    @SerialName("Aliases")
    val aliases: List<String>,
)

@Serializable
data class NetworkingConfig(
    @SerialName("EndpointsConfig")
    val endpointsConfig: Map<String, NetworkInterface>
)

@Serializable
data class Device(
    @SerialName("Driver")
    val driver: String,
    @SerialName("Count")
    val count: Int,
    @SerialName("DeviceIDs")
    val deviceIDs: List<String>,
    @SerialName("Capabilities")
    val capabilities: List<String>,
    @SerialName("Options")
    val options: Map<String, String>,
)