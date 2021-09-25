package bitcoin

import bitcoin.messages.MessageHeader
import bitcoin.messages.VersionMessage
import util.Log
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class Network: IMessageProcessor {
    val port: Short = 18333                                                                 //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults

    private val defaultSeedAddresses = listOf(
        "testnet-seed.bluematt.me",
        "seed.testnet.bitcoin.sprovoost.nl",
        "seed.tbtc.petertodd.org",                                            //https://github.com/bitcoin/bitcoin/blob/master/src/chainparams.cpp#L233
        "testnet-seed.bitcoin.jonasschnelli.ch",
    )
    private val activeConnections = mutableListOf<Connection>()
    private var isConnected = AtomicBoolean(false)

    fun connect() {
        if (isConnected.getAndSet(true)) return

        // First connect to the default seeds and as them for other clients
        for (seed in defaultSeedAddresses) {
            establishSeedConnection(seed)
            break
        }
    }

    override fun processIncomingMessageVersion(header: MessageHeader, payload: VersionMessage, connection: Connection) {
        if (payload.protocolVersion < Connection.minimumProtocolVersion) {
            connection.close()
            removeActiveConnection(connection)
            Log.info("Connection ${connection.seed}(${connection.port}) removed due to old version ${payload.protocolVersion} < ${Connection.minimumProtocolVersion}.")
        } else {
            connection.sendVerack()
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
    }
}