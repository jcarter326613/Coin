package bitcoin.chain

import bitcoin.messages.TxMessage
import storage.IStorage

class TransactionDb(private val storageController: IStorage) {
    fun removeSources(transaction: TxMessage) {
        for (input in transaction.inputs) {
            val key = "${input.outPoint.hash}:${input.outPoint.index}"
            storageController.insertData(input.outPoint.toByteArray(), key)
        }
    }

    fun addOutputs(transaction: TxMessage) {
        for (output in transaction.outputs) {
            val key = "${input.outPoint.hash}:${input.outPoint.index}"
            storageController.insertData(output.toByteArray(), key)
        }
    }

    fun getTransaction(blockHash: ByteArray, index: Int) {

    }
}