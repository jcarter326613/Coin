package bitcoin

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class Network {
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

    private fun establishSeedConnection(seed: String) {
        val newConnection = Connection(seed, port, {message, connection -> receiveMessage(message, connection)}, {connectionDisconnected(it)} )
        activeConnections.add(newConnection)
    }

    private fun receiveMessage(message: String, connection: Connection) {

    }

    private fun connectionDisconnected(connection: Connection) {
    }
}