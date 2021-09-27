package bitcoin.messages

import org.junit.jupiter.api.Test
import kotlin.random.Random

class TxMessageTest {
    @Test
    fun inAndOut_Normal_Success() {
        val inputs = listOf<TxMessage.TxIn>(
            createRandomTxIn(),
            createRandomTxIn(),
            createRandomTxIn(),
            createRandomTxIn(),
        )
        val outputs = listOf<TxMessage.TxOut>(
            createRandomTxOut(),
            createRandomTxOut(),
            createRandomTxOut(),
            createRandomTxOut(),
            createRandomTxOut(),
        )

        val message = TxMessage(
            version = 85682,
            inputs = inputs,
            outputs = outputs,
            locktime = 24588
        )

        val messageByteArray = message.toByteArray()
        val outMessage = TxMessage.fromByteArray(messageByteArray)

        assert(message.version == outMessage.version)
        assert(message.locktime == outMessage.locktime)
        assert(message.inputs.size == outMessage.inputs.size)
        assert(message.outputs.size == outMessage.outputs.size)
        for (i in message.inputs.indices) {
            assertTxInEqual(message.inputs[i], outMessage.inputs[i])
        }
        for (i in message.outputs.indices) {
            assertTxOutEqual(message.outputs[i], outMessage.outputs[i])
        }
    }

    private fun assertTxInEqual(m1: TxMessage.TxIn, m2: TxMessage.TxIn) {
        assert(m1.outPoint.hash.contentEquals(m2.outPoint.hash))
        assert(m1.outPoint.index == m2.outPoint.index)
        assert(m1.sequence == m2.sequence)
        assert(m1.signatureScript.contentEquals(m2.signatureScript))
    }

    private fun assertTxOutEqual(m1: TxMessage.TxOut, m2: TxMessage.TxOut) {
        assert(m1.value == m2.value)
        assert(m1.script.contentEquals(m2.script))
    }

    private fun createRandomTxIn(): TxMessage.TxIn {
        return TxMessage.TxIn(
            TxMessage.OutPoint(
                createRandomHash(),
                Random.nextInt()
            ),
            Random.Default.nextBytes(17),
            Random.nextInt()
        )
    }

    private fun createRandomTxOut(): TxMessage.TxOut {
        return TxMessage.TxOut(
            Random.Default.nextLong(),
            Random.Default.nextBytes(60)
        )
    }

    private fun createRandomHash(): ByteArray = Random.Default.nextBytes(32)
}