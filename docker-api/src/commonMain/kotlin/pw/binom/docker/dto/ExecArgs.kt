package pw.binom.docker.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExecArgs(
    /**
     * Attach to stdin of the exec command.
     */
    @SerialName("AttachStdin")
    val attachStdin: Boolean? = null,

    /**
     * Attach to stdout of the exec command.
     */
    @SerialName("AttachStdout")
    val attachStdout: Boolean? = null,

    /**
     * Attach to stderr of the exec command.
     */
    @SerialName("AttachStderr")
    val attachStderr: Boolean? = null,

    /**
     * Override the key sequence for detaching a container. Format is a single character
     * `[a-Z]` or `ctrl-<value>` where `<value>` is one of: `a-z`, `@`, `^`, `[`, `,` or `_`.
     */
    @SerialName("DetachKeys")
    val detachKeys: String? = null,

    /**
     * Allocate a pseudo-TTY.
     */
    @SerialName("Tty")
    val tty: Boolean? = null,

    @SerialName("Env")
    val env: List<Env>? = null,

    @SerialName("Cmd")
    val cmd: List<String>? = null,

    /**
     * Default: `false`
     *
     * Runs the exec process with extended privileges.
     */
    @SerialName("Privileged")
    val privileged: Boolean? = null,

    /**
     * The user, and optionally, group to run the exec process inside the container. Format is one of: `user`, `user:group`, `uid`, or `uid:gid`.
     */
    @SerialName("User")
    val user: String? = null,

    /**
     * The working directory for the exec process inside the container.
     */
    @SerialName("WorkingDir")
    val workingDir: String? = null,
)