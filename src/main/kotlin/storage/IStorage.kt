package storage

interface IStorage {
    fun insertData(data: ByteArray, key: ByteArray)
    fun retrieveData(key: ByteArray): ByteArray?
    fun removeData(key: ByteArray)
}