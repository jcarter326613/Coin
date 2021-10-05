package bitcoin.network

import bitcoin.chain.BlockDb
import bitcoin.messages.*
import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.NetworkAddress.Companion.convertAddressToByteArray
import bitcoin.messages.components.NetworkAddress.Companion.convertByteArrayToAddress
import bitcoin.messages.components.VariableString
import kotlinx.coroutines.*
import util.Log
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.ZonedDateTime
import kotlin.random.Random

class Connection(
    val addr: NetworkAddress,
    lastBlockHeight: Int,
    private val messageProcessor: IMessageProcessor,
    private val disconnectHandler: ((c: Connection) -> Unit)
) {
    var protocolVersion = minimumProtocolVersion
    val sha256Hasher = MessageDigest.getInstance("SHA-256")

    private var _isReady = false
    val isReady: Boolean get() = _isReady || isClosed
    var isClosed = false
        private set(value) {
            field = value
            disconnectHandler(this)
        }
    private var suppressDisconnectNotification: Boolean = false

    val isTimedOut: Boolean
        get() {
            val nowSeconds = ZonedDateTime.now().toEpochSecond()
            val lastMessageReceiveTime = lastMessageReceiveTime
            return (lastMessageReceiveTime == null && nowSeconds - creationTime.toEpochSecond() > initialSetupTimeoutSeconds) ||
                    (lastMessageReceiveTime != null && nowSeconds - lastMessageReceiveTime.toEpochSecond() > timeoutSeconds)
        }

    var address: NetworkAddress? = null

    lateinit var socket: Socket
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream

    private val creationTime = ZonedDateTime.now()
    private var lastMessageReceiveTime: ZonedDateTime? = null
    private var lastPingNonce: Long? = null

    init {
        Log.info("Connecting to $addr")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                socket = Socket(convertByteArrayToAddress(addr.address), addr.port.toInt())
                inputStream = socket.getInputStream()
                outputStream = socket.getOutputStream()

                startStreamReader()
                sendVersionMessage(lastBlockHeight)
            } catch (e: Throwable) {
                Log.error(e)
                isClosed = true
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        val otherConnection = other as? Connection ?: return false
        return otherConnection.addr == addr
    }

    override fun hashCode(): Int {
        return addr.hashCode()
    }

    fun close() {
        isClosed = true
        try {
            if (!socket.isClosed) {
                socket.close()
            }
        } catch (e: Throwable) {
        }
    }

    fun sendVerack() {
        val messageByteArray = "".toByteArray()
        val messageChecksum = calculateHeaderChecksum(messageByteArray)

        val header = MessageHeader(
            messageHeaderStart,
            "verack",
            messageByteArray.size,
            messageChecksum
        )
        val headerByteArray = header.toByteArray()

        outputStream.write(headerByteArray)
        outputStream.flush()
    }

    fun sendMessage(message: IMessage) {
        sendMessageBytes(message.name, message.toByteArray())
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
                Log.error("Exception from address $addr.  Disconnecting.}")
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

    private fun headerChecksumIsValid(checksum: ByteArray, payload: ByteArray): Boolean {
        synchronized(sha256Hasher) {
            val computedHash = calculateHeaderChecksum(payload)
            return checksum.contentEquals(computedHash)
        }
    }

    private fun calculateHeaderChecksum(payload: ByteArray): ByteArray {
        val actualHash = sha256Hasher.digest(sha256Hasher.digest(payload))
        val computedHash = ByteBuffer.allocate(4)
        for (i in 0 until 4) {
            computedHash.put(actualHash[i])
        }
        return computedHash.array()
    }

    private fun processIncomingMessage(header: MessageHeader, payload: ByteArray) {
        if (isClosed) return
        lastMessageReceiveTime = ZonedDateTime.now()

        when (header.command) {
            "version" -> {
                val versionMessage = VersionMessage.fromByteArray(payload)
                address = versionMessage.sourceAddress
                messageProcessor.processIncomingMessageVersion(versionMessage, this)
            }
            "verack" -> processIncomingVerack()
            "ping" -> processIncomingPing(PingMessage.fromByteArray(payload))
            "pong" -> processIncomingPong(PongMessage.fromByteArray(payload))
            "addr" -> messageProcessor.processIncomingMessageAddr(AddrMessage.fromByteArray(payload), this)
            "inv" -> messageProcessor.processIncomingMessageInv(InvMessage.fromByteArray(payload), this)
            "reject" -> messageProcessor.processIncomingMessageReject(RejectMessage.fromByteArray(payload), this)
            "block" -> messageProcessor.processIncomingMessageBlock(BlockMessage.fromByteArray(payload), this)
            else -> Log.info("Received command ${header.command} but don't know how to process")
        }
    }

    private fun processIncomingVerack() {
        _isReady = true

        // Start up the ping cycle
        GlobalScope.launch(Dispatchers.IO) {
            delay(millisecondsBetweenPing)
            while (!isClosed) {
                val elapsedTimeSinceLastMessage = lastMessageReceiveTime?.let {
                    (ZonedDateTime.now().toEpochSecond() - it.toEpochSecond()) * 1000
                } ?: millisecondsBetweenPing
                val nextWaitTime = if (elapsedTimeSinceLastMessage >= millisecondsBetweenPing) {
                    val lastPingNonce = Random.nextLong()
                    sendMessageBytes("ping", PingMessage(lastPingNonce).toByteArray())
                    this@Connection.lastPingNonce = lastPingNonce
                    millisecondsBetweenPing
                } else {
                    millisecondsBetweenPing - elapsedTimeSinceLastMessage
                }
                delay(nextWaitTime)
            }
        }
    }

    private fun processIncomingPing(message: PingMessage) {
        val toSend = PongMessage(nonce = message.nonce)
        sendMessageBytes("pong", toSend.toByteArray())
    }

    private fun processIncomingPong(message: PongMessage) {
        if (lastPingNonce != message.nonce) {
            close()
        }
    }

    private suspend fun readMessagePayload(payloadSize: Int): ByteArray {
        val payload = ByteArray(payloadSize)
        var bytesRead = 0
        while (!socket.isInputShutdown && bytesRead < payloadSize) {
            var bytesToRead = inputStream.available()
            if (bytesToRead > 0) {
                if (bytesToRead + bytesRead > payloadSize) {
                    bytesToRead = payloadSize - bytesRead
                }
            }
            inputStream.readNBytes(payload, bytesRead, bytesToRead)
            bytesRead += bytesToRead
            yield()
        }

        if (payloadSize < bytesRead) {
            throw Exception("Read too many bytes")
        }
        return payload
    }

    private suspend fun readMessageHeader(): MessageHeader {
        val header = ByteArray(messageHeaderSize)
        var bytesRead = 0
        while (!socket.isInputShutdown && bytesRead < messageHeaderSize) {
            var bytesToRead = inputStream.available()
            if (bytesToRead > 0) {
                if (bytesToRead + bytesRead > messageHeaderSize) {
                    bytesToRead = messageHeaderSize - bytesRead
                }
            }
            inputStream.readNBytes(header, bytesRead, bytesToRead)
            bytesRead += bytesToRead
            yield()
        }

        if (messageHeaderSize < bytesRead) {
            throw Exception("Read too many bytes")
        }

        // Convert the bytes into a message header object
        return MessageHeader.fromByteArray(header)
    }

    private fun sendVersionMessage(lastBlockHeight: Int) {
        val message = VersionMessage(
            protocolVersion = protocolVersion,
            services = NetworkAddress.serviceFlagsNetwork,
            timestamp = ZonedDateTime.now().toEpochSecond(),
            targetAddress = addr,
            sourceAddress = NetworkAddress(convertAddressToByteArray("0.0.0.0"), 0, NetworkAddress.serviceFlagsNetwork),
            nonce = Random.nextLong(),
            userAgent = VariableString(""),
            startHeight = lastBlockHeight,
            relay = true
        )
        sendMessage(message)
    }

    private fun sendMessageBytes(command: String, message: ByteArray) {
        val messageChecksum = calculateHeaderChecksum(message)

        val header = MessageHeader(
            messageHeaderStart,
            command,
            message.size,
            messageChecksum
        )
        val headerByteArray = header.toByteArray()

        outputStream.write(headerByteArray)
        outputStream.write(message)
        outputStream.flush()
    }

    companion object {
        const val messageHeaderStart = 0x0b110907          //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults
        const val messageHeaderSize = 4 + 12 + 4 + 4       //https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
        const val timeoutSeconds = 60                      //https://en.bitcoin.it/wiki/Protocol_documentation#getaddr
        const val initialSetupTimeoutSeconds = 5
        const val minimumProtocolVersion = 70015           //https://developer.bitcoin.org/reference/p2p_networking.html#protocol-versions
        const val millisecondsBetweenPing = (timeoutSeconds - 5) * 1000L
    }
}