package pw.binom.testcontainers

import pw.binom.io.AsyncCloseable
import pw.binom.io.bufferedWriter
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.network.NetworkManager
import pw.binom.network.TcpConnection
import pw.binom.network.tcpConnect

class RyukClient private constructor(private val connection: TcpConnection) : AsyncCloseable {
    companion object {
        suspend fun connect(address: DomainSocketAddress, networkManager: NetworkManager) =
            RyukClient(networkManager.tcpConnect(address.resolve()))
    }

    private val writer = connection.bufferedWriter()

    suspend fun registry(id: String?, labels: List<String>) {
        val sb = StringBuilder()
        if (id != null) {
            sb.append("id=").append(id)
        }
        labels.forEach { label ->
            if (sb.isNotEmpty()) {
                sb.append("&")
            }
            sb.append("label=").append(label)
        }
        sb.append("\n")
        writer.append(sb.toString())
        writer.flush()
    }

    override suspend fun asyncClose() {
        writer.asyncClose()
        connection.asyncClose()
    }
}