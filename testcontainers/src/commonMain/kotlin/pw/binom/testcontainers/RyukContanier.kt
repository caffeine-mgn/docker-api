package pw.binom.testcontainers

import pw.binom.docker.DockerClient
import pw.binom.docker.dto.CreateContainerRequest
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.network.NetworkManager
import pw.binom.network.TcpServerConnection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object RyukContanier {
    suspend fun create(
        client: DockerClient,
        image: String = "testcontainers/ryuk",
        networkManager: NetworkManager,
        pathToDockerSocket: String,
        startTimeout: Duration = 1.minutes,
    ): RyukClient {
        val tcpPort = TcpServerConnection.randomPort()
        client.pullImageOfNotExist(
            image = image,
        )
        val container = client.createContainer(
            arguments = CreateContainerRequest(
                labels = mapOf(),
                image = "testcontainers/ryuk",
                exposedPorts = mapOf(
                    "eee" to mapOf(
                        "$tcpPort" to "8080"
                    )
                ),
                volumes = mapOf(
                    "docker" to mapOf(pathToDockerSocket to "/var/run/docker.sock")
                )
            )
        )
        client.startContainer(container.id)
        client.waitContainerRunning(id = container.id, timeout = startTimeout)
        return RyukClient.connect(
            address = DomainSocketAddress(host = "127.0.0.1", port = tcpPort),
            networkManager = networkManager,
        )
    }
}