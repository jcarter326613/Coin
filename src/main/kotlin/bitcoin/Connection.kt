package bitcoin

import bitcoin.messages.MessageHeader
import bitcoin.messages.VersionMessage
import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import util.Log
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.ZonedDateTime
import kotlin.random.Random

class Connection(
    val seed: String,
    val port: Int,
    messageCallback: (message: String, connection: Connection) -> Unit,
    disconnectionCallback: (connection: Connection) -> Unit,
) {
    val protocolVersion = 70015                                                             //https://developer.bitcoin.org/reference/p2p_networking.html#protocol-versions
    val servicesBitFlag = 1L                                                                //https://en.bitcoin.it/wiki/Protocol_documentation
    val sha256Hasher = MessageDigest.getInstance("SHA-256")

    private var _isReady = false
    val isReady: Boolean get() = _isReady || isClosed
    var isClosed = false
        private set

    lateinit var socket: Socket
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                socket = Socket(seed, port)
                inputStream = socket.getInputStream()
                outputStream = socket.getOutputStream()

                startStreamReader()
                sendVersionMessage()
            } catch (e: Throwable) {
                Log.error(e)
                isClosed = true
            }
        }
    }

    private fun startStreamReader() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                while (!socket.isInputShutdown) {
                    val header = readMessageHeader()
                    val payload = readMessagePayload(header.payloadLength)

                    if (headerIsValid(header, payload)) {
                        processIncomingMessage(header, payload)
                    }
                }
            } catch (e: Throwable) {
                Log.error("Exception from seed ($seed) and port ($port).  Disconnecting.}")
                Log.error(e)
                socket.close()
            } finally {
                isClosed = true
            }
        }
    }

    private fun headerIsValid(header: MessageHeader, payload: ByteArray): Boolean {
        return header.magic == messageHeaderStart &&
                headerChecksumIsValid(header.checksum, payload)
    }

    private fun headerChecksumIsValid(checksum: Int, payload: ByteArray): Boolean {
        synchronized(sha256Hasher) {
            val computedHash = calculateHeaderChecksum(payload)
            return checksum == computedHash
        }
    }

    private fun calculateHeaderChecksum(payload: ByteArray): Int {
        val actualHash = sha256Hasher.digest(sha256Hasher.digest(payload))
        val computedHash = ByteBuffer.allocate(4).get(actualHash, 0, 4).int
        return computedHash
    }

    private fun processIncomingMessage(header: MessageHeader, payload: ByteArray) {
        when (header.command) {

        }
    }

    private fun readMessagePayload(payloadSize: Int): ByteArray {
        val payload = ByteArray(payloadSize)
        var bytesRead = 0
        while (!socket.isInputShutdown && bytesRead < payloadSize) {
            val bytesToRead = inputStream.available()
            inputStream.readNBytes(payload, bytesRead, bytesToRead)
            bytesRead += bytesToRead
        }

        if (messageHeaderSize < bytesRead) {
            throw Exception("Read too many bytes")
        }

        return payload
    }

    private fun readMessageHeader(): MessageHeader {
        val header = ByteArray(messageHeaderSize)
        var bytesRead = 0
        while (!socket.isInputShutdown && bytesRead < messageHeaderSize) {
            val bytesToRead = inputStream.available()
            inputStream.readNBytes(header, bytesRead, bytesToRead)
            bytesRead += bytesToRead
        }

        if (messageHeaderSize < bytesRead) {
            throw Exception("Read too many bytes")
        }

        // Convert the bytes into a message header object
        return MessageHeader.fromByteArray(header)
    }

    private fun sendVersionMessage() {
        val message = VersionMessage(
            protocolVersion = protocolVersion,
            services = servicesBitFlag,
            timestamp = ZonedDateTime.now().toEpochSecond(),
            targetAddress = NetworkAddress(seed),
            sourceAddress = NetworkAddress("0.0.0.0"),
            nonce = Random.nextLong(),
            userAgent = VariableString(""),
            startHeight = BlockDb.instance.lastBlock,
            relay = true
        )

        val messageByteArray = message.toByteArray()
        val messageChecksum = calculateHeaderChecksum(messageByteArray)

        val header = MessageHeader(
            messageHeaderStart,
            "version",
            messageByteArray.size,
            messageChecksum
        )
        outputStream.write(header.toByteArray())
        outputStream.write(messageByteArray)
        outputStream.flush()
    }

    companion object {
        const val messageHeaderStart = 0x0b110907                                                       //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults
        val messageHeaderStartBytes = ByteBuffer.allocate(4).putInt(messageHeaderStart)   //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults
        const val messageHeaderSize = 4 + 12 + 4 + 4                                                    //https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
    }
}