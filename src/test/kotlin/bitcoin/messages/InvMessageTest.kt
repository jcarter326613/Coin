package bitcoin.messages

import org.junit.jupiter.api.Test
import kotlin.random.Random

class InvMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val hash1 = createRandomHash()
        val hash2 = createRandomHash()
        val hash3 = createRandomHash()
        val hash4 = createRandomHash()
        val hash5 = createRandomHash()

        val message = InvMessage(
            transactionHashes = listOf(hash1, hash2, hash3),
            blockHashes = listOf(hash4, hash5)
        )
        val messageByteArray = message.toByteArray()
        val outMessage = InvMessage.fromByteArray(messageByteArray)

        assert(outMessage.transactionHashes[0].contentEquals(hash1))
        assert(outMessage.transactionHashes[1].contentEquals(hash2))
        assert(outMessage.transactionHashes[2].contentEquals(hash3))
        assert(outMessage.blockHashes[0].contentEquals(hash4))
        assert(outMessage.blockHashes[1].contentEquals(hash5))
    }

    private fun createRandomHash(): ByteArray = Random.Default.nextBytes(32)
}