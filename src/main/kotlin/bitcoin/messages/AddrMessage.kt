package bitcoin.messages

import bitcoin.messages.components.NetworkAddress
import bitcoin.messages.components.VariableInt

data class AddrMessage(
    val entries: List<NetworkAddress>
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateMessageSize())
        var currentOffset = 0

        val numItems = VariableInt(entries.size.toLong())
        currentOffset = numItems.intoByteArray(array, currentOffset)
        for (entry in entries) {
            currentOffset = entry.intoByteArray(array, currentOffset, includeTime = true)
        }
        return array
    }

    private fun calculateMessageSize(): Int {
        val numItems = VariableInt(entries.size.toLong())
        var size = numItems.calculateMessageSize()
        for (entry in entries) {
            size += entry.calculateMessageSize(true)
        }
        return size
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): AddrMessage {
            val size = VariableInt.fromByteArray(buffer, 0)
            var currentOffset = size.nextIndex
            val addressList = mutableListOf<NetworkAddress>()
            for (i in 1..size.value.value) {
                val item = NetworkAddress.fromByteArray(buffer, currentOffset, true)
                currentOffset += item.calculateMessageSize(includeTime = true)
                addressList.add(item)
            }

            return AddrMessage(
                entries = addressList
            )
        }
    }
}