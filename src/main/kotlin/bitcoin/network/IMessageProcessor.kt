package bitcoin.network

import bitcoin.messages.*

interface IMessageProcessor {
    fun processIncomingMessageVersion(payload: VersionMessage, connection: Connection)
    fun processIncomingMessageAddr(payload: AddrMessage, connection: Connection)
    fun processIncomingMessageInv(payload: InvMessage, connection: Connection)
    fun processIncomingMessageReject(payload: RejectMessage, connection: Connection)
    fun processIncomingMessageBlock(payload: BlockMessage, connection: Connection)
}
