package pw.binom.docker.exceptions

class ContainerNotFoundException(val id: String) : DockerException() {
    override val message: String
        get() = "Container $id not found"
}
