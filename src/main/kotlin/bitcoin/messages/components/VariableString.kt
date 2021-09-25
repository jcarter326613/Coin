package bitcoin.messages.components

data class VariableString (
    val s: String
) {
    fun intoByteArray(dest: ByteArray, destIndex: Int): Int {
        val length = VariableInt(s.length.toLong())
        val nextIndex = length.intoByteArray(dest, destIndex)
        s.toByteArray().copyInto(dest, nextIndex)
        return nextIndex + s.length
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