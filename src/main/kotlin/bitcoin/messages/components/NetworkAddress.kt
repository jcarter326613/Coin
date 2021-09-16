package bitcoin.messages.components

import java.nio.ByteBuffer

data class NetworkAddress(
    val address: String,
    val includeTime: Boolean
) {
    fun intoByteArray(dest: ByteArray, destIndex: Int): Int {

    }

    fun calculateMessageSize(): Int {
        
    }

    companion object {
        fun fromByteArray(buffer: ByteArray, startIndex: Int): NetworkAddress {
            ByteBuffer.allocate(4).put(buffer.slice(0 until 4).toByteArray()).int
        }
    }
}