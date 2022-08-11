package pw.binom.docker.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExecInstance(
    @SerialName("CanRemove")
    val canRemove: Boolean?,
    @SerialName("DetachKeys")
    val detachKeys: String?,
    @SerialName("ID")
    val id: String,
    @SerialName("Running")
    val running: Boolean,
    @SerialName("ExitCode")
    val exitCode: Int? = null,
    @SerialName("OpenStdin")
    val openStdin: Boolean,
    @SerialName("OpenStderr")
    val openStderr: Boolean,
    @SerialName("OpenStdout")
    val openStdout: Boolean,
    @SerialName("ContainerID")
    val containerID: String,
    @SerialName("Pid")
    val pid: Int,
    @SerialName("ProcessConfig")
    val processConfig: ProcessConfig
)
