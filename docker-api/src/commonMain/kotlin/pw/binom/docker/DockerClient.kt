package pw.binom.docker

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.addHeader
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.URI
import pw.binom.net.toURI

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
            filters["filters"] = JsonArray(tags.map {
                JsonPrimitive(it)
            })
        }
        if (ids.isNotEmpty()) {
            filters["id"] = JsonArray(ids.map {
                JsonPrimitive(it)
            })
        }
        if (names.isNotEmpty()) {
            filters["name"] = JsonArray(names.map {
                JsonPrimitive(it)
            })
        }
        var uri = baseUrl.appendPath("containers/json")
            .appendQuery("all", !onlyRunning)
        if (filters.isNotEmpty()) {
            uri = uri.appendQuery("filters", json.encodeToString(JsonObject.serializer(), JsonObject(filters)))
        }

        return client.connect(method = HTTPMethod.GET.code, uri = uri).use {
            val txt = it.getResponse().readText().let { it.readText() }
            json.decodeFromString(ListSerializer(Container.serializer()), txt)
        }
    }

    suspend fun createContainer(arguments: CreateContainerRequest, name: String? = null): CreateContainerResponse {
        var uri = baseUrl.appendPath("containers/create")
        if (name != null) {
            uri = uri.appendQuery("name", name)
        }
        return client.connect(method = HTTPMethod.POST.code, uri = uri).use {
            it.addHeader(Headers.CONTENT_TYPE, "application/json")
            val r = it.writeText {
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
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/start")).use {
            val r = it.getResponse()
            val txt = r.readText().let { it.readText() }
            if (r.responseCode == 304) {
                throw RuntimeException("Containers $id already started")
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

    suspend fun stopContainer(id: String, stopTimeout: Int? = null) {
        require(stopTimeout == null || stopTimeout >= 0) { "stopTimeout should be more or equal 0" }

        var uri = baseUrl.appendPath("containers/$id/stop")
        if (stopTimeout != null) {
            uri = uri.appendQuery("t", stopTimeout)
        }

        client.connect(HTTPMethod.POST.code, uri).use {
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
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/pause")).use {
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
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/unpause")).use {
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
        client.connect(HTTPMethod.POST.code, baseUrl.appendPath("containers/$id/wait")).use {
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
            uri = uri.appendQuery("t", stopTimeout)
        }
        client.connect(HTTPMethod.POST.code, uri).use {
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

    suspend fun kill(id: String, signal: String? = null) {
        require(signal == null || signal.isNotEmpty()) { "stopTimeout should be more or equal 0" }
        var uri = "http://127.0.0.1:2375/containers/$id/kill".toURI()
        if (signal != null) {
            uri = uri.appendQuery("signal", signal)
        }
        client.connect(HTTPMethod.POST.code, uri).use {
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

    suspend fun rename(id: String, newName: String) {
        require(newName.isNotEmpty()) { "stopTimeout should be more or equal 0" }

        val uri = baseUrl.appendPath("containers/$id/rename")
            .appendQuery("name", newName)
        client.connect(HTTPMethod.POST.code, uri).use {
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

    suspend fun pullImage(image: String, tag: String? = null) {
        var uri = baseUrl.appendPath("images/create")
        uri = uri.appendQuery("fromImage", image)
        if (tag != null) {
            uri = uri.appendQuery("tag", tag)
        }

        client.connect(HTTPMethod.POST.code, uri).use {
            val r = it.getResponse()
            if (r.responseCode == 200) {
                return@use
            }
            val txt = r.readText().let { it.readText() }
            val obj = json.decodeFromString(ErrorResponse.serializer(), txt)
            if (r.responseCode == 404) {
                throw RuntimeException(obj.msg)
            }

            if (r.responseCode == 404) {
                throw RuntimeException(obj.msg)
            }
        }
    }

    /**
     * @param volumes Remove anonymous volumes associated with the container.
     * @param force If the container is running, kill it before removing it.
     * @param link Remove the specified link associated with the container.
     */
    suspend fun remove(id: String, volumes: Boolean? = null, force: Boolean? = null, link: Boolean? = null) {
        var uri = baseUrl.appendPath("containers/$id")
        if (volumes != null) {
            uri = uri.appendQuery("v", volumes)
        }
        if (force != null) {
            uri = uri.appendQuery("force", force)
        }
        if (link != null) {
            uri = uri.appendQuery("link", link)
        }
        client.connect(HTTPMethod.DELETE.code, uri).use {
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
}