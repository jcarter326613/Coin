package bitcoin.messages

import org.junit.jupiter.api.Test
import kotlin.random.Random

class BlockMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val message = BlockMessage(
            62464262, //version
            createRandomHash(), //previousBlockHash
            createRandomHash(), //merkleRootHash
            8899287, //timestamp
            11, //difficultyTarget
            999933848, //nonce
            listOf(
                TxMessageTest.createRandomTx(),
                TxMessageTest.createRandomTx(),
                TxMessageTest.createRandomTx(),
                TxMessageTest.createRandomTx(),
                TxMessageTest.createRandomTx()
            )
        )

        val messageByteArray = message.toByteArray()
        val outMessage = BlockMessage.fromByteArray(messageByteArray)

        assert(message.version == outMessage.version)
        assert(message.previousBlockHash.contentEquals(outMessage.previousBlockHash))
        assert(message.merkleRootHash.contentEquals(outMessage.merkleRootHash))
        assert(message.timestamp == outMessage.timestamp)
        assert(message.difficultyTarget == outMessage.difficultyTarget)
        assert(message.nonce == outMessage.nonce)
        assert(message.transactions.size == outMessage.transactions.size)
        for (i in 0 until 5) {
            TxMessageTest.assertTxEqual(message.transactions[i], message.transactions[i])
        }
    }

    private fun createRandomHash(): ByteArray = Random.Default.nextBytes(32)
}