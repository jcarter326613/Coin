package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableString
import org.junit.jupiter.api.Test
import java.util.*

class VersionMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val sourceAddress = ByteArray(16)
        sourceAddress[0] = 127
        sourceAddress[3] = 1
        val targetAddress = ByteArray(16)
        targetAddress[0] = 0xFF.toByte()
        targetAddress[1] = 0xFF.toByte()
        targetAddress[2] = 0xFF.toByte()
        targetAddress[3] = 0xFF.toByte()
        targetAddress[4] = 0xFF.toByte()
        targetAddress[5] = 0xFF.toByte()
        targetAddress[6] = 0xBD.toByte()
        targetAddress[7] = 0xFF.toByte()

        val checksum = ByteArray(4)
        checksum[0] = 4
        checksum[1] = 76
        checksum[2] = 100
        checksum[3] = -12
        val message = VersionMessage(
            protocolVersion = 895468,
            services = 2458728211245L,
            timestamp = 123456789123456789L,
            targetAddress = NetworkAddress(targetAddress, 543, NetworkAddress.serviceFlagsAll),
            sourceAddress = NetworkAddress(sourceAddress, 88, NetworkAddress.serviceFlagsNetwork),
            nonce = 998,
            userAgent = VariableString("UserAgent"),
            startHeight = 7,
            relay = true
        )
        val messageByteArray = message.toByteArray()
        val outMessage = VersionMessage.fromByteArray(messageByteArray)

        assert(message.protocolVersion == outMessage.protocolVersion)
        assert(message.services == outMessage.services)
        assert(message.timestamp == outMessage.timestamp)
        assert(message.targetAddress == outMessage.targetAddress)
        assert(message.sourceAddress == outMessage.sourceAddress)
        assert(message.nonce == outMessage.nonce)
        assert(message.userAgent == outMessage.userAgent)
        assert(message.startHeight == outMessage.startHeight)
        assert(message.relay == outMessage.relay)
    }
}