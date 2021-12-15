package pw.binom.docker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pw.binom.io.httpClient.HttpClient
import pw.binom.network.NetworkCoroutineDispatcher
import pw.binom.concurrency.sleep
import pw.binom.docker.console.FrameConsole
import pw.binom.docker.dto.*
import pw.binom.io.*
import pw.binom.io.httpClient.create
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

fun runBlockingAnyway(func: suspend CoroutineScope.() -> Unit) {
    var exception: Throwable? = null
    val job = GlobalScope.launch {
        try {
            func()
        } catch (e: Throwable) {
            exception = e
        }
    }

    while (job.isActive) {
        sleep(10)
    }
    if (exception != null) {
        throw exception!!
    }
}

class DockerClientTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun test2() {
        val nd = NetworkCoroutineDispatcher.create()
        runBlockingAnyway {
            HttpClient.create(
                networkDispatcher = nd,
                useKeepAlive = true,
            ).use { client ->
                val c = DockerClient(client)
                repeat(10) {
                    c.pullImage("tarantool/tarantool:2.8.2")
                }
                println("pulled!")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        runBlockingAnyway {
            HttpClient.create(
                useKeepAlive = true,
            ).use { client ->
                val c = DockerClient(client)
                c.pullImageOfNotExist("tarantool/tarantool:2.8.2")
                c.pullImageOfNotExist("postgres:11")
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
                        ),
                        attachStderr = true,
                        attachStdin = false,
                        attachStdout = true,
                        openStdin = false,
                        tty = false,
                        stdinOnce = false,
                    )
                )
                c.startContainer(cc.id)
                c.waitContainerRunning(cc.id)

                while (true) {
                    val execId = c.exec(
                        cc.id, ExecArgs(
                            attachStdin = false,
                            attachStdout = true,
                            attachStderr = true,
                            tty = false,
//                    cmd = listOf("/bin/bash"),
                            cmd = listOf("pg_isready"),
//                        cmd = listOf("sleep", "10"),
                        )
                    )
                    println("execId=$execId")
                    c.startExec(
                        id = execId,
                        detach = false,
                        tty = false,
                        raw = false
                    ).use { console ->
                        if (console is FrameConsole) {
                            console.readTextFrame {
                                println("->${it.streamType}: ${it.data}")
                            }
                        }
                    }
                    val ccc = c.inspectExec(execId)
                    println("info:$ccc")
                    if (ccc.exitCode != 0) {
                        println("postgres not started yet")
                    } else {
                        println("postgres started")
                        break
                    }
                    delay(1000)
                }
//                c.attachToContainer2(
//                    id = execId,
//                    logs = false,
//                    stream = true,
//                    stdin = true,
//                    stdout = true,
//                    stderr = true
//                ).use { console ->
//                    console.writeText("ls -l")
//                    console.readBinary { t, d ->
//                        println("Что-то прочитанно")
//                    }
//                }
                assertTrue(c.getContainers().any { it.id == cc.id })
                c.stopContainer(cc.id)
                c.removeContainer(id = cc.id)
            }
        }
    }
}