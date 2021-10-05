package bitcoin.chain

import bitcoin.messages.BlockMessageTest
import org.junit.jupiter.api.Test
import storage.IStorage

class BlockDbTest {
    @Test
    fun locatorHash_Normal_Success() {
        val blockDb = BlockDb(NullStorage())
        val blockList = createRandomChain(1000, blockDb)

        val locatorHashes = blockDb.locatorHashes

        assert(locatorHashes[0].contentEquals(blockList[blockList.size - 1].hash))
        assert(locatorHashes[1].contentEquals(blockList[blockList.size - 2].hash))
        assert(locatorHashes[10].contentEquals(blockList[blockList.size - 12].hash))
        assert(locatorHashes[11].contentEquals(blockList[blockList.size - 16].hash))
        assert(locatorHashes[12].contentEquals(blockList[blockList.size - 24].hash))
    }

    @Test
    fun addBlock_PreviousNotHead_Success() {
        val blockDb = BlockDb(NullStorage())

        assert(blockDb.addBlock(Block(BlockMessageTest.createRandomBlock())))
        assert(!blockDb.addBlock(Block(BlockMessageTest.createRandomBlock())))
    }

    @Test
    fun addBlock_WithRemoveAndReAdd_Success() {
        val blockDb = BlockDb(NullStorage())
        val blockList = createRandomChain(10, null)

        for (i in 0 until 10) {
            assert(blockDb.addBlock(blockList[i]))
        }
        blockDb.removeBlock(blockList[4])
        for (i in 4 until 10) {
            assert(blockDb.addBlock(blockList[i]))
        }
    }

    @Test
    fun addBlock_WithRemoveAndAddNew_Success() {
        val blockDb = BlockDb(NullStorage())
        val block1 = Block(BlockMessageTest.createRandomBlock())
        val block2 = Block(BlockMessageTest.createRandomBlock(previousHash = block1.hash))
        val block3 = Block(BlockMessageTest.createRandomBlock(previousHash = block1.hash))

        assert(blockDb.addBlock(block1))
        assert(blockDb.addBlock(block2))
        blockDb.removeBlock(block2)
        assert(blockDb.addBlock(block3))
    }

    @Test
    fun addBlock_NewByteArrayAddress_Success() {
        val blockDb = BlockDb(NullStorage())
        val blockMessage1 = BlockMessageTest.createRandomBlock()
        val block1 = Block(blockMessage1)
        val blockMessage1HashCopy = ByteArray(block1.hash.size)
        block1.hash.copyInto(blockMessage1HashCopy)

        val blockMessage2 = BlockMessageTest.createRandomBlock(previousHash = blockMessage1HashCopy)
        val block2 = Block(blockMessage2)

        assert(blockDb.addBlock(block1))
        assert(blockDb.addBlock(block2))

        assert(blockDb.lastBlockHeight == 2)
    }

    private fun createRandomChain(numBlocks: Int, blockDb: BlockDb?): List<Block> {
        val blockList = mutableListOf<Block>()
        var previousHash: ByteArray? = null
        for (i in 1..numBlocks) {
            val newBlock = Block(BlockMessageTest.createRandomBlock(previousHash = previousHash))
            previousHash = newBlock.hash
            blockList.add(newBlock)
            blockDb?.addBlock(newBlock)
        }

        return blockList
    }

    private class NullStorage: IStorage {
        override fun insertData(data: ByteArray, key: ByteArray) {
        }
        override fun retrieveData(key: ByteArray) {
        }
    }
}