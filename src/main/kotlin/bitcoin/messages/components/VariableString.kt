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
        fun fromByteArray(buffer: ByteArray, startIndex: Int): ValueIndexPair {
            val varIntPair = VariableInt.fromByteArray(buffer, startIndex)
            val length = varIntPair.value

            if (length.value >= Int.MAX_VALUE || length.value < 0) {
                throw Exception("Invalid string length ${length.value}")
            }

            val s = String(buffer.slice(varIntPair.nextIndex until (varIntPair.nextIndex + length.value.toInt())).toByteArray())
            return ValueIndexPair(VariableString(s), varIntPair.nextIndex + length.value.toInt())
        }
    }

    data class ValueIndexPair(
        val value: VariableString,
        val nextIndex: Int
    )
}