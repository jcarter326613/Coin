package ui

import bitcoin.Network
import ui.bitcoin.NetworkView
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Toolkit
import javax.swing.JButton
import javax.swing.JFrame

class MainWindow {
    private val mainFrame = JFrame("Coin")
    private val network = Network()

    init {
        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainFrame.layout = FlowLayout()

        val networkView = NetworkView()
        mainFrame.contentPane.add(networkView)

        val connectButton = JButton("Connect")
        mainFrame.contentPane.add(connectButton)

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