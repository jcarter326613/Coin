package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import org.junit.jupiter.api.Test
import kotlin.random.Random

class GetBlocksMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val message = GetBlocksMessage(
            Random.nextInt(),
            listOf(
                createRandomHash(),
                createRandomHash(),
                createRandomHash(),
                createRandomHash(),
                createRandomHash(),
            ),
            createRandomHash()
        )
        val messageByteArray = message.toByteArray()
        val outMessage = GetBlocksMessage.fromByteArray(messageByteArray)

        assert(message.version == outMessage.version)
        for (i in 0 until message.locatorHashes.size) {
            val messageEntry = message.locatorHashes[i]
            val outMessageEntry = outMessage.locatorHashes[i]
            assert(messageEntry.contentEquals(outMessageEntry))
        }
        assert(message.hashStop.contentEquals(outMessage.hashStop))
    }

    private fun createRandomHash(): ByteArray = Random.Default.nextBytes(32)
}