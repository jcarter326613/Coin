package ui.bitcoin

import bitcoin.Network
import bitcoin.messages.components.NetworkAddress
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class NetworkView: JPanel(), Network.IUpdateListener {
    private val activeConnections = JList<NetworkAddress>()

    init {
        val layout = BorderLayout()
        add(activeConnections)
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

    override fun networkUpdated(network: Network) {
        updateScrollPane(network)
    }
}