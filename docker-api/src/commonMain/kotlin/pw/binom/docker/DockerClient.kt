@file:OptIn(ExperimentalTime::class)

package pw.binom.docker

import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import pw.binom.docker.console.Console
import pw.binom.docker.console.DetachConsole
import pw.binom.docker.console.FrameConsole
import pw.binom.docker.console.RawConsole
import pw.binom.docker.dto.*
import pw.binom.docker.exceptions.ContainerNotFoundException
import pw.binom.docker.exceptions.CreateContainerException
import pw.binom.docker.exceptions.DockerException
import pw.binom.docker.exceptions.ExecNotFoundException
import pw.binom.http.client.Http11ClientExchange
import pw.binom.http.client.HttpClientRunnable
import pw.binom.http.client.tcpRequest
import pw.binom.http.client.wsRequest
import pw.binom.io.AsyncChannel
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.Headers
import pw.binom.io.http.HttpContentLength
import pw.binom.io.http.headersOf
import pw.binom.io.http.httpContentLength
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.useAsync
import pw.binom.url.URI
import pw.binom.url.UrlEncoder
import pw.binom.url.toPath
import pw.binom.url.toURI
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime

class DockerClient(val client: HttpClientRunnable, val baseUrl: URI = "http://127.0.0.1:2375".toURI()) {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    suspend fun getContainers(
        onlyRunning: Boolean = false,
        ids: List<String> = emptyList(),
        names: List<String> = emptyList(),
        tags: List<String> = emptyList(),
    ): List<Container> {
        val filters = HashMap<String, JsonElement>()
        if (tags.isNotEmpty()) {
            filters["filters"] =
                JsonArray(
                    tags.map {
                        JsonPrimitive(it)
                    },
                )
        }
        if (ids.isNotEmpty()) {
            filters["id"] =
                JsonArray(
                    ids.map {
                        JsonPrimitive(it)
                    },
                )
        }
        if (names.isNotEmpty()) {
            filters["name"] =
                JsonArray(
                    names.map {
                        JsonPrimitive(it)
                    },
                )
        }
        var uri = baseUrl.appendPath("containers/json".toPath).appendQuery("all", (!onlyRunning).toString())
        if (filters.isNotEmpty()) {
            uri = uri.appendQuery("filters", json.encodeToString(JsonObject.serializer(), JsonObject(filters)))
        }
        val response = client.connect(method = HTTPMethod.GET.code, url = uri.toURL(), headers = headersOf()).useAsync {
            it.readAllText()

        }
        return json.decodeFromString(ListSerializer(Container.serializer()), response)
    }

    suspend fun createContainer(
        arguments: CreateContainerRequest,
        name: String? = null,
    ): CreateContainerResponse {
        var uri = baseUrl.appendPath("containers/create".toPath)
        if (name != null) {
            uri = uri.appendQuery("name", name)
        }
        return client.request(
            method = HTTPMethod.POST.code,
            url = uri.toURL(),
        ).also {
            it.headers.contentType = "application/json"
            it.headers.httpContentLength = HttpContentLength.CHUNKED
        }.connect().useAsync {
            it.sendText(json.encodeToString(CreateContainerRequest.serializer(), arguments))
            val txt = it.readAllText()
            if (it.getResponseCode() != 200) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw CreateContainerException(obj.msg)
            }
            json.decodeFromString(CreateContainerResponse.serializer(), txt)
        }
    }

    suspend fun startContainer(id: String) {
        client.connect(
            method = HTTPMethod.POST.code,
            url = baseUrl.appendPath("containers/$id/start".toPath).toURL(),
            headers = headersOf()
        ).useAsync {
            if (it.getResponseCode() == 204) {
                return
            }
            if (it.getResponseCode() == 304) {
                throw DockerException("Containers $id already started")
            }
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            val txt = it.readAllText()
            val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
            throw DockerException("Can't create container: ${obj.msg}")
        }
    }

    suspend fun stopContainer(
        id: String,
        stopTimeout: Int? = null,
    ) {
        require(stopTimeout == null || stopTimeout >= 0) { "stopTimeout should be more or equal 0" }

        var uri = baseUrl.appendPath("containers/$id/stop".toPath)
        if (stopTimeout != null) {
            uri = uri.appendQuery("t", stopTimeout.toString())
        }

        client.connect(HTTPMethod.POST.code, uri.toURL()).useAsync {
            if (it.getResponseCode() == 304) {
                throw RuntimeException("Containers $id already stopped")
            }
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            if (it.getResponseCode() != 204) {
                val txt = it.readAllText()
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun pauseContainer(id: String) {
        client.connect(
            method = HTTPMethod.POST.code,
            url = baseUrl.appendPath("containers/$id/pause".toPath).toURL(),
            headers = headersOf()
        )
            .useAsync {
                val txt = it.readAllText()
                if (it.getResponseCode() == 404) {
                    throw ContainerNotFoundException(id)
                }
                if (it.getResponseCode() != 204) {
                    val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                    throw RuntimeException("Can't create container: ${obj.msg}")
                }
            }
    }

    suspend fun unpauseContainer(id: String) {
        client.connect(
            method = HTTPMethod.POST.code,
            url = baseUrl.appendPath("containers/$id/unpause".toPath).toURL(),
        ).useAsync {
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            if (it.getResponseCode() != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun waitForStopContainer(id: String): WaitResponse {
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/wait".toPath).toURL())
            .useAsync {
                if (it.getResponseCode() == 200) {
                    val txt = it.readAllText()
                    return json.decodeFromString(WaitResponse.serializer(), txt)
                }

                if (it.getResponseCode() == 404) {
                    throw ContainerNotFoundException(id)
                }
                val txt = it.readAllText()
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
    }

    suspend fun restartContainer(
        id: String,
        stopTimeout: Int? = null,
    ) {
        require(stopTimeout == null || stopTimeout >= 0) { "stopTimeout should be more or equal 0" }
        var uri = "http://127.0.0.1:2375/containers/$id/restart".toURI()
        if (stopTimeout != null) {
            uri = uri.appendQuery("t", stopTimeout.toString())
        }
        client.connect(HTTPMethod.POST.code, uri.toURL()).useAsync {
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            if (it.getResponseCode() != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun killContainers(
        id: String,
        signal: String? = null,
    ) {
        require(signal == null || signal.isNotEmpty()) { "stopTimeout should be more or equal 0" }
        var uri = "http://127.0.0.1:2375/containers/$id/kill".toURI()
        if (signal != null) {
            uri = uri.appendQuery("signal", signal)
        }
        client.connect(HTTPMethod.POST.code, uri.toURL()).useAsync { it ->
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            if (it.getResponseCode() == 409) {
                throw RuntimeException("Containers $id is not running")
            }
            if (it.getResponseCode() != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun renameContainer(
        id: String,
        newName: String,
    ) {
        require(newName.isNotEmpty()) { "stopTimeout should be more or equal 0" }

        val uri = baseUrl.appendPath("containers/$id/rename".toPath).appendQuery("name", newName)
        client.connect(HTTPMethod.POST.code, uri.toURL()).useAsync {
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            if (it.getResponseCode() == 409) {
                throw RuntimeException("Name $newName already in use")
            }
            if (it.getResponseCode() != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun pullImageOfNotExist(
        image: String,
        tag: String? = null,
    ) {
        try {
            inspectImage(image)
        } catch (_: ImageNotFoundException) {
            pullImage(image = image, tag = tag)
        }
    }

    suspend fun pullImage(
        image: String,
        tag: String? = null,
    ) {
        var uri = baseUrl.appendPath("images/create".toPath)
        uri = uri.appendQuery("fromImage", image)
        if (tag != null) {
            uri = uri.appendQuery("tag", tag)
        }

        client.connect(HTTPMethod.POST.code, uri.toURL())
            .useAsync {
                if (it.getResponseCode() == 200) {
                    it.readAllText()
                    return@useAsync
                }

                if (it.getResponseCode() == 404) {
                    throw RuntimeException("Can't find image \"$image\"")
                }
                val txt = it.readAllText()
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                if (it.getResponseCode() != 200) {
                    throw RuntimeException(obj.msg)
                }
            }
    }

    /**
     * Return low-level information about a container.
     * @param id ID or name of the container
     * @param size Return the size of container as fields SizeRw and SizeRootFs
     */
    suspend fun inspectСontainer(
        id: String,
        size: Boolean = false,
    ): ContainerInfo {
        val uri = baseUrl.appendPath("containers/$id/json".toPath).appendQuery("size", size.toString())
        client.connect(
            method = HTTPMethod.GET.code,
            url = uri.toURL(),
        ).useAsync {
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            if (it.getResponseCode() == 409) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
            }
            if (it.getResponseCode() != 200) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't inspect a container: ${obj.msg}")
            }
            try {
                return json.decodeFromString(ContainerInfo.serializer(), txt)
            } catch (e: SerializationException) {
                throw RuntimeException("Can't parse response \"$txt\"", e)
            }
        }
    }

    /**
     * Return low-level information about a container.
     * @param id ID or name of the container
     * @param size Return the size of container as fields SizeRw and SizeRootFs
     */
    suspend fun inspectImage(name: String): ImageInfo {
        val uri =
            baseUrl.appendPath("images".toPath).appendPath(UrlEncoder.encode(name).toPath).appendPath("json".toPath)
        client.connect(
            method = HTTPMethod.GET.code,
            url = uri.toURL(),
        ).useAsync {
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ImageNotFoundException(name)
            }
            if (it.getResponseCode() == 409) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
            }
            if (it.getResponseCode() != 200) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't inspect a container: ${obj.msg}")
            }
            try {
                return json.decodeFromString(ImageInfo.serializer(), txt)
            } catch (e: SerializationException) {
                throw RuntimeException("Can't parse response \"$txt\"", e)
            }
        }
    }

    /**
     * @param volumes Remove anonymous volumes associated with the container.
     * @param force If the container is running, kill it before removing it.
     * @param link Remove the specified link associated with the container.
     */
    suspend fun removeContainer(
        id: String,
        volumes: Boolean? = null,
        force: Boolean? = null,
        link: Boolean? = null,
    ) {
        var uri = baseUrl.appendPath("containers/$id".toPath)
        if (volumes != null) {
            uri = uri.appendQuery("v", volumes.toString())
        }
        if (force != null) {
            uri = uri.appendQuery("force", force.toString())
        }
        if (link != null) {
            uri = uri.appendQuery("link", link.toString())
        }
        client.connect(HTTPMethod.DELETE.code, uri.toURL()).useAsync {
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ContainerNotFoundException(id)
            }
            if (it.getResponseCode() == 409) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
            }
            if (it.getResponseCode() != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    /**
     * Run a command inside a running container.
     */
    suspend fun exec(
        id: String,
        args: ExecArgs,
    ) = client.request(
        method = HTTPMethod.POST.code,
        url = baseUrl.appendPath("containers/$id/exec".toPath).toURL(),
    ).also {
        it.headers.contentType = "application/json"
        it.headers.httpContentLength = HttpContentLength.CHUNKED
    }.connect().useAsync {
        @Serializable
        data class ExecIdDto(
            @SerialName("Id") val id: String,
        )
        it.sendText(json.encodeToString(ExecArgs.serializer(), args))
        if (it.getResponseCode() == 404) {
            throw ContainerNotFoundException(id)
        }
        val txt = it.readAllText()
        if (it.getResponseCode() == 409) {
            val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
            throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
        }
        if (it.getResponseCode() != 201) {
            val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
            throw RuntimeException("Can't execute: ${obj.msg}")
        }
        ExecId(json.decodeFromString(ExecIdDto.serializer(), txt).id)
    }

    /**
     * Attach to a container
     * @param id ID or name of the container
     * @param logs Return logs
     * @param stream Return stream
     * @param stdin Attach to stdin
     * @param stdout Attach to stdout
     * @param stderr Attach to stderr
     */
    @Deprecated("Not worked yet")
    suspend fun attachToContainer(
        id: String,
        logs: Boolean = false,
        stream: Boolean = false,
        stdin: Boolean = false,
        stdout: Boolean = false,
        stderr: Boolean = false,
    ): WebSocketConnection {
        var uri = baseUrl.appendPath("containers/$id/attach/ws".toPath)
        if (logs) {
            uri = uri.appendQuery("logs", "1")
        }
        if (stream) {
            uri = uri.appendQuery("stream", "1")
        }
        if (stdin) {
            uri = uri.appendQuery("stdin", "1")
        }
        if (stdout) {
            uri = uri.appendQuery("stdout", "1")
        }
        if (stderr) {
            uri = uri.appendQuery("stderr", "1")
        }
        return client.wsRequest(
            url = uri.toURL()
        ).connect()
    }

    /**
     * Attach to a container
     * @param id ID or name of the container
     * @param logs Return logs
     * @param stream Return stream
     * @param stdin Attach to stdin
     * @param stdout Attach to stdout
     * @param stderr Attach to stderr
     */
    @Deprecated("Not worked yet")
    suspend fun attachToContainer2(
        id: String,
        logs: Boolean = false,
        stream: Boolean = false,
        stdin: Boolean = false,
        stdout: Boolean = false,
        stderr: Boolean = false,
        raw: Boolean,
    ): Console {
        var uri = baseUrl.appendPath("containers/$id/attach".toPath)
        if (logs) {
            uri = uri.appendQuery("logs", "1")
        }
        if (stream) {
            uri = uri.appendQuery("stream", "1")
        }
        if (stdin) {
            uri = uri.appendQuery("stdin", "1")
        }
        if (stdout) {
            uri = uri.appendQuery("stdout", "1")
        }
        if (stderr) {
            uri = uri.appendQuery("stderr", "1")
        }
        val r = client.tcpRequest(method = HTTPMethod.POST.code, url = uri.toURL())
        r.headers[Headers.CONTENT_TYPE] = "application/vnd.docker.raw-stream"
        return if (raw) {
            RawConsole(r.connect())
        } else {
            FrameConsole(r.connect())
        }
    }

    suspend fun startExec(
        id: ExecId,
        detach: Boolean,
        tty: Boolean? = null,
        raw: Boolean,
    ): Console {
        val uri = baseUrl.appendPath("exec/${id.value}/start".toPath)

        @Serializable
        data class ExecStartData(@SerialName("Detach") val detach: Boolean, @SerialName("Tty") val tty: Boolean? = null)

        val r =
            client.request(
                method = HTTPMethod.POST.code,
                url = uri.toURL(),
            ).also {
                it.headers.contentType = "application/json"
                it.headers.httpContentLength = HttpContentLength.CHUNKED
            }.connect() as Http11ClientExchange
        r.sendText(
            json.encodeToString(
                ExecStartData.serializer(),
                ExecStartData(detach = detach, tty = tty),
            ),
        )
        if (r.getResponseCode() != 101 && r.getResponseCode() != 200) {
            throw DockerException("Invalid docker response code: ${r.getResponseCode()}")
        }
        val ct = r.getResponseHeaders()[Headers.CONTENT_TYPE]
        if (ct == null || ct.isEmpty()) {
            return DetachConsole
        }
        if (ct.size > 1) {
            throw DockerException("Docker returns several headers ${Headers.CONTENT_TYPE}: ${ct.joinToString()}")
        }
        if (ct.single() != "application/vnd.docker.raw-stream") {
            throw DockerException("Unknown ${Headers.CONTENT_TYPE}: ${ct.single()}")
        }

        val tcpChannel =
            AsyncChannel.create(
                input = r.getInput(),
                output = r.getOutput(),
            )
        return if (raw) {
            RawConsole(tcpChannel)
        } else {
            FrameConsole(tcpChannel)
        }
    }

    suspend fun inspectExec(id: ExecId): ExecInstance {
        val uri =
            baseUrl.appendPath("exec".toPath).appendPath(UrlEncoder.encode(id.value).toPath).appendPath("json".toPath)
        client.connect(
            method = HTTPMethod.GET.code,
            url = uri.toURL(),
        ).useAsync {
            val txt = it.readAllText()
            if (it.getResponseCode() == 404) {
                throw ExecNotFoundException(id)
            }
            if (it.getResponseCode() != 200) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't inspect a exec: ${obj.msg}")
            }
            try {
                return json.decodeFromString(ExecInstance.serializer(), txt)
            } catch (e: SerializationException) {
                throw RuntimeException("Can't parse response \"$txt\"", e)
            }
        }
    }

    suspend fun waitContainerRunning(id: String, timeout: Duration) {
//        println("Wating health")
        val now = TimeSource.Monotonic.markNow()
        while (true) {
            if (!timeout.isInfinite() && now.elapsedNow() > timeout) {
                throw IllegalStateException("Timeout waiting for start container with ID $id")
            }
            val c = inspectСontainer(id)
            if (c.state.status == ContainerStateEnum.RUNNING) {
                break
            }
//            println("health: ${c.state}")
            delay(500)
        }
    }
}
