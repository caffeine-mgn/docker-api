package pw.binom.docker

import pw.binom.concurrency.DeadlineTimer
import pw.binom.io.httpClient.BaseHttpClient
import pw.binom.io.use
import pw.binom.network.NetworkDispatcher
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

class DockerClientTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun test2() {
        val nd = NetworkDispatcher()
        nd.runSingle {
            BaseHttpClient(nd).use { client ->
                val c = DockerClient(client)
                println("pull...")
                c.pullImage("tarantool/tarantool:2.8.2")
                println("pulled!")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        val nd = NetworkDispatcher()
        nd.runSingle {
            BaseHttpClient(nd).use { client ->
                val c = DockerClient(client)
                c.pullImage("tarantool/tarantool:2.8.2")
                c.pullImage("postgres:11")
                val cc = c.createContainer(
                    CreateContainerRequest(
                        image = "postgres:11",
                        env = listOf(
                            Env("POSTGRES_USER", "postgres"),
                            Env("POSTGRES_PASSWORD", "postgres"),
                            Env("POSTGRES_DB", "sellsystem")
                        ),
                        exposedPorts = mapOf("5432/tcp" to mapOf()),
                        hostConfig = HostConfig(
                            portBindings = mapOf(
                                "5432/tcp" to listOf(PortBind("8833"))
                            )
                        )
                    )
                )
                c.startContainer(cc.id)
                assertTrue(c.getContainers().any { it.id == cc.id })
                c.stopContainer(cc.id)
                c.remove(id = cc.id)
            }
        }
    }
}