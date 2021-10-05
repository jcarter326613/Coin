package bitcoin.chain

import bitcoin.messages.TxMessage

class Transaction(
    val message: TxMessage,
    val sourceBlockHash: ByteArray
) {
}