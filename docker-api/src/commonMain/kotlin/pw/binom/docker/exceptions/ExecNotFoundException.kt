package pw.binom.docker.exceptions

import pw.binom.docker.dto.ExecId

class ExecNotFoundException(val id: ExecId) : DockerException() {
    override val message: String
        get() = "Exec instance ${id.value} not found"
}
