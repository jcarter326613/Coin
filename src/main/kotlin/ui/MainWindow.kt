package ui

import bitcoin.Network
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
        //mainFrame.preferredSize = mainFrame.graphicsConfiguration.device.defaultConfiguration.bounds.size
        //mainFrame.extendedState = JFrame.MAXIMIZED_BOTH
        mainFrame.layout = FlowLayout()

        val connectButton = JButton("Connect")
        mainFrame.contentPane.add(connectButton)

        connectButton.addActionListener {
            network.connect()
        }
    }

    fun start() {
        mainFrame.pack()
        mainFrame.isVisible = true
    }
}