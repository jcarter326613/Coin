package bitcoin.messages

import bitcoin.messages.components.VariableInt
import util.ByteManipulation

class GetBlocksMessage(
    val version: Int,
    val locatorHashes: List<ByteArray>,
    val hashStop: ByteArray
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateMessageSize())
        var currentOffset = 0

        currentOffset = ByteManipulation.writeIntToArray(version, array, currentOffset)
        currentOffset = VariableInt(locatorHashes.size.toLong()).intoByteArray(array, currentOffset)
        for (hash in locatorHashes) {
            hash.copyInto(array, currentOffset)
            currentOffset += hash.size
        }
        hashStop.copyInto(array, currentOffset)
        return array
    }

    private fun calculateMessageSize(): Int {
        return 4 + VariableInt(locatorHashes.size.toLong()).calculateMessageSize() +
                32 * locatorHashes.size + 32
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): GetBlocksMessage {
            val version = ByteManipulation.readIntFromArray(buffer, 0)
            val numLocatorHashes = VariableInt.fromByteArray(buffer, version.nextIndex)
            val locatorHashes = mutableListOf<ByteArray>()
            for (i in 1..numLocatorHashes.value.value.toInt()) {
                val range = (numLocatorHashes.nextIndex + ((i-1) * 32)) until (numLocatorHashes.nextIndex + i * 32)
                locatorHashes.add(buffer.sliceArray(range))
            }
            val range = (numLocatorHashes.nextIndex + numLocatorHashes.value.value.toInt() * 32) until
                    (numLocatorHashes.nextIndex + (numLocatorHashes.value.value.toInt()+1) * 32)
            val hashStop = buffer.sliceArray(range)

            return GetBlocksMessage(
                version = version.value,
                locatorHashes = locatorHashes,
                hashStop = hashStop
            )
        }
    }
}