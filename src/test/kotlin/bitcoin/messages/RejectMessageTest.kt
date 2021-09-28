package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableString
import org.junit.jupiter.api.Test

class RejectMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val sourceAddress = ByteArray(3)
        sourceAddress[0] = 127
        sourceAddress[1] = 1
        sourceAddress[2] = 0x4F
        val message = RejectMessage("Test Message", RejectMessage.Code.Malformed, "Because", sourceAddress)
        val messageByteArray = message.toByteArray()
        val outMessage = RejectMessage.fromByteArray(messageByteArray)

        assert(message.message == outMessage.message)
        assert(message.code == outMessage.code)
        assert(message.reason == outMessage.reason)
        assert(message.data.contentEquals(outMessage.data))
    }
}