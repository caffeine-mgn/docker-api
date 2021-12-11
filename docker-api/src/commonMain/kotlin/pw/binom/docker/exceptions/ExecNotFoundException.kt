package pw.binom.docker.exceptions

class ExecNotFoundException(val id: String) : DockerException() {
    override val message: String
        get() = "Exec instance $id not found"
}