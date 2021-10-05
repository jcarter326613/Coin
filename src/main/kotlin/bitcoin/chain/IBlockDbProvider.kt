package bitcoin.chain

interface IBlockDbProvider {
    fun createBlockDb(): BlockDb
}