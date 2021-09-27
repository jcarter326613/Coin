package ui

import bitcoin.network.Network
import ui.bitcoin.NetworkView
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JFrame

class MainWindow {
    private val mainFrame = JFrame("Coin")
    private val network = Network()

    init {
        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val layout = BorderLayout()

        val networkView = NetworkView()
        mainFrame.contentPane.add(networkView)
        layout.addLayoutComponent(networkView, BorderLayout.CENTER)

        val connectButton = JButton("Connect")
        mainFrame.contentPane.add(connectButton)
        layout.addLayoutComponent(connectButton, BorderLayout.SOUTH)

        mainFrame.contentPane.layout = layout

        // Setup the network
        network.addUpdateListener(networkView)
        connectButton.addActionListener {
            network.connect()
        }
    }

    fun start() {
        mainFrame.pack()
        mainFrame.isVisible = true
    }
}