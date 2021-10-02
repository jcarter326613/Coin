package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import org.junit.jupiter.api.Test
import kotlin.random.Random

class AddrMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val message = AddrMessage(listOf(
            NetworkAddress(createRandomAddress(), Random.nextInt().toShort(), Random.nextLong()),
            NetworkAddress(createRandomAddress(), Random.nextInt().toShort(), Random.nextLong()),
            NetworkAddress(createRandomAddress(), Random.nextInt().toShort(), Random.nextLong())
        ))
        val messageByteArray = message.toByteArray()
        val outMessage = AddrMessage.fromByteArray(messageByteArray)

        assert(message.entries.size == outMessage.entries.size)
        for (i in 0 until message.entries.size) {
            val messageEntry = message.entries[i]
            val outMessageEntry = outMessage.entries[i]
            assert(messageEntry.address.contentEquals(outMessageEntry.address))
            assert(messageEntry.port == outMessageEntry.port)
            assert(messageEntry.serviceFlags == outMessageEntry.serviceFlags)
        }
    }

    private fun createRandomAddress(): ByteArray = Random.Default.nextBytes(16)
}