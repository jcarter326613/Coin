package ui.bitcoin

import bitcoin.chain.BlockDb
import bitcoin.network.Network
import bitcoin.messages.components.NetworkAddress
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class NetworkView: JPanel(), Network.IUpdateListener {
    private val activeConnections = JList<NetworkAddress>()
    private val chainLength = JLabel()

    init {
        val layout = BorderLayout()
        add(chainLength)
        add(activeConnections)
        layout.addLayoutComponent(chainLength, BorderLayout.NORTH)
        layout.addLayoutComponent(activeConnections, BorderLayout.CENTER)
        preferredSize = Dimension(300, 200)

        this.layout = layout
    }

    fun updateScrollPane(network: Network) {
        val addresses = network.activeConnectionAddresses
        val listModel = DefaultListModel<NetworkAddress>()
        listModel.addAll(addresses)
        activeConnections.model = listModel
    }

    fun updateChainStatistics() {
        chainLength.text = "Chain Length: ${BlockDb.instance.lastBlockHeight}"
    }

    override fun networkUpdated(network: Network) {
        updateScrollPane(network)
        updateChainStatistics()
    }
}