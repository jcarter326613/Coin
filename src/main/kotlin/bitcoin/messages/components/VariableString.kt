package bitcoin.messages.components

import java.nio.ByteBuffer

data class VariableString (
    val s: String
) {
    fun intoByteArray(dest: ByteArray, destIndex: Int): Int {

    }

    fun calculateMessageSize(): Int = VariableInt(s.length.toLong()).calculateMessageSize() + s.length

    companion object {
        fun fromByteArray(buffer: ByteArray, startIndex: Int): VariableString {
            val length = VariableInt.fromByteArray(buffer, startIndex)
            val lengthMessageSize = length.calculateMessageSize()

            if (length.value >= Int.MAX_VALUE || length.value < 0) {
                throw Exception("Invalid string length ${length.value}")
            }

            val s = String(buffer.slice((startIndex + lengthMessageSize) until (startIndex + lengthMessageSize + length.value.toInt())).toByteArray())
            return VariableString(s)
        }
    }
}