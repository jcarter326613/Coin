package bitcoin.messages

import bitcoin.messages.components.VariableInt
import util.ByteManipulation

class BlockHeaderMessage(
    val version: Int,
    val previousBlockHash: ByteArray,
    val merkleRootHash: ByteArray,
    val timestamp: Int,
    val difficultyTarget: Int,
    val nonce: Int
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
        VariableInt(0).intoByteArray(array, currentOffset)

        return array
    }

    fun calculateMessageSize(): Int {
        return 4 + 4 + 4 + 4 + previousBlockHash.size + merkleRootHash.size +
                VariableInt(0).calculateMessageSize()
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): BlockHeaderMessage {
            val version = ByteManipulation.readIntFromArray(buffer, 0)
            val previousBlockHash = buffer.slice(version.nextIndex until (version.nextIndex + 32)).toByteArray()
            val merkleRootHash = buffer.slice((version.nextIndex + 32) until (version.nextIndex + 32 * 2)).toByteArray()

            val timestamp = ByteManipulation.readIntFromArray(buffer, version.nextIndex + 32 * 2)
            val difficultyTarget = ByteManipulation.readIntFromArray(buffer, timestamp.nextIndex)
            val nonce = ByteManipulation.readIntFromArray(buffer, difficultyTarget.nextIndex)

            return BlockHeaderMessage(
                version = version.value,
                previousBlockHash = previousBlockHash,
                merkleRootHash = merkleRootHash,
                timestamp = timestamp.value,
                difficultyTarget = difficultyTarget.value,
                nonce = nonce.value
            )
        }
    }
}