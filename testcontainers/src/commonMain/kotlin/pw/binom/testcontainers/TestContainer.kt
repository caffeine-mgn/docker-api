package pw.binom.testcontainers

import pw.binom.docker.DockerClient
import pw.binom.docker.dto.CreateContainerRequest

class TestContainer {
    companion object {
        suspend fun create(
            client: DockerClient,
            config: CreateContainerRequest,
            image: String,
            ports: Map<String, String>,
            volumes: Map<String, String>,
        ) {
            client.pullImageOfNotExist(image)
            val e = client.createContainer(config)

        }
    }
}