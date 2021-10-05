package bitcoin.network

import bitcoin.chain.Block
import bitcoin.chain.BlockDb
import bitcoin.chain.IBlockDbProvider
import bitcoin.messages.*
import bitcoin.messages.components.NetworkAddress
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import storage.LocalStorage
import storage.NullStorage
import util.Log
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.floor
import kotlin.random.Random

class Network(val blockDbProvider: IBlockDbProvider = object: IBlockDbProvider {
    override fun createBlockDb(): BlockDb {
        return BlockDb(LocalStorage())
    }
}): IMessageProcessor {
    val activeConnectionAddresses: List<NetworkAddress>
        get() {
            val retList = mutableListOf<NetworkAddress>()
            synchronized(activeConnections) {
                for (connection in activeConnections) {
                    retList.add(connection.addr)
                }
            }
            return retList
        }
    val updateListeners = mutableListOf<IUpdateListener>()
    val loadingBlocks: Boolean
        get() = ZonedDateTime.now().minusSeconds(5).isBefore(mainChainDb.lastBlockReceived)
    val lastBlockHeight get() = mainChainDb.lastBlockHeight

    private val port: Short = 18333                                                                 //https://developer.bitcoin.org/reference/p2p_networking.html#constants-and-defaults
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
    private val mainChainDb = blockDbProvider.createBlockDb()
    private val secondaryChains = mutableListOf<BlockDb>()

    fun addUpdateListener(newListener: IUpdateListener) {
        updateListeners.add(newListener)
    }

    fun connect() {
        if (isConnected.getAndSet(true)) return

        GlobalScope.launch {
            while (isConnected.get()) {
                notifyListenersOfUpdate()
                delay(2000)
            }
        }

        // First connect to the default seeds and ask them for other clients
        for (seed in defaultSeedAddresses) {
            val address = NetworkAddress.convertAddressToByteArray(seed)
            val networkAddress = NetworkAddress(address, port, NetworkAddress.serviceFlagsAll)
            activePeers[networkAddress] = ZonedDateTime.now()
        }
        establishNewRandomConnection()

        // Start the cleanup thread
        GlobalScope.launch {
            while (true) {
                delay(cleanOutDelayMilliseconds)
                cleanOutBadConnections()
            }
        }

        // Start the pruning thread
        GlobalScope.launch {
            while (true) {
                delay(pruneDelayMilliseconds)
                pruneConnections()
            }
        }
    }

    override fun processIncomingMessageVersion(payload: VersionMessage, connection: Connection) {
        val address = connection.address
        if (address != null && payload.protocolVersion >= Connection.minimumProtocolVersion) {
            activePeers[address] = ZonedDateTime.now()
            connection.sendVerack()

            // Request any new blocks since the most recent block in the database
            val getBlocksMessage = GetBlocksMessage(
                version = Connection.minimumProtocolVersion,
                locatorHashes = mainChainDb.locatorHashes,
                hashStop = ByteArray(32) {0}
            )
            connection.sendMessage(getBlocksMessage)
        } else {
            connection.close()
            Log.info("Connection ${connection.addr.address}(${connection.addr.port}) removed due to old version ${payload.protocolVersion} < ${Connection.minimumProtocolVersion}.")
        }
    }

    override fun processIncomingMessageAddr(payload: AddrMessage, connection: Connection) {
        synchronized(activePeers) {
            for (entry in payload.entries) {
                activePeers[entry] = ZonedDateTime.now()
            }
        }

        establishNewRandomConnection()
    }

    override fun processIncomingMessageInv(payload: InvMessage, connection: Connection) {
        val listToRequest = mutableListOf<ByteArray>()
        for (blockHash in payload.blockHashes) {
            if (mainChainDb.getBlock(blockHash) == null) {
                listToRequest.add(blockHash)
            }
        }

        if (listToRequest.size > 0) {
            val getMessage = GetDataMessage(
                blockHashes = listToRequest
            )
            connection.sendMessage(getMessage)
        }
    }

    override fun processIncomingMessageReject(payload: RejectMessage, connection: Connection) {
        Log.error("Connection ${connection.addr} returned rejection\n\tMessage: ${payload.message}\n\tCode: ${payload.code}\n\tReason: ${payload.reason}")
    }

    override fun processIncomingMessageBlock(payload: BlockMessage, connection: Connection) {
        val newBlock = Block(payload)
        if (mainChainDb.addBlock(newBlock)) {
            notifyListenersOfUpdate()
        } else {
            addToSecondaryChain(newBlock)
        }
    }

    private fun removeActiveConnection(connectionToRemove: Connection) {
        synchronized(activeConnections) {
            activeConnections.remove(connectionToRemove)
        }

        synchronized(activePeers) {
            activePeers.remove(connectionToRemove.address)
        }

        notifyListenersOfUpdate()
    }

    private fun cleanOutBadConnections() {
        for (connection in activeConnections) {
            if (connection.isClosed || connection.isTimedOut) {
                connection.close()
            }
        }
        establishNewRandomConnection()
    }

    private fun pruneConnections() {
        val targetNumConnections = floor(maxActiveConnections * (1- prunePercentage))
        while (activeConnections.size > targetNumConnections) {
            var connectionToClose: Connection?
            synchronized(activeConnections) {
                val i = Random.nextInt(activeConnections.size)
                connectionToClose = activeConnections[i]
            }
            connectionToClose?.close()
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
        try {
            while (activeConnections.size < maxActiveConnections) {
                // Select an address to connect to
                var peer: NetworkAddress? = null
                synchronized(activePeers) {
                    while (activePeers.isNotEmpty()) {
                        val i = Random.nextInt(activePeers.size)
                        peer = activePeers.keys.toTypedArray()[i]
                        val entryDate = activePeers[peer] ?: throw Exception("Should never happen")
                        if (entryDate.plusSeconds(maxPeerAgeSeconds.toLong()).isBefore(ZonedDateTime.now())) {
                            activePeers.remove(peer)
                        } else {
                            break
                        }
                    }
                }

                // Connect to the address
                synchronized(activeConnections) {
                    val setPeer = peer
                    if (setPeer != null) {
                        var alreadyConnected = false
                        for (connection in activeConnections) {
                            if (connection.addr == setPeer) {
                                alreadyConnected = true
                                break
                            }
                        }
                        if (!alreadyConnected) {
                            activeConnections.add(Connection(setPeer, mainChainDb.lastBlockHeight, this) {
                                removeActiveConnection(it)
                            })
                        }
                    }
                }
                notifyListenersOfUpdate()
            }
        } finally {
            numConnectionsBeingCreated.decrementAndGet()
        }
    }

    private fun addToSecondaryChain(block: Block) {
        synchronized(secondaryChains) {
            for (chain in secondaryChains) {
                if (chain.addBlock(block)) {
                    return
                }
            }

            //We didn't have a chain with the previous block.  If the main branch has it, create a secondary chain
            //otherwise the main branch may be building to this point, or it's just a garbage block
            val previousBlock = mainChainDb.getBlock(block.previousBlockHash)
            if (previousBlock != null) {
                val newSecondaryChain = BlockDb(NullStorage())
                newSecondaryChain.addBlock(block)
                secondaryChains.add(newSecondaryChain)
            }
        }
    }

    private fun notifyListenersOfUpdate() {
        for (listener in updateListeners) {
            listener.networkUpdated(this)
        }
    }

    companion object {
        private const val maxActiveConnections = 2
        private const val cleanOutDelayMilliseconds = 60 * 1000L
        private const val pruneDelayMilliseconds = 60 * 5 * 1000L
        private const val maxPeerAgeSeconds = 60 * 60 * 3             //https://en.bitcoin.it/wiki/Protocol_documentation#getaddr
        private const val prunePercentage = 0.1
    }

    interface IUpdateListener {
        fun networkUpdated(network: Network)
    }
}