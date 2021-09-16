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
import java.net.Inet6Address
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.time.ZonedDateTime
import kotlin.random.Random

class Connection(
    val seed: String,
    val port: Short,
    messageCallback: (message: String, connection: Connection) -> Unit,
    disconnectionCallback: (connection: Connection) -> Unit,
) {
    val protocolVersion = 70015                                                             //https://developer.bitcoin.org/reference/p2p_networking.html#protocol-versions
    val sha256Hasher = MessageDigest.getInstance("SHA-256")

    private var _isReady = false
    val isReady: Boolean get() = _isReady || isClosed
    var isClosed = false
        private set

    lateinit var socket: Socket
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream

    init {
        Log.info("Connecting to $seed:$port")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                socket = Socket(seed, port.toInt())
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
        val computedHash = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).get(actualHash, 0, 4).position(0).int
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
            targetAddress = NetworkAddress(convertAddressToByteArray(seed), port),
            sourceAddress = NetworkAddress(convertAddressToByteArray("0.0.0.0"), port),
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
        val headerByteArray = header.toByteArray()
        Log.info("Version message header")
        Log.info(headerByteArray)
        Log.info("Version message payload")
        Log.info(messageByteArray)
        outputStream.write(headerByteArray)
        outputStream.write(messageByteArray)
        outputStream.flush()
    }

    private fun convertAddressToByteArray(a: String): ByteArray {
        //Inet6Address.getAllByName(seed)  gets all the addresses.  We just want the first one for now.
        val address = Inet6Address.getByName(a).address
        if (address.size == 4) {
            val lengthenedAddress = ByteArray(16)
            for ( i in 0..9) {
                lengthenedAddress[0] = 0
            }
            lengthenedAddress[10] = 0xFF.toByte()
            lengthenedAddress[11] = 0xFF.toByte()
            lengthenedAddress[12] = address[0]
            lengthenedAddress[13] = address[1]
            lengthenedAddress[14] = address[2]
            lengthenedAddress[15] = address[3]
            return lengthenedAddress
        }
        return address
    }

    companion object {
        const val messageHeaderStart = 0x0b110907                                                       //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults
        val messageHeaderStartBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(messageHeaderStart)   //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults
        const val messageHeaderSize = 4 + 12 + 4 + 4                                                    //https://en.bitcoin.it/wiki/Protocol_documentation#Message_structure
        const val servicesBitFlag = 1L                                                                //https://en.bitcoin.it/wiki/Protocol_documentation
    }
}