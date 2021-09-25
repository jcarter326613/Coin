package bitcoin

import bitcoin.messages.AddrMessage
import bitcoin.messages.MessageHeader
import bitcoin.messages.VersionMessage
import bitcoin.messages.components.NetworkAddress
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import util.Log
import java.nio.ByteBuffer
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class Network: IMessageProcessor {
    val port: Short = 18333                                                                 //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults

    private val defaultSeedAddresses = listOf(
        "testnet-seed.bluematt.me",
        "seed.testnet.bitcoin.sprovoost.nl",
        "seed.tbtc.petertodd.org",                                            //https://github.com/bitcoin/bitcoin/blob/master/src/chainparams.cpp#L233
        "testnet-seed.bitcoin.jonasschnelli.ch",
    )
    private val activeConnections = mutableListOf<Connection>()
    private val activePeers = mutableMapOf<NetworkAddress, ZonedDateTime>()
    private var isConnected = AtomicBoolean(false)
    private val numConnectionsBeingCreated = AtomicInteger(0)

    fun connect() {
        if (isConnected.getAndSet(true)) return

        // First connect to the default seeds and ask them for other clients
        for (seed in defaultSeedAddresses) {
            val address = Connection.convertAddressToByteArray(seed)
            val networkAddress = NetworkAddress(address, port)
            activePeers[networkAddress] = ZonedDateTime.now()
        }
        establishNewRandomConnection()

        // Start the cleanup thread
        GlobalScope.launch {
            while (true) {
                delay(cleanoutDelayMilliseconds)
                cleanoutBadConnections()
            }
        }
    }

    override fun processIncomingMessageVersion(payload: VersionMessage, connection: Connection) {
        val address = connection.address
        if (address != null && payload.protocolVersion >= Connection.minimumProtocolVersion) {
            activePeers[address] = ZonedDateTime.now()
            connection.sendVerack()
        } else {
            connection.close()
            removeActiveConnection(connection)
            Log.info("Connection ${connection.seed}(${connection.port}) removed due to old version ${payload.protocolVersion} < ${Connection.minimumProtocolVersion}.")
        }
    }

    override fun processIncomingMessageAddr(payload: AddrMessage, connection: Connection) {
        synchronized(activePeers) {
            for (entry in payload.entries) {
                activePeers[entry] = ZonedDateTime.now()
            }
        }

        if (activeConnections.size < maxActiveConnections) {
            val entryIndex = Random.nextInt(payload.entries.size)
        }
    }

    private fun establishSeedConnection(seed: String) {
        val newConnection = Connection(seed, port, this )
        activeConnections.add(newConnection)
    }

    private fun removeActiveConnection(connectionToRemove: Connection) {
        synchronized(activeConnections) {
            for (i in activeConnections.indices) {
                val current = activeConnections[i]
                if (current.seed == connectionToRemove.seed && current.port == connectionToRemove.port) {
                    activeConnections.removeAt(i)
                    return@synchronized
                }
            }
        }

        synchronized(activePeers) {
            activePeers.remove(connectionToRemove.address)
        }
    }

    private fun cleanoutBadConnections() {
        for (connection in activeConnections) {
            if (connection.isClosed) {
                removeActiveConnection(connection)
            }
        }
        establishNewRandomConnection()
    }

    private fun establishNewRandomConnection() {
        // Gate entry to this function to one thread
        if (numConnectionsBeingCreated.incrementAndGet() > 1) {
            if (numConnectionsBeingCreated.decrementAndGet() == 0) {
                establishNewRandomConnection()
            }
            return
        }

        // Add a new connection until we have all the connections we need to have
        while (activeConnections.size < maxActiveConnections) {
            // Select an address to connect to
            var peer: NetworkAddress
            synchronized(activePeers) {
                while (activePeers.isNotEmpty()) {
                    val i = Random.nextInt(activePeers.size)
                    peer = activePeers.keys.toTypedArray()[i]
                    val entryDate = activePeers[peer] ?: throw Exception("Should never happen")
                    if (entryDate.plusSeconds(maxPeerAgeSeconds.toLong()).isBefore(ZonedDateTime.now())) {
                        activePeers.remove(peer)
                    }
                    break
                }
            }

            // Connect to the address
        }
    }

    companion object {
        private const val maxActiveConnections = 1
        private const val cleanoutDelayMilliseconds = 60 * 1000L
        private const val maxPeerAgeSeconds = 60 * 60 * 3             //https://en.bitcoin.it/wiki/Protocol_documentation#getaddr
    }
}