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
        var currentOffset = 0

        currentOffset = ByteManipulation.writeIntToArray(version, array, currentOffset)
        previousBlockHash.copyInto(array, currentOffset)
        currentOffset += previousBlockHash.size
        merkleRootHash.copyInto(array, currentOffset)
        currentOffset += merkleRootHash.size
        currentOffset = ByteManipulation.writeIntToArray(timestamp, array, currentOffset)
        currentOffset = ByteManipulation.writeIntToArray(difficultyTarget, array, currentOffset)
        currentOffset = ByteManipulation.writeIntToArray(nonce, array, currentOffset)
        currentOffset = VariableInt(transactions.size.toLong()).intoByteArray(array, currentOffset)

        for (transaction in transactions) {
            currentOffset = transaction.intoByteArray(array, currentOffset)
        }

        return array
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
            val version = ByteManipulation.readIntFromArray(buffer, 0)
            val previousBlockHash = buffer.slice(version.nextIndex..(version.nextIndex + 32)).toByteArray()
            val merkleRootHash = buffer.slice((version.nextIndex + 32)..(version.nextIndex + 32 * 2)).toByteArray()

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

            return BlockMessage(
                version = version.value,
                previousBlockHash = previousBlockHash,
                merkleRootHash = merkleRootHash,
                timestamp = timestamp.value,
                difficultyTarget = difficultyTarget.value,
                nonce = difficultyTarget.value,
                transactions = transactions
            )
        }
    }
}