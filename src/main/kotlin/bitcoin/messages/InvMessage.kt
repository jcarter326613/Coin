package bitcoin.messages

import bitcoin.messages.components.VariableInt
import util.ByteManipulation

class InvMessage(
    val transactionHashes: List<ByteArray>,
    val dataBlockHashes: List<ByteArray>
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateMessageSize())
        var currentOffset = 0

        currentOffset = VariableInt(transactionHashes.size + dataBlockHashes.size.toLong()).intoByteArray(array, currentOffset)

        for (hash in transactionHashes) {
            currentOffset = ByteManipulation.writeIntToArray(1, array, currentOffset)
            hash.copyInto(array, currentOffset)
            currentOffset += hash.size
        }

        for (hash in dataBlockHashes) {
            currentOffset = ByteManipulation.writeIntToArray(2, array, currentOffset)
            hash.copyInto(array, currentOffset)
            currentOffset += hash.size
        }

        return array
    }

    fun calculateMessageSize(): Int {
        val numItems = VariableInt(transactionHashes.size + dataBlockHashes.size.toLong())
        val numItemsSize = numItems.calculateMessageSize()
        return numItemsSize + 36 * numItems.value.toInt()
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): InvMessage {
            val numItemsInfo = VariableInt.fromByteArray(buffer, 0)
            var currentOffset = numItemsInfo.nextIndex
            val numItems = numItemsInfo.value.value

            val transactionHashes = mutableListOf<ByteArray>()
            val dataBlockHashes = mutableListOf<ByteArray>()

            for (i in 0 until numItems) {
                val typeData = ByteManipulation.readIntFromArray(buffer, currentOffset)
                currentOffset = typeData.nextIndex
                when (typeData.value) {
                    1 -> {
                        val hash = ByteArray(32)
                        buffer.copyInto(hash, 0, currentOffset, currentOffset + 32)
                        transactionHashes.add(hash)
                    }
                    2 -> {
                        val hash = ByteArray(32)
                        buffer.copyInto(hash, 0, currentOffset, currentOffset + 32)
                        dataBlockHashes.add(hash)
                    }
                }
                currentOffset += 32
            }

            return InvMessage(
                transactionHashes = transactionHashes,
                dataBlockHashes = dataBlockHashes
            )
        }
    }
}