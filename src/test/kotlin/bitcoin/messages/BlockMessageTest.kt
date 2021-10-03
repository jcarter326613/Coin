package bitcoin.messages

import org.junit.jupiter.api.Test
import kotlin.random.Random

class BlockMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val message = createRandomBlock()
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

    companion object {
        fun createRandomBlock(previousHash: ByteArray? = null): BlockMessage {
            return BlockMessage(
                Random.nextInt(), //version
                previousHash ?: createRandomHash(), //previousBlockHash
                createRandomHash(), //merkleRootHash
                Random.nextInt(), //timestamp
                Random.nextInt(), //difficultyTarget
                Random.nextInt(), //nonce
                listOf(
                    TxMessageTest.createRandomTx(),
                    TxMessageTest.createRandomTx(),
                    TxMessageTest.createRandomTx(),
                    TxMessageTest.createRandomTx(),
                    TxMessageTest.createRandomTx()
                )
            )
        }
        private fun createRandomHash(): ByteArray = Random.Default.nextBytes(32)
    }
}