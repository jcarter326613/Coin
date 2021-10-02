package bitcoin.messages

import org.junit.jupiter.api.Test
import kotlin.random.Random

class BlockHeaderMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val message = BlockHeaderMessage(
            62464262, //version
            createRandomHash(), //previousBlockHash
            createRandomHash(), //merkleRootHash
            8899287, //timestamp
            11, //difficultyTarget
            999933848, //nonce
        )

        val messageByteArray = message.toByteArray()
        val outMessage = BlockMessage.fromByteArray(messageByteArray)

        assert(message.version == outMessage.version)
        assert(message.previousBlockHash.contentEquals(outMessage.previousBlockHash))
        assert(message.merkleRootHash.contentEquals(outMessage.merkleRootHash))
        assert(message.timestamp == outMessage.timestamp)
        assert(message.difficultyTarget == outMessage.difficultyTarget)
        assert(message.nonce == outMessage.nonce)
    }

    private fun createRandomHash(): ByteArray = Random.Default.nextBytes(32)
}