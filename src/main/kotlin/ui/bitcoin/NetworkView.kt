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
    private val chainLoading = JLabel()

    init {
        val layout = BorderLayout()
        val statusPanel = JPanel()

        add(activeConnections)
        add(statusPanel)
        layout.addLayoutComponent(activeConnections, BorderLayout.CENTER)
        layout.addLayoutComponent(statusPanel, BorderLayout.EAST)

        val statusLayout = BoxLayout(statusPanel, BoxLayout.Y_AXIS)
        statusPanel.add(chainLength)
        statusPanel.add(chainLoading)
        statusLayout.addLayoutComponent("Chain Length", chainLength)
        statusLayout.addLayoutComponent("Chain Loading", chainLoading)

        preferredSize = Dimension(300, 200)

        this.layout = layout
        statusPanel.layout = statusLayout
    }

    fun updateScrollPane(network: Network) {
        val addresses = network.activeConnectionAddresses
        val listModel = DefaultListModel<NetworkAddress>()
        listModel.addAll(addresses)
        activeConnections.model = listModel
    }

    fun updateChainStatistics(network: Network) {
        chainLength.text = "Chain Length: ${network.lastBlockHeight}"
        chainLoading.text = if (network.loadingBlocks) "Loading" else ""
    }

    override fun networkUpdated(network: Network) {
        updateScrollPane(network)
        updateChainStatistics(network)
    }
}