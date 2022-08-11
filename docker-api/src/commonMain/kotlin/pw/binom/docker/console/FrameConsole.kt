package pw.binom.docker.console

import pw.binom.concurrency.AsyncReentrantLock
import pw.binom.io.*
import pw.binom.readInt
import pw.binom.writeInt

class FrameConsole(val channel: AsyncChannel) : Console {
    private val lock = AsyncReentrantLock()
    private val reader = channel.bufferedInput(closeParent = false)
    private val writer = channel.bufferedOutput(closeStream = false)
    private val packageBuffer = ByteBuffer.alloc(8)

    private val textFrame = object : TextFrame {
        override var data: String = ""
        override var streamType: StreamType = StreamType.STDIN
    }

    private val binaryFrame = object : BinaryFrame {
        override val data: ByteBuffer
            get() = internalData ?: throw IllegalStateException("data not defined")
        var internalData: ByteBuffer? = null
        override var streamType: StreamType = StreamType.STDIN
    }

    private fun throwIsNotFramedStream(): Nothing = throw IllegalArgumentException("Stream is not framed stream")
    private suspend fun <T> readFrame(func: (StreamType, data: ByteBuffer) -> T): T =
        lock.synchronize {
            packageBuffer.clear()
            reader.readFully(packageBuffer)
            packageBuffer.flip()
            val stdTypeNum = packageBuffer.getByte()
            if (stdTypeNum !in 0.toByte()..2.toByte()) {
                throwIsNotFramedStream()
            }
            val streamType = StreamType.getType(stdTypeNum)
            repeat(3) {
                if (packageBuffer.getByte() != 0.toByte()) {
                    throwIsNotFramedStream()
                }
            }
            val packageSize = packageBuffer.readInt()
            return@synchronize ByteBuffer.alloc(packageSize).use {
                reader.readFully(it)
                it.flip()
                func(streamType, it)
            }
        }

    suspend fun <T> readBinaryFrame(func: (BinaryFrame) -> T): T =
        readFrame { s, d ->
            binaryFrame.internalData = d
            binaryFrame.streamType = s
            func(binaryFrame)
        }

    suspend fun <T> readTextFrame(func: (TextFrame) -> T): T =
        readFrame { s, d ->
            textFrame.data = d.toByteArray().decodeToString()
            textFrame.streamType = s
            func(textFrame)
        }

    private fun makeFrameHeader(streamType: StreamType, dataSize: Int) {
        packageBuffer.clear()
        packageBuffer.put(streamType.code)
        repeat(3) {
            packageBuffer.put(0.toByte())
        }
        packageBuffer.writeInt(dataSize)
        packageBuffer.flip()
    }

    suspend fun writeBinary(data: ByteBuffer) {
        lock.synchronize {
            makeFrameHeader(StreamType.STDIN, data.remaining)
            writer.writeFully(packageBuffer)
            writer.writeFully(data)
            writer.flush()
        }
    }

    suspend fun writeText(data: String) {
        val dataBytes = data.encodeToByteArray()
        ByteBuffer.wrap(dataBytes).use { buf ->
            writeBinary(buf)
        }
    }

    sealed interface Frame {
        val streamType: StreamType
    }

    interface BinaryFrame : Frame {
        val data: ByteBuffer
    }

    interface TextFrame : Frame {
        val data: String
    }

    enum class StreamType(val code: Byte) {
        STDIN(0),
        STDOUT(1),
        STDERR(2);

        companion object {
            fun getType(num: Byte) =
                when (num) {
                    0.toByte() -> STDIN
                    1.toByte() -> STDOUT
                    2.toByte() -> STDERR
                    else -> throw IllegalArgumentException("Can't find stream type. Stream Type is $num")
                }
        }
    }

    override suspend fun asyncClose() {
        reader.asyncClose()
        writer.asyncClose()
        packageBuffer.close()
        channel.asyncClose()
    }
}
