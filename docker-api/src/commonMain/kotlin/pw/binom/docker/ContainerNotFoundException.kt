package pw.binom.docker

class ContainerNotFoundException(val id: String) : DockerException() {
    override val message: String
        get() = "Container $id not found"
}