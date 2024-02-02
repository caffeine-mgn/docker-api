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
import pw.binom.io.AsyncChannel
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.Headers
import pw.binom.io.http.headersOf
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.*
import pw.binom.io.use
import pw.binom.url.URI
import pw.binom.url.UrlEncoder
import pw.binom.url.toPath
import pw.binom.url.toURI
import kotlin.time.ExperimentalTime

class DockerClient(val client: HttpClient, val baseUrl: URI = "http://127.0.0.1:2375".toURI()) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun getContainers(
        onlyRunning: Boolean = false,
        ids: List<String> = emptyList(),
        names: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): List<Container> {
        val filters = HashMap<String, JsonElement>()
        if (tags.isNotEmpty()) {
            filters["filters"] = JsonArray(
                tags.map {
                    JsonPrimitive(it)
                }
            )
        }
        if (ids.isNotEmpty()) {
            filters["id"] = JsonArray(
                ids.map {
                    JsonPrimitive(it)
                }
            )
        }
        if (names.isNotEmpty()) {
            filters["name"] = JsonArray(
                names.map {
                    JsonPrimitive(it)
                }
            )
        }
        var uri = baseUrl.appendPath("containers/json".toPath).appendQuery("all", (!onlyRunning).toString())
        if (filters.isNotEmpty()) {
            uri = uri.appendQuery("filters", json.encodeToString(JsonObject.serializer(), JsonObject(filters)))
        }

        return client.connect(method = HTTPMethod.GET.code, uri = uri.toURL()).use {
            val txt = it.getResponse().readText().let { it.readText() }
            json.decodeFromString(ListSerializer(Container.serializer()), txt)
        }
    }

    suspend fun createContainer(arguments: CreateContainerRequest, name: String? = null): CreateContainerResponse {
        var uri = baseUrl.appendPath("containers/create".toPath)
        if (name != null) {
            uri = uri.appendQuery("name", name)
        }
        return client.connect(method = HTTPMethod.POST.code, uri = uri.toURL()).use {
            it.addHeader(Headers.CONTENT_TYPE, "application/json")
            val r = it.writeTextAndGetResponse {
                it.append(json.encodeToString(CreateContainerRequest.serializer(), arguments))
            }
            val txt = r.readText().let { it.readText() }
            try {
                if (r.responseCode != 201) {
                    val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                    throw CreateContainerException(obj.msg)
                }
            } catch (e: Throwable) {
                throw SerializationException("Can't parse \"$txt\" to ErrorResponse.", e)
            }
            json.decodeFromString(CreateContainerResponse.serializer(), txt)
        }
    }

    suspend fun startContainer(id: String) {
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/start".toPath).toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 304) {
                throw DockerException("Containers $id already started")
            }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw DockerException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun stopContainer(id: String, stopTimeout: Int? = null) {
        require(stopTimeout == null || stopTimeout >= 0) { "stopTimeout should be more or equal 0" }

        var uri = baseUrl.appendPath("containers/$id/stop".toPath)
        if (stopTimeout != null) {
            uri = uri.appendQuery("t", stopTimeout.toString())
        }

        client.connect(HTTPMethod.POST.code, uri.toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 304) {
                throw RuntimeException("Containers $id already stopped")
            }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun pauseContainer(id: String) {
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/pause".toPath).toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun unpauseContainer(id: String) {
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/unpause".toPath).toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun waitForStopContainer(id: String): WaitResponse {
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/wait".toPath).toURL()).use {
            val r = it.getResponse()
            if (r.responseCode == 200) {
                val txt = r.readText().let { it.readText() }
                return json.decodeFromString(WaitResponse.serializer(), txt)
            }

            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            val txt = r.readText().let { it.readText() }
            val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
            throw RuntimeException("Can't create container: ${obj.msg}")
        }
    }

    suspend fun restartContainer(id: String, stopTimeout: Int? = null) {
        require(stopTimeout == null || stopTimeout >= 0) { "stopTimeout should be more or equal 0" }
        var uri = "http://127.0.0.1:2375/containers/$id/restart".toURI()
        if (stopTimeout != null) {
            uri = uri.appendQuery("t", stopTimeout.toString())
        }
        client.connect(HTTPMethod.POST.code, uri.toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun killContainers(id: String, signal: String? = null) {
        require(signal == null || signal.isNotEmpty()) { "stopTimeout should be more or equal 0" }
        var uri = "http://127.0.0.1:2375/containers/$id/kill".toURI()
        if (signal != null) {
            uri = uri.appendQuery("signal", signal)
        }
        client.connect(HTTPMethod.POST.code, uri.toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode == 409) {
                throw RuntimeException("Containers $id is not running")
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun renameContainer(id: String, newName: String) {
        require(newName.isNotEmpty()) { "stopTimeout should be more or equal 0" }

        val uri = baseUrl.appendPath("containers/$id/rename".toPath).appendQuery("name", newName)
        client.connect(HTTPMethod.POST.code, uri.toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode == 409) {
                throw RuntimeException("Name $newName already in use")
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    suspend fun pullImageOfNotExist(image: String, tag: String? = null) {
        try {
            inspectImage(image)
        } catch (e: ImageNotFoundException) {
            pullImage(image = image, tag = tag)
        }
    }

    suspend fun pullImage(image: String, tag: String? = null) {
        var uri = baseUrl.appendPath("images/create".toPath)
        uri = uri.appendQuery("fromImage", image)
        if (tag != null) {
            uri = uri.appendQuery("tag", tag)
        }

        client.connect(HTTPMethod.POST.code, uri.toURL()).use {
            val r = it.getResponse()
            if (r.responseCode == 200) {
                r.readText().let { it.readText() }
                return@use
            }

            if (r.responseCode == 404) {
                throw RuntimeException("Can't find image \"$image\"")
            }
            val txt = r.readText().let { it.readText() }
            val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
            if (r.responseCode != 200) {
                throw RuntimeException(obj.msg)
            }
        }
    }

    /**
     * Return low-level information about a container.
     * @param id ID or name of the container
     * @param size Return the size of container as fields SizeRw and SizeRootFs
     */
    suspend fun inspectСontainer(id: String, size: Boolean = false): ContainerInfo {
        val uri = baseUrl.appendPath("containers/$id/json".toPath).appendQuery("size", size.toString())
        client.connect(
            method = HTTPMethod.GET.code,
            uri = uri.toURL()
        ).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode == 409) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
            }
            if (r.responseCode != 200) {
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
            uri = uri.toURL()
        ).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ImageNotFoundException(name)
            }
            if (r.responseCode == 409) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
            }
            if (r.responseCode != 200) {
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
    suspend fun removeContainer(id: String, volumes: Boolean? = null, force: Boolean? = null, link: Boolean? = null) {
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
        client.connect(HTTPMethod.DELETE.code, uri.toURL()).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ContainerNotFoundException(id)
            }
            if (r.responseCode == 409) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
            }
            if (r.responseCode != 204) {
                val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
                throw RuntimeException("Can't create container: ${obj.msg}")
            }
        }
    }

    /**
     * Run a command inside a running container.
     */
    suspend fun exec(id: String, args: ExecArgs) = client.connect(
        method = HTTPMethod.POST.code,
        uri = baseUrl.appendPath("containers/$id/exec".toPath).toURL()
    ).use {
        @Serializable
        data class ExecIdDto(@SerialName("Id") val id: String)
        it.setHeader(Headers.CONTENT_TYPE, "application/json")
        val r = it.writeTextAndGetResponse {
            it.append(json.encodeToString(ExecArgs.serializer(), args))
        }
        val txt = r.readAllText()
        if (r.responseCode == 404) {
            throw ContainerNotFoundException(id)
        }
        if (r.responseCode == 409) {
            val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
            throw RuntimeException("Can't create container. He has conflict: ${obj.msg}")
        }
        if (r.responseCode != 201) {
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
        stderr: Boolean = false
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
        return client.connectWebSocket(uri = uri.toURL()).start()
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
        raw: Boolean
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
        val r = client.connectTcp(method = HTTPMethod.POST.code, uri = uri.toURL())
        r.headers[Headers.CONTENT_TYPE] = "application/vnd.docker.raw-stream"
        return if (raw) {
            RawConsole(r.start())
        } else {
            FrameConsole(r.start())
        }
    }

    suspend fun startExec(
        id: ExecId,
        detach: Boolean,
        tty: Boolean? = null,
        raw: Boolean
    ): Console {
        val uri = baseUrl.appendPath("exec/${id.value}/start".toPath)

        @Serializable
        class ExecStartData(val Detach: Boolean, val Tty: Boolean? = null)

        val r = client.startConnect(
            method = HTTPMethod.POST.code,
            uri = uri.toURL(),
            headers = headersOf(Headers.CONTENT_TYPE to "application/json")
        )
        val resp = r.send(
            json.encodeToString(
                ExecStartData.serializer(),
                ExecStartData(Detach = detach, Tty = tty)
            )
        )

        if (resp.responseCode != 101 && resp.responseCode != 200) {
            throw DockerException("Invalid docker response code: ${resp.responseCode}")
        }
        val ct = resp.inputHeaders[Headers.CONTENT_TYPE]
        if (ct == null || ct.isEmpty()) {
            return DetachConsole
        }
        if (ct.size > 1) {
            throw DockerException("Docker returns several headers ${Headers.CONTENT_TYPE}: ${ct.joinToString()}")
        }
        if (ct.single() != "application/vnd.docker.raw-stream") {
            throw DockerException("Unknown ${Headers.CONTENT_TYPE}: ${ct.single()}")
        }
        val tcpChannel = AsyncChannel.create(
            input = r.input,
            output = r.output,
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
            uri = uri.toURL()
        ).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 404) {
                throw ExecNotFoundException(id)
            }
            if (r.responseCode != 200) {
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

    suspend fun waitContainerRunning(id: String) {
//        println("Wating health")
        while (true) {
            val c = inspectСontainer(id)
            if (c.state.status == ContainerStateEnum.RUNNING) {
                break
            }
//            println("health: ${c.state}")
            delay(500)
        }
    }
}
