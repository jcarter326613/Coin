package bitcoin.messages

import bitcoin.messages.components.VariableInt
import util.ByteManipulation

class BlockMessage(
    val version: Int,
    val previousBlockHash: ByteArray,
    val merkleRootHash: ByteArray,
    val timestamp: Int,
    val difficultyTarget: Int,
    val nonce: Int,
    val transactions: List<TxMessage>
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateMessageSize())
        intoByteArray(array, 0)
        return array
    }

    fun intoByteArray(dest: ByteArray, destIndex: Int): Int {
        var currentOffset = ByteManipulation.writeIntToArray(version, dest, destIndex)
        previousBlockHash.copyInto(dest, currentOffset)
        currentOffset += previousBlockHash.size
        merkleRootHash.copyInto(dest, currentOffset)
        currentOffset += merkleRootHash.size
        currentOffset = ByteManipulation.writeIntToArray(timestamp, dest, currentOffset)
        currentOffset = ByteManipulation.writeIntToArray(difficultyTarget, dest, currentOffset)
        currentOffset = ByteManipulation.writeIntToArray(nonce, dest, currentOffset)
        currentOffset = VariableInt(transactions.size.toLong()).intoByteArray(dest, currentOffset)

        for (transaction in transactions) {
            currentOffset = transaction.intoByteArray(dest, currentOffset)
        }

        return currentOffset
    }

    fun toBlockHeaderMessage(): BlockHeaderMessage {
        return BlockHeaderMessage(
            version = version,
            previousBlockHash = previousBlockHash,
            merkleRootHash = merkleRootHash,
            timestamp = timestamp,
            difficultyTarget = difficultyTarget,
            nonce = nonce
        )
    }

    fun calculateMessageSize(): Int {
        var size = 4 + 4 + 4 + 4 + previousBlockHash.size + merkleRootHash.size +
                VariableInt(transactions.size.toLong()).calculateMessageSize()
        for (transaction in transactions) {
            size += transaction.calculateMessageSize()
        }

        return size
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): BlockMessage {
            return fromByteArrayWithIndex(buffer, 0).value
        }

        fun fromByteArrayWithIndex(buffer: ByteArray, startIndex: Int): BlockIndexPair {
            val version = ByteManipulation.readIntFromArray(buffer, startIndex)
            val previousBlockHash = buffer.slice(version.nextIndex until (version.nextIndex + 32)).toByteArray()
            val merkleRootHash = buffer.slice((version.nextIndex + 32) until (version.nextIndex + 32 * 2)).toByteArray()

            val timestamp = ByteManipulation.readIntFromArray(buffer, version.nextIndex + 32 * 2)
            val difficultyTarget = ByteManipulation.readIntFromArray(buffer, timestamp.nextIndex)
            val nonce = ByteManipulation.readIntFromArray(buffer, difficultyTarget.nextIndex)
            val numTransactions = VariableInt.fromByteArray(buffer, nonce.nextIndex)
            val transactions = mutableListOf<TxMessage>()

            var currentOffset = numTransactions.nextIndex
            for (i in 0 until numTransactions.value.value) {
                val newTransaction = TxMessage.fromByteArray(buffer, currentOffset)
                transactions.add(newTransaction.value)
                currentOffset = newTransaction.nextIndex
            }

            return BlockIndexPair(
                BlockMessage(
                    version = version.value,
                    previousBlockHash = previousBlockHash,
                    merkleRootHash = merkleRootHash,
                    timestamp = timestamp.value,
                    difficultyTarget = difficultyTarget.value,
                    nonce = nonce.value,
                    transactions = transactions
                ),
                currentOffset
            )
        }
    }

    data class BlockIndexPair (
        val value: BlockMessage,
        val nextIndex: Int
    )
}