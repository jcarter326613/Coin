package bitcoin.messages

import bitcoin.messages.components.VariableInt
import bitcoin.messages.components.VariableString
import util.ByteManipulation

class RejectMessage(
    val message: String,
    val code: Code,
    val reason: String,
    val data: ByteArray
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateMessageSize())
        var currentOffset = 0

        currentOffset = VariableString(message).intoByteArray(array, currentOffset)
        array[currentOffset] = code.value
        currentOffset++
        currentOffset = VariableString(reason).intoByteArray(array, currentOffset)
        data.copyInto(array, currentOffset)

        return array
    }

    fun calculateMessageSize(): Int {
        return 1 + data.size +
                VariableString(message).calculateMessageSize() +
                VariableString(reason).calculateMessageSize()
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): RejectMessage {
            val message = VariableString.fromByteArray(buffer, 0)
            val code = Code.fromValue(buffer[message.nextIndex])
            val reason = VariableString.fromByteArray(buffer, message.nextIndex + 1)
            val data = ByteArray(buffer.size - reason.nextIndex)
            buffer.copyInto(data, 0, reason.nextIndex)

            return RejectMessage(
                message = message.value.s,
                code = code,
                reason = reason.value.s,
                data = data
            )
        }
    }

    enum class Code(val value: Byte) {
        Malformed(0x01),
        Invalid(0x10),
        Obsolete(0x11),
        Duplicate(0x12),
        NonStandard(0x40),
        Dust(0x41),
        InsufficientFee(0x42),
        CheckPoint(0x43);

        companion object {
            fun fromValue(value: Byte): Code = values().first { it.value == value }
        }
    }
}