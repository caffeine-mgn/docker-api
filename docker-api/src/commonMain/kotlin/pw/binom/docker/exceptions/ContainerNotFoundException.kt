package pw.binom.docker.exceptions

import pw.binom.docker.exceptions.DockerException

class ContainerNotFoundException(val id: String) : DockerException() {
    override val message: String
        get() = "Container $id not found"
}