package bitcoin.messages

import bitcoin.messages.components.VariableInt
import util.ByteManipulation

class TxMessage(
    val version: Int,
    val inputs: List<TxIn>,
    val outputs: List<TxOut>,
    val locktime: Int
) {
    fun toByteArray(): ByteArray {
        val array = ByteArray(calculateMessageSize())
        var currentOffset = 0

        currentOffset = ByteManipulation.writeIntToArray(version, array, currentOffset)
        currentOffset = VariableInt(inputs.size.toLong()).intoByteArray(array, currentOffset)
        for (input in inputs) {
            currentOffset = input.intoByteArray(array, currentOffset)
        }
        currentOffset = VariableInt(outputs.size.toLong()).intoByteArray(array, currentOffset)
        for (output in outputs) {
            currentOffset = output.intoByteArray(array, currentOffset)
        }
        ByteManipulation.writeIntToArray(locktime, array, currentOffset)

        return array
    }

    fun calculateMessageSize(): Int {
        var size = 8

        size += VariableInt(inputs.size.toLong()).calculateMessageSize()
        for (input in inputs) {
            size += input.calculateMessageSize()
        }
        size += VariableInt(outputs.size.toLong()).calculateMessageSize()
        for (output in outputs) {
            size += output.calculateMessageSize()
        }

        return size
    }

    companion object {
        fun fromByteArray(buffer: ByteArray): TxMessage {
            val version = ByteManipulation.readIntFromArray(buffer, 0)

            val txInList = mutableListOf<TxIn>()
            val numTxIn = VariableInt.fromByteArray(buffer, version.nextIndex)
            var currentOffset = numTxIn.nextIndex
            for (i in 1..numTxIn.value.value) {
                val txInPair = TxIn.fromByteArray(buffer, currentOffset)
                txInList.add(txInPair.value)
                currentOffset = txInPair.index
            }

            val txOutList = mutableListOf<TxOut>()
            val numTxOut = VariableInt.fromByteArray(buffer, currentOffset)
            currentOffset = numTxOut.nextIndex
            for (i in 1..numTxOut.value.value) {
                val txOutPair = TxOut.fromByteArray(buffer, currentOffset)
                txOutList.add(txOutPair.value)
                currentOffset = txOutPair.index
            }

            val locktime = ByteManipulation.readIntFromArray(buffer, currentOffset)

            return TxMessage(
                version = version.value,
                inputs = txInList,
                outputs = txOutList,
                locktime = locktime.value
            )
        }
    }

    class TxIn(
        val outPoint: OutPoint,
        val signatureScript: ByteArray,
        val sequence: Int
    ) {
        fun intoByteArray(array: ByteArray, destIndex: Int): Int {
            var currentOffset = destIndex
            currentOffset = outPoint.intoByteArray(array, currentOffset)
            currentOffset = VariableInt(signatureScript.size.toLong()).intoByteArray(array, currentOffset)
            signatureScript.copyInto(array, currentOffset)
            currentOffset += signatureScript.size
            currentOffset = ByteManipulation.writeIntToArray(sequence, array, currentOffset)
            return currentOffset
        }

        fun calculateMessageSize(): Int {
            return outPoint.calculateMessageSize() +
                    VariableInt(signatureScript.size.toLong()).calculateMessageSize() +
                    signatureScript.size +
                    4
        }

        companion object {
            fun fromByteArray(buffer: ByteArray, startIndex: Int): TxInPair {
                val previousOutput = OutPoint.fromByteArray(buffer, startIndex)
                val scriptLength = VariableInt.fromByteArray(buffer, previousOutput.index)
                val script = ByteArray(scriptLength.value.value.toInt())
                val scriptEndIndex = scriptLength.nextIndex + script.size
                buffer.copyInto(script, 0, scriptLength.nextIndex, scriptEndIndex)
                val sequence = ByteManipulation.readIntFromArray(buffer, scriptEndIndex)

                return TxInPair(
                    TxIn(previousOutput.value, script, sequence.value),
                    sequence.nextIndex
                )
            }
        }

        class TxInPair(
            val value: TxIn,
            val index: Int
        )
    }

    class OutPoint(
        val hash: ByteArray,
        val index: Int
    ) {
        fun intoByteArray(array: ByteArray, destIndex: Int): Int {
            hash.copyInto(array, destIndex)
            return ByteManipulation.writeIntToArray(index, array, destIndex + hash.size)
        }

        fun calculateMessageSize(): Int = 32 + 4

        companion object {
            fun fromByteArray(buffer: ByteArray, startIndex: Int): OutPointPair {
                val hash = ByteArray(32)
                buffer.copyInto(hash, 0, startIndex, startIndex + 32)
                val index = ByteManipulation.readIntFromArray(buffer, startIndex + 32)

                return OutPointPair(
                    OutPoint(hash, index.value),
                    index.nextIndex
                )
            }
        }

        class OutPointPair(
            val value: OutPoint,
            val index: Int
        )
    }

    class TxOut(
        val value: Long,
        val script: ByteArray
    ) {
        fun intoByteArray(array: ByteArray, destIndex: Int): Int {
            var currentOffset = destIndex
            currentOffset = ByteManipulation.writeLongToArray(value, array, currentOffset)
            currentOffset = VariableInt(script.size.toLong()).intoByteArray(array, currentOffset)
            script.copyInto(array, currentOffset)
            return currentOffset + script.size
        }

        fun calculateMessageSize(): Int {
            return VariableInt(script.size.toLong()).calculateMessageSize() +
                    script.size + 8
        }

        companion object {
            fun fromByteArray(buffer: ByteArray, startIndex: Int): TxOutPair {
                val value = ByteManipulation.readLongFromArray(buffer, startIndex)
                val scriptLength = VariableInt.fromByteArray(buffer, value.nextIndex)
                val script = ByteArray(scriptLength.value.value.toInt())
                val scriptEndIndex = scriptLength.nextIndex + scriptLength.value.value.toInt()
                buffer.copyInto(script, 0, scriptLength.nextIndex, scriptEndIndex)

                return TxOutPair(
                    TxOut(value.value, script),
                    scriptEndIndex
                )
            }
        }

        class TxOutPair(
            val value: TxOut,
            val index: Int
        )
    }
}