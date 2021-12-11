package pw.binom.docker.dto

import pw.binom.docker.exceptions.DockerException

class ImageNotFoundException(val id: String) : DockerException() {
    override val message: String
        get() = "Image $id not found"
}